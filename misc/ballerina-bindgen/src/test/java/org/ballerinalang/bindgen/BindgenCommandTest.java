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
package org.ballerinalang.bindgen;

import org.ballerinalang.bindgen.command.BindgenCommand;
import org.ballerinalang.maven.MavenResolver;
import org.ballerinalang.maven.exceptions.MavenResolverException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

/**
 * Test the ballerina bindgen command options and functionality.
 *
 * @since 1.2
 */
public class BindgenCommandTest extends CommandTest {

    private Path testResources;

    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        try {
            this.testResources = super.tmpDir.resolve("build-test-resources");
            URI testResourcesURI = Objects.requireNonNull(getClass().getClassLoader()
                    .getResource("mvn-test-resources")).toURI();
            Files.walkFileTree(Paths.get(testResourcesURI), new BindgenCommandTest.Copy(Paths.get(testResourcesURI),
                    this.testResources));
        } catch (URISyntaxException e) {
            Assert.fail("error loading resources");
        }
    }

    @Test(description = "Test whether the bindgen tool loads the existing platform libraries " +
            "specified in the Ballerina.toml file")
    public void testExistingPlatformLibraries() throws IOException, MavenResolverException {
        String projectDir = Paths.get(testResources.toString(), "balProject").toString();
        String[] args = {"-o=" + projectDir, "java.lang.Object", "org.apache.log4j.Logger"};

        // Platform libraries specified through maven dependencies should be automatically resolved.
        // Explicitly add a jar to test the platform libraries specified as a path.
        MavenResolver mavenResolver = new MavenResolver(projectDir);
        mavenResolver.resolve("log4j", "log4j", "1.2.17", false);

        BindgenCommand bindgenCommand = new BindgenCommand(printStream, printStream, false);
        new CommandLine(bindgenCommand).parseArgs(args);

        bindgenCommand.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("Ballerina project detected at:"));
        Assert.assertTrue(output.contains("Following jars were added to the classpath:"));
        Assert.assertTrue(output.contains("snakeyaml-1.25.jar"));
        Assert.assertTrue(output.contains("commons-logging-1.1.1.jar"));
        Assert.assertTrue(output.contains("log4j-1.2.17.jar"));
        Assert.assertFalse(output.contains("Failed to add the following to classpath:"));
        Assert.assertFalse(output.contains("error: unable to generate the '"));
    }

    @Test(description = "Test if the correct error is given for incorrect classpaths")
    public void testIncorrectClasspath() throws IOException {
        String projectDir = Paths.get(testResources.toString(), "balProject").toString();
        String incorrectJarPath = Paths.get("./incorrect.jar").toString();
        String invalidDirPath = Paths.get("/User/invalidDir").toString();
        String[] args = {"-cp=" + incorrectJarPath + ", test.txt, " + invalidDirPath, "-o=" +
                projectDir, "java.lang.Object"};

        BindgenCommand bindgenCommand = new BindgenCommand(printStream, printStream, false);
        new CommandLine(bindgenCommand).parseArgs(args);

        bindgenCommand.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("Failed to add the following to classpath:"));
        Assert.assertTrue(output.contains("test.txt"));
        Assert.assertTrue(output.contains(invalidDirPath));
        Assert.assertTrue(output.contains(incorrectJarPath));
    }

    @Test(description = "Test if the correct error is given for incorrect maven option value")
    public void testIncorrectMavenLibrary() throws IOException {
        String projectDir = Paths.get(testResources.toString(), "balProject").toString();
        String[] args = {"-mvn=org.yaml.snakeyaml.1.25", "-o=" + projectDir, "java.lang.Object"};

        BindgenCommand bindgenCommand = new BindgenCommand(printStream, printStream, false);
        new CommandLine(bindgenCommand).parseArgs(args);

        bindgenCommand.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("error: invalid maven dependency provided"));
    }

    @Test(description = "Test if the correct error is given for an incorrect output path")
    public void testOutputPath() throws IOException {
        String incorrectPath = Paths.get("./incorrect").toString();
        String[] args = {"-o=" + incorrectPath, "java.lang.Object"};

        BindgenCommand bindgenCommand = new BindgenCommand(printStream, printStream, false);
        new CommandLine(bindgenCommand).parseArgs(args);

        bindgenCommand.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("Failed to generate the Ballerina bindings."));
        Assert.assertTrue(output.contains("error: output path provided could not be found: "));
    }

    @Test(description = "Test a scenario where the output path resides inside a project")
    public void testOutputPathInsideProject() throws IOException {
        String projectDir = Paths.get(testResources.toString(), "balProject", "tests").toString();
        String[] args = {"-o=" + projectDir, "java.lang.String"};

        BindgenCommand bindgenCommand = new BindgenCommand(printStream, printStream, false);
        new CommandLine(bindgenCommand).parseArgs(args);

        bindgenCommand.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("Ballerina project detected at: "));
    }

    @Test(description = "Test if the correct error is given when the output path is provided with the modules flag")
    public void testOutputPathWithModulesFlag() throws IOException {
        String projectDir = Paths.get(testResources.toString(), "balProject").toString();
        String[] args = {"-o=" + projectDir, "-m", "java.lang.Object"};

        BindgenCommand bindgenCommand = new BindgenCommand(printStream, printStream, false);
        new CommandLine(bindgenCommand).parseArgs(args);

        bindgenCommand.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("error: output path is not supported together with the modules flag"));
    }

    @Test(description = "Test if the correct error is given when no class names are provided")
    public void testNoClassNames() throws IOException {
        String projectDir = Paths.get(testResources.toString(), "balProject").toString();
        String[] args = {"-o=" + projectDir};

        BindgenCommand bindgenCommand = new BindgenCommand(printStream, printStream, false);
        new CommandLine(bindgenCommand).parseArgs(args);

        bindgenCommand.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("error: one or more class names are required"));
    }

    @Test(description = "Test if the correct error is given when a user tries to generate " +
            "module level mappings outside a ballerina project")
    public void testModuleFlagWithoutProject() throws IOException {
        String[] args = {"-m", "java.lang.Object"};

        BindgenCommand bindgenCommand = new BindgenCommand(printStream, printStream, false);
        new CommandLine(bindgenCommand).parseArgs(args);

        bindgenCommand.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("error: module level mappings can only be generated " +
                "inside a ballerina project"));
    }

    @Test(description = "Test if the correct error is given for invalid class names")
    public void testInvalidClassNames() throws IOException {
        String projectDir = Paths.get(testResources.toString(), "balProject").toString();
        String[] args = {"-o=" + projectDir, "java.lang.Objec", "java.lang.Apple", "java.lang.String"};

        BindgenCommand bindgenCommand = new BindgenCommand(printStream, printStream, false);
        new CommandLine(bindgenCommand).parseArgs(args);

        bindgenCommand.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("error: unable to generate the 'java.lang.Objec' binding class: " +
                "java.lang.ClassNotFoundException: java.lang.Objec"));
        Assert.assertTrue(output.contains("error: unable to generate the 'java.lang.Apple' binding class: " +
                "java.lang.ClassNotFoundException: java.lang.Apple"));
    }

    @Test(description = "Test if the correct error is given for failed method generations")
    public void testFailedMethodGenerations() throws IOException {
        String projectDir = Paths.get(testResources.toString(), "balProject").toString();
        String[] args = {"-o=" + projectDir, "org.ballerinalang.bindgen.MethodsTestResource", "java.invalid.Class"};

        BindgenCommand bindgenCommand = new BindgenCommand(printStream, printStream, false);
        new CommandLine(bindgenCommand).parseArgs(args);

        bindgenCommand.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("error: unable to generate the 'java.invalid.Class' binding class: " +
                "java.lang.ClassNotFoundException: java.invalid.Class"));
        Assert.assertTrue(output.contains("error: unable to generate the binding function 'unsupportedParam' of " +
                "'org.ballerinalang.bindgen.MethodsTestResource': multidimensional arrays are currently unsupported"));
        Assert.assertTrue(output.contains("error: unable to generate the binding function 'unsupportedReturnType' of " +
                "'org.ballerinalang.bindgen.MethodsTestResource': multidimensional arrays are currently unsupported"));
    }

    @Test(description = "Test if the correct error is given for a failure in the project loading")
    public void testFailedProjectLoad() throws IOException {
        String projectDir = Paths.get(testResources.toString(), "balProject").toString();
        String[] args = {"-o=" + projectDir, "java.lang.Object"};
        BindgenCommand bindgenCommand = new BindgenCommand(printStream, printStream, false);
        new CommandLine(bindgenCommand).parseArgs(args);

        File file = new File(Paths.get(projectDir, "Ballerina.toml").toString());
        if (file.exists()) {
            boolean success = file.setReadable(false);
            if (success) {
                bindgenCommand.execute();
                String output = readOutput(true);
                file.setReadable(true);
                Assert.assertTrue(output.contains("error: unable to load the project ["));
                Assert.assertTrue(output.contains("Ballerina.toml' does not have read permissions"));
            }
        }
    }

    @Test(description = "Test if the correct error is given for a failure in the writing bal files")
    public void testFileWriteFailure() throws IOException {
        String projectDir = Paths.get(testResources.toString(), "balProject", "tests").toString();
        String[] args = {"-o=" + projectDir, "java.lang.Object"};
        BindgenCommand bindgenCommand = new BindgenCommand(printStream, printStream, false);
        new CommandLine(bindgenCommand).parseArgs(args);

        File file = new File(projectDir);
        if (file.exists()) {
            boolean success = file.setWritable(false);
            if (success) {
                bindgenCommand.execute();
                String output = readOutput(true);
                file.setWritable(true);
                Assert.assertTrue(output.contains("error: unable to create the file:"));
                Assert.assertTrue(output.contains("Object.bal (Permission denied)"));
            }
        }
    }

    @Test(description = "Test to make sure complete class implementations are not replaced by empty bindings")
    public void testOverwriting() throws IOException {
        String projectDir = Paths.get(testResources.toString(), "balProject").toString();
        String[] args = {"-o" + projectDir, "java.io.FileInputStream"};

        BindgenCommand bindgenCommand = new BindgenCommand(printStream, printStream, false);
        new CommandLine(bindgenCommand).parseArgs(args);

        bindgenCommand.execute();
        String balFileContent = Files.readString(Paths.get(projectDir, "File.bal"));
        Assert.assertFalse(balFileContent.contains("This is an empty Ballerina class autogenerated to represent " +
                "the `java.io.File` Java class."));
    }

    @AfterClass
    public void cleanup() throws IOException {
        super.cleanup();
    }

    static class Copy extends SimpleFileVisitor<Path> {

        private Path fromPath;
        private Path toPath;
        private StandardCopyOption copyOption;

        private Copy(Path fromPath, Path toPath, StandardCopyOption copyOption) {
            this.fromPath = fromPath;
            this.toPath = toPath;
            this.copyOption = copyOption;
        }

        private Copy(Path fromPath, Path toPath) {
            this(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException {
            Path targetPath = toPath.resolve(fromPath.relativize(dir).toString());
            if (!Files.exists(targetPath)) {
                Files.createDirectory(targetPath);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
            Files.copy(file, toPath.resolve(fromPath.relativize(file).toString()), copyOption);
            return FileVisitResult.CONTINUE;
        }
    }
}
