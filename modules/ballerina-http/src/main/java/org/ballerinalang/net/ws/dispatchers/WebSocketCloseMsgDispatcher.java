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
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.carbon.transport.http.netty.message.HTTPMessageUtil;

import javax.websocket.Session;

/**
 * Created by rajith on 9/5/17.
 */
public class WebSocketCloseMsgDispatcher implements Dispatcher {
    private WebSocketCloseMessage webSocketCloseMessage;

    public WebSocketCloseMsgDispatcher(WebSocketCloseMessage webSocketCloseMessage) {
        this.webSocketCloseMessage = webSocketCloseMessage;
    }

    @Override
    public void setRegistry(Registry registry) {

    }

    @Override
    public Resource findResource() {
        CarbonMessage carbonMessage = HTTPMessageUtil.convertWebSocketCloseMessage(webSocketCloseMessage);
        Session serverSession = webSocketCloseMessage.getChannelSession();
        WebSocketConnectionManager.getInstance().removeSessionFromAll(serverSession);
        HttpService service = DispatcherUtil.findService(carbonMessage, webSocketCloseMessage);
        return DispatcherUtil.getResource(service, Constants.ANNOTATION_NAME_ON_CLOSE);
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
