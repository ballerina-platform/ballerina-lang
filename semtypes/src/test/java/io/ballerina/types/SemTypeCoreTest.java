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

import io.ballerina.types.definition.FunctionDefinition;
import io.ballerina.types.definition.FunctionQualifiers;
import io.ballerina.types.definition.ListDefinition;
import io.ballerina.types.subtypedata.AllOrNothingSubtype;
import io.ballerina.types.subtypedata.IntSubtype;
import io.ballerina.types.subtypedata.Range;
import io.ballerina.types.subtypedata.StringSubtype;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import static io.ballerina.types.TypeTestUtils.roTuple;
import static io.ballerina.types.TypeTestUtils.tuple;
import static io.ballerina.types.subtypedata.StringSubtype.stringConst;

/**
 * Tests Core functions of Semtypes.
 *
 * @since 2201.8.0
 */
public class SemTypeCoreTest {

    @Test
    public void testSubtypeSimple() {
        Assert.assertTrue(Core.isSubtypeSimple(PredefinedType.NIL, PredefinedType.ANY));
        Assert.assertTrue(Core.isSubtypeSimple(PredefinedType.INT, PredefinedType.VAL));
        Assert.assertTrue(Core.isSubtypeSimple(PredefinedType.ANY, PredefinedType.VAL));
        Assert.assertFalse(Core.isSubtypeSimple(PredefinedType.INT, PredefinedType.BOOLEAN));
        Assert.assertFalse(Core.isSubtypeSimple(PredefinedType.ERROR, PredefinedType.ANY));
    }

    @Test
    public void testSingleNumericType() {
        Assert.assertEquals(Core.singleNumericType(PredefinedType.INT), Optional.of(PredefinedType.INT));
        Assert.assertEquals(Core.singleNumericType(PredefinedType.BOOLEAN), Optional.empty());
        Core.singleNumericType(Core.singleton(1L));
        Assert.assertEquals(Core.singleNumericType(Core.singleton(1L)), Optional.of(PredefinedType.INT));
        Assert.assertEquals(Core.singleNumericType(Core.union(PredefinedType.INT, PredefinedType.FLOAT)),
               Optional.empty());
    }

    @Test
    public void testBitTwiddling() {
        Assert.assertEquals(Long.numberOfTrailingZeros(0x10), 4);
        Assert.assertEquals(Long.numberOfTrailingZeros(0x100), 8);
        Assert.assertEquals(Long.numberOfTrailingZeros(0x1), 0);
        Assert.assertEquals(Long.numberOfTrailingZeros(0x0), 64);
        Assert.assertEquals(Integer.bitCount(0x10000), 1);
        Assert.assertEquals(Integer.bitCount(0), 0);
        Assert.assertEquals(Integer.bitCount(1), 1);
        Assert.assertEquals(Integer.bitCount(0x10010010), 3);
    }

    @Test
    public void test1() {
        Env env = new Env();
        disjoint(Core.typeCheckContext(env), PredefinedType.STRING, PredefinedType.INT);
        disjoint(Core.typeCheckContext(env), PredefinedType.INT, PredefinedType.NIL);
        SemType t1 = createTupleType(env, PredefinedType.INT, PredefinedType.INT);
        disjoint(Core.typeCheckContext(env), t1, PredefinedType.INT);
        SemType t2 = createTupleType(env, PredefinedType.STRING, PredefinedType.STRING);
        disjoint(Core.typeCheckContext(env), PredefinedType.NIL, t2);
    }

    private void disjoint(Context cx, SemType t1, SemType t2) {
        Assert.assertFalse(Core.isSubtype(cx, t1, t2));
        Assert.assertFalse(Core.isSubtype(cx, t2, t1));
        Assert.assertTrue(Core.isEmpty(cx, Core.intersect(t1, t2)));
    }

    @Test
    public void test2() {
        Assert.assertTrue(Core.isSubtype(Core.typeCheckContext(new Env()), PredefinedType.INT, PredefinedType.VAL));
    }

