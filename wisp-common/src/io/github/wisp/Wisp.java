package io.github.wisp;

import io.github.wisp.event.Event;
import io.github.wisp.event.EventPublisher;
import io.github.wisp.event.EventPublishingException;
import io.github.wisp.subscription.Subscriber;
import io.github.wisp.subscription.SubscribingException;
import io.github.wisp.subscription.SubscriptionRegistry;
import io.github.wisp.subscription.result.ResultProcessor;
import io.github.wisp.subscription.result.ResultProcessorRegistry;

public sealed interface Wisp permits WispImpl {

    static Wisp create() {
        return create(Runnable::run, SubscriptionRegistry.create(), ResultProcessorRegistry.create());
    }

    static Wisp create(
            EventPublisher eventPublisher,
            SubscriptionRegistry subscriptionRegistry,
            ResultProcessorRegistry resultProcessorRegistry) {
        return new WispImpl(eventPublisher, subscriptionRegistry, resultProcessorRegistry);
    }

    <E extends Event, T> Wisp result(Class<T> resultType, ResultProcessor<E, T> resultProcessor);

    void publish(Event event, String... topics) throws EventPublishingException;

    void subscribe(Subscriber subscriber) throws SubscribingException;
}
