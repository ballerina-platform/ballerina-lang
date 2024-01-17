/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static io.ballerina.projects.test.TestUtils.assertTomlFilesEquals;
import static io.ballerina.projects.test.TestUtils.replaceDistributionVersionOfDependenciesToml;
import static io.ballerina.projects.util.ProjectConstants.BUILD_FILE;
import static io.ballerina.projects.util.ProjectConstants.DEPENDENCIES_TOML;
import static io.ballerina.projects.util.ProjectConstants.DIST_CACHE_DIRECTORY;
import static io.ballerina.projects.util.ProjectConstants.DOT;
import static io.ballerina.projects.util.ProjectConstants.RESOURCE_DIR_NAME;
import static io.ballerina.projects.util.ProjectConstants.TARGET_DIR_NAME;

/**
 * Contains integrations test cases to test package resolution logic.
 *
 * @since 2201.0.1
 */
public class PackageResolutionIntegrationTests extends BaseTest {

    private static final Path RESOURCE_DIRECTORY = Paths.get(
            "src/test/resources/projects_for_resolution_integration_tests").toAbsolutePath();
    private static Path tempResourceDir;
    private static Path distVersionResourcesDirectory;
    private static final Path testBuildDirectory = Paths.get("build").toAbsolutePath();
    private static final Path testDistCacheDirectory = testBuildDirectory.resolve(DIST_CACHE_DIRECTORY);
    Path customUserHome = Paths.get("build", "user-home");
    Environment environment = EnvironmentBuilder.getBuilder().setUserHome(customUserHome).build();
    ProjectEnvironmentBuilder projectEnvironmentBuilder = ProjectEnvironmentBuilder.getBuilder(environment);

    @BeforeClass(alwaysRun = true)
    public void setup() throws IOException {
        tempResourceDir = Files.createTempDirectory("project-api-test");
        FileUtils.copyDirectory(RESOURCE_DIRECTORY.toFile(), tempResourceDir.toFile());
        distVersionResourcesDirectory = Paths.get(String.valueOf(tempResourceDir))
                .resolve("projects_for_distribution_version_increment_tests");
    }

    @Test(description = "A new patch and minor version of a transitive has been released to central")
    public void testCase0001(ITestContext ctx) throws IOException {
        // package_c --> package_b
        // package_b --> package_a
        // Cache package_a to central
        cacheDependencyToCentralRepository(tempResourceDir.resolve("package_a_1_0_0"));
        // Cache package_b
        BCompileUtil.compileAndCacheBala(tempResourceDir.resolve("package_b_1_0_0"),
                testDistCacheDirectory, projectEnvironmentBuilder);

        // 1. First build package_c
        Path projectDirPath = tempResourceDir.resolve("package_c_1_0_0");
        ctx.getCurrentXmlTest().addParameter("packagePath", String.valueOf(projectDirPath));
        ctx.getCurrentXmlTest().addParameter("deleteDependenciesToml", "true");

        BuildProject buildProject = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        buildProject.save();
        failIfDiagnosticsExists(buildProject);
        // Compare Dependencies.toml file
        // package_a ---> 1.0.0
        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies-0001-1.toml"));

        // Cache package_a patch version 1.0.2 to central
        cacheDependencyToCentralRepository(tempResourceDir.resolve("package_a_1_0_2"));
        // Cache package_a minor version 1.1.0 to central
        cacheDependencyToCentralRepository(tempResourceDir.resolve("package_a_1_1_0"));

        // 2. Build package_c again w/o deleting Dependencies.toml and build file
        BuildProject buildProjectAgain = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        buildProjectAgain.save();
        failIfDiagnosticsExists(buildProjectAgain);
        // Compare Dependencies.toml file
        // package_a ---> 1.0.0
        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies-0001-1.toml"));

        // 3. Build package_c w/o deleting Dependencies.toml, after deleting build file and setting sticky == false
        deleteBuildFile(projectDirPath);
        BuildProject buildProject3 = BuildProject.load(projectEnvironmentBuilder, projectDirPath,
                BuildOptions.builder().setSticky(false).build());
        buildProject3.save();
        failIfDiagnosticsExists(buildProject3);
        // Compare Dependencies.toml file
        // package_a ---> 1.0.2
        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies-0001-2.toml"));

        // 4. Build package_c again after deleting Dependencies.toml and build file
        deleteDependenciesTomlAndBuildFile(projectDirPath);
        BuildProject buildProject4 = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        buildProject4.save();
        failIfDiagnosticsExists(buildProject4);
        // Compare Dependencies.toml file
        // package_a ---> 1.0.2 patch version
        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies-0001-2.toml"));

        // 5. Build package_c again after deleting Dependencies.toml and build file, and setting sticky == false
        deleteDependenciesTomlAndBuildFile(projectDirPath);
        BuildProject buildProject5 = BuildProject.load(projectEnvironmentBuilder, projectDirPath,
                BuildOptions.builder().setSticky(false).build());
        buildProject5.save();
        // Compare Dependencies.toml file
        // package_a ---> 1.1.0 minor version
        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies-0001-3.toml"));
    }

