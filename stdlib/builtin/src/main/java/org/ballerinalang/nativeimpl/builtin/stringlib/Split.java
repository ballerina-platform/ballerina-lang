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
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Extern function ballerina.model.strings:split(string, string).
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "builtin",
        functionName = "string.split",
        args = {@Argument(name = "mainString", type = TypeKind.STRING),
                @Argument(name = "regex", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.STRING)},
        isPublic = true
)
public class Split extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        String initialString = context.getStringArgument(0);
        String regex = context.getStringArgument(1);

        String[] splitArray = initialString.split(regex);
        BStringArray bSplitArray = new BStringArray(splitArray);
        context.setReturnValues(bSplitArray);
    }
}
