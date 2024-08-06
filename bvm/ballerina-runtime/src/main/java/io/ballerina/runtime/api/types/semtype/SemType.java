/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.runtime.api.types.semtype;

import io.ballerina.runtime.internal.types.BSemTypeWrapper;
import io.ballerina.runtime.internal.types.semtype.PureSemType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_UNDEF;

/**
 * Runtime representation of SemType.
 *
 * @since 2201.10.0
 */
public abstract sealed class SemType implements BasicTypeBitSet permits BSemTypeWrapper, PureSemType {

    public final int all;
    public final int some;
    // TODO: this is messing up allignment either fill it or may be get rid of this
    private final boolean useCache;
    private final SubType[] subTypeData;
    private static final SubType[] EMPTY_SUBTYPE_DATA = new SubType[0];
    // TODO: use a lazy supplier instead
    private Integer hashCode;
    private static final int CACHEABLE_TYPE_MASK = (~BasicTypeCode.BASIC_TYPE_MASK) & ((1 << (CODE_UNDEF + 1)) - 1);
    private final TypeCheckResultCache resultCache;

    // TODO: this is for debug purposes get rid of this
    private static volatile AtomicInteger nextId = new AtomicInteger(1);
    private final Integer typeID = nextId.getAndIncrement();

    protected SemType(int all, int some, SubType[] subTypeData) {
        this.all = all;
        this.some = some;
        this.subTypeData = subTypeData;
        if ((some & CACHEABLE_TYPE_MASK) != 0) {
            useCache = true;
            this.resultCache = new TypeCheckResultCache();
        } else {
            useCache = false;
            this.resultCache = TypeCheckResultCache.EMPTY;
        }
    }

    protected SemType(int all) {
        this(all, 0, EMPTY_SUBTYPE_DATA);
    }

    protected SemType(SemType semType) {
        this(semType.all(), semType.some(), semType.subTypeData());
    }

    public static SemType from(int all, int some, SubType[] subTypeData) {
        return new PureSemType(all, some, subTypeData);
    }

    public static SemType from(int all) {
        return new PureSemType(all);
    }

    @Override
    public String toString() {
        return SemTypeHelper.stringRepr(this);
    }

    public final int all() {
        return all;
    }

    public final int some() {
        return some;
    }

    public final SubType[] subTypeData() {
        return subTypeData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SemType semType)) {
            return false;
        }
        return all == semType.all && some == semType.some && Objects.deepEquals(subTypeData, semType.subTypeData);
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
        return Objects.hash(all, some, Arrays.hashCode(subTypeData));
    }

    enum CachedResult {
        TRUE,
        FALSE,
        NOT_FOUND
    }

    CachedResult cachedSubTypeRelation(SemType other) {
        if (!useCache) {
            return CachedResult.NOT_FOUND;
        }
        int tid = other.typeID;
        if (tid == typeID) {
            return CachedResult.TRUE;
        }
        return resultCache.getCachedResult(tid);
    }

    void cacheSubTypeRelation(SemType other, boolean result) {
        if (useCache) {
            resultCache.cacheResult(other.typeID, result);

            CachedResult cachedResult = cachedSubTypeRelation(other);
            if (cachedResult != CachedResult.NOT_FOUND &&
                    cachedResult != (result ? CachedResult.TRUE : CachedResult.FALSE)) {
                throw new IllegalStateException("Inconsistent cache state");
            }
        }
    }

    private static sealed class TypeCheckResultCache {

        private static final TypeCheckResultCache EMPTY = new EmptyTypeCheckResultCache();
        private static final int CACHE_LIMIT = 100;
        // See if we can use an identity hashmap on semtypes instead of tid
        private Map<Long, Boolean> cache = new HashMap<>();

        protected void cacheResult(int tid, boolean result) {
            cache.put((long) tid, result);
        }

        protected CachedResult getCachedResult(int tid) {
            Boolean cachedData = cache.get((long) tid);
            if (cachedData == null) {
                return CachedResult.NOT_FOUND;
            }
            return cachedData ? CachedResult.TRUE : CachedResult.FALSE;
        }
    }

    private static final class EmptyTypeCheckResultCache extends TypeCheckResultCache {

        @Override
        public void cacheResult(int tid, boolean result) {
            throw new UnsupportedOperationException("Empty cache");
        }

        @Override
        public CachedResult getCachedResult(int tid) {
            throw new UnsupportedOperationException("Empty cache");
        }
    }

    public final SubType subTypeByCode(int code) {
        if ((some() & (1 << code)) == 0) {
            return null;
        }
        int someMask = (1 << code) - 1;
        int some = some() & someMask;
        return subTypeData()[Integer.bitCount(some)];
    }
}
