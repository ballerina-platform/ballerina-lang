package io.ballerina.cli.cmd;

import io.ballerina.cli.launcher.BLauncherException;
import io.ballerina.cli.launcher.RuntimePanicException;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.projects.internal.model.BuildJson;
import io.ballerina.projects.util.ProjectUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static io.ballerina.cli.cmd.CommandOutputUtils.getOutput;
import static io.ballerina.cli.cmd.CommandOutputUtils.replaceDependenciesTomlContent;
import static io.ballerina.projects.util.ProjectConstants.DIST_CACHE_DIRECTORY;
import static io.ballerina.projects.util.ProjectConstants.USER_DIR_PROPERTY;
import static io.ballerina.projects.util.ProjectUtils.deleteDirectory;
import static io.ballerina.projects.util.ProjectUtils.readBuildJson;

/**
 * Run command tests.
 *
 * @since 2.0.0
 */
public class RunCommandTest extends BaseCommandTest {
    private Path testResources;
    private Path testDistCacheDirectory;
    private ProjectEnvironmentBuilder projectEnvironmentBuilder;
    static Path logFile = Path.of("build/logs/log_creator_combined_plugin/compiler-plugin.txt").toAbsolutePath();

    @BeforeSuite
    public void setupSuite() throws IOException {
        Files.createDirectories(logFile.getParent());
        Files.writeString(logFile, "");
    }

