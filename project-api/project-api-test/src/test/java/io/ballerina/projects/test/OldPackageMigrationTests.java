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
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;

import static io.ballerina.projects.test.TestUtils.assertTomlFilesEquals;
import static io.ballerina.projects.test.TestUtils.replaceDistributionVersionOfDependenciesToml;
import static io.ballerina.projects.util.ProjectConstants.BUILD_FILE;
import static io.ballerina.projects.util.ProjectConstants.DEPENDENCIES_TOML;
import static io.ballerina.projects.util.ProjectConstants.DOT;
import static io.ballerina.projects.util.ProjectConstants.RESOURCE_DIR_NAME;
import static io.ballerina.projects.util.ProjectConstants.TARGET_DIR_NAME;

/**
 * Contains cases to test old package migration logic.
 *
 * @since 2.0.0
 */
public class OldPackageMigrationTests extends BaseTest {

    private static final Path RESOURCE_DIRECTORY =
            Path.of("src/test/resources/projects_for_resolution_tests").toAbsolutePath();
    private static Path tempResourceDir;

    @BeforeClass
    public void setup() throws IOException {
        // copy the resource directory to a temp directory
        tempResourceDir = Files.createTempDirectory("project-api-test");
        FileUtils.copyDirectory(RESOURCE_DIRECTORY.toFile(), tempResourceDir.toFile());
    }

    @Test
    public void testOldPackage(ITestContext ctx) throws IOException {
        // package_f --> package_d
        // package_d --> package_b --> package_c
        // package_d --> package_e
        BCompileUtil.compileAndCacheBala("projects_for_resolution_tests/package_d");
        Path packagePath = tempResourceDir.resolve("package_f");
        ctx.getCurrentXmlTest().addParameter("packagePath", String.valueOf(packagePath));

        deleteBuildFileAndDependenciesToml(packagePath);

        // Create & write old Dependencies.toml content
        Files.createFile(packagePath.resolve(DEPENDENCIES_TOML));
        String oldDepsToml = Files.readString(packagePath.resolve(RESOURCE_DIR_NAME).resolve("OldDependencies.toml"));
        Files.write(packagePath.resolve(DEPENDENCIES_TOML), Collections.singleton(oldDepsToml));

        // Build the project
        BuildProject buildProject = TestUtils.loadBuildProject(packagePath);
        buildProject.save();
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();

        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        diagnosticResult.diagnostics().forEach(OUT::println);
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 1, "Unexpected compilation diagnostics");
        Iterator<Diagnostic> diagnosticIterator = diagnosticResult.diagnostics().iterator();
        Assert.assertEquals(diagnosticIterator.next().message(),
                "Detected an old version of Dependencies.toml file. This will be updated to v2 format.");
        // Check updating to v2 the warning
        assertTomlFilesEquals(packagePath.resolve(DEPENDENCIES_TOML),
                packagePath.resolve(RESOURCE_DIR_NAME).resolve("UpdatedDependencies.toml"));
    }

    @Test
    public void testOldPackageWithLocalDependencies(ITestContext ctx) throws IOException {
        // package_f --> package_d
        // package_d --> package_b --> package_c
        // package_d --> package_e
        // package_b and package_d are local packages
        Path packageBPath = tempResourceDir.resolve("package_b");
        replaceDistributionVersionOfDependenciesToml(packageBPath, RepoUtils.getBallerinaShortVersion());

        cacheDependencyToLocalRepository(packageBPath);
        cacheDependencyToLocalRepository(tempResourceDir.resolve("package_d"));
        Path packagePath = tempResourceDir.resolve("package_f_old_local");
        ctx.getCurrentXmlTest().addParameter("packagePath", String.valueOf(packagePath));

        deleteBuildFileAndDependenciesToml(packagePath);

        // Create & write old Dependencies.toml content
        Files.createFile(packagePath.resolve(DEPENDENCIES_TOML));
        String oldDepsToml = Files.readString(packagePath.resolve(RESOURCE_DIR_NAME).resolve("OldDependencies.toml"));
        Files.write(packagePath.resolve(DEPENDENCIES_TOML), Collections.singleton(oldDepsToml));

        // Build the project
        BuildProject buildProject = TestUtils.loadBuildProject(packagePath);
        buildProject.save();
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();

        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        diagnosticResult.diagnostics().forEach(OUT::println);
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 3, "Unexpected compilation diagnostics");
        Iterator<Diagnostic> diagnosticIterator = diagnosticResult.diagnostics().iterator();
        // Check updating to v2 the warning
        Assert.assertEquals(diagnosticIterator.next().message(),
                "Detected an old version of Dependencies.toml file. This will be updated to v2 format.");
        // Check detected local dependency declarations warnings
        Assert.assertEquals(diagnosticIterator.next().message(),
                """
                        Detected local dependency declarations in Dependencies.toml file. \
                        Add them to Ballerina.toml using following syntax:
                        [[dependency]]
                        org = "samjs"
                        name = "package_b"
                        version = "0.1.0"
                        repository = "local"
                        """);
        Assert.assertEquals(diagnosticIterator.next().message(),
                """
                        Detected local dependency declarations in Dependencies.toml file. \
                        Add them to Ballerina.toml using following syntax:
                        [[dependency]]
                        org = "samjs"
                        name = "package_d"
                        version = "0.1.0"
                        repository = "local"
                        """);
        // Check updated Dependencies.toml
        assertTomlFilesEquals(packagePath.resolve(DEPENDENCIES_TOML),
                packagePath.resolve(RESOURCE_DIR_NAME).resolve("UpdatedDependencies.toml"));
    }

    @AfterMethod
    private void cleanUp(ITestContext ctx) throws IOException {
        Path packagePath = Path.of(ctx.getCurrentXmlTest().getParameter("packagePath"));
        deleteBuildFileAndDependenciesToml(packagePath);
    }

    private void deleteBuildFileAndDependenciesToml(Path packagePath) throws IOException {
        // Delete build file if exists
        Files.deleteIfExists(packagePath.resolve(TARGET_DIR_NAME).resolve(BUILD_FILE));
        // Delete Dependencies.toml file if exists
        Files.deleteIfExists(packagePath.resolve(DEPENDENCIES_TOML));
    }

    private String getDistributionVersionForDiagnostics() {
        String[] versionParts = RepoUtils.getBallerinaShortVersion().split("\\.");
        int minor = Integer.parseInt(versionParts[1]);
        int patch = Integer.parseInt(versionParts[2].split("-")[0]);
        String versionForDiagnostic = String.valueOf(minor);
        if (patch != 0) {
            versionForDiagnostic += DOT + patch;
        }
        return versionForDiagnostic;
    }
}
