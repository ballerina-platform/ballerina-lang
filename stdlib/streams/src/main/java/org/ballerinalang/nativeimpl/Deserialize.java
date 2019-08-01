/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.persistence.Serializer;

/**
 * ballerina.model.streams:deserialize(string str).
 *
 * @since 0.990.5
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "streams",
        functionName = "deserialize",
        args = {@Argument(name = "serializedString", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.MAP)},
        isPublic = true
)
public class Deserialize extends BlockingNativeCallableUnit {
    public void execute(Context ctx) {
        String serializedStr = ctx.getStringArgument(0);
        BMap<String, BValue> bStruct = Serializer.getJsonSerializer().deserialize(serializedStr, BMap.class);
        ctx.setReturnValues(bStruct);
    }

    public static MapValue deserialize(Strand strand, Object object, String str) {
        return new MapValueImpl();
    }
}

