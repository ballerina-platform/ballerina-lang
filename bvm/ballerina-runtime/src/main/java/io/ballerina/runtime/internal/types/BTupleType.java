/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.ballerina.runtime.internal.types;

import io.ballerina.identifier.Utils;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.flags.TypeFlags;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.TupleType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.TypeTags;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.Core;
import io.ballerina.runtime.api.types.semtype.Env;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.api.types.semtype.ShapeAnalyzer;
import io.ballerina.runtime.internal.types.semtype.CellAtomicType;
import io.ballerina.runtime.internal.types.semtype.DefinitionContainer;
import io.ballerina.runtime.internal.types.semtype.ListDefinition;
import io.ballerina.runtime.internal.values.AbstractArrayValue;
import io.ballerina.runtime.internal.values.ReadOnlyUtils;
import io.ballerina.runtime.internal.values.TupleValueImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static io.ballerina.runtime.api.types.semtype.Builder.getNeverType;
import static io.ballerina.runtime.internal.types.semtype.CellAtomicType.CellMutability.CELL_MUT_NONE;
import static io.ballerina.runtime.internal.types.semtype.CellAtomicType.CellMutability.CELL_MUT_UNLIMITED;

/**
 * {@code {@link BTupleType}} represents a tuple type in Ballerina.
 *
 * @since 0.995.0
 */
public class BTupleType extends BAnnotatableType implements TupleType, TypeWithShape {

    private List<Type> tupleTypes;
    private Type restType;
    private int typeFlags;
    private final boolean readonly;
    // This is used avoid unnecessary flag updates when we change the members. If this
    // is set before accessing flags you must call {@code checkAllMembers}.
    private volatile boolean flagsPoisoned = false;
    private IntersectionType immutableType;
    private IntersectionType intersectionType = null;
    public boolean isCyclic = false;
    private boolean resolving;
    private boolean resolvingReadonly;
    private String cachedToString;
    private final DefinitionContainer<ListDefinition> defn = new DefinitionContainer<>();
    private final DefinitionContainer<ListDefinition> acceptedTypeDefn = new DefinitionContainer<>();

    /**
     * Create a {@code BTupleType} which represents the tuple type.
     *
     * @param typeList of the tuple type
     */
    public BTupleType(List<Type> typeList) {
        super(null, null, Object.class);
        this.tupleTypes = typeList;
        this.restType = null;
        this.flagsPoisoned = true;
        this.readonly = false;
    }

    public BTupleType(List<Type> typeList, int typeFlags) {
        this(typeList, null, typeFlags, false, false);
    }

    public BTupleType(List<Type> typeList, Type restType, int typeFlags, boolean readonly) {
        this(typeList, restType, typeFlags, false, readonly);
    }

    /**
     * Create a {@code BTupleType} which represents the tuple type.
     *
     * @param typeList of the tuple type
     * @param restType of the tuple type
     * @param typeFlags flags associated with the type
     * @param readonly whether immutable
     */
    public BTupleType(List<Type> typeList, Type restType, int typeFlags, boolean isCyclic, boolean readonly) {
        super(null, null, Object.class);
        if (readonly) {
            this.resolvingReadonly = true;
            this.tupleTypes = getReadOnlyTypes(typeList);
            this.restType = restType != null ? ReadOnlyUtils.getReadOnlyType(restType) : null;
            this.resolvingReadonly = false;
        } else {
            this.tupleTypes = typeList;
            this.restType = restType;
        }
        this.typeFlags = typeFlags;
        this.isCyclic = isCyclic;
        this.readonly = readonly;
    }

    public BTupleType(String name, Module pkg, int typeFlags, boolean isCyclic, boolean readonly) {
        super(name, pkg, Object.class);
        this.typeFlags = typeFlags;
        this.tupleTypes = new ArrayList<>(0);
        this.restType = null;
        this.isCyclic = isCyclic;
        this.readonly = readonly;
    }

    private void checkAllMembers() {
        if (this.resolving) {
            return;
        }
        this.resolving = true;
        this.resolvingReadonly = true;
        boolean isAllMembersPure = true;
        boolean isAllMembersAnydata = true;
        for (Type memberType : tupleTypes) {
            isAllMembersPure &= memberType.isPureType();
            isAllMembersAnydata &= memberType.isAnydata();
        }
        if (restType != null) {
            isAllMembersPure &= restType.isPureType();
            isAllMembersAnydata &= restType.isAnydata();
        }
        this.resolvingReadonly = false;
        this.resolving = false;
        if (isAllMembersPure) {
            this.typeFlags = TypeFlags.addToMask(this.typeFlags, TypeFlags.PURETYPE);
        }
        if (isAllMembersAnydata) {
            this.typeFlags = TypeFlags.addToMask(this.typeFlags, TypeFlags.ANYDATA, TypeFlags.PURETYPE);
        }
    }

    private List<Type> getReadOnlyTypes(List<Type> typeList) {
        List<Type> readOnlyTypes = new ArrayList<>();
        for (Type type : typeList) {
            readOnlyTypes.add(ReadOnlyUtils.getReadOnlyType(type));
        }
        return readOnlyTypes;
    }

    @Override
    public List<Type> getTupleTypes() {
        return tupleTypes;
    }

    @Override
    public Type getRestType() {
        return restType;
    }

    public void setCyclic(boolean isCyclic) {
        this.isCyclic = isCyclic;
    }

    public void setMemberTypes(List<Type> members, Type restType) {
        resetSemType();
        if (members == null) {
            return;
        }
        if (readonly) {
            this.resolvingReadonly = true;
            this.tupleTypes = getReadOnlyTypes(members);
            this.restType = restType != null ? ReadOnlyUtils.getReadOnlyType(restType) : null;
            this.resolvingReadonly = false;
        } else {
            this.tupleTypes = members;
            this.restType = restType;
        }
        flagsPoisoned = true;
        defn.clear();
    }

