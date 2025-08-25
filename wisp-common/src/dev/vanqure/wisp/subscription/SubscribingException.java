package dev.vanqure.wisp.subscription;

public final class SubscribingException extends RuntimeException {

    SubscribingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
