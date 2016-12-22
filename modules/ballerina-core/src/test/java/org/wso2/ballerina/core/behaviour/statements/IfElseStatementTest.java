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
package org.wso2.ballerina.core.behaviour.statements;

//import org.testng.Assert;
//import org.testng.annotations.BeforeTest;
//import org.testng.annotations.Test;
//import org.wso2.ballerina.core.interpreter.BLangInterpreter;
//import org.wso2.ballerina.core.interpreter.Context;
//import org.wso2.ballerina.core.linker.BLangLinker;
//import org.wso2.ballerina.core.model.BallerinaFile;
//import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
//import org.wso2.ballerina.core.model.values.BInteger;
//import org.wso2.ballerina.core.model.values.BValueType;
//import org.wso2.ballerina.core.utils.FunctionUtils;
//import org.wso2.ballerina.core.utils.ParserUtils;

public class IfElseStatementTest {

//    private BallerinaFile bFile;
//    private final String funcName = "test";
//
//    @BeforeTest
//    public void setup() {
//        bFile = ParserUtils.parseBalFile("samples/statements/ifcondition.bal");
//        // Linker
//        BLangLinker linker = new BLangLinker(bFile);
//        linker.link(null);
//    }
//
//    @Test(description = "Check a == b")
//    public void testIfBlock() {
//        BValueType[] args = {new BInteger(10), new BInteger(10), new BInteger(20), null};
//        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, funcName, args.length);
//
//        Context bContext = FunctionUtils.createInvocationContext(args, 2);
//        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
//        funcIExpr.accept(bLangInterpreter);
//
//        int actualA = ((BInteger) FunctionUtils.getValue(bContext, args.length - 1)).intValue();
//        int expectedA = 110;
//
//        Assert.assertEquals(actualA, expectedA);
//
//        // TODO: Fix 2nd Return Values.
////        int actualB = FunctionUtils.getValue(bContext, 1).getInt();
////        int expectedB = 21;
////        Assert.assertEquals(actualB, expectedB);
//
//    }
//
//    public static void main(String[] args) {
//        IfElseStatementTest test = new IfElseStatementTest();
//
//        test.setup();
//        test.testIfBlock();
//    }
//
//    @Test(description = "Check a == b + 2")
//    public void testElseIfSecondBlock() {
//        BValueType[] args = {new BInteger(12), new BInteger(10), new BInteger(20)};
//        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, funcName, args.length);
//
//        Context bContext = FunctionUtils.createInvocationContext(args, 2);
//        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
//        funcIExpr.accept(bLangInterpreter);
//
//        int actualA = ((BInteger) FunctionUtils.getValue(bContext)).intValue();
//        int expectedA = 310;
//        Assert.assertEquals(actualA, expectedA);
//
//        // TODO: Fix 2nd Return Values.
////        int actualB = FunctionUtils.getValue(bContext, 1).getInt();
////        int expectedB = 21;
////        Assert.assertEquals(actualB, expectedB);
//
//    }
//
//    @Test(description = "Check else")
//    public void testElseBlock() {
//        BValueType[] args = {new BInteger(10), new BInteger(100), new BInteger(20)};
//        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, funcName, args.length);
//
//        Context bContext = FunctionUtils.createInvocationContext(args, 2);
//        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
//        funcIExpr.accept(bLangInterpreter);
//
//        int actualA = ((BInteger) FunctionUtils.getValue(bContext)).intValue();
//        int expectedA = 410;
//        Assert.assertEquals(actualA, expectedA);
//
//        // TODO: Fix 2nd Return Values.
////        int actualB = FunctionUtils.getValue(bContext, 1).getInt();
////        int expectedB = 21;
////        Assert.assertEquals(actualB, expectedB);
//
//    }


}
