package org.ballerinalang.oci;

public class OciClientException extends RuntimeException {

    public OciClientException(String message) {
        super(message);
    }

    public OciClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
