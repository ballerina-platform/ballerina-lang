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
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.environment.ResolutionResponse;
import io.ballerina.projects.internal.ImportModuleRequest;
import io.ballerina.projects.internal.ImportModuleResponse;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains cases to test hierarchical package names.
 *
 * @since 2.0.0
 */
public class HierarchicalPackageNameTests {
    private static final Path RESOURCE_DIRECTORY = Paths.get(
            "src/test/resources/hierarchical_pkg_names").toAbsolutePath();
    private static final PrintStream out = System.out;
    Path customUserHome = Paths.get("build", "userHome");

    @BeforeTest
    public void setup() {
        BCompileUtil.compileAndCacheBala("hierarchical_pkg_names/package_a");
        BCompileUtil.compileAndCacheBala("hierarchical_pkg_names/package_a.b");
    }

    @Test(description = "tests a project with hierarchical package name")
    public void testProjectWithHierarchicalName() {
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_x.y.z");
        BuildProject buildProject = BuildProject.load(projectDirPath);
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        diagnosticResult.errors().forEach(out::println);
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 0, "Unexpected compilation diagnostics");

        // Check direct package dependencies
        Assert.assertEquals(buildProject.currentPackage().packageDependencies().size(), 0,
                "Unexpected number of dependencies");
    }

    @Test(description = "tests a project with dependencies to packages with hierarchical names")
    public void testDependenciesWithHierarchicalNames() {
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_app1");
        BuildProject buildProject = BuildProject.load(projectDirPath);
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        diagnosticResult.errors().forEach(out::println);
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 0, "Unexpected compilation diagnostics");

        // Check direct package dependencies
        Assert.assertEquals(buildProject.currentPackage().packageDependencies().size(), 2,
                "Unexpected number of dependencies");
    }

    @Test
    public void testResolveHierarchicalPackageInDist() {
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_x.y.z");
        Environment environment = EnvironmentBuilder.getBuilder().setUserHome(customUserHome).build();
        ProjectEnvironmentBuilder projectEnvironmentBuilder = ProjectEnvironmentBuilder.getBuilder(environment);
        BuildProject project = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        PackageResolver packageResolver = project.projectEnvironmentContext().getService(PackageResolver.class);
        ImportModuleRequest request1 = new ImportModuleRequest(
                PackageOrg.from("samjs"), "a.c", Collections.emptyList());
        ImportModuleRequest request2 = new ImportModuleRequest(
                PackageOrg.from("samjs"), "a.b", Collections.emptyList());

        PackageDescriptor possiblePkg1 = PackageDescriptor.from(
                PackageOrg.from("samjs"), PackageName.from("a"), PackageVersion.from("1.0.0"));
        PackageDescriptor possiblePkg2 = PackageDescriptor.from(
                PackageOrg.from("samjs"), PackageName.from("a.b"), PackageVersion.from("1.1.0"));

        List<PackageDescriptor> possiblePackages = new ArrayList<>();
        possiblePackages.add(possiblePkg1);
        possiblePackages.add(possiblePkg2);
        ImportModuleRequest request3 = new ImportModuleRequest(
                PackageOrg.from("samjs"), "a.b.c", possiblePackages);
        ImportModuleRequest request4 = new ImportModuleRequest(
                PackageOrg.from("samjs"), "a.b.x", Collections.emptyList());

        List<ImportModuleRequest> importModuleRequests = new ArrayList<>();
        importModuleRequests.add(request1);
        importModuleRequests.add(request2);
        importModuleRequests.add(request3);
        importModuleRequests.add(request4);
        List<ImportModuleResponse> importModuleResponseList = packageResolver.resolvePackageNames(importModuleRequests);
        Assert.assertEquals(importModuleResponseList.size(), 4);

        for (ImportModuleResponse importModuleResponse : importModuleResponseList) {
            if (importModuleResponse.resolutionStatus().equals(ResolutionResponse.ResolutionStatus.UNRESOLVED)) {
                Assert.assertEquals(importModuleResponse.importModuleRequest(), request4);
            } else if (importModuleResponse.importModuleRequest().moduleName().equals("a.c")) {
                Assert.assertEquals(importModuleResponse.packageDescriptor().name().toString(), "a");
            } else if (importModuleResponse.importModuleRequest().moduleName().equals("a.b.c")) {
                Assert.assertEquals(importModuleResponse.packageDescriptor().name().toString(), "a.b");
            } else {
                Assert.assertEquals(importModuleResponse.packageDescriptor().name().toString(), "a.b");
            }
        }
    }

    @Test(dependsOnMethods = "testResolveHierarchicalPackageInDist")
    public void testResolveDifferentPackagesFromEachRepo() {
        Path centralCache = customUserHome.resolve("repositories/central.ballerina.io");
        BCompileUtil.compileAndCacheBala("hierarchical_pkg_names/package_a.c", centralCache);
        BCompileUtil.compileAndCacheBala("hierarchical_pkg_names/package_a_latest", centralCache);

        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_x.y.z");
        Environment environment = EnvironmentBuilder.getBuilder().setUserHome(customUserHome).build();
        ProjectEnvironmentBuilder projectEnvironmentBuilder = ProjectEnvironmentBuilder.getBuilder(environment);
        BuildProject project = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        PackageResolver packageResolver = project.projectEnvironmentContext().getService(PackageResolver.class);

        ImportModuleRequest request1 = new ImportModuleRequest(
                PackageOrg.from("samjs"), "a.c", Collections.emptyList());
        ImportModuleRequest request2 = new ImportModuleRequest(
                PackageOrg.from("samjs"), "a.b", Collections.emptyList());
        List<ImportModuleRequest> importModuleRequests = new ArrayList<>();
        importModuleRequests.add(request1);
        importModuleRequests.add(request2);

        List<ImportModuleResponse> importModuleResponseList = packageResolver.resolvePackageNames(importModuleRequests);
        Assert.assertEquals(importModuleResponseList.size(), 2);
        for (ImportModuleResponse importModuleResponse : importModuleResponseList) {
            if (importModuleResponse.importModuleRequest().moduleName().equals("a.c")) {
                Assert.assertEquals(importModuleResponse.packageDescriptor().name().toString(), "a.c");
            } else {
                Assert.assertEquals(importModuleResponse.packageDescriptor().name().toString(), "a.b");
            }
        }

    }
}
