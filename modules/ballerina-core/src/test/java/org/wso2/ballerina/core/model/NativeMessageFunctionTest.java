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
import org.testng.annotations.Test;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.ControlStack;
import org.wso2.ballerina.core.interpreter.StackFrame;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.model.values.JSONValue;
import org.wso2.ballerina.core.model.values.MessageValue;
import org.wso2.ballerina.core.model.values.StringValue;
import org.wso2.ballerina.core.nativeimpl.lang.message.GetHeader;
import org.wso2.ballerina.core.nativeimpl.lang.message.GetJsonPayload;
import org.wso2.ballerina.core.nativeimpl.lang.message.SetJsonPayload;
import org.wso2.carbon.messaging.DefaultCarbonMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Test Native function invocation.
 */
public class NativeMessageFunctionTest {

    @Test
    public void testGetJSONPayload() {
        Context ctx = new Context();
        ControlStack controlStack = ctx.getControlStack();
        
        DefaultCarbonMessage carbonMsg = new DefaultCarbonMessage();
        String payload = "{\"name\":\"Jack\",\"address\":\"WSO2\"}";
        carbonMsg.setStringMessageBody(payload);
        /*
         * message msg;
         * json msg = message:getJsonPayload(msg);
         */
        BValueRef[] localVariables = new BValueRef[1];
        localVariables[0] = new BValueRef(new MessageValue(carbonMsg));
        StackFrame stackFrame = new StackFrame(new BValueRef[0], null, localVariables);
        controlStack.pushFrame(stackFrame);

        SymbolName msg = new SymbolName("msg");
        VariableRefExpr varRefExprMsg = new VariableRefExpr(msg);
        varRefExprMsg.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(0));

        List<Expression> nestedFunctionInvokeExpr = new ArrayList<>(1);
        nestedFunctionInvokeExpr.add(varRefExprMsg);

        FunctionInvocationExpr invocationExpr =
            new FunctionInvocationExpr(new SymbolName("getJsonPayload"), nestedFunctionInvokeExpr);
        invocationExpr.setFunction(new GetJsonPayload());
        BValueRef returnValue = invocationExpr.evaluate(ctx);

        BValue<?> result = returnValue.getBValue();
        Assert.assertTrue(result instanceof JSONValue);
        
        String returnVal = result.toString();
        Assert.assertEquals(returnVal, "{\"name\":\"Jack\",\"address\":\"WSO2\"}");
    }
    
    @Test
    public void testSetJSONPayload() {
        Context ctx = new Context();
        ControlStack controlStack = ctx.getControlStack();
        
        String payloadStr = "{\"name\":\"Jack\",\"address\":\"WSO2\"}";
        
        // carbon message with empty payload
        DefaultCarbonMessage carbonMsg = new DefaultCarbonMessage();
        
        /*
         * message msg;
         * json payload = `{"name":"Jack","address":"WSO2"}`;
         * message:setJsonPayload(msg, payload);
         */
        BValueRef[] localVariables = new BValueRef[2];
        localVariables[0] = new BValueRef(new MessageValue(carbonMsg));
        localVariables[1] = new BValueRef(new JSONValue(payloadStr));
        StackFrame stackFrame = new StackFrame(new BValueRef[0], null, localVariables);
        controlStack.pushFrame(stackFrame);

        SymbolName msg = new SymbolName("msg");
        VariableRefExpr varRefExprMsg = new VariableRefExpr(msg);
        varRefExprMsg.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(0));
        
        SymbolName payload = new SymbolName("payload");
        VariableRefExpr varRefExprPayload = new VariableRefExpr(payload);
        varRefExprPayload.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(1));

        List<Expression> nestedFunctionInvokeExpr = new ArrayList<>(2);
        nestedFunctionInvokeExpr.add(varRefExprMsg);
        nestedFunctionInvokeExpr.add(varRefExprPayload);

        FunctionInvocationExpr invocationExpr =
            new FunctionInvocationExpr(new SymbolName("setJsonPayload"), nestedFunctionInvokeExpr);
        invocationExpr.setFunction(new SetJsonPayload());
        invocationExpr.evaluate(ctx);

        BValue<?> newPayload = ((MessageValue) localVariables[0].getBValue()).getBuiltPayload();
        Assert.assertTrue(newPayload instanceof JSONValue);
        
        String returnVal = newPayload.toString();
        Assert.assertEquals(returnVal, "{\"name\":\"Jack\",\"address\":\"WSO2\"}");
    }

    @Test
    public void testGetHeader() {
        Context ctx = new Context();
        ControlStack controlStack = ctx.getControlStack();

        // carbon message with empty payload
        DefaultCarbonMessage carbonMsg = new DefaultCarbonMessage();

        BValueRef[] localVariables = new BValueRef[2];
        MessageValue messageVal = new MessageValue(carbonMsg);
        messageVal.addHeader("Foo", "Bar");
        localVariables[0] = new BValueRef(messageVal);
        localVariables[1] = new BValueRef(new StringValue("Foo"));

        StackFrame stackFrame = new StackFrame(new BValueRef[0], null, localVariables);
        controlStack.pushFrame(stackFrame);

        SymbolName msg = new SymbolName("msg");
        VariableRefExpr varRefExprMsg = new VariableRefExpr(msg);
        varRefExprMsg.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(0));

        SymbolName headerName = new SymbolName("headerName");
        VariableRefExpr varRefHeaderName = new VariableRefExpr(headerName);
        varRefHeaderName.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(1));

        List<Expression> nestedFunctionInvokeExpr = new ArrayList<>(2);
        nestedFunctionInvokeExpr.add(varRefExprMsg);
        nestedFunctionInvokeExpr.add(varRefHeaderName);

        FunctionInvocationExpr invocationExpr =
                new FunctionInvocationExpr(new SymbolName("getHeader"), nestedFunctionInvokeExpr);
        invocationExpr.setFunction(new GetHeader());

        BValueRef returnValue = invocationExpr.evaluate(ctx);
        Assert.assertEquals(returnValue.getString(), "Bar");
    }
}
