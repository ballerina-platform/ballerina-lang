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

import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for String Template Literal.
 */
@Test(groups = { "disableOnOldParser" })
public class StringTemplateLiteralTest {

    private CompileResult result;
    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/string/string-template-literal.bal");
    }

    @Test
    public void testStringTemplateWithText1() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "stringTemplateWithText1", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "`");
    }

    @Test
    public void testStringTemplateWithText2() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "stringTemplateWithText2", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "\\\\");
    }

    @Test
    public void testStringTemplateWithText3() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "stringTemplateWithText3", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "\\{");
    }

    @Test
    public void testStringTemplateWithText4() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "stringTemplateWithText4", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "{{");
    }

    @Test
    public void testStringTemplateWithText5() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "stringTemplateWithText5", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "$\\{");
    }

    @Test
    public void testStringTemplateWithText6() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "stringTemplateWithText6", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "}");

    }

    @Test
    public void testStringTemplateWithText7() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "stringTemplateWithText7", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "}}");
    }

    @Test
    public void testStringTemplateWithText8() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "stringTemplateWithText8", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "}}}");
    }

    @Test
    public void testStringTemplateWithText9() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "stringTemplateWithText9", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Hello");
    }

    @Test
    public void testStringTemplateWithText10() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "stringTemplateWithText10", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina");
    }

    @Test
    public void testStringTemplateWithText11() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "stringTemplateWithText11", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Hello Ballerina");
    }

    @Test
    public void testStringTemplateWithText12() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "stringTemplateWithText12", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina !!!");
    }

    @Test
    public void testStringTemplateWithText13() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "stringTemplateWithText13", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Hello Ballerina !!!");
    }

    @Test
    public void testStringTemplateWithText14() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "stringTemplateWithText14", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Hello Smith, John");
    }

    @Test
    public void testStringTemplateWithText15() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "stringTemplateWithText15", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Hello Smith, John !!!");
    }

    @Test
    public void testStringTemplateWithText16() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "stringTemplateWithText16", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Count = 10");
    }

    @Test
    public void testStringTemplateWithText17() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "stringTemplateWithText17", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "$\\{count}");
    }

    @Test
    public void testStringTemplateWithText18() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "stringTemplateWithText18", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "\\\\10");
    }

    @Test
    public void testStringTemplateWithText19() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "stringTemplateWithText19", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Path = \\\\root");
    }

    @Test
    public void testStringTemplateWithText20() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "stringTemplateWithText20", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Path = \\\\");
    }

    @Test
    public void testStringTemplateWithText21() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "stringTemplateWithText21", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Hello John Smith !!!");
    }

    @Test
    public void testStringTemplateWithText22() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "stringTemplateWithText22", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Hello John Smith !!!");
    }

    @Test
    public void testStringTemplateWithText23() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "stringTemplateWithText23", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Hello John Smith !!!");
    }

    @Test
    public void testEmptyStringTemplate() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "emptyStringTemplate", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "");
    }

    @Test
    public void concatStringTemplateExprs() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "concatStringTemplateExprs", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "FirstName: John. Second name: Doe");
    }

    @Test
    public void stringTemplateEscapeChars() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "stringTemplateEscapeChars", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "\\n\\r\\b\\t\\f\\'\\\"`\\{\\\\");
    }

    @Test
    public void stringTemplateStartsWithDollar() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "stringTemplateStartsWithDollar", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "$$$$ A 8 B");
    }

    @Test
    public void stringTemplateEndsWithDollar() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "stringTemplateEndsWithDollar", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "A 8 B $$$$");
    }

    @Test
    public void stringTemplateWithOnlyDollar() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "stringTemplateWithOnlyDollar", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "$$$$$$$$$");
    }

    @Test
    public void stringTemplateDollarFollowedByEscapedLeftBrace() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "stringTemplateDollarFollowedByEscapedLeftBrace", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Hi $$$$\\{ 25 End");
    }

    @Test
    public void stringTemplateDollarFollowedByRightBrace() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "stringTemplateDollarFollowedByRightBrace", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Hi $$$$}}} 25 End");
    }

    @Test
    public void stringTemplateWithBraces() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "stringTemplateWithBraces", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "{{{{4 + 4}}}}}}}}}}");
    }

    @Test
    public void testComplexStringTemplateExpr() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "complexStringTemplateExpr", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(),
                "Hello \\n$\\\\$$\\{Dummy\\tText\\`\\\\test Ballerina endText\\\\{{{{{innerStartText 7 }}!!!");
    }
}
