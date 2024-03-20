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
package io.ballerina.types;

import io.ballerina.types.subtypedata.AllOrNothingSubtype;
import io.ballerina.types.subtypedata.BddAllOrNothing;
import io.ballerina.types.subtypedata.BddNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import static io.ballerina.types.Conjunction.and;
import static io.ballerina.types.typeops.BddCommonOps.bddComplement;
import static io.ballerina.types.typeops.BddCommonOps.bddDiff;
import static io.ballerina.types.typeops.BddCommonOps.bddIntersect;
import static io.ballerina.types.typeops.BddCommonOps.bddUnion;

/**
 * Code common to implementation of multiple basic types.
 *
 * @since 2201.8.0
 */
public class Common {

    // [from nballerina] A Bdd represents a disjunction of conjunctions of atoms, where each atom is either positive or
    // negative (negated). Each path from the root to a leaf that is true represents one of the conjunctions
    // We walk the tree, accumulating the positive and negative conjunctions for a path as we go.
    // When we get to a leaf that is true, we apply the predicate to the accumulated conjunctions.

    public static boolean bddEvery(Context cx,
                                   Bdd b,
                                   Conjunction pos,
                                   Conjunction neg,
                                   BddPredicate predicate) {
        if (b instanceof BddAllOrNothing allOrNothing) {
            return !allOrNothing.isAll() || predicate.apply(cx, pos, neg);
        } else {
            BddNode bn = (BddNode) b;
            return bddEvery(cx, bn.left, and(bn.atom, pos), neg, predicate)
                    && bddEvery(cx, bn.middle, pos, neg, predicate)
                    && bddEvery(cx, bn.right, pos, and(bn.atom, neg), predicate);
        }
    }

    public static boolean bddEveryPositive(Context cx, Bdd b, Conjunction pos, Conjunction neg,
                                           BddPredicate predicate) {
        if (b instanceof BddAllOrNothing allOrNothing) {
            return !allOrNothing.isAll() || predicate.apply(cx, pos, neg);
        } else {
            BddNode bn = (BddNode) b;
            return bddEveryPositive(cx, bn.left, andIfPositive(bn.atom, pos), neg, predicate)
                    && bddEveryPositive(cx, bn.middle, pos, neg, predicate)
                    && bddEveryPositive(cx, bn.right, pos, andIfPositive(bn.atom, neg), predicate);
        }
    }

    public static Conjunction andIfPositive(Atom atom, Conjunction next) {
        if (atom instanceof RecAtom recAtom && recAtom.index < 0) {
            return next;
        }
        return and(atom, next);
    }

    public static SubtypeData bddSubtypeUnion(SubtypeData t1, SubtypeData t2) {
        return bddUnion((Bdd) t1, (Bdd) t2);
    }

    public static SubtypeData bddSubtypeIntersect(SubtypeData t1, SubtypeData t2) {
        return bddIntersect((Bdd) t1, (Bdd) t2);
    }

    public static SubtypeData bddSubtypeDiff(SubtypeData t1, SubtypeData t2) {
        return bddDiff((Bdd) t1, (Bdd) t2);
    }

    public static SubtypeData bddSubtypeComplement(SubtypeData t) {
        return bddComplement((Bdd) t);
    }

    public static SemType[] shallowCopyTypes(SemType[] v) {
        return Arrays.copyOf(v, v.length);
    }

    public static CellSemType[] shallowCopyCellTypes(CellSemType[] v) {
        return shallowCopyCellTypes(v, v.length);
    }

    public static CellSemType[] shallowCopyCellTypes(CellSemType[] v, int newLength) {
        return Arrays.copyOf(v, newLength);
    }

    public static List<SemType> shallowCopyTypes(List<SemType> v) {
        return new ArrayList<>(v);
    }

    public static String[] shallowCopyStrings(String[] v, int newLength) {
        return Arrays.copyOf(v, newLength);
    }

    public static boolean notIsEmpty(Context cx, SubtypeData d) {
        return false;
    }

