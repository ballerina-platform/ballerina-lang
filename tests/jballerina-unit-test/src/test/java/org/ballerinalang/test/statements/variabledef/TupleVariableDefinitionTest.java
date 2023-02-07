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
package org.ballerinalang.test.statements.variabledef;

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for tuple variable definition.
 * <p>
 * (string, int, float) (s, i, f) = ("Test", 45, 2.3);
 *
 * @since 0.982.0
 */
public class TupleVariableDefinitionTest {

    private CompileResult result, resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/variabledef/tuple-variable-definition.bal");
        resultNegative = BCompileUtil.compile("test-src/statements/variabledef/tuple-variable-definition-negative.bal");
    }

    @Test(description = "Test tuple basic variable definition")
    public void testBasic() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testBasic");
        Assert.assertEquals(returns.size(), 3);
        Assert.assertEquals(returns.get(0).toString(), "Fooo");
        Assert.assertEquals(returns.get(1), 4L);
        Assert.assertEquals(returns.get(2), 6.7);
    }

    @Test(description = "Test tuple basic recursive definition")
    public void testBasicRecursive1() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testBasicRecursive1");
        Assert.assertEquals(returns.size(), 3);
        Assert.assertEquals(returns.get(0).toString(), "Fooo");
        Assert.assertEquals(returns.get(1), 4L);
        Assert.assertEquals(returns.get(2), 6.7);
    }

    @Test(description = "Test tuple basic recursive definition")
    public void testBasicRecursive2() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testBasicRecursive2");
        Assert.assertEquals(returns.size(), 4);
        Assert.assertEquals(returns.get(0).toString(), "Fooo");
        Assert.assertEquals(returns.get(1), 34L);
        Assert.assertTrue((Boolean) returns.get(2));
        Assert.assertEquals(returns.get(3), 6.7);
    }

    @Test(description = "Test tuple complex recursive definition")
    public void testComplexRecursive() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testComplexRecursive");
        Assert.assertEquals(returns.size(), 6);
        Assert.assertEquals(returns.get(0).toString(), "Bal");
        Assert.assertEquals(returns.get(1), 3L);
        Assert.assertTrue((Boolean) returns.get(2));
        Assert.assertEquals(returns.get(3), 34);
        Assert.assertEquals(returns.get(4), 5.6);
        Assert.assertEquals(returns.get(5), 45L);
    }

    @Test(description = "Test tuple recursive definition with expression on rhs")
    public void testRecursiveWithExpression() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testRecursiveWithExpression");
        Assert.assertEquals(returns.size(), 6);
        Assert.assertEquals(returns.get(0).toString(), "Bal");
        Assert.assertEquals(returns.get(1), 3L);
        Assert.assertTrue((Boolean) returns.get(2));
        Assert.assertEquals(returns.get(3), 34);
        Assert.assertEquals(returns.get(4), 5.6);
        Assert.assertEquals(returns.get(5), 45L);
    }

    @Test(description = "Test tuple binding with records and objects 1")
    public void testTupleBindingWithRecordsAndObjects() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testTupleBindingWithRecordsAndObjects1");
        Assert.assertEquals(returns.size(), 9);
        int i = -1;
        Assert.assertEquals(returns.get(++i).toString(), "Test");
        Assert.assertEquals(returns.get(++i), 23L);
        Assert.assertEquals(returns.get(++i), 34L);
        Assert.assertTrue((Boolean) returns.get(++i));
        Assert.assertEquals(returns.get(++i).toString(), "Fooo");
        Assert.assertEquals(returns.get(++i), 3.7);
        Assert.assertEquals(returns.get(++i), 23);
        Assert.assertTrue((Boolean) returns.get(++i));
        Assert.assertEquals(returns.get(++i), 56L);
    }

    @Test(description = "Test tuple binding with records and objects 2")
    public void testTupleBindingWithRecordsAndObjects2() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testTupleBindingWithRecordsAndObjects2");
        Assert.assertEquals(returns.size(), 11);
        int i = -1;
        Assert.assertEquals(returns.get(++i).toString(), "Test");
        Assert.assertEquals(returns.get(++i).toString(), "Test");
        Assert.assertEquals(returns.get(++i), 23L);
        Assert.assertEquals(returns.get(++i), 34L);
        Assert.assertTrue((Boolean) returns.get(++i));
        Assert.assertEquals(returns.get(++i).toString(), "Fooo");
        Assert.assertEquals(returns.get(++i), 3.7);
        Assert.assertEquals(returns.get(++i), 23);
        Assert.assertTrue((Boolean) returns.get(++i));
        Assert.assertEquals(returns.get(++i), 56L);
        Assert.assertEquals(returns.get(++i), 56L);
    }

    @Test(description = "Test tuple binding with records and objects")
    public void testRecordInsideTuple() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testRecordInsideTuple");
        Assert.assertEquals(returns.size(), 3);
        Assert.assertEquals(returns.get(0).toString(), "Peter Parker");
        Assert.assertEquals(returns.get(1), 12L);
        Assert.assertTrue((Boolean) returns.get(2));
    }

    @Test(description = "Test tuple var definition with var declaration 1")
    public void testTupleVarDefinition1() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testTupleVarDef1");
        Assert.assertEquals(returns.size(), 3);
        Assert.assertEquals(returns.get(0).toString(), "Ballerina");
        Assert.assertEquals(returns.get(1), 123L);
        Assert.assertTrue((Boolean) returns.get(2));
    }

    @Test(description = "Test tuple var definition with var declaration 2")
    public void testTupleVarDefinition2() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testTupleVarDef2");
        Assert.assertEquals(returns.size(), 9);
        int i = -1;
        Assert.assertEquals(returns.get(++i).toString(), "Test");
        Assert.assertEquals(returns.get(++i), 23L);
        Assert.assertEquals(returns.get(++i), 34L);
        Assert.assertTrue((Boolean) returns.get(++i));
        Assert.assertEquals(returns.get(++i).toString(), "Fooo");
        Assert.assertEquals(returns.get(++i), 3.7);
        Assert.assertEquals(returns.get(++i), 23);
        Assert.assertTrue((Boolean) returns.get(++i));
        Assert.assertEquals(returns.get(++i), 56L);
    }

    @Test
    public void testTupleVarDefinition3() {
        BRunUtil.invoke(result, "testTupleVarDef3");
    }

    @Test
    public void testTupleVarDefinition4() {
        BRunUtil.invoke(result, "testTupleVarDef4");
    }

    @Test(description = "Test tuple var definition with array 1")
    public void testTupleVarDefWithArray1() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testTupleVarDefWithArray1");
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

    @Test(description = "Test tuple var definition with array 2")
    public void testTupleVarDefWithArray2() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testTupleVarDefWithArray2");
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
        Assert.assertEquals(intArray.getInt(0), 123L);
        Assert.assertEquals(intArray.getInt(1), 345L);

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

    @Test(description = "Test tuple var definition with array 3")
    public void testTupleVarDefWithArray3() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testTupleVarDefWithArray3");
        validateArrayResults(returns);
    }

    @Test(description = "Test tuple var definition with array 4")
    public void testTupleVarDefWithArray4() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testTupleVarDefWithArray4");
        validateArrayResults(returns);
    }

    private void validateArrayResults(BArray returns) {
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
        Assert.assertEquals(intArray1.getInt(0), 123L);
        Assert.assertEquals(intArray1.getInt(1), 345L);

        BArray intArray2 = ((BArray) refValueArray2.getRefValue(1));
        Assert.assertEquals(intArray2.getInt(0), 12L);
        Assert.assertEquals(intArray2.getInt(1), 34L);
        Assert.assertEquals(intArray2.getInt(2), 56L);

        Object val3 = returns.get(++i);
        Assert.assertTrue(val3 instanceof BArray);
        BArray floatArray = ((BArray) val3);
        Assert.assertEquals(floatArray.getFloat(0), 2.3);
        Assert.assertEquals(floatArray.getFloat(1), 4.5);
    }

    @Test(description = "Test tuple recursive definition with var on lhs 1")
    public void testRecursiveExpressionWithVar1() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testRecursiveExpressionWithVar1");
        validateRecursiveVarRefTestResults(returns);
    }

    @Test(description = "Test tuple recursive definition with var on lhs 2")
    public void testRecursiveExpressionWithVar2() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testRecursiveExpressionWithVar2");
        validateRecursiveVarRefTestResults(returns);
    }

    @Test(description = "Test tuple recursive definition with var on lhs 3")
    public void testRecursiveExpressionWithVar3() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testRecursiveExpressionWithVar3");
        validateRecursiveVarRefTestResults(returns);
    }

    @Test(description = "Test tuple recursive definition with var on lhs 4")
    public void testRecursiveExpressionWithVar4() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testRecursiveExpressionWithVar4");
        validateRecursiveVarRefTestResults(returns);
    }

    private void validateRecursiveVarRefTestResults(BArray returns) {
        Assert.assertEquals(returns.size(), 6);
        Assert.assertEquals(returns.get(0).toString(), "Bal");
        Assert.assertEquals(returns.get(1), 3L);
        Assert.assertTrue((Boolean) returns.get(2));
        Assert.assertEquals(returns.get(3), 34L);
        Assert.assertEquals(returns.get(4), 5.6);
        Assert.assertEquals(returns.get(5), 45L);
    }

    @Test(description = "Test tuple definition with union type 1")
    public void testVarDefWithUnionType1() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testVarDefWithUnionType1");
        validateVarDefWithUnionResults(returns);
    }

    @Test(description = "Test tuple definition with union type 2")
    public void testVarDefWithUnionType2() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testVarDefWithUnionType2");
        validateVarDefWithUnionResults(returns);
    }

    @Test(description = "Test tuple definition with union type 3")
    public void testVarDefWithUnionType3() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testVarDefWithUnionType3");
        validateVarDefWithUnionResults(returns);
    }

    private void validateVarDefWithUnionResults(BArray returns) {
        Assert.assertEquals(returns.size(), 3);
        Assert.assertEquals(returns.get(0), 34L);
        Assert.assertEquals(returns.get(1), 6.7);
        Assert.assertEquals(returns.get(2).toString(), "Test");
    }

    @Test(description = "Test tuple definition with union type 4")
    public void testVarDefWithUnionType4() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testVarDefWithUnionType4");
        validateTupleVarDefWithUnitionComplexResults(returns);
    }

    @Test(description = "Test tuple definition with union type 5")
    public void testVarDefWithUnionType5() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testVarDefWithUnionType5");
        validateTupleVarDefWithUnitionComplexResults(returns);
    }

    @Test(description = "Test tuple definition with union type 6")
    public void testVarDefWithUnionType6() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testVarDefWithUnionType6");
        Assert.assertEquals(returns.size(), 2);

        BString val1 = (BString) returns.get(0);
        Assert.assertEquals(val1.toString(), "Test");
        long val2 = (long) returns.get(1);
        Assert.assertEquals(val2, 23L);
    }

    @Test(description = "Test tuple variable with ignore variable")
    public void testIgnoreVariable() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testIgnoreVariable");
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0), 23L);
        Assert.assertEquals(returns.get(1), 24L);
    }

    @Test(description = "Test tuple variable with error BP")
    public void testTupleVariableWithErrorBP() {
        BRunUtil.invoke(result, "testTupleVariableWithErrorBP");
    }

    @Test
    public void testTupleVarDeclWithTypeReferenceTypedExpr() {
        BRunUtil.invoke(result, "testTupleVarDeclWithTypeReferenceTypedExpr");
    }

    @Test
    public void testTupleVarDefWithRestBPContainsErrorBPWithNamedArgs() {
        BRunUtil.invoke(result, "testTupleVarDefWithRestBPContainsErrorBPWithNamedArgs");
    }

    @Test
    public void testTupleVarDefWithRestBPContainsErrorBPWithRestBP() {
        BRunUtil.invoke(result, "testTupleVarDefWithRestBPContainsErrorBPWithRestBP");
    }

    @Test
    public void testReadOnlyListWithListBindingPatternInVarDecl() {
        BRunUtil.invoke(result, "testReadOnlyListWithListBindingPatternInVarDecl");
    }

    private void validateTupleVarDefWithUnitionComplexResults(BArray returns) {
        Assert.assertEquals(returns.size(), 3);

        Object val1 = returns.get(0);
        BArray refValueArray1 = (BArray) val1;
        Assert.assertEquals(refValueArray1.getRefValue(0).toString(), "Test");
        Assert.assertEquals((refValueArray1.getRefValue(1)), 23L);

        Assert.assertEquals(returns.get(1), 4.5);

        Object val2 = returns.get(2);
        BArray refValueArray2 = (BArray) val2;
        Assert.assertEquals((refValueArray2.getRefValue(0)), 5.7);
        Assert.assertEquals(refValueArray2.getRefValue(1).toString(), "Foo");
    }

    @Test
    public void testNegativeTupleVariables() {
        int i = -1;
        String errorMsg1 = "invalid list binding pattern; member variable count mismatch with member type count";
        String errorMsg2 = "invalid list binding pattern: expected an array or a tuple, but found ";
        String errorMsg3 = "tuple and expression size does not match";
        String errorMsg4 = "incompatible types: expected ";
        String errorMsg5 = "invalid usage of list constructor: ";

        BAssertUtil.validateError(resultNegative, ++i, errorMsg1, 19, 5);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg1, 23, 5);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg1, 24, 5);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg2 + "'int'", 25, 34);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg3, 29, 41);
        BAssertUtil.validateError(resultNegative, ++i,
                errorMsg5 + "type 'NoFillerObject' does not have a filler value", 30, 48);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg4 + "'string', found 'int'", 31, 42);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg4 + "'int', found 'float'", 31, 45);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg4 + "'float', found 'string'", 31, 50);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg4 + "'Foo', found 'Bar'", 39, 59);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg4 + "'BarObj', found 'FooObj'", 39, 65);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg4 + "'FooObj', found 'BarObj'", 39, 73);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg4 + "'Bar', found 'Foo'", 39, 83);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg4 + "'Foo', found 'Bar'", 47, 54);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg4 + "'[BarObj,FooObj]', found 'FooObj'", 47, 59);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg4 + "'Bar', found 'Foo'", 47, 67);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg4 + "'int', found 'Bar'", 55, 87);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg4 + "'Foo', found 'int'", 55, 92);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg4 + "'BarObj', found 'FooObj'", 55, 97);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg4 + "'FooObj', found 'BarObj'", 55, 111);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg4 + "'Bar', found 'Foo'", 55, 120);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg4 + "'[[string,[int,[boolean,int]]],[float,int]]', " +
                "found 'any'", 91, 84);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected '[[string,[int,[boolean,int]]],[float,int]]', found 'any'", 101, 84);
        BAssertUtil.validateError(resultNegative, ++i,
                "no new variables on left side", 106, 5);
        BAssertUtil.validateError(resultNegative, ++i, "invalid list binding pattern: " +
                "expected an array or a tuple, but found '(string|int)'", 110, 5);
        BAssertUtil.validateError(resultNegative, ++i, "invalid list binding pattern; member variable count mismatch " +
                "with member type count", 120, 5);
        BAssertUtil.validateError(resultNegative, ++i, "invalid list binding pattern: expected an array or a tuple, " +
                "but found 'Ints'", 128, 5);
        BAssertUtil.validateError(resultNegative, ++i, "invalid list binding pattern: expected an array or a tuple, " +
                "but found 'IntsOrStrings'", 129, 5);
        BAssertUtil.validateError(resultNegative, ++i, "incompatible types: expected '[(string[] & readonly)," +
                "string]', found 'ReadOnlyTuple'", 136, 44);
        BAssertUtil.validateError(resultNegative, ++i, "incompatible types: expected 'int[] & readonly', found " +
                "'int[]'", 140, 9);
        BAssertUtil.validateError(resultNegative, ++i, "incompatible types: expected 'int[] & readonly', found " +
                "'int[]'", 143, 9);

        Assert.assertEquals(resultNegative.getErrorCount(), i + 1);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        resultNegative = null;
    }
}
