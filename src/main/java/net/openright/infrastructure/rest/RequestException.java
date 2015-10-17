package net.openright.infrastructure.rest;

import java.util.function.Supplier;

public class RequestException extends RuntimeException {

    private static final long serialVersionUID = -8877435859449649574L;
    private final int statusCode;

    public RequestException(String string) {
        this(400, string);
    }

    public RequestException(int statusCode, String string) {
        super(string);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public static Supplier<RequestException> notFound(String id) {
        return () -> new RequestException(401, "Not Found " + id);
    }

}
