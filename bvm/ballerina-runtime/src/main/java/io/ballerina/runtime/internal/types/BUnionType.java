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
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.flags.TypeFlags;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.SelectivelyImmutableReferenceType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.values.ReadOnlyUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Pattern;

/**
 * {@code BUnionType} represents a union type in Ballerina.
 *
 * @since 0.995.0
 */
public class BUnionType extends BType implements UnionType, SelectivelyImmutableReferenceType {

    public boolean isCyclic = false;
    public static final String  PIPE = "|";
    private List<Type> memberTypes;
    private List<Type> originalMemberTypes;
    private Boolean nullable;
    private long flags = 0;
    private int typeFlags;
    private boolean readonly;
    protected IntersectionType immutableType;
    private IntersectionType intersectionType = null;
    private String cachedToString;
    private boolean resolving;
    public boolean resolvingReadonly;

    private static final String INT_CLONEABLE = "__Cloneable";
    private static final String CLONEABLE = "Cloneable";
    private static final Pattern pCloneable = Pattern.compile(INT_CLONEABLE);

    public BUnionType(List<Type> memberTypes, int typeFlags, boolean readonly,  boolean isCyclic) {
        this(memberTypes, memberTypes, typeFlags, isCyclic, (readonly ? SymbolFlags.READONLY : 0));
    }

    private BUnionType(List<Type> memberTypes, List<Type> originalMemberTypes, int typeFlags, boolean isCyclic,
                       long flags) {
        super(null, null, Object.class);
        this.typeFlags = typeFlags;
        this.readonly = isReadOnlyFlagOn(flags);
        this.flags = flags;
        setMemberTypes(memberTypes, originalMemberTypes);
        this.isCyclic = isCyclic;
    }

    public BUnionType(int typeFlags, boolean isCyclic, long flags) {
        super(null, null, Object.class);
        this.typeFlags = typeFlags;
        this.readonly = isReadOnlyFlagOn(flags);
        this.memberTypes = new ArrayList<>(0);
        this.isCyclic = isCyclic;
        this.flags = flags;
    }

    public BUnionType(List<Type> memberTypes) {
        this(memberTypes, false);
    }

    public BUnionType(String typeName, Module pkg, List<Type> memberTypes, boolean readonly) {
        super(typeName, pkg, Object.class);
        this.readonly = readonly;
        setMemberTypes(memberTypes);
    }

    public BUnionType(List<Type> memberTypes, boolean readonly) {
        this(memberTypes, 0, readonly, false);
    }

    public BUnionType(List<Type> memberTypes, boolean readonly, boolean isCyclic) {
        super(null, null, Object.class);
        this.typeFlags = 0;
        this.readonly = readonly;
        setMemberTypes(memberTypes);
        this.isCyclic = isCyclic;
    }

    public BUnionType(Type[] memberTypes, Type[] originalMemberTypes, int typeFlags) {
        this(memberTypes, originalMemberTypes, typeFlags, false, 0);
    }

    public BUnionType(Type[] memberTypes, Type[] originalMemberTypes, int typeFlags, boolean isCyclic, long flags) {
        this(Arrays.asList(memberTypes), Arrays.asList(originalMemberTypes), typeFlags, isCyclic, flags);
    }

    public BUnionType(List<Type> memberTypes, String name, Module pkg, int typeFlags, boolean isCyclic, long flags) {
        super(name, pkg, Object.class);
        this.typeFlags = typeFlags;
        this.readonly = isReadOnlyFlagOn(flags);
        this.memberTypes = memberTypes;
        this.isCyclic = isCyclic;
        this.flags = flags;
    }

    public BUnionType(String name, Module pkg, int typeFlags, boolean isCyclic, long flags) {
        this(new ArrayList<>(0), name, pkg, typeFlags, isCyclic, flags);
    }

    protected BUnionType(String typeName, Module pkg, boolean readonly, Class<? extends Object> valueClass) {
        super(typeName, pkg, valueClass);
        this.readonly = readonly;
    }

    /**
     * Constructor used when defining union type defs where cyclic reference is possible.
     *
     * @param unionType flags associated with the type
     * @param typeName typename associated with the type
     */
    protected BUnionType(BUnionType unionType, String typeName, boolean readonly) {
        super(typeName, unionType.pkg, unionType.valueClass);
        this.typeFlags = unionType.typeFlags;
        this.memberTypes = new ArrayList<>(unionType.memberTypes.size());
        this.originalMemberTypes = new ArrayList<>(unionType.memberTypes.size());
        this.mergeUnionType(unionType);
        this.readonly = readonly;
    }

    public BUnionType(Type[] memberTypes, Type[] originalMemberTypes, String name, Module pkg, int typeFlags,
                      boolean isCyclic, long flags) {
        super(name, pkg, Object.class);
        this.typeFlags = typeFlags;
        this.readonly = isReadOnlyFlagOn(flags);
        this.isCyclic = isCyclic;
        this.flags = flags;
        setMemberTypes(Arrays.asList(memberTypes), Arrays.asList(originalMemberTypes));
        this.typeName = name;
    }

    public void setMemberTypes(Type[] members) {
        if (members == null) {
            return;
        }
        this.memberTypes = readonly ? getReadOnlyTypes(members) : Arrays.asList(members);
        setFlagsBasedOnMembers();
    }

    public void setOriginalMemberTypes(Type[] originalMemberTypes) {
        this.originalMemberTypes = Arrays.asList(originalMemberTypes);
    }

    private void setOriginalMemberTypes(List<Type> originalMemberTypes) {
        this.originalMemberTypes = originalMemberTypes;
    }

    private void setMemberTypes(List<Type> members) {
        setMemberTypes(members, members);
    }

