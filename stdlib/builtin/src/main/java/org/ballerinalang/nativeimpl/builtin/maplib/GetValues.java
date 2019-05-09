/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.nativeimpl.builtin.maplib;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Extern function to get value array from the map.
 * ballerina.model.map:values()
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "builtin",
        functionName = "map.values",
        args = {@Argument(name = "m", type = TypeKind.MAP)},
        returnType = {@ReturnType(type = TypeKind.ARRAY, elementType = TypeKind.ANY)},
        isPublic = true
)
public class GetValues extends BlockingNativeCallableUnit {

    public void execute(Context ctx) {
        BMap<String, BValue> map = (BMap<String, BValue>) ctx.getRefArgument(0);
        BValueArray bRefValueArray = new BValueArray((BRefType<?>[]) map.values(), BTypes.typeAny);
        ctx.setReturnValues(bRefValueArray);
    }

    public static ArrayValue values(Strand strand, MapValue<?, ?> map) {
        BMapType mapType = (BMapType) map.getType();
        return new ArrayValue(map.values().toArray(new Object[0]), new BArrayType(mapType.getConstrainedType()));
    }
}
