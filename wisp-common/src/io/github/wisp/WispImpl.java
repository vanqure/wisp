package io.github.wisp;

import io.github.wisp.event.Event;
import io.github.wisp.event.EventPublisher;
import io.github.wisp.event.EventPublishingException;
import io.github.wisp.subscription.Subscriber;
import io.github.wisp.subscription.Subscription;
import io.github.wisp.subscription.SubscriptionRegistry;
import io.github.wisp.subscription.result.ResultProcessor;
import io.github.wisp.subscription.result.ResultProcessorRegistry;
import java.lang.invoke.MethodHandle;

final class WispImpl implements Wisp {

    private final EventPublisher eventPublisher;
    private final SubscriptionRegistry subscriptionRegistry;
    private final ResultProcessorRegistry resultProcessorRegistry;

    WispImpl(
            EventPublisher eventPublisher,
            SubscriptionRegistry subscriptionRegistry,
            ResultProcessorRegistry resultProcessorRegistry) {
        this.eventPublisher = eventPublisher;
        this.subscriptionRegistry = subscriptionRegistry;
        this.resultProcessorRegistry = resultProcessorRegistry;
    }

    @Override
    public <E extends Event, T> Wisp result(Class<T> resultType, ResultProcessor<E, T> resultProcessor) {
        resultProcessorRegistry.register(resultType, resultProcessor);
        return this;
    }

    @Override
    public void publish(Event event, String... topics) throws EventPublishingException {
        var eventType = event.getClass();
        for (Subscription subscription : subscriptionRegistry.getSubscriptionsByEventType(eventType)) {
            var subscriber = subscription.subscriber();
            if (topics.length > 0 && shouldExcludeSubscriber(subscriber, topics)) {
                continue;
            }

            for (var invocation : subscription.invocations()) {
                eventPublisher.execute(() -> invokeSubscription(invocation, subscriber, event));
            }
        }
    }

    @Override
    public void subscribe(Subscriber subscriber) {
        subscriptionRegistry.subscribe(subscriber);
    }

    private boolean shouldExcludeSubscriber(Subscriber subscriber, String[] destinedTopics) {
        for (var destinedTopic : destinedTopics) {
            if (subscriber.topic().equals(destinedTopic)) {
                return false;
            }
        }
        return true;
    }

    private void invokeSubscription(MethodHandle invocation, Subscriber subscriber, Event event) {
        try {
            var returnedValue = invocation.invoke(subscriber, event);
            if (returnedValue != null) {
                resultProcessorRegistry.process(event, returnedValue);
            }
        } catch (Throwable throwable) {
            throw new EventPublishingException("Couldn't publish event %s.".formatted(event), throwable);
        }
    }
}
