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
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.runtime.Constants;
import org.ballerinalang.services.dispatchers.Session;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Headers;

import static org.ballerinalang.nativeimpl.actions.http.Constants.COOKIE_HEADER;

/**
 * Native function to set session attributes to the message.
 */
@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "setSessionAttribute",
        args = {@Argument(name = "m", type = TypeEnum.MESSAGE),
                @Argument(name = "attributeKey", type = TypeEnum.STRING),
                @Argument(name = "attributeValue", type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Sets session attributes to the message")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "m", value = "A message Object")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "attributeKey",
        value = "HTTPSession attribute key")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "attributeValue",
        value = "HTTPSession attribute Value")})
public class SetSessionAttribute extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
//        try {
            CarbonMessage carbonMessage = ((BMessage) getRefArgument(context, 0)).value();
            String attributeKey = getStringArgument(context, 0);
            String attributeValue = getStringArgument(context, 1);

            String cookieHeader = carbonMessage.getHeader(COOKIE_HEADER);
            Session session;

            if (cookieHeader != null) {
                if (context.getSessionManager().getHTTPSession(cookieHeader) != null) {
                    session = context.getSessionManager().getHTTPSession(cookieHeader);
                    session.setAttribute(attributeKey, attributeValue);
                } else {
                    throw new IllegalStateException();
                }
            } else {
                session = context.getSessionManager().createHTTPSession();
                session.setAttribute(attributeKey, attributeValue);
                carbonMessage.setHeader(COOKIE_HEADER, session.getId());

                // Set any intermediate headers set during ballerina execution
                if (carbonMessage.getProperty(Constants.INTERMEDIATE_HEADERS) != null) {
                    Headers headers = (Headers) carbonMessage.getProperty(Constants.INTERMEDIATE_HEADERS);
                    carbonMessage.setHeaders(headers.getAll());
                }
            }

//        } catch (Exception e) {
//            throw new BallerinaException("Exception while setting attribute");
//        }
        return VOID_RETURN;
    }
}
