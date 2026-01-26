package dev.akarah.purpur.decompiler;

import com.google.common.collect.Lists;
import dev.akarah.purpur.parser.AST;
import dev.akarah.purpur.mappings.MappingsRepository;
import dev.dfonline.flint.templates.CodeBlock;
import dev.dfonline.flint.templates.codeblock.*;
import dev.dfonline.flint.templates.codeblock.Process;
import dev.dfonline.flint.templates.codeblock.abstracts.CodeBlockAction;

import java.util.List;
import java.util.Optional;

public class CodeBlockDecompiler {
    public static AST.Statement.Invoke decompile(BracketManager.BracketDraft draft) {
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

    public static String fancyNameToId(String fancyName) {
        return switch (fancyName) {
            case "PLAYER ACTION" -> "player_action";
            case "IF VARIABLE" -> "if_var";
            case "ENTITY ACTION" -> "entity_action";
            case "IF ENTITY" -> "if_entity";
            case "IF PLAYER" -> "if_player";
            case "GAME ACTION" -> "game_action";
            case "IF GAME" -> "if_game";
            case "SET VARIABLE" -> "set_var";
            case "CONTROL" -> "control";
            case "ELSE" -> "else";
            case "REPEAT" -> "repeat";
            case "CALL FUNCTION" -> "call_func";
            case "START PROCESS" -> "start_process";
            case "FUNCTION" -> "func";
            case "PROCESS" -> "process";
            case "PLAYER EVENT" -> "event";
            case "ENTITY EVENT" -> "entity_event";
            case "SELECT OBJECT" -> "select_obj";
            default -> null;
        };
    }

    public static AST.Statement.Invoke decompile(CodeBlock codeBlock) {
        switch (codeBlock) {
            case Function function -> {
                return new AST.Statement.Invoke(
                        new AST.Value.Variable("func." + function.getFunctionName(), "line"),
                        function.getArguments().getOrderedList()
                                .stream()
                                .map(VarItemDecompiler::decompile)
                                .toList(),
                        Optional.empty()
                );
            }
            case Process process -> {
                return new AST.Statement.Invoke(
                        new AST.Value.Variable("proc." + process.getProcessName(), "line"),
                        process.getArguments().getOrderedList()
                                .stream()
                                .map(VarItemDecompiler::decompile)
                                .toList(),
                        Optional.empty()
                );
            }
            case CallFunction callFunction -> {
                return new AST.Statement.Invoke(
                        new AST.Value.Variable("func." + callFunction.getData(), "line"),
                        callFunction.getArguments().getOrderedList()
                                .stream()
                                .map(VarItemDecompiler::decompile)
                                .toList(),
                        Optional.empty()
                );
            }
            case StartProcess startProcess -> {
                return new AST.Statement.Invoke(
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
                return new AST.Statement.Invoke(
                        new AST.Value.Variable(scriptName.name(), "line"),
                        List.of(),
                        Optional.empty()
                );
            }
            case EntityEvent entityEvent -> {
                var dfName = new MappingsRepository.DfFunction("ENTITY EVENT", entityEvent.getAction());
                var scriptName = MappingsRepository.get().getScriptFunction(dfName);
                return new AST.Statement.Invoke(
                        new AST.Value.Variable(scriptName.name(), "line"),
                        List.of(),
                        Optional.empty()
                );
            }
            case CodeBlockAction action -> {
                var dfName = new MappingsRepository.DfFunction(idToFancyName(action.getBlock()), action.getAction());
                var scriptName = MappingsRepository.get().getScriptFunction(dfName);
                return new AST.Statement.Invoke(
                        new AST.Value.Variable(scriptName.name(), "line"),
                        action.getArguments().getOrderedList()
                                .stream()
                                .map(VarItemDecompiler::decompile)
                                .toList(),
                        Optional.empty()
                );
            }
            case Else elseBlock -> {
                return new AST.Statement.Invoke(
                        new AST.Value.Variable("else", "line"),
                        List.of(),
                        Optional.empty()
                );
            }
            default -> {
                return new AST.Statement.Invoke(
                        new AST.Value.Variable("?", "line"),
                        List.of(),
                        Optional.empty()
                );
            }
        }
    }
}
