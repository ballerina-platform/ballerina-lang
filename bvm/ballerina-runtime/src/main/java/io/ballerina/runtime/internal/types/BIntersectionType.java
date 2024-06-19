/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.internal.types;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.flags.TypeFlags;
import io.ballerina.runtime.api.types.IntersectableReferenceType;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * {@code BIntersectionType} represents an intersection type in Ballerina.
 *
 * @since 2.0.0
 */
public class BIntersectionType extends BType implements IntersectionType {

    private static final String PADDED_AMPERSAND = " & ";
    private static final String OPENING_PARENTHESIS = "(";
    private static final String CLOSING_PARENTHESIS = ")";

    public final List<Type> constituentTypes;
    private Type effectiveType;

    private final int typeFlags;
    private final boolean readonly;
    private IntersectionType immutableType;
    private IntersectionType intersectionType = null;

    private String cachedToString;
    private boolean resolving;

    public BIntersectionType(Module pkg, Type[] constituentTypes, Type effectiveType,
                             int typeFlags, boolean readonly) {
        this(TypeConstants.INTERSECTION_TNAME, pkg, constituentTypes, typeFlags, readonly);
        this.effectiveType = effectiveType;
    }

    public BIntersectionType(Module pkg, Type[] constituentTypes, IntersectableReferenceType effectiveType,
                             int typeFlags, boolean readonly) {
        this(TypeConstants.INTERSECTION_TNAME, pkg, constituentTypes, typeFlags, readonly);
        this.effectiveType = effectiveType;
        effectiveType.setIntersectionType(this);
    }

    public BIntersectionType(String typeName, Module pkg, Type[] constituentTypes, Type effectiveType,
                             int typeFlags, boolean readonly) {
        this(typeName, pkg, constituentTypes, typeFlags, readonly);
        this.effectiveType = effectiveType;
    }

    public BIntersectionType(String typeName, Module pkg, Type[] constituentTypes,
                             IntersectableReferenceType effectiveType, int typeFlags, boolean readonly) {
        this(typeName, pkg, constituentTypes, typeFlags, readonly);
        this.effectiveType = effectiveType;
        effectiveType.setIntersectionType(this);
    }

    private BIntersectionType(String typeName, Module pkg, Type[] constituentTypes, int typeFlags, boolean readonly) {
        super(typeName, pkg, Object.class);
        this.constituentTypes = Arrays.asList(constituentTypes);
        this.typeFlags = typeFlags;
        this.readonly = readonly;
        if (readonly) {
            this.immutableType = this;
        }
    }

    @Override
    public List<Type> getConstituentTypes() {
        return new ArrayList<>(constituentTypes);
    }


    @Override
    public <V extends Object> V getZeroValue() {
        return effectiveType.getZeroValue();
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        return effectiveType.getEmptyValue();
    }

    @Override
    public int getTag() {
        return TypeTags.INTERSECTION_TAG;
    }

    @Override
    public String toString() {
        if (resolving) {
            return "";
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
        StringJoiner joiner = new StringJoiner(PADDED_AMPERSAND, OPENING_PARENTHESIS, CLOSING_PARENTHESIS);

        for (Type constituentType : this.constituentTypes) {
            if (constituentType.getTag() == TypeTags.NULL_TAG) {
                joiner.add("()");
                continue;
            }

            joiner.add(constituentType.toString());
        }
        cachedToString = joiner.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof BIntersectionType that)) {
            return false;
        }

        if (this.constituentTypes.size() != that.constituentTypes.size()) {
            return false;
        }

        for (int i = 0; i < constituentTypes.size(); i++) {
            if (!this.constituentTypes.get(i).equals(that.constituentTypes.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), constituentTypes);
    }

    @Override
    public boolean isNilable() {
        return TypeFlags.isFlagOn(this.typeFlags, TypeFlags.NILABLE);
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
    public IntersectionType getImmutableType() {
        return this.immutableType;
    }

    @Override
    public void setImmutableType(IntersectionType immutableType) {
        this.immutableType = immutableType;
    }

    @Override
    public Type getEffectiveType() {
        return this.effectiveType;
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
