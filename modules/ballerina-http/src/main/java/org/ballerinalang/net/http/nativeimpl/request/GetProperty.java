/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.net.http.nativeimpl.request;

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
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Ballerina function to set a message property.
 * <br>
 * ballerina.model.messages:getProperty
 */
@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "getProperty",
        args = {@Argument(name = "request", type = TypeEnum.STRUCT, structType = "Request",
                          structPackage = "ballerina.net.http"),
                @Argument(name = "propertyName", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.STRING)}, // TODO: Ballerina only supports string properties ATM
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Retrieve a message property") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "request",
                                                                        value = "The current request object") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "propertyName",
        value = "The name of the property") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "string",
        value = "The property value") })
public class GetProperty extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BStruct requestStruct = (BStruct) getRefArgument(context, 0);
        String propertyName = getStringArgument(context, 0);

        HTTPCarbonMessage httpCarbonMessage = (HTTPCarbonMessage) requestStruct
                .getNativeData(org.ballerinalang.net.http.Constants.TRANSPORT_MESSAGE);
        Object propertyValue = httpCarbonMessage.getProperty(propertyName);

        if (propertyValue == null) {
            return VOID_RETURN;
        }

        if (propertyValue instanceof String) {
            return getBValues(new BString((String) propertyValue));
        } else {
            throw new BallerinaException("Property value is of unknown type : " + propertyValue.getClass().getName());
        }
    }
}
