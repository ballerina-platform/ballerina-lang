package io.ballerina.cli.cmd;

import io.ballerina.cli.task.RunBuildToolsTask;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.util.ToolUtils;
import org.ballerinalang.test.BCompileUtil;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.ballerina.cli.cmd.CommandOutputUtils.getOutput;
import static io.ballerina.projects.util.ProjectConstants.DIST_CACHE_DIRECTORY;

public class RunBuildToolsTaskTest extends BaseCommandTest {
    private Path buildToolResources;
    private static final Path testBuildDirectory = Paths.get("build").toAbsolutePath();
    private static final Path testDistCacheDirectory = testBuildDirectory.resolve(DIST_CACHE_DIRECTORY);
    Path mockCentralBalaDirPath = testDistCacheDirectory.resolve("bala");

    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        try {
            Path testResources = super.tmpDir.resolve("build-tool-test-resources");
            this.buildToolResources = testResources.resolve("buildToolResources");
            Path testResourcesPath = Paths.get(
                    Objects.requireNonNull(getClass().getClassLoader().getResource("test-resources")).toURI());
            Files.walkFileTree(testResourcesPath, new BuildCommandTest.Copy(testResourcesPath, testResources));
        } catch (Exception e) {
            Assert.fail("error loading resources");
        }
        BCompileUtil.compileAndCacheBala(buildToolResources.resolve("tools")
                .resolve("dummy-tool-pkg").toString(), testDistCacheDirectory);
        BCompileUtil.compileAndCacheBala(buildToolResources.resolve("tools")
                        .resolve("invalid-name-tool-pkg").toString(), testDistCacheDirectory);
        BCompileUtil.compileAndCacheBala(buildToolResources.resolve("tools")
                        .resolve("ballerina-generate-file").toString(), testDistCacheDirectory);
    }

    @Test(description = "Resolve a tool offline", dataProvider = "buildToolProvider")
    public void testOfflineToolResolution(String projectName, String outputFileName) throws IOException {
        Path projectPath = buildToolResources.resolve(projectName);
        Project project = BuildProject.load(projectPath, BuildOptions.builder().setOffline(true).build());
        RunBuildToolsTask runBuildToolsTask = new RunBuildToolsTask(printStream);
        try (MockedStatic<ToolUtils> repoUtils = Mockito.mockStatic(ToolUtils.class, Mockito.CALLS_REAL_METHODS)) {
            repoUtils.when(ToolUtils::getCentralBalaDirPath).thenReturn(mockCentralBalaDirPath);
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
        try (MockedStatic<ToolUtils> repoUtils = Mockito.mockStatic(ToolUtils.class, Mockito.CALLS_REAL_METHODS)) {
            repoUtils.when(ToolUtils::getCentralBalaDirPath).thenReturn(mockCentralBalaDirPath);
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

    @DataProvider(name = "buildToolProvider")
    public Object[][] buildToolProvider() {
        return new Object[][]{
                {
                    "project-with-central-build-tool",
                    "build-tool-offline.txt"
                },
                {
                    "project-with-non-existent-build-tool",
                    "build-tool-offline-resolve-failed.txt"
                },
                {
                    "fresh-project-with-central-build-tool",
                    "build-tool-offline-resolve-failed-wo-version.txt"
                },
                {
                    "project-with-2.x-central-build-tool",
                    "build-tool-offline-with-new-major-version-locked.txt"
                },
                {
                    "project-with-non-existent-subcommand",
                    "build-tool-non-existent-subcommand.txt"
                },
                {
                    "project-with-invalid-name-build-tool",
                    "build-tool-invalid-name.txt"
                },
                {
                    "project-with-multilevel-subcommands",
                    "build-tool-multilevel-subcommands.txt"
                },
                {
                    "project-with-only-subcommands",
                    "build-tool-only-subcommands.txt"
                },
        };
    }
}
