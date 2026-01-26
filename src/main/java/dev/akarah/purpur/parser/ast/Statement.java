package dev.akarah.purpur.parser.ast;

import dev.akarah.purpur.parser.CodegenContext;

public sealed interface Statement extends AST permits Invoke {

    void buildTemplate(CodegenContext ctx);
}
