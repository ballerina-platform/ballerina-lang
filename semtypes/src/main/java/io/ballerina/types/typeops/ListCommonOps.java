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

import io.ballerina.types.Atom;
import io.ballerina.types.Bdd;
import io.ballerina.types.BddMemo;
import io.ballerina.types.Common;
import io.ballerina.types.Conjunction;
import io.ballerina.types.Context;
import io.ballerina.types.Core;
import io.ballerina.types.FixedLengthArray;
import io.ballerina.types.ListAtomicType;
import io.ballerina.types.ListConjunction;
import io.ballerina.types.SemType;
import io.ballerina.types.SubtypeData;
import io.ballerina.types.subtypedata.BddAllOrNothing;
import io.ballerina.types.subtypedata.BddNode;
import io.ballerina.types.subtypedata.IntSubtype;
import io.ballerina.types.subtypedata.Range;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static io.ballerina.types.Common.shallowCopyTypes;
import static io.ballerina.types.Core.diff;
import static io.ballerina.types.Core.intersect;
import static io.ballerina.types.Core.isEmpty;
import static io.ballerina.types.Core.union;
import static io.ballerina.types.PredefinedType.NEVER;
import static io.ballerina.types.PredefinedType.TOP;
import static io.ballerina.types.subtypedata.IntSubtype.intSubtypeContains;
import static io.ballerina.types.typeops.IntOps.intSubtypeMax;
import static io.ballerina.types.typeops.IntOps.intSubtypeOverlapRange;

/**
 * Operations Common to ListRo and ListRw.
 *
 * @since 3.0.0
 */
public class ListCommonOps {
    static boolean listSubtypeIsEmpty(Context cx, SubtypeData t) {
        Bdd b = (Bdd) t;
        BddMemo mm = cx.listMemo.get(b);
        BddMemo m;
        if (mm == null) {
            m = BddMemo.from(b);
            cx.listMemo.put(m.bdd, m);
        } else {
            m = mm;
            BddMemo.MemoStatus res = m.isEmpty;
            if (res == BddMemo.MemoStatus.NOT_SET) {
                // we've got a loop
                // XXX is this right???
                return true;
            } else {
                return res == BddMemo.MemoStatus.TRUE;
            }
        }
        boolean isEmpty = Common.bddEvery(cx, b, null, null, ListCommonOps::listFormulaIsEmpty);
        m.setIsEmpty(isEmpty);
        return isEmpty;
    }

