package org.wso2.carbon.transport.http.netty.contractimpl;

import org.wso2.carbon.transport.http.netty.contract.HTTPConnectorListener;
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorFuture;
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
    public void notifyHTTPListener(HTTPCarbonMessage httpMessage) throws ServerConnectorException {
        if (httpConnectorListener == null) {
            throw new ServerConnectorException("HTTP connector listener is not set");
        }
        httpConnectorListener.onMessage(httpMessage);
    }

    @Override
    public void setWSConnectorListener(WebSocketConnectorListener wsConnectorListener) {
        this.wsConnectorListener = wsConnectorListener;
    }

    @Override
    public void notifyWSListener(WebSocketInitMessage initMessage) throws ServerConnectorException {
        if (wsConnectorListener == null) {
            throw new ServerConnectorException("WebSocket connector listener is not set");
        }
        wsConnectorListener.onMessage(initMessage);
    }

    @Override
    public void notifyWSListener(WebSocketTextMessage textMessage) throws ServerConnectorException {
        if (wsConnectorListener == null) {
            throw new ServerConnectorException("WebSocket connector listener is not set");
        }
        wsConnectorListener.onMessage(textMessage);
    }

    @Override
    public void notifyWSListener(WebSocketBinaryMessage binaryMessage) throws ServerConnectorException {
        if (wsConnectorListener == null) {
            throw new ServerConnectorException("WebSocket connector listener is not set");
        }
        wsConnectorListener.onMessage(binaryMessage);
    }

    @Override
    public void notifyWSListener(WebSocketControlMessage controlMessage) throws ServerConnectorException {
        if (wsConnectorListener == null) {
            throw new ServerConnectorException("WebSocket connector listener is not set");
        }
        wsConnectorListener.onMessage(controlMessage);
    }

    @Override
    public void notifyWSListener(WebSocketCloseMessage closeMessage) throws ServerConnectorException {
        if (wsConnectorListener == null) {
            throw new ServerConnectorException("WebSocket connector listener is not set");
        }
        wsConnectorListener.onMessage(closeMessage);
    }

    @Override
    public void notifyWSListener(Throwable throwable) throws ServerConnectorException {
        if (wsConnectorListener == null) {
            throw new ServerConnectorException("WebSocket connector listener is not set");
        }
        wsConnectorListener.onError(throwable);
    }
}
