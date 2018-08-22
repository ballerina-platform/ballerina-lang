/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http.nativeimpl.connection;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.DataContext;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.PipelineResponseListener;
import org.ballerinalang.net.http.caching.ResponseCacheControlStruct;
import org.ballerinalang.net.http.util.CacheUtils;
import org.ballerinalang.util.observability.ObservabilityUtils;
import org.ballerinalang.util.observability.ObserverContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.util.Optional;
import java.util.Queue;

import static org.ballerinalang.net.http.HttpConstants.HTTP_STATUS_CODE;
import static org.ballerinalang.net.http.HttpConstants.RESPONSE_CACHE_CONTROL_FIELD;
import static org.ballerinalang.net.http.HttpConstants.RESPONSE_STATUS_CODE_FIELD;
import static org.ballerinalang.util.observability.ObservabilityConstants.TAG_KEY_HTTP_STATUS_CODE;

/**
 * Extern function to respond back the caller with outbound response.
 *
 * @since 0.96
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "nativeRespond",
        args = {@Argument(name = "connection", type = TypeKind.OBJECT),
                @Argument(name = "res", type = TypeKind.OBJECT, structType = "Response",
                        structPackage = "ballerina/http")},
        returnType = @ReturnType(type = TypeKind.RECORD, structType = "HttpConnectorError",
                structPackage = "ballerina/http"),
        isPublic = true
)
public class Respond extends ConnectionAction {

    private static final Logger log = LoggerFactory.getLogger(Respond.class);

    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        BMap<String, BValue> connectionStruct = (BMap<String, BValue>) context.getRefArgument(0);
        HttpCarbonMessage inboundRequestMsg = HttpUtil.getCarbonMsg(connectionStruct, null);
        HttpUtil.checkFunctionValidity(connectionStruct, inboundRequestMsg);
        DataContext dataContext = new DataContext(context, callback, inboundRequestMsg);
        BMap<String, BValue> outboundResponseStruct = (BMap<String, BValue>) context.getRefArgument(1);
        HttpCarbonMessage outboundResponseMsg = HttpUtil
                .getCarbonMsg(outboundResponseStruct, HttpUtil.createHttpCarbonMessage(false));

        setCacheControlHeader(outboundResponseStruct, outboundResponseMsg);
        HttpUtil.prepareOutboundResponse(context, inboundRequestMsg, outboundResponseMsg, outboundResponseStruct);

        // Based on https://tools.ietf.org/html/rfc7232#section-4.1
        if (CacheUtils.isValidCachedResponse(outboundResponseMsg, inboundRequestMsg)) {
            outboundResponseMsg.setProperty(HTTP_STATUS_CODE, HttpResponseStatus.NOT_MODIFIED.code());
            outboundResponseMsg.removeHeader(HttpHeaderNames.CONTENT_LENGTH.toString());
            outboundResponseMsg.removeHeader(HttpHeaderNames.CONTENT_TYPE.toString());
            outboundResponseMsg.waitAndReleaseAllEntities();
            outboundResponseMsg.completeMessage();
        }

        Optional<ObserverContext> observerContext = ObservabilityUtils.getParentContext(context);
        observerContext.ifPresent(ctx -> ctx.addTag(TAG_KEY_HTTP_STATUS_CODE,
                String.valueOf(outboundResponseStruct.get(RESPONSE_STATUS_CODE_FIELD))));

        try {
            boolean pipeliningNeeded = true;
            String httpVersion = (String) inboundRequestMsg.getProperty(Constants.HTTP_VERSION);
            if (inboundRequestMsg.isKeepAlive() && !Constants.HTTP2_VERSION.equalsIgnoreCase(httpVersion)) {
                PipelinedResponse pipelinedResponse = new PipelinedResponse(inboundRequestMsg.getSequenceId(),
                        inboundRequestMsg, outboundResponseMsg, dataContext, outboundResponseStruct);
                outboundResponseMsg.setPipelineListener(new PipelineResponseListener());
                ChannelHandlerContext sourceContext = inboundRequestMsg.getSourceContext();
                Queue<PipelinedResponse> responseQueue = sourceContext.channel().attr(Constants.RESPONSE_QUEUE).get();
                Integer maxQueuedResponses = sourceContext.channel()
                        .attr(Constants.MAX_RESPONSES_ALLOWED_TO_BE_QUEUED).get();
                if (responseQueue.size() > maxQueuedResponses) {
                    //Cannot queue up indefinitely which might cause out of memory issues, so closing the connection
                    sourceContext.close();
                    return;
                }
                responseQueue.add(pipelinedResponse);
                while (!responseQueue.isEmpty()) {
                    Integer nextSequenceNumber = sourceContext.channel().attr(Constants.NEXT_SEQUENCE_NUMBER).get();
                    final PipelinedResponse queuedPipelinedResponse = responseQueue.peek();
                    int currentSequenceNumber = queuedPipelinedResponse.getSequenceId();
                    if (currentSequenceNumber != nextSequenceNumber) {
                        break;
                    }
                    responseQueue.remove();

                    //IMPORTANT: Do not increment the nextSequenceNumber after 'sendOutboundResponseRobust()' call
                    // under any circumstance.  nextSequenceNumber should be updated only when the last http
                    // content of this message has been written to the socket because in case if one response has
                    //delayed http contents, there's a good chance that the contents of another response will be sent
                    //out before its turn.
                    PipeliningHandler.sendOutboundResponseRobust(queuedPipelinedResponse.getDataContext(),
                            queuedPipelinedResponse.getInboundRequestMsg(),
                            queuedPipelinedResponse.getOutboundResponseStruct(),
                            queuedPipelinedResponse.getOutboundResponseMsg());
                }

            } else {
                sendOutboundResponseRobust(dataContext, inboundRequestMsg, outboundResponseStruct, outboundResponseMsg);
            }

        } catch (EncoderException e) {
            //Exception is already notified by http transport.
            log.debug("Couldn't complete outbound response", e);
        }
    }

    private void setCacheControlHeader(BMap<String, BValue> outboundRespStruct, HttpCarbonMessage outboundResponse) {
        BMap<String, BValue> cacheControl =
                (BMap<String, BValue>) outboundRespStruct.get(RESPONSE_CACHE_CONTROL_FIELD);
        if (cacheControl != null &&
                outboundResponse.getHeader(HttpHeaderNames.CACHE_CONTROL.toString()) == null) {
            ResponseCacheControlStruct respCC = new ResponseCacheControlStruct(cacheControl);
            outboundResponse.setHeader(HttpHeaderNames.CACHE_CONTROL.toString(), respCC.buildCacheControlDirectives());
        }
    }
}