    static boolean listFormulaIsEmpty(Context cx, Conjunction pos, Conjunction neg) {
        FixedLengthArray members;
        SemType rest;
        if (pos == null) {
            members = FixedLengthArray.from(new ArrayList<>(), 0);
            rest = TOP;
        } else {
            // combine all the positive tuples using intersection
            ListAtomicType lt = cx.listAtomType(pos.atom);
            members = lt.members;
            rest = lt.rest;
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
                    FixedLengthArraySemtypePair intersected = listIntersectWith(members, rest, lt);
                    if (intersected == null) {
                        return true;
                    }
                    members = intersected.members;
                    rest = intersected.rest;
                }
            }
            if (fixedArrayAnyEmpty(cx, members)) {
                return true;
            }
            // Ensure that we can use isNever on rest in listInhabited
            if (!NEVER.equals(rest) && isEmpty(cx, rest)) {
                rest = NEVER;
            }
        }
        return !listInhabited(cx, members, rest, listConjunction(cx, neg));
    }

    static FixedLengthArraySemtypePair listIntersectWith(FixedLengthArray members,
                                                         SemType rest, ListAtomicType newType) {
        int newTypeLen = newType.members.fixedLength;
        int intersectedLen = Integer.max(members.fixedLength, newTypeLen);
        // We can specifically handle the case where length of `initial` and `fixedLength` are the same
        if (members.fixedLength < intersectedLen) {
            if (Core.isNever(rest)) {
                return null;
            }
            fixedArrayFill(members, intersectedLen, rest);
        }
        int maxInitialLen = Integer.max(members.initial.size(), newType.members.initial.size());
        for (int i = 0; i < maxInitialLen; i++) {
            fixedArraySet(members,
                          i,
                          intersect(listMemberAt(members, rest, i), listMemberAt(newType.members, newType.rest, i)));
        }
        // If the last member is repeating we need to intersect the repeating member as it will have pushed backed
        // in `initial` array
        if (maxInitialLen < members.fixedLength) {
            SemType repeatingMember = intersect(listMemberAt(members, rest, maxInitialLen),
                                                listMemberAt(newType.members, newType.rest, maxInitialLen));
            if (!repeatingMember.equals(members.initial.get(maxInitialLen))) {
                members.initial.set(maxInitialLen, repeatingMember);
            }
        }
        if (newTypeLen < intersectedLen) {
            if (Core.isNever(newType.rest)) {
                return null;
            }
            for (int i = newTypeLen; i < intersectedLen; i++) {
                fixedArraySet(members, i, intersect(listMemberAt(members, rest, i), newType.rest));
            }
        }
        return FixedLengthArraySemtypePair.from(members, intersect(rest, newType.rest));
    }

    // This function returns true if there is a list shape v such that
    // is in the type described by `members` and `rest`, and
    // for each tuple t in `neg`, v is not in t.
    // `neg` represents a set of negated list types.
    // Precondition is that each of `members` is not empty.
    // ListConjunction is sorted in decreasing order of fixedLength
    // This is formula Phi' in section 7.3.1 of Alain Frisch's PhD thesis,
    // generalized to tuples of arbitrary length.
    static boolean listInhabited(Context cx, FixedLengthArray members, SemType rest, ListConjunction neg) {
        if (neg == null) {
            return true;
        } else {
            int len = members.fixedLength;
            ListAtomicType nt = neg.listType;
            int negLen = nt.members.fixedLength;
            if (negLen < len ? Core.isNever(nt.rest) : (len < negLen && Core.isNever(rest))) {
                // Either
                // - the negative has a fixed length that is shorter than the minimum
                // length of the positive, or
                // - the positive has a fixed length that is shorter than the minimum
                // length of the negative.
                // In either case this negative cannot cancel out the positive,
                // so we can just skip over this negative.
                return listInhabited(cx, members, rest, neg.next);
            }
            if (len < negLen) {
                // Note in this case we know positive rest is not never.

                // Explore the possibility of shapes that are not matched
                // by the negative because their lengths are less than negLen.

                // First a shape with exactly len members
                // No need to copy members here
                if (listInhabited(cx, members, NEVER, neg.next)) {
                    return true;
                }
                // Check list types with fixedLength >= `len` and  < `negLen`
                if (negLen - len > 200) {
                    System.exit(1);
                    // [jBallerina] may need to change
                    // panic error("fixed length too big " + negLen.toString());
                }
                for (int i = len + 1; i < negLen; i++) {
                    FixedLengthArray s = fixedArrayShallowCopy(members);
                    fixedArrayFill(s, i, rest);
                    if (listInhabited(cx, s, NEVER, neg.next)) {
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
            // if (!isEmpty(cx, d0) && tupleInhabited(cx, [d0, s[1]], neg.rest)) {
            //     return true;
            // }
            // Case (2)
            // For v1 to be in s1 but not t1, d1 must not be empty.
            // We must then find a [v0,v1] satisfying the remaining negated tuples,
            // such that v1 is in d1.
            // SemType d1 = diff(s[1], t[1]);
            // return !isEmpty(cx, d1) &&  tupleInhabited(cx, [s[0], d1], neg.rest);
            // We can generalize this to tuples of arbitrary length.

            // The key point here that because ListConjunction is sorted in decreasing order, the negs handle every
            // index >= negLen in the same way i.e. for every N in neg, for every i, j >= negLen, N[i] = N[j].
            // Define two indices i, j to be equivalent iff for type T that is the positive and any of the negatives,
            // T[i] = T[j]. The value of maxLen here sufficient that the set { i | 0 <= i < maxLen } will contain
            // at least one index from each equivalence class.

            int maxLen = Integer.max(len + 1, negLen + 1);
            if (maxLen > 200) {
                System.exit(1);
                // [jBallerina] may need to change
                //panic error("fixed length too big " + maxLen.toString());
            }
            for (int i = 0; i < maxLen; i++) {
                SemType d = diff(listMemberAt(members, rest, i), listMemberAt(nt.members, nt.rest, i));
                if (!isEmpty(cx, d)) {
                    FixedLengthArray s = fixedArrayReplace(members, i, d, rest);
                    if (listInhabited(cx, s, rest, neg.next)) {
                        return true;
                    }
                }
            }
            // This is correct for length 0, because we know that the length of the
            // negative is 0, and [] - [] is empty.
            return false;
        }
    }

    static SemType listMemberAt(FixedLengthArray fixedArray, SemType rest, int index) {
        if (index < fixedArray.fixedLength) {
            return fixedArrayGet(fixedArray, index);
        }
        return rest;
    }

    static boolean fixedArrayAnyEmpty(Context cx, FixedLengthArray array) {
        for (var t : array.initial) {
            if (isEmpty(cx, t)) {
                return true;
            }
        }
        return false;
    }

    static void fixedArrayFill(FixedLengthArray arr, int newLen, SemType filler) {
        List<SemType> initial = arr.initial;
        if (newLen <= initial.size()) {
            return;
        }
        int initLen = initial.size();
        int fixedLen = arr.fixedLength;
        if (fixedLen == 0) {
            initial.add(filler);
        } else if (!initial.get(initLen - 1).equals(filler)) {
            SemType last = initial.get(initLen - 1);
            for (int i = 0; i < fixedLen - initLen; i++) {
                initial.add(last);
            }
            initial.add(filler);
        }
        arr.fixedLength = newLen;
    }

    private static SemType fixedArrayGet(FixedLengthArray members, int index) {
        int memberLen = members.initial.size();
        int i = Integer.min(index, memberLen - 1);
        return members.initial.get(i);
    }

    private static void fixedArraySet(FixedLengthArray members, int setIndex, SemType m) {
        int initCount = members.initial.size();
        boolean lastMemberRepeats = members.fixedLength > initCount;

        // No need to expand
        if (setIndex < initCount - (lastMemberRepeats ? 1 : 0)) {
            members.initial.set(setIndex, m);
            return;
        }
        if (lastMemberRepeats) {
            int lastIndex = initCount - 1;
            SemType lastMember = members.initial.get(lastIndex);
            int pushBack = lastIndex == setIndex ? 1 : 0;
            for (int i = initCount; i <= (setIndex + pushBack); i++) {
                members.initial.add(lastMember);
            }
        }
        members.initial.set(setIndex, m);
    }

    static FixedLengthArray fixedArrayShallowCopy(FixedLengthArray array) {
        return FixedLengthArray.from(shallowCopyTypes(array.initial), array.fixedLength);
    }

    static FixedLengthArray fixedArrayReplace(FixedLengthArray array, int index, SemType t, SemType rest) {
        FixedLengthArray copy = fixedArrayShallowCopy(array);
        fixedArrayFill(copy, index + 1, rest);
        fixedArraySet(copy, index, t);
        return copy;
    }

    static ListConjunction listConjunction(Context cx, Conjunction con) {
        Conjunction c = con;
        ArrayList<ListAtomicType> atoms = new ArrayList<>();
        while (c != null) {
            atoms.add(cx.listAtomType(c.atom));
            c = c.next;
        }
        // This is in ascending order.
        // It gets reversed as we cons it up.
        // atoms = from var a in atoms let int len = a.members.fixedLength order by len select a;
        atoms.sort(Comparator.comparing(ListAtomicType::getFixedLength));
        ListConjunction next = null;
        int maxInitialLen = 0;
        for (var listType : atoms) {
            maxInitialLen = Integer.max(maxInitialLen, listType.members.initial.size());
            ListConjunction lc = ListConjunction.from(listType, maxInitialLen, next);
            next = lc;
        }
        return next;
    }


    static SemType listAtomicMemberType(ListAtomicType atomic, SubtypeData key) {
        return listAtomicMemberTypeAt(atomic.members, atomic.rest, key);
    }

    static SemType listAtomicMemberTypeAt(FixedLengthArray fixedArray, SemType rest, SubtypeData key) {
        if (key instanceof IntSubtype) {
            SemType m = NEVER;
            int initLen = fixedArray.initial.size();
            int fixedLen = fixedArray.fixedLength;
            if (fixedLen != 0) {
                for (int i = 0; i < initLen; i++) {
                    if (intSubtypeContains(key, i)) {
                        m = union(m, fixedArrayGet(fixedArray, i));
                    }
                }
                if (intSubtypeOverlapRange((IntSubtype) key, Range.from(initLen, fixedLen - 1))) {
                    m = union(m, fixedArrayGet(fixedArray, fixedLen - 1));
                }
            }
            if (fixedLen == 0 || intSubtypeMax((IntSubtype) key) > fixedLen - 1) {
                m = union(m, rest);
            }
            return m;
        }
        SemType m = rest;
        if (fixedArray.fixedLength > 0) {
            for (SemType ty : fixedArray.initial) {
                m = union(m, ty);
            }
        }
        return m;
    }

    public static SemType bddListMemberType(Context cx, Bdd b, SubtypeData key, SemType accum) {
        if (b instanceof BddAllOrNothing) {
            return ((BddAllOrNothing) b).isAll() ? accum : NEVER;
        } else {
            BddNode bddNode = (BddNode) b;
            return union(bddListMemberType(cx,
                                           bddNode.left,
                                           key,
                                           intersect(listAtomicMemberType(cx.listAtomType(bddNode.atom), key), accum)),
                         union(bddListMemberType(cx, bddNode.middle, key, accum),
                               bddListMemberType(cx, bddNode.right, key, accum)));
        }
    }

    static class FixedLengthArraySemtypePair {
        FixedLengthArray members;
        SemType rest;

        private FixedLengthArraySemtypePair(FixedLengthArray members, SemType rest) {
            this.members = members;
            this.rest = rest;
        }

        public static FixedLengthArraySemtypePair from(FixedLengthArray members, SemType rest) {
            return new FixedLengthArraySemtypePair(members, rest);
        }
    }
}
