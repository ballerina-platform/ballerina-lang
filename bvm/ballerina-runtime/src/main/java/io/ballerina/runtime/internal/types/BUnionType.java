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

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.flags.TypeFlags;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.internal.values.ReadOnlyUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * {@code BUnionType} represents a union type in Ballerina.
 *
 * @since 0.995.0
 */
public class BUnionType extends BType implements UnionType {

    public boolean isCyclic = false;
    public static final char PIPE = '|';
    private List<Type> memberTypes;
    private Boolean nullable;
    private String cachedToString;
    private int typeFlags;
    private final boolean readonly;
    protected IntersectionType immutableType;
    private boolean resolving = false;
    public boolean resolvingReadonly = false;

    private static final String INT_CLONEABLE = "__Cloneable";
    private static final String CLONEABLE = "Cloneable";
    private static final Pattern pCloneable = Pattern.compile(INT_CLONEABLE + "([12])?");

    /**
     * Create a {@code BUnionType} which represents the union type.
     */
    @Deprecated
    public BUnionType() {
        super(null, null, Object.class);
        this.readonly = false;
    }

    /**
     * Create a {@code BUnionType} which represents the union type.
     *
     * @param memberTypes of the union type
     * @param typeFlags flags associated with the type
     */
    public BUnionType(List<Type> memberTypes, int typeFlags) {
        this(memberTypes, typeFlags, false, false);
    }

    public BUnionType(List<Type> memberTypes, int typeFlags, boolean readonly,  boolean isCyclic) {
        super(null, null, Object.class);
        this.typeFlags = typeFlags;
        this.readonly = readonly;
        setMemberTypes(memberTypes);
        this.isCyclic = isCyclic;
    }

    public BUnionType(List<Type> memberTypes, int typeFlags, boolean readonly,  boolean isCyclic,
                      Set<Type> unresolvedTypes) {
        super(null, null, Object.class);
        this.typeFlags = typeFlags;
        this.readonly = readonly;
        setMemberTypes(memberTypes);
        this.isCyclic = isCyclic;
    }

    public BUnionType(List<Type> memberTypes) {
        this(memberTypes, false);
    }

    public BUnionType(List<Type> memberTypes, String typeName, int typeFlags, boolean readonly, boolean isCyclic) {
        super(typeName, null, Object.class);
        this.typeFlags = typeFlags;
        this.readonly = readonly;
        this.isCyclic = isCyclic;
        setMemberTypes(memberTypes);
        this.typeName = typeName;
    }

    public BUnionType(String typeName, Module pkg, List<Type> memberTypes) {
        super(typeName, pkg, Object.class);
        this.readonly = false;
        setMemberTypes(memberTypes);
    }

    public BUnionType(List<Type> memberTypes, boolean readonly) {
        this(memberTypes, 0, readonly, false);
    }

    public BUnionType(List<Type> memberTypes, boolean readonly, boolean isCyclic, Set<Type> unresolvedTypes) {
        this(memberTypes, 0, readonly, isCyclic, unresolvedTypes);
    }

    public BUnionType(String typeName, Module pkg, boolean readonly, Class<? extends Object> valueClass) {
        super(typeName, pkg, valueClass);
        this.readonly = readonly;
    }

    public BUnionType(Type[] memberTypes, int typeFlags, boolean readonly, boolean isCyclic) {
        this(Arrays.asList(memberTypes), typeFlags, readonly, isCyclic);
    }

    public BUnionType(Type[] memberTypes, int typeFlags) {
        this(memberTypes, typeFlags, false, false);
    }

    /**
     * Constructor used when defining union type defs where cyclic reference is possible.
     *
     * @param typeFlags flags associated with the type
     * @param readonly boolean represents if the type is readonly
     * @param isCyclic boolean represents if the type is cyclic
     */
    public BUnionType(int typeFlags, boolean readonly, boolean isCyclic) {
        super(null, null, Object.class);
        this.typeFlags = typeFlags;
        this.readonly = readonly;
        this.memberTypes = new ArrayList<>(2);
        this.isCyclic = isCyclic;
    }

    /**
     * Constructor used when defining union type defs where cyclic reference is possible.
     *
     * @param unionType flags associated with the type
     */
    public BUnionType(BUnionType unionType) {
        super(unionType.typeName, unionType.pkg, unionType.valueClass);
        this.typeFlags = unionType.typeFlags;
        this.readonly = unionType.readonly;
        this.memberTypes = new ArrayList<>(unionType.memberTypes.size());
        this.mergeUnionType(unionType);
    }