    @Test(description = "Adding of a new import which is already there as a transitive in the graph " +
            "with an old version", dependsOnMethods = "testCase0001")
    public void testCase0002(ITestContext ctx) throws IOException {
        // package_c --> package_b 1.0.0
        // package_b --> package_a 1.0.0, 1.0.2, 1.1.0
        // Add package_a major version to central
        cacheDependencyToCentralRepository(tempResourceDir.resolve("package_a_2_0_0"));
        // package_a 1.0.0, 1.0.2, 1.1.0, 2.0.0
        // 1. Build package_c after adding import to package_a
        Path projectDirPath = tempResourceDir.resolve("package_c_1_0_0_new_import");
        // Add Dependencies.toml
        // package_a ---> 1.0.0
        Files.copy(projectDirPath.resolve(RESOURCE_DIR_NAME).resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(DEPENDENCIES_TOML));
        replaceDistributionVersionOfDependenciesToml(projectDirPath, RepoUtils.getBallerinaShortVersion());
        ctx.getCurrentXmlTest().addParameter("packagePath", String.valueOf(projectDirPath));
        ctx.getCurrentXmlTest().addParameter("deleteDependenciesToml", "true");

        BuildProject buildProject = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        buildProject.save();
        failIfDiagnosticsExists(buildProject);
        // Compare Dependencies.toml file
        // package_a ---> 1.1.0
        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies-0002.toml"));
    }

    @Test(description = "Remove existing import which is also a transitive dependency from another " +
            "import", dependsOnMethods = "testCase0002")
    public void testCase0003(ITestContext ctx) throws IOException {
        // package_c --> package_b 1.0.0
        // package_b --> package_a 1.0.0, 1.0.2, 1.1.0
        // 1. Build package_c after adding import to package_a
        Path projectDirPath = tempResourceDir.resolve("package_c_1_0_0_remove_import");
        // Add Dependencies.toml
        // package_a ---> 1.1.0
        Files.copy(projectDirPath.resolve(RESOURCE_DIR_NAME).resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(DEPENDENCIES_TOML));
        replaceDistributionVersionOfDependenciesToml(projectDirPath, RepoUtils.getBallerinaShortVersion());
        ctx.getCurrentXmlTest().addParameter("packagePath", String.valueOf(projectDirPath));
        ctx.getCurrentXmlTest().addParameter("deleteDependenciesToml", "true");

        BuildProject buildProject = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        buildProject.save();
        failIfDiagnosticsExists(buildProject);
        // Compare Dependencies.toml file
        // package_a ---> 1.1.0
        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies-0003-1.toml"));

        // 2. Build package_c w/o deleting Dependencies.toml, after deleting build file and setting sticky == false
        deleteBuildFile(projectDirPath);
        BuildProject buildProject3 = BuildProject.load(projectEnvironmentBuilder, projectDirPath,
                BuildOptions.builder().setSticky(false).build());
        buildProject3.save();
        failIfDiagnosticsExists(buildProject3);
        // Compare Dependencies.toml file
        // package_a ---> 1.1.0
        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies-0003-1.toml"));

        // 3. Build package_c again after deleting Dependencies.toml and build file
        deleteDependenciesTomlAndBuildFile(projectDirPath);
        BuildProject buildProject4 = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        buildProject4.save();
        failIfDiagnosticsExists(buildProject4);
        // Compare Dependencies.toml file
        // package_a ---> 1.0.2 version
        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies-0003-2.toml"));

        // 4. Build package_c again after deleting Dependencies.toml and build file, and setting sticky == false
        deleteDependenciesTomlAndBuildFile(projectDirPath);
        BuildProject buildProject5 = BuildProject.load(projectEnvironmentBuilder, projectDirPath,
                BuildOptions.builder().setSticky(false).build());
        buildProject5.save();
        // Compare Dependencies.toml file
        // package_a ---> 1.1.0 patch version
        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies-0003-1.toml"));
    }

    @Test(description = "Package contains a built-in transitive dependency with a non-zero version")
    public void testCase0004(ITestContext ctx) throws IOException {
        // package_i --> package_h 1.0.0 --> ballerinai/package_g 1.0.0
        // Cache ballerinai/package_g
        BCompileUtil.compileAndCacheBala(tempResourceDir.resolve("package_g_1_0_0"),
                testDistCacheDirectory, projectEnvironmentBuilder);
        // Cache package_h to central
        cacheDependencyToCentralRepository(tempResourceDir.resolve("package_h_1_0_0"));

        // 1. Build package_i
        Path projectDirPath = tempResourceDir.resolve("package_i_1_0_0");
        ctx.getCurrentXmlTest().addParameter("packagePath", String.valueOf(projectDirPath));
        ctx.getCurrentXmlTest().addParameter("deleteDependenciesToml", "true");

        BuildProject buildProject = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        buildProject.save();
        failIfDiagnosticsExists(buildProject);
        // Compare Dependencies.toml file
        // package_h ---> 1.0.0
        // package_g ---> 0.0.0
        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies-0004-1.toml"));
    }

