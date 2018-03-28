/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.nativeimpl.os;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Native function ballerina.os:getMultivaluedEnv.
 *
 * @since 0.94.1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "os",
        functionName = "getMultivaluedEnv",
        args = {@Argument(name = "name", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.STRING)},
        isPublic = true
)
public class GetMultivaluedEnv extends BlockingNativeCallableUnit {

    private static final String PROPERTY_NAME = "path.separator";

    @Override
    public void execute(Context context) {
        String str = context.getStringArgument(0);
        String value = System.getenv(str);
        String separator = System.getProperty(PROPERTY_NAME);
        if (value == null || separator == null) {
            BStringArray valueArray = new BStringArray();
            context.setReturnValues(valueArray);
            return;
        }
        String[] values = value.split(separator);
        BStringArray valueArray = new BStringArray(values);
        context.setReturnValues(valueArray);
    }
}
