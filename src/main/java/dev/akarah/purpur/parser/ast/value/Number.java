package dev.akarah.purpur.parser.ast.value;

import dev.akarah.purpur.misc.SpanData;
import dev.akarah.purpur.parser.CodegenContext;
import dev.akarah.purpur.parser.ast.value.Value;
import dev.dfonline.flint.actiondump.codeblocks.ActionType;
import dev.dfonline.flint.templates.argument.NumberArgument;
import dev.dfonline.flint.templates.argument.abstracts.Argument;

public record Number(String literal, SpanData spanData) implements Value {
    @Override
    public void lowerToParsable(StringBuilder builder, int depth) {
        if (literal.contains("%")) {
            builder.append("n\"").append(literal).append("\"");
        } else {
            builder.append(literal);
        }
    }

    public Argument createArgument(CodegenContext ctx, ActionType actionType, int argIndex) {
        return new NumberArgument(argIndex, this.literal);
    }
}