    @Test(description = "Package contains a built-in transitive dependency which has " +
            "its dependencies changed in the current dist", dependsOnMethods = "testCase0003")
    public void testCase0005(ITestContext ctx) throws IOException {
        // package_l            --> package_k 1.0.0
        // package_k            --> ballerinai/package_j 1.0.0
        // ballerinai/package_j --> package_d 1.0.0, package_e 2.0.0
        // Cache package_d
        BCompileUtil.compileAndCacheBala(tempResourceDir.resolve("package_d_1_0_0"), testDistCacheDirectory,
                projectEnvironmentBuilder);
        // Cache package_e
        BCompileUtil.compileAndCacheBala(tempResourceDir.resolve("package_e_2_0_0"), testDistCacheDirectory,
                projectEnvironmentBuilder);
        // Cache ballerinai/package_j
        BCompileUtil.compileAndCacheBala(tempResourceDir.resolve("package_j_1_0_0"), testDistCacheDirectory,
                projectEnvironmentBuilder);
        // Cache package_k
        BCompileUtil.compileAndCacheBala(tempResourceDir.resolve("package_k_1_0_0"), testDistCacheDirectory,
                projectEnvironmentBuilder);

        // 1. Build package_l
        Path projectDirPath = tempResourceDir.resolve("package_l_1_0_0");
        ctx.getCurrentXmlTest().addParameter("packagePath", String.valueOf(projectDirPath));
        ctx.getCurrentXmlTest().addParameter("deleteDependenciesToml", "true");

        BuildProject buildProject = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        buildProject.save();
        failIfDiagnosticsExists(buildProject);
        // Compare Dependencies.toml file
        // package_d            ---> 1.0.0
        // package_e            ---> 2.0.0
        // ballerinai/package_j ---> 0.0.0
        // package_k            ---> 1.0.0
        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies-0005-1.toml"));

        // 2. Build package_l after releasing new version of ballerinai/package_j
        // ballerinai/package_j --> package_dd 2.0.0, package_e 2.0.0
        // Cache package_dd
        BCompileUtil.compileAndCacheBala(tempResourceDir.resolve("package_dd_2_0_0"), testDistCacheDirectory,
                projectEnvironmentBuilder);
        // Cache ballerinai/package_j again
        BCompileUtil.compileAndCacheBala(tempResourceDir.resolve("package_j_2_0_0"), testDistCacheDirectory,
                projectEnvironmentBuilder);
        // Build package_l again
        BuildProject buildProject1 = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        buildProject1.save();
        failIfDiagnosticsExists(buildProject1);
        // Compare Dependencies.toml file
        // package_dd           ---> 2.0.0
        // package_e            ---> 2.0.0
        // ballerinai/package_j ---> 0.0.0
        // package_k            ---> 1.0.0
        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies-0005-2.toml"));
    }

    @Test(description = "Remove existing import which also is a dependency of a newer patch version " +
            "of another import", dependsOnMethods = "testCase0005")
    public void testCase0006(ITestContext ctx) throws IOException {
        // package_f --> package_d 1.0.0, package_e 2.0.0
        // 1. Build package_f
        Path projectDirPath = tempResourceDir.resolve("package_f_1_0_0");
        ctx.getCurrentXmlTest().addParameter("packagePath", String.valueOf(projectDirPath));
        ctx.getCurrentXmlTest().addParameter("deleteDependenciesToml", "true");

        BuildProject buildProject = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        buildProject.save();
        failIfDiagnosticsExists(buildProject);
        // Compare Dependencies.toml file
        // package_d ---> 1.0.0
        // package_e ---> 2.0.0
        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                        projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies-0006-1.toml"));

        // 2. Publish new patch version of package_e which has dependency on package_d to central
        cacheDependencyToCentralRepository(tempResourceDir.resolve("package_e_2_0_2"), projectEnvironmentBuilder);
        // Remove package_d import from package_f
        Path projectDirPath2 = tempResourceDir.resolve("package_f_1_0_0_remove_import_d");
        // Add Dependencies.toml
        // package_e ---> 2.0.0
        Files.copy(projectDirPath2.resolve(RESOURCE_DIR_NAME).resolve(DEPENDENCIES_TOML),
                projectDirPath2.resolve(DEPENDENCIES_TOML));
        replaceDistributionVersionOfDependenciesToml(projectDirPath2, RepoUtils.getBallerinaShortVersion());
        // Build package_f w/o deleting Dependencies.toml and build file
        // package_f       ---> package_e 2.0.0, 2.0.2
        // package_e 2.0.2 ---> package_d 1.0.0
        BuildProject buildProject2 = BuildProject.load(projectEnvironmentBuilder, projectDirPath2);
        buildProject2.save();
        failIfDiagnosticsExists(buildProject2);
        // Compare Dependencies.toml file
        // package_e ---> 2.0.0
        assertTomlFilesEquals(projectDirPath2.resolve(DEPENDENCIES_TOML),
                projectDirPath2.resolve(RESOURCE_DIR_NAME).resolve("Dependencies-0006-2.toml"));

        // 3. Build package_f after deleting build file and Dependencies.toml
        deleteDependenciesTomlAndBuildFile(projectDirPath2);
        BuildProject buildProject3 = BuildProject.load(projectEnvironmentBuilder, projectDirPath2);
        buildProject3.save();
        failIfDiagnosticsExists(buildProject3);
        // Compare Dependencies.toml file
        // package_e ---> 2.0.2
        assertTomlFilesEquals(projectDirPath2.resolve(DEPENDENCIES_TOML),
                projectDirPath2.resolve(RESOURCE_DIR_NAME).resolve("Dependencies-0006-3.toml"));

        // clean up project
        deleteDependenciesTomlAndBuildFile(projectDirPath2);
    }

