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
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.Set;

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
        Set<String> keySet = map.keySet();
        BRefValueArray bRefValueArray = new BRefValueArray(BTypes.typeAny);
        int i = 0;
        for (String key : keySet) {
            BValue value = map.get(key);
            bRefValueArray.add(i, ((BRefType) value));
            i++;
        }
        ctx.setReturnValues(bRefValueArray);
    }
}
