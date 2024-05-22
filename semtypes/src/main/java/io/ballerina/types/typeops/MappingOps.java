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

import io.ballerina.types.BasicTypeOps;
import io.ballerina.types.Bdd;
import io.ballerina.types.CellSemType;
import io.ballerina.types.Common;
import io.ballerina.types.Conjunction;
import io.ballerina.types.Context;
import io.ballerina.types.Core;
import io.ballerina.types.Env;
import io.ballerina.types.MappingAtomicType;
import io.ballerina.types.SemType;
import io.ballerina.types.SubtypeData;
import io.ballerina.types.subtypedata.BddAllOrNothing;
import io.ballerina.types.subtypedata.BddNode;
import io.ballerina.types.subtypedata.StringSubtype;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.types.Common.bddSubtypeComplement;
import static io.ballerina.types.Common.bddSubtypeDiff;
import static io.ballerina.types.Common.bddSubtypeIntersect;
import static io.ballerina.types.Common.bddSubtypeUnion;
import static io.ballerina.types.Common.isAllSubtype;
import static io.ballerina.types.Common.memoSubtypeIsEmpty;
import static io.ballerina.types.PredefinedType.MAPPING_ATOMIC_INNER;
import static io.ballerina.types.PredefinedType.NEVER;
import static io.ballerina.types.PredefinedType.UNDEF;
import static io.ballerina.types.typeops.StringOps.stringSubtypeListCoverage;

/**
 * Basic type ops for mapping type.
 *
 * @since 2201.8.0
 */
public class MappingOps extends CommonOps implements BasicTypeOps {
    // This works the same as the tuple case, except that instead of
    // just comparing the lengths of the tuples we compare the sorted list of field names
    public static boolean mappingFormulaIsEmpty(Context cx, Conjunction posList, Conjunction negList) {
        MappingAtomicType combined;
        if (posList == null) {
            combined = MAPPING_ATOMIC_INNER;
        } else {
            // combine all the positive atoms using intersection
            combined = cx.mappingAtomType(posList.atom);
            Conjunction p = posList.next;
            while (true) {
                if (p == null) {
                    break;
                } else {
                    MappingAtomicType m = intersectMapping(cx.env, combined, cx.mappingAtomType(p.atom));
                    if (m == null) {
                        return true;
                    } else {
                        combined = m;
                    }
                    p = p.next;
                }
            }
            for (SemType t : combined.types()) {
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

            FieldPairs pairing = new FieldPairs(pos, neg);
            if (!Core.isEmpty(cx, Core.diff(pos.rest(), neg.rest()))) {
                return mappingInhabited(cx, pos, negList.next);
            }
            for (FieldPair fieldPair : pairing) {
                CellSemType d = (CellSemType) Core.diff(fieldPair.type1(), fieldPair.type2());
                if (!Core.isEmpty(cx, d)) {
                    MappingAtomicType mt;
                    if (fieldPair.index1() == null) {
                        // the posType came from the rest type
                        mt = insertField(pos, fieldPair.name(), d);
                    } else {
                        CellSemType[] posTypes = pos.types();
                        posTypes[fieldPair.index1()] = d;
                        mt = MappingAtomicType.from(pos.names(), posTypes, pos.rest());
                    }
                    if (mappingInhabited(cx, mt, negList.next)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private static MappingAtomicType insertField(MappingAtomicType m, String name, CellSemType t) {
        String[] orgNames = m.names();
        String[] names = Common.shallowCopyStrings(orgNames, orgNames.length + 1);
        CellSemType[] orgTypes = m.types();
        CellSemType[] types = Common.shallowCopyCellTypes(orgTypes, orgTypes.length + 1);
        int i = orgNames.length;
        while (true) {
            if (i == 0 || Common.codePointCompare(names[i - 1], name)) {
                names[i] = name;
                types[i] = t;
                break;
            }
            names[i] = names[i - 1];
            types[i] = types[i - 1];
            i -= 1;
        }
        return MappingAtomicType.from(names, types, m.rest());
    }

    private static MappingAtomicType intersectMapping(Env env, MappingAtomicType m1, MappingAtomicType m2) {
        List<String> names = new ArrayList<>();
        List<CellSemType> types = new ArrayList<>();
        FieldPairs pairing = new FieldPairs(m1, m2);
        for (FieldPair fieldPair : pairing) {
            names.add(fieldPair.name());
            CellSemType t = Core.intersectMemberSemTypes(env, fieldPair.type1(), fieldPair.type2());
            if (Core.isNever(Core.cellInner(fieldPair.type1()))) {
                return null;
            }
            types.add(t);
        }
        CellSemType rest = Core.intersectMemberSemTypes(env, m1.rest(), m2.rest());
        return MappingAtomicType.from(names.toArray(new String[]{}), types.toArray(new CellSemType[]{}), rest);
    }

    public static boolean mappingSubtypeIsEmpty(Context cx, SubtypeData t) {
        return memoSubtypeIsEmpty(cx, cx.mappingMemo,
                (context, bdd) -> Common.bddEvery(context, bdd, null, null, MappingOps::mappingFormulaIsEmpty),
                (Bdd) t);
    }

    public static SemType bddMappingMemberTypeInner(Context cx, Bdd b, SubtypeData key, SemType accum)  {
        if (b instanceof BddAllOrNothing allOrNothing) {
            return allOrNothing.isAll() ? accum : NEVER;
        } else {
            BddNode bdd = (BddNode) b;
            return Core.union(
                    bddMappingMemberTypeInner(cx, bdd.left(), key,
                            Core.intersect(mappingAtomicMemberTypeInner(cx.mappingAtomType(bdd.atom()), key),
                                                        accum)),
                    Core.union(bddMappingMemberTypeInner(cx, bdd.middle(), key, accum),
                            bddMappingMemberTypeInner(cx, bdd.right(), key, accum)));
        }
    }

    static SemType mappingAtomicMemberTypeInner(MappingAtomicType atomic, SubtypeData key) {
        SemType memberType = null;
        for (SemType ty : mappingAtomicApplicableMemberTypesInner(atomic, key)) {
            if (memberType == null) {
                memberType = ty;
            } else {
                memberType = Core.union(memberType, ty);
            }
        }
        return memberType == null ? UNDEF : memberType;
    }

    static List<SemType> mappingAtomicApplicableMemberTypesInner(MappingAtomicType atomic, SubtypeData key) {
        List<SemType> types = new ArrayList<>(atomic.types().length);
        for (CellSemType t : atomic.types()) {
            types.add(Core.cellInner(t));
        }

        List<SemType> memberTypes = new ArrayList<>();
        SemType rest = Core.cellInner(atomic.rest());
        if (isAllSubtype(key)) {
            memberTypes.addAll(types);
            memberTypes.add(rest);
        } else {
            StringSubtype.StringSubtypeListCoverage coverage = stringSubtypeListCoverage((StringSubtype) key,
                    atomic.names());
            for (int index : coverage.indices) {
                memberTypes.add(types.get(index));
            }
            if (!coverage.isSubtype) {
                memberTypes.add(rest);
            }
        }
        return memberTypes;
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
        return mappingSubtypeIsEmpty(cx, d);
    }
}
