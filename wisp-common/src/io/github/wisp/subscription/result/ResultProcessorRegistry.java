package io.github.wisp.subscription.result;

import io.github.wisp.event.Event;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface ResultProcessorRegistry permits ResultProcessorRegistryImpl {

    static ResultProcessorRegistry create() {
        return create(new HashMap<>());
    }

    static ResultProcessorRegistry create(Map<Class<?>, ResultProcessor<?, ?>> processorsByTypeMap) {
        return new ResultProcessorRegistryImpl(processorsByTypeMap);
    }

    <E extends Event, T> void register(@NotNull Class<T> resultType, @NotNull ResultProcessor<E, T> resultProcessor);

    <E extends Event, T> void process(@NotNull E event, @Nullable T value);
}