    @Test
    public void test3() {
        Env env = new Env();
        SemType s = roTuple(env, PredefinedType.INT, Core.union(PredefinedType.INT,
                PredefinedType.STRING));
        SemType t = Core.union(roTuple(env, PredefinedType.INT, PredefinedType.INT),
                roTuple(env, PredefinedType.INT, PredefinedType.STRING));
        equiv(env, s, t);
    }

    private void equiv(Env env, SemType s, SemType t) {
        Assert.assertTrue(Core.isSubtype(Core.typeCheckContext(env), s, t));
        Assert.assertTrue(Core.isSubtype(Core.typeCheckContext(env), t, s));
    }

    @Test
    public void test4() {
        Env env = new Env();
        SemType isT = createTupleType(env, PredefinedType.INT, PredefinedType.STRING);
        SemType itT = createTupleType(env, PredefinedType.INT, PredefinedType.VAL);
        SemType tsT = createTupleType(env, PredefinedType.VAL, PredefinedType.STRING);
        SemType iiT = createTupleType(env, PredefinedType.INT, PredefinedType.INT);
        SemType ttT = createTupleType(env, PredefinedType.VAL, PredefinedType.VAL);
        Context cx = Core.typeCheckContext(env);
        Assert.assertTrue(Core.isSubtype(cx, isT, itT));
        Assert.assertTrue(Core.isSubtype(cx, isT, tsT));
        Assert.assertTrue(Core.isSubtype(cx, iiT, ttT));
    }

    @Test
    public void test5() {
        Env env = new Env();
        SemType s = roTuple(env, PredefinedType.INT, Core.union(PredefinedType.NIL,
                Core.union(PredefinedType.INT, PredefinedType.STRING)));
        SemType t = Core.union(roTuple(env, PredefinedType.INT, PredefinedType.INT),
                Core.union(roTuple(env, PredefinedType.INT, PredefinedType.NIL),
                        roTuple(env, PredefinedType.INT, PredefinedType.STRING)));
        equiv(env, s, t);
    }

    @Test
    public void test6() {
        Env env = new Env();
        SemType s = tuple(env, PredefinedType.INT, Core.union(PredefinedType.NIL,
                Core.union(PredefinedType.INT, PredefinedType.STRING)));
        SemType t = Core.union(tuple(env, PredefinedType.INT, PredefinedType.INT),
                Core.union(tuple(env, PredefinedType.INT, PredefinedType.NIL),
                        tuple(env, PredefinedType.INT, PredefinedType.STRING)));
        Assert.assertTrue(Core.isSubtype(Core.typeCheckContext(env), t, s));
        Assert.assertFalse(Core.isSubtype(Core.typeCheckContext(env), s, t));
    }

    @Test
    public void test7() {
        Env env = new Env();
        SemType s = tuple(env, PredefinedType.INT, Core.union(PredefinedType.INT,
                PredefinedType.STRING));
        SemType t = Core.union(tuple(env, PredefinedType.INT, PredefinedType.INT),
                tuple(env, PredefinedType.INT, PredefinedType.STRING));
        Assert.assertTrue(Core.isSubtype(Core.typeCheckContext(env), t, s));
        Assert.assertFalse(Core.isSubtype(Core.typeCheckContext(env), s, t));
    }

    @Test
    public void tupleTest1() {
        Env env = new Env();
        SemType s = createTupleType(env, PredefinedType.INT, PredefinedType.STRING, PredefinedType.NIL);
        SemType t = createTupleType(env, PredefinedType.VAL, PredefinedType.VAL, PredefinedType.VAL);
        Assert.assertTrue(Core.isSubtype(Core.typeCheckContext(env), s, t));
        Assert.assertFalse(Core.isSubtype(Core.typeCheckContext(env), t, s));
    }

    @Test
    public void tupleTest2() {
        Env env = new Env();
        SemType s = createTupleType(env, PredefinedType.INT, PredefinedType.STRING, PredefinedType.NIL);
        SemType t = createTupleType(env, PredefinedType.VAL, PredefinedType.VAL);
        Assert.assertFalse(Core.isSubtype(Core.typeCheckContext(env), s, t));
        Assert.assertFalse(Core.isSubtype(Core.typeCheckContext(env), t, s));
    }

