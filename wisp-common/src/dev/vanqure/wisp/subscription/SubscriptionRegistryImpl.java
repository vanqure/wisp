package dev.vanqure.wisp.subscription;

import dev.vanqure.wisp.event.Event;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

final class SubscriptionRegistryImpl implements SubscriptionRegistry {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private final Map<Class<? extends Event>, Set<Subscription>> subscriptionsByEventTypeMap;

    SubscriptionRegistryImpl(
            final Map<Class<? extends Event>, Set<Subscription>> subscriptionsByEventTypeMap) {
        this.subscriptionsByEventTypeMap = subscriptionsByEventTypeMap;
    }

    @Override
    public void subscribe(final @NotNull Subscriber subscriber) throws SubscribingException {
        final var subscriberType = subscriber.getClass();
        final Map<Class<? extends Event>, Set<MethodHandle>> invocationsByEventType = new HashMap<>();
        for (final var declaredMethod : subscriberType.getDeclaredMethods()) {
            if (!isSubscribedMethod(declaredMethod)) {
                continue;
            }

            final var methodHandle = getMethodHandle(subscriberType, declaredMethod);
            invocationsByEventType.computeIfAbsent(getSubscribedEvent(methodHandle), k -> new HashSet<>())
                    .add(methodHandle);
        }

        for (final var entry : invocationsByEventType.entrySet()) {
            final var eventType = entry.getKey();
            final var invocations = entry.getValue();
            final var subscriptions = subscriptionsByEventTypeMap.computeIfAbsent(eventType, k -> new HashSet<>());
            subscriptions.add(new Subscription(subscriber, invocations));
        }
    }

    private boolean isSubscribedMethod(final @NotNull Method method) {
        return method.isAnnotationPresent(Subscribe.class) && method.getParameterCount() == 1
                && Event.class.isAssignableFrom(method.getParameterTypes()[0]);
    }

    @SuppressWarnings("unchecked")
    private @NotNull Class<? extends Event> getSubscribedEvent(final @NotNull MethodHandle methodHandle) {
        return (Class<? extends Event>) methodHandle.type().lastParameterType();
    }

    private @NotNull MethodHandle getMethodHandle(
            final @NotNull Class<?> subscriberType,
            final @NotNull Method method) {
        try {
            return getLookupForSubscriberType(subscriberType).unreflect(method);
        } catch (final IllegalAccessException exception) {
            throw new SubscribingException(
                    "Could not resolve method handle for %s method, because of illegal access.".formatted(method.getName()),
                    exception);
        }
    }

    private @NotNull MethodHandles.Lookup getLookupForSubscriberType(final @NotNull Class<?> subscriberType)
            throws IllegalAccessException {
        return Modifier.isPublic(subscriberType.getModifiers())
                ? LOOKUP
                : MethodHandles.privateLookupIn(subscriberType, LOOKUP);
    }

    @Override
    public Set<Subscription> getSubscriptionsByEventType(final @NotNull Class<? extends Event> eventType) {
        return Set.copyOf(subscriptionsByEventTypeMap.getOrDefault(eventType, Set.of()));
    }
}
