package dev.akarah.purpur.parser.ast.value;

import dev.akarah.purpur.misc.SpanData;
import dev.akarah.purpur.misc.SpannedException;
import dev.akarah.purpur.parser.CodegenContext;
import dev.akarah.purpur.parser.ast.AST;
import dev.dfonline.flint.actiondump.codeblocks.ActionType;
import dev.dfonline.flint.templates.argument.NumberArgument;
import dev.dfonline.flint.templates.argument.abstracts.Argument;
import net.minecraft.client.Minecraft;
import net.minecraft.data.structures.SnbtToNbt;
import net.minecraft.nbt.*;
import net.minecraft.resources.RegistryOps;

public record NbtLiteral(Tag tag, SpanData spanData) implements Value {
    @Override
    public void lowerToParsable(StringBuilder builder, int depth) {
        builder.append("nbt(");

        var inStr = NbtUtils.structureToSnbt((CompoundTag) tag)
                .replace("data: [],", "")
                .replace("palette: [],", "");
        inStr = inStr.replaceAll("\n( *?)\n( *?)", "");
        inStr = inStr.replaceAll("(?<=\n)( *?)", " ".repeat(depth + 4));
        builder.append(inStr);
        builder.append(")");
    }

    public Argument createArgument(CodegenContext ctx, ActionType actionType, int argIndex) {
        ctx.errors().add(new SpannedException(
                "NBT literals can not be used as code block arguments",
                this.spanData()
        ));
        return new NumberArgument(argIndex, 0);
    }
}
