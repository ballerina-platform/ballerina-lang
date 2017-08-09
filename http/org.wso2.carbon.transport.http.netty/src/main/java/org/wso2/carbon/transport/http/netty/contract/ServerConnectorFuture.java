package org.wso2.carbon.transport.http.netty.contract;

import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketInitMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketTextMessage;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Allows to set listeners.
 */
public interface ServerConnectorFuture {
    void setHTTPConnectorListener(HTTPConnectorListener connectorListener);
    void removeHTTPListener(HTTPConnectorListener connectorListener);
    void notifyHTTPListener(HTTPCarbonMessage httpMessage);

    void setWSConnectorListener(WebSocketConnectorListener connectorListener);
    void removeWSListener(WebSocketConnectorListener connectorListener);
    void notifyWSListener(WebSocketInitMessage initMessage);
    void notifyWSListener(WebSocketTextMessage textMessage);
    void notifyWSListener(WebSocketBinaryMessage binaryMessage);
    void notifyWSListener(WebSocketControlMessage controlMessage);
    void notifyWSListener(WebSocketCloseMessage closeMessage);
    void notifyWSListener(Throwable throwable);
}
