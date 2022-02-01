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
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.PackageDependencyScope;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageResolution;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.directory.BuildProject;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * Contains cases to test pre-release version resolution logic.
 *
 * @since 2.0.0
 */
public class PreReleaseVersionResolutionTests {
    private static final Path RESOURCE_DIRECTORY = Paths.get(
            "src/test/resources/projects_for_resolution_tests").toAbsolutePath();
    static final PrintStream OUT = System.out;

    @BeforeSuite
    public void init() {
        BCompileUtil.compileAndCacheBala("projects_for_resolution_tests/package_unstable_k_alpha");
        BCompileUtil.compileAndCacheBala("projects_for_resolution_tests/package_unstable_k_beta");
        BCompileUtil.compileAndCacheBala("projects_for_resolution_tests/package_unstable_k_GA");
        BCompileUtil.compileAndCacheBala("projects_for_resolution_tests/package_l_with_unstable_dep");
    }

    @Test(description = "tests projects with pre-release versions")
    public void testProjectWithAPreReleaseVersion() {
        // package_unstable_k_alpha has no dependencies and its version is 1.1.0-alpha
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_unstable_k_alpha");
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

    @Test(description = "Two pre-release versions (specified by the user) in the dependency graph")
    public void testTwoCompatiblePreReleaseVersionsInDependencyGraph() {
        // package_m_with_unstable_dep --> package_l (2.1.0) --> package_k (1.1.0-beta)
        // package_l (2.1.0) --> package_k (1.1.0-alpha)
        // This test case should fail. There are two incompatible versions of package-k in the dependency graph of
        //  package_m_with_unstable_dep.
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_m_with_unstable_dep");
        BuildProject buildProject = TestUtils.loadBuildProject(projectDirPath);
        buildProject.currentPackage().getResolution();
    }

    @Test(description = "Two pre-release versions in the dependency graph, one is specified by the user, " +
            "other one is not")
    public void testDependenciesWithPreReleaseVersions() {
        // package_m_with_unstable_transitive_dep --> package_l (2.1.0) --> package_k (1.1.0-alpha)
        // package_m_with_unstable_transitive_dep --> package_k (version is not specified)
        // This test case should not fail. The package_k version in the graph should be 1.1.0-alpha
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_m_with_unstable_transitive_dep");
        BuildProject buildProject = TestUtils.loadBuildProject(projectDirPath);
        buildProject.currentPackage().getResolution();
    }

    @Test(description = "There are pre-release and stables versions of a package dependency available in " +
            "a repository. The resolution logic should pic the latest stable version in that case")
    public void testVersionResolutionWithPreReleaseVersion() {
        // package_l_with_stable_dep --> package_k (version is not specified)
        // There are three versions of package_k in the repository: 1.1.0-alpha, 1.1.0-beta and 1.0.0
        // The resolution logic should pick 1.0.0
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_l_with_stable_dep");
        BuildProject buildProject = TestUtils.loadBuildProject(projectDirPath);
        Package currentPackage = buildProject.currentPackage();
        PackageResolution resolution = currentPackage.getResolution();
        final Collection<ResolvedPackageDependency> directDependencies =
                resolution.dependencyGraph().getDirectDependencies(
                        new ResolvedPackageDependency(currentPackage, PackageDependencyScope.DEFAULT));
        ResolvedPackageDependency resolvedDependency = directDependencies.iterator().next();
        Assert.assertNotNull(resolvedDependency);
        Package pkgDependency = resolvedDependency.packageInstance();
        PackageDescriptor pkdDesc = pkgDependency.descriptor();
        Assert.assertEquals(pkdDesc.name(), PackageName.from("package_k"));
        Assert.assertEquals(pkdDesc.version(), PackageVersion.from("1.0.0"));
    }
}
