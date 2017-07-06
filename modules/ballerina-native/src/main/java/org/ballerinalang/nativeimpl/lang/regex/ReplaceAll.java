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

package org.ballerinalang.nativeimpl.lang.regex;

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
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Native function ballerina.lang.regex:replaceAll(string, string, string) string.
 *
 * @since 0.90
 */
@BallerinaFunction(
        packageName = "ballerina.lang.regex",
        functionName = "replaceAll",
        args = {
                @Argument(name = "inputText", type = TypeEnum.STRING),
                @Argument(name = "regex", type = TypeEnum.STRING),
                @Argument(name = "replacingText", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Replaces all instances of the replacePattern with the replaceWith string and returns the result")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "inputText",
        value = "The string to be applied the regular expression")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "regex",
        value = "Regular expression to be applied")})
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "replacingText",
        value = "The replacement string")})
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "string",
        value = "The derived string")})
public class ReplaceAll extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        String inputText = getStringArgument(context, 0);
        String regex = getStringArgument(context, 1);
        String replacingText = getStringArgument(context, 2);
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(inputText);
            String replacedText = matcher.replaceAll(replacingText);
            BString replacedString = new BString(replacedText);
            return getBValues(replacedString);
        } catch (Exception e) {
            throw new BallerinaException("Invalid regular expression. " + e.getMessage());
        }
    }
}
