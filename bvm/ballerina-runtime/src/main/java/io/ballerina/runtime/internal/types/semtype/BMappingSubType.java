/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
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
 *
 */

package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.Bdd;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.Conjunction;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.Core;
import io.ballerina.runtime.api.types.semtype.FieldPair;
import io.ballerina.runtime.api.types.semtype.FieldPairs;
import io.ballerina.runtime.api.types.semtype.MappingAtomicType;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.api.types.semtype.SubType;

import java.util.Arrays;
import java.util.Objects;

import static io.ballerina.runtime.api.types.semtype.Bdd.bddEvery;

public class BMappingSubType extends SubType implements DelegatedSubType {

    public final Bdd inner;

    private BMappingSubType(Bdd inner) {
        super(inner.isAll(), inner.isNothing());
        this.inner = inner;
    }

    public static BMappingSubType createDelegate(SubType inner) {
        if (inner instanceof Bdd bdd) {
            return new BMappingSubType(bdd);
        } else if (inner.isAll() || inner.isNothing()) {
            throw new IllegalStateException("unimplemented");
        } else if (inner instanceof BMappingSubType bMapping) {
            return new BMappingSubType(bMapping.inner);
        }
        throw new IllegalArgumentException("Unexpected inner type");
    }

    @Override
    public Bdd inner() {
        return inner;
    }

    @Override
    public SubType union(SubType other) {
        if (!(other instanceof BMappingSubType otherList)) {
            throw new IllegalArgumentException("union of different subtypes");
        }
        return createDelegate(inner.union(otherList.inner));
    }

    @Override
    public SubType intersect(SubType other) {
        if (!(other instanceof BMappingSubType otherList)) {
            throw new IllegalArgumentException("intersect of different subtypes");
        }
        return createDelegate(inner.intersect(otherList.inner));
    }

    @Override
    public SubType complement() {
        return createDelegate(inner.complement());
    }

    @Override
    public boolean isEmpty(Context cx) {
        return cx.memoSubtypeIsEmpty(cx.mappingMemo,
                (context, bdd) -> bddEvery(context, bdd, null, null, BMappingSubType::mappingFormulaIsEmpty), inner);
    }

    static boolean mappingFormulaIsEmpty(Context cx, Conjunction posList, Conjunction negList) {
        MappingAtomicType combined;
        if (posList == null) {
            combined = Builder.mappingAtomicInner();
        } else {
            // combine all the positive atoms using intersection
            combined = cx.mappingAtomType(posList.atom());
            Conjunction p = posList.next();
            while (true) {
                if (p == null) {
                    break;
                } else {
                    MappingAtomicType m =
                            combined.intersectMapping(cx.env, cx.mappingAtomType(p.atom()));
                    if (m == null) {
                        return true;
                    } else {
                        combined = m;
                    }
                    p = p.next();
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
            MappingAtomicType neg = cx.mappingAtomType(negList.atom());

            if (!Core.isEmpty(cx, Core.diff(pos.rest(), neg.rest()))) {
                return mappingInhabited(cx, pos, negList.next());
            }
            for (FieldPair fieldPair : new FieldPairs(pos, neg)) {
                SemType intersect = Core.intersect(fieldPair.type1(), fieldPair.type2());
                // if types of at least one field are disjoint, the neg atom will not contribute to the next iteration.
                // Therefore, we can skip the current neg atom.
                // i.e. if we have isEmpty(T1 & S1) or isEmpty(T2 & S2) then,
                // record { T1 f1; T2 f2; } / record { S1 f1; S2 f2; } = record { T1 f1; T2 f2; }
                if (Core.isEmpty(cx, intersect)) {
                    return mappingInhabited(cx, pos, negList.next());
                }

                SemType d = Core.diff(fieldPair.type1(), fieldPair.type2());
                if (!Core.isEmpty(cx, d)) {
                    MappingAtomicType mt;
                    if (fieldPair.index1() == null) {
                        // the posType came from the rest type
                        mt = insertField(pos, fieldPair.name(), d);
                    } else {
                        SemType[] posTypes = pos.types().clone();
                        posTypes[fieldPair.index1()] = d;
                        mt = new MappingAtomicType(pos.names(), posTypes, pos.rest());
                    }
                    if (mappingInhabited(cx, mt, negList.next())) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private static MappingAtomicType insertField(MappingAtomicType m, String name, SemType t) {
        String[] orgNames = m.names();
        String[] names = shallowCopyStrings(orgNames, orgNames.length + 1);
        SemType[] orgTypes = m.types();
        SemType[] types = shallowCopySemTypes(orgTypes, orgTypes.length + 1);
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
        return new MappingAtomicType(names, types, m.rest());
    }

    static SemType[] shallowCopySemTypes(SemType[] v, int newLength) {
        return Arrays.copyOf(v, newLength);
    }

    private static String[] shallowCopyStrings(String[] v, int newLength) {
        return Arrays.copyOf(v, newLength);
    }

    @Override
    public SubTypeData data() {
        return inner();
    }

    @Override
    public SubType diff(SubType other) {
        if (!(other instanceof BMappingSubType otherList)) {
            throw new IllegalArgumentException("diff of different subtypes");
        }
        return createDelegate(inner.diff(otherList.inner));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BMappingSubType that)) {
            return false;
        }
        return Objects.equals(inner, that.inner);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(inner);
    }

}
