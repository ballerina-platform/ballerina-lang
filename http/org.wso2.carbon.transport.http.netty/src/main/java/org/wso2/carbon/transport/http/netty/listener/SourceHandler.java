/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.transport.http.netty.listener;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.common.Util;
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.carbon.transport.http.netty.contractimpl.HttpResponseListener;
import org.wso2.carbon.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Class responsible for handle  incoming message through netty inbound pipeline.
 */
public class SourceHandler extends ChannelInboundHandlerAdapter {
    private static Logger log = LoggerFactory.getLogger(SourceHandler.class);

    protected ChannelHandlerContext ctx;
    private HTTPCarbonMessage cMsg;
    private Map<String, GenericObjectPool> targetChannelPool = new ConcurrentHashMap<>();
    private ServerConnectorFuture serverConnectorFuture;
    private String interfaceId;

    public SourceHandler(ServerConnectorFuture serverConnectorFuture, String interfaceId)
            throws Exception {
        this.serverConnectorFuture = serverConnectorFuture;
        this.interfaceId = interfaceId;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        this.ctx = ctx;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        // Start the server connection Timer
        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
            HTTPTransportContextHolder.getInstance().getHandlerExecutor()
                    .executeAtSourceConnectionInitiation(Integer.toString(ctx.hashCode()));
        }
        this.ctx = ctx;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof FullHttpMessage) {
            FullHttpMessage fullHttpMessage = (FullHttpMessage) msg;
            cMsg = setupCarbonMessage(fullHttpMessage);
            publishToMessageProcessor(cMsg);
            ByteBuf content = ((FullHttpMessage) msg).content();
            cMsg.addHttpContent(new DefaultLastHttpContent(content));
            cMsg.setEndOfMsgAdded(true);
//            if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
//                HTTPTransportContextHolder.getInstance().getHandlerExecutor().executeAtSourceRequestSending(cMsg);
//            }

        } else if (msg instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) msg;
            cMsg = (HTTPCarbonMessage) setupCarbonMessage(httpRequest);
            publishToMessageProcessor(cMsg);

        } else {
            if (cMsg != null) {
                if (msg instanceof HttpContent) {
                    HttpContent httpContent = (HttpContent) msg;
                    cMsg.addHttpContent(httpContent);
                    if (msg instanceof LastHttpContent) {
                        cMsg.setEndOfMsgAdded(true);
//                        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
//                            HTTPTransportContextHolder.getInstance().getHandlerExecutor().
//                                    executeAtSourceRequestSending(cMsg);
//                        }
                    }
                }
            }
        }
    }

    //Carbon Message is published to registered message processor and Message Processor should return transport thread
    //immediately
    protected void publishToMessageProcessor(HTTPCarbonMessage httpRequestMsg) throws URISyntaxException {
        // TODO: Revisit when the refactor is complete
//        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
//            HTTPTransportContextHolder.getInstance().getHandlerExecutor().
//                    executeAtSourceRequestReceiving(httpRequestMsg);
//        }

        boolean continueRequest = true;

//        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
//
//            continueRequest = HTTPTransportContextHolder.getInstance().getHandlerExecutor()
//                    .executeRequestContinuationValidator(httpRequestMsg, carbonMessage -> {
//                        CarbonCallback responseCallback = (CarbonCallback) httpRequestMsg
//                                .getProperty(org.wso2.carbon.messaging.Constants.CALL_BACK);
//                        responseCallback.done(carbonMessage);
//                    });
//
//        }
        if (continueRequest) {
            if (serverConnectorFuture != null) {
                try {
                    ServerConnectorFuture serverConnectorFuture = httpRequestMsg.getHTTPConnectorFuture();
                    serverConnectorFuture.setHttpConnectorListener(new HttpResponseListener(this.ctx, httpRequestMsg));
                    this.serverConnectorFuture.notifyHttpListener(httpRequestMsg);
                } catch (Exception e) {
                    log.error("Error while notifying listeners", e);
                }
            } else {
                log.error("Cannot find registered listener to forward the message");
            }
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // Stop the connector timer
        ctx.close();
        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
            HTTPTransportContextHolder.getInstance().getHandlerExecutor()
                    .executeAtSourceConnectionTermination(Integer.toString(ctx.hashCode()));
        }

        targetChannelPool.forEach((k, genericObjectPool) -> {
            try {
                targetChannelPool.remove(k).close();
            } catch (Exception e) {
                log.error("Couldn't close target channel socket connections", e);
            }
        });
    }

    public Map<String, GenericObjectPool> getTargetChannelPool() {
        return targetChannelPool;
    }

    public ChannelHandlerContext getInboundChannelContext() {
        return ctx;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (ctx != null && ctx.channel().isActive()) {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
        serverConnectorFuture.notifyErrorListener(cause);
    }

    protected HTTPCarbonMessage setupCarbonMessage(HttpMessage httpMessage) throws URISyntaxException {
        cMsg = new HTTPCarbonMessage();
        boolean isSecuredConnection = false;
//        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
//            HTTPTransportContextHolder.getInstance().getHandlerExecutor().executeAtSourceRequestReceiving(cMsg);
//        }

        HttpRequest httpRequest = (HttpRequest) httpMessage;
        cMsg.setProperty(Constants.CHNL_HNDLR_CTX, this.ctx);
        cMsg.setProperty(Constants.SRC_HANDLER, this);
        cMsg.setProperty(Constants.HTTP_VERSION, httpRequest.getProtocolVersion().text());
        cMsg.setProperty(Constants.HTTP_METHOD, httpRequest.getMethod().name());

        InetSocketAddress localAddress = (InetSocketAddress) ctx.channel().localAddress();
        cMsg.setProperty(org.wso2.carbon.messaging.Constants.LISTENER_PORT, localAddress.getPort());
        cMsg.setProperty(org.wso2.carbon.messaging.Constants.LISTENER_INTERFACE_ID, interfaceId);
        cMsg.setProperty(org.wso2.carbon.messaging.Constants.PROTOCOL, Constants.HTTP_SCHEME);
        if (ctx.channel().pipeline().get(Constants.SSL_HANDLER) != null) {
            isSecuredConnection = true;
        }
        cMsg.setProperty(Constants.IS_SECURED_CONNECTION, isSecuredConnection);
        cMsg.setProperty(Constants.LOCAL_ADDRESS, ctx.channel().localAddress());

        cMsg.setProperty(Constants.REQUEST_URL, httpRequest.getUri());
        cMsg.setProperty(Constants.TO, httpRequest.getUri());
        cMsg.setHeaders(Util.getHeaders(httpRequest).getAll());
        //Added protocol name as a string

        return cMsg;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ctx.close();
        }
    }
}
