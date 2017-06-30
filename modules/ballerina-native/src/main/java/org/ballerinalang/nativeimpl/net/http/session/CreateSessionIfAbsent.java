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
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.services.dispatchers.session.Session;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.Arrays;
import java.util.NoSuchElementException;

import static org.ballerinalang.services.dispatchers.http.Constants.BASE_PATH;
import static org.ballerinalang.services.dispatchers.http.Constants.COOKIE_HEADER;
import static org.ballerinalang.services.dispatchers.http.Constants.SESSION_ID;

/**
 * Native function to create session if session id not exist, otherwise return existing session.
 *
 * @since 0.89
 */
@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "createSessionIfAbsent",
        args = {@Argument(name = "m", type = TypeEnum.MESSAGE)},
        returnType = {@ReturnType(type = TypeEnum.STRUCT, structType = "Session",
                structPackage = "ballerina.net.http")},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Gets the session struct for valid id, otherwise create new") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "m", value = "A message Object")})
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "Session",
        value = "HTTP session struct") })
public class CreateSessionIfAbsent extends AbstractNativeFunction {

    public static final String SESSION_PACKAGE = "ballerina.net.http";
    public static final String STRUCT_SESSION = "Session";
    private static final Logger logger = LoggerFactory.getLogger(CreateSessionIfAbsent.class);

    @Override
    public BValue[] execute(Context context) {
        CarbonMessage carbonMessage;
        try {
            carbonMessage = ((BMessage) getRefArgument(context, 0)).value();
            String cookieHeader = carbonMessage.getHeader(COOKIE_HEADER);
            String path = (String) carbonMessage.getProperty(BASE_PATH);
            Session session = context.getCurrentSession();

            if (cookieHeader != null) {
                try {
                    String sessionId = Arrays.stream(cookieHeader.split(";"))
                                        .filter(cookie -> cookie.startsWith(SESSION_ID))
                                        .findFirst().get().substring(SESSION_ID.length());
                    //return value from cached session
                    if (session != null && (sessionId.equals(session.getId()))) {
                        session = session.setAccessed();
                        return new BValue[]{createSessionStruct(context, session)};
                    }
                    session = context.getSessionManager().getHTTPSession(sessionId);
                } catch (NoSuchElementException e) {
                    //ignore throwable
                    logger.info("Failed to get session: Incorrect Session cookie");
                }
                if (session != null) {
                    //path Validity check
                    if (session.getPath().equals(path)) {
                        session.setNew(false);
                        session.setAccessed();
                    } else {
                        throw new BallerinaException("Failed to get session: " + path + " is not an allowed path");
                    }
                } else {
                    session = context.getSessionManager().createHTTPSession(path);
                }
            } else {
                //Cached session will return of this function is called twice.
                if (session != null) {
                    session = session.setAccessed();
                    return new BValue[]{createSessionStruct(context, session)};
                }
                //create session since  request doesn't have a cookie
                session = context.getSessionManager().createHTTPSession(path);
            }
            context.setCurrentSession(session);
            carbonMessage.removeHeader(COOKIE_HEADER);
            return new BValue[]{createSessionStruct(context, session)};

        } catch (IllegalStateException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    public static BStruct createSessionStruct(Context context, Session session) {

        //gather package details from natives
        PackageInfo sessionPackageInfo = context.getProgramFile().getPackageInfo(SESSION_PACKAGE);
        StructInfo sessionStructInfo = sessionPackageInfo.getStructInfo(STRUCT_SESSION);

        //create session struct
        BStruct bStruct = new BStruct(sessionStructInfo.getType());
        bStruct.setFieldTypes(sessionStructInfo.getFieldTypes());
        bStruct.init(sessionStructInfo.getFieldCount());

        //Add session Id to the struct as a string
        bStruct.setStringField(0, session.getId());

        return bStruct;
    }
}
