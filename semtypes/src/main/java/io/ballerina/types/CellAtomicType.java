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
package io.ballerina.types;

/**
 * CellAtomicType node.
 *
 * @param ty  Type "wrapped" by this cell
 * @param mut Mutability of the cell
 * @since 2201.10.0
 */
public record CellAtomicType(SemType ty, CellMutability mut) implements AtomicType {

    public static CellAtomicType from(SemType ty, CellMutability mut) {
        assert ty != null;
        if (Core.isNever(ty)) {
            return CellAtomicTypeCache.NEVER;
        } else if (ty.isSingleType()) {
            CellAtomicTypeCacheInner cache = switch (mut) {
                case CELL_MUT_NONE -> CellAtomicTypeCache.NONE_CACHE;
                case CELL_MUT_LIMITED -> CellAtomicTypeCache.LIMITED_CACHE;
                case CELL_MUT_UNLIMITED -> CellAtomicTypeCache.UNLIMITED_CACHE;
            };
            return cache.cached(ty);
        }
        return new CellAtomicType(ty, mut);
    }

    @Override
    public Atom.Kind atomKind() {
        return Atom.Kind.CELL_ATOM;
    }

    public enum CellMutability {
        CELL_MUT_NONE,
        CELL_MUT_LIMITED,
        CELL_MUT_UNLIMITED
    }

    private static final class CellAtomicTypeCache {

        private static final CellAtomicType NEVER =
                new CellAtomicType(PredefinedType.NEVER, CellMutability.CELL_MUT_NONE);
        private static final CellAtomicTypeCacheInner NONE_CACHE =
                new CellAtomicTypeCacheInner(CellMutability.CELL_MUT_NONE);
        private static final CellAtomicTypeCacheInner LIMITED_CACHE =
                new CellAtomicTypeCacheInner(CellMutability.CELL_MUT_LIMITED);
        private static final CellAtomicTypeCacheInner UNLIMITED_CACHE =
                new CellAtomicTypeCacheInner(CellMutability.CELL_MUT_UNLIMITED);
    }

    private static final class CellAtomicTypeCacheInner {

        private static final int SIZE = 0x14;
        private final CellAtomicType[] cache = new CellAtomicType[SIZE];

        private CellAtomicTypeCacheInner(CellMutability mut) {
            for (int i = 0; i < SIZE; i++) {
                cache[i] = new CellAtomicType(BasicTypeBitSet.from(1 << i), mut);
            }
        }

        private CellAtomicType cached(SemType ty) {
            int idx = Integer.numberOfTrailingZeros(ty.all());
            return cache[idx];
        }
    }
}
