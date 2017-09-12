/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Native function ballerina.model.strings:split(string, string).
 */
@BallerinaFunction(
        packageName = "ballerina.lang.strings",
        functionName = "split",
        args = {@Argument(name = "mainString", type = TypeEnum.STRING),
                @Argument(name = "regex", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.ARRAY, elementType = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Splits the string with the given regular expression to produce a string array. ") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "mainString",
        value = "The original string argument") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "regex",
        value = "The regex to split the string") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "string[]",
        value = "The split string array") })
public class Split extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        String initialString = getStringArgument(context, 0);
        String regex = getStringArgument(context, 1);

        String[] splitArray = initialString.split(regex);
        BStringArray bSplitArray = new BStringArray(splitArray);
        return getBValues(bSplitArray);
    }
}
