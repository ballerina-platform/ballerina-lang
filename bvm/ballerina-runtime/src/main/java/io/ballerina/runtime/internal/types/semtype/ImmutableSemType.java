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

import io.ballerina.runtime.api.types.semtype.BasicTypeCode;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.api.types.semtype.SubType;
import io.ballerina.runtime.internal.types.BSemTypeWrapper;

import java.util.Arrays;
import java.util.Objects;

import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_UNDEF;

/**
 * Runtime representation of SemType.
 *
 * @since 2201.10.0
 */
public abstract sealed class ImmutableSemType extends SemType permits BSemTypeWrapper, PureSemType {

    private static final SubType[] EMPTY_SUBTYPE_DATA = new SubType[0];
    private static final int CACHEABLE_TYPE_MASK = (~BasicTypeCode.BASIC_TYPE_MASK) & ((1 << (CODE_UNDEF + 1)) - 1);

    private Integer hashCode;

    ImmutableSemType(int all, int some, SubType[] subTypeData) {
        super(all, some, subTypeData);
    }

    ImmutableSemType(int all) {
        this(all, 0, EMPTY_SUBTYPE_DATA);
    }

    protected ImmutableSemType(SemType semType) {
        this(semType.all(), semType.some(), semType.subTypeData());
    }

    @Override
    public String toString() {
        return SemTypeHelper.stringRepr(this);
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

    private boolean shouldCache() {
        return (some() & CACHEABLE_TYPE_MASK) != 0;
    }

    @Override
    public CachedResult cachedSubTypeRelation(SemType other) {
        return CachedResult.NOT_FOUND;
    }

    @Override
    public void cacheSubTypeRelation(SemType other, boolean result) {
        return;
    }

    private boolean isValidCacheState(SemType other, boolean result) {
        CachedResult cachedResult = cachedSubTypeRelation(other);
        return cachedResult == CachedResult.NOT_FOUND ||
                cachedResult == (result ? CachedResult.TRUE : CachedResult.FALSE);
    }
}
