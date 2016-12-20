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
package org.wso2.ballerina.core.nativeimpl.functions;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.interpreter.BLangInterpreter;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.linker.BLangLinker;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.StringValue;
import org.wso2.ballerina.core.nativeimpl.lang.system.PrintString;
import org.wso2.ballerina.core.nativeimpl.lang.system.PrintlnString;
import org.wso2.ballerina.core.utils.FunctionUtils;
import org.wso2.ballerina.core.utils.ParserUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class NativeFunctionTest {

    private BallerinaFile bFile;
    private final String funcName = "invokeNativeFunction";
    private static final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    private PrintStream original;

    @BeforeTest
    public void setup() {
        original = System.out;
        System.setOut(new PrintStream(outContent));
        bFile = ParserUtils.parseBalFile("samples/nativeimpl/println.bal");

        // Linking Native functions.
        SymScope symScope = new SymScope(null);
        FunctionUtils.addNativeFunction(symScope, new PrintlnString());
        FunctionUtils.addNativeFunction(symScope, new PrintString());
        BLangLinker linker = new BLangLinker(bFile);
        linker.link(symScope);
    }

    @AfterClass
    public void cleanup() throws IOException {
        System.setOut(original);
        outContent.close();
    }

    @Test
    public void testNativeFuncSystemPrint() {
        final String s1 = "Hello World...!!!";
        final String s2 = "A Greeting from Ballerina...!!!";
        final String expected = s1 + "\n" + s2;
        BValue[] arguments = {new StringValue(s1), new StringValue(s2)};
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, funcName, arguments);

        Context bContext = FunctionUtils.createInvocationContext(1);
        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);

        Assert.assertEquals(outContent.toString(), expected);
    }

}
