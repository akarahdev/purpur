package dev.akarah.purpur.ast;

import dev.akarah.purpur.decompiler.CodeBlockDecompiler;
import dev.akarah.purpur.mappings.MappingsRepository;
import dev.dfonline.flint.actiondump.codeblocks.ActionType;
import dev.dfonline.flint.templates.Arguments;
import dev.dfonline.flint.templates.CodeBlocks;
import dev.dfonline.flint.templates.Template;
import dev.dfonline.flint.templates.VariableScope;
import dev.dfonline.flint.templates.argument.*;
import dev.dfonline.flint.templates.argument.abstracts.Argument;
import dev.dfonline.flint.templates.codeblock.*;
import dev.dfonline.flint.templates.codeblock.target.EntityTarget;
import dev.dfonline.flint.templates.codeblock.target.PlayerTarget;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.Optional;

public sealed interface AST {
    int TAB_SPACES_LENGTH = 4;

    public sealed interface Statement extends AST {
        record Invoke(
                Value.Variable invoking,
                List<Value> arguments,
                Optional<Block> childBlock
        ) implements AST.Statement {
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
                    var nsb = new StringBuilder();
                    int idx2 = 0;
                    for(var arg : arguments) {
                        nsb.append("\n").append(" ".repeat(depth + TAB_SPACES_LENGTH));

                        arg.lowerToParsable(nsb, depth);

                        if(idx2 != arguments.size() - 1) {
                            nsb.append(",");
                        }

                        idx2 += 1;
                    }
                    builder.append(nsb).append("\n");
                    builder.append(" ".repeat(depth));
                } else {
                    builder.append(msbOut);
                }

                builder.append(")");

                this.childBlock.ifPresent(
                        cbs -> {
                            builder.append(" ");
                            cbs.lowerToParsable(builder, depth);
                        }
                );
            }

