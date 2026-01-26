package dev.akarah.purpur.parser.ast;

import dev.akarah.purpur.mappings.MappingsRepository;
import dev.akarah.purpur.misc.SpanData;
import dev.akarah.purpur.misc.SpannedException;
import dev.akarah.purpur.parser.CodegenContext;
import dev.dfonline.flint.actiondump.codeblocks.ActionType;
import dev.dfonline.flint.templates.VariableScope;
import dev.dfonline.flint.templates.argument.*;
import dev.dfonline.flint.templates.argument.abstracts.Argument;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

sealed public interface Value extends AST {
    record Variable(String name, String scope, SpanData spanData) implements dev.akarah.purpur.parser.ast.Value {
        public static boolean charIsAllowedInIdentifier(int c) {
            return Character.isAlphabetic(c) || Character.isDigit(c) || c == '_' || c == '.' || c == '/' || c == ':';
        }

        public boolean hasNormalIdentifier() {
            return name.chars().allMatch(Variable::charIsAllowedInIdentifier);
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
            return new VariableArgument(argIndex, this.name, VariableScope.valueOf(this.scope.toUpperCase()));
        }
    }

    record Number(String literal, SpanData spanData) implements Value {
        @Override
        public void lowerToParsable(StringBuilder builder, int depth) {
            if (literal.contains("%")) {
                builder.append("n\"").append(literal).append("\"");
            } else {
                builder.append(literal);
            }
        }

        public Argument createArgument(CodegenContext ctx, ActionType actionType, int argIndex) {
            return new NumberArgument(argIndex, Double.parseDouble(this.literal));
        }
    }

    record StringLiteral(String literal, SpanData spanData) implements Value {
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

    record ComponentLiteral(String literal, SpanData spanData) implements Value {
        @Override
        public void lowerToParsable(StringBuilder builder, int depth) {
            builder.append("$\"");
            builder.append(this.literal.replace("\"", "\\\""));
            builder.append("\"");
        }

        public Argument createArgument(CodegenContext ctx, ActionType actionType, int argIndex) {
            return new TextArgument(argIndex, this.literal);
        }
    }

    record TagLiteral(String tag, String option, SpanData spanData) implements Value {
        @Override
        public void lowerToParsable(StringBuilder builder, int depth) {
            builder.append("tag ");
            builder.append(tag);
            builder.append(".");
            builder.append(option);
        }

        public Argument createArgument(CodegenContext ctx, ActionType actionType, int argIndex) {
            var dfTag = MappingsRepository.get().getDfTag(new MappingsRepository.ScriptBlockTag(this.tag, this.option));
            if (dfTag == null) {
                ctx.errors().add(new SpannedException(
                        this.tag + "." + this.option + " is not a valid block tag",
                        this.spanData()
                ));
                return new NumberArgument(argIndex, 0);
            }
            for(var tagPossibility : actionType.tags()) {
                if(tagPossibility.name().equals(dfTag.tag())) {
                    return new TagArgument(tagPossibility.slot(), dfTag.option(), dfTag.tag(), actionType.name(), MappingsRepository.fancyNameToId(actionType.codeblockName()));
                }
            }
            ctx.errors().add(new SpannedException(
                    this.tag + "." + this.option + " is not a valid block tag for this codeblock",
                    this.spanData()
            ));
            return new NumberArgument(argIndex, 0);
        }
    }

    record LocationLiteral(double x, double y, double z, double pitch, double yaw,
                           SpanData spanData) implements Value {
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

        public Argument createArgument(CodegenContext ctx, ActionType actionType, int argIndex) {
            return new LocationArgument(argIndex, x, y, z, pitch, yaw, false);
        }
    }

    record VecLiteral(double x, double y, double z, SpanData spanData) implements Value {
        @Override
        public void lowerToParsable(StringBuilder builder, int depth) {
            builder.append("vec(")
                    .append(x)
                    .append(", ")
                    .append(y)
                    .append(", ")
                    .append(z)
                    .append(")");
        }

        public Argument createArgument(CodegenContext ctx, ActionType actionType, int argIndex) {
            return new VectorArgument(argIndex, x, y, z);
        }
    }

    record ParameterLiteral(String name, String type, boolean plural, boolean optional,
                            @Nullable Value defaultValue,
                            SpanData spanData) implements Value {
        @Override
        public void lowerToParsable(StringBuilder builder, int depth) {
            builder.append("param ")
                    .append(name)
                    .append("[");
            if (plural) builder.append("plural ");
            if (optional) builder.append("optional ");

            builder.append(type);
            builder.append("]");
            if (defaultValue != null) {
                builder.append(" = ");
                defaultValue.lowerToParsable(builder, depth);
            }
        }

        public Argument createArgument(CodegenContext ctx, ActionType actionType, int argIndex) {
            if (defaultValue == null) {
                return new ParameterArgument(argIndex, name, ParameterArgument.ParameterType.fromName(type), optional, plural, null);
            }
            return new ParameterArgument(argIndex, name, ParameterArgument.ParameterType.fromName(type), optional, plural, defaultValue.createArgument(ctx, actionType, argIndex));
        }
    }

    record ItemStackVarItem(ItemStack item, SpanData spanData) implements Value {
        @Override
        public void lowerToParsable(StringBuilder builder, int depth) {
            builder.append("item(")
                    .append(item.getItemHolder().unwrapKey().orElseThrow().identifier())
                    .append(", ")
                    .append(item.getCount())
                    .append(")");
        }

        public Argument createArgument(CodegenContext ctx, ActionType actionType, int argIndex) {
            return new ItemArgument(argIndex, item);
        }
    }

    record GameValue(String value, String target, SpanData spanData) implements Value {
        @Override
        public void lowerToParsable(StringBuilder builder, int depth) {
            builder.append("gamevalue ")
                    .append(target)
                    .append(".")
                    .append(value);
        }

        public Argument createArgument(CodegenContext ctx, ActionType actionType, int argIndex) {
            var out = MappingsRepository.get().getDfGameValue(new MappingsRepository.ScriptGameValue(target, value));
            if (out == null) {
                ctx.errors().add(new SpannedException(
                        this.target + "." + this.value + " is not a valid game value",
                        this.spanData()
                ));
                return new NumberArgument(argIndex, 0);
            }
            return new GameValueArgument(argIndex, out.option(), GameValueArgument.GameValueTarget.valueOf(out.target().toUpperCase()));
        }
    }

    record HintVarItem(SpanData spanData) implements Value {
        @Override
        public void lowerToParsable(StringBuilder builder, int depth) {
            builder.append("hint");
        }

        public Argument createArgument(CodegenContext ctx, ActionType actionType, int argIndex) {
            return new HintArgument(argIndex, HintArgument.HintType.FUNCTION);
        }
    }

    record UnknownVarItem(SpanData spanData) implements Value {
        @Override
        public void lowerToParsable(StringBuilder builder, int depth) {
            builder.append("?");
        }

        public Argument createArgument(CodegenContext ctx, ActionType actionType, int argIndex) {
            return new NumberArgument(argIndex, 0);
        }
    }

    Argument createArgument(CodegenContext ctx, ActionType actionType, int argIndex);

    SpanData spanData();
}
