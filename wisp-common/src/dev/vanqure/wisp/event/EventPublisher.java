package dev.vanqure.wisp.event;

import java.util.concurrent.Executor;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface EventPublisher extends Executor {

    @Override
    void execute(final @NotNull Runnable task);
}
