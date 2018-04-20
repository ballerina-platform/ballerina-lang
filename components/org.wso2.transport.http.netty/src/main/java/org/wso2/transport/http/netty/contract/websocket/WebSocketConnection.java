package org.wso2.transport.http.netty.contract.websocket;

import javax.websocket.Session;

/**
 * Represents successfully opened WebSocket connection.
 */
public interface WebSocketConnection {

    /**
     * Get the id of the connection.
     *
     * @return the id of the connection.
     */
    String getId();

    /**
     * Get the {@link javax.websocket.Session} of the WebSocket connection.
     *
     * <br><i>This is going to be removed in the future.</i>
     * <br><i>For more details </i>
     * @see <a href="https://github.com/wso2/transport-http/issues/130">
     *     [WebSocket] Need to Remove javax.websocket.session interface from Http-Transport</a>
     *
     * @return the session of the WebSocket connection.
     */
    Session getSession();

    /**
     * Reading WebSocket frames after successful handshake is blocked by default in transport level.
     * In order to read the next frame from wire this method should be called.
     *
     * <br><b>Note: This will allow reading frame by frame from the wire. If all frames should be read automatically
     * then startReadingFrames() method should be called.</b>
     */
    void readNextFrame();

    /**
     * Reading WebSocket frames after successful handshake is blocked by default in transport level.
     * In order to start reading WebSocket frames from the wire this method should be called.
     *
     * <br><b>Note: This will allow reading frames automatically from the wire. If frame by frame should be read
     * from the wire readNextFrame() method should be called.</b>
     */
    void startReadingFrames();

    /**
     * Stop reading WebSocket frames from the wire.
     */
    void stopReadingFrames();

}
