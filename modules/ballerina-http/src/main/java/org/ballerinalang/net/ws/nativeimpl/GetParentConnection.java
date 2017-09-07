/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.net.ws.nativeimpl;

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
import org.ballerinalang.net.ws.Constants;
import org.ballerinalang.util.exceptions.BallerinaException;

import javax.websocket.Session;

/**
 * Send text to the same client who sent the message to the given WebSocket Upgrade Path.
 */

@BallerinaFunction(
        packageName = "ballerina.net.ws",
        functionName = "getParentConnection",
        args = {@Argument(name = "conn", type = TypeEnum.STRUCT, structType = "Connection",
                          structPackage = "ballerina.net.ws")},
        returnType = {@ReturnType(type = TypeEnum.STRUCT)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description",
                     attributes = { @Attribute(name = "value", value = "Get the unique ID of the connection") })
@BallerinaAnnotation(annotationName = "Return",
                     attributes = {@Attribute(name = "conn", value = "Parent connection of the connection")})
public class GetParentConnection extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {

        if (context.getServiceInfo() == null ||
                !context.getServiceInfo().getProtocolPkgPath().equals(Constants.WEBSOCKET_PACKAGE_PATH)) {
            throw new BallerinaException("This function is only working with WebSocket services");
        }

        BStruct wsConnection = (BStruct) getRefArgument(context, 0);
        BStruct parentConnection = (BStruct) wsConnection.getNativeData(Constants.NATIVE_DATA_PARENT_CONNECTION);
        return getBValues(parentConnection);
    }
}
