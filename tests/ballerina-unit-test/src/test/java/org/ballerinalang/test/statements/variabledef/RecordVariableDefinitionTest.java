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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * TestCases for Record Variable Definitions.
 *
 * @since 0.982.0
 */
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

    @Test
    public void testNegativeRecordVariables() {
        Assert.assertEquals(resultNegative.getErrorCount(), 11);
        String redeclaredSymbol = "redeclared symbol ";
        String invalidRecordLiteralInBindingPattern = "invalid record literal in binding pattern. ";
        String invalidClosedRecordBindingPattern = "invalid closed record binding pattern. ";
        String invalidRecordBindingPattern = "invalid record binding pattern. ";

        int i = -1;
        BAssertUtil.validateError(resultNegative, ++i, redeclaredSymbol + "'married'", 35, 61);
        BAssertUtil.validateError(resultNegative, ++i, redeclaredSymbol + "'fName'", 35, 26);
        BAssertUtil.validateError(resultNegative, ++i, redeclaredSymbol + "'fiName'", 36, 19);
        BAssertUtil.validateError(resultNegative, ++i,
                invalidRecordLiteralInBindingPattern + "'name1' field not found in literal", 40, 11);
        BAssertUtil.validateError(resultNegative, ++i,
                invalidRecordBindingPattern + "unknown field 'name1' in record type 'Person'", 41, 11);
        BAssertUtil.validateError(resultNegative, ++i,
                invalidRecordLiteralInBindingPattern + "'name' field not found in literal", 42, 11);
        BAssertUtil.validateError(resultNegative, ++i,
                invalidClosedRecordBindingPattern + "expected '2' fields, but found '3'", 43, 11);
        BAssertUtil.validateError(resultNegative, ++i,
                invalidRecordLiteralInBindingPattern + "'name' field not found in literal", 44, 11);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'Person', found 'PersonWithAge'", 49, 37);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'string', found 'int'", 50, 55);
        BAssertUtil.validateError(resultNegative, ++i,
                "incompatible types: expected 'boolean', found 'string'", 50, 68);
    }
}
