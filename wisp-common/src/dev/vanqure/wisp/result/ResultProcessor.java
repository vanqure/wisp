package dev.vanqure.wisp.result;

import dev.vanqure.wisp.event.Event;

@FunctionalInterface
public interface ResultProcessor<E extends Event, T> {

    void process(final E event, final T result);
}
