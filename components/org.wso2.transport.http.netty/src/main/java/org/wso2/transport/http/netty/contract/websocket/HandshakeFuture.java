package org.wso2.transport.http.netty.contract.websocket;

/**
 * Future for WebSocket handshake.
 */
public interface HandshakeFuture {

    /**
     * Set the listener for WebSocket handshake.
     *
     * @param handshakeListener Listener for WebSocket handshake.
     * @return the same handshake future.
     */
    public HandshakeFuture setHandshakeListener(HandshakeListener handshakeListener);

    /**
     * Notify the success of the WebSocket handshake.
     *
     * @param webSocketConnection {@link WebSocketConnection} for the successful connection.
     */
    public void notifySuccess(WebSocketConnection webSocketConnection);

    /**
     * Notify any error occurred during the handshake.
     *
     * @param throwable error occurred during handshake.
     */
    public void notifyError(Throwable throwable);
}
