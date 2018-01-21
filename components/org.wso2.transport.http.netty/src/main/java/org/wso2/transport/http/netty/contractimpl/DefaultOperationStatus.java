package org.wso2.transport.http.netty.contractimpl;

import org.wso2.transport.http.netty.contract.OperationStatus;

/**
 * Class represents the status of outbound response.
 */
public class DefaultOperationStatus implements OperationStatus {
    private Throwable cause;
    private boolean isSuccess;

    public DefaultOperationStatus(Throwable throwable, boolean isSuccess) {
        this.cause = throwable;
        this.isSuccess = isSuccess;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }

    @Override
    public boolean isSuccess() {
        return this.isSuccess;
    }
}
