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
package io.ballerina.semtype.definition;

import io.ballerina.semtype.Atom;
import io.ballerina.semtype.Common;
import io.ballerina.semtype.ComplexSemType;
import io.ballerina.semtype.Core;
import io.ballerina.semtype.Definition;
import io.ballerina.semtype.Env;
import io.ballerina.semtype.ListAtomicType;
import io.ballerina.semtype.PredefinedType;
import io.ballerina.semtype.RecAtom;
import io.ballerina.semtype.SemType;
import io.ballerina.semtype.UniformSubtype;
import io.ballerina.semtype.UniformTypeCode;
import io.ballerina.semtype.subtypedata.BddNode;
import io.ballerina.semtype.typeops.BddCommonOps;

import java.util.List;

import static io.ballerina.semtype.Core.isReadOnly;

/**
 * Represent list/tuple type desc.
 *
 * @since 2.0.0
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

    public ComplexSemType define(Env env, List<SemType> members, SemType rest) {
        ListAtomicType rwType = ListAtomicType.from(members.toArray(new SemType[]{}), rest);
        Atom rw;
        RecAtom rwRec = this.rwRec;
        if (rwRec != null) {
            rw = rwRec;
            env.setRecListAtomType(rwRec, rwType);
        } else {
            rw = env.listAtom(rwType);
        }

        Atom ro;
        if (Common.typeListIsReadOnly(rwType.members) && isReadOnly(rwType.rest)) {
            RecAtom roRec = this.roRec;
            if (roRec == null) {
                // share the definitions
                ro = rw;
            } else {
                ro = roRec;
                env.setRecListAtomType(roRec, rwType);
            }
        } else {
            ListAtomicType roType = ListAtomicType.from(
                    Common.readOnlyTypeList(rwType.members),
                    Core.intersect(rwType.rest, PredefinedType.READONLY));

            ro = env.listAtom(roType);
            RecAtom roRec = this.roRec;
            if (roRec != null) {
                env.setRecListAtomType(roRec, roType);
            }
        }
        return this.createSemType(env, ro, rw);
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
        return def.define(env, List.of(members), PredefinedType.NEVER);
    }
}
