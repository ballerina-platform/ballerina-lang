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

package org.ballerinalang.nativeimpl.net.http.session;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.services.dispatchers.session.Session;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.IllegalFormatException;

/**
 * Native function to set session attributes to the message.
 *
 * @since 0.89
 */
@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "setAttribute",
        args = {@Argument(name = "session", type = TypeEnum.STRUCT, structType = "Session",
                structPackage = "ballerina.net.http"),
                @Argument(name = "attributeKey", type = TypeEnum.STRING),
                @Argument(name = "attributeValue", type = TypeEnum.ANY)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Sets session attributes to the message")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "session", value = "A session struct")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "attributeKey",
        value = "HTTPSession attribute key")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "attributeValue",
        value = "HTTPSession attribute Value")})
public class SetAttribute extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) throws IllegalFormatException {
        try {
            BStruct sessionStruct  = ((BStruct) getRefArgument(context, 0));
            String attributeKey = getStringArgument(context, 0);
            BValue attributeValue = getRefArgument(context, 1);
            String sessionId = sessionStruct.getStringField(0);
            Session session = context.getCurrentSession();

            if (attributeKey == null || attributeValue == null) {
                throw new NullPointerException("Failed to set attribute: Attribute key: "
                        + attributeKey + "Attribute Value: " + attributeValue);
            }
            //return value from cached session
            if (session != null && (sessionId.equals(session.getId()))) {
                session.setAttribute(attributeKey, attributeValue);
            } else {
                session = context.getSessionManager().getHTTPSession(sessionId);
                if (session != null) {
                    session.setAttribute(attributeKey, attributeValue);
                } else {
                    //no session available bcz of the time out
                    throw new IllegalStateException("Failed to set attribute: No such session in progress");
                }
            }
        } catch (IllegalStateException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
        return VOID_RETURN;
    }
}
