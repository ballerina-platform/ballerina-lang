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
package io.ballerina.types.subtypedata;

import io.ballerina.types.Bdd;
import io.ballerina.types.CellAtomicType;
import io.ballerina.types.Env;
import io.ballerina.types.SemType;
import io.ballerina.types.definition.ListDefinition;

import static io.ballerina.types.BasicTypeCode.BT_LIST;
import static io.ballerina.types.BasicTypeCode.BT_TABLE;
import static io.ballerina.types.CellAtomicType.CellMutability.CELL_MUT_LIMITED;
import static io.ballerina.types.Core.createBasicSemType;
import static io.ballerina.types.Core.subtypeData;
import static io.ballerina.types.PredefinedType.LIST_SUBTYPE_MAPPING;
import static io.ballerina.types.PredefinedType.TABLE;

/**
 * TableSubtype.
 *
 * @since 2201.8.0
 */
public final class TableSubtype {

    private TableSubtype() {
    }

    public static SemType tableContaining(Env env, SemType mappingType, CellAtomicType.CellMutability mut) {
        ListDefinition listDef = new ListDefinition();
        SemType listType = listDef.defineListTypeWrapped(env, mappingType, mut);
        Bdd bdd = (Bdd) subtypeData(listType, BT_LIST);
        if (bdd.equals(LIST_SUBTYPE_MAPPING)) {
            return TABLE;
        }
        return createBasicSemType(BT_TABLE, bdd);
    }

    public static SemType tableContaining(Env env, SemType mappingType) {
        return tableContaining(env, mappingType, CELL_MUT_LIMITED);
    }
}
