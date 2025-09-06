package io.github.wisp.subscription;

import io.github.wisp.event.Event;
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
            Map<Class<? extends Event>, Set<Subscription>> subscriptionsByEventTypeMap) {
        this.subscriptionsByEventTypeMap = subscriptionsByEventTypeMap;
    }

    @Override
    public Set<Subscription> getSubscriptionsByEventType(@NotNull Class<? extends Event> eventType) {
        return Set.copyOf(subscriptionsByEventTypeMap.getOrDefault(eventType, Set.of()));
    }

    @Override
    public void subscribe(@NotNull Subscriber subscriber) throws SubscribingException {
        var subscriberType = subscriber.getClass();
        Map<Class<? extends Event>, Set<MethodHandle>> invocationsByEventType = new HashMap<>();
        for (var declaredMethod : subscriberType.getDeclaredMethods()) {
            if (!isValidSubscriptionMethod(declaredMethod)) {
                continue;
            }

            var methodHandle = getMethodHandle(subscriberType, declaredMethod);
            invocationsByEventType.computeIfAbsent(extractEventType(methodHandle), k -> new HashSet<>())
                    .add(methodHandle);
        }

        for (var entry : invocationsByEventType.entrySet()) {
            var eventType = entry.getKey();
            var invocations = entry.getValue();
            var subscriptions = subscriptionsByEventTypeMap.computeIfAbsent(eventType, k -> new HashSet<>());
            subscriptions.add(new Subscription(subscriber, invocations));
        }
    }

    private boolean isValidSubscriptionMethod(@NotNull Method method) {
        return method.isAnnotationPresent(Subscribe.class) && method.getParameterCount() == 1
                && Event.class.isAssignableFrom(method.getParameterTypes()[0]);
    }

    private @NotNull Class<? extends Event> extractEventType(@NotNull MethodHandle methodHandle) {
        // noinspection unchecked
        return (Class<? extends Event>) methodHandle.type().lastParameterType();
    }

    private @NotNull MethodHandle getMethodHandle(
            @NotNull Class<?> subscriberType,
            @NotNull Method method) {
        try {
            return getLookupForSubscriberType(subscriberType).unreflect(method);
        } catch (IllegalAccessException exception) {
            throw new SubscribingException(
                    "Could not resolve method handle for %s method, because of illegal access.".formatted(method.getName()),
                    exception);
        }
    }

    private @NotNull MethodHandles.Lookup getLookupForSubscriberType(@NotNull Class<?> subscriberType)
            throws IllegalAccessException {
        return Modifier.isPublic(subscriberType.getModifiers())
                ? LOOKUP
                : MethodHandles.privateLookupIn(subscriberType, LOOKUP);
    }
}
