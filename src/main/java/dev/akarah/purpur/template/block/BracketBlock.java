package dev.akarah.purpur.template.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;

public record BracketBlock(String type, String direction) implements ActionBlock {
    public static MapCodec<BracketBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("type").forGetter(BracketBlock::type),
            Codec.STRING.fieldOf("direct").forGetter(BracketBlock::direction)
    ).apply(instance, BracketBlock::new));

    @Override
    public String blockId() {
        return "bracket";
    }

    @Override
    public MapCodec<? extends ActionBlock> codec() {
        return CODEC;
    }
}