    @Test
    public void tupleTest3() {
        Env env = new Env();
        SemType z1 = createTupleType(env);
        SemType z2 = createTupleType(env);
        SemType t = createTupleType(env, PredefinedType.INT);
        Assert.assertTrue(!Core.isEmpty(Core.typeCheckContext(env), z1));
        Assert.assertTrue(Core.isSubtype(Core.typeCheckContext(env), z1, z2));
        Assert.assertTrue(Core.isEmpty(Core.typeCheckContext(env), Core.diff(z1, z2)));
        Assert.assertFalse(Core.isEmpty(Core.typeCheckContext(env), Core.diff(z1, PredefinedType.INT)));
        Assert.assertFalse(Core.isEmpty(Core.typeCheckContext(env), Core.diff(PredefinedType.INT, z1)));
    }

    @Test
    public void tupleTest4() {
        Env env = new Env();
        SemType s = createTupleType(env, PredefinedType.INT, PredefinedType.INT);
        SemType t = createTupleType(env, PredefinedType.INT, PredefinedType.INT, PredefinedType.INT);
        Assert.assertFalse(Core.isEmpty(Core.typeCheckContext(env), s));
        Assert.assertFalse(Core.isEmpty(Core.typeCheckContext(env), t));
        Assert.assertFalse(Core.isSubtype(Core.typeCheckContext(env), s, t));
        Assert.assertFalse(Core.isSubtype(Core.typeCheckContext(env), t, s));
        Assert.assertTrue(Core.isEmpty(Core.typeCheckContext(env), Core.intersect(s, t)));
    }

    private SemType func(Env env, SemType args, SemType ret) {
        FunctionDefinition def = new FunctionDefinition();
        return def.define(env, args, ret, FunctionQualifiers.from(env, false, false));
    }

    @Test
    public void funcTest1() {
        Env env = new Env();
        SemType s = func(env, PredefinedType.INT, PredefinedType.INT);
        SemType t = func(env, PredefinedType.INT, Core.union(PredefinedType.NIL, PredefinedType.INT));
        Assert.assertTrue(Core.isSubtype(Core.typeCheckContext(env), s, t));
        Assert.assertFalse(Core.isSubtype(Core.typeCheckContext(env), t, s));
    }

    @Test
    public void funcTest2() {
        Env env = new Env();
        SemType s = func(env, Core.union(PredefinedType.NIL, PredefinedType.INT), PredefinedType.INT);
        SemType t = func(env, PredefinedType.INT, PredefinedType.INT);
        Assert.assertTrue(Core.isSubtype(Core.typeCheckContext(env), s, t));
        Assert.assertFalse(Core.isSubtype(Core.typeCheckContext(env), t, s));
    }

    @Test
    public void funcTest3() {
        Env env = new Env();
        SemType s = func(env, createTupleType(env, Core.union(PredefinedType.NIL, PredefinedType.INT)),
                PredefinedType.INT);
        SemType t = func(env, createTupleType(env, PredefinedType.INT), PredefinedType.INT);
        Assert.assertTrue(Core.isSubtype(Core.typeCheckContext(env), s, t));
        Assert.assertFalse(Core.isSubtype(Core.typeCheckContext(env), t, s));
    }

    @Test
    public void funcTest4() {
        Env env = new Env();
        SemType s = func(env, createTupleType(env, Core.union(PredefinedType.NIL, PredefinedType.INT)),
                PredefinedType.INT);
        SemType t = func(env, createTupleType(env, PredefinedType.INT),
                Core.union(PredefinedType.NIL, PredefinedType.INT));
        Assert.assertTrue(Core.isSubtype(Core.typeCheckContext(env), s, t));
        Assert.assertFalse(Core.isSubtype(Core.typeCheckContext(env), t, s));
    }

