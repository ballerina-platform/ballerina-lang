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

package io.ballerina.types.typeops;

import io.ballerina.types.BasicTypeOps;
import io.ballerina.types.Bdd;
import io.ballerina.types.Context;
import io.ballerina.types.SubtypeData;

import static io.ballerina.types.Common.bddEveryPositive;
import static io.ballerina.types.Common.bddSubtypeDiff;
import static io.ballerina.types.Common.memoSubtypeIsEmpty;
import static io.ballerina.types.PredefinedType.MAPPING_SUBTYPE_OBJECT;

public final class ObjectOps extends CommonOps implements BasicTypeOps {

    @Override
    public SubtypeData complement(SubtypeData t) {
        return objectSubTypeComplement(t);
    }

    @Override
    public boolean isEmpty(Context cx, SubtypeData t) {
        return objectSubTypeIsEmpty(cx, t);
    }

    private static boolean objectSubTypeIsEmpty(Context cx, SubtypeData t) {
        return memoSubtypeIsEmpty(cx, cx.mappingMemo, ObjectOps::objectBddIsEmpty, (Bdd) t);
    }

    private static boolean objectBddIsEmpty(Context cx, Bdd b) {
        return bddEveryPositive(cx, b, null, null, MappingOps::mappingFormulaIsEmpty);
    }

    private SubtypeData objectSubTypeComplement(SubtypeData t) {
        return bddSubtypeDiff(MAPPING_SUBTYPE_OBJECT, t);
    }
}
