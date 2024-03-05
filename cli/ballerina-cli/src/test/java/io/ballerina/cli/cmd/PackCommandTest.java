package io.ballerina.cli.cmd;

import io.ballerina.cli.launcher.BLauncherException;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static io.ballerina.cli.cmd.CommandOutputUtils.assertTomlFilesEquals;
import static io.ballerina.cli.cmd.CommandOutputUtils.getOutput;
import static io.ballerina.cli.cmd.CommandOutputUtils.replaceDependenciesTomlContent;
import static io.ballerina.projects.util.ProjectConstants.DEPENDENCIES_TOML;
import static io.ballerina.projects.util.ProjectConstants.RESOURCE_DIR_NAME;
import static io.ballerina.projects.util.ProjectConstants.USER_DIR_PROPERTY;

/**
 * Pack command tests.
 *
 * @since 2.0.0
 */
public class PackCommandTest extends BaseCommandTest {

    private static final String VALID_PROJECT = "validApplicationProject";
    private Path testResources;

    static Path logFile = Paths.get("./src/test/resources/compiler_plugin_tests/" +
            "log_creator_combined_plugin/compiler-plugin.txt");

    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        try {
            this.testResources = super.tmpDir.resolve("build-test-resources");
            URI testResourcesURI = Objects.requireNonNull(getClass().getClassLoader().getResource("test-resources"))
                    .toURI();
            Path testResourcesPath = Paths.get(testResourcesURI);
            Files.walkFileTree(testResourcesPath,
                    new BuildCommandTest.Copy(testResourcesPath, this.testResources));

            // Copy the compiler plugin jars to the test resources directory
            Path compilerPluginJarsPath = Paths.get("build", "compiler-plugin-jars");
            Files.walkFileTree(compilerPluginJarsPath,
                    new BuildCommandTest.Copy(compilerPluginJarsPath,
                            this.testResources.resolve("compiler-plugin-jars")));
        } catch (URISyntaxException e) {
            Assert.fail("error loading resources");
        }
        Files.createDirectories(logFile.getParent());
        Files.writeString(logFile, "");
    }

    @Test(description = "Test package command")
    public void testPackCommand() throws IOException {
        Path projectPath = this.testResources.resolve(VALID_PROJECT);
        System.setProperty("user.dir", projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
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
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                getOutput("compile-bal-project.txt"));
        Assert.assertTrue(
                projectPath.resolve("target").resolve("bala").resolve("foo-winery-any-0.1.0.bala").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("java17")
                .resolve("foo-winery-0.1.0.jar").toFile().exists());
    }

    @Test(description = "Pack a ballerina project with the engagement of all type of compiler plugins")
    public void testRunBalProjectWithAllCompilerPlugins() throws IOException {
        Path compilerPluginPath = Paths.get("./src/test/resources/test-resources/compiler-plugins");
        BCompileUtil.compileAndCacheBala(compilerPluginPath.resolve("log_creator_pkg_provided_code_analyzer_im")
                        .toAbsolutePath().toString());
        BCompileUtil.compileAndCacheBala(compilerPluginPath.resolve("log_creator_pkg_provided_code_generator_im")
                .toAbsolutePath().toString());
        BCompileUtil.compileAndCacheBala(compilerPluginPath.resolve("log_creator_pkg_provided_code_modifier_im")
                .toAbsolutePath().toString());

        Path projectPath = this.testResources.resolve("compiler-plugins").resolve("log_creator_combined_plugin");
        System.setProperty("user.dir", projectPath.toString());
        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
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

    @Test(description = "Pack an application package")
    public void testPackApplicationPackage() {
        Path projectPath = this.testResources.resolve("validApplicationProject");
        System.setProperty("user.dir", projectPath.toString());
        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        try {
            packCommand.execute();
        } catch (BLauncherException e) {
            Assert.assertTrue(e.getDetailedMessages().get(0)
                    .contains("'package' information not found in Ballerina.toml"));
        }
    }

    @Test(description = "Pack a Standalone Ballerina file")
    public void testPackStandaloneFile() throws IOException {
        Path projectPath = this.testResources.resolve("valid-bal-file").resolve("hello_world.bal");
        System.setProperty("user.dir", this.testResources.resolve("valid-bal-file").toString());
        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertTrue(buildLog.contains(" bal pack can only be used with a Ballerina package."));
    }

    @Test(description = "Pack a package with platform libs")
    public void testPackageWithPlatformLibs() throws IOException {
        Path projectPath = this.testResources.resolve("validGraalvmCompatibleProjectWithPlatformLibs");
        System.setProperty("user.dir", projectPath.toString());
        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                getOutput("build-project-with-platform-libs.txt"));
        Assert.assertTrue(projectPath.resolve("target").resolve("bala").resolve("sameera-myproject-java17-0.1.0.bala")
                .toFile().exists());
    }

    @Test(description = "Pack a package with java11 platform libs")
    public void testPackageWithJava11PlatformLibs() throws IOException {
        Path projectPath = this.testResources.resolve("projectWithJava11PlatformLibs");
        System.setProperty("user.dir", projectPath.toString());
        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                getOutput("build-project-with-platform-libs.txt"));
        Path balaDirPath = projectPath.resolve("target").resolve("bala");
        Assert.assertTrue(balaDirPath.resolve("sameera-myproject-java17-0.1.0.bala")
                .toFile().exists());

        Path balaDestPath = balaDirPath.resolve("extracted");
        ProjectUtils.extractBala(balaDirPath.resolve("sameera-myproject-java17-0.1.0.bala"), balaDestPath);
        Assert.assertTrue(balaDestPath.resolve("platform").resolve("java17").resolve("one-1.0.0.jar")
                .toFile().exists());
    }

    @Test(description = "Pack a package with java11 and java17 platform libs")
    public void testPackageWithJava11andJava17PlatformLibs() throws IOException {
        Path projectPath = this.testResources.resolve("projectWithJava11and17PlatformLibs");
        System.setProperty("user.dir", projectPath.toString());
        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                getOutput("build-project-with-platform-libs.txt"));
        Path balaDirPath = projectPath.resolve("target").resolve("bala");
        Assert.assertTrue(balaDirPath.resolve("sameera-myproject-java17-0.1.0.bala")
                .toFile().exists());

        Path balaDestPath = balaDirPath.resolve("extracted");
        ProjectUtils.extractBala(balaDirPath.resolve("sameera-myproject-java17-0.1.0.bala"), balaDestPath);
        Assert.assertTrue(balaDestPath.resolve("platform").resolve("java17").resolve("one-1.0.0.jar")
                .toFile().exists());
        Assert.assertTrue(balaDestPath.resolve("platform").resolve("java17").resolve("two-2.0.0.jar")
                .toFile().exists());
    }

    @Test(description = "Pack a package with testOnly platform libs")
    public void testPackageWithTestOnlyPlatformLibs() throws IOException {
        Path projectPath = this.testResources.resolve("projectWithTestOnlyPlatformLibs");
        System.setProperty("user.dir", projectPath.toString());
        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                getOutput("pack-project-with-test-only-platform-libs.txt"));
        Assert.assertTrue(projectPath.resolve("target").resolve("bala").resolve("sameera-myproject-any-0.1.0.bala")
                .toFile().exists());
    }

    @Test(description = "Pack a package with ballerina/java imports only in tests")
    public void testPackageWithTestOnlyJavaImports() throws IOException {
        Path projectPath = this.testResources.resolve("projectWithTestOnlyJavaImports");
        System.setProperty("user.dir", projectPath.toString());
        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                getOutput("pack-project-with-test-only-platform-libs.txt"));
        Assert.assertTrue(projectPath.resolve("target").resolve("bala").resolve("sameera-myproject-any-0.1.0.bala")
                .toFile().exists());
    }

    @Test(description = "Pack a project with a build tool execution")
    public void testPackProjectWithBuildTool() throws IOException {
        Path projectPath = this.testResources.resolve("proper-build-tool");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                getOutput("pack-project-with-build-tool.txt"));
        Assert.assertTrue(projectPath.resolve("target").resolve("bala").resolve("foo-winery-any-0.1.0.bala")
                .toFile().exists());
    }

    @Test(description = "Pack a package with an empty Dependencies.toml")
    public void testPackageWithEmptyDependenciesToml() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithDependenciesToml");
        System.setProperty("user.dir", projectPath.toString());
        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                getOutput("build-project-with-dependencies-toml.txt"));
        Assert.assertTrue(projectPath.resolve("target").resolve("bala").resolve("foo-winery-any-0.1.0.bala")
                .toFile().exists());
        // `Dependencies.toml` file should not get deleted
        Assert.assertTrue(projectPath.resolve(DEPENDENCIES_TOML).toFile().exists());
        // `dependencies-toml-version` should exists in `Dependencies.toml`
        assertTomlFilesEquals(projectPath.resolve(DEPENDENCIES_TOML),
                projectPath.resolve("resources").resolve("expectedDependencies.toml"));
    }

    @Test(description = "Pack a package without root package in Dependencies.toml")
    public void testPackageWithoutRootPackageInDependenciesToml() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWoRootPkgInDepsToml");
        System.setProperty("user.dir", projectPath.toString());
        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                getOutput("build-project-wo-root-pkg-in-deps-toml.txt"));
        Assert.assertTrue(projectPath.resolve("target").resolve("bala").resolve("foo-winery-java17-0.1.0.bala")
                .toFile().exists());

        assertTomlFilesEquals(projectPath.resolve(DEPENDENCIES_TOML),
                projectPath.resolve(RESOURCE_DIR_NAME).resolve("expectedDependencies.toml"));
    }

    @Test(description = "Pack an empty package with compiler plugin")
    public void testPackEmptyProjectWithCompilerPlugin() throws IOException {
        Path projectPath = this.testResources.resolve("emptyProjectWithCompilerPlugin");
        System.setProperty("user.dir", projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertTrue(projectPath.resolve("target").resolve("bala")
                .resolve("wso2-emptyProjWithCompilerPlugin-java17-0.1.0.bala").toFile().exists());
        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                getOutput("compile-empty-project-with-compiler-plugin.txt"));
    }

    @Test(description = "Pack an empty package as a tool")
    public void testPackEmptyProjectWithTool() throws IOException {
        Path projectPath = this.testResources.resolve("emptyProjectWithTool");
        System.setProperty("user.dir", projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertTrue(projectPath.resolve("target").resolve("bala")
                .resolve("wso2-emptyProjWithTool-java17-0.1.0.bala").toFile().exists());
        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                getOutput("compile-empty-project-with-tool.txt"));
    }

    @Test(description = "Pack an empty package with tests only")
    public void testPackEmptyProjectWithTestsOnly() {
        Path projectPath = this.testResources.resolve("emptyProjectWithTestsOnly");
        System.setProperty("user.dir", projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();

        Assert.assertTrue(projectPath.resolve("target").resolve("bala")
                .resolve("wso2-emptyProjWithTestsOnly-any-0.1.0.bala").toFile().exists());
    }

    @Test(description = "Pack an empty package with Non Default modules")
    public void testPackEmptyProjectWithNonDefaultModules() {
        Path projectPath = this.testResources.resolve("emptyProjectWithNonDefaultModules");
        System.setProperty("user.dir", projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();

        Assert.assertTrue(projectPath.resolve("target").resolve("bala")
                .resolve("wso2-emptyProjWithNonDefaultModules-any-0.1.0.bala").toFile().exists());
    }

    @Test(description = "Pack an empty package with Non Default modules with Tests only")
    public void testPackEmptyProjectWithNonDefaultModulesTestOnly() {
        Path projectPath = this.testResources.resolve("emptyProjectWithNonDefaultModulesTestOnly");
        System.setProperty("user.dir", projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();

        Assert.assertTrue(projectPath.resolve("target").resolve("bala")
                .resolve("wso2-emptyProjWithNonDefaultModulesTestOnly-any-0.1.0.bala").toFile().exists());
    }

    @Test(description = "Pack an empty package with empty Non Default")
    public void testPackEmptyNonDefaultModule() throws IOException {
        Path projectPath = this.testResources.resolve("emptyNonDefaultModule");
        System.setProperty("user.dir", projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("build-empty-nondefault-module.txt"));
        Assert.assertFalse(projectPath.resolve("target").resolve("bala")
                .resolve("wso2-emptyNonDefaultModule-any-0.1.0.bala").toFile().exists());
    }

    @Test(description = "Tests packing with a custom dir")
    public void testCustomTargetDir() throws IOException {
        Path projectPath = this.testResources.resolve(VALID_PROJECT);
        Path customTargetDir = projectPath.resolve("customTargetDir");
        System.setProperty("user.dir", projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true,
                customTargetDir);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                getOutput("pack-bal-project-custom-dir.txt"));
        Assert.assertFalse(Files.exists(customTargetDir.resolve("bin")));
        Assert.assertTrue(Files.exists(customTargetDir.resolve("cache")));
        Assert.assertTrue(Files.exists(customTargetDir.resolve("build")));
        Assert.assertTrue(Files.exists(customTargetDir.resolve("bala")));
        Assert.assertTrue(customTargetDir.resolve("bala").resolve("foo-winery-any-0.1.0.bala").toFile().exists());
    }

    @Test(description = "Tests packing with a custom dir")
    public void testCustomTargetDirWithRelativePath() throws IOException {
        Path projectPath = this.testResources.resolve(VALID_PROJECT);
        Path customTargetDir = projectPath.resolve("./customTargetDir");
        System.setProperty("user.dir", projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true,
                customTargetDir);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                getOutput("pack-bal-project-custom-dir.txt"));
        Assert.assertFalse(Files.exists(customTargetDir.resolve("bin")));
        Assert.assertTrue(Files.exists(customTargetDir.resolve("cache")));
        Assert.assertTrue(Files.exists(customTargetDir.resolve("build")));
        Assert.assertTrue(Files.exists(customTargetDir.resolve("bala")));
        Assert.assertTrue(customTargetDir.resolve("bala").resolve("foo-winery-any-0.1.0.bala").toFile().exists());
    }

    @Test(description = "Pack an empty package")
    public void testPackEmptyPackage() throws IOException {
        Path projectPath = this.testResources.resolve("emptyPackage");
        System.setProperty("user.dir", projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();

        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                getOutput("pack-empty-package.txt"));
    }

    @Test(description = "Pack an empty package with compiler plugin")
    public void testPackEmptyPackageWithCompilerPlugin() throws IOException {
        Path projectPath = this.testResources.resolve("emptyPackageWithCompilerPlugin");
        replaceDependenciesTomlContent(
                projectPath, "**INSERT_DISTRIBUTION_VERSION_HERE**", RepoUtils.getBallerinaShortVersion());
        System.setProperty("user.dir", projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();

        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                getOutput("pack-empty-package-with-compiler-plugin.txt"));
    }

    @BeforeGroups("packWithCompilerPlugin")
    private void buildAndCacheCompilerPluginPackage() {
        Path compilerPluginPath = this.testResources.resolve("compiler-plugins")
                .resolve("package_comp_plugin_code_modify_add_function");
        BCompileUtil.compileAndCacheBala(compilerPluginPath.toString());
    }

    @Test(description = "Pack a non template package with a compiler plugin dependency",
            groups = {"packWithCompilerPlugin"})
    public void testPackNonTemplatePackageWithACompilerPackageDependency() throws IOException {

        // BALA should contain the source documents modified by the compiler plugin
        Path projectPath = this.testResources.resolve("projects-using-compiler-plugins")
                .resolve("package_plugin_code_modify_user_not_template");
        System.setProperty("user.dir", projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();

        Path balaDirPath = projectPath.resolve("target").resolve("bala");
        Path balaFilePath = balaDirPath.resolve("samjs-package_plugin_code_modify_user_not_template-any-0.1.0.bala");
        Assert.assertTrue(balaFilePath.toFile().exists());

        Path balaDestPath = balaDirPath.resolve("extracted");
        ProjectUtils.extractBala(balaFilePath, balaDestPath);
        String mainBalContent = Files.readString(balaDestPath.resolve("modules")
                .resolve("package_plugin_code_modify_user_not_template").resolve("main.bal"));
        Assert.assertTrue(mainBalContent.contains("public function newFunctionByCodeModifiermain(string params) " +
                "returns error? {\n}"));
    }

    @Test(description = "Pack a template package with a compiler plugin dependency",
            groups = {"packWithCompilerPlugin"})
    public void testPackTemplatePackageWithACompilerPackageDependency() throws IOException {

        // BALA should contain the original source documents and not documents modified by the compiler plugin
        Path projectPath = this.testResources.resolve("projects-using-compiler-plugins")
                .resolve("package_plugin_code_modify_user_template");
        System.setProperty("user.dir", projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();

        Path balaDirPath = projectPath.resolve("target").resolve("bala");
        Path balaFilePath = balaDirPath.resolve("samjs-package_plugin_code_modify_user_template-any-0.1.0.bala");
        Assert.assertTrue(balaFilePath.toFile().exists());

        Path balaDestPath = balaDirPath.resolve("extracted");
        ProjectUtils.extractBala(balaFilePath, balaDestPath);
        String mainBalContent = Files.readString(balaDestPath.resolve("modules")
                .resolve("package_plugin_code_modify_user_template").resolve("main.bal"));
        Assert.assertFalse(mainBalContent.contains("public function newFunctionByCodeModifiermain(string params) " +
                "returns error? {\n}"));
    }

    @Test(description = "Pack a library package with platform libraries")
    public void testPackProjectWithPlatformLibs() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithPlatformLibs1");
        System.setProperty("user.dir", projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput("pack-project-with-platform-libs.txt"));
    }

    @AfterClass
    public void cleanUp() throws IOException {
        Files.deleteIfExists(logFile);
        Files.deleteIfExists(logFile.getParent());
        Files.deleteIfExists(logFile.getParent().getParent());
    }
}
