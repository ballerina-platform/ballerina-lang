package org.wso2.transport.http.netty.contract.websocket;

import javax.websocket.Session;

public interface WebSocketConnection {

    String getId();

    Session getSession();

    void readNextFrame();

    void startReadingFrames();

    void stopReadingFrames();

}
