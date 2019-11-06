/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.command;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.context.LogLeecher.LeecherType;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This class tests CLI parsing.
 *
 * @since 0.981.2
 */
public class CommandParserTestCase extends BaseTest {

    private String sourceArg = (new File("src/test/resources/command/test_cmd_parser.bal")).getAbsolutePath();
    private Path tempProjectDirectory;

    @BeforeClass()
    public void setUp() throws BallerinaTestException, IOException {
        tempProjectDirectory = Files.createTempDirectory("bal-test-option-ordering");
    }

    @Test (description = "Tests correct identification of empty strings as args")
    public void testEmptyStringArg() throws BallerinaTestException {
        LogLeecher outLogLeecher = new LogLeecher("empty_string");
        balClient.runMain(sourceArg, null, new String[]{""}, new LogLeecher[]{outLogLeecher});
        outLogLeecher.waitForText(2000);
    }

    @Test (description = "Tests enforcing CLI option/param ordering: any argument specified after the first "
            + "non-option argument (the source to run) needs to be identified as a param, and not an option, even if "
            + "an option of the same name exists",
           dataProvider = "runCmdOptions")
    public void testCliParamOrderEnforcement(String cmdOption) throws BallerinaTestException {
        LogLeecher outLogLeecher = new LogLeecher(cmdOption);
        balClient.runMain(sourceArg, null, new String[]{cmdOption}, new LogLeecher[]{outLogLeecher});
        outLogLeecher.waitForText(2000);
    }

    @Test (description = "Test arguments starting with a dash", dataProvider = "valuesWithDashPrefix")
    public void testDashPrefixedArg(String argWithDashPrefix) throws BallerinaTestException {
        LogLeecher outLogLeecher = new LogLeecher(argWithDashPrefix);
        balClient.runMain(sourceArg, null, new String[]{argWithDashPrefix}, new LogLeecher[]{outLogLeecher});
        outLogLeecher.waitForText(2000);
    }

    @Test(description = "Test option param ordering not being enforced for non run command")
    public void testBuildWithOutputOptionAfterSource() throws Exception {
        Path targetDirPath = tempProjectDirectory.resolve("target");
        Files.createDirectories(targetDirPath);
        String[] clientArgs = {(new File("src/test/resources/command/test_option_ordering.bal")).getAbsolutePath(),
                                "-o", targetDirPath.toString().concat(File.separator).concat("order_test_one")};
        balClient.runMain("build", clientArgs, null, new String[]{},
                new LogLeecher[]{}, tempProjectDirectory.toString());

        Path generatedBalx = targetDirPath.resolve("order_test_one.balx");
        Assert.assertTrue(Files.exists(generatedBalx));
    }

    @Test(description = "Test option param ordering not being enforced for non run command")
    public void testBuildWithOutputOptionBeforeSource() throws Exception {
        Path targetDirPath = tempProjectDirectory.resolve("target");
        Files.createDirectories(targetDirPath);
        String[] clientArgs = {"-o", targetDirPath.toString().concat(File.separator).concat("order_test_two"),
                        (new File("src/test/resources/command/test_option_ordering.bal")).getAbsolutePath()};
        balClient.runMain("build", clientArgs, null, new String[]{},
                new LogLeecher[]{}, tempProjectDirectory.toString());
        Path generatedBalx = targetDirPath.resolve("order_test_two.balx");
        Assert.assertTrue(Files.exists(generatedBalx));
    }

    @Test (description = "Test unknown commands", dataProvider = "invalidCommands")
    public void testUnknownCommand(String unknownCmd) throws BallerinaTestException {
        LogLeecher errLogLeecher = new LogLeecher("ballerina: unknown command '"
                + unknownCmd + "'", LeecherType.ERROR);
        balClient.runMain(unknownCmd, new String[0], null, new String[0],
                new LogLeecher[]{errLogLeecher}, balServer.getServerHome());
        errLogLeecher.waitForText(2000);
    }

    @Test (description = "Test unknown option", dataProvider = "invalidOptions")
    public void testUnknownOption(String unknownOption) throws BallerinaTestException {
        LogLeecher errLogLeecher = new LogLeecher("ballerina: unknown option: " + unknownOption, LeecherType.ERROR);
        balClient.runMain(unknownOption, new String[0], null, new String[0], new LogLeecher[]{errLogLeecher},
                          balServer.getServerHome());
        errLogLeecher.waitForText(2000);
    }

    @DataProvider(name = "runCmdOptions")
    public Object[][] runCmdOptions() {
        return new Object[][] {
                { "--debug" },
                { "--offline" },
                { "-e" }
        };
    }

    @DataProvider(name = "valuesWithDashPrefix")
    public Object[][] valuesWithDashPrefix() {
        return new Object[][] {
                { "-5" },
                { "-1.0" },
                { "-config" }
        };
    }

    @DataProvider(name = "invalidCommands")
    public Object[][] invalidCommands() {
        return new Object[][] {
                { "runs" },
                { "buil" },
                { "text" }
        };
    }

    @DataProvider(name = "invalidOptions")
    public Object[][] invalidOptions() {
        return new Object[][] {
                { "-version" },
                { "--hel" }
        };
    }
}
