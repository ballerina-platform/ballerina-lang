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
package org.ballerinalang.nativeimpl.runtime;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Native function ballerina.runtime:getProperty.
 *
 * @since 0.95
 */
@BallerinaFunction(
        packageName = "ballerina.runtime",
        functionName = "getProperty",
        args = {@Argument(name = "name", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Returns the value associated with the specified property name.")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "name",
        value = "name of the property")})
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "string",
        value = "value of the property if the property exists, an empty string otherwise")})
public class GetProperty extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        String name = getStringArgument(context, 0);
        String value = System.getProperty(name);
        if (value == null) {
            value = "";
        }
        return getBValues(new BString(value));
    }
}
