package dev.akarah.purpur.editor.parser.ast.value;

import dev.akarah.purpur.editor.misc.SpanData;
import dev.akarah.purpur.editor.parser.CodegenContext;
import dev.dfonline.flint.actiondump.codeblocks.ActionType;
import dev.dfonline.flint.templates.argument.StringArgument;
import dev.dfonline.flint.templates.argument.abstracts.Argument;

public record StringLiteral(String literal, SpanData spanData) implements Value {
    @Override
    public void lowerToParsable(StringBuilder builder, int depth) {
        builder.append("\"");
        builder.append(this.literal.replace("\"", "\\\""));
        builder.append("\"");
    }

    public Argument createArgument(CodegenContext ctx, ActionType actionType, int argIndex) {
        return new StringArgument(argIndex, this.literal);
    }
}
