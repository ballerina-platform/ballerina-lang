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

import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.directory.BuildProject;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Contains cases to test package resolution logic.
 *
 * @since 2.0.0
 */
public class PackageResolutionTests {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/projects_for_resolution_tests");
    private static final PrintStream out = System.out;

    @BeforeTest
    public void setup() {
        // Here package_a depends on package_b
        // and package_b depends on package_c
        // Therefore package_c is transitive dependency of package_a
        BCompileUtil.compileAndCacheBalo("projects_for_resolution_tests/package_c");
        BCompileUtil.compileAndCacheBalo("projects_for_resolution_tests/package_b");
        BCompileUtil.compileAndCacheBalo("projects_for_resolution_tests/package_e");
    }

    @Test(description = "tests resolution with zero direct dependencies")
    public void testProjectWithZeroDependencies() {
        // package_c --> {}
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_c");
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

    @Test(description = "tests resolution with one direct dependency")
    public void testProjectWithOneDependency() {
        // package_b --> package_c
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_b");
        BuildProject buildProject = BuildProject.load(projectDirPath);
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        diagnosticResult.errors().forEach(out::println);
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 0, "Unexpected compilation diagnostics");

        // Check direct package dependencies
        Assert.assertEquals(buildProject.currentPackage().packageDependencies().size(), 1,
                "Unexpected number of dependencies");
    }

    @Test(description = "tests resolution with one transitive dependency")
    public void testProjectWithOneTransitiveDependency() {
        // package_a --> package_b --> package_c
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_a");
        BuildProject buildProject = BuildProject.load(projectDirPath);
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        diagnosticResult.errors().forEach(out::println);
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
}
