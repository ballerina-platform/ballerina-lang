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

package org.ballerinalang.test.balo.types;

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BByte;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.balo.BaloCreator;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test finite types with balo.
 */
public class FiniteTypeTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        BaloCreator.cleanCacheDirectories();
        BaloCreator.createAndSetupBalo("test-src/balo/test_projects/finite_type_project", "finiteTypeTest", "foo");
        result = BCompileUtil.compile("test-src/balo/test_balo/types/finite_type_test.bal");
    }

    @Test()
    public void finiteAssignmentStateType() {
        BValue[] returns = BRunUtil.invoke(result, "finiteAssignmentStateType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "off");
    }

    @Test()
    public void finiteAssignmentNumberSetType() {
        BValue[] returns = BRunUtil.invoke(result, "finiteAssignmentNumberSetType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);
    }

    @Test()
    public void finiteAssignmentStringOrIntSetType() {
        BValue[] returns = BRunUtil.invoke(result, "finiteAssignmentStringOrIntSetType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "This is a string");
    }

    @Test()
    public void finiteAssignmentStringOrIntSetTypeCaseTwo() {
        BValue[] returns = BRunUtil.invoke(result, "finiteAssignmentStringOrIntSetTypeCaseTwo");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals((((BInteger) returns[0]).intValue()), 111);
    }


    @Test()
    public void finiteAssignmentIntSetType() {
        BValue[] returns = BRunUtil.invoke(result, "finiteAssignmentIntSetType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals((((BInteger) returns[0]).intValue()), 222);
    }

    @Test()
    public void finiteAssignmentIntArrayType() {
        BValue[] returns = BRunUtil.invoke(result, "finiteAssignmentIntArrayType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals((((BInteger) returns[0]).intValue()), 9989);
    }

    @Test()
    public void finiteAssignmentStateSameTypeComparison() {
        BValue[] returns = BRunUtil.invoke(result, "finiteAssignmentStateSameTypeComparison");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals((((BInteger) returns[0]).intValue()), 2);
    }

    @Test()
    public void finiteAssignmentStateSameTypeComparisonCaseTwo() {
        BValue[] returns = BRunUtil.invoke(result, "finiteAssignmentStateSameTypeComparisonCaseTwo");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "on");
    }

    @Test()
    public void finiteAssignmentRefValueType() {
        BValue[] returns = BRunUtil.invoke(result, "finiteAssignmentRefValueType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BMap);
    }

    @Test()
    public void finiteAssignmentRefValueTypeCaseTwo() {
        BValue[] returns = BRunUtil.invoke(result, "finiteAssignmentRefValueTypeCaseTwo");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals((((BInteger) returns[0]).intValue()), 4);
    }

    @Test()
    public void testFiniteTypeWithMatch() {
        BValue[] returns = BRunUtil.invoke(result, "testFiniteTypeWithMatch");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "ss");
    }

    @Test()
    public void testFiniteTypesWithDefaultValues() {
        BValue[] returns = BRunUtil.invoke(result, "testFiniteTypesWithDefaultValues");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "on");
    }

    @Test()
    public void testFiniteTypesWithUnion() {
        BValue[] returns = BRunUtil.invoke(result, "testFiniteTypesWithUnion");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals((((BInteger) returns[0]).intValue()), 1);
    }

    @Test()
    public void testFiniteTypesWithUnionCaseOne() {
        BValue[] returns = BRunUtil.invoke(result, "testFiniteTypesWithUnionCaseOne");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals((((BInteger) returns[0]).intValue()), 100);
    }

    @Test()
    public void testFiniteTypesWithUnionCaseTwo() {
        BValue[] returns = BRunUtil.invoke(result, "testFiniteTypesWithUnionCaseTwo");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "on");
    }

    @Test()
    public void testFiniteTypesWithUnionCaseThree() {
        BValue[] returns = BRunUtil.invoke(result, "testFiniteTypesWithUnionCaseThree");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals((((BInteger) returns[0]).intValue()), 1001);
    }

    @Test()
    public void testFiniteTypesWithTuple() {
        BValue[] returns = BRunUtil.invoke(result, "testFiniteTypesWithTuple");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "on");
    }

    @Test()
    public void testTypeAliasing() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeAliasing");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Anonymous name");
    }

    @Test()
    public void testTypeAliasingCaseOne() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeAliasingCaseOne");
        Assert.assertEquals(returns.length, 2);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals((((BInteger) returns[0]).intValue()), 100);
        Assert.assertNotNull(returns[1]);
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "hundred");
    }

    @Test()
    public void testTypeDefinitionWithVarArgs() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeDefinitionWithVarArgs");
        Assert.assertEquals(returns.length, 2);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "John");
        Assert.assertNotNull(returns[1]);
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "Anne");
    }

    @Test()
    public void testTypeDefinitionWithArray() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeDefinitionWithArray");
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals((((BInteger) returns[0]).intValue()), 2);
        Assert.assertEquals((((BInteger) returns[1]).intValue()), 23);
    }

    @Test()
    public void testTypeDefinitionWithByteArray() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeDefinitionWithByteArray");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BByte.class);
        Assert.assertEquals((((BInteger) returns[0]).intValue()), 2);
        Assert.assertEquals((((BByte) returns[1]).byteValue()), 23);
    }

    @Test()
    public void testFiniteAssignmentByteType() {
        BValue[] returns = BRunUtil.invoke(result, "testFiniteAssignmentByteType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertSame(returns[0].getClass(), BByte.class);
        Assert.assertEquals((((BByte) returns[0]).byteValue()), 222);
    }

    @Test()
    public void testByteTypeDefinitionWithVarArgs() {
        BValue[] returns = BRunUtil.invoke(result, "testByteTypeDefinitionWithVarArgs");
        Assert.assertEquals(returns.length, 2);
        Assert.assertNotNull(returns[0]);
        Assert.assertSame(returns[0].getClass(), BByte.class);
        Assert.assertEquals((((BByte) returns[0]).byteValue()), 34);
        Assert.assertNotNull(returns[1]);
        Assert.assertSame(returns[1].getClass(), BFloat.class);
        Assert.assertEquals((((BFloat) returns[1]).floatValue()), 4.5);
    }

    @Test
    public void testTypeDefWithFunctions() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeDefWithFunctions");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), "Hello".length());
    }

    @Test
    public void testTypeDefWithFunctions2() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeDefWithFunctions2");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), "Hello".length());
    }

    @Test(dataProvider = "assignmentToBroaderTypeFunctions")
    public void testFiniteTypeAssignmentToBroaderType(String function) {
        BValue[] returns = BRunUtil.invoke(result, function);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @DataProvider(name = "assignmentToBroaderTypeFunctions")
    public Object[][] assignmentToBroaderTypeFunctions() {
        return new Object[][]{
                {"testStringOnlyFiniteTypeAssignmentToTypeWithString"},
                {"testIntOnlyFiniteTypeAssignmentToTypeWithInt"},
                {"testFloatOnlyFiniteTypeAssignmentToTypeWithFloat"},
                {"testBooleanOnlyFiniteTypeAssignmentToTypeWithBoolean"},
                {"testByteOnlyFiniteTypeAssignmentToTypeWithByte"},
                {"testFiniteTypeAssignmentToBroaderType"},
                {"testFiniteTypeWithConstAssignmentToBroaderType"},
                {"testFiniteTypeWithConstAndTypeAssignmentToBroaderType"},
                {"testFiniteTypesAsUnionsAsBroaderTypes_1"},
                {"testFiniteTypesAsUnionsAsBroaderTypes_2"}
        };
    }

    @AfterClass
    public void tearDown() {
        BaloCreator.clearPackageFromRepository(
                "test-src/balo/test_projects/finite_type_project", "finiteTypeTest", "foo");
    }
}

