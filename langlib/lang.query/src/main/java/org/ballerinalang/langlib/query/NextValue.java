/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.query;

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.IteratorUtils;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.TypedescValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.query", functionName = "nextValue",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = "_StreamPipeline",
                structPackage = "ballerina/lang.query"
        ),
        args = {@Argument(name = "value", type = TypeKind.ANY)},
        returnType = {@ReturnType(type = TypeKind.RECORD)},
        isPublic = true
)
public class NextValue {

    public static Object nextValue(Strand strand, ObjectValue pipeline, Object value) {
        TypedescValue typedesc = (TypedescValue) pipeline.get("resType");
        BRecordType returnType = IteratorUtils.createIteratorNextReturnType(typedesc.getDescribingType());
        return BallerinaValues.createRecord(new MapValueImpl<>(returnType), value);
    }

}
