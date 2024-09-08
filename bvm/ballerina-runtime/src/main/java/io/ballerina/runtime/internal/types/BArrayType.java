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

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.flags.TypeFlags;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.internal.types.semtype.CellAtomicType;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.Definition;
import io.ballerina.runtime.api.types.semtype.Env;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.types.semtype.ListDefinition;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.ArrayValueImpl;
import io.ballerina.runtime.internal.values.ReadOnlyUtils;

import java.util.Optional;

import static io.ballerina.runtime.api.types.semtype.Builder.neverType;
import static io.ballerina.runtime.internal.types.semtype.CellAtomicType.CellMutability.CELL_MUT_NONE;

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
    private ListDefinition defn;
    private final Env env = Env.getInstance();
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
            if (other.state == ArrayState.CLOSED && this.size != other.size) {
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
    public Optional<IntersectionType> getIntersectionType() {
        return this.intersectionType ==  null ? Optional.empty() : Optional.of(this.intersectionType);
    }

    @Override
    public void setIntersectionType(IntersectionType intersectionType) {
        this.intersectionType = intersectionType;
    }

    @Override
    public synchronized SemType createSemType() {
        if (defn != null) {
            return defn.getSemType(env);
        }
        ListDefinition ld = new ListDefinition();
        defn = ld;
        return getSemTypePart(ld, isReadOnly(), size, tryInto(getElementType()));
    }

    private SemType getSemTypePart(ListDefinition defn, boolean isReadOnly, int size, SemType elementType) {
        CellAtomicType.CellMutability mut = isReadOnly ? CellAtomicType.CellMutability.CELL_MUT_NONE :
                CellAtomicType.CellMutability.CELL_MUT_LIMITED;
        if (size == -1) {
            return defn.defineListTypeWrapped(env, EMPTY_SEMTYPE_ARR, 0, elementType, mut);
        } else {
            SemType[] initial = {elementType};
            return defn.defineListTypeWrapped(env, initial, size, neverType(), mut);
        }
    }

    @Override
    public void resetSemType() {
        defn = null;
        super.resetSemType();
    }

    @Override
    public Optional<SemType> inherentTypeOf(Context cx, ShapeSupplier shapeSupplier, Object object) {
        if (!couldInherentTypeBeDifferent()) {
            return Optional.of(getSemType());
        }
        BArray value = (BArray) object;
        SemType cachedShape = value.shapeOf();
        if (cachedShape != null) {
            return Optional.of(cachedShape);
        }
        SemType semType = readonlyShape(cx, shapeSupplier, value);
        value.cacheShape(semType);
        return Optional.of(semType);
    }

    @Override
    public boolean couldInherentTypeBeDifferent() {
        return isReadOnly();
    }

    @Override
    public Optional<SemType> shapeOf(Context cx, ShapeSupplier shapeSupplier, Object object) {
        return Optional.of(readonlyShape(cx, shapeSupplier, (BArray) object));
    }

    private SemType readonlyShape(Context cx, ShapeSupplier shapeSupplier, BArray value) {
        int size = value.size();
        SemType[] memberTypes = new SemType[size];
        ListDefinition ld;
        Optional<Definition> readonlyShapeDefinition = value.getReadonlyShapeDefinition();
        if (readonlyShapeDefinition.isPresent()) {
            ld = (ListDefinition) readonlyShapeDefinition.get();
            return ld.getSemType(cx.env);
        } else {
            ld = new ListDefinition();
            value.setReadonlyShapeDefinition(ld);
        }
        for (int i = 0; i < size; i++) {
            Optional<SemType> memberType = shapeSupplier.get(cx, value.get(i));
            assert memberType.isPresent();
            memberTypes[i] = memberType.get();
        }
        SemType semType = ld.defineListTypeWrapped(env, memberTypes, memberTypes.length, neverType(), CELL_MUT_NONE);
        value.resetReadonlyShapeDefinition();
        return semType;
    }
}
