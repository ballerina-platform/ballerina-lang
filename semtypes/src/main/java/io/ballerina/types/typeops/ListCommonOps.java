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
import io.ballerina.types.Core;
import io.ballerina.types.ListAtomicType;
import io.ballerina.types.SemType;
import io.ballerina.types.SubtypeData;
import io.ballerina.types.Context;

import java.util.ArrayList;
import java.util.Arrays;
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
        List<SemType> members;
        SemType rest;
        if (pos == null) {
            members = new ArrayList<>();
            rest = TOP;
        } else {
            // combine all the positive tuples using intersection
            ListAtomicType lt = cx.listAtomType(pos.atom);
            members = Arrays.asList(lt.members);
            rest = lt.rest;
            Conjunction p = pos.next;
            // the neg case is in case we grow the array in listInhabited
            if (p != null || neg != null) {
                // Jbal note: we don't need this as we alredy created copies when converting from array to list.
                // Just keeping this for the sake of source similarity between Bal code and Java.
                members = Common.shallowCopyTypes(members);
            }
            while (true) {
                if (p == null) {
                    break;
                } else {
                    Atom d = p.atom;
                    p = p.next;
                    lt = cx.listAtomType(d);
                    int newLen = Integer.max(members.size(), lt.members.length);
                    if (members.size() < newLen) {
                        if (Core.isNever(rest)) {
                            return true;
                        }
                        for (int i = members.size(); i < newLen; i++) {
                            members.add(rest);
                        }
                    }
                    for (int i = 0; i < lt.members.length; i++) {
                        members.set(i, Core.intersect(members.get(i), lt.members[i]));
                    }
                    if (lt.members.length < newLen) {
                        if (Core.isNever(lt.rest)) {
                            return true;
                        }
                        for (int i = lt.members.length; i < newLen; i++) {
                            members.set(i, Core.intersect(members.get(i), lt.rest));
                        }
                    }
                    rest = Core.intersect(rest, lt.rest);
                }
            }
            for (var m : members) {
                if (Core.isEmpty(cx, m)) {
                    return true;
                }
            }
        }
        return !listInhabited(cx, members, rest, neg);
    }

    // This function returns true if there is a list shape v such that
// is in the type described by `members` and `rest`, and
// for each tuple t in `neg`, v is not in t.
// `neg` represents a set of negated list types.
// Precondition is that each of `members` is not empty.
// This is formula Phi' in section 7.3.1 of Alain Frisch's PhD thesis,
// generalized to tuples of arbitrary length.
    static boolean listInhabited(Context cx, List<SemType> members, SemType rest, Conjunction neg) {
        if (neg == null) {
            return true;
        } else {
            int len = members.size();
            ListAtomicType nt = cx.listAtomType(neg.atom);
            int negLen = nt.members.length;
            if (len < negLen) {
                if (Core.isNever(rest)) {
                    return listInhabited(cx, members, rest, neg.next);
                }
                for (int i = len; i < negLen; i++) {
                    members.add(rest);
                }
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
                SemType ntm = i < negLen ? nt.members[i] : nt.rest;
                SemType d = Core.diff(members.get(i), ntm);
                if (!Core.isEmpty(cx, d)) {
                    List<SemType> s = Common.shallowCopyTypes(members);
                    s.set(i, d);
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
}
