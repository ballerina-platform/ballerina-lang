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

import org.apache.axiom.om.OMElement;
import org.testng.Assert;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.ControlStack;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.model.values.MapValue;
import org.wso2.ballerina.core.model.values.StringValue;
import org.wso2.ballerina.core.model.values.XMLValue;
import org.wso2.ballerina.core.nativeimpl.lang.xml.GetString;
import org.wso2.ballerina.core.nativeimpl.lang.xml.GetXML;

import java.util.ArrayList;
import java.util.List;

/**
 * Test XML Native functions.
 */
public class NativeXMLFunctionTest {

    //@Test
    public void testGetString() {
        Context ctx = new Context();
        ControlStack controlStack = ctx.getControlStack();

        /*
         * xml msg = `<persons><person><name>Jack</name><address>wso2</address></person></persons`;
         * string xPath = "/persons/person/name";
         * map namespaces;
         * string result = json:get(msg, jsonPath);
         */
        BValueRef[] localVariables = new BValueRef[3];
        localVariables[0] = new BValueRef(new XMLValue("<persons><person><name>Jack</name><address>wso2</address>" +
            "</person></persons>"));
        localVariables[1] = new BValueRef(new StringValue("/persons/person/name/text()"));
        localVariables[2] = new BValueRef(new MapValue<>());
        //StackFrame stackFrame = new StackFrame(new BValueRef[0], null, localVariables);
        //controlStack.pushFrame(stackFrame);

        SymbolName msg = new SymbolName("msg");
        VariableRefExpr varRefExprMsg = new VariableRefExpr(msg);
        varRefExprMsg.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(0));

        SymbolName jsonPath = new SymbolName("xPath");
        VariableRefExpr varRefExprJsonPath = new VariableRefExpr(jsonPath);
        varRefExprJsonPath.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(1));
        
        SymbolName namepsaces = new SymbolName("namespaces");
        VariableRefExpr varRefExprNameSpaces = new VariableRefExpr(namepsaces);
        varRefExprNameSpaces.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(2));

        List<Expression> nestedFunctionInvokeExpr = new ArrayList<>(3);
        nestedFunctionInvokeExpr.add(varRefExprMsg);
        nestedFunctionInvokeExpr.add(varRefExprJsonPath);
        nestedFunctionInvokeExpr.add(varRefExprNameSpaces);

        FunctionInvocationExpr invocationExpr =
            new FunctionInvocationExpr(new SymbolName("get"), nestedFunctionInvokeExpr);
        invocationExpr.setFunction(new GetString());
        BValueRef returnValue = invocationExpr.evaluate(ctx);

        Assert.assertTrue(returnValue.getBValue() instanceof StringValue);
        
        String returnVal = (String) returnValue.getString();
        Assert.assertEquals(returnVal, "Jack");
    }
    
    //@Test
    public void testGetXML() {
        Context ctx = new Context();
        ControlStack controlStack = ctx.getControlStack();

        /*
         * xml msg = `<persons><person><name>Jack</name><address>wso2</address></person></persons`;
         * string xPath = "/persons/person/name";
         * map namespaces;
         * string result = json:get(msg, jsonPath);
         */
        BValueRef[] localVariables = new BValueRef[3];
        localVariables[0] = new BValueRef(new XMLValue("<persons><person><name>Jack</name><address>wso2</address>" +
            "</person></persons>"));
        localVariables[1] = new BValueRef(new StringValue("/persons/person"));
        localVariables[2] = new BValueRef(new MapValue<>());
        //StackFrame stackFrame = new StackFrame(new BValueRef[0], null, localVariables);
        //controlStack.pushFrame(stackFrame);

        SymbolName msg = new SymbolName("msg");
        VariableRefExpr varRefExprMsg = new VariableRefExpr(msg);
        varRefExprMsg.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(0));

        SymbolName jsonPath = new SymbolName("xPath");
        VariableRefExpr varRefExprJsonPath = new VariableRefExpr(jsonPath);
        varRefExprJsonPath.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(1));
        
        SymbolName namepsaces = new SymbolName("namespaces");
        VariableRefExpr varRefExprNameSpaces = new VariableRefExpr(namepsaces);
        varRefExprNameSpaces.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(2));

        List<Expression> nestedFunctionInvokeExpr = new ArrayList<>(3);
        nestedFunctionInvokeExpr.add(varRefExprMsg);
        nestedFunctionInvokeExpr.add(varRefExprJsonPath);
        nestedFunctionInvokeExpr.add(varRefExprNameSpaces);

        FunctionInvocationExpr invocationExpr =
            new FunctionInvocationExpr(new SymbolName("get"), nestedFunctionInvokeExpr);
        invocationExpr.setFunction(new GetXML());
        BValueRef returnValue = invocationExpr.evaluate(ctx);

        Assert.assertTrue(returnValue.getBValue() instanceof XMLValue);
        
        OMElement returnElement = returnValue.getXML();
        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), "<person><name>Jack</name>" +
            "<address>wso2</address></person>");
    }
}
