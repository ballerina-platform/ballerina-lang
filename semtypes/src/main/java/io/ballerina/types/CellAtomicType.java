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
 * @since 2201.10.0
 */
public final class CellAtomicType implements AtomicType {
    public final SemType ty;
    public final CellMutability mut;

    public static final CellAtomicType CELL_ATOMIC_VAL = from(PredefinedType.VAL, CellMutability.CELL_MUT_LIMITED);
    public static final CellAtomicType CELL_ATOMIC_INNER = from(PredefinedType.INNER, CellMutability.CELL_MUT_LIMITED);
    public static final CellAtomicType CELL_ATOMIC_NEVER = from(PredefinedType.NEVER, CellMutability.CELL_MUT_LIMITED);
    public static final CellAtomicType CELL_ATOMIC_INNER_MAPPING = from(
            Core.union(PredefinedType.MAPPING, PredefinedType.UNDEF), CellMutability.CELL_MUT_LIMITED
    );

    private CellAtomicType(SemType ty, CellMutability mut) {
        this.ty = ty;
        this.mut = mut;
    }

    public static CellAtomicType from(SemType ty) {
        // TODO: is mutability correct?
        return from(ty, CellMutability.CELL_MUT_LIMITED);
    }

    public static CellAtomicType from(SemType ty, CellMutability mut) {
        // TODO: return final fields where applicable
        return new CellAtomicType(ty, mut);
    }

    public enum CellMutability {
        CELL_MUT_NONE,
        CELL_MUT_LIMITED,
        CELL_MUT_UNLIMITED;
    }
}
