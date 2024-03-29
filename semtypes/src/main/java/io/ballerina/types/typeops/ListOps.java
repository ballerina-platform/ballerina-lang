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

import io.ballerina.types.Atom;
import io.ballerina.types.BasicTypeOps;
import io.ballerina.types.Bdd;
import io.ballerina.types.CellSemType;
import io.ballerina.types.Common;
import io.ballerina.types.Conjunction;
import io.ballerina.types.Context;
import io.ballerina.types.Core;
import io.ballerina.types.Env;
import io.ballerina.types.FixedLengthArray;
import io.ballerina.types.ListAtomicType;
import io.ballerina.types.SemType;
import io.ballerina.types.SubtypeData;
import io.ballerina.types.subtypedata.BddAllOrNothing;
import io.ballerina.types.subtypedata.BddNode;
import io.ballerina.types.subtypedata.IntSubtype;
import io.ballerina.types.subtypedata.Range;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.ballerina.types.Common.bddSubtypeComplement;
import static io.ballerina.types.Common.bddSubtypeDiff;
import static io.ballerina.types.Common.bddSubtypeIntersect;
import static io.ballerina.types.Common.bddSubtypeUnion;
import static io.ballerina.types.Common.memoSubtypeIsEmpty;
import static io.ballerina.types.Core.cellContainingInnerVal;
import static io.ballerina.types.Core.cellInnerVal;
import static io.ballerina.types.Core.intersectMemberSemType;
import static io.ballerina.types.PredefinedType.NEVER;
import static io.ballerina.types.subtypedata.IntSubtype.intSubtypeContains;
import static io.ballerina.types.typeops.IntOps.intSubtypeMax;
import static io.ballerina.types.typeops.IntOps.intSubtypeOverlapRange;

/**
 * Basic type ops for list type.
 *
 * @since 2201.8.0
 */
public class ListOps extends CommonOps implements BasicTypeOps {

    private static boolean listSubtypeIsEmpty(Context cx, SubtypeData t) {
        return memoSubtypeIsEmpty(cx, cx.listMemo,
                (context, bdd) -> Common.bddEvery(context, bdd, null, null, ListOps::listFormulaIsEmpty),
                (Bdd) t);
    }

    private static boolean listFormulaIsEmpty(Context cx, Conjunction pos, Conjunction neg) {
        FixedLengthArray members;
        CellSemType rest;
        if (pos == null) {
            ListAtomicType atom = ListAtomicType.LIST_ATOMIC_INNER;
            members = atom.members();
            rest = atom.rest();
        } else {
            // combine all the positive tuples using intersection
            ListAtomicType lt = cx.listAtomType(pos.atom);
            members = lt.members();
            rest = lt.rest();
            Conjunction p = pos.next;
            // the neg case is in case we grow the array in listInhabited
            if (p != null || neg != null) {
                // Jbal note: we don't need this as we already created copies when converting from array to list.
                // Just keeping this for the sake of source similarity between Bal code and Java.
                members = fixedArrayShallowCopy(members);
            }
            while (true) {
                if (p == null) {
                    break;
                } else {
                    Atom d = p.atom;
                    p = p.next;
                    lt = cx.listAtomType(d);
                    TwoTuple<FixedLengthArray, CellSemType>
                            intersected = listIntersectWith(cx.env, members, rest, lt.members(), lt.rest());
                    if (intersected == null) {
                        return true;
                    }
                    members = intersected.item1;
                    rest = intersected.item2;
                }
            }
            if (fixedArrayAnyEmpty(cx, members)) {
                return true;
            }
        }
        List<Integer> indices = listSamples(cx, members, rest, neg);
        TwoTuple<List<CellSemType>, Integer> sampleTypes = listSampleTypes(cx, members, rest, indices);
        return !listInhabited(cx, indices.toArray(new Integer[0]),
                sampleTypes.item1.toArray(SemType[]::new),
                sampleTypes.item2, neg);
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
    static List<Integer> listSamples(Context cx, FixedLengthArray members, SemType rest, Conjunction neg) {
        int maxInitialLength = members.initial().size();
        List<Integer> fixedLengths = new ArrayList<>();
        fixedLengths.add(members.fixedLength());
        Conjunction tem = neg;
        int nNeg = 0;
        while (true) {
            if (tem != null) {
                ListAtomicType lt = cx.listAtomType(tem.atom);
                FixedLengthArray m = lt.members();
                maxInitialLength = Integer.max(maxInitialLength, m.initial().size());
                if (m.fixedLength() > maxInitialLength) {
                    fixedLengths.add(m.fixedLength());
                }
                nNeg += 1;
                tem = tem.next;
            } else {
                break;
            }
        }
        Collections.sort(fixedLengths);
        // `boundaries` partitions the non-negative integers
        // Construct `boundaries` from `fixedLengths` and `maxInitialLength`
        // An index b is a boundary point if indices < b are different from indices >= b
        //int[] boundaries = from int i in 1 ... maxInitialLength select i;
        List<Integer> boundaries = new ArrayList<>();
        for (int i = 1; i <= maxInitialLength; i++) {
            boundaries.add(i);
        }
        for (int n : fixedLengths) {
            // this also removes duplicates
            if (boundaries.size() == 0 || n > boundaries.get(boundaries.size() - 1)) {
                boundaries.add(n);
            }
        }
        // Now construct the list of indices by taking nNeg samples from each partition.
        List<Integer> indices = new ArrayList<>();
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
            if (lastBoundary > Long.MAX_VALUE - i) {
                break;
            }
            indices.add(lastBoundary + i);
        }
        return indices;
    }

