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
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.session.Session;
import org.ballerinalang.net.http.session.SessionManager;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.util.NoSuchElementException;

/**
 * Native function to get session if valid session id exist.
 *
 * @since 0.89
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "getSession",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Request",
                structPackage = "ballerina.http"),
        returnType = {@ReturnType(type = TypeKind.STRUCT, structType = "Session",
                                  structPackage = "ballerina.http")},
        isPublic = true
)
public class GetSession extends BlockingNativeCallableUnit {

    private static final Logger logger = LoggerFactory.getLogger(GetSession.class);

    @Override
    public void execute(Context context) {
        try {
            BStruct requestStruct  = ((BStruct) context.getRefArgument(0));
            //TODO check below line
            HTTPCarbonMessage httpCarbonMessage = HttpUtil.getCarbonMsg(requestStruct, null);
            String cookieHeader = httpCarbonMessage.getHeader(HttpConstants.COOKIE_HEADER);
            String path = (String) httpCarbonMessage.getProperty(HttpConstants.BASE_PATH);
            Session session = (Session) httpCarbonMessage.getProperty(HttpConstants.HTTP_SESSION);

            if (cookieHeader != null) {
                try {
                    String sessionId = HttpUtil.getSessionID(cookieHeader);
                    //return value from cached session
                    if (session != null && (sessionId.equals(session.getId()))) {
                        session = session.setAccessed();
                        context.setReturnValues(HttpUtil.createSessionStruct(context, session));
                        return;
                    }
                    session = SessionManager.getInstance().getHTTPSession(sessionId);
                } catch (NoSuchElementException e) {
                    //ignore throwable
                    logger.info("Failed to get session: Incorrect Session cookie");
                }
                if (session == null) {
                    //Return null as the session is invalidated.
                    logger.info("Failed to get session: session is not available");
                    context.setReturnValues();
                    return;
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
                context.setReturnValues();
                return;
            }
            httpCarbonMessage.setProperty(HttpConstants.HTTP_SESSION, session);
            httpCarbonMessage.removeHeader(HttpConstants.COOKIE_HEADER);
            context.setReturnValues(HttpUtil.createSessionStruct(context, session));
        } catch (IllegalStateException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }
}

