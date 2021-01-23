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
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Contains cases to test hierarchical package names.
 *
 * @since 2.0.0
 */
public class HierarchicalPackageNameTests {
    private static final Path RESOURCE_DIRECTORY = Paths.get(
            "src/test/resources/hierarchical_pkg_names").toAbsolutePath();
    private static final PrintStream out = System.out;

    @BeforeTest
    public void setup() {
        BCompileUtil.compileAndCacheBalo("hierarchical_pkg_names/package_a");
        BCompileUtil.compileAndCacheBalo("hierarchical_pkg_names/package_a.b");
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
}
