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

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.testng.Assert;
import org.wso2.ballerina.core.interpreter.BContext;
import org.wso2.ballerina.core.interpreter.BLangInterpreter;
import org.wso2.ballerina.core.interpreter.ControlStack;
import org.wso2.ballerina.core.interpreter.StackFrame;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.builder.BLangModelBuilder;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.model.values.IntValue;
import org.wso2.ballerina.core.parser.antlr4.BLangAntlr4Listener;
import org.wso2.ballerina.core.semantics.SemanticAnalyzer;

import java.io.FileInputStream;
import java.io.IOException;

public class BLangInterpreterTest {

    private BLangAntlr4Listener langModelBuilder;
    private BallerinaFile bFile;
    private String funcName = "test";

    //    @BeforeTest
    public void setup() {
        try {
            ANTLRInputStream antlrInputStream = new ANTLRInputStream(new FileInputStream(
                    getClass().getClassLoader().getResource("samples/parser/function.bal").getFile()));

            BallerinaLexer ballerinaLexer = new BallerinaLexer(antlrInputStream);
            CommonTokenStream ballerinaToken = new CommonTokenStream(ballerinaLexer);

            BallerinaParser ballerinaParser = new BallerinaParser(ballerinaToken);

            BLangModelBuilder modelBuilder = new BLangModelBuilder();
            langModelBuilder = new BLangAntlr4Listener(modelBuilder);

            ballerinaParser.addParseListener(langModelBuilder);
            ballerinaParser.compilationUnit();
//            ballerinaParser.setErrorHandler();

            bFile = modelBuilder.build();


            SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
            bFile.accept(semanticAnalyzer);

            // Linker

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testFuncInvocation() {
        BallerinaFunction function = (BallerinaFunction) bFile.getFunctions().get(funcName);

        // Create control stack and the stack frame
        BContext ctx = new BContext();
        ControlStack controlStack = ctx.getControlStack();

        int sizeOfValueArray =  function.getStackFrameSize();

        BValueRef[] values = new BValueRef[sizeOfValueArray];
        values[0] = new BValueRef(new IntValue(10));
        values[1] = new BValueRef(new IntValue(1000));
        values[2] = new BValueRef(new IntValue(20));

        BValueRef[] returnVals = new BValueRef[function.getReturnTypesC().length];

        StackFrame stackFrame = new StackFrame(values, returnVals);
        controlStack.pushFrame(stackFrame);

        BLangInterpreter interpreter = new BLangInterpreter(ctx);
        function.accept(interpreter);

        int expectedA = returnVals[0].getInt();
        int actualA = 1020;
        Assert.assertEquals(actualA, expectedA);

        int expectedB = returnVals[1].getInt();
        int actualB = 40;
        Assert.assertEquals(actualB, expectedB);

//        System.out.println(expectedA);
//        System.out.println(expectedB);
    }

    public static void main(String[] args) {
        BLangInterpreterTest test = new BLangInterpreterTest();
        test.setup();
        test.testFuncInvocation();
    }
}
