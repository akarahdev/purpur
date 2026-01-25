package dev.akarah.purpur.decompiler;

import dev.akarah.purpur.ast.AST;
import dev.dfonline.flint.templates.argument.*;
import dev.dfonline.flint.templates.argument.abstracts.Argument;

public class VarItemDecompiler {
    public static AST.Value decompile(Argument argument) {
        return switch (argument) {
            case NumberArgument numberArgument ->
                    new AST.Value.Number(numberArgument.getNumber());
            case VariableArgument variableArgument ->
                    new AST.Value.Variable(variableArgument.getName(), variableArgument.getScope().internalName);
            case StringArgument stringArgument ->
                    new AST.Value.StringLiteral(stringArgument.getValue());
            case TextArgument textArgument ->
                    new AST.Value.ComponentLiteral(textArgument.getValue());
            case VectorArgument vectorArgument ->
                    new AST.Value.VecLiteral(
                            vectorArgument.getX(),
                            vectorArgument.getY(),
                            vectorArgument.getZ()
                    );
            case LocationArgument locationArgument ->
                    new AST.Value.LocationLiteral(
                            locationArgument.getX(),
                            locationArgument.getY(),
                            locationArgument.getZ(),
                            locationArgument.getPitch(),
                            locationArgument.getYaw()
                    );
            case ParameterArgument parameterArgument -> new AST.Value.ParameterLiteral(
                    parameterArgument.getName(),
                    parameterArgument.getType().name,
                    parameterArgument.isPlural(),
                    parameterArgument.isOptional(),
                    parameterArgument.getDefaultValue() == null ? null : decompile(parameterArgument.getDefaultValue())
            );
            default -> new AST.Value.UnknownVarItem();
        };
    }
}
