package dev.akarah.purpur.parser.ast.value;

import dev.akarah.purpur.mappings.MappingsRepository;
import dev.akarah.purpur.misc.SpanData;
import dev.akarah.purpur.misc.SpannedException;
import dev.akarah.purpur.parser.CodegenContext;
import dev.akarah.purpur.parser.ast.value.Value;
import dev.dfonline.flint.actiondump.codeblocks.ActionType;
import dev.dfonline.flint.templates.argument.NumberArgument;
import dev.dfonline.flint.templates.argument.SoundArgument;
import dev.dfonline.flint.templates.argument.abstracts.Argument;

public record SoundLiteral(String id, double volume, double pitch, SpanData spanData) implements Value {
    @Override
    public void lowerToParsable(StringBuilder builder, int depth) {
        builder.append("sound(").append(id).append(", ").append(volume).append(", ").append(pitch).append(")");
    }

    public Argument createArgument(CodegenContext ctx, ActionType actionType, int argIndex) {
        var dfSound = MappingsRepository.get().getDfSound(new MappingsRepository.ScriptSound(this.id));
        if (dfSound == null) {
            ctx.errors().add(new SpannedException(
                    "Not a valid DF sound",
                    this.spanData()
            ));
            return new NumberArgument(argIndex, 0);
        }
        return new SoundArgument(
                argIndex,
                dfSound.sound(),
                volume,
                pitch,
                dfSound.variant()
        );
    }
}
