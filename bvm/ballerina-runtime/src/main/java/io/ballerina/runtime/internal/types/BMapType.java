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
import io.ballerina.runtime.api.types.semtype.CellAtomicType;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.Core;
import io.ballerina.runtime.api.types.semtype.Env;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.types.semtype.MappingDefinition;
import io.ballerina.runtime.internal.values.MapValueImpl;
import io.ballerina.runtime.internal.values.ReadOnlyUtils;

import java.util.Optional;

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
public class BMapType extends BType implements MapType, PartialSemTypeSupplier {

    public static final MappingDefinition.Field[] EMPTY_FIELD_ARR = new MappingDefinition.Field[0];
    private final Type constraint;
    private final boolean readonly;
    private IntersectionType immutableType;
    private IntersectionType intersectionType = null;
    private MappingDefinition defn;
    private final Env env = Env.getInstance();

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
    SemType createSemType(Context cx) {
        if (defn != null) {
            return defn.getSemType(env);
        }
        defn = new MappingDefinition();
        SemType restType = Builder.from(cx, getConstrainedType());
        SemType pureBTypePart = Core.intersect(restType, Core.B_TYPE_TOP);
        if (!Core.isNever(pureBTypePart)) {
            cx.markProvisionTypeReset();
            SemType pureSemTypePart = Core.intersect(restType, Core.SEMTYPE_TOP);
            SemType semTypePart = getSemTypePart(pureSemTypePart);
            SemType bTypePart = BTypeConverter.wrapAsPureBType(this);
            return Core.union(semTypePart, bTypePart);
        }
        return getSemTypePart(restType);
    }

    @Override
    public void resetSemTypeCache() {
        super.resetSemTypeCache();
        defn = null;
    }

    private SemType getSemTypePart(SemType restType) {
        CellAtomicType.CellMutability mut = isReadOnly() ? CellAtomicType.CellMutability.CELL_MUT_NONE :
                CellAtomicType.CellMutability.CELL_MUT_LIMITED;
        return defn.defineMappingTypeWrapped(env, EMPTY_FIELD_ARR, restType, mut);
    }
}
