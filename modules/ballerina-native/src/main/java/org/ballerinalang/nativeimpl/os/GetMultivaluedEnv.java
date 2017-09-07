/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.nativeimpl.os;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Native function ballerina.os:getMultivaluedEnv.
 *
 * @since 0.95
 */
@BallerinaFunction(
        packageName = "ballerina.os",
        functionName = "getMultivaluedEnv",
        args = {@Argument(name = "name", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.ARRAY, elementType = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Splits the value of a environment variable using path separator and returns the separated values as " +
                "an array.") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "name",
        value = "name of the environment variable") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "string[]",
        value = "environment variable values as an array if the provided environment variable exists, otherwise an " +
                "empty array") })
public class GetMultivaluedEnv extends AbstractNativeFunction {

    private static final String PROPERTY_NAME = "path.separator";

    @Override
    public BValue[] execute(Context context) {
        String str = getStringArgument(context, 0);
        String value = System.getenv(str);
        String separator = System.getProperty(PROPERTY_NAME);
        if (value == null || separator == null) {
            BStringArray valueArray = new BStringArray();
            return getBValues(valueArray);
        }
        String[] values = value.split(separator);
        BStringArray valueArray = new BStringArray(values);
        return getBValues(valueArray);
    }
}
