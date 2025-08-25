package dev.vanqure.wisp.subscription;

public interface Subscriber {

    default String topic() {
        return null;
    }
}
