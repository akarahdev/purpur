package dev.akarah.purpur.parser.ast.value;

import dev.akarah.purpur.misc.SpanData;
import dev.akarah.purpur.parser.CodegenContext;
import dev.dfonline.flint.actiondump.codeblocks.ActionType;
import dev.dfonline.flint.templates.argument.PotionArgument;
import dev.dfonline.flint.templates.argument.abstracts.Argument;

import java.lang.reflect.Field;

public record PotionLiteral(PotionArgument.PotionType potionType, int amplifier, int duration, SpanData spanData) implements Value {
    @Override
    public void lowerToParsable(StringBuilder builder, int depth) {
        builder.append("potion(")
                .append(potionType.getName()
                        .toLowerCase()
                        .replace("'", "")
                        .replace(" ", "_"))
                .append(", ")
                .append(amplifier)
                .append(", ")
                .append(duration)
                .append(")");
    }

    public Argument createArgument(CodegenContext ctx, ActionType actionType, int argIndex) {
        return new PotionArgument(argIndex, potionType, amplifier, duration);
    }
}
