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
package io.ballerina.runtime.types;

import io.ballerina.runtime.api.TypeFlags;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * {@code BUnionType} represents a union type in Ballerina.
 *
 * @since 0.995.0
 */
public class BUnionType extends BType implements UnionType {

    public static final char PIPE = '|';
    private List<Type> memberTypes;
    private Boolean nullable;
    private String cachedToString;
    private int typeFlags;
    private final boolean readonly;
    private IntersectionType immutableType;

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
        this(memberTypes, typeFlags, false);
    }

    public BUnionType(List<Type> memberTypes, int typeFlags, boolean readonly) {
        super(null, null, Object.class);
        this.memberTypes = memberTypes;
        this.typeFlags = typeFlags;
        this.readonly = readonly;
    }

    public BUnionType(List<Type> memberTypes) {
        this(memberTypes, false);
    }

    public BUnionType(List<Type> memberTypes, boolean readonly) {
        this(memberTypes, 0, readonly);
        boolean nilable = false, isAnydata = true, isPureType = true;
        for (Type memberType : memberTypes) {
            nilable |= memberType.isNilable();
            isAnydata &= memberType.isAnydata();
            isPureType &= memberType.isPureType();
        }

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

    public BUnionType(Type[] memberTypes, int typeFlags, boolean readonly) {
        this(Arrays.asList(memberTypes), typeFlags, readonly);
    }

    public BUnionType(Type[] memberTypes, int typeFlags) {
        this(memberTypes, typeFlags, false);
    }

    public List<Type> getMemberTypes() {
        return memberTypes;
    }

    public boolean isNullable() {
        return isNilable();
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
        if (cachedToString == null) {
            StringBuilder sb = new StringBuilder();
            int size = memberTypes.size();
            int i = 0;
            while (i < size) {
                sb.append(memberTypes.get(i).toString());
                if (++i < size) {
                    sb.append(PIPE);
                }
            }
            cachedToString = sb.toString();

        }
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

    public boolean isNilable() {
        // TODO: use the flag
        if (nullable == null) {
            nullable = checkNillable(memberTypes);
        }
        return nullable;
    }

    private boolean checkNillable(List<Type> memberTypes) {
        for (Type t : memberTypes) {
            if (t.isNilable()) {
                return true;
            }
        }
        return false;
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
}
