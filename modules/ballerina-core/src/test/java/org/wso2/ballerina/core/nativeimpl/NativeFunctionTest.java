/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.ballerina.core.nativeimpl;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.interpreter.BLangInterpreter;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.StackFrame;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.expressions.BasicLiteral;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.model.values.StringValue;
import org.wso2.ballerina.core.utils.TestUtils;

public class NativeFunctionTest {

    private BallerinaFile bFile;
    private String funcName = "invokeNativeFunction";

    @BeforeTest
    public void setup() {
        bFile = TestUtils.parseBalFile("bal_files/nativeimpl/println.bal");
    }

    @Test
    public void testNativeFuncInvocation() {
        BValueRef valueRefA = new BValueRef(new StringValue("Hello World"));
        BasicLiteral basicLiteralA = new BasicLiteral(valueRefA);

        Expression[] exprs = new Expression[1];
        exprs[0] = basicLiteralA;

        FunctionInvocationExpr funcIExpr = new FunctionInvocationExpr(new SymbolName(funcName), exprs);
        funcIExpr.setOffset(0);
        funcIExpr.setFunction(bFile.getFunctions().get(funcName));

        BValueRef[] results = new BValueRef[1];
        StackFrame stackFrame = new StackFrame(results, null);

        Context bContext = new Context();
        bContext.getControlStack().pushFrame(stackFrame);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);
    }

    public static void main(String[] args) {
        NativeFunctionTest test = new NativeFunctionTest();
        test.setup();
        test.testNativeFuncInvocation();
    }

}
