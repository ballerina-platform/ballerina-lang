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
package io.ballerina.runtime.types;

import io.ballerina.runtime.api.TypeFlags;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.TupleType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.values.TupleValueImpl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * {@code {@link BTupleType}} represents a tuple type in Ballerina.
 *
 * @since 0.995.0
 */
public class BTupleType extends BType implements TupleType {

    private List<Type> tupleTypes;
    private Type restType;
    private int typeFlags;
    private final boolean readonly;
    private IntersectionType immutableType;

    /**
     * Create a {@code BTupleType} which represents the tuple type.
     *
     * @param typeList of the tuple type
     */
    public BTupleType(List<Type> typeList) {
        super(null, null, Object.class);
        this.tupleTypes = typeList;
        this.restType = null;

        boolean isAllMembersPure = true;
        for (Type memberType : tupleTypes) {
            isAllMembersPure &= memberType.isPureType();
        }

        if (isAllMembersPure) {
            this.typeFlags = TypeFlags.addToMask(this.typeFlags, TypeFlags.ANYDATA, TypeFlags.PURETYPE);
        }
        this.readonly = false;
    }

    public BTupleType(List<Type> typeList, int typeFlags) {
        super(null, null, Object.class);
        this.tupleTypes = typeList;
        this.restType = null;
        this.typeFlags = typeFlags;
        this.readonly = false;
    }

    /**
     * Create a {@code BTupleType} which represents the tuple type.
     *
     * @param typeList of the tuple type
     * @param restType of the tuple type
     * @param typeFlags flags associated with the type
     * @param readonly whether immutable
     */
    public BTupleType(List<Type> typeList, Type restType, int typeFlags, boolean readonly) {
        super(null, null, Object.class);
        this.tupleTypes = typeList;
        this.restType = restType;
        this.typeFlags = typeFlags;
        this.readonly = readonly;
    }

    public List<Type> getTupleTypes() {
        return tupleTypes;
    }

    public Type getRestType() {
        return restType;
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

    @Override
    public String toString() {
        List<String> list = tupleTypes.stream().map(Type::toString).collect(Collectors.toList());
        if (!readonly) {
            return "[" + String.join(",", list) + "]";
        }

        return "[" + String.join(",", list) + "] & readonly";
    }

    @Override
    public String getName() {
        return toString();
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
        return this.readonly == that.readonly && Objects.equals(tupleTypes, that.tupleTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), tupleTypes);
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
