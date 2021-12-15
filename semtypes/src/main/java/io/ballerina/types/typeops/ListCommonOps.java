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
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.SubtypeData;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.types.PredefinedType.TOP;

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
                    FixedLengthArraySemtype intersected = listIntersectWith(members, rest, lt);
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
            // XXX equals method for UniformTypeBitset will be overridden in #33756
            if (!PredefinedType.NEVER.equals(rest) && Core.isEmpty(cx, rest)) {
                rest = PredefinedType.NEVER;
            }
        }
        return !listInhabited(cx, members, rest, neg);
    }

    private static FixedLengthArraySemtype listIntersectWith(FixedLengthArray members,
                                                             SemType rest, ListAtomicType lt) {
        int ltLen = lt.members.fixedLength;
        int newLen = Integer.max(members.fixedLength, ltLen);
        // We can specifically handle the case where length of `initial` and `fixedLength` are the same
        if (members.fixedLength < newLen) {
            if (Core.isNever(rest)) {
                return null;
            }
            fixedArrayFill(members, newLen, rest);
        }
        int nonRepeatedLen = Integer.max(members.initial.size(), lt.members.initial.size());
        for (int i = 0; i < nonRepeatedLen; i++) {
            fixedArraySet(members, i,
                    Core.intersect(listMemberAt(members, rest, i), listMemberAt(lt.members, lt.rest, i)));
        }
        if (ltLen < newLen) {
            if (Core.isNever(lt.rest)) {
                return null;
            }
            for (int i = ltLen; i < newLen; i++) {
                fixedArraySet(members, i, Core.intersect(listMemberAt(members, rest, i), lt.rest));
            }
        }
        return FixedLengthArraySemtype.from(members, Core.intersect(rest, lt.rest));
    }

    // This function returns true if there is a list shape v such that
    // is in the type described by `members` and `rest`, and
    // for each tuple t in `neg`, v is not in t.
    // `neg` represents a set of negated list types.
    // Precondition is that each of `members` is not empty.
    // This is formula Phi' in section 7.3.1 of Alain Frisch's PhD thesis,
    // generalized to tuples of arbitrary length.
    static boolean listInhabited(Context cx, FixedLengthArray members, SemType rest, Conjunction neg) {
        if (neg == null) {
            return true;
        } else {
            int len = members.fixedLength;
            ListAtomicType nt = cx.listAtomType(neg.atom);
            int negLen = nt.members.fixedLength;
            if (len < negLen) {
                if (Core.isNever(rest)) {
                    return listInhabited(cx, members, rest, neg.next);
                }
                fixedArrayFill(members, negLen, rest);
                len = negLen;
            } else if (negLen < len && Core.isNever(nt.rest)) {
                return listInhabited(cx, members, rest, neg.next);
            }
            // now we have nt.members.length() <= len

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
            for (int i = 0; i < len; i++) {
                SemType d = Core.diff(fixedArrayGet(members, i), listMemberAt(nt.members, nt.rest, i));
                if (!Core.isEmpty(cx, d)) {
                    FixedLengthArray s = fixedArrayShallowCopy(members);
                    fixedArraySet(s, i, d);
                    if (listInhabited(cx, s, rest, neg.next)) {
                        return true;
                    }
                }
            }
            if (!Core.isEmpty(cx, Core.diff(rest, nt.rest))) {
                return true;
            }
            // This is correct for length 0, because we know that the length of the
            // negative is 0, and [] - [] is empty.
            return false;
        }
    }

    private static SemType listMemberAt(FixedLengthArray fixedArray, SemType rest, int index) {
        if (index < fixedArray.fixedLength) {
            return fixedArrayGet(fixedArray, index);
        }
        return rest;
    }

    private static boolean fixedArrayAnyEmpty(Context cx, FixedLengthArray array) {
        for (var t : array.initial) {
            if (Core.isEmpty(cx, t)) {
                return true;
            }
        }
        return false;
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

        SemType lastMember = members.initial.get(initCount - 1);
        for (int i = initCount; i < setIndex - 1; i++) {
            members.initial.add(lastMember);
        }
        members.initial.set(setIndex, m);
    }

    private static FixedLengthArray fixedArrayShallowCopy(FixedLengthArray array) {
        return FixedLengthArray.from(Common.shallowCopyTypes(array.initial), array.fixedLength);
    }

    private static void fixedArrayFill(FixedLengthArray arr, int newLen, SemType filler) {
        List<SemType> initial = arr.initial;
        if (arr.fixedLength == 0 || initial.get(initial.size() - 1) != filler) {
            initial.add(filler);
        }
        arr.fixedLength = newLen;
    }
}

class FixedLengthArraySemtype {
    FixedLengthArray members;
    SemType rest;

    private FixedLengthArraySemtype(FixedLengthArray members, SemType rest) {
        this.members = members;
        this.rest = rest;
    }

    public static FixedLengthArraySemtype from(FixedLengthArray members, SemType rest) {
        return new FixedLengthArraySemtype(members, rest);
    }
}