    @Test(description = "An imported module has a new version pushed to central with a submodule")
    public void testCase0007(ITestContext ctx) throws IOException {
        Path projectDirPath = tempResourceDir.resolve("project_s");
        ctx.getCurrentXmlTest().addParameter("packagePath", String.valueOf(projectDirPath));
        ctx.getCurrentXmlTest().addParameter("deleteDependenciesToml", "true");

        Path cacheDir = testBuildDirectory.resolve("repo").resolve("bala").resolve("adv_res").resolve("package_r");
        deleteDirectory(cacheDir);

        // project --> package_protobuf:0.6.0
        BCompileUtil.compileAndCacheBala(tempResourceDir.resolve("package_r_0_6_0"), testDistCacheDirectory,
                projectEnvironmentBuilder);
        BuildProject buildProject1 = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        buildProject1.save();
        failIfDiagnosticsExists(buildProject1);

        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies-0007-1.toml"));

        // project --> package_protobuf:0.7.0
        BCompileUtil.compileAndCacheBala(tempResourceDir.resolve("package_r_0_6_1"), testDistCacheDirectory,
                projectEnvironmentBuilder);
        // Sticky
        deleteBuildFile(projectDirPath);
        BuildProject buildProject2 = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        buildProject2.save();
        failIfDiagnosticsExists(buildProject2);

        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies-0007-1.toml"));

        // No Sticky
        deleteBuildFile(projectDirPath);
        BuildProject buildProject3 = BuildProject.load(projectEnvironmentBuilder, projectDirPath,
                BuildOptions.builder().setSticky(false).build());
        buildProject3.save();
        failIfDiagnosticsExists(buildProject3);

        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies_NoSticky.toml"));
    }

    @Test(description = "Test package with similar name to module of another package")
    public void testCase0008(ITestContext ctx) throws IOException {
        Path projectDirPath = tempResourceDir.resolve("project_t");
        ctx.getCurrentXmlTest().addParameter("packagePath", String.valueOf(projectDirPath));
        ctx.getCurrentXmlTest().addParameter("deleteDependenciesToml", "true");

        // project --> package_protobuf:0.6.0
        BCompileUtil.compileAndCacheBala(tempResourceDir.resolve("package_r_0_6_0"), testDistCacheDirectory,
                projectEnvironmentBuilder);
        BuildProject buildProject1 = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        buildProject1.save();
        failIfDiagnosticsExists(buildProject1);

        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies.toml"));

        // project --> package_protobuf.types.timestamp
        BCompileUtil.compileAndCacheBala(tempResourceDir.resolve("package_r.types.timestamp_0_6_0"),
                testDistCacheDirectory, projectEnvironmentBuilder);
        Path projectDirPath2 = tempResourceDir.resolve("project_t_with_import");
        BuildProject buildProject2 = BuildProject.load(projectEnvironmentBuilder, projectDirPath2);
        buildProject2.save();
        failIfDiagnosticsExists(buildProject2);

        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies.toml"));
    }

    @Test(description = "A newer pre-release version of a dependency has been released to central")
    public void testCase0010(ITestContext ctx) throws IOException {
        Path projectDirPath = tempResourceDir.resolve("project_o");
        ctx.getCurrentXmlTest().addParameter("packagePath", String.valueOf(projectDirPath));
        ctx.getCurrentXmlTest().addParameter("deleteDependenciesToml", "true");

        // package_n:2.0.0 ---> package_m:1.0.1
        // project_o ---> package_n

        // User has imported package_n:2.0.0 which depends on package_m:1.0.1
        // Cache package_m to central
        cacheDependencyToCentralRepository(tempResourceDir.resolve("package_m_1_0_1"));
        // Cache package_n
        BCompileUtil.compileAndCacheBala(tempResourceDir.resolve("package_n_2_0_0"), testDistCacheDirectory,
                projectEnvironmentBuilder);

        //1. Build project_o
        BuildProject buildProject1 = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        buildProject1.save();
        failIfDiagnosticsExists(buildProject1);

        // Compare dependencies.toml
        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies.toml"));

        // User caches package_m:1_3_0-beta.1
        cacheDependencyToCentralRepository(tempResourceDir.resolve("package_m_1_3_0_beta"));
        // User caches package_m:1_3_0-beta.1 to local repository
        cacheDependencyToLocalRepository(tempResourceDir.resolve("package_m_1_3_0_beta"));
        // Cache package_n
        BCompileUtil.compileAndCacheBala(tempResourceDir.resolve("package_n_2_0_0"), testDistCacheDirectory,
                projectEnvironmentBuilder);

        //2. Build project_o with sticky == true
        deleteBuildFile(projectDirPath);
        BuildProject buildProject2 = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        buildProject2.save();
        failIfDiagnosticsExists(buildProject2);

        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies.toml"));

        //3. Build project_o with sticky == false
        deleteBuildFile(projectDirPath);
        BuildProject buildProject3 = BuildProject.load(projectEnvironmentBuilder, projectDirPath,
                BuildOptions.builder().setSticky(false).build());
        buildProject3.save();
        failIfDiagnosticsExists(buildProject3);

        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies.toml"));

        //4. Directly import package_m
        projectDirPath = tempResourceDir.resolve("project_o_with_import");
        deleteBuildFile(projectDirPath);
        BuildProject buildProject4 = BuildProject.load(projectEnvironmentBuilder, projectDirPath,
                BuildOptions.builder().setSticky(false).build());
        buildProject4.save();

        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies.toml"));
    }

