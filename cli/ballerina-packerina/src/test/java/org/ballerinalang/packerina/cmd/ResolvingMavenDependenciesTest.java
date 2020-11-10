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
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Resolving Maven dependency tests.
 *
 * @since 2.0.0
 */
public class ResolvingMavenDependenciesTest extends CommandTest {
    private Path testResources;
    private String mavenArtifactVersion = "3.6.3";
    private String cassandraVersion = "0.8.3";

    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        try {
            this.testResources = super.tmpDir.resolve("maven-dependency-test-resources");
            String testResourcesPath = Paths.get("test-resources", "maven-dependency-project").toString();
            URI testResourcesURI = getClass().getClassLoader().getResource(testResourcesPath).toURI();
            Files.walkFileTree(Paths.get(testResourcesURI), new BuildCommandTest.Copy(Paths.get(testResourcesURI),
                    this.testResources));
        } catch (URISyntaxException e) {
            Assert.fail("error loading resources");
        }
    }

    @Test(description = "Test the project with Maven dependencies", enabled = false)
    public void testBuildWithMavenDependencies() {
        // valid source root path
        Path validBalFilePath = this.testResources.resolve("project-with-maven-dependencies");
        BuildCommand buildCommand = new BuildCommand(validBalFilePath, printStream, printStream, false, true);
        new CommandLine(buildCommand).parse("-a");
        buildCommand.execute();
        Path target = validBalFilePath.resolve(ProjectDirConstants.TARGET_DIR_NAME);
        Path platformLibsDir = target.resolve("platform-libs");
        Assert.assertTrue(Files.exists(target), "Check if target directory is created");
        Assert.assertTrue(Files.exists(platformLibsDir), "Check if platform-libs is created");

        //Assert maven dependencies are downloaded
        Assert.assertTrue(Files.exists(getJarPath(platformLibsDir.toString(), "org.apache.maven",
                "maven-artifact", mavenArtifactVersion)));
        Assert.assertTrue(Files.exists(getJarPath(platformLibsDir.toString(), "org.ballerinalang",
                "wso2-cassandra", cassandraVersion)));
    }

    @Test(description = "Test the project without listing the custom maven repos to [platform.repositories]",
            enabled = false)
    public void testWithoutCustomMavenDependencies() throws IOException {
        // valid source root path
        Path validBalFilePath = this.testResources.resolve("project-without-custom-maven-dependencies");
        BuildCommand buildCommand = new BuildCommand(validBalFilePath, printStream, printStream, false, true);
        new CommandLine(buildCommand).parse("-a");
        String exMsg = executeAndGetException(buildCommand);
        Assert.assertEquals(exMsg, "error: custom maven repository properties are not specified for " +
                "given platform repository.");
    }

    @Test(description = "Test the project with adding invalid maven dependencies",
            dependsOnMethods = {"testWithoutCustomMavenDependencies"}, enabled = false)
    public void testWithInvalidMavenDependencies() throws IOException {
        // valid source root path
        Path validBalFilePath = this.testResources.resolve("project-without-custom-maven-dependencies");
        String tomlContent = "[project]\n" +
                "org-name=\"foo\"\n" +
                "version=\"0.1.0\"\n\n" +
                "[platform]\n" +
                "target=\"java\"\n\n\t" +
                "[[platform.libraries]]\n\t" +
                "artifactId=\"maven-artifact\"\n\t" +
                "groupId=\"org.apche.maven\"\n\t" +
                "version=\"" + mavenArtifactVersion + "\"";
        Files.write(validBalFilePath.resolve("Ballerina.toml"), tomlContent.getBytes(),
                StandardOpenOption.TRUNCATE_EXISTING);
        BuildCommand buildCommand = new BuildCommand(validBalFilePath, printStream, printStream, false, true);
        new CommandLine(buildCommand).parse("-a");
        String exMsg = executeAndGetException(buildCommand);
        Assert.assertEquals(exMsg, "error: cannot resolve maven-artifact: Could not find artifact org.apche.maven:" +
                "maven-artifact:jar:" + mavenArtifactVersion + " in central (https://repo.maven.apache.org/maven2/)");
    }

    @Test(description = "Validate the maven dependencies", dependsOnMethods = {"testWithInvalidMavenDependencies"}
    , enabled = false)
    public void validateMavenDependencies() throws IOException {
        // valid source root path
        Path validBalFilePath = this.testResources.resolve("project-without-custom-maven-dependencies");
        String tomlContent = "[project]\n" +
                "org-name=\"foo\"\n" +
                "version=\"0.1.0\"\n\n" +
                "[platform]\n" +
                "target=\"java\"\n\n\t" +
                "[[platform.libraries]]\n\t" +
                "artifactId=\"maven-artifact\"\n\t" +
                "groupId=\"org.apache.maven\"\n\t";
        Files.write(validBalFilePath.resolve("Ballerina.toml"), tomlContent.getBytes(),
                StandardOpenOption.TRUNCATE_EXISTING);
        BuildCommand buildCommand = new BuildCommand(validBalFilePath, printStream, printStream, false, true);
        new CommandLine(buildCommand).parse("-a");
        String exMsg = executeAndGetException(buildCommand);
        Assert.assertEquals(exMsg, "error: artifact-id, group-id, and version should be specified to resolve " +
                "the maven dependency.");
    }

    public static Path getJarPath(String targetPath, String groupId, String artifactId, String version) {
        return Paths.get(targetPath, groupId.replace('.', File.separatorChar),
                artifactId, version, artifactId + "-" + version + ".jar");
    }
}
