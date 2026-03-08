package dev.akarah.purpur.editor.parser;

import dev.akarah.purpur.editor.misc.SpannedException;
import dev.dfonline.flint.templates.CodeBlocks;

import java.util.List;

public record CodegenContext(
        List<SpannedException> errors,
        CodeBlocks codeBlocks
) {
}
