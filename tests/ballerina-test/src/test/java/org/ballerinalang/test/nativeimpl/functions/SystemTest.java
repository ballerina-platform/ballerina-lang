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
package org.ballerinalang.test.nativeimpl.functions;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueType;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Test Native functions in ballerina.model.system.
 */
public class SystemTest {

    private CompileResult compileResult;
    private final String printFuncName = "testPrintAndPrintln";

    private PrintStream original;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        original = System.out;
        compileResult = BCompileUtil.compile("test-src/nativeimpl/functions/system-test.bal");
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws IOException {
        System.setOut(original);
    }

    @Test
    public void testStringPrintAndPrintln() throws IOException {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(outContent));
            final String s1 = "Hello World...!!!";
            final String s2 = "A Greeting from Ballerina...!!!";
            final String expected = s1 + "\n" + s2;

            BValueType[] args = {new BString(s1), new BString(s2)};
            BRunUtil.invoke(compileResult, printFuncName + "String", args);
            Assert.assertEquals(outContent.toString().replace("\r", ""), expected);
        } finally {
            outContent.close();
            System.setOut(original);
        }
    }

    @Test
    public void testIntPrintAndPrintln() throws IOException {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(outContent));
            final int v1 = 1000;
            final int v2 = 1;
            final String expected = v1 + "\n" + v2;

            BValueType[] args = {new BInteger(v1), new BInteger(v2)};
            BRunUtil.invoke(compileResult, printFuncName + "Int", args);
            Assert.assertEquals(outContent.toString().replace("\r", ""), expected);
        } finally {
            outContent.close();
            System.setOut(original);
        }
    }

    @Test
    public void testFloatPrintAndPrintln() throws IOException {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(outContent));
            final float v1 = 1000;
            final float v2 = 1;
            final String expected = v1 + "\n" + v2;

            BValueType[] args = {new BFloat(v1), new BFloat(v2)};
            BRunUtil.invoke(compileResult, printFuncName + "Float", args);
            Assert.assertEquals(outContent.toString().replace("\r", ""), expected);
        } finally {
            outContent.close();
            System.setOut(original);
        }
    }

    @Test
    public void testBooleanPrintAndPrintln() throws IOException {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(outContent));
            final boolean v1 = false;
            final boolean v2 = true;
            final String expected = v1 + "\n" + v2;

            BValueType[] args = {new BBoolean(v1), new BBoolean(v2)};
            BRunUtil.invoke(compileResult, printFuncName + "Boolean", args);
            Assert.assertEquals(outContent.toString().replace("\r", ""), expected);
        } finally {
            outContent.close();
            System.setOut(original);
        }
    }

    @Test
    public void testConnectorPrintAndPrintln() throws IOException {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(outContent));
            final String expected = "\n";

            BRunUtil.invoke(compileResult, printFuncName + "Connector");
            Assert.assertEquals(outContent.toString().replace("\r", ""), expected);
        } finally {
            outContent.close();
            System.setOut(original);
        }
    }

    @Test
    public void testFunctionPointerPrintAndPrintln() throws IOException {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(outContent));
            final String expected = "\n";

            BRunUtil.invoke(compileResult, printFuncName + "FunctionPointer");
            Assert.assertEquals(outContent.toString().replace("\r", ""), expected);
        } finally {
            outContent.close();
            System.setOut(original);
        }
    }

    @Test(description = "Test new line character in string")
    public void testNewlineCharacter() {
        ByteArrayOutputStream out = null;
        PrintStream mainStream = System.out;
        try {
            out = new ByteArrayOutputStream();
            System.setOut(new PrintStream(out));
            BValue[] args = {};
            BRunUtil.invoke(compileResult, "printNewline", args);
            String outPut = out.toString();
            Assert.assertNotNull(outPut, "string is not printed");
            //getting the last new line character
            Assert.assertEquals(outPut.charAt(outPut.length() - 1), '\n'
                    , "New line character not found in output string");
        } finally {
            System.setOut(mainStream);
            try {
                out.close();
            } catch (IOException e) {
                //ignore
            }
        }

    }

//    @Test(expectedExceptions = BallerinaException.class)
//    public void testGetEnvNonExisting() throws IOException {
//        try (ByteArrayOutputStream outContent = new ByteArrayOutputStream()) {
//            System.setOut(new PrintStream(outContent));
//            BValueType[] args = {new BString("PATH2")};
//            BLangFunctions.invokeNew(compileResult, "getEnvVar", args);
//            outContent.toString();
//        } finally {
//            System.setOut(original);
//        }
//    }

//    @Test(expectedExceptions = BLangRuntimeException.class)
//    public void testGetEnvEmptyKey() throws IOException {
//        try (ByteArrayOutputStream outContent = new ByteArrayOutputStream()) {
//            System.setOut(new PrintStream(outContent));
//            BValueType[] args = {new BString("")};
//            BLangFunctions.invokeNew(compileResult, "getEnvVar", args);
//            outContent.toString();
//        } finally {
//            System.setOut(original);
//        }
//    }
}
