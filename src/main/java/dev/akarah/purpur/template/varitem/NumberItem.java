package dev.akarah.purpur.template.varitem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record NumberItem(String name) implements VarItem {
    public static Codec<NumberItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(NumberItem::name)
    ).apply(instance, NumberItem::new));

    @Override
    public String itemId() {
        return "num";
    }
}