    @Test
    public void stringTest() {
        List<EnumerableString> result = new ArrayList<>();
        // TODO may have to assert lists by converting the output to a string list

        EnumerableSubtype.enumerableListUnion(new EnumerableString[]{EnumerableString.from("a"),
                        EnumerableString.from("b"), EnumerableString.from("d")},
                new EnumerableString[]{EnumerableString.from("c")}, result);
        Assert.assertEquals(result.get(0).value, "a");
        Assert.assertEquals(result.get(1).value, "b");
        Assert.assertEquals(result.get(2).value, "c");
        Assert.assertEquals(result.get(3).value, "d");

        result = new ArrayList<>();
        EnumerableSubtype.enumerableListIntersect(new EnumerableString[]{EnumerableString.from("a"),
                        EnumerableString.from("b"), EnumerableString.from("d")},
                new EnumerableString[]{EnumerableString.from("d")}, result);
        Assert.assertEquals(result.get(0).value, "d");

        result = new ArrayList<>();
        EnumerableSubtype.enumerableListDiff(new EnumerableString[]{EnumerableString.from("a"),
                        EnumerableString.from("b"), EnumerableString.from("c"), EnumerableString.from("d")},
                new EnumerableString[]{EnumerableString.from("a"), EnumerableString.from("c")}, result);
        Assert.assertEquals(result.get(0).value, "b");
        Assert.assertEquals(result.get(1).value, "d");
    }

    @Test
    public void roListTest() {
        SemType t1 = Core.intersect(PredefinedType.LIST, PredefinedType.VAL_READONLY);
        Env env = new Env();
        ListDefinition ld = new ListDefinition();
        SemType t2 = ld.defineListTypeWrapped(env, new ArrayList<>(), 0, PredefinedType.VAL);
        SemType t = Core.diff(t1, t2);
        Context cx = Core.typeCheckContext(env);
        boolean b = Core.isEmpty(cx, t);
        Assert.assertTrue(b);
    }

    @Test
    public void testIntSubtypeWidenUnsigned() {
        Assert.assertTrue(((AllOrNothingSubtype) IntSubtype.intSubtypeWidenUnsigned(AllOrNothingSubtype.createAll()))
                .isAllSubtype());
        Assert.assertTrue(((AllOrNothingSubtype) IntSubtype.intSubtypeWidenUnsigned(
                IntSubtype.createIntSubtype(new Range(-1L, 10L)))).isAllSubtype());
        IntSubtype intType1 = (IntSubtype) IntSubtype.intSubtypeWidenUnsigned(
                IntSubtype.createIntSubtype(new Range(0L, 0L)));
        Assert.assertEquals(intType1.ranges[0].min, 0L);
        Assert.assertEquals(intType1.ranges[0].max, 255L);
        IntSubtype intType2 = (IntSubtype) IntSubtype.intSubtypeWidenUnsigned(
                IntSubtype.createIntSubtype(new Range(0L, 257L)));
        Assert.assertEquals(intType2.ranges[0].min, 0L);
        Assert.assertEquals(intType2.ranges[0].max, 65535L);
    }

    public SemType recursiveTuple(Env env, BiFunction<Env, SemType, List<SemType>> f) {
        ListDefinition def = new ListDefinition();
        SemType t = def.getSemType(env);
        List<SemType> members = f.apply(env, t);
        return def.defineListTypeWrapped(env, members, members.size());
    }

    @Test
    public void recTest() {
        Env env = new Env();
        SemType t1 = recursiveTuple(env,
                (e, t) -> Arrays.asList(PredefinedType.INT, Core.union(t, PredefinedType.NIL)));
        SemType t2 = recursiveTuple(env, (e, t) -> Arrays.asList(Core.union(PredefinedType.INT, PredefinedType.STRING),
                Core.union(t, PredefinedType.NIL)));
        Assert.assertTrue(Core.isSubtype(Core.typeCheckContext(env), t1, t2));
        Assert.assertFalse(Core.isSubtype(Core.typeCheckContext(env), t2, t1));
    }

