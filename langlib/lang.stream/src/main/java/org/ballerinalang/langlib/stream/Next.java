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

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.TypeFlags;
import org.ballerinalang.jvm.util.Flags;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.StreamValue;
import org.ballerinalang.jvm.values.api.BValueCreator;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.HashMap;
import java.util.Map;


/**
 * Native implementation of lang.stream:next(stream<type>).
 *
 * @since 1.2
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.stream", functionName = "next",
        args = {@Argument(name = "strm", type = TypeKind.STREAM)},
        returnType = {@ReturnType(type = TypeKind.RECORD)},
        isPublic = true
)
public class Next {
    public static Object next(Strand strand, StreamValue strm) {
        Object next = strm.next(strand);
        if (next == null) {
            return null;
        }

        Map<String, BField> fields = new HashMap<>();
        fields.put("value", new BField(strm.getConstraintType(), "value", Flags.PUBLIC + Flags.REQUIRED));
        BRecordType recordType = new BRecordType("$$returnType$$", strm.getType().getPackage(), 0, fields,
                null, true, TypeFlags.asMask(TypeFlags.ANYDATA, TypeFlags.PURETYPE));
        return BValueCreator.createRecord(new MapValueImpl<>(recordType), next);
    }
}
