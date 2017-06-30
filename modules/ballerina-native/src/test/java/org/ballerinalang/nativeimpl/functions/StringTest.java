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

import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;

/**
 * Test Native functions in ballerina.model.string.
 */
public class StringTest {
    private ProgramFile programFile;
    private static final String s1 = "WSO2 Inc.";

    @BeforeClass
    public void setup() {
        // Add Native functions.
        programFile = BTestUtils.getProgramFile("samples/stringTest.bal");
    }

    @Test
    public void testBooleanValueOf() {
        BValue[] args = {new BBoolean(true)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "booleanValueOf", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "true";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testContains() {
        BValue[] args = {new BString(s1), new BString("WSO2")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "contains", args);

        Assert.assertTrue(returns[0] instanceof BBoolean);

        final String expected = "true";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testEqualsIgnoreCase() {
        BValue[] args = {new BString("WSO2"), new BString("wso2")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "equalsIgnoreCase", args);

        Assert.assertTrue(returns[0] instanceof BBoolean);

        final String expected = "true";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testFloatValueOf() {
        BValue[] args = {new BFloat(1.345f)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "floatValueOf", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "1.345";
        Assert.assertEquals(returns[0].stringValue().substring(0, 5), expected);
    }

    @Test
    public void testHasPrefix() {
        BValue[] args = {new BString("Expendables"), new BString("Ex")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "hasPrefix", args);

        Assert.assertTrue(returns[0] instanceof BBoolean);

        final String expected = "true";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testHasSuffix() {
        BValue[] args = {new BString("One Two"), new BString("Two")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "hasSuffix", args);

        Assert.assertTrue(returns[0] instanceof BBoolean);

        final String expected = "true";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testIndexOf() {
        BValue[] args = {new BString("Lion in the town"), new BString("in")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "indexOf", args);

        Assert.assertTrue(returns[0] instanceof BInteger);

        final String expected = "5";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testIntValueOf() {
        BValue[] args = {new BInteger(25)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "intValueOf", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "25";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testJsonValueOf() {
        BValue[] args = {new BJSON("{\"name\":\"chanaka\"}")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "jsonValueOf", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "{\"name\":\"chanaka\"}";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testLastIndexOf() {
        BValue[] args = {new BString("test x value x is x 18"), new BString("x")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "lastIndexOf", args);

        Assert.assertTrue(returns[0] instanceof BInteger);

        final String expected = "18";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testLength() {
        BValue[] args = {new BString("Bandwagon")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "length", args);

        Assert.assertTrue(returns[0] instanceof BInteger);

        final String expected = "9";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testReplace() {
        BValue[] args = {new BString("Best Company is Google"), new BString("Google"), new BString("WSO2")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "replace", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "Best Company is WSO2";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testReplaceAll() {
        BValue[] args = {new BString("abc is not abc as abc anymore"), new BString("abc"), new BString("xyz")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "replaceAll", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "xyz is not xyz as xyz anymore";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testReplaceFirst() {
        BValue[] args = {new BString("abc is not abc as abc anymore"), new BString("abc"), new BString("xyz")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "replaceFirst", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "xyz is not abc as abc anymore";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testStringValueOf() {
        BValue[] args = {new BString("This is a String")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "stringValueOf", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "This is a String";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testSubString() {
        BValue[] args = {new BString("testValues"), new BInteger(0), new BInteger(9)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "subString", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "testValue";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testToLowerCase() {
        BValue[] args = {new BString("COMPANY")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "toLowerCase", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "company";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testToUpperCase() {
        BValue[] args = {new BString("company")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "toUpperCase", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "COMPANY";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testTrim() {
        BValue[] args = {new BString(" This is a String ")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "trim", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "This is a String";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testUnescape() {
        BValue[] args = {new BString("This \\is an escaped \\String")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "unescape", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "This is an escaped String";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testXmlValueOf() {
        BValue[] args = {new BXMLItem("<test>name</test>")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "xmlValueOf", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "<test>name</test>";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class})
    public void testXmlValueOfNegative() {
        BValue[] args = {new BXMLItem("<test>name<test>")};
        BLangFunctions.invokeNew(programFile, "xmlValueOf", args);
    }

    @Test
    public void testSplit() {
        BValue[] args = {new BString("name1 name2 name3"), new BString(" ")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "split", args);

        Assert.assertTrue(returns[0] instanceof BStringArray);

        BStringArray bStringArray = (BStringArray) returns[0];
        Assert.assertEquals(bStringArray.get(0), "name1");
        Assert.assertEquals(bStringArray.get(1), "name2");
        Assert.assertEquals(bStringArray.get(2), "name3");
    }

    @Test
    public void testToBlob() throws UnsupportedEncodingException {

        String content = "Sample Content";
        BValue[] args = { new BString(content), new BString("UTF-8") };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "toBlob", args);

        Assert.assertEquals(((BBlob) returns[0]).blobValue(), content.getBytes("UTF-8"),
                            "Produced Blob value is wrong");
    }

}