    private void setMemberTypes(List<Type> members, List<Type> originalMembers) {
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

        setOriginalMemberTypes(originalMembers);
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

    private void addMember(Type type) {
        this.memberTypes.add(type);
        setFlagsBasedOnMembers();
        this.originalMemberTypes.add(type);
    }

    public void addMembers(Type... types) {
        this.memberTypes.addAll(Arrays.asList(types));
        setFlagsBasedOnMembers();
        this.originalMemberTypes.addAll(Arrays.asList(types));
    }

    private void setFlagsBasedOnMembers() {
        if (this.resolving) {
            return;
        }
        this.resolving = true;
        this.resolvingReadonly = true;
        boolean nilable = false, isAnydata = true, isPureType = true, readonly = true;
        for (Type memberType : memberTypes) {
            nilable |= memberType.isNilable();
            isAnydata &= memberType.isAnydata();
            isPureType &= memberType.isPureType();
            readonly &= memberType.isReadOnly();
        }
        this.resolvingReadonly = false;
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
        this.readonly = readonly;
    }

    public List<Type> getMemberTypes() {
        return memberTypes;
    }

    @Override
    public List<Type> getOriginalMemberTypes() {
        return this.originalMemberTypes;
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

        Type firstMemberType = TypeUtils.getReferredType(memberTypes.get(0));
        if (firstMemberType.getTag() == TypeTags.FINITE_TYPE_TAG) {
            return TypeChecker.getType(
                    ((BFiniteType) firstMemberType).getValueSpace().iterator().next()).getZeroValue();
        } else {
            return firstMemberType.getZeroValue();
        }
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        if (isNilable() || memberTypes.stream().anyMatch(Type::isNilable)) {
            return null;
        }
        Type firstMemberType = TypeUtils.getReferredType(memberTypes.get(0));
        if (firstMemberType.getTag() == TypeTags.FINITE_TYPE_TAG) {
            return TypeChecker.getType(
                    ((BFiniteType) firstMemberType).getValueSpace().iterator().next()).getEmptyValue();
        } else {
            return firstMemberType.getEmptyValue();
        }
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
                    return getQualifiedName(CLONEABLE);
                }
                return getQualifiedName(this.typeName);
            } else {
                return "...";
            }
        }

        if (this.typeName != null && !this.typeName.isEmpty()) {
            return getQualifiedName(this.typeName);
        }

        resolving = true;
        computeStringRepresentation();
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

        if (this.isCyclic || that.isCyclic) {
            if (this.isCyclic != that.isCyclic) {
                return false;
            }

            return super.equals(that);
        }

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

    public long getFlags() {
        return this.flags;
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
    public boolean isCyclic() {
        return isCyclic;
    }

    public void mergeUnionType(BUnionType unionType) {
        if (!unionType.isCyclic) {
            this.addMembers(unionType.getMemberTypes().toArray(new Type[0]));
            return;
        }
        this.isCyclic = true;
        for (Type member : unionType.getMemberTypes()) {
            if (member instanceof BArrayType) {
                BArrayType arrayType = (BArrayType) member;
                if (TypeUtils.getReferredType(arrayType.getElementType()) == unionType) {
                    BArrayType newArrayType = new BArrayType(this, this.readonly);
                    this.addMember(newArrayType);
                    continue;
                }
            } else if (member instanceof BMapType) {
                BMapType mapType = (BMapType) member;
                if (mapType.getConstrainedType() == unionType) {
                    BMapType newMapType = new BMapType(this, this.readonly);
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

    public void computeStringRepresentation() {
        if (cachedToString != null) {
            return;
        }
        LinkedHashSet<Type> uniqueTypes = new LinkedHashSet<>();
        for (Type type : this.originalMemberTypes) {
            if (type.getTag() != TypeTags.UNION_TAG) {
                uniqueTypes.add(type);
                continue;
            }

            BUnionType unionMemType = (BUnionType) type;
            String typeName = unionMemType.typeName;
            if (typeName != null && !typeName.isEmpty()) {
                uniqueTypes.add(type);
                continue;
            }

            uniqueTypes.addAll(unionMemType.originalMemberTypes);
        }

        StringJoiner joiner = new StringJoiner(PIPE);

        boolean hasNilableMember = false;
        // This logic is added to prevent duplicate recursive calls to toString
        long numberOfNotNilTypes = 0L;
        for (Type type : uniqueTypes) {
            int tag = type.getTag();
            if (tag == TypeTags.NULL_TAG) {
                continue;
            }
            String memToString = type.toString();

            if (tag == TypeTags.UNION_TAG && memToString.startsWith("(") && memToString.endsWith(")")) {
                joiner.add(memToString.substring(1, memToString.length() - 1));
            } else {
                joiner.add(memToString);
            }
            numberOfNotNilTypes++;

            if (!hasNilableMember && type.isNilable()) {
                hasNilableMember = true;
            }
        }

        String typeStr = numberOfNotNilTypes > 1 ? "(" + joiner.toString() + ")" : joiner.toString();
        boolean hasNilType = uniqueTypes.size() > numberOfNotNilTypes;
        cachedToString = (hasNilType && !hasNilableMember) ? (typeStr + "?") : typeStr;
    }

    private String getQualifiedName(String name) {
        return (pkg == null || pkg.getName() == null || pkg.getName().equals(".")) ? name :
                pkg.toString() + ":" + name;
    }

    private boolean isReadOnlyFlagOn(long flags) {
        return SymbolFlags.isFlagOn(flags, SymbolFlags.READONLY);
    }

    @Override
    public Optional<IntersectionType> getIntersectionType() {
        return this.intersectionType ==  null ? Optional.empty() : Optional.of(this.intersectionType);
    }

    @Override
    public void setIntersectionType(IntersectionType intersectionType) {
        this.intersectionType = intersectionType;
    }
}