    @Override
    public <V extends Object> V getZeroValue() {
        return (V) new TupleValueImpl(this);
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        return getZeroValue();
    }

    @Override
    public int getTag() {
        return TypeTags.TUPLE_TAG;
    }

    private String getQualifiedName(String name) {
        return (pkg == null || pkg.getName() == null || pkg.getName().equals(".")) ? name :
                pkg.toString() + ":" + name;
    }

    @Override
    public String toString() {
        // This logic is added to prevent duplicate recursive calls to toString
        if (resolving) {
            if (typeName != null) {
                return getQualifiedName(typeName);
            }
            return "...";
        }

        resolving = true;
        computeStringRepresentation();
        resolving = false;
        return cachedToString;
    }

    private void computeStringRepresentation() {
        if (cachedToString != null) {
            return;
        }
        StringBuilder stringRep =
                new StringBuilder("[").append(tupleTypes.stream().map(Type::toString).collect(Collectors.joining(",")));
        if (restType != null) {
            stringRep.append(tupleTypes.isEmpty() ? "" : ",").append(restType).append("...]");
        } else {
            stringRep.append("]");
        }
        cachedToString = readonly ? stringRep + " & readonly" : stringRep.toString();
    }

    @Override
    public boolean isCyclic() {
        return isCyclic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BTupleType that)) {
            return false;
        }

        if (this.isCyclic || that.isCyclic) {
            if (this.isCyclic != that.isCyclic) {
                return false;
            }
            return super.equals(that);
        }
        if (this.readonly != that.readonly) {
            return false;
        }
        if ((this.restType == null || that.restType == null) && this.restType != that.restType) {
            // If the rest type is null in only one tuple type.
            return false;
        }
        if (this.restType == null) {
            return Objects.equals(tupleTypes, that.tupleTypes);
        }
        return Objects.equals(tupleTypes, that.tupleTypes) && this.restType.equals(that.restType);
    }

    @Override
    public boolean isAnydata() {
        return TypeFlags.isFlagOn(getTypeFlags(), TypeFlags.ANYDATA);
    }

    @Override
    public boolean isPureType() {
        return TypeFlags.isFlagOn(getTypeFlags(), TypeFlags.PURETYPE);
    }

    @Override
    public int getTypeFlags() {
        if (flagsPoisoned) {
            synchronized (this) {
                if (flagsPoisoned) {
                    checkAllMembers();
                    flagsPoisoned = false;
                }
            }
        }
        return this.typeFlags;
    }

    @Override
    public boolean isReadOnly() {
        if (this.resolvingReadonly) {
            return true;
        }
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
    public String getAnnotationKey() {
        return Utils.decodeIdentifier(this.typeName);
    }

    @Override
    public SemType createSemType(Context cx) {
        Env env = cx.env;
        if (defn.isDefinitionReady()) {
            return defn.getSemType(env);
        }
        var result = defn.trySetDefinition(ListDefinition::new);
        if (!result.updated()) {
            return defn.getSemType(env);
        }
        ListDefinition ld = result.definition();
        return createSemTypeInner(cx, ld, SemType::tryInto, mut());
    }

    private CellAtomicType.CellMutability mut() {
        return isReadOnly() ? CELL_MUT_NONE : CellAtomicType.CellMutability.CELL_MUT_LIMITED;
    }

    private SemType createSemTypeInner(Context cx, ListDefinition ld,
                                       BiFunction<Context, Type, SemType> semTypeFunction,
                                       CellAtomicType.CellMutability mut) {
        Env env = cx.env;
        SemType[] memberTypes = new SemType[tupleTypes.size()];
        for (int i = 0; i < tupleTypes.size(); i++) {
            SemType memberType = semTypeFunction.apply(cx, tupleTypes.get(i));
            if (Core.isNever(memberType)) {
                return getNeverType();
            }
            memberTypes[i] = memberType;
        }
        SemType rest = restType != null ? semTypeFunction.apply(cx, restType) : getNeverType();
        return ld.defineListTypeWrapped(env, memberTypes, memberTypes.length, rest, mut);
    }

    @Override
    public void resetSemType() {
        defn.clear();
        super.resetSemType();
    }

    @Override
    protected boolean isDependentlyTypedInner(Set<MayBeDependentType> visited) {
        return tupleTypes.stream().filter(each -> each instanceof MayBeDependentType)
                .anyMatch(each -> ((MayBeDependentType) each).isDependentlyTyped(visited));
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
    public Optional<SemType> acceptedTypeOf(Context cx) {
        Env env = cx.env;
        if (acceptedTypeDefn.isDefinitionReady()) {
            return Optional.of(acceptedTypeDefn.getSemType(env));
        }
        var result = acceptedTypeDefn.trySetDefinition(ListDefinition::new);
        if (!result.updated()) {
            return Optional.of(acceptedTypeDefn.getSemType(env));
        }
        ListDefinition ld = result.definition();
        return Optional.of(createSemTypeInner(cx, ld,
                (context, type) -> ShapeAnalyzer.acceptedTypeOf(context, type).orElseThrow(),
                CELL_MUT_UNLIMITED));
    }

    private SemType shapeOfInner(Context cx, ShapeSupplier shapeSupplier, AbstractArrayValue value) {
        Env env = cx.env;
        ListDefinition defn = value.getReadonlyShapeDefinition();
        if (defn != null) {
            return defn.getSemType(env);
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
        SemType semType = ld.defineListTypeWrapped(env, memberTypes, memberTypes.length, getNeverType(), mut());
        value.resetReadonlyShapeDefinition();
        return semType;
    }
}
