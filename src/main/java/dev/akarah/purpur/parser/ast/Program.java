package dev.akarah.purpur.parser.ast;

import java.util.List;

public record Program(List<Invoke> statements) {
}
