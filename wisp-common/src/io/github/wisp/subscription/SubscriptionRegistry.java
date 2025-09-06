package io.github.wisp.subscription;

import io.github.wisp.event.Event;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;

public sealed interface SubscriptionRegistry permits SubscriptionRegistryImpl {

    static SubscriptionRegistry create() {
        return create(new ConcurrentHashMap<>());
    }

    static SubscriptionRegistry create(Map<Class<? extends Event>, Set<Subscription>> subscriptionsByEventTypeMap) {
        return new SubscriptionRegistryImpl(subscriptionsByEventTypeMap);
    }

    Set<Subscription> getSubscriptionsByEventType(@NotNull Class<? extends Event> eventType);

    void subscribe(@NotNull Subscriber subscriber) throws SubscribingException;
}
