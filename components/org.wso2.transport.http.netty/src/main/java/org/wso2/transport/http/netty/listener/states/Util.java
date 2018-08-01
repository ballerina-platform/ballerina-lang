/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.listener.states;


/**
 * State between connection creation and start of inbound request header read
 */
public class Util {

//    private ChannelFuture sendRequestTimeoutResponse(ChannelHandlerContext ctx, HttpResponseStatus status,
//                                                     ByteBuf content, int length) {
//        HttpResponse outboundResponse;
//        if (inboundRequestMsg != null) {
//            float httpVersion = Float.parseFloat((String) inboundRequestMsg.getProperty(Constants.HTTP_VERSION));
//            if (httpVersion == Constants.HTTP_1_0) {
//                outboundResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, status, content);
//            } else {
//                outboundResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
//            }
//        } else {
//            outboundResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
//        }
//        outboundResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, length);
//        outboundResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, Constants.TEXT_PLAIN);
//        outboundResponse.headers().set(HttpHeaderNames.CONNECTION.toString(), Constants.CONNECTION_CLOSE);
//        outboundResponse.headers().set(HttpHeaderNames.SERVER.toString(), sourceHandler.getServerName());
//        return ctx.channel().writeAndFlush(outboundResponse);
//    }
//
//    private void handleIncompleteInboundRequest(String errorMessage) {
//        LastHttpContent lastHttpContent = new DefaultLastHttpContent();
//        lastHttpContent.setDecoderResult(DecoderResult.failure(new DecoderException(errorMessage)));
//        this.inboundRequestMsg.addHttpContent(lastHttpContent);
//        log.warn(errorMessage);
//    }
}
