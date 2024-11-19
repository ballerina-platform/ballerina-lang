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
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.flags.TypeFlags;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.TypeTags;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.Core;
import io.ballerina.runtime.api.types.semtype.Env;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.api.types.semtype.ShapeAnalyzer;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.types.semtype.CellAtomicType.CellMutability;
import io.ballerina.runtime.internal.types.semtype.DefinitionContainer;
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
import java.util.function.Function;

import static io.ballerina.runtime.api.types.semtype.Builder.getNeverType;
import static io.ballerina.runtime.internal.types.semtype.CellAtomicType.CellMutability.CELL_MUT_NONE;
import static io.ballerina.runtime.internal.types.semtype.CellAtomicType.CellMutability.CELL_MUT_UNLIMITED;

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
    private final DefinitionContainer<MappingDefinition> defn = new DefinitionContainer<>();
    private final DefinitionContainer<MappingDefinition> acceptedTypeDefn = new DefinitionContainer<>();
    private byte couldInhereTypeBeDifferentCache = 0;

    private final Map<String, BFunctionPointer> defaultValues = new LinkedHashMap<>();

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
        TypeCreator.registerRecordType(this);
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
        TypeCreator.registerRecordType(this);
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
        if (defaultValues.isEmpty()) {
            return (V) recordValue;
        }
        for (Map.Entry<String, BFunctionPointer> field : defaultValues.entrySet()) {
            recordValue.put(StringUtils.fromString(field.getKey()),
                    field.getValue().call(Scheduler.getStrand().scheduler.runtime));
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

    public void setDefaultValue(String fieldName, BFunctionPointer defaultValue) {
        defaultValues.put(fieldName, defaultValue);
    }

    public Map<String, BFunctionPointer> getDefaultValues() {
        return defaultValues;
    }

    @Override
    public SemType createSemType() {
        Env env = Env.getInstance();
        if (defn.isDefinitionReady()) {
            return defn.getSemType(env);
        }
        var result = defn.trySetDefinition(MappingDefinition::new);
        if (!result.updated()) {
            return defn.getSemType(env);
        }
        MappingDefinition md = result.definition();
        return createSemTypeInner(md, env, mut(), SemType::tryInto);
    }

    private CellMutability mut() {
        return isReadOnly() ? CELL_MUT_NONE : CellMutability.CELL_MUT_LIMITED;
    }

    private SemType createSemTypeInner(MappingDefinition md, Env env, CellMutability mut,
                                       Function<Type, SemType> semTypeFunction) {
        Field[] fields = getFields().values().toArray(Field[]::new);
        MappingDefinition.Field[] mappingFields = new MappingDefinition.Field[fields.length];
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            boolean isOptional = SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.OPTIONAL);
            SemType fieldType = semTypeFunction.apply(field.getFieldType());
            if (!isOptional && Core.isNever(fieldType)) {
                return getNeverType();
            }
            boolean isReadonly =
                    SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.READONLY) || Core.isNever(fieldType);
            mappingFields[i] = new MappingDefinition.Field(field.getFieldName(), fieldType,
                    isReadonly, isOptional);
        }
        SemType rest;
        rest = restFieldType != null ? semTypeFunction.apply(restFieldType) : getNeverType();
        return md.defineMappingTypeWrapped(env, mappingFields, rest, mut);
    }

    @Override
    public void resetSemType() {
        defn.clear();
        super.resetSemType();
    }

    @Override
    public boolean isDependentlyTypedInner(Set<MayBeDependentType> visited) {
        return fields.values().stream().map(Field::getFieldType).filter(each -> each instanceof MayBeDependentType)
                .anyMatch(each -> ((MayBeDependentType) each).isDependentlyTyped(visited));
    }

    @Override
    public Optional<SemType> inherentTypeOf(Context cx, ShapeSupplier shapeSupplier, Object object) {
        if (!couldInherentTypeBeDifferent()) {
            return Optional.of(getSemType());
        }
        MapValueImpl<?, ?> value = (MapValueImpl<?, ?>) object;
        SemType cachedSemType = value.shapeOf();
        if (cachedSemType != null) {
            return Optional.of(cachedSemType);
        }
        SemType semTypePart = shapeOfInner(cx, shapeSupplier, value, isReadOnly());
        value.cacheShape(semTypePart);
        return Optional.of(semTypePart);
    }

    private SemType shapeOfInner(Context cx, ShapeSupplier shapeSupplier, MapValueImpl<?, ?> value,
                                 boolean takeFieldShape) {
        Env env = cx.env;
        int nFields = value.size();
        Map.Entry<?, ?>[] entries = value.entrySet().toArray(Map.Entry[]::new);
        Set<String> handledFields = new HashSet<>(nFields);
        MappingDefinition md;
        if (takeFieldShape) {
            MappingDefinition readonlyShapeDefinition = value.getReadonlyShapeDefinition();
            if (readonlyShapeDefinition != null) {
                return readonlyShapeDefinition.getSemType(env);
            } else {
                md = new MappingDefinition();
                value.setReadonlyShapeDefinition(md);
            }
        } else {
            md = new MappingDefinition();
        }
        List<MappingDefinition.Field> fields = new ArrayList<>(nFields);
        for (int i = 0; i < nFields; i++) {
            String fieldName = entries[i].getKey().toString();
            Object fieldValue = entries[i].getValue();
            handledFields.add(fieldName);
            fields.add(fieldShape(cx, shapeSupplier, fieldName, fieldValue, takeFieldShape));
        }
        if (!takeFieldShape) {
            getFields().values().stream()
                    .filter(field -> !handledFields.contains(field.getFieldName()))
                    .map(field -> fieldShapeWithoutValue(field, field.getFieldName()))
                    .forEach(fields::add);
        }
        MappingDefinition.Field[] fieldsArray = fields.toArray(MappingDefinition.Field[]::new);
        SemType rest;
        if (takeFieldShape) {
            rest = Builder.getNeverType();
        } else {
            rest = restFieldType != null ? SemType.tryInto(restFieldType) : getNeverType();
        }
        SemType shape = md.defineMappingTypeWrapped(env, fieldsArray, rest, mut());
        value.resetReadonlyShapeDefinition();
        return shape;
    }

    private MappingDefinition.Field fieldShapeWithoutValue(Field field, String fieldName) {
        boolean isOptional = fieldIsOptional(fieldName);
        boolean isReadonly = fieldIsReadonly(fieldName);
        SemType fieldType = SemType.tryInto(field.getFieldType());
        if (isReadonly && isOptional) {
            fieldType = Builder.getUndefType();
        }
        MappingDefinition.Field field1 = new MappingDefinition.Field(field.getFieldName(), fieldType,
                isReadonly, isOptional);
        return field1;
    }

    @Override
    public boolean couldInherentTypeBeDifferent() {
        if (couldInhereTypeBeDifferentCache != 0) {
            return couldInhereTypeBeDifferentCache == 1;
        }
        boolean result = couldShapeBeDifferentInner();
        couldInhereTypeBeDifferentCache = (byte) (result ? 1 : 2);
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
        return Optional.of(shapeOfInner(cx, shapeSupplier, (MapValueImpl<?, ?>) object, true));
    }

    @Override
    public Optional<SemType> acceptedTypeOf(Context cx) {
        Env env = cx.env;
        if (acceptedTypeDefn.isDefinitionReady()) {
            return Optional.of(acceptedTypeDefn.getSemType(env));
        }
        var result = acceptedTypeDefn.trySetDefinition(MappingDefinition::new);
        if (!result.updated()) {
            return Optional.of(acceptedTypeDefn.getSemType(env));
        }
        MappingDefinition md = result.definition();
        return Optional.of(createSemTypeInner(md, env, CELL_MUT_UNLIMITED,
                (type) -> ShapeAnalyzer.acceptedTypeOf(cx, type).orElseThrow()));
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

    private MappingDefinition.Field fieldShape(Context cx, ShapeSupplier shapeSupplier, String fieldName,
                                               Object fieldValue, boolean alwaysTakeValueShape) {
        boolean readonlyField = fieldIsReadonly(fieldName);
        boolean optionalField = fieldIsOptional(fieldName);
        SemType fieldType;
        if (alwaysTakeValueShape || readonlyField) {
            optionalField = false;
            fieldType = shapeSupplier.get(cx, fieldValue).orElseThrow();
        } else {
            fieldType = SemType.tryInto(fieldType(fieldName));
        }
        return new MappingDefinition.Field(fieldName, fieldType, readonlyField, optionalField);
    }
}
