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
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.util.MessageUtils;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.Constants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.runtime.message.StringDataSource;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Get the Form params from HTTP message and return a map.
 *
 * @since 0.93
 */
@BallerinaFunction(
        packageName = "ballerina.net.http.request",
        functionName = "getFormParams",
        args = {@Argument(name = "req", type = TypeEnum.STRUCT, structType = "Request",
                          structPackage = "ballerina.net.http")},
        returnType = {@ReturnType(type = TypeEnum.MAP, elementType = TypeEnum.STRING)},
        isPublic = true
)
public class GetFormParams extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {
        try {
            BStruct requestStruct  = ((BStruct) getRefArgument(context, 0));
            //TODO check below line
            HTTPCarbonMessage httpCarbonMessage = HttpUtil.getCarbonMsg(requestStruct, new HTTPCarbonMessage());
            String contentType = httpCarbonMessage.getHeader(Constants.CONTENT_TYPE);
            if (contentType != null && contentType.contains(Constants.APPLICATION_FORM)) {
                String payload;
                if (httpCarbonMessage.isAlreadyRead()) {
                    payload = httpCarbonMessage.getMessageDataSource().getMessageAsString();
                } else {
                    payload = MessageUtils.getStringFromInputStream(httpCarbonMessage.getInputStream());
                    StringDataSource stringDataSource = new StringDataSource(payload);
                    httpCarbonMessage.setMessageDataSource(stringDataSource);
                    httpCarbonMessage.setAlreadyRead(true);
                }
                if (!payload.isEmpty()) {
                    return getBValues(processFormParams(payload));
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

    private BMap<String, BString> processFormParams(String payload) throws UnsupportedEncodingException {
        BMap<String, BString> formParams = new BMap<>();
        String[] entries = payload.split("&");
        for (String entry : entries) {
            int index = entry.indexOf('=');
            if (index != -1) {
                String name = entry.substring(0, index).trim();
                String value = URLDecoder.decode(entry.substring(index + 1).trim(), "UTF-8");
                formParams.put(name, new BString(value));
            }
        }
        return formParams;
    }
}
