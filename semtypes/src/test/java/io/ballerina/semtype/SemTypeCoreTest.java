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
package io.ballerina.semtype;

import io.ballerina.semtype.definition.FunctionDefinition;
import io.ballerina.semtype.definition.ListDefinition;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * Tests Core functions of Semtypes.
 *
 */
public class SemTypeCoreTest {

    @Test
    public void testSubtypeSimple() {
        Assert.assertTrue(Core.isSubtypeSimple(PredefinedType.NIL, PredefinedType.ANY));
        Assert.assertTrue(Core.isSubtypeSimple(PredefinedType.INT, PredefinedType.TOP));
        Assert.assertTrue(Core.isSubtypeSimple(PredefinedType.ANY, PredefinedType.TOP));
        Assert.assertTrue(Core.isSubtypeSimple(PredefinedType.INT, PredefinedType.BOOLEAN));
        Assert.assertTrue(Core.isSubtypeSimple(PredefinedType.ERROR, PredefinedType.ANY));
    }

    @Test
    public void testSingleNumericType() {
        Assert.assertEquals(Core.singleNumericType(PredefinedType.INT), Optional.of(PredefinedType.INT));
        Assert.assertEquals(Core.singleNumericType(PredefinedType.BOOLEAN), Optional.empty());
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
        SemType t1 = ListDefinition.tuple(env, PredefinedType.INT, PredefinedType.INT);
        disjoint(Core.typeCheckContext(env), (UniformTypeBitSet) t1, PredefinedType.INT);
        SemType t2 = ListDefinition.tuple(env, PredefinedType.STRING, PredefinedType.STRING);
        disjoint(Core.typeCheckContext(env), PredefinedType.NIL, (UniformTypeBitSet) t2);
    }

    private void disjoint(TypeCheckContext tc, UniformTypeBitSet t1, UniformTypeBitSet t2) {
        Assert.assertFalse(Core.isSubtype(tc, t1, t2));
        Assert.assertFalse(Core.isSubtype(tc, t2, t1));
        Assert.assertFalse(Core.isEmpty(tc, Core.intersect(t1, t2)));
    }

    @Test
    public void test2() {
        Assert.assertTrue(Core.isSubtype(Core.typeCheckContext(new Env()), PredefinedType.INT, PredefinedType.TOP));
    }
    
    @Test
    public void test3() {
        Env env = new Env();
        SemType s = ListDefinition.tuple(env, PredefinedType.INT, Core.union(PredefinedType.INT,
                PredefinedType.STRING));
        SemType t = Core.union(ListDefinition.tuple(env, PredefinedType.INT, PredefinedType.INT),
                ListDefinition.tuple(env, PredefinedType.INT, PredefinedType.STRING));
        equiv(env, s, t);
    }

    private void equiv(Env env, SemType s, SemType t) {
        Assert.assertTrue(Core.isSubtype(Core.typeCheckContext(env), s, t));
        Assert.assertTrue(Core.isSubtype(Core.typeCheckContext(env), t, s));
    }

    @Test
    public void test4() {
        Env env = new Env();
        SemType isT = ListDefinition.tuple(env, PredefinedType.INT, PredefinedType.STRING);
        SemType itT = ListDefinition.tuple(env, PredefinedType.INT, PredefinedType.TOP);
        SemType tsT = ListDefinition.tuple(env, PredefinedType.TOP, PredefinedType.STRING);
        SemType iiT = ListDefinition.tuple(env, PredefinedType.INT, PredefinedType.INT);
        SemType ttT = ListDefinition.tuple(env, PredefinedType.TOP, PredefinedType.TOP);
        TypeCheckContext tc = Core.typeCheckContext(env);
        Assert.assertTrue(Core.isSubtype(tc, isT, itT));
        Assert.assertTrue(Core.isSubtype(tc, isT, tsT));
        Assert.assertTrue(Core.isSubtype(tc, iiT, ttT));
    }

    @Test
    public void test5() {
        Env env = new Env();
        SemType s = ListDefinition.tuple(env, PredefinedType.INT, Core.union(PredefinedType.NIL,
                Core.union(PredefinedType.INT, PredefinedType.STRING)));
        SemType t = Core.union(ListDefinition.tuple(env, PredefinedType.INT, PredefinedType.INT),
                Core.union(ListDefinition.tuple(env, PredefinedType.INT, PredefinedType.NIL),
                        ListDefinition.tuple(env, PredefinedType.INT, PredefinedType.STRING)));
        equiv(env, s, t);
    }

