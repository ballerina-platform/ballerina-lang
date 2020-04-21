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

package org.ballerinalang.packerina.cmd;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.programfile.ProgramFileConstants;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_PKG_BINARY_EXT;

/**
 * Dependency scope tests.
 *
 * @since 1.2.x
 */
public class DependencyScopeTest extends CommandTest {

    private Path testResources;
    private URI testResourcesURI;

    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        try {
            this.testResources = super.tmpDir.resolve("scope-test-resources");
            testResourcesURI = getClass().getClassLoader().getResource("test-resources").toURI();
            Path storedJarDependencyProject = Paths.get(testResourcesURI).resolve("stored-jar-dependency-project");
            Files.walkFileTree(storedJarDependencyProject, new BuildCommandTest.Copy(storedJarDependencyProject,
                    this.testResources));
        } catch (URISyntaxException e) {
            Assert.fail("error loading resources");
        }
    }

    @Test(description = "Test if platform libs are packed with the balo based on the scope.")
    public void testBuildCommandWithStoredJarDependency() throws IOException {
        String baloFileName = "mymodule-" + ProgramFileConstants.IMPLEMENTATION_VERSION + "-java8-0.1.0"
                + BLANG_COMPILED_PKG_BINARY_EXT;
        Path balo = this.testResources.resolve(ProjectDirConstants.TARGET_DIR_NAME).
                resolve(ProjectDirConstants.TARGET_BALO_DIRECTORY);
        File baloFile = new File(balo.toString() + File.separator + baloFileName);
        File baloZipFile = new File(balo.toString() + File.separator +
                baloFileName.concat(".zip"));
        Path scopeToml = Paths.get(testResourcesURI).resolve("scope-toml");
        String storedJarPath = "platform-libs/storedJar.jar";

        // Build the project
        String[] compileArgs = {"--all", "--skip-tests"};
        BuildCommand buildCommand = new BuildCommand(this.testResources, printStream, printStream,
                false, true);
        new CommandLine(buildCommand).parse(compileArgs);
        // default scope
        buildCommand.execute();
        Assert.assertTrue(Files.exists(balo), "Check if balo directory exists");

        // Check whether dependency jars getting packed to balo
        Assert.assertTrue(renameFile(baloFile, baloZipFile));
        Assert.assertTrue(isJarExists(baloZipFile, storedJarPath));

        // package scope
        copy(scopeToml.resolve("case1").resolve(ProjectDirConstants.MANIFEST_FILE_NAME),
                this.testResources.resolve(ProjectDirConstants.MANIFEST_FILE_NAME));
        buildCommand.execute();
        Assert.assertTrue(Files.exists(balo), "Check if balo directory exists");

        // Check whether dependency jars getting packed to balo
        Assert.assertTrue(renameFile(baloFile, baloZipFile));
        Assert.assertTrue(isJarExists(baloZipFile, storedJarPath));

        // package scope
        copy(scopeToml.resolve("case2").resolve(ProjectDirConstants.MANIFEST_FILE_NAME),
                this.testResources.resolve(ProjectDirConstants.MANIFEST_FILE_NAME));
        buildCommand.execute();
        Assert.assertTrue(Files.exists(balo), "Check if balo directory exists");

        // Check whether dependency jars getting packed to balo
        Assert.assertTrue(renameFile(baloFile, baloZipFile));
        Assert.assertFalse(isJarExists(baloZipFile, storedJarPath));

        // compile scope
        copy(scopeToml.resolve("case3").resolve(ProjectDirConstants.MANIFEST_FILE_NAME),
                this.testResources.resolve(ProjectDirConstants.MANIFEST_FILE_NAME));
        buildCommand.execute();
        Assert.assertTrue(Files.exists(balo), "Check if balo directory exists");

        // Check whether dependency jars getting packed to balo
        Assert.assertTrue(renameFile(baloFile, baloZipFile));
        Assert.assertFalse(isJarExists(baloZipFile, storedJarPath));
    }

    private boolean renameFile(File oldName, File newName) {
        return oldName.renameTo(newName);
    }

    private boolean isJarExists (File file, String jarEntry) throws IOException {
        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                if (entries.nextElement().getName().equals(jarEntry)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void copy(Path source, Path dest) throws IOException {
        Files.copy(source, dest, REPLACE_EXISTING);
    }
}
