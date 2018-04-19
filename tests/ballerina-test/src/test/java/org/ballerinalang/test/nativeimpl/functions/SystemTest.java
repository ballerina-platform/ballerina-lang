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
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BRefValueArray;
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
            final String expected = "{}\n{}";

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

    @Test
    public void testFormatBooleanTrue() {
        BRefValueArray fArgs = new BRefValueArray();
        fArgs.add(0, new BBoolean(true));
        BValue[] args = {new BString("%b"), fArgs};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns[0].stringValue(), "true");
    }

    @Test
    public void testFormatBooleanFalse() {
        BRefValueArray fArgs = new BRefValueArray();
        fArgs.add(0, new BBoolean(false));
        BValue[] args = {new BString("%b"), fArgs};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns[0].stringValue(), "false");
    }

    @Test
    public void testFormatDecimal() {
        BRefValueArray fArgs = new BRefValueArray();
        fArgs.add(0, new BInteger(65));
        BValue[] args = {new BString("%d"), fArgs};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns[0].stringValue(), "65");
    }

    @Test
    public void testFormatFloat() {
        BRefValueArray fArgs = new BRefValueArray();
        fArgs.add(0, new BFloat(3.25));
        BValue[] args = {new BString("%f"), fArgs};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns[0].stringValue(), "3.250000");
    }

    @Test
    public void testFormatString() {
        String name = "John";
        BRefValueArray fArgs = new BRefValueArray();
        fArgs.add(0, new BString(name));
        BValue[] args = {new BString("%s"), fArgs};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns[0].stringValue(), name);
    }

    @Test
    public void testFormatHex() {
        BRefValueArray fArgs = new BRefValueArray();
        fArgs.add(0, new BInteger(57005));
        BValue[] args = {new BString("%x"), fArgs};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns[0].stringValue(), "dead");
    }

    @Test
    public void testFormatOctal() {
        BRefValueArray fArgs = new BRefValueArray();
        fArgs.add(0, new BInteger(57005));
        BValue[] args = {new BString("%o"), fArgs};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns[0].stringValue(), "157255");
    }

    @Test
    public void testFormatIntArray() {
        BRefValueArray fArgs = new BRefValueArray();
        BIntArray arr = new BIntArray();
        arr.add(0, 111);
        arr.add(1, 222);
        arr.add(2, 333);

        fArgs.add(0, arr);
        BValue[] args = {new BString("%a"), fArgs};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns[0].stringValue(), "[111, 222, 333]");
    }

    @Test
    public void testFormatLiteralPercentChar() {
        BRefValueArray fArgs = new BRefValueArray();
        fArgs.add(0, new BString("test"));
        BValue[] args = {new BString("%% %s"), fArgs};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns[0].stringValue(), "% test");
    }

    @Test
    public void testFormatStringWithPadding() {
        BRefValueArray fArgs = new BRefValueArray();
        fArgs.add(0, new BString("Hello Ballerina"));
        BValue[] args = {new BString("%9.2s"), fArgs};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns[0].stringValue(), "       He");
    }

    @Test
    public void testFormatFloatWithPadding() {
        BRefValueArray fArgs = new BRefValueArray();
        fArgs.add(0, new BFloat(123456789.9876543));
        BValue[] args = {new BString("%5.4f"), fArgs};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns[0].stringValue(), "123456789.9877");
    }

    @Test
    public void testFormatDecimalWithPadding() {
        BRefValueArray fArgs = new BRefValueArray();
        fArgs.add(0, new BInteger(12345));
        BValue[] args = {new BString("%15d"), fArgs};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns[0].stringValue(), "          12345");
    }

    @Test
    public void testFormatBinary() {
        BRefValueArray fArgs = new BRefValueArray();
        fArgs.add(0, new BInteger(12345));
        BValue[] args = {new BString("%B"), fArgs};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSprintf", args);
        Assert.assertEquals(returns[0].stringValue(), "11000000111001");
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
