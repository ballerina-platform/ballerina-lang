/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.langlib.array;

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.IteratorUtils;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BTupleType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.types.TypeFlags;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.Flags;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.IteratorValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;


/**
 * Native implementation of lang.array.ArrayIterator:next().
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.array", functionName = "next",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "ArrayIterator",
                structPackage = "ballerina/lang.array"),
        returnType = {@ReturnType(type = TypeKind.RECORD)},
        isPublic = true
)
public class Next {
    //TODO: refactor hard coded values
    public static Object next(Strand strand, ObjectValue m) {
        IteratorValue arrIterator = (IteratorValue) m.getNativeData("&iterator&");
        ArrayValue arr = m.getArrayValue("m");
        if (arrIterator == null) {
            arrIterator = arr.getIterator();
            m.addNativeData("&iterator&", arrIterator);
        }

        if (arrIterator.hasNext()) {
            Object element =  arrIterator.next();
            BRecordType recordType = (BRecordType) m.getNativeData("&recordType&");
            if (recordType == null) {
                Map<String, BField> fields = new HashMap<>();
                BType type;
                if (arr.getType().getTag() == TypeTags.ARRAY_TAG) {
                    type = arr.getElementType();
                } else {
                    BTupleType tupleType = (BTupleType) arr.getType();
                    LinkedHashSet<BType> types = new LinkedHashSet<>(tupleType.getTupleTypes());
                    if (tupleType.getRestType() != null) {
                        types.add(tupleType.getRestType());
                    }
                    if (types.size() == 1) {
                        type = types.iterator().next();
                    } else {
                        type = new BUnionType(new ArrayList<>(types));
                    }
                }

                fields.put("value", new BField(type, "value", Flags.PUBLIC + Flags.REQUIRED));
                recordType = new BRecordType("$$returnType$$", null, 0, fields, null, true,
                        TypeFlags.asMask(IteratorUtils.getAnydataTypeFlag(type), IteratorUtils.getPuretypeTypeFlag(type)));
                m.addNativeData("&recordType&", recordType);
            }
            return BallerinaValues.createRecord(new MapValueImpl<>(recordType), element);
        }

        return null;
    }
}
