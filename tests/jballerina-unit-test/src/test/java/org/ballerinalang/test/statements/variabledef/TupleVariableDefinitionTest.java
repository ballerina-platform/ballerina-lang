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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BByte;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
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
        BValue[] returns = BRunUtil.invoke(result, "testBasic");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(returns[0].stringValue(), "Fooo");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 4);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 6.7);
    }

    @Test(description = "Test tuple basic recursive definition")
    public void testBasicRecursive1() {
        BValue[] returns = BRunUtil.invoke(result, "testBasicRecursive1");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(returns[0].stringValue(), "Fooo");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 4);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 6.7);
    }

    @Test(description = "Test tuple basic recursive definition")
    public void testBasicRecursive2() {
        BValue[] returns = BRunUtil.invoke(result, "testBasicRecursive2");
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(returns[0].stringValue(), "Fooo");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 34);
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), 6.7);
    }

    @Test(description = "Test tuple complex recursive definition")
    public void testComplexRecursive() {
        BValue[] returns = BRunUtil.invoke(result, "testComplexRecursive");
        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(returns[0].stringValue(), "Bal");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 3);
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
        Assert.assertEquals(((BByte) returns[3]).byteValue(), 34);
        Assert.assertEquals(((BFloat) returns[4]).floatValue(), 5.6);
        Assert.assertEquals(((BInteger) returns[5]).intValue(), 45);
    }


    @Test(description = "Test tuple recursive definition with expression on rhs")
    public void testRecursiveWithExpression() {
        BValue[] returns = BRunUtil.invoke(result, "testRecursiveWithExpression");
        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(returns[0].stringValue(), "Bal");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 3);
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
        Assert.assertEquals(((BByte) returns[3]).byteValue(), 34);
        Assert.assertEquals(((BFloat) returns[4]).floatValue(), 5.6);
        Assert.assertEquals(((BInteger) returns[5]).intValue(), 45);
    }

    @Test(description = "Test tuple binding with records and objects 1")
    public void testTupleBindingWithRecordsAndObjects() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleBindingWithRecordsAndObjects1");
        Assert.assertEquals(returns.length, 9);
        int i = -1;
        Assert.assertEquals(returns[++i].stringValue(), "Test");
        Assert.assertEquals(((BInteger) returns[++i]).intValue(), 23);
        Assert.assertEquals(((BInteger) returns[++i]).intValue(), 34);
        Assert.assertTrue(((BBoolean) returns[++i]).booleanValue());
        Assert.assertEquals(returns[++i].stringValue(), "Fooo");
        Assert.assertEquals(((BFloat) returns[++i]).floatValue(), 3.7);
        Assert.assertEquals(((BByte) returns[++i]).byteValue(), 23);
        Assert.assertTrue(((BBoolean) returns[++i]).booleanValue());
        Assert.assertEquals(((BInteger) returns[++i]).intValue(), 56);
    }

    @Test(description = "Test tuple binding with records and objects 2")
    public void testTupleBindingWithRecordsAndObjects2() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleBindingWithRecordsAndObjects2");
        Assert.assertEquals(returns.length, 11);
        int i = -1;
        Assert.assertEquals(returns[++i].stringValue(), "Test");
        Assert.assertEquals(returns[++i].stringValue(), "Test");
        Assert.assertEquals(((BInteger) returns[++i]).intValue(), 23);
        Assert.assertEquals(((BInteger) returns[++i]).intValue(), 34);
        Assert.assertTrue(((BBoolean) returns[++i]).booleanValue());
        Assert.assertEquals(returns[++i].stringValue(), "Fooo");
        Assert.assertEquals(((BFloat) returns[++i]).floatValue(), 3.7);
        Assert.assertEquals(((BByte) returns[++i]).byteValue(), 23);
        Assert.assertTrue(((BBoolean) returns[++i]).booleanValue());
        Assert.assertEquals(((BInteger) returns[++i]).intValue(), 56);
        Assert.assertEquals(((BInteger) returns[++i]).intValue(), 56);
    }

    @Test(description = "Test tuple binding with records and objects")
    public void testRecordInsideTuple() {
        BValue[] returns = BRunUtil.invoke(result, "testRecordInsideTuple");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(returns[0].stringValue(), "Peter Parker");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 12);
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
    }

    @Test(description = "Test tuple var definition with var declaration 1")
    public void testTupleVarDefinition1() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleVarDef1");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 123);
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
    }


    @Test(description = "Test tuple var definition with var declaration 2")
    public void testTupleVarDefinition2() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleVarDef2");
        Assert.assertEquals(returns.length, 9);
        int i = -1;
        Assert.assertEquals(returns[++i].stringValue(), "Test");
        Assert.assertEquals(((BInteger) returns[++i]).intValue(), 23);
        Assert.assertEquals(((BInteger) returns[++i]).intValue(), 34);
        Assert.assertTrue(((BBoolean) returns[++i]).booleanValue());
        Assert.assertEquals(returns[++i].stringValue(), "Fooo");
        Assert.assertEquals(((BFloat) returns[++i]).floatValue(), 3.7);
        Assert.assertEquals(((BByte) returns[++i]).byteValue(), 23);
        Assert.assertTrue(((BBoolean) returns[++i]).booleanValue());
        Assert.assertEquals(((BInteger) returns[++i]).intValue(), 56);
    }

    @Test(description = "Test tuple var definition with array 1")
    public void testTupleVarDefWithArray1() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleVarDefWithArray1");
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

    @Test(description = "Test tuple var definition with array 2")
    public void testTupleVarDefWithArray2() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleVarDefWithArray2");
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

    @Test(description = "Test tuple var definition with array 3")
    public void testTupleVarDefWithArray3() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleVarDefWithArray3");
        validateArrayResults(returns);
    }

    @Test(description = "Test tuple var definition with array 4")
    public void testTupleVarDefWithArray4() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleVarDefWithArray4");
        validateArrayResults(returns);
    }

    private void validateArrayResults(BValue[] returns) {
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

    @Test(description = "Test tuple recursive definition with var on lhs 1")
    public void testRecursiveExpressionWithVar1() {
        BValue[] returns = BRunUtil.invoke(result, "testRecursiveExpressionWithVar1");
        validateRecursiveVarRefTestResults(returns);
    }

    @Test(description = "Test tuple recursive definition with var on lhs 2")
    public void testRecursiveExpressionWithVar2() {
        BValue[] returns = BRunUtil.invoke(result, "testRecursiveExpressionWithVar2");
        validateRecursiveVarRefTestResults(returns);
    }

    @Test(description = "Test tuple recursive definition with var on lhs 3")
    public void testRecursiveExpressionWithVar3() {
        BValue[] returns = BRunUtil.invoke(result, "testRecursiveExpressionWithVar3");
        validateRecursiveVarRefTestResults(returns);
    }

    @Test(description = "Test tuple recursive definition with var on lhs 4")
    public void testRecursiveExpressionWithVar4() {
        BValue[] returns = BRunUtil.invoke(result, "testRecursiveExpressionWithVar4");
        validateRecursiveVarRefTestResults(returns);
    }

    private void validateRecursiveVarRefTestResults(BValue[] returns) {
        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(returns[0].stringValue(), "Bal");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 3);
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 34);
        Assert.assertEquals(((BFloat) returns[4]).floatValue(), 5.6);
        Assert.assertEquals(((BInteger) returns[5]).intValue(), 45);
    }

    @Test(description = "Test tuple definition with union type 1")
    public void testVarDefWithUnionType1() {
        BValue[] returns = BRunUtil.invoke(result, "testVarDefWithUnionType1");
        validateVarDefWithUnionResults(returns);
    }

    @Test(description = "Test tuple definition with union type 2")
    public void testVarDefWithUnionType2() {
        BValue[] returns = BRunUtil.invoke(result, "testVarDefWithUnionType2");
        validateVarDefWithUnionResults(returns);
    }

    @Test(description = "Test tuple definition with union type 3")
    public void testVarDefWithUnionType3() {
        BValue[] returns = BRunUtil.invoke(result, "testVarDefWithUnionType3");
        validateVarDefWithUnionResults(returns);
    }

    private void validateVarDefWithUnionResults(BValue[] returns) {
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 34);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 6.7);
        Assert.assertEquals(returns[2].stringValue(), "Test");
    }

    @Test(description = "Test tuple definition with union type 4")
    public void testVarDefWithUnionType4() {
        BValue[] returns = BRunUtil.invoke(result, "testVarDefWithUnionType4");
        validateTupleVarDefWithUnitionComplexResults(returns);
    }

    @Test(description = "Test tuple definition with union type 5")
    public void testVarDefWithUnionType5() {
        BValue[] returns = BRunUtil.invoke(result, "testVarDefWithUnionType5");
        validateTupleVarDefWithUnitionComplexResults(returns);
    }

    @Test(description = "Test tuple definition with union type 6")
    public void testVarDefWithUnionType6() {
        BValue[] returns = BRunUtil.invoke(result, "testVarDefWithUnionType6");
        Assert.assertEquals(returns.length, 2);

        BString val1 = (BString) returns[0];
        Assert.assertEquals(val1.stringValue(), "Test");
        BInteger val2 = (BInteger) returns[1];
        Assert.assertEquals(val2.intValue(), 23);
    }

    @Test(description = "Test tuple variable with ignore variable")
    public void testIgnoreVariable() {
        BValue[] returns = BRunUtil.invoke(result, "testIgnoreVariable");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 23);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 24);
    }

    private void validateTupleVarDefWithUnitionComplexResults(BValue[] returns) {
        Assert.assertEquals(returns.length, 3);

        BValue val1 = returns[0];
        BValueArray refValueArray1 = (BValueArray) val1;
        Assert.assertEquals(refValueArray1.getRefValue(0).stringValue(), "Test");
        Assert.assertEquals(((BInteger) refValueArray1.getRefValue(1)).intValue(), 23);

        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 4.5);

        BValue val2 = returns[2];
        BValueArray refValueArray2 = (BValueArray) val2;
        Assert.assertEquals(((BFloat) refValueArray2.getRefValue(0)).floatValue(), 5.7);
        Assert.assertEquals(refValueArray2.getRefValue(1).stringValue(), "Foo");
    }

    @Test
    public void testNegativeTupleVariables() {
        int i = -1;
        String errorMsg1 = "invalid tuple binding pattern; member variable count mismatch with member type count";
        String errorMsg2 = "invalid tuple binding pattern: expected a tuple type, but found ";
        String errorMsg3 = "tuple and expression size does not match";
        String errorMsg4 = "incompatible types: expected ";

        BAssertUtil.validateError(resultNegative, ++i, errorMsg1, 19, 26);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg1, 23, 26);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg1, 24, 26);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg2 + "'int'", 25, 34);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg3, 29, 41);
        BAssertUtil.validateError(resultNegative, ++i, errorMsg3, 30, 48);
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
                "no new variables on left side", 106, 26);
        BAssertUtil.validateError(resultNegative, ++i,
                                  "invalid tuple binding pattern: expected a tuple type, but found '(string|int)'",
                                  110, 16);

        Assert.assertEquals(resultNegative.getErrorCount(), i + 1);
    }
}