    static TwoTuple<FixedLengthArray, CellSemType> listIntersectWith(Env env, FixedLengthArray members1,
                                                                     CellSemType rest1, FixedLengthArray members2,
                                                                     CellSemType rest2) {
        if (listLengthsDisjoint(members1, rest1, members2, rest2)) {
            return null;
        }
        int max = Integer.max(members1.initial().size(), members2.initial().size());
        List<CellSemType> initial =
                IntStream.range(0, max)
                        .mapToObj(i -> Core.intersectMemberSemType(env, listMemberAt(members1, rest1, i),
                                listMemberAt(members2, rest2, i)))
                        .collect(Collectors.toList());
        return TwoTuple.from(FixedLengthArray.from(initial,
                        Integer.max(members1.fixedLength(), members2.fixedLength())),
                intersectMemberSemType(env, rest1, rest2));
    }


    static FixedLengthArray fixedArrayShallowCopy(FixedLengthArray array) {
        return FixedLengthArray.from(array.initial(), array.fixedLength());
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
    static boolean listInhabited(Context cx, Integer[] indices, SemType[] memberTypes, int nRequired, Conjunction neg) {
        if (neg == null) {
            return true;
        } else {
            final ListAtomicType nt = cx.listAtomType(neg.atom);
            if (nRequired > 0 && Core.isNever(listMemberAtInnerVal(nt.members(), nt.rest(), indices[nRequired - 1]))) {
                // Skip this negative if it is always shorter than the minimum required by the positive
                return listInhabited(cx, indices, memberTypes, nRequired, neg.next);
            }
            // Consider cases we can avoid this negative by having a sufficiently short list
            int negLen = nt.members().fixedLength();
            if (negLen > 0) {
                int len = memberTypes.length;
                if (len < indices.length && indices[len] < negLen) {
                    return listInhabited(cx, indices, memberTypes, nRequired, neg.next);
                }
                for (int i = nRequired; i < memberTypes.length; i++) {
                    if (indices[i] >= negLen) {
                        break;
                    }
                    // TODO: avoid creating new arrays here
                    SemType[] t = Arrays.copyOfRange(memberTypes, 0, i);
                    if (listInhabited(cx, indices, t, nRequired, neg.next)) {
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
                    if (listInhabited(cx, indices, t, Integer.max(nRequired, i + 1), neg.next)) {
                        return true;
                    }
                }
            }
            // This is correct for length 0, because we know that the length of the
            // negative is 0, and [] - [] is empty.
            return false;
        }
    }

    private static SemType listMemberAtInnerVal(FixedLengthArray fixedArray, CellSemType rest, int index) {
        return cellInnerVal(listMemberAt(fixedArray, rest, index));
    }

    private static boolean listLengthsDisjoint(FixedLengthArray members1, SemType rest1,
                                               FixedLengthArray members2, SemType rest2) {
        int len1 = members1.fixedLength();
        int len2 = members2.fixedLength();
        if (len1 < len2) {
            return Core.isNever(rest1);
        }
        if (len2 < len1) {
            return Core.isNever(rest2);
        }
        return false;
    }

    public static TwoTuple<List<CellSemType>, Integer> listSampleTypes(Context cx, FixedLengthArray members,
                                                                       CellSemType rest, List<Integer> indices) {
        List<CellSemType> memberTypes = new ArrayList<>();
        int nRequired = 0;
        for (int i = 0; i < indices.size(); i++) {
            int index = indices.get(i);
            CellSemType t = cellContainingInnerVal(cx.env, listMemberAt(members, rest, index));
            if (Core.isEmpty(cx, t)) {
                break;
            }
            memberTypes.add(t);
            if (index < members.fixedLength()) {
                nRequired = i + 1;
            }
        }
        return TwoTuple.from(memberTypes, nRequired);
    }

    static boolean fixedArrayAnyEmpty(Context cx, FixedLengthArray array) {
        for (var t : array.initial()) {
            if (Core.isEmpty(cx, t)) {
                return true;
            }
        }
        return false;
    }

    static CellSemType listMemberAt(FixedLengthArray fixedArray, CellSemType rest, int index) {
        if (index < fixedArray.fixedLength()) {
            return fixedArrayGet(fixedArray, index);
        }
        return rest;
    }

    private static CellSemType fixedArrayGet(FixedLengthArray members, int index) {
        int memberLen = members.initial().size();
        int i = Integer.min(index, memberLen - 1);
        return members.initial().get(i);
    }

    static SemType listAtomicMemberType(ListAtomicType atomic, SubtypeData key) {
        return listAtomicMemberTypeAt(atomic.members(), atomic.rest(), key);
    }

    static SemType listAtomicMemberTypeAt(FixedLengthArray fixedArray, SemType rest, SubtypeData key) {
        if (key instanceof IntSubtype intSubtype) {
            SemType m = NEVER;
            int initLen = fixedArray.initial().size();
            int fixedLen = fixedArray.fixedLength();
            if (fixedLen != 0) {
                for (int i = 0; i < initLen; i++) {
                    if (intSubtypeContains(key, i)) {
                        m = Core.union(m, fixedArrayGet(fixedArray, i));
                    }
                }
                if (intSubtypeOverlapRange(intSubtype, Range.from(initLen, fixedLen - 1))) {
                    m = Core.union(m, fixedArrayGet(fixedArray, fixedLen - 1));
                }
            }
            if (fixedLen == 0 || intSubtypeMax((IntSubtype) key) > fixedLen - 1) {
                m = Core.union(m, rest);
            }
            return m;
        }
        SemType m = rest;
        if (fixedArray.fixedLength() > 0) {
            for (SemType ty : fixedArray.initial()) {
                m = Core.union(m, ty);
            }
        }
        return m;
    }

    public static SemType bddListMemberType(Context cx, Bdd b, SubtypeData key, SemType accum) {
        if (b instanceof BddAllOrNothing allOrNothing) {
            return allOrNothing.isAll() ? accum : NEVER;
        } else {
            BddNode bddNode = (BddNode) b;
            return Core.union(bddListMemberType(cx, bddNode.left(), key,
                            Core.intersect(listAtomicMemberType(cx.listAtomType(bddNode.atom()), key), accum)),
                    Core.union(bddListMemberType(cx, bddNode.middle(), key, accum),
                            bddListMemberType(cx, bddNode.right(), key, accum)));
        }
    }

    @Override
    public SubtypeData union(SubtypeData d1, SubtypeData d2) {
        return bddSubtypeUnion(d1, d2);
    }

    @Override
    public SubtypeData intersect(SubtypeData d1, SubtypeData d2) {
        return bddSubtypeIntersect(d1, d2);
    }

    @Override
    public SubtypeData diff(SubtypeData d1, SubtypeData d2) {
        return bddSubtypeDiff(d1, d2);
    }

    @Override
    public SubtypeData complement(SubtypeData d) {
        return bddSubtypeComplement(d);
    }

    @Override
    public boolean isEmpty(Context cx, SubtypeData d) {
        return listSubtypeIsEmpty(cx, d);
    }
}
