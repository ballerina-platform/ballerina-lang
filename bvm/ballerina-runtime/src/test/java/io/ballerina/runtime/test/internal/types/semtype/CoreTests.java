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

package io.ballerina.runtime.test.internal.types.semtype;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.types.BasicTypeBitSet;
import io.ballerina.runtime.api.types.SemType.BasicTypeCode;
import io.ballerina.runtime.api.types.SemType.Builder;
import io.ballerina.runtime.api.types.SemType.Context;
import io.ballerina.runtime.api.types.SemType.Core;
import io.ballerina.runtime.api.types.SemType.SemType;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.types.semtype.BBasicTypeBitSet;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CoreTests {

    private final Context cx = new Context();

    @Test
    public static void testSimpleUnion() {
        BasicTypeBitSet t1 = BBasicTypeBitSet.from(1 << 1);
        BasicTypeBitSet t2 = BBasicTypeBitSet.from(1 << 2);
        BasicTypeBitSet result = (BasicTypeBitSet) Core.union(t1, t2);
        Assert.assertTrue(isBasicTypeSame(result, BBasicTypeBitSet.from(1 << 1 | 1 << 2)));
    }

    @Test
    public static void testSimpleUnionWithNever() {
        BasicTypeBitSet t1 = BBasicTypeBitSet.from(1 << 1);
        BasicTypeBitSet t2 = BBasicTypeBitSet.from(0);
        BasicTypeBitSet result = (BasicTypeBitSet) Core.union(t1, t2);
        Assert.assertTrue(isBasicTypeSame(result, t1));
    }

    @Test
    public static void testSimpleDiff() {
        BasicTypeBitSet t1 = BBasicTypeBitSet.from(1 << 1 | 1 << 2);
        BasicTypeBitSet t2 = BBasicTypeBitSet.from(1 << 2);
        BasicTypeBitSet res = (BasicTypeBitSet) Core.diff(t1, t2);
        Assert.assertTrue(isBasicTypeSame(res, BBasicTypeBitSet.from(1 << 1)));

        BasicTypeBitSet res2 = (BasicTypeBitSet) Core.diff(t2, t1);
        Assert.assertTrue(isBasicTypeSame(res2, BBasicTypeBitSet.from(0)));
    }

    @Test
    public static void testSimpleIntersection() {
        BasicTypeBitSet t1 = BBasicTypeBitSet.from(1 << 1 | 1 << 2);
        BasicTypeBitSet t2 = BBasicTypeBitSet.from(1 << 2);
        BasicTypeBitSet res = (BasicTypeBitSet) Core.intersect(t1, t2);
        Assert.assertTrue(isBasicTypeSame(res, t2));

        BasicTypeBitSet t3 = BBasicTypeBitSet.from(0);
        BasicTypeBitSet res2 = (BasicTypeBitSet) Core.intersect(t1, t3);
        Assert.assertTrue(isBasicTypeSame(res2, t3));
    }

    @Test
    public void testSimpleSubType() {
        SemType intSingleton1 = Builder.intConst(1);
        SemType intTop = Builder.from(BasicTypeCode.BT_INT);
        Assert.assertTrue(Core.isSubType(cx, intSingleton1, intTop));
        Assert.assertFalse(Core.isSubType(cx, intTop, intSingleton1));

        SemType intSingleton2 = Builder.intConst(2);
        SemType intUnion = Core.union(intSingleton1, intSingleton2);
        Assert.assertTrue(Core.isSubType(cx, intSingleton1, intUnion));
        Assert.assertTrue(Core.isSubType(cx, intSingleton2, intUnion));
        Assert.assertTrue(Core.isSubType(cx, intUnion, intTop));
    }

    @Test
    public void testBTypeSubType() {
        SemType booleanTy = Builder.from(PredefinedTypes.TYPE_BOOLEAN);
        SemType anyTy = Builder.from(PredefinedTypes.TYPE_ANY);
        Assert.assertTrue(Core.isSubType(cx, booleanTy, anyTy, (t1, t2) -> TypeChecker.checkIsType(t1, t2)));
    }

    @Test
    public void testMixSubType() {
        SemType intSingleton1 = Builder.intConst(1);
        SemType BooleanBType = Builder.from(PredefinedTypes.TYPE_BOOLEAN);
        SemType T1 = Core.union(intSingleton1, BooleanBType); // 1(semType) | boolean (BType)

        SemType intType = Builder.from(BasicTypeCode.BT_INT);
        SemType T2 = Core.union(intType, BooleanBType); // int(semType) | boolean (BType)

        Assert.assertTrue(Core.isSubType(cx, T1, T2, (t1, t2) -> TypeChecker.checkIsType(t1, t2)));
    }

    @Test
    public void testSimpleTypeArithmetic() {
        SemType int1 = Builder.intConst(1);
        SemType int2 = Builder.intConst(2);
        SemType int12 = Core.union(int1, int2);
        SemType int1New = Core.diff(int12, int2);
        Assert.assertTrue(Core.isSameType(cx, int1New, int1));
    }

    private static boolean isBasicTypeSame(BasicTypeBitSet t1, BasicTypeBitSet t2) {
        return t1.all() == t2.all();
    }
}
