package dev.akarah.purpur.parser.ast.value;

import dev.akarah.purpur.misc.SpanData;
import dev.akarah.purpur.parser.CodegenContext;
import dev.akarah.purpur.parser.ast.AST;
import dev.dfonline.flint.actiondump.codeblocks.ActionType;
import dev.dfonline.flint.templates.argument.abstracts.Argument;

public interface Value extends AST {
    Argument createArgument(CodegenContext ctx, ActionType actionType, int argIndex);
    SpanData spanData();
}
