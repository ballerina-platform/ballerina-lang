package org.wso2.carbon.transport.http.netty.contractimpl;

/**
 * Class represents the status of outbound response.
 */
public class HttpResponseStatus {
    private Throwable cause;

    public HttpResponseStatus(Throwable throwable) {
        this.cause = throwable;
    }

    public Throwable getCause() {
        return cause;
    }
}
