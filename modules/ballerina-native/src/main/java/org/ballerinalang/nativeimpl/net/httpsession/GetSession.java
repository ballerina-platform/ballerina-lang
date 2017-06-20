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
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.Arrays;

import static org.ballerinalang.services.dispatchers.http.Constants.COOKIE_HEADER;
import static org.ballerinalang.services.dispatchers.http.Constants.SESSION_ID;

/**
 * Native function to get session with boolean parameter.
 */
@BallerinaFunction(
        packageName = "ballerina.net.httpsession",
        functionName = "getSession",
        args = {@Argument(name = "m", type = TypeEnum.MESSAGE)},
        returnType = {@ReturnType(type = TypeEnum.STRUCT, structType = "Session",
                structPackage = "ballerina.net.httpsession")},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Gets the session struct") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "m", value = "A message Object")})
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "Session",
        value = "HTTP session struct") })
public class GetSession extends AbstractNativeFunction {

    public static final String SESSION_PACKAGE = "ballerina.net.httpsession";
    public static final String STRUCT_SESSION = "Session";

    @Override
    public BValue[] execute(Context context) {
        try {
            CarbonMessage carbonMessage = ((BMessage) getRefArgument(context, 0)).value();
            String cookieHeader = carbonMessage.getHeader(COOKIE_HEADER);
            Session session = context.getSessioContext().getCurrentSession();

            if (session != null) {
                session = session.setAccessed();
                return new BValue[]{createSessionStruct(context, session)};
            }

            if (cookieHeader != null) {
                try {
                    session = Arrays.stream(cookieHeader.split(";"))
                            .filter(cookie -> cookie.startsWith(SESSION_ID))
                            .findFirst()
                            .map(jsession -> context.getSessioContext().getSessionManager()
                                    .getHTTPSession(jsession.substring(SESSION_ID.length()))).get(); //retrieve from session map
                } catch (Exception e) {
                    new IllegalStateException(e);
                }
                if (session != null) {
                    session.setNew(false);
                } else {
                    session = context.getSessioContext().getSessionManager().createHTTPSession();
                }

                //path match
                context.getSessioContext().getPathMatcher().pathMatchWithURI(context,session);




            } else {
                //create session since a request without cookie
                session = context.getSessioContext().getSessionManager().createHTTPSession();
            }

            context.getSessioContext().setCurrentSession(session);
            carbonMessage.removeHeader(COOKIE_HEADER); //do null check
            return new BValue[]{createSessionStruct(context, session)};

        } catch (Exception e) {
            throw new BallerinaException(e);
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
