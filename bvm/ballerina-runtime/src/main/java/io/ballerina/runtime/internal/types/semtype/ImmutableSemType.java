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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_UNDEF;

/**
 * Runtime representation of SemType.
 *
 * @since 2201.10.0
 */
public abstract sealed class ImmutableSemType implements SemType permits BSemTypeWrapper, PureSemType {

    private static final SubType[] EMPTY_SUBTYPE_DATA = new SubType[0];
    private static final int CACHEABLE_TYPE_MASK = (~BasicTypeCode.BASIC_TYPE_MASK) & ((1 << (CODE_UNDEF + 1)) - 1);

    private final int all;
    private final int some;
    private final SubType[] subTypeData;

    private Integer hashCode;

    private final TypeCheckResultCache resultCache;

    ImmutableSemType(int all, int some, SubType[] subTypeData) {
        this.all = all;
        this.some = some;
        this.subTypeData = subTypeData;
        if ((some & CACHEABLE_TYPE_MASK) != 0) {
            this.resultCache = new TypeCheckResultCache();
        } else {
            this.resultCache = TypeCheckResultCache.EMPTY;
        }
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
    public final int all() {
        return all;
    }

    @Override
    public final int some() {
        return some;
    }

    @Override
    public final SubType[] subTypeData() {
        return subTypeData;
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
                Objects.deepEquals(subTypeData, semType.subTypeData);
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
        return Objects.hash(all(), some(), Arrays.hashCode(subTypeData));
    }

    private boolean shouldCache() {
        return (some() & CACHEABLE_TYPE_MASK) != 0;
    }

    @Override
    public CachedResult cachedSubTypeRelation(SemType other) {
        if (!shouldCache()) {
            return CachedResult.NOT_FOUND;
        }
        return resultCache.getCachedResult(other);
    }

    @Override
    public void cacheSubTypeRelation(SemType other, boolean result) {
        if (shouldCache()) {
            resultCache.cacheResult(other, result);
            assert isValidCacheState(other, result) : "Invalid cache state";
        }
    }

    private boolean isValidCacheState(SemType other, boolean result) {
        CachedResult cachedResult = cachedSubTypeRelation(other);
        return cachedResult == CachedResult.NOT_FOUND ||
                cachedResult == (result ? CachedResult.TRUE : CachedResult.FALSE);
    }

    @Override
    public final SubType subTypeByCode(int code) {
        if ((some() & (1 << code)) == 0) {
            return null;
        }
        int someMask = (1 << code) - 1;
        int some = some() & someMask;
        return subTypeData()[Integer.bitCount(some)];
    }

    private static sealed class TypeCheckResultCache {

        private static final TypeCheckResultCache EMPTY = new EmptyTypeCheckResultCache();
        // make this an int
        private final Map<TypeCheckCacheKey, Boolean> cache = new WeakHashMap<>();

        public void cacheResult(SemType semType, boolean result) {
            cache.put(TypeCheckCacheKey.from(semType), result);
        }

        public CachedResult getCachedResult(SemType semType) {
            Boolean cachedData = cache.get(TypeCheckCacheKey.from(semType));
            if (cachedData == null) {
                return CachedResult.NOT_FOUND;
            }
            return cachedData ? CachedResult.TRUE : CachedResult.FALSE;
        }
    }

    private static final class EmptyTypeCheckResultCache extends TypeCheckResultCache {

        @Override
        public void cacheResult(SemType semType, boolean result) {
            throw new UnsupportedOperationException("Empty cache");
        }

        @Override
        public CachedResult getCachedResult(SemType semType) {
            throw new UnsupportedOperationException("Empty cache");
        }
    }

    private record TypeCheckCacheKey(Reference<SemType> semtype) {

        static TypeCheckCacheKey from(SemType semType) {
            return new TypeCheckCacheKey(new WeakReference<>(semType));
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TypeCheckCacheKey other)) {
                return false;
            }
            SemType thisSemType = semtype.get();
            SemType otherSemType = other.semtype.get();
            if (thisSemType == null || otherSemType == null) {
                return false;
            }
            return thisSemType == otherSemType;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(semtype.get());
        }
    }
}
