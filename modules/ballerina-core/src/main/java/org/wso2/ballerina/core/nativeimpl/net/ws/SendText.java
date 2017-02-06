package org.wso2.ballerina.core.nativeimpl.net.ws;

import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.connectors.http.websocket.server.SessionManager;
import org.wso2.carbon.transport.http.netty.common.Constants;

import javax.websocket.Session;

/**
 * get a WebSocket connector for a given connection
 */

@BallerinaFunction(
        packageName = "ballerina.net.ws",
        functionName = "sendText",
        args = {
                @Argument(name = "message", type = TypeEnum.MESSAGE),
                @Argument(name = "text", type = TypeEnum.STRING)
        },
        isPublic = true
)
public class SendText extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        Session session = null;
        try {
            if (context.getCarbonMessage().getProperty(Constants.CHANNEL_ID) != null) {
                String sessionId = (String) context.getCarbonMessage().getProperty(Constants.CHANNEL_ID);
                String uri = (String) context.getCarbonMessage().getProperty(Constants.TO);
                SessionManager sessionManager = SessionManager.getInstance();
                session = sessionManager.getSession(uri, sessionId);
                String text = getArgument(context, 1).stringValue();
                session.getBasicRemote().sendText("Received message: " + text);
            }
        } catch (Throwable t) {
            throw new BallerinaException("Cannot send the message");
        }
        return VOID_RETURN;
    }
}
