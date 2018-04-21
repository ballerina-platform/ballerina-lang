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

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;

/**
 * Test Native functions in ballerina.model.string.
 */
public class StringTest {
    private static final String s1 = "WSO2 Inc.";
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/string/string-test.bal");
    }

    @Test
    public void testBooleanValueOf() {
        BValue[] args = {new BBoolean(true)};
        BValue[] returns = BRunUtil.invoke(result, "booleanValueOf", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "true";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testContains() {
        BValue[] args = {new BString(s1), new BString("WSO2")};
        BValue[] returns = BRunUtil.invoke(result, "contains", args);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testContainsNull() {
        // With null substring
        BValue[] args = new BValue[]{new BString(s1), new BString(null)};
        BRunUtil.invoke(result, "contains", args);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testContainsInNull() {
        // With null parent string
        BValue[] args = new BValue[]{new BString(null), new BString("WSO2")};
        BRunUtil.invoke(result, "contains", args);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testContainsNullInNull() {
        // With null parent string and substring
        BValue[] args = new BValue[]{new BString(null), new BString(null)};
        BRunUtil.invoke(result, "contains", args);
    }

    @Test
    public void testEqualsIgnoreCase() {
        BValue[] args = {new BString("WSO2"), new BString("wso2")};
        BRunUtil.invoke(result, "equalsIgnoreCase", args);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testEqualsIgnoreCaseNull() {
        // With null second string
        BValue[] args = new BValue[]{new BString("WSO2"), new BString(null)};
        BRunUtil.invoke(result, "equalsIgnoreCase", args);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testEqualsIgnoreCaseNullOther() {
        // With null first string
        BValue[] args = new BValue[]{new BString(null), new BString("WSO2")};
        BRunUtil.invoke(result, "equalsIgnoreCase", args);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testEqualsIgnoreCaseBothNull() {
        // With null first and second string
        BValue[] args = new BValue[]{new BString(null), new BString(null)};
        BRunUtil.invoke(result, "equalsIgnoreCase", args);
    }

    @Test
    public void testFloatValueOf() {
        BValue[] args = {new BFloat(1.345f)};
        BValue[] returns = BRunUtil.invoke(result, "floatValueOf", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "1.345";
        Assert.assertEquals(returns[0].stringValue().substring(0, 5), expected);
    }

    @Test
    public void testHasPrefix() {
        BValue[] args = {new BString("Expendables"), new BString("Ex")};
        BRunUtil.invoke(result, "hasPrefix", args);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testHasNullPrefix() {
        // With null second string
        BValue[] args = new BValue[]{new BString("Expendables"), new BString(null)};
        BRunUtil.invoke(result, "hasPrefix", args);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testHasPrefixInNull() {
        // With null first string
        BValue[] args = new BValue[]{new BString(null), new BString("Ex")};
        BRunUtil.invoke(result, "hasPrefix", args);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testHasNullPrefixInNull() {
        // With null first and second string
        BValue[] args = new BValue[]{new BString(null), new BString(null)};
        BRunUtil.invoke(result, "hasPrefix", args);
    }

    @Test
    public void testHasSuffix() {
        BValue[] args = {new BString("One Two"), new BString("Two")};
        BRunUtil.invoke(result, "hasSuffix", args);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testHasNullSuffix() {
        // With null second string
        BValue[] args = new BValue[]{new BString("One Two"), new BString(null)};
        BRunUtil.invoke(result, "hasSuffix", args);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testHasSuffixInNull() {
        // With null first string
        BValue[] args = new BValue[]{new BString(null), new BString("Two")};
        BRunUtil.invoke(result, "hasSuffix", args);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testHasNullSuffixInNull() {
        // With null first and second string
        BValue[] args = new BValue[]{new BString(null), new BString(null)};
        BRunUtil.invoke(result, "hasSuffix", args);
    }

    @Test
    public void testIndexOf() {
        BValue[] args = {new BString("Lion in the town"), new BString("in")};
        BRunUtil.invoke(result, "indexOf", args);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testIndexOfNull() {
        // With null second string
        BValue[] args = new BValue[]{new BString("Lion in the town"), new BString(null)};
        BRunUtil.invoke(result, "indexOf", args);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testIndexOfInNull() {
        // With null first string
        BValue[] args = new BValue[]{new BString(null), new BString("in")};
        BRunUtil.invoke(result, "indexOf", args);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testIndexOfNullInNull() {
        // With null first and second string
        BValue[] args = new BValue[]{new BString(null), new BString(null)};
        BRunUtil.invoke(result, "indexOf", args);
    }

    @Test
    public void testIntValueOf() {
        BValue[] args = {new BInteger(25)};
        BValue[] returns = BRunUtil.invoke(result, "intValueOf", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "25";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testJsonValueOf() {
        BValue[] args = {new BJSON("{\"name\":\"chanaka\"}")};
        BValue[] returns = BRunUtil.invoke(result, "jsonValueOf", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "{\"name\":\"chanaka\"}";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testLastIndexOf() {
        BValue[] args = {new BString("test x value x is x 18"), new BString("x")};
        BRunUtil.invoke(result, "lastIndexOf", args);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testLastIndexOfNull() {
        // With null second string
        BValue[] args = new BValue[]{new BString("test x value x is x 18"), new BString(null)};
        BRunUtil.invoke(result, "indexOf", args);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testLastIndexOfInNul() {
        // With null first string
        BValue[] args = new BValue[]{new BString(null), new BString("x")};
        BRunUtil.invoke(result, "indexOf", args);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testLastIndexOfNullInNull() {
        // With null first and second string
        BValue[] args = new BValue[]{new BString(null), new BString(null)};
        BRunUtil.invoke(result, "indexOf", args);
    }

    @Test
    public void testLength() {
        BValue[] args = {new BString("Bandwagon")};
        BValue[] returns = BRunUtil.invoke(result, "length", args);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 9);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testLengthofNull() {
        BValue[] args = {new BString(null)};
        BRunUtil.invoke(result, "length", args);
    }

    @Test
    public void testReplace() {
        BValue[] args = {new BString("Best Company is Google"), new BString("Google"), new BString("WSO2")};
        BValue[] returns = BRunUtil.invoke(result, "replace", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Best Company is WSO2");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testReplaceInNull() {
        BValue[] args = {new BString(null), new BString("Google"), new BString("WSO2")};
        BRunUtil.invoke(result, "replace", args);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testReplaceNull() {
        BValue[] args = {new BString("Best Company is Google"), new BString(null), new BString("WSO2")};
        BRunUtil.invoke(result, "replace", args);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testReplaceByNull() {
        BValue[] args = {new BString("Best Company is Google"), new BString("Google"), new BString(null)};
        BRunUtil.invoke(result, "replace", args);
    }

    @Test
    public void testReplaceAll() {
        BValue[] args = {new BString("abc is not abc as abc anymore"), new BString("abc"), new BString("xyz")};
        BValue[] returns = BRunUtil.invoke(result, "replaceAll", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "xyz is not xyz as xyz anymore");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testReplaceAllInNull() {
        BValue[] args = {new BString(null), new BString("abc"), new BString("xyz")};
        BRunUtil.invoke(result, "replaceAll", args);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testReplaceAllNull() {
        BValue[] args = {new BString("abc is not abc as abc anymore"), new BString(null), new BString("xyz")};
        BRunUtil.invoke(result, "replaceAll", args);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testReplaceAllByNull() {
        BValue[] args = {new BString("abc is not abc as abc anymore"), new BString("abc"), new BString(null)};
        BRunUtil.invoke(result, "replaceAll", args);
    }

    @Test
    public void testReplaceFirst() {
        BValue[] args = {new BString("abc is not abc as abc anymore"), new BString("abc"), new BString("xyz")};
        BValue[] returns = BRunUtil.invoke(result, "replaceFirst", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "xyz is not abc as abc anymore");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testReplaceFirstInNull() {
        BValue[] args = {new BString(null), new BString("abc"), new BString("xyz")};
        BRunUtil.invoke(result, "replaceFirst", args);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testReplaceFirstNull() {
        BValue[] args = {new BString("abc is not abc as abc anymore"), new BString(null), new BString("xyz")};
        BRunUtil.invoke(result, "replaceFirst", args);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testReplaceFirstByNull() {
        BValue[] args = {new BString("abc is not abc as abc anymore"), new BString("abc"), new BString(null)};
        BRunUtil.invoke(result, "replaceFirst", args);
    }

    @Test
    public void testStringValueOf() {
        BValue[] args = {new BString("This is a String")};
        BValue[] returns = BRunUtil.invoke(result, "stringValueOf", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "This is a String";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testSubString() {
        BValue[] args = {new BString("testValues"), new BInteger(0), new BInteger(9)};
        BValue[] returns = BRunUtil.invoke(result, "substring", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "testValue");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testSubStringFromNull() {
        BValue[] args = {new BString(null), new BInteger(0), new BInteger(9)};
        BRunUtil.invoke(result, "substring", args);
    }

    @Test
    public void testToLowerCase() {
        BValue[] args = {new BString("COMPANY")};
        BValue[] returns = BRunUtil.invoke(result, "toLower", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "company");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testNullToLowerCase() {
        BValue[] args = {new BString(null)};
        BRunUtil.invoke(result, "toLower", args);
    }

    @Test
    public void testToUpperCase() {
        BValue[] args = {new BString("company")};
        BValue[] returns = BRunUtil.invoke(result, "toUpper", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "COMPANY");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testNullToUpperCase() {
        BValue[] args = {new BString(null)};
        BRunUtil.invoke(result, "toUpper", args);
    }

    @Test
    public void testTrim() {
        BValue[] args = {new BString(" This is a String ")};
        BValue[] returns = BRunUtil.invoke(result, "trim", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "This is a String");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testTrimNull() {
        BValue[] args = {new BString(null)};
        BValue[] returns = BRunUtil.invoke(result, "trim", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "This is a String");
    }

    @Test
    public void testUnescape() {
        BValue[] args = {new BString("This \\is an escaped \\String")};
        BValue[] returns = BRunUtil.invoke(result, "unescape", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "This is an escaped String");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testUnescapeNull() {
        BValue[] args = {new BString(null)};
        BRunUtil.invoke(result, "unescape", args);
    }

    @Test
    public void testXmlValueOf() {
        BValue[] args = {new BXMLItem("<test>name</test>")};
        BValue[] returns = BRunUtil.invoke(result, "xmlValueOf", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "<test>name</test>";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    // TODO test this
    @Test(expectedExceptions = {BLangRuntimeException.class})
    public void testXmlValueOfNegative() {
        BValue[] args = {new BXMLItem("<test>name<test>")};
        BRunUtil.invoke(result, "xmlValueOf", args);
    }

    @Test
    public void testSplit() {
        BValue[] args = {new BString("name1 name2 name3"), new BString(" ")};
        BValue[] returns = BRunUtil.invoke(result, "split", args);
        Assert.assertTrue(returns[0] instanceof BStringArray);
        BStringArray bStringArray = (BStringArray) returns[0];
        Assert.assertEquals(bStringArray.get(0), "name1");
        Assert.assertEquals(bStringArray.get(1), "name2");
        Assert.assertEquals(bStringArray.get(2), "name3");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testSplitNull() {
        BValue[] args = {new BString(null), new BString(" ")};
        BRunUtil.invoke(result, "split", args);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    public void testSplitByNull() {
        BValue[] args = {new BString("name1 name2 name3"), new BString(null)};
        BRunUtil.invoke(result, "split", args);
    }

    @Test
    public void testToBlob() throws UnsupportedEncodingException {
        String content = "Sample Content";
        BValue[] args = {new BString(content), new BString("UTF-8")};
        BValue[] returns = BRunUtil.invoke(result, "toBlob", args);
        Assert.assertEquals(((BBlob) returns[0]).blobValue(), content.getBytes("UTF-8"),
                "Produced Blob value is wrong");
    }

    @Test
    public void testEncoding() {
        String[] stringsToTest = {"Hi", "`Welcome\" \r^to^\tBallerina\\ /$TestCases$\n # ~^!!!",
                "https://example.com/test/index.html#title1",
                "https://example.com/test/index.html?greeting=hello world&your-name=Ballerina Test cases",
                "バレリーナ日本語", "Ballerina 的中文翻译", "Traducción al español de  la bailarina"};

        for (String s : stringsToTest) {
            BValue[] returnVals = BRunUtil.invoke(result, "testEncodeDecode", new BValue[]{new BString(s)});
            Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                    "Invalid Return Values for :" + s);
            Assert.assertEquals(returnVals[0].stringValue(), s, "Original and Return value didn't match");
        }
    }

    @Test
    public void testBase64EncodeString() {
        String expectedValue = "SGVsbG8gQmFsbGVyaW5h";
        BValue[] args = new BValue[]{new BString("Hello Ballerina")};
        BValue[] returnValues = BRunUtil.invoke(result, "testBase64EncodeString", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                "Invalid return value");
        Assert.assertEquals(returnValues[0].stringValue(), expectedValue);
    }

    @Test
    public void testBase64EncodeBlob() {
        String expectedValue = "SGVsbG8gQmFsbGVyaW5h";
        BValue[] args = new BValue[]{new BBlob("Hello Ballerina".getBytes())};
        BValue[] returnValues = BRunUtil.invoke(result, "testBase64EncodeBlob", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                "Invalid return value");
        Assert.assertEquals(returnValues[0].stringValue(), expectedValue);
    }

    @Test
    public void testBase64DecodeString() {
        String expectedValue = "Hello Ballerina";
        BValue[] args = new BValue[]{new BString("SGVsbG8gQmFsbGVyaW5h")};
        BValue[] returnValues = BRunUtil.invoke(result, "testBase64DecodeString", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                "Invalid return value");
        Assert.assertEquals(returnValues[0].stringValue(), expectedValue);
        Assert.assertFalse(returnValues[0] == null, "Invalid return value");
        Assert.assertEquals(returnValues[0].stringValue(), expectedValue);
    }

    @Test
    public void testBase64DecodeBlob() {
        String expectedValue = "Hello Ballerina";
        BValue[] args = new BValue[]{new BBlob("SGVsbG8gQmFsbGVyaW5h".getBytes())};
        BValue[] returnValues = BRunUtil.invoke(result, "testBase64DecodeBlob", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                "Invalid return value");
        Assert.assertEquals(returnValues[0].stringValue(), expectedValue);
    }

    @Test
    public void testBase16ToBase64Encoding() {
        String expectedValue = "SGVsbG8gQmFsbGVyaW5h";
        BValue[] args = new BValue[]{new BString("48656C6C6F2042616C6C6572696E61")};
        BValue[] returnValues = BRunUtil.invoke(result, "testBase16ToBase64Encoding", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                "Invalid return value");
        Assert.assertEquals(returnValues[0].stringValue(), expectedValue);
    }

    @Test
    public void testBase64ToBase16Encoding() {
        String expectedValue = "48656C6C6F2042616C6C6572696E61";
        BValue[] args = new BValue[]{new BString("SGVsbG8gQmFsbGVyaW5h")};
        BValue[] returnValues = BRunUtil.invoke(result, "testBase64ToBase16Encoding", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                "Invalid return value");
        Assert.assertEquals(returnValues[0].stringValue(), expectedValue);
    }

    @Test
    public void testHMACValueFromBase16ToBase64Encoding() {
        String expectedValue = "S4qhC4EdsEcvY64gs+JsmA==";
        BValue[] args = new BValue[]{new BString("Hello Ballerina"), new BString("abcdefghijk")};
        BValue[] returnValues = BRunUtil.invoke(result, "testHMACValueFromBase16ToBase64Encoding", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                "Invalid return value");
        Assert.assertEquals(returnValues[0].stringValue(), expectedValue);
    }

    @Test
    public void testHMACValueFromBase64ToBase16Encoding() {
        String expectedValue = "4B8AA10B811DB0472F63AE20B3E26C98";
        BValue[] args = new BValue[]{new BString("Hello Ballerina"), new BString("abcdefghijk")};
        BValue[] returnValues = BRunUtil.invoke(result, "testHMACValueFromBase64ToBase16Encoding", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                "Invalid return value");
        Assert.assertEquals(returnValues[0].stringValue(), expectedValue);
    }
}
