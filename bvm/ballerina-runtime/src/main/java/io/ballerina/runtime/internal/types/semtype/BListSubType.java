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
import io.ballerina.runtime.api.types.semtype.Bdd;
import io.ballerina.runtime.api.types.semtype.BddAllOrNothing;
import io.ballerina.runtime.api.types.semtype.BddNode;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.Conjunction;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.Core;
import io.ballerina.runtime.api.types.semtype.Env;
import io.ballerina.runtime.api.types.semtype.ListAtomicType;
import io.ballerina.runtime.api.types.semtype.Pair;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.api.types.semtype.SubType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.ballerina.runtime.api.types.semtype.Bdd.bddEvery;
import static io.ballerina.runtime.api.types.semtype.Core.cellContainingInnerVal;
import static io.ballerina.runtime.api.types.semtype.Core.cellInner;
import static io.ballerina.runtime.api.types.semtype.Core.cellInnerVal;
import static io.ballerina.runtime.api.types.semtype.Core.intersectMemberSemTypes;
import static io.ballerina.runtime.api.types.semtype.ListAtomicType.LIST_ATOMIC_INNER;
import static io.ballerina.runtime.internal.types.semtype.BIntSubType.intSubtypeContains;

// TODO: this has lot of common code with cell (and future mapping), consider refact
public class BListSubType extends SubType implements DelegatedSubType {

    public final Bdd inner;

    private BListSubType(Bdd inner) {
        super(inner.isAll(), inner.isNothing());
        this.inner = inner;
    }

    public static BListSubType createDelegate(SubType inner) {
        if (inner instanceof Bdd bdd) {
            return new BListSubType(bdd);
        } else if (inner.isAll() || inner.isNothing()) {
            throw new IllegalStateException("unimplemented");
        } else if (inner instanceof BListSubType bList) {
            return new BListSubType(bList.inner);
        }
        throw new IllegalArgumentException("Unexpected inner type");
    }

    @Override
    public SubType union(SubType other) {
        if (!(other instanceof BListSubType otherList)) {
            throw new IllegalArgumentException("union of different subtypes");
        }
        return createDelegate(inner.union(otherList.inner));
    }

    @Override
    public SubType intersect(SubType other) {
        if (!(other instanceof BListSubType otherList)) {
            throw new IllegalArgumentException("intersect of different subtypes");
        }
        return createDelegate(inner.intersect(otherList.inner));
    }

    @Override
    public SubType complement() {
        return createDelegate(inner.complement());
    }

    @Override
    public SubType diff(SubType other) {
        if (!(other instanceof BListSubType otherList)) {
            throw new IllegalArgumentException("diff of different subtypes");
        }
        return createDelegate(inner.diff(otherList.inner));
    }

    @Override
    public boolean isEmpty(Context cx) {
        return cx.memoSubtypeIsEmpty(cx.listMemo,
                (context, bdd) -> bddEvery(context, bdd, null, null, BListSubType::listFormulaIsEmpty), inner);
    }

    private static boolean listFormulaIsEmpty(Context cx, Conjunction pos, Conjunction neg) {
        FixedLengthArray members;
        SemType rest;
        if (pos == null) {
            ListAtomicType atom = LIST_ATOMIC_INNER;
            members = atom.members();
            rest = atom.rest();
        } else {
            // combine all the positive tuples using intersection
            ListAtomicType lt = cx.listAtomType(pos.atom());
            members = lt.members();
            rest = lt.rest();
            Conjunction p = pos.next();
            // the neg case is in case we grow the array in listInhabited
            if (p != null || neg != null) {
                members = members.shallowCopy();
            }
            while (true) {
                if (p == null) {
                    break;
                } else {
                    Atom d = p.atom();
                    p = p.next();
                    lt = cx.listAtomType(d);
                    Pair<FixedLengthArray, SemType>
                            intersected = listIntersectWith(cx.env, members, rest, lt.members(), lt.rest());
                    if (intersected == null) {
                        return true;
                    }
                    members = intersected.first();
                    rest = intersected.second();
                }
            }
            if (fixedArrayAnyEmpty(cx, members)) {
                return true;
            }
        }
        Integer[] indices = listSamples(cx, members, rest, neg);
        Pair<SemType[], Integer> sampleTypes = listSampleTypes(cx, members, rest, indices);
        return !listInhabited(cx, indices, sampleTypes.first(), sampleTypes.second(), neg);
    }

