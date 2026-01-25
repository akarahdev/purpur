package dev.akarah.purpur.mappings;

import com.google.common.collect.Maps;
import dev.dfonline.flint.actiondump.ActionDump;
import dev.dfonline.flint.actiondump.codeblocks.ActionType;
import org.apache.commons.text.CaseUtils;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MappingsRepository {
    private static final MappingsRepository INSTANCE = new MappingsRepository();

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

    public record DfName(String codeblockName, String actionName) {
        @Override
        public @NonNull String toString() {
            return codeblockName + "." + actionName;
        }
    }

    public record ScriptName(String name) {
        @Override
        public @NonNull String toString() {
            return name;
        }
    }

    Map<String, String> dfCodeBlockToScript = Maps.newHashMap();
    Map<String, String> scriptToDfCodeBlock = Maps.newHashMap();
    Map<String, ActionType> scriptToActionType = Maps.newHashMap();

    public static MappingsRepository get() {
        return INSTANCE;
    }

    static {
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
                    .replace("ifVariable", "ifVars");

            for(var entry : SCRIPT_NAME_HELPER.entrySet()) {
                scriptCodeblockName = scriptCodeblockName.replace(entry.getKey(), entry.getValue());
            }
            var scriptActionName = Character.toLowerCase(action.name().trim().charAt(0)) + action.name().trim().substring(1);

            var scriptName = scriptCodeblockName + "." + scriptActionName;
            var dfName = action.codeblockName() + "." + action.name();

            INSTANCE.dfCodeBlockToScript.put(dfName, scriptName);
            INSTANCE.scriptToDfCodeBlock.put(scriptName, dfName);
            INSTANCE.scriptToActionType.put(scriptName, action);
        }
    }

    public ScriptName getScriptName(DfName dfName) {
        return new ScriptName(this.dfCodeBlockToScript.get(dfName.toString()));
    }

    public DfName getDfCodeblockName(String scriptName) {
        var base = this.scriptToDfCodeBlock.get(scriptName);
        var splits = base.split("\\.");
        return new DfName(splits[0], splits[1]);
    }

    public ActionType getActionType(String scriptName) {
        return this.scriptToActionType.get(scriptName);
    }

    public Set<ScriptName> getScriptNames() {
        return this.scriptToActionType.keySet()
                .stream()
                .map(ScriptName::new)
                .collect(Collectors.toSet());
    }
}
