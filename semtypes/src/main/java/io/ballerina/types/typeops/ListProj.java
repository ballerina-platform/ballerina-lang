/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.types.ComplexSemType;
import io.ballerina.types.Conjunction;
import io.ballerina.types.Context;
import io.ballerina.types.Core;
import io.ballerina.types.FixedLengthArray;
import io.ballerina.types.ListAtomicType;
import io.ballerina.types.ListConjunction;
import io.ballerina.types.SemType;
import io.ballerina.types.SubtypeData;
import io.ballerina.types.UniformTypeBitSet;
import io.ballerina.types.subtypedata.BddAllOrNothing;
import io.ballerina.types.subtypedata.BddNode;

import static io.ballerina.types.Common.isListBitsSet;
import static io.ballerina.types.Common.isNothingSubtype;
import static io.ballerina.types.Conjunction.and;
import static io.ballerina.types.Core.diff;
import static io.ballerina.types.Core.getComplexSubtypeData;
import static io.ballerina.types.Core.isEmpty;
import static io.ballerina.types.Core.isNever;
import static io.ballerina.types.Core.union;
import static io.ballerina.types.PredefinedType.NEVER;
import static io.ballerina.types.PredefinedType.TOP;
import static io.ballerina.types.UniformTypeCode.UT_LIST_RO;
import static io.ballerina.types.UniformTypeCode.UT_LIST_RW;
import static io.ballerina.types.typeops.ListCommonOps.fixedArrayAnyEmpty;
import static io.ballerina.types.typeops.ListCommonOps.fixedArrayFill;
import static io.ballerina.types.typeops.ListCommonOps.fixedArrayReplace;
import static io.ballerina.types.typeops.ListCommonOps.fixedArrayShallowCopy;
import static io.ballerina.types.typeops.ListCommonOps.listAtomicMemberTypeAt;
import static io.ballerina.types.typeops.ListCommonOps.listConjunction;
import static io.ballerina.types.typeops.ListCommonOps.listIntersectWith;
import static io.ballerina.types.typeops.ListCommonOps.listMemberAt;

/**
 * Class to hold functions ported from `listProj.bal` file.
 *
 * @since 3.0.0
 */
public class ListProj {
    // Untested full implementation of list projection.

    // Based on listMemberType
    public static SemType listProj(Context cx, SemType t, SemType k) {
        if (t instanceof UniformTypeBitSet) {
            return isListBitsSet((UniformTypeBitSet) t) ? TOP : NEVER;
        } else {
            SubtypeData keyData = Core.intSubtype(k);
            if (isNothingSubtype(keyData)) {
                return NEVER;
            }
            return union(listProjBdd(cx,
                                     keyData,
                                     (Bdd) getComplexSubtypeData((ComplexSemType) t, UT_LIST_RO),
                                     null, null),
                         listProjBdd(cx,
                                     keyData,
                                     (Bdd) getComplexSubtypeData((ComplexSemType) t, UT_LIST_RW),
                                     null, null));
        }
    }

    // Based on bddEvery
    static SemType listProjBdd(Context cx, SubtypeData k, Bdd b, Conjunction pos, Conjunction neg) {
        if (b instanceof BddAllOrNothing) {
            return ((BddAllOrNothing) b).isAll() ? listProjPath(cx, k, pos, neg) : NEVER;
        } else {
            BddNode bddNode = (BddNode) b;
            return union(listProjBdd(cx, k, bddNode.left, and(bddNode.atom, pos), neg),
                         union(listProjBdd(cx, k, bddNode.middle, pos, neg),
                               listProjBdd(cx, k, bddNode.right, pos, and(bddNode.atom, neg))));
        }
    }

    // Based on listFormulaIsEmpty
    static SemType listProjPath(Context cx, SubtypeData k, Conjunction pos, Conjunction neg) {
        FixedLengthArray members;
        SemType rest;
        if (pos == null) {
            members = FixedLengthArray.empty();
            rest = TOP;
        } else {
            // combine all the positive tuples using intersection
            ListAtomicType lt = cx.listAtomType(pos.atom);
            members = lt.members;
            rest = lt.rest;
            Conjunction p = pos.next;
            // the neg case is in case we grow the array in listInhabited
            if (p != null || neg != null) {
                members = fixedArrayShallowCopy(members);
            }

            while (true) {
                if (p == null) {
                    break;
                } else {
                    Atom d = p.atom;
                    p = p.next;
                    lt = cx.listAtomType(d);
                    ListCommonOps.FixedLengthArraySemtypePair intersected = listIntersectWith(members, rest, lt);
                    if (intersected == null) {
                        return NEVER;
                    }
                    members = intersected.members;
                    rest = intersected.rest;
                }
            }
            if (fixedArrayAnyEmpty(cx, members)) {
                return NEVER;
            }
            // Ensure that we can use isNever on rest in listInhabited
            if (rest != NEVER && isEmpty(cx, rest)) {
                rest = NEVER;
            }
        }
        return listProjExclude(cx, k, members, rest, listConjunction(cx, neg));
    }

    // Precondition k >= 0 and members[i] not empty for all i
    // This finds the projection of e[k], excluding the list of atoms in neg
    // when the type of e is given by members and rest.
    // Based on listInhabited
    // Corresponds to phi^x in AMK tutorial generalized for list types.
    static SemType listProjExclude(Context cx, SubtypeData k, FixedLengthArray members, SemType rest,
                                   ListConjunction neg) {
        if (neg == null) {
            return listAtomicMemberTypeAt(members, rest, k);
        } else {
            int len = members.fixedLength;
            ListAtomicType nt = neg.listType;
            int negLen = nt.members.fixedLength;
            if (len < negLen) {
                if (isNever(rest)) {
                    return listProjExclude(cx, k, members, rest, neg.next);
                }
                fixedArrayFill(members, negLen, rest);
            } else if (negLen < len && isNever(nt.rest)) {
                return listProjExclude(cx, k, members, rest, neg.next);
            }
            // now we have nt.members.length() <= len
            SemType p = NEVER;
            for (int i = 0; i < Integer.max(members.initial.size(), neg.maxInitialLen); i++) {
                SemType d = diff(listMemberAt(members, rest, i), listMemberAt(nt.members, nt.rest, i));
                if (!isEmpty(cx, d)) {
                    FixedLengthArray s = fixedArrayReplace(members, i, d, rest);
                    p = union(p, listProjExclude(cx, k, s, rest, neg.next));
                }
            }
            SemType rd = diff(rest, nt.rest);
            if (!isEmpty(cx, rd)) {
                p = union(p, listProjExclude(cx, k, members, rd, neg.next));
            }
            return p;
        }
    }
}
