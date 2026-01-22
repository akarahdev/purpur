package dev.akarah.purpur.template;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CodeTemplate(
        String author,
        String name,
        CodeLine code,
        int version
) {
    public static Codec<CodeTemplate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("author").forGetter(CodeTemplate::author),
            Codec.STRING.fieldOf("name").forGetter(CodeTemplate::name),
            CodeLine.AS_GZIPPED_JSON.fieldOf("code").forGetter(CodeTemplate::code),
            Codec.INT.fieldOf("version").forGetter(CodeTemplate::version)
    ).apply(instance, CodeTemplate::new));
}
