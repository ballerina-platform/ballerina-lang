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
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.flags.TypeFlags;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.CellAtomicType.CellMutability;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.Core;
import io.ballerina.runtime.api.types.semtype.Definition;
import io.ballerina.runtime.api.types.semtype.Env;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.ValueUtils;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.types.semtype.MappingDefinition;
import io.ballerina.runtime.internal.values.MapValue;
import io.ballerina.runtime.internal.values.MapValueImpl;
import io.ballerina.runtime.internal.values.ReadOnlyUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.runtime.api.types.semtype.Builder.neverType;
import static io.ballerina.runtime.api.types.semtype.CellAtomicType.CellMutability.CELL_MUT_LIMITED;
import static io.ballerina.runtime.api.types.semtype.CellAtomicType.CellMutability.CELL_MUT_NONE;

/**
 * {@code BRecordType} represents a user defined record type in Ballerina.
 *
 * @since 0.995.0
 */
public class BRecordType extends BStructureType implements RecordType, TypeWithShape {
    private final String internalName;
    public boolean sealed;
    public Type restFieldType;
    public int typeFlags;
    private final boolean readonly;
    private IntersectionType immutableType;
    private IntersectionType intersectionType = null;
    private MappingDefinition defn;
    private final Env env = Env.getInstance();
    private byte couldShapeBeDifferentCache = 0;

