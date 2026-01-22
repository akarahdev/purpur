package dev.akarah.purpur.template;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.purpur.template.block.CodeBlock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public record CodeLine(
        List<CodeBlock> blocks
) {
    public static Codec<CodeLine> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CodeBlock.CODEC.listOf().fieldOf("blocks").forGetter(CodeLine::blocks)
    ).apply(instance, CodeLine::new));

    public static Codec<CodeLine> AS_GZIPPED_JSON = Codec.STRING
            .xmap(
                    str -> {
                        var bytes = Base64.getDecoder().decode(str);
                        var bais = new ByteArrayInputStream(bytes);
                        JsonElement json;
                        try (GZIPInputStream gzipIn = new GZIPInputStream(bais)) {
                            json = JsonParser.parseString(new String(gzipIn.readAllBytes(), StandardCharsets.UTF_8));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        return CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
                    },
                    line -> {
                        var json = CODEC.encodeStart(JsonOps.INSTANCE, line).getOrThrow().toString();
                        var baos = new ByteArrayOutputStream();
                        try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
                            gzipOut.write(json.getBytes(StandardCharsets.UTF_8));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        return Base64.getEncoder().encodeToString(baos.toByteArray());
                    }
            );
}
