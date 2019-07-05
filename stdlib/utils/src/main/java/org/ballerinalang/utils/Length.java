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
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.RefValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BLangRuntimeException;

import static org.ballerinalang.bre.bvm.BLangVMErrors.NULL_REF_EXCEPTION;

/**
 * Returns an integer representing the number of items that a ref value contains, where the meaning of item depends on
 * the basic type of value.
 *
 * @since 0.990.4
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "utils",
        functionName = "length",
        args = {@Argument(name = "value", type = TypeKind.ANYDATA)},
        returnType = { @ReturnType(type = TypeKind.INT) }
)
public class Length extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context ctx) {
        BValue refRegVal = ctx.getNullableRefArgument(0);
        if (refRegVal == null) {
            throw new BLangRuntimeException(NULL_REF_EXCEPTION);
        }
        ctx.setReturnValues(new BInteger(refRegVal.size()));
    }

    public static long length(Strand strand, Object value) {
        if (value instanceof String) {
            return ((String) value).length();
        }
        RefValue refValue = (RefValue) value;
        if (refValue == null) {
            throw BallerinaErrors.createError(BallerinaErrors.NULL_REF_EXCEPTION);
        }
        return refValue.size();
    }
}