    /**
     * Constructor used when defining union type defs where cyclic reference is possible.
     *
     * @param unionType flags associated with the type
     * @param typeName typename associated with the type
     */
    public BUnionType(BUnionType unionType, String typeName) {
        super(typeName, unionType.pkg, unionType.valueClass);
        this.readonly = unionType.readonly;
        this.typeFlags = unionType.typeFlags;
        this.memberTypes = new ArrayList<>(unionType.memberTypes.size());
        this.mergeUnionType(unionType);
    }

    /**
     * Constructor used when defining union type defs where cyclic reference is possible.
     *
     * @param name typename
     * @param typeFlags flags associated with the type
     * @param readonly boolean represents if the type is readonly
     * @param isCyclic boolean represents if the type is cyclic
     */
    public BUnionType(String name, int typeFlags, boolean readonly, boolean isCyclic) {
        super(name, null, Object.class);
        this.typeFlags = typeFlags;
        this.readonly = readonly;
        this.memberTypes = null;
        this.isCyclic = isCyclic;
    }

    public BUnionType(Type[] memberTypes, String name, int typeFlags, boolean readonly, boolean isCyclic) {
        this(Arrays.asList(memberTypes), name, typeFlags, readonly, isCyclic);
    }

    public void setMemberTypes(Type[] members) {
        if (members == null) {
            return;
        }
        this.memberTypes = readonly ? getReadOnlyTypes(members) : Arrays.asList(members);
        setFlagsBasedOnMembers();
    }

    public void setMemberTypes(List<Type> members) {
        if (members == null) {
            return;
        }
        if (members.isEmpty()) {
            this.memberTypes = members;
            return;
        }
        this.resolvingReadonly = true;
        this.memberTypes = readonly ? getReadOnlyTypes(members, new HashSet<>(members.size())) : members;
        this.resolvingReadonly = false;
        setFlagsBasedOnMembers();
    }

    public void setMemberTypes(List<Type> members, Set<Type> unresolvedTypes) {
        if (members == null) {
            return;
        }
        if (members.isEmpty()) {
            this.memberTypes = members;
            return;
        }
        this.resolvingReadonly = true;
        this.memberTypes = readonly ? getReadOnlyTypes(members, unresolvedTypes) : members;
        this.resolvingReadonly = false;
        setFlagsBasedOnMembers();
    }

    public void setCyclic(boolean isCyclic) {
        this.isCyclic = isCyclic;
    }

    public boolean isNilable() {
        if (memberTypes == null || memberTypes.isEmpty()) {
            return true;
        }

        if (this.resolving) {
            return false;
        }

        // TODO: use the flag
        if (nullable == null) {
            nullable = checkNillable(memberTypes);
        }
        return nullable;
    }

    private boolean checkNillable(List<Type> memberTypes) {
        this.resolving = true;
        for (Type member : memberTypes) {
            if (member.isNilable()) {
                this.resolving = false;
                return true;
            }
        }
        this.resolving = false;
        return false;
    }

    public void addMember(Type type) {
        this.memberTypes.add(type);
    }

    private void setFlagsBasedOnMembers() {
        if (this.resolving) {
            return;
        }
        this.resolving = true;
        boolean nilable = false, isAnydata = true, isPureType = true;
        for (Type memberType : memberTypes) {
            nilable |= memberType.isNilable();
            isAnydata &= memberType.isAnydata();
            isPureType &= memberType.isPureType();
        }
        this.resolving = false;
        if (nilable) {
            this.typeFlags = TypeFlags.addToMask(this.typeFlags, TypeFlags.NILABLE);
        }
        if (isAnydata) {
            this.typeFlags = TypeFlags.addToMask(this.typeFlags, TypeFlags.ANYDATA);
        }
        if (isPureType) {
            this.typeFlags = TypeFlags.addToMask(this.typeFlags, TypeFlags.PURETYPE);
        }
    }

    public List<Type> getMemberTypes() {
        return memberTypes;
    }

    public boolean isNullable() {
        return isNilable();
    }

    private List<Type> getReadOnlyTypes(List<Type> memberTypes, Set<Type> unresolvedTypes) {
        List<Type> readOnlyTypes = new ArrayList<>(memberTypes.size());
        for (Type type : memberTypes) {
            readOnlyTypes.add(ReadOnlyUtils.getReadOnlyType(type, unresolvedTypes));
        }
        return readOnlyTypes;
    }

