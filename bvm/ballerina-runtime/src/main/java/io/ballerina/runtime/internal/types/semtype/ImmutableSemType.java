/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.CacheableTypeDescriptor;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.api.types.semtype.SubType;
import io.ballerina.runtime.api.types.semtype.TypeCheckCache;
import io.ballerina.runtime.api.types.semtype.TypeCheckCacheFactory;
import io.ballerina.runtime.internal.types.BSemTypeWrapper;
import io.ballerina.runtime.internal.types.TypeIdSupplier;

import java.util.Arrays;
import java.util.Objects;

/**
 * Runtime representation of an immutable semtype.
 *
 * @since 2201.11.0
 */
public abstract sealed class ImmutableSemType extends SemType implements CacheableTypeDescriptor
        permits BSemTypeWrapper {

    private static final SubType[] EMPTY_SUBTYPE_DATA = new SubType[0];

    private Integer hashCode;

    private final TypeCheckCache typeCheckCache;
    private final int typeId;

    ImmutableSemType(int all, int some, SubType[] subTypeData) {
        super(all, some, subTypeData);
        this.typeCheckCache = TypeCheckCacheFactory.create();
        this.typeId = TypeIdSupplier.getAnonId();
    }

    ImmutableSemType(int all) {
        this(all, 0, EMPTY_SUBTYPE_DATA);
    }

    protected ImmutableSemType(SemType semType) {
        this(semType.all(), semType.some(), semType.subTypeData());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImmutableSemType semType)) {
            return false;
        }
        return all() == semType.all() && some() == semType.some() &&
                Objects.deepEquals(this.subTypeData(), semType.subTypeData());
    }

    @Override
    public int hashCode() {
        Integer result = hashCode;
        if (result == null) {
            synchronized (this) {
                result = hashCode;
                if (result == null) {
                    hashCode = result = computeHashCode();
                }
            }
        }
        return result;
    }

    private int computeHashCode() {
        return Objects.hash(all(), some(), Arrays.hashCode(subTypeData()));
    }

    @Override
    protected void setAll(int all) {
        throw new UnsupportedOperationException("Immutable semtypes cannot be modified");
    }

    @Override
    protected void setSome(int some, SubType[] subTypeData) {
        throw new UnsupportedOperationException("Immutable semtypes cannot be modified");
    }

    @Override
    public Boolean cachedTypeCheckResult(Context cx, CacheableTypeDescriptor other) {
        return typeCheckCache.cachedTypeCheckResult(other);
    }

    public void cacheTypeCheckResult(CacheableTypeDescriptor other, boolean result) {

        typeCheckCache.cacheTypeCheckResult(other, result);
    }

    public int typeId() {
        return typeId;
    }
}
