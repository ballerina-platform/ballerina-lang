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

package io.ballerina.runtime.internal.types;

import io.ballerina.identifier.Utils;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.flags.TypeFlags;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.values.MapValue;
import io.ballerina.runtime.internal.values.MapValueImpl;
import io.ballerina.runtime.internal.values.ReadOnlyUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * {@code BRecordType} represents a user defined record type in Ballerina.
 *
 * @since 0.995.0
 */
public class BRecordType extends BStructureType implements RecordType {
    private final String internalName;
    public boolean sealed;
    public Type restFieldType;
    public int typeFlags;
    private final boolean readonly;
    private IntersectionType immutableType;
    private IntersectionType intersectionType = null;

    /**
     * Create a {@code BRecordType} which represents the user defined record type.
     *
     * @param typeName string name of the record type
     * @param pkg package of the record type
     * @param flags of the record type
     * @param sealed flag indicating the sealed status
     * @param typeFlags flags associated with the type
     */
    public BRecordType(String typeName, String internalName, Module pkg, long flags, boolean sealed, int typeFlags) {
        super(typeName, pkg, flags, MapValueImpl.class);
        this.internalName = internalName;
        this.sealed = sealed;
        this.typeFlags = typeFlags;
        this.readonly = SymbolFlags.isFlagOn(flags, SymbolFlags.READONLY);
    }

    /**
     * Create a {@code BRecordType} which represents the user defined record type.
     *
     * @param typeName string name of the record type
     * @param pkg package of the record type
     * @param flags of the record type
     * @param fields record fields
     * @param restFieldType type of the rest field
     * @param sealed flag to indicate whether the record is sealed
     * @param typeFlags flags associated with the type
     */
    public BRecordType(String typeName, Module pkg, long flags, Map<String, Field> fields, Type restFieldType,
                       boolean sealed, int typeFlags) {
        super(typeName, pkg, flags, MapValueImpl.class);
        this.sealed = sealed;
        this.typeFlags = typeFlags;
        this.readonly = SymbolFlags.isFlagOn(flags, SymbolFlags.READONLY);
        if (readonly) {
            this.fields = getReadOnlyFields(fields);
            if (restFieldType != null) {
                this.restFieldType = ReadOnlyUtils.getReadOnlyType(restFieldType);
            }
        } else {
            this.restFieldType = restFieldType;
            this.fields = fields;
        }
        this.internalName = typeName;
    }

    private Map<String, Field> getReadOnlyFields(Map<String, Field> fields) {
        Map<String, Field> fieldMap = new HashMap<>(fields.size());
        for (Map.Entry<String, Field> fieldEntry : fields.entrySet()) {
            Field field = fieldEntry.getValue();
            if (!field.getFieldType().isReadOnly()) {
                field = new BField(ReadOnlyUtils.getReadOnlyType(field.getFieldType()), field.getFieldName(),
                                   field.getFlags());
            }
            fieldMap.put(fieldEntry.getKey(), field);
        }
        return fieldMap;
    }

    @Override
    public <V extends Object> V getZeroValue() {
        String typeName = this.typeName;
        if (intersectionType != null) {
            typeName = ReadOnlyUtils.getMutableType((BIntersectionType) intersectionType).getName();
        }
        if (isReadOnly()) {
            return (V) ValueCreator.createReadonlyRecordValue(this.pkg, typeName, new HashMap<>());
        }
        return (V) ValueCreator.createRecordValue(this.pkg, typeName);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends Object> V getEmptyValue() {
        MapValue<BString, Object> implicitInitValue = new MapValueImpl<>(this);
        this.fields.entrySet().stream()
                .filter(entry -> !SymbolFlags.isFlagOn(entry.getValue().getFlags(), SymbolFlags.OPTIONAL))
                .forEach(entry -> {
                    Object value = entry.getValue().getFieldType().getEmptyValue();
                    implicitInitValue.put(StringUtils.fromString(entry.getKey()), value);
                });
        return (V) implicitInitValue;
    }

    @Override
    public int getTag() {
        return TypeTags.RECORD_TYPE_TAG;
    }

    @Override
    public String getAnnotationKey() {
        return Utils.decodeIdentifier(this.internalName);
    }

    @Override
    public boolean isAnydata() {
        return TypeFlags.isFlagOn(this.typeFlags, TypeFlags.ANYDATA);
    }

    @Override
    public boolean isPureType() {
        return TypeFlags.isFlagOn(this.typeFlags, TypeFlags.PURETYPE);
    }

    @Override
    public boolean isReadOnly() {
        return this.readonly;
    }

    @Override
    public IntersectionType getImmutableType() {
        return this.immutableType;
    }

    @Override
    public void setImmutableType(IntersectionType immutableType) {
        this.immutableType = immutableType;
    }

    @Override
    public Optional<IntersectionType> getIntersectionType() {
        return this.intersectionType ==  null ? Optional.empty() : Optional.of(this.intersectionType);
    }

    @Override
    public void setIntersectionType(IntersectionType intersectionType) {
        this.intersectionType = intersectionType;
    }

    public boolean isSealed() {
        return sealed;
    }

    public Type getRestFieldType() {
        return restFieldType;
    }

    public int getTypeFlags() {
        return typeFlags;
    }
}
