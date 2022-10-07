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
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.projects.test.TestUtils;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Test cases for IDL Client generation.
 *
 * @since 2201.3.0
 */
public class IDLClientGenPluginTests {

    private Path testResourceDir;

    @BeforeClass
    public void setup() throws IOException {

        Path testSourceDir = Paths.get(
                "src/test/resources/compiler_plugin_tests/idl_plugin_packages").toAbsolutePath();
        testResourceDir = Files.createTempDirectory("idl-plugin-tests" + System.currentTimeMillis());
        Files.walk(testSourceDir).forEach(source -> {
            try {
                Files.copy(source, testResourceDir.resolve(testSourceDir.relativize(source)), REPLACE_EXISTING);
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        });
    }

    @Test
    public void testIdlPluginBuildProject() {
        assertIdlPluginProject("package_test_idl_plugin_1", 2);
    }

    @Test
    public void testIdlPluginLocalPaths() throws IOException {
        String projectName = "package_test_idl_plugin_local_test";
        Path projectDir = testResourceDir.resolve(projectName);
        Path mainPath = projectDir.resolve("main.bal");
        String originalContent = writeBalFile(projectDir, mainPath);

        assertIdlPluginProject(projectName, 4);

        undoBalFile(mainPath, originalContent);
    }

    private String writeBalFile(Path projectDir, Path balPath) throws IOException {
        String mainContent = Files.readString(balPath);

        Path path = projectDir.resolve("projectapiclientplugin.json").toAbsolutePath().normalize();

        String absPath = Matcher.quoteReplacement(path.toString());
        absPath = absPath.replace("\\", "\\\\");

        String newMainContent = mainContent.replaceAll("<<IDL_ABSOLUTE_PATH>>", absPath);

        URL fileProtocol = path.toUri().toURL();
        newMainContent = newMainContent.replaceAll("<<IDL_FILE_PROTOCOL>>",
                Matcher.quoteReplacement(fileProtocol.toString()));

        Files.write(balPath, newMainContent.getBytes(StandardCharsets.UTF_8));
        return mainContent;
    }

    private void undoBalFile(Path balPath, String oldContent) throws IOException {
        Files.write(balPath, oldContent.getBytes(StandardCharsets.UTF_8));
    }

    private void assertIdlPluginProject(String projectName, int expectedModules) {
        Path projectDir = testResourceDir.resolve(projectName);
        Project project = TestUtils.loadBuildProject(projectDir);
        IDLClientGeneratorResult idlClientGeneratorResult = project.currentPackage().runIDLGeneratorPlugins();
        Assert.assertTrue(idlClientGeneratorResult.reportedDiagnostics().diagnostics().isEmpty(),
                TestUtils.getDiagnosticsAsString(idlClientGeneratorResult.reportedDiagnostics()));

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = project.currentPackage().getCompilation().diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 0,
                TestUtils.getDiagnosticsAsString(idlClientGeneratorResult.reportedDiagnostics()));

        Assert.assertEquals(project.currentPackage().moduleIds().size(), expectedModules);
    }

