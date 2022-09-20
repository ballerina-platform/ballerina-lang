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

package io.ballerina.projects.test.plugins;

import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.IDLClientGeneratorResult;
import io.ballerina.projects.Project;
import io.ballerina.projects.test.TestUtils;
import io.ballerina.projects.util.ProjectUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test cases for IDL Client generation.
 *
 * @since 2201.3.0
 */
public class IDLClientGenPluginTests {

    private static final Path RESOURCE_DIRECTORY = Paths.get(
            "src/test/resources/compiler_plugin_tests").toAbsolutePath();
    private String targetPath = ProjectUtils.getTemporaryTargetPath();

    @Test
    public void testIdlPluginBuildProject() {
        BuildOptions buildOptions = BuildOptions.builder().targetDir(targetPath).build();
        Project project = TestUtils.loadBuildProject(
                RESOURCE_DIRECTORY.resolve("package_test_idl_plugin"), buildOptions);
        IDLClientGeneratorResult idlClientGeneratorResult = project.currentPackage().runIDLGeneratorPlugins();
        Assert.assertTrue(idlClientGeneratorResult.reportedDiagnostics().diagnostics().isEmpty());

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = project.currentPackage().getCompilation().diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 0,
                "Unexpected number of compilation diagnostics");

        Assert.assertEquals(project.currentPackage().moduleIds().size(), 2);
    }

    @Test (dependsOnMethods = "testIdlPluginBuildProject")
    public void testIdlPluginBuildProjectLoadExisting() {
        BuildOptions buildOptions = BuildOptions.builder().targetDir(targetPath).build();
        Project project = TestUtils.loadBuildProject(
                RESOURCE_DIRECTORY.resolve("package_test_idl_plugin"), buildOptions);

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = project.currentPackage().getCompilation().diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 0,
                "Unexpected number of compilation diagnostics");

        Assert.assertEquals(project.currentPackage().moduleIds().size(), 2);
    }

    @Test
    public void testIdlPluginBuildProjectRepeatCompilation() {
        BuildOptions buildOptions = BuildOptions.builder().targetDir(ProjectUtils.getTemporaryTargetPath()).build();
        Project project = TestUtils.loadBuildProject(RESOURCE_DIRECTORY.resolve("package_test_idl_plugin"),
                buildOptions);
        Assert.assertEquals(project.currentPackage().getCompilation().diagnosticResult().errors().size(), 6);
        Assert.assertEquals(project.currentPackage().moduleIds().size(), 1);
        IDLClientGeneratorResult idlClientGeneratorResult = project.currentPackage().runIDLGeneratorPlugins();

        Assert.assertTrue(idlClientGeneratorResult.reportedDiagnostics().diagnostics().isEmpty());
        Assert.assertEquals(project.currentPackage().getCompilation().diagnosticResult().errors().size(), 0);
        Assert.assertEquals(project.currentPackage().moduleIds().size(), 2);
    }

    @Test
    public void testIdlPluginBuildProjectRepeatEditsWithCompilation() {
        BuildOptions buildOptions = BuildOptions.builder().targetDir(ProjectUtils.getTemporaryTargetPath()).build();
        Project project = TestUtils.loadBuildProject(RESOURCE_DIRECTORY.resolve("package_test_idl_plugin"),
                buildOptions);
        Assert.assertEquals(project.currentPackage().getCompilation().diagnosticResult().errors().size(), 6);

        DocumentId documentId = project.currentPackage().getDefaultModule().documentIds()
                .stream().findFirst().orElseThrow();
        String sourceCode = project.currentPackage().getDefaultModule()
                .document(documentId).syntaxTree().toSourceCode();

        // Edit, compile and check diagnostics
        for (int i = 1; i < 6; i++) {
            sourceCode += ("int a" + i + " = " + i);
            project.currentPackage().getDefaultModule().document(documentId).modify().withContent(sourceCode).apply();
            Assert.assertEquals(
                    project.currentPackage().getCompilation().diagnosticResult().diagnostics().size(), 6 + i);
        }
        Assert.assertEquals(project.currentPackage().moduleIds().size(), 1);

        // Run IDL client generator plugins
        IDLClientGeneratorResult idlClientGeneratorResult = project.currentPackage().runIDLGeneratorPlugins();
        Assert.assertTrue(idlClientGeneratorResult.reportedDiagnostics().diagnostics().isEmpty());
        Assert.assertEquals(project.currentPackage().getCompilation().diagnosticResult().errors().size(), 5);
        Assert.assertEquals(project.currentPackage().moduleIds().size(), 2);

        // Repeat: Edit, compile and check diagnostics
        for (int i = 6; i < 11; i++) {
            sourceCode += ("int a" + i + " = " + i);
            project.currentPackage().getDefaultModule().document(documentId).modify().withContent(sourceCode).apply();
            Assert.assertEquals(
                    project.currentPackage().getCompilation().diagnosticResult().diagnostics().size(), i);
        }

        Assert.assertEquals(project.currentPackage().moduleIds().size(), 2);
    }
}
