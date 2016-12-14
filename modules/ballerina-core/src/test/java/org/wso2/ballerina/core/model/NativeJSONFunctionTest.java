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
package org.wso2.ballerina.core.model;

import org.testng.Assert;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.ControlStack;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.model.values.JSONValue;
import org.wso2.ballerina.core.model.values.StringValue;
import org.wso2.ballerina.core.nativeimpl.lang.json.AddStringToArray;
import org.wso2.ballerina.core.nativeimpl.lang.json.AddStringToObject;
import org.wso2.ballerina.core.nativeimpl.lang.json.GetString;
import org.wso2.ballerina.core.nativeimpl.lang.json.Remove;
import org.wso2.ballerina.core.nativeimpl.lang.json.Rename;
import org.wso2.ballerina.core.nativeimpl.lang.json.SetString;

import java.util.ArrayList;
import java.util.List;

/**
 * Test Native function invocation.
 */
public class NativeJSONFunctionTest {

//    @Test
    public void testGetString() {
        Context ctx = new Context();
        ControlStack controlStack = ctx.getControlStack();

        /*
         * json msg = `{"name":"Jack"}`;
         * string jsonPath = "$.name";
         * string result = json:get(msg, jsonPath);
         */
        BValueRef[] localVariables = new BValueRef[2];
        localVariables[0] = new BValueRef(new JSONValue("{'name':'Jack'}"));
        localVariables[1] = new BValueRef(new StringValue("$.name"));
//        StackFrame stackFrame = new StackFrame(new BValueRef[0], null, localVariables);
//        controlStack.pushFrame(stackFrame);

        SymbolName msg = new SymbolName("msg");
        VariableRefExpr varRefExprMsg = new VariableRefExpr(msg);
        varRefExprMsg.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(0));

        SymbolName jsonPath = new SymbolName("jsonPath");
        VariableRefExpr varRefExprJsonPath = new VariableRefExpr(jsonPath);
        varRefExprJsonPath.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(1));

        List<Expression> nestedFunctionInvokeExpr = new ArrayList<>(2);
        nestedFunctionInvokeExpr.add(varRefExprMsg);
        nestedFunctionInvokeExpr.add(varRefExprJsonPath);

        FunctionInvocationExpr invocationExpr =
            new FunctionInvocationExpr(new SymbolName("get"), nestedFunctionInvokeExpr);
        invocationExpr.setFunction(new GetString());
        BValueRef returnValue = invocationExpr.evaluate(ctx);

        String returnVal = (String) returnValue.getBValue().getValue();
        Assert.assertEquals(returnVal, "Jack");
    }
    

//    @Test
    public void testSetString() {
        Context ctx = new Context();
        ControlStack controlStack = ctx.getControlStack();

        /*
         * json msg = `{"name":"Jack"}`;
         * string jsonPath = "$.name";
         * string value = "Peter";
         * json:get(msg, jsonPath, value);
         */
        BValueRef[] localVariables = new BValueRef[3];
        localVariables[0] = new BValueRef(new JSONValue("{'name':'Jack'}"));
        localVariables[1] = new BValueRef(new StringValue("$.name"));
        localVariables[2] = new BValueRef(new StringValue("Peter"));
//        StackFrame stackFrame = new StackFrame(new BValueRef[0], null, localVariables);
//        controlStack.pushFrame(stackFrame);

        // Create expression for:   json msg = `{"name":"Jack"}`;
        SymbolName msg = new SymbolName("msg");
        VariableRefExpr varRefExprMsg = new VariableRefExpr(msg);
        varRefExprMsg.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(0));

        // Create expression for:   string jsonPath = "$.name";
        SymbolName jsonPath = new SymbolName("jsonPath");
        VariableRefExpr varRefExprJsonPath = new VariableRefExpr(jsonPath);
        varRefExprJsonPath.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(1));
        
        // Create expression for:   string value = "Peter";
        SymbolName value = new SymbolName("value");
        VariableRefExpr varRefExprValue = new VariableRefExpr(value);
        varRefExprValue.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(2));

        List<Expression> nestedFunctionInvokeExpr = new ArrayList<>(3);
        nestedFunctionInvokeExpr.add(varRefExprMsg);
        nestedFunctionInvokeExpr.add(varRefExprJsonPath);
        nestedFunctionInvokeExpr.add(varRefExprValue);

        FunctionInvocationExpr invocationExpr =
            new FunctionInvocationExpr(new SymbolName("set"), nestedFunctionInvokeExpr);
        invocationExpr.setFunction(new SetString());
        invocationExpr.evaluate(ctx);

        String modifiedJson = (String) localVariables[0].getBValue().toString();
        Assert.assertEquals(modifiedJson, "{\"name\":\"Peter\"}");
    }
    
