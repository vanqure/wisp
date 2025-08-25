package dev.vanqure.wisp;

import dev.vanqure.wisp.event.Event;
import dev.vanqure.wisp.event.EventPublisher;
import dev.vanqure.wisp.event.EventPublishingException;
import dev.vanqure.wisp.result.ResultProcessor;
import dev.vanqure.wisp.result.ResultProcessorRegistry;
import dev.vanqure.wisp.subscription.Subscriber;
import dev.vanqure.wisp.subscription.Subscription;
import dev.vanqure.wisp.subscription.SubscriptionRegistry;
import java.lang.invoke.MethodHandle;

final class WispImpl implements Wisp {

    private final EventPublisher eventPublisher;
    private final SubscriptionRegistry subscriptionRegistry;
    private final ResultProcessorRegistry resultProcessorRegistry;

    WispImpl(
            final EventPublisher eventPublisher,
            final SubscriptionRegistry subscriptionRegistry,
            final ResultProcessorRegistry resultProcessorRegistry) {
        this.eventPublisher = eventPublisher;
        this.subscriptionRegistry = subscriptionRegistry;
        this.resultProcessorRegistry = resultProcessorRegistry;
    }

    @Override
    public <E extends Event, T> Wisp result(final Class<T> resultType, final ResultProcessor<E, T> resultProcessor) {
        resultProcessorRegistry.register(resultType, resultProcessor);
        return this;
    }

    @Override
    public void publish(final Event event, final String... topics) throws EventPublishingException {
        final var eventType = event.getClass();
        for (final Subscription subscription : subscriptionRegistry.getSubscriptionsByEventType(eventType)) {
            notifySubscription(subscription, eventPublisher, event, topics);
        }
    }

    private void notifySubscription(
            final Subscription subscription,
            final EventPublisher eventPublisher,
            final Event event,
            final String[] destinedTopics) {
        final var subscriber = subscription.subscriber();
        if (destinedTopics.length > 0 && isExcludedSubscription(subscriber, destinedTopics)) {
            return;
        }

        for (final var invocation : subscription.invocations()) {
            eventPublisher.execute(() -> notifySubscribedMethods(invocation, subscriber, event));
        }
    }

    private void notifySubscribedMethods(
            final MethodHandle invocation,
            final Subscriber subscriber,
            final Event event) {
        try {
            final var returnedValue = invocation.invoke(subscriber, event);
            if (returnedValue != null) {
                resultProcessorRegistry.process(event, returnedValue);
            }
        } catch (final Throwable throwable) {
            throw new EventPublishingException("Couldn't publish event %s.".formatted(event), throwable);
        }
    }

    private boolean isExcludedSubscription(final Subscriber subscriber, final String[] destinedTopics) {
        for (final var destinedTopic : destinedTopics) {
            if (subscriber.topic().equals(destinedTopic)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void subscribe(final Subscriber subscriber) {
        subscriptionRegistry.subscribe(subscriber);
    }
}
