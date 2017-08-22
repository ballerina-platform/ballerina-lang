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

package org.ballerinalang.nativeimpl.net.http;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.util.MessageUtils;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.lang.utils.Constants;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.runtime.message.StringDataSource;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Get the Form params from HTTP message and return a map.
 *
 * @since 0.93
 */
@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "getFormParams",
        args = {@Argument(name = "m", type = TypeEnum.MESSAGE)},
        returnType = {@ReturnType(type = TypeEnum.MAP, elementType = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Gets formParam map from HTTP message") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "m",
        value = "The message object") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "map",
        value = "The map of form params") })
public class GetFormParams extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {
        try {
            BMessage msg = (BMessage) getRefArgument(context, 0);
            String contentType = msg.getHeader(Constants.CONTENT_TYPE);
            if (contentType != null && contentType.contains(Constants.APPLICATION_FORM)) {
                String payload;
                if (msg.value().isAlreadyRead()) {
                    payload = msg.value().getMessageDataSource().getMessageAsString();
                } else {
                    payload = MessageUtils.getStringFromInputStream(msg.value().getInputStream());
                    StringDataSource stringDataSource = new StringDataSource(payload);
                    msg.value().setMessageDataSource(stringDataSource);
                    msg.value().setAlreadyRead(true);
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
