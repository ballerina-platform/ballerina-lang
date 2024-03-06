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
package io.ballerina.types.subtypedata;

import io.ballerina.types.CellAtomicType;
import io.ballerina.types.CellSemType;
import io.ballerina.types.ComplexSemType;
import io.ballerina.types.Env;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.TypeAtom;
import io.ballerina.types.UniformTypeCode;

import static io.ballerina.types.typeops.BddCommonOps.bddAtom;

/**
 * CellSubType.
 *
 * @since 2201.10.0
 */
public class CellSubtype {

    public static CellSemType cellContaining(Env env, SemType ty, CellAtomicType.CellMutability mut) {
        CellAtomicType atomicCell = CellAtomicType.from(ty, mut);
        TypeAtom atom = env.cellAtom(atomicCell);
        BddNode bdd = bddAtom(atom);
        ComplexSemType complexSemType = (ComplexSemType) PredefinedType.uniformSubtype(UniformTypeCode.BT_CELL, bdd);
        return new CellSemType(complexSemType.subtypeDataList);
    }
}
