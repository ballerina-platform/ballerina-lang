/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.nativeimpl.util;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Native function ballerina.util:hexToDecimal.
 *
 * @since 0.962.0
 */
@BallerinaFunction(
        packageName = "ballerina.util",
        functionName = "hexToDecimal",
        args = {@Argument(name = "hexValue", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.INT)},
        isPublic = true
)
public class HexToDecimal extends AbstractNativeFunction {

    private static final String ERROR_PREFIX = "invalid character: ";

    @Override
    public BValue[] execute(Context context) {
        try {
            String hexValue = getStringArgument(context, 0).substring(2);
            return getBValues(new BInteger(Integer.parseInt(hexValue, 16)));
        } catch (Throwable e) {
            throw new BallerinaException(ERROR_PREFIX + e.getMessage());
        }
    }
}
