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
import io.ballerina.runtime.api.types.semtype.Conjunction;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.Core;
import io.ballerina.runtime.api.types.semtype.Env;
import io.ballerina.runtime.api.types.semtype.MappingAtomicType;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.api.types.semtype.SubType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.runtime.api.types.semtype.Bdd.bddEvery;
import static io.ballerina.runtime.api.types.semtype.Builder.MAPPING_ATOMIC_INNER;
import static io.ballerina.runtime.api.types.semtype.Core.cellInner;
import static io.ballerina.runtime.api.types.semtype.Core.intersectMemberSemTypes;
import static io.ballerina.runtime.api.types.semtype.Core.isNever;

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

    // FIXME: not sure this is needed (java string comparision should support unicode)
    static boolean codePointCompare(String s1, String s2) {
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
        for (int cp = 0; cp < cpCount1 && cp < cpCount2; ) {
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

    private static boolean mappingFormulaIsEmpty(Context cx, Conjunction posList, Conjunction negList) {
        MappingAtomicType combined;
        if (posList == null) {
            combined = MAPPING_ATOMIC_INNER;
        } else {
            // combine all the positive atoms using intersection
            combined = cx.mappingAtomType(posList.atom());
            Conjunction p = posList.next();
            while (true) {
                if (p == null) {
                    break;
                } else {
                    MappingAtomicType m = intersectMapping(cx.env, combined, cx.mappingAtomType(p.atom()));
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

            FieldPairs pairing = new FieldPairs(pos, neg);
            if (!Core.isEmpty(cx, Core.diff(pos.rest(), neg.rest()))) {
                return mappingInhabited(cx, pos, negList.next());
            }
            for (FieldPair fieldPair : pairing) {
                SemType d = Core.diff(fieldPair.type1(), fieldPair.type2());
                if (!Core.isEmpty(cx, d)) {
                    MappingAtomicType mt;
                    if (fieldPair.index1() == null) {
                        // the posType came from the rest type
                        mt = insertField(pos, fieldPair.name(), d);
                    } else {
                        SemType[] posTypes = pos.types();
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
            if (i == 0 || codePointCompare(names[i - 1], name)) {
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

    // FIXME: make this an instance method in mappingAtomicType
    private static MappingAtomicType intersectMapping(Env env, MappingAtomicType m1, MappingAtomicType m2) {
        int expectedSize = Integer.min(m1.types().length, m2.types().length);
        List<String> names = new ArrayList<>(expectedSize);
        List<SemType> types = new ArrayList<>(expectedSize);
        FieldPairs pairing = new FieldPairs(m1, m2);
        for (FieldPair fieldPair : pairing) {
            names.add(fieldPair.name());
            SemType t = intersectMemberSemTypes(env, fieldPair.type1(), fieldPair.type2());
            if (isNever(cellInner(fieldPair.type1()))) {
                return null;
            }
            types.add(t);
        }
        SemType rest = intersectMemberSemTypes(env, m1.rest(), m2.rest());
        return new MappingAtomicType(names.toArray(String[]::new), types.toArray(SemType[]::new), rest);
    }

    @Override
    public SubTypeData data() {
        throw new IllegalStateException("unimplemented");
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

    private static class FieldPairs implements Iterable<FieldPair> {

        MappingAtomicType m1;
        MappingAtomicType m2;
        public MappingPairIterator itr;

        public FieldPairs(MappingAtomicType m1, MappingAtomicType m2) {
            this.m1 = m1;
            this.m2 = m2;
        }

        @Override
        public Iterator<FieldPair> iterator() {
            itr = new MappingPairIterator(m1, m2);
            return itr;
        }
    }

    private record FieldPair(String name, SemType type1, SemType type2, Integer index1, Integer index2) {

        public static FieldPair create(String name, SemType type1, SemType type2, Integer index1,
                                       Integer index2) {
            return new FieldPair(name, type1, type2, index1, index2);
        }
    }

    // TODO: refact
    private static class MappingPairIterator implements Iterator<FieldPair> {

        private final String[] names1;
        private final String[] names2;
        private final SemType[] types1;
        private final SemType[] types2;
        private final int len1;
        private final int len2;
        private int i1 = 0;
        private int i2 = 0;
        private final SemType rest1;
        private final SemType rest2;

        private boolean doneIteration = false;
        private boolean shouldCalculate = true;
        private FieldPair cache = null;

        private MappingPairIterator(MappingAtomicType m1, MappingAtomicType m2) {
            this.names1 = m1.names();
            this.len1 = this.names1.length;
            this.types1 = m1.types();
            this.rest1 = m1.rest();
            this.names2 = m2.names();
            this.len2 = this.names2.length;
            this.types2 = m2.types();
            this.rest2 = m2.rest();
        }

        @Override
        public boolean hasNext() {
            if (this.doneIteration) {
                return false;
            }
            if (this.shouldCalculate) {
                FieldPair cache = internalNext();
                if (cache == null) {
                    this.doneIteration = true;
                }
                this.cache = cache;
                this.shouldCalculate = false;
            }
            return !this.doneIteration;
        }

        @Override
        public FieldPair next() {
            if (this.doneIteration) {
                throw new NoSuchElementException("Exhausted iterator");
            }

            if (this.shouldCalculate) {
                FieldPair cache = internalNext();
                if (cache == null) {
                    // this.doneIteration = true;
                    throw new IllegalStateException();
                }
                this.cache = cache;
            }
            this.shouldCalculate = true;
            return this.cache;
        }

        /*
         * This method corresponds to `next` method of MappingPairing.
         */
        private FieldPair internalNext() {
            FieldPair p;
            if (this.i1 >= this.len1) {
                if (this.i2 >= this.len2) {
                    return null;
                }
                p = FieldPair.create(curName2(), this.rest1, curType2(), null, this.i2);
                this.i2 += 1;
            } else if (this.i2 >= this.len2) {
                p = FieldPair.create(curName1(), curType1(), this.rest2, this.i1, null);
                this.i1 += 1;
            } else {
                String name1 = curName1();
                String name2 = curName2();
                if (codePointCompare(name1, name2)) {
                    p = FieldPair.create(name1, curType1(), this.rest2, this.i1, null);
                    this.i1 += 1;
                } else if (codePointCompare(name2, name1)) {
                    p = FieldPair.create(name2, this.rest1, curType2(), null, this.i2);
                    this.i2 += 1;
                } else {
                    p = FieldPair.create(name1, curType1(), curType2(), this.i1, this.i2);
                    this.i1 += 1;
                    this.i2 += 1;
                }
            }
            return p;
        }

        private SemType curType1() {
            return this.types1[this.i1];
        }

        private String curName1() {
            return this.names1[this.i1];
        }

        private SemType curType2() {
            return this.types2[this.i2];
        }

        private String curName2() {
            return this.names2[this.i2];
        }

        public void reset() {
            this.i1 = 0;
            this.i2 = 0;
        }

        public Optional<Integer> index1(String name) {
            int i1Prev = this.i1 - 1;
            return i1Prev >= 0 && Objects.equals(this.names1[i1Prev], name) ? Optional.of(i1Prev) : Optional.empty();
        }
    }
}
