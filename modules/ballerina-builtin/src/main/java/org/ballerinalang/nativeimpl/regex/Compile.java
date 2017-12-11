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
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Native function ballerina.model.strings:compile.
 *
 */
@BallerinaFunction(
        packageName = "ballerina.regex",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Regex", structPackage = "ballerina.regex"),
        functionName = "compile",
        isPublic = true
)
public class Compile extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        try {
            BStruct regexStruct = (BStruct) getRefArgument(context, 0);
            String regex = regexStruct.getStringField(0);
            Pattern pattern = Pattern.compile(regex);
            regexStruct.addNativeData(REGEXConstants.COMPILED_REGEX, pattern);
            return VOID_RETURN;
        } catch (PatternSyntaxException e) {
            throw new BallerinaException("Syntax error in the regular-expression pattern", e);
        }
    }
}
