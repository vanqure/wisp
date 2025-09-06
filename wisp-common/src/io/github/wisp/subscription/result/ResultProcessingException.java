package io.github.wisp.subscription.result;

final class ResultProcessingException extends IllegalStateException {

    ResultProcessingException(String message) {
        super(message);
    }

    ResultProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
