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
package org.ballerinalang.jvm.types;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * {@code BIntersectionType} represents an intersection type in Ballerina.
 *
 * @since 2.0.0
 */
public class BIntersectionType extends BType {

    private static final String PADDED_AMPERSAND = " & ";
    private static final String OPENING_PARENTHESIS = "(";
    private static final String CLOSING_PARENTHESIS = ")";

    private List<BType> constituentTypes;
    private BType effectiveType;

    private int typeFlags;
    private final boolean readonly;
    private BIntersectionType immutableType;

    public BIntersectionType(BType[] constituentTypes, BType effectiveType, int typeFlags, boolean readonly) {
        super(null, null, Object.class);
        this.constituentTypes = Arrays.asList(constituentTypes);
        this.effectiveType = effectiveType;
        this.typeFlags = typeFlags;
        this.readonly = readonly;

        if (readonly) {
            this.immutableType = this;
        }
    }

    public List<BType> getConstituentTypes() {
        return constituentTypes;
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
        StringJoiner joiner = new StringJoiner(PADDED_AMPERSAND, OPENING_PARENTHESIS, CLOSING_PARENTHESIS);

        for (BType constituentType : this.constituentTypes) {
            if (constituentType.getTag() == TypeTags.NULL_TAG) {
                joiner.add("()");
                continue;
            }

            joiner.add(constituentType.toString());
        }

        return joiner.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof BIntersectionType)) {
            return false;
        }

        BIntersectionType that = (BIntersectionType) o;
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
    public String getName() {
        return toString();
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), constituentTypes);
    }

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
    public BType getImmutableType() {
        return this.immutableType;
    }

    @Override
    public void setImmutableType(BIntersectionType immutableType) {
        this.immutableType = immutableType;
    }

    public BType getEffectiveType() {
        return this.effectiveType;
    }
}
