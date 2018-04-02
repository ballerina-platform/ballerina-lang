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
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.session.Session;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.IllegalFormatException;

/**
 * Native function to set session attributes to the message.
 *
 * @since 0.89
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "setAttribute",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Session",
                             structPackage = "ballerina.http"),
        args = {@Argument(name = "attributeKey", type = TypeKind.STRING),
                @Argument(name = "attributeValue", type = TypeKind.ANY)},
        isPublic = true
)
public class SetAttribute extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) throws IllegalFormatException {
        try {
            BStruct sessionStruct  = ((BStruct) context.getRefArgument(0));
            String attributeKey = context.getStringArgument(0);
            BValue attributeValue = context.getRefArgument(1);
            Session session = (Session) sessionStruct.getNativeData(HttpConstants.HTTP_SESSION);

            if (attributeKey == null || attributeValue == null) {
                throw new NullPointerException("Failed to set attribute: Attribute key: "
                        + attributeKey + "Attribute Value: " + attributeValue);
            }
            if (session != null && session.isValid()) {
                session.setAttribute(attributeKey, attributeValue);
            } else {
                throw new IllegalStateException("Failed to set attribute: No such session in progress");
            }
        } catch (IllegalStateException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
        context.setReturnValues();
    }
}
