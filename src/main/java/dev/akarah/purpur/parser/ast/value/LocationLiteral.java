package dev.akarah.purpur.parser.ast.value;

import dev.akarah.purpur.misc.SpanData;
import dev.akarah.purpur.parser.CodegenContext;
import dev.akarah.purpur.parser.ast.value.Value;
import dev.dfonline.flint.actiondump.codeblocks.ActionType;
import dev.dfonline.flint.templates.argument.LocationArgument;
import dev.dfonline.flint.templates.argument.abstracts.Argument;

public record LocationLiteral(double x, double y, double z, double pitch, double yaw,
                              SpanData spanData) implements Value {
    @Override
    public void lowerToParsable(StringBuilder builder, int depth) {
        builder.append("loc(")
                .append(x)
                .append(", ")
                .append(y)
                .append(", ")
                .append(z)
                .append(", ")
                .append(pitch)
                .append(", ")
                .append(yaw)
                .append(")");
    }

    public Argument createArgument(CodegenContext ctx, ActionType actionType, int argIndex) {
        return new LocationArgument(argIndex, x, y, z, pitch, yaw, false);
    }
}
