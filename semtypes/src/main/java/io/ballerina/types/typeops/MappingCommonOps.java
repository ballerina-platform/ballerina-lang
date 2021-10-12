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

import io.ballerina.types.Bdd;
import io.ballerina.types.BddMemo;
import io.ballerina.types.Common;
import io.ballerina.types.Conjunction;
import io.ballerina.types.Context;
import io.ballerina.types.Core;
import io.ballerina.types.MappingAtomicType;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.SubtypeData;
import io.ballerina.types.UniformTypeOps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Common mapping related methods operate on SubtypeData.
 *
 * @since 3.0.0
 */
public abstract class MappingCommonOps extends CommonOps implements UniformTypeOps {


    // This works the same as the tuple case, except that instead of
    // just comparing the lengths of the tuples we compare the sorted list of field names
    public static boolean mappingFormulaIsEmpty(Context cx, Conjunction posList, Conjunction negList) {
        MappingAtomicType combined;
        if (posList == null) {
            combined = MappingAtomicType.from(new String[0], new SemType[0],
                    // This isn't right for the readonly case.
                    // bddFixReadOnly avoids this
                    PredefinedType.TOP);
        } else {
            // combine all the positive atoms using intersection
            combined = cx.mappingAtomType(posList.atom);
            Conjunction p = posList.next;
            while (true) {
                if (p == null) {
                    break;
                } else {
                    MappingAtomicType m = intersectMapping(combined, cx.mappingAtomType(p.atom));
                    if (m == null) {
                        return true;
                    } else {
                        combined = m;
                    }
                    p = p.next;
                }
            }
           for (SemType t : combined.types) {
                if (Core.isEmpty(cx, t)) {
                    return true;
                }
            }

        }
        return !mappingInhabited(cx, combined, negList);
    }

    private static boolean mappingInhabited(Context cx, MappingAtomicType pos, Conjunction negList) {
        if (negList == null) {
            return true;
        } else {
            MappingAtomicType neg = cx.mappingAtomType(negList.atom);

            FieldPairs pairing;

            if (!Arrays.equals(pos.names, neg.names)) {
                // If this negative type has required fields that the positive one does not allow
                // or vice-versa, then this negative type has no effect,
                // so we can move on to the next one

                // Deal the easy case of two closed records fast.
                if (Core.isNever(pos.rest) && Core.isNever(neg.rest)) {
                    return mappingInhabited(cx, pos, negList.next);
                }
                pairing = new FieldPairs(pos, neg);
                for (FieldPair fieldPair : pairing) {
                    if (Core.isNever(fieldPair.type1) || Core.isNever(fieldPair.type2)) {
                        return mappingInhabited(cx, pos, negList.next);
                    }
                }
                pairing.itr.reset();
            } else {
                pairing = new FieldPairs(pos, neg);
            }

            if (!Core.isEmpty(cx, Core.diff(pos.rest, neg.rest))) {
                return true;
            }
            for (FieldPair fieldPair : pairing) {
                SemType d = Core.diff(fieldPair.type1, fieldPair.type2);
                if (!Core.isEmpty(cx, d)) {
                    MappingAtomicType mt;
                    Optional<Integer> i = pairing.itr.index1(fieldPair.name);
                    if (i.isEmpty()) {
                        // the posType came from the rest type
                        mt = insertField(pos, fieldPair.name, d);
                    } else {
                        SemType[] posTypes = Common.shallowCopyTypes(pos.types);
                        posTypes[i.get()] = d;
                        mt = MappingAtomicType.from(pos.names, posTypes, pos.rest);
                    }
                    if (mappingInhabited(cx, mt, negList.next)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private static MappingAtomicType insertField(MappingAtomicType m, String name, SemType t) {
        String[] names = Common.shallowCopyStrings(m.names);
        SemType[] types = Common.shallowCopyTypes(m.types);
        int i = names.length;
        while (true) {
            if (i == 0 || Objects.equals(name, names[i - 1]) || Common.codePointCompare(name, names[i - 1])) {
                names[i] = name;
                types[i] = t;
                break;
            }
            names[i] = names[i - 1];
            types[i] = types[i - 1];
            i -= 1;
        }
        return MappingAtomicType.from(names, types, m.rest);
    }

    private static MappingAtomicType intersectMapping(MappingAtomicType m1, MappingAtomicType m2) {
        List<String> names = new ArrayList<>();
        List<SemType> types = new ArrayList<>();
        FieldPairs pairing = new FieldPairs(m1, m2);
        for (FieldPair fieldPair : pairing) {
            names.add(fieldPair.name);
            SemType t = Core.intersect(fieldPair.type1, fieldPair.type2);
            if (Core.isNever(t)) {
                return null;
            }
            types.add(t);
        }
        SemType rest = Core.intersect(m1.rest, m2.rest);
        return MappingAtomicType.from(names.toArray(new String[]{}), types.toArray(new SemType[]{}), rest);
    }

    public static boolean mappingSubtypeIsEmpty(Context cx, SubtypeData t) {
        Bdd b = (Bdd) t;
        BddMemo mm = cx.mappingMemo.get(b);
        BddMemo m;
        if (mm == null) {
            m = BddMemo.from(b);
            cx.mappingMemo.put(b, m);
        } else {
            m = mm;
            BddMemo.MemoStatus res = m.isEmpty;
            switch (res) {
                case NOT_SET:
                    // we've got a loop
                    return true;
                case TRUE:
                    return true;
                case FALSE:
                    return false;
            }
        }
        boolean isEmpty = Common.bddEvery(cx, b, null, null, MappingCommonOps::mappingFormulaIsEmpty);
        m.setIsEmpty(isEmpty);
        return isEmpty;
    }
}
