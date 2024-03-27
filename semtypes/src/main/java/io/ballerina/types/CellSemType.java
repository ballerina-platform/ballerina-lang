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

import static io.ballerina.types.TypeAtom.ATOM_CELL_INNER;
import static io.ballerina.types.TypeAtom.ATOM_CELL_INNER_MAPPING;
import static io.ballerina.types.TypeAtom.ATOM_CELL_VAL;
import static io.ballerina.types.typeops.BddCommonOps.bddAtom;

/**
 * This is to represent a SemType belonging to cell basic type.
 *
 * @since 2201.10.0
 */
public class CellSemType extends ComplexSemType {

    public static final CellSemType CELL_SEMTYPE_VAL = from(new ProperSubtypeData[]{bddAtom(ATOM_CELL_VAL)});
    public static final CellSemType CELL_SEMTYPE_INNER = from(new ProperSubtypeData[]{bddAtom(ATOM_CELL_INNER)});
    public static final CellSemType CELL_SEMTYPE_INNER_MAPPING =
            from(new ProperSubtypeData[]{bddAtom(ATOM_CELL_INNER_MAPPING)});

    private CellSemType(ProperSubtypeData[] subtypeDataList) {
        super(BasicTypeBitSet.from(0), PredefinedType.CELL, subtypeDataList);
        assert subtypeDataList.length == 1;
    }

    public static CellSemType from(ProperSubtypeData[] subtypeDataList) {
        return new CellSemType(subtypeDataList);
    }
}
