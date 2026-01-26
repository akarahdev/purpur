package dev.akarah.purpur.mappings;

import com.google.common.collect.Maps;
import dev.dfonline.flint.actiondump.ActionDump;
import dev.dfonline.flint.actiondump.codeblocks.ActionType;
import org.apache.commons.text.CaseUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class MappingsRepository {
    private static MappingsRepository INSTANCE;

    private static final Map<String, String> SCRIPT_NAME_HELPER = Map.ofEntries(
            Map.entry("ifVars.>=", "ifVars.greaterThanOrEqualTo"),
            Map.entry("ifVars.<=", "ifVars.lessThanOrEqualTo"),
            Map.entry("ifVars.>", "ifVars.greaterThan"),
            Map.entry("ifVars.<", "ifVars.lessThan"),
            Map.entry("ifVars.!=", "ifVars.notEquals"),
            Map.entry("ifVars.=", "ifVars.equals"),
            Map.entry("vars.=", "vars.set"),
            Map.entry("vars.+=", "vars.inc"),
            Map.entry("vars.-=", "vars.dec"),
            Map.entry("vars.+", "vars.add"),
            Map.entry("vars.-", "vars.sub"),
            Map.entry("vars.*", "vars.mul"),
            Map.entry("vars./", "vars.div"),
            Map.entry("vars.%", "vars.rem")
    );

    public static Optional<String> scriptTypeToDfType(String type) {
        return switch (type) {
            case "number" -> Optional.of("num");
            case "string" -> Optional.of("txt");
            case "text" -> Optional.of("comp");
            case "location" -> Optional.of("loc");
            case "vector" -> Optional.of("vec");
            case "sound" -> Optional.of("snd");
            case "particle" -> Optional.of("part");
            case "potion" -> Optional.of("pot");
            case "item" -> Optional.of("item");
            case "any" -> Optional.of("any");
            case "variable" -> Optional.of("var");
            case "list" -> Optional.of("list");
            case "dict" -> Optional.of("dict");
            default -> Optional.empty();
        };
    }

    public static Optional<String> dfTypeToScriptType(String type) {
        return switch (type) {
            case "num" -> Optional.of("number");
            case "txt" -> Optional.of("string");
            case "comp" -> Optional.of("text");
            case "loc" -> Optional.of("location");
            case "vec" -> Optional.of("vector");
            case "snd" -> Optional.of("sound");
            case "part" -> Optional.of("particle");
            case "pot" -> Optional.of("potion");
            case "item" -> Optional.of("item");
            case "any" -> Optional.of("any");
            case "var" -> Optional.of("variable");
            case "list" -> Optional.of("list");
            case "dict" -> Optional.of("dict");
            default -> Optional.empty();
        };
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

    public record DfFunction(String codeblockName, String actionName) {
        @Override
        public @NonNull String toString() {
            return codeblockName + "." + actionName;
        }
    }

    public record ScriptFunction(String name) {
        @Override
        public @NonNull String toString() {
            return name;
        }
    }

    public record DfBlockTag(String tag, String option) {
        @Override
        public @NonNull String toString() {
            return tag + "." + option;
        }
    }

    public record ScriptBlockTag(String tag, String option) {
        @Override
        public @NonNull String toString() {
            return tag + "." + option;
        }
    }

    public record DfGameValue(String option, String target) {
        @Override
        public @NonNull String toString() {
            return target + "." + option;
        }
    }

    public record ScriptGameValue(String option, String target) {
        @Override
        public @NonNull String toString() {
            return target + "." + option;
        }
    }

    public record DfSound(String sound, @Nullable String variant) {

    }

    public record ScriptSound(String id) {}



    Map<String, String> dfCodeBlockToScript = Maps.newHashMap();
    Map<String, String> scriptToDfCodeBlock = Maps.newHashMap();
    Map<String, ActionType> scriptToActionType = Maps.newHashMap();
    Map<ActionType, String> actionTypeToScript = Maps.newHashMap();
    Map<DfBlockTag, ScriptBlockTag> dfTagToScript = Maps.newHashMap();
    Map<ScriptBlockTag, DfBlockTag> scriptTagToDfTag = Maps.newHashMap();
    Map<DfGameValue, ScriptGameValue> dfGameValueToScript = Maps.newHashMap();
    Map<ScriptGameValue, DfGameValue> scriptGameValueToDf = Maps.newHashMap();
    Map<String, ActionType> dfSubActionToActionType = Maps.newHashMap();
    Map<ActionType, String> actionTypeToDfSubAction = Maps.newHashMap();
    Map<DfSound, ScriptSound> dfSoundToScript = Maps.newHashMap();
    Map<ScriptSound, DfSound> scriptSoundToDf = Maps.newHashMap();

    public static MappingsRepository get() {
        return INSTANCE;
    }

    static {
        init();
    }

    public static void init() {
        INSTANCE = new MappingsRepository();
        var dump = ActionDump.get();

        for(var action : dump.actions()) {
            // skip legacy actions
            if(action.name().contains("legacy ")) continue;

            var scriptCodeblockName = CaseUtils.toCamelCase(action.codeblockName(), false, ' ');

            // make the code block names conform to proper identifiers
            scriptCodeblockName = scriptCodeblockName
                    .replace("playerAction", "player")
                    .replace("entityAction", "entity")
                    .replace("gameAction", "game")
                    .replace("setVariable", "vars")
                    .replace("ifVariable", "ifVars")
                    .replace("selectObject", "select");

            var scriptActionName = Character.toLowerCase(action.name().trim().charAt(0)) + action.name().trim().substring(1);

            var a = scriptActionName;
            for(var entry : SCRIPT_NAME_HELPER.entrySet()) {
                if((scriptCodeblockName + "." + scriptActionName).equals(entry.getKey())) {
                    var splits = entry.getValue().split("\\.");
                    scriptCodeblockName = splits[0];
                    scriptActionName = splits[1];
                }
            }
            var b = scriptActionName;
            var scriptName = scriptCodeblockName + "." + scriptActionName;
            var dfName = action.codeblockName() + "." + action.name();

            INSTANCE.dfCodeBlockToScript.put(dfName, scriptName);
            INSTANCE.scriptToDfCodeBlock.put(scriptName, dfName);
            INSTANCE.scriptToActionType.put(scriptName, action);
            INSTANCE.actionTypeToScript.put(action, scriptName);

            INSTANCE.actionTypeToDfSubAction.put(action, action.name());
            for(var alias : action.aliases()) {
                INSTANCE.dfSubActionToActionType.put(alias, action);
                INSTANCE.actionTypeToDfSubAction.put(action, alias);
            }

            for(var tag : action.tags()) {
                for(var option : tag.options()) {
                    var dfTag = new DfBlockTag(tag.name(), option.name());
                    var scriptTag = new ScriptBlockTag(
                            CaseUtils.toCamelCase(tag.name(), false, ' ')
                                    .replace("'", "")
                                    .replaceAll("\\((.*?)\\)", ""),
                            CaseUtils.toCamelCase(option.name(), false, ' ')
                                    .replace("'", "")
                                    .replaceAll("\\((.*?)\\)", "")
                    );
                    INSTANCE.scriptTagToDfTag.put(scriptTag, dfTag);
                    INSTANCE.dfTagToScript.put(dfTag, scriptTag);
                }
            }
        }

        for(var gameValue : dump.gameValues()) {
            var targets = List.of("Selection", "Default", "Killer", "Damager", "Victim", "Shooter", "Projectile", "Last-Spawned Entity", "NO_TARGET");
            for(var target : targets) {
                var name = gameValue.icon().name()
                        .replaceAll("<(.*?)>", "");
                var dfVal = new DfGameValue(name, target);
                var scriptVal = new ScriptGameValue(
                        CaseUtils.toCamelCase(name.replace("-", " "), false, ' '),
                        CaseUtils.toCamelCase(
                                target
                                        .replace("-", "")
                                        .replace("NO_TARGET", "plot"),
                                false,
                                ' '
                        )
                );
                INSTANCE.dfGameValueToScript.put(dfVal, scriptVal);
                INSTANCE.scriptGameValueToDf.put(scriptVal, dfVal);
            }
        }

        INSTANCE.scriptSoundToDf.clear();
        INSTANCE.dfSoundToScript.clear();

        for(var sound : dump.sounds()) {
            var iconName = sound.icon().name().replaceAll("<(.*?)>", "");
            if(sound.variants() != null) {
                for(var variant : sound.variants()) {
                    var scriptId = new ScriptSound(
                            CaseUtils.toCamelCase(iconName, false, ' ')
                                    + "."
                                    + CaseUtils.toCamelCase(variant.id(), false, ' ', '_')
                    );
                    var dfId = new DfSound(iconName, variant.id());
                    INSTANCE.scriptSoundToDf.put(scriptId, dfId);
                    INSTANCE.dfSoundToScript.put(dfId, scriptId);
                }
            }
            var scriptId = new ScriptSound(
                    CaseUtils.toCamelCase(iconName, false, ' ')
            );
            var dfId = new DfSound(iconName, null);
            INSTANCE.scriptSoundToDf.put(scriptId, dfId);
            INSTANCE.dfSoundToScript.put(dfId, scriptId);
        }

        System.out.println(INSTANCE.dfSoundToScript.keySet());

    }

    public ScriptFunction getScriptFunction(DfFunction dfName) {
        return new ScriptFunction(this.dfCodeBlockToScript.get(dfName.toString()));
    }

    public DfFunction getDfFunction(String scriptName) {
        var base = this.scriptToDfCodeBlock.get(scriptName);
        var splits = base.split("\\.");
        return new DfFunction(splits[0], splits[1]);
    }

    public ActionType getActionType(String scriptName) {
        return this.scriptToActionType.get(scriptName);
    }

    public ScriptBlockTag getScriptTag(DfBlockTag dfTag) {
        return this.dfTagToScript.get(dfTag);
    }

    public DfBlockTag getDfTag(ScriptBlockTag scriptTag) {
        return this.scriptTagToDfTag.get(scriptTag);
    }

    public ScriptGameValue getScriptGameValue(DfGameValue dfGameValue) {
        return this.dfGameValueToScript.get(dfGameValue);
    }

    public DfGameValue getDfGameValue(ScriptGameValue scriptGameValue) {
        return this.scriptGameValueToDf.get(scriptGameValue);
    }

    public Set<ScriptFunction> getScriptNames() {
        return this.scriptToActionType.keySet()
                .stream()
                .map(ScriptFunction::new)
                .collect(Collectors.toSet());
    }

    public Map<String, ActionType> dfSubActionsToActionTypeMap() {
        return this.dfSubActionToActionType;
    }

    public Map<ActionType, String> actionTypeToDfSubActionsMap() {
        return this.actionTypeToDfSubAction;
    }

    public Collection<ActionType> allActionTypes() {
        return this.scriptToActionType.values();
    }

    public ActionType getSubAction(String scriptName) {
        return this.dfSubActionToActionType.get(scriptName);
    }

    public ScriptSound getScriptSound(DfSound dfSound) {
        return this.dfSoundToScript.get(dfSound);
    }

    public DfSound getDfSound(ScriptSound scriptSound) {
        return this.scriptSoundToDf.get(scriptSound);
    }
}
