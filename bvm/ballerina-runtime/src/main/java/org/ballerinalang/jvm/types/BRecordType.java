/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.jvm.types;

import org.ballerinalang.jvm.values.MapValue;

import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

/**
 * {@code BRecordType} represents a user defined record type in Ballerina.
 *
 * @since 0.995.0
 */
public class BRecordType extends BStructureType {

    public boolean sealed;
    public BType restFieldType;

    /**
     * Create a {@code BRecordType} which represents the user defined record type.
     *
     * @param typeName string name of the record type
     * @param pkgPath package of the record type
     * @param flags of the record type
     * @param sealed flag indicating the sealed status
     */
    public BRecordType(String typeName, String pkgPath, int flags, boolean sealed) {
        super(typeName, pkgPath, flags, MapValue.class);
        this.sealed = sealed;
    }

    /**
     * Create a {@code BRecordType} which represents the user defined record type.
     *
     * @param typeName string name of the record type
     * @param pkgPath package of the record type
     * @param flags of the record type
     * @param fields record fields
     * @param restFieldType type of the rest field
     * @param sealed flag to indicate whether the record is sealed
     */
    public BRecordType(String typeName, String pkgPath, int flags, Map<String, BField> fields, BType restFieldType,
            boolean sealed) {
        super(typeName, pkgPath, flags, MapValue.class, fields);
        this.restFieldType = restFieldType;
        this.sealed = sealed;
    }

    @Override
    public <V extends Object> V getZeroValue() {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends Object> V getEmptyValue() {
        return (V) new MapValue<>(this);
    }

    @Override
    public int getTag() {
        return TypeTags.RECORD_TYPE_TAG;
    }

    public String toString() {
        String name = (pkgPath == null || pkgPath.equals(".")) ? typeName : pkgPath + ":" + typeName;
        StringJoiner sj = new StringJoiner(",\n\t", name + " {\n\t", "\n}");
        for (Entry<String, BField> field : getFields().entrySet()) {
            sj.add(field.getKey() + " : " + field.getValue().type);
        }
        return sj.toString();
    }

    @Override
    public String getAnnotationKey() {
        return this.typeName;
    }
}
