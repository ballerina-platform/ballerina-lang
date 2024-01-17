/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.identifier.Utils;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.flags.TypeFlags;
import io.ballerina.runtime.api.types.IntersectableReferenceType;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.Type;

import java.util.Objects;
import java.util.Optional;

/**
 * {@code TypeReferencedType} represents a type description which refers to another type.
 *
 * @since 2201.2.0
 */
public class BTypeReferenceType extends BAnnotatableType implements IntersectableReferenceType {

    private final int typeFlags;
    private final boolean readOnly;
    private Type referredType;
    private IntersectionType intersectionType;

    public BTypeReferenceType(String typeName, Module pkg, int typeFlags, boolean readOnly) {
        super(typeName, pkg, Object.class);
        this.typeFlags = typeFlags;
        this.readOnly = readOnly;
    }

    public void setReferredType(Type referredType) {
        this.referredType = referredType;
    }

    @Override
    public Type getReferredType() {
        return referredType;
    }

    public int getTypeFlags() {
        return typeFlags;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof BTypeReferenceType) {
            return this.referredType.equals(((BTypeReferenceType) obj).getReferredType());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), referredType);
    }

    @Override
    public String getAnnotationKey() {
        return Utils.decodeIdentifier(this.typeName);
    }

    @Override
    public <V> V getZeroValue() {
        return this.referredType.getZeroValue();
    }

    @Override
    public <V> V getEmptyValue() {
        return this.referredType.getEmptyValue();
    }

    @Override
    public int getTag() {
        return TypeTags.TYPE_REFERENCED_TYPE_TAG;
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

    @Override
    public boolean isReadOnly() {
        return this.readOnly;
    }

    @Override
    public Optional<IntersectionType> getIntersectionType() {
        return this.intersectionType == null ? Optional.empty() : Optional.of(this.intersectionType);
    }

    @Override
    public void setIntersectionType(IntersectionType intersectionType) {
        this.intersectionType = intersectionType;
    }
}
