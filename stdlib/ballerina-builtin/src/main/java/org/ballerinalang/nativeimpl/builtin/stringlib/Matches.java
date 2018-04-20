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

package org.ballerinalang.nativeimpl.builtin.stringlib;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Native function ballerina.model.strings:matches.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "builtin",
        functionName = "string.matches",
        args = {@Argument(name = "s", type = TypeKind.STRING),
                @Argument(name = "reg", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.BOOLEAN), @ReturnType(type = TypeKind.STRUCT)},
        isPublic = true
)
public class Matches extends AbstractRegexFunction {
    @Override
    public void execute(Context context) {
        String s = context.getStringArgument(0);
        String regex = context.getStringArgument(1);
        try {
            Pattern pattern = validatePattern(regex);
            Matcher matcher = pattern.matcher(s);
            BBoolean matches = new BBoolean(matcher.matches());
            context.setReturnValues(matches);
        } catch (PatternSyntaxException e) {
            context.setReturnValues(BLangVMErrors.createError(context, 0, e.getMessage()));
        }
    }
}
