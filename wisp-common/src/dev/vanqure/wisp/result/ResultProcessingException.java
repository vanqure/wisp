package dev.vanqure.wisp.result;

final class ResultProcessingException extends IllegalStateException {

    ResultProcessingException(final String message) {
        super(message);
    }

    ResultProcessingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
