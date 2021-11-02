package io.ballerina.cli.cmd;

import io.ballerina.cli.launcher.BLauncherException;
import org.testng.Assert;
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

import static io.ballerina.cli.cmd.CommandOutputUtils.getOutput;
import static io.ballerina.cli.cmd.CommandOutputUtils.readFileAsString;
import static io.ballerina.projects.util.ProjectConstants.DEPENDENCIES_TOML;
import static io.ballerina.projects.util.ProjectConstants.RESOURCE_DIR_NAME;

/**
 * Pack command tests.
 *
 * @since 2.0.0
 */
public class PackCommandTest extends BaseCommandTest {

    private static final String VALID_PROJECT = "validApplicationProject";
    private Path testResources;

    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        try {
            this.testResources = super.tmpDir.resolve("build-test-resources");
            URI testResourcesURI = Objects.requireNonNull(getClass().getClassLoader().getResource("test-resources"))
                    .toURI();
            Files.walkFileTree(Paths.get(testResourcesURI),
                    new BuildCommandTest.Copy(Paths.get(testResourcesURI), this.testResources));
        } catch (URISyntaxException e) {
            Assert.fail("error loading resources");
        }
    }

    @Test(description = "Test package command")
    public void testPackCommand() throws IOException {
        Path projectPath = this.testResources.resolve(VALID_PROJECT);
        System.setProperty("user.dir", projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parse();
        packCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("compile-bal-project.txt"));
        Assert.assertTrue(
                projectPath.resolve("target").resolve("bala").resolve("foo-winery-any-0.1.0.bala").toFile().exists());
    }

    @Test(description = "Pack a library package")
    public void testPackProject() throws IOException {
        Path projectPath = this.testResources.resolve("validLibraryProject");
        System.setProperty("user.dir", projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parse();
        packCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                getOutput("compile-bal-project.txt"));
        Assert.assertTrue(
                projectPath.resolve("target").resolve("bala").resolve("foo-winery-any-0.1.0.bala").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("java11")
                .resolve("foo-winery-0.1.0.jar").toFile().exists());
    }

    @Test(description = "Pack an application package")
    public void testPackApplicationPackage() {
        Path projectPath = this.testResources.resolve("validApplicationProject");
        System.setProperty("user.dir", projectPath.toString());
        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parse();
        try {
            packCommand.execute();
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getDetailedMessages().get(0)
                    .contains("'package' information not found in Ballerina.toml"));
        }
    }

    @Test(description = "Pack a Standalone Ballerina file")
    public void testPackStandaloneFile() {
        Path projectPath = this.testResources.resolve("valid-bal-file").resolve("hello_world.bal");
        System.setProperty("user.dir", this.testResources.resolve("valid-bal-file").toString());
        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parse();
        try {
            packCommand.execute();
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getDetailedMessages().get(0)
                    .contains("'-c' or '--compile' can only be used with a Ballerina package."));
        }
    }

    @Test(description = "Pack a package with platform libs")
    public void testPackageWithPlatformLibs() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithPlatformLibs");
        System.setProperty("user.dir", projectPath.toString());
        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parse();
        packCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                getOutput("build-project-with-platform-libs.txt"));
        Assert.assertTrue(projectPath.resolve("target").resolve("bala").resolve("sameera-myproject-java11-0.1.0.bala")
                .toFile().exists());
    }

    @Test(description = "Pack a package with an empty Dependencies.toml")
    public void testPackageWithEmptyDependenciesToml() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithDependenciesToml");
        System.setProperty("user.dir", projectPath.toString());
        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parse();
        packCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                getOutput("build-project-with-dependencies-toml.txt"));
        Assert.assertTrue(projectPath.resolve("target").resolve("bala").resolve("foo-winery-any-0.1.0.bala")
                .toFile().exists());
        // `Dependencies.toml` file should not get deleted
        Assert.assertTrue(projectPath.resolve(DEPENDENCIES_TOML).toFile().exists());
        // `dependencies-toml-version` should exists in `Dependencies.toml`
        String expected = "[ballerina]\n"
                + "dependencies-toml-version = \"2\"";
        String actual = Files.readString(projectPath.resolve(DEPENDENCIES_TOML));
        Assert.assertTrue(actual.contains(expected));
    }

    @Test(description = "Pack a package without root package in Dependencies.toml")
    public void testPackageWithoutRootPackageInDependenciesToml() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWoRootPkgInDepsToml");
        System.setProperty("user.dir", projectPath.toString());
        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parse();
        packCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                getOutput("build-project-wo-root-pkg-in-deps-toml.txt"));
        Assert.assertTrue(projectPath.resolve("target").resolve("bala").resolve("foo-winery-java11-0.1.0.bala")
                .toFile().exists());

        Assert.assertEquals(readFileAsString(projectPath.resolve(DEPENDENCIES_TOML)).trim(), readFileAsString(
                projectPath.resolve(RESOURCE_DIR_NAME).resolve("expectedDependencies.toml")).trim());
    }

    @Test(description = "Pack an empty package with compiler plugin")
    public void testPackEmptyProjectWithCompilerPlugin() throws IOException {
        Path projectPath = this.testResources.resolve("emptyProjectWithCompilerPlugin");
        System.setProperty("user.dir", projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parse();
        packCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertTrue(projectPath.resolve("target").resolve("bala")
                .resolve("wso2-emptyProjWithCompilerPlugin-any-0.1.0.bala").toFile().exists());
        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                getOutput("compile-empty-project-with-compiler-plugin.txt"));
    }

    @Test(description = "Pack an empty package with tests only")
    public void testPackEmptyProjectWithTestsOnly() {
        Path projectPath = this.testResources.resolve("emptyProjectWithTestsOnly");
        System.setProperty("user.dir", projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parse();
        packCommand.execute();

        Assert.assertTrue(projectPath.resolve("target").resolve("bala")
                .resolve("wso2-emptyProjWithTestsOnly-any-0.1.0.bala").toFile().exists());
    }

    @Test(description = "Pack an empty package with Non Default modules")
    public void testPackEmptyProjectWithNonDefaultModules() {
        Path projectPath = this.testResources.resolve("emptyProjectWithNonDefaultModules");
        System.setProperty("user.dir", projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parse();
        packCommand.execute();

        Assert.assertTrue(projectPath.resolve("target").resolve("bala")
                .resolve("wso2-emptyProjWithNonDefaultModules-any-0.1.0.bala").toFile().exists());
    }

    @Test(description = "Pack an empty package with Non Default modules with Tests only")
    public void testPackEmptyProjectWithNonDefaultModulesTestOnly() {
        Path projectPath = this.testResources.resolve("emptyProjectWithNonDefaultModulesTestOnly");
        System.setProperty("user.dir", projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parse();
        packCommand.execute();

        Assert.assertTrue(projectPath.resolve("target").resolve("bala")
                .resolve("wso2-emptyProjWithNonDefaultModulesTestOnly-any-0.1.0.bala").toFile().exists());
    }

    @Test(description = "Pack an empty package with empty Non Default")
    public void testPackEmptyNonDefaultModule() {
        Path projectPath = this.testResources.resolve("emptyNonDefaultModule");
        System.setProperty("user.dir", projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parse();
        packCommand.execute();

        Assert.assertTrue(projectPath.resolve("target").resolve("bala")
                .resolve("wso2-emptyNonDefaultModule-any-0.1.0.bala").toFile().exists());
    }

}
