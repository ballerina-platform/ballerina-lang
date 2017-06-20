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
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.services.dispatchers.session.Session;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.awt.geom.IllegalPathStateException;
import java.util.IllegalFormatException;

/**
 * Native function to set session cookie scope.
 */
@BallerinaFunction(
        packageName = "ballerina.net.httpsession",
        functionName = "setPath",
        args = {@Argument(name = "session", type = TypeEnum.STRUCT, structType = "Session",
                structPackage = "ballerina.net.httpsession"),
                @Argument(name = "path", type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Sets session cookie scope")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "session", value = "A session struct")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "path",
        value = "Session available Path")})
public class SetPath extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        try {
            BStruct sessionStruct = ((BStruct) getRefArgument(context, 0));
            String path = getStringArgument(context, 0);

            //check path validity
            if (!pathValidity(context, path)) {
                throw new IllegalPathStateException("Path: " + path + "not according to the format");
            }

            String sessionId = sessionStruct.getStringField(0);
            Session session = context.getSessioContext().getCurrentSession();

            //return value from cached session
            if (session != null) {
                session.setSessionScope(path);
            }

            //if session is not available in cache
            if (sessionId != null) {
                session = context.getSessioContext().getSessionManager().getHTTPSession(sessionId);
                if (session != null) {
                    session.setSessionScope(path);
                } else {
                    //no session available bcz of the time out
                    throw new IllegalStateException("Session timeout");
                }
            } else {
                //no session available for particular cookie
                throw new IllegalStateException("No session available");
            }
        } catch (Exception e) {
            throw new BallerinaException(e);
        }
        return VOID_RETURN;
    }

    private boolean pathValidity(Context context, String path) {
        if (path.equals("")) {
            return false;
        } else {
            return context.getSessioContext().getPathMatcher().pathValidityCheck(context, path);
        }
    }
}
