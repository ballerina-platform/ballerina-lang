/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.transport.http.netty.listener;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.config.RequestSizeValidationConfiguration;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * custom Http Request Decoder to validate the header size.
 */
public class CustomHttpRequestDecoder extends HttpRequestDecoder {
    private static final Logger log = LoggerFactory.getLogger(CustomHttpRequestDecoder.class);

    public CustomHttpRequestDecoder() {
        super(RequestSizeValidationConfiguration.getInstance().getHeaderMaxRequestLineSize(),
                RequestSizeValidationConfiguration.getInstance().getHeaderMaxSize(),
                RequestSizeValidationConfiguration.getInstance().getMaxChunkSize(), true);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        super.decode(ctx, buffer, out);

        for (Object o : out) {
            if (o instanceof DefaultHttpRequest) {
                DefaultHttpRequest httpRequest = (DefaultHttpRequest) o;
                if (httpRequest.getDecoderResult().isFailure() && httpRequest.getDecoderResult()
                        .cause() instanceof TooLongFrameException) {

                    log.warn("Header size is larger than the valid limit");

                    Iterator<Map.Entry<String, ChannelHandler>> iterator = ctx.pipeline().iterator();

                    boolean canRemove = false;
                    while (iterator.hasNext()) {
                        Map.Entry<String, ChannelHandler> channelHandlerEntry = iterator.next();
                        if (channelHandlerEntry.getKey().equalsIgnoreCase(ctx.name())) {
                            canRemove = true;
                        }
                        if (canRemove && !channelHandlerEntry.getKey().equalsIgnoreCase(ctx.name())) {
                            ctx.pipeline().remove(channelHandlerEntry.getKey());
                        }
                    }

                    String rejectMessage = RequestSizeValidationConfiguration.getInstance().getHeaderRejectMessage();
                    byte[] errorMessageBytes = rejectMessage.getBytes(Charset.defaultCharset());
                    ByteBuf content = Unpooled.wrappedBuffer(errorMessageBytes);
                    DefaultFullHttpResponse rejectResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                            HttpResponseStatus.valueOf(
                                    RequestSizeValidationConfiguration.getInstance().getHeaderRejectStatusCode()),
                            content);
                    rejectResponse.headers().set(Constants.HTTP_CONTENT_LENGTH, errorMessageBytes.length);
                    rejectResponse.headers().set(Constants.HTTP_CONTENT_TYPE,
                            RequestSizeValidationConfiguration.getInstance().getHeaderRejectMsgContentType());

                    ctx.writeAndFlush(rejectResponse);
                    break;
                }
            }
        }
    }
}
