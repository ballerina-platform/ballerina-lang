/*
*   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.test.types.json;

import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.util.JsonNode.Type;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.model.values.BXMLItem;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test Native functions in ballerina.model.json.
 */
@SuppressWarnings("javadoc")
@Test(groups = {"broken"})
public class JSONTest {

    private CompileResult compileResult;
    private static final String json1 = "{'name':{'fname':'Jack','lname':'Taylor'}, 'state':'CA', 'age':20}";
    private static final String jsonToXML1 = "{'bookStore':{'storeName':'foo','postalCode':94,'isOpen':true,'address':"
            + "{'street':'PalmGrove','city':'Colombo','country':'SriLanka'},'codes':[4, 8, 9]}}";
    private static final String jsonToXML2 = "{'books':[{'bookName':'book1','bookId':101},{'bookName':'book2',"
            + "'bookId':102},{'bookName':'book3','bookId':103}]}";
    private static final String jsonToXML3 = "{'books':[[{'bookName':'book1','bookId':101}],[{'bookName':'book2',"
            + "'bookId':102}],[{'bookName':'book3','bookId':103}]]}";
    private static final String jsonToXML4 = "{'books':['book1','book2','book3']}";
    private static final String jsonToXML5 = "5";
    private static final String jsonToXML6 = "[3,4,5]";
    private static final String jsonToXML7 = "{'bookStore':{'storeName':'foo','postalCode':94,'isOpen':true,'address':"
            + "{'street':'PalmGrove','city': 'Colombo','country':'SriLanka'},'codes':[4, 8, 9]},'metaInfo':'someinfo'}";
    private static final String jsonToXML8 = "{'name': 'John','age': 30,'car': null}";
    private static final String jsonToXML9 = "{'Person':{'name':'John','age':30,'car':null}}";
    private static final String jsonToXML10 = "{'address': {},'homeAddresses': [],'phoneNumbers': []}";
    private static final String jsonToXML11 = "{'info':{'address': {},'homeAddresses': ['a', 'b'],'phoneNumbers': []}}";
    private static final String jsonToXML12 = "{'info':{'name': 'John','age': 30,'car': 'honda', '@id': '100'}}";
    private static final String jsonToXML13 = "{'bookStore':{'@storeName':'foo','postalCode':94,'isOpen':true,"
            + "'address':{'street':'PalmGrove','@city':'Colombo','country':'SriLanka'},'codes':[4, 8, 9]}}";
    private static final String jsonToXML14 = "{'bookStore':{'#storeName':'foo','postalCode':94,'isOpen':true,"
            + "'address':{'street':'PalmGrove','#city':'Colombo','country':'SriLanka'},'codes':[4, 8, 9]}}";
    private static final String jsonToXML15 = "{\"key\":\"value\"}";
    private static final String jsonToXML16 = "{\"key1\": \"value1\",\"key2\": \"value2\"}";
    private static final String jsonToXML17 = "[\"test\", 6, true]";
    private static final String jsonToXML18 = "{\"foo\":{\"@key\": \"value\"}}";
    private static final String jsonToXML19 = "{\"foo\":{\"@key\": \"value\",\"test\":\"hello\"}}";

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/jsontype/json-test.bal");
    }

    // Test Remove-Function.
    @Test(description = "Remove an element in a valid jsonpath")
    public void testRemove() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "remove", args);

        final String expected = "{\"state\":\"CA\",\"age\":20}";
        Assert.assertEquals(getJsonAsString(returns[0]), expected);
    }

    // Test toString-Function.
    @Test(description = "Get string representation of json")
    public void testToString() {
        BValue[] args = {new BJSON(json1)};
        BValue[] returns = BRunUtil.invoke(compileResult, "toString", args);

        final String expected = "{\"name\":{\"fname\":\"Jack\",\"lname\":\"Taylor\"},\"state\":\"CA\",\"age\":20}";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(description = "Get JSON string from a string")
    public void testParseString() {
        BValue[] args = {new BString("\"hello\"")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testParse", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(((BJSON) returns[0]).value().getType(), Type.STRING);
        Assert.assertEquals(returns[0].stringValue(), "hello");
    }

    @Test(description = "Get JSON boolean from a string")
    public void testParseBoolean() {
        BValue[] args = {new BString("true")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testParse", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(((BJSON) returns[0]).value().getType(), Type.BOOLEAN);
        Assert.assertEquals(returns[0].stringValue(), "true");
    }

    @Test(description = "Get JSON number from a string")
    public void testParseNumber() {
        BValue[] args = {new BString("45678")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testParse", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(((BJSON) returns[0]).value().getType(), Type.LONG);
        Assert.assertEquals(returns[0].stringValue(), "45678");
    }

    @Test(description = "Get JSON null from a string")
    public void testParseNull() {
        BValue[] args = {new BString("null")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testParse", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(((BJSON) returns[0]).value().getType(), Type.NULL);
        Assert.assertEquals(returns[0].stringValue(), "null");
    }

    @Test(description = "Get JSON object from a string")
    public void testParseObject() {
        BValue[] args = {new BString("{\"name\":\"supun\"}")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testParse", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(((BJSON) returns[0]).value().getType(), Type.OBJECT);
        Assert.assertEquals(returns[0].stringValue(), "{\"name\":\"supun\"}");
    }

    @Test(description = "Get JSON array from a string")
    public void testParseArray() {
        BValue[] args = {new BString("[\"supun\", 45, true, null]")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testParse", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(((BJSON) returns[0]).value().getType(), Type.ARRAY);
        Assert.assertEquals(returns[0].stringValue(), "[\"supun\",45,true,null]");
    }

    @Test(description = "Get JSON array from a string")
    public void testParseComplexObject() {
        BValue[] args = {new BString("{\"name\":\"supun\",\"address\":{\"street\":\"Palm Grove\"}, " +
                                             "\"marks\":[78, 45, 87]}")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testParse", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(((BJSON) returns[0]).value().getType(), Type.OBJECT);
        Assert.assertEquals(returns[0].stringValue(), "{\"name\":\"supun\",\"address\":{\"street\":\"Palm Grove\"}," +
                "\"marks\":[78,45,87]}");
    }

    @Test(description = "Get JSON from a malformed string")
    public void testParseMalformedString() {
        BValue[] args = {new BString("some words without quotes")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testParse", args);
        Assert.assertTrue(returns[0] instanceof BStruct);
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0),
                "Failed to parse json string: unrecognized token 'some' at line: 1 column: 6");
    }

    @Test(description = "Convert complex json object in to xml")
    public void testToXMLComplexObject() {
        BValue[] args = {new BJSON(jsonToXML1)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testToXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), "<bookStore><storeName>foo"
                + "</storeName><postalCode>94</postalCode><isOpen>true</isOpen><address><street>PalmGrove</street>"
                + "<city>Colombo</city><country>SriLanka</country></address><codes><item>4</item><item>8</item>"
                + "<item>9</item></codes></bookStore>");
    }

    @Test(description = "Convert json object with array within")
    public void testToXMLArrayObject() {
        BValue[] args = {new BJSON(jsonToXML2)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testToXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), "<books><item><bookName>book1</bookName><bookId>101</bookId>"
                + "</item><item><bookName>book2</bookName><bookId>102</bookId></item><item><bookName>book3</bookName>"
                + "<bookId>103</bookId></item></books>");
    }

    @Test(description = "Convert json object which has an array within an array")
    public void testToXMLArrayWithinArrayObject() {
        BValue[] args = {new BJSON(jsonToXML3)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testToXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), "<books><item><item><bookName>book1</bookName><bookId>101"
                + "</bookId></item></item><item><item><bookName>book2</bookName><bookId>102</bookId></item></item>"
                + "<item><item><bookName>book3</bookName><bookId>103</bookId></item></item></books>");
    }

    @Test(description = "Convert json object with simple array")
    public void testToXMLSimpleArrayObject() {
        BValue[] args = {new BJSON(jsonToXML4)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testToXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), "<books><item>book1</item><item>book2</item><item>book3"
                + "</item></books>");
    }

    @Test(description = "Convert a json object with number value only")
    public void testToXMLJsonWithValue() {
        BValue[] args = {new BJSON(jsonToXML5)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testToXMLString", args);

        Assert.assertTrue(returns[0] instanceof BString);

        String returnElement = (returns[0]).stringValue();
        Assert.assertEquals(returnElement, "5");
    }

    @Test(description = "Convert a json object with string value only")
    public void testToXMLJsonWithStringValue() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testToXMLStringValue");

        Assert.assertTrue(returns[0] instanceof BXML);
        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(((OMText) returnElement).getText(), "value");
    }

    @Test(description = "Convert a json object with boolean value only")
    public void testToXMLBooleanValue() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testToXMLBooleanValue");

        Assert.assertTrue(returns[0] instanceof BXML);
        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(((OMText) returnElement).getText(), "true");
    }

    @Test(description = "Convert json object with key value")
    public void testToXMLKeyValue() {
        BValue[] args = {new BJSON(jsonToXML15)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testToXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), "<key>value</key>");
    }

    @Test(description = "Convert a json object")
    public void testToXMLJsonObject() {
        BValue[] args = {new BJSON(jsonToXML16)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testToXMLWithXMLSequence", args);

        Assert.assertTrue(returns[0] instanceof BString);

        String textValue = returns[0].stringValue();
        Assert.assertEquals(textValue, "<key1>value1</key1><key2>value2</key2>");
    }

    @Test(description = "Convert a json array with different types")
    public void testToXMLJsonArrayWithDifferentTypes() {
        BValue[] args = {new BJSON(jsonToXML17)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testToXMLWithXMLSequence", args);

        Assert.assertTrue(returns[0] instanceof BString);

        String textValue = returns[0].stringValue();
        Assert.assertEquals(textValue, "<item>test</item><item>6</item><item>true</item>");
    }

    @Test(description = "Convert json object with attribute")
    public void testToXMLKeyWithAttribute() {
        BValue[] args = {new BJSON(jsonToXML18)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testToXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), "<foo key=\"value\"/>");
    }

    @Test(description = "Convert json object with attribute and value")
    public void testToXMLKeyWithAttributeAndValue() {
        BValue[] args = {new BJSON(jsonToXML19)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testToXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), "<foo key=\"value\"><test>hello</test></foo>");
    }

    @Test(description = "Convert a json object with array")
    public void testToXMLJsonArray() {
        BValue[] args = {new BJSON(jsonToXML6)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testToXMLWithXMLSequence", args);

        Assert.assertTrue(returns[0] instanceof BString);

        String textValue = returns[0].stringValue();
        Assert.assertEquals(textValue, "<item>3</item><item>4</item><item>5</item>");
    }

    @Test(description = "Convert a json object with multi rooted object")
    public void testToXMLMultirootedObjects() {
        BValue[] args = {new BJSON(jsonToXML7)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testToXMLWithXMLSequence", args);

        Assert.assertTrue(returns[0] instanceof BString);

        String textValue = returns[0].stringValue();
        Assert.assertEquals(textValue, "<bookStore><storeName>foo</storeName><postalCode>94</postalCode>"
                + "<isOpen>true</isOpen><address><street>PalmGrove</street><city>Colombo</city>"
                + "<country>SriLanka</country></address><codes><item>4</item><item>8</item><item>9</item></codes>"
                + "</bookStore><metaInfo>someinfo</metaInfo>");
    }

    @Test(description = "Convert a json object with null elements in the array")
    public void testToXMLNullElementsInArray() {
        BValue[] args = {new BJSON(jsonToXML8)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testToXMLWithXMLSequence", args);

        Assert.assertTrue(returns[0] instanceof BString);

        String textValue = returns[0].stringValue();
        Assert.assertEquals(textValue, "<name>John</name><age>30</age>"
                + "<car xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/>");
    }

    @Test(description = "Convert a json object with null object elements")
    public void testToXMLNullElementsInObject() {
        BValue[] args = {new BJSON(jsonToXML9)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testToXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), "<Person><name>John</name><age>30</age>"
                + "<car xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/></Person>");
    }

    @Test(description = "Convert a json object with empty elements")
    public void testToXMLEmptyElements() {
        BValue[] args = {new BJSON(jsonToXML10)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testToXMLWithXMLSequence", args);

        Assert.assertTrue(returns[0] instanceof BString);

        String textValue = returns[0].stringValue();
        Assert.assertEquals(textValue, "<address/><homeAddresses/><phoneNumbers/>");
    }

    @Test(description = "Convert a json object with empty elements and non emepty elements")
    public void testToXMLEmptyElementsWithNonEmpty() {
        BValue[] args = {new BJSON(jsonToXML11)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testToXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);
        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), "<info><address/><homeAddresses><item>a</item><item>b</item>"
                + "</homeAddresses><phoneNumbers/></info>");
    }

    @Test(description = "Convert a simple json object with attributes")
    public void testToXMLAttributes() {
        BValue[] args = {new BJSON(jsonToXML12)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testToXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);
        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), "<info id=\"100\"><name>John</name><age>30</age><car>honda</car>"
                + "</info>");
    }

    @Test(description = "Convert a complex json object with attributes")
    public void testToXMLComplexAttributes() {
        BValue[] args = {new BJSON(jsonToXML13)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testToXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);
        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), "<bookStore storeName=\"foo\"><postalCode>94</postalCode>"
                + "<isOpen>true</isOpen><address city=\"Colombo\"><street>PalmGrove</street><country>SriLanka</country>"
                + "</address><codes><item>4</item><item>8</item><item>9</item></codes></bookStore>");
    }

    @Test(description = "Convert a complex json object with attributes and custom attribute prefix")
    public void testToXMLComplexAttributesCustomPrefix() {
        BValue[] args = {new BJSON(jsonToXML14)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testToXMLWithOptions", args);

        Assert.assertTrue(returns[0] instanceof BXML);
        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), "<bookStore storeName=\"foo\"><postalCode>94</postalCode>"
                + "<isOpen>true</isOpen><address city=\"Colombo\"><street>PalmGrove</street><country>SriLanka</country>"
                + "</address><codes><wrapper>4</wrapper><wrapper>8</wrapper><wrapper>9</wrapper></codes></bookStore>");
    }

    private String getJsonAsString(BValue bValue) {
        return bValue.stringValue().replace("\\r|\\n|\\t| ", "");
    }

    @Test(description = "Get keys from a JSON")
    public void testGetKeys() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetKeys", args);

        Assert.assertTrue(returns[0] instanceof BStringArray);
        BStringArray keys = (BStringArray) returns[0];
        Assert.assertEquals(keys.size(), 3);
        Assert.assertEquals(keys.get(0), "fname");
        Assert.assertEquals(keys.get(1), "lname");
        Assert.assertEquals(keys.get(2), "age");

        Assert.assertTrue(returns[1] instanceof BStringArray);
        Assert.assertEquals(((BStringArray) returns[1]).size(), 0);

        Assert.assertTrue(returns[2] instanceof BStringArray);
        Assert.assertEquals(((BStringArray) returns[2]).size(), 0);

        Assert.assertTrue(returns[3] instanceof BStringArray);
        Assert.assertEquals(((BStringArray) returns[3]).size(), 0);
    }

    @Test(description = "Convert a complex json object to xml, with an unsupported tag name")
    public void testJSONToXMLWithUnsupportedChar() {
        BValue[] args = {new BJSON(jsonToXML13)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testToXMLWithOptions", args);

        Assert.assertTrue(returns[0] instanceof BStruct);
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0),
                "failed to convert json to xml: invalid xml qualified name: unsupported characters in '@storeName'");
    }

    @Test(description = "Convert a string to json")
    public void testStringToJSONConversion() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStringToJSONConversion");
        Assert.assertEquals(returns[0].stringValue(), "{\"foo\":\"bar\"}");
    }

    @Test
    public void testJSONArrayToJsonAssignment() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJSONArrayToJsonAssignment");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "[{\"a\":\"b\"},{\"c\":\"d\"}]");
    }

    @Test
    public void testFieldAccessOfNullableJSON() {
        CompileResult compileResult = BCompileUtil.compile("test-src/types/jsontype/nullable-json-test.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testFieldAccessOfNullableJSON");
        Assert.assertNull(returns[0]);
    }
}
