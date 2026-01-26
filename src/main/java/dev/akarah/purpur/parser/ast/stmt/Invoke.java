package dev.akarah.purpur.parser.ast.stmt;

import com.google.common.collect.Lists;
import dev.akarah.purpur.mappings.MappingsRepository;
import dev.akarah.purpur.misc.SpanData;
import dev.akarah.purpur.misc.SpannedException;
import dev.akarah.purpur.parser.CodegenContext;
import dev.akarah.purpur.parser.ast.Block;
import dev.akarah.purpur.parser.ast.value.TagLiteral;
import dev.akarah.purpur.parser.ast.value.Value;
import dev.akarah.purpur.parser.ast.value.Variable;
import dev.dfonline.flint.actiondump.codeblocks.ActionType;
import dev.dfonline.flint.templates.Arguments;
import dev.dfonline.flint.templates.argument.TagArgument;
import dev.dfonline.flint.templates.codeblock.*;
import dev.dfonline.flint.templates.codeblock.Process;
import dev.dfonline.flint.templates.codeblock.target.EntityTarget;
import dev.dfonline.flint.templates.codeblock.target.PlayerTarget;

import java.util.List;
import java.util.Optional;

public record Invoke(
        Variable invoking,
        Optional<Variable> subInvoking,
        List<Value> arguments,
        Optional<Block> childBlock
) implements Statement {
    public Invoke withChildBlock(Block block) {
        return new Invoke(invoking, subInvoking, arguments, Optional.of(block));
    }

    @Override
    public void lowerToParsable(StringBuilder builder, int depth) {

        invoking.lowerToParsable(builder, depth);

        subInvoking.ifPresent(subInvoking -> {
            builder.append("[");
            subInvoking.lowerToParsable(builder, depth);
            builder.append("]");
        });


        builder.append("(");

        var msb = new StringBuilder();
        int idx = 0;
        for (var arg : arguments) {
            arg.lowerToParsable(msb, depth);
            idx += 1;
            if (idx != arguments.size()) {
                msb.append(", ");
            }
        }

        var msbOut = msb.toString();
        if (msbOut.length() > 80) {
            var nsb = new StringBuilder();
            int idx2 = 0;
            for (var arg : arguments) {
                nsb.append("\n").append(" ".repeat(depth + TAB_SPACES_LENGTH));

                arg.lowerToParsable(nsb, depth);

                if (idx2 != arguments.size() - 1) {
                    nsb.append(",");
                }

                idx2 += 1;
            }
            builder.append(nsb).append("\n");
            builder.append(" ".repeat(depth));
        } else {
            builder.append(msbOut);
        }

        builder.append(")");

        this.childBlock.ifPresent(
                cbs -> {
                    builder.append(" ");
                    cbs.lowerToParsable(builder, depth);
                }
        );
    }

    @Override
    public void buildTemplate(CodegenContext ctx) {
        // else needs special logic
        if(this.invoking.name().equals("else")) {
            ctx.codeBlocks().add(new Else());
            ctx.codeBlocks().add(new Bracket(Bracket.Type.NORMAL, Bracket.Direction.OPEN));
            this.childBlock.ifPresent(childBlock -> childBlock.statements().forEach(childStatement -> childStatement.buildTemplate(ctx)));
            ctx.codeBlocks().add(new Bracket(Bracket.Type.NORMAL, Bracket.Direction.CLOSE));
        }
        var lookupId = new MappingsRepository.ScriptFunction(this.invoking.name());
        if(lookupId.name().startsWith("proc.")) {
            if(childBlock.isPresent()) {
                lookupId = new MappingsRepository.ScriptFunction("process.dynamic");
            } else {
                lookupId = new MappingsRepository.ScriptFunction("startProcess.dynamic");
            }
        }
        if(lookupId.name().startsWith("func.")) {
            if(childBlock.isPresent()) {
                lookupId = new MappingsRepository.ScriptFunction("function.dynamic");
            } else {
                lookupId = new MappingsRepository.ScriptFunction("callFunction.dynamic");
            }
        }
        var actionType = MappingsRepository.get().getActionType(lookupId.name());
        if (actionType == null) {
            return;
        }

        ActionType blockTagActionType;
        if(this.subInvoking.isPresent()) {
            blockTagActionType = MappingsRepository.get().getActionType(
                    new MappingsRepository.ScriptFunction(this.subInvoking.orElseThrow().name())
                            .name()
            );
        } else {
            blockTagActionType = actionType;
        }

        var arguments = new Arguments();
        int idx = 0;
        var tagsWritten = Lists.newArrayList();
        for (var arg : this.arguments) {
            if(arg == null) {
                continue;
            }
            if (arg instanceof TagLiteral(String tag, String option, SpanData spanData)) {
                var dfTag = MappingsRepository.get().getDfTag(new MappingsRepository.ScriptBlockTag(tag, option));
                if (dfTag == null) {
                    ctx.errors().add(new SpannedException(
                            tag + "." + option + " is not a valid block tag for this object",
                            spanData
                    ));
                    continue;
                }
                tagsWritten.add(dfTag.tag());
            }
            var createdArg = arg.createArgument(ctx, blockTagActionType, idx);
            if(createdArg != null) arguments.add(createdArg);
            idx += 1;
        }
        for (var tag : actionType.tags()) {
            if (!tagsWritten.contains(tag.name())) {
                arguments.add(new TagArgument(
                        tag.slot(),
                        tag.defaultOption(),
                        tag.name(),
                        blockTagActionType.name(),
                        MappingsRepository.fancyNameToId(blockTagActionType.codeblockName())
                ));
            }
        }

        // player blocks
        if (this.invoking.name().contains("player.")) {
            ctx.codeBlocks().add(new PlayerAction(
                    actionType.name(),
                    PlayerTarget.NONE
            ).setArguments(arguments));
        }
        if (this.invoking.name().contains("playerEvent.")) {
            ctx.codeBlocks().add(new PlayerEvent(
                    actionType.name(),
                    false
            ));
        }
        if (this.invoking.name().contains("ifPlayer.")) {
            ctx.codeBlocks().add(new IfPlayer(
                    actionType.name(),
                    PlayerTarget.NONE,
                    false
            ).setArguments(arguments));
        }

        // entity blocks
        if (this.invoking.name().contains("entity.")) {
            ctx.codeBlocks().add(new EntityAction(
                    EntityTarget.NONE,
                    actionType.name()
            ).setArguments(arguments));
        }
        if (this.invoking.name().contains("entityEvent.")) {
            ctx.codeBlocks().add(new EntityEvent(
                    actionType.name(),
                    false
            ));
        }
        if (this.invoking.name().contains("ifEntity.")) {
            ctx.codeBlocks().add(new IfEntity(
                    actionType.name(),
                    EntityTarget.NONE,
                    false
            ).setArguments(arguments));
        }

        // game blocks
        if (this.invoking.name().contains("ifGame.")) {
            ctx.codeBlocks().add(new IfGame(
                    actionType.name(),
                    false
            ).setArguments(arguments));
        }
        if (this.invoking.name().contains("game.")) {
            ctx.codeBlocks().add(new GameAction(actionType.name()).setArguments(arguments));
        }

        // vars blocks
        if(this.invoking.name().contains("vars.")) {
            ctx.codeBlocks().add(new SetVariable(actionType.name()).setArguments(arguments));
        }
        if(this.invoking.name().contains("ifVars.")) {
            ctx.codeBlocks().add(new IfVariable(actionType.name(), false).setArguments(arguments));
        }

        // funcs and procs
        if(this.invoking.name().contains("func.")) {
            var newName = this.invoking.name().replaceFirst("func\\.", "");
            if(this.childBlock.isPresent()) {
                ctx.codeBlocks().add(new Function(newName).setArguments(arguments));
            } else {
                ctx.codeBlocks().add(new CallFunction(newName).setArguments(arguments));
            }
        }
        if(this.invoking.name().contains("proc.")) {
            var newName = this.invoking.name().replaceFirst("proc\\.", "");
            if(this.childBlock.isPresent()) {
                ctx.codeBlocks().add(new Process(newName).setArguments(arguments));
            } else {
                ctx.codeBlocks().add(new StartProcess(newName).setArguments(arguments));
            }
        }

        // control flow blocks
        if (this.invoking.name().contains("repeat.")) {
            var cb = new Repeat(
                    actionType.name(),
                    "",
                    false
            );
            cb.setArguments(arguments);
            if(this.subInvoking.isPresent()) {
                System.out.println(blockTagActionType);
                var subAction = MappingsRepository.get().actionTypeToDfSubActionsMap().get(blockTagActionType);
                if(subAction == null) {
                    ctx.errors().add(new SpannedException(
                            "Sub-action blocks must have a valid action",
                            this.invoking.spanData()
                    ));
                }
                cb.setSubAction(subAction);
            }
            ctx.codeBlocks().add(cb);
        }
        if (this.invoking.name().contains("select.")) {
            var cb = new SelectObject(
                    actionType.name(),
                    "",
                    false
            );
            cb.setArguments(arguments);
            if(this.subInvoking.isPresent()) {
                System.out.println(blockTagActionType);
                cb.setSubAction(
                        MappingsRepository.get().actionTypeToDfSubActionsMap().get(blockTagActionType)
                );
            }
            ctx.codeBlocks().add(cb);
        }
        if (this.invoking.name().contains("control.")) {
            ctx.codeBlocks().add(new Control(actionType.name()).setArguments(arguments));
        }
        this.childBlock.ifPresent(childBlock -> {
            if (this.invoking.name().startsWith("if")) {
                ctx.codeBlocks().add(new Bracket(Bracket.Type.NORMAL, Bracket.Direction.OPEN));
                childBlock.statements().forEach(childStatement -> childStatement.buildTemplate(ctx));
                ctx.codeBlocks().add(new Bracket(Bracket.Type.NORMAL, Bracket.Direction.CLOSE));
            }
            if (this.invoking.name().startsWith("func.") || this.invoking.name().startsWith("proc.")) {
                childBlock.statements().forEach(childStatement -> childStatement.buildTemplate(ctx));
            }
            if (this.invoking.name().startsWith("repeat")) {
                ctx.codeBlocks().add(new Bracket(Bracket.Type.REPEAT, Bracket.Direction.OPEN));
                childBlock.statements().forEach(childStatement -> childStatement.buildTemplate(ctx));
                ctx.codeBlocks().add(new Bracket(Bracket.Type.REPEAT, Bracket.Direction.CLOSE));
            }
            if (this.invoking.name().startsWith("select")) {
                childBlock.statements().forEach(childStatement -> childStatement.buildTemplate(ctx));
                ctx.codeBlocks().add(new SelectObject("Reset", "", false));
            }
            if (this.invoking.name().startsWith("playerEvent") || this.invoking.name().startsWith("entityEvent") || this.invoking.name().startsWith("gameEvent")) {
                childBlock.statements().forEach(childStatement -> childStatement.buildTemplate(ctx));
            }
        });
    }
}
