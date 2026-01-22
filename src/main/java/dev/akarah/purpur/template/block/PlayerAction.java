package dev.akarah.purpur.template.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.purpur.template.varitem.Args;

public record PlayerAction(
        String action,
        BlockAttribute attribute,
        Args args
) implements ActionBlock {
    public static MapCodec<PlayerAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("action").forGetter(PlayerAction::action),
            BlockAttribute.CODEC.optionalFieldOf("attribute", BlockAttribute.NONE).forGetter(PlayerAction::attribute),
            Args.CODEC.fieldOf("args").forGetter(PlayerAction::args)
    ).apply(instance, PlayerAction::new));

    @Override
    public String blockId() {
        return "player_action";
    }

    @Override
    public MapCodec<? extends ActionBlock> codec() {
        return CODEC;
    }
}
