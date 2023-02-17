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

import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
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
        Object arr = BRunUtil.invoke(result, "testTupleVarRefBasic1");
        BArray returns = (BArray) arr;
        validateVarRefBasicTestResults(returns);
    }

    @Test(description = "Test tuple var reference 3")
    public void testTupleVarRefBasic3() {
        Object arr = BRunUtil.invoke(result, "testTupleVarRefBasic3");
        BArray returns = (BArray) arr;
        validateVarRefBasicTestResults(returns);
    }

    @Test(description = "Test tuple var reference 4")
    public void testTupleVarRefBasic4() {
        Object arr = BRunUtil.invoke(result, "testTupleVarRefBasic4");
        BArray returns = (BArray) arr;
        validateVarRefBasicTestResults(returns);
    }

    @Test(description = "Test tuple var reference 6")
    public void testTupleVarRefBasic6() {
        Object arr = BRunUtil.invoke(result, "testTupleVarRefBasic6");
        BArray returns = (BArray) arr;
        validateVarRefBasicTestResults(returns);
    }

    @Test(description = "Test tuple var reference 7")
    public void testTupleVarRefBasic7() {
        Object arr = BRunUtil.invoke(result, "testTupleVarRefBasic7");
        BArray returns = (BArray) arr;
        validateVarRefBasicTestResults(returns);
    }

    @Test(description = "Test tuple var reference 8")
    public void testTupleVarRefBasic8() {
        Object arr = BRunUtil.invoke(result, "testTupleVarRefBasic8");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 4);
        Assert.assertEquals(returns.get(0).toString(), "UpdatedBallerina");
        Assert.assertEquals(returns.get(1), 453L);
        Assert.assertFalse((Boolean) returns.get(2));
        Assert.assertEquals(returns.get(3), 12.34);
    }

    @Test(description = "Test tuple var reference 9")
    public void testTupleVarRefBasic9() {
        Object arr = BRunUtil.invoke(result, "testTupleVarRefBasic9");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 4);
        Assert.assertEquals(returns.get(0).toString(), "UpdatedBallerina");
        Assert.assertEquals(returns.get(1), 657L);
        Assert.assertTrue((Boolean) returns.get(2));
        Assert.assertEquals(returns.get(3), 76.8);
    }

    @Test(description = "Test tuple var reference assignment 1")
    public void testTupleVarRefAssignment1() {
        Object arr = BRunUtil.invoke(result, "testTupleVarRefAssignment1");
        BArray returns = (BArray) arr;
        validateVarRefBasicTestResults(returns);
    }

    private void validateVarRefBasicTestResults(BArray returns) {
        Assert.assertEquals(returns.size(), 3);
        Assert.assertEquals(returns.get(0).toString(), "UpdatedBallerina");
        Assert.assertEquals(returns.get(1), 453L);
        Assert.assertFalse((Boolean) returns.get(2));
    }

    @Test(description = "Test tuple var reference assignment 2")
    public void testTupleVarRefAssignment2() {
        Object arr = BRunUtil.invoke(result, "testTupleVarRefAssignment2");
        BArray returns = (BArray) arr;
        validateRedAssignmentResults(returns);
    }

    @Test(description = "Test tuple var reference assignment 3")
    public void testTupleVarRefAssignment3() {
        Object arr = BRunUtil.invoke(result, "testTupleVarRefAssignment3");
        BArray returns = (BArray) arr;
        validateRedAssignmentResults(returns);
    }

    private void validateRedAssignmentResults(BArray returns) {
        Assert.assertEquals(returns.size(), 9);
        int i = -1;
        Assert.assertEquals(returns.get(++i).toString(), "TestUpdate");
        Assert.assertEquals(returns.get(++i), 24L);
        Assert.assertEquals(returns.get(++i), 35L);
        Assert.assertFalse((Boolean) returns.get(++i));
        Assert.assertEquals(returns.get(++i).toString(), "FoooUpdate");
        Assert.assertEquals(returns.get(++i), 4.7);
        Assert.assertEquals(returns.get(++i), 24);
        Assert.assertFalse((Boolean) returns.get(++i));
        Assert.assertEquals(returns.get(++i), 66L);
    }

    @Test(description = "Test tuple var reference with array 1")
    public void testTupleVarRefWithArray1() {
        Object arr = BRunUtil.invoke(result, "testTupleVarRefWithArray1");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 4);
        int i = -1;
        Assert.assertEquals(returns.get(++i).toString(), "Ballerina");
        Object val1 = returns.get(++i);
        Assert.assertTrue(val1 instanceof BArray);
        BArray intArray = ((BArray) val1);
        Assert.assertEquals(intArray.getInt(0), 123L);
        Assert.assertEquals(intArray.getInt(1), 345L);
        Assert.assertTrue((Boolean) returns.get(++i));
        Object val2 = returns.get(++i);
        Assert.assertTrue(val2 instanceof BArray);
        BArray floatArray = ((BArray) val2);
        Assert.assertEquals(floatArray.getFloat(0), 2.3);
        Assert.assertEquals(floatArray.getFloat(1), 4.5);
    }

    @Test(description = "Test tuple var reference with array 2")
    public void testTupleVarRefWithArray2() {
        Object arr = BRunUtil.invoke(result, "testTupleVarRefWithArray2");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 4);
        int i = -1;
        Object val1 = returns.get(++i);
        Assert.assertTrue(val1 instanceof BArray);
        BArray stringArray = (BArray) val1;
        Assert.assertEquals(stringArray.getString(0), "A");
        Assert.assertEquals(stringArray.getString(1), "B");

        Object val2 = returns.get(++i);
        Assert.assertTrue(val2 instanceof BArray);
        BArray intArray = ((BArray) val2);
        Assert.assertEquals(intArray.getInt(0), 123);
        Assert.assertEquals(intArray.getInt(1), 345);

        Object val3 = returns.get(++i);
        Assert.assertTrue(val3 instanceof BArray);
        BArray bBooleanArray = (BArray) val3;
        Assert.assertTrue(bBooleanArray.getBoolean(0));
        Assert.assertFalse(bBooleanArray.getBoolean(1));

        Object val4 = returns.get(++i);
        Assert.assertTrue(val4 instanceof BArray);
        BArray floatArray = ((BArray) val4);
        Assert.assertEquals(floatArray.getFloat(0), 2.3);
        Assert.assertEquals(floatArray.getFloat(1), 4.5);
    }

    @Test(description = "Test tuple var reference with array 3")
    public void testTupleVarRefWithArray3() {
        Object arr = BRunUtil.invoke(result, "testTupleVarRefWithArray3");
        BArray returns = (BArray) arr;
        validateVarRefArrayResults(returns);
    }

    private void validateVarRefArrayResults(BArray returns) {
        Assert.assertEquals(returns.size(), 3);
        int i = -1;
        Object val1 = returns.get(++i);
        BArray refValueArray1 = (BArray) val1;
        BArray stringArray1 = (BArray) refValueArray1.getRefValue(0);
        Assert.assertEquals(stringArray1.getString(0), "A");
        Assert.assertEquals(stringArray1.getString(1), "B");

        BArray stringArray2 = (BArray) refValueArray1.getRefValue(1);
        Assert.assertEquals(stringArray2.getString(0), "C");
        Assert.assertEquals(stringArray2.getString(1), "D");

        Object val2 = returns.get(++i);
        BArray refValueArray2 = (BArray) val2;
        BArray intArray1 = ((BArray) refValueArray2.getRefValue(0));
        Assert.assertEquals(intArray1.getInt(0), 123);
        Assert.assertEquals(intArray1.getInt(1), 345);

        BArray intArray2 = ((BArray) refValueArray2.getRefValue(1));
        Assert.assertEquals(intArray2.getInt(0), 12);
        Assert.assertEquals(intArray2.getInt(1), 34);
        Assert.assertEquals(intArray2.getInt(2), 56);

        Object val3 = returns.get(++i);
        Assert.assertTrue(val3 instanceof BArray);
        BArray floatArray = ((BArray) val3);
        Assert.assertEquals(floatArray.getFloat(0), 2.3);
        Assert.assertEquals(floatArray.getFloat(1), 4.5);
    }

    @Test(description = "Test tuple reference with union type 1")
    public void testVarRefWithUnionType1() {
        Object arr = BRunUtil.invoke(result, "testVarRefWithUnionType1");
        BArray returns = (BArray) arr;
        validateVarRefWithUnionResults(returns);
    }

    @Test(description = "Test tuple reference with union type 2")
    public void testVarRefWithUnionType2() {
        Object arr = BRunUtil.invoke(result, "testVarRefWithUnionType2");
        BArray returns = (BArray) arr;
        validateVarRefWithUnionResults(returns);
    }

    @Test(description = "Test tuple reference with union type 3")
    public void testVarRefWithUnionType3() {
        Object arr = BRunUtil.invoke(result, "testVarRefWithUnionType3");
        BArray returns = (BArray) arr;
        validateVarRefWithUnionResults(returns);
    }

    private void validateVarRefWithUnionResults(BArray returns) {
        Assert.assertEquals(returns.size(), 3);
        Assert.assertEquals(returns.get(0), 34L);
        Assert.assertEquals(returns.get(1), 6.7);
        Assert.assertEquals(returns.get(2).toString(), "Test");
    }

    @Test(description = "Test tuple reference with union type 4")
    public void testVarRefWithUnionType4() {
        Object arr = BRunUtil.invoke(result, "testVarRefWithUnionType4");
        BArray returns = (BArray) arr;
        validateTupleVarRefWithUnitionComplexResults(returns);
    }

    @Test(description = "Test tuple reference with union type 5")
    public void testVarRefWithUnionType5() {
        Object arr = BRunUtil.invoke(result, "testVarRefWithUnionType5");
        BArray returns = (BArray) arr;
        validateTupleVarRefWithUnitionComplexResults(returns);
    }

    private void validateTupleVarRefWithUnitionComplexResults(BArray returns) {
        Assert.assertEquals(returns.size(), 3);

        Object value1 = returns.get(0);
        BArray refValueArray1 = (BArray) value1;
        Assert.assertEquals(refValueArray1.getRefValue(0).toString(), "TestUpdated");
        Assert.assertEquals((refValueArray1.getRefValue(1)), 23L);

        Assert.assertEquals(returns.get(1), 4.5);

        Object value2 = returns.get(2);
        BArray refValueArray2 = (BArray) value2;
        Assert.assertEquals((refValueArray2.getRefValue(0)), 5.7);
        Assert.assertEquals(refValueArray2.getRefValue(1).toString(), "FooUpdated");
    }

    @Test(dataProvider = "dataToTestRestVarRefType", description = "Test tuple rest var ref type")
    public void testNeverWithExpressions(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider
    public Object[] dataToTestRestVarRefType() {
        return new Object[]{
                "testRestVarRefType1",
                "testRestVarRefType2",
                "testRestVarRefType3",
                "testRestVarRefType4",
                "testRestVarRefType5",
                "testRestVarRefType6",
                "testRestVarRefType7",
                "testRestVarRefType8",
                "testRestVarRefType9"
        };
    }

    @Test
    public void testReadOnlyTupleWithListBindingPatternInDestructuringAssignment() {
        BRunUtil.invoke(result, "testReadOnlyTupleWithListBindingPatternInDestructuringAssignment");
    }

    @Test
    public void testTupleVariablesReferencesSemanticsNegative() {
        resultSemanticsNegative = BCompileUtil.compile(
                "test-src/expressions/varref/tuple-variable-reference-semantics-negative.bal");
        int i = -1;
        String errorMsg1 = "incompatible types: expected ";

        BAssertUtil.validateError(resultSemanticsNegative, ++i, "tuple and expression size does not match", 19, 17);
        BAssertUtil.validateError(resultSemanticsNegative, ++i,
                "invalid usage of list constructor: type 'NoFillerObject' does not have a filler value", 24, 17);
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
                "invalid expr in assignment lhs", 160, 5);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, errorMsg1 + "'boolean[]', found " +
                "'[string,boolean...]'", 173, 19);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, errorMsg1 + "'[int,string,string]', found " +
                "'[int,string,boolean...]'", 176, 31);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "self referenced variable 'a'", 180, 26);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "self referenced variable 'b'", 180, 29);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "self referenced variable 'c'", 181, 44);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "self referenced variable 'd'", 181, 47);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "self referenced variable 'f'", 182, 31);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "self referenced variable 'i'", 183, 103);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "self referenced variable 'j'", 183, 107);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "self referenced variable 'l'", 183, 116);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "self referenced variable 'm'", 183, 120);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "self referenced variable 'n'", 183, 124);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "incompatible types: expected '[string[] & readonly," +
                "string]', found 'ReadOnlyTuple'", 192, 14);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "incompatible types: expected '[string[] & readonly," +
                "[int[] & readonly,any]]', found '([int[],ReadOnlyTuple] & readonly)'", 197, 19);
        BAssertUtil.validateError(resultSemanticsNegative, ++i, "incompatible types: expected 'int[] & readonly', " +
                "found 'int[]'", 199, 9);
        Assert.assertEquals(resultSemanticsNegative.getErrorCount(), i + 1);
    }

    @Test
    public void testTupleVariablesReferencesDataFlowNegative() {
        resultSemanticsNegative = BCompileUtil.compile(
                "test-src/expressions/varref/tuple_variable_reference_dataflow_negative.bal");
        int i = 0;
        BAssertUtil.validateWarning(resultSemanticsNegative, i++, "unused variable 's1'", 19, 16);
        BAssertUtil.validateWarning(resultSemanticsNegative, i++, "unused variable 'f1'", 19, 20);
        BAssertUtil.validateError(resultSemanticsNegative, i++, "cannot assign a value to final 's1'", 20, 6);
        BAssertUtil.validateError(resultSemanticsNegative, i++, "cannot assign a value to final 'f1'", 20, 10);
        BAssertUtil.validateWarning(resultSemanticsNegative, i++, "unused variable 's2'", 23, 48);
        BAssertUtil.validateWarning(resultSemanticsNegative, i++, "unused variable 'f2'", 23, 53);
        BAssertUtil.validateWarning(resultSemanticsNegative, i++, "unused variable 'b2'", 23, 57);
        BAssertUtil.validateWarning(resultSemanticsNegative, i++, "unused variable 'n2'", 23, 62);
        BAssertUtil.validateError(resultSemanticsNegative, i++, "cannot assign a value to final 's2'", 24, 6);
        BAssertUtil.validateError(resultSemanticsNegative, i++, "cannot assign a value to final 'f2'", 24, 11);
        BAssertUtil.validateError(resultSemanticsNegative, i++, "cannot assign a value to final 'b2'", 24, 15);
        BAssertUtil.validateError(resultSemanticsNegative, i++, "cannot assign a value to final 'n2'", 24, 23);
        BAssertUtil.validateError(resultSemanticsNegative, i++, "cannot assign a value to final 's2'", 25, 5);
        BAssertUtil.validateError(resultSemanticsNegative, i++, "cannot assign a value to final 'f2'", 26, 6);
        BAssertUtil.validateError(resultSemanticsNegative, i++, "cannot assign a value to final 'b2'", 26, 10);
        Assert.assertEquals(resultSemanticsNegative.getDiagnostics().length, i);
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
