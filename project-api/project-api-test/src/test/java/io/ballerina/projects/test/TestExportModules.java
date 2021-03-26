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

import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.bala.BalaProject;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.repos.TempDirCompilationCache;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Contains cases to test export modules.
 *
 * @since 2.0.0
 */
public class TestExportModules extends BaseTest {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src", "test", "resources");
    private static final Path EXPORT_MODULES = RESOURCE_DIRECTORY.resolve("export_modules");

    @Test(description = "tests loading a valid bala project with export in package.json")
    public void testBalaProjectWithExportModules() {
        Path balaPath = EXPORT_MODULES.resolve("bala_with_export").resolve("foo-winery-any-0.1.0.bala");
        // Initialize the project instance
        BalaProject balaProject = null;
        try {
            ProjectEnvironmentBuilder defaultBuilder = ProjectEnvironmentBuilder.getDefaultBuilder();
            defaultBuilder.addCompilationCacheFactory(TempDirCompilationCache::from);
            balaProject = BalaProject.loadProject(defaultBuilder, balaPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }

        Assert.assertEquals(balaProject.currentPackage().manifest().export().size(), 2);
        Assert.assertEquals(balaProject.currentPackage().manifest().export().get(0), "winery");
        Assert.assertEquals(balaProject.currentPackage().manifest().export().get(1), "winery.service");
    }

    @Test(description = "tests loading a valid bala project without export in package.json")
    public void testBalaProjectWithoutExportModules() {
        Path balaPath = EXPORT_MODULES.resolve("bala_without_export").resolve("foo-winery-any-0.1.0.bala");
        // Initialize the project instance
        BalaProject balaProject = null;
        try {
            ProjectEnvironmentBuilder defaultBuilder = ProjectEnvironmentBuilder.getDefaultBuilder();
            defaultBuilder.addCompilationCacheFactory(TempDirCompilationCache::from);
            balaProject = BalaProject.loadProject(defaultBuilder, balaPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }

        // Default module should exists in exported modules
        Assert.assertEquals(balaProject.currentPackage().manifest().export().size(), 1);
        Assert.assertEquals(balaProject.currentPackage().manifest().export().get(0), "winery");
    }

    @Test(description = "test build project with export entry in Ballerina.toml")
    public void testBuildProjectWithExportModules() {
        Path projectPath = EXPORT_MODULES.resolve("build_project_with_export");
        Project project = BuildProject.load(projectPath);

        PackageCompilation packageCompilation = project.currentPackage().getCompilation();
        if (packageCompilation.diagnosticResult().hasErrors()) {
            Assert.fail("compilation failed:" + packageCompilation.diagnosticResult().errors());
        }

        Assert.assertFalse(project.currentPackage().manifest().export().isEmpty());
        Assert.assertEquals(project.currentPackage().manifest().export().get(0), "winery");
        Assert.assertEquals(project.currentPackage().manifest().export().get(1), "winery.service");
    }

    @Test(description = "test build project without export entry in Ballerina.toml")
    public void testBuildProjectWithoutExportModules() {
        Path projectPath = EXPORT_MODULES.resolve("build_project_without_export");
        Project project = BuildProject.load(projectPath);

        PackageCompilation packageCompilation = project.currentPackage().getCompilation();
        if (packageCompilation.diagnosticResult().hasErrors()) {
            Assert.fail("compilation failed:" + packageCompilation.diagnosticResult().errors());
        }

        // Default module should exists in exported modules
        Assert.assertFalse(project.currentPackage().manifest().export().isEmpty());
        Assert.assertEquals(project.currentPackage().manifest().export().get(0), "winery");
    }
}
