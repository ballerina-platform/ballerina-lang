package org.ballerinalang.net.ws;

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.ConnectorFutureListener;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.HttpConnectorFutureListener;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import javax.websocket.CloseReason;
import javax.websocket.Session;
import java.io.IOException;

/**
 * Created by rajith on 9/7/17.
 */
public class WebSocketConnectorFutureListener implements ConnectorFutureListener {
    private static final Logger log = LoggerFactory.getLogger(HttpConnectorFutureListener.class);

    public WebSocketConnectorFutureListener() {
    }

    @Override
    public void notifySuccess(BValue response) {
        //TODO
    }

    @Override
    public void notifyFailure(BallerinaConnectorException ex) {
        //TODO
    }

    @Override
    public void notifySuccess(CarbonMessage carbonMessage) {

    }

    @Override
    public void notifyFailure(Exception ex, CarbonMessage carbonMessage) {
        if (ex.getMessage().startsWith("no Service found to handle the service request")) {
//            carbonCallback.done(carbonMessage);
            ErrorHandlerUtils.printError(ex);
            Session session = (Session) carbonMessage.getProperty(Constants.WEBSOCKET_SERVER_SESSION);
            String uri = (String) carbonMessage.getProperty(Constants.TO);
            try {
                session.close(new CloseReason(
                        () -> 1001, "Server closing connection since no service found for URI: " + uri));
            } catch (IOException e1) {
                log.error("Error in closing the websocket session", e1);
            }
        }
    }
}
