package dev.akarah.purpur.parser.ast.value;

import dev.akarah.purpur.mappings.MappingsRepository;
import dev.akarah.purpur.misc.SpanData;
import dev.akarah.purpur.misc.SpannedException;
import dev.akarah.purpur.parser.CodegenContext;
import dev.akarah.purpur.parser.ast.value.Value;
import dev.dfonline.flint.actiondump.codeblocks.ActionType;
import dev.dfonline.flint.templates.argument.GameValueArgument;
import dev.dfonline.flint.templates.argument.NumberArgument;
import dev.dfonline.flint.templates.argument.abstracts.Argument;

public record GameValue(String value, String target, SpanData spanData) implements Value {
    @Override
    public void lowerToParsable(StringBuilder builder, int depth) {
        builder.append("gamevalue ")
                .append(target)
                .append(".")
                .append(value);
    }

    public Argument createArgument(CodegenContext ctx, ActionType actionType, int argIndex) {
        var out = MappingsRepository.get().getDfGameValue(new MappingsRepository.ScriptGameValue(target, value));
        if (out == null) {
            ctx.errors().add(new SpannedException(
                    this.target + "." + this.value + " is not a valid game value",
                    this.spanData()
            ));
            return new NumberArgument(argIndex, 0);
        }
        return new GameValueArgument(argIndex, out.option(), GameValueArgument.GameValueTarget.valueOf(out.target().toUpperCase()));
    }
}
