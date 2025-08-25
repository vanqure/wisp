package dev.vanqure.wisp;

import dev.vanqure.wisp.event.Event;
import dev.vanqure.wisp.event.EventPublisher;
import dev.vanqure.wisp.event.EventPublishingException;
import dev.vanqure.wisp.result.ResultProcessor;
import dev.vanqure.wisp.result.ResultProcessorRegistry;
import dev.vanqure.wisp.subscription.Subscriber;
import dev.vanqure.wisp.subscription.SubscribingException;
import dev.vanqure.wisp.subscription.SubscriptionRegistry;

public sealed interface Wisp permits WispImpl {

    static Wisp create() {
        return create(Runnable::run, SubscriptionRegistry.create(), ResultProcessorRegistry.create());
    }

    static Wisp create(
            final EventPublisher eventPublisher,
            final SubscriptionRegistry subscriptionRegistry,
            final ResultProcessorRegistry resultProcessorRegistry) {
        return new WispImpl(eventPublisher, subscriptionRegistry, resultProcessorRegistry);
    }

    <E extends Event, T> Wisp result(final Class<T> resultType, final ResultProcessor<E, T> resultProcessor);

    void publish(final Event event, final String... topics) throws EventPublishingException;

    void subscribe(final Subscriber subscriber) throws SubscribingException;
}
