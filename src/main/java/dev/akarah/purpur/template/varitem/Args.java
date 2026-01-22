package dev.akarah.purpur.template.varitem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record Args(List<Slot> items) {
    record Slot(
            VarItem item,
            int slot
    ) {
        public static Codec<Slot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                VarItem.CODEC.fieldOf("item").forGetter(Slot::item),
                Codec.INT.fieldOf("slot").forGetter(Slot::slot)
        ).apply(instance, Slot::new));
    }

    public static Codec<Args> CODEC = Slot.CODEC.listOf()
            .fieldOf("items")
            .xmap(Args::new, Args::items)
            .codec();
}
