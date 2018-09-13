package org.wso2.transport.http.netty.listener.states.listener.http2;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http2.Http2Exception;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.Http2OutboundRespListener;
import org.wso2.transport.http.netty.message.Http2DataFrame;
import org.wso2.transport.http.netty.message.Http2HeadersFrame;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

/**
 * ListenerStates of HTTP/2 source handler.
 */
public interface ListenerState {

    /**
     * Read headers of inbound request.
     *
     * @param headersFrame {@link Http2HeadersFrame} which represents the inbound header frame
     */
    void readInboundRequestHeaders(Http2HeadersFrame headersFrame) throws Http2Exception;

    /**
     * Read entity body of inbound request.
     *
     * @param dataFrame {@link Http2DataFrame} which represents the inbound data frame
     * @throws ServerConnectorException if an error occurs while notifying to server connector future
     */
    void readInboundRequestBody(Http2DataFrame dataFrame) throws ServerConnectorException;

    /**
     * Write headers of outbound response.
     *
     * @param outboundResponseMsg {@link HttpCarbonMessage} which represents the outbound message
     * @param httpContent         the initial content of the entity body
     * @throws Http2Exception if an error occurs while writing
     */
    void writeOutboundResponseHeaders(HttpCarbonMessage outboundResponseMsg, HttpContent httpContent)
            throws Http2Exception;

    /**
     * Write entity body of outbound response.
     *
     * @param http2OutboundRespListener outbound response listener of response future
     * @param outboundResponseMsg       {@link HttpCarbonMessage} which represents the outbound message
     * @param httpContent               the content of the entity body
     * @throws Http2Exception if an error occurs while writing
     */
    void writeOutboundResponseBody(Http2OutboundRespListener http2OutboundRespListener,
                                   HttpCarbonMessage outboundResponseMsg, HttpContent httpContent)
            throws Http2Exception;
}
