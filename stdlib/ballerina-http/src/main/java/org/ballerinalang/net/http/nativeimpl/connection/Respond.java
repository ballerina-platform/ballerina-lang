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

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.DataContext;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.caching.ResponseCacheControlStruct;
import org.ballerinalang.net.http.util.CacheUtils;
import org.ballerinalang.util.observability.ObservabilityUtils;
import org.ballerinalang.util.observability.ObserverContext;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import static org.ballerinalang.net.http.HttpConstants.HTTP_STATUS_CODE;
import static org.ballerinalang.net.http.HttpConstants.RESPONSE_CACHE_CONTROL_INDEX;
import static org.ballerinalang.net.http.HttpConstants.RESPONSE_STATUS_CODE_INDEX;
import static org.ballerinalang.util.observability.ObservabilityConstants.TAG_KEY_HTTP_STATUS_CODE;

/**
 * Native function to respond back the caller with outbound response.
 *
 * @since 0.96
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "respond",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Connection",
                structPackage = "ballerina.http"),
        args = {@Argument(name = "res", type = TypeKind.STRUCT, structType = "Response",
                structPackage = "ballerina.http")},
        returnType = @ReturnType(type = TypeKind.STRUCT, structType = "HttpConnectorError",
                structPackage = "ballerina.http"),
        isPublic = true
)
public class Respond extends ConnectionAction {

    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        DataContext dataContext = new DataContext(context, callback);
        BStruct connectionStruct = (BStruct) context.getRefArgument(0);
        HTTPCarbonMessage inboundRequestMsg = HttpUtil.getCarbonMsg(connectionStruct, null);
        HttpUtil.checkFunctionValidity(connectionStruct, inboundRequestMsg);

        BStruct outboundResponseStruct = (BStruct) context.getRefArgument(1);
        HTTPCarbonMessage outboundResponseMsg = HttpUtil
                .getCarbonMsg(outboundResponseStruct, HttpUtil.createHttpCarbonMessage(false));

        setCacheControlHeader(outboundResponseStruct, outboundResponseMsg);
        HttpUtil.prepareOutboundResponse(context, inboundRequestMsg, outboundResponseMsg, outboundResponseStruct);

        if (CacheUtils.isValidCachedResponse(outboundResponseMsg, inboundRequestMsg)) {
            outboundResponseMsg.setProperty(HTTP_STATUS_CODE, HttpResponseStatus.NOT_MODIFIED.code());
            outboundResponseMsg.waitAndReleaseAllEntities();
            outboundResponseMsg.completeMessage();
        }

        if (ObservabilityUtils.isObservabilityEnabled()) {
            ObserverContext observerContext = ObservabilityUtils.getParentContext(context);
            observerContext.addTag(TAG_KEY_HTTP_STATUS_CODE, String.valueOf(outboundResponseStruct.
                    getIntField(RESPONSE_STATUS_CODE_INDEX)));
        }
        sendOutboundResponseRobust(dataContext, inboundRequestMsg, outboundResponseStruct, outboundResponseMsg);
    }

    private void setCacheControlHeader(BStruct outboundRespStruct, HTTPCarbonMessage outboundResponse) {
        BStruct cacheControl = (BStruct) outboundRespStruct.getRefField(RESPONSE_CACHE_CONTROL_INDEX);
        if (cacheControl != null &&
                outboundResponse.getHeader(HttpHeaderNames.CACHE_CONTROL.toString()) == null) {
            ResponseCacheControlStruct respCC = new ResponseCacheControlStruct(cacheControl);
            outboundResponse.setHeader(HttpHeaderNames.CACHE_CONTROL.toString(), respCC.buildCacheControlDirectives());
        }
    }
}
