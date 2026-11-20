/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.projects.test;

import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.util.ProjectUtils;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.ballerina.projects.util.ProjectConstants.CACHES_DIR_NAME;
import static io.ballerina.projects.util.ProjectConstants.DIST_CACHE_DIRECTORY;

/**
 * Tests building against a dependency whose cached BIR was compiled against another build of its own dependency.
 *
 * @since 2201.14.3
 */
public class CachedBirDependencyTests {

    private static final Path RESOURCE_DIRECTORY =
            Path.of("src/test/resources/projects_for_cached_bir_tests").toAbsolutePath();
    private static final Path DIST_REPO = Path.of("build").toAbsolutePath().normalize().resolve(DIST_CACHE_DIRECTORY);
    private static final Path ORG_CACHE = DIST_REPO.resolve(CACHES_DIR_NAME).resolve("cachedbir");
    private static final PrintStream OUT = System.out;
    private Path tempResourceDir;

    @BeforeClass
    public void setUp() throws IOException {
        tempResourceDir = Files.createTempDirectory("cached-bir-tests");
        FileUtils.copyDirectory(RESOURCE_DIRECTORY.toFile(), tempResourceDir.toFile());
        // package_a --> package_b; both land in the distribution repository with their BIRs cached.
        BCompileUtil.compileAndCacheBala(tempResourceDir.resolve("package_b"), DIST_REPO,
                BCompileUtil.getTestProjectEnvironmentBuilder());
        BCompileUtil.compileAndCacheBala(tempResourceDir.resolve("package_a"), DIST_REPO,
                BCompileUtil.getTestProjectEnvironmentBuilder());
    }

    @Test(description = "Compile a cached dependency from sources when its own dependency is compiled from sources")
    public void testCachedBirWithDependencyCompiledFromSources() throws IOException {
        Assert.assertTrue(Files.exists(ORG_CACHE.resolve("package_a")), "package_a BIR is not cached");
        // package_a keeps its cached BIR; package_b loses its own, so the build compiles it from the bala sources.
        ProjectUtils.deleteDirectory(ORG_CACHE.resolve("package_b"));

        BuildProject buildProject = TestUtils.loadBuildProject(tempResourceDir.resolve("package_app"));
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        diagnosticResult.diagnostics().forEach(OUT::println);
        Assert.assertEquals(diagnosticResult.errorCount(), 0, "Unexpected compilation errors");
    }

    @Test(description = "Report a missing dependency of a cached module as a diagnostic",
            dependsOnMethods = "testCachedBirWithDependencyCompiledFromSources")
    public void testCachedBirWithUnresolvedDependency() throws IOException {
        Assert.assertTrue(Files.exists(ORG_CACHE.resolve("package_a")), "package_a BIR is not cached");
        // package_a keeps its cached BIR; package_b is no longer available, so the build cannot resolve it.
        ProjectUtils.deleteDirectory(ORG_CACHE.resolve("package_b"));
        ProjectUtils.deleteDirectory(DIST_REPO.resolve("bala").resolve("cachedbir").resolve("package_b"));
        ProjectUtils.deleteDirectory(tempResourceDir.resolve("package_app").resolve("target"));
        Files.deleteIfExists(tempResourceDir.resolve("package_app").resolve("Dependencies.toml"));

        BuildProject buildProject = TestUtils.loadBuildProject(tempResourceDir.resolve("package_app"));
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        diagnosticResult.diagnostics().forEach(OUT::println);
        Assert.assertTrue(diagnosticResult.errors().stream().anyMatch(diagnostic ->
                        diagnostic.message().equals("cannot resolve module 'cachedbir/package_b'")),
                "Missing dependency is not reported");
    }

    @AfterClass
    public void cleanUp() throws IOException {
        ProjectUtils.deleteDirectory(ORG_CACHE);
        ProjectUtils.deleteDirectory(DIST_REPO.resolve("bala").resolve("cachedbir"));
        FileUtils.deleteDirectory(tempResourceDir.toFile());
    }
}
