/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.cli.cmd;

import org.testng.Assert;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Search command tests.
 *
 * @since 2.0.
 */
public class CompgenCommandTest extends BaseCommandTest {

    @Test(description = "Test listed sub commands for bal")
    public void testBalCompgenSuggestion() throws IOException {
        CompgenCommand compgenCommand = new CompgenCommand(tmpDir, printStream);
        new CommandLine(compgenCommand).parse();
        compgenCommand.execute();

        String output = readOutput(true);
        Assert.assertTrue(output.contains("add"));
        Assert.assertTrue(output.contains("build"));
        Assert.assertTrue(output.contains("new"));
        Assert.assertTrue(output.contains("pull"));
        Assert.assertTrue(output.contains("push"));
    }

    @Test(description = "Test listed sub commands in accordance with the args")
    public void testCompgenSuggestion() throws IOException {
        CompgenCommand compgenCommand = new CompgenCommand(tmpDir, printStream);
        new CommandLine(compgenCommand).parse("--", "b");
        compgenCommand.execute();

        String output = readOutput(true);
        Assert.assertTrue(output.contains("build"));
    }

    @Test(description = "Test listed flags in accordance with the args")
    public void testCompgenFlagSuggestion() throws IOException {
        CompgenCommand compgenCommand = new CompgenCommand(tmpDir, printStream);
        new CommandLine(compgenCommand).parse("--", "build", "--c");
        compgenCommand.execute();

        String output = readOutput(true);
        Assert.assertTrue(output.contains("--compile"));
        Assert.assertTrue(output.contains("--code-coverage"));
    }

    @Test(description = "Test listed flags in accordance with the multiple args")
    public void testCompgenMultipleFlagSuggestion() throws IOException {
        CompgenCommand compgenCommand = new CompgenCommand(tmpDir, printStream);
        new CommandLine(compgenCommand).parse("--", "build", "--compile", "--s");
        compgenCommand.execute();

        String output = readOutput(true);
        Assert.assertTrue(output.contains("--skip-tests"));
    }

    @Test(description = "Test listed sub commands for dist")
    public void testDistCompgenSuggestion() throws IOException {
        CompgenCommand compgenCommand = new CompgenCommand(tmpDir, printStream);
        new CommandLine(compgenCommand).parse("--", "dist");
        compgenCommand.execute();

        String output = readOutput(true);
        Assert.assertTrue(output.contains("pull"));
        Assert.assertTrue(output.contains("use"));
        Assert.assertTrue(output.contains("update"));
    }

    @Test(description = "Test listed suggestions if the arg is not starts with the flag options")
    public void testCompgenDirectorySuggestion() throws IOException {
        Path dirPath = tmpDir.resolve("sample");
        Files.createDirectory(dirPath);
        Path balFile1 = tmpDir.resolve("sample.bal");
        Files.createFile(balFile1);
        Path balFile2 = tmpDir.resolve("data.bal");
        Files.createFile(balFile2);
        Path textFile = tmpDir.resolve("data.txt");
        Files.createFile(textFile);
        CompgenCommand compgenCommand = new CompgenCommand(tmpDir, printStream);
        new CommandLine(compgenCommand).parse("--", "build");
        compgenCommand.execute();

        String output = readOutput(true);
        Assert.assertTrue(output.contains("sample/"));
        Assert.assertTrue(output.contains("sample.bal"));
        Assert.assertTrue(output.contains("data.bal"));
        Assert.assertFalse(output.contains("data.txt"));
    }
}
