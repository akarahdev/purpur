package dev.akarah.purpur.parser.ast.value;

import dev.akarah.purpur.misc.SpanData;
import dev.akarah.purpur.parser.CodegenContext;
import dev.akarah.purpur.parser.ast.value.Value;
import dev.dfonline.flint.actiondump.codeblocks.ActionType;
import dev.dfonline.flint.templates.argument.VectorArgument;
import dev.dfonline.flint.templates.argument.abstracts.Argument;

public record VecLiteral(double x, double y, double z, SpanData spanData) implements Value {
    @Override
    public void lowerToParsable(StringBuilder builder, int depth) {
        builder.append("vec(")
                .append(x)
                .append(", ")
                .append(y)
                .append(", ")
                .append(z)
                .append(")");
    }

    public Argument createArgument(CodegenContext ctx, ActionType actionType, int argIndex) {
        return new VectorArgument(argIndex, x, y, z);
    }
}
