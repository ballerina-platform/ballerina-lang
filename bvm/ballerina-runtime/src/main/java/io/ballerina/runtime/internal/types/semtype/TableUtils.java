/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
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
 *
 */

package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.BasicTypeCode;
import io.ballerina.runtime.api.types.semtype.Bdd;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.CellAtomicType;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.Core;
import io.ballerina.runtime.api.types.semtype.Env;
import io.ballerina.runtime.api.types.semtype.ListAtomicType;
import io.ballerina.runtime.api.types.semtype.SemType;

import java.util.Optional;

import static io.ballerina.runtime.api.types.semtype.CellAtomicType.CellMutability.CELL_MUT_LIMITED;
import static io.ballerina.runtime.api.types.semtype.CellAtomicType.CellMutability.CELL_MUT_NONE;

public final class TableUtils {

    private static final SemType[] EMPTY_SEMTYPE_ARR = new SemType[0];

    private TableUtils() {
    }

    public static SemType tableContainingKeySpecifier(Context cx, SemType tableConstraint, String[] fieldNames) {
        SemType[] fieldNameSingletons = new SemType[fieldNames.length];
        SemType[] fieldTypes = new SemType[fieldNames.length];
        for (int i = 0; i < fieldNames.length; i++) {
            SemType key = Builder.stringConst(fieldNames[i]);
            fieldNameSingletons[i] = key;
            fieldTypes[i] = Core.mappingMemberTypeInnerVal(cx, tableConstraint, key);
        }

        SemType normalizedKs =
                new ListDefinition().defineListTypeWrapped(cx.env, fieldNameSingletons, fieldNameSingletons.length,
                        Builder.neverType(), CELL_MUT_NONE);

        SemType normalizedKc = fieldNames.length > 1 ? new ListDefinition().defineListTypeWrapped(cx.env, fieldTypes,
                fieldTypes.length, Builder.neverType(), CELL_MUT_NONE) : fieldTypes[0];

        return tableContaining(cx.env, tableConstraint, normalizedKc, normalizedKs, CELL_MUT_LIMITED);
    }

    public static SemType tableContainingKeyConstraint(Context cx, SemType tableConstraint, SemType keyConstraint) {
        Optional<ListAtomicType> lat = Core.listAtomicType(cx, keyConstraint);
        SemType normalizedKc = lat.map(atom -> {
            FixedLengthArray member = atom.members();
            return switch (member.fixedLength()) {
                case 0 -> Builder.valType();
                case 1 -> Core.cellAtomicType(member.initial()[0]).orElseThrow().ty();
                default -> keyConstraint;
            };
        }).orElse(keyConstraint);
        return tableContaining(cx.env, tableConstraint, normalizedKc, Builder.valType(), CELL_MUT_LIMITED);
    }

    public static SemType tableContaining(Env env, SemType tableConstraint) {
        return tableContaining(env, tableConstraint, CELL_MUT_LIMITED);
    }

    private static SemType tableContaining(Env env, SemType tableConstraint, CellAtomicType.CellMutability mut) {
        // FIXME: type
        return tableContaining(env, tableConstraint, Builder.valType(), Builder.valType(), mut);
    }

    private static SemType tableContaining(Env env, SemType tableConstraint, SemType normalizedKc, SemType normalizedKs,
                                           CellAtomicType.CellMutability mut) {
        tableConstraint = Core.intersect(tableConstraint, Builder.mappingType());
        ListDefinition typeParamArrDef = new ListDefinition();
        SemType typeParamArray = typeParamArrDef.defineListTypeWrapped(env, EMPTY_SEMTYPE_ARR, 0, tableConstraint, mut);

        ListDefinition listDef = new ListDefinition();
        SemType tupleType =
                listDef.defineListTypeWrapped(env, new SemType[]{typeParamArray, normalizedKc, normalizedKs}, 3,
                        Builder.neverType(),
                        CELL_MUT_LIMITED);
        Bdd bdd = (Bdd) Core.subTypeData(tupleType, BasicTypeCode.BT_LIST);
        return Core.createBasicSemType(BasicTypeCode.BT_TABLE, bdd);
    }
}
