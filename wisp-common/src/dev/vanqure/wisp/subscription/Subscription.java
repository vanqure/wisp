package dev.vanqure.wisp.subscription;

import java.lang.invoke.MethodHandle;
import java.util.Set;

public record Subscription(Subscriber subscriber, Set<MethodHandle> invocations) {}