    private List<Type> getReadOnlyTypes(Type[] memberTypes) {
        List<Type> readOnlyTypes = new ArrayList<>(memberTypes.length);
        for (Type type : memberTypes) {
            readOnlyTypes.add(ReadOnlyUtils.getReadOnlyType(type));
        }
        return readOnlyTypes;
    }

    @Override
    public <V extends Object> V getZeroValue() {
        if (isNilable() || memberTypes.stream().anyMatch(Type::isNilable)) {
            return null;
        }

        return memberTypes.get(0).getZeroValue();
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        if (isNilable() || memberTypes.stream().anyMatch(Type::isNilable)) {
            return null;
        }

        return memberTypes.get(0).getEmptyValue();
    }

    @Override
    public int getTag() {
        return TypeTags.UNION_TAG;
    }

    @Override
    public String toString() {
        if (resolving) {
            // show name when visited repeatedly
            if (this.typeName != null) {
                if (pCloneable.matcher(this.typeName).matches()) {
                    return CLONEABLE;
                }
                return this.typeName;
            } else {
                return "...";
            }
        }
        resolving = true;
        if (cachedToString == null) {
            StringBuilder sb = new StringBuilder();
            int size = memberTypes != null ? memberTypes.size() : 0;
            int i = 0;
            while (i < size) {
                sb.append(memberTypes.get(i).toString());
                if (++i < size) {
                    sb.append(PIPE);
                }
            }
            cachedToString = sb.toString();

        }
        resolving = false;
        return cachedToString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof BUnionType)) {
            return false;
        }

        BUnionType that = (BUnionType) o;
        if (this.memberTypes.size() != that.memberTypes.size()) {
            return false;
        }

        // Note: Ordered comparison is used here as an optimization to speed up the union equals method.
        // union types that are like (A|B is B|A) will be fall through to assignable check in jvm/TypeChecker
        // Refer: https://github.com/ballerina-platform/ballerina-lang/pull/19197#discussion_r328972983
        for (int i = 0; i < memberTypes.size(); i++) {
            if (!this.memberTypes.get(i).equals(that.memberTypes.get(i))) {
                return false;
            }
        }
        return this.readonly == that.readonly;
    }

    @Override
    public String getName() {
        return toString();
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), memberTypes);
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
        return this.readonly;
    }

    @Override
    public Type getImmutableType() {
        return this.immutableType;
    }

    @Override
    public void setImmutableType(IntersectionType immutableType) {
        this.immutableType = immutableType;
    }

    @Override
    public boolean isCyclic() {
        return isCyclic;
    }

    public void mergeUnionType(BUnionType unionType) {
        if (!unionType.isCyclic) {
            for (Type member : unionType.getMemberTypes()) {
                this.addMember(member);
            }
            setFlagsBasedOnMembers();
            return;
        }
        this.isCyclic = true;
        for (Type member : unionType.getMemberTypes()) {
            if (member instanceof BArrayType) {
                BArrayType arrayType = (BArrayType) member;
                if (arrayType.getElementType() == unionType) {
                    BArrayType newArrayType = new BArrayType(this);
                    this.addMember(newArrayType);
                    continue;
                }
            } else if (member instanceof BMapType) {
                BMapType mapType = (BMapType) member;
                if (mapType.getConstrainedType() == unionType) {
                    BMapType newMapType = new BMapType(this);
                    this.addMember(newMapType);
                    continue;
                }
            } else if (member instanceof BTableType) {
                BTableType tableType = (BTableType) member;
                if (tableType.getConstrainedType() == unionType) {
                    BTableType newTableType = new BTableType(this, tableType.isReadOnly());
                    this.addMember(newTableType);
                    continue;
                } else if (tableType.getConstrainedType() instanceof BMapType) {
                    BMapType mapType = (BMapType) tableType.getConstrainedType();
                    if (mapType.getConstrainedType() == unionType) {
                        BMapType newMapType = new BMapType(this);
                        BTableType newTableType = new BTableType(newMapType,
                                tableType.getConstrainedType().isReadOnly());
                        this.addMember(newTableType);
                        continue;
                    }
                }
            }
            this.addMember(member);
        }
        setFlagsBasedOnMembers();
    }
}