//    @Test
    public void testAddStringToObject() {
        Context ctx = new Context();
        ControlStack controlStack = ctx.getControlStack();

        /*
         * json msg = `{"name":"Jack"}`;
         * string jsonPath = "$.name";
         * string key = "address";
         * string value = "WSO2";
         * json:get(msg, jsonPath, key, value);
         */
        BValueRef[] localVariables = new BValueRef[4];
        localVariables[0] = new BValueRef(new JSONValue("{'name':'Jack'}"));
        localVariables[1] = new BValueRef(new StringValue("$"));
        localVariables[2] = new BValueRef(new StringValue("address"));
        localVariables[3] = new BValueRef(new StringValue("WSO2"));
//        StackFrame stackFrame = new StackFrame(new BValueRef[0], null, localVariables);
//        controlStack.pushFrame(stackFrame);

        // Create expression for:   json msg = `{"name":"Jack"}`;
        SymbolName msg = new SymbolName("msg");
        VariableRefExpr varRefExprMsg = new VariableRefExpr(msg);
        varRefExprMsg.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(0));

        // Create expression for:   string jsonPath = "$.name";
        SymbolName jsonPath = new SymbolName("jsonPath");
        VariableRefExpr varRefExprJsonPath = new VariableRefExpr(jsonPath);
        varRefExprJsonPath.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(1));
        
        // Create expression for:   string key = "address";
        SymbolName key = new SymbolName("key");
        VariableRefExpr varRefExprKey = new VariableRefExpr(key);
        varRefExprKey.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(2));
        
        // Create expression for:   string value = "WSO2";
        SymbolName value = new SymbolName("value");
        VariableRefExpr varRefExprValue = new VariableRefExpr(value);
        varRefExprValue.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(3));

        List<Expression> nestedFunctionInvokeExpr = new ArrayList<>(4);
        nestedFunctionInvokeExpr.add(varRefExprMsg);
        nestedFunctionInvokeExpr.add(varRefExprJsonPath);
        nestedFunctionInvokeExpr.add(varRefExprKey);
        nestedFunctionInvokeExpr.add(varRefExprValue);

        FunctionInvocationExpr invocationExpr =
            new FunctionInvocationExpr(new SymbolName("add"), nestedFunctionInvokeExpr);
        invocationExpr.setFunction(new AddStringToObject());
        invocationExpr.evaluate(ctx);

        String modifiedJson = (String) localVariables[0].getBValue().toString();
        Assert.assertEquals(modifiedJson, "{\"name\":\"Jack\",\"address\":\"WSO2\"}");
    }
    
//    @Test
    public void testAddStringToArray() {
        Context ctx = new Context();
        ControlStack controlStack = ctx.getControlStack();

        /*
         * json msg = `{"names":["Jack","Peter"]]}`;
         * string jsonPath = "$.name";
         * string value = "Jos";
         * json:get(msg, jsonPath, value);
         */
        BValueRef[] localVariables = new BValueRef[3];
        localVariables[0] = new BValueRef(new JSONValue("{'names':['Jack','Peter']}"));
        localVariables[1] = new BValueRef(new StringValue("$.names"));
        localVariables[2] = new BValueRef(new StringValue("Jos"));
//        StackFrame stackFrame = new StackFrame(new BValueRef[0], null, localVariables);
//        controlStack.pushFrame(stackFrame);

        // Create expression for:   json msg = `{"names":["Jack","Peter"]]}`;
        SymbolName msg = new SymbolName("msg");
        VariableRefExpr varRefExprMsg = new VariableRefExpr(msg);
        varRefExprMsg.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(0));

        // Create expression for:   string jsonPath = "$.name";
        SymbolName jsonPath = new SymbolName("jsonPath");
        VariableRefExpr varRefExprJsonPath = new VariableRefExpr(jsonPath);
        varRefExprJsonPath.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(1));
        
        // Create expression for:   value = "Jos";
        SymbolName value = new SymbolName("value");
        VariableRefExpr varRefExprValue = new VariableRefExpr(value);
        varRefExprValue.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(2));

        List<Expression> nestedFunctionInvokeExpr = new ArrayList<>(3);
        nestedFunctionInvokeExpr.add(varRefExprMsg);
        nestedFunctionInvokeExpr.add(varRefExprJsonPath);
        nestedFunctionInvokeExpr.add(varRefExprValue);

        FunctionInvocationExpr invocationExpr =
            new FunctionInvocationExpr(new SymbolName("add"), nestedFunctionInvokeExpr);
        invocationExpr.setFunction(new AddStringToArray());
        invocationExpr.evaluate(ctx);

        String modifiedJson = (String) localVariables[0].getBValue().toString();
        Assert.assertEquals(modifiedJson, "{\"names\":[\"Jack\",\"Peter\",\"Jos\"]}");
    }
    
