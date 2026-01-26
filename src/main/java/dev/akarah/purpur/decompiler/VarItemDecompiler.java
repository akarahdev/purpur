package dev.akarah.purpur.decompiler;

import dev.akarah.purpur.mappings.MappingsRepository;
import dev.akarah.purpur.parser.ast.Value;
import dev.dfonline.flint.templates.argument.*;
import dev.dfonline.flint.templates.argument.abstracts.Argument;

public class VarItemDecompiler {
    public static Value decompile(Argument argument) {
        return switch (argument) {
            case NumberArgument numberArgument ->
                    new Value.Number(numberArgument.getNumber(), null);
            case VariableArgument variableArgument ->
                    new Value.Variable(variableArgument.getName(), variableArgument.getScope().internalName, null);
            case StringArgument stringArgument ->
                    new Value.StringLiteral(stringArgument.getValue().replace("\\", "\\\\"), null);
            case TextArgument textArgument ->
                    new Value.ComponentLiteral(textArgument.getValue().replace("\\", "\\\\"), null);
            case VectorArgument vectorArgument ->
                    new Value.VecLiteral(
                            vectorArgument.getX(),
                            vectorArgument.getY(),
                            vectorArgument.getZ(),
                            null
                    );
            case LocationArgument locationArgument ->
                    new Value.LocationLiteral(
                            locationArgument.getX(),
                            locationArgument.getY(),
                            locationArgument.getZ(),
                            locationArgument.getPitch(),
                            locationArgument.getYaw(),
                            null
                    );
            case ParameterArgument parameterArgument -> new Value.ParameterLiteral(
                    parameterArgument.getName(),
                    MappingsRepository.dfTypeToScriptType(parameterArgument.getType().name).orElse("any"),
                    parameterArgument.isPlural(),
                    parameterArgument.isOptional(),
                    parameterArgument.getDefaultValue() == null ? null : decompile(parameterArgument.getDefaultValue()),
                    null
            );
            case TagArgument tagArgument -> {
                var tag = MappingsRepository.get().getScriptTag(new MappingsRepository.DfBlockTag(tagArgument.getTag(), tagArgument.getOption()));
                yield new Value.TagLiteral(tag.tag(), tag.option(), null);
            }
            case GameValueArgument gameValueArgument -> {
                System.out.println(gameValueArgument.getType());
                System.out.println(gameValueArgument.getTarget().name());
                System.out.println(new MappingsRepository.DfGameValue(gameValueArgument.getType(),  gameValueArgument.getTarget().name()));
                System.out.println(MappingsRepository.get().getScriptGameValue(new MappingsRepository.DfGameValue(gameValueArgument.getType(),  gameValueArgument.getTarget().name())));
                var tag = MappingsRepository.get().getScriptGameValue(new MappingsRepository.DfGameValue(gameValueArgument.getType(),  gameValueArgument.getTarget().name()));
                yield new Value.GameValue(tag.option(), tag.target(), null);
            }
            case ItemArgument itemArgument -> new Value.ItemStackVarItem(itemArgument.getItem(), null);
            case HintArgument hintArgument -> new Value.HintVarItem(null);
            default -> new Value.UnknownVarItem(null);
        };
    }
}
