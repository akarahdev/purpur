package dev.akarah.purpur.parser.ast;

import dev.akarah.purpur.parser.ast.stmt.Invoke;

import java.util.List;

public record Program(List<Invoke> statements) {
}
