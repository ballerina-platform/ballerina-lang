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

package org.ballerinalang.net.http.nativeimpl.session;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.Constants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.session.Session;
import org.ballerinalang.net.http.session.SessionManager;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * Native function to get session if valid session id exist.
 *
 * @since 0.89
 */
@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "getSession",
        args = {@Argument(name = "req", type = TypeKind.STRUCT, structType = "Request",
                structPackage = "ballerina.net.http")},
        returnType = {@ReturnType(type = TypeKind.STRUCT, structType = "Session",
                                  structPackage = "ballerina.net.http")},
        isPublic = true
)
public class GetSession extends AbstractNativeFunction {

    private static final Logger logger = LoggerFactory.getLogger(GetSession.class);

    @Override
    public BValue[] execute(Context context) {
        try {
            BStruct requestStruct  = ((BStruct) getRefArgument(context, 0));
            //TODO check below line
            HTTPCarbonMessage httpCarbonMessage = HttpUtil.getCarbonMsg(requestStruct, null);
            String cookieHeader = httpCarbonMessage.getHeader(Constants.COOKIE_HEADER);
            String path = (String) httpCarbonMessage.getProperty(Constants.BASE_PATH);
            Session session = (Session) requestStruct.getNativeData(Constants.HTTP_SESSION);

            if (cookieHeader != null) {
                try {
                    String sessionId = Arrays.stream(cookieHeader.split(";"))
                                        .filter(cookie -> cookie.startsWith(Constants.SESSION_ID))
                                        .findFirst().get().substring(Constants.SESSION_ID.length());
                    //return value from cached session
                    if (session != null && (sessionId.equals(session.getId()))) {
                        session = session.setAccessed();
                        return new BValue[]{CreateSessionIfAbsent.createSessionStruct(context, session)};
                    }
                    session = SessionManager.getInstance().getHTTPSession(sessionId);
                } catch (NoSuchElementException e) {
                    //ignore throwable
                    logger.info("Failed to get session: Incorrect Session cookie");
                }
                if (session == null) {
                    //Return null as the session is invalidated.
                    logger.info("Failed to get session: session is not available");
                    return new BValue[]{};
                }
                //path Validity check
                if (session != null && session.getPath().equals(path)) {
                    session.setNew(false);
                    session.setAccessed();
                } else {
                    throw new BallerinaException("Failed to get session: " + path + " is not an allowed path");
                }
            } else {
                //Return null as the session cookie is not available.
                logger.info("Failed to get session: session cookie is not available");
                return new BValue[]{};
            }
            httpCarbonMessage.removeHeader(Constants.COOKIE_HEADER);
            requestStruct.addNativeData(Constants.HTTP_SESSION, session);
            return new BValue[]{CreateSessionIfAbsent.createSessionStruct(context, session)};
        } catch (IllegalStateException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }
}

