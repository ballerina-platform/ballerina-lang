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

import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
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
        BValue[] args = {new BString("WSO2")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testJSONInit", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BMap.class);
        String expected = "{\"name\":\"John\"}";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(description = "Test JSON backtick expression with string variable reference")
    public void testStringVariableAccessInJSONInit() {
        BValue[] args = {new BString("WSO2")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testStringVariableAccessInJSONInit", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BMap.class);
        String expected = "{\"name\":\"WSO2\"}";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(description = "Test JSON backtick expression with integer variable reference")
    public void testIntegerVariableAccessInJSONInit() {
        BValue[] args = {new BInteger(11)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testIntegerVariableAccessInJSONInit", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BMap.class);
        String expected = "{\"age\":11}";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(description = "Test JSON backtick expression with embedding full JSON")
    public void testEnrichFullJSON() {
        BValue[] args = {new BInteger(11)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testEnrichFullJSON", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BMap.class);
        String expected = "{\"name\":\"John\"}";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(description = "Test JSON backtick expression with multiple variables embedding full JSON")
    public void testMultipleVariablesInJSONInit() {
        BValue[] args = {new BString("Chanaka"), new BString("Fernando")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testMultipleVariablesInJSONInit", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BMap.class);
        String expected = "{\"name\":{\"first_name\":\"Chanaka\", \"last_name\":\"Fernando\"}}";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(description = "Test JSON backtick expression with int and string arrays variable reference")
    public void testArrayVariableAccessInJSONInit() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testArrayVariableAccessInJSONInit");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BMap.class);
        String expected = "{\"strIndex0\":\"value0\", \"intIndex2\":2, \"strIndex2\":\"value2\"}";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(description = "Test JSON backtick expression with map variable reference")
    public void testMapVariableAccessInJSONInit() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMapVariableAccessInJSONInit");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BMap.class);
        String expected = "{\"val1\":\"value0\", \"val2\":1}";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(description = "Test JSON backtick expression with boolean and integers as string values")
    public void testBooleanIntegerValuesAsStringsInJSONInit() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testBooleanIntegerValuesAsStringsInJSONInit");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BMap.class);
        String expected = "{\"intStrIndex0\":\"0\", \"intStrIndex1\":\"1\", "
                + "\"boolStrIndex0\":\"true\", \"boolStrIndex1\":\"false\"}";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }
}
