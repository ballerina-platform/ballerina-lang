package io.ballerina.projects.test;

import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.projects.util.ProjectConstants;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.projects.test.TestUtils.readFileAsString;
import static io.ballerina.projects.util.ProjectConstants.BUILD_FILE;
import static io.ballerina.projects.util.ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME;
import static io.ballerina.projects.util.ProjectConstants.DEPENDENCIES_TOML;
import static io.ballerina.projects.util.ProjectConstants.DIST_CACHE_DIRECTORY;
import static io.ballerina.projects.util.ProjectConstants.LOCAL_REPOSITORY_NAME;
import static io.ballerina.projects.util.ProjectConstants.RESOURCE_DIR_NAME;
import static io.ballerina.projects.util.ProjectConstants.TARGET_DIR_NAME;

/**
 * Contains integrations test cases to test local package resolution logic.
 *
 * @since 2201.0.3
 */
public class PackageResolutionLocalTests extends BaseTest {

    private static final Path RESOURCE_DIRECTORY = Paths.get(
            "src/test/resources/projects_for_resolution_local_tests").toAbsolutePath();
    private static final Path PROJECTS_DIRECTORY = RESOURCE_DIRECTORY.resolve("projects");
    private static final Path PACKAGES_DIRECTORY = RESOURCE_DIRECTORY.resolve("packages");
    private static final String ORG = "local_res";

    private static final Path testBuildDirectory = Paths.get("build").toAbsolutePath();
    private static final Path testDistCacheDirectory = testBuildDirectory.resolve(DIST_CACHE_DIRECTORY);
    Path customUserHome = Paths.get("build", "user-home");
    Environment environment = EnvironmentBuilder.getBuilder().setUserHome(customUserHome).build();
    ProjectEnvironmentBuilder projectEnvironmentBuilder = ProjectEnvironmentBuilder.getBuilder(environment);


