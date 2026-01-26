package dev.akarah.purpur.parser.ast.stmt;

import dev.akarah.purpur.parser.CodegenContext;
import dev.akarah.purpur.parser.ast.AST;

public interface Statement extends AST {
    void buildTemplate(CodegenContext ctx);
}
