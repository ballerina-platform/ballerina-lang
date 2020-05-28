/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.io.FileUtils;
import org.ballerinalang.bindgen.command.BindgenCommand;
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
 * Tests associated with the maven support in Bindgen tool.
 *
 * @since 1.2.5
 */
public class MavenSupportTest extends CommandTest {

    private Path testResources;

    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        try {
            this.testResources = super.tmpDir.resolve("build-test-resources");
            URI testResourcesURI = Objects.requireNonNull(getClass().getClassLoader()
                    .getResource("mvn-test-resources")).toURI();
            Files.walkFileTree(Paths.get(testResourcesURI), new MavenSupportTest.Copy(Paths.get(testResourcesURI),
                    this.testResources));
        } catch (URISyntaxException e) {
            Assert.fail("error loading resources");
        }
    }

    @Test(description = "Test the maven support in the Bindgen tool inside a project")
    public void testBindgenMvnCmd() throws IOException {
        String projectDir = Paths.get(testResources.toString(), "balProject").toString();
        String moduleDir = Paths.get(projectDir, "src", "balModule1").toString();
        Path mavenRepoPath = Paths.get(projectDir, "target", "platform-libs");
        String jarName = "snakeyaml-1.25.jar";
        String[] args = {"-mvn=org.yaml:snakeyaml:1.25", "-o=" + moduleDir, "org.yaml.snakeyaml.Yaml"};
        BindgenCommand bindgenCommand = new BindgenCommand(printStream, printStream);
        new CommandLine(bindgenCommand).parseArgs(args);

        bindgenCommand.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("Ballerina project detected at:"));
        Assert.assertTrue(output.contains("Resolving maven dependencies..."));
        Assert.assertTrue(output.contains("Following jars were added to the classpath"));
        Assert.assertTrue(output.contains("snakeyaml-1.25.jar"));
        Assert.assertTrue(isJarAvailable(mavenRepoPath, jarName));
    }

    @Test(description = "Test the maven support in the Bindgen tool to see if the Ballerina.toml is updated")
    public void testBindgenMvnToml() throws IOException {
        String projectDir = Paths.get(testResources.toString(), "balProject").toString();
        String moduleDir = Paths.get(projectDir, "src", "balModule1").toString();
        String[] args = {"-mvn=commons-logging:commons-logging:1.1.1", "-o=" + moduleDir, "org.yaml.snakeyaml.Yaml"};
        BindgenCommand bindgenCommand = new BindgenCommand(printStream, printStream);
        new CommandLine(bindgenCommand).parseArgs(args);

        bindgenCommand.execute();
        Assert.assertTrue(isTomlUpdated(Paths.get(projectDir, "Ballerina.toml").toString(),
                Paths.get(testResources.toString(), "Ballerina.toml").toString()));
    }

    private boolean isTomlUpdated(String updated, String expected) throws IOException {
        File updatedToml = new File(updated);
        File expectedToml = new File(expected);
        return FileUtils.contentEqualsIgnoreEOL(updatedToml, expectedToml, null);
    }

    private boolean isJarAvailable(Path directory, String jarName) {
        File folder = new File(directory.toString());
        File[] matchingFiles = folder.listFiles((dir, name) -> name.equals(jarName));
        return matchingFiles != null;
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
