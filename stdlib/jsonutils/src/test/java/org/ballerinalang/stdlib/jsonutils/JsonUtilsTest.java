/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.jsonutils;

import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.jvm.values.utils.StringUtils;
import org.ballerinalang.model.types.BMapType;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test functionality of jsonutils.
 *
 * @since 1.0
 */
public class JsonUtilsTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/jsonutils_test.bal");
    }

    @Test(description = "Test jsonutils:fromXML function")
    public void testFromXMLFunction() {
        BValue[] returns = BRunUtil.invoke(result, "testFromXML");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(((BMapType) returns[0].getType()).getConstrainedType().getTag(), TypeTags.JSON_TAG);
        Assert.assertEquals(returns[0].stringValue(), "{\"name\":\"supun\"}");
    }

    @Test(description = "Test jsonutils:fromXML function")
    public void testFromXMLFunction2() {
        BValue[] returns = BRunUtil.invoke(result, "testFromXML2");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].getType().getTag(), TypeTags.STRING_TAG);
        Assert.assertEquals(returns[0].stringValue(), "foo");
    }

    // Added a temporary fix due to following issue
    // https://github.com/ballerina-platform/ballerina-standard-library/issues/2559
    @Test
    public void testFromTableFunction() {
        BValue[] returns = BRunUtil.invoke(result, "testFromTable");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(),
                "[{\"id\":1, \"age\":30, \"salary\":\"301\", \"name\":\"Mary\", \"married\":true}, " +
                        "{\"id\":2, \"age\":20, \"salary\":\"301\", \"name\":\"John\", \"married\":true}]");
    }

    @Test
    public void testComplexXMLElementToJson() {
        BValue[] jsonStr = BRunUtil.invoke(result, "testComplexXMLElementToJson");
        Assert.assertEquals(jsonStr[0].stringValue(),
                "{\"Invoice\":[\"\\n        \", {\"PurchesedItems\":[\"\\n            \", " +
                        "{\"PLine\":{\"ItemCode\":{\"ItemCode\":\"223345\", \"@xmlns\":\"example.com\"}, " +
                        "\"Count\":{\"Count\":\"10\", \"@xmlns\":\"example.com\"}}, \"@xmlns\":\"example.com\"}, " +
                        "\"\\n            \", " +
                        "{\"PLine\":{\"ItemCode\":{\"ItemCode\":\"223300\", \"@xmlns\":\"example.com\"}, " +
                        "\"Count\":{\"Count\":\"7\", \"@xmlns\":\"example.com\"}}, \"@xmlns\":\"example.com\"}, " +
                        "\"\\n            \", {\"PLine\":{\"ItemCode\":{\"ItemCode\":\"200777\", " +
                        "\"@xmlns\":\"example.com\", \"@discount\":\"22%\"}, \"Count\":{\"Count\":\"7\", " +
                        "\"@xmlns\":\"example.com\"}}, \"@xmlns\":\"example.com\"}, \"\\n        \"], " +
                        "\"@xmlns\":\"example.com\"}, \"\\n        \", {\"Address\":[\"\\n            \", " +
                        "{\"StreetAddress\":\"20, Palm grove, Colombo 3\"}, \"\\n            \", " +
                        "{\"City\":\"Colombo\"}, \"\\n            \", {\"Zip\":\"00300\"}, \"\\n            \", " +
                        "{\"Country\":\"LK\"}, \"\\n        \"], \"@xmlns\":\"\"}, \"\\n    \"], " +
                        "\"@xmlns\":\"example.com\", \"@xmlns:ns\":\"ns.com\", \"@ns\":\"ns.com\", " +
                        "\"@attr\":\"attr-val\", \"@ns:attr\":\"ns-attr-val\"}");
    }

    @Test
    public void testComplexXMLElementToJsonNoPreserveNS() {
        BValue[] jsonStr = BRunUtil.invoke(result, "testComplexXMLElementToJsonNoPreserveNS");
        Assert.assertEquals(jsonStr[0].stringValue(),
                "{\"Invoice\":[\"\\n        \", {\"PurchesedItems\":[\"\\n            \", " +
                        "{\"PLine\":{\"ItemCode\":\"223345\", \"Count\":\"10\"}}, \"\\n            \", " +
                        "{\"PLine\":{\"ItemCode\":\"223300\", \"Count\":\"7\"}}, \"\\n            \", " +
                        "{\"PLine\":{\"ItemCode\":{\"ItemCode\":\"200777\", \"@discount\":\"22%\"}, " +
                        "\"Count\":\"7\"}}, \"\\n        \"]}, \"\\n        \", {\"Address\":[\"\\n            \", " +
                        "{\"StreetAddress\":\"20, Palm grove, Colombo 3\"}, \"\\n            \", " +
                        "{\"City\":\"Colombo\"}, \"\\n            \", {\"Zip\":\"00300\"}, \"\\n            \", " +
                        "{\"Country\":\"LK\"}, \"\\n        \"]}, \"\\n    \"], " +
                        "\"@ns\":\"ns.com\", \"@attr\":\"ns-attr-val\"}");
    }

    @Test
    public void testSequenceOfSameElementNamedItems() {
        convertChildrenToJsonAndAssert("<root><hello>1</hello><hello>2</hello></root>",
                "{\"hello\":[\"1\", \"2\"]}");
    }

    @Test
    public void testSequenceOfDifferentElementNamedItems() {
        convertChildrenToJsonAndAssert("<root><hello-0>1</hello-0><hello-1>2</hello-1></root>",
                "{\"hello-0\":\"1\", \"hello-1\":\"2\"}");
    }

    @Test
    public void testElementWithDifferentNamedChildrenElementItems() {
        convertToJsonAndAssert("<root><hello-0>1</hello-0><hello-1>2</hello-1></root>",
                "{\"root\":{\"hello-0\":\"1\", \"hello-1\":\"2\"}}");
    }

    @Test
    public void testElementWithSameNamedChildrenElementItems() {
        convertToJsonAndAssert("<root><hello>1</hello><hello>2</hello></root>",
                "{\"root\":{\"hello\":[\"1\", \"2\"]}}");
    }

    @Test
    public void testElementWithSameNamedChildrenElementItemsWithNonConvertible() {
        convertToJsonAndAssert("<root><hello>1</hello><!--cmnt--><hello>2</hello></root>",
                "{\"root\":{\"hello\":[\"1\", \"2\"]}}");
    }

    @Test
    public void testElementWithSameNamedChildrenElementItemsWithNonConvertibleBegin() {
        convertToJsonAndAssert("<root><!--cmnt--><hello>1</hello><hello>2</hello></root>",
                "{\"root\":{\"hello\":[\"1\", \"2\"]}}");
    }

    @Test
    public void testElementWithSameNamedChildrenElementItemsWithNonConvertibleEnd() {
        convertToJsonAndAssert("<root><hello>1</hello><hello>2</hello><!--cmnt--></root>",
                "{\"root\":{\"hello\":[\"1\", \"2\"]}}");
    }

    @Test
    public void testElementWithSameNamedEmptyChildren() {
        convertToJsonAndAssert("<root><hello attr0=\"hello\"></hello><hello></hello></root>",
                "{\"root\":{\"hello\":[{\"@attr0\":\"hello\"}, []]}}");
    }


    @Test
    public void testComplexXMLtoJson() {
        convertToJsonAndAssert(
                "<Invoice xmlns=\"example.com\" attr=\"attr-val\" xmlns:ns=\"ns.com\" ns:attr=\"ns-attr-val\">\n" +
                        "        <PurchesedItems>\n" +
                        "            <PLine><ItemCode>223345</ItemCode><Count>10</Count></PLine>\n" +
                        "            <PLine><ItemCode>223300</ItemCode><Count>7</Count></PLine>\n" +
                        "            <PLine><ItemCode discount=\"22%\">200777</ItemCode><Count>7</Count></PLine>\n" +
                        "        </PurchesedItems>\n" +
                        "        <Address xmlns=\"\">\n" +
                        "            <StreetAddress>20, Palm grove, Colombo 3</StreetAddress>\n" +
                        "            <City>Colombo</City>\n" +
                        "            <Zip>00300</Zip>\n" +
                        "            <Country>LK</Country>\n" +
                        "        </Address>\n" +
                        "    </Invoice>",
                "{\"Invoice\":{\"Invoice\":[\"\\n        \", {\"PurchesedItems\":[\"\\n            \", " +
                        "{\"PLine\":{\"ItemCode\":\"223345\", \"Count\":\"10\"}}, \"\\n            \", " +
                        "{\"PLine\":{\"ItemCode\":\"223300\", \"Count\":\"7\"}}, \"\\n            \", " +
                        "{\"PLine\":{\"ItemCode\":{\"ItemCode\":\"200777\", \"@discount\":\"22%\"}, " +
                        "\"Count\":\"7\"}}, \"\\n        \"]}, \"\\n        \", {\"Address\":[\"\\n            \", " +
                        "{\"StreetAddress\":\"20, Palm grove, Colombo 3\"}, \"\\n            \", " +
                        "{\"City\":\"Colombo\"}, \"\\n            \", {\"Zip\":\"00300\"}, \"\\n            \", " +
                        "{\"Country\":\"LK\"}, \"\\n        \"], \"@xmlns:\":\"\", \"@\":\"\"}, \"\\n    \"], " +
                        "\"@xmlns:ns\":\"ns.com\", \"@xmlns:\":\"example.com\", \"@attr\":\"attr-val\", " +
                        "\"@ns:attr\":\"ns-attr-val\", \"@ns\":\"ns.com\", \"@\":\"example.com\"}}");
    }

    @Test
    public void testXMLWithEmptyChildren() {
        BValue[] returns = BRunUtil.invoke(result, "testXMLWithEmptyChildren");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "{\"foo\":{\"bar\":\"2\", \"car\":\"\"}}");
    }

    private void convertToJsonAndAssert(String xmlStr, String jsonStr) {
        XMLValue parse = XMLFactory.parse(xmlStr);
        Object json = XmlToJsonConverter.convertToJSON(parse, "@", true);
        String jsonString = StringUtils.getJsonString(json);
        Assert.assertEquals(jsonString, jsonStr);
    }

    private void convertChildrenToJsonAndAssert(String xmlStr, String jsonStr) {
        XMLValue parse = XMLFactory.parse(xmlStr).children();
        Object json = XmlToJsonConverter.convertToJSON(parse, "@", true);
        String jsonString = StringUtils.getJsonString(json);
        Assert.assertEquals(jsonString, jsonStr);
    }
}
