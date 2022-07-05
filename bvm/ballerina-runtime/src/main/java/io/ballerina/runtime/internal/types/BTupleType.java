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
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.flags.TypeFlags;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.TupleType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.internal.values.ReadOnlyUtils;
import io.ballerina.runtime.internal.values.TupleValueImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * {@code {@link BTupleType}} represents a tuple type in Ballerina.
 *
 * @since 0.995.0
 */
public class BTupleType extends BAnnotatableType implements TupleType {

    private List<Type> tupleTypes;
    private Type restType;
    private int typeFlags;
    private final boolean readonly;
    private IntersectionType immutableType;
    private IntersectionType intersectionType = null;
    public boolean isCyclic = false;
    private boolean resolving;
    private boolean resolvingReadonly;
    private String cachedToString;

    /**
     * Create a {@code BTupleType} which represents the tuple type.
     *
     * @param typeList of the tuple type
     */
    public BTupleType(List<Type> typeList) {
        super(null, null, Object.class);
        this.tupleTypes = typeList;
        this.restType = null;
        checkAllMembers();
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

    public List<Type> getTupleTypes() {
        return tupleTypes;
    }

    public Type getRestType() {
        return restType;
    }

    public void setCyclic(boolean isCyclic) {
        this.isCyclic = isCyclic;
    }

    public void setMemberTypes(List<Type> members, Type restType) {
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
        checkAllMembers();
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
        if (!(o instanceof BTupleType)) {
            return false;
        }
        BTupleType that = (BTupleType) o;

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
        return TypeFlags.isFlagOn(this.typeFlags, TypeFlags.ANYDATA);
    }

    @Override
    public boolean isPureType() {
        return TypeFlags.isFlagOn(this.typeFlags, TypeFlags.PURETYPE);
    }

    public int getTypeFlags() {
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
}