    @Test(description = "A newer pre-release version of a dependency is being used from the local repo")
    public void testCase0011(ITestContext ctx) throws IOException {

        // 1. User caches package_m:1_3_0-beta.1 to local repository
        cacheDependencyToCentralRepository(tempResourceDir.resolve("package_m_1_0_1"));
        cacheDependencyToLocalRepository(tempResourceDir.resolve("package_m_1_3_0_beta"));
        // Cache package_n
        BCompileUtil.compileAndCacheBala(tempResourceDir.resolve("package_n_2_0_0"),
                testDistCacheDirectory, projectEnvironmentBuilder);

        // 2. Directly import package while specifying in Ballerina.toml
        Path projectDirPath = tempResourceDir.resolve("project_o_with_import_local_dependency");
        ctx.getCurrentXmlTest().addParameter("packagePath", String.valueOf(projectDirPath));
        ctx.getCurrentXmlTest().addParameter("deleteDependenciesToml", "true");

        BuildProject buildProject2 = BuildProject.load(projectEnvironmentBuilder, projectDirPath,
                BuildOptions.builder().build());
        buildProject2.save();
        failIfDiagnosticsExists(buildProject2);

        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies.toml"));

        // 3. Check sticky false
        deleteBuildFile(projectDirPath);
        BuildProject buildProject3 = BuildProject.load(projectEnvironmentBuilder, projectDirPath,
                BuildOptions.builder().setSticky(false).build());
        buildProject3.save();
        failIfDiagnosticsExists(buildProject3);
        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies.toml"));
    }

    @Test(description = "A dependency has only pre-release versions released to central")
    public void testCase0012(ITestContext ctx) throws IOException {
        Path projectDirPath = tempResourceDir.resolve("project_q_pre_release_only");
        ctx.getCurrentXmlTest().addParameter("packagePath", String.valueOf(projectDirPath));
        ctx.getCurrentXmlTest().addParameter("deleteDependenciesToml", "true");

        // User has imported package_p:1.0.0-alpha.1
        cacheDependencyToCentralRepository(tempResourceDir.resolve("package_p_1_0_0_alpha"));

        //1. Build project_q_pre_release_only
        BuildProject buildProject1 = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        buildProject1.save();
        failIfDiagnosticsExists(buildProject1);

        // Compare dependencies.toml
        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies.toml"));

        // The following versions have been releasing to central
        //      - package_p:1.0.0-beta.1

        // Cache package_p pre-release version 1.0.0-beta.1 to central
        cacheDependencyToCentralRepository(tempResourceDir.resolve("package_p_1_0_0_beta"));

        //2. User builds the project_q again with sticky = true as default
        BuildProject buildProject2 = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        buildProject2.save();
        failIfDiagnosticsExists(buildProject2);

        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies.toml"));

        //3. User deletes the project_q build file
        deleteBuildFile(projectDirPath);

        BuildProject buildProject3 = BuildProject.load(projectEnvironmentBuilder, projectDirPath,
                BuildOptions.builder().setSticky(false).build());
        buildProject3.save();
        failIfDiagnosticsExists(buildProject3);

        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies_NoSticky.toml"));
    }

    // package_w -> package_x -> package_y
    @Test(description = "Build package_y with the latest available package_w, package_x versions")
    public void testCase0013(ITestContext ctx) throws IOException {
        Path projectDirPath = distVersionResourcesDirectory.resolve("package_y");
        ctx.getCurrentXmlTest().addParameter("packagePath", String.valueOf(projectDirPath));
        ctx.getCurrentXmlTest().addParameter("deleteDependenciesToml", "false");

        // update the distribution version of package_y with the current ballerina version
        replaceDistributionVersionOfDependenciesToml(projectDirPath, RepoUtils.getBallerinaShortVersion());

        // cache package_w:1.0.0 to the central
        BCompileUtil.compileAndCacheBala(distVersionResourcesDirectory.resolve("package_w_1_0_0").toString(),
        testDistCacheDirectory, projectEnvironmentBuilder);
        cacheDependencyToCentralRepository(distVersionResourcesDirectory.resolve("package_w_1_0_0"));

        // cache package_x:1.0.0
        cacheDependencyToCentralRepository(distVersionResourcesDirectory.resolve("package_x_1_0_0"));

        // build package_y
        BuildProject buildProject1 = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        buildProject1.save();
        failIfDiagnosticsExists(buildProject1);

        // Compare Dependencies.toml
        // Should contain package_w:1.0.0, package_x:1.0.0
        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies_01.toml"));
    }

    // package_w -> package_x
    // package_x -> package_y
    @Test(description = "Bump the patch version of a direct dependency and run with the same distribution version",
            dependsOnMethods = "testCase0013")
    public void testCase0014(ITestContext ctx) throws IOException {
        Path projectDirPath = distVersionResourcesDirectory.resolve("package_y");
        ctx.getCurrentXmlTest().addParameter("packagePath", String.valueOf(projectDirPath));
        ctx.getCurrentXmlTest().addParameter("deleteDependenciesToml", "false");

        // cache package_x:1.0.1 to the central
        cacheDependencyToCentralRepository(distVersionResourcesDirectory.resolve("package_x_1_0_1"));

        // build package_y
        deleteDirectory(projectDirPath.resolve(TARGET_DIR_NAME));
        BuildProject buildProject1 = BuildProject.load(projectEnvironmentBuilder, projectDirPath,
                BuildOptions.builder().setSticky(false).build());
        buildProject1.save();

        failIfDiagnosticsExists(buildProject1);

        // Compare Dependencies.toml
        // Possible candidates for package_x are 1.0.0 and 1.0.1
        // Should contain package_w:1.0.0, package_x:1.0.1
        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies_02.toml"));
    }

