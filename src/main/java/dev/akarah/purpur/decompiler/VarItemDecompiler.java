package dev.akarah.purpur.decompiler;

import dev.akarah.purpur.mappings.MappingsRepository;
import dev.akarah.purpur.parser.ast.value.Value;
import dev.akarah.purpur.parser.ast.value.*;
import dev.akarah.purpur.parser.ast.value.Number;
import dev.dfonline.flint.templates.argument.*;
import dev.dfonline.flint.templates.argument.abstracts.Argument;

public class VarItemDecompiler {
    public static Value decompile(Argument argument) {
        return switch (argument) {
            case NumberArgument numberArgument ->
                    new Number(numberArgument.getNumber(), null);
            case VariableArgument variableArgument ->
                    new Variable(variableArgument.getName(), variableArgument.getScope().internalName, null);
            case StringArgument stringArgument ->
                    new StringLiteral(stringArgument.getValue().replace("\\", "\\\\"), null);
            case TextArgument textArgument ->
                    new ComponentLiteral(textArgument.getValue().replace("\\", "\\\\"), null);
            case VectorArgument vectorArgument ->
                    new VecLiteral(
                            vectorArgument.getX(),
                            vectorArgument.getY(),
                            vectorArgument.getZ(),
                            null
                    );
            case LocationArgument locationArgument ->
                    new LocationLiteral(
                            locationArgument.getX(),
                            locationArgument.getY(),
                            locationArgument.getZ(),
                            locationArgument.getPitch(),
                            locationArgument.getYaw(),
                            null
                    );
            case ParameterArgument parameterArgument -> new ParameterLiteral(
                    parameterArgument.getName(),
                    MappingsRepository.dfTypeToScriptType(parameterArgument.getType().name).orElse("any"),
                    parameterArgument.isPlural(),
                    parameterArgument.isOptional(),
                    parameterArgument.getDefaultValue() == null ? null : decompile(parameterArgument.getDefaultValue()),
                    null
            );
            case TagArgument tagArgument -> {
                var tag = MappingsRepository.get().getScriptTag(new MappingsRepository.DfBlockTag(tagArgument.getTag(), tagArgument.getOption()));
                yield new TagLiteral(tag.tag(), tag.option(), null);
            }
            case GameValueArgument gameValueArgument -> {
                System.out.println(new MappingsRepository.DfGameValue(gameValueArgument.getType(),  gameValueArgument.getTarget().name()));
                System.out.println(MappingsRepository.get().getScriptGameValue(new MappingsRepository.DfGameValue(gameValueArgument.getType(),  gameValueArgument.getTarget().name())));
                var tag = MappingsRepository.get().getScriptGameValue(new MappingsRepository.DfGameValue(gameValueArgument.getType(),  gameValueArgument.getTarget().name()));
                yield new GameValue(tag.option(), tag.target(), null);
            }
            case ItemArgument itemArgument -> new ItemStackVarItem(itemArgument.getItem(), null);
            case HintArgument hintArgument -> new HintVarItem(null);
            case SoundArgument soundArgument -> {
                var dfSound = new MappingsRepository.DfSound(soundArgument.getSound(), soundArgument.getVariant());
                yield new SoundLiteral(
                        MappingsRepository.get().getScriptSound(dfSound).id(),
                        soundArgument.getVolume(),
                        soundArgument.getPitch(),
                        null
                );
            }
            case ParticleArgument particleArgument -> new ParticleLiteral(particleArgument.getValues(), null);
            case PotionArgument potionArgument -> {
                for(var t : PotionArgument.PotionType.values()) {
                    System.out.println(t.name() + " / " + t.getName());
                }
                System.out.println("recv " + potionArgument);
                yield new PotionLiteral(potionArgument.getType(), potionArgument.getAmplifier(), potionArgument.getTicks(), null);
            }
            default -> new UnknownVarItem(null);
        };
    }
}
