package io.ballerina.cli.cmd;

import io.ballerina.cli.utils.ProjectWatcher;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static io.ballerina.cli.cmd.CommandOutputUtils.getOutput;

/**
 * Tests for the --watch flag in the run command.
 *
 * @since 2201.11.0
 */
public class ProjectWatcherTest extends BaseCommandTest {
    private static final String WATCH_FLAG = "--watch";
    private static final int THREAD_SLEEP_DURATION_IN_MS = 8000;
    private static final String PROJECT_NAME_PLACEHOLDER = "INSERT_PROJECT_NAME";

    private Path watchTestResources;
    private Thread watcherThread;
    private AtomicReference<ProjectWatcher> watcher;

    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        try {
            Path testResources = super.tmpDir.resolve("build-test-resources");
            this.watchTestResources = testResources.resolve("watchFlagResources");
            URI testResourcesURI = Objects.requireNonNull(
                    getClass().getClassLoader().getResource("test-resources")).toURI();
            Files.walkFileTree(Paths.get(testResourcesURI),
                    new BuildCommandTest.Copy(Paths.get(testResourcesURI), testResources));
            watcher = new AtomicReference<>();
        } catch (URISyntaxException e) {
            Assert.fail("error loading resources");
        }
    }

    @Test(description = "Run a correct bal service file and do a correct change")
    public void testRunWatchCorrectBalFileWithCorrectChange() throws IOException, InterruptedException {
        Path balFilePath = createTempFileFromTestResource("service.bal");
        RunCommand runCommand = new RunCommand(balFilePath, printStream, false);
        new CommandLine(runCommand).parseArgs(WATCH_FLAG, balFilePath.toString());
        CountDownLatch latch = new CountDownLatch(1);
        watcherThread = new Thread(() -> {
            try {
                watcher.set(new ProjectWatcher(runCommand, balFilePath, printStream));
                latch.countDown();
                watcher.get().watch();
            } catch (IOException e) {
                Assert.fail("Error occurred while watching the project: " + e);
            }
        });
        watcherThread.start();
        latch.await();
        Thread.sleep(THREAD_SLEEP_DURATION_IN_MS);
        replaceFileContent(balFilePath, this.watchTestResources.resolve("service-updated.bal"));
        Thread.sleep(THREAD_SLEEP_DURATION_IN_MS);
        String actualOutput = readOutput(true).replace("\r", "");
        String expectedOutput = readExpectedOutputFile("watch-correct-service-file-correct-change.txt", balFilePath);
        Assert.assertEquals(actualOutput, expectedOutput);
    }

    @Test(description = "Run a correct bal service file and do a erroneous change")
    public void testRunWatchCorrectBalFileWithErroneousChange() throws IOException, InterruptedException {
        Path balFilePath = createTempFileFromTestResource("service.bal");
        RunCommand runCommand = new RunCommand(balFilePath, printStream, false);
        new CommandLine(runCommand).parseArgs(WATCH_FLAG, balFilePath.toString());
        CountDownLatch latch = new CountDownLatch(1);
        watcherThread = new Thread(() -> {
            try {
                watcher.set(new ProjectWatcher(runCommand, balFilePath, printStream));
                latch.countDown();
                watcher.get().watch();
            } catch (IOException e) {
                Assert.fail("Error occurred while watching the project: " + e);
            }
        });
        watcherThread.start();
        latch.await();
        Thread.sleep(THREAD_SLEEP_DURATION_IN_MS);
        replaceFileContent(balFilePath, this.watchTestResources.resolve("service-error.bal"));
        Thread.sleep(THREAD_SLEEP_DURATION_IN_MS);
        String actualOutput = readOutput(true).replace("\r", "");
        String expectedOutput = readExpectedOutputFile("watch-correct-service-file-error-change.txt", balFilePath);
        Assert.assertEquals(actualOutput, expectedOutput);
    }

    @Test(description = "Run a erroneous bal service file and do a correct change")
    public void testRunWatchErroneousBalFileWithCorrectChange() throws IOException, InterruptedException {
        Path balFilePath = createTempFileFromTestResource("service-error.bal");
        RunCommand runCommand = new RunCommand(balFilePath, printStream, false);
        new CommandLine(runCommand).parseArgs(WATCH_FLAG, balFilePath.toString());
        CountDownLatch latch = new CountDownLatch(1);
        watcherThread = new Thread(() -> {
            try {
                watcher.set(new ProjectWatcher(runCommand, balFilePath, printStream));
                latch.countDown();
                watcher.get().watch();
            } catch (IOException e) {
                Assert.fail("Error occurred while watching the project: " + e);
            }
        });
        watcherThread.start();
        latch.await();
        Thread.sleep(THREAD_SLEEP_DURATION_IN_MS);
        replaceFileContent(balFilePath, this.watchTestResources.resolve("service.bal"));
        Thread.sleep(THREAD_SLEEP_DURATION_IN_MS);
        String actualOutput = readOutput(true).replace("\r", "");
        String expectedOutput = readExpectedOutputFile("watch-error-service-file-correct-change.txt", balFilePath);
        Assert.assertEquals(actualOutput, expectedOutput);
    }

    @Test(description = "Run a bal file with no service")
    public void testRunWatchBalFileWithNoService() throws IOException, InterruptedException {
        Path balFilePath = createTempFileFromTestResource("main.bal");
        RunCommand runCommand = new RunCommand(balFilePath, printStream, false);
        new CommandLine(runCommand).parseArgs(WATCH_FLAG, balFilePath.toString());
        CountDownLatch latch = new CountDownLatch(1);
        watcherThread = new Thread(() -> {
            try {
                watcher.set(new ProjectWatcher(runCommand, balFilePath, printStream));
                latch.countDown();
                watcher.get().watch();
            } catch (IOException e) {
                Assert.fail("Error occurred while watching the project: " + e);
            }
        });
        watcherThread.start();
        latch.await();
        watcherThread.join(THREAD_SLEEP_DURATION_IN_MS);
        Assert.assertFalse(watcherThread.isAlive());
        String actualOutput = readOutput(true).replace("\r", "");
        String expectedOutput = readExpectedOutputFile("watch-no-service-file.txt", balFilePath);
        Assert.assertEquals(actualOutput, expectedOutput);
    }

    @Test(description = "Run a bal service project and do a correct change")
    public void testRunWatchBalProjectWithCorrectChange() throws IOException, InterruptedException {
        Path balProjectPath = createTempDirFromTestResource("service");
        RunCommand runCommand = new RunCommand(balProjectPath, printStream, false);
        new CommandLine(runCommand).parseArgs(WATCH_FLAG, balProjectPath.toString());
        CountDownLatch latch = new CountDownLatch(1);
        watcherThread = new Thread(() -> {
            try {
                watcher.set(new ProjectWatcher(runCommand, balProjectPath, printStream));
                latch.countDown();
                watcher.get().watch();
            } catch (IOException e) {
                Assert.fail("Error occurred while watching the project: " + e);
            }
        });
        watcherThread.start();
        latch.await();
        Thread.sleep(THREAD_SLEEP_DURATION_IN_MS);

        // Update a source file
        replaceFileContent(balProjectPath.resolve("service.bal"),
                this.watchTestResources.resolve("project-service-updated.bal"));
        Thread.sleep(THREAD_SLEEP_DURATION_IN_MS);

        // Add a new source file
        Files.copy(this.watchTestResources.resolve("constants.bal"), balProjectPath.resolve("constants.bal"));
        Thread.sleep(THREAD_SLEEP_DURATION_IN_MS);

        // Remove a source file
        Files.delete(balProjectPath.resolve("constants.bal"));
        Thread.sleep(THREAD_SLEEP_DURATION_IN_MS);
        String actualOutput = readOutput(true).replace("\r", "");
        String expectedOutput = readExpectedOutputFile("watch-correct-service-project-correct-change.txt",
                balProjectPath);
        Assert.assertEquals(actualOutput, expectedOutput);
    }

    @Test(description = "Run a bal project with no service")
    public void testRunWatchBalProjectWithNoService() throws IOException, InterruptedException {
        Path balProjectPath = createTempDirFromTestResource("main");
        RunCommand runCommand = new RunCommand(balProjectPath, printStream, false);
        new CommandLine(runCommand).parseArgs(WATCH_FLAG, balProjectPath.toString());
        CountDownLatch latch = new CountDownLatch(1);
        watcherThread = new Thread(() -> {
            try {
                watcher.set(new ProjectWatcher(runCommand, balProjectPath, printStream));
                latch.countDown();
                watcher.get().watch();
            } catch (IOException e) {
                Assert.fail("Error occurred while watching the project: " + e);
            }
        });
        watcherThread.start();
        latch.await();
        watcherThread.join(THREAD_SLEEP_DURATION_IN_MS);
        Assert.assertFalse(watcherThread.isAlive());
        String actualOutput = readOutput(true).replace("\r", "");
        String expectedOutput = readExpectedOutputFile("watch-no-service-project.txt", balProjectPath);
        Assert.assertEquals(actualOutput, expectedOutput);
    }

    @Test(description = "Run a bal service project and do valid file change")
    public void testRunWatchBalProjectWithValidFileChanges() throws IOException, InterruptedException {
        Path balProjectPath = createTempDirFromTestResource("service");
        RunCommand runCommand = new RunCommand(balProjectPath, printStream, false);
        new CommandLine(runCommand).parseArgs(WATCH_FLAG, balProjectPath.toString());
        CountDownLatch latch = new CountDownLatch(1);
        watcherThread = new Thread(() -> {
            try {
                watcher.set(new ProjectWatcher(runCommand, balProjectPath, printStream));
                latch.countDown();
                watcher.get().watch();
            } catch (IOException e) {
                Assert.fail("Error occurred while watching the project: " + e);
            }
        });
        watcherThread.start();
        latch.await();
        Thread.sleep(THREAD_SLEEP_DURATION_IN_MS);

        // Update the Ballerina.toml
        replaceFileContent(balProjectPath.resolve("Ballerina.toml"),
                this.watchTestResources.resolve("Ballerina-copy.toml"));
        Thread.sleep(THREAD_SLEEP_DURATION_IN_MS);

        // Add a new module with a source file
        Files.copy(this.watchTestResources.resolve("mod1.bal"),
                balProjectPath.resolve("modules").resolve("mod1").resolve("mod1.bal"));
        Thread.sleep(THREAD_SLEEP_DURATION_IN_MS);

        // Add a new resource file
        Files.copy(this.watchTestResources.resolve("hello.txt"),
                balProjectPath.resolve("resources").resolve("hello.txt"));
        Thread.sleep(THREAD_SLEEP_DURATION_IN_MS);

        String actualOutput = readOutput(true).replace("\r", "");
        String expectedOutput = readExpectedOutputFile("watch-service-project-valid-changes.txt", balProjectPath);
        Assert.assertEquals(actualOutput, expectedOutput);
    }

    @Test(description = "Run a bal service project and do invalid file change")
    public void testRunWatchBalProjectWithInvalidFileChanges() throws IOException, InterruptedException {
        Path balProjectPath = createTempDirFromTestResource("service");
        RunCommand runCommand = new RunCommand(balProjectPath, printStream, false);
        new CommandLine(runCommand).parseArgs(WATCH_FLAG, balProjectPath.toString());
        CountDownLatch latch = new CountDownLatch(1);
        watcherThread = new Thread(() -> {
            try {
                watcher.set(new ProjectWatcher(runCommand, balProjectPath, printStream));
                latch.countDown();
                watcher.get().watch();
            } catch (IOException e) {
                Assert.fail("Error occurred while watching the project: " + e);
            }
        });
        watcherThread.start();
        latch.await();
        Thread.sleep(THREAD_SLEEP_DURATION_IN_MS);

        // Add test file
        Path testFilePath = balProjectPath.resolve("tests").resolve("test.bal");
        Files.copy(this.watchTestResources.resolve("constants.bal"), testFilePath);
        Thread.sleep(THREAD_SLEEP_DURATION_IN_MS);

        // Change test file
        replaceFileContent(testFilePath, this.watchTestResources.resolve("project-service-updated.bal"));
        Thread.sleep(THREAD_SLEEP_DURATION_IN_MS);

        // Delete a test file
        Files.delete(testFilePath);
        Thread.sleep(THREAD_SLEEP_DURATION_IN_MS);

        // Add a test file to a module
        Files.copy(this.watchTestResources.resolve("constants.bal"),
                balProjectPath.resolve("modules").resolve("mod1").resolve("tests").resolve("test.bal"));
        Thread.sleep(THREAD_SLEEP_DURATION_IN_MS);

        // Add a source file into modules/
        Files.copy(this.watchTestResources.resolve("constants.bal"),
                balProjectPath.resolve("modules").resolve("constants.bal"));

        // Add a json file to the root
        Files.copy(this.watchTestResources.resolve("hello.json"), balProjectPath.resolve("hello.json"));

        // Add a source file to target/
        Files.copy(this.watchTestResources.resolve("constants.bal"),
                balProjectPath.resolve("target").resolve("constants.bal"));
        Thread.sleep(THREAD_SLEEP_DURATION_IN_MS);

        String actualOutput = readOutput(true).replace("\r", "");
        String expectedOutput = readExpectedOutputFile("watch-service-project-invalid-changes.txt", balProjectPath);
        Assert.assertEquals(actualOutput, expectedOutput);
    }

    @AfterMethod
    public void afterMethod() {
        try {
            if (watcherThread != null && watcher.get() != null) {
                stopProjectWatcher(watcherThread, watcher.get());
            }
        } catch (InterruptedException e) {
            Assert.fail("Error occurred while stopping the project watcher. " +
                    "Please kill any stale java processes that were started by the project watcher tests: " + e);
        }
    }

    private Path createTempFileFromTestResource(String fileName) throws IOException {
        Path balFilePath = this.watchTestResources.resolve(fileName);
        Path tempFilePath = Files.createTempFile("service", ".bal");
        replaceFileContent(tempFilePath, balFilePath);
        return tempFilePath;
    }

    private Path createTempDirFromTestResource(String projectName) throws IOException {
        Path balProjectPath = this.watchTestResources.resolve(projectName);
        Path tempProjectPath = Files.createTempDirectory("service");
        tempProjectPath.toFile().deleteOnExit();
        Files.walkFileTree(balProjectPath, new BuildCommandTest.Copy(balProjectPath, tempProjectPath));
        return tempProjectPath;
    }

    private void replaceFileContent(Path filePath, Path copyFrom) {
        try {
            String newContent = Files.readString(copyFrom);
            Files.writeString(filePath, newContent);
        } catch (IOException e) {
            Assert.fail("Error occurred while writing to the file: " + e);
        }
    }

    private String readExpectedOutputFile(String expectedOutputFile, Path balFilePath) throws IOException {
        return getOutput(expectedOutputFile)
                .replace(PROJECT_NAME_PLACEHOLDER, balFilePath.getFileName().toString());
    }

    private void stopProjectWatcher(Thread thread, ProjectWatcher watcher) throws InterruptedException {
        watcher.stopWatching();
        thread.join();
    }
}
