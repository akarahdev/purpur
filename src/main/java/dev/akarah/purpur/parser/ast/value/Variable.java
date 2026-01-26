package dev.akarah.purpur.parser.ast.value;

import dev.akarah.purpur.lexer.Lexer;
import dev.akarah.purpur.lexer.TokenTree;
import dev.akarah.purpur.misc.SpanData;
import dev.akarah.purpur.parser.CodegenContext;
import dev.dfonline.flint.actiondump.codeblocks.ActionType;
import dev.dfonline.flint.templates.VariableScope;
import dev.dfonline.flint.templates.argument.VariableArgument;
import dev.dfonline.flint.templates.argument.abstracts.Argument;

public record Variable(String name, String scope, SpanData spanData) implements Value {
    public static boolean charIsAllowedInIdentifier(int c) {
        return Character.isAlphabetic(c) || Character.isDigit(c) || c == '_' || c == '.' || c == '/' || c == ':';
    }

    public boolean hasNormalIdentifier() {
        var tok = new Lexer(name).parseSingleToken();
        return name.chars().allMatch(Variable::charIsAllowedInIdentifier)
                && tok instanceof TokenTree.Identifier;
    }

    @Override
    public void lowerToParsable(StringBuilder builder, int depth) {
        var wrapIdent = !hasNormalIdentifier();
        switch (scope) {
            case "line" -> {
            }
            case "local" -> builder.append("local ");
            case "unsaved", "game" -> builder.append("game ");
            case "saved" -> builder.append("saved ");
        }
        if (wrapIdent) builder.append("`");
        builder.append(name);
        if (wrapIdent) builder.append("`");
    }

    public Argument createArgument(CodegenContext ctx, ActionType actionType, int argIndex) {
        return new VariableArgument(argIndex, this.name, VariableScope.fromInternalName(
                switch (this.scope) {
                    case "local" -> "local";
                    case "game" -> "unsaved";
                    case "saved" -> "saved";
                    default -> "line";
                }
        ));
    }
}
