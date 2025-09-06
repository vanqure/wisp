package io.github.wisp.subscription.result;

import io.github.wisp.event.Event;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class ResultProcessorRegistryImpl implements ResultProcessorRegistry {

    private final Map<Class<?>, ResultProcessor<?, ?>> processorsByTypeMap;

    ResultProcessorRegistryImpl(Map<Class<?>, ResultProcessor<?, ?>> processorsByTypeMap) {
        this.processorsByTypeMap = processorsByTypeMap;
    }

    @Override
    public <E extends Event, T> void register(
            @NotNull Class<T> resultType,
            @NotNull ResultProcessor<E, T> resultProcessor) {
        processorsByTypeMap.put(resultType, resultProcessor);
    }

    private boolean isAssignableFrom(@NotNull Class<?> type, @NotNull Class<?> otherType) {
        return type.isAssignableFrom(otherType) || otherType.isAssignableFrom(type);
    }

    @Override
    public <E extends Event, T> void process(@NotNull E event, @Nullable T value) {
        if (value == null || processorsByTypeMap.isEmpty()) {
            return;
        }

        var resultType = value.getClass();
        if (CompletionStage.class.isAssignableFrom(resultType)) {
            processPromise(event, (CompletionStage<?>) value);
            return;
        }

        var resultProcessor = getResultProcessorByType(resultType);
        if (resultProcessor == null) {
            throw new ResultProcessingException(
                    "Couldn't process result of type %s, because of a missing result processor.".formatted(resultType));
        }

        //noinspection unchecked
        ((ResultProcessor<E, T>) resultProcessor).process(event, value);
    }

    private <E extends Event, T> void processPromise(
            @NotNull E event,
            @NotNull CompletionStage<T> promise) {
        promise.whenComplete((result, cause) -> process(event, result)).exceptionally(cause -> {
            throw new ResultProcessingException("Couldn't process result of %s.".formatted(event), cause);
        });
    }

    private @Nullable ResultProcessor<?, ?> getResultProcessorByType(@NotNull Class<?> resultType) {
        var processor = processorsByTypeMap.get(resultType);
        if (processor != null) {
            return processor;
        }

        for (var eventTypeToProcessor : processorsByTypeMap.entrySet()) {
            if (isAssignableFrom(eventTypeToProcessor.getKey(), resultType)) {
                return eventTypeToProcessor.getValue();
            }
        }

        return null;
    }
}
