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
package io.ballerina.types.definition;

import io.ballerina.types.Atom;
import io.ballerina.types.BasicTypeCode;
import io.ballerina.types.CellAtomicType;
import io.ballerina.types.CellSemType;
import io.ballerina.types.ComplexSemType;
import io.ballerina.types.Definition;
import io.ballerina.types.Env;
import io.ballerina.types.FixedLengthArray;
import io.ballerina.types.ListAtomicType;
import io.ballerina.types.RecAtom;
import io.ballerina.types.SemType;
import io.ballerina.types.subtypedata.BddNode;
import io.ballerina.types.typeops.BddCommonOps;

import java.util.List;

import static io.ballerina.types.CellAtomicType.CellMutability.CELL_MUT_LIMITED;
import static io.ballerina.types.CellAtomicType.CellMutability.CELL_MUT_NONE;
import static io.ballerina.types.Core.isNever;
import static io.ballerina.types.Core.union;
import static io.ballerina.types.PredefinedType.NEVER;
import static io.ballerina.types.PredefinedType.UNDEF;
import static io.ballerina.types.PredefinedType.basicSubtype;
import static io.ballerina.types.subtypedata.CellSubtype.cellContaining;

/**
 * Represent list/tuple type desc.
 *
 * @since 2201.8.0
 */
public class ListDefinition implements Definition {

    private RecAtom rec = null;
    private ComplexSemType semType = null;

    @Override
    public SemType getSemType(Env env) {
        ComplexSemType s = this.semType;
        if (s == null) {
            RecAtom rec = env.recListAtom();
            this.rec = rec;
            return this.createSemType(env, rec);
        } else {
            return s;
        }
    }

    public static SemType tuple(Env env, SemType... members) {
        ListDefinition def = new ListDefinition();
        return def.define(env, List.of(members), members.length);
    }

    // Overload define method for commonly used default parameter values

    public SemType define(Env env, List<SemType> initial, int size) {
        return define(env, initial, size, NEVER, CELL_MUT_LIMITED);
    }

    public SemType define(Env env, List<SemType> initial, int fixedLength, SemType rest) {
        return define(env, initial, fixedLength, rest, CELL_MUT_LIMITED);
    }

    public SemType define(Env env, List<SemType> initial, int fixedLength, SemType rest,
                          CellAtomicType.CellMutability mut) {
        List<CellSemType> initialCells = initial.stream().map(t -> cellContaining(env, t, mut))
                .toList();
        CellSemType restCell = cellContaining(env, union(rest, UNDEF), isNever(rest) ? CELL_MUT_NONE : mut);
        return defineInner(env, initialCells, fixedLength, restCell);
    }

    private ComplexSemType defineInner(Env env, List<CellSemType> initial, int fixedLength, CellSemType rest) {
        FixedLengthArray members = fixedLengthNormalize(FixedLengthArray.from(initial, fixedLength));
        ListAtomicType atomicType = ListAtomicType.from(members, rest);
        Atom atom;
        RecAtom rec = this.rec;
        if (rec != null) {
            atom = rec;
            env.setRecListAtomType(rec, atomicType);
        } else {
            atom = env.listAtom(atomicType);
        }
        return this.createSemType(env, atom);
    }

    private FixedLengthArray fixedLengthNormalize(FixedLengthArray array) {
        List<CellSemType> initial = array.initial;
        int i = initial.size() - 1;
        if (i <= 0) {
            return array;
        }
        SemType last = initial.get(i);
        i -= 1;
        while (i >= 0) {
            if (last != initial.get(i)) {
                break;
            }
            i -= 1;
        }
        return FixedLengthArray.from(initial.subList(0, i + 2), array.fixedLength);
    }

    private ComplexSemType createSemType(Env env, Atom atom) {
        BddNode bdd = BddCommonOps.bddAtom(atom);
        ComplexSemType complexSemType = basicSubtype(BasicTypeCode.BT_LIST, bdd);
        this.semType = complexSemType;
        return complexSemType;
    }

    public SemType define(Env env, List<CellSemType> initial) {
        return defineInner(env, initial, initial.size(), cellContaining(env, union(NEVER, UNDEF)));
    }

    public SemType define(Env env, SemType rest) {
        return defineInner(env, List.of(), 0,
                cellContaining(env, union(rest, UNDEF), isNever(rest) ? CELL_MUT_NONE : CELL_MUT_LIMITED));
    }

    public SemType define(Env env, List<SemType> initial, SemType rest) {
        return define(env, initial, initial.size(), rest, CELL_MUT_LIMITED);
    }
}