            @Override
            public void buildTemplate(CodeBlocks codeBlocks) {
                var lookupId = new MappingsRepository.ScriptFunction(this.invoking.name());
                var actionType = MappingsRepository.get().getActionType(lookupId.name());
                var arguments = new Arguments();
                int idx = 0;
                for(var arg : this.arguments) {
                    arguments.add(arg.createArgument(actionType, idx));
                    idx += 1;
                }
                if(this.invoking.name().contains("playerEvent.")) {
                    codeBlocks.add(new PlayerEvent(
                            actionType.name(),
                            false
                    ));
                }
                if(this.invoking.name().contains("entityEvent.")) {
                    codeBlocks.add(new EntityEvent(
                            actionType.name(),
                            false
                    ));
                }
                if(this.invoking.name().contains("player.")) {
                    codeBlocks.add(new PlayerAction(
                            actionType.name(),
                            PlayerTarget.NONE
                    ).setArguments(arguments));
                }
                if(this.invoking.name().contains("ifPlayer.")) {
                    codeBlocks.add(new IfPlayer(
                            actionType.name(),
                            PlayerTarget.NONE,
                            false
                    ).setArguments(arguments));
                }
                if(this.invoking.name().contains("entity.")) {
                    codeBlocks.add(new EntityAction(
                            EntityTarget.NONE,
                            actionType.name()
                    ).setArguments(arguments));
                }
                if(this.invoking.name().contains("ifEntity.")) {
                    codeBlocks.add(new IfEntity(
                            actionType.name(),
                            EntityTarget.NONE,
                            false
                    ).setArguments(arguments));
                }
                if(this.invoking.name().contains("ifGame.")) {
                    codeBlocks.add(new IfGame(
                            actionType.name(),
                            false
                    ).setArguments(arguments));
                }
                if(this.invoking.name().contains("game.")) {
                    codeBlocks.add(new GameAction(actionType.name()).setArguments(arguments));
                }
                if(this.invoking.name().contains("repeat.")) {
                    codeBlocks.add(new Repeat(
                            actionType.name(),
                            "",
                            false
                    ).setArguments(arguments));
                }
                if(this.invoking.name().contains("control.")) {
                    codeBlocks.add(new Control(actionType.name()).setArguments(arguments));
                }
                this.childBlock.ifPresent(childBlock -> {
                    if(this.invoking.name().contains("if")) {
                        codeBlocks.add(new Bracket(Bracket.Type.NORMAL, Bracket.Direction.OPEN));
                        childBlock.statements.forEach(childStatement -> childStatement.buildTemplate(codeBlocks));
                        codeBlocks.add(new Bracket(Bracket.Type.NORMAL, Bracket.Direction.CLOSE));
                    }
                    if(this.invoking.name().contains("repeat")) {
                        codeBlocks.add(new Bracket(Bracket.Type.REPEAT, Bracket.Direction.OPEN));
                        childBlock.statements.forEach(childStatement -> childStatement.buildTemplate(codeBlocks));
                        codeBlocks.add(new Bracket(Bracket.Type.REPEAT, Bracket.Direction.CLOSE));
                    }
                    if(this.invoking.name().contains("select")) {
                        childBlock.statements.forEach(childStatement -> childStatement.buildTemplate(codeBlocks));
                        codeBlocks.add(new SelectObject("Reset", "", false));
                    }
                    if(this.invoking.name().contains("Event")) {
                        childBlock.statements.forEach(childStatement -> childStatement.buildTemplate(codeBlocks));
                    }
                });
            }
        }

        void buildTemplate(CodeBlocks codeBlocks);
    }

    record Block(
            List<Statement.Invoke> statements
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


    sealed interface Value extends AST {
        record Variable(String name, String scope) implements Value {
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
                    case "line" -> {}
                    case "local" -> builder.append("local ");
                    case "unsaved", "game" -> builder.append("game ");
                    case "saved" -> builder.append("saved ");
                }
                if(wrapIdent) builder.append("`");
                builder.append(name);
                if(wrapIdent) builder.append("`");
            }

            public Argument createArgument(ActionType actionType, int argIndex) {
                return new VariableArgument(argIndex, this.name, VariableScope.valueOf(this.scope.toUpperCase()));
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

            public Argument createArgument(ActionType actionType, int argIndex) {
                return new NumberArgument(argIndex, Double.parseDouble(this.literal));
            }
        }

        record StringLiteral(String literal) implements Value {
            @Override
            public void lowerToParsable(StringBuilder builder, int depth) {
                builder.append("\"");
                builder.append(this.literal.replace("\"", "\\\""));
                builder.append("\"");
            }

            public Argument createArgument(ActionType actionType, int argIndex) {
                return new StringArgument(argIndex, this.literal);
            }
        }

        record ComponentLiteral(String literal) implements Value {
            @Override
            public void lowerToParsable(StringBuilder builder, int depth) {
                builder.append("$\"");
                builder.append(this.literal.replace("\"", "\\\""));
                builder.append("\"");
            }

            public Argument createArgument(ActionType actionType, int argIndex) {
                return new TextArgument(argIndex, this.literal);
            }
        }

        record TagLiteral(String tag, String option) implements Value {
            @Override
            public void lowerToParsable(StringBuilder builder, int depth) {
                builder.append("tag ");
                builder.append(tag);
                builder.append(".");
                builder.append(option);
            }

            public Argument createArgument(ActionType actionType, int argIndex) {
                var dfTag = MappingsRepository.get().getDfTag(new MappingsRepository.ScriptBlockTag(this.tag, this.option));
                return new TagArgument(argIndex, dfTag.tag(), dfTag.option(), actionType.name(), CodeBlockDecompiler.fancyNameToId(actionType.codeblockName()));
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

            public Argument createArgument(ActionType actionType, int argIndex) {
                return new LocationArgument(argIndex, x, y, z, pitch, yaw, false);
            }
        }

        record VecLiteral(double x, double y, double z) implements Value {
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

            public Argument createArgument(ActionType actionType, int argIndex) {
                return new VectorArgument(argIndex, x, y, z);
            }
        }

        record ParameterLiteral(String name, String type, boolean plural, boolean optional, @Nullable Value defaultValue) implements Value {
            @Override
            public void lowerToParsable(StringBuilder builder, int depth) {
                builder.append("param ")
                        .append(name)
                        .append(" = ");
                if(plural) builder.append("plural ");
                if(optional) builder.append("optional ");
                builder.append(type);
                if(defaultValue != null) {
                    builder.append(" = ");
                    defaultValue.lowerToParsable(builder, depth);
                }
            }

            public Argument createArgument(ActionType actionType, int argIndex) {
                if(defaultValue == null) {
                    return new ParameterArgument(argIndex, name, ParameterArgument.ParameterType.fromName(type), optional, plural, null);
                }
                return new ParameterArgument(argIndex, name, ParameterArgument.ParameterType.fromName(type), optional, plural, defaultValue.createArgument(actionType, argIndex));
            }
        }

        record ItemStackVarItem(ItemStack item) implements Value {
            @Override
            public void lowerToParsable(StringBuilder builder, int depth) {
                builder.append("item(")
                        .append(item.getItemHolder().unwrapKey().orElseThrow().identifier())
                        .append(", ")
                        .append(item.getCount())
                        .append(")");
            }

            public Argument createArgument(ActionType actionType, int argIndex) {
                return new ItemArgument(argIndex, item);
            }
        }

        record GameValue(String value, String target) implements Value {
            @Override
            public void lowerToParsable(StringBuilder builder, int depth) {
                builder.append("gamevalue ")
                        .append(target)
                        .append(".")
                        .append(value);
            }

            public Argument createArgument(ActionType actionType, int argIndex) {
                var out = MappingsRepository.get().getDfGameValue(new MappingsRepository.ScriptGameValue(target, value));
                return new GameValueArgument(argIndex, out.option(), GameValueArgument.GameValueTarget.valueOf(out.target().toUpperCase()));
            }
        }

        record HintVarItem() implements Value {
            @Override
            public void lowerToParsable(StringBuilder builder, int depth) {
                builder.append("hint");
            }

            public Argument createArgument(ActionType actionType, int argIndex) {
                return new HintArgument(argIndex, HintArgument.HintType.FUNCTION);
            }
        }

        record UnknownVarItem() implements Value {
            @Override
            public void lowerToParsable(StringBuilder builder, int depth) {
                builder.append("?");
            }

            public Argument createArgument(ActionType actionType, int argIndex) {
                return new NumberArgument(argIndex, 0);
            }
        }

        Argument createArgument(ActionType actionType, int argIndex);
    }

    void lowerToParsable(StringBuilder builder, int depth);
}
