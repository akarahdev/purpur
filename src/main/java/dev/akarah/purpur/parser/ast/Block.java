package dev.akarah.purpur.parser.ast;

import dev.akarah.purpur.parser.ast.stmt.Invoke;

import java.util.List;

public record Block(
        List<Invoke> statements
) implements AST {
    @Override
    public void lowerToParsable(StringBuilder builder, int depth) {
        builder.append("\n");
        builder.append(" ".repeat(depth));
        builder.append("{");
        for (var cb : this.statements) {
            builder.append("\n");
            builder.append(" ".repeat(depth + AST.TAB_SPACES_LENGTH));
            cb.lowerToParsable(builder, depth + AST.TAB_SPACES_LENGTH);
        }
        builder.append("\n").append(" ".repeat(depth)).append("}");
    }
}
