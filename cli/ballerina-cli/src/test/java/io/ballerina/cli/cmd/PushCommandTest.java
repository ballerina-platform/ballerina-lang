/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.cli.cmd;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.Settings;
import io.ballerina.projects.TomlDocument;
import io.ballerina.projects.internal.ProjectFiles;
import io.ballerina.projects.internal.SettingsBuilder;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static io.ballerina.cli.cmd.CommandOutputUtils.getOutput;
import static io.ballerina.projects.util.ProjectConstants.BALA_EXTENSION;

/**
 * Push command tests.
 *
 * @since 2.0.0
 */
//@PowerMockIgnore({"jdk.internal.reflect.*", "javax.net.*", "com.sun.*"})
public class PushCommandTest extends BaseCommandTest {

    private static final String VALID_PROJECT = "validApplicationProject";
    private static final String POM_EXTENSION = ".pom";
    private Path testResources;

    @Override
    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        try {
            this.testResources = super.tmpDir.resolve("build-test-resources");
            URI testResourcesURI = Objects.requireNonNull(getClass().getClassLoader().getResource("test-resources"))
                    .toURI();
            Files.walkFileTree(Path.of(testResourcesURI),
                               new BuildCommandTest.Copy(Path.of(testResourcesURI), this.testResources));
        } catch (URISyntaxException e) {
            Assert.fail("error loading resources");
        }
    }

    @AfterMethod(alwaysRun = true)
    @Override
    public void afterMethod() throws IOException {
        super.afterMethod();
        Path validBalProject = Path.of("build/validProjectWithTarget");
        ProjectUtils.deleteDirectory(validBalProject);
        validBalProject = Path.of("build/tool-gayals");
        ProjectUtils.deleteDirectory(validBalProject);
    }

    @Test(description = "Push package with invalid path")
    public void testPushWithInvalidPath() throws IOException {
        Path validBalProject = this.testResources.resolve(VALID_PROJECT);
        PushCommand pushCommand = new PushCommand(validBalProject, printStream, printStream, false);
        String invalidPath = "tests";
        new CommandLine(pushCommand).parseArgs(invalidPath);
        pushCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replace("\r", "");
        String expected = "path provided for the bala file does not exist: " + invalidPath + ".";
        Assert.assertTrue(actual.contains(expected));
    }

    @Test (description = "Push a package to a custom remote repository")
    public void testPushPackageCustom() throws IOException {
        String org = "luheerathan";
        String packageName = "pact1";
        String version = "0.1.0";
        String expected = "Successfully pushed src/test/resources/test-resources/custom-repo/" +
                "luheerathan-pact1-any-0.1.0.bala to 'repo-push-pull' repository.\n";

        Path mockRepo = Path.of("build/ballerina-home/repositories/repo-push-pull");
        Path balaPath = Path.of("src/test/resources/test-resources/custom-repo/luheerathan-pact1-any-0.1.0.bala");
        PushCommand pushCommand = new PushCommand(null, printStream, printStream, false, balaPath);
        String[] args = { "--repository=repo-push-pull" };
        new CommandLine(pushCommand).parseArgs(args);
        try (MockedStatic<RepoUtils> repoUtils = Mockito.mockStatic(RepoUtils.class, Mockito.CALLS_REAL_METHODS)) {
            repoUtils.when(RepoUtils::readSettings).thenReturn(readSettings(testResources.resolve("custom-repo")
                    .resolve("Settings.toml"), mockRepo.toAbsolutePath().toString()
                    .replace("\\", "/")));
            pushCommand.execute();
        }
        String buildLog = readOutput(true);
        String actual = buildLog.replace("\r", "");
        Assert.assertEquals(actual.replace("\\", "/"), expected);
        String artifact = packageName + "-" + version + BALA_EXTENSION;
        String pomFile = packageName + "-" + version + POM_EXTENSION;
        String pushPullPath = mockRepo.resolve(org).resolve(packageName).resolve(version).toAbsolutePath().toString();
        for (String ext : new String[]{".sha1", ".md5", ""}) {
            Assert.assertTrue(Path.of(pushPullPath, artifact + ext).toFile().exists());
            Assert.assertTrue(Path.of(pushPullPath, pomFile + ext).toFile().exists());
        }
    }

    @Test (description = "Push a package to a custom remote repository(not exist in Settings.toml)")
    public void testPushPackageNonExistingCustom() throws IOException {
        String expected = "ballerina: unsupported repository 'repo-push-pul' found. " +
                "Only 'local' repository and repositories mentioned in the Settings.toml are supported.\n";

        Path mockRepo = Path.of("build/ballerina-home/repositories/repo-push-pull");
        Path balaPath = Path.of("src/test/resources/test-resources/custom-repo/luheerathan-pact1-any-0.1.0.bala");
        PushCommand pushCommand = new PushCommand(null, printStream, printStream, false, balaPath);
        String[] args = { "--repository=repo-push-pul" };
        new CommandLine(pushCommand).parseArgs(args);
        try (MockedStatic<RepoUtils> repoUtils = Mockito.mockStatic(RepoUtils.class, Mockito.CALLS_REAL_METHODS)) {
            repoUtils.when(RepoUtils::readSettings).thenReturn(
                    readSettings(testResources.resolve("custom-repo/Settings.toml"),
                            mockRepo.toAbsolutePath().toString()));
            pushCommand.execute();
        }
        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertEquals(actual, expected);
    }

    private static Settings readSettings(Path settingsFilePath, String repoPath) {
        try {
            String settingString = Files.readString(settingsFilePath);
            settingString = settingString.replaceAll("REPO_PATH", repoPath);
            TomlDocument settingsTomlDocument = TomlDocument
                    .from(String.valueOf(settingsFilePath.getFileName()), settingString);
            SettingsBuilder settingsBuilder = SettingsBuilder.from(settingsTomlDocument);
            return settingsBuilder.settings();
        } catch (IOException e) {
            return Settings.from();
        }
    }


    @Test(description = "Push package with invalid file extension")
    public void testPushWithInvalidFileExtension() throws IOException {
        Path validBalProject = this.testResources.resolve(VALID_PROJECT);
        PushCommand pushCommand = new PushCommand(validBalProject, printStream, printStream, false);
        String invalidExtensionFilePath = this.testResources.resolve("non-bal-file/hello_world.txt").toString();
        new CommandLine(pushCommand).parseArgs(invalidExtensionFilePath);
        pushCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        String expected = "file provided is not a bala file: " + invalidExtensionFilePath + ".";
        Assert.assertTrue(actual.contains(expected));
    }

    @Test(description = "Push package with custom path")
    public void testPushWithCustomPath() throws IOException {
        Path validBalProject = Path.of("build/validProjectWithTarget");

        FileUtils.copyDirectory(
                this.testResources.resolve("validProjectWithTarget").toFile(), validBalProject.toFile());
        FileUtils.moveDirectory(
                validBalProject.resolve("target-dir").toFile(), validBalProject.resolve("custom").toFile());

        Path customTargetDirBalaPath = validBalProject.resolve("custom/bala/foo-winery-any-0.1.0.bala");
        PushCommand pushCommand = new PushCommand(validBalProject, printStream, printStream, false,
                customTargetDirBalaPath);
        String[] args = { "--repository=local" };
        new CommandLine(pushCommand).parseArgs(args);

        Path mockRepo = Path.of("build/ballerina-home");

        try (MockedStatic<RepoUtils> repoUtils = Mockito.mockStatic(RepoUtils.class)) {
            repoUtils.when(RepoUtils::createAndGetHomeReposPath).thenReturn(mockRepo);
            repoUtils.when(RepoUtils::getBallerinaShortVersion).thenReturn("1.0.0");
            repoUtils.when(RepoUtils::readSettings).thenReturn(Settings.from());
            pushCommand.execute();
        }

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        String expected = "Successfully pushed " + customTargetDirBalaPath + " to 'local' repository.";
        Assert.assertTrue(actual.contains(expected));

        try {
            ProjectFiles.validateBalaProjectPath(mockRepo.resolve("repositories/local/bala/foo/winery/0.1.0/any"));
        } catch (ProjectException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Push a bala with a path-traversal platformDependencies path is rejected")
    public void testPushWithPathTraversalBalaRejected() throws IOException {
        Path testRoot = Files.createTempDirectory("push-path-traversal-test");
        try {
            // balaDir is the intended extraction directory; the malicious path below tries to
            // escape it and write two directories up, into testRoot.
            Path balaDir = testRoot.resolve("nested").resolve("bala-dir");
            Files.createDirectories(balaDir);
            Path maliciousBala = balaDir.resolve("testorg-attackpkg-any-0.1.0.bala");
            String packageJson = "{"
                    + "\"organization\":\"testorg\","
                    + "\"name\":\"attackpkg\","
                    + "\"version\":\"0.1.0\","
                    + "\"platform\":\"java17\","
                    + "\"platformDependencies\":[{"
                    + "\"path\":\"../../evil-marker.txt\","
                    + "\"artifactId\":\"evil\",\"groupId\":\"evil\",\"version\":\"1.0.0\"}]"
                    + "}";
            try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(maliciousBala))) {
                zipOut.putNextEntry(new ZipEntry("package.json"));
                zipOut.write(packageJson.getBytes(StandardCharsets.UTF_8));
                zipOut.closeEntry();
            }

            PushCommand pushCommand = new PushCommand(testRoot, printStream, printStream, false, maliciousBala);
            String[] args = { "--repository=local" };
            new CommandLine(pushCommand).parseArgs(args);

            Path mockRepo = Path.of("build/ballerina-home");
            try (MockedStatic<RepoUtils> repoUtils = Mockito.mockStatic(RepoUtils.class)) {
                repoUtils.when(RepoUtils::createAndGetHomeReposPath).thenReturn(mockRepo);
                repoUtils.when(RepoUtils::getBallerinaShortVersion).thenReturn("1.0.0");
                repoUtils.when(RepoUtils::readSettings).thenReturn(Settings.from());
                pushCommand.execute();
            }

            String buildLog = readOutput(true);
            String actual = buildLog.replaceAll("\r", "");
            Assert.assertFalse(actual.contains("Successfully pushed"),
                    "malicious bala should not have been pushed successfully: " + actual);
            Assert.assertTrue(actual.contains("resolves outside the bala directory"),
                    "expected a path traversal rejection message but got: " + actual);
            Assert.assertFalse(Files.exists(testRoot.resolve("evil-marker.txt")),
                    "path traversal wrote a file outside the intended bala directory");
        } finally {
            ProjectUtils.deleteDirectory(testRoot);
        }
    }

    @Test(description = "Push a tool to local repository")
    public void testPushToolToLocal() throws IOException {
        Path validBalProject = Path.of("build/tool-gayals");

        FileUtils.copyDirectory(
                this.testResources.resolve("tool-gayals").toFile(), validBalProject.toFile());
        FileUtils.moveDirectory(
                validBalProject.resolve("target-dir").toFile(), validBalProject.resolve("custom").toFile());
        Path customTargetDirBalaPath =
                validBalProject.resolve("custom/bala/gayaldassanayake-tool_gayal-java25-1.1.0.bala");
        PushCommand pushCommand = new PushCommand(validBalProject, printStream, printStream, false,
                customTargetDirBalaPath);
        String[] args = { "--repository=local" };
        new CommandLine(pushCommand).parseArgs(args);

        Path mockRepo = Path.of("build/ballerina-home");

        try (MockedStatic<RepoUtils> repoUtils = Mockito.mockStatic(RepoUtils.class)) {
            repoUtils.when(RepoUtils::createAndGetHomeReposPath).thenReturn(mockRepo);
            repoUtils.when(RepoUtils::getBallerinaShortVersion).thenReturn("1.0.0");
            repoUtils.when(RepoUtils::readSettings).thenReturn(Settings.from());
            pushCommand.execute();
        }

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        String expected = "Successfully pushed " + customTargetDirBalaPath + " to 'local' repository.";
        Assert.assertTrue(actual.contains(expected));

        try {
            ProjectFiles.validateBalaProjectPath(
                    mockRepo.resolve("repositories/local/bala/gayaldassanayake/tool_gayal/1.1.0/java25"));
        } catch (ProjectException e) {
            Assert.fail(e.getMessage());
        }

        Path localToolJsonPath = mockRepo.resolve("repositories/local/bala/local-tools.json");

        Assert.assertTrue(Files.exists(localToolJsonPath));

        try (BufferedReader bufferedReader = Files.newBufferedReader(localToolJsonPath, StandardCharsets.UTF_8)) {
            JsonObject localToolJson = new Gson().fromJson(bufferedReader, JsonObject.class);
            JsonObject pkgDesc = localToolJson.get("luhee").getAsJsonObject();

            Assert.assertEquals(pkgDesc.get("org").getAsString(), "gayaldassanayake");
            Assert.assertEquals(pkgDesc.get("name").getAsString(), "tool_gayal");
        }

    }

    @Test(description = "Push package without bala directory")
    public void testPushWithoutBalaDir() throws IOException {
        String expected = "cannot find bala file for the package: winery. Run "
                + "'bal pack' to compile and generate the bala.";

        Path validBalProject = this.testResources.resolve(VALID_PROJECT);
        PushCommand pushCommand = new PushCommand(validBalProject, printStream, printStream, false);
        new CommandLine(pushCommand).parseArgs();
        pushCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(actual.contains(expected));
    }

    @Test(description = "Push package without bala file")
    public void testPushWithoutBala() throws IOException {
        Path projectPath = this.testResources.resolve(VALID_PROJECT);
        System.setProperty("user.dir", projectPath.toString());

        // Build project
        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("compile-bal-project.txt"));
        Assert.assertTrue(projectPath.resolve("target/bala/foo-winery-any-0.1.0.bala").toFile().exists());

        // Delete bala
        Assert.assertTrue(projectPath.resolve("target/bala/foo-winery-any-0.1.0.bala").toFile().delete());

        // Push
        String expected = "cannot find bala file for the package: winery. Run "
                + "'bal pack' to compile and generate the bala.";
        PushCommand pushCommand = new PushCommand(projectPath, printStream, printStream, false);
        new CommandLine(pushCommand).parseArgs();
        pushCommand.execute();

        buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(actual.contains(expected));
    }

    @Test(description = "Test push command with argument and a help")
    public void testPushCommandArgAndHelp() throws IOException {
        Path validBalProject = this.testResources.resolve(VALID_PROJECT);
        // Test if no arguments was passed in
        String[] args = { "sample2", "--help" };
        PushCommand pushCommand = new PushCommand(validBalProject, printStream, printStream, false);
        new CommandLine(pushCommand).parseArgs(args);
        pushCommand.execute();

        Assert.assertTrue(readOutput().contains("ballerina-push - Push the Ballerina Archive (BALA)"));
    }

    @Test(description = "Test push command with help flag")
    public void testPushCommandWithHelp() throws IOException {
        Path validBalProject = this.testResources.resolve(VALID_PROJECT);
        // Test if no arguments was passed in
        String[] args = { "-h" };
        PushCommand pushCommand = new PushCommand(validBalProject, printStream, printStream, false);
        new CommandLine(pushCommand).parseArgs(args);
        pushCommand.execute();

        Assert.assertTrue(readOutput().contains("ballerina-push - Push the Ballerina Archive (BALA)"));
    }

    @Test
    public void testPushToCustomRepo() throws IOException {
        Path validBalProject = Path.of("build/validProjectWithTarget");
        FileUtils.copyDirectory(
                this.testResources.resolve("validProjectWithTarget").toFile(), validBalProject.toFile());
        FileUtils.moveDirectory(
                validBalProject.resolve("target-dir").toFile(), validBalProject.resolve("target").toFile());

        Path mockRepo = Path.of("build/ballerina-home");
        // Test if no arguments was passed in
        String[] args = { "--repository=local" };
        PushCommand pushCommand = new PushCommand(validBalProject, printStream, printStream, false);
        new CommandLine(pushCommand).parseArgs(args);
        try (MockedStatic<RepoUtils> repoUtils = Mockito.mockStatic(RepoUtils.class)) {
            repoUtils.when(RepoUtils::createAndGetHomeReposPath).thenReturn(mockRepo);
            repoUtils.when(RepoUtils::getBallerinaShortVersion).thenReturn("1.0.0");
            repoUtils.when(RepoUtils::readSettings).thenReturn(Settings.from());
            pushCommand.execute();
        }

        try {
            ProjectFiles.validateBalaProjectPath(mockRepo.resolve("repositories/local/bala/foo/winery/0.1.0/any"));
        } catch (ProjectException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testPushWithoutPackageMd() throws IOException {
        Path projectPath = this.testResources.resolve(VALID_PROJECT);
        System.setProperty("user.dir", projectPath.toString());

        // Pack project
        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        Assert.assertTrue(projectPath.resolve("target/bala/foo-winery-any-0.1.0.bala").toFile().exists());

        // Push
        String expected = "README.md file is missing in the bala file";

        PushCommand pushCommand = new PushCommand(projectPath, printStream, printStream, false);
        new CommandLine(pushCommand).parseArgs();
        pushCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(actual.contains(expected));
    }

    @Test (enabled = false)
    public void testPushWithEmptyPackageMd() throws IOException {
        Path projectPath = this.testResources.resolve(VALID_PROJECT);
        System.setProperty("user.dir", projectPath.toString());
        Files.createFile(projectPath.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME));

        // Pack project
        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        Assert.assertTrue(projectPath.resolve("target/bala/foo-winery-any-0.1.0.bala").toFile().exists());

        Files.delete(projectPath.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME));

        // Push
        String expected = "md file cannot be empty";

        PushCommand pushCommand = new PushCommand(projectPath, printStream, printStream, false);
        new CommandLine(pushCommand).parseArgs();
        pushCommand.execute();

        String buildLog = readOutput(true);
        String actual = buildLog.replaceAll("\r", "");
        Assert.assertTrue(actual.contains(expected));
    }

    @Test
    public void testPushToAnUnsupportedRepo() throws IOException {
        Path projectPath = this.testResources.resolve("validLibraryProject");
        // Build project
        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        Assert.assertTrue(projectPath.resolve("target/bala/foo-winery-any-0.1.0.bala").toFile().exists());

        String[] args = { "--repository=stdlib.local" };
        PushCommand pushCommand = new PushCommand(projectPath, printStream, printStream, false);
        new CommandLine(pushCommand).parseArgs(args);
        pushCommand.execute();
        String errMsg = "unsupported repository 'stdlib.local' found. Only 'local' repository and repositories " +
                "mentioned in the Settings.toml are supported.";
        Assert.assertTrue(readOutput().contains(errMsg));
    }

    @Test(description = "Find the Sigstore bundle next to the bala")
    public void testFindSiblingSigstoreBundle() throws IOException {
        Path balaDir = Files.createTempDirectory(super.tmpDir, "sigstore-bundle");
        Path balaPath = balaDir.resolve("foo-winery-any-0.1.0.bala");
        Files.writeString(balaPath, "bala");
        Assert.assertNull(PushCommand.findSiblingSigstoreBundle(balaPath, printStream));

        Path bundlePath = balaDir.resolve("foo-winery-any-0.1.0.bala.sigstore.json");
        Path bundleTarget = balaDir.resolve("bundle.json");
        Files.writeString(bundleTarget, "{}");
        boolean symlinkCreated;
        try {
            Files.createSymbolicLink(bundlePath, bundleTarget.getFileName());
            symlinkCreated = true;
        } catch (UnsupportedOperationException | IOException e) {
            // Creating symbolic links is not permitted on this platform
            symlinkCreated = false;
        }
        if (symlinkCreated) {
            readOutput(true);
            Assert.assertNull(PushCommand.findSiblingSigstoreBundle(balaPath, printStream));
            Assert.assertTrue(readOutput(true).contains(
                    "WARNING: Sigstore bundle foo-winery-any-0.1.0.bala.sigstore.json is not a regular file"));
            Files.delete(bundlePath);
        }

        Files.writeString(bundlePath, "{}");
        Assert.assertEquals(PushCommand.findSiblingSigstoreBundle(balaPath, printStream), bundlePath);
    }

    @Test(description = "Push a bala to Central with the Sigstore bundle next to it")
    public void testPushToCentralWithSigstoreBundle() throws IOException, CentralClientException {
        String output = pushWithSiblingSigstoreBundle(true);
        Assert.assertTrue(output.contains("Attaching Sigstore bundle foo-winery-any-0.1.0.bala.sigstore.json"));
    }

    @Test(description = "Push a bala to Central without the bundle next to it when --no-sigstore-bundle is given")
    public void testPushToCentralWithNoSigstoreBundleFlag() throws IOException, CentralClientException {
        String output = pushWithSiblingSigstoreBundle(false, "--no-sigstore-bundle");
        Assert.assertFalse(output.contains("Attaching Sigstore bundle"));
    }

    private String pushWithSiblingSigstoreBundle(boolean expectBundle, String... args)
            throws IOException, CentralClientException {
        Path validBalProject = Path.of("build/validProjectWithSigstoreBundle");
        FileUtils.copyDirectory(
                this.testResources.resolve("validProjectWithTarget").toFile(), validBalProject.toFile());
        Path balaPath = validBalProject.resolve("target-dir/bala/foo-winery-any-0.1.0.bala");
        Path bundlePath = balaPath.resolveSibling("foo-winery-any-0.1.0.bala.sigstore.json");
        Files.writeString(bundlePath, "{}");

        PushCommand pushCommand = new PushCommand(validBalProject, printStream, printStream, false, balaPath);
        new CommandLine(pushCommand).parseArgs(args);
        try (MockedStatic<RepoUtils> repoUtils = Mockito.mockStatic(RepoUtils.class, Mockito.CALLS_REAL_METHODS);
             MockedConstruction<CentralAPIClient> clients = Mockito.mockConstruction(CentralAPIClient.class,
                     (mock, ctx) -> Mockito.when(mock.accessToken()).thenReturn("token"))) {
            repoUtils.when(RepoUtils::readSettings).thenReturn(Settings.from());
            repoUtils.when(RepoUtils::createAndGetHomeReposPath).thenReturn(Path.of("build/ballerina-home"));
            pushCommand.execute();

            List<CentralAPIClient> pushClients = clients.constructed().stream()
                    .filter(client -> Mockito.mockingDetails(client).getInvocations().stream()
                            .anyMatch(invocation -> invocation.getMethod().getName().equals("pushPackage")))
                    .toList();
            Assert.assertEquals(pushClients.size(), 1);
            Mockito.verify(pushClients.get(0)).pushPackage(Mockito.eq(balaPath), Mockito.eq("foo"),
                    Mockito.eq("winery"), Mockito.eq("0.1.0"), Mockito.any(), Mockito.any(),
                    Mockito.eq(expectBundle ? bundlePath : null));
        } finally {
            ProjectUtils.deleteDirectory(validBalProject);
        }
        return readOutput(true);
    }
}
