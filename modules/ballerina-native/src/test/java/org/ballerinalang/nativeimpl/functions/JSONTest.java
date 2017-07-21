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
package org.ballerinalang.nativeimpl.functions;

import com.fasterxml.jackson.databind.node.JsonNodeType;

import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueType;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test Native functions in ballerina.model.json.
 */
@SuppressWarnings("javadoc")
public class JSONTest {

    private ProgramFile bLangProgram;
    private static final String json1 = "{'name':{'fname':'Jack','lname':'Taylor'}, 'state':'CA', 'age':20}";
    private static final String json2 = "{'item':{'price':3.54, 'available':true}}";
    private static final String jsonStringArray = "{'users':['Jack', 'Peter']}";
    private static final String jsonIntArray = "{'ages':[25, 28]}";
    private static final String jsonFloatArray = "{'prices':[3.12, 4.87]}";
    private static final String jsonBooleanArray = "{'availability':[true, false]}";
    private static final String jsonElementArray = "{'persons':[{'fname':'Jack','lname':'Taylor'}, {'fname':'Peter'," +
            "'lname':'Roy'}]}";
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
        bLangProgram = BTestUtils.getProgramFile("samples/jsonTest.bal");
    }

    
    /*
     * Test Get-Functions 
     */

    @Test(description = "Get a string in a valid jsonpath")
    public void testGetString() {
        BValue[] args = {new BJSON(json1), new BString("$.name.fname")};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "getString", args);

        Assert.assertEquals(returns[0].stringValue(), "Jack");
    }

    @Test(description = "Get an integer in a valid jsonpath")
    public void testGetInt() {
        BValue[] args = {new BJSON(json1), new BString("$.age")};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "getInt", args);

        Assert.assertEquals(((BValueType) returns[0]).intValue(), 20);
    }

    @Test(description = "Get a json element in a valid jsonpath")
    public void testGetJSON() {
        BValue[] args = {new BJSON(json1), new BString("$.name")};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "getJson", args);

        Assert.assertEquals(getJsonAsString(returns[0]), "{\"fname\":\"Jack\",\"lname\":\"Taylor\"}");
    }

    @Test(description = "Get a float in a valid jsonpath")
    public void testGetFloat() {
        BValue[] args = {new BJSON(json2), new BString("$.item.price")};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "getFloat", args);

        Assert.assertEquals(((BValueType) returns[0]).floatValue(), 3.54);
    }

    @Test(description = "Get a double in a valid jsonpath")
    public void testGetDouble() {
        BValue[] args = {new BJSON(json2), new BString("$.item.price")};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "getFloat", args);

        Assert.assertEquals(((BValueType) returns[0]).floatValue(), 3.54);
    }

    @Test(description = "Get a float in a valid jsonpath")
    public void testGetBoolean() {
        BValue[] args = {new BJSON(json2), new BString("$.item.available")};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "getBoolean", args);

        Assert.assertEquals(((BValueType) returns[0]).booleanValue(), true);
    }

    @Test(description = "Get json arrays length")
    public void testGetArrayLength() {
        BValue[] args = {new BJSON(jsonStringArray), new BString("$.users.length()")};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "getInt", args);

        final int expected = 2;
        Assert.assertEquals(((BValueType) returns[0]).intValue(), expected);
    }

    @Test(description = "Get json arrays min value")
    public void testGetArrayMin() {
        BValue[] args = {new BJSON(jsonIntArray), new BString("$.ages.min()")};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "getFloat", args);

        final double expected = 25;
        Assert.assertEquals(((BValueType) returns[0]).floatValue(), expected);
    }

    @Test(description = "Get json arrays max value")
    public void testGetArrayMax() {
        BValue[] args = {new BJSON(jsonIntArray), new BString("$.ages.max()")};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "getFloat", args);

        final double expected = 28;
        Assert.assertEquals(((BValueType) returns[0]).floatValue(), expected);
    }

    @Test(description = "Get json arrays average value")
    public void testGetArrayAvg() {
        BValue[] args = {new BJSON(jsonIntArray), new BString("$.ages.avg()")};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "getFloat", args);

        final double expected = 26.5;
        Assert.assertEquals(((BValueType) returns[0]).floatValue(), expected);
    }

    @Test(description = "Get json arrays standard deviation of population")
    public void testGetArrayStdDevP() {
        BValue[] args = {new BJSON(jsonIntArray), new BString("$.ages.stddev()")};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "getFloat", args);

        final double expected = 1.5;
        Assert.assertEquals(((BValueType) returns[0]).floatValue(), expected);
    }

    @Test(description = "Get a value of incorrect type", expectedExceptions = {BLangRuntimeException.class})
    public void testGetValueOfIncorrectType() {
        BValue[] args = {new BJSON(json1), new BString("$.name")};
        BLangFunctions.invokeNew(bLangProgram, "getInt", args);
    }

    @Test(description = "Get a value in a non-existing jsonpath", expectedExceptions = BLangRuntimeException.class)
    public void testGetNonExistingValue() {
        BValue[] args = {new BJSON(json1), new BString("$.name.surname")};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "getString", args);

        Assert.assertEquals(returns[0], null);
    }

    @Test(description = "Get a value using a malformed jsonpath", expectedExceptions = {BLangRuntimeException.class})
    public void testGetValueFromInvalidJsonpath() {
        BValue[] args = {new BJSON(json1), new BString("$/wrong/path.")};
        BLangFunctions.invokeNew(bLangProgram, "getString", args);
    }


    /*
     * Test Set-Functions
     */

    @Test(description = "Set a string to a valid jsonpath")
    public void testSetString() {
        final String val = "Paul";
        BValue[] args = {new BJSON(json1), new BString("$.name.fname"), new BString(val)};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "setString", args);

        Assert.assertEquals(returns[0].stringValue(), val);
    }

    @Test(description = "Set an int to a valid jsonpath")
    public void testSetInteger() {
        final int val = 25;
        BValue[] args = {new BJSON(json1), new BString("$.age"), new BInteger(val)};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "setInt", args);

        Assert.assertEquals(((BValueType) returns[0]).intValue(), val);
    }

    @Test(description = "Set a float to a valid jsonpath")
    public void testSetFloat() {
        final double val = 4.78;
        BValue[] args = {new BJSON(json2), new BString("$.item.price"), new BFloat(val)};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "setFloat", args);

        Assert.assertEquals(((BValueType) returns[0]).floatValue(), val);
    }

    @Test(description = "Set a boolean to a valid jsonpath")
    public void testSetBoolean() {
        final boolean val = false;
        BValue[] args = {new BJSON(json2), new BString("$.item.available"), new BBoolean(val)};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "setBoolean", args);

        Assert.assertEquals(((BValueType) returns[0]).booleanValue(), val);
    }

    @Test(description = "Set a json element to a valid jsonpath")
    public void testSetJSON() {
        final String val = "{\"id\":\"item123\"}";
        BValue[] args = {new BJSON(json2), new BString("$.item.available"), new BJSON(val)};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "setJson", args);

        Assert.assertEquals(getJsonAsString(returns[0]), val);
    }

    @Test(description = "Set a value to a non-existing jsonpath", expectedExceptions = BLangRuntimeException.class)
    public void testSetNonExistingValue() {
        BValue[] args = {new BJSON(json1), new BString("$.name.surname"), new BString("Paul")};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "setString", args);

        final String expected = "{\"name\":{\"fname\":\"Jack\",\"lname\":\"Taylor\"},\"state\":\"CA\",\"age\":20}";
        Assert.assertEquals(args[0].stringValue(), expected);
    }

    @Test(description = "Set a value using a malformed jsonpath", expectedExceptions = {BLangRuntimeException.class})
    public void testSetValueToInvalidJsonpath() {
        BValue[] args = {new BJSON(json1), new BString("$/wrong/path.")};
        BLangFunctions.invokeNew(bLangProgram, "getString", args);
    }

    /*
     * Test Add-to-object Functions
     */

    @Test(description = "Add a string to a valid json object")
    public void testAddStringToObject() {
        BValue[] args = {new BJSON(json1), new BString("$.name"), new BString("nickName"),
                new BString("Paul")};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "addStringToObject", args);

        final String expected = "{\"name\":{\"fname\":\"Jack\",\"lname\":\"Taylor\",\"nickName\":\"Paul\"}," +
                "\"state\":\"CA\",\"age\":20}";
        Assert.assertEquals(getJsonAsString(returns[0]), expected);
    }

    @Test(description = "Add an integer to a valid json object")
    public void testAddIntToObject() {
        BValue[] args = {new BJSON(json1), new BString("$"), new BString("zipCode"),
                new BInteger(90001)};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "addIntToObject", args);

        final String expected = "{\"name\":{\"fname\":\"Jack\",\"lname\":\"Taylor\"},\"state\":\"CA\",\"age\":20," +
                "\"zipCode\":90001}";
        Assert.assertEquals(getJsonAsString(returns[0]), expected);
    }

    @Test(description = "Add a float to a valid json object")
    public void testAddFloatToObject() {
        BValue[] args = {new BJSON(json2), new BString("$.item"), new BString("discount"),
                new BFloat(0.15)};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "addFloatToObject", args);

        final String expected = "{\"item\":{\"price\":3.54,\"available\":true,\"discount\":0.15}}";
        Assert.assertEquals(getJsonAsString(returns[0]), expected);
    }

    @Test(description = "Add a boolean to a valid json object")
    public void testAddBooleanToObject() {
        BValue[] args = {new BJSON(json2), new BString("$.item"), new BString("vegi"),
                new BBoolean(true)};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "addBooleanToObject", args);

        final String expected = "{\"item\":{\"price\":3.54,\"available\":true,\"vegi\":true}}";
        Assert.assertEquals(getJsonAsString(returns[0]), expected);
    }

    @Test(description = "Add an element to a valid json object")
    public void testAddElementToObject() {
        BValue[] args = {new BJSON(json2), new BString("$.item"), new BString("expires"),
                new BJSON("{\"year\":2020,\"month\":12}")};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "addElementToObject", args);

        final String expected = "{\"item\":{\"price\":3.54,\"available\":true,\"expires\":{\"year\":2020,\"month\"" +
                ":12}}}";
        Assert.assertEquals(getJsonAsString(returns[0]), expected);
    }

    @Test(description = "Add a value to a non-existing object")
    public void testAddToNonExistingObject() {
        BValue[] args = {new BJSON(json1), new BString("$.name.surname"), new BString("nickName"),
                new BString("Paul")};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "addStringToObject", args);

        final String expected = "{\"name\":{\"fname\":\"Jack\",\"lname\":\"Taylor\"},\"state\":\"CA\",\"age\":20}";
        Assert.assertEquals(getJsonAsString(returns[0]), expected);
    }

    @Test(description = "Add a value to object, using a malformed jsonpath",
            expectedExceptions = {BLangRuntimeException.class})
    public void testAddToInvalidJsonpathObject() {
        BValue[] args = {new BJSON(json1), new BString("$/wrong/path."), new BString("nickName"),
                new BString("Paul")};
        BLangFunctions.invokeNew(bLangProgram, "addStringToObject", args);
    }

    /*
     * Test Add-to-arrays Functions
     */

    @Test(description = "Add a string to a valid json arrays")
    public void testAddStringToArray() {
        BValue[] args = {new BJSON(jsonStringArray), new BString("$.users"), new BString("Jos")};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "addStringToArray", args);

        final String expected = "{\"users\":[\"Jack\",\"Peter\",\"Jos\"]}";
        Assert.assertEquals(getJsonAsString(returns[0]), expected);
    }

    @Test(description = "Add an integer to a valid json arrays")
    public void testAddIntToArray() {
        BValue[] args = {new BJSON(jsonIntArray), new BString("$.ages"), new BInteger(23)};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "addIntToArray", args);

        final String expected = "{\"ages\":[25,28,23]}";
        Assert.assertEquals(getJsonAsString(returns[0]), expected);
    }

    @Test(description = "Add a float to a valid json arrays")
    public void testAddFloatToArray() {
        BValue[] args = {new BJSON(jsonFloatArray), new BString("$.prices"),
                new BFloat(5.96)};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "addFloatToArray", args);

        final String expected = "{\"prices\":[3.12,4.87,5.96]}";
        Assert.assertEquals(getJsonAsString(returns[0]), expected);
    }

    @Test(description = "Add a boolean to a valid json arrays")
    public void testAddBooleanToArray() {
        BValue[] args = {new BJSON(jsonBooleanArray), new BString("$.availability"),
                new BBoolean(true)};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "addBooleanToArray", args);

        final String expected = "{\"availability\":[true,false,true]}";
        Assert.assertEquals(getJsonAsString(returns[0]), expected);
    }

    @Test(description = "Add an element to a valid json arrays")
    public void testAddElementToArray() {
        BValue[] args = {new BJSON(jsonElementArray), new BString("$.persons"),
                new BJSON("{'fname':'Jos','lname':'Allen'}")};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "addElementToArray", args);

        final String expected = "{\"persons\":[{\"fname\":\"Jack\",\"lname\":\"Taylor\"},{\"fname\":\"Peter\"," +
                "\"lname\":\"Roy\"},{\"fname\":\"Jos\",\"lname\":\"Allen\"}]}";
        Assert.assertEquals(getJsonAsString(returns[0]), expected);
    }

    @Test(description = "Add a value to a non-existing arrays")
    public void testAddToNonExistingArray() {
        BValue[] args = {new BJSON(jsonStringArray), new BString("$.persons"), new BString("Jos")};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "addStringToArray", args);

        final String expected = "{\"users\":[\"Jack\",\"Peter\"]}";
        Assert.assertEquals(getJsonAsString(returns[0]), expected);
    }

    @Test(description = "Add a value to arrays, using a malformed jsonpath",
            expectedExceptions = {BLangRuntimeException.class})
    public void testAddToInvalidJsonpathArray() {
        BValue[] args = {new BJSON(jsonIntArray), new BString("$/wrong/path."), new BInteger(23)};
        BLangFunctions.invokeNew(bLangProgram, "addIntToArray", args);
    }


    /*
     * Test Remove-Function.
     */

    @Test(description = "Remove an element in a valid jsonpath")
    public void testRemove() {
        BValue[] args = {new BJSON(json1), new BString("$.name")};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "remove", args);

        final String expected = "{\"state\":\"CA\",\"age\":20}";
        Assert.assertEquals(getJsonAsString(returns[0]), expected);
    }

    @Test(description = "Remove an element in a non-existing jsonpath")
    public void testRemoveNonExistingElement() {
        BValue[] args = {new BJSON(json1), new BString("$.user")};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "remove", args);

        final String expected = "{\"name\":{\"fname\":\"Jack\",\"lname\":\"Taylor\"},\"state\":\"CA\",\"age\":20}";
        Assert.assertEquals(getJsonAsString(returns[0]), expected);
    }

    @Test(description = "Remove an element in a malformed jsonpath",
            expectedExceptions = {BLangRuntimeException.class})
    public void testRemoveElementFromInvalidJsonpath() {
        BValue[] args = {new BJSON(json1), new BString("$/wrong/path.")};
        BLangFunctions.invokeNew(bLangProgram, "remove", args);
    }


    /*
     * Test Rename-Function.
     */

    @Test(description = "Rename an element in a valid jsonpath")
    public void testRename() {
        BValue[] args = {new BJSON(json1), new BString("$.name"), new BString("fname"), new BString("firstName")};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "rename", args);

        final String expected = "Jack";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(description = "Rename an element in a non-existing jsonpath",
            expectedExceptions = BLangRuntimeException.class)
    public void testRenameNonExistingElement() {
        BValue[] args = {new BJSON(json1), new BString("$.user"), new BString("fname"), new BString("firstName")};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "rename", args);

        final String expected = "{\"name\":{\"fname\":\"Jack\",\"lname\":\"Taylor\"},\"state\":\"CA\",\"age\":20}";
        Assert.assertEquals(args[0].stringValue(), expected);
    }

    @Test(description = "Rename an element in a malformed jsonpath",
            expectedExceptions = {BLangRuntimeException.class})
    public void testRenameElementFromInvalidJsonpath() {
        BValue[] args = {new BJSON(json1), new BString("$/wrong/path.")};
        BLangFunctions.invokeNew(bLangProgram, "remove", args);
    }

    /*
     * Test toString-Function.
     */
    @Test(description = "Get string representation of json")
    public void testToString() {
        BValue[] args = {new BJSON(json1)};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "toString", args);

        final String expected = "{\"name\":{\"fname\":\"Jack\",\"lname\":\"Taylor\"},\"state\":\"CA\",\"age\":20}";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(description = "Get JSON string from a string")
    public void testParseString() {
        BValue[] args = { new BString("\"hello\"") };
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testParse", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(((BJSON) returns[0]).value().getNodeType(), JsonNodeType.STRING);
        Assert.assertEquals(returns[0].stringValue(), "hello");
    }

    @Test(description = "Get JSON boolean from a string")
    public void testParseBoolean() {
        BValue[] args = { new BString("true") };
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testParse", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(((BJSON) returns[0]).value().getNodeType(), JsonNodeType.BOOLEAN);
        Assert.assertEquals(returns[0].stringValue(), "true");
    }

    @Test(description = "Get JSON number from a string")
    public void testParseNumber() {
        BValue[] args = { new BString("45678") };
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testParse", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(((BJSON) returns[0]).value().getNodeType(), JsonNodeType.NUMBER);
        Assert.assertEquals(returns[0].stringValue(), "45678");
    }

    @Test(description = "Get JSON null from a string")
    public void testParseNull() {
        BValue[] args = { new BString("null") };
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testParse", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(((BJSON) returns[0]).value().getNodeType(), JsonNodeType.NULL);
        Assert.assertEquals(returns[0].stringValue(), "null");
    }

    @Test(description = "Get JSON object from a string")
    public void testParseObject() {
        BValue[] args = { new BString("{\"name\":\"supun\"}") };
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testParse", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(((BJSON) returns[0]).value().getNodeType(), JsonNodeType.OBJECT);
        Assert.assertEquals(returns[0].stringValue(), "{\"name\":\"supun\"}");
    }

    @Test(description = "Get JSON array from a string")
    public void testParseArray() {
        BValue[] args = { new BString("[\"supun\", 45, true, null]") };
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testParse", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(((BJSON) returns[0]).value().getNodeType(), JsonNodeType.ARRAY);
        Assert.assertEquals(returns[0].stringValue(), "[\"supun\",45,true,null]");
    }

    @Test(description = "Get JSON array from a string")
    public void testParseComplexObject() {
        BValue[] args = { new BString("{\"name\":\"supun\",\"address\":{\"street\":\"Palm Grove\"}, " +
                "\"marks\":[78, 45, 87]}") };
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testParse", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(((BJSON) returns[0]).value().getNodeType(), JsonNodeType.OBJECT);
        Assert.assertEquals(returns[0].stringValue(), "{\"name\":\"supun\",\"address\":{\"street\":\"Palm Grove\"}," +
                "\"marks\":[78,45,87]}");
    }

    @Test(description = "Get JSON from a malformed string",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:Error, message: failed to parse json: " +
            "Unrecognized token 'some': was expecting \\('true', 'false' or 'null'\\)\n at \\[Source: some words " +
            "without quotes; line: 1, column: 5\\].*")
    public void testParseMalformedString() {
        BValue[] args = { new BString("some words without quotes") };
        BLangFunctions.invokeNew(bLangProgram, "testParse", args);
    }

    @Test(description = "Convert complex json object in to xml")
    public void testToXMLComplexObject() {
        BValue[] args = { new BJSON(jsonToXML1) };
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testToXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), "<bookStore><storeName>foo"
                + "</storeName><postalCode>94</postalCode><isOpen>true</isOpen><address><street>PalmGrove</street>"
                + "<city>Colombo</city><country>SriLanka</country></address><codes><item>4</item><item>8</item>"
                + "<item>9</item></codes></bookStore>");
    }

    @Test(description = "Convert json object with array within")
    public void testToXMLArrayObject() {
        BValue[] args = { new BJSON(jsonToXML2) };
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testToXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), "<books><item><bookName>book1</bookName><bookId>101</bookId>"
                + "</item><item><bookName>book2</bookName><bookId>102</bookId></item><item><bookName>book3</bookName>"
                + "<bookId>103</bookId></item></books>");
    }

    @Test(description = "Convert json object which has an array within an array")
    public void testToXMLArrayWithinArrayObject() {
        BValue[] args = { new BJSON(jsonToXML3) };
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testToXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), "<books><item><item><bookName>book1</bookName><bookId>101"
                + "</bookId></item></item><item><item><bookName>book2</bookName><bookId>102</bookId></item></item>"
                + "<item><item><bookName>book3</bookName><bookId>103</bookId></item></item></books>");
    }

    @Test(description = "Convert json object with simple array")
    public void testToXMLSimpleArrayObject() {
        BValue[] args = { new BJSON(jsonToXML4) };
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testToXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), "<books><item>book1</item><item>book2</item><item>book3"
                + "</item></books>");
    }

    @Test(description = "Convert a json object with number value only")
    public void testToXMLJsonWithValue() {
        BValue[] args = { new BJSON(jsonToXML5) };
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testToXMLString", args);

        Assert.assertTrue(returns[0] instanceof BString);

        String returnElement = (returns[0]).stringValue();
        Assert.assertEquals(returnElement, "5");
    }

    @Test(description = "Convert a json object with string value only")
    public void testToXMLJsonWithStringValue() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testToXMLStringValue");

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(((OMText) returnElement).getText(), "value");
    }

    @Test(description = "Convert a json object with boolean value only")
    public void testToXMLBooleanValue() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testToXMLBooleanValue");

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(((OMText) returnElement).getText(), "true");
    }

    @Test(description = "Convert json object with key value")
    public void testToXMLKeyValue() {
        BValue[] args = { new BJSON(jsonToXML15) };
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testToXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), "<key>value</key>");
    }

    @Test(description = "Convert a json object")
    public void testToXMLJsonObject() {
        BValue[] args = { new BJSON(jsonToXML16) };
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testToXMLWithXMLSequence", args);

        Assert.assertTrue(returns[0] instanceof BString);

        String textValue = returns[0].stringValue();
        Assert.assertEquals(textValue, "<key1>value1</key1><key2>value2</key2>");
    }

    @Test(description = "Convert a json array with different types")
    public void testToXMLJsonArrayWithDifferentTypes() {
        BValue[] args = { new BJSON(jsonToXML17) };
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testToXMLWithXMLSequence", args);

        Assert.assertTrue(returns[0] instanceof BString);

        String textValue = returns[0].stringValue();
        Assert.assertEquals(textValue, "<item>test</item><item>6</item><item>true</item>");
    }

    @Test(description = "Convert json object with attribute")
    public void testToXMLKeyWithAttribute() {
        BValue[] args = { new BJSON(jsonToXML18) };
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testToXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), "<foo key=\"value\"/>");
    }

    @Test(description = "Convert json object with attribute and value")
    public void testToXMLKeyWithAttributeAndValue() {
        BValue[] args = { new BJSON(jsonToXML19) };
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testToXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), "<foo key=\"value\"><test>hello</test></foo>");
    }

    @Test(description = "Convert a json object with array")
    public void testToXMLJsonArray() {
        BValue[] args = { new BJSON(jsonToXML6) };
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testToXMLWithXMLSequence", args);

        Assert.assertTrue(returns[0] instanceof BString);

        String textValue = returns[0].stringValue();
        Assert.assertEquals(textValue, "<item>3</item><item>4</item><item>5</item>");
    }

    @Test(description = "Convert a json object with multi rooted object")
    public void testToXMLMultirootedObjects() {
        BValue[] args = { new BJSON(jsonToXML7) };
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testToXMLWithXMLSequence", args);

        Assert.assertTrue(returns[0] instanceof BString);

        String textValue = returns[0].stringValue();
        Assert.assertEquals(textValue, "<bookStore><storeName>foo</storeName><postalCode>94</postalCode>"
                + "<isOpen>true</isOpen><address><street>PalmGrove</street><city>Colombo</city>"
                + "<country>SriLanka</country></address><codes><item>4</item><item>8</item><item>9</item></codes>"
                + "</bookStore><metaInfo>someinfo</metaInfo>");
    }

    @Test(description = "Convert a json object with null elements in the array")
    public void testToXMLNullElementsInArray() {
        BValue[] args = { new BJSON(jsonToXML8) };
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testToXMLWithXMLSequence", args);

        Assert.assertTrue(returns[0] instanceof BString);

        String textValue = returns[0].stringValue();
        Assert.assertEquals(textValue, "<name>John</name><age>30</age>"
                + "<car xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/>");
    }

    @Test(description = "Convert a json object with null object elements")
    public void testToXMLNullElementsInObject() {
        BValue[] args = { new BJSON(jsonToXML9) };
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testToXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), "<Person><name>John</name><age>30</age>"
                + "<car xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/></Person>");
    }

    @Test(description = "Convert a json object with empty elements")
    public void testToXMLEmptyElements() {
        BValue[] args = { new BJSON(jsonToXML10) };
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testToXMLWithXMLSequence", args);

        Assert.assertTrue(returns[0] instanceof BString);

        String textValue = returns[0].stringValue();
        Assert.assertEquals(textValue, "<address/><homeAddresses/><phoneNumbers/>");
    }

    @Test(description = "Convert a json object with empty elements and non emepty elements")
    public void testToXMLEmptyElementsWithNonEmpty() {
        BValue[] args = { new BJSON(jsonToXML11) };
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testToXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), "<info><address/><homeAddresses><item>a</item><item>b</item>"
                + "</homeAddresses><phoneNumbers/></info>");
    }

    @Test(description = "Convert a simple json object with attributes")
    public void testToXMLAttributes() {
        BValue[] args = { new BJSON(jsonToXML12) };
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testToXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), "<info id=\"100\"><name>John</name><age>30</age><car>honda</car>"
                + "</info>");
    }

    @Test(description = "Convert a complex json object with attributes")
    public void testToXMLComplexAttributes() {
        BValue[] args = { new BJSON(jsonToXML13) };
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testToXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), "<bookStore storeName=\"foo\"><postalCode>94</postalCode>"
                + "<isOpen>true</isOpen><address city=\"Colombo\"><street>PalmGrove</street><country>SriLanka</country>"
                + "</address><codes><item>4</item><item>8</item><item>9</item></codes></bookStore>");
    }

    @Test(description = "Convert a complex json object with attributes and custom attribute prefix")
    public void testToXMLComplexAttributesCustomPrefix() {
        BValue[] args = { new BJSON(jsonToXML14) };
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testToXMLWithOptions", args);

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
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "testGetKeys", args);

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
}
