/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.stdlib.runtime.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.stdlib.runtime.utils.RuntimeUtils;

/**
 * Extern function ballerina.runtime:getProperty.
 *
 * @since 0.94.1
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "runtime",
        functionName = "getProperty",
        args = {@Argument(name = "name", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class GetProperty extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        String name = context.getStringArgument(0);
        context.setReturnValues(RuntimeUtils.getSystemProperty(name));
    }

    public static String getProperty(Strand strand, String name) {
        String value = System.getProperty(name);
        if (value == null) {
            return BTypes.typeString.getZeroValue();
        }
        return value;
    }
}
