/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.lang.strings;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Native function ballerina.model.strings:contains.
 *
 * @since 0.8.0
 */
@BallerinaFunction(
        packageName = "ballerina.lang.strings",
        functionName = "contains",
        args = {@Argument(name = "mainString", type = TypeEnum.STRING),
                @Argument(name = "subString", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.BOOLEAN)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Returns a Boolean value indicating whether a string contains the specified substring") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "mainString",
        value = "The original string argument") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "subString",
        value = "The substring to be compared") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "boolean",
        value = "True if the string contains the substring; false otherwise") })
public class Contains extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        String param1 = getArgument(context, 0).stringValue();
        String subString = getArgument(context, 1).stringValue();

        BBoolean isContains = new BBoolean(param1.contains(subString));
        return getBValues(isContains);

    }
}
