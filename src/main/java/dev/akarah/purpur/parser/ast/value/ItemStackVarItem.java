package dev.akarah.purpur.parser.ast.value;

import dev.akarah.purpur.misc.SpanData;
import dev.akarah.purpur.parser.CodegenContext;
import dev.akarah.purpur.parser.ast.value.Value;
import dev.dfonline.flint.actiondump.codeblocks.ActionType;
import dev.dfonline.flint.templates.argument.ItemArgument;
import dev.dfonline.flint.templates.argument.abstracts.Argument;
import net.minecraft.world.item.ItemStack;

public record ItemStackVarItem(ItemStack item, SpanData spanData) implements Value {
    @Override
    public void lowerToParsable(StringBuilder builder, int depth) {
        builder.append("item(")
                .append(item.getItemHolder().unwrapKey().orElseThrow().identifier())
                .append(", ")
                .append(item.getCount())
                .append(")");
    }

    public Argument createArgument(CodegenContext ctx, ActionType actionType, int argIndex) {
        return new ItemArgument(argIndex, item);
    }
}
