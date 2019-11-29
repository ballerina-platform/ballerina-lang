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
package org.ballerinalang.openapi.cmd;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

/**
 * OpenAPI command cmd common class to handle temp dirs and outputs.
 */
public abstract class OpenAPICommandTest {
    protected Path tmpDir;
    private ByteArrayOutputStream console;
    protected PrintStream printStream;

    protected String readOutput() throws IOException {
        return readOutput(false);
    }

    protected String readOutput(boolean slient) throws IOException {
        String output = "";
        output = console.toString();
        console.close();
        console = new ByteArrayOutputStream();
        printStream = new PrintStream(console);
        if (!slient) {
            PrintStream out = System.out;
            out.println(output);
        }
        return output;
    }

    @BeforeClass
    public void setup() throws IOException {
        tmpDir = Files.createTempDirectory("openapi-cmd");
        console = new ByteArrayOutputStream();
        printStream = new PrintStream(console);
    }

    @AfterClass
    public void cleanup() throws IOException {
        Files.walk(tmpDir)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        Assert.fail(e.getMessage(), e);
                    }
                });
        console.close();
        printStream.close();
    }

    /**
     * Retrieve resource location for test purpose.
     *
     * @return resource path.
     */
    public static Path getResourceFolderPath() {
        return Paths.get("src/test/resources/").toAbsolutePath();
    }

    public static OpenAPIBallerinaProject createBalProject(String directory) throws IOException {
        OpenAPIBallerinaProject openAPIBallerinaProject = new OpenAPIBallerinaProject();
        Path projectPath = Paths.get(directory);
        if (Files.notExists(projectPath)) {
            Files.createDirectory(projectPath);
        }

        if (Files.notExists(projectPath.resolve("Ballerina.toml"))) {
            File file = new File(projectPath.resolve("Ballerina.toml").toString());
            file.createNewFile();
        }
        openAPIBallerinaProject.setBalProjectPath(projectPath);

        Path srcPath = projectPath.resolve("src");
        if (Files.notExists(srcPath)) {
            Files.createDirectory(srcPath);
        }
        openAPIBallerinaProject.setSrcPath(srcPath);

        return openAPIBallerinaProject;
    }

    public static void createBalProjectModule(OpenAPIBallerinaProject openApiProject, String moduleName)
            throws IOException {
        if (moduleName != null) {
            Path implPath = openApiProject.getSrcPath().resolve(moduleName);
            if (Files.notExists(implPath)) {
                Files.createDirectory(implPath);
            }
            openApiProject.setImplPath(implPath);

            Path resourcePath = implPath.resolve("resources");
            if (Files.notExists(resourcePath)) {
                Files.createDirectory(resourcePath);
            }
        }
    }
}
