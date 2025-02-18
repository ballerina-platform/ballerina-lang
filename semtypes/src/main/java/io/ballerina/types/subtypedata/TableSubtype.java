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
package io.ballerina.types.subtypedata;

import io.ballerina.types.Bdd;
import io.ballerina.types.CellAtomicType;
import io.ballerina.types.Context;
import io.ballerina.types.Core;
import io.ballerina.types.Env;
import io.ballerina.types.FixedLengthArray;
import io.ballerina.types.ListAtomicType;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.SemTypes;
import io.ballerina.types.definition.ListDefinition;

import static io.ballerina.types.BasicTypeCode.BT_LIST;
import static io.ballerina.types.BasicTypeCode.BT_TABLE;
import static io.ballerina.types.CellAtomicType.CellMutability.CELL_MUT_LIMITED;
import static io.ballerina.types.Core.createBasicSemType;
import static io.ballerina.types.Core.subtypeData;

/**
 * TableSubtype.
 *
 * @since 2201.12.0
 */
public final class TableSubtype {

    private TableSubtype() {
    }

    public static SemType tableContainingKeyConstraint(Context cx, SemType tableConstraint, SemType keyConstraint) {
        SemType normalizedKc;
        ListAtomicType lat = Core.listAtomicType(cx, keyConstraint);
        if (lat != null && PredefinedType.CELL_ATOMIC_UNDEF.equals(Core.cellAtomicType(lat.rest()))) {
            FixedLengthArray members = lat.members();
            normalizedKc = switch (members.fixedLength()) {
                case 0 -> PredefinedType.VAL;
                case 1 -> Core.cellAtomicType(members.initial().get(0)).ty();
                default -> keyConstraint;
            };
        } else {
            normalizedKc = keyConstraint;
        }
        return tableContaining(cx.env, tableConstraint, normalizedKc, PredefinedType.VAL);
    }

    public static SemType tableContainingKeySpecifier(Context cx, SemType tableConstraint, String[] fieldNames) {
        SemType[] fieldNameSingletons = new SemType[fieldNames.length];
        SemType[] fieldTypes = new SemType[fieldNames.length];
        for (int i = 0; i < fieldNames.length; i++) {
            SemType key = SemTypes.stringConst(fieldNames[i]);
            fieldNameSingletons[i] = key;
            fieldTypes[i] = Core.mappingMemberTypeInnerVal(cx, tableConstraint, key);
        }

        SemType normalizedKs = new ListDefinition().tupleTypeWrapped(cx.env, fieldNameSingletons);

        SemType normalizedKc;
        if (fieldTypes.length > 1) {
            ListDefinition ld = new ListDefinition();
            normalizedKc = ld.tupleTypeWrapped(cx.env, fieldTypes);
        } else {
            normalizedKc = fieldTypes[0];
        }
        return tableContaining(cx.env, tableConstraint, normalizedKc, normalizedKs);
    }

    public static SemType tableContaining(Env env, SemType tableConstraint) {
        return tableContaining(env, tableConstraint, CELL_MUT_LIMITED);
    }

    public static SemType tableContaining(Env env, SemType tableConstraint, CellAtomicType.CellMutability mut) {
        SemType normalizedKc = PredefinedType.VAL; // TODO: Ideally this should be anydata
        SemType normalizedKs = PredefinedType.VAL; // TODO: Ideally this should be string[]
        return tableContaining(env, tableConstraint, normalizedKc, normalizedKs, mut);
    }

    private static SemType tableContaining(Env env, SemType tableConstraint,
                                          SemType normalizedKc, SemType normalizedKs,
                                          CellAtomicType.CellMutability mut) {

        assert SemTypes.isSubtypeSimple(tableConstraint, PredefinedType.MAPPING);
        ListDefinition typeParamArrDef = new ListDefinition();
        SemType typeParamArray = typeParamArrDef.defineListTypeWrapped(env, tableConstraint, mut);

        ListDefinition listDef = new ListDefinition();
        SemType tupleType = listDef.tupleTypeWrapped(env, typeParamArray, normalizedKc, normalizedKs);
        Bdd bdd = (Bdd) subtypeData(tupleType, BT_LIST);
        return createBasicSemType(BT_TABLE, bdd);
    }

    private static SemType tableContaining(Env env, SemType tableConstraint,
                                          SemType normalizedKc, SemType normalizedKs) {
        return tableContaining(env, tableConstraint, normalizedKc, normalizedKs, CELL_MUT_LIMITED);
    }
}
