/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.Bdd;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.Conjunction;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.Core;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.api.types.semtype.SubType;

import java.util.Objects;

import static io.ballerina.runtime.api.types.semtype.Bdd.bddEvery;

public class BFunctionSubType extends SubType implements DelegatedSubType {

    public final Bdd inner;

    private BFunctionSubType(Bdd inner) {
        super(inner.isAll(), inner.isNothing());
        this.inner = inner;
    }

    public static BFunctionSubType createDelegate(SubType inner) {
        if (inner instanceof Bdd bdd) {
            return new BFunctionSubType(bdd);
        } else if (inner.isAll() || inner.isNothing()) {
            throw new IllegalStateException("unimplemented");
        } else if (inner instanceof BFunctionSubType bFunction) {
            return new BFunctionSubType(bFunction.inner);
        }
        throw new IllegalArgumentException("Unexpected inner type");
    }

    @Override
    public SubType union(SubType other) {
        if (!(other instanceof BFunctionSubType otherFn)) {
            throw new IllegalArgumentException("union of different subtypes");
        }
        return createDelegate(inner.union(otherFn.inner));
    }

    @Override
    public SubType intersect(SubType other) {
        if (!(other instanceof BFunctionSubType otherList)) {
            throw new IllegalArgumentException("intersect of different subtypes");
        }
        return createDelegate(inner.intersect(otherList.inner));
    }

    @Override
    public SubType complement() {
        return createDelegate(inner.complement());
    }

    @Override
    public boolean isEmpty(Context cx) {
        return cx.memoSubtypeIsEmpty(cx.functionMemo,
                (context, bdd) -> bddEvery(context, bdd, BFunctionSubType::functionFormulaIsEmpty), inner);
    }

    private static boolean functionFormulaIsEmpty(Context cx, Conjunction pos, Conjunction neg) {
        return functionPathIsEmpty(cx, functionUnionParams(cx, pos), functionUnionQualifiers(cx, pos), pos, neg);
    }

    private static boolean functionPathIsEmpty(Context cx, SemType params, SemType qualifier, Conjunction pos,
                                               Conjunction neg) {
        if (neg == null) {
            return false;
        }
        FunctionAtomicType t = cx.functionAtomicType(neg.atom());
        SemType t0 = t.paramType();
        SemType t1 = t.retType();
        SemType t2 = t.qualifiers();
        return (Core.isSubType(cx, qualifier, t2) && Core.isSubType(cx, t0, params) &&
                functionPhi(cx, t0, Core.complement(t1), pos))
                || functionPathIsEmpty(cx, params, qualifier, pos, neg.next());
    }

    private static boolean functionPhi(Context cx, SemType t0, SemType t1, Conjunction pos) {
        if (pos == null) {
            // t0 is NEVER only for function top types with qualifiers
            return !Core.isNever(t0) && (Core.isEmpty(cx, t0) || Core.isEmpty(cx, t1));
        }
        return functionPhiInner(cx, t0, t1, pos);
    }

    private static boolean functionPhiInner(Context cx, SemType t0, SemType t1, Conjunction pos) {
        if (pos == null) {
            return Core.isEmpty(cx, t0) || Core.isEmpty(cx, t1);
        } else {
            FunctionAtomicType s = cx.functionAtomicType(pos.atom());
            SemType s0 = s.paramType();
            SemType s1 = s.retType();
            return (Core.isSubType(cx, t0, s0)
                    || Core.isSubType(cx, functionIntersectRet(cx, pos.next()), Core.complement(t1)))
                    && functionPhiInner(cx, t0, Core.intersect(t1, s1), pos.next())
                    && functionPhiInner(cx, Core.diff(t0, s0), t1, pos.next());
        }
    }

    private static SemType functionIntersectRet(Context cx, Conjunction pos) {
        if (pos == null) {
            return Builder.getValType();
        }
        return Core.intersect(cx.functionAtomicType(pos.atom()).retType(), functionIntersectRet(cx, pos.next()));
    }

    private static SemType functionUnionParams(Context cx, Conjunction pos) {
        if (pos == null) {
            return Builder.neverType();
        }
        return Core.union(cx.functionAtomicType(pos.atom()).paramType(), functionUnionParams(cx, pos.next()));
    }

    private static SemType functionUnionQualifiers(Context cx, Conjunction pos) {
        if (pos == null) {
            return Builder.neverType();
        }
        return Core.union(cx.functionAtomicType(pos.atom()).qualifiers(), functionUnionQualifiers(cx, pos.next()));
    }

    @Override
    public SubTypeData data() {
        throw new IllegalStateException("unimplemented");
    }

    @Override
    public Bdd inner() {
        return inner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BMappingSubType that)) {
            return false;
        }
        return Objects.equals(inner, that.inner);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(inner);
    }

}
