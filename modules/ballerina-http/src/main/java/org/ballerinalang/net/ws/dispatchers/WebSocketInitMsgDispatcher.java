package org.ballerinalang.net.ws.dispatchers;

import org.ballerinalang.connector.api.Dispatcher;
import org.ballerinalang.connector.api.Registry;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.HttpService;
import org.ballerinalang.net.ws.Constants;
import org.ballerinalang.net.ws.WebSocketConnectionManager;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketInitMessage;
import org.wso2.carbon.transport.http.netty.message.HTTPMessageUtil;

import javax.websocket.Session;

/**
 * Created by rajith on 9/5/17.
 */
public class WebSocketInitMsgDispatcher implements Dispatcher {
    private WebSocketInitMessage webSocketInitMessage;
    private Session session;

    public WebSocketInitMsgDispatcher(WebSocketInitMessage webSocketInitMessage, Session session) {
        this.webSocketInitMessage = webSocketInitMessage;
        this.session = session;
    }

    @Override
    public void setRegistry(Registry registry) {

    }

    @Override
    public Resource findResource() {
        CarbonMessage carbonMessage = HTTPMessageUtil.convertWebSocketInitMessage(webSocketInitMessage);
        HttpService service = DispatcherUtil.findService(carbonMessage, webSocketInitMessage);
        //when it comes to here, session cannot be null
        //TODO do we need to move below line to "BallerinaWebSocketConnectorListener"?
        WebSocketConnectionManager.getInstance().addServerSession(service, session, carbonMessage);
        return DispatcherUtil.getResource(service, Constants.ANNOTATION_NAME_ON_OPEN);
    }

    @Override
    public BValue[] createParameters() {
        return new BValue[0];
    }

    @Override
    public CarbonCallback getCallback() {
        return null;
    }

    @Override
    public CarbonMessage getCarbonMsg() {
        return null;
    }


}
