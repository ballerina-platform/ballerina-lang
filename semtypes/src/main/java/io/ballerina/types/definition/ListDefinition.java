/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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
package io.ballerina.types.definition;

import io.ballerina.types.Atom;
import io.ballerina.types.Common;
import io.ballerina.types.ComplexSemType;
import io.ballerina.types.Core;
import io.ballerina.types.Definition;
import io.ballerina.types.Env;
import io.ballerina.types.FixedLengthArray;
import io.ballerina.types.ListAtomicType;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.RecAtom;
import io.ballerina.types.SemType;
import io.ballerina.types.UniformSubtype;
import io.ballerina.types.UniformTypeCode;
import io.ballerina.types.subtypedata.BddNode;
import io.ballerina.types.typeops.BddCommonOps;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent list/tuple type desc.
 *
 * @since 3.0.0
 */
public class ListDefinition implements Definition {
    private RecAtom roRec = null;
    private RecAtom rwRec = null;

    // The SemType is created lazily so that we have the possibility
    // to share the Bdd between the RO and RW cases.
    private ComplexSemType semType = null;

    @Override
    public SemType getSemType(Env env) {
        ComplexSemType s = this.semType;
        if (s == null) {
            RecAtom ro = env.recListAtom();
            RecAtom rw = env.recListAtom();
            this.roRec = ro;
            this.rwRec = rw;
            return this.createSemType(env, ro, rw);
        } else {
            return s;
        }
    }

    // Overload define method for commonly used default parameter values
    public ComplexSemType define(Env env, List<SemType> initial) {
        return define(env, initial, initial.size(), PredefinedType.NEVER);
    }

    public ComplexSemType define(Env env, List<SemType> initial, int size) {
        return define(env, initial, size, PredefinedType.NEVER);
    }

    public ComplexSemType define(Env env, SemType rest) {
        return define(env, new ArrayList<>(), 0, rest);
    }

    public ComplexSemType define(Env env, List<SemType> initial, SemType rest) {
        return define(env, initial, initial.size(), rest);
    }

    public ComplexSemType define(Env env, List<SemType> initial, int fixedLength , SemType rest) {
        FixedLengthArray members = fixedLengthNormalize(FixedLengthArray.from(initial, fixedLength));
        ListAtomicType rwType = ListAtomicType.from(members, rest);
        Atom rw;
        RecAtom rwRec = this.rwRec;
        if (rwRec != null) {
            rw = rwRec;
            env.setRecListAtomType(rwRec, rwType);
        } else {
            rw = env.listAtom(rwType);
        }

        Atom ro;
        ListAtomicType roType = readOnlyListAtomicType(rwType);
        if (roType == rwType) {
            RecAtom roRec = this.roRec;
            if (roRec == null) {
                // share the definitions
                ro = rw;
            } else {
                ro = roRec;
                env.setRecListAtomType(roRec, rwType);
            }
        } else {
            ro = env.listAtom(roType);
            RecAtom roRec = this.roRec;
            if (roRec != null) {
                env.setRecListAtomType(roRec, roType);
            }
        }
        return this.createSemType(env, ro, rw);
    }

    private ListAtomicType readOnlyListAtomicType(ListAtomicType ty) {
        if ((Common.typeListIsReadOnly(ty.members.initial.toArray(new SemType[0])))
                && Core.isReadOnly(ty.rest)) {
            return ty;
        }
        return ListAtomicType.from(
                FixedLengthArray.from(List.of(Common.readOnlyTypeList(ty.members.initial.toArray(new SemType[0]))),
                        ty.members.fixedLength), Core.intersect(ty.rest, PredefinedType.READONLY));
    }

    private FixedLengthArray fixedLengthNormalize(FixedLengthArray array) {
        List<SemType> initial = array.initial;
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

    private ComplexSemType createSemType(Env env, Atom ro, Atom rw) {
        BddNode roBdd = BddCommonOps.bddAtom(ro);
        BddNode rwBdd;
        if (BddCommonOps.atomCmp(ro, rw) == 0) {
            // share the BDD
            rwBdd = roBdd;
        } else {
            rwBdd = BddCommonOps.bddAtom(rw);
        }

        ComplexSemType s = ComplexSemType.createComplexSemType(0,
                UniformSubtype.from(UniformTypeCode.UT_LIST_RO, roBdd),
                UniformSubtype.from(UniformTypeCode.UT_LIST_RW, rwBdd));
        this.semType = s;
        return s;
    }

    public static SemType tuple(Env env, SemType... members) {
        ListDefinition def = new ListDefinition();
        return def.define(env, List.of(members));
    }
}
