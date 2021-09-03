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
import io.ballerina.projects.bala.BalaProject;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.repos.TempDirCompilationCache;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Contains concurrency tests for project loading.
 *
 * @since 2.0.0
 */
public class TestConcurrentProjectAccess {

    private static final Path RESOURCE_DIRECTORY = Paths.get(
            "src/test/resources/projects_for_resolution_tests").toAbsolutePath();
    private static final Path REPO_BALA_DIRECTORY = Paths.get(
            "build/repo/bala/").toAbsolutePath();

    @Test(threadPoolSize = 50, invocationCount = 50,  timeOut = 30000)
    public void testConcurrentProjectBuild() {
        // package_a --> package_b --> package_c
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_a");
        BuildProject buildProject = TestUtils.loadBuildProject(projectDirPath);
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 0, "Unexpected compilation diagnostics");

        // Check direct package dependencies
        Assert.assertEquals(buildProject.currentPackage().packageDependencies().size(), 1,
                "Unexpected number of dependencies");
    }

    @Test(threadPoolSize = 50, invocationCount = 50,  timeOut = 30000)
    public void testConcurrentBalaProjectLoad() {
        // package_b --> package_c
        Path balaPath = REPO_BALA_DIRECTORY.resolve("samjs").resolve("package_b").resolve("0.1.0").resolve("any");
        ProjectEnvironmentBuilder defaultBuilder = ProjectEnvironmentBuilder.getDefaultBuilder();
        defaultBuilder.addCompilationCacheFactory(TempDirCompilationCache::from);
        BalaProject balaProject = BalaProject.loadProject(defaultBuilder, balaPath);
        PackageCompilation compilation = balaProject.currentPackage().getCompilation();

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 0, "Unexpected compilation diagnostics");

        // Check direct package dependencies
        Assert.assertEquals(balaProject.currentPackage().packageDependencies().size(), 1,
                "Unexpected number of dependencies");
    }
}
