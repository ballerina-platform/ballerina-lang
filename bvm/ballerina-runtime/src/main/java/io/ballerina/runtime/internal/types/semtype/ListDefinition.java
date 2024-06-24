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

package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.Atom;
import io.ballerina.runtime.api.types.semtype.BasicTypeCode;
import io.ballerina.runtime.api.types.semtype.BddNode;
import io.ballerina.runtime.api.types.semtype.CellAtomicType;
import io.ballerina.runtime.api.types.semtype.Env;
import io.ballerina.runtime.api.types.semtype.ListAtomicType;
import io.ballerina.runtime.api.types.semtype.RecAtom;
import io.ballerina.runtime.api.types.semtype.SemType;

import static io.ballerina.runtime.api.types.semtype.BddNode.bddAtom;
import static io.ballerina.runtime.api.types.semtype.Builder.basicSubType;
import static io.ballerina.runtime.api.types.semtype.Builder.cellContaining;
import static io.ballerina.runtime.api.types.semtype.Builder.undef;
import static io.ballerina.runtime.api.types.semtype.CellAtomicType.CellMutability.CELL_MUT_NONE;
import static io.ballerina.runtime.api.types.semtype.Core.isNever;
import static io.ballerina.runtime.api.types.semtype.Core.union;

public class ListDefinition implements Definition {

    private RecAtom rec = null;
    private SemType semType = null;

    @Override
    public SemType getSemType(Env env) {
        SemType s = this.semType;
        if (s == null) {
            RecAtom rec = env.recListAtom();
            this.rec = rec;
            return this.createSemType(env, rec);
        }
        return s;
    }

    public SemType defineListTypeWrapped(Env env, SemType[] initial, int fixedLength, SemType rest,
                                         CellAtomicType.CellMutability mut) {
        SemType[] initialCells = new SemType[initial.length];
        for (int i = 0; i < initial.length; i++) {
            initialCells[i] = cellContaining(env, initial[i], mut);
        }
        SemType restCell = cellContaining(env, union(rest, undef()), isNever(rest) ? CELL_MUT_NONE : mut);
        return define(env, initialCells, fixedLength, restCell);
    }

    private SemType define(Env env, SemType[] initial, int fixedLength, SemType rest) {
        FixedLengthArray members = FixedLengthArray.normalized(initial, fixedLength);
        ListAtomicType atomicType = new ListAtomicType(members, rest);
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

    private SemType createSemType(Env env, Atom atom) {
        BddNode bdd = bddAtom(atom);
        this.semType = basicSubType(BasicTypeCode.BT_LIST, BListSubType.createDelegate(bdd));
        return this.semType;
    }

}
