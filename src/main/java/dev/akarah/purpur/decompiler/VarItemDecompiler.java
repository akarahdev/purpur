package dev.akarah.purpur.decompiler;

import dev.akarah.purpur.ast.AST;
import dev.dfonline.flint.templates.argument.NumberArgument;
import dev.dfonline.flint.templates.argument.abstracts.Argument;

public class VarItemDecompiler {
    public static AST.Value decompile(Argument argument) {
        return switch (argument) {
            case NumberArgument numberArgument -> new AST.Value.Number(numberArgument.getNumber());
            default -> new AST.Value.UnknownVarItem();
        };
    }
}
