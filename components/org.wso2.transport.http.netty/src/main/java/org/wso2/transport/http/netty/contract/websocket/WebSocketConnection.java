package org.wso2.transport.http.netty.contract.websocket;

import io.netty.channel.ChannelFuture;

import java.nio.ByteBuffer;

/**
 * Represents successfully opened WebSocket connection.
 */
public interface WebSocketConnection {

    /**
     * Retrieve the Channel ID of the connection.
     *
     * @return the channel ID of the connection.
     */
    String getChannelId();

    /**
     * Check if the connection is open.
     *
     * @return true if the connection is open
     */
    boolean isOpen();

    /**
     * Check if the connection is secure.
     *
     * @return true if the connection is secure
     */
    boolean isSecure();

    /**
     * Retrieve the host of the connection.
     *
     * @return the host of the connection.
     */
    String getHost();

    /**
     * Retrieve the port of the connection.
     *
     * @return the port of the connection.
     */
    int getPort();

    /**
     * Retrieve the negotiated sub-protocol during WebSocket handshake.
     *
     * @return the negotiated sub-protocol during WebSocket handshake
     */
    String getNegotiatedSubProtocol();

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

    /**
     * Push text frame to the WebSocket connection asynchronously.
     *
     * @param text text to be sent
     * @return Future to represent the completion of asynchronous frame sending
     */
    ChannelFuture pushText(String text);

    /**
     * Push text frame to the WebSocket connection asynchronously.
     *
     * @param text text to be sent
     * @param finalFrame true if sending final frame
     * @return Future to represent the completion of asynchronous frame sending
     */
    ChannelFuture pushText(String text, boolean finalFrame);

    /**
     * Push binary frame to the WebSocket connection asynchronously.
     *
     * @param data binary data to be sent
     * @return Future to represent the completion of asynchronous frame sending
     */
    ChannelFuture pushBinary(ByteBuffer data);

    /**
     * Push binary frame to the WebSocket connection asynchronously.
     *
     * @param data binary data to be sent
     * @param finalFrame true if sending final frame
     * @return Future to represent the completion of asynchronous frame sending
     */
    ChannelFuture pushBinary(ByteBuffer data, boolean finalFrame);

    /**
     * Ping remote endpoint asynchronously.
     *
     * @param data data to be sent with ping frame
     * @return Future to represent the completion of asynchronous frame sending
     */
    ChannelFuture ping(ByteBuffer data);

    /**
     * Send pong to remote endpoint asynchronously.
     *
     * @param data data to be sent with ping frame
     * @return Future to represent the completion of asynchronous frame sending
     */
    ChannelFuture pong(ByteBuffer data);

    /**
     * Initiates connection closure. {@link ChannelFuture} will complete operation successfully if and only if it
     * receives back a echoed close WebSocket frame from the remote endpoint with the same status code as was written.
     * Also {@link ChannelFuture} will not reach operationComplete state until it receives a close WebSocket frame
     * from the remote endpoint. If user does not need to wait for the echoed back WebSocket frame from the remote
     * endpoint, user need to handle it separately.
     *
     * @param statusCode Status code to indicate the reason of closure
     *                   @see <a href="https://tools.ietf.org/html/rfc6455">WebSocket Protocol</a>
     * @param reason Reason to close the connection
     * @return Future to represent the completion of asynchronous frame sending
     */
    ChannelFuture initiateConnectionClosure(int statusCode, String reason);

    /**
     * Initiates connection closure. {@link ChannelFuture} will complete operation successfully if and only if it
     * receives back a echoed close WebSocket frame from the remote endpoint with the same status code as was written.
     * Also {@link ChannelFuture} will not reach operationComplete state until it receives a close WebSocket frame from
     * the remote endpoint. If user does not need to wait for the echoed back WebSocket frame from the remote endpoint,
     * user need to handle it separately.
     *
     * @return the future to represent the completion of asynchronous frame sending
     */
    ChannelFuture initiateConnectionClosure();

    /**
     * Finish the connection closure if a close frame has been received without this connection sending a close frame.
     *
     * @param statusCode Status code to indicate the reason of closure
     *                   @see <a href="https://tools.ietf.org/html/rfc6455">WebSocket Protocol</a>
     * @param reason Reason to close the connection
     * @return Future to represent the completion of asynchronous frame sending
     */
    ChannelFuture finishConnectionClosure(int statusCode, String reason);

    /**
     * Finish the connection closure if a close frame has been received without this connection sending a close frame.
     *
     * @return the future to represent the completion of asynchronous frame sending
     */
    ChannelFuture finishConnectionClosure();

    /**
     * Terminate connection without close frame.
     *
     * @return Future to represent the completion of closure asynchronously
     */
    ChannelFuture terminateConnection();

    /**
     * Send a close frame and close the connection without waiting for a response.
     *
     * @param statusCode Status code to indicate the reason of closure
     *                   @see <a href="https://tools.ietf.org/html/rfc6455">WebSocket Protocol</a>
     * @param reason Reason to close the connection
     * @return Future to represent the completion of asynchronous frame sending
     */
    ChannelFuture terminateConnection(int statusCode, String reason);
}
