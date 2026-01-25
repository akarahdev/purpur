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
        record Variable(String name, String scope) implements AST {
            @Override
            public void lowerToParsable(StringBuilder builder, int depth) {
                if(!scope.equals("line")) builder.append(scope).append(" ");
                builder.append(name);
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

        record UnknownVarItem() implements Value {
            @Override
            public void lowerToParsable(StringBuilder builder, int depth) {
                builder.append("?");
            }
        }
    }

    void lowerToParsable(StringBuilder builder, int depth);
}
