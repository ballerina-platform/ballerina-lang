package org.ballerinalang.connector.api;

import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Created by rajith on 9/4/17.
 */
public interface ConnectorFutureListener {

    void notifySuccess(BValue response);

    void notifyFailure(BallerinaException ex);
}
