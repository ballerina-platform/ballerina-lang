/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.runtime.exceptions;

import org.ballerinalang.BLangProgramLoader;
import org.ballerinalang.BLangProgramRunner;
import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.BLangProgram;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test Cases for Main Program error handling.
 */
public class MainProgramErrorTest {

    private BLangProgram bLangProgram;
    private PrintStream original;

    @BeforeClass
    public void setup() {
        original = System.err;
        String sourceFilePath = "lang/exceptions/main";
        Path path = Paths.get(BTestUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        bLangProgram = new BLangProgramLoader().loadMain(path, Paths.get(sourceFilePath));
    }

    @AfterClass
    public void tearDown() {
        System.setOut(original);
    }

    @Test(description = "Testcase for validating error thrown at main function.")
    public void testMainFunctionErrorTest1() throws IOException {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try {
            System.setErr(new PrintStream(outContent));
            BLangProgramRunner bLangProgramRunner = new BLangProgramRunner();
            String[] args = {"test1"};
            bLangProgramRunner.runMain(bLangProgram, args);
            String output = outContent.toString();
            String expected = "error in ballerina program: arrays index out of range: Index: 10, Size: 4\n" +
                    "\t at main(lang/exceptions/main/main-program.bal:6)\n" +
                    "\t at main(lang/exceptions/main/main-program.bal:3)\n";
            Assert.assertEquals(output, expected, "Error message didn't match.");
        } finally {
            outContent.close();
            System.setErr(original);
        }
    }

    @Test(description = "Testcase for validating error thrown using throw statement.")
    public void testMainFunctionErrorTest2() throws IOException {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try {
            System.setErr(new PrintStream(outContent));
            BLangProgramRunner bLangProgramRunner = new BLangProgramRunner();
            String[] args = {"test2"};
            bLangProgramRunner.runMain(bLangProgram, args);
            String output = outContent.toString();
            String expected = "error in ballerina program: arrays index out of range: Index: 10, Size: 4\n" +
                    "\t at main(lang/exceptions/main/main-program.bal:10)\n" +
                    "\t at main(lang/exceptions/main/main-program.bal:3)\n";
            Assert.assertEquals(output, expected, "Error message didn't match.");
        } finally {
            outContent.close();
            System.setErr(original);
        }
    }

    public static void main(String[] args) {
        try {
            int[] a = {11, 12, 13, 14};
            int value = a[10];
        } catch (Exception e) {
            throw e;
        }
    }

}
