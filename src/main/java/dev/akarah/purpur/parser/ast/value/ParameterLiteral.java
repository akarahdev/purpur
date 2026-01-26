package dev.akarah.purpur.parser.ast.value;

import dev.akarah.purpur.misc.SpanData;
import dev.akarah.purpur.parser.CodegenContext;
import dev.akarah.purpur.parser.ast.value.Value;
import dev.dfonline.flint.actiondump.codeblocks.ActionType;
import dev.dfonline.flint.templates.argument.ParameterArgument;
import dev.dfonline.flint.templates.argument.abstracts.Argument;
import org.jspecify.annotations.Nullable;

public record ParameterLiteral(String name, String type, boolean plural, boolean optional,
                               @Nullable Value defaultValue,
                               SpanData spanData) implements Value {
    @Override
    public void lowerToParsable(StringBuilder builder, int depth) {
        builder.append("param ")
                .append(name)
                .append("[");
        if (plural) builder.append("plural ");
        if (optional) builder.append("optional ");

        builder.append(type);
        builder.append("]");
        if (defaultValue != null) {
            builder.append(" = ");
            defaultValue.lowerToParsable(builder, depth);
        }
    }

    public Argument createArgument(CodegenContext ctx, ActionType actionType, int argIndex) {
        if (defaultValue == null) {
            return new ParameterArgument(argIndex, name, ParameterArgument.ParameterType.fromName(type), optional, plural, null);
        }
        return new ParameterArgument(argIndex, name, ParameterArgument.ParameterType.fromName(type), optional, plural, defaultValue.createArgument(ctx, actionType, argIndex));
    }
}
