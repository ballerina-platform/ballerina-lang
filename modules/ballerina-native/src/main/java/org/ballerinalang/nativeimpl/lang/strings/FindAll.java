/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Native function ballerina.lang.strings:findAll(string, string) string[].
 *
 * @since 0.90
 */
@BallerinaFunction(
        packageName = "ballerina.lang.strings",
        functionName = "findAll",
        args = {@Argument(name = "regex", type = TypeEnum.STRING),
                @Argument(name = "inputText", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.ARRAY, elementType = TypeEnum.STRING)},
        isPublic = true)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Returns string array with all sub sequences of the input sequence that matches the regular expression")
})
@BallerinaAnnotation(annotationName = "Param", attributes = {
        @Attribute(name = "regex", value = "Regular expression to be applied")})
@BallerinaAnnotation(annotationName = "Param", attributes = {
        @Attribute(name = "inputText", value = "The string to be applied the regular expression")})
@BallerinaAnnotation(annotationName = "Return", attributes = {
        @Attribute(name = "string[]", value = "Matched string elements array")})
public class FindAll extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        String regex = getStringArgument(context, 0);
        String inputText = getStringArgument(context, 1);
        BStringArray stringArray = new BStringArray();
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(inputText);
            int i = 0;
            while (matcher.find()) {
                stringArray.add(i, matcher.group());
                i++;
            }
        } catch (Exception e) {
            throw new BallerinaException("Invalid regular expression. " + e.getMessage());
        }
        return getBValues(stringArray);
    }
}