    @Test(description = "Bump the patch version of a direct dependency and run with a newer distribution version",
            dependsOnMethods = "testCase0014")
    public void testCase0015(ITestContext ctx) throws IOException {
        Path projectDirPath = distVersionResourcesDirectory.resolve("package_y");
        ctx.getCurrentXmlTest().addParameter("packagePath", String.valueOf(projectDirPath));
        ctx.getCurrentXmlTest().addParameter("deleteDependenciesToml", "false");

        // We manually edit the Dependencies.toml to read and reduce the distribution-version field value.
        // That way, when we build using the latest distribution version, it is as if the previous build was using
        // the prior minor version.
        // This may fail if there is a major distribution version.
        decreaseMinorVersionInDepenciesToml(projectDirPath);

        // cache package_x:1.0.2 to the central
        cacheDependencyToCentralRepository(distVersionResourcesDirectory.resolve("package_x_1_0_2"));

        // build package_y
        deleteDirectory(projectDirPath.resolve(TARGET_DIR_NAME));
        BuildProject buildProject1 = BuildProject.load(projectEnvironmentBuilder, projectDirPath,
                BuildOptions.builder().setSticky(false).build());
        buildProject1.save();
        assertDiagnosticCount(buildProject1, 1, "Should contain only one diagnostic regarding the distribution " +
                "version increment");

        // Compare Dependencies.toml
        // Possible candidates for package_x are 1.0.1 and 1.0.2
        // Should contain package_w:1.0.0, package_x:1.0.2
        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies_03.toml"));
    }

    // package_w -> package_x -> package_y
    @Test(description = "Bump the patch, minor & major versions of a direct dependency and run with the same " +
            "distribution version", dependsOnMethods = "testCase0015")
    public void testCase0016(ITestContext ctx) throws IOException {
        Path projectDirPath = distVersionResourcesDirectory.resolve("package_y");
        ctx.getCurrentXmlTest().addParameter("packagePath", String.valueOf(projectDirPath));
        ctx.getCurrentXmlTest().addParameter("deleteDependenciesToml", "false");

        // cache package_x:1.0.3
        cacheDependencyToCentralRepository(distVersionResourcesDirectory.resolve("package_x_1_0_3"));
        // cache package_x:1.1.0
        cacheDependencyToCentralRepository(distVersionResourcesDirectory.resolve("package_x_1_1_0"));
        // cache package_x:2.0.0
        cacheDependencyToCentralRepository(distVersionResourcesDirectory.resolve("package_x_2_0_0"));

        // build package_y
        deleteDirectory(projectDirPath.resolve(TARGET_DIR_NAME));
        BuildProject buildProject1 = BuildProject.load(projectEnvironmentBuilder, projectDirPath,
                BuildOptions.builder().setSticky(false).build());
        buildProject1.save();

        failIfDiagnosticsExists(buildProject1);

        // Compare Dependencies.toml
        // Possible candidates for package_x are 1.0.3, 1.1.0, and 2.0.0
        // Should contain package_w:1.0.0, package_x:1.0.3
        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies_04.toml"));
    }

    @Test(description = "Bump the patch, minor & major versions of a direct dependency and run with a new " +
            "distribution version", dependsOnMethods = "testCase0016")
    public void testCase0017(ITestContext ctx) throws IOException {
        Path projectDirPath = distVersionResourcesDirectory.resolve("package_y");
        ctx.getCurrentXmlTest().addParameter("packagePath", String.valueOf(projectDirPath));
        ctx.getCurrentXmlTest().addParameter("deleteDependenciesToml", "false");

        decreaseMinorVersionInDepenciesToml(projectDirPath);

        // cache package_x:1.0.4
        cacheDependencyToCentralRepository(distVersionResourcesDirectory.resolve("package_x_1_0_4"));

        // build package_y
        deleteDirectory(projectDirPath.resolve(TARGET_DIR_NAME));
        BuildProject buildProject1 = BuildProject.load(projectEnvironmentBuilder, projectDirPath,
                BuildOptions.builder().setSticky(false).build());
        buildProject1.save();
        assertDiagnosticCount(buildProject1, 1, "Should contain only one diagnostic regarding the distribution " +
                "version increment");

        // Compare Dependencies.toml
        // Possible candidates for package_x are 1.0.0 and 1.0.1
        // Possible candidates for package_x are 1.0.4, 1.1.0, and 2.0.0
        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies_05.toml"));
    }

