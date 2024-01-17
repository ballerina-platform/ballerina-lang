/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.expressions;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class to validate the backtick based inline xml and json definitions.
 */
public class TemplateExpressionTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        // Add Native functions.
        compileResult = BCompileUtil.compile("test-src/expressions/template-expr.bal");
    }

    @Test(description = "Test JSON backtick expression definition")
    public void testJSONInit() {
        Object[] args = {StringUtils.fromString("WSO2")};
        Object returns = BRunUtil.invoke(compileResult, "testJSONInit", args);
        Assert.assertTrue(returns instanceof BMap);
        String expected = "{\"name\":\"John\"}";
        Assert.assertEquals(returns.toString(), expected);
    }

    @Test(description = "Test JSON backtick expression with string variable reference")
    public void testStringVariableAccessInJSONInit() {
        Object[] args = {StringUtils.fromString("WSO2")};
        Object returns = BRunUtil.invoke(compileResult, "testStringVariableAccessInJSONInit", args);
        Assert.assertTrue(returns instanceof BMap);
        String expected = "{\"name\":\"WSO2\"}";
        Assert.assertEquals(returns.toString(), expected);
    }

    @Test(description = "Test JSON backtick expression with integer variable reference")
    public void testIntegerVariableAccessInJSONInit() {
        Object[] args = {(11)};
        Object returns = BRunUtil.invoke(compileResult, "testIntegerVariableAccessInJSONInit", args);
        Assert.assertTrue(returns instanceof BMap);
        String expected = "{\"age\":11}";
        Assert.assertEquals(returns.toString(), expected);
    }

    @Test(description = "Test JSON backtick expression with embedding full JSON")
    public void testEnrichFullJSON() {
        Object[] args = {(11)};
        Object returns = BRunUtil.invoke(compileResult, "testEnrichFullJSON", args);
        Assert.assertTrue(returns instanceof BMap);
        String expected = "{\"name\":\"John\"}";
        Assert.assertEquals(returns.toString(), expected);
    }

    @Test(description = "Test JSON backtick expression with multiple variables embedding full JSON")
    public void testMultipleVariablesInJSONInit() {
        Object[] args = {StringUtils.fromString("Chanaka"), StringUtils.fromString("Fernando")};
        Object returns = BRunUtil.invoke(compileResult, "testMultipleVariablesInJSONInit", args);
        Assert.assertTrue(returns instanceof BMap);
        String expected = "{\"name\":{\"first_name\":\"Chanaka\",\"last_name\":\"Fernando\"}}";
        Assert.assertEquals(returns.toString(), expected);
    }

    @Test(description = "Test JSON backtick expression with int and string arrays variable reference")
    public void testArrayVariableAccessInJSONInit() {
        Object returns = BRunUtil.invoke(compileResult, "testArrayVariableAccessInJSONInit");
        Assert.assertTrue(returns instanceof BMap);
        String expected = "{\"strIndex0\":\"value0\",\"intIndex2\":2,\"strIndex2\":\"value2\"}";
        Assert.assertEquals(returns.toString(), expected);
    }

    @Test(description = "Test JSON backtick expression with map variable reference")
    public void testMapVariableAccessInJSONInit() {
        Object returns = BRunUtil.invoke(compileResult, "testMapVariableAccessInJSONInit");
        Assert.assertTrue(returns instanceof BMap);
        String expected = "{\"val1\":\"value0\",\"val2\":1}";
        Assert.assertEquals(returns.toString(), expected);
    }

    @Test(description = "Test JSON backtick expression with boolean and integers as string values")
    public void testBooleanIntegerValuesAsStringsInJSONInit() {
        Object returns = BRunUtil.invoke(compileResult, "testBooleanIntegerValuesAsStringsInJSONInit");
        Assert.assertTrue(returns instanceof BMap);
        String expected = "{\"intStrIndex0\":\"0\",\"intStrIndex1\":\"1\",\"boolStrIndex0\":\"true\"," +
                "\"boolStrIndex1\":\"false\"}";
        Assert.assertEquals(returns.toString(), expected);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
