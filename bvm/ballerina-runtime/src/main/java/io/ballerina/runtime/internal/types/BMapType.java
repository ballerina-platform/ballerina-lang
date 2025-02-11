/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.internal.types;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.PredefinedTypes;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.TypeTags;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.CacheableTypeDescriptor;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.Env;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.api.types.semtype.ShapeAnalyzer;
import io.ballerina.runtime.api.types.semtype.TypeCheckCache;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.types.semtype.CellAtomicType;
import io.ballerina.runtime.internal.types.semtype.DefinitionContainer;
import io.ballerina.runtime.internal.types.semtype.MappingDefinition;
import io.ballerina.runtime.internal.values.MapValueImpl;
import io.ballerina.runtime.internal.values.ReadOnlyUtils;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static io.ballerina.runtime.internal.types.semtype.CellAtomicType.CellMutability.CELL_MUT_NONE;
import static io.ballerina.runtime.internal.types.semtype.CellAtomicType.CellMutability.CELL_MUT_UNLIMITED;

/**
 * {@code BMapType} represents a type of a map in Ballerina.
 * <p>
 * Maps are defined using the map keyword as follows:
 * map mapName
 * <p>
 * All maps are unbounded in length and support key based indexing.
 *
 * @since 0.995.0
 */
@SuppressWarnings("unchecked")
public class BMapType extends BType implements MapType, TypeWithShape, Cloneable {

    public static final MappingDefinition.Field[] EMPTY_FIELD_ARR = new MappingDefinition.Field[0];
    private final Type constraint;
    private final boolean readonly;
    private IntersectionType immutableType;
    private IntersectionType intersectionType = null;
    private final DefinitionContainer<MappingDefinition> defn = new DefinitionContainer<>();
    private final DefinitionContainer<MappingDefinition> acceptedTypeDefn = new DefinitionContainer<>();
    private final boolean shouldCache;

    public BMapType(Type constraint) {
        this(constraint, false);
    }

    public BMapType(Type constraint, boolean readonly) {
        this(TypeConstants.MAP_TNAME, constraint, null, readonly);
    }

    /**
     * Create a type from the given name.
     *
     * @param typeName   string name of the type.
     * @param constraint constraint type which particular map is bound to.
     * @param pkg        package for the type.
     */
    public BMapType(String typeName, Type constraint, Module pkg) {
        this(typeName, constraint, pkg, false);
    }

    public BMapType(String typeName, Type constraint, Module pkg, boolean readonly) {
        super(typeName, pkg, MapValueImpl.class);
        this.constraint = readonly ? ReadOnlyUtils.getReadOnlyType(constraint) : constraint;
        this.readonly = readonly;
        this.shouldCache = constraint instanceof CacheableTypeDescriptor cacheableTypeDescriptor &&
                cacheableTypeDescriptor.shouldCache();
        var data = readonly ? TypeCheckCacheData.getRO(constraint) : TypeCheckCacheData.getRW(constraint);
        this.typeId = data.typeId;
        this.typeCheckCache = data.typeCheckCache;
    }

    /**
     * Returns element types which this map is constrained to.
     *
     * @return constraint type.
     */
    @Override
    public Type getConstrainedType() {
        return constraint;
    }

    /**
     * Returns element type which this map contains.
     *
     * @return element type.
     * @deprecated use {@link #getConstrainedType()} instead.
     */
    @Deprecated
    public Type getElementType() {
        return constraint;
    }

    @Override
    public <V extends Object> V getZeroValue() {
        return (V) new MapValueImpl<BString, V>(new BMapType(constraint));
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        return getZeroValue();
    }

    @Override
    public int getTag() {
        return TypeTags.MAP_TAG;
    }