     @Test(description = "Bump the patch, minor & major versions of a direct dependency and run with no " +
             "distribution version field in the toml", dependsOnMethods = "testCase0017")
     public void testCase0018(ITestContext ctx) throws IOException {
        Path projectDirPath = distVersionResourcesDirectory.resolve("package_y");
        ctx.getCurrentXmlTest().addParameter("packagePath", String.valueOf(projectDirPath));
        ctx.getCurrentXmlTest().addParameter("deleteDependenciesToml", "false");

        // remove the distribution version from the Dependencies.toml
        replaceDistributionVersionOfDependenciesToml(projectDirPath, null);

        // cache package_x:1.1.1
         cacheDependencyToCentralRepository(distVersionResourcesDirectory.resolve("package_x_1_1_1"));
         // cache package_x:1.2.0
         cacheDependencyToCentralRepository(distVersionResourcesDirectory.resolve("package_x_1_2_0"));


         // build package_y
        BuildProject buildProject1 = BuildProject.load(projectEnvironmentBuilder, projectDirPath,
                BuildOptions.builder().setSticky(false).build());
        buildProject1.save();
        assertDiagnosticCount(buildProject1, 1, "Should contain only one diagnostic regarding the distribution " +
                "version increment");

        // Compare Dependencies.toml
         // Possible candidates for package_x are 1.1.1, 1.2.0, and 2.0.0
         // Should contain package_w:1.0.0, package_x:1.2.0
        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies_06.toml"));
     }

    @Test(description = "Bump the patch version of an indirect dependency and run with the same distribution version",
            dependsOnMethods = "testCase0018")
    public void testCase0019(ITestContext ctx) throws IOException {
        Path projectDirPath = distVersionResourcesDirectory.resolve("package_y");
        ctx.getCurrentXmlTest().addParameter("packagePath", String.valueOf(projectDirPath));
        ctx.getCurrentXmlTest().addParameter("deleteDependenciesToml", "false");

        // cache package_w:1.0.1
        cacheDependencyToCentralRepository(distVersionResourcesDirectory.resolve("package_w_1_0_1"));

        // build package_y
        BuildProject buildProject1 = BuildProject.load(projectEnvironmentBuilder, projectDirPath,
                BuildOptions.builder().setSticky(false).build());
        buildProject1.save();

        failIfDiagnosticsExists(buildProject1);

        // Compare Dependencies.toml
        // Should contain package_w:1.0.1, package_x:1.2.0
        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies_07.toml"));
    }

    @Test(description = "Bump the patch version of an indirect dependency and run with a newer distribution version",
            dependsOnMethods = "testCase0019")
    public void testCase0020(ITestContext ctx) throws IOException {
        Path projectDirPath = distVersionResourcesDirectory.resolve("package_y");
        ctx.getCurrentXmlTest().addParameter("packagePath", String.valueOf(projectDirPath));
        ctx.getCurrentXmlTest().addParameter("deleteDependenciesToml", "false");

        decreaseMinorVersionInDepenciesToml(projectDirPath);

        // cache package_x:1.0.2 to the central
        cacheDependencyToCentralRepository(distVersionResourcesDirectory.resolve("package_w_1_0_2"));

        // build package_y
        deleteDirectory(projectDirPath.resolve(TARGET_DIR_NAME));
        BuildProject buildProject1 = BuildProject.load(projectEnvironmentBuilder, projectDirPath,
                BuildOptions.builder().setSticky(false).build());
        buildProject1.save();
        assertDiagnosticCount(buildProject1, 1, "Should contain only one diagnostic regarding the distribution " +
                "version increment");

        // Compare Dependencies.toml
        // Possible candidates for package_w are 1.0.1 and 1.0.2
        // Should contain package_w:1.0.2, package_x:1.2.0
        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies_08.toml"));
    }

    @Test(description = "Bump the patch, minor & major versions of an indirect dependency and run with the same " +
            "distribution version", dependsOnMethods = "testCase0020")
    public void testCase0021(ITestContext ctx) throws IOException {
        Path projectDirPath = distVersionResourcesDirectory.resolve("package_y");
        ctx.getCurrentXmlTest().addParameter("packagePath", String.valueOf(projectDirPath));
        ctx.getCurrentXmlTest().addParameter("deleteDependenciesToml", "false");

        // cache package_w:1.0.3
        cacheDependencyToCentralRepository(distVersionResourcesDirectory.resolve("package_w_1_0_3"));
        // cache package_w:1.1.0
        cacheDependencyToCentralRepository(distVersionResourcesDirectory.resolve("package_w_1_1_0"));
        // cache package_w:2.0.0
        cacheDependencyToCentralRepository(distVersionResourcesDirectory.resolve("package_w_2_0_0"));

        // build package_y
        deleteDirectory(projectDirPath.resolve(TARGET_DIR_NAME));
        BuildProject buildProject1 = BuildProject.load(projectEnvironmentBuilder, projectDirPath,
                BuildOptions.builder().setSticky(false).build());
        buildProject1.save();

        failIfDiagnosticsExists(buildProject1);

        // Compare Dependencies.toml
        // Possible candidates for package_w are 1.0.3, 1.1.0, and 2.0.0
        // Should contain package_w:1.0.3, package_x:1.2.0
        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies_09.toml"));
    }

