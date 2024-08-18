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

import java.util.HashMap;
import java.util.Map;

/**
 * CellAtomicType node.
 *
 * @param ty  Type "wrapped" by this cell
 * @param mut Mutability of the cell
 * @since 2201.10.0
 */
public record CellAtomicType(SemType ty, CellMutability mut) implements AtomicType {

    public CellAtomicType {
        assert ty != null;
    }

    public static CellAtomicType from(SemType ty, CellMutability mut) {
        return CellAtomCache.get(ty, mut);
    }

    public static CellAtomicType intersectCellAtomicType(CellAtomicType c1, CellAtomicType c2) {
        SemType ty = Core.intersect(c1.ty(), c2.ty());
        CellMutability mut = min(c1.mut(), c2.mut());
        return CellAtomicType.from(ty, mut);
    }

    private static CellMutability min(CellMutability m1,
                                      CellMutability m2) {
        return m1.compareTo(m2) <= 0 ? m1 : m2;
    }

    public static CellAtomicType cellAtomType(Atom atom) {
        return (CellAtomicType) ((TypeAtom) atom).atomicType();
    }

    public enum CellMutability {
        CELL_MUT_NONE,
        CELL_MUT_LIMITED,
        CELL_MUT_UNLIMITED
    }

    private static final class CellAtomCache {

        private final static Map<Integer, CellAtomicType> NONE_CACHE = new HashMap<>();
        private final static Map<Integer, CellAtomicType> LIMITED_CACHE = new HashMap<>();
        private final static Map<Integer, CellAtomicType> UNLIMITED_CACHE = new HashMap<>();

        private static CellAtomicType get(SemType semType, CellMutability mut) {
            if (semType.some() != 0) {
                return new CellAtomicType(semType, mut);
            }
            int key = semType.all();
            return switch (mut) {
                case CELL_MUT_NONE -> NONE_CACHE.computeIfAbsent(key, (ignored) -> new CellAtomicType(semType, mut));
                case CELL_MUT_LIMITED ->
                        LIMITED_CACHE.computeIfAbsent(key, (ignored) -> new CellAtomicType(semType, mut));
                case CELL_MUT_UNLIMITED ->
                        UNLIMITED_CACHE.computeIfAbsent(key, (ignored) -> new CellAtomicType(semType, mut));
            };
        }
    }
}
