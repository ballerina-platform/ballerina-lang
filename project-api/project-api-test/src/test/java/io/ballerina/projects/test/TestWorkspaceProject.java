/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.projects.test;

import io.ballerina.projects.DocumentId;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectLoadResult;
import io.ballerina.projects.WorkspaceBallerinaToml;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.directory.WorkspaceProject;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TestWorkspaceProject extends BaseTest {
    private static final Path RESOURCE_DIRECTORY = Path.of("src/test/resources/workspaces");
    private static Path tempResourceDir;
    static final PrintStream OUT = System.out;

    @BeforeClass
    public void setup() throws IOException {
        tempResourceDir = Files.createTempDirectory("project-api-test");
        FileUtils.copyDirectory(RESOURCE_DIRECTORY.toFile(), tempResourceDir.toFile());
    }

    @Test
    public void testSimpleWorkspaceProject() {
        Path projectPath = tempResourceDir.resolve("wp-simple");
        ProjectLoadResult projectLoadResult = TestUtils.loadWorkspaceProject(projectPath);
        Assert.assertTrue(projectLoadResult.diagnostics().errors().isEmpty());
        WorkspaceProject project = (WorkspaceProject) projectLoadResult.project();
        List<BuildProject> topologicallySortedList = project.getResolution().dependencyGraph()
                .toTopologicallySortedList();
        Assert.assertEquals(topologicallySortedList.size(), 3);
        for (BuildProject buildProject : topologicallySortedList) {
            PackageCompilation compilation = buildProject.currentPackage().getCompilation();
            Assert.assertTrue(compilation.diagnosticResult().diagnostics().isEmpty());
            JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_21);
            Assert.assertTrue(jBallerinaBackend.diagnosticResult().diagnostics().isEmpty());
        }
    }

    @Test
    public void testWorkspaceWithOneRoot() {
        Path projectPath = tempResourceDir.resolve("wp-one-root");
        ProjectLoadResult projectLoadResult = TestUtils.loadWorkspaceProject(projectPath);
        Assert.assertTrue(projectLoadResult.diagnostics().errors().isEmpty());
        WorkspaceProject project = (WorkspaceProject) projectLoadResult.project();
        List<BuildProject> topologicallySortedList = project.getResolution().dependencyGraph()
                .toTopologicallySortedList();
        Assert.assertEquals(topologicallySortedList.size(), 3);
        Assert.assertEquals(
                topologicallySortedList.get(0).currentPackage().descriptor().name().toString(), "depB");
        Assert.assertEquals(
                topologicallySortedList.get(1).currentPackage().descriptor().name().toString(), "depA");
        Assert.assertEquals(
                topologicallySortedList.get(2).currentPackage().descriptor().name().toString(), "hello_app");
        for (BuildProject buildProject : topologicallySortedList) {
            PackageCompilation compilation = buildProject.currentPackage().getCompilation();
            Assert.assertTrue(compilation.diagnosticResult().diagnostics().isEmpty());
            JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_21);
            Assert.assertTrue(jBallerinaBackend.diagnosticResult().diagnostics().isEmpty());
        }
    }

    @Test
    public void testWorkspaceWithMultipleRoots() {
        Path projectPath = tempResourceDir.resolve("wp-multiple-roots");
        ProjectLoadResult projectLoadResult = TestUtils.loadWorkspaceProject(projectPath);
        Assert.assertTrue(projectLoadResult.diagnostics().errors().isEmpty());
        WorkspaceProject project = (WorkspaceProject) projectLoadResult.project();
        List<BuildProject> topologicallySortedList = project.getResolution().dependencyGraph()
                .toTopologicallySortedList();
        if (topologicallySortedList.get(0).currentPackage().descriptor().name().toString().equals("depB")) {
            Assert.assertEquals(
                    topologicallySortedList.get(0).currentPackage().descriptor().name().toString(), "depB");
            Assert.assertEquals(
                    topologicallySortedList.get(1).currentPackage().descriptor().name().toString(), "depA");
            Assert.assertEquals(
                    topologicallySortedList.get(2).currentPackage().descriptor().name().toString(), "hello_app");
            Assert.assertEquals(
                    topologicallySortedList.get(3).currentPackage().descriptor().name().toString(), "bye_app");
        } else {
            Assert.assertEquals(
                    topologicallySortedList.get(0).currentPackage().descriptor().name().toString(), "bye_app");
            Assert.assertEquals(
                    topologicallySortedList.get(1).currentPackage().descriptor().name().toString(), "depB");
            Assert.assertEquals(
                    topologicallySortedList.get(2).currentPackage().descriptor().name().toString(), "depA");
            Assert.assertEquals(
                    topologicallySortedList.get(3).currentPackage().descriptor().name().toString(), "hello_app");
        }

        Assert.assertEquals(topologicallySortedList.size(), 4);
        for (BuildProject buildProject : topologicallySortedList) {
            PackageCompilation compilation = buildProject.currentPackage().getCompilation();
            Assert.assertTrue(compilation.diagnosticResult().diagnostics().isEmpty());
            JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_21);
            Assert.assertTrue(jBallerinaBackend.diagnosticResult().diagnostics().isEmpty());
        }
    }

    @Test
    public void testWorkspaceWithDifferentBuildOptions() {
        Path projectPath = tempResourceDir.resolve("wp-different-build-options");
        ProjectLoadResult projectLoadResult = ProjectLoader.load(projectPath);
        Assert.assertTrue(projectLoadResult.diagnostics().errors().isEmpty());
        WorkspaceProject project = (WorkspaceProject) projectLoadResult.project();
        List<BuildProject> topologicallySortedList = project.getResolution().dependencyGraph()
                .toTopologicallySortedList();

        Assert.assertTrue(topologicallySortedList.get(0).buildOptions().offlineBuild());
        Assert.assertFalse(topologicallySortedList.get(0).buildOptions().observabilityIncluded());
        Assert.assertFalse(topologicallySortedList.get(1).buildOptions().offlineBuild());
        Assert.assertTrue(topologicallySortedList.get(1).buildOptions().observabilityIncluded());
        Assert.assertFalse(topologicallySortedList.get(2).buildOptions().offlineBuild());
        Assert.assertFalse(topologicallySortedList.get(2).buildOptions().observabilityIncluded());
    }

    @Test
    public void testWorkspaceWithNestedPaths() {
        Path projectPath = tempResourceDir.resolve("wp-nested-path");
        ProjectLoadResult projectLoadResult = TestUtils.loadWorkspaceProject(projectPath);
        Assert.assertTrue(projectLoadResult.diagnostics().errors().isEmpty());
        WorkspaceProject project = (WorkspaceProject) projectLoadResult.project();
        List<BuildProject> topologicallySortedList = project.getResolution().dependencyGraph()
                .toTopologicallySortedList();
        Assert.assertEquals(topologicallySortedList.size(), 3);
        for (BuildProject buildProject : topologicallySortedList) {
            PackageCompilation compilation = buildProject.currentPackage().getCompilation();
            Assert.assertTrue(compilation.diagnosticResult().diagnostics().isEmpty());
            JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_21);
            Assert.assertTrue(jBallerinaBackend.diagnosticResult().diagnostics().isEmpty());
        }
    }

    @Test
    public void testWorkspaceWithWrongPackagePath() {
        Path projectPath = tempResourceDir.resolve("wp-wrong-path");
        ProjectLoadResult projectLoadResult = TestUtils.loadWorkspaceProject(projectPath);
        Assert.assertEquals(projectLoadResult.diagnostics().errorCount(), 1);
        Assert.assertEquals(projectLoadResult.diagnostics().errors().iterator().next().toString(),
                "ERROR [Ballerina.toml:(1:1,2:33)] could not locate the package path 'pkgB'");
    }

    @Test
    public void testWorkspaceWithMultipleOrgs() {
        Path projectPath = tempResourceDir.resolve("wp-multiple-orgs");
        ProjectLoadResult projectLoadResult = TestUtils.loadWorkspaceProject(projectPath);
        Assert.assertEquals(projectLoadResult.diagnostics().errorCount(), 0);
        WorkspaceProject project = (WorkspaceProject) projectLoadResult.project();
        for (BuildProject buildProject : project.getResolution().dependencyGraph().toTopologicallySortedList()) {
            if (buildProject.currentPackage().descriptor().name().toString().equals("bye_app")) {
                Assert.assertEquals(buildProject.currentPackage().packageOrg().toString(), "asmaj");
                Assert.assertEquals(buildProject.currentPackage().manifest().diagnostics().warningCount(), 1);
                Assert.assertEquals(buildProject.currentPackage().manifest().diagnostics().warnings().iterator().next()
                        .toString(), "WARNING [Ballerina.toml:(2:1,2:12)] multiple orgs are not allowed in a " +
                        "workspace. Found 'foo', defaulting to 'asmaj'");
            } else {
                Assert.assertEquals(buildProject.currentPackage().packageOrg().toString(), "asmaj");
                Assert.assertEquals(buildProject.currentPackage().manifest().diagnostics().warningCount(), 0);
            }
        }
    }

    @Test
    public void testWorkspacePackageEdit() {
        Path projectPath = tempResourceDir.resolve("wp-edit");
        ProjectLoadResult projectLoadResult = TestUtils.loadWorkspaceProject(projectPath);
        Assert.assertEquals(projectLoadResult.diagnostics().errorCount(), 0);
        WorkspaceProject project = (WorkspaceProject) projectLoadResult.project();
        List<BuildProject> topologicallySortedList = project.getResolution().dependencyGraph()
                .toTopologicallySortedList();
        Assert.assertEquals(topologicallySortedList.size(), 4);
        Project depAProject = null;
        Project depBProject = null;
        Project helloProject = null;
        Project byeProject = null;
        Package depAPackage = null;
        Package depBPackage = null;
        Package helloPackage = null;
        Package byePackage = null;
        for (BuildProject buildProject : topologicallySortedList) {
            PackageCompilation compilation = buildProject.currentPackage().getCompilation();
            Assert.assertTrue(compilation.diagnosticResult().diagnostics().isEmpty());
            if (buildProject.currentPackage().descriptor().name().toString().equals("depA")) {
                depAProject = buildProject;
                depAPackage = buildProject.currentPackage();
            } else if (buildProject.currentPackage().descriptor().name().toString().equals("depB")) {
                depBProject = buildProject;
                depBPackage = buildProject.currentPackage();
            } else if (buildProject.currentPackage().descriptor().name().toString().equals("hello_app")) {
                helloProject = buildProject;
                helloPackage = buildProject.currentPackage();
            } else {
                byeProject = buildProject;
                byePackage = buildProject.currentPackage();
            }
        }

        // edit the depB package
        Assert.assertNotNull(depBProject);
        Assert.assertNotNull(depAProject);
        Assert.assertNotNull(helloProject);
        Assert.assertNotNull(byeProject);
        ModuleId moduleId = depBProject.currentPackage().moduleIds().stream().findFirst().orElseThrow();
        DocumentId documentId = depBProject.currentPackage().module(moduleId).documentIds().stream().findFirst()
                .orElseThrow();
        depBProject.currentPackage().module(moduleId).document(documentId).modify().withContent(
                """
                public function bye(string name) returns string {
                     return "Bye, World!";
                }
                """).apply();

        Assert.assertNotEquals(depBPackage, depBProject.currentPackage());
        Assert.assertNotEquals(depAPackage, depAProject.currentPackage());
        PackageCompilation compilation = depAProject.currentPackage().getCompilation();
        Assert.assertTrue(compilation.diagnosticResult().hasErrors());
        Assert.assertEquals(compilation.diagnosticResult().errors().iterator().next().toString(),
                "ERROR [depA.bal:(4:12,4:28)] undefined function 'hello'");

        // hello_app and bye_app should not have changed
        Assert.assertEquals(helloProject.currentPackage(), helloPackage);
        compilation = helloProject.currentPackage().getCompilation();
        Assert.assertFalse(compilation.diagnosticResult().hasErrors());

        Assert.assertEquals(byeProject.currentPackage(), byePackage);
        compilation = byeProject.currentPackage().getCompilation();
        Assert.assertFalse(compilation.diagnosticResult().hasErrors());
    }

    @Test
    public void testWorkspaceBalTomlEdit() {
        Path projectPath = tempResourceDir.resolve("wp-edit");
        ProjectLoadResult projectLoadResult = TestUtils.loadWorkspaceProject(projectPath);
        Assert.assertEquals(projectLoadResult.diagnostics().errorCount(), 0);
        WorkspaceProject project = (WorkspaceProject) projectLoadResult.project();
        List<BuildProject> topologicallySortedList = project.getResolution().dependencyGraph()
                .toTopologicallySortedList();
        Assert.assertEquals(topologicallySortedList.size(), 4);

        // Edit the workspace Ballerina.toml file and remove bye_app
        WorkspaceBallerinaToml workspaceBallerinaToml = project.ballerinaToml().modify().withContent(
                """
                [workspace]
                packages = ["hello-app", "depA", "depB"]
                """).apply();
        project = workspaceBallerinaToml.project();
        topologicallySortedList = project.getResolution().dependencyGraph().toTopologicallySortedList();
        Assert.assertEquals(topologicallySortedList.size(), 3);

        // Edit again to remove depB
        workspaceBallerinaToml = project.ballerinaToml().modify().withContent(
                """
                [workspace]
                packages = ["hello-app", "depA"]
                """).apply();
        project = workspaceBallerinaToml.project();
        topologicallySortedList = project.getResolution().dependencyGraph()
                .toTopologicallySortedList();
        Assert.assertEquals(topologicallySortedList.size(), 2);
        PackageCompilation compilation = topologicallySortedList.get(0).currentPackage().getCompilation();
        Assert.assertEquals(compilation.diagnosticResult().errorCount(), 3);
    }

    @Test
    public void testWorkspaceWithMinVersion() {
        Path projectPath = tempResourceDir.resolve("wp-with-minversion");
        ProjectLoadResult projectLoadResult = TestUtils.loadWorkspaceProject(projectPath);
        Assert.assertEquals(projectLoadResult.diagnostics().errorCount(), 0);
        WorkspaceProject project = (WorkspaceProject) projectLoadResult.project();
        List<BuildProject> topologicallySortedList = project.getResolution().dependencyGraph()
                .toTopologicallySortedList();
        Assert.assertEquals(topologicallySortedList.size(), 3);
        Assert.assertEquals(project.getResolution().diagnosticResult().diagnosticCount(), 0);
    }

    @Test (enabled = false, description = "The test is disabled due to the blocker #44240")
    public void testWorkspaceWithNonExistentMinVersion() {
        Path projectPath = tempResourceDir.resolve("wp-with-non-existent-minversion");
        ProjectLoadResult projectLoadResult = TestUtils.loadWorkspaceProject(projectPath);
        WorkspaceProject project = (WorkspaceProject) projectLoadResult.project();
        List<BuildProject> topologicallySortedList = project.getResolution().dependencyGraph()
                .toTopologicallySortedList();
        List<Diagnostic> diagnostics = new ArrayList<>();
        for (BuildProject buildProject : topologicallySortedList) {
            PackageCompilation compilation = buildProject.currentPackage().getCompilation();
            diagnostics.addAll(compilation.diagnosticResult().diagnostics());
        }
        Assert.assertEquals(diagnostics.size(), 1);
        Assert.assertEquals(diagnostics.get(0).toString(),
                "ERROR [depA.bal:(1:1,1:24)] cannot resolve module 'asmaj/depA as _'");
    }
}
