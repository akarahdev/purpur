package dev.akarah.purpur.ast;

import java.util.List;
import java.util.Optional;

public sealed interface AST {
    int TAB_SPACES_LENGTH = 4;

    record Block(
            List<Invoke> statements
    ) implements AST {
        @Override
        public void lowerToParsable(StringBuilder builder, int depth) {
            builder.append("{");
            for(var cb : this.statements) {
                builder.append("\n");
                builder.append(" ".repeat(depth + AST.TAB_SPACES_LENGTH));
                cb.lowerToParsable(builder, depth + AST.TAB_SPACES_LENGTH);
            }
            builder.append("\n").append(" ".repeat(depth)).append("}");
        }
    }

    record Invoke(
            Value.Variable invoking,
            List<Value> arguments,
            Optional<Block> childBlock
    ) implements AST {
        public Invoke withChildBlock(Block block) {
            return new Invoke(invoking, arguments, Optional.of(block));
        }

        @Override
        public void lowerToParsable(StringBuilder builder, int depth) {
            invoking.lowerToParsable(builder, depth);
            builder.append("(");
            int idx = 0;
            for(var arg : arguments) {
                arg.lowerToParsable(builder, depth);
                idx += 1;
                if(idx != arguments.size()) {
                    builder.append(", ");
                }
            }
            builder.append(")");

            this.childBlock.ifPresent(cbs -> {
                builder.append(" ");
                cbs.lowerToParsable(builder, depth);
            });
        }
    }

    sealed interface Value extends AST {
        record Variable(String name, String scope) implements Value {
            @Override
            public void lowerToParsable(StringBuilder builder, int depth) {
                var wrapIdent = name.chars().anyMatch(x -> !(Character.isAlphabetic(x) && Character.isDigit(x)));
                switch (scope) {
                    case "line" -> {}
                    case "local" -> builder.append("local");
                    case "unsaved", "game" -> builder.append("game ");
                    case "saved" -> builder.append("saved ");
                }
                if(!scope.equals("line")) builder.append(scope).append(" ");
                if(wrapIdent) builder.append("`");
                builder.append(name);
                if(wrapIdent) builder.append("`");
            }
        }

        record Number(String literal) implements Value {
            @Override
            public void lowerToParsable(StringBuilder builder, int depth) {
                if(literal.contains("%")) {
                    builder.append("n\"").append(literal).append("\"");
                } else {
                    builder.append(literal);
                }
            }
        }

        record StringLiteral(String literal) implements Value {
            @Override
            public void lowerToParsable(StringBuilder builder, int depth) {
                builder.append("\"");
                builder.append(this.literal.replace("\"", "\\\""));
                builder.append("\"");
            }
        }

        record ComponentLiteral(String literal) implements Value {
            @Override
            public void lowerToParsable(StringBuilder builder, int depth) {
                builder.append("$\"");
                builder.append(this.literal.replace("\"", "\\\""));
                builder.append("\"");
            }
        }

        record LocationLiteral(double x, double y, double z, double pitch, double yaw) implements Value {
            @Override
            public void lowerToParsable(StringBuilder builder, int depth) {
                builder.append("loc(")
                        .append(x)
                        .append(", ")
                        .append(y)
                        .append(", ")
                        .append(z)
                        .append(", ")
                        .append(pitch)
                        .append(", ")
                        .append(yaw)
                        .append(")");
            }
        }

        record VecLiteral(double x, double y, double z) implements Value {
            @Override
            public void lowerToParsable(StringBuilder builder, int depth) {
                builder.append("loc(")
                        .append(x)
                        .append(", ")
                        .append(y)
                        .append(", ")
                        .append(z)
                        .append(")");
            }
        }

        record UnknownVarItem() implements Value {
            @Override
            public void lowerToParsable(StringBuilder builder, int depth) {
                builder.append("?");
            }
        }
    }

    void lowerToParsable(StringBuilder builder, int depth);
}
