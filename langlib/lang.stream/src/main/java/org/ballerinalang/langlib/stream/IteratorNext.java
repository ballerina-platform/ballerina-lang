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

package org.ballerinalang.langlib.stream;

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.TypeFlags;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.StreamValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.wso2.ballerinalang.util.Flags;

import java.util.HashMap;
import java.util.Map;


/**
 * Native implementation of lang.stream.StreamIterator:next().
 *
 * @since 1.2
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.stream", functionName = "next",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "StreamIterator",
                structPackage = "ballerina/lang.stream"),
        returnType = {@ReturnType(type = TypeKind.RECORD)},
        isPublic = true
)
public class IteratorNext {
    //TODO: refactor hard coded values
    public static Object next(Strand strand, ObjectValue m) {
        StreamValue strmIterator = (StreamValue) m.getNativeData("&iterator&");

        if (strmIterator == null) {
            strmIterator = ((StreamValue) m.get("strm"));
            m.addNativeData("&iterator&", strmIterator);
        }

        Object next = strmIterator.next(strand);
        if (next != null) {
            Map<String, BField> fields = new HashMap<>();
            fields.put("value", new BField(strmIterator.getConstraintType(), "value", Flags.PUBLIC + Flags.REQUIRED));
            BRecordType recordType = new BRecordType("$$returnType$$", strmIterator.getType().getPackage(), 0, fields,
                    null, true, TypeFlags.PURETYPE);
            return BallerinaValues.createRecord(new MapValueImpl<>(recordType), next);
        }

        return null;
    }
}