    @Test(description = "tests resolution of local repository package (unpublished) as direct " +
            "dependencies")
    public void testCase0001(ITestContext ctx) throws IOException {

        /*
            Pushed package_a to local repository
            Built project_0001 successfully and generated jars
         */

        Path projectDirPath = PROJECTS_DIRECTORY.resolve("project_0001");
        ctx.getCurrentXmlTest().addParameter("packagePath", String.valueOf(projectDirPath));

        // project_001 --> package_a:0.0.1
        // Cache package_a to local
        cacheDependencyToLocalRepository(PACKAGES_DIRECTORY.resolve("package_a_0_0_1"));

        // Build project_0001:1.0.0
        BuildProject buildProject = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        buildProject.save();
        failIfDiagnosticsExists(buildProject);

        Assert.assertEquals(readFileAsString(projectDirPath.resolve(DEPENDENCIES_TOML)),
                readFileAsString(projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies.toml")));
    }

    @Test(description = "Tests local packages (published, but patched with the same version) as direct dependencies",
            dependsOnMethods = "testCase0001")
    public void testCase0002(ITestContext ctx) throws IOException {

        /*
            Pushed package_a to central
            Changed write method module, built again and pushed to local repository with 'same' version
            Built project_0002
                package_a resolved from local repository since it mentioned in the Ballerina.toml
                package_a resolved from central and built successfully and generated jars when dependency removed
                from Ballerina.toml
         */

        Path projectDirPath = PROJECTS_DIRECTORY.resolve("project_0002");
        ctx.getCurrentXmlTest().addParameter("packagePath", String.valueOf(projectDirPath));

        // project_002 --> package_a:0.0.1
        // Push package_a:0.0.1 to central
        cacheDependencyToCentralRepository(PACKAGES_DIRECTORY.resolve("package_a_0_0_1_central"));
        // Check if its actually cached to central properly
        Path centralCache = USER_HOME.resolve(ProjectConstants.REPOSITORIES_DIR)
                .resolve(CENTRAL_REPOSITORY_CACHE_NAME).resolve(ProjectConstants.BALA_DIR_NAME).resolve(ORG).resolve(
                        "package_a").resolve("0.0.1").resolve("java11").resolve("dependency-graph.json");
        Assert.assertTrue(centralCache.toFile().exists());

        // Push package_a:0.0.1 to local
        // package_a is already in local
        // Check if its actually cached to local properly
        Path localCache = USER_HOME.resolve(ProjectConstants.REPOSITORIES_DIR)
                .resolve(LOCAL_REPOSITORY_NAME).resolve(ProjectConstants.BALA_DIR_NAME).resolve(ORG).resolve(
                        "package_a").resolve("0.0.1").resolve("java11").resolve("dependency-graph.json");
        Assert.assertTrue(localCache.toFile().exists());

        // Build project_0002:1.0.0
        BuildProject buildProject = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        buildProject.save();
        failIfDiagnosticsExists(buildProject);

        Assert.assertEquals(readFileAsString(projectDirPath.resolve(DEPENDENCIES_TOML)),
                readFileAsString(projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies.toml")));

        // If the user removes the local dependency from the Ballerina.toml
        projectDirPath = PROJECTS_DIRECTORY.resolve("project_0002_2");

        // Build project_0002:1.0.0
        buildProject = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        buildProject.save();
        // expectedDiagnostics(buildProject, 1, "undefined function 'func1'");
        failIfDiagnosticsExists(buildProject);

        Assert.assertEquals(readFileAsString(projectDirPath.resolve(DEPENDENCIES_TOML)),
                readFileAsString(projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies.toml")));

        // Delete it manually since aftermethod only works for previous project
        deleteDependenciesToml(projectDirPath);
    }

    @Test(description = "Tests Local repository packages (published, but patched as a newer unpublished version) " +
            "as direct dependencies", dependsOnMethods = "testCase0002")
    public void testCase0003(ITestContext ctx) throws IOException {

        /*
            Updated version of package_a to 0.1.1, rebuilt and pushed to local
            Built project_0003 with existing Dependencies.toml ---> resolved package_a:0.0.1 from central
            Changed Ballerina.toml of project_0003 to 0.1.1 and build again ---> resolved package_a:0.1.1 from local
         */

        Path projectDirPath = PROJECTS_DIRECTORY.resolve("project_0003");
        ctx.getCurrentXmlTest().addParameter("packagePath", String.valueOf(projectDirPath));

        // project_003 --> package_a:0.0.1
        // Push package_a:0.1.1 to local
        cacheDependencyToLocalRepository(PACKAGES_DIRECTORY.resolve("package_a_0_1_1"));

        // Check if its actually cached to local properly
        Path localCache = USER_HOME.resolve(ProjectConstants.REPOSITORIES_DIR)
                .resolve(LOCAL_REPOSITORY_NAME).resolve(ProjectConstants.BALA_DIR_NAME).resolve(ORG).resolve(
                        "package_a").resolve("0.1.1").resolve("java11").resolve("dependency-graph.json");
        Assert.assertTrue(localCache.toFile().exists());

        // package_a should already be pushed to central so it should pick from there
        // Build project_0003:1.0.0
        BuildProject buildProject = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        buildProject.save();
        failIfDiagnosticsExists(buildProject);

        Assert.assertEquals(readFileAsString(projectDirPath.resolve(DEPENDENCIES_TOML)),
                readFileAsString(projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies.toml")));

        // Change the Ballerina.toml to include the new version to be pulled from local
        projectDirPath = PROJECTS_DIRECTORY.resolve("project_0003_2");

        buildProject = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        buildProject.save();
        failIfDiagnosticsExists(buildProject);

        Assert.assertEquals(readFileAsString(projectDirPath.resolve(DEPENDENCIES_TOML)),
                readFileAsString(projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies.toml")));

        // Delete it manually since aftermethod only works for previous project
        deleteDependenciesToml(projectDirPath);
    }

    @Test(description = "Tests  Local repository packages (unpublished) as indirect dependencies")
    public void testCase0004(ITestContext ctx) throws IOException {
        /*
            project_0004 ---> package_b:0.0.1 ---> package_c:0.0.1

            package_c pushed to local repository
            package_c added to package_b Ballerina.toml, build and pushed package_b to local repository
            Add both package_b and package_c to Ballerina.toml and build project_004 again
         */


        Path projectDirPath = PROJECTS_DIRECTORY.resolve("project_0004");
        ctx.getCurrentXmlTest().addParameter("packagePath", String.valueOf(projectDirPath));

        // Push package_c:0.0.1 to local
        BCompileUtil.compileAndCacheBala(PACKAGES_DIRECTORY.resolve("package_c_0_0_1").toString());
        // cacheDependencyToLocalRepository(PACKAGES_DIRECTORY.resolve("package_c_0_0_1"));

        // Push package_b:0.0.1 to local
        cacheDependencyToLocalRepository(PACKAGES_DIRECTORY.resolve("package_b_0_0_1"));

        // Build project and assert deps toml
        BuildProject buildProject = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        buildProject.save();
        failIfDiagnosticsExists(buildProject);

        Assert.assertEquals(readFileAsString(projectDirPath.resolve(DEPENDENCIES_TOML)),
                readFileAsString(projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies.toml")));
    }

    @Test(enabled = false, description = "Local repository packages (published, but patched with the same version) as" +
            " indirect dependencies", dependsOnMethods = "testCase0004")
    public void testCase0005(ITestContext ctx) throws IOException {
        /*
            project_0004 ---> package_d:0.0.1 ---> package_c:0.0.1

            package_c:0.0.1 pushed to central repository
            change package_c:0.0.1 and push to local
            package_c added to package_d Ballerina.toml, build and pushed package_d to local repository
            Add both package_d and package_c to Ballerina.toml and build project_004 again
         */

        // Note that we are using a similar project to the previous test scenario project_0004
        Path projectDirPath = PROJECTS_DIRECTORY.resolve("project_0005");
        ctx.getCurrentXmlTest().addParameter("packagePath", String.valueOf(projectDirPath));

        // Push package_c:0.0.1 to central
//        BCompileUtil.compileAndCacheBala(PACKAGES_DIRECTORY.resolve("package_c_0_0_1").toString());
        cacheDependencyToCentralRepository(PACKAGES_DIRECTORY.resolve("package_c_0_0_1"));

        // Change package_c with same version and push to local
        cacheDependencyToLocalRepository(PACKAGES_DIRECTORY.resolve("package_c_0_0_1_v2"));

        // Push package_d:0.0.1 to local
        // Note we are changing the package here as well to support the api change in package_c
        cacheDependencyToLocalRepository(PACKAGES_DIRECTORY.resolve("package_d_0_0_1"));

        // Build project and assert deps toml
        BuildProject buildProject = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        buildProject.save();
        failIfDiagnosticsExists(buildProject);

        Assert.assertEquals(readFileAsString(projectDirPath.resolve(DEPENDENCIES_TOML)),
                readFileAsString(projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies.toml")));
    }

    @Test(enabled = false, description = " Local repository packages (published, but patched with the same version " +
            "with different indirect deps) as direct dependencies", dependsOnMethods = "testCase0004")
    public void testCase0007(ITestContext ctx) throws IOException {
        /*
            project_0007 ---> package_b:2.0.0 (central) ---> package_c:2.0.0 (central)

            project_0007 ---> package_b:2.0.0 (local) ---> package_c:2.0.0 (central)
                              package_b:2.0.0 (local) ---> package_d:2.0.0 (central)

            Package_b resolved from local repo as expected.
         */

        // Note that we are using a similar project to the previous test scenario project_0004
        Path projectDirPath = PROJECTS_DIRECTORY.resolve("project_0007");
        ctx.getCurrentXmlTest().addParameter("packagePath", String.valueOf(projectDirPath));

        // Push package_c:2.0.0 to central
        BCompileUtil.compileAndCacheBala(PACKAGES_DIRECTORY.resolve("package_c_2_0_0").toString());
        // cacheDependencyToCentralRepository(PACKAGES_DIRECTORY.resolve("package_c_2_0_0"));

        // Push package_b:2.0.0 to central
        //cacheDependencyToCentralRepository(PACKAGES_DIRECTORY.resolve("package_b_2_0_0_central"));
        BCompileUtil.compileAndCacheBala(PACKAGES_DIRECTORY.resolve("package_b_2_0_0").toString());

        // Build project and assert deps toml
        BuildProject buildProject = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        buildProject.save();
        failIfDiagnosticsExists(buildProject);

        Assert.assertEquals(readFileAsString(projectDirPath.resolve(DEPENDENCIES_TOML)),
                readFileAsString(projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies.toml")));

        // Change the deps in toml
        projectDirPath = PROJECTS_DIRECTORY.resolve("project_0007_v2");
        ctx.getCurrentXmlTest().addParameter("packagePath", String.valueOf(projectDirPath));

        // Push package_b:2.0.0 to local
        cacheDependencyToLocalRepository(PACKAGES_DIRECTORY.resolve("package_b_2_0_0"));

        // Push package_d:2.0.0 to central
        BCompileUtil.compileAndCacheBala(PACKAGES_DIRECTORY.resolve("package_d_2_0_0").toString());
        // cacheDependencyToLocalRepository(PACKAGES_DIRECTORY.resolve("package_d_2_0_0"));

        // Build project and assert deps toml
        buildProject = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        buildProject.save();
        failIfDiagnosticsExists(buildProject);

        Assert.assertEquals(readFileAsString(projectDirPath.resolve(DEPENDENCIES_TOML)),
                readFileAsString(projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("Dependencies.toml")));
    }

    @AfterMethod
    private void deleteBuildFile(ITestContext ctx) throws IOException {
        Path packagePath = Path.of(ctx.getCurrentXmlTest().getParameter("packagePath"));
        deleteBuildFile(packagePath);
    }

    @AfterMethod
    private void deleteDependenciesToml(ITestContext ctx) throws IOException {
        Path packagePath = Path.of(ctx.getCurrentXmlTest().getParameter("packagePath"));
        deleteDependenciesToml(packagePath);
    }

    @AfterClass
    public void afterClass() throws IOException {
        Path advResBalaDir = testBuildDirectory.resolve("user-home").resolve("repositories")
                .resolve("central.ballerina.io").resolve("bala").resolve("local_res");
        if (advResBalaDir.toFile().exists()) {
            Files.walk(advResBalaDir)
                    .map(Path::toFile)
                    .sorted((o1, o2) -> -o1.compareTo(o2))
                    .forEach(File::delete);
        }

        advResBalaDir = testBuildDirectory.resolve("user-home").resolve("repositories")
                .resolve("local").resolve("bala").resolve("local_res");
        if (advResBalaDir.toFile().exists()) {
            Files.walk(advResBalaDir)
                    .map(Path::toFile)
                    .sorted((o1, o2) -> -o1.compareTo(o2))
                    .forEach(File::delete);
        }
    }

    private static void deleteBuildFile(Path packagePath) throws IOException {
        Files.deleteIfExists(packagePath.resolve(TARGET_DIR_NAME).resolve(BUILD_FILE));
    }

    private static void deleteDependenciesToml(Path packagePath) throws IOException {
        Files.deleteIfExists(packagePath.resolve(DEPENDENCIES_TOML));
    }

    private void failIfDiagnosticsExists(BuildProject buildProject) {
        expectedDiagnostics(buildProject, 0, null);
    }

    private void expectedDiagnostics(BuildProject buildProject, int expectedCount, String expectedMessage) {
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();
        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        diagnosticResult.errors().forEach(OUT::println);

        // Check for diagnostic count
        Assert.assertEquals(diagnosticResult.diagnosticCount(), expectedCount, "Unexpected compilation diagnostics");

        // Check for diagnostic message
        if (expectedMessage != null) {
            Assert.assertTrue(diagnosticResult.diagnostics().stream().findAny().get().message()
                    .contains(expectedMessage));
        }
    }

}
