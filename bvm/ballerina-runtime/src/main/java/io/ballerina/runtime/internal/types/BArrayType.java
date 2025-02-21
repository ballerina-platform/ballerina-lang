/*
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.internal.types;

import io.ballerina.runtime.api.flags.TypeFlags;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.TypeTags;
import io.ballerina.runtime.api.types.semtype.BasicTypeBitSet;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.CacheableTypeDescriptor;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.Env;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.api.types.semtype.ShapeAnalyzer;
import io.ballerina.runtime.api.types.semtype.TypeCheckCache;
import io.ballerina.runtime.api.types.semtype.TypeCheckCacheFactory;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.types.semtype.CacheFactory;
import io.ballerina.runtime.internal.types.semtype.CellAtomicType;
import io.ballerina.runtime.internal.types.semtype.DefinitionContainer;
import io.ballerina.runtime.internal.types.semtype.ListDefinition;
import io.ballerina.runtime.internal.values.AbstractArrayValue;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.ArrayValueImpl;
import io.ballerina.runtime.internal.values.ReadOnlyUtils;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static io.ballerina.runtime.api.types.semtype.Builder.getNeverType;
import static io.ballerina.runtime.internal.types.semtype.CellAtomicType.CellMutability.CELL_MUT_LIMITED;
import static io.ballerina.runtime.internal.types.semtype.CellAtomicType.CellMutability.CELL_MUT_NONE;
import static io.ballerina.runtime.internal.types.semtype.CellAtomicType.CellMutability.CELL_MUT_UNLIMITED;

/**
 * {@code BArrayType} represents a type of an arrays in Ballerina.
 * <p>
 * Arrays are defined using the arrays constructor [] as follows:
 * TypeName[]
 * <p>
 * All arrays are unbounded in length and support 0 based indexing.
 *
 * @since 0.995.0
 */
@SuppressWarnings("unchecked")
public class BArrayType extends BType implements ArrayType, TypeWithShape {

    private static final BasicTypeBitSet BASIC_TYPE = Builder.getListType();

    private static final SemType[] EMPTY_SEMTYPE_ARR = new SemType[0];
    private Type elementType;
    private int dimensions = 1;
    private int size = -1;
    private final boolean hasFillerValue;
    private ArrayState state = ArrayState.OPEN;

    private final boolean readonly;
    private IntersectionType immutableType;
    private IntersectionType intersectionType = null;
    private int typeFlags;
    private final DefinitionContainer<ListDefinition> defn = new DefinitionContainer<>();
    private final DefinitionContainer<ListDefinition> acceptedTypeDefn = new DefinitionContainer<>();
    public BArrayType(Type elementType) {
        this(elementType, false);
    }

    public BArrayType(Type elementType, boolean readonly) {
        this(elementType, -1, readonly);
    }

    public BArrayType(Type elemType, int size) {
        this(elemType, size, false);
    }

    public BArrayType(Type elemType, int size, boolean readonly) {
        this(elemType, size, readonly, 0);
    }

    public BArrayType(Type elemType, int size, boolean readonly, int typeFlags) {
        this(typeFlags, size, readonly, TypeChecker.hasFillerValue(elemType));
        setElementType(elemType, 1, elemType.isReadOnly());
        setFlagsBasedOnElementType();
    }

    public BArrayType(int typeFlags, int size, boolean readonly, boolean hasFillerValue) {
        super(null, null, ArrayValue.class);
        this.typeFlags = typeFlags;
        if (size != -1) {
            state = ArrayState.CLOSED;
            this.size = size;
        }
        this.readonly = readonly;
        this.hasFillerValue = hasFillerValue;
    }

