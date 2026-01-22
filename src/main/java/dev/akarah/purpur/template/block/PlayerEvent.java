package dev.akarah.purpur.template.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.purpur.template.varitem.Args;

public record PlayerEvent(
        String action,
        BlockAttribute attribute,
        Args args
) implements ActionBlock {
    public static MapCodec<PlayerEvent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("action").forGetter(PlayerEvent::action),
            BlockAttribute.CODEC.optionalFieldOf("attribute", BlockAttribute.NONE).forGetter(PlayerEvent::attribute),
            Args.CODEC.fieldOf("args").forGetter(PlayerEvent::args)
    ).apply(instance, PlayerEvent::new));

    @Override
    public String blockId() {
        return "event";
    }

    @Override
    public MapCodec<? extends ActionBlock> codec() {
        return CODEC;
    }
}
