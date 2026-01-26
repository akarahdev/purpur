package dev.akarah.purpur.parser.ast;

import dev.akarah.purpur.decompiler.CodeBlockDecompiler;
import dev.akarah.purpur.mappings.MappingsRepository;
import dev.akarah.purpur.misc.SpanData;
import dev.akarah.purpur.misc.SpannedException;
import dev.akarah.purpur.parser.CodegenContext;
import dev.dfonline.flint.actiondump.codeblocks.ActionType;
import dev.dfonline.flint.templates.VariableScope;
import dev.dfonline.flint.templates.argument.*;
import dev.dfonline.flint.templates.argument.abstracts.Argument;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;

public sealed interface AST permits Block, Statement, Value {
    int TAB_SPACES_LENGTH = 4;

    void lowerToParsable(StringBuilder builder, int depth);
}
