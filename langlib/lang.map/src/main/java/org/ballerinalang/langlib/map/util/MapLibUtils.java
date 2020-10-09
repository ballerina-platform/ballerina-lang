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

package org.ballerinalang.langlib.map.util;

import io.ballerina.jvm.api.TypeTags;
import io.ballerina.jvm.api.types.Type;
import io.ballerina.jvm.types.BField;
import io.ballerina.jvm.types.BMapType;
import io.ballerina.jvm.types.BRecordType;
import io.ballerina.jvm.types.BUnionType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

import static io.ballerina.jvm.MapUtils.createOpNotSupportedError;

/**
 * Utility methods for map lib functions.
 *
 * @since 1.0
 */
public class MapLibUtils {

    public static Type getFieldType(Type mapType, String funcName) {
        switch (mapType.getTag()) {
            case TypeTags.MAP_TAG:
                return ((BMapType) mapType).getConstrainedType();
            case TypeTags.RECORD_TYPE_TAG:
                return getCommonTypeForRecordField((BRecordType) mapType);
            default:
                throw createOpNotSupportedError(mapType, funcName);
        }
    }

    public static Type getCommonTypeForRecordField(BRecordType recordType) {
        LinkedHashSet<Type> typeSet = new LinkedHashSet<>();
        Collection<BField> fields = (recordType.getFields().values());

        for (BField f : fields) {
            typeSet.add(f.type);
        }

        if (recordType.restFieldType != null) {
            typeSet.add(recordType.restFieldType);
        }

        return typeSet.size() == 1 ? typeSet.iterator().next() : new BUnionType(new ArrayList<>(typeSet));
    }
}
