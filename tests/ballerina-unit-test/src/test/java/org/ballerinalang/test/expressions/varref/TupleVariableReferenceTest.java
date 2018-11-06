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

import org.ballerinalang.launcher.util.BAssertUtil;
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

    private CompileResult result, resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/varref/tuple-variable-reference.bal");
        resultNegative = BCompileUtil.compile("test-src/expressions/varref/tuple-variable-reference-negative.bal");
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

    @Test(description = "Test tuple var reference 4")
    public void testTupleVarRefBasic4() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleVarRefBasic4");
        validateVarRefBasicTestResults(returns);
    }

    @Test(description = "Test tuple var reference 5")
    public void testTupleVarRefBasic5() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleVarRefBasic5");
        validateVarRefBasicTestResults(returns);
    }

    @Test(description = "Test tuple var reference 6")
    public void testTupleVarRefBasic6() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleVarRefBasic6");
        validateVarRefBasicTestResults(returns);
    }

    @Test(description = "Test tuple var reference 7")
    public void testTupleVarRefBasic7() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleVarRefBasic7");
        validateVarRefBasicTestResults(returns);
    }

    @Test(description = "Test tuple var reference 8")
    public void testTupleVarRefBasic8() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleVarRefBasic8");
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(returns[0].stringValue(), "UpdatedBallerina");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 453);
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 12.34);
    }

    @Test(description = "Test tuple var reference 9")
    public void testTupleVarRefBasic9() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleVarRefBasic9");
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(returns[0].stringValue(), "UpdatedBallerina");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 657);
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 76.8);
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

    @Test(description = "Test tuple var reference with array 1")
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

    @Test(description = "Test tuple var reference with array 2")
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

    @Test(description = "Test tuple var reference with array 3")
    public void testTupleVarRefWithArray3() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleVarRefWithArray3");
        validateVarRefArrayResults(returns);
    }

    @Test(description = "Test tuple var reference with array 4")
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

    @Test(description = "Test tuple reference with union type 1")
    public void testVarRefWithUnionType1() {
        BValue[] returns = BRunUtil.invoke(result, "testVarRefWithUnionType1");
        validateVarRefWithUnionResults(returns);
    }

    @Test(description = "Test tuple reference with union type 2")
    public void testVarRefWithUnionType2() {
        BValue[] returns = BRunUtil.invoke(result, "testVarRefWithUnionType2");
        validateVarRefWithUnionResults(returns);
    }

    @Test(description = "Test tuple reference with union type 3")
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

    @Test(description = "Test tuple reference with union type 4")
    public void testVarRefWithUnionType4() {
        BValue[] returns = BRunUtil.invoke(result, "testVarRefWithUnionType4");
        validateTupleVarRefWithUnitionComplexResults(returns);
    }

    @Test(description = "Test tuple reference with union type 5")
    public void testVarRefWithUnionType5() {
        BValue[] returns = BRunUtil.invoke(result, "testVarRefWithUnionType5");
        validateTupleVarRefWithUnitionComplexResults(returns);
    }

    private void validateTupleVarRefWithUnitionComplexResults(BValue[] returns) {
        Assert.assertEquals(returns.length, 3);

        BValue value1 = returns[0];
        BRefValueArray refValueArray1 = (BRefValueArray) value1;
        Assert.assertEquals(refValueArray1.get(0).stringValue(), "TestUpdated");
        Assert.assertEquals(((BInteger) refValueArray1.get(1)).intValue(), 23);

        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 4.5);

        BValue value2 = returns[2];
        BRefValueArray refValueArray2 = (BRefValueArray) value2;
        Assert.assertEquals(((BFloat) refValueArray2.get(0)).floatValue(), 5.7);
        Assert.assertEquals(refValueArray2.get(1).stringValue(), "FooUpdated");
    }

    @Test(description = "Test tuple var ref with index and field based var refs")
    public void testFieldAndIndexBasedVarRefs() {
        BValue[] returns = BRunUtil.invoke(result, "testFieldAndIndexBasedVarRefs");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2002);
        Assert.assertEquals(returns[1].stringValue(), "S1");
    }

    @Test
    public void testNegativeTupleVariablesReferences() {
        Assert.assertEquals(resultNegative.getErrorCount(), 20);
        int i = -1;
        String errorMsg1 = "incompatible types: expected ";

        BAssertUtil.validateError(resultNegative, ++i, errorMsg1 +
                "'(string,int,float)', found '(string,int,float,string)'", 19, 17);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg1 +
                "'(string,int,float)', found '(string,int)'", 24, 17);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg1 +
                "'(string,int,float)', found '(int,string,boolean)'", 29, 17);
        BAssertUtil.validateError(resultNegative, ++i, "undefined symbol 'e'", 34, 15);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg1 +
                "'(string,int,float,other)', found '(int,string,boolean,int)'", 34, 20);
        BAssertUtil.validateError(resultNegative, ++i, "redeclared symbol 's'", 40, 20);
        BAssertUtil.validateError(resultNegative, ++i, "redeclared symbol 'i'", 40, 23);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg1 + "'string', found 'int'", 45, 10);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg1 + "'int', found 'string'", 46, 10);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg1 + "'float', found 'boolean'", 47, 10);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg1 + "'(string,int,float)', found '(float,boolean,int)'",
                52, 20);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg1 + "'(string,int)', found '(int,string)'", 62, 16);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg1 + "'float', found 'boolean'", 63, 10);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg1 +
                "'(Foo,(BarObj,FooObj))', found '(Bar,(FooObj,BarObj))'", 72, 26);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg1 +
                "'((Foo,(BarObj,FooObj)),Bar)', found '((Bar,(FooObj,BarObj)),Foo)'", 72, 26);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg1 +
                "'(Foo,(BarObj,FooObj),Bar)', found '(Bar,FooObj,Foo)'", 81, 24);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg1 +
                "'(int,Foo,(BarObj,string,FooObj),Bar,boolean)', found '(Bar,int,(FooObj,string,BarObj),Foo,boolean)'",
                90, 35);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg1 +
                "'((string,(int,(boolean,int))),(float,int))', found 'any'", 120, 36);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg1 +
                "'((string,(int,(boolean,int))),(float,int))', found '(string,int,boolean,int,float,int)'", 127, 36);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg1 +
                "'((string,(int,(boolean,int))),(float,int))', found 'any'", 132, 84);
    }
}
