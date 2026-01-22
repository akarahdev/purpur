package dev.akarah.purpur.template.block;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;

import java.util.Map;

public enum BlockAttribute {
    NONE(""),
    NOT("NOT"),
    LS_CANCEL("LS-CANCEL");

    public final String signText;
    private static final Map<String, BlockAttribute> ATTR_MAP = Map.ofEntries(
            Map.entry("", NONE),
            Map.entry("NOT", NOT),
            Map.entry("LS-CANCEL", LS_CANCEL)
    );

    BlockAttribute(String signText) {
        this.signText = signText;
    }

    public static final Codec<BlockAttribute> CODEC = Codec.STRING.xmap(ATTR_MAP::get, BlockAttribute::toString);
}