    // Returns whether s1.codePoints < s2.codePoints
    public static boolean codePointCompare(String s1, String s2) {
        if (s1.equals(s2)) {
            return false;
        }
        int len1 = s1.length();
        int len2 = s2.length();
        if (len1 < len2 && s2.substring(0, len1).equals(s1)) {
            return true;
        }
        int cpCount1 = s1.codePointCount(0, len1);
        int cpCount2 = s2.codePointCount(0, len2);
        for (int cp = 0; cp < cpCount1 && cp < cpCount2;) {
            int codepoint1 = s1.codePointAt(cp);
            int codepoint2 = s2.codePointAt(cp);
            if (codepoint1 == codepoint2) {
                cp++;
                continue;
            }
            return codepoint1 < codepoint2;
        }
        return false;
    }


    public static boolean isNothingSubtype(SubtypeData data) {
        return data instanceof AllOrNothingSubtype allOrNothingSubtype && allOrNothingSubtype.isNothingSubtype();
    }

    /**
     * Function interface used for method references.
     *
     * @since 3.0.0
     */
    public interface BddPredicate {
        boolean apply(Context cx, Conjunction posList, Conjunction negList);
    }

    public interface BddIsEmptyPredicate extends BiFunction<Context, Bdd, Boolean> {

    }

    public static boolean memoSubtypeIsEmpty(Context cx, Map<Bdd, BddMemo> memoTable,
                                             BddIsEmptyPredicate isEmptyPredicate, Bdd b) {
        BddMemo mm = memoTable.get(b);
        BddMemo m;
        if (mm != null) {
            BddMemo.MemoStatus res = mm.isEmpty;
            switch (res) {
                case CYCLIC:
                    // Since we define types inductively we consider these to be empty
                    return true;
                case TRUE, FALSE:
                    // We know whether b is empty or not for certain
                    return res == BddMemo.MemoStatus.TRUE;
                case NULL:
                    // this is same as not having memo so fall through
                    m = mm;
                    break;
                case LOOP, PROVISIONAL:
                    // We've got a loop.
                    mm.isEmpty = BddMemo.MemoStatus.LOOP;
                    return true;
                default:
                    throw new AssertionError("Unexpected memo status: " + res);
            }
        } else {
            m = BddMemo.from(b);
            cx.listMemo.put(b, m);
        }
        m.isEmpty = BddMemo.MemoStatus.PROVISIONAL;
        int initStackDepth = cx.memoStack.size();
        cx.memoStack.add(m);
        boolean isEmpty = isEmptyPredicate.apply(cx, b);
        boolean isLoop = m.isEmpty == BddMemo.MemoStatus.LOOP;
        if (!isEmpty || initStackDepth == 0) {
            for (int i = initStackDepth + 1; i < cx.memoStack.size(); i++) {
                BddMemo.MemoStatus memoStatus = cx.memoStack.get(i).isEmpty;
                if (Objects.requireNonNull(memoStatus) == BddMemo.MemoStatus.PROVISIONAL ||
                        memoStatus == BddMemo.MemoStatus.LOOP || memoStatus == BddMemo.MemoStatus.CYCLIC) {
                    cx.memoStack.get(i).isEmpty = isEmpty ? BddMemo.MemoStatus.TRUE : BddMemo.MemoStatus.NULL;
                }
            }
            // TODO: think of a more efficient way to do this
            while (cx.memoStack.size() > initStackDepth) {
                cx.memoStack.remove(cx.memoStack.size() - 1);
            }
            // The only way that we have found that this can be empty is by going through a loop.
            // This means that the shapes in the type would all be infinite.
            // But we define types inductively, which means we only consider finite shapes.
            if (isLoop && isEmpty) {
                m.isEmpty = BddMemo.MemoStatus.CYCLIC;
            } else {
                m.isEmpty = isEmpty ? BddMemo.MemoStatus.TRUE : BddMemo.MemoStatus.FALSE;
            }
        }
        return isEmpty;
    }

    public static boolean isAllSubtype(SubtypeData d) {
        if (d instanceof AllOrNothingSubtype allOrNothingSubtype) {
            return allOrNothingSubtype.isAllSubtype();
        }
        return false;
    }
}
