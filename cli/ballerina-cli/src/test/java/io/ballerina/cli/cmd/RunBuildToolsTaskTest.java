/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package io.ballerina.cli.cmd;

import io.ballerina.cli.task.RunBuildToolsTask;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.util.BuildToolUtils;
import org.ballerinalang.test.BCompileUtil;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.ballerina.cli.cmd.CommandOutputUtils.getOutput;
import static io.ballerina.projects.util.ProjectConstants.DIST_CACHE_DIRECTORY;

/**
 * Test cases for the RunBuildToolsTask.
 *
 * @since 2201.9.0
 */
public class RunBuildToolsTaskTest extends BaseCommandTest {
    private Path buildToolResources;
    private static final Path testBuildDirectory = Paths.get("build").toAbsolutePath();
    private static final Path testDistCacheDirectory = testBuildDirectory.resolve(DIST_CACHE_DIRECTORY);
    Path mockCentralBalaDirPath = testDistCacheDirectory.resolve("bala");

    private static final long TWO_DAYS = 2 * 24 * 60 * 60 * 1000;
    private static final long HALF_DAY = 12 * 60 * 60 * 1000;

    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        // copy all test resources
        try {
            Path testResources = super.tmpDir.resolve("build-tool-test-resources");
            this.buildToolResources = testResources.resolve("buildToolResources");
            Path testResourcesPath = Paths.get(
                    Objects.requireNonNull(getClass().getClassLoader().getResource("test-resources")).toURI());
            Files.walkFileTree(testResourcesPath, new BuildCommandTest.Copy(testResourcesPath, testResources));
        } catch (Exception e) {
            Assert.fail("error loading resources");
        }
        // compile and cache tools
        BCompileUtil.compileAndCacheBala(buildToolResources.resolve("tools")
                .resolve("dummy-tool-pkg").toString(), testDistCacheDirectory);
        BCompileUtil.compileAndCacheBala(buildToolResources.resolve("tools")
                        .resolve("invalid-name-tool-pkg").toString(), testDistCacheDirectory);
        BCompileUtil.compileAndCacheBala(buildToolResources.resolve("tools")
                        .resolve("ballerina-generate-file").toString(), testDistCacheDirectory);
        BCompileUtil.compileAndCacheBala(buildToolResources.resolve("tools")
                .resolve("hidden-cmd-tool-pkg").toString(), testDistCacheDirectory);
        BCompileUtil.compileAndCacheBala(buildToolResources.resolve("tools")
                .resolve("missing-interface-tool-pkg").toString(), testDistCacheDirectory);
        BCompileUtil.compileAndCacheBala(buildToolResources.resolve("tools")
                .resolve("no-options-tool-pkg").toString(), testDistCacheDirectory);
        // add build.json files
        addBuildJsonToProjects("project-lt-24h-with-build-tool", System.currentTimeMillis() - HALF_DAY);
        addBuildJsonToProjects("project-gt-24h-with-build-tool", System.currentTimeMillis() - TWO_DAYS);
    }

    @Test(description = "Resolve a tool offline", dataProvider = "buildToolOfflineProvider")
    public void testOfflineToolResolution(String projectName, String outputFileName, boolean sticky)
            throws IOException {
        Path projectPath = buildToolResources.resolve(projectName);
        Project project = BuildProject.load(projectPath,
                BuildOptions.builder().setOffline(true).setSticky(sticky).build());
        RunBuildToolsTask runBuildToolsTask = new RunBuildToolsTask(printStream);
        try (MockedStatic<BuildToolUtils> repoUtils = Mockito.mockStatic(
                BuildToolUtils.class, Mockito.CALLS_REAL_METHODS)) {
            repoUtils.when(BuildToolUtils::getCentralBalaDirPath).thenReturn(mockCentralBalaDirPath);
            runBuildToolsTask.execute(project);
        }
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput(outputFileName));
    }

    @Test(description = "Generate files using a project and find the generated file in project instance")
    public void testProjectForAddedGeneratedCode() throws IOException {
        Path projectPath = buildToolResources.resolve("project-with-generate-file-tool");
        Project project = BuildProject.load(projectPath, BuildOptions.builder().setOffline(true).build());
        RunBuildToolsTask runBuildToolsTask = new RunBuildToolsTask(printStream);
        try (MockedStatic<BuildToolUtils> repoUtils = Mockito.mockStatic(
                BuildToolUtils.class, Mockito.CALLS_REAL_METHODS)) {
            repoUtils.when(BuildToolUtils::getCentralBalaDirPath).thenReturn(mockCentralBalaDirPath);
            runBuildToolsTask.execute(project);
        }
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("build-tool-generate-file.txt"));
        AtomicBoolean fileFound = new AtomicBoolean(false);
        project.currentPackage().modules().forEach(module -> {
            if (module.moduleName().toString().equals("winery.mod_generate")) {
                module.documentIds().forEach(documentId -> {
                    if (module.document(documentId).name().equals("client.bal")) {
                        fileFound.set(true);
                    }
                });
            }
        });
        Assert.assertTrue(fileFound.get(),
                "Generated file not found. Project instance hasn't been updated after build tools task");
    }

    @DataProvider(name = "buildToolOfflineProvider")
    public Object[][] buildToolOfflineProvider() {
        return new Object[][]{
            {
                "project-with-central-build-tool",
                "build-tool-offline.txt",
                false
            },
            {
                "project-with-non-existent-build-tool",
                "build-tool-offline-resolve-failed.txt",
                false
            },
            {
                "fresh-project-with-central-build-tool",
                "build-tool-offline-resolve-failed-wo-version.txt",
                false
            },
            {
                "project-with-2.x-central-build-tool",
                "build-tool-offline-with-new-major-version-locked.txt",
                false
            },
            {
                "project-with-non-existent-subcommand",
                "build-tool-non-existent-subcommand.txt",
                false
            },
            {
                "project-with-invalid-name-build-tool",
                "build-tool-invalid-name.txt",
                false
            },
            {
                "project-with-multilevel-subcommands",
                "build-tool-multilevel-subcommands.txt",
                false
            },
            {
                "project-with-only-subcommands",
                "build-tool-only-subcommands.txt",
                false
            },
            {
                "project-with-hidden-commands",
                "build-tool-hidden-commands.txt",
                false
            },
            {
                "project-with-missing-interface-build-tool",
                "build-tool-missing-interface.txt",
                false
            },
            {
                "project-with-no-options-build-tool",
                "build-tool-no-options.txt",
                false
            },
            {
                "project-with-old-build-tool",
                "build-tool-without-sticky.txt",
                false
            },
            {
                "project-with-old-build-tool",
                "build-tool-with-sticky.txt",
                true
            },
            {
                "project-lt-24h-with-build-tool",
                "build-tool-lt-24-build-file.txt",
                false
            },
            {
                "project-gt-24h-with-build-tool",
                "build-tool-gt-24-build-file.txt",
                false
            },
        };
    }

    private void addBuildJsonToProjects(String projectName, long time) {
        Path buildJsonPath = buildToolResources.resolve(projectName).resolve("target").resolve("build");
        String buildJsonContent = "{\n" +
                "  \"last_build_time\": 1710907945705,\n" +
                "  \"last_update_time\": " + time + ",\n" +
                "  \"distribution_version\": \"" + RepoUtils.getBallerinaShortVersion() + "\",\n" +
                "  \"last_modified_time\": {\n" +
                "    \"sample_build_tool_ballerina\": 1710907943604\n" +
                "  }\n" +
                "}";
        try {
            Files.createDirectories(buildJsonPath.getParent());
            Files.write(buildJsonPath, buildJsonContent.getBytes());
        } catch (IOException e) {
            Assert.fail("Error writing build.json file");
        }
    }
}
