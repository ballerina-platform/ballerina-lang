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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BByte;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for tuple variable references.
 *
 * @since 0.983.0
 */
public class TupleVariableReferenceTest {

    private CompileResult result, resultNegative, resultSemanticsNegative;

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
        Assert.assertEquals(val1.getClass(), BValueArray.class);
        BValueArray intArray = ((BValueArray) val1);
        Assert.assertEquals(intArray.getInt(0), 123);
        Assert.assertEquals(intArray.getInt(1), 345);
        Assert.assertTrue(((BBoolean) returns[++i]).booleanValue());
        BValue val2 = returns[++i];
        Assert.assertEquals(val2.getClass(), BValueArray.class);
        BValueArray floatArray = ((BValueArray) val2);
        Assert.assertEquals(floatArray.getFloat(0), 2.3);
        Assert.assertEquals(floatArray.getFloat(1), 4.5);
    }

    @Test(description = "Test tuple var reference with array 2")
    public void testTupleVarRefWithArray2() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleVarRefWithArray2");
        Assert.assertEquals(returns.length, 4);
        int i = -1;
        BValue val1 = returns[++i];
        Assert.assertEquals(val1.getClass(), BValueArray.class);
        BValueArray stringArray = (BValueArray) val1;
        Assert.assertEquals(stringArray.getString(0), "A");
        Assert.assertEquals(stringArray.getString(1), "B");

        BValue val2 = returns[++i];
        Assert.assertEquals(val2.getClass(), BValueArray.class);
        BValueArray intArray = ((BValueArray) val2);
        Assert.assertEquals(intArray.getInt(0), 123);
        Assert.assertEquals(intArray.getInt(1), 345);

        BValue val3 = returns[++i];
        Assert.assertEquals(val3.getClass(), BValueArray.class);
        BValueArray bBooleanArray = (BValueArray) val3;
        Assert.assertEquals(bBooleanArray.getBoolean(0), 1);
        Assert.assertEquals(bBooleanArray.getBoolean(1), 0);

        BValue val4 = returns[++i];
        Assert.assertEquals(val4.getClass(), BValueArray.class);
        BValueArray floatArray = ((BValueArray) val4);
        Assert.assertEquals(floatArray.getFloat(0), 2.3);
        Assert.assertEquals(floatArray.getFloat(1), 4.5);
    }

    @Test(description = "Test tuple var reference with array 3")
    public void testTupleVarRefWithArray3() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleVarRefWithArray3");
        validateVarRefArrayResults(returns);
    }

    private void validateVarRefArrayResults(BValue[] returns) {
        Assert.assertEquals(returns.length, 3);
        int i = -1;
        BValue val1 = returns[++i];
        BValueArray refValueArray1 = (BValueArray) val1;
        BValueArray stringArray1 = (BValueArray) refValueArray1.getRefValue(0);
        Assert.assertEquals(stringArray1.getString(0), "A");
        Assert.assertEquals(stringArray1.getString(1), "B");

        BValueArray stringArray2 = (BValueArray) refValueArray1.getRefValue(1);
        Assert.assertEquals(stringArray2.getString(0), "C");
        Assert.assertEquals(stringArray2.getString(1), "D");

        BValue val2 = returns[++i];
        BValueArray refValueArray2 = (BValueArray) val2;
        BValueArray intArray1 = ((BValueArray) refValueArray2.getRefValue(0));
        Assert.assertEquals(intArray1.getInt(0), 123);
        Assert.assertEquals(intArray1.getInt(1), 345);

        BValueArray intArray2 = ((BValueArray) refValueArray2.getRefValue(1));
        Assert.assertEquals(intArray2.getInt(0), 12);
        Assert.assertEquals(intArray2.getInt(1), 34);
        Assert.assertEquals(intArray2.getInt(2), 56);

        BValue val3 = returns[++i];
        Assert.assertEquals(val3.getClass(), BValueArray.class);
        BValueArray floatArray = ((BValueArray) val3);
        Assert.assertEquals(floatArray.getFloat(0), 2.3);
        Assert.assertEquals(floatArray.getFloat(1), 4.5);
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
        BValueArray refValueArray1 = (BValueArray) value1;
        Assert.assertEquals(refValueArray1.getRefValue(0).stringValue(), "TestUpdated");
        Assert.assertEquals(((BInteger) refValueArray1.getRefValue(1)).intValue(), 23);

        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 4.5);

        BValue value2 = returns[2];
        BValueArray refValueArray2 = (BValueArray) value2;
        Assert.assertEquals(((BFloat) refValueArray2.getRefValue(0)).floatValue(), 5.7);
        Assert.assertEquals(refValueArray2.getRefValue(1).stringValue(), "FooUpdated");
    }

    @Test(groups = { "disableOnOldParser" })
    public void testTupleVariablesReferencesSemanticsNegative() {
        resultSemanticsNegative = BCompileUtil.compile("test-src/expressions/varref/tuple-variable-reference" +
                "-semantics-negative.bal");
        int i = -1;
        String errorMsg1 = "incompatible types: expected ";

        BAssertUtil.validateError(resultSemanticsNegative, ++i, "tuple and expression size does not match", 19, 17);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "tuple and expression size does not match", 24, 17);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, errorMsg1 + "'string', found 'int'", 29, 18);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, errorMsg1 + "'int', found 'string'", 29, 22);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, errorMsg1 + "'float', found 'boolean'", 29, 30);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "undefined symbol 'e'", 34, 15);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, errorMsg1 + "'string', found 'int'", 34, 21);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, errorMsg1 + "'int', found 'string'", 34, 25);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, errorMsg1 + "'float', found 'boolean'", 34, 33);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "redeclared symbol 's'", 40, 20);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "redeclared symbol 'i'", 40, 23);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, errorMsg1 + "'string', found 'int'", 45, 10);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, errorMsg1 + "'int', found 'string'", 46, 10);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, errorMsg1 + "'float', found 'boolean'", 47, 10);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, errorMsg1 + "'[string,int,float]', found '[float," +
                        "boolean,int]'", 52, 20);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, errorMsg1 + "'string', found 'int'", 62, 17);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, errorMsg1 + "'int', found 'string'", 62, 21);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, errorMsg1 + "'float', found 'boolean'", 63, 10);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, errorMsg1 + "'Foo', found 'Bar'", 72, 28);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, errorMsg1 + "'BarObj', found 'FooObj'", 72, 34);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, errorMsg1 + "'FooObj', found 'BarObj'", 72, 42);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, errorMsg1 + "'Bar', found 'Foo'", 72, 52);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, errorMsg1 + "'Foo', found 'Bar'", 81, 25);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, errorMsg1 + "'[BarObj,FooObj]', found 'FooObj'", 81,
                30);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, errorMsg1 + "'Bar', found 'Foo'", 81, 38);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, errorMsg1 + "'int', found 'Bar'", 90, 36);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, errorMsg1 + "'Foo', found 'int'", 90, 41);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, errorMsg1 + "'BarObj', found 'FooObj'", 90, 46);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, errorMsg1 + "'FooObj', found 'BarObj'", 90, 60);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, errorMsg1 + "'Bar', found 'Foo'", 90, 69);
        BAssertUtil.validateError(resultSemanticsNegative, ++i,
                errorMsg1 + "'[[string,[int,[boolean,int]]],[float,int]]', found 'any'", 127, 36);
        BAssertUtil.validateError(resultSemanticsNegative, ++i,
                errorMsg1 + "'[[string,[int,[boolean,int]]],[float,int]]', found '[string,int,boolean,int,float,int]'",
                134, 36);
        BAssertUtil.validateError(resultSemanticsNegative, ++i,
                errorMsg1 + "'[[string,[int,[boolean,int]]],[float,int]]', found 'any'", 139, 84);
        BAssertUtil.validateError(resultSemanticsNegative, ++i,
                "invalid expr in assignment lhs", 160, 33);
        Assert.assertEquals(resultSemanticsNegative.getErrorCount(), i + 1);
    }

    @Test(groups = { "disableOnOldParser" })
    public void testTupleVariablesReferencesDataFlowNegative() {
        resultSemanticsNegative = BCompileUtil.compile(
                "test-src/expressions/varref/tuple_variable_reference_dataflow_negative.bal");
        int i = 0;
        BAssertUtil.validateError(resultSemanticsNegative, i++, "cannot assign a value to final 's1'", 20, 6);
        BAssertUtil.validateError(resultSemanticsNegative, i++, "cannot assign a value to final 'f1'", 20, 10);
        BAssertUtil.validateError(resultSemanticsNegative, i++, "cannot assign a value to final 's2'", 24, 6);
        BAssertUtil.validateError(resultSemanticsNegative, i++, "cannot assign a value to final 'f2'", 24, 11);
        BAssertUtil.validateError(resultSemanticsNegative, i++, "cannot assign a value to final 'b2'", 24, 15);
        BAssertUtil.validateError(resultSemanticsNegative, i++, "cannot assign a value to final 'n2'", 24, 23);
        BAssertUtil.validateError(resultSemanticsNegative, i++, "cannot assign a value to final 's2'", 25, 5);
        BAssertUtil.validateError(resultSemanticsNegative, i++, "cannot assign a value to final 'f2'", 26, 6);
        BAssertUtil.validateError(resultSemanticsNegative, i++, "cannot assign a value to final 'b2'", 26, 10);
        Assert.assertEquals(resultSemanticsNegative.getErrorCount(), i);
    }

    @Test
    public void testNegativeTupleVariablesReferences() {
        Assert.assertEquals(resultNegative.getErrorCount(), 3);
        int i = -1;
        BAssertUtil.validateError(resultNegative, ++i, "variables in a binding pattern must be distinct; found " +
                "duplicate variable 'a'", 20, 9);
        BAssertUtil.validateError(resultNegative, ++i, "variables in a binding pattern must be distinct; found " +
                "duplicate variable 'a'", 27, 10);
        BAssertUtil.validateError(resultNegative, ++i, "variables in a binding pattern must be distinct; found " +
                "duplicate variable 'a'", 27, 13);
    }
}
