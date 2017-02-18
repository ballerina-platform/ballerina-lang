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
package org.ballerinalang.nativeimpl.functions;

import org.ballerinalang.bre.SymScope;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BDouble;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BLong;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.runtime.internal.GlobalScopeHolder;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test Native functions in ballerina.model.string.
 */
public class StringTest {
    private BLangProgram bLangProgram;
    private static final String s1 = "WSO2 Inc.";

    @BeforeClass
    public void setup() {
        // Add Native functions.
        SymScope symScope = GlobalScopeHolder.getInstance().getScope();
        bLangProgram = BTestUtils.parseBalFile("samples/stringTest.bal");
    }

    @Test
    public void testBooleanValueOf() {
        BValue[] args = {new BBoolean(true)};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "booleanValueOf", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "true";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testContains() {
        BValue[] args = {new BString(s1), new BString("WSO2")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "contains", args);

        Assert.assertTrue(returns[0] instanceof BBoolean);

        final String expected = "true";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testDoubleValueOf() {
        BValue[] args = {new BDouble(1.345)};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "doubleValueOf", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "1.345";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testEqualsIgnoreCase() {
        BValue[] args = {new BString("WSO2"), new BString("wso2")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "equalsIgnoreCase", args);

        Assert.assertTrue(returns[0] instanceof BBoolean);

        final String expected = "true";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testFloatValueOf() {
        BValue[] args = {new BFloat(1.345f)};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "floatValueOf", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "1.345";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testHasPrefix() {
        BValue[] args = {new BString("Expendables"), new BString("Ex")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "hasPrefix", args);

        Assert.assertTrue(returns[0] instanceof BBoolean);

        final String expected = "true";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testHasSuffix() {
        BValue[] args = {new BString("One Two"), new BString("Two")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "hasSuffix", args);

        Assert.assertTrue(returns[0] instanceof BBoolean);

        final String expected = "true";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testIndexOf() {
        BValue[] args = {new BString("Lion in the town"), new BString("in")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "indexOf", args);

        Assert.assertTrue(returns[0] instanceof BInteger);

        final String expected = "5";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testIntValueOf() {
        BValue[] args = {new BInteger(25)};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "intValueOf", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "25";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testJsonValueOf() {
        BValue[] args = {new BJSON("{\"name\":\"chanaka\"}")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "jsonValueOf", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "{\"name\":\"chanaka\"}";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testLastIndexOf() {
        BValue[] args = {new BString("test x value x is x 18"), new BString("x")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "lastIndexOf", args);

        Assert.assertTrue(returns[0] instanceof BInteger);

        final String expected = "18";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testLength() {
        BValue[] args = {new BString("Bandwagon")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "length", args);

        Assert.assertTrue(returns[0] instanceof BInteger);

        final String expected = "9";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testLongValueOf() {
        BValue[] args = {new BLong(655555L)};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "longValueOf", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "655555";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testReplace() {
        BValue[] args = {new BString("Best Company is Google"), new BString("Google"), new BString("WSO2")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "replace", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "Best Company is WSO2";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testReplaceAll() {
        BValue[] args = {new BString("abc is not abc as abc anymore"), new BString("abc"), new BString("xyz")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "replaceAll", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "xyz is not xyz as xyz anymore";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testReplaceFirst() {
        BValue[] args = {new BString("abc is not abc as abc anymore"), new BString("abc"), new BString("xyz")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "replaceFirst", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "xyz is not abc as abc anymore";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testStringValueOf() {
        BValue[] args = {new BString("This is a String")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "stringValueOf", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "This is a String";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testSubString() {
        BValue[] args = {new BString("testValues"), new BInteger(0), new BInteger(9)};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "subString", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "testValue";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testToLowerCase() {
        BValue[] args = {new BString("COMPANY")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "toLowerCase", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "company";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testToUpperCase() {
        BValue[] args = {new BString("company")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "toUpperCase", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "COMPANY";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testTrim() {
        BValue[] args = {new BString(" This is a String ")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "trim", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "This is a String";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testUnescape() {
        BValue[] args = {new BString("This \\is an escaped \\String")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "unescape", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "This is an escaped String";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testXmlValueOf() {
        BValue[] args = {new BXML("<test>name</test>")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "xmlValueOf", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "<test>name</test>";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(expectedExceptions = {BallerinaException.class})
    public void testXmlValueOfNegative() {
        BValue[] args = {new BXML("<test>name<test>")};
        BLangFunctions.invoke(bLangProgram, "xmlValueOf", args);
    }

}
