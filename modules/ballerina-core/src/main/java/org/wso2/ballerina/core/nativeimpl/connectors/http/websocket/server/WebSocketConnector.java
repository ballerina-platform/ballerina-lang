package org.wso2.ballerina.core.nativeimpl.connectors.http.websocket.server;

import org.osgi.service.component.annotations.Component;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BMessage;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaConnector;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeConnector;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.http.netty.common.Constants;

import java.io.IOException;
import javax.websocket.Session;

/**
 * native WebSocket Connector
 */

@BallerinaConnector(
        packageName = "ballerina.net.http",
        connectorName = WebSocketConnector.CONNECTOR_NAME,
        args = {
                @Argument(name = "message", type = TypeEnum.MESSAGE)
        })
@Component(
        name = "ballerina.net.connectors.websocket",
        immediate = true,
        service = AbstractNativeConnector.class)
public class WebSocketConnector extends AbstractNativeConnector {

    public static final String CONNECTOR_NAME = "WebSocketConnector";
    private Session session;

    @Override
    public boolean init(BValue[] bValueRefs) {
        if (bValueRefs != null && bValueRefs.length == 1) {
            BMessage bMessage = (BMessage) bValueRefs[0];
            CarbonMessage cMsg = bMessage.value();
            String sessionId = (String) cMsg.getProperty(Constants.CHANNEL_ID);
            String uri = (String) cMsg.getProperty(Constants.TO);
            session = SessionManager.getInstance().getSession(uri, sessionId);
        }
        return true;
    }

    @Override
    public WebSocketConnector getInstance() {
        return new WebSocketConnector();
    }

    @Override
    public String getPackageName() {
        return "ballerina.net.http";
    }

    public void sendText(String text) {
        try {
            session.getBasicRemote().sendText(text);
        } catch (IOException e) {
            throw new BallerinaException("Error in sending the text message");
        }
    }
}
