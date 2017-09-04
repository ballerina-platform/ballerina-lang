package org.ballerinalang.connector.impl;

import org.ballerinalang.connector.api.ConnectorFuture;
import org.ballerinalang.connector.api.ConnectorFutureListener;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Created by rajith on 9/4/17.
 */
public class BConnectorFuture implements ConnectorFuture {
    private ConnectorFutureListener connectorFutureListener;

    private BValue response;
    private BallerinaException ex;

    @Override
    public void setConnectorFutureListener(ConnectorFutureListener futureListener) {
        this.connectorFutureListener = futureListener;
        if (response != null) {
            connectorFutureListener.notifySuccess(response);
        } else if (ex != null) {
            connectorFutureListener.notifyFailure(ex);
        }
        response = null;
        ex = null;
    }

    public void notifySuccess(BValue response) {
        if (connectorFutureListener != null) {
            connectorFutureListener.notifySuccess(response);
        } else {
            this.response = response;
        }
    }

    public void notifyFailure(BallerinaException ex) {
        if (connectorFutureListener != null) {
            connectorFutureListener.notifyFailure(ex);
        } else {
            this.ex = ex;
        }
    }

}
