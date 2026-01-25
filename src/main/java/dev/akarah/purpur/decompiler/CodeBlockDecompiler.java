package dev.akarah.purpur.decompiler;

import com.google.common.collect.Lists;
import dev.akarah.purpur.ast.AST;
import dev.akarah.purpur.mappings.MappingsRepository;
import dev.dfonline.flint.templates.CodeBlock;
import dev.dfonline.flint.templates.codeblock.*;
import dev.dfonline.flint.templates.codeblock.Process;
import dev.dfonline.flint.templates.codeblock.abstracts.CodeBlockAction;

import java.util.List;
import java.util.Optional;

public class CodeBlockDecompiler {
    public static AST.Invoke decompile(BracketManager.BracketDraft draft) {
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

    public static AST.Invoke decompile(CodeBlock codeBlock) {
        switch (codeBlock) {
            case Function function -> {
                return new AST.Invoke(
                        new AST.Value.Variable("func." + function.getFunctionName(), "line"),
                        function.getArguments().getOrderedList()
                                .stream()
                                .map(VarItemDecompiler::decompile)
                                .toList(),
                        Optional.empty()
                );
            }
            case Process process -> {
                return new AST.Invoke(
                        new AST.Value.Variable("proc." + process.getProcessName(), "line"),
                        process.getArguments().getOrderedList()
                                .stream()
                                .map(VarItemDecompiler::decompile)
                                .toList(),
                        Optional.empty()
                );
            }
            case CallFunction callFunction -> {
                return new AST.Invoke(
                        new AST.Value.Variable("func." + callFunction.getData(), "line"),
                        callFunction.getArguments().getOrderedList()
                                .stream()
                                .map(VarItemDecompiler::decompile)
                                .toList(),
                        Optional.empty()
                );
            }
            case StartProcess startProcess -> {
                return new AST.Invoke(
                        new AST.Value.Variable("proc." + startProcess.getData(), "line"),
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
                return new AST.Invoke(
                        new AST.Value.Variable(scriptName.name(), "line"),
                        List.of(),
                        Optional.empty()
                );
            }
            case EntityEvent entityEvent -> {
                var dfName = new MappingsRepository.DfFunction("ENTITY EVENT", entityEvent.getAction());
                var scriptName = MappingsRepository.get().getScriptFunction(dfName);
                return new AST.Invoke(
                        new AST.Value.Variable(scriptName.name(), "line"),
                        List.of(),
                        Optional.empty()
                );
            }
            case CodeBlockAction action -> {
                var dfName = new MappingsRepository.DfFunction(idToFancyName(action.getBlock()), action.getAction());
                var scriptName = MappingsRepository.get().getScriptFunction(dfName);
                return new AST.Invoke(
                        new AST.Value.Variable(scriptName.name(), "line"),
                        action.getArguments().getOrderedList()
                                .stream()
                                .map(VarItemDecompiler::decompile)
                                .toList(),
                        Optional.empty()
                );
            }
            case Else elseBlock -> {
                return new AST.Invoke(
                        new AST.Value.Variable("else", "line"),
                        List.of(),
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
}
