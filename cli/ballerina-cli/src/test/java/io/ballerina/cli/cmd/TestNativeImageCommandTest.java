package io.ballerina.cli.cmd;

import io.ballerina.cli.launcher.BLauncherException;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.projects.util.ProjectConstants;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static io.ballerina.projects.util.ProjectConstants.DIST_CACHE_DIRECTORY;

/**
 * Test Native Image command tests.
 *
 * @since 2.3.0
 */
public class TestNativeImageCommandTest extends BaseCommandTest {
    private Path testResources;
    private Path testDistCacheDirectory;
    ProjectEnvironmentBuilder projectEnvironmentBuilder;
    private static final Path LOG_FILE = Path.of("build/logs/log_creator_combined_plugin/compiler-plugin.txt")
            .toAbsolutePath();

    @Override
    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        Files.createDirectories(LOG_FILE.getParent());
        Files.writeString(LOG_FILE, "");
        try {
            this.testResources = super.tmpDir.resolve("test-cmd-test-resources");
            Path testBuildDirectory = Path.of("build").toAbsolutePath();
            this.testDistCacheDirectory = testBuildDirectory.resolve(DIST_CACHE_DIRECTORY);
            Path customUserHome = Path.of("build", "user-home");
            Environment environment = EnvironmentBuilder.getBuilder().setUserHome(customUserHome).build();
            projectEnvironmentBuilder = ProjectEnvironmentBuilder.getBuilder(environment);
            URI testResourcesURI = Objects.requireNonNull(
                    getClass().getClassLoader().getResource("test-resources")).toURI();
            Files.walkFileTree(Path.of(testResourcesURI), new TestCommandTest.Copy(Path.of(testResourcesURI),
                    this.testResources));
        } catch (URISyntaxException e) {
            Assert.fail("error loading resources");
        }
    }

    @Test(description = "Test a valid ballerina project")
    public void testNativeImageTests() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithTests");
        System.setProperty(ProjectConstants.USER_DIR, projectPath.toString());
        TestCommand testCommand = new TestCommand(projectPath, printStream, printStream, false, true, "");
        new CommandLine(testCommand).parseArgs();
        testCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertTrue(buildLog.contains("1 passing"));
    }

    @Test(description = "Test a valid ballerina project")
    public void testNativeImageTestsWithAdditionalArgs() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithTests");
        System.setProperty(ProjectConstants.USER_DIR, projectPath.toString());
        TestCommand testCommand = new TestCommand(projectPath, printStream, printStream, false, true, "-H:Name=foo");
        new CommandLine(testCommand).parseArgs();
        try {
            testCommand.execute();
        } catch (BLauncherException e) {
            readOutput(false);
            Assert.fail(e.getDetailedMessages().get(0));
        }
        String buildLog = readOutput(true);
        Assert.assertTrue(buildLog.contains("Generating 'winery' (executable)"), buildLog);
    }

    //TODO: Fix this testcase (#42937)
    @Test(description = "Test function mocking", enabled = false)
    public void testNativeFunctionMockTests() throws IOException {
        Path projectPath = this.testResources.resolve("mockFunctionNative");
        System.setProperty(ProjectConstants.USER_DIR, projectPath.toString());
        TestCommand testCommand = new TestCommand(projectPath, printStream, printStream, false, true, "");
        // non existing bal file
        new CommandLine(testCommand).parseArgs();
        try {
            testCommand.execute();
        } catch (BLauncherException e) {
            String output = readOutput(true);
            Assert.fail(e + "\n" + e.getDetailedMessages().toString() + "\n" + output);
        }
        String buildLog = readOutput(true);
        Assert.assertTrue(buildLog.contains("[pass] intAddTest"));
        Assert.assertTrue(buildLog.contains("[pass] intAddTest2"));
        Assert.assertTrue(buildLog.contains("[pass] intAddTest3"));
        Assert.assertTrue(buildLog.contains("[pass] intAddTest4"));
        Assert.assertTrue(buildLog.contains("[pass] intAddInsideTest"));
        Assert.assertTrue(buildLog.contains("[pass] intDivInsideTest"));
    }


    //TODO: Change the output once the resource generation plugin is disabled
    @Test(description = "Test a valid ballerina file")
    public void testTestBalFile() {
        Path validBalFilePath = this.testResources.resolve("valid-test-bal-file").resolve("sample_tests.bal");
        System.setProperty(ProjectConstants.USER_DIR, this.testResources.resolve("valid-test-bal-file").toString());
        TestCommand testCommand = new TestCommand(validBalFilePath, printStream, printStream, false, true, "");
        new CommandLine(testCommand).parseArgs(validBalFilePath.toString());
        try {
            testCommand.execute();
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getDetailedMessages().get(0).contains("native image testing is not supported for " +
                    "standalone Ballerina files containing resources"), e.getDetailedMessages().get(0));
        }
    }

    //TODO: Change the output once the resource generation plugin is disabled
    @Test(description = "Test a valid ballerina file with additional args")
    public void testTestBalFileWithAdditionalArgs() {
        Path validBalFilePath = this.testResources.resolve("valid-test-bal-file").resolve("sample_tests.bal");
        System.setProperty(ProjectConstants.USER_DIR, this.testResources.resolve("valid-test-bal-file").toString());
        TestCommand testCommand = new TestCommand(validBalFilePath, printStream, printStream,
                false, true, "-H:Name=foo");
        new CommandLine(testCommand).parseArgs(validBalFilePath.toString());
        try {
            testCommand.execute();
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getDetailedMessages().get(0).contains("native image testing is not supported for " +
                    "standalone Ballerina files containing resources"), e.getDetailedMessages().get(0));
        }
    }

    //TODO: Change the output once the resource generation plugin is disabled
    @Test(description = "Test a valid ballerina file with periods in the file name")
    public void testTestBalFileWithPeriods() {
        Path validBalFilePath = this.testResources.resolve("valid-test-bal-file").resolve("sample.tests.bal");

        System.setProperty(ProjectConstants.USER_DIR, this.testResources.resolve("valid-test-bal-file").toString());
        TestCommand testCommand = new TestCommand(validBalFilePath, printStream, printStream,
                false, true, "");
        new CommandLine(testCommand).parseArgs(validBalFilePath.toString());
        try {
            testCommand.execute();
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getDetailedMessages().get(0).contains("native image testing is not supported for " +
                    "standalone Ballerina files containing resources"), e.getDetailedMessages().get(0));
        }
    }
    
    @AfterClass
    public void cleanUp() throws IOException {
        Files.deleteIfExists(LOG_FILE);
        Files.deleteIfExists(LOG_FILE.getParent());
        Files.deleteIfExists(LOG_FILE.getParent().getParent());
    }
}
