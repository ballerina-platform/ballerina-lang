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
package io.ballerina.projects.test;

import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.projects.test.TestUtils.deleteDirectory;
import static io.ballerina.projects.test.TestUtils.readFileAsString;
import static io.ballerina.projects.util.ProjectConstants.BUILD_FILE;
import static io.ballerina.projects.util.ProjectConstants.DEPENDENCIES_TOML;
import static io.ballerina.projects.util.ProjectConstants.DIST_CACHE_DIRECTORY;
import static io.ballerina.projects.util.ProjectConstants.RESOURCE_DIR_NAME;
import static io.ballerina.projects.util.ProjectConstants.TARGET_DIR_NAME;

/**
 * Contains cases to test package resolution logic.
 *
 * @since 2.0.0
 */
public class PackageResolutionAdvancedTests extends BaseTest {

    private static final Path RESOURCE_DIRECTORY = Paths.get(
            "src/test/resources/projects_for_adv_resolution_tests").toAbsolutePath();
    private static final Path testBuildDirectory = Paths.get("build").toAbsolutePath();
    private static final Path testDistCacheDirectory = testBuildDirectory.resolve(DIST_CACHE_DIRECTORY);
    Path customUserHome = Paths.get("build", "user-home");
    Environment environment = EnvironmentBuilder.getBuilder().setUserHome(customUserHome).build();
    ProjectEnvironmentBuilder projectEnvironmentBuilder = ProjectEnvironmentBuilder.getBuilder(environment);

    @BeforeMethod
    public void setUp() {
        System.setProperty("LANG_REPO_BUILD", "False");
    }

    @Test(description = "A new patch and minor version of a transitive has been released to central")
    public void testCase0001(ITestContext ctx) throws IOException {
        // package_c --> package_b
        // package_b --> package_a
        // Cache package_a to central
        cacheDependencyToCentralRepository(RESOURCE_DIRECTORY.resolve("package_a_1_0_0"));
        // Cache package_b
        BCompileUtil.compileAndCacheBala("projects_for_adv_resolution_tests/package_b_1_0_0",
                testDistCacheDirectory, projectEnvironmentBuilder);

        // First build package_c
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_c_1_0_0");
        ctx.getCurrentXmlTest().addParameter("packagePath", String.valueOf(projectDirPath));
        BuildProject buildProject = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        buildProject.save();
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();
        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        diagnosticResult.errors().forEach(OUT::println);
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 0, "Unexpected compilation diagnostics");
        // Compare Dependencies.toml file
        // package_a ---> 1.0.0
        Assert.assertEquals(readFileAsString(projectDirPath.resolve(DEPENDENCIES_TOML)), readFileAsString(
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Build1Dependencies.toml")));

        // Cache package_a patch version 1.0.2 to central
        cacheDependencyToCentralRepository(RESOURCE_DIRECTORY.resolve("package_a_1_0_2"));
        // Cache package_a minor version 1.1.0 to central
        cacheDependencyToCentralRepository(RESOURCE_DIRECTORY.resolve("package_a_1_1_0"));

        // Build package_c again w/o deleting Dependencies.toml
        BuildProject buildProjectAgain = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        buildProjectAgain.save();
        PackageCompilation compilationAgain = buildProjectAgain.currentPackage().getCompilation();
        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResultAgain = compilationAgain.diagnosticResult();
        diagnosticResultAgain.errors().forEach(OUT::println);
        Assert.assertEquals(diagnosticResultAgain.diagnosticCount(), 0, "Unexpected compilation diagnostics");
        // Compare Dependencies.toml file
        // package_a ---> 1.0.0
        Assert.assertEquals(readFileAsString(projectDirPath.resolve(DEPENDENCIES_TOML)), readFileAsString(
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Build1Dependencies.toml")));

        // Build package_c again after deleting Dependencies.toml
        deleteDependenciesTomlAndBuildFile(projectDirPath);
        BuildProject buildProject3 = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        buildProject3.save();
        PackageCompilation compilation3 = buildProject3.currentPackage().getCompilation();
        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult3 = compilation3.diagnosticResult();
        diagnosticResult3.errors().forEach(OUT::println);
        Assert.assertEquals(diagnosticResult3.diagnosticCount(), 0, "Unexpected compilation diagnostics");
        // Compare Dependencies.toml file
        // package_a ---> 1.0.2 patch version
        Assert.assertEquals(readFileAsString(projectDirPath.resolve(DEPENDENCIES_TOML)), readFileAsString(
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Build2Dependencies.toml")));
    }

    @AfterMethod
    private void afterMethod(ITestContext ctx) throws IOException {
        Path packagePath = Path.of(ctx.getCurrentXmlTest().getParameter("packagePath"));
        deleteDependenciesTomlAndBuildFile(packagePath);
    }

    @AfterClass
    public void afterClass() {
        deleteDirectory(testBuildDirectory.resolve("user-home").toFile());
    }

    private static void deleteDependenciesTomlAndBuildFile(Path packagePath) throws IOException {
        Files.deleteIfExists(packagePath.resolve(DEPENDENCIES_TOML));
        Files.deleteIfExists(packagePath.resolve(TARGET_DIR_NAME).resolve(BUILD_FILE));
    }

    private static void updateFileToken(Path filePath, String guessToken, String actualToken) throws IOException {
        Stream<String> lines = Files.lines(filePath);
        List<String> replaced = lines.map(line -> line.replaceAll(guessToken, actualToken))
                .collect(Collectors.toList());
        Files.write(filePath, replaced);
        lines.close();
    }
}
