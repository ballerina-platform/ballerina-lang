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
 * @since 2.0.0
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
        String[] args = {"-mvn=org.yaml:snakeyaml:1.25", "-o=" + projectDir, "org.yaml.snakeyaml.Yaml"};

        // Platform libraries specified through maven dependencies should be automatically resolved.
        // Explicitly add a jar to test the platform libraries specified as a path.
        MavenResolver mavenResolver = new MavenResolver(projectDir);
        mavenResolver.resolve("log4j", "log4j", "1.2.17", false);

        BindgenCommand bindgenCommand = new BindgenCommand(printStream, printStream);
        new CommandLine(bindgenCommand).parseArgs(args);

        bindgenCommand.execute();
        String output = readOutput(true);
        Assert.assertFalse(output.contains("Failed to add the following to classpath:"));
    }

    @Test(description = "Test if the correct error is given for incorrect classpaths")
    public void testIncorrectClasspath() throws IOException {
        String projectDir = Paths.get(testResources.toString(), "balProject").toString();
        String[] args = {"-cp=./incorrect.jar", "-o=" + projectDir, "java.lang.Object"};

        BindgenCommand bindgenCommand = new BindgenCommand(printStream, printStream);
        new CommandLine(bindgenCommand).parseArgs(args);

        bindgenCommand.execute();
        String output = readOutput(false);
        Assert.assertTrue(output.contains("Failed to add the following to classpath:" + System.lineSeparator() +
                "\t./incorrect.jar"));
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
