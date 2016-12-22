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
import org.wso2.ballerina.core.model.values.BBoolean;
import org.wso2.ballerina.core.model.values.BDouble;
import org.wso2.ballerina.core.model.values.BFloat;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BJSON;
import org.wso2.ballerina.core.model.values.BRefType;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BValueType;
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
        BValue[] args = { new BJSON(json1), new BString("$.name.fname") };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "getString", args.length);
        Context bContext = FunctionUtils.createInvocationContext(args, 1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        Assert.assertEquals(getBValueType(bContext).stringValue(), "Jack");
    }

    @Test(description = "Get an integer in a valid jsonpath")
    public void testGetInt() {
        BValue[] args = { new BJSON(json1), new BString("$.age") };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "getInt", args.length);
        Context bContext = FunctionUtils.createInvocationContext(args, 1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        Assert.assertEquals(getBValueType(bContext).intValue(), 20);
    }

    @Test(description = "Get a json element in a valid jsonpath")
    public void testGetJSON() {
        BValue[] args = { new BJSON(json1), new BString("$.name") };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "getJson", args.length);
        Context bContext = FunctionUtils.createInvocationContext(args, 1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        Assert.assertEquals(getJsonAsString(bContext), "{\"fname\":\"Jack\",\"lname\":\"Taylor\"}");
    }
    
    @Test(description = "Get a float in a valid jsonpath")
    public void testGetFloat() {
        BValue[] args = { new BJSON(json2), new BString("$.item.price") };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "getFloat", args.length);
        Context bContext = FunctionUtils.createInvocationContext(args, 1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        Assert.assertEquals(getBValueType(bContext).floatValue(), (float) 3.54);
    }
    
    @Test(description = "Get a double in a valid jsonpath")
    public void testGetDouble() {
        BValue[] args = { new BJSON(json2), new BString("$.item.price") };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "getDouble", args.length);
        Context bContext = FunctionUtils.createInvocationContext(args, 1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        Assert.assertEquals(getBValueType(bContext).doubleValue(), 3.54);
    }
    
    @Test(description = "Get a float in a valid jsonpath")
    public void testGetBoolean() {
        BValue[] args = { new BJSON(json2), new BString("$.item.available") };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "getBoolean", args.length);
        Context bContext = FunctionUtils.createInvocationContext(args, 1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        Assert.assertEquals(getBValueType(bContext).booleanValue(), true);
    }
    
    // TODO: Add get() tests for jsonpath-functions such as length(), min(), max(), etc..

    
    /*
     * Test Set-Functions 
     */
    
    @Test(description = "Set a string to a valid jsonpath")
    public void testSetString() {
        final String val = "Paul";
        BValue[] args = { new BJSON(json1), new BString("$.name.fname"), new BString(val) };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "setString", args.length);
        Context bContext = FunctionUtils.createInvocationContext(args, 1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        Assert.assertEquals(getBValueType(bContext).stringValue(), val);
    }

    @Test(description = "Set an int to a valid jsonpath")
    public void testSetInteger() {
        final int val = 25;
        BValue[] args = { new BJSON(json1), new BString("$.age"), new BInteger(val) };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "setInt", args.length);
        Context bContext = FunctionUtils.createInvocationContext(args, 1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        Assert.assertEquals(getBValueType(bContext).intValue(), val);
    }
    
    @Test(description = "Set a double to a valid jsonpath")
    public void testSetDouble() {
        final double val = 4.78;
        BValue[] args = { new BJSON(json2), new BString("$.item.price"), new BDouble(val) };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "setDouble", args.length);
        Context bContext = FunctionUtils.createInvocationContext(args, 1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        Assert.assertEquals(getBValueType(bContext).doubleValue(), val);
    }
    
    @Test(description = "Set a float to a valid jsonpath")
    public void testSetFloat() {
        final float val = (float) 4.78;
        BValue[] args = { new BJSON(json2), new BString("$.item.price"), new BFloat(val) };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "setFloat", args.length);
        Context bContext = FunctionUtils.createInvocationContext(args, 1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        Assert.assertEquals(getBValueType(bContext).floatValue(), val);
    }
    
    @Test(description = "Set a boolean to a valid jsonpath")
    public void testSetBoolean() {
        final boolean val = false;
        BValue[] args = { new BJSON(json2), new BString("$.item.available"), new BBoolean(val) };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "setBoolean", args.length);
        Context bContext = FunctionUtils.createInvocationContext(args, 1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        Assert.assertEquals(getBValueType(bContext).booleanValue(), val);
    }
    
    @Test(description = "Set a json element to a valid jsonpath")
    public void testSetJSON() {
        final String val = "{\"id\":\"item123\"}";
        BValue[] args = { new BJSON(json2), new BString("$.item.available"), new BJSON(val) };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "setJson", args.length);
        Context bContext = FunctionUtils.createInvocationContext(args, 1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        
        Assert.assertEquals(getJsonAsString(bContext), val);
    }
    
    
    
    /*
     * Test Add-to-object Functions
     */
    
    @Test(description = "Add a string to a valid json object")
    public void testAddStringToObject() {
        BValue[] args = { new BJSON(json1), new BString("$.name"), new BString("nickName"), 
                new BString("Paul") };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "addStringToObject", args.length);
        Context bContext = FunctionUtils.createInvocationContext(args, 1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        final String expected = "{\"name\":{\"fname\":\"Jack\",\"lname\":\"Taylor\",\"nickName\":\"Paul\"}," +
                "\"state\":\"CA\",\"age\":20}";
        Assert.assertEquals(getJsonAsString(bContext), expected);
    }

    @Test(description = "Add an integer to a valid json object")
    public void testAddIntToObject() {
        BValue[] args = { new BJSON(json1), new BString("$"), new BString("zipCode"), 
                new BInteger(90001) };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "addIntToObject", args.length);
        Context bContext = FunctionUtils.createInvocationContext(args, 1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        final String expected = "{\"name\":{\"fname\":\"Jack\",\"lname\":\"Taylor\"},\"state\":\"CA\",\"age\":20," +
                "\"zipCode\":90001}";
        Assert.assertEquals(getJsonAsString(bContext), expected);
    }
    
    @Test(description = "Add a double to a valid json object")
    public void testAddDoubleToObject() {
        BValue[] args = { new BJSON(json2), new BString("$.item"), new BString("discount"), 
                new BDouble(0.15) };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "addDoubleToObject", args.length);
        Context bContext = FunctionUtils.createInvocationContext(args, 1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        final String expected = "{\"item\":{\"price\":3.54,\"available\":true,\"discount\":0.15}}";
        Assert.assertEquals(getJsonAsString(bContext), expected);
    }
    
    @Test(description = "Add a float to a valid json object")
    public void testAddFloatToObject() {
        BValue[] args = { new BJSON(json2), new BString("$.item"), new BString("discount"),
                new BFloat((float) 0.15) };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "addFloatToObject", args.length);
        Context bContext = FunctionUtils.createInvocationContext(args, 1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        final String expected = "{\"item\":{\"price\":3.54,\"available\":true,\"discount\":0.15}}";
        Assert.assertEquals(getJsonAsString(bContext), expected);
    }
    
    @Test(description = "Add a boolean to a valid json object")
    public void testAddBooleanToObject() {
        BValue[] args = { new BJSON(json2), new BString("$.item"), new BString("vegi"),
                new BBoolean(true) };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "addBooleanToObject", 
                args.length);
        Context bContext = FunctionUtils.createInvocationContext(args, 1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        final String expected = "{\"item\":{\"price\":3.54,\"available\":true,\"vegi\":true}}";
        Assert.assertEquals(getJsonAsString(bContext), expected);
    }
    
    @Test(description = "Add an element to a valid json object")
    public void testAddElementToObject() {
        BValue[] args = { new BJSON(json2), new BString("$.item"), new BString("expires"),
                new BJSON("{\"year\":2020,\"month\":12}") };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "addElementToObject", 
                args.length);
        Context bContext = FunctionUtils.createInvocationContext(args, 1);
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
        BValue[] args = { new BJSON(jsonStringArray), new BString("$.users"), new BString("Jos") };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "addStringToArray", args.length);
        Context bContext = FunctionUtils.createInvocationContext(args, 1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        final String expected = "{\"users\":[\"Jack\",\"Peter\",\"Jos\"]}";
        Assert.assertEquals(getJsonAsString(bContext), expected);
    }

    @Test(description = "Add an integer to a valid json array")
    public void testAddIntToArray() {
        BValue[] args = { new BJSON(jsonIntArray), new BString("$.ages"), new BInteger(23) };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "addIntToArray", args.length);
        Context bContext = FunctionUtils.createInvocationContext(args, 1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        final String expected = "{\"ages\":[25,28,23]}";
        Assert.assertEquals(getJsonAsString(bContext), expected);
    }
    
    @Test(description = "Add a float to a valid json array")
    public void testAddFloatToArray() {
        BValue[] args = { new BJSON(jsonFloatArray), new BString("$.prices"), 
                new BFloat((float) 5.96)};
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "addFloatToArray", args.length);
        Context bContext = FunctionUtils.createInvocationContext(args, 1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        final String expected = "{\"prices\":[3.12,4.87,5.96]}";
        Assert.assertEquals(getJsonAsString(bContext), expected);
    }
    
    @Test(description = "Add a double to a valid json array")
    public void testAddDoubleToArray() {
        BValue[] args = { new BJSON(jsonFloatArray), new BString("$.prices"), new BDouble(5.96) };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "addDoubleToArray", args.length);
        Context bContext = FunctionUtils.createInvocationContext(args, 1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        final String expected = "{\"prices\":[3.12,4.87,5.96]}";
        Assert.assertEquals(getJsonAsString(bContext), expected);
    }
    
    @Test(description = "Add a boolean to a valid json array")
    public void testAddBooleanToArray() {
        BValue[] args = { new BJSON(jsonBooleanArray), new BString("$.availability"), 
                new BBoolean(true) };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "addBooleanToArray", args.length);
        Context bContext = FunctionUtils.createInvocationContext(args, 1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        final String expected = "{\"availability\":[true,false,true]}";
        Assert.assertEquals(getJsonAsString(bContext), expected);
    }
    
    @Test(description = "Add an element to a valid json array")
    public void testAddElementToArray() {
        BValue[] args = {new BJSON(jsonElementArray), new BString("$.persons"),
                new BJSON("{'fname':'Jos','lname':'Allen'}") };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "addElementToArray", args.length);
        Context bContext = FunctionUtils.createInvocationContext(args, 1);
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
        BValue[] args = { new BJSON(json1), new BString("$.name") };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "remove", args.length);
        Context bContext = FunctionUtils.createInvocationContext(args, 1);
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
        BValue[] args = { new BJSON(json1), new BString("$.name"), new BString("fname"), 
                new BString("firstName") };
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "rename", args.length);
        Context bContext = FunctionUtils.createInvocationContext(args, 1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
        final String expected = "Jack";
        Assert.assertEquals(FunctionUtils.getReturnValue(bContext).stringValue(), expected);
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
        return getBRefType(bContext).toString().replace("\\r|\\n|\\t| ", "");
    }
    
    private BValueType getBValueType(Context bContext) {
        return (BValueType) FunctionUtils.getReturnValue(bContext);
    }
    
    private BRefType<?> getBRefType(Context bContext) {
        return (BRefType<?>) FunctionUtils.getReturnValue(bContext);
    }
}