    public void setElementType(Type elementType, int dimensions, boolean elementRO) {
        if (this.elementType != null) {
            resetSemType();
        }
        this.elementType = readonly && !elementRO ? ReadOnlyUtils.getReadOnlyType(elementType) : elementType;
        this.dimensions = dimensions;
        if (size == -1) {
            TypeCheckCacheData.TypeCheckCacheFlyweight data;
            if (isReadOnly()) {
                data = TypeCheckCacheData.getRO(elementType);
            } else {
                data = TypeCheckCacheData.getRW(elementType);
            }
            this.typeId = data.typeId;
            this.typeCheckCache = data.typeCheckCache;
        }
    }

    private void setFlagsBasedOnElementType() {
        if (elementType.isNilable()) {
            this.typeFlags = TypeFlags.addToMask(this.typeFlags, TypeFlags.NILABLE);
        }
        if (elementType.isAnydata()) {
            this.typeFlags = TypeFlags.addToMask(this.typeFlags, TypeFlags.ANYDATA);
        }
        if (elementType.isPureType()) {
            this.typeFlags = TypeFlags.addToMask(this.typeFlags, TypeFlags.PURETYPE);
        }
    }

    @Override
    public Type getElementType() {
        return elementType;
    }

    @Override
    public <V extends Object> V getZeroValue() {
        return getEmptyValue();
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        return (V) new ArrayValueImpl(this);
    }

    @Override
    public int getTag() {
        return TypeTags.ARRAY_TAG;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BArrayType other) {
            if ((other.state == ArrayState.CLOSED || this.state == ArrayState.CLOSED) && this.size != other.size) {
                return false;
            }
            return this.elementType.equals(other.elementType) && this.readonly == other.readonly;
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Type tempElementType = elementType;
        sb.append(getSizeString());
        while (tempElementType.getTag() == TypeTags.ARRAY_TAG) {
            BArrayType arrayElement = (BArrayType) tempElementType;
            sb.append(arrayElement.getSizeString());
            tempElementType = arrayElement.elementType;
        }
        sb.insert(0, tempElementType);
        return !readonly ? sb.toString() : sb.append(" & readonly").toString();
    }

    private String getSizeString() {
        return size != -1 ? "[" + size + "]" : "[]";
    }

