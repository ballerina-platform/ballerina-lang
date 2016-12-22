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
package org.wso2.ballerina.core.nativeimpl.functions;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.interpreter.BLangInterpreter;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.linker.BLangLinker;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BooleanValue;
import org.wso2.ballerina.core.model.values.DoubleValue;
import org.wso2.ballerina.core.model.values.FloatValue;
import org.wso2.ballerina.core.model.values.IntValue;
import org.wso2.ballerina.core.model.values.JSONValue;
import org.wso2.ballerina.core.model.values.StringValue;
import org.wso2.ballerina.core.nativeimpl.lang.json.AddBooleanToArray;
import org.wso2.ballerina.core.nativeimpl.lang.json.AddBooleanToObject;
import org.wso2.ballerina.core.nativeimpl.lang.json.AddDoubleToArray;
import org.wso2.ballerina.core.nativeimpl.lang.json.AddDoubleToObject;
import org.wso2.ballerina.core.nativeimpl.lang.json.AddFloatToArray;
import org.wso2.ballerina.core.nativeimpl.lang.json.AddFloatToObject;
import org.wso2.ballerina.core.nativeimpl.lang.json.AddIntToArray;
import org.wso2.ballerina.core.nativeimpl.lang.json.AddIntToObject;
import org.wso2.ballerina.core.nativeimpl.lang.json.AddJSONToArray;
import org.wso2.ballerina.core.nativeimpl.lang.json.AddJSONToObject;
import org.wso2.ballerina.core.nativeimpl.lang.json.AddStringToArray;
import org.wso2.ballerina.core.nativeimpl.lang.json.AddStringToObject;
import org.wso2.ballerina.core.nativeimpl.lang.json.GetBoolean;
import org.wso2.ballerina.core.nativeimpl.lang.json.GetDouble;
import org.wso2.ballerina.core.nativeimpl.lang.json.GetFloat;
import org.wso2.ballerina.core.nativeimpl.lang.json.GetInt;
import org.wso2.ballerina.core.nativeimpl.lang.json.GetJSON;
import org.wso2.ballerina.core.nativeimpl.lang.json.GetString;
import org.wso2.ballerina.core.nativeimpl.lang.json.Remove;
import org.wso2.ballerina.core.nativeimpl.lang.json.Rename;
import org.wso2.ballerina.core.nativeimpl.lang.json.SetBoolean;
import org.wso2.ballerina.core.nativeimpl.lang.json.SetDouble;
import org.wso2.ballerina.core.nativeimpl.lang.json.SetFloat;
import org.wso2.ballerina.core.nativeimpl.lang.json.SetInt;
import org.wso2.ballerina.core.nativeimpl.lang.json.SetJSON;
import org.wso2.ballerina.core.nativeimpl.lang.json.SetString;
import org.wso2.ballerina.core.nativeimpl.lang.json.ToString;
import org.wso2.ballerina.core.utils.FunctionUtils;
import org.wso2.ballerina.core.utils.ParserUtils;

/**
 * Test Native functions in ballerina.lang.json.
 */
@SuppressWarnings("javadoc")
public class JSONTest {

    private BallerinaFile bFile;
    private static final String json1 = "{'name':{'fname':'Jack','lname':'Taylor'}, 'state':'CA', 'age':20}";
    private static final String json2 = "{'item':{'price':3.54, 'available':true}}";
    private static final String jsonStringArray = "{'users':['Jack', 'Peter']}";
    private static final String jsonIntArray = "{'ages':[25, 28]}";
    private static final String jsonFloatArray = "{'prices':[3.12, 4.87]}";
    private static final String jsonBooleanArray = "{'availability':[true, false]}";
    private static final String jsonElementArray = "{'persons':[{'fname':'Jack','lname':'Taylor'}, {'fname':'Peter'," +
            "'lname':'Roy'}]}";
    
