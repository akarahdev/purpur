package dev.akarah.purpur.editor.parser.ast;

import dev.akarah.purpur.editor.parser.ast.stmt.Invoke;

import java.util.List;

public record Program(List<Invoke> statements) {
}
