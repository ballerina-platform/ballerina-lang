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
package io.ballerina.projects.test;

import com.sun.management.UnixOperatingSystemMXBean;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.PackageDependencyScope;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageResolution;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.bala.BalaProject;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.internal.ImportModuleRequest;
import io.ballerina.projects.internal.ImportModuleResponse;
import io.ballerina.projects.internal.environment.DefaultPackageResolver;
import io.ballerina.projects.internal.model.BuildJson;
import io.ballerina.projects.repos.TempDirCompilationCache;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ballerina.projects.test.TestUtils.isWindows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Contains cases to test package resolution logic.
 *
 * @since 2.0.0
 */
public class PackageResolutionTests extends BaseTest {
    private static final Path RESOURCE_DIRECTORY = Paths.get(
            "src/test/resources/projects_for_resolution_tests").toAbsolutePath();
    private static final Path testBuildDirectory = Paths.get("build").toAbsolutePath();

    @BeforeTest
    public void setup() throws IOException {
        // Compile and cache dependency for custom repo tests
        cacheDependencyToLocalRepo(RESOURCE_DIRECTORY.resolve("package_c_with_pkg_private_function"));
    }

    @Test(description = "tests resolution with zero direct dependencies")
    public void testProjectWithZeroDependencies() {
        // package_c --> {}
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_c");
        BuildProject buildProject = TestUtils.loadBuildProject(projectDirPath);
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        diagnosticResult.errors().forEach(OUT::println);
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 0, "Unexpected compilation diagnostics");