    public SemType recursiveTuple(Env env, Method f) throws InvocationTargetException, IllegalAccessException {
        ListDefinition def = new ListDefinition();
        SemType t = def.getSemType(env);
        SemType[] members = (SemType[]) f.invoke(env, t);
        return def.define(env, List.of(members), PredefinedType.NEVER);
    }

    @Test
    public void tupleTest1() {
        Env env = new Env();
        SemType s = ListDefinition.tuple(env, PredefinedType.INT, PredefinedType.STRING, PredefinedType.NIL);
        SemType t = ListDefinition.tuple(env, PredefinedType.TOP, PredefinedType.TOP, PredefinedType.TOP);
        Assert.assertTrue(Core.isSubtype(Core.typeCheckContext(env), s, t));
        Assert.assertFalse(Core.isSubtype(Core.typeCheckContext(env), t, s));
    }

    @Test
    public void tupleTest2() {
        Env env = new Env();
        SemType s = ListDefinition.tuple(env, PredefinedType.INT, PredefinedType.STRING, PredefinedType.NIL);
        SemType t = ListDefinition.tuple(env, PredefinedType.TOP, PredefinedType.TOP);
        Assert.assertFalse(Core.isSubtype(Core.typeCheckContext(env), s, t));
        Assert.assertFalse(Core.isSubtype(Core.typeCheckContext(env), t, s));
    }

    @Test
    public void tupleTest3() {
        Env env = new Env();
        SemType z1 = ListDefinition.tuple(env);
        SemType z2 = ListDefinition.tuple(env);
        SemType t = ListDefinition.tuple(env, PredefinedType.INT);
        Assert.assertTrue(!Core.isEmpty(Core.typeCheckContext(env), z1));
        Assert.assertTrue(Core.isSubtype(Core.typeCheckContext(env), z1, z2));
        Assert.assertTrue(Core.isEmpty(Core.typeCheckContext(env), Core.diff(z1, z2)));
        Assert.assertFalse(Core.isEmpty(Core.typeCheckContext(env), Core.diff(z1, PredefinedType.INT)));
        Assert.assertFalse(Core.isEmpty(Core.typeCheckContext(env), Core.diff(PredefinedType.INT, z1)));
    }

    @Test
    public void tupleTest4() {
        Env env = new Env();
        SemType s = ListDefinition.tuple(env, PredefinedType.INT, PredefinedType.INT);
        SemType t = ListDefinition.tuple(env, PredefinedType.INT, PredefinedType.INT, PredefinedType.INT);
        Assert.assertFalse(Core.isEmpty(Core.typeCheckContext(env), s));
        Assert.assertFalse(Core.isEmpty(Core.typeCheckContext(env), t));
        Assert.assertFalse(Core.isSubtype(Core.typeCheckContext(env), s, t));
        Assert.assertFalse(Core.isSubtype(Core.typeCheckContext(env), t, s));
        Assert.assertTrue(Core.isEmpty(Core.typeCheckContext(env), Core.intersect(s, t)));
    }

    private SemType func(Env env, SemType args, SemType ret) {
        FunctionDefinition def = new FunctionDefinition(env);
        return def.define(env, args, ret);
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
        SemType s = func(env, ListDefinition.tuple(env, Core.union(PredefinedType.NIL, PredefinedType.INT)),
                PredefinedType.INT);
        SemType t = func(env, ListDefinition.tuple(env, PredefinedType.INT), PredefinedType.INT);
        Assert.assertTrue(Core.isSubtype(Core.typeCheckContext(env), s, t));
        Assert.assertFalse(Core.isSubtype(Core.typeCheckContext(env), t, s));
    }

    @Test
    public void funcTest4() {
        Env env = new Env();
        SemType s = func(env, ListDefinition.tuple(env, Core.union(PredefinedType.NIL, PredefinedType.INT)),
                PredefinedType.INT);
        SemType t = func(env, ListDefinition.tuple(env, PredefinedType.INT),
                Core.union(PredefinedType.NIL, PredefinedType.INT));
        Assert.assertTrue(Core.isSubtype(Core.typeCheckContext(env), s, t));
        Assert.assertFalse(Core.isSubtype(Core.typeCheckContext(env), t, s));
    }
}