    @Test(description = "Bump the patch, minor & major versions of an indirect dependency and run with a new " +
            "distribution version", dependsOnMethods = "testCase0021")
    public void testCase0022(ITestContext ctx) throws IOException {
        Path projectDirPath = distVersionResourcesDirectory.resolve("package_y");
        ctx.getCurrentXmlTest().addParameter("packagePath", String.valueOf(projectDirPath));
        ctx.getCurrentXmlTest().addParameter("deleteDependenciesToml", "false");

        decreaseMinorVersionInDepenciesToml(projectDirPath);

        // cache package_w:1.0.4
        cacheDependencyToCentralRepository(distVersionResourcesDirectory.resolve("package_w_1_0_4"));

        // build package_y
        deleteDirectory(projectDirPath.resolve(TARGET_DIR_NAME));
        BuildProject buildProject1 = BuildProject.load(projectEnvironmentBuilder, projectDirPath,
                BuildOptions.builder().setSticky(false).build());
        buildProject1.save();
        assertDiagnosticCount(buildProject1, 1, "Should contain only one diagnostic regarding the distribution " +
                "version increment");

        // Compare Dependencies.toml
        // Possible candidates for package_w are 1.0.4, 1.1.0, and 2.0.0
        // Should contain package_w:1.1.0, package_x:1.2.0
        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies_10.toml"));
    }

    @Test(description = "Bump the patch, minor & major versions of an indirect dependency and run with no " +
            "distribution version field in the toml", dependsOnMethods = "testCase0022")
    public void testCase0023(ITestContext ctx) throws IOException {
        Path projectDirPath = distVersionResourcesDirectory.resolve("package_y");
        ctx.getCurrentXmlTest().addParameter("packagePath", String.valueOf(projectDirPath));
        ctx.getCurrentXmlTest().addParameter("deleteDependenciesToml", "false");

        // remove the distribution version from the Dependencies.toml
        replaceDistributionVersionOfDependenciesToml(projectDirPath, null);

        // cache package_w:1.1.1
        cacheDependencyToCentralRepository(distVersionResourcesDirectory.resolve("package_w_1_1_1"));
        // cache package_w:1.2.0
        cacheDependencyToCentralRepository(distVersionResourcesDirectory.resolve("package_w_1_2_0"));


        // build package_y
        BuildProject buildProject1 = BuildProject.load(projectEnvironmentBuilder, projectDirPath,
                BuildOptions.builder().setSticky(false).build());
        buildProject1.save();
        assertDiagnosticCount(buildProject1, 1, "Should contain only one diagnostic regarding the distribution " +
                "version increment");

        // Compare Dependencies.toml
        // Possible candidates for package_w are 1.1.1, 1.2.0, and 2.0.0
        // Should contain package_w:1.2.0, package_x:1.2.0
        assertTomlFilesEquals(projectDirPath.resolve(DEPENDENCIES_TOML),
                projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies_11.toml"));
    }

    @AfterMethod
    private void afterMethod(ITestContext ctx) throws IOException {
        Path packagePath = Path.of(ctx.getCurrentXmlTest().getParameter("packagePath"));
        String deleteDependenciesTomlStr = ctx.getCurrentXmlTest().getParameter("deleteDependenciesToml");
        boolean deleteDependenciesToml = Boolean.parseBoolean(deleteDependenciesTomlStr);
        if (deleteDependenciesToml) {
            deleteDependenciesTomlAndBuildFile(packagePath);
        } else {
            deleteBuildFile(packagePath);
        }
    }

    @AfterClass
    public void afterClass() throws IOException {
        Path advResBalaDir = testBuildDirectory.resolve("user-home").resolve("repositories")
                .resolve("central.ballerina.io").resolve("bala").resolve("adv_res");
        Files.walk(advResBalaDir)
                .map(Path::toFile)
                .sorted((o1, o2) -> -o1.compareTo(o2))
                .forEach(File::delete);
    }

    private static void deleteDependenciesTomlAndBuildFile(Path packagePath) throws IOException {
        Files.deleteIfExists(packagePath.resolve(DEPENDENCIES_TOML));
        deleteBuildFile(packagePath);
    }

    private static void deleteBuildFile(Path packagePath) throws IOException {
        Files.deleteIfExists(packagePath.resolve(TARGET_DIR_NAME).resolve(BUILD_FILE));
    }

    private void failIfDiagnosticsExists(BuildProject buildProject) {
        assertDiagnosticCount(buildProject, 0, "Unexpected compilation diagnostics");
    }

    private void assertDiagnosticCount(BuildProject buildProject, int count, String message) {
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();
        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        diagnosticResult.errors().forEach(OUT::println);
        Assert.assertEquals(diagnosticResult.diagnosticCount(), count, message);
    }

    private void decreaseMinorVersionInDepenciesToml(Path projectDirPath) throws IOException {
        String currentDistVersion = RepoUtils.getBallerinaShortVersion();
        String lesserDistVersion = decreaseMinorVersion(currentDistVersion);
        replaceDistributionVersionOfDependenciesToml(projectDirPath, lesserDistVersion);
    }

    private String decreaseMinorVersion(String version) {
        String[] versionComponents = version.split("\\.");
        int minorVersion = Integer.parseInt(versionComponents[1]);
        return versionComponents[0] + DOT + (minorVersion - 1) + DOT + versionComponents[2];
    }

    private void deleteDirectory(Path directory) throws IOException {
        if (Files.exists(directory)) {
            try (Stream<Path> paths = Files.walk(directory)) {
                paths.sorted((path1, path2) -> -path1.compareTo(path2))
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                // ignore
                            }
                        });
            }
        }
    }
}
