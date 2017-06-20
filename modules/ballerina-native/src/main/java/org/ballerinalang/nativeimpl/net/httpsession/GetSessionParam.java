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
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.services.dispatchers.session.Session;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.Arrays;

import static org.ballerinalang.services.dispatchers.http.Constants.COOKIE_HEADER;
import static org.ballerinalang.services.dispatchers.http.Constants.SESSION_ID;

/**
 * Native function to get session with boolean parameter.
 */
@BallerinaFunction(
        packageName = "ballerina.net.httpsession",
        functionName = "getSessionWithParam",
        args = {@Argument(name = "m", type = TypeEnum.MESSAGE),
                @Argument(name = "create", type = TypeEnum.BOOLEAN)},
        returnType = {@ReturnType(type = TypeEnum.STRUCT, structType = "Session",
                structPackage = "ballerina.net.httpsession")},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Gets the session struct") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "m", value = "A message Object")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "create",
        value = "Create a new session or not") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "Session",
        value = "HTTP session struct") })
public class GetSessionParam extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        try {
            CarbonMessage carbonMessage = ((BMessage) getRefArgument(context, 0)).value();
            Boolean create = getBooleanArgument(context, 0);
            String cookieHeader = carbonMessage.getHeader(COOKIE_HEADER);
            Session session = context.getSessioContext().getCurrentSession();

            if (session != null) {
                session = session.setAccessed();
                return new BValue[]{GetSession.createSessionStruct(context, session)};
            }

            if (cookieHeader != null) {
                try {
                    session = Arrays.stream(cookieHeader.split(";"))
                            .filter(cookie -> cookie.startsWith(SESSION_ID))
                            .findFirst()
                            .map(jsession -> context.getSessioContext().getSessionManager()
                                    .getHTTPSession(jsession.substring(SESSION_ID.length()))).get();
                } catch (Exception e) {
                    new IllegalStateException(e);
                }
                if (session != null) {
                    session.setNew(false);
                } else {
                    if (create) {
                        context.getSessioContext().getSessionManager().createHTTPSession();
                    }
                    return null;
                }
                session.setAccessed();
            } else if (create) {
                session = context.getSessioContext().getSessionManager().createHTTPSession();
            }
            carbonMessage.removeHeader(COOKIE_HEADER);
            if (session != null) {
                context.getSessioContext().setCurrentSession(session);
                return new BValue[]{GetSession.createSessionStruct(context, session)};
            }
            return new BValue[]{};

        } catch (Exception e) {
            throw new BallerinaException(e);
        }
    }
}


/*

@doc:Description { value:"Gets the session struct"}
@doc:Param { value:"m: A message object" }
@doc:Param { value:"create: Create a new session or not" }
@doc:Return { value:"Session: HTTP session struct" }
native function getSessionWithParam (message m, Boolean create) (Session);

*/
