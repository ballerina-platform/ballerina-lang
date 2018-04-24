package org.wso2.transport.http.netty.contract.websocket;

import io.netty.channel.ChannelFuture;

import java.nio.ByteBuffer;
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
    @Deprecated
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

    /**
     * Push text frame to the WebSocket connection asynchronously.
     *
     * @param text text to be sent.
     * @return Future to represent the completion of asynchronous frame sending.
     */
    ChannelFuture pushText(String text);

    /**
     * Push text frame to the WebSocket connection asynchronously.
     *
     * @param text text to be sent.
     * @param finalFrame true if sending final frame.
     * @return Future to represent the completion of asynchronous frame sending.
     */
    ChannelFuture pushText(String text, boolean finalFrame);

    /**
     * Push binary frame to the WebSocket connection asynchronously.
     *
     * @param data binary data to be sent.
     * @return Future to represent the completion of asynchronous frame sending.
     */
    ChannelFuture pushBinary(ByteBuffer data);

    /**
     * Push binary frame to the WebSocket connection asynchronously.
     *
     * @param data binary data to be sent.
     * @param finalFrame true if sending final frame.
     * @return Future to represent the completion of asynchronous frame sending.
     */
    ChannelFuture pushBinary(ByteBuffer data, boolean finalFrame);

    /**
     * Ping remote endpoint asynchronously.
     *
     * @param data data to be sent with ping frame.
     * @return Future to represent the completion of asynchronous frame sending.
     */
    ChannelFuture ping(ByteBuffer data);

    /**
     * Send pong to remote endpoint asynchronously.
     *
     * @param data data to be sent with ping frame.
     * @return Future to represent the completion of asynchronous frame sending.
     */
    ChannelFuture pong(ByteBuffer data);

    /**
     * Close connection with close frame.
     *
     * @param statusCode Status code to indicate the reason of closure
     *                   @see <a href="https://tools.ietf.org/html/rfc6455">WebSocket Protocol</a>
     * @param reason Reason to close the connection.
     * @return Future to represent the completion of asynchronous frame sending.
     */
    ChannelFuture close(int statusCode, String reason);

    /**
     * Close connection without close frame.
     *
     * @return Future to represent the completion of closure asynchronously.
     */
    ChannelFuture close();
}
