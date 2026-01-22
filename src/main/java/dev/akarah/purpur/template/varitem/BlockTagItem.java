package dev.akarah.purpur.template.varitem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BlockTagItem(
        String option,
        String tag,
        String block,
        String action
) implements VarItem {
    public static Codec<BlockTagItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("option").forGetter(BlockTagItem::option),
            Codec.STRING.fieldOf("tag").forGetter(BlockTagItem::tag),
            Codec.STRING.fieldOf("block").forGetter(BlockTagItem::block),
            Codec.STRING.fieldOf("action").forGetter(BlockTagItem::action)
    ).apply(instance, BlockTagItem::new));

    @Override
    public String itemId() {
        return "bl_tag";
    }
}
