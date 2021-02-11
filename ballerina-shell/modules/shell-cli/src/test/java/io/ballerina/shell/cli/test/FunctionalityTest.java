/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.cli.test;

import io.ballerina.shell.cli.BShellConfiguration;
import io.ballerina.shell.cli.ReplShellApplication;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Test for CLI functionality.
 *
 * @since 2.0.0
 */
public class FunctionalityTest {
    @Test
    public void testShellExit() throws Exception {
        // Give no input to emulate EOF
        // Having an empty stream will cause JLine to throw an EndOfFileException.
        // This should cause shell to print "Bye!!!"

        String input = "";
        ByteArrayOutputStream shellOut = new ByteArrayOutputStream();
        ByteArrayInputStream shellIn = new ByteArrayInputStream(input.getBytes());

        // Run the Shell
        BShellConfiguration configuration = new BShellConfiguration.Builder()
                .setInputStream(shellIn).setOutputStream(shellOut)
                .setDumb(true).setTreeParsingTimeoutMs(10000).build();
        ReplShellApplication.execute(configuration);

        // Output should have Bye!!!
        Assert.assertTrue(shellOut.toString().contains("Bye!!!"));
    }

    @Test
    public void testInvalidStartFile() throws Exception {
        ByteArrayOutputStream shellOut = new ByteArrayOutputStream();
        ByteArrayInputStream shellIn = new ByteArrayInputStream("".getBytes());

        // Run the Shell with an invalid start file
        BShellConfiguration configuration = new BShellConfiguration.Builder()
                .setInputStream(shellIn).setOutputStream(shellOut).setStartFile("invalid_file.bal")
                .setDumb(true).setTreeParsingTimeoutMs(10000).build();
        ReplShellApplication.execute(configuration);

        // Initialization should fail
        Assert.assertTrue(shellOut.toString().contains("Shell Initialization Failed!!!"));
    }
}
