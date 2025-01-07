/*
 *  Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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
package io.ballerina.types.typeops;

import io.ballerina.types.BasicTypeOps;
import io.ballerina.types.Bdd;
import io.ballerina.types.Context;
import io.ballerina.types.SubtypeData;

import static io.ballerina.types.Common.bddEveryPositive;
import static io.ballerina.types.Common.bddPosMaybeEmpty;
import static io.ballerina.types.Common.bddSubtypeDiff;
import static io.ballerina.types.Common.memoSubtypeIsEmpty;
import static io.ballerina.types.PredefinedType.BDD_SUBTYPE_RO;
import static io.ballerina.types.typeops.BddCommonOps.bddIntersect;

/**
 * Basic type ops for error type.
 *
 * @since 2201.8.0
 */
public class ErrorOps extends CommonOps implements BasicTypeOps {

    private static SubtypeData errorSubtypeComplement(SubtypeData t) {
        return bddSubtypeDiff(BDD_SUBTYPE_RO, t);
    }

    private static boolean errorSubtypeIsEmpty(Context cx, SubtypeData t) {
        Bdd b = (Bdd) t;
        // The goal of this is to ensure that mappingFormulaIsEmpty call in errorBddIsEmpty beneath
        // does not get an empty posList, because it will interpret that
        // as `map<any|error>` rather than `readonly & map<readonly>`.
        b = bddPosMaybeEmpty(b) ? bddIntersect(b, BDD_SUBTYPE_RO) : b;
        return memoSubtypeIsEmpty(cx, cx.mappingMemo, ErrorOps::errorBddIsEmpty, b);
    }

    private static boolean errorBddIsEmpty(Context cx, Bdd b) {
        return bddEveryPositive(cx, b, null, null, MappingOps::mappingFormulaIsEmpty);
    }

    @Override
    public SubtypeData complement(SubtypeData d) {
        return errorSubtypeComplement(d);
    }

    @Override
    public boolean isEmpty(Context cx, SubtypeData t) {
        return errorSubtypeIsEmpty(cx, t);
    }
}
