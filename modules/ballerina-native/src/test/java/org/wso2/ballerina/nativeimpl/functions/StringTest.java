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
package org.wso2.ballerina.nativeimpl.functions;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BBoolean;
import org.wso2.ballerina.core.model.values.BDouble;
import org.wso2.ballerina.core.model.values.BFloat;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BJSON;
import org.wso2.ballerina.core.model.values.BLong;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BXML;
import org.wso2.ballerina.core.runtime.internal.GlobalScopeHolder;
import org.wso2.ballerina.nativeimpl.util.Functions;
import org.wso2.ballerina.nativeimpl.util.ParserUtils;

/**
 * Test Native functions in ballerina.lang.string.
 */
public class StringTest {
    private BallerinaFile bFile;
    private static final String s1 = "WSO2 Inc.";

    @BeforeClass
    public void setup() {
        // Add Native functions.
        SymScope symScope = GlobalScopeHolder.getInstance().getScope();
        bFile = ParserUtils.parseBalFile("samples/stringTest.bal");
    }

    @Test
    public void testBooleanValueOf() {
        BValue[] args = {new BBoolean(true)};
        BValue[] returns = Functions.invoke(bFile, "booleanValueOf", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "true";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testContains() {
        BValue[] args = {new BString(s1), new BString("WSO2")};
        BValue[] returns = Functions.invoke(bFile, "contains", args);

        Assert.assertTrue(returns[0] instanceof BBoolean);

        final String expected = "true";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testDoubleValueOf() {
        BValue[] args = {new BDouble(1.345)};
        BValue[] returns = Functions.invoke(bFile, "doubleValueOf", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "1.345";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testEqualsIgnoreCase() {
        BValue[] args = {new BString("WSO2"), new BString("wso2")};
        BValue[] returns = Functions.invoke(bFile, "equalsIgnoreCase", args);

        Assert.assertTrue(returns[0] instanceof BBoolean);

        final String expected = "true";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testFloatValueOf() {
        BValue[] args = {new BFloat(1.345f)};
        BValue[] returns = Functions.invoke(bFile, "floatValueOf", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "1.345";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testHasPrefix() {
        BValue[] args = {new BString("Expendables"), new BString("Ex")};
        BValue[] returns = Functions.invoke(bFile, "hasPrefix", args);

        Assert.assertTrue(returns[0] instanceof BBoolean);

        final String expected = "true";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testHasSuffix() {
        BValue[] args = {new BString("One Two"), new BString("Two")};
        BValue[] returns = Functions.invoke(bFile, "hasSuffix", args);

        Assert.assertTrue(returns[0] instanceof BBoolean);

        final String expected = "true";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testIndexOf() {
        BValue[] args = {new BString("Lion in the town"), new BString("in")};
        BValue[] returns = Functions.invoke(bFile, "indexOf", args);

        Assert.assertTrue(returns[0] instanceof BInteger);

        final String expected = "5";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testIntValueOf() {
        BValue[] args = {new BInteger(25)};
        BValue[] returns = Functions.invoke(bFile, "intValueOf", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "25";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testJsonValueOf() {
        BValue[] args = {new BJSON("{\"name\":\"chanaka\"}")};
        BValue[] returns = Functions.invoke(bFile, "jsonValueOf", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "{\"name\":\"chanaka\"}";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testLastIndexOf() {
        BValue[] args = {new BString("test x value x is x 18"), new BString("x")};
        BValue[] returns = Functions.invoke(bFile, "lastIndexOf", args);

        Assert.assertTrue(returns[0] instanceof BInteger);

        final String expected = "18";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testLength() {
        BValue[] args = {new BString("Bandwagon")};
        BValue[] returns = Functions.invoke(bFile, "length", args);

        Assert.assertTrue(returns[0] instanceof BInteger);

        final String expected = "9";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testLongValueOf() {
        BValue[] args = {new BLong(655555L)};
        BValue[] returns = Functions.invoke(bFile, "longValueOf", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "655555";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testReplace() {
        BValue[] args = {new BString("Best Company is Google"), new BString("Google"), new BString("WSO2")};
        BValue[] returns = Functions.invoke(bFile, "replace", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "Best Company is WSO2";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testReplaceAll() {
        BValue[] args = {new BString("abc is not abc as abc anymore"), new BString("abc"), new BString("xyz")};
        BValue[] returns = Functions.invoke(bFile, "replaceAll", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "xyz is not xyz as xyz anymore";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testReplaceFirst() {
        BValue[] args = {new BString("abc is not abc as abc anymore"), new BString("abc"), new BString("xyz")};
        BValue[] returns = Functions.invoke(bFile, "replaceFirst", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "xyz is not abc as abc anymore";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testStringValueOf() {
        BValue[] args = {new BString("This is a String")};
        BValue[] returns = Functions.invoke(bFile, "stringValueOf", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "This is a String";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testSubString() {
        BValue[] args = {new BString("testValues"), new BInteger(0), new BInteger(9)};
        BValue[] returns = Functions.invoke(bFile, "subString", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "testValue";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testToLowerCase() {
        BValue[] args = {new BString("COMPANY")};
        BValue[] returns = Functions.invoke(bFile, "toLowerCase", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "company";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testToUpperCase() {
        BValue[] args = {new BString("company")};
        BValue[] returns = Functions.invoke(bFile, "toUpperCase", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "COMPANY";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testTrim() {
        BValue[] args = {new BString(" This is a String ")};
        BValue[] returns = Functions.invoke(bFile, "trim", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "This is a String";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testUnescape() {
        BValue[] args = {new BString("This \\is an escaped \\String")};
        BValue[] returns = Functions.invoke(bFile, "unescape", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "This is an escaped String";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testXmlValueOf() {
        BValue[] args = {new BXML("<test>name</test>")};
        BValue[] returns = Functions.invoke(bFile, "xmlValueOf", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "<test>name</test>";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(expectedExceptions = {BallerinaException.class})
    public void testXmlValueOfNegative() {
        BValue[] args = {new BXML("<test>name<test>")};
        Functions.invoke(bFile, "xmlValueOf", args);
    }

}
