/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.http.nativeimpl.request;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.Constants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.runtime.message.MessageDataSource;
import org.ballerinalang.runtime.message.StringDataSource;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

/**
 * Get the Form params from HTTP message and return a map.
 *
 * @since 0.93
 */
@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "getFormParams",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Request",
                             structPackage = "ballerina.net.http"),
        returnType = {@ReturnType(type = TypeKind.MAP, elementType = TypeKind.STRING)},
        isPublic = true
)
public class GetFormParams extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {
        try {
            BStruct requestStruct  = ((BStruct) getRefArgument(context, 0));
            HTTPCarbonMessage httpCarbonMessage = HttpUtil
                    .getCarbonMsg(requestStruct, HttpUtil.createHttpCarbonMessage(true));

            String contentType = httpCarbonMessage.getHeader(Constants.CONTENT_TYPE);
            if (contentType != null && contentType.contains(Constants.APPLICATION_FORM)) {
                String payload;
                MessageDataSource messageDataSource = HttpUtil.getMessageDataSource(requestStruct);
                if (messageDataSource != null) {
                    payload = messageDataSource.getMessageAsString();
                } else {
                    payload = StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(httpCarbonMessage)
                            .getInputStream());
                    StringDataSource stringDataSource = new StringDataSource(payload);

                    requestStruct.addNativeData(Constants.MESSAGE_DATA_SOURCE, stringDataSource);
                }
                if (!payload.isEmpty()) {
                    return getBValues(HttpUtil.getParamMap(payload));
                } else {
                    throw new BallerinaException("empty message payload");
                }
            } else {
                throw new BallerinaException("unsupported media type");
            }
        } catch (Throwable e) {
            throw new BallerinaException("Error while retrieving form param from message: " + e.getMessage());
        }
    }
}
