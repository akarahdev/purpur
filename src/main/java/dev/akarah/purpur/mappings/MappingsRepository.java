package dev.akarah.purpur.mappings;

import com.google.common.collect.Maps;
import dev.dfonline.flint.actiondump.ActionDump;
import dev.dfonline.flint.actiondump.codeblocks.ActionType;
import org.apache.commons.text.CaseUtils;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    Map<String, String> dfCodeBlockToScript = Maps.newHashMap();
    Map<String, String> scriptToDfCodeBlock = Maps.newHashMap();
    Map<String, ActionType> scriptToActionType = Maps.newHashMap();
    Map<DfBlockTag, ScriptBlockTag> dfTagToScript = Maps.newHashMap();
    Map<ScriptBlockTag, DfBlockTag> scriptTagToDfTag = Maps.newHashMap();
    Map<DfGameValue, ScriptGameValue> dfGameValueToScript = Maps.newHashMap();
    Map<ScriptGameValue, DfGameValue> scriptGameValueToDf = Maps.newHashMap();

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

            for(var entry : SCRIPT_NAME_HELPER.entrySet()) {
                scriptActionName = scriptActionName.replace(entry.getKey(), entry.getValue());
            }
            var scriptName = scriptCodeblockName + "." + scriptActionName;
            var dfName = action.codeblockName() + "." + action.name();

            INSTANCE.dfCodeBlockToScript.put(dfName, scriptName);
            INSTANCE.scriptToDfCodeBlock.put(scriptName, dfName);
            INSTANCE.scriptToActionType.put(scriptName, action);

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
        System.out.println(INSTANCE.dfGameValueToScript.keySet());
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
}