    // This function determines whether a list type P & N is inhabited.
    // where P is a positive list type and N is a list of negative list types.
    // With just straightforward fixed-length tuples we can consider every index of the tuple.
    // But we cannot do this in general because of rest types and fixed length array types e.g. `byte[10000000]`.
    // So we consider instead a collection of indices that is sufficient for us to determine inhabitation,
    // given the types of P and N.
    // `indices` is this list of sample indices: these are indicies into members of the list type.
    // We don't represent P directly. Instead P is represented by `memberTypes` and `nRequired`:
    // `memberTypes[i]` is the type that P gives to `indices[i]`;
    // `nRequired` is the number of members of `memberTypes` that are required by P.
    // `neg` represents N.
    private static boolean listInhabited(Context cx, Integer[] indices, SemType[] memberTypes, int nRequired,
                                         Conjunction neg) {
        if (neg == null) {
            return true;
        } else {
            final ListAtomicType nt = cx.listAtomType(neg.atom());
            if (nRequired > 0 && Core.isNever(listMemberAtInnerVal(nt.members(), nt.rest(), indices[nRequired - 1]))) {
                // Skip this negative if it is always shorter than the minimum required by the positive
                return listInhabited(cx, indices, memberTypes, nRequired, neg.next());
            }
            // Consider cases we can avoid this negative by having a sufficiently short list
            int negLen = nt.members().fixedLength();
            if (negLen > 0) {
                int len = memberTypes.length;
                // If we have isEmpty(T1 & S1) or isEmpty(T2 & S2) then we have [T1, T2] / [S1, S2] = [T1, T2].
                // Therefore, we can skip the negative
                for (int i = 0; i < len; i++) {
                    int index = indices[i];
                    if (index >= negLen) {
                        break;
                    }
                    SemType negMemberType = listMemberAt(nt.members(), nt.rest(), index);
                    SemType common = Core.intersect(memberTypes[i], negMemberType);
                    if (Core.isEmpty(cx, common)) {
                        return listInhabited(cx, indices, memberTypes, nRequired, neg.next());
                    }
                }
                // Consider cases we can avoid this negative by having a sufficiently short list
                if (len < indices.length && indices[len] < negLen) {
                    return listInhabited(cx, indices, memberTypes, nRequired, neg.next());
                }
                for (int i = nRequired; i < memberTypes.length; i++) {
                    if (indices[i] >= negLen) {
                        break;
                    }
                    // TODO: avoid creating new arrays here, maybe use an object pool for this
                    //  -- Or use a copy on write array?
                    SemType[] t = Arrays.copyOfRange(memberTypes, 0, i);
                    if (listInhabited(cx, indices, t, nRequired, neg.next())) {
                        return true;
                    }
                }
            }
            // Now we need to explore the possibility of shapes with length >= neglen
            // This is the heart of the algorithm.
            // For [v0, v1] not to be in [t0,t1], there are two possibilities
            // (1) v0 is not in t0, or
            // (2) v1 is not in t1
            // Case (1)
            // For v0 to be in s0 but not t0, d0 must not be empty.
            // We must then find a [v0,v1] satisfying the remaining negated tuples,
            // such that v0 is in d0.
            // SemType d0 = diff(s[0], t[0]);
            // if !isEmpty(cx, d0) && tupleInhabited(cx, [d0, s[1]], neg.rest) {
            //     return true;
            // }
            // Case (2)
            // For v1 to be in s1 but not t1, d1 must not be empty.
            // We must then find a [v0,v1] satisfying the remaining negated tuples,
            // such that v1 is in d1.
            // SemType d1 = diff(s[1], t[1]);
            // return !isEmpty(cx, d1) &&  tupleInhabited(cx, [s[0], d1], neg.rest);
            // We can generalize this to tuples of arbitrary length.
            for (int i = 0; i < memberTypes.length; i++) {
                SemType d = Core.diff(memberTypes[i], listMemberAt(nt.members(), nt.rest(), indices[i]));
                if (!Core.isEmpty(cx, d)) {
                    SemType[] t = memberTypes.clone();
                    t[i] = d;
                    // We need to make index i be required
                    if (listInhabited(cx, indices, t, Integer.max(nRequired, i + 1), neg.next())) {
                        return true;
                    }
                }
            }
            // This is correct for length 0, because we know that the length of the
            // negative is 0, and [] - [] is empty.
            return false;
        }
    }

