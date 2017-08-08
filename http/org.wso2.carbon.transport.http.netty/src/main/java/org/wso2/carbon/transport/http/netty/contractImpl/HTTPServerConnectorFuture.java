package org.wso2.carbon.transport.http.netty.contractImpl;

import org.wso2.carbon.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.carbon.transport.http.netty.contract.HTTPConnectorListener;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketInitMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketTextMessage;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Server connector future implementation
 */
public class HTTPServerConnectorFuture implements ServerConnectorFuture {

    private HTTPConnectorListener httpConnectorListener;
    private WebSocketConnectorListener wsConnectorListener;

    @Override
    public void setHTTPConnectorListener(HTTPConnectorListener httpConnectorListener) {
        this.httpConnectorListener = httpConnectorListener;
    }

    @Override
    public void removeHTTPListener(HTTPConnectorListener connectorListener) {
        this.httpConnectorListener = null;
    }

    @Override
    public void notifyHTTPListener(HTTPCarbonMessage httpMessage) {
        httpConnectorListener.onMessage(httpMessage);
    }

    @Override
    public void setWSConnectorListener(WebSocketConnectorListener wsConnectorListener) {
        this.wsConnectorListener = wsConnectorListener;
    }

    @Override
    public void removeWSListener(WebSocketConnectorListener connectorListener) {
        this.wsConnectorListener = null;
    }

    @Override
    public void notifyWSListener(WebSocketInitMessage initMessage) {
        wsConnectorListener.onMessage(initMessage);
    }

    @Override
    public void notifyWSListener(WebSocketTextMessage textMessage) {
        wsConnectorListener.onMessage(textMessage);
    }

    @Override
    public void notifyWSListener(WebSocketBinaryMessage binaryMessage) {
        wsConnectorListener.onMessage(binaryMessage);
    }

    @Override
    public void notifyWSListener(WebSocketControlMessage controlMessage) {
        wsConnectorListener.onMessage(controlMessage);
    }

    @Override
    public void notifyWSListener(WebSocketCloseMessage closeMessage) {
        wsConnectorListener.onMessage(closeMessage);
    }

    @Override
    public void notifyWSListener(Throwable throwable) {
        wsConnectorListener.onError(throwable);
    }
}
