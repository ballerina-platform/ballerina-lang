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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValueType;
import org.wso2.ballerina.core.nativeimpl.lang.system.PrintString;
import org.wso2.ballerina.core.nativeimpl.lang.system.PrintlnString;
import org.wso2.ballerina.core.utils.FunctionUtils;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.lang.util.Functions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Test Native functions in ballerina.lang.system.
 */
public class SystemTest {

    private BallerinaFile bFile;
    private final String funcName = "invokeNativeFunction";
    private static final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    private PrintStream original;

    @BeforeClass
    public void setup() {
        original = System.out;
        System.setOut(new PrintStream(outContent));

        // Add  Native functions.
        SymScope symScope = new SymScope(null);
        FunctionUtils.addNativeFunction(symScope, new PrintlnString());
        FunctionUtils.addNativeFunction(symScope, new PrintString());
        bFile = ParserUtils.parseBalFile("samples/nativeimpl/systemTest.bal", symScope);
    }

    @AfterClass
    public void cleanup() throws IOException {
        System.setOut(original);
        outContent.close();
    }

    @Test
    public void testPrintString() {
        final String s1 = "Hello World...!!!";
        final String s2 = "A Greeting from Ballerina...!!!";
        final String expected = s1 + "\n" + s2;

        BValueType[] args = {new BString(s1), new BString(s2)};
        Functions.invoke(bFile, funcName, args);

        Assert.assertEquals(outContent.toString(), expected);
    }
}
