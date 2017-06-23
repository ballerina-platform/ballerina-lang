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

package org.ballerinalang.nativeimpl.net.httpsession;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.services.dispatchers.session.Session;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.Set;

/**
 * Native function to get session attribute keys.
 */
@BallerinaFunction(
        packageName = "ballerina.net.httpsession",
        functionName = "getAttributeNames",
        args = {@Argument(name = "session", type = TypeEnum.STRUCT, structType = "Session",
                structPackage = "ballerina.net.httpsession")},
        returnType = {@ReturnType(type = TypeEnum.ARRAY)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Gets the session attribute names")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "session",
        value = "A session struct")})
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "string[]",
        value = "HTTPSession attribute name array") })
public class GetAttributeNames extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {
        try {
            BStruct sessionStruct  = ((BStruct) getRefArgument(context, 0));
            String sessionId = sessionStruct.getStringField(0);
            Session session = context.getCurrentSession();

            if (session != null && (sessionId.equals(session.getId()))) {
                return createBvalueArray(session.getAttributeNames());
            } else {
                if (sessionId != null) {
                    session = context.getSessionManager().getHTTPSession(sessionId);
                    if (session != null) {
                        return createBvalueArray(session.getAttributeNames());
                    } else {
                        //no session available bcz of the time out
                        throw new IllegalStateException("Session timeout");
                    }
                } else {
                    //no session available for particular cookie
                    throw new IllegalStateException("No session available");
                }

            }

        } catch (IllegalStateException e) {
            throw new BallerinaException(e);
        }
    }

    private BValue[] createBvalueArray(Set<String> attributeNames) {
        BValue[] arr = new BValue[attributeNames.size()];
        int i = 0;
        for (String name : attributeNames) {
            arr[i++] = new BString(name);
        }
        return arr;
    }
}
