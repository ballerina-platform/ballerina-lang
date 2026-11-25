/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.cli.cmd;

import io.ballerina.cli.launcher.BLauncherException;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static io.ballerina.cli.cmd.CommandOutputUtils.getOutput;
import static io.ballerina.projects.util.ProjectConstants.DIST_CACHE_DIRECTORY;
import static io.ballerina.projects.util.ProjectConstants.USER_DIR_PROPERTY;
import static io.ballerina.runtime.api.constants.RuntimeConstants.USER_HOME;

/**
 * Clean command tests.
 *
 * @since 2.0.0
 */
public class CleanCommandTest extends BaseCommandTest {
    private Path testResources;

    @Override
    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        try {
            this.testResources = super.tmpDir.resolve("build-test-resources");
            URI testResourcesURI = Objects.requireNonNull(
                    getClass().getClassLoader().getResource("test-resources")).toURI();
            Files.walkFileTree(Path.of(testResourcesURI), new BuildCommandTest.Copy(Path.of(testResourcesURI),
                    this.testResources));
        } catch (URISyntaxException e) {
            Assert.fail("error loading resources");
        }
    }

    @Test(description = "Test clean command on a ballerina project.")
    public void testCleanCommandInProject() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithTargetAndGenerated");
        FileUtils.copyDirectory(projectPath.resolve("target-dir").toFile(),
                projectPath.resolve("target").toFile());
        FileUtils.copyDirectory(projectPath.resolve("generated-dir").toFile(),
                projectPath.resolve("generated").toFile());

        Assert.assertTrue(Objects.requireNonNull(
                projectPath.resolve("target").resolve("bala").toFile().listFiles()).length > 0);
        Assert.assertTrue(Objects.requireNonNull(
                projectPath.resolve("target").resolve("cache").toFile().listFiles()).length > 0);
        Assert.assertTrue(Objects.requireNonNull(
                projectPath.resolve("target").resolve("report").toFile().listFiles()).length > 0);
        Assert.assertTrue(Objects.requireNonNull(
                projectPath.resolve("generated").toFile().listFiles()).length > 0);

        CleanCommand cleanCommand = new CleanCommand(projectPath, false);
        cleanCommand.execute();

        Assert.assertFalse(Files.exists(projectPath.resolve("target")));
        Assert.assertFalse(Files.exists(projectPath.resolve("generated")));
    }

    @Test(description = "Test clean command on a ballerina project with custom target dir.")
    public void testCleanCommandInProjectWithCustomTarget() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithTargetAndGenerated");
        Path customTargetDir = projectPath.resolve("customTargetDir4");
        FileUtils.copyDirectory(projectPath.resolve("target-dir").toFile(), customTargetDir.toFile());

        Assert.assertTrue(Objects.requireNonNull(
                customTargetDir.resolve("bala").toFile().listFiles()).length > 0);
        Assert.assertTrue(Objects.requireNonNull(
                customTargetDir.resolve("cache").toFile().listFiles()).length > 0);
        Assert.assertTrue(Objects.requireNonNull(
                customTargetDir.resolve("report").toFile().listFiles()).length > 0);

        CleanCommand cleanCommand = new CleanCommand(projectPath, printStream, false, customTargetDir);
        cleanCommand.execute();

        Assert.assertFalse(Files.exists(customTargetDir));
    }

    @Test(description = "Test clean command on a ballerina project with custom target dir.")
    public void testCleanCommandNonExistentTargetAndGenerated() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithTargetAndGenerated");
        Path customTargetDir = Path.of("customTargetDirNotExists");
        Assert.assertTrue(Files.notExists(customTargetDir));

        CleanCommand cleanCommand = new CleanCommand(projectPath, printStream, false, customTargetDir);
        cleanCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""),
                getOutput("clean-non-existent-target.txt")
                .replace("%TARGET_LOCATION%", customTargetDir.toString()));
    }

    @Test(description = "Test clean command on a regular file as the custom target dir.")
    public void testCleanCommandOnRegularFile() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithTargetAndGenerated");
        Path customTargetDir = projectPath.resolve("main.bal");
        Assert.assertTrue(Files.exists(customTargetDir));

        CleanCommand cleanCommand = new CleanCommand(projectPath, printStream, false, customTargetDir);
        cleanCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""),
                getOutput("clean-regular-file.txt")
                .replace("%TARGET_LOCATION%", customTargetDir.toString()));
    }

    @Test
    public void testCleanInValidTargetDir() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithTargetAndGenerated");
        Assert.assertTrue(Files.exists(projectPath));
        Assert.assertTrue(Files.exists(projectPath.resolve("Ballerina.toml")));
        Assert.assertTrue(Files.exists(projectPath.resolve("main.bal")));
        Assert.assertTrue(Files.exists(projectPath.resolve("target-dir/bala")));
        Assert.assertTrue(Files.exists(projectPath.resolve("target-dir/cache")));
        Assert.assertTrue(Files.exists(projectPath.resolve("target-dir/report")));
        Assert.assertTrue(Files.exists(projectPath.resolve("generated-dir/gen.bal")));
        Assert.assertTrue(Files.notExists(projectPath.resolve("tests/main_test.bal")));
        CleanCommand cleanCommand = new CleanCommand(projectPath, printStream, false, projectPath);
        cleanCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertTrue(buildLog.replace("\r", "")
                .contains("provided target directory '" + projectPath + "' is not a valid target directory."));
        Assert.assertTrue(Files.exists(projectPath));
        Assert.assertTrue(Files.exists(projectPath.resolve("Ballerina.toml")));
        Assert.assertTrue(Files.exists(projectPath.resolve("main.bal")));
        Assert.assertTrue(Files.exists(projectPath.resolve("target-dir/bala")));
        Assert.assertTrue(Files.exists(projectPath.resolve("target-dir/cache")));
        Assert.assertTrue(Files.exists(projectPath.resolve("target-dir/report")));
        Assert.assertTrue(Files.exists(projectPath.resolve("generated-dir/gen.bal")));
        Assert.assertTrue(Files.notExists(projectPath.resolve("tests/main_test.bal")));
    }

    @Test(description = "Clean the downloaded dependencies and the compiled variants the project uses")
    public void testDependencyCacheDeleteWithProjectCache() throws IOException {
        Path root = this.tmpDir.resolve("clean-project-dependency-cache");
        Path dependencyPath = root.resolve("cleandep");
        Files.createDirectories(dependencyPath);
        Files.writeString(dependencyPath.resolve("Ballerina.toml"),
                "[package]\norg = \"testorg\"\nname = \"cleandep\"\nversion = \"1.0.0\"\n");
        Files.writeString(dependencyPath.resolve("lib.bal"), "public function hello() returns string => \"hello\";\n");
        Path projectPath = root.resolve("cleanapp");
        Files.createDirectories(projectPath);
        Files.writeString(projectPath.resolve("Ballerina.toml"),
                "[package]\norg = \"testorg\"\nname = \"cleanapp\"\nversion = \"0.1.0\"\n");
        Files.writeString(projectPath.resolve("main.bal"),
                "import testorg/cleandep;\n\npublic function main() {\n    string _ = cleandep:hello();\n}\n");
        BCompileUtil.compileAndCacheBala(dependencyPath.toString(), testCentralRepoCache);
        Path dependencyBala = testCentralRepoCache.resolve("bala").resolve("testorg").resolve("cleandep")
                .resolve("1.0.0").resolve("any");

        Path dependencyVersionCache = testCentralRepoCache.resolve("cache-" + RepoUtils.getBallerinaShortVersion())
                .resolve("testorg").resolve("cleandep").resolve("1.0.0");
        // A variant compiled against other dependency versions, e.g. for another project.
        Path otherVariant = dependencyVersionCache.resolve("0000000000000000");
        Files.createDirectories(otherVariant.resolve("bir"));

        String userDir = System.getProperty(USER_DIR_PROPERTY);
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        Path variant = null;
        try {
            new BuildCommand(projectPath, printStream, printStream, false).execute();
            try (Stream<Path> variants = Files.list(dependencyVersionCache)) {
                variant = variants.filter(path -> !path.equals(otherVariant)).findFirst().orElseThrow();
            }
            Assert.assertTrue(Files.exists(variant.resolve("bir").resolve("cleandep.bir")),
                    "The dependency should be cached in the home cache");
            Assert.assertFalse(Files.exists(projectPath.resolve("target").resolve("cache").resolve("testorg")
                    .resolve("cleandep")), "The dependency should not be cached in the project target");

            new CleanCommand(projectPath, printStream, true, false).execute();
        } catch (BLauncherException e) {
            Assert.fail(readOutput() + "\n" + e.getDetailedMessages().get(0));
        } finally {
            System.setProperty(USER_DIR_PROPERTY, userDir);
        }

        String cleanLog = readOutput().replace("\r", "");
        Assert.assertFalse(Files.exists(dependencyBala), "The dependency bala should be deleted");
        Assert.assertTrue(cleanLog.contains("Successfully deleted " + dependencyBala), cleanLog);
        Assert.assertFalse(Files.exists(projectPath.resolve("target")), "The project target should be deleted");
        Assert.assertFalse(Files.exists(variant), "The variant the project uses should be deleted");
        Assert.assertTrue(cleanLog.contains("Successfully deleted " + variant), cleanLog);
        Assert.assertTrue(Files.exists(otherVariant), "Variants other projects may use should be kept");
        FileUtils.deleteDirectory(dependencyVersionCache.toFile());
    }

    @Test(description = "A dependency from the local repository keeps its bala, which cannot be pulled again, and "
            + "only its compiled variant is deleted")
    public void testDependencyCacheKeepsLocalRepositoryBala() throws IOException {
        Path root = this.tmpDir.resolve("clean-local-dependency-cache");
        Path dependencyPath = writePackage(root.resolve("cleanlocaldep"), "cleanlocaldep", "1.0.0", "",
                "public function hello() returns string => \"hello\";\n");
        Path projectPath = writePackage(root.resolve("cleanlocalapp"), "cleanlocalapp", "0.1.0", """

                [[dependency]]
                org = "testorg"
                name = "cleanlocaldep"
                version = "1.0.0"
                repository = "local"
                """, "import testorg/cleanlocaldep;\n\npublic function main() {\n"
                + "    string _ = cleanlocaldep:hello();\n}\n");
        Path localRepository = testDotBallerina.resolve("repositories").resolve("local");
        BCompileUtil.compileAndCacheBala(dependencyPath.toString(), localRepository);
        Path dependencyBala = localRepository.resolve("bala").resolve("testorg").resolve("cleanlocaldep")
                .resolve("1.0.0").resolve("any");
        Path dependencyVersionCache = localRepository.resolve("cache-" + RepoUtils.getBallerinaShortVersion())
                .resolve("testorg").resolve("cleanlocaldep").resolve("1.0.0");
        try {
            Path variant = buildAndFindVariant(projectPath, dependencyVersionCache);

            cleanDependencyCache(projectPath);

            String cleanLog = readOutput().replace("\r", "");
            Assert.assertTrue(Files.exists(dependencyBala), "The local repository bala should be kept");
            Assert.assertTrue(cleanLog.contains("Kept the local repository package " + dependencyBala), cleanLog);
            Assert.assertFalse(Files.exists(variant), "The variant the project uses should be deleted");
            Assert.assertTrue(cleanLog.contains("Successfully deleted " + variant), cleanLog);
        } finally {
            FileUtils.deleteDirectory(localRepository.resolve("bala").resolve("testorg").resolve("cleanlocaldep")
                    .toFile());
            FileUtils.deleteDirectory(dependencyVersionCache.toFile());
        }
    }

    @Test(description = "A dependency from the distribution keeps its bala and the cache the distribution is shipped "
            + "with, and its variant compiled into the user home is deleted")
    public void testDependencyCacheDeletesDistributionVariantInUserHome() throws IOException {
        Path root = this.tmpDir.resolve("clean-distribution-dependency-cache");
        Path dependencyPath = writePackage(root.resolve("cleandistdep"), "cleandistdep", "1.0.0", "",
                "public function hello() returns string => \"hello\";\n");
        Path projectPath = writePackage(root.resolve("cleandistapp"), "cleandistapp", "0.1.0", "",
                "import testorg/cleandistdep;\n\npublic function main() {\n    string _ = cleandistdep:hello();\n}\n");
        Path distributionRepository = Path.of(System.getProperty("ballerina.home")).resolve(DIST_CACHE_DIRECTORY);
        BCompileUtil.compileAndCacheBala(dependencyPath.toString());
        Path dependencyBala = distributionRepository.resolve("bala").resolve("testorg").resolve("cleandistdep")
                .resolve("1.0.0").resolve("any");
        // The distribution's cache of the package is compiled against other versions, so the build compiles the
        // package into the user home. The cache is not deleted, as its JAR is still loaded on Windows.
        Path distributionCache = distributionRepository.resolve("cache").resolve("testorg").resolve("cleandistdep")
                .resolve("1.0.0");
        Files.createDirectories(distributionCache);
        Files.writeString(distributionCache.resolve(".fingerprint"), "0000000000000000");
        Path homeVersionCache = testDotBallerina.resolve("repositories").resolve("distribution")
                .resolve("cache-" + RepoUtils.getBallerinaShortVersion()).resolve("testorg").resolve("cleandistdep")
                .resolve("1.0.0");
        try {
            Path variant = buildAndFindVariant(projectPath, homeVersionCache);

            cleanDependencyCache(projectPath);

            String cleanLog = readOutput().replace("\r", "");
            Assert.assertTrue(Files.exists(dependencyBala), "The distribution bala should be kept");
            Assert.assertTrue(Files.exists(distributionCache.resolve(".fingerprint")),
                    "The cache the distribution is shipped with should be kept");
            Assert.assertFalse(Files.exists(variant), "The variant in the user home should be deleted");
            Assert.assertTrue(cleanLog.contains("Successfully deleted " + variant), cleanLog);
        } finally {
            FileUtils.deleteDirectory(distributionRepository.resolve("bala").resolve("testorg")
                    .resolve("cleandistdep").toFile());
            // The JAR of the distribution cache is still loaded on Windows.
            FileUtils.deleteQuietly(distributionCache.toFile());
            FileUtils.deleteDirectory(homeVersionCache.toFile());
        }
    }

    private static Path writePackage(Path packagePath, String name, String version, String extraToml, String source)
            throws IOException {
        Files.createDirectories(packagePath);
        Files.writeString(packagePath.resolve("Ballerina.toml"),
                "[package]\norg = \"testorg\"\nname = \"" + name + "\"\nversion = \"" + version + "\"\n" + extraToml);
        Files.writeString(packagePath.resolve("main.bal"), source);
        return packagePath;
    }

    /** Builds the project and returns the only variant of a dependency in the given cache of its version. */
    private Path buildAndFindVariant(Path projectPath, Path dependencyVersionCache) throws IOException {
        String userDir = System.getProperty(USER_DIR_PROPERTY);
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        try {
            new BuildCommand(projectPath, printStream, printStream, false).execute();
        } catch (BLauncherException e) {
            Assert.fail(readOutput() + "\n" + e.getDetailedMessages().get(0));
        } finally {
            System.setProperty(USER_DIR_PROPERTY, userDir);
        }
        Assert.assertTrue(Files.isDirectory(dependencyVersionCache), readOutput());
        try (Stream<Path> variants = Files.list(dependencyVersionCache)) {
            List<Path> found = variants.filter(Files::isDirectory).toList();
            Assert.assertEquals(found.size(), 1, found.toString());
            return found.get(0);
        }
    }

    private void cleanDependencyCache(Path projectPath) throws IOException {
        String userDir = System.getProperty(USER_DIR_PROPERTY);
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        try {
            new CleanCommand(projectPath, printStream, true, false).execute();
        } catch (BLauncherException e) {
            Assert.fail(readOutput() + "\n" + e.getDetailedMessages().get(0));
        } finally {
            System.setProperty(USER_DIR_PROPERTY, userDir);
        }
    }

    @Test (enabled = false)
    public void testDependencyCacheDelete() throws IOException {
        Path projectsRoot = this.testResources.resolve("clean-cache-project");
        Path projectPath = projectsRoot.resolve("mainPackage");
        String userHomeDir = System.getProperty(USER_HOME);
        System.setProperty(USER_HOME, testDotBallerina.getParent().toString());

        BCompileUtil.compileAndCacheBala(projectsRoot.resolve("depFromDist").toString());
        BCompileUtil.compileAndCacheBala(projectsRoot.resolve("depFromCentral").toString(), testCentralRepoCache);

        String userDir = System.getProperty(USER_DIR_PROPERTY);
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        // Execute the build command to cache the dependencies
        BuildCommand buildCommand = new BuildCommand(projectPath, printStream, printStream, false);
        try {
            buildCommand.execute();
        } catch (BLauncherException e) {
            Assert.fail("build command failed: " + e.getDetailedMessages().get(0));
        } finally {
            System.setProperty(USER_DIR_PROPERTY, userDir); // reset user dir for other tests
        }

        CleanCommand cleanCommand = new CleanCommand(projectPath, printStream, true, false);
        try {
            cleanCommand.execute();
        } catch (BLauncherException e) {
            Assert.fail("clean command failed: " + e.getDetailedMessages().get(0));
        } finally {
            System.setProperty(USER_HOME, userHomeDir); // reset user home for other tests
            System.setProperty(USER_DIR_PROPERTY, userDir); // reset user dir for other tests
        }
        String buildLog = readOutput();

        Path testDistRepoCache = Paths.get(userHomeDir).resolve(DIST_CACHE_DIRECTORY);
        Path depFromDistBalaPath = testDistRepoCache.resolve("bala/testorg/depFromDist/2.0.0/any");
        Path depFromDistCachePath = testDistRepoCache.resolve(
                "cache-" + RepoUtils.getBallerinaShortVersion() + "/testorg/depFromDist/2.0.0/");
        Path depFromCentralBalaPath = testCentralRepoCache.resolve("bala/testorg/depFromCentral/2.0.0/any");
        Path depFromCentralCachePath = testCentralRepoCache.resolve(
                "cache-" + RepoUtils.getBallerinaShortVersion() + "/testorg/depFromCentral/2.0.0/");

        Assert.assertTrue(Files.exists(depFromDistBalaPath), depFromDistBalaPath.toString());
        Assert.assertFalse(buildLog.replace("\r", "")
                .contains("Successfully deleted " + depFromDistBalaPath), buildLog);
        Assert.assertTrue(Files.exists(depFromDistCachePath), depFromDistCachePath.toString());
        Assert.assertFalse(buildLog.replace("\r", "")
                .contains("Successfully deleted " + depFromDistCachePath), buildLog);

        Assert.assertFalse(Files.exists(depFromCentralBalaPath), depFromCentralBalaPath.toString());
        Assert.assertTrue(buildLog.replace("\r", "")
                .contains("Successfully deleted " + depFromCentralBalaPath), buildLog);
        Assert.assertFalse(Files.exists(depFromCentralCachePath), depFromCentralCachePath.toString());
        Assert.assertTrue(buildLog.replace("\r", "")
                .contains("Successfully deleted " + depFromCentralCachePath), buildLog);
    }

    @Test(description = "Remove the compiled dependencies that no build used for a number of days")
    public void testPruneDependencyCache() throws IOException {
        Path versionCache = testCentralRepoCache.resolve("cache-" + RepoUtils.getBallerinaShortVersion())
                .resolve("testorg").resolve("prunedep").resolve("1.0.0");
        Path recent = pruneTestVariant(versionCache.resolve("aaaaaaaaaaaaaaaa"), 2);
        Path old = pruneTestVariant(versionCache.resolve("bbbbbbbbbbbbbbbb"), 20);
        try {
            CleanCommand cleanCommand = new CleanCommand(this.tmpDir, printStream, false, false);
            new CommandLine(cleanCommand).parseArgs("--prune-dependency-cache", "--unused-days", "10");
            cleanCommand.execute();

            String log = readOutput();
            Assert.assertTrue(log.contains("Removed 1 compiled dependencies unused for 10 days"), log);
            Assert.assertFalse(Files.exists(old), "A variant unused for longer than the period should be removed");
            Assert.assertTrue(Files.exists(recent), "A recently used variant should be kept");

            cleanCommand = new CleanCommand(this.tmpDir, printStream, false, false);
            new CommandLine(cleanCommand).parseArgs("--prune-dependency-cache", "--unused-days", "0");
            cleanCommand.execute();
            Assert.assertFalse(Files.exists(recent), "A zero period should remove every variant");
        } finally {
            FileUtils.deleteDirectory(versionCache.toFile());
        }
    }

    @Test(description = "The number of days requires --prune-dependency-cache and must not be negative")
    public void testPruneDependencyCacheOptionValidation() throws IOException {
        CleanCommand cleanCommand = new CleanCommand(this.tmpDir, printStream, false, false);
        new CommandLine(cleanCommand).parseArgs("--unused-days", "10");
        cleanCommand.execute();
        String log = readOutput();
        Assert.assertTrue(log.contains("'--unused-days' can only be used with '--prune-dependency-cache'"), log);

        cleanCommand = new CleanCommand(this.tmpDir, printStream, false, false);
        new CommandLine(cleanCommand).parseArgs("--prune-dependency-cache", "--unused-days", "-1");
        cleanCommand.execute();
        log = readOutput();
        Assert.assertTrue(log.contains("'--unused-days' must not be negative"), log);
    }

    /** Creates a compiled dependency variant last used the given number of days ago. */
    private static Path pruneTestVariant(Path variant, int daysAgo) throws IOException {
        Files.createDirectories(variant.resolve("bir"));
        Files.writeString(variant.resolve("bir").resolve("prunedep.bir"), "bir");
        Path lastUsed = variant.resolve(".last-used");
        Files.writeString(lastUsed, "");
        Files.setLastModifiedTime(lastUsed, FileTime.from(Instant.now().minus(Duration.ofDays(daysAgo))));
        return variant;
    }
}
