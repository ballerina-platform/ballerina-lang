/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.types.string;

import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for String Template Literal.
 */
public class StringTemplateLiteralTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/string/string-template-literal.bal");
    }

    @Test
    public void testStringTemplateWithText1() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "stringTemplateWithText1", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "`");
    }

    @Test
    public void testStringTemplateWithText2() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "stringTemplateWithText2", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "\\\\");
    }

    @Test
    public void testStringTemplateWithText3() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "stringTemplateWithText3", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "\\{");
    }

    @Test
    public void testStringTemplateWithText4() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "stringTemplateWithText4", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "{{");
    }

    @Test
    public void testStringTemplateWithText5() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "stringTemplateWithText5", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "$\\{");
    }

    @Test
    public void testStringTemplateWithText6() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "stringTemplateWithText6", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "}");

    }

    @Test
    public void testStringTemplateWithText7() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "stringTemplateWithText7", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "}}");
    }

    @Test
    public void testStringTemplateWithText8() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "stringTemplateWithText8", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "}}}");
    }

    @Test
    public void testStringTemplateWithText9() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "stringTemplateWithText9", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Hello");
    }

    @Test
    public void testStringTemplateWithText10() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "stringTemplateWithText10", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Ballerina");
    }

    @Test
    public void testStringTemplateWithText11() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "stringTemplateWithText11", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Hello Ballerina");
    }

    @Test
    public void testStringTemplateWithText12() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "stringTemplateWithText12", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Ballerina !!!");
    }

    @Test
    public void testStringTemplateWithText13() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "stringTemplateWithText13", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Hello Ballerina !!!");
    }

    @Test
    public void testStringTemplateWithText14() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "stringTemplateWithText14", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Hello Smith, John");
    }

    @Test
    public void testStringTemplateWithText15() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "stringTemplateWithText15", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Hello Smith, John !!!");
    }

    @Test
    public void testStringTemplateWithText16() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "stringTemplateWithText16", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Count = 10");
    }

    @Test
    public void testStringTemplateWithText17() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "stringTemplateWithText17", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "$\\{count}");
    }

    @Test
    public void testStringTemplateWithText18() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "stringTemplateWithText18", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "\\\\10");
    }

    @Test
    public void testStringTemplateWithText19() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "stringTemplateWithText19", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Path = \\\\root");
    }

    @Test
    public void testStringTemplateWithText20() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "stringTemplateWithText20", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Path = \\\\");
    }

    @Test
    public void testStringTemplateWithText21() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "stringTemplateWithText21", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Hello John Smith !!!");
    }

    @Test
    public void testStringTemplateWithText22() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "stringTemplateWithText22", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Hello John Smith !!!");
    }

    @Test
    public void testStringTemplateWithText23() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "stringTemplateWithText23", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Hello John Smith !!!");
    }

    @Test
    public void testEmptyStringTemplate() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "emptyStringTemplate", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "");
    }

    @Test
    public void concatStringTemplateExprs() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "concatStringTemplateExprs", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "FirstName: John. Second name: Doe");
    }

    @Test
    public void stringTemplateEscapeChars() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "stringTemplateEscapeChars", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "\\n\\r\\b\\t\\f\\'\\\"`\\{\\\\");
    }

    @Test
    public void stringTemplateStartsWithDollar() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "stringTemplateStartsWithDollar", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "$$$$ A 8 B");
    }

    @Test
    public void stringTemplateEndsWithDollar() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "stringTemplateEndsWithDollar", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "A 8 B $$$$");
    }

    @Test
    public void stringTemplateWithOnlyDollar() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "stringTemplateWithOnlyDollar", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "$$$$$$$$$");
    }

    @Test
    public void stringTemplateDollarFollowedByEscapedLeftBrace() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "stringTemplateDollarFollowedByEscapedLeftBrace", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Hi $$$$\\{ 25 End");
    }

    @Test
    public void stringTemplateDollarFollowedByRightBrace() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "stringTemplateDollarFollowedByRightBrace", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Hi $$$$}}} 25 End");
    }

    @Test
    public void stringTemplateWithBraces() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "stringTemplateWithBraces", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "{{{{4 + 4}}}}}}}}}}");
    }

    @Test
    public void testComplexStringTemplateExpr() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "complexStringTemplateExpr", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(),
                "Hello \\n$\\\\$$\\{Dummy\\tText\\`\\\\test Ballerina endText\\\\{{{{{innerStartText 7 }}!!!");
    }

    @Test
    public void testStringTemplateExprWithUnionType() {
        BRunUtil.invoke(result, "testStringTemplateExprWithUnionType");
    }

    @Test(description = "Test numeric escapes inside string template")
    public void testNumericEscapes() {
        BRunUtil.invoke(result, "testNumericEscapes");
    }

    @Test
    public void testStringTemplateWithFiniteType() {
        BRunUtil.invoke(result, "testStringTemplateWithFiniteType");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
