package dev.akarah.purpur.template.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import java.util.Map;

public interface CodeBlock {
    String blockType();

    Map<String, MapCodec<? extends CodeBlock>> CODECS = Map.ofEntries(
            Map.entry("block", ActionBlock.CODEC),
            Map.entry("bracket", BracketBlock.CODEC)
    );

    Codec<CodeBlock> CODEC = Codec.STRING.dispatch(
            "id",
            CodeBlock::blockType,
            CODECS::get
    );
}
