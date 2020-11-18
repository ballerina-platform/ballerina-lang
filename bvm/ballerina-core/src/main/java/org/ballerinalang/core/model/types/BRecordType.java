/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.core.model.types;

import org.ballerinalang.core.model.util.Flags;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;

/**
 * {@code BRecordType} represents a user defined record type in Ballerina.
 *
 * @since 0.971.0
 */
public class BRecordType extends BStructureType {

    public boolean sealed;
    public BType restFieldType;

    /**
     * Create a {@code BStructType} which represents the user defined struct type.
     *
     * @param typeName string name of the type
     * @param pkgPath  package of the struct
     * @param flags of the record type
     */
    public BRecordType(String typeName, String pkgPath, int flags) {
        super(typeName, pkgPath, flags, BMap.class);
    }

    @Override
    public <V extends BValue> V getZeroValue() {
        BMap<String, BValue> implicitInitValue = new BMap<>(this);
        this.fields.entrySet().stream()
                .filter(entry -> !Flags.isFlagOn(entry.getValue().flags, Flags.OPTIONAL))
                .forEach(entry -> {
                    BValue value = entry.getValue().fieldType.getZeroValue();
                    implicitInitValue.put(entry.getKey(), value);
                });

        return (V) implicitInitValue;
    }

    @Override
    public <V extends BValue> V getEmptyValue() {
        BMap<String, BValue> implicitInitValue = new BMap<>(this);
        this.fields.entrySet().stream()
                .filter(entry -> !Flags.isFlagOn(entry.getValue().flags, Flags.OPTIONAL))
                .forEach(entry -> {
                    BValue value = entry.getValue().fieldType.getEmptyValue();
                    implicitInitValue.put(entry.getKey(), value);
                });

        return (V) implicitInitValue;
    }

    @Override
    public int getTag() {
        return TypeTags.RECORD_TYPE_TAG;
    }
}

