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
package org.wso2.ballerina.core.parser;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.interpreter.BLangInterpreter;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.ControlStack;
import org.wso2.ballerina.core.interpreter.StackFrame;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.VariableDcl;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.model.values.IntValue;
import org.wso2.ballerina.core.utils.ParserUtils;

public class BLangInterpreterTest {

    private BallerinaFile bFile;
    private final String funcName = "test";

    @BeforeTest
    public void setup() {
        bFile = ParserUtils.parseBalFile("samples/parser/ifcondition.bal");
    }

    @Test
    public void testFuncInvocation() {
        BallerinaFunction function = (BallerinaFunction) bFile.getFunctions().get(funcName);

        // Create control stack and the stack frame
        Context ctx = new Context();
        ControlStack controlStack = ctx.getControlStack();

        int sizeOfValueArray = function.getStackFrameSize();

        BValueRef[] values = new BValueRef[sizeOfValueArray];
        values[0] = new BValueRef(new IntValue(10));
        values[1] = new BValueRef(new IntValue(1000));
        values[2] = new BValueRef(new IntValue(20));

        // Create default values for all declared local variables
        VariableDcl[] variableDcls = function.getVariableDcls();
        for (int i = 0; i < variableDcls.length; i++) {
            values[i + 3] = BValueRef.getDefaultValue(variableDcls[i].getTypeC());
        }

        BValueRef[] returnVals = new BValueRef[function.getReturnTypesC().length];

        StackFrame stackFrame = new StackFrame(values, returnVals);
        controlStack.pushFrame(stackFrame);

        BLangInterpreter interpreter = new BLangInterpreter(ctx);
        function.accept(interpreter);

        final int expectedA = 310;
        int actualA = returnVals[0].getInt();
        Assert.assertEquals(actualA, expectedA);

        final int expectedB = 21;
        int actualB = returnVals[1].getInt();
        Assert.assertEquals(actualB, expectedB);
    }

    public static void main(String[] args) {
        BLangInterpreterTest test = new BLangInterpreterTest();
        test.setup();
        test.testFuncInvocation();
    }
}