//    @Test
    public void testRemove() {
        Context ctx = new Context();
        ControlStack controlStack = ctx.getControlStack();

        /*
         * json msg = `{"name":"Jack","address":"WSO2"}`;
         * string jsonPath = "$.address";
         * json:get(msg, jsonPath);
         */
        BValueRef[] localVariables = new BValueRef[2];
        localVariables[0] = new BValueRef(new JSONValue("{\"name\":\"Jack\",\"address\":\"WSO2\"}"));
        localVariables[1] = new BValueRef(new StringValue("$.address"));
//        StackFrame stackFrame = new StackFrame(new BValueRef[0], null, localVariables);
//        controlStack.pushFrame(stackFrame);

        // Create expression for:   json msg = `{"name":"Jack",'address':'WSO2'}`;
        SymbolName msg = new SymbolName("msg");
        VariableRefExpr varRefExprMsg = new VariableRefExpr(msg);
        varRefExprMsg.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(0));

        // Create expression for:   string jsonPath = "$.address";
        SymbolName jsonPath = new SymbolName("jsonPath");
        VariableRefExpr varRefExprJsonPath = new VariableRefExpr(jsonPath);
        varRefExprJsonPath.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(1));
        

        List<Expression> nestedFunctionInvokeExpr = new ArrayList<>(2);
        nestedFunctionInvokeExpr.add(varRefExprMsg);
        nestedFunctionInvokeExpr.add(varRefExprJsonPath);

        FunctionInvocationExpr invocationExpr =
            new FunctionInvocationExpr(new SymbolName("remove"), nestedFunctionInvokeExpr);
        invocationExpr.setFunction(new Remove());
        invocationExpr.evaluate(ctx);

        String modifiedJson = (String) localVariables[0].getBValue().toString();
        Assert.assertEquals(modifiedJson, "{\"name\":\"Jack\"}");
    }

//    @Test
    public void testRename() {
        Context ctx = new Context();
        ControlStack controlStack = ctx.getControlStack();

        /*
         * json msg = `{"name":"Jack","address":"WSO2"}`;
         * string jsonPath = "$.address";
         * string oldKey = "name";
         * string newKey = "firstName";
         * json:get(msg, jsonPath, oldKey, newKey);
         */
        BValueRef[] localVariables = new BValueRef[4];
        localVariables[0] = new BValueRef(new JSONValue("{\"name\":\"Jack\"}"));
        localVariables[1] = new BValueRef(new StringValue("$"));
        localVariables[2] = new BValueRef(new StringValue("name"));
        localVariables[3] = new BValueRef(new StringValue("firstName"));
//        StackFrame stackFrame = new StackFrame(new BValueRef[0], null, localVariables);
//        controlStack.pushFrame(stackFrame);

        // Create expression for:   json msg = `{"name":"Jack","address":"WSO2"}`;
        SymbolName msg = new SymbolName("msg");
        VariableRefExpr varRefExprMsg = new VariableRefExpr(msg);
        varRefExprMsg.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(0));

        // Create expression for:   string jsonPath = "$.address";
        SymbolName jsonPath = new SymbolName("jsonPath");
        VariableRefExpr varRefExprJsonPath = new VariableRefExpr(jsonPath);
        varRefExprJsonPath.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(1));
        
        // Create expression for:   string jsonPath = "$.address";
        SymbolName oldKey = new SymbolName("oldKey");
        VariableRefExpr varRefExprOldKey = new VariableRefExpr(oldKey);
        varRefExprOldKey.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(2));
        
        // Create expression for:   string jsonPath = "$.address";
        SymbolName newKey = new SymbolName("newKey");
        VariableRefExpr varRefExprNewKey = new VariableRefExpr(newKey);
        varRefExprNewKey.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(3));
        

        List<Expression> nestedFunctionInvokeExpr = new ArrayList<>(4);
        nestedFunctionInvokeExpr.add(varRefExprMsg);
        nestedFunctionInvokeExpr.add(varRefExprJsonPath);
        nestedFunctionInvokeExpr.add(varRefExprOldKey);
        nestedFunctionInvokeExpr.add(varRefExprNewKey);

        FunctionInvocationExpr invocationExpr =
            new FunctionInvocationExpr(new SymbolName("rename"), nestedFunctionInvokeExpr);
        invocationExpr.setFunction(new Rename());
        invocationExpr.evaluate(ctx);

        String modifiedJson = (String) localVariables[0].getBValue().toString();
        Assert.assertEquals(modifiedJson, "{\"firstName\":\"Jack\"}");
    }
}
