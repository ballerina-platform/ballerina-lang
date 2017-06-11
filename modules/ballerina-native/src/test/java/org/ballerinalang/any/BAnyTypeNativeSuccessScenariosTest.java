/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.any;

import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * This class contains methods to test the any type implementation success scenarios.
 *
 * @since 0.85
 */
public class BAnyTypeNativeSuccessScenariosTest {
    private ProgramFile bLangProgram;
    private PrintStream original;

    @BeforeClass
    public void setup() {
        original = System.out;
        bLangProgram = BTestUtils.getProgramFile("lang/any/any-type-native-success.bal");
    }

    @Test(description = "Test json value in any type get casted to xml in two steps", enabled = false)
    public void testJsonInAnyCastToX() {
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "successfulXmlCasting");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        BXML xmlVal = (BXML) returns[0];
        Assert.assertTrue(xmlVal.stringValue().contains("<PropertyName>Value</PropertyName>"),
                          "Invalid xml value returned.");
    }


    @Test(description = "This tests the finding of best native function when there are no direct match for println")
    public void testAnyPrintln() throws IOException {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(outContent));
            BLangFunctions.invokeNew(bLangProgram, "printlnAnyVal");
            Assert.assertEquals(outContent.toString().replace("\r", ""), "{\"PropertyName\":\"Value\"}\n",
                              "Invalid xml printed");
        } finally {
            outContent.close();
            System.setOut(original);
        }
    }

    @Test(description = "This tests the finding of best native function when there are no direct match for print")
    public void testAnyPrint() throws IOException {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(outContent));
            BLangFunctions.invokeNew(bLangProgram, "printAnyVal");
            Assert.assertEquals(outContent.toString().replace("\r", ""), "{\"PropertyName\":\"Value\"}",
                                "Invalid xml printed");
        } finally {
            outContent.close();
            System.setOut(original);
        }
    }

    @Test(description = "This tests the finding of best native function when there are no direct match for print int")
    public void testFindBestMatchForNativeFunctionPrintln() throws IOException {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(outContent));
            BLangFunctions.invokeNew(bLangProgram, "findBestNativeFunctionPrintln");
            Assert.assertEquals(outContent.toString().replace("\r", ""), "8\n", "Invalid int value printed");
        } finally {
            outContent.close();
            System.setOut(original);
        }
    }

    @Test(description = "This tests the finding of best native function when there are no direct match for println int")
    public void testFindBestMatchForNativeFunctionPrint() throws IOException {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(outContent));
            BLangFunctions.invokeNew(bLangProgram, "findBestNativeFunctionPrint");
            Assert.assertEquals(outContent.toString().replace("\r", ""), "7", "Invalid int value printed");
        } finally {
            outContent.close();
            System.setOut(original);
        }
    }

}
