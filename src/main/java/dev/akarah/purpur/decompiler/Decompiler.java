package dev.akarah.purpur.decompiler;

import com.google.common.collect.Lists;
import dev.akarah.purpur.ast.AST;
import dev.akarah.purpur.mappings.MappingsRepository;
import dev.dfonline.flint.templates.CodeBlock;
import dev.dfonline.flint.templates.Template;
import dev.dfonline.flint.templates.argument.NumberArgument;
import dev.dfonline.flint.templates.argument.abstracts.Argument;
import dev.dfonline.flint.templates.codeblock.*;
import dev.dfonline.flint.templates.codeblock.abstracts.CodeBlockAction;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Decompiler {
    public static String idToFancyName(String id) {
        return switch (id) {
            case "player_action" -> "PLAYER ACTION";
            case "if_var" -> "IF VARIABLE";
            case "entity_action" -> "ENTITY ACTION";
            case "if_entity" -> "IF ENTITY";
            case "if_player" -> "IF PLAYER";
            case "game_action" -> "GAME ACTION";
            case "if_game" -> "IF GAME";
            case "set_var" -> "SET VARIABLE";
            case "control" -> "CONTROL";
            case "else" -> "ELSE";
            case "repeat" -> "REPEAT";
            case "call_func" -> "CALL FUNCTION";
            case "start_process" -> "START PROCESS";
            case "func" -> "FUNCTION";
            case "process" -> "PROCESS";
            case "event" -> "PLAYER EVENT";
            case "entity_event" -> "ENTITY EVENT";
            case "select_obj" -> "SELECT OBJECT";
            default -> null;
        };
    }

    public static class BracketDraft {
        CodeBlock codeBlock;
        @Nullable List<BracketDraft> children;

        public BracketDraft(CodeBlock block, @Nullable List<BracketDraft> children) {
            this.codeBlock = block;
            this.children = children;
        }
    }

    public static BracketDraft makeDraft(Template template) {
        var head = template.getBlocks().getBlocks().getFirst();
        var remainder = template.getBlocks().getBlocks().subList(1, template.getBlocks().getBlocks().size());

        return new BracketDraft(
                head,
                Decompiler.makeDrafts(remainder)
        );
    }

    public static List<BracketDraft> makeDrafts(List<CodeBlock> blocks) {
        List<BracketDraft> drafts = Lists.newArrayList();
        int idx = 0;
        BracketDraft lastDraft = null;
        while (idx < blocks.size()) {
            var block = blocks.get(idx);
            if (Objects.requireNonNull(block) instanceof Bracket bracket && bracket.getDirection() == Bracket.Direction.OPEN) {
                int depth = 0;
                List<CodeBlock> children = Lists.newArrayList();
                while (idx < blocks.size()) {
                    var bracket2 = blocks.get(idx);
                    if (bracket2 instanceof Bracket b && b.getDirection() == Bracket.Direction.OPEN) {
                        depth += 1;
                    } else if (bracket2 instanceof Bracket b && b.getDirection() == Bracket.Direction.CLOSE) {
                        depth -= 1;
                    }

                    if (depth > 0) {
                        if (depth == 1 && bracket2 instanceof Bracket b && b.getDirection() == Bracket.Direction.OPEN) {
                            // Don't add the first open bracket to children
                        } else {
                            children.add(bracket2);
                        }
                    }

                    idx++;
                    if (depth == 0) {
                        break;
                    }
                }
                if (lastDraft != null) {
                    lastDraft.children = makeDrafts(children);
                }
                continue; // idx is already incremented
            } else {
                var draft = new BracketDraft(block, null);
                drafts.add(draft);
                lastDraft = draft;
            }
            idx++;
        }
        return drafts;
    }

    public static AST.Invoke decompile(BracketDraft draft) {
        var invoke = decompile(draft.codeBlock);

        if(draft.children != null) {
            var block = new AST.Block(Lists.newArrayList());
            for(var child : draft.children) {
                block.statements().add(decompile(child));
            }
            invoke = invoke.withChildBlock(block);
        }

        return invoke;
    }

    public static AST.Invoke decompile(CodeBlock codeBlock) {
        switch (codeBlock) {
            case CodeBlockAction action -> {
                var dfName = new MappingsRepository.DfName(idToFancyName(action.getBlock()), action.getAction());
                var scriptName = MappingsRepository.get().getScriptName(dfName);
                return new AST.Invoke(
                        new AST.Value.Variable(scriptName.name(), "line"),
                        action.getArguments().getOrderedList()
                                .stream()
                                .map(Decompiler::decompile)
                                .toList(),
                        Optional.empty()
                );
            }
            default -> {
                return new AST.Invoke(
                        new AST.Value.Variable("?", "line"),
                        List.of(),
                        Optional.empty()
                );
            }
        }
    }

    public static AST.Value decompile(Argument argument) {
        return switch (argument) {
            case NumberArgument numberArgument -> new AST.Value.Number(numberArgument.getNumber());
            default -> new AST.Value.UnknownVarItem();
        };
    }
}
