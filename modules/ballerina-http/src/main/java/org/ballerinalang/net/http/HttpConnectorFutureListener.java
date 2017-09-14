package org.ballerinalang.net.http;

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.ConnectorFutureListener;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * {@code HttpConnectorFutureListener} is the responsible for acting on notifications received from Ballerina side.
 *
 * @since 0.94
 */
public class HttpConnectorFutureListener implements ConnectorFutureListener {
    private static final Logger log = LoggerFactory.getLogger(HttpConnectorFutureListener.class);
    private HTTPCarbonMessage requestMessage;

    public HttpConnectorFutureListener(HTTPCarbonMessage requestMessage) {
        this.requestMessage = requestMessage;
    }

    @Override
    public void notifySuccess() {
        //nothing to do, this will get invoked once resource finished execution
    }

    @Override
    public void notifyReply(BValue response) {
        HTTPCarbonMessage responseMessage = (HTTPCarbonMessage) ((BStruct) response)
                .getNativeData(org.ballerinalang.net.http.Constants.TRANSPORT_MESSAGE);

        HttpUtil.handleResponse(requestMessage, responseMessage);
    }

    @Override
    public void notifyFailure(BallerinaConnectorException ex) {
        HttpUtil.handleFailure(requestMessage, ex);
    }
}
