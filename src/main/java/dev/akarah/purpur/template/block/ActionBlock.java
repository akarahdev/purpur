package dev.akarah.purpur.template.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import java.util.Map;

public interface ActionBlock extends CodeBlock {
    default String blockType() {
        return "block";
    }

    String blockId();
    MapCodec<? extends ActionBlock> codec();

    Map<String, MapCodec<? extends ActionBlock>> CODECS = Map.ofEntries(
            Map.entry("event", PlayerEvent.CODEC),
            Map.entry("player_action", PlayerAction.CODEC)
    );

    MapCodec<ActionBlock> CODEC = Codec.STRING.dispatchMap(
            "block",
            ActionBlock::blockId,
            CODECS::get
    );
}