        // Check direct package dependencies
        Assert.assertEquals(buildProject.currentPackage().packageDependencies().size(), 0,
                "Unexpected number of dependencies");
    }

    @Test(description = "tests resolution with one direct dependency")
    public void testProjectWithOneDependency() {
        // package_b --> package_c
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_b");
        BuildProject buildProject = TestUtils.loadBuildProject(projectDirPath);
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        diagnosticResult.errors().forEach(OUT::println);
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 0, "Unexpected compilation diagnostics");

        // Check direct package dependencies
        Assert.assertEquals(buildProject.currentPackage().packageDependencies().size(), 1,
                "Unexpected number of dependencies");
    }

    @Test(description = "tests resolution with invalid build file")
    public void testProjectWithInvalidBuildFile() throws IOException {
        // Package path
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_n");

        BCompileUtil.compileAndCacheBala("projects_for_resolution_tests/package_o_1_0_0");
        BCompileUtil.compileAndCacheBala("projects_for_resolution_tests/package_o_1_0_2");

        BuildOptions.BuildOptionsBuilder buildOptionsBuilder = BuildOptions.builder().setExperimental(true);
        buildOptionsBuilder.setSticky(false);
        BuildOptions buildOptions = buildOptionsBuilder.build();

        Project loadProject = TestUtils.loadBuildProject(projectDirPath, buildOptions);

        // Delete the build file
        if (loadProject.sourceRoot().resolve(ProjectConstants.TARGET_DIR_NAME).toFile().exists()) {
            TestUtils.deleteDirectory(loadProject.sourceRoot().resolve(ProjectConstants.TARGET_DIR_NAME).toFile());
        }

        // Create empty build file
        Files.createDirectory(loadProject.sourceRoot().resolve(ProjectConstants.TARGET_DIR_NAME));
        Files.createFile(loadProject.sourceRoot().resolve(ProjectConstants.TARGET_DIR_NAME)
                .resolve(ProjectConstants.BUILD_FILE));

        PackageCompilation compilation = loadProject.currentPackage().getCompilation();

        DependencyGraph<ResolvedPackageDependency> dependencyGraph = compilation.getResolution().dependencyGraph();
        for (ResolvedPackageDependency graphNode : dependencyGraph.getNodes()) {
            Collection<ResolvedPackageDependency> directDeps = dependencyGraph.getDirectDependencies(graphNode);
            PackageManifest manifest = graphNode.packageInstance().manifest();
            if (manifest.name().value().equals("package_o")) {
                Assert.assertEquals(manifest.version().toString(), "1.0.2");
            }
        }
    }

    @Test(dependsOnMethods = "testProjectWithInvalidBuildFile", description = "tests project with empty build file")
    public void testProjectSaveWithEmptyBuildFile() throws IOException {
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_n");
        Project loadProject = TestUtils.loadBuildProject(projectDirPath);
        Path buildPath = loadProject.sourceRoot().resolve(ProjectConstants.TARGET_DIR_NAME)
                .resolve(ProjectConstants.BUILD_FILE);

        Files.deleteIfExists(buildPath);
        Files.createFile(buildPath); // Empty build file

        loadProject.save();

        Assert.assertTrue(Files.exists(buildPath));
        BuildJson buildJson = ProjectUtils.readBuildJson(buildPath);
        Assert.assertFalse(buildJson.isExpiredLastUpdateTime());
    }

    @Test(dependsOnMethods = "testProjectSaveWithEmptyBuildFile", description = "tests project with empty build file")
    public void testProjectSaveWithNewlineBuildFile() throws IOException {
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_n");
        Project loadProject = TestUtils.loadBuildProject(projectDirPath);
        Path buildPath = loadProject.sourceRoot().resolve(ProjectConstants.TARGET_DIR_NAME)
                .resolve(ProjectConstants.BUILD_FILE);

        Files.deleteIfExists(buildPath);
        Files.createFile(buildPath);
        Files.write(buildPath, "\n".getBytes());

        loadProject.save();

        Assert.assertTrue(Files.exists(buildPath));
        BuildJson buildJson = ProjectUtils.readBuildJson(buildPath);
        Assert.assertFalse(buildJson.isExpiredLastUpdateTime());
    }

    @Test(dependsOnMethods = "testProjectSaveWithNewlineBuildFile",
            description = "tests project with corrupt build file")
    public void testProjectSaveWithCorruptBuildFile() throws IOException {
        // Skip test in windows due to file permission setting issue
        if (isWindows()) {
            throw new SkipException("Skipping tests on Windows");
        }
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_n");
        Project loadProject = TestUtils.loadBuildProject(projectDirPath);
        Path buildPath = loadProject.sourceRoot().resolve(ProjectConstants.TARGET_DIR_NAME)
                .resolve(ProjectConstants.BUILD_FILE);

        Files.deleteIfExists(buildPath);
        Files.createFile(buildPath);
        Files.writeString(buildPath, "Invalid"); // Invalid build file for JSONSyntaxException

        loadProject.save();

        Assert.assertTrue(Files.exists(buildPath));
        BuildJson buildJson = ProjectUtils.readBuildJson(buildPath);
        Assert.assertFalse(buildJson.isExpiredLastUpdateTime());
    }

    @Test(dependsOnMethods = "testProjectSaveWithCorruptBuildFile", description = "tests project with no read " +
            "permissions")
    public void testProjectSaveWithNoReadPermission() throws IOException {
        // Skip test in windows due to file permission setting issue
        if (isWindows()) {
            throw new SkipException("Skipping tests on Windows");
        }
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_n");
        Project loadProject = TestUtils.loadBuildProject(projectDirPath);
        Path buildPath = loadProject.sourceRoot().resolve(ProjectConstants.TARGET_DIR_NAME)
                .resolve(ProjectConstants.BUILD_FILE);
        boolean readable = buildPath.toFile().setReadable(false, false);
        if (!readable) {
            Assert.fail("could not set readable permission");
        }

        loadProject.save();

        PackageCompilation compilation = loadProject.currentPackage().getCompilation();
        Assert.assertTrue(Files.exists(buildPath));

        readable = buildPath.toFile().setReadable(true, true);
        if (!readable) {
            Assert.fail("could not set readable permission");
        }
        BuildJson buildJson = ProjectUtils.readBuildJson(buildPath);
        Assert.assertFalse(buildJson.isExpiredLastUpdateTime());
    }

    @Test(dependsOnMethods = "testProjectSaveWithNoReadPermission", description = "tests project with no write " +
            "permissions")
    public void testProjectSaveWithNoWritePermission() throws IOException {
        // Skip test in windows due to file permission setting issue
        if (isWindows()) {
            throw new SkipException("Skipping tests on Windows");
        }
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_n");
        Project loadProject = TestUtils.loadBuildProject(projectDirPath);
        Path buildPath = loadProject.sourceRoot().resolve(ProjectConstants.TARGET_DIR_NAME)
                .resolve(ProjectConstants.BUILD_FILE);
        boolean writable = buildPath.toFile().setWritable(false, false);
        if (!writable) {
            Assert.fail("could not set writable permission");
        }

        loadProject.save();

        PackageCompilation compilation = loadProject.currentPackage().getCompilation();
        Assert.assertTrue(Files.exists(buildPath));
        BuildJson buildJson = ProjectUtils.readBuildJson(buildPath);
        Assert.assertFalse(buildJson.isExpiredLastUpdateTime());
    }

    @Test()
    public void testClearingEnvironmentCache() throws IOException {
        // Skip test in windows due to file permission setting issue
        if (isWindows()) {
            throw new SkipException("Skipping tests on Windows");
        }

        // Setup : If exists, clear previous cache
        Path projectBCachePath = testBuildDirectory.resolve("repo").resolve("cache").resolve("samjs").resolve(
                "projectB");
        FileUtils.deleteDirectory(projectBCachePath.toFile());


        // Step 1 : Build ProjectB1 and Cache
        CompileResult depCompileResult = BCompileUtil.compileAndCacheBala("projects_for_resolution_tests/projectB1");
        if (depCompileResult.getErrorCount() > 0) {
            Assert.fail("Package B contains compilations error");
        }


        // Step 2 : Build ProjectA with ProjectB as an import
        Path projectA = RESOURCE_DIRECTORY.resolve("projectA");
        Project loadProjectA = TestUtils.loadBuildProject(projectA);


        // Step 3 : Get compilation of ProjectA and verify dependencies
        PackageCompilation compilation = loadProjectA.currentPackage().getCompilation();
        DependencyGraph<ResolvedPackageDependency> dependencyGraph = compilation.getResolution().dependencyGraph();
        for (ResolvedPackageDependency graphNode : dependencyGraph.getNodes()) {
            PackageManifest manifest = graphNode.packageInstance().manifest();
            if (manifest.name().value().equals("projectB")) {
                Assert.assertEquals(manifest.version().toString(), "1.0.0");
            }
        }


        // Step 4 : Modify projectA to be blank

        // - Step 4.1 : Get the main.bal file document for ProjectA
        String newMainProjectAContent = "";
        Module defaultModuleProjectA = loadProjectA.currentPackage().getDefaultModule();
        Optional<DocumentId> mainDocumentId =
                defaultModuleProjectA.documentIds()
                        .stream()
                        .filter(documentId -> defaultModuleProjectA.document(documentId).name().equals("main.bal"))
                        .findFirst();
        if (mainDocumentId.isEmpty()) {
            Assert.fail("Failed to retrieve the document ID");
        }

        // - Step 4.2 : Modify the content of ProjectA
        Document document = defaultModuleProjectA.document(mainDocumentId.get());
        document.modify().withContent(newMainProjectAContent).apply();


        // Step 5 : Compile ProjectA and verify dependency
        compilation = loadProjectA.currentPackage().getCompilation();
        dependencyGraph = compilation.getResolution().dependencyGraph();
        for (ResolvedPackageDependency graphNode : dependencyGraph.getNodes()) {
            PackageManifest manifest = graphNode.packageInstance().manifest();
            if (manifest.name().value().equals("projectB")) {
                Assert.fail("ProjectB found in dependency after editing ProjectA");
            }
        }


        // Step 6 : Clear ProjectB cache
        FileUtils.deleteDirectory(projectBCachePath.toFile());


        // Step 7 : Compile ProjectB2 and cache
        depCompileResult = BCompileUtil.compileAndCacheBala("projects_for_resolution_tests/projectB2");
        if (depCompileResult.getErrorCount() > 0) {
            Assert.fail("Package B contains compilations error");
        }

        // Step 8 : Clear ProjectB cache
        FileUtils.deleteDirectory(projectBCachePath.toFile());


        // Step 9 : Modify ProjectA again with the old content

        // - Step 9.1 : Get the main.bal file document
        String oldMainProjectAContent = "import samjs/projectB;\n" + "\n" + "public function getHello() returns " +
                "(string) {\n" + "    return projectB:hello();\n" + "}";

        Module oldModuleProjectA = loadProjectA.currentPackage().getDefaultModule();

        mainDocumentId = defaultModuleProjectA.documentIds()
                .stream()
                .filter(documentId -> oldModuleProjectA.document(documentId).name().equals("main.bal"))
                .findFirst();
        if (mainDocumentId.isEmpty()) {
            Assert.fail("Failed to retrieve the document ID");
        }

        // - Step 9.2 : Modify the content
        document = defaultModuleProjectA.document(mainDocumentId.get());
        document.modify().withContent(oldMainProjectAContent).apply();


        // Step 10 : Compile ProjectA and verify dependency
        compilation = loadProjectA.currentPackage().getCompilation();
        Assert.assertNotEquals(compilation.diagnosticResult().errorCount(), 0, "Package A has compiled successfully " +
                "when it should fail");

    }

    @Test(description = "tests resolution with one transitive dependency")
    public void testProjectWithOneTransitiveDependency() {
        // package_a --> package_b --> package_c
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_a");
        BuildProject buildProject = TestUtils.loadBuildProject(projectDirPath);
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        diagnosticResult.errors().forEach(OUT::println);
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 0, "Unexpected compilation diagnostics");

        // Check direct package dependencies
        Assert.assertEquals(buildProject.currentPackage().packageDependencies().size(), 1,
                "Unexpected number of dependencies");
    }

    @Test(description = "tests resolution with two direct dependencies and one transitive")
    public void testProjectWithTwoDirectDependencies() {
        // package_d --> package_b --> package_c
        // package_d --> package_e
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_d");
        BuildProject buildProject = TestUtils.loadBuildProject(projectDirPath);
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        diagnosticResult.errors().forEach(OUT::println);
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 0, "Unexpected compilation diagnostics");

        // Check direct package dependencies
        Assert.assertEquals(buildProject.currentPackage().packageDependencies().size(), 2,
                "Unexpected number of dependencies");
    }

    @Test(description = "tests resolution with one transitive dependency",
            expectedExceptions = ProjectException.class,
            expectedExceptionsMessageRegExp = "Transitive dependency cannot be found: " +
                    "org=samjs, package=package_missing, version=1.0.0", enabled = false)
    public void testProjectWithMissingTransitiveDependency() throws IOException {
        // package_missing_transitive_dep --> package_b --> package_c
        // package_missing_transitive_dep --> package_k --> package_z (this is missing)
        Path balaPath = RESOURCE_DIRECTORY.resolve("balas").resolve("missing_transitive_deps")
                .resolve("samjs-package_kk-any-1.0.0.bala");
        BCompileUtil.copyBalaToDistRepository(balaPath, "samjs", "package_kk", "1.0.0");

        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_missing_transitive_dep");
        BuildProject buildProject = TestUtils.loadBuildProject(projectDirPath);
        buildProject.currentPackage().getResolution();
    }

    @Test(description = "Test dependencies should not be stored in bala archive")
    public void testProjectWithTransitiveTestDependencies() throws IOException {
        // package_with_test_dependency --> package_c
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_with_test_dependency");
        BuildProject buildProject = TestUtils.loadBuildProject(projectDirPath);
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();

        // Dependency graph should contain two entries here
        DependencyGraph<ResolvedPackageDependency> depGraphOfSrcProject =
                compilation.getResolution().dependencyGraph();
        Assert.assertEquals(depGraphOfSrcProject.getNodes().size(), 2);

        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_11);

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = jBallerinaBackend.diagnosticResult();
        diagnosticResult.errors().forEach(OUT::println);
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 0, "Unexpected compilation diagnostics");

        String balaName = ProjectUtils.getBalaName(buildProject.currentPackage().manifest());
        Path balaDir = testBuildDirectory.resolve("test_gen_balas");
        Path balaPath = balaDir.resolve(balaName);
        Files.createDirectories(balaDir);
        jBallerinaBackend.emit(JBallerinaBackend.OutputType.BALA, balaDir);

        // Load the bala file now.
        BalaProject balaProject = BalaProject.loadProject(BCompileUtil.getTestProjectEnvironmentBuilder(), balaPath);
        PackageResolution resolution = balaProject.currentPackage().getResolution();

        // Dependency graph should contain only one entry
        DependencyGraph<ResolvedPackageDependency> depGraphOfBala = resolution.dependencyGraph();
        Assert.assertEquals(depGraphOfBala.getNodes().size(), 1);
    }

    // TODO: enable after https://github.com/ballerina-platform/ballerina-lang/pull/31972 is merged
    @Test(description = "Ultimate test case", enabled = false)
    public void testProjectWithManyDependencies() {
        BCompileUtil.compileAndCacheBala(
                "projects_for_resolution_tests/ultimate_package_resolution/package_runtime");
        BCompileUtil.compileAndCacheBala(
                "projects_for_resolution_tests/ultimate_package_resolution/package_jsonutils");
        BCompileUtil.compileAndCacheBala(
                "projects_for_resolution_tests/ultimate_package_resolution/package_io_1_4_2");
        BCompileUtil.compileAndCacheBala(
                "projects_for_resolution_tests/ultimate_package_resolution/package_io_1_5_0");
        BCompileUtil.compileAndCacheBala(
                "projects_for_resolution_tests/ultimate_package_resolution/package_cache");

        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        long initialOpenCount = 0;
        if (os instanceof UnixOperatingSystemMXBean) {
            UnixOperatingSystemMXBean unixOperatingSystemMXBean = (UnixOperatingSystemMXBean) os;
            initialOpenCount = unixOperatingSystemMXBean.getOpenFileDescriptorCount();
        }
        Project project = BCompileUtil.loadProject(
                "projects_for_resolution_tests/ultimate_package_resolution/package_http");

        PackageCompilation compilation = project.currentPackage().getCompilation();
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_11);
        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = jBallerinaBackend.diagnosticResult();
        diagnosticResult.errors().forEach(OUT::println);
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 0, "Unexpected compilation diagnostics");

        if (os instanceof UnixOperatingSystemMXBean) {
            UnixOperatingSystemMXBean unixOperatingSystemMXBean = (UnixOperatingSystemMXBean) os;
            Assert.assertEquals(initialOpenCount, unixOperatingSystemMXBean.getOpenFileDescriptorCount());
        }

        Package currentPkg = project.currentPackage();
        Assert.assertEquals(currentPkg.packageDependencies().size(), 3);
        DependencyGraph<ResolvedPackageDependency> dependencyGraph = compilation.getResolution().dependencyGraph();

        for (ResolvedPackageDependency graphNode : dependencyGraph.getNodes()) {
            Collection<ResolvedPackageDependency> directDeps = dependencyGraph.getDirectDependencies(graphNode);
            PackageManifest manifest = graphNode.packageInstance().manifest();
            switch (manifest.name().value()) {
                case "io":
                    // Version conflict resolution has happened
                    Assert.assertEquals(manifest.version().toString(), "1.5.0");
                    break;
                case "http":
                    Assert.assertEquals(directDeps.size(), 3);
                    break;
                case "cache":
                    // No test dependencies are available in the graph
                    Assert.assertEquals(directDeps.size(), 1);
                    break;
                case "jsonutils":
                    Assert.assertEquals(graphNode.scope(), PackageDependencyScope.TEST_ONLY);
                    break;
                default:
                    throw new IllegalStateException("Unexpected dependency");
            }
        }
    }

    @Test(description = "tests loading a valid bala project")
    public void testBalaProjectDependencyResolution() {
        Path balaPath = getBalaPath("samjs", "package_b", "0.1.0");
        ProjectEnvironmentBuilder defaultBuilder = ProjectEnvironmentBuilder.getDefaultBuilder();
        defaultBuilder.addCompilationCacheFactory(TempDirCompilationCache::from);
        BalaProject balaProject = BalaProject.loadProject(defaultBuilder, balaPath);
        PackageResolution resolution = balaProject.currentPackage().getResolution();
        DependencyGraph<ResolvedPackageDependency> dependencyGraph = resolution.dependencyGraph();
        List<ResolvedPackageDependency> nodeInGraph = dependencyGraph.toTopologicallySortedList();
        Assert.assertEquals(nodeInGraph.size(), 2);
    }
    // For this to be enabled, #31026 should be fixed.
    @Test(enabled = false, dependsOnMethods = "testResolveDependencyFromUnsupportedCustomRepo")
    public void testResolveDependencyFromCustomRepo() {
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_b");
        String dependencyContent = "[[dependency]]\n" +
                "org = \"samjs\"\n" +
                "name = \"package_c\"\n" +
                "version = \"0.1.0\"\n" +
                "repository = \"local\"";

        // 1) load the build project
        Environment environment = EnvironmentBuilder.getBuilder().setUserHome(USER_HOME).build();
        ProjectEnvironmentBuilder projectEnvironmentBuilder = ProjectEnvironmentBuilder.getBuilder(environment);
        BuildProject project = TestUtils.loadBuildProject(projectEnvironmentBuilder, projectDirPath);

        // 2) set local repository to dependency
        project.currentPackage().dependenciesToml().orElseThrow().modify().withContent(dependencyContent).apply();

        // 3) Compile and check the diagnostics
        PackageCompilation compilation = project.currentPackage().getCompilation();
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();

        // 4) The dependency is expected to load from distribution cache, hence zero diagnostics
        Assert.assertEquals(diagnosticResult.errorCount(), 2);
    }

    // For this to be enabled, #31026 should be fixed.
    @Test (enabled = false)
    public void testResolveDependencyFromUnsupportedCustomRepo() {
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_b");
        String dependencyContent = "[[dependency]]\n" +
                "org = \"samjs\"\n" +
                "name = \"package_c\"\n" +
                "version = \"0.1.0\"\n" +
                "repository = \"stdlib.local\"";

        // 2) load the build project
        Environment environment = EnvironmentBuilder.getBuilder().setUserHome(USER_HOME).build();
        ProjectEnvironmentBuilder projectEnvironmentBuilder = ProjectEnvironmentBuilder.getBuilder(environment);
        BuildProject project = TestUtils.loadBuildProject(projectEnvironmentBuilder, projectDirPath);

        // 3) set local repository to dependency compile the package and check diagnostics
        project.currentPackage().dependenciesToml().get().modify().withContent(dependencyContent).apply();
        PackageCompilation compilation = project.currentPackage().getCompilation();
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();

        // 4) The dependency is expected to load from distribution cache, hence zero diagnostics
        Assert.assertEquals(diagnosticResult.errorCount(), 3);
        List<String> diagnosticMsgs = diagnosticResult.errors().stream()
                .map(Diagnostic::message).collect(Collectors.toList());
        Assert.assertTrue(diagnosticMsgs.contains("cannot resolve module 'samjs/package_c.mod_c1 as mod_c1'"));
    }

    @Test(description = "tests resolution with invalid bala dependency", enabled = false)
    public void testProjectWithInvalidBalaDependency() throws IOException {
        // package_x --> package_bash/soap
        Path balaPath = RESOURCE_DIRECTORY.resolve("balas").resolve("invalid")
                .resolve("bash-soap-any-0.1.0.bala");
        BCompileUtil.copyBalaToDistRepository(balaPath, "bash", "soap", "0.1.0");

        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_x_with_invalid_bala_dep");
        BuildProject buildProject = TestUtils.loadBuildProject(projectDirPath);
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        diagnosticResult.errors().forEach(OUT::println);
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 6, "Unexpected compilation diagnostics");

        Iterator<Diagnostic> diagnosticIterator = diagnosticResult.diagnostics().iterator();
        // Check invalid bala diagnostics
        // TODO: Following logs should be asserted after fixing the diagnostics order issue
        // TODO: https://github.com/ballerina-platform/ballerina-lang/issues/31082
        // TODO: 1. ERROR [foo.bal:(1:1,1:18)] invalid bala file:
        // TODO: 2. ERROR [bar.bal:(3:1,3:18)] invalid bala file:
        Assert.assertTrue(diagnosticIterator.next().toString().contains("invalid bala file:"));
        Assert.assertTrue(diagnosticIterator.next().toString().contains("invalid bala file:"));
        // Check syntax diagnostics
        Assert.assertEquals(diagnosticIterator.next().toString(),
                            "ERROR [bar.bal:(3:1,3:18)] cannot resolve module 'bash/soap'");
        Assert.assertEquals(diagnosticIterator.next().toString(),
                            "ERROR [bar.bal:(6:1,6:1)] missing semicolon token");
        Assert.assertEquals(diagnosticIterator.next().toString(),
                            "ERROR [foo.bal:(1:1,1:18)] cannot resolve module 'bash/soap'");
        Assert.assertEquals(diagnosticIterator.next().toString(),
                            "ERROR [foo.bal:(5:1,5:1)] missing semicolon token");
    }

    @Test(description = "tests resolution with invalid transitive bala dependency", enabled = false)
    public void testProjectWithInvalidTransitiveBalaDependency() throws IOException {
        // package_hello --> package_zip
        // package_xx    --> package_hello
        Path zipBalaPath = RESOURCE_DIRECTORY.resolve("balas").resolve("invalid")
                .resolve("zip-2020r1-java8-1.0.4.balo");
        BCompileUtil.copyBalaToDistRepository(zipBalaPath, "hemikak", "zip", "1.0.4");
        Path helloBalaPath = RESOURCE_DIRECTORY.resolve("balas").resolve("invalid")
                .resolve("hello-2020r1-any-0.1.0.balo");
        BCompileUtil.copyBalaToDistRepository(helloBalaPath, "bache", "hello", "0.1.0");

        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_xx_with_invalid_transitive_bala_dep");
        BuildProject buildProject = TestUtils.loadBuildProject(projectDirPath);
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        diagnosticResult.errors().forEach(OUT::println);
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 4, "Unexpected compilation diagnostics");

        Iterator<Diagnostic> diagnosticIterator = diagnosticResult.diagnostics().iterator();
        // Check invalid bala diagnostics
        Assert.assertTrue(diagnosticIterator.next().toString().contains(
                "ERROR [foo.bal:(1:1,1:20)] invalid bala file:"));
        // Check syntax diagnostics
        Assert.assertEquals(diagnosticIterator.next().toString(),
                            "ERROR [foo.bal:(1:1,1:20)] cannot resolve module 'bache/hello'");
        Assert.assertEquals(diagnosticIterator.next().toString(),
                            "ERROR [foo.bal:(4:20,4:39)] undefined function 'zip'");
        Assert.assertEquals(diagnosticIterator.next().toString(),
                            "ERROR [foo.bal:(4:20,4:39)] undefined module 'hello'");
    }

    @Test(description = "tests package name resolution response")
    public void testPackageNameResolution() {
        DefaultPackageResolver mockResolver = mock(DefaultPackageResolver.class);

        //dummyRequest
        List<ImportModuleRequest> moduleRequests = new ArrayList<>();
        moduleRequests.add(new ImportModuleRequest(PackageOrg.from("ballerina"), "java.arrays"));
        moduleRequests.add(new ImportModuleRequest(PackageOrg.from("ballerina"), "sample.module"));

        //dummyResponse
        List<ImportModuleResponse> moduleResponse = new ArrayList<>();
        for (ImportModuleRequest request : moduleRequests) {
            String[] parts = request.moduleName().split("[.]");
            moduleResponse.add(new ImportModuleResponse(
                    PackageDescriptor.from(request.packageOrg(), PackageName.from(parts[0])), request));
        }

        when(mockResolver.resolvePackageNames(any(), any(ResolutionOptions.class))).thenReturn(moduleResponse);

        Assert.assertEquals(mockResolver.resolvePackageNames(moduleRequests,
                ResolutionOptions.builder().build()).size(), 2);
    }

    @Test(description = "tests resolution for dependency given in Ballerina.toml without repository")
    public void testPackageResolutionOfDependencyMissingRepository() {
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_y_having_dependency_missing_repo");
        BuildProject buildProject = TestUtils.loadBuildProject(projectDirPath);
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        diagnosticResult.errors().forEach(OUT::println);
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 4, "Unexpected compilation diagnostics");

        Iterator<Diagnostic> diagnosticIterator = diagnosticResult.diagnostics().iterator();
        // Check dependency repository is not given diagnostic
        Assert.assertTrue(diagnosticIterator.next().toString().contains(
                "ERROR [Ballerina.toml:(6:1,9:18)] 'repository' under [[dependency]] is missing"));
        // Check dependency cannot be resolved diagnostic
        Assert.assertEquals(diagnosticIterator.next().toString(),
                            "ERROR [fee.bal:(1:1,1:16)] cannot resolve module 'ccc/ddd'");
        Assert.assertEquals(diagnosticIterator.next().toString(),
                            "ERROR [fee.bal:(4:2,4:27)] undefined function 'notExistingFunction'");
        Assert.assertEquals(diagnosticIterator.next().toString(),
                            "ERROR [fee.bal:(4:2,4:27)] undefined module 'ddd'");
    }
}
