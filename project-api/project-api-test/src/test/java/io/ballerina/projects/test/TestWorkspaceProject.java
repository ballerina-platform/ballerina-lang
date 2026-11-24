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

import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectLoadResult;
import io.ballerina.projects.WorkspaceBallerinaToml;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.directory.WorkspaceProject;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.PackageLockingMode;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.internal.environment.DependencyFingerprints;
import io.ballerina.projects.internal.environment.FingerprintCompilationCache;
import io.ballerina.projects.internal.repositories.WorkspaceRepository;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class TestWorkspaceProject extends BaseTest {
    private static final Path RESOURCE_DIRECTORY = Path.of("src/test/resources/workspaces");
    private static final Path DISTRIBUTION_REPOSITORY = Path.of("build").resolve(ProjectConstants.DIST_CACHE_DIRECTORY)
            .toAbsolutePath();
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
        // All members of the copy share one environment: hello_app -> depA -> depB depend on one another, and
        // bye_app does not share any dependency with them in another version.
        WorkspaceProject duplicateWorkspace = (WorkspaceProject) duplicateWp;
        Environment helloAppEnvironment = memberEnvironment(duplicateWorkspace, "hello_app");
        Assert.assertSame(memberEnvironment(duplicateWorkspace, "depA"), helloAppEnvironment);
        Assert.assertSame(memberEnvironment(duplicateWorkspace, "depB"), helloAppEnvironment);
        Assert.assertSame(memberEnvironment(duplicateWorkspace, "bye_app"), helloAppEnvironment);
        Project duplicate = ((WorkspaceProject) duplicateWp).projects().stream().filter(prj ->
                prj.currentPackage().descriptor().name().toString().equals("hello_app")).findFirst().orElseThrow();
        Assert.assertFalse(duplicate.currentPackage().getCompilation().diagnosticResult().hasErrors());
    }

    @Test(description = "Independent packages that resolve their shared dependencies alike share an environment")
    public void testIndependentPackagesWithCompatibleDependenciesShareEnvironment() {
        Path projectPath = tempResourceDir.resolve("wp-multiple-roots");
        ProjectLoadResult projectLoadResult = TestUtils.loadWorkspaceProject(projectPath);
        Assert.assertTrue(projectLoadResult.diagnostics().errors().isEmpty());
        WorkspaceProject project = (WorkspaceProject) projectLoadResult.project();

        // hello_app -> depA -> depB form one group. bye_app depends on no other member, but resolves no shared
        // dependency to another version, so all packages share one environment.
        Environment helloAppEnvironment = memberEnvironment(project, "hello_app");
        Assert.assertSame(memberEnvironment(project, "depB"), helloAppEnvironment);
        Assert.assertSame(memberEnvironment(project, "bye_app"), helloAppEnvironment);

        // Placement after resolving all packages keeps them in one environment, and every package compiles.
        Assert.assertFalse(project.getResolution().diagnosticResult().hasErrors());
        project.clearCaches();
        Assert.assertSame(memberEnvironment(project, "bye_app"), memberEnvironment(project, "hello_app"));
        for (BuildProject member : project.projects()) {
            Assert.assertFalse(member.currentPackage().getCompilation().diagnosticResult().hasErrors(),
                    member.currentPackage().packageName().toString());
        }
    }

    @Test(description = "An edit that adds an import of another workspace package compiles with that package")
    public void testEditAddingCrossGroupImportMergesGroups() {
        Path projectPath = tempResourceDir.resolve("wp-multiple-roots");
        ProjectLoadResult projectLoadResult = TestUtils.loadWorkspaceProject(projectPath);
        Assert.assertTrue(projectLoadResult.diagnostics().errors().isEmpty());
        WorkspaceProject project = (WorkspaceProject) projectLoadResult.project();

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

    @Test(description = "A copy of a single workspace package is compiled with the packages of its environment")
    public void testDuplicateWorkspacePackageUsesItsGroup() {
        Path projectPath = tempResourceDir.resolve("wp-multiple-roots");
        ProjectLoadResult projectLoadResult = TestUtils.loadWorkspaceProject(projectPath);
        Assert.assertTrue(projectLoadResult.diagnostics().errors().isEmpty());
        WorkspaceProject project = (WorkspaceProject) projectLoadResult.project();

        Project byeAppCopy = member(project, "bye_app").duplicate();
        Assert.assertFalse(byeAppCopy.currentPackage().getCompilation().diagnosticResult().hasErrors());

        Project helloAppCopy = member(project, "hello_app").duplicate();
        WorkspaceRepository helloAppRepository = helloAppCopy.projectEnvironmentContext().environment()
                .getService(WorkspaceRepository.class);
        Assert.assertTrue(helloAppRepository.isPackageExists(PackageOrg.from("testorg"), PackageName.from("depB"),
                null), "A copy should resolve the packages of its environment");
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

    @Test(description = "A package that resolves a shared dependency to other versions than the other packages is "
            + "compiled in an environment of its own, when it is compiled with the project API alone")
    public void testPackageWithConflictingDependencyCompiledApart() throws IOException {
        // 'shared_lib' depends on 'versioned_lib'. 'older' and 'second' lock 'versioned_lib' 0.1.0, while 'third'
        // requires 0.1.1, so 'shared_lib' must be compiled against each version.
        Path fixture = tempResourceDir.resolve("wp-conflicting-dependency");
        publishConflictingDependencies(fixture.resolve("deps"));
        Path workspace = fixture.resolve("workspace");
        for (String name : List.of("older", "second")) {
            TestUtils.replaceDistributionVersionOfDependenciesToml(workspace.resolve(name),
                    RepoUtils.getBallerinaShortVersion());
        }
        WorkspaceProject project = (WorkspaceProject) ProjectLoader.load(workspace,
                BuildOptions.builder().setOffline(true).setLockingMode(PackageLockingMode.HARD).build()).project();
        Environment loadedEnvironment = memberEnvironment(project, "older");
        Assert.assertSame(memberEnvironment(project, "third"), loadedEnvironment,
                "Packages start in one environment, as their dependency versions are not known yet");

        // A copy of a package is compiled in the environment it was copied into, and moves no workspace package.
        Project thirdCopy = member(project, "third").duplicate();
        Assert.assertFalse(thirdCopy.currentPackage().getCompilation().diagnosticResult().hasErrors());
        Assert.assertSame(memberEnvironment(project, "third"), loadedEnvironment,
                "Compiling a copy should not move the package it was copied from");

        // A tool holds the packages it compiles, which may be replaced when they move.
        Map<String, Package> heldPackages = new HashMap<>();
        for (String name : List.of("older", "second", "third")) {
            Package heldPackage = member(project, name).currentPackage();
            heldPackages.put(name, heldPackage);
            PackageCompilation compilation = heldPackage.getCompilation();
            Assert.assertFalse(compilation.diagnosticResult().hasErrors(),
                    name + ": " + compilation.diagnosticResult().errors());
            JBallerinaBackend.from(compilation, JvmTarget.JAVA_25);
        }

        Assert.assertEquals(resolvedVersion(member(project, "older"), "versioned_lib"), "0.1.0");
        Assert.assertEquals(resolvedVersion(member(project, "third"), "versioned_lib"), "0.1.1");
        Environment thirdEnvironment = memberEnvironment(project, "third");
        Assert.assertSame(memberEnvironment(project, "second"), memberEnvironment(project, "older"),
                "Packages that resolve their shared dependencies alike should share an environment");
        Assert.assertNotSame(thirdEnvironment, memberEnvironment(project, "older"),
                "The package that resolves a shared dependency differently should be compiled apart");
        Assert.assertEquals(compiledVariants("shared_lib"),
                Stream.of("0.1.0", "0.1.1").map(TestWorkspaceProject::versionedLibFingerprint).sorted().toList(),
                "The shared dependency should be compiled once against each version");

        // A package held from before it moved compiles and resolves as the moved package, and compiling again
        // moves nothing.
        Package movedThird = member(project, "third").currentPackage();
        Package heldThird = heldPackages.get("third");
        Assert.assertNotSame(heldThird, movedThird, "The moved package should replace the held one");
        Assert.assertSame(heldThird.getCompilation(), movedThird.getCompilation());
        Assert.assertSame(heldThird.getResolution(), movedThird.getResolution());
        for (String name : List.of("older", "second", "third")) {
            Assert.assertFalse(heldPackages.get(name).getCompilation().diagnosticResult().hasErrors(), name);
        }
        Assert.assertSame(memberEnvironment(project, "third"), thirdEnvironment);
        Assert.assertSame(member(project, "older").currentPackage(), heldPackages.get("older"),
                "A package that does not move should not be replaced");
    }

    /**
     * Publishes 'versioned_lib' 0.1.0, 'shared_lib' compiled against it, and then 'versioned_lib' 0.1.1 to the test
     * distribution, and removes their compiled caches, which the test writes.
     */
    private static void publishConflictingDependencies(Path deps) throws IOException {
        for (String name : List.of("versioned_lib", "shared_lib")) {
            for (Path dir : List.of(DISTRIBUTION_REPOSITORY.resolve(ProjectConstants.BALA_DIR_NAME),
                    DISTRIBUTION_REPOSITORY.resolve(ProjectConstants.CACHES_DIR_NAME), homeDistributionCache())) {
                FileUtils.deleteDirectory(dir.resolve("testorg").resolve(name).toFile());
            }
        }
        for (String dependency : List.of("versioned_lib_0_1_0", "shared_lib", "versioned_lib_0_1_1")) {
            CompileResult result = BCompileUtil.compileAndCacheBala(deps.resolve(dependency), DISTRIBUTION_REPOSITORY,
                    BCompileUtil.getTestProjectEnvironmentBuilder());
            Assert.assertFalse(result.getDiagnosticResult().hasErrors(), dependency);
        }
        for (String name : List.of("versioned_lib", "shared_lib")) {
            FileUtils.deleteDirectory(DISTRIBUTION_REPOSITORY.resolve(ProjectConstants.CACHES_DIR_NAME)
                    .resolve("testorg").resolve(name).toFile());
        }
    }

    /**
     * Fingerprints of the compiled variants of the given package of the test distribution, sorted: the one the
     * distribution cache records, and the names of the variants in the user home.
     */
    private static List<String> compiledVariants(String name) throws IOException {
        List<String> fingerprints = new ArrayList<>();
        Path fingerprintFile = DISTRIBUTION_REPOSITORY.resolve(ProjectConstants.CACHES_DIR_NAME).resolve("testorg")
                .resolve(name).resolve("0.1.0").resolve(FingerprintCompilationCache.FINGERPRINT_FILE);
        if (Files.isRegularFile(fingerprintFile)) {
            fingerprints.add(Files.readString(fingerprintFile).trim());
        }
        Path homeVersionDir = homeDistributionCache().resolve("testorg").resolve(name).resolve("0.1.0");
        if (Files.isDirectory(homeVersionDir)) {
            try (Stream<Path> paths = Files.list(homeVersionDir)) {
                paths.map(path -> path.getFileName().toString()).filter(fileName -> fileName.matches("[0-9a-f]{16}"))
                        .forEach(fingerprints::add);
            }
        }
        return fingerprints.stream().sorted().toList();
    }

    /** Fingerprint of a package that depends only on the given version of 'versioned_lib'. */
    private static String versionedLibFingerprint(String version) {
        return DependencyFingerprints.of(List.of(PackageDescriptor.from(PackageOrg.from("testorg"),
                PackageName.from("versioned_lib"), PackageVersion.from(version))));
    }

    /** Cache of distribution packages compiled against other versions than the distribution, in the user home. */
    private static Path homeDistributionCache() {
        return RepoUtils.createAndGetHomeReposPath().resolve(ProjectConstants.REPOSITORIES_DIR).resolve("distribution")
                .resolve(ProjectConstants.CACHES_DIR_NAME + "-" + RepoUtils.getBallerinaShortVersion());
    }

    private static String resolvedVersion(BuildProject buildProject, String packageName) {
        return buildProject.currentPackage().getResolution().allDependencies().stream()
                .map(dependency -> dependency.packageInstance().descriptor())
                .filter(descriptor -> descriptor.name().toString().equals(packageName))
                .map(descriptor -> descriptor.version().toString())
                .findFirst().orElseThrow();
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