    private final Map<String, BFunctionPointer<Object, ?>> defaultValues = new LinkedHashMap<>();

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
        registerWithTypeCreator();
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
        registerWithTypeCreator();
    }

    private void registerWithTypeCreator() {
        if (this.typeName != null && this.pkg != null) {
            TypeCreator.registerRecordType(this.typeName, this.pkg, this);
        }
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
        BMap<BString, Object> recordValue = ValueCreator.createRecordValue(this.pkg, typeName);
        ValueUtils.populateDefaultValues(recordValue, this, new HashSet<>());
        if (defaultValues.isEmpty()) {
            return (V) recordValue;
        }
        for (Map.Entry<String, BFunctionPointer<Object, ?>> field : defaultValues.entrySet()) {
            recordValue.put(StringUtils.fromString(field.getKey()),
                    field.getValue().call(new Object[] {Scheduler.getStrand()}));
        }
        return (V) recordValue;
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

    @Override
    public boolean isSealed() {
        return sealed;
    }

    @Override
    public Type getRestFieldType() {
        return restFieldType;
    }

    @Override
    public int getTypeFlags() {
        return typeFlags;
    }

    public void setDefaultValue(String fieldName, BFunctionPointer<Object, ?> defaultValue) {
        defaultValues.put(fieldName, defaultValue);
    }

    public Map<String, BFunctionPointer<Object, ?>> getDefaultValues() {
        return defaultValues;
    }

    @Override
    public synchronized SemType createSemType() {
        if (defn != null) {
            return defn.getSemType(env);
        }
        MappingDefinition md = new MappingDefinition();
        defn = md;
        Field[] fields = getFields().values().toArray(Field[]::new);
        MappingDefinition.Field[] mappingFields = new MappingDefinition.Field[fields.length];
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            boolean isOptional = SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.OPTIONAL);
            SemType fieldType = tryInto(field.getFieldType());
            if (!isOptional && Core.isNever(fieldType)) {
                return neverType();
            }
            boolean isReadonly = SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.READONLY);
            if (Core.isNever(fieldType)) {
                isReadonly = true;
            }
            mappingFields[i] = new MappingDefinition.Field(field.getFieldName(), fieldType,
                    isReadonly, isOptional);
        }
        CellMutability mut = isReadOnly() ? CELL_MUT_NONE : CellMutability.CELL_MUT_LIMITED;
        SemType rest;
        rest = restFieldType != null ? tryInto(restFieldType) : neverType();
        return md.defineMappingTypeWrapped(env, mappingFields, rest, mut);
    }

    @Override
    public void resetSemType() {
        defn = null;
        super.resetSemType();
    }

    @Override
    public Optional<SemType> inherentTypeOf(Context cx, ShapeSupplier shapeSupplier, Object object) {
        BMap<?, ?> value = (BMap<?, ?>) object;
        SemType cachedSemType = value.shapeOf();
        if (cachedSemType != null) {
            return Optional.of(cachedSemType);
        }
        SemType semTypePart = shapeOfInner(cx, shapeSupplier, value, isReadOnly());
        value.cacheShape(semTypePart);
        return Optional.of(semTypePart);
    }

    private SemType shapeOfInner(Context cx, ShapeSupplier shapeSupplier, BMap<?, ?> value, boolean readonly) {
        int nFields = value.size();
        List<MappingDefinition.Field> fields = new ArrayList<>(nFields);
        Map.Entry<?, ?>[] entries = value.entrySet().toArray(Map.Entry[]::new);
        Set<String> handledFields = new HashSet<>(nFields);
        MappingDefinition md;
        if (readonly) {
            Optional<Definition> readonlyShapeDefinition = value.getReadonlyShapeDefinition();
            if (readonlyShapeDefinition.isPresent()) {
                md = (MappingDefinition) readonlyShapeDefinition.get();
                return md.getSemType(env);
            } else {
                md = new MappingDefinition();
                value.setReadonlyShapeDefinition(md);
            }
        } else {
            md = new MappingDefinition();
        }
        for (int i = 0; i < nFields; i++) {
            String fieldName = entries[i].getKey().toString();
            Object fieldValue = entries[i].getValue();
            handledFields.add(fieldName);
            boolean readonlyField = fieldIsReadonly(fieldName);
            boolean optionalField = fieldIsOptional(fieldName);
            Optional<SemType> fieldType;
            if (readonly || readonlyField) {
                optionalField = false;
                fieldType = shapeSupplier.get(cx, fieldValue);
            } else {
                fieldType = Optional.of(SemType.tryInto(fieldType(fieldName)));
            }
            assert fieldType.isPresent();
            fields.add(new MappingDefinition.Field(fieldName, fieldType.get(), readonlyField,
                    optionalField));
        }
        if (!readonly) {
            for (var field : getFields().values()) {
                String name = field.getFieldName();
                if (handledFields.contains(name)) {
                    continue;
                }
                boolean isOptional = SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.OPTIONAL);
                boolean isReadonly = SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.READONLY);
                // TODO: refactor this
                SemType fieldType = SemType.tryInto(field.getFieldType());
                if (isReadonly && isOptional && value.get(StringUtils.fromString(name)) == null) {
                    fieldType = Builder.undef();
                }
                fields.add(new MappingDefinition.Field(field.getFieldName(), fieldType,
                        isReadonly, isOptional));
            }
        }
        SemType semTypePart;
        MappingDefinition.Field[] fieldsArray = fields.toArray(MappingDefinition.Field[]::new);
        if (readonly) {
            semTypePart = md.defineMappingTypeWrapped(env, fieldsArray, neverType(), CELL_MUT_NONE);
        } else {
            SemType rest = restFieldType != null ? SemType.tryInto(restFieldType) : neverType();
            semTypePart = md.defineMappingTypeWrapped(env, fieldsArray, rest, CELL_MUT_LIMITED);
        }
        value.resetReadonlyShapeDefinition();
        return semTypePart;
    }

    @Override
    public boolean couldInherentTypeBeDifferent() {
        if (couldShapeBeDifferentCache != 0) {
            return couldShapeBeDifferentCache == 1;
        }
        boolean result = couldShapeBeDifferentInner();
        couldShapeBeDifferentCache = (byte) (result ? 1 : 2);
        return result;
    }

    private boolean couldShapeBeDifferentInner() {
        if (isReadOnly()) {
            return true;
        }
        return fields.values().stream().anyMatch(field -> SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.READONLY));
    }

    @Override
    public Optional<SemType> shapeOf(Context cx, ShapeSupplier shapeSupplier, Object object) {
        return Optional.of(shapeOfInner(cx, shapeSupplier, (BMap<?, ?>) object, true));
    }

    private Type fieldType(String fieldName) {
        Field field = fields.get(fieldName);
        return field == null ? restFieldType : field.getFieldType();
    }

    private boolean fieldIsReadonly(String fieldName) {
        Field field = fields.get(fieldName);
        return field != null && SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.READONLY);
    }

    private boolean fieldIsOptional(String fieldName) {
        Field field = fields.get(fieldName);
        return field != null && SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.OPTIONAL);
    }
}
