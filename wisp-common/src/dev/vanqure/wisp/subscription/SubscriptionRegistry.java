package dev.vanqure.wisp.subscription;

import dev.vanqure.wisp.event.Event;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;

public sealed interface SubscriptionRegistry permits SubscriptionRegistryImpl {

    static SubscriptionRegistry create() {
        return create(new ConcurrentHashMap<>());
    }

    static SubscriptionRegistry create(final Map<Class<? extends Event>, Set<Subscription>> subscriptionsByEventTypeMap) {
        return new SubscriptionRegistryImpl(subscriptionsByEventTypeMap);
    }

    void subscribe(final @NotNull Subscriber subscriber) throws SubscribingException;

    Set<Subscription> getSubscriptionsByEventType(final @NotNull Class<? extends Event> eventType);
}