    @Override
    public String toString() {
        String stringRep;

        if (constraint == PredefinedTypes.TYPE_ANY) {
            stringRep = super.toString();
        } else {
            stringRep = "map" + "<" + constraint.toString() + ">";
        }

        return !readonly ? stringRep : stringRep.concat(" & readonly");
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj) || !(obj instanceof BMapType other)) {
            return false;
        }

        if (this.readonly != other.readonly) {
            return false;
        }

        if (constraint == other.constraint) {
            return true;
        }

        return constraint.equals(other.constraint);
    }

    @Override
    public boolean isAnydata() {
        return this.constraint.isAnydata();
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
    public SemType createSemType(Context cx) {
        Env env = cx.env;
        if (defn.isDefinitionReady()) {
            return defn.getSemType(env);
        }
        SemType cachedSemtype = TypeCheckCacheData.cachedSemTypes.get(typeId);
        if (cachedSemtype != null) {
            return cachedSemtype;
        }
        var result = defn.trySetDefinition(MappingDefinition::new);
        if (!result.updated()) {
            return defn.getSemType(env);
        }
        MappingDefinition md = result.definition();
        CellAtomicType.CellMutability mut = isReadOnly() ? CELL_MUT_NONE :
                CellAtomicType.CellMutability.CELL_MUT_LIMITED;
        SemType semType = createSemTypeInner(env, md, tryInto(cx, getConstrainedType()), mut);
        TypeCheckCacheData.cachedSemTypes.put(typeId, semType);
        return semType;
    }

    @Override
    public void resetSemType() {
        defn.clear();
        super.resetSemType();
    }

    @Override
    public Optional<SemType> inherentTypeOf(Context cx, ShapeSupplier shapeSupplier, Object object) {
        if (!couldInherentTypeBeDifferent()) {
            return Optional.of(getSemType(cx));
        }
        MapValueImpl<?, ?> value = (MapValueImpl<?, ?>) object;
        SemType cachedShape = value.shapeOf();
        if (cachedShape != null) {
            return Optional.of(cachedShape);
        }

        return shapeOfInner(cx, shapeSupplier, value);
    }

    @Override
    public boolean couldInherentTypeBeDifferent() {
        return isReadOnly();
    }

    @Override
    public Optional<SemType> shapeOf(Context cx, ShapeSupplier shapeSupplierFn, Object object) {
        return shapeOfInner(cx, shapeSupplierFn, (MapValueImpl<?, ?>) object);
    }

    @Override
    public synchronized SemType acceptedTypeOf(Context cx) {
        Env env = cx.env;
        if (acceptedTypeDefn.isDefinitionReady()) {
            return acceptedTypeDefn.getSemType(env);
        }
        var result = acceptedTypeDefn.trySetDefinition(MappingDefinition::new);
        if (!result.updated()) {
            return acceptedTypeDefn.getSemType(env);
        }
        MappingDefinition md = result.definition();
        SemType elementType = ShapeAnalyzer.acceptedTypeOf(cx, getConstrainedType());
        return createSemTypeInner(env, md, elementType, CELL_MUT_UNLIMITED);
    }

    static Optional<SemType> shapeOfInner(Context cx, ShapeSupplier shapeSupplier, MapValueImpl<?, ?> value) {
        MappingDefinition readonlyShapeDefinition = value.getReadonlyShapeDefinition();
        if (readonlyShapeDefinition != null) {
            return Optional.of(readonlyShapeDefinition.getSemType(cx.env));
        }
        int nFields = value.size();
        MappingDefinition md = new MappingDefinition();
        value.setReadonlyShapeDefinition(md);
        MappingDefinition.Field[] fields = new MappingDefinition.Field[nFields];
        Map.Entry<?, ?>[] entries = value.entrySet().toArray(Map.Entry[]::new);
        for (int i = 0; i < nFields; i++) {
            Optional<SemType> valueType = shapeSupplier.get(cx, entries[i].getValue());
            SemType fieldType = valueType.orElseThrow();
            fields[i] = new MappingDefinition.Field(entries[i].getKey().toString(), fieldType, true, false);
        }
        CellAtomicType.CellMutability mut = value.getType().isReadOnly() ? CELL_MUT_NONE :
                CellAtomicType.CellMutability.CELL_MUT_LIMITED;
        SemType semType = md.defineMappingTypeWrapped(cx.env, fields, Builder.getNeverType(), mut);
        value.cacheShape(semType);
        value.resetReadonlyShapeDefinition();
        return Optional.of(semType);
    }

    private SemType createSemTypeInner(Env env, MappingDefinition defn, SemType restType,
                                       CellAtomicType.CellMutability mut) {
        return defn.defineMappingTypeWrapped(env, EMPTY_FIELD_ARR, restType, mut);
    }

    @Override
    public BMapType clone() {
        BMapType clone = (BMapType) super.clone();
        clone.defn.clear();
        return clone;
    }

    @Override
    public boolean shouldCache() {
        return shouldCache;
    }

    @Override
    protected boolean isDependentlyTypedInner(Set<MayBeDependentType> visited) {
        return constraint instanceof MayBeDependentType constraintType && constraintType.isDependentlyTyped(visited);
    }

    private static class TypeCheckCacheData {

        private static final Map<Integer, SemType> cachedSemTypes = new ConcurrentHashMap<>();

        private static final Map<Type, TypeCheckCacheRecord> cacheRO = new IdentityHashMap<>();
        private static final Map<Type, TypeCheckCacheRecord> cacheRW = new IdentityHashMap<>();

        public static TypeCheckCacheRecord getRO(Type constraint) {
            if (constraint instanceof BTypeReferenceType referenceType) {
                assert referenceType.getReferredType() != null;
                return getRO(referenceType.getReferredType());
            }
            return cacheRO.computeIfAbsent(constraint, ignored -> new TypeCheckCacheRecord(TypeIdSupplier.getAnonId(),
                    TypeCheckCache.TypeCheckCacheFactory.create()));

        }

        private record TypeCheckCacheRecord(int typeId, TypeCheckCache typeCheckCache) {

        }

        public static TypeCheckCacheRecord getRW(Type constraint) {
            if (constraint instanceof BTypeReferenceType referenceType) {
                assert referenceType.getReferredType() != null;
                return getRW(referenceType.getReferredType());
            }
            return cacheRW.computeIfAbsent(constraint, ignored -> new TypeCheckCacheRecord(TypeIdSupplier.getAnonId(),
                    TypeCheckCache.TypeCheckCacheFactory.create()));

        }
    }
}
