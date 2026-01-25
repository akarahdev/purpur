package dev.akarah.purpur.decompiler;

import com.google.common.collect.Lists;
import dev.akarah.purpur.ast.AST;
import dev.akarah.purpur.mappings.MappingsRepository;
import dev.dfonline.flint.templates.Template;
import dev.dfonline.flint.templates.argument.NumberArgument;
import dev.dfonline.flint.templates.argument.abstracts.Argument;
import dev.dfonline.flint.templates.codeblock.*;

import java.util.List;
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

    public static AST.Invoke decompile(Template template) {
        List<AST.Invoke> list = Lists.newArrayList();
        var head = template.getBlocks().getBlocks().getFirst();

        int idx = 0;
        for(var block : template.getBlocks().getBlocks()) {
            idx += 1;
            if(idx == 1) {
                continue;
            }

            switch (block) {
                case PlayerAction playerAction -> {
                    var dfName = new MappingsRepository.DfName("PLAYER ACTION", playerAction.getAction());
                    var scriptName = MappingsRepository.get().getScriptName(dfName);
                    list.add(new AST.Invoke(
                            new AST.Value.Variable(scriptName.name(), "line"),
                            playerAction.getArguments().getOrderedList()
                                    .stream()
                                    .map(Decompiler::decompile)
                                    .toList(),
                            Optional.empty()
                    ));
                }
                default -> {}
            }
        }


        switch (head) {
            case PlayerEvent playerEvent -> {
                var dfName = new MappingsRepository.DfName("PLAYER EVENT", playerEvent.getAction());
                var scriptName = MappingsRepository.get().getScriptName(dfName);
                return new AST.Invoke(
                        new AST.Value.Variable(scriptName.name(), "line"),
                        List.of(),
                        Optional.of(new AST.Block(list))
                );
            }
            default -> {
                return new AST.Invoke(
                        new AST.Value.Variable("unknownHeader", "line"),
                        List.of(),
                        Optional.of(new AST.Block(list))
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
