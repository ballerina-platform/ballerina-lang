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
import io.ballerina.types.Common;
import io.ballerina.types.Conjunction;
import io.ballerina.types.Context;
import io.ballerina.types.Core;
import io.ballerina.types.FunctionAtomicType;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.SubtypeData;

import java.io.PrintStream;

import static io.ballerina.types.Common.memoSubtypeIsEmpty;

/**
 * Function specific methods operate on SubtypeData.
 *
 * @since 2201.8.0
 */
public class FunctionOps extends CommonOps implements BasicTypeOps {

    private static final PrintStream console = System.out;

    @Override
    public boolean isEmpty(Context cx, SubtypeData t) {
        return memoSubtypeIsEmpty(cx, cx.functionMemo,
                (context, bdd) -> Common.bddEvery(context, bdd, null, null, FunctionOps::functionFormulaIsEmpty),
                (Bdd) t);
    }

    private static boolean functionFormulaIsEmpty(Context cx, Conjunction pos, Conjunction neg) {
        return functionPathIsEmpty(cx, functionUnionParams(cx, pos), pos, neg);
    }

    private static boolean functionPathIsEmpty(Context cx, SemType params, Conjunction pos, Conjunction neg) {
        if (neg == null) {
            return false;
        } else {
            FunctionAtomicType t = cx.functionAtomType(neg.atom);
            SemType t0 = t.paramType();
            SemType t1 = t.retType();
            return (Core.isSubtype(cx, t0, params) && functionPhi(cx, t0, Core.complement(t1), pos))
                    || functionPathIsEmpty(cx, params, pos, neg.next);
        }
    }

    private static boolean functionPhi(Context cx, SemType t0, SemType t1, Conjunction pos) {
        if (pos == null) {
            return Core.isEmpty(cx, t0) || Core.isEmpty(cx, t1);
        } else {
            FunctionAtomicType s = cx.functionAtomType(pos.atom);
            SemType s0 = s.paramType();
            SemType s1 = s.retType();
            return (Core.isSubtype(cx, t0, s0)
                    || Core.isSubtype(cx, functionIntersectRet(cx, pos.next), Core.complement(t1)))
                    && functionPhi(cx, t0, Core.intersect(t1, s1), pos.next)
                    && functionPhi(cx, Core.diff(t0, s0), t1, pos.next);
        }
    }

    private static SemType functionUnionParams(Context cx, Conjunction pos) {
        if (pos == null) {
            return PredefinedType.NEVER;
        }
        return Core.union(cx.functionAtomType(pos.atom).paramType(), functionUnionParams(cx, pos.next));
    }

    private static SemType functionIntersectRet(Context cx, Conjunction pos) {
        if (pos == null) {
            return PredefinedType.VAL;
        }
        return Core.intersect(cx.functionAtomType(pos.atom).retType(), functionIntersectRet(cx, pos.next));
    }

    private boolean functionTheta(Context cx, SemType t0, SemType t1, Conjunction pos) {
        if (pos == null) {
            return Core.isEmpty(cx, t0) || Core.isEmpty(cx, t1);
        } else {
            // replaces the SemType[2] [s0, s1] in nballerina where s0 = paramType, s1 = retType
            FunctionAtomicType s = cx.functionAtomType(pos.atom);
            SemType s0 = s.paramType();
            SemType s1 = s.retType();
            return (Core.isSubtype(cx, t0, s0) || functionTheta(cx, Core.diff(s0, t0), s1, pos.next))
                    && (Core.isSubtype(cx, t1, Core.complement(s1))
                    || functionTheta(cx, s0, Core.intersect(s1, t1), pos.next));
        }
    }
}
