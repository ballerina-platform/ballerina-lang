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

import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.ProjectLoadResult;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.directory.WorkspaceProject;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
}
