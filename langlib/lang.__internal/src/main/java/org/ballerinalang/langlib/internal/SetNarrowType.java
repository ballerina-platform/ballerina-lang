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

package org.ballerinalang.langlib.internal;

import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.TypeCreator;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.util.Flags;

import java.util.HashMap;

/**
 * Native implementation of lang.internal:setNarrowType(typedesc, (any|error)[]).
 *
 * @since 1.2.0
 */
public class SetNarrowType {

    public static BMap setNarrowType(BTypedesc td, BMap value) {
        RecordType recordType = (RecordType) value.getType();
        RecordType newRecordType =
                TypeCreator.createRecordType("narrowType", recordType.getPackage(), recordType.getTypeFlags(),
                                             recordType.isSealed(), recordType.getTypeFlags());
        newRecordType.setFields(new HashMap() {{
            put("value", TypeCreator.createField(td.getDescribingType(), "value", Flags.PUBLIC + Flags.REQUIRED));
        }});

        BMap<BString, Object> newRecord = ValueCreator.createMapValue(newRecordType);
        newRecord.put(StringUtils.fromString("value"), value.get(StringUtils.fromString("value")));
        return newRecord;
    }
}