    public int getDimensions() {
        return this.dimensions;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public boolean hasFillerValue() {
        return hasFillerValue;
    }

    @Override
    public ArrayState getState() {
        return state;
    }

    @Override
    public boolean isAnydata() {
        return TypeFlags.isFlagOn(this.typeFlags, TypeFlags.ANYDATA);
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
    public BasicTypeBitSet getBasicType() {
        return BASIC_TYPE;
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
        var result = defn.trySetDefinition(ListDefinition::new);
        if (!result.updated()) {
            return defn.getSemType(env);
        }
        ListDefinition ld = result.definition();
        CellAtomicType.CellMutability mut = isReadOnly() ? CellAtomicType.CellMutability.CELL_MUT_NONE :
                CellAtomicType.CellMutability.CELL_MUT_LIMITED;
        SemType semType = getSemTypePart(env, ld, size, tryInto(cx, getElementType()), mut);
        TypeCheckCacheData.cachedSemTypes.put(typeId, semType);
        return semType;
    }

    private SemType getSemTypePart(Env env, ListDefinition defn, int size, SemType elementType,
                                   CellAtomicType.CellMutability mut) {
        if (size == -1) {
            return defn.defineListTypeWrapped(env, EMPTY_SEMTYPE_ARR, 0, elementType, mut);
        } else {
            SemType[] initial = {elementType};
            return defn.defineListTypeWrapped(env, initial, size, getNeverType(), mut);
        }
    }

    @Override
    public void resetSemType() {
        defn.clear();
        super.resetSemType();
    }

    @Override
    protected boolean isDependentlyTypedInner(Set<MayBeDependentType> visited) {
        return elementType instanceof MayBeDependentType eType && eType.isDependentlyTyped(visited);
    }

    @Override
    public Optional<SemType> inherentTypeOf(Context cx, ShapeSupplier shapeSupplier, Object object) {
        if (!couldInherentTypeBeDifferent()) {
            return Optional.of(getSemType(cx));
        }
        AbstractArrayValue value = (AbstractArrayValue) object;
        SemType cachedShape = value.shapeOf();
        if (cachedShape != null) {
            return Optional.of(cachedShape);
        }
        SemType semType = shapeOfInner(cx, shapeSupplier, value);
        value.cacheShape(semType);
        return Optional.of(semType);
    }

    @Override
    public boolean couldInherentTypeBeDifferent() {
        return isReadOnly();
    }

    @Override
    public Optional<SemType> shapeOf(Context cx, ShapeSupplier shapeSupplier, Object object) {
        return Optional.of(shapeOfInner(cx, shapeSupplier, (AbstractArrayValue) object));
    }

    @Override
    public SemType acceptedTypeOf(Context cx) {
        Env env = cx.env;
        if (acceptedTypeDefn.isDefinitionReady()) {
            return acceptedTypeDefn.getSemType(cx.env);
        }
        var result = acceptedTypeDefn.trySetDefinition(ListDefinition::new);
        if (!result.updated()) {
            return acceptedTypeDefn.getSemType(env);
        }
        return getSemTypePart(env, result.definition(), size, ShapeAnalyzer.acceptedTypeOf(cx, getElementType()),
                CELL_MUT_UNLIMITED);
    }

    private SemType shapeOfInner(Context cx, ShapeSupplier shapeSupplier, AbstractArrayValue value) {
        ListDefinition readonlyShapeDefinition = value.getReadonlyShapeDefinition();
        if (readonlyShapeDefinition != null) {
            return readonlyShapeDefinition.getSemType(cx.env);
        }
        int size = value.size();
        SemType[] memberTypes = new SemType[size];
        ListDefinition ld = new ListDefinition();
        value.setReadonlyShapeDefinition(ld);
        for (int i = 0; i < size; i++) {
            Optional<SemType> memberType = shapeSupplier.get(cx, value.get(i));
            assert memberType.isPresent();
            memberTypes[i] = memberType.get();
        }
        CellAtomicType.CellMutability mut = isReadOnly() ? CELL_MUT_NONE : CELL_MUT_LIMITED;
        SemType semType = ld.defineListTypeWrapped(cx.env, memberTypes, memberTypes.length, getNeverType(), mut);
        value.resetReadonlyShapeDefinition();
        return semType;
    }

    private static class TypeCheckCacheData {

        private static final Map<Integer, SemType> cachedSemTypes = new ConcurrentHashMap<>();
        private static final Map<Integer, TypeCheckCacheFlyweight> cacheRW = CacheFactory.createCachingHashMap();
        private static final Map<Integer, TypeCheckCacheFlyweight> cacheRO = CacheFactory.createCachingHashMap();

        private record TypeCheckCacheFlyweight(int typeId, TypeCheckCache typeCheckCache) {

            public static TypeCheckCacheFlyweight create() {
                return new TypeCheckCacheFlyweight(TypeIdSupplier.getAnonId(), TypeCheckCacheFactory.create());
            }
        }

        public static TypeCheckCacheFlyweight getRW(Type constraint) {
            if (constraint instanceof CacheableTypeDescriptor cacheableTypeDescriptor) {
                return get(cacheableTypeDescriptor, cacheRW);
            }
            return TypeCheckCacheFlyweight.create();
        }

        public static TypeCheckCacheFlyweight getRO(Type constraint) {
            if (constraint instanceof CacheableTypeDescriptor cacheableTypeDescriptor) {
                return get(cacheableTypeDescriptor, cacheRO);
            }
            return TypeCheckCacheFlyweight.create();
        }

        private static TypeCheckCacheFlyweight get(CacheableTypeDescriptor type,
                                                   Map<Integer, TypeCheckCacheFlyweight> cache) {
            int typeId = type.typeId();
            var cached = cache.get(typeId);
            if (cached != null) {
                return cached;
            }
            var flyWeight = TypeCheckCacheFlyweight.create();
            cache.put(typeId, flyWeight);
            return flyWeight;
        }
    }
}
