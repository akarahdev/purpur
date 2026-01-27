package dev.akarah.purpur.decompiler;

import com.google.common.collect.Lists;
import dev.akarah.purpur.mappings.MappingsRepository;
import dev.akarah.purpur.parser.ast.Block;
import dev.akarah.purpur.parser.ast.stmt.Invoke;
import dev.akarah.purpur.parser.ast.value.TagLiteral;
import dev.akarah.purpur.parser.ast.value.Variable;
import dev.dfonline.flint.actiondump.codeblocks.ActionType;
import dev.dfonline.flint.actiondump.codeblocks.BlockTagType;
import dev.dfonline.flint.templates.CodeBlock;
import dev.dfonline.flint.templates.argument.TagArgument;
import dev.dfonline.flint.templates.codeblock.*;
import dev.dfonline.flint.templates.codeblock.Process;
import dev.dfonline.flint.templates.codeblock.abstracts.CodeBlockAction;
import dev.dfonline.flint.templates.codeblock.abstracts.CodeBlockSubAction;

import java.util.List;
import java.util.Optional;

public class CodeBlockDecompiler {
    public static Invoke decompile(BracketManager.BracketDraft draft) {
        var invoke = decompile(draft.codeBlock);

        if(draft.children != null) {
            var block = new Block(Lists.newArrayList());
            for(var child : draft.children) {
                block.statements().add(decompile(child));
            }
            invoke = invoke.withChildBlock(block);
        }

        return invoke;
    }

    public static Invoke decompile(CodeBlock codeBlock) {
        switch (codeBlock) {
            case Function function -> {
                return new Invoke(
                        new Variable("func." + function.getFunctionName(), "line", null),
                        Optional.empty(),
                        function.getArguments().getOrderedList()
                                .stream()
                                .map(VarItemDecompiler::decompile)
                                .toList(),
                        Optional.empty()
                );
            }
            case Process process -> {
                return new Invoke(
                        new Variable("proc." + process.getProcessName(), "line", null),
                        Optional.empty(),
                        process.getArguments().getOrderedList()
                                .stream()
                                .map(VarItemDecompiler::decompile)
                                .toList(),
                        Optional.empty()
                );
            }
            case CallFunction callFunction -> {
                return new Invoke(
                        new Variable("func." + callFunction.getData(), "line", null),
                        Optional.empty(),
                        callFunction.getArguments().getOrderedList()
                                .stream()
                                .map(VarItemDecompiler::decompile)
                                .toList(),
                        Optional.empty()
                );
            }
            case StartProcess startProcess -> {
                return new Invoke(
                        new Variable("proc." + startProcess.getData(), "line", null),
                        Optional.empty(),
                        startProcess.getArguments().getOrderedList()
                                .stream()
                                .map(VarItemDecompiler::decompile)
                                .toList(),
                        Optional.empty()
                );
            }
            case PlayerEvent playerEvent -> {
                var dfName = new MappingsRepository.DfFunction("PLAYER EVENT", playerEvent.getAction());
                var scriptName = MappingsRepository.get().getScriptFunction(dfName);
                return new Invoke(
                        new Variable(scriptName.name(), "line", null),
                        Optional.empty(),
                        List.of(),
                        Optional.empty()
                );
            }
            case EntityEvent entityEvent -> {
                var dfName = new MappingsRepository.DfFunction("ENTITY EVENT", entityEvent.getAction());
                var scriptName = MappingsRepository.get().getScriptFunction(dfName);
                return new Invoke(
                        new Variable(scriptName.name(), "line", null),
                        Optional.empty(),
                        List.of(),
                        Optional.empty()
                );
            }
            case GameEvent entityEvent -> {
                var dfName = new MappingsRepository.DfFunction("GAME EVENT", entityEvent.getAction());
                var scriptName = MappingsRepository.get().getScriptFunction(dfName);
                return new Invoke(
                        new Variable(scriptName.name(), "line", null),
                        Optional.empty(),
                        List.of(),
                        Optional.empty()
                );
            }
            case CodeBlockSubAction action -> {
                return decompileSubAction(action);
            }
            case CodeBlockAction action -> {
                return decompileAction(action);
            }
            case Else elseBlock -> {
                return new Invoke(
                        new Variable("else", "line", null),
                        Optional.empty(),
                        List.of(),
                        Optional.empty()
                );
            }
            default -> {
                return new Invoke(
                        new Variable("?", "line", null),
                        Optional.empty(),
                        List.of(),
                        Optional.empty()
                );
            }
        }
    }

    private static MappingsRepository.DfFunction createDfFunction(CodeBlockAction action) {
        return new MappingsRepository.DfFunction(MappingsRepository.idToFancyName(action.getBlock()), action.getAction());
    }

    private static List<dev.akarah.purpur.parser.ast.value.Value> mapArguments(CodeBlockAction action) {
        return action.getArguments().getOrderedList()
                .stream()
                .filter(argument -> {
                    if(!(argument instanceof TagArgument tagArgument)) {
                        return true;
                    }
                    var scriptCodeBlockName = MappingsRepository.get().getScriptFunction(new MappingsRepository.DfFunction(
                            MappingsRepository.idToFancyName(action.getBlock()),
                            action.getAction()
                    ));
                    for(var tagType : MappingsRepository.get().getActionType(scriptCodeBlockName.name()).tags()) {
                        System.out.println(tagType.name() + " vs. " + tagArgument.getTag());
                        System.out.println(tagType.defaultOption() + " vs. " + tagArgument.getOption());

                        if(tagType.name().equals(tagArgument.getTag()) && tagType.defaultOption().equals(tagArgument.getOption())) {
                            return false;
                        }
                    }
                    return true;
                })
                .map(VarItemDecompiler::decompile)
                .toList();
    }

    public static Invoke decompileAction(CodeBlockAction action) {
        var dfName = createDfFunction(action);
        var scriptName = MappingsRepository.get().getScriptFunction(dfName);
        return new Invoke(
                new Variable(scriptName.name(), "line", null),
                Optional.empty(),
                mapArguments(action),
                Optional.empty()
        );
    }

    public static Invoke decompileSubAction(CodeBlockSubAction action) {
        var dfName = createDfFunction(action);
        ActionType dfSubAction = null;
        for (var subActionBlock : MappingsRepository.get().dfSubActionsToActionTypeMap().entrySet()) {
            if (subActionBlock.getKey().equals(action.getSubAction())) {
                dfSubAction = subActionBlock.getValue();
            }
        }
        for (var subActionBlock : MappingsRepository.get().allActionTypes()) {
            if (subActionBlock.name().equals(action.getSubAction())) {
                dfSubAction = subActionBlock;
            }
        }
        System.out.println(dfSubAction);
        MappingsRepository.ScriptFunction subActionFunction;
        if (dfSubAction != null) {
            subActionFunction = MappingsRepository.get().getScriptFunction(new MappingsRepository.DfFunction(
                    dfSubAction.codeblockName(), dfSubAction.name()
            ));
        } else {
            subActionFunction = null;
        }
        var scriptName = MappingsRepository.get().getScriptFunction(dfName);
        return new Invoke(
                new Variable(scriptName.name(), "line", null),
                Optional.ofNullable(dfSubAction)
                        .map(dfActionType -> new Variable(subActionFunction.name(), "line", null)),
                mapArguments(action),
                Optional.empty()
        );
    }
}
