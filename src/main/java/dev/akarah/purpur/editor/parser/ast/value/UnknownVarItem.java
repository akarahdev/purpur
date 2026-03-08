package dev.akarah.purpur.editor.parser.ast.value;

import dev.akarah.purpur.editor.misc.SpanData;
import dev.akarah.purpur.editor.parser.CodegenContext;
import dev.dfonline.flint.actiondump.codeblocks.ActionType;
import dev.dfonline.flint.templates.argument.NumberArgument;
import dev.dfonline.flint.templates.argument.abstracts.Argument;

public record UnknownVarItem(SpanData spanData) implements Value {
    @Override
    public void lowerToParsable(StringBuilder builder, int depth) {
        builder.append("?");
    }

    public Argument createArgument(CodegenContext ctx, ActionType actionType, int argIndex) {
        return new NumberArgument(argIndex, 0);
    }
}
