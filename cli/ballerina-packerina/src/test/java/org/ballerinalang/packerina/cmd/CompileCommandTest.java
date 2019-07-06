/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.packerina.cmd;

import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Test cases for ballerina compile command.
 *
 * @since 1.0.0
 */
public class CompileCommandTest extends CommandTest {

    @Test(description = "Test Compile Command")
    public void testCompileCommand() throws IOException {
        String[] newArgs = {"compileTest"};
        // Create a project
        NewCommand newCommand = new NewCommand(tmpDir, printStream);
        new CommandLine(newCommand).parse(newArgs);
        newCommand.execute();

        Path projectDirectory = tmpDir.resolve("compileTest");
        Path moduleDir = projectDirectory
                .resolve(ProjectDirConstants.SOURCE_DIR_NAME)
                .resolve("mymodule");
        Files.createDirectory(moduleDir);

        Path balFile = moduleDir.resolve("main.bal");
        Files.createFile(balFile);

        String question = "public function main(){ int i =5; }";
        Files.write(balFile, question.getBytes());

        // Compile the project
        String[] compileArgs = {"--skip-tests", "-c", "--jvmTarget"};
        CompileCommand compileCommand = new CompileCommand(projectDirectory, printStream);
        new CommandLine(compileCommand).parse(compileArgs);
        compileCommand.execute();
        String sample = "test";

    }
}
