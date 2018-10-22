/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.test.expressions.varref;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BBooleanArray;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BFloatArray;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for tuple variable references.
 *
 * @since 0.983.0
 */
public class TupleVariableReferenceTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/varref/tuple-variable-reference.bal");
    }

    @Test(description = "Test tuple var reference 1")
    public void testTupleVarRefBasic1() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleVarRefBasic1");
        validateVarRefBasicTestResults(returns);
    }

    @Test(description = "Test tuple var reference 2")
    public void testTupleVarRefBasic2() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleVarRefBasic2");
        validateVarRefBasicTestResults(returns);
    }

    @Test(description = "Test tuple var reference 3")
    public void testTupleVarRefBasic3() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleVarRefBasic3");
        validateVarRefBasicTestResults(returns);
    }

    @Test(description = "Test tuple var reference assignment 1")
    public void testTupleVarRefAssignment1() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleVarRefAssignment1");
        validateVarRefBasicTestResults(returns);
    }

    private void validateVarRefBasicTestResults(BValue[] returns) {
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(returns[0].stringValue(), "UpdatedBallerina");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 453);
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
    }

    @Test(description = "Test tuple var reference assignment 2")
    public void testTupleVarRefAssignment2() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleVarRefAssignment2");
        validateRedAssignmentResults(returns);
    }

    @Test(description = "Test tuple var reference assignment 3")
    public void testTupleVarRefAssignment3() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleVarRefAssignment3");
        validateRedAssignmentResults(returns);
    }

    private void validateRedAssignmentResults(BValue[] returns) {
        Assert.assertEquals(returns.length, 9);
        int i = -1;
        Assert.assertEquals(returns[++i].stringValue(), "TestUpdate");
        Assert.assertEquals(((BInteger) returns[++i]).intValue(), 24);
        Assert.assertEquals(((BInteger) returns[++i]).intValue(), 35);
        Assert.assertFalse(((BBoolean) returns[++i]).booleanValue());
        Assert.assertEquals(returns[++i].stringValue(), "FoooUpdate");
        Assert.assertEquals(((BFloat) returns[++i]).floatValue(), 4.7);
        Assert.assertEquals(((BByte) returns[++i]).byteValue(), 24);
        Assert.assertFalse(((BBoolean) returns[++i]).booleanValue());
        Assert.assertEquals(((BInteger) returns[++i]).intValue(), 66);
    }

    @Test(description = "Test tuple var refinition with array 1")
    public void testTupleVarRefWithArray1() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleVarRefWithArray1");
        Assert.assertEquals(returns.length, 4);
        int i = -1;
        Assert.assertEquals(returns[++i].stringValue(), "Ballerina");
        BValue val1 = returns[++i];
        Assert.assertEquals(val1.getClass(), BIntArray.class);
        BIntArray intArray = ((BIntArray) val1);
        Assert.assertEquals(intArray.get(0), 123);
        Assert.assertEquals(intArray.get(1), 345);
        Assert.assertTrue(((BBoolean) returns[++i]).booleanValue());
        BValue val2 = returns[++i];
        Assert.assertEquals(val2.getClass(), BFloatArray.class);
        BFloatArray floatArray = ((BFloatArray) val2);
        Assert.assertEquals(floatArray.get(0), 2.3);
        Assert.assertEquals(floatArray.get(1), 4.5);
    }

    @Test(description = "Test tuple var refinition with array 2")
    public void testTupleVarRefWithArray2() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleVarRefWithArray2");
        Assert.assertEquals(returns.length, 4);
        int i = -1;
        BValue val1 = returns[++i];
        Assert.assertEquals(val1.getClass(), BStringArray.class);
        BStringArray stringArray = (BStringArray) val1;
        Assert.assertEquals(stringArray.get(0), "A");
        Assert.assertEquals(stringArray.get(1), "B");

        BValue val2 = returns[++i];
        Assert.assertEquals(val2.getClass(), BIntArray.class);
        BIntArray intArray = ((BIntArray) val2);
        Assert.assertEquals(intArray.get(0), 123);
        Assert.assertEquals(intArray.get(1), 345);

        BValue val3 = returns[++i];
        Assert.assertEquals(val3.getClass(), BBooleanArray.class);
        BBooleanArray bBooleanArray = (BBooleanArray) val3;
        Assert.assertEquals(bBooleanArray.get(0), 1);
        Assert.assertEquals(bBooleanArray.get(1), 0);

        BValue val4 = returns[++i];
        Assert.assertEquals(val4.getClass(), BFloatArray.class);
        BFloatArray floatArray = ((BFloatArray) val4);
        Assert.assertEquals(floatArray.get(0), 2.3);
        Assert.assertEquals(floatArray.get(1), 4.5);
    }

    @Test(description = "Test tuple var refinition with array 3")
    public void testTupleVarRefWithArray3() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleVarRefWithArray3");
        validateVarRefArrayResults(returns);
    }

    @Test(description = "Test tuple var refinition with array 4")
    public void testTupleVarRefWithArray4() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleVarRefWithArray4");
        validateVarRefArrayResults(returns);
    }

    private void validateVarRefArrayResults(BValue[] returns) {
        Assert.assertEquals(returns.length, 3);
        int i = -1;
        BValue val1 = returns[++i];
        BRefValueArray refValueArray1 = (BRefValueArray) val1;
        BStringArray stringArray1 = (BStringArray) refValueArray1.get(0);
        Assert.assertEquals(stringArray1.get(0), "A");
        Assert.assertEquals(stringArray1.get(1), "B");

        BStringArray stringArray2 = (BStringArray) refValueArray1.get(1);
        Assert.assertEquals(stringArray2.get(0), "C");
        Assert.assertEquals(stringArray2.get(1), "D");

        BValue val2 = returns[++i];
        BRefValueArray refValueArray2 = (BRefValueArray) val2;
        BIntArray intArray1 = ((BIntArray) refValueArray2.get(0));
        Assert.assertEquals(intArray1.get(0), 123);
        Assert.assertEquals(intArray1.get(1), 345);

        BIntArray intArray2 = ((BIntArray) refValueArray2.get(1));
        Assert.assertEquals(intArray2.get(0), 12);
        Assert.assertEquals(intArray2.get(1), 34);
        Assert.assertEquals(intArray2.get(2), 56);

        BValue val3 = returns[++i];
        Assert.assertEquals(val3.getClass(), BFloatArray.class);
        BFloatArray floatArray = ((BFloatArray) val3);
        Assert.assertEquals(floatArray.get(0), 2.3);
        Assert.assertEquals(floatArray.get(1), 4.5);
    }

    @Test(description = "Test tuple refinition with union type 1")
    public void testVarRefWithUnionType1() {
        BValue[] returns = BRunUtil.invoke(result, "testVarRefWithUnionType1");
        validateVarRefWithUnionResults(returns);
    }

    @Test(description = "Test tuple refinition with union type 2")
    public void testVarRefWithUnionType2() {
        BValue[] returns = BRunUtil.invoke(result, "testVarRefWithUnionType2");
        validateVarRefWithUnionResults(returns);
    }

    @Test(description = "Test tuple refinition with union type 3")
    public void testVarRefWithUnionType3() {
        BValue[] returns = BRunUtil.invoke(result, "testVarRefWithUnionType3");
        validateVarRefWithUnionResults(returns);
    }

    private void validateVarRefWithUnionResults(BValue[] returns) {
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 34);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 6.7);
        Assert.assertEquals(returns[2].stringValue(), "Test");
    }

    @Test(description = "Test tuple refinition with union type 4")
    public void testVarRefWithUnionType4() {
        BValue[] returns = BRunUtil.invoke(result, "testVarRefWithUnionType4");
        validateTupleVarRefWithUnitionComplexResults(returns);
    }

    @Test(description = "Test tuple refinition with union type 5")
    public void testVarRefWithUnionType5() {
        BValue[] returns = BRunUtil.invoke(result, "testVarRefWithUnionType5");
        validateTupleVarRefWithUnitionComplexResults(returns);
    }

    private void validateTupleVarRefWithUnitionComplexResults(BValue[] returns) {
        Assert.assertEquals(returns.length, 3);

        BValue val1 = returns[0];
        BRefValueArray refValueArray1 = (BRefValueArray) val1;
        Assert.assertEquals(refValueArray1.get(0).stringValue(), "Test");
        Assert.assertEquals(((BInteger) refValueArray1.get(1)).intValue(), 23);

        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 4.5);

        BValue val2 = returns[2];
        BRefValueArray refValueArray2 = (BRefValueArray) val2;
        Assert.assertEquals(((BFloat) refValueArray2.get(0)).floatValue(), 5.7);
        Assert.assertEquals(refValueArray2.get(1).stringValue(), "Foo");
    }
}
