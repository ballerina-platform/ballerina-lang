package org.ballerinalang.harbor;

public class HarborClientException extends RuntimeException {

    public HarborClientException(String message) {
        super(message);
    }

    public HarborClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
