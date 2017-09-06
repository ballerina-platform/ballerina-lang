package org.ballerinalang.net.ws.dispatchers;

import org.ballerinalang.connector.api.Dispatcher;
import org.ballerinalang.connector.api.Registry;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.HttpService;
import org.ballerinalang.net.ws.Constants;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketTextMessage;
import org.wso2.carbon.transport.http.netty.message.HTTPMessageUtil;

/**
 * Created by rajith on 9/5/17.
 */
public class WebSocketTextMsgDispatcher implements Dispatcher {
    private WebSocketTextMessage webSocketTextMessage;

    public WebSocketTextMsgDispatcher(WebSocketTextMessage webSocketTextMessage) {
        this.webSocketTextMessage = webSocketTextMessage;
    }

    @Override
    public void setRegistry(Registry registry) {

    }

    @Override
    public String getProtocolPackage() {
        return Constants.WEBSOCKET_PACKAGE_PATH;
    }

    @Override
    public Resource findResource() {
        CarbonMessage carbonMessage = HTTPMessageUtil.convertWebSocketTextMessage(webSocketTextMessage);
        HttpService service = DispatcherUtil.findService(carbonMessage, webSocketTextMessage);
        return DispatcherUtil.getResource(service, Constants.ANNOTATION_NAME_ON_TEXT_MESSAGE);
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
