/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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
package io.ballerina.types;

/**
 * CellAtomicType node.
 *
 * @param ty  Type "wrapped" by this cell
 * @param mut Mutability of the cell
 * @since 2201.12.0
 */
public record CellAtomicType(SemType ty, CellMutability mut) implements AtomicType {

    public CellAtomicType {
        assert ty != null;
    }

    public static CellAtomicType from(SemType ty, CellMutability mut) {
        assert ty != null;
        // TODO: return final fields where applicable
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
}
