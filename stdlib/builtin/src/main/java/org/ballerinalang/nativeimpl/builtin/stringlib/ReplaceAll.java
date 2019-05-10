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

package org.ballerinalang.nativeimpl.builtin.stringlib;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Extern function ballerina.model.strings:replaceAll.
 *
 * @since 0.8.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "builtin",
        functionName = "string.replaceAll",
        args = {@Argument(name = "s", type = TypeKind.STRING),
                @Argument(name = "regex", type = TypeKind.STRING),
                @Argument(name = "replaceWith", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class ReplaceAll extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        String s = context.getStringArgument(0);
        String regex = context.getStringArgument(1);
        String replaceWith = context.getStringArgument(2);

        String replacedString = s.replaceAll(regex, replaceWith);
        context.setReturnValues(new BString(replacedString));
    }

    public static String replaceAll(Strand strand, String value, String regex, String replaceWith) {
        return value.replaceAll(regex, replaceWith);
    }
}
