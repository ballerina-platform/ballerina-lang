package org.wso2.ballerina.core.nativeimpl.net.ws;

import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.carbon.serverconnector.framework.websocket.SessionManager;
import org.wso2.carbon.transport.http.netty.common.Constants;

import java.io.IOException;
import java.util.List;
import javax.websocket.Session;

/**
 * Broadcast the message to each user who has connected to the given WebSocket upgrade path.
 *
 * @since 0.8.0
 */

@BallerinaFunction(
        packageName = "ballerina.net.ws",
        functionName = "broadcastText",
        args = {
                @Argument(name = "message", type = TypeEnum.MESSAGE),
                @Argument(name = "text", type = TypeEnum.STRING)
        },
        isPublic = true
)
public class BroadcastText extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
      if (context.getCarbonMessage().getProperty(Constants.CHANNEL_ID) != null) {
            String uri = (String) context.getCarbonMessage().getProperty(Constants.TO);
            SessionManager sessionManager = SessionManager.getInstance();
            List<Session> sessions = sessionManager.getAllSessions(uri);
            String text = getArgument(context, 1).stringValue();
            sessions.forEach(
                    session -> {
                        try {
                            session.getBasicRemote().sendText(text);
                        } catch (IOException e) {
                            throw new BallerinaException("Error occurred when sending the message");
                        }
                    }
            );
        }
        return VOID_RETURN;
    }
}
