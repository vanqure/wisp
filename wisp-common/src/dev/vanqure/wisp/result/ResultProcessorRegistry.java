package dev.vanqure.wisp.result;

import dev.vanqure.wisp.event.Event;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface ResultProcessorRegistry permits ResultProcessorRegistryImpl {

    static ResultProcessorRegistry create() {
        return create(new HashMap<>());
    }

    static ResultProcessorRegistry create(final Map<Class<?>, ResultProcessor<?, ?>> processorsByTypeMap) {
        return new ResultProcessorRegistryImpl(processorsByTypeMap);
    }

    <E extends Event, T> void register(@NotNull Class<T> resultType, @NotNull ResultProcessor<E, T> resultProcessor);

    <E extends Event, T> void process(final @NotNull E event, final @Nullable T value);
}