    @BeforeTest
    public void setup() {
        bFile = ParserUtils.parseBalFile("samples/nativeimpl/jsonTest.bal");
        // Linking Native functions.
        SymScope symScope = new SymScope(null);
        FunctionUtils.addNativeFunction(symScope, new AddBooleanToArray());
        FunctionUtils.addNativeFunction(symScope, new AddBooleanToObject());
        FunctionUtils.addNativeFunction(symScope, new AddDoubleToArray());
        FunctionUtils.addNativeFunction(symScope, new AddDoubleToObject());
        FunctionUtils.addNativeFunction(symScope, new AddFloatToArray());
        FunctionUtils.addNativeFunction(symScope, new AddFloatToObject());
        FunctionUtils.addNativeFunction(symScope, new AddIntToArray());
        FunctionUtils.addNativeFunction(symScope, new AddIntToObject());
        FunctionUtils.addNativeFunction(symScope, new AddJSONToArray());
        FunctionUtils.addNativeFunction(symScope, new AddJSONToObject());
        FunctionUtils.addNativeFunction(symScope, new AddJSONToObject());
        FunctionUtils.addNativeFunction(symScope, new AddStringToArray());
        FunctionUtils.addNativeFunction(symScope, new AddStringToObject());
        FunctionUtils.addNativeFunction(symScope, new GetBoolean());
        FunctionUtils.addNativeFunction(symScope, new GetDouble());
        FunctionUtils.addNativeFunction(symScope, new GetFloat());
        FunctionUtils.addNativeFunction(symScope, new GetInt());
        FunctionUtils.addNativeFunction(symScope, new GetJSON());
        FunctionUtils.addNativeFunction(symScope, new GetString());
        FunctionUtils.addNativeFunction(symScope, new Remove());
        FunctionUtils.addNativeFunction(symScope, new Rename());
        FunctionUtils.addNativeFunction(symScope, new SetBoolean());
        FunctionUtils.addNativeFunction(symScope, new SetDouble());
        FunctionUtils.addNativeFunction(symScope, new SetFloat());
        FunctionUtils.addNativeFunction(symScope, new SetInt());
        FunctionUtils.addNativeFunction(symScope, new SetJSON());
        FunctionUtils.addNativeFunction(symScope, new SetString());
        FunctionUtils.addNativeFunction(symScope, new ToString());
        BLangLinker linker = new BLangLinker(bFile);
        linker.link(symScope);
    }

    
    /*
     * Test Get-Functions 
     */
    
