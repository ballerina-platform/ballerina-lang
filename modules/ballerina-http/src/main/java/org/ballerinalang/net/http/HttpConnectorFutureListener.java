package org.ballerinalang.net.http;

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.ConnectorFutureListener;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.session.Session;
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
    private BValue request;

    public HttpConnectorFutureListener(HTTPCarbonMessage requestMessage, BValue request) {
        this.requestMessage = requestMessage;
        this.request = request;
    }

    @Override
    public void notifySuccess() {
        //nothing to do, this will get invoked once resource finished execution
    }

    @Override
    public void notifyReply(BValue response) {
        HTTPCarbonMessage responseMessage = HttpUtil
                .getCarbonMsg((BStruct) response, HttpUtil.createHttpCarbonMessage(false));
        Session session = (Session) ((BStruct) request).getNativeData(Constants.HTTP_SESSION);
        if (session != null) {
            session.generateSessionHeader(responseMessage);
        }
        //Process CORS if exists.
        if (requestMessage.getHeader("Origin") != null) {
            CorsHeaderGenerator.process(requestMessage, responseMessage, true);
        }
        HttpUtil.handleResponse(requestMessage, responseMessage);
    }

    @Override
    public void notifyFailure(BallerinaConnectorException ex) {
        HttpUtil.handleFailure(requestMessage, ex);
    }
}
