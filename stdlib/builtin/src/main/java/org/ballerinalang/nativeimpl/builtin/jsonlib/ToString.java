/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.nativeimpl.builtin.jsonlib;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.lang.utils.ErrorHandler;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Extern function ballerina.model.json:toString.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "builtin",
        functionName = "json.toString",
        args = {@Argument(name = "j", type = TypeKind.JSON)},
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class ToString extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context ctx) {
        String jsonStr = null;
        try {
            // Accessing Parameters.
            BValue json = ctx.getNullableRefArgument(0);
            if (json == null) {
                jsonStr = "null";
            } else {
                jsonStr = json.stringValue();
            }
        } catch (Throwable e) {
            ErrorHandler.handleJsonException("convert json to string", e);
        }

        ctx.setReturnValues(new BString(jsonStr));
    }
}
