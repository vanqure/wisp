package io.github.wisp.subscription.result;

import io.github.wisp.event.Event;

@FunctionalInterface
public interface ResultProcessor<E extends Event, T> {

    void process(E event, T result);
}
