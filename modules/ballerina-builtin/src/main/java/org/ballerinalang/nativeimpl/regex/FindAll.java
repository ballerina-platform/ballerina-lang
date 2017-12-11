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

package org.ballerinalang.nativeimpl.regex;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Native function ballerina.regex:findAll.
 *
 */
@BallerinaFunction(
        packageName = "ballerina.regex",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Regex", structPackage = "ballerina.regex"),
        functionName = "findAll",
        args = {@Argument(name = "mainString", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.STRING)},
        isPublic = true
)
public class FindAll extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        String initialString = getStringArgument(context, 0);

        BStruct regexStruct = (BStruct) getRefArgument(context, 0);
        Pattern pattern = (Pattern) regexStruct.getNativeData(REGEXConstants.COMPILED_REGEX);
        if (pattern == null) {
            throw new BallerinaException("Regular Expression has to be compiled first.");
        }

        BStringArray stringArray = new BStringArray();
        Matcher matcher = pattern.matcher(initialString);
        int i = 0;
        while (matcher.find()) {
            stringArray.add(i++, matcher.group());
        }
        return getBValues(stringArray);
    }
}
