package dev.akarah.purpur.parser;

import dev.akarah.purpur.misc.SpannedException;
import dev.dfonline.flint.templates.CodeBlocks;

import java.util.List;

public record CodegenContext(
        List<SpannedException> errors,
        CodeBlocks codeBlocks
) {
}