    @Test(description = "Get a string in a valid jsonpath")
    public void testGetString() {
        BValue<?>[] arguments = { new JSONValue(json1), new StringValue("$.name.fname") };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "getString", arguments);
        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        Assert.assertEquals(FunctionUtils.getValue(bContext).getString(), "Jack");
    }

    @Test(description = "Get an integer in a valid jsonpath")
    public void testGetInt() {
        BValue<?>[] arguments = { new JSONValue(json1), new StringValue("$.age") };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "getInt", arguments);
        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        Assert.assertEquals(FunctionUtils.getValue(bContext).getInt(), 20);
    }

    @Test(description = "Get a json element in a valid jsonpath")
    public void testGetJSON() {
        BValue<?>[] arguments = { new JSONValue(json1), new StringValue("$.name") };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "getJson", arguments);
        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        Assert.assertEquals(getJsonAsString(bContext), "{\"fname\":\"Jack\",\"lname\":\"Taylor\"}");
    }
    
    @Test(description = "Get a float in a valid jsonpath")
    public void testGetFloat() {
        BValue<?>[] arguments = { new JSONValue(json2), new StringValue("$.item.price") };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "getFloat", arguments);
        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        Assert.assertEquals(FunctionUtils.getValue(bContext).getFloat(), (float) 3.54);
    }
    
    @Test(description = "Get a double in a valid jsonpath")
    public void testGetDouble() {
        BValue<?>[] arguments = { new JSONValue(json2), new StringValue("$.item.price") };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "getDouble", arguments);
        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        Assert.assertEquals(FunctionUtils.getValue(bContext).getDouble(), 3.54);
    }
    
    @Test(description = "Get a float in a valid jsonpath")
    public void testGetBoolean() {
        BValue<?>[] arguments = { new JSONValue(json2), new StringValue("$.item.available") };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "getBoolean", arguments);
        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        Assert.assertEquals(FunctionUtils.getValue(bContext).getBoolean(), true);
    }
    
    // TODO: Add get() tests for jsonpath-functions such as length(), min(), max(), etc..

    
    /*
     * Test Set-Functions 
     */
    
    @Test(description = "Set a string to a valid jsonpath")
    public void testSetString() {
        final String val = "Paul";
        BValue<?>[] arguments = { new JSONValue(json1), new StringValue("$.name.fname"), new StringValue(val) };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "setString", arguments);
        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        Assert.assertEquals(FunctionUtils.getValue(bContext).getString(), val);
    }

    @Test(description = "Set an int to a valid jsonpath")
    public void testSetInteger() {
        final int val = 25;
        BValue<?>[] arguments = { new JSONValue(json1), new StringValue("$.age"), new IntValue(val) };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "setInt", arguments);
        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        Assert.assertEquals(FunctionUtils.getValue(bContext).getInt(), val);
    }
    
    @Test(description = "Set a double to a valid jsonpath")
    public void testSetDouble() {
        final double val = 4.78;
        BValue<?>[] arguments = { new JSONValue(json2), new StringValue("$.item.price"), new DoubleValue(val) };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "setDouble", arguments);
        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        Assert.assertEquals(FunctionUtils.getValue(bContext).getDouble(), val);
    }
    
    @Test(description = "Set a float to a valid jsonpath")
    public void testSetFloat() {
        final float val = (float) 4.78;
        BValue<?>[] arguments = { new JSONValue(json2), new StringValue("$.item.price"), new FloatValue(val) };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "setFloat", arguments);
        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        Assert.assertEquals(FunctionUtils.getValue(bContext).getFloat(), val);
    }
    
    @Test(description = "Set a boolean to a valid jsonpath")
    public void testSetBoolean() {
        final boolean val = false;
        BValue<?>[] arguments = { new JSONValue(json2), new StringValue("$.item.available"), new BooleanValue(val) };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "setBoolean", arguments);
        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        Assert.assertEquals(FunctionUtils.getValue(bContext).getBoolean(), val);
    }
    
    @Test(description = "Set a json element to a valid jsonpath")
    public void testSetJSON() {
        final String val = "{\"id\":\"item123\"}";
        BValue<?>[] arguments = { new JSONValue(json2), new StringValue("$.item.available"), new JSONValue(val) };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "setJson", arguments);
        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        
        Assert.assertEquals(getJsonAsString(bContext), val);
    }
    
    
    
    /*
     * Test Add-to-object Functions
     */
    
    @Test(description = "Add a string to a valid json object")
    public void testAddStringToObject() {
        BValue<?>[] arguments = { new JSONValue(json1), new StringValue("$.name"), new StringValue("nickName"), 
                new StringValue("Paul") };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "addStringToObject", arguments);
        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        final String expected = "{\"name\":{\"fname\":\"Jack\",\"lname\":\"Taylor\",\"nickName\":\"Paul\"}," +
                "\"state\":\"CA\",\"age\":20}";
        Assert.assertEquals(getJsonAsString(bContext), expected);
    }

    @Test(description = "Add an integer to a valid json object")
    public void testAddIntToObject() {
        BValue<?>[] arguments = { new JSONValue(json1), new StringValue("$"), new StringValue("zipCode"), 
                new IntValue(90001) };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "addIntToObject", arguments);
        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        final String expected = "{\"name\":{\"fname\":\"Jack\",\"lname\":\"Taylor\"},\"state\":\"CA\",\"age\":20," +
                "\"zipCode\":90001}";
        Assert.assertEquals(getJsonAsString(bContext), expected);
    }
    
    @Test(description = "Add a double to a valid json object")
    public void testAddDoubleToObject() {
        BValue<?>[] arguments = { new JSONValue(json2), new StringValue("$.item"), new StringValue("discount"), 
                new DoubleValue(0.15) };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "addDoubleToObject", arguments);
        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        final String expected = "{\"item\":{\"price\":3.54,\"available\":true,\"discount\":0.15}}";
        Assert.assertEquals(getJsonAsString(bContext), expected);
    }
    
    @Test(description = "Add a float to a valid json object")
    public void testAddFloatToObject() {
        BValue<?>[] arguments = { new JSONValue(json2), new StringValue("$.item"), new StringValue("discount"),
                new FloatValue((float) 0.15) };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "addFloatToObject", arguments);
        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        final String expected = "{\"item\":{\"price\":3.54,\"available\":true,\"discount\":0.15}}";
        Assert.assertEquals(getJsonAsString(bContext), expected);
    }
    
    @Test(description = "Add a boolean to a valid json object")
    public void testAddBooleanToObject() {
        BValue<?>[] arguments = { new JSONValue(json2), new StringValue("$.item"), new StringValue("vegi"),
                new BooleanValue(true) };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "addBooleanToObject", arguments);
        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        final String expected = "{\"item\":{\"price\":3.54,\"available\":true,\"vegi\":true}}";
        Assert.assertEquals(getJsonAsString(bContext), expected);
    }
    
    @Test(description = "Add an element to a valid json object")
    public void testAddElementToObject() {
        BValue<?>[] arguments = { new JSONValue(json2), new StringValue("$.item"), new StringValue("expires"),
                new JSONValue("{\"year\":2020,\"month\":12}") };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "addElementToObject", arguments);
        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        final String expected = "{\"item\":{\"price\":3.54,\"available\":true,\"expires\":{\"year\":2020,\"month\"" +
                ":12}}}";
        Assert.assertEquals(getJsonAsString(bContext), expected);
    }
    
    
    
    /*
     * Test Add-to-array Functions
     */
    
    @Test(description = "Add a string to a valid json array")
    public void testAddStringToArray() {
        BValue<?>[] arguments = { new JSONValue(jsonStringArray), new StringValue("$.users"), new StringValue("Jos") };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "addStringToArray", arguments);
        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        final String expected = "{\"users\":[\"Jack\",\"Peter\",\"Jos\"]}";
        Assert.assertEquals(getJsonAsString(bContext), expected);
    }

    @Test(description = "Add an integer to a valid json array")
    public void testAddIntToArray() {
        BValue<?>[] arguments = { new JSONValue(jsonIntArray), new StringValue("$.ages"), new IntValue(23) };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "addIntToArray", arguments);
        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        final String expected = "{\"ages\":[25,28,23]}";
        Assert.assertEquals(getJsonAsString(bContext), expected);
    }
    
    @Test(description = "Add a float to a valid json array")
    public void testAddFloatToArray() {
        BValue<?>[] arguments = { new JSONValue(jsonFloatArray), new StringValue("$.prices"), 
                new FloatValue((float) 5.96)};
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "addFloatToArray", arguments);
        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        final String expected = "{\"prices\":[3.12,4.87,5.96]}";
        Assert.assertEquals(getJsonAsString(bContext), expected);
    }
    
    @Test(description = "Add a double to a valid json array")
    public void testAddDoubleToArray() {
        BValue<?>[] arguments = { new JSONValue(jsonFloatArray), new StringValue("$.prices"), new DoubleValue(5.96) };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "addDoubleToArray", arguments);
        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        final String expected = "{\"prices\":[3.12,4.87,5.96]}";
        Assert.assertEquals(getJsonAsString(bContext), expected);
    }
    
    @Test(description = "Add a boolean to a valid json array")
    public void testAddBooleanToArray() {
        BValue<?>[] arguments = { new JSONValue(jsonBooleanArray), new StringValue("$.availability"), 
                new BooleanValue(true) };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "addBooleanToArray", arguments);
        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        final String expected = "{\"availability\":[true,false,true]}";
        Assert.assertEquals(getJsonAsString(bContext), expected);
    }
    
    @Test(description = "Add an element to a valid json array")
    public void testAddElementToArray() {
        BValue<?>[] arguments = {new JSONValue(jsonElementArray), new StringValue("$.persons"),
                new JSONValue("{'fname':'Jos','lname':'Allen'}") };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "addElementToArray", arguments);
        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        final String expected = "{\"persons\":[{\"fname\":\"Jack\",\"lname\":\"Taylor\"},{\"fname\":\"Peter\"," +
                "\"lname\":\"Roy\"},{\"fname\":\"Jos\",\"lname\":\"Allen\"}]}";
        Assert.assertEquals(getJsonAsString(bContext), expected);
    }
    
    
    /*
     * Test Remove-Function.
     */
    
    @Test(description = "Remove an element in a valid jsonpath")
    public void testRemove() {
        BValue<?>[] arguments = { new JSONValue(json1), new StringValue("$.name") };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "remove", arguments);
        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        final String expected = "{\"state\":\"CA\",\"age\":20}";
        Assert.assertEquals(getJsonAsString(bContext), expected);
    }

    
    /*
     * Test Rename-Function.
     */
    
    @Test(description = "Remove an element in a valid jsonpath")
    public void testRename() {
        BValue<?>[] arguments = { new JSONValue(json1), new StringValue("$.name"), new StringValue("fname"), 
                new StringValue("firstName") };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "rename", arguments);
        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        final String expected = "Jack";
        Assert.assertEquals(FunctionUtils.getValue(bContext).getString(), expected);
    }

    
    /*
     * Test toString-Function.
     */
    @Test(description = "get string representation of json")
    public void testToString() {
        // TODO
    }
    
    // TODO : Add remaining test cases.
    
    
    private String getJsonAsString(Context bContext) {
        return FunctionUtils.getValue(bContext).getJSON().toString().replace("\\r|\\n|\\t| ", "");
    }
}
