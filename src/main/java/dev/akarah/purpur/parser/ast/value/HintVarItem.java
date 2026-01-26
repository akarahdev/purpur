package dev.akarah.purpur.parser.ast.value;

import dev.akarah.purpur.misc.SpanData;
import dev.akarah.purpur.parser.CodegenContext;
import dev.akarah.purpur.parser.ast.value.Value;
import dev.dfonline.flint.actiondump.codeblocks.ActionType;
import dev.dfonline.flint.templates.argument.HintArgument;
import dev.dfonline.flint.templates.argument.abstracts.Argument;

public record HintVarItem(SpanData spanData) implements Value {
    @Override
    public void lowerToParsable(StringBuilder builder, int depth) {
        builder.append("hint");
    }

    public Argument createArgument(CodegenContext ctx, ActionType actionType, int argIndex) {
        return new HintArgument(argIndex, HintArgument.HintType.FUNCTION);
    }
}
