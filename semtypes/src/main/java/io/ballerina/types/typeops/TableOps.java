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

import static io.ballerina.types.Common.bddPosMaybeEmpty;
import static io.ballerina.types.Common.bddSubtypeDiff;
import static io.ballerina.types.PredefinedType.LIST_SUBTYPE_MAPPING;
import static io.ballerina.types.typeops.BddCommonOps.bddIntersect;
import static io.ballerina.types.typeops.ListOps.listSubtypeIsEmpty;

/**
 * Basic type ops for table type.
 *
 * @since 2201.8.0
 */
public class TableOps extends CommonOps implements BasicTypeOps {

    private static SubtypeData tableSubtypeComplement(SubtypeData t) {
        return bddSubtypeDiff(LIST_SUBTYPE_MAPPING, t);
    }

    private static boolean tableSubtypeIsEmpty(Context cx, SubtypeData t) {
        Bdd b = (Bdd) t;
        // The goal of this is to ensure that listSubtypeIsEmpty call beneath does
        // not get an empty posList, because it will interpret that
        // as `(any|error)[]` rather than `(map<any|error>)[]`.
        b = bddPosMaybeEmpty(b) ? bddIntersect(b, LIST_SUBTYPE_MAPPING) : b;
        return listSubtypeIsEmpty(cx, b);
    }

    @Override
    public SubtypeData complement(SubtypeData d) {
        return tableSubtypeComplement(d);
    }

    @Override
    public boolean isEmpty(Context cx, SubtypeData d) {
        return tableSubtypeIsEmpty(cx, d);
    }
}
