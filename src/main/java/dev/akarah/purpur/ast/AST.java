package dev.akarah.purpur.ast;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public sealed interface AST {
    int TAB_SPACES_LENGTH = 4;

    record Block(
            List<Invoke> statements
    ) implements AST {
        @Override
        public void lowerToParsable(StringBuilder builder, int depth) {
            builder.append("\n");
            builder.append(" ".repeat(depth));
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

            var msb = new StringBuilder();
            int idx = 0;
            for(var arg : arguments) {
                arg.lowerToParsable(msb, depth);
                idx += 1;
                if(idx != arguments.size()) {
                    msb.append(", ");
                }
            }

            var msbOut = msb.toString();
            if(msbOut.length() > 80) {
                var lines = msbOut.split(", ");
                builder.append("\n");
                for(var line : lines) {
                    builder.append(" ".repeat(depth + AST.TAB_SPACES_LENGTH)).append(line).append(",\n");
                }
                builder.append(" ".repeat(depth));
            } else {
                builder.append(msbOut);
            }

            builder.append(")");

            this.childBlock.ifPresentOrElse(
                    cbs -> {
                        builder.append(" ");
                        cbs.lowerToParsable(builder, depth);
                    },
                    () -> builder.append(";")
            );
        }
    }

    sealed interface Value extends AST {
        record Variable(String name, String scope) implements Value {
            public static boolean charIsAllowedInIdentifier(int c) {
                return Character.isAlphabetic(c) || Character.isDigit(c) || c == '_' || c == '.';
            }

            @Override
            public void lowerToParsable(StringBuilder builder, int depth) {
                var wrapIdent = name.chars()
                        .noneMatch(Variable::charIsAllowedInIdentifier);
                switch (scope) {
                    case "line" -> {}
                    case "local" -> builder.append("local ");
                    case "unsaved", "game" -> builder.append("game ");
                    case "saved" -> builder.append("saved ");
                }
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

        record ParameterLiteral(String name, String type, boolean plural, boolean optional, @Nullable Value defaultValue) implements Value {
            @Override
            public void lowerToParsable(StringBuilder builder, int depth) {
                builder.append(name)
                        .append(": ")
                        .append(type);
                if(plural) builder.append("...");
                if(optional) builder.append("?");
                if(defaultValue != null) {
                    builder.append(" = ");
                    defaultValue.lowerToParsable(builder, depth);
                }
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
