package org.ballerinalang.core;

import org.ballerinalang.services.dispatchers.file.Constants;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.ServerConnectorErrorHandler;

/**
 * Error handler for the file server connector tests.
 */
public class TestFileErrorHandler implements ServerConnectorErrorHandler {
    @Override
    public void handleError(Exception e, CarbonMessage carbonMessage, CarbonCallback carbonCallback)
            throws Exception {
        throw new BallerinaException(e);
    }

    @Override
    public String getProtocol() {
        return Constants.PROTOCOL_FILE;
    }
}
