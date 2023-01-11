package io.ballerina.cli.cmd;

import io.ballerina.cli.launcher.BLauncherException;
import io.ballerina.cli.launcher.RuntimePanicException;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.projects.util.ProjectUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static io.ballerina.cli.cmd.CommandOutputUtils.getOutput;
import static io.ballerina.projects.util.ProjectConstants.DIST_CACHE_DIRECTORY;

/**
 * Run command tests.
 *
 * @since 2.0.0
 */
public class RunCommandTest extends BaseCommandTest {
    private Path testResources;
    private Path testDistCacheDirectory;
    private ProjectEnvironmentBuilder projectEnvironmentBuilder;

    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        try {
            this.testResources = super.tmpDir.resolve("build-test-resources");
            Path testBuildDirectory = Paths.get("build").toAbsolutePath();
            this.testDistCacheDirectory = testBuildDirectory.resolve(DIST_CACHE_DIRECTORY);
            Path customUserHome = Paths.get("build", "user-home");
            Environment environment = EnvironmentBuilder.getBuilder().setUserHome(customUserHome).build();
            this.projectEnvironmentBuilder = ProjectEnvironmentBuilder.getBuilder(environment);
            URI testResourcesURI = Objects.requireNonNull(
                    getClass().getClassLoader().getResource("test-resources")).toURI();
            Files.walkFileTree(Paths.get(testResourcesURI), new BuildCommandTest.Copy(Paths.get(testResourcesURI),
                    this.testResources));
        } catch (URISyntaxException e) {
            Assert.fail("error loading resources");
        }
    }

    @Test(description = "Run a valid ballerina file")
    public void testRunValidBalFile() throws IOException {
        Path validBalFilePath = this.testResources.resolve("valid-run-bal-file").resolve("file_create.bal");

        System.setProperty("user.dir", this.testResources.resolve("valid-run-bal-file").toString());
        Path tempFile = this.testResources.resolve("valid-run-bal-file").resolve("temp.txt");
        // set valid source root
        RunCommand runCommand = new RunCommand(validBalFilePath, printStream, false);
        // name of the file as argument
        new CommandLine(runCommand).setEndOfOptionsDelimiter("").setUnmatchedOptionsArePositionalParams(true)
                .parseArgs(validBalFilePath.toString(), "--", tempFile.toString());

        Assert.assertFalse(tempFile.toFile().exists());
        runCommand.execute();

        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("run-bal.txt"));

        Assert.assertTrue(tempFile.toFile().exists());

        Files.delete(tempFile);
    }

    @Test(description = "Run non existing bal file")
    public void testRunNonExistingBalFile() throws IOException {
        // valid source root path
        Path validBalFilePath = this.testResources.resolve("valid-run-bal-file").resolve("xyz.bal");
        RunCommand runCommand = new RunCommand(validBalFilePath, printStream, false);
        // non existing bal file
        new CommandLine(runCommand).parseArgs(validBalFilePath.toString());
        runCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertTrue(buildLog.replaceAll("\r", "")
                .contains("The file does not exist: " + validBalFilePath));

    }

    @Test(description = "Run bal file containing syntax error")
    public void testRunBalFileWithSyntaxError() {
        // valid source root path
        Path balFilePath = this.testResources.resolve("bal-file-with-syntax-error").resolve("hello_world.bal");
        RunCommand runCommand = new RunCommand(balFilePath, printStream, false);
        // non existing bal file
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
        // non existing bal file
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

    @Test(description = "Run a valid ballerina project from the project directory")
    public void testRunValidBalProjectFromProjectDir() throws IOException {
        Path projectPath = this.testResources.resolve("validRunProject");
        Path tempFile = projectPath.resolve("temp.txt");

        System.setProperty("user.dir", this.testResources.resolve("validRunProject").toString());
        // set valid source root
        RunCommand runCommand = new RunCommand(projectPath, printStream, false);
        // name of the file as argument
        new CommandLine(runCommand).setEndOfOptionsDelimiter("").setUnmatchedOptionsArePositionalParams(true)
                .parseArgs("--", tempFile.toString());

        Assert.assertFalse(tempFile.toFile().exists());
        runCommand.execute();
        Assert.assertTrue(tempFile.toFile().exists());

        Files.delete(tempFile);
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

    @Test(description = "Run a valid ballerina file that has an import having platform libs")
    public void testRunProjectContainingImportsWithPlatformLibs() {
        Path projectPath = this.testResources.resolve("validRunProjectImportsWithPlatformLibs");
        // set valid source root
        RunCommand runCommand = new RunCommand(projectPath, printStream, false);
        // name of the file as argument
        new CommandLine(runCommand).parseArgs(projectPath.toString());

        // No assertions required since the command will fail upon expected behavior
        runCommand.execute();
    }

    @Test(description = "Run a jar file")
    public void testRunJarFile() {
        Path projectPath = this.testResources.resolve("jar-file");
        System.setProperty("user.dir", this.testResources.resolve("jar-file").toString());

        // Run build command to generate jar file
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        buildCommand.execute();
        Assert.assertTrue(projectPath.resolve("target").resolve("bin").resolve("foo.jar").toFile().exists());

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
            FileFilter fileFilter = new WildcardFileFilter("java_pid*.hprof");
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
        Assert.assertTrue(Files.exists(customTargetDir.resolve("cache").resolve("wso2").resolve("foo").resolve("0.1" +
                ".0")));
        Assert.assertTrue(Files.exists(customTargetDir.resolve("cache").resolve("wso2").resolve("foo").resolve("0.1" +
                ".0")));
        if (!(Files.exists(customTargetDir.resolve("cache").resolve("wso2").resolve("foo").resolve("0.1" +
                ".0").resolve("java11").resolve("wso2-foo-0.1.0.jar")) || Files.exists(customTargetDir.resolve(
                        "cache").resolve("wso2").resolve("foo").resolve("0.1" +
                ".0").resolve("any").resolve("wso2-foo-0.1.0.jar")))) {
            Assert.fail("Run command with custom target dir failed");
        }
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

        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(out));

        RunCommand runCommand = new RunCommand(projectPath, printStream, false);
        new CommandLine(runCommand).parseArgs("--dump-graph");
        runCommand.execute();
        String buildLog = readOutput(true).replaceAll("\r", "").strip();

        Assert.assertEquals(buildLog, getOutput("run-project-with-dump-graph.txt"));
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("package_a").resolve("0.1.0").resolve("java11")
                .resolve("foo-package_a-0.1.0.jar").toFile().exists());

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
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("package_a").resolve("0.1.0").resolve("java11")
                .resolve("foo-package_a-0.1.0.jar").toFile().exists());

        ProjectUtils.deleteDirectory(projectPath.resolve("target"));
    }

    @Test(description = "Run an empty package")
    public void testRunEmptyPackage() throws IOException {
        Path projectPath = this.testResources.resolve("emptyPackage");
        System.setProperty("user.dir", projectPath.toString());

        RunCommand runCommand = new RunCommand(projectPath, printStream, false);
        new CommandLine(runCommand).parseArgs();
        runCommand.execute();

        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("build-empty-package.txt"));
    }
}
