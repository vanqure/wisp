package io.github.wisp.subscription;

public interface Subscriber {

    default String topic() {
        return null;
    }
}
