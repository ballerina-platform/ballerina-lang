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
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.interpreter.BLangInterpreter;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.StackFrame;
import org.wso2.ballerina.core.linker.BLangLinker;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.builder.BLangModelBuilder;
import org.wso2.ballerina.core.model.expressions.BasicLiteral;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.model.values.IntValue;
import org.wso2.ballerina.core.parser.antlr4.BLangAntlr4Listener;
import org.wso2.ballerina.core.semantics.SemanticAnalyzer;
import org.wso2.ballerina.core.utils.TestUtils;

import java.io.FileInputStream;
import java.io.IOException;

/**
 *
 */
public class LocalFuncInvocationTest {

    private BLangAntlr4Listener langModelBuilder;
    private BallerinaFile bFile;
    private String funcName = "process";

    @BeforeTest
    public void setup() {
        try {
            ANTLRInputStream antlrInputStream = new ANTLRInputStream(new FileInputStream(
                    getClass().getClassLoader().getResource("samples/parser/localFuncInvocationTest.bal").getFile()));

            BallerinaLexer ballerinaLexer = new BallerinaLexer(antlrInputStream);
            CommonTokenStream ballerinaToken = new CommonTokenStream(ballerinaLexer);

            BallerinaParser ballerinaParser = new BallerinaParser(ballerinaToken);

            BLangModelBuilder modelBuilder = new BLangModelBuilder();
            langModelBuilder = new BLangAntlr4Listener(modelBuilder);

            ballerinaParser.addParseListener(langModelBuilder);
            ballerinaParser.compilationUnit();
//            ballerinaParser.setErrorHandler();

            bFile = modelBuilder.build();

            SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(bFile);
            bFile.accept(semanticAnalyzer);

            // Linking functions defined in the same source file
//            for (FunctionInvocationExpr expr : bFile.getFuncIExprs()) {
//                SymbolName symName = expr.getFunctionName();
//                BallerinaFunction bFunction = (BallerinaFunction) bFile.getFunctions().get(symName.getName());
//
//                if (bFunction == null) {
//                    throw new IllegalStateException("Undefined function: " + symName.getName());
//                }
//
//                expr.setFunction(bFunction);
//            }

            // Linker
            BLangLinker linker = new BLangLinker(bFile);
            linker.link(null);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLocalFuncInvocation() {

        BallerinaFile bFile = TestUtils.parseBalFile("samples/parser/localFuncInvocationTest.bal");

        BValueRef valueRefA = new BValueRef(new IntValue(100));
        BasicLiteral basicLiteralA = new BasicLiteral(valueRefA);

        BValueRef valueRefB = new BValueRef(new IntValue(5));
        BasicLiteral basicLiteralB = new BasicLiteral(valueRefB);

        BValueRef valueRefC = new BValueRef(new IntValue(1));
        BasicLiteral basicLiteralC = new BasicLiteral(valueRefC);

        Expression[] exprs = new Expression[3];
        exprs[0] = basicLiteralA;
        exprs[1] = basicLiteralB;
        exprs[2] = basicLiteralC;

        FunctionInvocationExpr funcIExpr = new FunctionInvocationExpr(new SymbolName(funcName), exprs);
        funcIExpr.setOffset(0);
        funcIExpr.setFunction(bFile.getFunctions().get(funcName));

        BValueRef[] results = new BValueRef[1];
        StackFrame stackFrame = new StackFrame(results, null);

        Context bContext = new Context();
        bContext.getControlStack().pushFrame(stackFrame);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);

        int actual = results[0].getInt();
        int expected = 116;

        Assert.assertEquals(actual, expected);
    }

    public static void main(String[] args) {
        LocalFuncInvocationTest test = new LocalFuncInvocationTest();
//        test.setup();
        test.testLocalFuncInvocation();
    }
}
