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
import io.ballerina.runtime.api.types.semtype.BasicTypeCode;
import io.ballerina.runtime.api.types.semtype.Bdd;
import io.ballerina.runtime.api.types.semtype.BddAllOrNothing;
import io.ballerina.runtime.api.types.semtype.BddNode;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.Conjunction;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.Core;
import io.ballerina.runtime.api.types.semtype.ListAtomicType;
import io.ballerina.runtime.api.types.semtype.Pair;
import io.ballerina.runtime.api.types.semtype.SemType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static io.ballerina.runtime.api.types.semtype.Builder.cellContaining;
import static io.ballerina.runtime.api.types.semtype.Builder.roCellContaining;
import static io.ballerina.runtime.api.types.semtype.Conjunction.and;
import static io.ballerina.runtime.api.types.semtype.Core.cellInnerVal;
import static io.ballerina.runtime.api.types.semtype.Core.diff;
import static io.ballerina.runtime.api.types.semtype.Core.getComplexSubtypeData;
import static io.ballerina.runtime.api.types.semtype.Core.isEmpty;
import static io.ballerina.runtime.api.types.semtype.Core.isNever;
import static io.ballerina.runtime.api.types.semtype.Core.isNothingSubtype;
import static io.ballerina.runtime.api.types.semtype.Core.union;
import static io.ballerina.runtime.internal.types.semtype.BIntSubType.intSubtypeContains;
import static io.ballerina.runtime.internal.types.semtype.BListSubType.fixedArrayAnyEmpty;
import static io.ballerina.runtime.internal.types.semtype.BListSubType.listIntersectWith;
import static io.ballerina.runtime.internal.types.semtype.BListSubType.listMemberAtInnerVal;
import static io.ballerina.runtime.internal.types.semtype.BListSubType.listSampleTypes;
import static io.ballerina.runtime.internal.types.semtype.BListSubType.listSamples;

/**
 * utility class for list type projection.
 *
 * @since 2201.10.0
 */
public final class BListProj {

    private BListProj() {
    }

    public static SemType listProjInnerVal(Context cx, SemType t, SemType k) {
        if (t.some == 0) {
            return t == Builder.listType() ? Builder.valType() : Builder.neverType();
        } else {
            SubTypeData keyData = Core.intSubtype(k);
            if (isNothingSubtype(keyData)) {
                return Builder.neverType();
            }
            return listProjBddInnerVal(cx, keyData, (Bdd) getComplexSubtypeData(t, BasicTypeCode.BT_LIST), null,
                    null);
        }
    }

    private static SemType listProjBddInnerVal(Context cx, SubTypeData k, Bdd b, Conjunction pos, Conjunction neg) {
        if (b instanceof BddAllOrNothing allOrNothing) {
            return allOrNothing.isAll() ? listProjPathInnerVal(cx, k, pos, neg) : Builder.neverType();
        } else {
            BddNode bddNode = (BddNode) b;
            return union(listProjBddInnerVal(cx, k, bddNode.left(), and(bddNode.atom(), pos), neg),
                    union(listProjBddInnerVal(cx, k, bddNode.middle(), pos, neg),
                            listProjBddInnerVal(cx, k, bddNode.right(), pos, and(bddNode.atom(), neg))));
        }
    }

    private static SemType listProjPathInnerVal(Context cx, SubTypeData k, Conjunction pos, Conjunction neg) {
        FixedLengthArray members;
        SemType rest;
        if (pos == null) {
            members = FixedLengthArray.empty();
            rest = cellContaining(cx.env, union(Builder.valType(), Builder.undef()));
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
                        return Builder.neverType();
                    }
                    members = intersected.first();
                    rest = intersected.second();
                }
            }
            if (fixedArrayAnyEmpty(cx, members)) {
                return Builder.neverType();
            }
            // Ensure that we can use isNever on rest in listInhabited
            if (!isNever(cellInnerVal(rest)) && isEmpty(cx, rest)) {
                rest = roCellContaining(cx.env, Builder.neverType());
            }
        }
        Integer[] indices = listSamples(cx, members, rest, neg);
        Pair<Integer[], Integer[]> projSamples = listProjSamples(indices, k);
        indices = projSamples.first();
        Pair<SemType[], Integer> sampleTypes = listSampleTypes(cx, members, rest, indices);
        return listProjExcludeInnerVal(cx, projSamples.first(),
                projSamples.second(),
                sampleTypes.first(),
                sampleTypes.second(), neg);
    }

    private static SemType listProjExcludeInnerVal(Context cx, Integer[] indices, Integer[] keyIndices,
                                                   SemType[] memberTypes, int nRequired, Conjunction neg) {
        SemType p = Builder.neverType();
        if (neg == null) {
            int len = memberTypes.length;
            for (int k : keyIndices) {
                if (k < len) {
                    p = union(p, cellInnerVal(memberTypes[k]));
                }
            }
        } else {
            final ListAtomicType nt = cx.listAtomType(neg.atom());
            if (nRequired > 0 && isNever(listMemberAtInnerVal(nt.members(), nt.rest(), indices[nRequired - 1]))) {
                return listProjExcludeInnerVal(cx, indices, keyIndices, memberTypes, nRequired, neg.next());
            }
            int negLen = nt.members().fixedLength();
            if (negLen > 0) {
                int len = memberTypes.length;
                if (len < indices.length && indices[len] < negLen) {
                    return listProjExcludeInnerVal(cx, indices, keyIndices, memberTypes, nRequired, neg.next());
                }
                for (int i = nRequired; i < memberTypes.length; i++) {
                    if (indices[i] >= negLen) {
                        break;
                    }
                    SemType[] t = Arrays.copyOfRange(memberTypes, 0, i);
                    p = union(p, listProjExcludeInnerVal(cx, indices, keyIndices, t, nRequired, neg.next()));
                }
            }
            for (int i = 0; i < memberTypes.length; i++) {
                SemType d =
                        diff(cellInnerVal(memberTypes[i]), listMemberAtInnerVal(nt.members(), nt.rest(), indices[i]));
                if (!Core.isEmpty(cx, d)) {
                    SemType[] t = memberTypes.clone();
                    t[i] = cellContaining(cx.env, d);
                    // We need to make index i be required
                    p = union(p, listProjExcludeInnerVal(cx, indices, keyIndices, t, Integer.max(nRequired, i + 1),
                            neg.next()));
                }
            }
        }
        return p;
    }

    private static Pair<Integer[], Integer[]> listProjSamples(Integer[] indices, SubTypeData k) {
        List<Pair<Integer, Boolean>> v = new ArrayList<>();
        for (int i : indices) {
            v.add(Pair.from(i, intSubtypeContains(k, i)));
        }
        if (k instanceof BIntSubType.IntSubTypeData intSubtype) {
            for (BIntSubType.Range range : intSubtype.ranges) {
                long max = range.max();
                if (range.max() >= 0) {
                    v.add(Pair.from((int) max, true));
                    int min = Integer.max(0, (int) range.min());
                    if (min < max) {
                        v.add(Pair.from(min, true));
                    }
                }
            }
        }
        v.sort(Comparator.comparingInt(Pair::first));
        List<Integer> indices1 = new ArrayList<>();
        List<Integer> keyIndices = new ArrayList<>();
        for (var ib : v) {
            if (indices1.isEmpty() || !Objects.equals(ib.first(), indices1.get(indices1.size() - 1))) {
                if (ib.second()) {
                    keyIndices.add(indices1.size());
                }
                indices1.add(ib.first());
            }
        }
        return Pair.from(indices1.toArray(Integer[]::new), keyIndices.toArray(Integer[]::new));
    }
}
