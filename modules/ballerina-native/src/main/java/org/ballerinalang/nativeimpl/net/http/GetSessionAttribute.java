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
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.runtime.Constants;
import org.ballerinalang.services.dispatchers.Session;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Headers;

import static org.ballerinalang.nativeimpl.actions.http.Constants.COOKIE_HEADER;

/**
 * Native function to get session attribute.
 */
@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "getSessionAttribute",
        args = {@Argument(name = "m", type = TypeEnum.MESSAGE),
                @Argument(name = "attributeKey", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Gets the session attribute")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "m", value = "A message Object")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "attributeKey",
        value = "HTTPSession attribute key")})
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "string",
        value = "HTTPSession attribute value") })
public class GetSessionAttribute extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {
        try {
            CarbonMessage carbonMessage = ((BMessage) getRefArgument(context, 0)).value();
            String attributeKey = getStringArgument(context, 0);
            String cookieHeader = carbonMessage.getHeader(COOKIE_HEADER);
            Session session;

            if (cookieHeader != null) {
                session = context.getSessionManager().getHTTPSession(cookieHeader);
                if (session != null) {
                    return getBValues(new BString(session.getAttributeValue(attributeKey)));
                } else {
                    //no session available bcz of the time out
                    throw new IllegalStateException("Session timeout");
                }
            } else {
                //not a request with cookie header
                session = context.getSessionManager().createHTTPSession();
                carbonMessage.setHeader(COOKIE_HEADER, session.getId());

                // Set any intermediate headers set during ballerina execution
                if (carbonMessage.getProperty(Constants.INTERMEDIATE_HEADERS) != null) {
                    Headers headers = (Headers) carbonMessage.getProperty(Constants.INTERMEDIATE_HEADERS);
                    carbonMessage.setHeaders(headers.getAll());
                }
                return VOID_RETURN;
            }
        } catch (Exception e) {
            throw new BallerinaException(e);

        }
    }
}
