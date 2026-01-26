package dev.akarah.purpur.decompiler;

import dev.akarah.purpur.parser.AST;
import dev.akarah.purpur.mappings.MappingsRepository;
import dev.dfonline.flint.templates.argument.*;
import dev.dfonline.flint.templates.argument.abstracts.Argument;

public class VarItemDecompiler {
    public static AST.Value decompile(Argument argument) {
        return switch (argument) {
            case NumberArgument numberArgument ->
                    new AST.Value.Number(numberArgument.getNumber(), null);
            case VariableArgument variableArgument ->
                    new AST.Value.Variable(variableArgument.getName(), variableArgument.getScope().internalName, null);
            case StringArgument stringArgument ->
                    new AST.Value.StringLiteral(stringArgument.getValue(), null);
            case TextArgument textArgument ->
                    new AST.Value.ComponentLiteral(textArgument.getValue(), null);
            case VectorArgument vectorArgument ->
                    new AST.Value.VecLiteral(
                            vectorArgument.getX(),
                            vectorArgument.getY(),
                            vectorArgument.getZ(),
                            null
                    );
            case LocationArgument locationArgument ->
                    new AST.Value.LocationLiteral(
                            locationArgument.getX(),
                            locationArgument.getY(),
                            locationArgument.getZ(),
                            locationArgument.getPitch(),
                            locationArgument.getYaw(),
                            null
                    );
            case ParameterArgument parameterArgument -> new AST.Value.ParameterLiteral(
                    parameterArgument.getName(),
                    parameterArgument.getType().name,
                    parameterArgument.isPlural(),
                    parameterArgument.isOptional(),
                    parameterArgument.getDefaultValue() == null ? null : decompile(parameterArgument.getDefaultValue()),
                    null
            );
            case TagArgument tagArgument -> {
                var tag = MappingsRepository.get().getScriptTag(new MappingsRepository.DfBlockTag(tagArgument.getTag(), tagArgument.getOption()));
                yield new AST.Value.TagLiteral(tag.tag(), tag.option(), null);
            }
            case GameValueArgument gameValueArgument -> {
                System.out.println(gameValueArgument.getType());
                System.out.println(gameValueArgument.getTarget().name());
                System.out.println(new MappingsRepository.DfGameValue(gameValueArgument.getType(),  gameValueArgument.getTarget().name()));
                System.out.println(MappingsRepository.get().getScriptGameValue(new MappingsRepository.DfGameValue(gameValueArgument.getType(),  gameValueArgument.getTarget().name())));
                var tag = MappingsRepository.get().getScriptGameValue(new MappingsRepository.DfGameValue(gameValueArgument.getType(),  gameValueArgument.getTarget().name()));
                yield new AST.Value.GameValue(tag.option(), tag.target(), null);
            }
            case ItemArgument itemArgument -> new AST.Value.ItemStackVarItem(itemArgument.getItem(), null);
            case HintArgument hintArgument -> new AST.Value.HintVarItem(null);
            default -> new AST.Value.UnknownVarItem(null);
        };
    }
}
