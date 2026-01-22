package dev.akarah.purpur.template.varitem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.purpur.template.block.PlayerEvent;

import java.util.Map;

public interface VarItem {
    String itemId();

    Map<String, Codec<? extends VarItem>> CODECS = Map.ofEntries(
            Map.entry("num", NumberItem.CODEC),
            Map.entry("bl_tag", BlockTagItem.CODEC)
    );

    Codec<VarItem> CODEC = Codec.STRING.dispatch(
            "id",
            VarItem::itemId,
            id -> CODECS.get(id).fieldOf("data")
    );
}
