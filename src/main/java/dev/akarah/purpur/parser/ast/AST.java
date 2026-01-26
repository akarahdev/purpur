package dev.akarah.purpur.parser.ast;

import com.google.common.collect.Lists;
import dev.akarah.purpur.decompiler.CodeBlockDecompiler;
import dev.akarah.purpur.mappings.MappingsRepository;
import dev.akarah.purpur.misc.ParseResult;
import dev.akarah.purpur.misc.SpanData;
import dev.akarah.purpur.misc.SpannedException;
import dev.akarah.purpur.parser.CodegenContext;
import dev.dfonline.flint.actiondump.codeblocks.ActionType;
import dev.dfonline.flint.templates.CodeBlocks;
import dev.dfonline.flint.templates.Template;
import dev.dfonline.flint.templates.VariableScope;
import dev.dfonline.flint.templates.argument.*;
import dev.dfonline.flint.templates.argument.abstracts.Argument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import org.jspecify.annotations.Nullable;

import java.util.List;

public sealed interface AST permits Block, Statement, Value {
    int TAB_SPACES_LENGTH = 4;

    void lowerToParsable(StringBuilder builder, int depth);

    static ParseResult<List<ItemStack>> buildTemplates(Program program) {
        var templates = Lists.<ItemStack>newArrayList();
        var errs = Lists.<SpannedException>newArrayList();
        for(var err : program.statements()) {
            var result = AST.buildTemplate(err);
            templates.add(result.partialResult());
            errs.addAll(result.errors());
        }
        return new ParseResult<>(templates, errs);
    }

    static ParseResult<ItemStack> buildTemplate(Invoke invoke) {
        var ctx = new CodegenContext(Lists.newArrayList(), new CodeBlocks());
        invoke.buildTemplate(ctx);

        var template = new Template();
        template.setBlocks(ctx.codeBlocks());

        var item = new ItemStack(Items.CHEST);

        var tag = new CompoundTag();
        var publicValues = new CompoundTag();
        publicValues.putString("hypercube:codetemplatedata", template.toJson().toString());
        tag.put("PublicBukkitValues", publicValues);
        item.set(
                DataComponents.CUSTOM_DATA,
                CustomData.of(tag)
        );
        return new ParseResult<>(item, ctx.errors());
    }
}
