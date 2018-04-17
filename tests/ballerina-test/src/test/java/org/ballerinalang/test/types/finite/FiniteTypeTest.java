/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.types.finite;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test finite type.
 */
public class FiniteTypeTest {

    private CompileResult result;


    @BeforeClass(alwaysRun = true)
    public void setup() {
        result = BCompileUtil.compile("test-src/types/finite/finite-type.bal");
    }

    @Test()
    public void testSingletonInlineFunction() {
        BValue[] returns = BRunUtil.invoke(result, "testSingletonInlineFunction");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
    }

    @Test()
    public void testRecursiveCallsSingletonParamReturn() {
        BValue[] returns = BRunUtil.invoke(result, "testRecursiveCallsSingletonParamReturn");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(((BString) returns[0]).stringValue(), "constant");
    }

    @Test()
    public void testAssignmentCompositeFiniteTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testAssignmentCompositeFiniteTypes");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(((BString) returns[0]).stringValue(), "off");
    }

    @Test()
    public void testTypeSetIntersectionCaseOne() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeSetIntersectionCaseOne");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(((BString) returns[0]).stringValue(), "off");
    }

    @Test()
    public void testTypeSetIntersectionCaseTwo() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeSetIntersectionCaseTwo");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 100);
    }

    @Test()
    public void testTypeSetIntersectionCaseThree() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeSetIntersectionCaseThree");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(((BString) returns[0]).stringValue(), "very good");
    }

    @Test()
    public void testAssignmentStateType() {
        BValue[] returns = BRunUtil.invoke(result, "testAssignmentStateType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(((BString) returns[0]).stringValue(), "off");
    }

    @Test()
    public void testAssignmentNumberSetType() {
        BValue[] returns = BRunUtil.invoke(result, "testAssignmentNumberSetType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);
    }

    @Test()
    public void testAssignmentStringOrIntSetType() {
        BValue[] returns = BRunUtil.invoke(result, "testAssignmentStringOrIntSetType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals((((BString) returns[0]).stringValue()), "This is a string");
    }

    @Test()
    public void testAssignmentStringOrIntSetTypeCaseTwo() {
        BValue[] returns = BRunUtil.invoke(result, "testAssignmentStringOrIntSetTypeCaseTwo");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals((((BInteger) returns[0]).intValue()), 111);
    }


    @Test()
    public void testAssignmentIntSetType() {
        BValue[] returns = BRunUtil.invoke(result, "testAssignmentIntSetType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals((((BInteger) returns[0]).intValue()), 222);
    }

    @Test()
    public void testAssignmentIntArrayType() {
        BValue[] returns = BRunUtil.invoke(result, "testAssignmentIntArrayType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals((((BInteger) returns[0]).intValue()), 9989);
    }

    @Test()
    public void testAssignmentStateSameTypeComparison() {
        BValue[] returns = BRunUtil.invoke(result, "testAssignmentStateSameTypeComparison");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals((((BInteger) returns[0]).intValue()), 2);
    }

    @Test()
    public void testAssignmentStateSameTypeComparisonCaseTwo() {
        BValue[] returns = BRunUtil.invoke(result, "testAssignmentStateSameTypeComparisonCaseTwo");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals((((BString) returns[0]).stringValue()), "on");
    }

    @Test()
    public void testAssignmentRefValueType() {
        BValue[] returns = BRunUtil.invoke(result, "testAssignmentRefValueType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BStruct);
    }

    @Test()
    public void testAssignmentRefValueTypeCaseTwo() {
        BValue[] returns = BRunUtil.invoke(result, "testAssignmentRefValueTypeCaseTwo");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals((((BInteger) returns[0]).intValue()), 4);
    }

    @Test()
    public void testSingletonAsGlobalVar() {
        BValue[] returns = BRunUtil.invoke(result, "testSingletonAsGlobalVar");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals((((BInteger) returns[0]).intValue()), 3);
    }

    @Test()
    public void testSingletonArray() {
        BValue[] returns = BRunUtil.invoke(result, "testSingletonArray");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals((((BInteger) returns[0]).intValue()), 1);
    }

    @Test()
    public void testSingletonToIntAssignment() {
        BValue[] returns = BRunUtil.invoke(result, "testSingletonToIntAssignment");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals((((BInteger) returns[0]).intValue()), 1);
    }

    @Test()
    public void testSingletonToFloatAssignment() {
        BValue[] returns = BRunUtil.invoke(result, "testSingletonToFloatAssignment");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BFloat);
        Assert.assertEquals((((BFloat) returns[0]).floatValue()), 1.0);
    }

    @Test()
    public void testBooleanSingletons() {
        BValue[] returns = BRunUtil.invoke(result, "testBooleanSingletons");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertEquals((((BBoolean) returns[0]).booleanValue()), true);
    }

    @Test()
    public void testBooleanSingletonsCaseTwo() {
        BValue[] returns = BRunUtil.invoke(result, "testBooleanSingletonsCaseTwo");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertEquals((((BBoolean) returns[0]).booleanValue()), false);
    }
}