    @Test
    public void recTest2() {
        Env env = new Env();
        SemType t1 = Core.union(PredefinedType.NIL, recursiveTuple(env,
                (e, t) -> Arrays.asList(PredefinedType.INT, Core.union(t, PredefinedType.NIL))));
        SemType t2 = recursiveTuple(env,
                (e, t) -> Arrays.asList(PredefinedType.INT, Core.union(t, PredefinedType.NIL)));
        Assert.assertTrue(Core.isSubtype(Core.typeCheckContext(env), t2, t1));
    }

    @Test
    public void recTest3() {
        Env env = new Env();
        SemType t1 = recursiveTuple(env,
                (e, t) -> Arrays.asList(PredefinedType.INT, Core.union(t, PredefinedType.NIL)));
        SemType t2 = recursiveTuple(env, (e, t) -> Arrays.asList(PredefinedType.INT, Core.union(PredefinedType.NIL,
                createTupleType(e, PredefinedType.INT, Core.union(PredefinedType.NIL, t)))));
        Assert.assertTrue(Core.isSubtype(Core.typeCheckContext(env), t1, t2));
    }

    @Test
    public void testStringCharSubtype() {
        ComplexSemType st = (ComplexSemType) stringConst("a");
        Assert.assertEquals(st.subtypeDataList().length, 1);
        StringSubtype subType = (StringSubtype) st.subtypeDataList()[0];
        Assert.assertEquals(subType.getChar().values.length, 1);
        Assert.assertEquals(subType.getChar().values[0].value, "a");
        Assert.assertEquals(subType.getChar().allowed, true);
        Assert.assertEquals(subType.getNonChar().values.length, 0);
        Assert.assertEquals(subType.getNonChar().allowed, true);
    }

    @Test
    public void testStringNonCharSubtype() {
        ComplexSemType st = (ComplexSemType) stringConst("abc");
        Assert.assertEquals(st.subtypeDataList().length, 1);
        StringSubtype subType = (StringSubtype) st.subtypeDataList()[0];
        Assert.assertEquals(subType.getChar().values.length, 0);
        Assert.assertEquals(subType.getChar().allowed, true);
        Assert.assertEquals(subType.getNonChar().values.length, 1);
        Assert.assertEquals(subType.getNonChar().values[0].value, "abc");
        Assert.assertEquals(subType.getNonChar().allowed, true);
    }

    @Test
    public void testStringSubtypeSingleValue() {
        ComplexSemType abc = (ComplexSemType) stringConst("abc");
        StringSubtype abcSD = (StringSubtype) abc.subtypeDataList()[0];
        Assert.assertEquals(StringSubtype.stringSubtypeSingleValue(abcSD).get(), "abc");

        ComplexSemType a = (ComplexSemType) stringConst("a");
        StringSubtype aSD = (StringSubtype) a.subtypeDataList()[0];
        Assert.assertEquals(StringSubtype.stringSubtypeSingleValue(aSD).get(), "a");

        ComplexSemType aAndAbc = (ComplexSemType) Core.union(a, abc);
        Assert.assertEquals(StringSubtype.stringSubtypeSingleValue(aAndAbc.subtypeDataList()[0]),
                Optional.empty());

        ComplexSemType intersect1 = (ComplexSemType) Core.intersect(aAndAbc, a);
        Assert.assertEquals(StringSubtype.stringSubtypeSingleValue(intersect1.subtypeDataList()[0]).get(), "a");
        ComplexSemType intersect2 = (ComplexSemType) Core.intersect(aAndAbc, abc);
        Assert.assertEquals(StringSubtype.stringSubtypeSingleValue(intersect2.subtypeDataList()[0]).get(), "abc");
        SemType intersect3 = Core.intersect(a, abc);
        Assert.assertEquals(intersect3.toString(), PredefinedType.NEVER.toString());
    }

    private static SemType createTupleType(Env env, SemType... members) {
        ListDefinition ld = new ListDefinition();
        return ld.tupleTypeWrapped(env, members);
    }
}
