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

import org.testng.annotations.BeforeTest;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.linker.BLangLinker;
import org.wso2.ballerina.core.model.BallerinaFile;
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
public class JSONTest {

    private BallerinaFile bFile;
    private static final String json1 = "{'name':{'fname':'Jack','lname':'Taylor'} , 'state' : 'CA' ,  'age' = 20  }";
    private static final String json2 = "{'users': ['Jack','Peter'] }";

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
//
//    @Test
//    public void testGetString() {
//        BValueNew[] arguments = {new JSONValue(json1)};
//        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "getString", arguments);
//
//        Context bContext = FunctionUtils.createInvocationContext(1);
//        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
//        funcIExpr.accept(bLangInterpreter);
//
//        final String expected = "Jack";
//        Assert.assertEquals(FunctionUtils.getValue(bContext).stringValue(), expected);
//    }

//    @Test
//    public void testGetInt() {
//        BValueNew[] arguments = {new JSONValue(json1)};
//        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "getInt", arguments);
//
//        Context bContext = FunctionUtils.createInvocationContext(1);
//        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
//        funcIExpr.accept(bLangInterpreter);
//
//        final int expected = 20;
//        Assert.assertEquals(((BInteger)FunctionUtils.getValue(bContext)).intValue(), expected);
//    }
//
//    @Test
//    public void testGetJSON() {
//        BValueNew[] arguments = {new JSONValue(json1)};
//        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "getJson", arguments);
//
//        Context bContext = FunctionUtils.createInvocationContext(1);
//        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
//        funcIExpr.accept(bLangInterpreter);
//
//        final String expected = "{\"fname\":\"Jack\",\"lname\":\"Taylor\"}";
//        Assert.assertEquals(FunctionUtils.getValue(bContext).getJSON().toString(), expected);
//    }
//
//    @Test
//    public void testSetString() {
//        BValue[] arguments = {new JSONValue(json1), new StringValue("Paul")};
//        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "setString", arguments);
//
//        Context bContext = FunctionUtils.createInvocationContext(1);
//        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
//        funcIExpr.accept(bLangInterpreter);
//
//        final String expected = "Paul";
//        Assert.assertEquals(FunctionUtils.getValue(bContext).getString(), expected);
//    }
//
//    @Test
//    public void testAddStringToObject() {
//        BValue[] arguments = {new JSONValue(json1), new StringValue("nickName"), new StringValue("Paul")};
//        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "addStringToObject", arguments);
//
//        Context bContext = FunctionUtils.createInvocationContext(1);
//        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
//        funcIExpr.accept(bLangInterpreter);
//
//        final String expected = "{\"name\":{\"fname\":\"Jack\",\"lname\":\"Taylor\",\"nickName\":\"Paul\"}," +
//                "\"state\":\"CA\",\"age\":20}";
//        Assert.assertEquals(FunctionUtils.getValue(bContext).getJSON().toString().replace("\\r|\\n|\\t| ", ""),
//                expected);
//    }
//
//    @Test
//    public void testAddStringToArray() {
//        BValue[] arguments = {new JSONValue(json2), new StringValue("Jos")};
//        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "addStringToArray", arguments);
//
//        Context bContext = FunctionUtils.createInvocationContext(1);
//        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
//        funcIExpr.accept(bLangInterpreter);
//
//        final String expected = "{\"users\":[\"Jack\",\"Peter\",\"Jos\"]}";
//        Assert.assertEquals(FunctionUtils.getValue(bContext).getJSON().toString().replace("\\r|\\n|\\t| ", ""),
//                expected);
//    }
//
//    @Test
//    public void testRemove() {
//        BValue[] arguments = {new JSONValue(json1)};
//        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "remove", arguments);
//
//        Context bContext = FunctionUtils.createInvocationContext(1);
//        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
//        funcIExpr.accept(bLangInterpreter);
//
//        final String expected = "{\"state\":\"CA\",\"age\":20}";
//        Assert.assertEquals(FunctionUtils.getValue(bContext).getJSON().toString(), expected);
//    }
//
//    @Test
//    public void testRename() {
//        BValue[] arguments = {new JSONValue(json1), new StringValue("fname"), new StringValue("firstName")};
//        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, "rename", arguments);
//
//        Context bContext = FunctionUtils.createInvocationContext(1);
//        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
//        funcIExpr.accept(bLangInterpreter);
//
//        final String expected = "Jack";
//        Assert.assertEquals(FunctionUtils.getValue(bContext).getString(), expected);
//    }

    // TODO : Add remaining test cases.
}