    @Override
    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        try {
            this.testResources = super.tmpDir.resolve("build-test-resources");
            Path testBuildDirectory = Path.of("build").toAbsolutePath();
            this.testDistCacheDirectory = testBuildDirectory.resolve(DIST_CACHE_DIRECTORY);
            Path customUserHome = Path.of("build", "user-home");
            Environment environment = EnvironmentBuilder.getBuilder().setUserHome(customUserHome).build();
            this.projectEnvironmentBuilder = ProjectEnvironmentBuilder.getBuilder(environment);
            URI testResourcesURI = Objects.requireNonNull(
                    getClass().getClassLoader().getResource("test-resources")).toURI();
            Files.walkFileTree(Path.of(testResourcesURI), new BuildCommandTest.Copy(Path.of(testResourcesURI),
                    this.testResources));
        } catch (URISyntaxException e) {
            Assert.fail("error loading resources");
        }
    }

    @Test(description = "Run a valid ballerina file", dataProvider = "optimizeDependencyCompilation")
    public void testRunValidBalFile(Boolean optimizeDependencyCompilation) throws IOException {
        Path validBalFilePath = this.testResources.resolve("valid-run-bal-file/file_create.bal");

        System.setProperty("user.dir", this.testResources.resolve("valid-run-bal-file").toString());
        Path tempFile = this.testResources.resolve("valid-run-bal-file/temp.txt");
        // set valid source root
        RunCommand runCommand = new RunCommand(validBalFilePath, printStream, false, optimizeDependencyCompilation);
        // name of the file as argument
        new CommandLine(runCommand).setEndOfOptionsDelimiter("").setUnmatchedOptionsArePositionalParams(true)
                .parseArgs(validBalFilePath.toString(), "--", tempFile.toString());

        Assert.assertFalse(tempFile.toFile().exists());
        runCommand.execute();

        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("run-bal.txt"));

        Assert.assertTrue(tempFile.toFile().exists());

        Files.delete(tempFile);
    }

    @Test(description = "Run non existing bal file")
    public void testRunNonExistingBalFile() throws IOException {
        // valid source root path
        Path balFilePath = this.testResources.resolve("valid-run-bal-file/xyz.bal");
        RunCommand runCommand = new RunCommand(balFilePath, printStream, false);
        new CommandLine(runCommand).parseArgs(balFilePath.toString());
        runCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertTrue(buildLog.replace("\r", "").contains(" provided file path does not exist"));
    }

    @Test(description = "Run bal file containing syntax error")
    public void testRunBalFileWithSyntaxError() {
        // valid source root path
        Path balFilePath = this.testResources.resolve("bal-file-with-syntax-error/hello_world.bal");
        RunCommand runCommand = new RunCommand(balFilePath, printStream, false);
        new CommandLine(runCommand).parseArgs(balFilePath.toString());
        try {
            runCommand.execute();
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getDetailedMessages().get(0).contains("compilation contains errors"));
        }
    }

    @Test(description = "Run bal file containing syntax error")
    public void testRunBalProjectWithSyntaxError() {
        // valid source root path
        Path balFilePath = this.testResources.resolve("bal-project-with-syntax-error");
        RunCommand runCommand = new RunCommand(balFilePath, printStream, false);
        new CommandLine(runCommand).parseArgs(balFilePath.toString());
        try {
            runCommand.execute();
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getDetailedMessages().get(0).contains("compilation contains errors"));
        }
    }

    @Test(description = "Run a valid ballerina file from a different directory")
    public void testRunValidBalProject() throws IOException {
        Path projectPath = this.testResources.resolve("validRunProject");

        Path tempFile = projectPath.resolve("temp.txt");
        // set valid source root
        RunCommand runCommand = new RunCommand(projectPath, printStream, false);
        // name of the file as argument
        new CommandLine(runCommand).setEndOfOptionsDelimiter("").setUnmatchedOptionsArePositionalParams(true)
                .parseArgs(projectPath.toString(), "--", tempFile.toString());

        Assert.assertFalse(tempFile.toFile().exists());
        runCommand.execute();
        Assert.assertTrue(tempFile.toFile().exists());

        Files.delete(tempFile);
    }

    @Test(description = "Run a valid ballerina project from the project directory",
            dataProvider = "optimizeDependencyCompilation")
    public void testRunValidBalProjectFromProjectDir(Boolean optimizeDependencyCompilation) throws IOException {
        Path projectPath = this.testResources.resolve("validRunProject");
        Path tempFile = projectPath.resolve("temp.txt");

        System.setProperty("user.dir", this.testResources.resolve("validRunProject").toString());
        // set valid source root
        RunCommand runCommand = new RunCommand(projectPath, printStream, false, optimizeDependencyCompilation);
        // name of the file as argument
        new CommandLine(runCommand).setEndOfOptionsDelimiter("").setUnmatchedOptionsArePositionalParams(true)
                .parseArgs("--", tempFile.toString());

        Assert.assertFalse(tempFile.toFile().exists());
        runCommand.execute();
        Assert.assertTrue(tempFile.toFile().exists());

        Files.delete(tempFile);
    }

    @Test(description = "Run a project with a build tool execution", dataProvider = "optimizeDependencyCompilation")
    public void testRunProjectWithBuildTool(Boolean optimizeDependencyCompilation) throws IOException {
        Path projectPath = this.testResources.resolve("proper-build-tool");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        RunCommand runCommand = new RunCommand(projectPath, printStream, false, optimizeDependencyCompilation);
        new CommandLine(runCommand).parseArgs();
        runCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""),
                getOutput("run-project-with-build-tool.txt"));
    }

    @Test(description = "Run a valid ballerina project with invalid argument")
    public void testRunCommandWithInvalidArg() {
        Path projectPath = this.testResources.resolve("validRunProject");
        Path tempFile = projectPath.resolve("temp.txt");

        System.setProperty("user.dir", this.testResources.resolve("validRunProject").toString());
        // set valid source root
        RunCommand runCommand = new RunCommand(projectPath, printStream, false);
        // name of the file as argument
        new CommandLine(runCommand).setEndOfOptionsDelimiter("").setUnmatchedOptionsArePositionalParams(true)
                .parseArgs(projectPath.toString(), tempFile.toString());
        try {
            runCommand.execute();
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getDetailedMessages().get(0).contains("unmatched command argument found"));
        }
    }

    @Test(description = "Run a valid ballerina file that has an import having platform libs",
            dataProvider = "optimizeDependencyCompilation")
    public void testRunProjectContainingImportsWithPlatformLibs(Boolean optimizeDependencyCompilation) {
        Path projectPath = this.testResources.resolve("validRunProjectImportsWithPlatformLibs");
        // set valid source root
        RunCommand runCommand = new RunCommand(projectPath, printStream, false, optimizeDependencyCompilation);
        // name of the file as argument
        new CommandLine(runCommand).parseArgs(projectPath.toString());

        // No assertions required since the command will fail upon expected behavior
        runCommand.execute();
    }

    @Test(description = "Run a valid ballerina project with provided scope platform jars")
    public void testRunProjectWithProvidedJars() throws IOException {
        Path resourcePath = this.testResources.resolve("projectWithProvidedDependency");
        BCompileUtil.compileAndCacheBala(resourcePath.resolve("pkg_a"), testDistCacheDirectory,
                projectEnvironmentBuilder);
        Path projectPath = resourcePath.resolve("pkg_b");
        System.setProperty("user.dir", projectPath.toString());

        RunCommand runCommand = new RunCommand(projectPath, printStream, false);
        runCommand.execute();
        String buildLog = readOutput(true).replaceAll("\r", "").strip();
        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                getOutput("run-project-with-provided-dep.txt"));

        ProjectUtils.deleteDirectory(projectPath.resolve("target"));
    }

    @Test(description = "Run a jar file")
    public void testRunJarFile() {
        Path projectPath = this.testResources.resolve("jar-file");
        System.setProperty("user.dir", this.testResources.resolve("jar-file").toString());

        // Run build command to generate jar file
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        buildCommand.execute();
        Assert.assertTrue(projectPath.resolve("target/bin/foo.jar").toFile().exists());

        // Try to run the har file
        Path tempFile = projectPath.resolve("foo.jar");
        RunCommand runCommand = new RunCommand(projectPath, printStream, false);

        String args = "--offline";
        new CommandLine(runCommand).setEndOfOptionsDelimiter("").setUnmatchedOptionsArePositionalParams(true)
                .parseArgs(projectPath.toString(), args, tempFile.toString());

        try {
            runCommand.execute();
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getDetailedMessages().get(0)
                    .contains("unsupported option(s) provided for jar execution"));
        }
    }

    @Test(description = "Test the heap dump generation for a project with an OOM error")
    public void testHeapDumpGenerationForOOM() {
        Path projectPath = this.testResources.resolve("oom-project");
        System.setProperty("user.dir", this.testResources.resolve("oom-project").toString());
        RunCommand runCommand = new RunCommand(projectPath, printStream, false);
        new CommandLine(runCommand);

        try {
            runCommand.execute();
        } catch (RuntimePanicException e) {
            File projectDir = new File(projectPath.toString());
            FileFilter fileFilter = WildcardFileFilter.builder().setWildcards("java_pid*.hprof").get();
            Assert.assertTrue(Objects.requireNonNull(projectDir.listFiles(fileFilter)).length > 0);
        }
    }

    @Test(description = "Run a valid ballerina file with custom target")
    public void testRunWithCustomTarget() {
        Path projectPath = this.testResources.resolve("jar-file");
        Path customTargetDir = projectPath.resolve("custom");
        System.setProperty("user.dir", projectPath.toString());

        RunCommand runCommand = new RunCommand(projectPath, printStream, false, customTargetDir);
        runCommand.execute();
        Assert.assertTrue(Files.exists(customTargetDir.resolve("cache")));
        Assert.assertTrue(Files.exists(customTargetDir.resolve("cache/wso2/foo/0.1.0")));
        Assert.assertTrue(Files.exists(customTargetDir.resolve("cache/wso2/foo/0.1.0")));
        if (!(Files.exists(customTargetDir.resolve("cache/wso2/foo/0.1.0/java21/wso2-foo-0.1.0.jar")) ||
                Files.exists(customTargetDir.resolve("cache/wso2/foo/0.1.0/any/wso2-foo-0.1.0.jar")))) {
            Assert.fail("Run command with custom target dir failed");
        }
    }

    @Test(description = "Run a ballerina project with the engagement of all type of compiler plugins",
            dataProvider = "optimizeDependencyCompilation")
    public void testRunBalProjectWithAllCompilerPlugins(Boolean optimizeDependencyCompilation) throws IOException {
        Path logFile = Path.of("build/logs/log_creator_combined_plugin/compiler-plugin.txt").toAbsolutePath();
        Files.createDirectories(logFile.getParent());
        Files.writeString(logFile, "");
        Path compilerPluginPath = Path.of("./src/test/resources/test-resources/compiler-plugins");
        BCompileUtil.compileAndCacheBala(compilerPluginPath.resolve("log_creator_pkg_provided_code_analyzer_im"),
                testDistCacheDirectory, projectEnvironmentBuilder);
        BCompileUtil.compileAndCacheBala(compilerPluginPath.resolve("log_creator_pkg_provided_code_generator_im"),
                testDistCacheDirectory, projectEnvironmentBuilder);
        BCompileUtil.compileAndCacheBala(compilerPluginPath.resolve("log_creator_pkg_provided_code_modifier_im"),
                testDistCacheDirectory, projectEnvironmentBuilder);

        Path projectPath = this.testResources.resolve("compiler-plugins/log_creator_combined_plugin");
        System.setProperty("user.dir", projectPath.toString());
        RunCommand runCommand = new RunCommand(projectPath, printStream, false, optimizeDependencyCompilation);
        new CommandLine(runCommand).parseArgs();
        runCommand.execute();
        String logFileContent =  Files.readString(logFile);
        Assert.assertTrue(logFileContent.contains("pkg-provided-syntax-node-analysis-analyzer"),
                "Package provided syntax node analysis from code analyzer has failed to run");
        Assert.assertTrue(logFileContent.contains("in-built-syntax-node-analysis-analyzer"),
                "In-Built syntax node analysis from code analyzer has failed to run");
        Assert.assertTrue(logFileContent.contains("pkg-provided-source-analyzer"),
                "Package provided source analyzer from code analyzer has failed to run");
        Assert.assertTrue(logFileContent.contains("in-built-source-analyzer"),
                "In-Built source analyzer from code analyzer has failed to run");
        Assert.assertTrue(logFileContent.contains("pkg-provided-syntax-node-analysis-generator"),
                "Package provided syntax node analysis from code generator has failed to run");
        Assert.assertTrue(logFileContent.contains("in-built-syntax-node-analysis-generator"),
                "In-Built syntax node analysis from code generator has failed to run");
        Assert.assertTrue(logFileContent.contains("pkg-provided-source-generator"),
                "Package provided source generator from code generator has failed to run");
        Assert.assertTrue(logFileContent.contains("in-built-source-generator"),
                "In-Built source generator from code generator has failed to run");
        Assert.assertTrue(logFileContent.contains("in-built-syntax-node-analysis-modifier"),
                "In-Built syntax node analysis from code modifier has failed to run");
        Assert.assertTrue(logFileContent.contains("in-built-source-modifier"),
                "In-Built source modifier from code modifier has failed to run");
        Assert.assertTrue(logFileContent.contains("pkg-provided-syntax-node-analysis-modifier"),
                "Package provided syntax node analysis from code modifier has failed to run");
        Assert.assertTrue(logFileContent.contains("pkg-provided-source-modifier"),
                "Package provided source modifier from code modifier has failed to run");
    }

    @Test(description = "Run a valid ballerina project with invalid argument")
    public void testNoClassDefProject() {
        Path projectPath = this.testResources.resolve("noClassDefProject");
        System.setProperty("user.dir", String.valueOf(projectPath));
        // set valid source root
        RunCommand runCommand = new RunCommand(projectPath, printStream, false);
        new CommandLine(runCommand).parseArgs();
        runCommand.execute();
    }

    @Test(description = "Run a ballerina project with the flag dump-graph")
    public void testRunBalProjectWithDumpGraphFlag() throws IOException {
        Path dumpGraphResourcePath = this.testResources.resolve("projectsForDumpGraph");
        BCompileUtil.compileAndCacheBala(dumpGraphResourcePath.resolve("package_c"), testDistCacheDirectory,
                projectEnvironmentBuilder);
        BCompileUtil.compileAndCacheBala(dumpGraphResourcePath.resolve("package_b"), testDistCacheDirectory,
                projectEnvironmentBuilder);

        Path projectPath = dumpGraphResourcePath.resolve("package_a");
        System.setProperty("user.dir", projectPath.toString());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        RunCommand runCommand = new RunCommand(projectPath, printStream, false);
        new CommandLine(runCommand).parseArgs("--dump-graph");
        runCommand.execute();
        String buildLog = readOutput(true).replaceAll("\r", "").strip();

        Assert.assertEquals(buildLog, getOutput("run-project-with-dump-graph.txt"));
        Assert.assertTrue(projectPath.resolve("target/cache/foo/package_a/0.1.0/java21/foo-package_a-0.1.0.jar")
                .toFile().exists());

        ProjectUtils.deleteDirectory(projectPath.resolve("target"));
    }

    @Test(description = "Run a ballerina project with the flag dump-raw-graphs")
    public void testRunBalProjectWithDumpRawGraphsFlag() throws IOException {
        Path dumpGraphResourcePath = this.testResources.resolve("projectsForDumpGraph");
        BCompileUtil.compileAndCacheBala(dumpGraphResourcePath.resolve("package_c"), testDistCacheDirectory,
                projectEnvironmentBuilder);
        BCompileUtil.compileAndCacheBala(dumpGraphResourcePath.resolve("package_b"), testDistCacheDirectory,
                projectEnvironmentBuilder);

        Path projectPath = dumpGraphResourcePath.resolve("package_a");
        System.setProperty("user.dir", projectPath.toString());

        RunCommand runCommand = new RunCommand(projectPath, printStream, false);
        new CommandLine(runCommand).parseArgs("--dump-raw-graphs");
        runCommand.execute();
        String buildLog = readOutput(true).replaceAll("\r", "").strip();

        Assert.assertEquals(buildLog, getOutput("run-project-with-dump-raw-graphs.txt"));
        Assert.assertTrue(projectPath.resolve("target/cache/foo/package_a/0.1.0/java21/foo-package_a-0.1.0.jar")
                .toFile().exists());
        ProjectUtils.deleteDirectory(projectPath.resolve("target"));
    }

    @Test(description = "Run an empty package")
    public void testRunEmptyPackage() throws IOException {
        Path projectPath = this.testResources.resolve("emptyPackage");
        System.setProperty("user.dir", projectPath.toString());

        RunCommand runCommand = new RunCommand(projectPath, printStream, false);
        new CommandLine(runCommand).parseArgs();
        try {
            runCommand.execute();
        } catch (BLauncherException e) {
            List<String> messages = e.getMessages();
            Assert.assertEquals(messages.size(), 1);
            Assert.assertEquals(messages.get(0), getOutput("build-empty-package.txt"));
        }
    }

    @Test(description = "Run an empty package with code generator build tools")
    public void testRunEmptyProjectWithBuildTools() throws IOException {
        Path projectPath = this.testResources.resolve("emptyProjectWithBuildTool");
        replaceDependenciesTomlContent(projectPath, "**INSERT_DISTRIBUTION_VERSION_HERE**",
                RepoUtils.getBallerinaShortVersion());
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        RunCommand runCommand = new RunCommand(projectPath, printStream, false);
        new CommandLine(runCommand).parseArgs();
        runCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("run-empty-project-with-build-tools.txt"));
    }

    @Test(description = "Run a project twice with the same flags and different flags")
    public void testRunAProjectTwiceWithFlags() throws IOException {
        String[] argsList1 = {
                "--offline",
                "--sticky",
                "--locking-mode=soft",
                "--experimental",
                "--optimize-dependency-compilation",
                "--remote-management",
                "--observability-included"
        };

        //Use the same flag that affects jar generation similarly in the consecutive builds
        for (String arg : argsList1) {
            Path projectPath = this.testResources.resolve("buildAProjectTwice");
            deleteDirectory(projectPath.resolve("target"));
            System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
            RunCommand runCommand = new RunCommand(projectPath, printStream, false);
            new CommandLine(runCommand).parseArgs(arg);
            runCommand.execute();
            String firstBuildLog = readOutput(true);
            runCommand = new RunCommand(projectPath, printStream, false);
            new CommandLine(runCommand).parseArgs(arg);
            runCommand.execute();
            String secondBuildLog = readOutput(true);
            Assert.assertTrue(firstBuildLog.contains("Compiling source"));
            Assert.assertFalse(firstBuildLog.contains("Compiling source (UP-TO-DATE)"));
            Assert.assertTrue(secondBuildLog.contains("Compiling source (UP-TO-DATE)"),
                    "Second build is not up-to-date for " + arg);
        }

        // Use different flags that affect jar generation differently in the consecutive builds
        for (String arg : argsList1) {
            if (arg.equals("--sticky") || arg.equals("--offline")) {
                // Skip --sticky since the second build will sticky anyway within 24 hours
                // Skip --offline since tests are always run offline
                continue;
            }
            Path projectPath = this.testResources.resolve("buildAProjectTwice");
            deleteDirectory(projectPath.resolve("target"));
            System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
            RunCommand runCommand = new RunCommand(projectPath, printStream, false);
            new CommandLine(runCommand).parseArgs(arg);
            runCommand.execute();
            String firstBuildLog = readOutput(true);
            runCommand = new RunCommand(projectPath, printStream, false);
            new CommandLine(runCommand).parseArgs();
            runCommand.execute();
            String secondBuildLog = readOutput(true);
            Assert.assertTrue(firstBuildLog.contains("Compiling source"));
            Assert.assertFalse(firstBuildLog.contains("Compiling source (UP-TO-DATE)"));
            Assert.assertTrue(secondBuildLog.contains("Compiling source"));
            Assert.assertFalse(secondBuildLog.contains("Compiling source (UP-TO-DATE)"));
        }

        String[] argsList2 = {
                "--watch",
                "--dump-bir",
                "--dump-graph",
                "--dump-raw-graphs",
                "--generate-config-schema",
                "--disable-syntax-tree-caching",
                "--dump-build-time",
                "--show-dependency-diagnostics"
        };

        // Use different flags that doesn't affect jar generation in the consecutive builds
        for (String arg : argsList2) {
            Path projectPath = this.testResources.resolve("buildAProjectTwice");
            deleteDirectory(projectPath.resolve("target"));
            System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
            RunCommand runCommand = new RunCommand(projectPath, printStream, false);
            new CommandLine(runCommand).parseArgs(arg);
            runCommand.execute();
            String firstBuildLog = readOutput(true);
            runCommand = new RunCommand(projectPath, printStream, false);
            new CommandLine(runCommand).parseArgs();
            runCommand.execute();
            String secondBuildLog = readOutput(true);
            Assert.assertTrue(firstBuildLog.contains("Compiling source"));
            Assert.assertFalse(firstBuildLog.contains("Compiling source (UP-TO-DATE)"));
            Assert.assertTrue(secondBuildLog.contains("Compiling source (UP-TO-DATE)"));
        }

        String[] argsList3 = {
                "--watch",
                "--dump-bir",
                "--dump-graph",
                "--dump-raw-graphs",
                "--generate-config-schema",
                "--disable-syntax-tree-caching",
                "--dump-build-time",
                "--show-dependency-diagnostics"
        };

        for (String arg : argsList3) {
            Path projectPath = this.testResources.resolve("buildAProjectTwice");
            deleteDirectory(projectPath.resolve("target"));
            System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
            RunCommand runCommand = new RunCommand(projectPath, printStream, false);
            new CommandLine(runCommand).parseArgs();
            runCommand.execute();
            String firstBuildLog = readOutput(true);
            runCommand = new RunCommand(projectPath, printStream, false);
            new CommandLine(runCommand).parseArgs(arg);
            runCommand.execute();
            String secondBuildLog = readOutput(true);
            Assert.assertTrue(firstBuildLog.contains("Compiling source"));
            Assert.assertFalse(firstBuildLog.contains("Compiling source (UP-TO-DATE)"));
            Assert.assertTrue(secondBuildLog.contains("Compiling source"));
            Assert.assertFalse(secondBuildLog.contains("Compiling source (UP-TO-DATE)"));
        }
    }

    @Test(description = "Run a project after 24 hours of the last build")
    public void testRunAProjectTwiceBeforeAfter24Hr() throws IOException {
        Path projectPath = this.testResources.resolve("buildAProjectTwice");
        deleteDirectory(projectPath.resolve("target"));
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        RunCommand runCommand = new RunCommand(projectPath, printStream, false);
        new CommandLine(runCommand).parseArgs();
        runCommand.execute();
        String firstBuildLog = readOutput(true);

        // Second build within 24 hours
        runCommand = new RunCommand(projectPath, printStream, false);
        new CommandLine(runCommand).parseArgs();
        runCommand.execute();
        String secondBuildLog = readOutput(true);

        Path buildFilePath = projectPath.resolve("target").resolve("build");
        BuildJson buildJson = readBuildJson(buildFilePath);
        buildJson.setLastUpdateTime(buildJson.lastUpdateTime() - (24 * 60 * 60 * 1000 + 1));
        ProjectUtils.writeBuildFile(buildFilePath, buildJson);
        runCommand = new RunCommand(projectPath, printStream, false);
        new CommandLine(runCommand).parseArgs();
        runCommand.execute();
        String thirdBuildLog = readOutput(true);
        Assert.assertTrue(firstBuildLog.contains("Compiling source"));
        Assert.assertFalse(firstBuildLog.contains("Compiling source (UP-TO-DATE)"));
        Assert.assertTrue(secondBuildLog.contains("Compiling source (UP-TO-DATE)"));
        Assert.assertTrue(thirdBuildLog.contains("Compiling source"));
        Assert.assertFalse(thirdBuildLog.contains("Compiling source (UP-TO-DATE)"));
    }

    @Test(description = "Run a project with a new file within 24 hours of the last build")
    public void testRunAProjectWithFileAddition() throws IOException {
        Path projectPath = this.testResources.resolve("buildAProjectTwice");
        deleteDirectory(projectPath.resolve("target"));
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        RunCommand runCommand = new RunCommand(projectPath, printStream, false);
        new CommandLine(runCommand).parseArgs();
        runCommand.execute();
        String firstBuildLog = readOutput(true);
        Path balFilePath = projectPath.resolve("main2.bal");
        String balContent = "public function main2() {\n}\n";
        Files.writeString(balFilePath, balContent);
        runCommand = new RunCommand(projectPath, printStream, false);
        new CommandLine(runCommand).parseArgs();
        runCommand.execute();
        String secondBuildLog = readOutput(true);
        Assert.assertTrue(firstBuildLog.contains("Compiling source"));
        Assert.assertFalse(firstBuildLog.contains("Compiling source (UP-TO-DATE)"));
        Assert.assertTrue(secondBuildLog.contains("Compiling source"));
        Assert.assertFalse(secondBuildLog.contains("Compiling source (UP-TO-DATE)"));
    }

    @Test(description = "Run a project with a file modification within 24 hours of the last build")
    public void testRunAProjectWithFileModification() throws IOException {
        Path projectPath = this.testResources.resolve("buildAProjectTwice");
        deleteDirectory(projectPath.resolve("target"));
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        RunCommand runCommand = new RunCommand(projectPath, printStream, false);
        new CommandLine(runCommand).parseArgs();
        runCommand.execute();
        String firstBuildLog = readOutput(true);
        Path balFilePath = projectPath.resolve("main.bal");
        String balContent = "public function math() {\n}\n";
        Files.writeString(balFilePath, balContent);
        runCommand = new RunCommand(projectPath, printStream, false);
        new CommandLine(runCommand).parseArgs();
        runCommand.execute();
        String secondBuildLog = readOutput(true);
        Assert.assertTrue(firstBuildLog.contains("Compiling source"));
        Assert.assertFalse(firstBuildLog.contains("Compiling source (UP-TO-DATE)"));
        Assert.assertTrue(secondBuildLog.contains("Compiling source"));
        Assert.assertFalse(secondBuildLog.contains("Compiling source (UP-TO-DATE)"));
    }

    @Test(description = "Run a project with no content change")
    public void testRunAProjectWithFileNoContentChange() throws IOException {
        Path projectPath = this.testResources.resolve("buildAProjectTwice");
        deleteDirectory(projectPath.resolve("target"));
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        RunCommand runCommand = new RunCommand(projectPath, printStream, false);
        new CommandLine(runCommand).parseArgs();
        runCommand.execute();
        String firstBuildLog = readOutput(true);
        Path balFilePath = projectPath.resolve("main.bal");
        String balContent = "public function math() {\n\n}\n";
        Files.writeString(balFilePath, balContent);
        runCommand = new RunCommand(projectPath, printStream, false);
        new CommandLine(runCommand).parseArgs();
        runCommand.execute();
        String secondBuildLog = readOutput(true);
        Assert.assertTrue(firstBuildLog.contains("Compiling source"));
        // Though the content is the same, the file modification time is changed.
        // Hence, the build should not be up-to-date
        Assert.assertFalse(firstBuildLog.contains("Compiling source (UP-TO-DATE)"));
        Assert.assertTrue(secondBuildLog.contains("Compiling source"));
        Assert.assertFalse(secondBuildLog.contains("Compiling source (UP-TO-DATE)"));
    }

    @Test(description = "Run a project after a build")
    public void testRunAProjectAfterABuild() throws IOException {
        Path projectPath = this.testResources.resolve("buildAProjectTwice");
        deleteDirectory(projectPath.resolve("target"));
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream , printStream, false);
        new CommandLine(buildCommand).parseArgs();
        buildCommand.execute();
        String firstBuildLog = readOutput(true);
        RunCommand runCommand = new RunCommand(projectPath, printStream, false);
        new CommandLine(runCommand).parseArgs();
        runCommand.execute();
        String secondBuildLog = readOutput(true);
        Assert.assertTrue(firstBuildLog.contains("buildAProjectTwice.jar"));
        Assert.assertTrue(secondBuildLog.contains("Compiling source (UP-TO-DATE)"));
    }

    @Test(description = "Run a project twice with the pack command in the middle")
    public void testBuildAProjectTwiceWithPackCommandMiddle() throws IOException {
        Path projectPath = this.testResources.resolve("buildAProjectTwice");
        deleteDirectory(projectPath.resolve("target"));
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        RunCommand runCommand = new RunCommand(projectPath, printStream, false);
        new CommandLine(runCommand).parseArgs();
        runCommand.execute();
        String firstBuildLog = readOutput(true);
        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String middleBuildLog = readOutput(true);
        runCommand = new RunCommand(projectPath, printStream, false);
        new CommandLine(runCommand).parseArgs();
        runCommand.execute();
        String secondBuildLog = readOutput(true);
        Assert.assertTrue(firstBuildLog.contains("Compiling source"));
        Assert.assertFalse(firstBuildLog.contains("Compiling source (UP-TO-DATE)"));
        Assert.assertTrue(middleBuildLog.contains("Compiling source"));
        Assert.assertFalse(middleBuildLog.contains("Compiling source (UP-TO-DATE)"));
        Assert.assertTrue(secondBuildLog.contains("Compiling source (UP-TO-DATE)"));
    }

    @AfterSuite
    public void cleanUp() throws IOException {
        Files.deleteIfExists(logFile);
        Files.deleteIfExists(logFile.getParent());
        Files.deleteIfExists(logFile.getParent().getParent());
    }
}
