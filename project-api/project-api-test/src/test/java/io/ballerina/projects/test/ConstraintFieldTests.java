/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Contains test cases to test constraint field.
 *
 * @since 2201.3.0
 */
public class ConstraintFieldTests {
    private static final Path RESOURCE_DIRECTORY = Paths.get(
            "src/test/resources/projects_for_constraint_field_tests").toAbsolutePath();
    private static final Path testBuildDirectory = Paths.get("build").toAbsolutePath();

    @DataProvider(name = "provideDataForConstraintFieldFormatValidation")
    private Object[][] provideDataForConstraintFieldFormatValidation() {
        Object[][] data = new Object[10][2];
        // package_a - empty string
        data[0] = new Object[]{"package_a", "ERROR [Ballerina.toml:(5:12,5:14)] invalid 'constraint' under " +
                "[package]: 'constraint' can only contain two strings of alphanumerics and underscores separated " +
                "by a forward slash"};
        // package_b - no forward slash
        data[1] = new Object[]{"package_b", "ERROR [Ballerina.toml:(5:12,5:17)] invalid 'constraint' under " +
                "[package]: 'constraint' can only contain two strings of alphanumerics and underscores separated " +
                "by a forward slash"};
        // package_c - no org part
        data[2] = new Object[]{"package_c", "ERROR [Ballerina.toml:(5:12,5:24)] invalid 'constraint' under " +
                "[package]: 'constraint' can only contain two strings of alphanumerics and underscores separated " +
                "by a forward slash"};
        // package_d - consecutive underscores
        data[3] = new Object[]{"package_d", "ERROR [Ballerina.toml:(5:12,5:29)] invalid 'constraint' under " +
                "[package]: 'constraint' cannot have consecutive underscore characters"};
        // package_e - org with initial underscore
        data[4] = new Object[]{"package_e", "ERROR [Ballerina.toml:(5:12,5:28)] invalid 'constraint' under " +
                "[package]: org part of 'constraint' cannot have initial underscore characters"};
        // package_f - name with initial underscore
        data[5] = new Object[]{"package_f", "ERROR [Ballerina.toml:(5:12,5:28)] invalid 'constraint' under " +
                "[package]: name part of 'constraint' cannot have initial underscore characters"};
        // package_g - org with trailing underscore
        data[6] = new Object[]{"package_g", "ERROR [Ballerina.toml:(5:12,5:28)] invalid 'constraint' under " +
                "[package]: org part of 'constraint' cannot have trailing underscore characters"};
        // package_h - name with trailing underscore
        data[7] = new Object[]{"package_h", "ERROR [Ballerina.toml:(5:12,5:28)] invalid 'constraint' under " +
                "[package]: name part of 'constraint' cannot have trailing underscore characters"};
        // package_i - org with initial numeric chars
        data[8] = new Object[]{"package_i", "ERROR [Ballerina.toml:(5:12,5:28)] invalid 'constraint' under " +
                "[package]: org part of 'constraint' cannot have initial numeric characters"};
        // package_j - name with initial numeric chars
        data[9] = new Object[]{"package_j", "ERROR [Ballerina.toml:(5:12,5:28)] invalid 'constraint' under " +
                "[package]: name part of 'constraint' cannot have initial numeric characters"};

        return data;
    }

    @Test(description = "Test a project with invalidly formatted constraint field. Should generate an error diagnostic",
            dataProvider = "provideDataForConstraintFieldFormatValidation")
    public void testProjectWithInvalidFormatConstraintField(String packageName, String diagnosticMessage) {
        Path projectDirPath = RESOURCE_DIRECTORY.resolve(packageName);
        BuildProject buildProject = TestUtils.loadBuildProject(projectDirPath);
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 1, "Invalid compilation diagnostics count");
        Assert.assertEquals(diagnosticResult.diagnostics().iterator().next().toString(), diagnosticMessage);

        // Check if compiler plugin is added as a dependency
        Assert.assertEquals(buildProject.currentPackage().packageDependencies().size(), 0,
                "Unexpected number of dependencies");
    }

    @Test(description = "Test a project with a non-existent constraint package. Should not import")
    public void testProjectWithNonExistentConstraintPackage() {
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_k");
        BuildProject buildProject = TestUtils.loadBuildProject(projectDirPath);
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        Assert.assertEquals(diagnosticResult.errorCount(), 0, "Invalid compilation diagnostics count");

        // Check if compiler plugin is added as a dependency
        Assert.assertEquals(buildProject.currentPackage().packageDependencies().size(), 0,
                "Unexpected number of dependencies");
    }

    @Test(description = "Test a project with valid constraint package")
    public void testProjectWithValidConstraintPackage() {
        BCompileUtil.compileAndCacheBala("projects_for_constraint_field_tests/constraint_packages/constraint1");

        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_l");

        BuildProject buildProject = TestUtils.loadBuildProject(projectDirPath);
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        Assert.assertEquals(diagnosticResult.errorCount(), 0, "Invalid compilation diagnostics count");
    }

    @Test(description = "Constraint package with non default modules. Should generate an error diagnostic")
    public void testProjectWithConstraintPackageWithNonDefaultModules() {
        BCompileUtil.compileAndCacheBala("projects_for_constraint_field_tests/constraint_packages/constraint2");

        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_m");

        BuildProject buildProject = TestUtils.loadBuildProject(projectDirPath);
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();

        Path buildProjectPath = testBuildDirectory.resolve(
                "repo/bala/foo/constraint2/0.1.0/any/modules/constraint2/constraint2");
        Assert.assertEquals(diagnosticResult.errorCount(), 1, "Invalid compilation diagnostics count");
        Assert.assertEquals(diagnosticResult.errors().iterator().next().toString(),
                "ERROR [foo/constraint2/0.1.0::" + buildProjectPath
                        + ":(1:1,1:1)] Invalid constraint package: 'foo/constraint2:0.1.0'");
    }

    @Test(description = "Constraint package which is not a compiler plugin. Should generate an error diagnostic")
    public void testProjectWithNonCompilerPluginConstraintPackage() {
        BCompileUtil.compileAndCacheBala("projects_for_constraint_field_tests/constraint_packages/constraint3");

        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_n");

        BuildProject buildProject = TestUtils.loadBuildProject(projectDirPath);
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();

        Path buildProjectPath = testBuildDirectory.resolve(
                "repo/bala/foo/constraint3/0.1.0/any/modules/constraint3/constraint3");
        Assert.assertEquals(diagnosticResult.errorCount(), 1, "Invalid compilation diagnostics count");
        Assert.assertEquals(diagnosticResult.errors().iterator().next().toString(),
                "ERROR [foo/constraint3/0.1.0::" + buildProjectPath
                        + ":(1:1,1:1)] Invalid constraint package: 'foo/constraint3:0.1.0'");
    }
}
