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
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectLoadResult;
import io.ballerina.projects.WorkspaceBallerinaToml;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.directory.WorkspaceProject;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.internal.repositories.WorkspaceRepository;
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
            JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_25);
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
            JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_25);
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
            Assert.assertFalse(compilation.diagnosticResult().hasErrors());
            JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_25);
            Assert.assertFalse(jBallerinaBackend.diagnosticResult().hasErrors());
        }
    }

    @Test
    public void testWorkspaceWithHierarchicalNames() {
        Path projectPath = tempResourceDir.resolve("wp-multiple-roots-with-hierarchical-names");
        ProjectLoadResult projectLoadResult = TestUtils.loadWorkspaceProject(projectPath);
        Assert.assertTrue(projectLoadResult.diagnostics().errors().isEmpty());
        WorkspaceProject project = (WorkspaceProject) projectLoadResult.project();
        List<BuildProject> topologicallySortedList = project.getResolution().dependencyGraph()
                .toTopologicallySortedList();
        Package depAPackage;
        if (topologicallySortedList.get(0).currentPackage().descriptor().name().toString().equals("dep.b")) {
            Assert.assertEquals(
                    topologicallySortedList.get(0).currentPackage().descriptor().name().toString(), "dep.b");
            Assert.assertEquals(
                    topologicallySortedList.get(1).currentPackage().descriptor().name().toString(), "depA");
            depAPackage = topologicallySortedList.get(1).currentPackage();
            Assert.assertEquals(
                    topologicallySortedList.get(2).currentPackage().descriptor().name().toString(), "hello_app");
            Assert.assertEquals(
                    topologicallySortedList.get(3).currentPackage().descriptor().name().toString(), "bye_app");
        } else {
            Assert.assertEquals(
                    topologicallySortedList.get(0).currentPackage().descriptor().name().toString(), "bye_app");
            Assert.assertEquals(
                    topologicallySortedList.get(1).currentPackage().descriptor().name().toString(), "dep.b");
            Assert.assertEquals(
                    topologicallySortedList.get(2).currentPackage().descriptor().name().toString(), "depA");
            depAPackage = topologicallySortedList.get(2).currentPackage();
            Assert.assertEquals(
                    topologicallySortedList.get(3).currentPackage().descriptor().name().toString(), "hello_app");
        }

        // Verify that the dependencies of depA contains dep.b
        depAPackage.getResolution().dependencyGraph().toTopologicallySortedList()
                .stream().filter(packageDependency -> packageDependency.packageInstance().descriptor().toString()
                        .equals("testorg/dep.b:0.1.0")).findFirst()
                .orElseThrow(() -> new AssertionError("dep.b not found in the dependencies of depA"));

        Assert.assertEquals(topologicallySortedList.size(), 4);
        for (BuildProject buildProject : topologicallySortedList) {
            PackageCompilation compilation = buildProject.currentPackage().getCompilation();
            Assert.assertFalse(compilation.diagnosticResult().hasErrors());
            JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_25);
            Assert.assertFalse(jBallerinaBackend.diagnosticResult().hasErrors());
        }
    }

    @Test
    public void testWorkspaceWithDifferentBuildOptions() {
        Path projectPath = tempResourceDir.resolve("wp-different-build-options");
        ProjectLoadResult projectLoadResult = ProjectLoader.load(projectPath);
        Assert.assertTrue(projectLoadResult.diagnostics().errors().isEmpty());
        WorkspaceProject project = (WorkspaceProject) projectLoadResult.project();
        List<BuildProject> topologicallySortedList = project.getResolution(
                ResolutionOptions.builder().setOffline(true).build()) // set offline build to avoid network calls
                .dependencyGraph().toTopologicallySortedList();

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
            JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_25);
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

    @Test
    public void testWorkspaceWithDiagnostics() {
        Path projectPath = tempResourceDir.resolve("wp-multiple-roots");
        ProjectLoadResult projectLoadResult = TestUtils.loadWorkspaceProject(projectPath);
        Assert.assertTrue(projectLoadResult.diagnostics().errors().isEmpty());
        WorkspaceProject project = (WorkspaceProject) projectLoadResult.project();
        List<BuildProject> topologicallySortedList = project.getResolution().dependencyGraph()
                .toTopologicallySortedList();
        Assert.assertEquals(topologicallySortedList.size(), 4);
        for (BuildProject buildProject : topologicallySortedList) {
            PackageCompilation compilation = buildProject.currentPackage().getCompilation();
            JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_25);
            if (buildProject.currentPackage().descriptor().name().toString().equals("bye_app")) {
                Assert.assertEquals(compilation.diagnosticResult().warningCount(), 0);
                Assert.assertEquals(jBallerinaBackend.diagnosticResult().warningCount(), 0);
                continue;
            }

            String warning;
            if (buildProject.currentPackage().descriptor().name().toString().equals("depB")) {
                warning = "WARNING [depB.bal:(9:5,9:24)] unused variable 'foo'";
            } else {
                warning = "WARNING [testorg/depB/0.1.0::depB.bal:(9:5,9:24)] unused variable 'foo'";
            }
            Assert.assertEquals(compilation.diagnosticResult().warningCount(), 1);
            Assert.assertEquals(compilation.diagnosticResult().warnings().iterator().next().toString(), warning,
                    "warning not found in package: " +
                            buildProject.currentPackage().descriptor().name().toString());
            Assert.assertEquals(jBallerinaBackend.diagnosticResult().warningCount(), 1);
            Assert.assertEquals(jBallerinaBackend.diagnosticResult().warnings().iterator().next().toString(), warning,
                    "warning not found in package: " +
                            buildProject.currentPackage().descriptor().name().toString());
        }
    }

    @Test
    public void testSkipDepDiagnosticsInWorkspace() {
        Path projectPath = tempResourceDir.resolve("wp-multiple-roots");
        ProjectLoadResult projectLoadResult = TestUtils.loadWorkspaceProject(projectPath);
        Assert.assertTrue(projectLoadResult.diagnostics().errors().isEmpty());
        WorkspaceProject project = (WorkspaceProject) projectLoadResult.project();
        List<BuildProject> topologicallySortedList = project.getResolution().dependencyGraph()
                .toTopologicallySortedList();
        Assert.assertEquals(topologicallySortedList.size(), 4);
        for (BuildProject buildProject : topologicallySortedList) {
            PackageCompilation compilation = buildProject.currentPackage().getCompilation();
            JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_25);
            if (buildProject.currentPackage().descriptor().name().toString().equals("bye_app")) {
                Assert.assertEquals(compilation.diagnosticResult().warningCount(), 0);
                Assert.assertEquals(jBallerinaBackend.diagnosticResult().warningCount(), 0);
                continue;
            }

            if (buildProject.currentPackage().descriptor().name().toString().equals("depB")) {
                String warning = "WARNING [depB.bal:(9:5,9:24)] unused variable 'foo'";
                Assert.assertEquals(compilation.diagnosticResult().diagnostics(false, true).size(), 1);
                Assert.assertEquals(compilation.diagnosticResult().warnings().iterator().next().toString(), warning,
                        "warning not found in package: " +
                                buildProject.currentPackage().descriptor().name().toString());
                Assert.assertEquals(jBallerinaBackend.diagnosticResult().diagnostics(false, true).size(), 1);
                Assert.assertEquals(jBallerinaBackend.diagnosticResult().warnings().iterator().next().toString(),
                        warning, "warning not found in package: " +
                                buildProject.currentPackage().descriptor().name().toString());
            } else {
                // Since depA depends on depB, and we are skipping dependency diagnostics, there should be no warnings.
                Assert.assertEquals(compilation.diagnosticResult().diagnostics(false, true).size(), 0);
                Assert.assertEquals(jBallerinaBackend.diagnosticResult().diagnostics(false, true).size(), 0);
            }
        }
    }

    @Test
    public void testDuplicateBuildProjectInWorkspace() {
        Path projectPath = tempResourceDir.resolve("wp-multiple-roots");
        ProjectLoadResult projectLoadResult = TestUtils.loadWorkspaceProject(projectPath);
        Assert.assertTrue(projectLoadResult.diagnostics().errors().isEmpty());
        WorkspaceProject project = (WorkspaceProject) projectLoadResult.project();
        List<BuildProject> topologicallySortedList = project.getResolution().dependencyGraph()
                .toTopologicallySortedList();
        Assert.assertEquals(topologicallySortedList.size(), 4);

        Project buildProject = project.projects().stream().filter(prj ->
                prj.currentPackage().descriptor().name().toString().equals("hello_app")).findFirst().orElseThrow();
        Assert.assertFalse(buildProject.currentPackage().getCompilation().diagnosticResult()
                .hasErrors());
        Project duplicateWp = buildProject.workspaceProject().orElseThrow().duplicate();
        // Members of the copy that depend on one another share one environment (hello_app -> depA -> depB).
        // bye_app depends on no other member, so it has an environment of its own.
        WorkspaceProject duplicateWorkspace = (WorkspaceProject) duplicateWp;
        Environment helloAppEnvironment = memberEnvironment(duplicateWorkspace, "hello_app");
        Assert.assertSame(memberEnvironment(duplicateWorkspace, "depA"), helloAppEnvironment);
        Assert.assertSame(memberEnvironment(duplicateWorkspace, "depB"), helloAppEnvironment);
        Assert.assertNotSame(memberEnvironment(duplicateWorkspace, "bye_app"), helloAppEnvironment);
        Project duplicate = ((WorkspaceProject) duplicateWp).projects().stream().filter(prj ->
                prj.currentPackage().descriptor().name().toString().equals("hello_app")).findFirst().orElseThrow();
        Assert.assertFalse(duplicate.currentPackage().getCompilation().diagnosticResult().hasErrors());
    }

    @Test(description = "Packages that depend on one another share an environment, independent packages do not")
    public void testWorkspacePackageGroupsHaveSeparateEnvironments() {
        Path projectPath = tempResourceDir.resolve("wp-multiple-roots");
        ProjectLoadResult projectLoadResult = TestUtils.loadWorkspaceProject(projectPath);
        Assert.assertTrue(projectLoadResult.diagnostics().errors().isEmpty());
        WorkspaceProject project = (WorkspaceProject) projectLoadResult.project();

        // hello_app -> depA -> depB form one group. bye_app depends on no other member.
        Environment helloAppEnvironment = memberEnvironment(project, "hello_app");
        Assert.assertSame(memberEnvironment(project, "depA"), helloAppEnvironment);
        Assert.assertSame(memberEnvironment(project, "depB"), helloAppEnvironment);
        Environment byeAppEnvironment = memberEnvironment(project, "bye_app");
        Assert.assertNotSame(byeAppEnvironment, helloAppEnvironment);

        // Each environment only provides the packages of its group.
        WorkspaceRepository helloAppRepository = helloAppEnvironment.getService(WorkspaceRepository.class);
        Assert.assertTrue(helloAppRepository.isPackageExists(PackageOrg.from("testorg"), PackageName.from("depB"),
                null));
        Assert.assertFalse(helloAppRepository.isPackageExists(PackageOrg.from("testorg"),
                PackageName.from("bye_app"), null));

        // Grouping is kept when the caches are cleared, and every package still compiles.
        project.clearCaches();
        Assert.assertNotSame(memberEnvironment(project, "bye_app"), memberEnvironment(project, "hello_app"));
        for (BuildProject member : project.projects()) {
            Assert.assertFalse(member.currentPackage().getCompilation().diagnosticResult().hasErrors(),
                    member.currentPackage().packageName().toString());
        }
    }

    @Test(description = "An edit that adds an import of a package of another group merges the groups")
    public void testEditAddingCrossGroupImportMergesGroups() {
        Path projectPath = tempResourceDir.resolve("wp-multiple-roots");
        ProjectLoadResult projectLoadResult = TestUtils.loadWorkspaceProject(projectPath);
        Assert.assertTrue(projectLoadResult.diagnostics().errors().isEmpty());
        WorkspaceProject project = (WorkspaceProject) projectLoadResult.project();
        Assert.assertNotSame(memberEnvironment(project, "bye_app"), memberEnvironment(project, "depB"));

        // bye_app starts importing depB, as an edit in the language server would.
        Package byeApp = editDefaultModule(project, "bye_app", "import testorg/depB as _;\n\n");
        Assert.assertFalse(byeApp.getCompilation().diagnosticResult().hasErrors(),
                byeApp.getCompilation().diagnosticResult().errors().toString());
        Environment mergedEnvironment = memberEnvironment(project, "depB");
        Assert.assertSame(memberEnvironment(project, "bye_app"), mergedEnvironment);
        Assert.assertSame(memberEnvironment(project, "hello_app"), mergedEnvironment);

        // Removing the import does not split the group until the workspace is loaded again.
        editDefaultModule(project, "bye_app", "");
        Assert.assertSame(memberEnvironment(project, "bye_app"), memberEnvironment(project, "depB"));
        for (BuildProject member : project.projects()) {
            Assert.assertFalse(member.currentPackage().getCompilation().diagnosticResult().hasErrors(),
                    member.currentPackage().packageName().toString());
        }
    }

    @Test(description = "A copy of a single workspace package is compiled with the packages of its group")
    public void testDuplicateWorkspacePackageUsesItsGroup() {
        Path projectPath = tempResourceDir.resolve("wp-multiple-roots");
        ProjectLoadResult projectLoadResult = TestUtils.loadWorkspaceProject(projectPath);
        Assert.assertTrue(projectLoadResult.diagnostics().errors().isEmpty());
        WorkspaceProject project = (WorkspaceProject) projectLoadResult.project();

        Project byeAppCopy = member(project, "bye_app").duplicate();
        WorkspaceRepository byeAppRepository = byeAppCopy.projectEnvironmentContext().environment()
                .getService(WorkspaceRepository.class);
        Assert.assertFalse(byeAppRepository.isPackageExists(PackageOrg.from("testorg"), PackageName.from("depB"),
                null), "A copy must not resolve packages of another group");
        Assert.assertFalse(byeAppCopy.currentPackage().getCompilation().diagnosticResult().hasErrors());

        Project helloAppCopy = member(project, "hello_app").duplicate();
        WorkspaceRepository helloAppRepository = helloAppCopy.projectEnvironmentContext().environment()
                .getService(WorkspaceRepository.class);
        Assert.assertTrue(helloAppRepository.isPackageExists(PackageOrg.from("testorg"), PackageName.from("depB"),
                null), "A copy should resolve the packages of its group");
        Assert.assertFalse(helloAppCopy.currentPackage().getCompilation().diagnosticResult().hasErrors());
    }

    /** Replaces the content of the first document of a package's default module with the given imports. */
    private static Package editDefaultModule(WorkspaceProject workspaceProject, String packageName,
                                             String imports) {
        Package pkg = member(workspaceProject, packageName).currentPackage();
        Module defaultModule = pkg.getDefaultModule();
        DocumentId documentId = defaultModule.documentIds().iterator().next();
        String content = defaultModule.document(documentId).textDocument().toString()
                .replace("import testorg/depB as _;\n\n", "");
        return defaultModule.document(documentId).modify().withContent(imports + content).apply()
                .module().packageInstance();
    }

    private static BuildProject member(WorkspaceProject workspaceProject, String packageName) {
        return workspaceProject.projects().stream()
                .filter(member -> member.currentPackage().packageName().toString().equals(packageName))
                .findFirst().orElseThrow();
    }

    private static Environment memberEnvironment(WorkspaceProject workspaceProject, String packageName) {
        return workspaceProject.projects().stream()
                .filter(member -> member.currentPackage().packageName().toString().equals(packageName))
                .findFirst().orElseThrow()
                .projectEnvironmentContext().environment();
    }
}
