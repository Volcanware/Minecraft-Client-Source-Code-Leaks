package com.thealtening.api;

public final class TheAlteningException
extends RuntimeException {
    public TheAlteningException(String errorType, String shortDescription) {
        super(String.format((String)"[%s]: %s", (Object[])new Object[]{errorType, shortDescription}));
    }

    public TheAlteningException(String errorType, Throwable cause) {
        super(errorType, cause);
    }
}
