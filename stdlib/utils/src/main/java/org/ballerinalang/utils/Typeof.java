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
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.RefValue;
import org.ballerinalang.jvm.values.TypedescValue;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypeDesc;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BTypeDescValue;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Return typedesc value of a given BValue.
 *
 * @since 0.995
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "utils",
        functionName = "typeof",
        args = {@Argument(name = "value", type = TypeKind.ANY)},
        returnType = {@ReturnType(type = TypeKind.TYPEDESC)}
)
public class Typeof extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context ctx) {
        BValue refRegVal = ctx.getNullableRefArgument(0);
        BType type = refRegVal.getType();
        BTypeDesc bTypeDesc = new BTypeDesc(type.getName(), type.getPackagePath());
        BTypeDescValue bTypeDescValue = new BTypeDescValue(bTypeDesc);
        // return a mocked typedesc and start writing tests
        ctx.setReturnValues(bTypeDescValue);
    }

    public static TypedescValue typeof(Strand strand, Object value) {
        if (value instanceof RefValue) {
            RefValue v = (RefValue) value;
            org.ballerinalang.jvm.types.BType type = v.getType();
            return new TypedescValue(type);
        } else if (value instanceof Double) {
            return new TypedescValue(BTypes.typeFloat);
        } else if (value instanceof Long) {
            return new TypedescValue(BTypes.typeInt);
        } else if (value instanceof String) {
            return new TypedescValue(BTypes.typeString);
        } else if (value instanceof Byte) {
            return new TypedescValue(BTypes.typeByte);
        } else if (value instanceof Boolean) {
            return new TypedescValue(BTypes.typeBoolean);
        }
        return null;
    }
}
