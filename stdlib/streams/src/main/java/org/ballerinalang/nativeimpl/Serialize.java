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
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.persistence.Serializer;

/**
 * ballerina.model.streams:serialize(map obj).
 *
 * @since 0.990.5
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "streams",
        functionName = "serialize",
        args = {@Argument(name = "data", type = TypeKind.MAP)},
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class Serialize extends BlockingNativeCallableUnit {
    public void execute(Context ctx) {
        String serialized = Serializer.getJsonSerializer().serialize(ctx.getRefArgument(0));
        ctx.setReturnValues(new BString(serialized));
    }

    public static String serialize(Strand strand, Object value, MapValue data) {
        //TODO: implement serializers for JBAL values
        return "";
    }
}

