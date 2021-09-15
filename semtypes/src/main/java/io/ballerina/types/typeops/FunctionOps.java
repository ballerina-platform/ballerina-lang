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
package io.ballerina.types.typeops;

import io.ballerina.types.Bdd;
import io.ballerina.types.BddMemo;
import io.ballerina.types.Conjunction;
import io.ballerina.types.Core;
import io.ballerina.types.FunctionAtomicType;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.SubtypeData;
import io.ballerina.types.TypeCheckContext;
import io.ballerina.types.UniformTypeOps;
import io.ballerina.types.subtypedata.BddAllOrNothing;
import io.ballerina.types.subtypedata.BddNode;

import java.io.PrintStream;

/**
 * Function specific methods operate on SubtypeData.
 *
 * @since 2.0.0
 */
public class FunctionOps extends CommonOps implements UniformTypeOps {

    private static final PrintStream console = System.out;

    @Override
    public boolean isEmpty(TypeCheckContext tc, SubtypeData t) {
        Bdd b = (Bdd) t;
        BddMemo mm = tc.functionMemo.get(b);
        BddMemo m;
        if (mm == null) {
            m = new BddMemo(b);
            tc.functionMemo.put(b, m);
        } else {
            m = mm;
            BddMemo.MemoStatus res = m.isEmpty;
            switch (res) {
                case NOT_SET:
                    // we've got a loop
                    console.println("got a function loop");
                    return true;
                case TRUE:
                    return true;
                case FALSE:
                    return false;
            }
        }
        boolean isEmpty = functionBddIsEmpty(tc, b, PredefinedType.NEVER, null, null);
        if (isEmpty) {
            m.isEmpty = BddMemo.MemoStatus.TRUE;
        } else {
            m.isEmpty = BddMemo.MemoStatus.FALSE;
        }
        return isEmpty;
    }

    private boolean functionBddIsEmpty(TypeCheckContext tc, Bdd b, SemType s, Conjunction pos, Conjunction neg) {
        if (b instanceof BddAllOrNothing) {
            if (!((BddAllOrNothing) b).isAll()) {
                return true;
            }
            if (neg == null) {
                return false;
            } else {
                // replaces the SemType[2] [t0, t1] in nballerina where t0 = paramType, t1 = retType
                FunctionAtomicType t = tc.functionAtomType(neg.atom);
                SemType t0 = t.paramType;
                SemType t1 = t.retType;
                return (Core.isSubtype(tc, t0, s) && functionTheta(tc, t0, Core.complement(t1), pos))
                        || functionBddIsEmpty(tc, BddAllOrNothing.bddAll(), s, pos, neg.next);
            }
        } else {
            BddNode bn = (BddNode) b;
            FunctionAtomicType st = tc.functionAtomType(bn.atom);
            SemType sd = st.paramType;
            SemType sr = st.retType;
            return functionBddIsEmpty(tc, bn.left, Core.union(s, sd), Conjunction.from(bn.atom, pos), neg)
                    && functionBddIsEmpty(tc, bn.middle, s, pos, neg)
                    && functionBddIsEmpty(tc, bn.right, s, pos, Conjunction.from(bn.atom, neg));
        }
    }

    private boolean functionTheta(TypeCheckContext tc, SemType t0, SemType t1, Conjunction pos) {
        if (pos == null) {
            return Core.isEmpty(tc, t0) || Core.isEmpty(tc, t1);
        } else {
            // replaces the SemType[2] [s0, s1] in nballerina where s0 = paramType, s1 = retType
            FunctionAtomicType s = tc.functionAtomType(pos.atom);
            SemType s0 = s.paramType;
            SemType s1 = s.retType;
            return (Core.isSubtype(tc, t0, s0) || functionTheta(tc, Core.diff(s0, t0), s1, pos.next))
                    && (Core.isSubtype(tc, t1, Core.complement(s1))
                    || functionTheta(tc, s0, Core.intersect(s1, t1), pos.next));
        }
    }
}
