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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
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
    private final SubType[] subTypeData;
    private static final SubType[] EMPTY_SUBTYPE_DATA = new SubType[0];
    private Integer hashCode;
    private static volatile AtomicInteger nextId = new AtomicInteger(0);
    private final Integer typeID = nextId.getAndIncrement();
    private final Map<Integer, Boolean> cachedSubTypeRelations;
    private static final int CACHEABLE_TYPE_MASK = (~BasicTypeCode.BASIC_TYPE_MASK) & ((1 << (CODE_UNDEF + 1)) - 1);
    private static final int MAX_CACHE_LIMIT = 1000;
    private final boolean useCache;

    protected SemType(int all, int some, SubType[] subTypeData) {
        this.all = all;
        this.some = some;
        this.subTypeData = subTypeData;
        if ((some & CACHEABLE_TYPE_MASK) != 0) {
            useCache = true;
            this.cachedSubTypeRelations = new ConcurrentHashMap<>();
        } else {
            useCache = false;
            this.cachedSubTypeRelations = null;
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

    Optional<Boolean> cachedSubTypeRelation(SemType other) {
        if (!useCache) {
            return Optional.empty();
        }
        if (other.typeID.equals(this.typeID)) {
            return Optional.of(true);
        }
        return Optional.ofNullable(cachedSubTypeRelations.get(other.typeID));
    }

    void cacheSubTypeRelation(SemType other, boolean result) {
        if (useCache) {
            if (cachedSubTypeRelations.size() > MAX_CACHE_LIMIT) {
                cachedSubTypeRelations.clear();
            }
            cachedSubTypeRelations.put(other.typeID, result);
        }
    }
}
