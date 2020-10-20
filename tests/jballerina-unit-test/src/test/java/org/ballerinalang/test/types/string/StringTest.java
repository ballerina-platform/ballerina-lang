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

import io.ballerina.runtime.XMLFactory;
import io.ballerina.runtime.values.ArrayValue;
import io.ballerina.runtime.values.XMLItem;
import org.apache.axiom.om.OMNode;
import org.ballerinalang.core.model.util.JsonParser;
import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.core.model.values.BXMLItem;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.test.utils.ByteArrayUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;

import static org.ballerinalang.test.util.BAssertUtil.validateError;

/**
 * Test Native functions in ballerina.model.string.
 */
public class StringTest {
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
        BValue[] results = BRunUtil.invoke(result, "hasPrefix", args);
        Assert.assertTrue(((BBoolean) results[0]).booleanValue());
    }

    @Test
    public void testHasSuffix() {
        BValue[] args = {new BString("One Two"), new BString("Two")};
        BValue[] results = BRunUtil.invoke(result, "hasSuffix", args);
        Assert.assertTrue(((BBoolean) results[0]).booleanValue());
    }

    @Test
    public void testIndexOf() {
        BValue[] args = {new BString("Lion in the town"), new BString("in")};
        BValue[] results = BRunUtil.invoke(result, "indexOf", args);
        Assert.assertEquals(((BInteger) results[0]).intValue(), 5);
    }

    @Test
    public void testIndexOfAfterEmoji() {
        BValue[] args = {new BString("Lion\uD83E\uDD81 in the town"), new BString("in")};
        BValue[] results = BRunUtil.invoke(result, "indexOf", args);
        Assert.assertEquals(((BInteger) results[0]).intValue(), 6);
    }

    @Test
    public void testIndexOfAtEmoji() {
        BValue[] args = {new BString("Lion\uD83E\uDD81 in the town"), new BString("\uD83E\uDD81")};
        BValue[] results = BRunUtil.invoke(result, "indexOf", args);
        Assert.assertEquals(((BInteger) results[0]).intValue(), 4);
    }

    @Test
    public void testIndexOfBeforeEmoji() {
        BValue[] args = {new BString("Lion\uD83E\uDD81 in the town"), new BString("Lion")};
        BValue[] results = BRunUtil.invoke(result, "indexOf", args);
        Assert.assertEquals(((BInteger) results[0]).intValue(), 0);
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
        BValue[] args = { JsonParser.parse("{\"name\":\"chanaka\"}") };
        BValue[] returns = BRunUtil.invoke(result, "jsonValueOf", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "{\"name\":\"chanaka\"}";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testLength() {
        BValue[] args = {new BString("Bandwagon")};
        BValue[] returns = BRunUtil.invoke(result, "lengthOfStr", args);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 9);
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

    @Test
    public void testSubStringAfterEmoji() {
        BValue[] args = {new BString("test\uD83D\uDC87\uD83C\uDFFE\u200D♂️Values"), new BInteger(0), new BInteger(12)};
        BValue[] returns = BRunUtil.invoke(result, "substring", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "test\uD83D\uDC87\uD83C\uDFFE\u200D♂️Val");
    }

    @Test
    public void testSubStringBeforeEmoji() {
        BValue[] args = {new BString("test\uD83D\uDC69\uD83C\uDFFC\u200D⚖️️️Values"), new BInteger(0), new BInteger(4)};
        BValue[] returns = BRunUtil.invoke(result, "substring", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "test");
    }

    @Test
    public void testSubStringAtEmoji() {
        BValue[] args = {new BString("test\uD83D\uDC69\uD83C\uDFFE\u200D\uD83C\uDF7C️️Values"), new BInteger(0),
                new BInteger(8)};
        BValue[] returns = BRunUtil.invoke(result, "substring", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "test\uD83D\uDC69\uD83C\uDFFE\u200D\uD83C\uDF7C");
    }

    @Test
    public void testToLowerCase() {
        BValue[] args = {new BString("COMPANY")};
        BValue[] returns = BRunUtil.invoke(result, "toLower", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "company");
    }

    @Test
    public void testToUpperCase() {
        BValue[] args = {new BString("company")};
        BValue[] returns = BRunUtil.invoke(result, "toUpper", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "COMPANY");
    }

    @Test
    public void testTrim() {
        BValue[] args = {new BString(" This is a String ")};
        BValue[] returns = BRunUtil.invoke(result, "trim", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "This is a String");
    }

    @Test
    public void testXmlValueOf() {
        OMNode omNode = ((XMLItem) ((ArrayValue) XMLFactory.parse("<test>name</test>").value()).get(0)).value();
        BValue[] args = { new BXMLItem(omNode) };
        BValue[] returns = BRunUtil.invoke(result, "xmlValueOf", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "<test>name</test>";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testToByteArray() {
        String content = "Sample Ballerina Byte Array Content";
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        BValue[] args = {new BString(content)};
        BValue[] returns = BRunUtil.invoke(result, "toByteArray", args);
        BValueArray bByteArray = (BValueArray) returns[0];
        Assert.assertEquals(bByteArray.size(), bytes.length);
        ByteArrayUtils.assertJBytesWithBBytes(bytes, bByteArray.getBytes());
    }

    @Test(groups = { "disableOnOldParser" })
    public void testMultilineStringLiterals() {
        CompileResult multilineLiterals = BCompileUtil.compile("test-src/types/string/string_negative.bal");
        int indx = 0;
        validateError(multilineLiterals, indx++, "missing double quote", 18, 17);
        validateError(multilineLiterals, indx++, "missing plus token", 19, 1);
        validateError(multilineLiterals, indx++, "undefined symbol 'World'", 19, 5);
        validateError(multilineLiterals, indx++, "missing plus token", 19, 10);
        validateError(multilineLiterals, indx++, "operator '!' not defined for 'string'", 19, 10);
        validateError(multilineLiterals, indx++, "missing double quote", 19, 11);
        validateError(multilineLiterals, indx++, "missing semicolon token", 20, 1);
        Assert.assertEquals(multilineLiterals.getErrorCount(), indx);
    }
}
