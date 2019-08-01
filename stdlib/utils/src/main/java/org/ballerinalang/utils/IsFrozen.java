/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/

package org.ballerinalang.utils;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.RefValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Check the freeze status of a given value.
 *
 * @since 0.990.4
 */
@BallerinaFunction(orgName = "ballerina",
        packageName = "utils",
        functionName = "isFrozen",
        args = {@Argument(name = "value", type = TypeKind.ANYDATA)},
        returnType = {@ReturnType(type = TypeKind.BOOLEAN)})
public class IsFrozen extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context ctx) {
        BValue value = ctx.getNullableRefArgument(0);
        boolean frozen = value == null || value.isFrozen();
        ctx.setReturnValues(new BBoolean(frozen));
    }

    public static boolean isFrozen(Strand strand, Object value) {
        return !(value instanceof RefValue) || ((RefValue) value).isFrozen();
    }
}
