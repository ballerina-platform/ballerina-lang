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
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * TestCases for Record Variable Definitions.
 *
 * @since 0.985.0
 */
@Test
public class RecordVariableDefinitionTest {

    private CompileResult result, resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.
                compile("test-src/statements/variabledef/record-variable-definition-stmt.bal");
        resultNegative = BCompileUtil.
                compile("test-src/statements/variabledef/record-variable-definition-stmt-negative.bal");
    }

    @Test(description = "Test simple record variable definition")
    public void simpleDefinition() {
        BValue[] returns = BRunUtil.invoke(result, "simpleDefinition");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "Peter");
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
    }

    @Test(description = "Test record variable inside record variable")
    public void recordVarInRecordVar() {
        BValue[] returns = BRunUtil.invoke(result, "recordVarInRecordVar");
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(returns[0].stringValue(), "Peter");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 29);
        Assert.assertEquals(returns[2].stringValue(), "Y");
        Assert.assertTrue(((BBoolean) returns[3]).booleanValue());
    }

    @Test(description = "Test record variable inside record variable")
    public void recordVarInRecordVar2() {
        BValue[] returns = BRunUtil.invoke(result, "recordVarInRecordVar2");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "Peter");
        Assert.assertEquals(((BInteger) ((BMap) returns[1]).get("age")).intValue(), 29);
        Assert.assertEquals(((BMap) returns[1]).get("format").stringValue(), "Y");
    }

    @Test(description = "Test record variable inside record variable inside record variable")
    public void recordVarInRecordVarInRecordVar() {
        BValue[] returns = BRunUtil.invoke(result, "recordVarInRecordVarInRecordVar");
        Assert.assertEquals(returns.length, 5);
        Assert.assertEquals(returns[0].stringValue(), "Peter");
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 1000);
        Assert.assertEquals(returns[3].stringValue(), "PG");
        Assert.assertEquals(returns[4].stringValue(), "Colombo 10");
    }

    @Test(description = "Test tuple variable inside record variable")
    public void tupleVarInRecordVar() {
        BValue[] returns = BRunUtil.invoke(result, "tupleVarInRecordVar");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(returns[0].stringValue(), "John");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 20);
        Assert.assertEquals(returns[2].stringValue(), "PG");
    }

    @Test(description = "Test declaring 3 record variables and using the variables")
    public void defineThreeRecordVariables() {
        BValue[] returns = BRunUtil.invoke(result, "defineThreeRecordVariables");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "JohnDoePeterYYMMDD");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 50);
    }

    @Test(description = "Test declaring 3 record variables and using the variables")
    public void recordVariableWithRHSInvocation() {
        BValue[] returns = BRunUtil.invoke(result, "recordVariableWithRHSInvocation");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "Jack Jill");
    }

    @Test(description = "Test declaring 3 record variables and using the variables")
    public void nestedRecordVariableWithRHSInvocation() {
        BValue[] returns = BRunUtil.invoke(result, "nestedRecordVariableWithRHSInvocation");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "Peter Parker");
    }

    @Test(description = "Test rest parameter")
    public void testRestParameter() {
        BValue[] returns = BRunUtil.invoke(result, "testRestParameter");
        Assert.assertTrue(returns[0] instanceof BMap);
        BMap bMap = (BMap) returns[0];
        Assert.assertEquals(bMap.size(), 2);
        Assert.assertEquals(bMap.get("work").stringValue(), "SE");
        Assert.assertEquals(((BInteger) ((BMap) bMap.get("other")).get("age")).intValue(), 99);
        Assert.assertEquals(((BMap) bMap.get("other")).get("format").stringValue(), "MM");
    }

    @Test(description = "Test rest parameter in nested record variable")
    public void testNestedRestParameter() {
        BValue[] returns = BRunUtil.invoke(result, "testNestedRestParameter");
        Assert.assertTrue(returns[0] instanceof BMap);
        BMap bMap = (BMap) returns[0];
        Assert.assertEquals(bMap.size(), 1);
        Assert.assertEquals(((BInteger) bMap.get("year")).intValue(), 1990);

        BMap bMap2 = (BMap) returns[1];
        Assert.assertEquals(bMap2.size(), 1);
        Assert.assertEquals(bMap2.get("work").stringValue(), "SE");
    }

    @Test(description = "Test rest parameter in nested record variable")
    public void testVariableAssignment() {
        BValue[] returns = BRunUtil.invoke(result, "testVariableAssignment");
        Assert.assertEquals(returns.length, 5);
        Assert.assertEquals(returns[0].stringValue(), "Peter");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 29);
        Assert.assertEquals(returns[2].stringValue(), "Y");
        Assert.assertTrue(((BBoolean) returns[3]).booleanValue());
        Assert.assertEquals(((BMap) returns[4]).get("work").stringValue(), "SE");
    }

    @Test(description = "Test rest parameter in nested record variable")
    public void testVariableAssignment2() {
        BValue[] returns = BRunUtil.invoke(result, "testVariableAssignment2");
        Assert.assertEquals(returns.length, 5);
        Assert.assertEquals(returns[0].stringValue(), "James");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 30);
        Assert.assertEquals(returns[2].stringValue(), "N");
        Assert.assertFalse(((BBoolean) returns[3]).booleanValue());
        Assert.assertEquals(((BMap) returns[4]).get("added").stringValue(), "later");
    }

    @Test(description = "Test tuple var def inside record var def")
    public void testTupleVarDefInRecordVarDef() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleVarDefInRecordVarDef");
        Assert.assertEquals(returns.length, 7);
        Assert.assertEquals(returns[0].stringValue(), "Mark");
        Assert.assertEquals(((BInteger) ((BValueArray) returns[1]).getRefValue(0)).intValue(), 1);
        Assert.assertEquals(((BInteger) ((BValueArray) returns[1]).getRefValue(1)).intValue(), 1);
        Assert.assertEquals(((BInteger) ((BValueArray) returns[1]).getRefValue(2)).intValue(), 1990);
        Assert.assertEquals(((BByte) returns[2]).intValue(), 1);
        Assert.assertEquals(returns[3].stringValue(), "Mark");
        Assert.assertEquals(((BInteger) returns[4]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[5]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[6]).intValue(), 1990);
    }

    @Test(description = "Test record var def inside tuple var def inside record var def")
    public void testRecordInsideTupleInsideRecord() {
        BValue[] returns = BRunUtil.invoke(result, "testRecordInsideTupleInsideRecord");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "A");
        Assert.assertEquals(((BValueArray) returns[0]).getString(1), "B");
        Assert.assertEquals(returns[1].stringValue(), "A");
        BMap child = (BMap) ((BMap) returns[2]).get("child");
        Assert.assertEquals(child.get("name").stringValue(), "C");
        Assert.assertEquals(((BInteger) ((BValueArray) child.get("yearAndAge")).getRefValue(0)).intValue(), 1996);
        Assert.assertEquals(((BMap) ((BValueArray) child.get("yearAndAge")).getRefValue(1)).get("format").stringValue(),
                "Z");
    }

    // TODO: enable the test case once module record variable is supported
    @Test(description = "Test record var def inside tuple var def inside record var def", enabled = false)
    public void testRecordInsideTupleInsideRecord2() {
        BValue[] returns = BRunUtil.invoke(result, "testRecordInsideTupleInsideRecord2");
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(returns[0].stringValue(), "C");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1996);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 22);
        Assert.assertEquals(returns[3].stringValue(), "Z");
    }

    @Test(description = "Test record var def inside tuple var def inside record var def")
    public void testRecordInsideTupleInsideRecordWithVar() {
        BValue[] returns = BRunUtil.invoke(result, "testRecordInsideTupleInsideRecordWithVar");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "A");
        Assert.assertEquals(((BValueArray) returns[0]).getString(1), "B");
        Assert.assertEquals(returns[1].stringValue(), "A");
        BMap child = (BMap) ((BMap) returns[2]).get("child");
        Assert.assertEquals(child.get("name").stringValue(), "C");
        Assert.assertEquals(((BInteger) ((BValueArray) child.get("yearAndAge")).getRefValue(0)).intValue(), 1996);
        Assert.assertEquals(((BMap) ((BValueArray) child.get("yearAndAge")).getRefValue(1)).get("format").stringValue(),
                "Z");
    }

    // TODO: enable the test case one module record variable is supported
    @Test(description = "Test record var def inside tuple var def inside record var def", enabled = false)
    public void testRecordInsideTupleInsideRecord2WithVar() {
        BValue[] returns = BRunUtil.invoke(result, "testRecordInsideTupleInsideRecord2WithVar");
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(returns[0].stringValue(), "D");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1998);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 20);
        Assert.assertEquals(returns[3].stringValue(), "A");
    }

    @Test(description = "Test record var def inside tuple var def inside record var def")
    public void testRecordVarWithUnionType() {
        BValue[] returns = BRunUtil.invoke(result, "testRecordVarWithUnionType");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 50);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 51.1);
        Assert.assertEquals(returns[2].getType().getName(), "UnionOne");
        Assert.assertEquals(((BMap) returns[2]).get("restP1").stringValue(), "stringP1");
    }

    @Test(description = "Test record variable with Union Type")
    public void testUnionRecordVariable() {
        BValue[] returns = BRunUtil.invoke(result, "testUnionRecordVariable");
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(returns[0].stringValue(), "A");
        Assert.assertEquals(returns[1].stringValue(), "B");
        Assert.assertNull(returns[2]);
        Assert.assertNull(returns[3]);
    }

    @Test(description = "Test record variable with Map Type")
    public void testMapRecordVar() {
        BValue[] returns = BRunUtil.invoke(result, "testMapRecordVar");
        Assert.assertEquals(returns.length, 6);
        Assert.assertEquals(returns[0].stringValue(), "A");
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertNull(returns[2]);
        Assert.assertEquals(returns[3].stringValue(), "B");
        Assert.assertEquals(returns[4].stringValue(), "C");
        Assert.assertNull(returns[5]);
    }

    @Test(description = "Test record variable with ignore variable")
    public void testIgnoreVariable() {
        BValue[] returns = BRunUtil.invoke(result, "testIgnoreVariable");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "John");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 30);
    }

    @Test(description = "Test record variable with only a rest parameter")
    public void testRecordVariableWithOnlyRestParam() {
        BValue[] returns = BRunUtil.invoke(result, "testRecordVariableWithOnlyRestParam");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "{\"name\":\"John\", \"age\":{\"age\":30, \"format\":\"YY\", " +
                "\"year\":1990}, \"married\":true, \"work\":\"SE\"}");
    }

    @Test(description = "Test record variables rest param types", enabled = false)
    public void testRestParameterType() {
        BValue[] returns = BRunUtil.invoke(result, "testRestParameterType");
        Assert.assertEquals(returns.length, 5);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[1]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[3]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[4]).booleanValue());
    }

    @Test
    public void testNegativeRecordVariables() {
        String redeclaredSymbol = "redeclared symbol ";
        int i = -1;
        BAssertUtil.validateError(resultNegative, ++i, redeclaredSymbol + "'fName'", 37, 26);
        BAssertUtil.validateError(resultNegative, ++i, redeclaredSymbol + "'fiName'", 40, 36);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'Person', found 'PersonWithAge'", 62, 37);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'string', found 'int'", 95, 13);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'int', found 'string'", 96, 11);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'string', found 'boolean'", 97, 14);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'boolean', found 'string'", 98, 15);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected '(UnionOne|UnionTwo)', found 'UnionRec1'", 141, 66);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected '(string|boolean)', found '(string|boolean)?'", 142, 25);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected '(int|float)', found '(int|float)?'", 142, 31);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'string', found 'anydata'", 152, 13);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'string', found 'string?'", 152, 31);
        BAssertUtil.validateError(resultNegative, ++i,
                "underscore is not allowed here", 157, 19);
        BAssertUtil.validateError(resultNegative, ++i,
                "underscore is not allowed here", 157, 19);
        BAssertUtil.validateError(resultNegative, ++i,
                "no new variables on left side", 158, 19);
        Assert.assertEquals(resultNegative.getErrorCount(), i + 1);
    }
}
