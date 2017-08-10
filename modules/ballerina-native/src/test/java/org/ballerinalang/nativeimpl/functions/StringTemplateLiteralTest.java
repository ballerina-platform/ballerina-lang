/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.nativeimpl.functions;

import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for String Template Literal.
 */
public class StringTemplateLiteralTest {

    private ProgramFile programFile;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("samples/stringTemplate/stringTemplateTest.bal");
    }

    @Test
    public void testEmptyStringTemplate() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "emptyStringTemplate", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "");
    }

    @Test
    public void testStringTemplateWithText1() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "stringTemplateWithText1", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "`");
    }

    @Test
    public void testStringTemplateWithText2() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "stringTemplateWithText2", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "\\");
    }

    @Test
    public void testStringTemplateWithText3() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "stringTemplateWithText3", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "{");
    }

    @Test
    public void testStringTemplateWithText4() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "stringTemplateWithText4", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "{{");
    }

    @Test
    public void testStringTemplateWithText5() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "stringTemplateWithText5", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "{{");
    }

    @Test
    public void testStringTemplateWithText6() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "stringTemplateWithText6", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "}");

    }

    @Test
    public void testStringTemplateWithText7() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "stringTemplateWithText7", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "}}");
    }

    @Test
    public void testStringTemplateWithText8() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "stringTemplateWithText8", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "}}}");
    }

    @Test
    public void testStringTemplateWithText9() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "stringTemplateWithText9", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Hello");
    }

    @Test
    public void testStringTemplateWithText10() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "stringTemplateWithText10", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina");
    }

    @Test
    public void testStringTemplateWithText11() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "stringTemplateWithText11", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Hello Ballerina");
    }

    @Test
    public void testStringTemplateWithText12() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "stringTemplateWithText12", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina !!!");
    }

    @Test
    public void testStringTemplateWithText13() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "stringTemplateWithText13", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Hello Ballerina !!!");
    }

    @Test
    public void testStringTemplateWithText14() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "stringTemplateWithText14", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Hello Smith, John");
    }

    @Test
    public void testStringTemplateWithText15() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "stringTemplateWithText15", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Hello Smith, John !!!");
    }

    @Test
    public void testStringTemplateWithText16() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "stringTemplateWithText16", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Count = 10");
    }

    @Test
    public void testStringTemplateWithText17() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "stringTemplateWithText17", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "{{count}}");
    }

    @Test
    public void testStringTemplateWithText18() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "stringTemplateWithText18", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "\\10");
    }

    @Test
    public void testStringTemplateWithText19() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "stringTemplateWithText19", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Path = \\root");
    }

    @Test
    public void testStringTemplateWithText20() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "stringTemplateWithText20", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Path = \\");
    }

    @Test
    public void testStringTemplateWithText21() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "stringTemplateWithText21", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Hello John Smith !!!");
    }

    @Test
    public void testStringTemplateWithText22() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "stringTemplateWithText22", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Hello John Smith !!!");
    }

    @Test
    public void testStringTemplateWithText23() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "stringTemplateWithText23", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Hello John Smith !!!");
    }
}