    public static Pair<SemType[], Integer> listSampleTypes(Context cx, FixedLengthArray members,
                                                           SemType rest, Integer[] indices) {
        List<SemType> memberTypes = new ArrayList<>(indices.length);
        int nRequired = 0;
        for (int i = 0; i < indices.length; i++) {
            int index = indices[i];
            SemType t = cellContainingInnerVal(cx.env, listMemberAt(members, rest, index));
            if (Core.isEmpty(cx, t)) {
                break;
            }
            memberTypes.add(t);
            if (index < members.fixedLength()) {
                nRequired = i + 1;
            }
        }
        SemType[] buffer = new SemType[memberTypes.size()];
        return Pair.from(memberTypes.toArray(buffer), nRequired);
    }

    // Return a list of sample indices for use as second argument of `listInhabited`.
    // The positive list type P is represented by `members` and `rest`.
    // The negative list types N are represented by `neg`
    // The `indices` list (first member of return value) is constructed in two stages.
    // First, the set of all non-negative integers is partitioned so that two integers are
    // in different partitions if they get different types as an index in P or N.
    // Second, we choose a number of samples from each partition. It doesn't matter
    // which sample we choose, but (this is the key point) we need at least as many samples
    // as there are negatives in N, so that for each negative we can freely choose a type for the sample
    // to avoid being matched by that negative.
    public static Integer[] listSamples(Context cx, FixedLengthArray members, SemType rest, Conjunction neg) {
        int maxInitialLength = members.initial().length;
        List<Integer> fixedLengths = new ArrayList<>();
        fixedLengths.add(members.fixedLength());
        Conjunction tem = neg;
        int nNeg = 0;
        while (true) {
            if (tem != null) {
                ListAtomicType lt = cx.listAtomType(tem.atom());
                FixedLengthArray m = lt.members();
                maxInitialLength = Integer.max(maxInitialLength, m.initial().length);
                if (m.fixedLength() > maxInitialLength) {
                    fixedLengths.add(m.fixedLength());
                }
                nNeg += 1;
                tem = tem.next();
            } else {
                break;
            }
        }
        Collections.sort(fixedLengths);
        // `boundaries` partitions the non-negative integers
        // Construct `boundaries` from `fixedLengths` and `maxInitialLength`
        // An index b is a boundary point if indices < b are different from indices >= b
        //int[] boundaries = from int i in 1 ... maxInitialLength select i;
        List<Integer> boundaries = new ArrayList<>(fixedLengths.size());
        for (int i = 1; i <= maxInitialLength; i++) {
            boundaries.add(i);
        }
        for (int n : fixedLengths) {
            // this also removes duplicates
            if (boundaries.isEmpty() || n > boundaries.get(boundaries.size() - 1)) {
                boundaries.add(n);
            }
        }
        // Now construct the list of indices by taking nNeg samples from each partition.
        List<Integer> indices = new ArrayList<>(boundaries.size());
        int lastBoundary = 0;
        if (nNeg == 0) {
            // this is needed for when this is used in listProj
            nNeg = 1;
        }
        for (int b : boundaries) {
            int segmentLength = b - lastBoundary;
            // Cannot have more samples than are in the parition.
            int nSamples = Integer.min(segmentLength, nNeg);
            for (int i = b - nSamples; i < b; i++) {
                indices.add(i);
            }
            lastBoundary = b;
        }
        for (int i = 0; i < nNeg; i++) {
            // Be careful to avoid integer overflow.
            if (lastBoundary > Integer.MAX_VALUE - i) {
                break;
            }
            indices.add(lastBoundary + i);
        }
        Integer[] arr = new Integer[indices.size()];
        return indices.toArray(arr);
    }

    public static boolean fixedArrayAnyEmpty(Context cx, FixedLengthArray array) {
        for (var t : array.initial()) {
            if (Core.isEmpty(cx, t)) {
                return true;
            }
        }
        return false;
    }

