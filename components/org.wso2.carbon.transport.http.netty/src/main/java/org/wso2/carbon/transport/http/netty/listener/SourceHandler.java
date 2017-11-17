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
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.common.Util;
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.carbon.transport.http.netty.contractimpl.HttpResponseListener;
import org.wso2.carbon.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.carbon.transport.http.netty.internal.HandlerExecutor;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.carbon.transport.http.netty.message.HttpCarbonRequest;

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
    private HTTPCarbonMessage sourceReqCmsg;
    private Map<String, GenericObjectPool> targetChannelPool;
    private ServerConnectorFuture serverConnectorFuture;
    private String interfaceId;
    private HandlerExecutor handlerExecutor;

    public SourceHandler(ServerConnectorFuture serverConnectorFuture, String interfaceId) throws Exception {
        this.serverConnectorFuture = serverConnectorFuture;
        this.interfaceId = interfaceId;
        this.targetChannelPool = new ConcurrentHashMap<>();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        // Start the server connection Timer
        this.handlerExecutor = HTTPTransportContextHolder.getInstance().getHandlerExecutor();
        if (this.handlerExecutor != null) {
            this.handlerExecutor.executeAtSourceConnectionInitiation(Integer.toString(ctx.hashCode()));
        }
        this.ctx = ctx;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof FullHttpMessage) {
            FullHttpMessage fullHttpMessage = (FullHttpMessage) msg;
            sourceReqCmsg = setupCarbonMessage(fullHttpMessage);
            notifyRequestListener(sourceReqCmsg, ctx);
            ByteBuf content = ((FullHttpMessage) msg).content();
            sourceReqCmsg.addHttpContent(new DefaultLastHttpContent(content));
            if (handlerExecutor != null) {
                handlerExecutor.executeAtSourceRequestSending(sourceReqCmsg);
            }

        } else if (msg instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) msg;
            sourceReqCmsg = setupCarbonMessage(httpRequest);
            notifyRequestListener(sourceReqCmsg, ctx);
        } else {
            if (sourceReqCmsg != null) {
                if (msg instanceof HttpContent) {
                    HttpContent httpContent = (HttpContent) msg;
                    sourceReqCmsg.addHttpContent(httpContent);
                    if (Util.isLastHttpContent(httpContent)) {
                        if (handlerExecutor != null) {
                            handlerExecutor.executeAtSourceRequestSending(sourceReqCmsg);
                        }
                    }
                }
            }
        }
    }

    //Carbon Message is published to registered message processor and Message Processor should return transport thread
    //immediately
    private void notifyRequestListener(HTTPCarbonMessage httpRequestMsg, ChannelHandlerContext ctx)
            throws URISyntaxException {

        if (handlerExecutor != null) {
            handlerExecutor.executeAtSourceRequestReceiving(httpRequestMsg);
        }

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

        boolean continueRequest = true;
        if (continueRequest) {
            if (serverConnectorFuture != null) {
                try {
                    ServerConnectorFuture serverConnectorFuture = httpRequestMsg.getHttpResponseFuture();
                    serverConnectorFuture.setHttpConnectorListener(new HttpResponseListener(ctx, httpRequestMsg));
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
        if (handlerExecutor != null) {
            handlerExecutor.executeAtSourceConnectionTermination(Integer.toString(ctx.hashCode()));
            handlerExecutor = null;
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

    private HTTPCarbonMessage setupCarbonMessage(HttpMessage httpMessage) throws URISyntaxException {

        if (handlerExecutor != null) {
            handlerExecutor.executeAtSourceRequestReceiving(sourceReqCmsg);
        }

        sourceReqCmsg = new HttpCarbonRequest((HttpRequest) httpMessage);

        HttpRequest httpRequest = (HttpRequest) httpMessage;
        sourceReqCmsg.setProperty(Constants.CHNL_HNDLR_CTX, this.ctx);
        sourceReqCmsg.setProperty(Constants.SRC_HANDLER, this);
        sourceReqCmsg.setProperty(Constants.HTTP_VERSION, httpRequest.getProtocolVersion().text());
        sourceReqCmsg.setProperty(Constants.HTTP_METHOD, httpRequest.getMethod().name());

        InetSocketAddress localAddress = (InetSocketAddress) ctx.channel().localAddress();
        sourceReqCmsg.setProperty(org.wso2.carbon.messaging.Constants.LISTENER_PORT, localAddress.getPort());
        sourceReqCmsg.setProperty(org.wso2.carbon.messaging.Constants.LISTENER_INTERFACE_ID, interfaceId);
        sourceReqCmsg.setProperty(org.wso2.carbon.messaging.Constants.PROTOCOL, Constants.HTTP_SCHEME);

        boolean isSecuredConnection = false;
        if (ctx.channel().pipeline().get(Constants.SSL_HANDLER) != null) {
            isSecuredConnection = true;
        }
        sourceReqCmsg.setProperty(Constants.IS_SECURED_CONNECTION, isSecuredConnection);

        sourceReqCmsg.setProperty(Constants.LOCAL_ADDRESS, ctx.channel().localAddress());
        sourceReqCmsg.setProperty(Constants.REQUEST_URL, httpRequest.getUri());
        sourceReqCmsg.setProperty(Constants.TO, httpRequest.getUri());
        //Added protocol name as a string

        return sourceReqCmsg;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ctx.close();
        }
    }
}
