package dev.akarah.purpur.parser.ast.value;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.akarah.purpur.misc.SpanData;
import dev.akarah.purpur.parser.CodegenContext;
import dev.dfonline.flint.actiondump.codeblocks.ActionType;
import dev.dfonline.flint.templates.argument.ItemArgument;
import dev.dfonline.flint.templates.argument.ParticleArgument;
import dev.dfonline.flint.templates.argument.abstracts.Argument;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;

public record ParticleLiteral(JsonObject data, SpanData spanData) implements Value {

    public static ParticleLiteral fromNbt(Tag tag, SpanData spanData) {
        return new ParticleLiteral(
                (JsonObject) Codec.PASSTHROUGH.encodeStart(
                        JsonOps.INSTANCE,
                        Codec.PASSTHROUGH.parse(NbtOps.INSTANCE, tag).getOrThrow()
                ).getOrThrow(),
                spanData
        );
    }

    public Tag asNbt() {
        return Codec.PASSTHROUGH.encodeStart(
                NbtOps.INSTANCE,
                Codec.PASSTHROUGH.parse(JsonOps.INSTANCE, data).getOrThrow()
        ).getOrThrow();
    }

    @Override
    public void lowerToParsable(StringBuilder builder, int depth) {
        builder.append("particle(");
        new NbtLiteral(this.asNbt(), spanData).lowerToParsable(builder, depth);
        builder.append(")");
    }

    public Argument createArgument(CodegenContext ctx, ActionType actionType, int argIndex) {
        return new ParticleArgument(argIndex, this.data);
    }
}