    public static Pair<FixedLengthArray, SemType> listIntersectWith(Env env, FixedLengthArray members1, SemType rest1,
                                                                    FixedLengthArray members2, SemType rest2) {

        if (listLengthsDisjoint(members1, rest1, members2, rest2)) {
            return null;
        }
        int max = Integer.max(members1.fixedLength(), members2.fixedLength());
        SemType[] initial = new SemType[max];
        for (int i = 0; i < max; i++) {
            initial[i] =
                    intersectMemberSemTypes(env, listMemberAt(members1, rest1, i), listMemberAt(members2, rest2, i));
        }
        return Pair.from(new FixedLengthArray(initial,
                        Integer.max(members1.fixedLength(), members2.fixedLength())),
                intersectMemberSemTypes(env, rest1, rest2));
    }

    private static boolean listLengthsDisjoint(FixedLengthArray members1, SemType rest1,
                                               FixedLengthArray members2, SemType rest2) {
        int len1 = members1.fixedLength();
        int len2 = members2.fixedLength();
        if (len1 < len2) {
            return Core.isNever(cellInnerVal(rest1));
        }
        if (len2 < len1) {
            return Core.isNever(cellInnerVal(rest2));
        }
        return false;
    }

    private static SemType listMemberAt(FixedLengthArray fixedArray, SemType rest, int index) {
        if (index < fixedArray.fixedLength()) {
            return fixedArrayGet(fixedArray, index);
        }
        return rest;
    }

    private static SemType fixedArrayGet(FixedLengthArray members, int index) {
        int memberLen = members.initial().length;
        int i = Integer.min(index, memberLen - 1);
        return members.initial()[i];
    }

    public static SemType listMemberAtInnerVal(FixedLengthArray fixedArray, SemType rest, int index) {
        return cellInnerVal(listMemberAt(fixedArray, rest, index));
    }

    public static SemType bddListMemberTypeInnerVal(Context cx, Bdd b, SubTypeData key, SemType accum) {
        if (b instanceof BddAllOrNothing allOrNothing) {
            return allOrNothing.isAll() ? accum : Builder.neverType();
        } else {
            BddNode bddNode = (BddNode) b;
            return Core.union(bddListMemberTypeInnerVal(cx, bddNode.left(), key,
                            Core.intersect(listAtomicMemberTypeInnerVal(cx.listAtomType(bddNode.atom()), key), accum)),
                    Core.union(bddListMemberTypeInnerVal(cx, bddNode.middle(), key, accum),
                            bddListMemberTypeInnerVal(cx, bddNode.right(), key, accum)));
        }
    }

    private static SemType listAtomicMemberTypeInnerVal(ListAtomicType atomic, SubTypeData key) {
        return Core.diff(listAtomicMemberTypeInner(atomic, key), Builder.undef());
    }

    private static SemType listAtomicMemberTypeInner(ListAtomicType atomic, SubTypeData key) {
        return listAtomicMemberTypeAtInner(atomic.members(), atomic.rest(), key);
    }

    static SemType listAtomicMemberTypeAtInner(FixedLengthArray fixedArray, SemType rest, SubTypeData key) {
        if (key instanceof BIntSubType.IntSubTypeData intSubtype) {
            SemType m = Builder.neverType();
            int initLen = fixedArray.initial().length;
            int fixedLen = fixedArray.fixedLength();
            if (fixedLen != 0) {
                for (int i = 0; i < initLen; i++) {
                    if (intSubtypeContains(key, i)) {
                        m = Core.union(m, cellInner(fixedArrayGet(fixedArray, i)));
                    }
                }
                if (intSubtype.isRangeOverlap(new BIntSubType.Range(initLen, fixedLen - 1))) {
                    m = Core.union(m, cellInner(fixedArrayGet(fixedArray, fixedLen - 1)));
                }
            }
            if (fixedLen == 0 || intSubtype.max() > fixedLen - 1) {
                m = Core.union(m, cellInner(rest));
            }
            return m;
        }
        SemType m = cellInner(rest);
        if (fixedArray.fixedLength() > 0) {
            for (SemType ty : fixedArray.initial()) {
                m = Core.union(m, cellInner(ty));
            }
        }
        return m;
    }

    @Override
    public SubTypeData data() {
        throw new IllegalStateException("unimplemented");
    }

    @Override
    public Bdd inner() {
        return inner;
    }
}
