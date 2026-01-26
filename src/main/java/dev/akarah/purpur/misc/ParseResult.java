package dev.akarah.purpur.misc;

import org.apache.commons.compress.utils.Lists;

import java.util.List;
import java.util.function.Function;

public record ParseResult<T>(
        T partialResult,
        List<SpannedException> errors
) {
    public boolean isSuccess() {
        return errors.isEmpty();
    }

    public <U> ParseResult<U> map(Function<T, U> mapper) {
        return new ParseResult<>(mapper.apply(this.partialResult), errors);
    }

    public <U> ParseResult<U> flatMap(Function<T, ParseResult<U>> mapper) {
        var mapped = mapper.apply(this.partialResult);
        var baseValue = mapped.partialResult;
        var errors = Lists.<SpannedException>newArrayList();
        errors.addAll(this.errors);
        errors.addAll(mapped.errors);
        return new ParseResult<>(mapped.partialResult, errors);
    }

}