    @Test(dependsOnMethods = "testIdlPluginBuildProject")
    public void testIdlPluginBuildProjectLoadExistingRemote() {
        Project project = TestUtils.loadBuildProject(testResourceDir.resolve("package_test_idl_plugin_1"));

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = project.currentPackage().getCompilation().diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 0,
                TestUtils.getDiagnosticsAsString(diagnosticResult));
        Assert.assertEquals(project.currentPackage().moduleIds().size(), 2);
    }

    @Test(dependsOnMethods = "testIdlPluginLocalPaths")
    public void testIdlPluginBuildProjectLoadExistingLocal() throws IOException {
        String projectName = "package_test_idl_plugin_local_test";
        Path projectDir = testResourceDir.resolve(projectName);
        Path mainPath = projectDir.resolve("main.bal");
        String originalContent = writeBalFile(projectDir, mainPath);
        Project project = TestUtils.loadBuildProject(projectDir);
        undoBalFile(mainPath, originalContent);

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = project.currentPackage().getCompilation().diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 0,
                TestUtils.getDiagnosticsAsString(diagnosticResult));
        Assert.assertEquals(project.currentPackage().moduleIds().size(), 4);

    }

    @Test
    public void testIdlPluginBuildProjectRepeatCompilation() {
        BuildOptions buildOptions = BuildOptions.builder().targetDir(ProjectUtils.getTemporaryTargetPath()).build();
        Project project = TestUtils.loadBuildProject(testResourceDir.resolve("package_test_idl_plugin_3"),
                buildOptions);
        Assert.assertEquals(project.currentPackage().getCompilation().diagnosticResult().errors().size(), 7);
        Assert.assertEquals(project.currentPackage().moduleIds().size(), 1);
        IDLClientGeneratorResult idlClientGeneratorResult = project.currentPackage().runIDLGeneratorPlugins();

        Assert.assertTrue(idlClientGeneratorResult.reportedDiagnostics().diagnostics().isEmpty(),
                TestUtils.getDiagnosticsAsString(idlClientGeneratorResult.reportedDiagnostics()));
        Assert.assertEquals(project.currentPackage().getCompilation().diagnosticResult().errors().size(), 0,
                TestUtils.getDiagnosticsAsString(project.currentPackage().getCompilation().diagnosticResult()));
        Assert.assertEquals(project.currentPackage().moduleIds().size(), 2);
    }

    @Test
    public void testIdlPluginBuildProjectRepeatEditsWithCompilation() {
        Project project = TestUtils.loadBuildProject(testResourceDir.resolve("package_test_idl_plugin_for_edits"));
        Assert.assertEquals(project.currentPackage().getCompilation().diagnosticResult().errors().size(), 6,
                TestUtils.getDiagnosticsAsString(project.currentPackage().getCompilation().diagnosticResult()));

        DocumentId documentId = project.currentPackage().getDefaultModule().documentIds()
                .stream().findFirst().orElseThrow();
        String sourceCode = project.currentPackage().getDefaultModule()
                .document(documentId).syntaxTree().toSourceCode();

        // Edit, compile and check diagnostics
        for (int i = 1; i < 6; i++) {
            sourceCode += ("int a" + i + " = " + i + "\n");
            project.currentPackage().getDefaultModule().document(documentId).modify().withContent(sourceCode).apply();
            Assert.assertEquals(
                    project.currentPackage().getCompilation().diagnosticResult().diagnostics().size(), 6 + i,
                    TestUtils.getDiagnosticsAsString(project.currentPackage().getCompilation().diagnosticResult()));
        }
        Assert.assertEquals(project.currentPackage().moduleIds().size(), 1);

        // Run IDL client generator plugins
        IDLClientGeneratorResult idlClientGeneratorResult = project.currentPackage().runIDLGeneratorPlugins();
        Assert.assertTrue(idlClientGeneratorResult.reportedDiagnostics().diagnostics().isEmpty());
        Assert.assertEquals(project.currentPackage().getCompilation().diagnosticResult().errors().size(), 5,
                TestUtils.getDiagnosticsAsString(project.currentPackage().getCompilation().diagnosticResult()));
        Assert.assertEquals(project.currentPackage().moduleIds().size(), 2);

        // Repeat: Edit, compile and check diagnostics
        for (int i = 6; i < 11; i++) {
            sourceCode += ("int a" + i + " = " + i + "\n");
            project.currentPackage().getDefaultModule().document(documentId).modify().withContent(sourceCode).apply();
            Assert.assertEquals(project.currentPackage().getCompilation().diagnosticResult().diagnostics().size(), i,
                    TestUtils.getDiagnosticsAsString(project.currentPackage().getCompilation().diagnosticResult()));
        }

        Assert.assertEquals(project.currentPackage().moduleIds().size(), 2);
    }

    @Test
    public void testIdlDeclarationInBala() throws IOException {
        Path projectPath = testResourceDir.resolve("package_test_idl_plugin_2");
        Path customUserHome = Paths.get(System.getProperty("user.dir")).resolve("build/user-home");
        Path customHomeRepo = customUserHome.resolve("repositories/central.ballerina.io");
        Environment environment = EnvironmentBuilder.getBuilder().setUserHome(customUserHome).build();
        ProjectEnvironmentBuilder projectEnvironmentBuilder = ProjectEnvironmentBuilder.getBuilder(environment);

        Path balaPath = projectPath.resolve("idl-dependency-bala/user-package_test_idl_plugin-any-0.1.0.bala");
        BCompileUtil.copyBalaToExtractedDist(balaPath, "user", "package_test_idl_plugin", "0.1.0", customHomeRepo);

        BuildProject buildProject = TestUtils.loadBuildProject(projectEnvironmentBuilder, projectPath);
        Assert.assertFalse(buildProject.currentPackage().getCompilation().diagnosticResult().hasErrors(),
                TestUtils.getDiagnosticsAsString(buildProject.currentPackage().getCompilation().diagnosticResult()));
    }
}
