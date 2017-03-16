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
package org.ballerinalang.runtime.main;

import org.ballerinalang.BLangProgramLoader;
import org.ballerinalang.BLangProgramRunner;
import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test cases for validate main function.
 */
public class MainFunctionTest {


    @BeforeClass
    public void setup() {

    }

    @AfterClass
    public void tearDown() {

    }

    @Test
    public void testInvokeMainFunction() {
        String sourceFilePath = "lang/runtime/main/main.bal";
        Path path;
        try {
            path = Paths.get(BTestUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("error while running test: " + e.getMessage());
        }
        BLangProgram mainProgram = new BLangProgramLoader().loadMain(path, Paths.get(sourceFilePath));
        new BLangProgramRunner().runMain(mainProgram, new String[0]);
    }

    @Test
    public void testInvokeFaultyMain() {
        String sourceFilePath = "lang/runtime/main/faulty-main.bal";
        Path path;
        try {
            path = Paths.get(BTestUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("error while running test: " + e.getMessage());
        }
        BLangProgram mainProgram = new BLangProgramLoader().loadMain(path, Paths.get(sourceFilePath));
        Exception exceptionExpected = null;
        try {
            new BLangProgramRunner().runMain(mainProgram, new String[0]);
        } catch (Exception ex) {
            exceptionExpected = ex;
        } finally {
            Assert.assertNotNull(exceptionExpected, "Exception expected");
            Assert.assertTrue(exceptionExpected instanceof BLangRuntimeException, "Exception "
                    + BLangRuntimeException.class);
            String expectedMessage = "error in ballerina program: arrays index out of range: Index: 2, Size: 0\n" +
                    "\t at test3(faulty-main.bal:21)\n" +
                    "\t at test2(faulty-main.bal:12)\n" +
                    "\t at test1(faulty-main.bal:8)\n" +
                    "\t at main(faulty-main.bal:3)";
            Assert.assertEquals(exceptionExpected.getMessage(), expectedMessage, "Unexpected error message");
        }
    }

    // TODO : Add more test cases covering thread switching.
}
