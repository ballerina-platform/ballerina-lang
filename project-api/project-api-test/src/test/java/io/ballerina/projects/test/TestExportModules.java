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

import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.bala.BalaProject;
import io.ballerina.projects.repos.TempDirCompilationCache;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Contains cases to test export modules.
 *
 * @since 2.0.0
 */
public class TestExportModules {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src", "test", "resources");
    private static final Path EXPORT_MODULES = RESOURCE_DIRECTORY.resolve("export_modules");

    @Test(description = "tests loading a valid bala project with export in package.json")
    public void testBalaProjectWithExportModules() {
        Path balaDirPath = EXPORT_MODULES.resolve("bala_with_export");
        // Initialize the project instance
        BalaProject balaProject = null;
        try {
            ProjectEnvironmentBuilder defaultBuilder = ProjectEnvironmentBuilder.getDefaultBuilder();
            defaultBuilder.addCompilationCacheFactory(TempDirCompilationCache::from);
            balaProject = BalaProject.loadProject(defaultBuilder, balaDirPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }

        Assert.assertEquals(balaProject.currentPackage().manifest().exportedModules().size(), 2);
        Assert.assertEquals(balaProject.currentPackage().manifest().exportedModules().get(0), "winery");
        Assert.assertEquals(balaProject.currentPackage().manifest().exportedModules().get(1), "winery.service");
    }

    @Test(description = "tests loading a valid bala project without export in package.json")
    public void testBalaProjectWithoutExportModules() {
        Path balaDirPath = EXPORT_MODULES.resolve("bala_without_export");
        // Initialize the project instance
        BalaProject balaProject = null;
        try {
            ProjectEnvironmentBuilder defaultBuilder = ProjectEnvironmentBuilder.getDefaultBuilder();
            defaultBuilder.addCompilationCacheFactory(TempDirCompilationCache::from);
            balaProject = BalaProject.loadProject(defaultBuilder, balaDirPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }

        // Default module should exists in exported modules
        Assert.assertEquals(balaProject.currentPackage().manifest().exportedModules().size(), 1);
        Assert.assertEquals(balaProject.currentPackage().manifest().exportedModules().get(0), "winery");
    }

    @Test(description = "test build project with export entry in Ballerina.toml")
    public void testBuildProjectWithExportModules() {
        Path projectPath = EXPORT_MODULES.resolve("build_project_with_export");
        Project project = TestUtils.loadBuildProject(projectPath);

        PackageCompilation packageCompilation = project.currentPackage().getCompilation();
        if (packageCompilation.diagnosticResult().hasErrors()) {
            Assert.fail("compilation failed:" + packageCompilation.diagnosticResult().errors());
        }

        Assert.assertFalse(project.currentPackage().manifest().exportedModules().isEmpty());
        Assert.assertEquals(project.currentPackage().manifest().exportedModules().get(0), "winery");
        Assert.assertEquals(project.currentPackage().manifest().exportedModules().get(1), "winery.services");
    }

    @Test(description = "test build project without export entry in Ballerina.toml")
    public void testBuildProjectWithoutExportModules() {
        Path projectPath = EXPORT_MODULES.resolve("build_project_without_export");
        Project project = TestUtils.loadBuildProject(projectPath);

        PackageCompilation packageCompilation = project.currentPackage().getCompilation();
        if (packageCompilation.diagnosticResult().hasErrors()) {
            Assert.fail("compilation failed:" + packageCompilation.diagnosticResult().errors());
        }

        // Default module should exists in exported modules
        Assert.assertFalse(project.currentPackage().manifest().exportedModules().isEmpty());
        Assert.assertEquals(project.currentPackage().manifest().exportedModules().get(0), "winery");
    }

    @Test(description = "test build project has non-exported module as an import")
    public void testBuildProjectHasNonExportedModuleImport() {
        CompileResult depCompileResult = BCompileUtil.compileAndCacheBala("export_modules/build_project_with_export");
        if (depCompileResult.getErrorCount() > 0) {
            Arrays.stream(depCompileResult.getDiagnostics()).forEach(System.out::println);
            Assert.fail("Compilation contains errors");
        }
        CompileResult compileResult = BCompileUtil.compile("export_modules/build_project_with_non_export_import");
        Assert.assertEquals(compileResult.getErrorCount(), 3);
        Assert.assertTrue(compileResult.getDiagnostics()[0].message()
                                  .contains("cannot resolve module 'foo/winery.storage:0.1.0 is not exported'"));
    }

    @Test(description = "test build project has non-exported, same package module as an import")
    public void testBuildProjectHasSamePackageNonExportedModuleImport() {
        CompileResult compileResult =
                BCompileUtil.compile("export_modules/build_project_with_non_export_same_package_import");
        if (compileResult.getErrorCount() > 0) {
            Arrays.stream(compileResult.getDiagnostics()).forEach(System.out::println);
            Assert.fail("Compilation contains errors");
        }
        Assert.assertEquals(compileResult.getErrorCount(), 0);
    }
}
