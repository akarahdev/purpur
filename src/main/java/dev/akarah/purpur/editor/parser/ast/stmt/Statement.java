package dev.akarah.purpur.editor.parser.ast.stmt;

import dev.akarah.purpur.editor.parser.CodegenContext;
import dev.akarah.purpur.editor.parser.ast.AST;

public interface Statement extends AST {
    void buildTemplate(CodegenContext ctx);
}
