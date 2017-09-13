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
import org.ballerinalang.model.types.TypeEnum;
//import org.ballerinalang.model.values.BBoolean;
//import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
//import org.ballerinalang.net.http.session.Session;
//import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Native function to get session status.
 *
 * @since 0.89
 */
@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "isNew",
        args = {@Argument(name = "session", type = TypeEnum.STRUCT, structType = "Session",
                structPackage = "ballerina.net.http")},
        returnType = {@ReturnType(type = TypeEnum.BOOLEAN)},
        isPublic = true
)
public class IsNew extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {

        //TODO enable and fix after resource signature change
        return null;
//        try {
//            BStruct sessionStruct = ((BStruct) getRefArgument(context, 0));
//            String sessionId = sessionStruct.getStringField(0);
//            Session session = context.getCurrentSession();
//
//            //return value from cached session
//            if (session != null && (sessionId.equals(session.getId()))) {
//                return getBValues(new BBoolean(session.isNew()));
//            } else {
//                session = context.getSessionManager().getHTTPSession(sessionId);
//                if (session != null) {
//                    return getBValues(new BBoolean(session.isNew()));
//                } else {
//                    //no session available bcz of the time out
//                    throw new IllegalStateException("Failed to get session status: No such session in progress");
//                }
//            }
//        } catch (IllegalStateException e) {
//            throw new BallerinaException(e.getMessage(), e);
//        }
    }
}
