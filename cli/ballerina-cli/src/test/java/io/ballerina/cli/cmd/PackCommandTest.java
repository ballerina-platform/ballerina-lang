package io.ballerina.cli.cmd;

import com.google.gson.Gson;
import io.ballerina.cli.launcher.BLauncherException;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.internal.bala.PackageJson;
import io.ballerina.projects.util.BuildToolUtils;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.test.BCompileUtil;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
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
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;

import static io.ballerina.cli.cmd.CommandOutputUtils.assertTomlFilesEquals;
import static io.ballerina.cli.cmd.CommandOutputUtils.getOutput;
import static io.ballerina.cli.cmd.CommandOutputUtils.replaceDependenciesTomlContent;
import static io.ballerina.projects.util.ProjectConstants.BALA_DOCS_DIR;
import static io.ballerina.projects.util.ProjectConstants.BALLERINA_TOML;
import static io.ballerina.projects.util.ProjectConstants.DEPENDENCIES_TOML;
import static io.ballerina.projects.util.ProjectConstants.DIST_CACHE_DIRECTORY;
import static io.ballerina.projects.util.ProjectConstants.MODULES_ROOT;
import static io.ballerina.projects.util.ProjectConstants.MODULE_MD_FILE_NAME;
import static io.ballerina.projects.util.ProjectConstants.PACKAGE_JSON;
import static io.ballerina.projects.util.ProjectConstants.PACKAGE_MD_FILE_NAME;
import static io.ballerina.projects.util.ProjectConstants.README_MD_FILE_NAME;
import static io.ballerina.projects.util.ProjectConstants.RESOURCE_DIR_NAME;
import static io.ballerina.projects.util.ProjectConstants.USER_DIR;
import static io.ballerina.projects.util.ProjectConstants.USER_DIR_PROPERTY;

/**
 * Pack command tests.
 *
 * @since 2.0.0
 */
public class PackCommandTest extends BaseCommandTest {

    private static final String VALID_PROJECT = "validApplicationProject";
    private Path testResources;

    private static final Path logFile = Path.of("build/logs/log_creator_combined_plugin/compiler-plugin.txt")
            .toAbsolutePath();

    @Override
    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        try {
            this.testResources = super.tmpDir.resolve("build-test-resources");
            URI testResourcesURI = Objects.requireNonNull(getClass().getClassLoader().getResource("test-resources"))
                    .toURI();
            Path testResourcesPath = Path.of(testResourcesURI);
            Files.walkFileTree(testResourcesPath,
                    new BuildCommandTest.Copy(testResourcesPath, this.testResources));

            // Copy the compiler plugin jars to the test resources directory
            Path compilerPluginJarsPath = Path.of("build", "compiler-plugin-jars");
            Files.walkFileTree(compilerPluginJarsPath,
                    new BuildCommandTest.Copy(compilerPluginJarsPath,
                            this.testResources.resolve("compiler-plugin-jars")));
        } catch (URISyntaxException e) {
            Assert.fail("error loading resources");
        }
        Files.createDirectories(logFile.getParent());
        Files.writeString(logFile, "");
    }

    @AfterMethod
    public void afterMethod() {
        ProjectUtils.clearDiagnostics();
    }

    @Test(description = "Pack a library package", dataProvider = "optimizeDependencyCompilation")
    public void testPackProject(Boolean optimizeDependencyCompilation) throws IOException {
        Path projectPath = this.testResources.resolve("validLibraryProject");
        System.setProperty(USER_DIR, projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true,
                optimizeDependencyCompilation);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replace("\r", ""),
                getOutput("compile-bal-project.txt"));
        Assert.assertTrue(
                projectPath.resolve("target").resolve("bala").resolve("foo-winery-any-0.1.0.bala").toFile().exists());
        Assert.assertTrue(projectPath.resolve("target").resolve("cache").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve(JvmTarget.JAVA_21.code())
                .resolve("foo-winery-0.1.0.jar").toFile().exists());

        Path balaDirPath = projectPath.resolve("target").resolve("bala");
        Path balaFilePath = balaDirPath.resolve("foo-winery-any-0.1.0.bala");
        Path extractedPath = balaDirPath.resolve("extracted");
        ProjectUtils.extractBala(balaFilePath, extractedPath);
        Assert.assertTrue(Files.exists(extractedPath.resolve(BALA_DOCS_DIR).resolve(README_MD_FILE_NAME)));

        String packageJsonContent = Files.readString(extractedPath.resolve(PACKAGE_JSON));
        PackageJson packageJson = new Gson().fromJson(packageJsonContent, PackageJson.class);
        Assert.assertEquals(BALA_DOCS_DIR + "/" + README_MD_FILE_NAME, packageJson.getReadme());
    }

    @Test(description = "Pack a ballerina project with the engagement of all type of compiler plugins",
            dataProvider = "optimizeDependencyCompilation")
    public void testRunBalProjectWithAllCompilerPlugins(Boolean optimizeDependencyCompilation) throws IOException {
        Path compilerPluginPath = Path.of("./src/test/resources/test-resources/compiler-plugins");
        BCompileUtil.compileAndCacheBala(compilerPluginPath.resolve("log_creator_pkg_provided_code_analyzer_im")
                .toAbsolutePath().toString());
        BCompileUtil.compileAndCacheBala(compilerPluginPath.resolve("log_creator_pkg_provided_code_generator_im")
                .toAbsolutePath().toString());
        BCompileUtil.compileAndCacheBala(compilerPluginPath.resolve("log_creator_pkg_provided_code_modifier_im")
                .toAbsolutePath().toString());

        Path projectPath = this.testResources.resolve("compiler-plugins").resolve("log_creator_combined_plugin");
        System.setProperty(USER_DIR, projectPath.toString());
        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true,
                optimizeDependencyCompilation);
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
        System.setProperty(USER_DIR, projectPath.toString());
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
        System.setProperty(USER_DIR, this.testResources.resolve("valid-bal-file").toString());
        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true, false);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertTrue(buildLog.contains(" bal pack can only be used with a Ballerina package."));
    }

    @Test(description = "Pack a package with platform libs", dataProvider = "optimizeDependencyCompilation")
    public void testPackageWithPlatformLibs(Boolean optimizeDependencyCompilation) throws IOException {
        Path projectPath = this.testResources.resolve("validGraalvmCompatibleProjectWithPlatformLibs");
        System.setProperty(USER_DIR, projectPath.toString());
        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true,
                optimizeDependencyCompilation);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replace("\r", ""),
                getOutput("build-project-with-platform-libs.txt"));
        Assert.assertTrue(projectPath.resolve("target").resolve("bala").resolve("sameera-myproject-java21-0.1.0.bala")
                .toFile().exists());
    }

    @Test(description = "Pack a package with java11 platform libs")
    public void testPackageWithJava11PlatformLibs() throws IOException {
        Path projectPath = this.testResources.resolve("projectWithJava11PlatformLibs");
        System.setProperty(USER_DIR, projectPath.toString());
        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replace("\r", ""),
                getOutput("build-project-with-platform-libs.txt"));
        Path balaDirPath = projectPath.resolve("target").resolve("bala");
        Assert.assertTrue(balaDirPath.resolve("sameera-myproject-java21-0.1.0.bala")
                .toFile().exists());

        Path balaDestPath = balaDirPath.resolve("extracted");
        ProjectUtils.extractBala(balaDirPath.resolve("sameera-myproject-java21-0.1.0.bala"), balaDestPath);
        Assert.assertTrue(balaDestPath.resolve("platform").resolve(JvmTarget.JAVA_21.code()).resolve("one-1.0.0.jar")
                .toFile().exists());
    }

    @Test(description = "Pack a package with multiple java platform libs")
    public void testPackageWithMultipleJavaPlatformLibs() throws IOException {
        Path projectPath = this.testResources.resolve("projectWithJava11and17PlatformLibs");
        System.setProperty(USER_DIR, projectPath.toString());
        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replace("\r", ""),
                getOutput("build-project-with-platform-libs.txt"));
        Path balaDirPath = projectPath.resolve("target").resolve("bala");
        Assert.assertTrue(balaDirPath.resolve("sameera-myproject-java21-0.1.0.bala")
                .toFile().exists());

        Path balaDestPath = balaDirPath.resolve("extracted");
        ProjectUtils.extractBala(balaDirPath.resolve("sameera-myproject-java21-0.1.0.bala"), balaDestPath);
        Assert.assertTrue(balaDestPath.resolve("platform").resolve(JvmTarget.JAVA_21.code()).resolve("one-1.0.0.jar")
                .toFile().exists());
        Assert.assertTrue(balaDestPath.resolve("platform").resolve(JvmTarget.JAVA_21.code()).resolve("two-2.0.0.jar")
                .toFile().exists());
    }

    @Test(description = "Pack a package with testOnly platform libs")
    public void testPackageWithTestOnlyPlatformLibs() throws IOException {
        Path projectPath = this.testResources.resolve("projectWithTestOnlyPlatformLibs");
        System.setProperty(USER_DIR, projectPath.toString());
        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replace("\r", ""),
                getOutput("pack-project-with-test-only-platform-libs.txt"));
        Assert.assertTrue(projectPath.resolve("target").resolve("bala").resolve("sameera-myproject-any-0.1.0.bala")
                .toFile().exists());
    }

    @Test(description = "Pack a package with ballerina/java imports only in tests")
    public void testPackageWithTestOnlyJavaImports() throws IOException {
        Path projectPath = this.testResources.resolve("projectWithTestOnlyJavaImports");
        System.setProperty(USER_DIR, projectPath.toString());
        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replace("\r", ""),
                getOutput("pack-project-with-test-only-platform-libs.txt"));
        Assert.assertTrue(projectPath.resolve("target").resolve("bala").resolve("sameera-myproject-any-0.1.0.bala")
                .toFile().exists());
    }

    @Test(description = "Pack a project with a build tool execution", dataProvider = "optimizeDependencyCompilation")
    public void testPackProjectWithBuildTool(Boolean optimizeDependencyCompilation) throws IOException {
        Path projectPath = this.testResources.resolve("proper-build-tool");
        System.setProperty(USER_DIR_PROPERTY, projectPath.toString());
        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true,
                optimizeDependencyCompilation);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", "").replace("\\", "/"),
                getOutput("pack-project-with-build-tool.txt"));
        Assert.assertTrue(projectPath.resolve("target").resolve("bala").resolve("foo-winery-any-0.1.0.bala")
                .toFile().exists());
    }

    @Test(description = "Pack a package with an empty Dependencies.toml",
            dataProvider = "optimizeDependencyCompilation")
    public void testPackageWithEmptyDependenciesToml(Boolean optimizeDependencyCompilation) throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithDependenciesToml");
        System.setProperty(USER_DIR, projectPath.toString());
        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true,
                optimizeDependencyCompilation);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replace("\r", ""),
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
    public void testPackageWithoutRootPackageInDependenciesToml()
            throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWoRootPkgInDepsToml");
        System.setProperty(USER_DIR, projectPath.toString());
        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replace("\r", ""),
                getOutput("build-project-wo-root-pkg-in-deps-toml.txt"));
        Assert.assertTrue(projectPath.resolve("target").resolve("bala").resolve("foo-winery-java21-0.1.0.bala")
                .toFile().exists());

        assertTomlFilesEquals(projectPath.resolve(DEPENDENCIES_TOML),
                projectPath.resolve(RESOURCE_DIR_NAME).resolve("expectedDependencies.toml"));
    }

    @Test(description = "Pack an empty package with compiler plugin")
    public void testPackEmptyProjectWithCompilerPlugin() throws IOException {
        Path projectPath = this.testResources.resolve("emptyProjectWithCompilerPlugin");
        System.setProperty(USER_DIR, projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertTrue(projectPath.resolve("target").resolve("bala")
                .resolve("wso2-emptyProjWithCompilerPlugin-java21-0.1.0.bala").toFile().exists());
        Assert.assertEquals(buildLog.replace("\r", ""),
                getOutput("compile-empty-project-with-compiler-plugin.txt"));
    }

    @Test(description = "Pack an empty package with compiler plugin")
    public void testPackEmptyProjectWithBuildTools() throws IOException {
        Path testDistCacheDirectory = Path.of("build").toAbsolutePath().resolve(DIST_CACHE_DIRECTORY);
        BCompileUtil.compileAndCacheBala(testResources.resolve("buildToolResources").resolve("tools")
                .resolve("ballerina-generate-file").toString(), testDistCacheDirectory);
        Path projectPath = this.testResources.resolve("emptyProjectWithBuildTool");
        replaceDependenciesTomlContent(projectPath, "**INSERT_DISTRIBUTION_VERSION_HERE**",
                RepoUtils.getBallerinaShortVersion());
        System.setProperty(USER_DIR, projectPath.toString());
        try (MockedStatic<BuildToolUtils> repoUtils = Mockito.mockStatic(
                BuildToolUtils.class, Mockito.CALLS_REAL_METHODS)) {
            repoUtils.when(BuildToolUtils::getCentralBalaDirPath).thenReturn(testDistCacheDirectory.resolve("bala"));
            PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
            new CommandLine(packCommand).parseArgs();
            packCommand.execute();
        }
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", "").replace("\\", "/"),
                getOutput("pack-empty-project-with-build-tools.txt"));
    }

    @Test(description = "Pack an empty package as a tool")
    public void testPackEmptyProjectWithTool() throws IOException {
        Path projectPath = this.testResources.resolve("emptyProjectWithTool");
        System.setProperty(USER_DIR, projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertTrue(projectPath.resolve("target").resolve("bala")
                .resolve("wso2-emptyProjWithTool-java21-0.1.0.bala").toFile().exists());
        Assert.assertEquals(buildLog.replace("\r", ""),
                getOutput("compile-empty-project-with-tool.txt"));
    }

    @Test(description = "Pack an empty package with tests only")
    public void testPackEmptyProjectWithTestsOnly() {
        Path projectPath = this.testResources.resolve("emptyProjectWithTestsOnly");
        System.setProperty(USER_DIR, projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();

        Assert.assertTrue(projectPath.resolve("target").resolve("bala")
                .resolve("wso2-emptyProjWithTestsOnly-any-0.1.0.bala").toFile().exists());
    }

    @Test(description = "Pack an empty package with Non Default modules")
    public void testPackEmptyProjectWithNonDefaultModules() {
        Path projectPath = this.testResources.resolve("emptyProjectWithNonDefaultModules");
        System.setProperty(USER_DIR, projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();

        Assert.assertTrue(projectPath.resolve("target").resolve("bala")
                .resolve("wso2-emptyProjWithNonDefaultModules-any-0.1.0.bala").toFile().exists());
    }

    @Test(description = "Pack an empty package with Non Default modules with Tests only")
    public void testPackEmptyProjectWithNonDefaultModulesTestOnly() {
        Path projectPath = this.testResources.resolve("emptyProjectWithNonDefaultModulesTestOnly");
        System.setProperty(USER_DIR, projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();

        Assert.assertTrue(projectPath.resolve("target").resolve("bala")
                .resolve("wso2-emptyProjWithNonDefaultModulesTestOnly-any-0.1.0.bala").toFile().exists());
    }

    @Test(description = "Pack an empty package with empty Non Default")
    public void testPackEmptyNonDefaultModule() throws IOException {
        Path projectPath = this.testResources.resolve("emptyNonDefaultModule");
        System.setProperty(USER_DIR, projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        try {
            packCommand.execute();
        } catch (BLauncherException e) {
            List<String> messages = e.getMessages();
            Assert.assertEquals(messages.size(), 1);
            Assert.assertEquals(messages.get(0), getOutput("build-empty-nondefault-module.txt"));
        }
        Assert.assertFalse(projectPath.resolve("target").resolve("bala")
                .resolve("wso2-emptyNonDefaultModule-any-0.1.0.bala").toFile().exists());
    }

    @Test(description = "Tests packing with a custom dir")
    public void testCustomTargetDir() throws IOException {
        Path projectPath = this.testResources.resolve(VALID_PROJECT);
        Path customTargetDir = projectPath.resolve("customTargetDir");
        System.setProperty(USER_DIR, projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true,
                customTargetDir);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replace("\r", ""),
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
        System.setProperty(USER_DIR, projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true,
                customTargetDir);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);

        Assert.assertEquals(buildLog.replace("\r", ""),
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
        System.setProperty(USER_DIR, projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        try {
            packCommand.execute();
        } catch (BLauncherException e) {
            List<String> messages = e.getMessages();
            Assert.assertEquals(messages.size(), 1);
            Assert.assertEquals(messages.get(0), getOutput("pack-empty-package.txt"));
        }
    }

    @Test(description = "Pack an empty package with compiler plugin")
    public void testPackEmptyPackageWithCompilerPlugin() throws IOException {
        Path projectPath = this.testResources.resolve("emptyPackageWithCompilerPlugin");
        replaceDependenciesTomlContent(
                projectPath, "**INSERT_DISTRIBUTION_VERSION_HERE**", RepoUtils.getBallerinaShortVersion());
        System.setProperty(USER_DIR, projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();

        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""),
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
        System.setProperty(USER_DIR, projectPath.toString());

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
        System.setProperty(USER_DIR, projectPath.toString());

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
        System.setProperty(USER_DIR, projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("pack-project-with-platform-libs.txt"));
        Path balaDirPath = projectPath.resolve("target").resolve("bala");
        Path balaFilePath = balaDirPath.resolve("sameera-myproject-java21-0.1.0.bala");
        Path balaDestPath = balaDirPath.resolve("extracted");
        ProjectUtils.extractBala(balaFilePath, balaDestPath);
        String packageJsonContent = Files.readString(balaDestPath.resolve("package.json"));
        Assert.assertFalse(packageJsonContent.contains("\"graalvmCompatible\""));
    }

    @Test(description = "Pack with graalvm compatible single dependency.")
    public void testPackProjectWithPlatformLibsGraal1() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithPlatformLibsGraal1");
        System.setProperty(USER_DIR, projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("pack-project-with-platform-libs-graal1.txt"));
        Path balaDirPath = projectPath.resolve("target").resolve("bala");
        Path balaFilePath = balaDirPath.resolve("sameera-myproject-java21-0.1.0.bala");
        Path balaDestPath = balaDirPath.resolve("extracted");
        ProjectUtils.extractBala(balaFilePath, balaDestPath);
        String packageJsonContent = Files.readString(balaDestPath.resolve("package.json"));
        Assert.assertTrue(packageJsonContent.contains("\"graalvmCompatible\": true"));
    }

    @Test(description = "Pack with non-graalvm compatible single dependency")
    public void testPackProjectWithPlatformLibsGraal2() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithPlatformLibsGraal2");
        System.setProperty(USER_DIR, projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("pack-project-with-platform-libs-graal2.txt"));
        Path balaDirPath = projectPath.resolve("target").resolve("bala");
        Path balaFilePath = balaDirPath.resolve("sameera-myproject-java21-0.1.0.bala");
        Path balaDestPath = balaDirPath.resolve("extracted");
        ProjectUtils.extractBala(balaFilePath, balaDestPath);
        String packageJsonContent = Files.readString(balaDestPath.resolve("package.json"));
        Assert.assertTrue(packageJsonContent.contains("\"graalvmCompatible\": false"));
    }

    @Test(description = "Pack with a non-graalvm compatible dependency, a graalvm compatible dependency, dependency " +
            "with unspecified attribute")
    public void testPackProjectWithPlatformLibsGraal3() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithPlatformLibsGraal3");
        System.setProperty(USER_DIR, projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("pack-project-with-platform-libs-graal3.txt"));
        Path balaDirPath = projectPath.resolve("target").resolve("bala");
        Path balaFilePath = balaDirPath.resolve("sameera-myproject-java21-0.1.0.bala");
        Path balaDestPath = balaDirPath.resolve("extracted");
        ProjectUtils.extractBala(balaFilePath, balaDestPath);
        String packageJsonContent = Files.readString(balaDestPath.resolve("package.json"));
        Assert.assertTrue(packageJsonContent.contains("\"graalvmCompatible\": false"));
    }

    @Test(description = "Pack with a graalvm compatible dependency, dependency with unspecified attribute")
    public void testPackProjectWithPlatformLibsGraal4() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithPlatformLibsGraal4");
        System.setProperty(USER_DIR, projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("pack-project-with-platform-libs-graal4.txt"));
        Path balaDirPath = projectPath.resolve("target").resolve("bala");
        Path balaFilePath = balaDirPath.resolve("sameera-myproject-java21-0.1.0.bala");
        Path balaDestPath = balaDirPath.resolve("extracted");
        ProjectUtils.extractBala(balaFilePath, balaDestPath);
        String packageJsonContent = Files.readString(balaDestPath.resolve("package.json"));
        Assert.assertFalse(packageJsonContent.contains("\"graalvmCompatible\""));
    }

    @Test(description = "Pack with a non-graalvm compatible dependency, 'graalvmCompatible=true' attribute set at " +
            "the platform")
    public void testPackProjectWithPlatformLibsGraal5() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithPlatformLibsGraal5");
        System.setProperty(USER_DIR, projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("pack-project-with-platform-libs-graal5.txt"));
        Path balaDirPath = projectPath.resolve("target").resolve("bala");
        Path balaFilePath = balaDirPath.resolve("sameera-myproject-java21-0.1.0.bala");
        Path balaDestPath = balaDirPath.resolve("extracted");
        ProjectUtils.extractBala(balaFilePath, balaDestPath);
        String packageJsonContent = Files.readString(balaDestPath.resolve("package.json"));
        Assert.assertTrue(packageJsonContent.contains("\"graalvmCompatible\": false"));
    }

    @Test(description = "Pack with a graalvm compatible dependency, 'graalvmCompatible=false' attribute set at " +
            "the platform")
    public void testPackProjectWithPlatformLibsGraal6() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithPlatformLibsGraal6");
        System.setProperty(USER_DIR, projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("pack-project-with-platform-libs-graal6.txt"));
        Path balaDirPath = projectPath.resolve("target").resolve("bala");
        Path balaFilePath = balaDirPath.resolve("sameera-myproject-java21-0.1.0.bala");
        Path balaDestPath = balaDirPath.resolve("extracted");
        ProjectUtils.extractBala(balaFilePath, balaDestPath);
        String packageJsonContent = Files.readString(balaDestPath.resolve("package.json"));
        Assert.assertTrue(packageJsonContent.contains("\"graalvmCompatible\": false"));
    }

    @Test(description = "Pack with a graalvm compatible dependency, dependency with unspecified attribute with " +
            "different java platforms")
    public void testPackProjectWithPlatformLibsGraal7() throws IOException {
        Path projectPath = this.testResources.resolve("validProjectWithPlatformLibsGraal7");
        System.setProperty(USER_DIR, projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replace("\r", ""), getOutput("pack-project-with-platform-libs-graal7.txt"));
        Path balaDirPath = projectPath.resolve("target").resolve("bala");
        Path balaFilePath = balaDirPath.resolve("sameera-myproject-java21-0.1.0.bala");
        Path balaDestPath = balaDirPath.resolve("extracted");
        ProjectUtils.extractBala(balaFilePath, balaDestPath);
        String packageJsonContent = Files.readString(balaDestPath.resolve("package.json"));
        Assert.assertTrue(packageJsonContent.contains("\"graalvmCompatible\": false"));
    }

    @Test (description = "Test a package that contains Package.md and Module.md for docs")
    public void testOldPackageDocStructure() throws IOException {
        Path projectPath = this.testResources.resolve("readme-test-projects")
                .resolve("validLibraryProjectWithOldMds");
        System.setProperty(USER_DIR, projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);
        String warning = """
                        The default file for package documentation is changed to README.md. If you prefer to \
                        use the Package.md, add the following line under the '[package]' section in your \
                        Ballerina.toml file:
                        \treadme = "Package.md"
                        """;
        Assert.assertTrue(buildLog.contains(warning));

        // Verify the docs
        Path balaDirPath = projectPath.resolve("target").resolve("bala");
        Path balaFilePath = balaDirPath.resolve("foo-winery-any-0.1.0.bala");
        Path extractedPath = balaDirPath.resolve("extracted");
        ProjectUtils.extractBala(balaFilePath, extractedPath);
        Assert.assertTrue(Files.exists(extractedPath.resolve(BALA_DOCS_DIR).resolve(PACKAGE_MD_FILE_NAME)));

        // Verify the docs entry in package.json
        String packageJsonContent = Files.readString(extractedPath.resolve("package.json"));
        PackageJson packageJson = new Gson().fromJson(packageJsonContent, PackageJson.class);
        Assert.assertEquals(BALA_DOCS_DIR + "/" + PACKAGE_MD_FILE_NAME, packageJson.getReadme());
    }

    @Test (description = "Add the readme entry to the package with old doc structure to resolve the warning",
            dependsOnMethods = "testOldPackageDocStructure")
    public void testConvertOldDocStructureToNew() throws IOException {
        Path projectPath = this.testResources.resolve("readme-test-projects")
                .resolve("validLibraryProjectWithOldMds");
        Files.move(projectPath.resolve("With-readme-Ballerina.toml"),
                projectPath.resolve(BALLERINA_TOML), StandardCopyOption.REPLACE_EXISTING);
        System.setProperty(USER_DIR, projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);
        String warning = """
                        The default file for package documentation is changed to README.md. If you prefer to \
                        use the Package.md, add the following line under the '[package]' section in your \
                        Ballerina.toml file:
                        \treadme = "Package.md"
                        """;
        Assert.assertFalse(buildLog.contains(warning));

        // Verify the docs
        Path balaDirPath = projectPath.resolve("target").resolve("bala");
        Path balaFilePath = balaDirPath.resolve("foo-winery-any-0.1.0.bala");
        Path extractedPath = balaDirPath.resolve("extracted");
        ProjectUtils.extractBala(balaFilePath, extractedPath);
        Assert.assertTrue(Files.exists(extractedPath.resolve(BALA_DOCS_DIR).resolve(PACKAGE_MD_FILE_NAME)));

        // Verify the docs entry in package.json
        String packageJsonContent = Files.readString(extractedPath.resolve("package.json"));
        PackageJson packageJson = new Gson().fromJson(packageJsonContent, PackageJson.class);
        Assert.assertEquals(BALA_DOCS_DIR + "/" + PACKAGE_MD_FILE_NAME, packageJson.getReadme());
    }

    @Test (description = "Package root contains README.md but the Ballerina.toml has Package.md")
    public void testLibPackageWithWrongMd() throws IOException {
        Path projectPath = this.testResources.resolve("readme-test-projects")
                .resolve("libraryProjectWithWrongMd");
        System.setProperty(USER_DIR, projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        try {
            packCommand.execute();
        } catch (BLauncherException e) {
            String buildLog = readOutput(true);
            Assert.assertTrue(buildLog.contains("could not locate the readme file"));
        }
    }

    @Test
    public void testMultiModuleProjectWithOldDocStructure() throws IOException {
        Path projectPath = this.testResources.resolve("readme-test-projects")
                .resolve("validMultiModuleProjectWithPackageAndModuleMds");
        System.setProperty(USER_DIR, projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);

        String warning = """
                        The default file for package documentation is changed to README.md. If you prefer to \
                        use the Package.md, add the following line under the '[package]' section in your \
                        Ballerina.toml file:
                        \treadme = "Package.md"
                        """;
        Assert.assertTrue(buildLog.contains(warning));

        // Verify the docs
        Path balaDirPath = projectPath.resolve("target").resolve("bala");
        Path balaFilePath = balaDirPath.resolve("foo-winery-any-0.1.0.bala");
        Path extractedPath = balaDirPath.resolve("extracted");
        ProjectUtils.extractBala(balaFilePath, extractedPath);

        String packageDocPath = BALA_DOCS_DIR + "/" + PACKAGE_MD_FILE_NAME;
        String nonDefaultModuleDocPath = BALA_DOCS_DIR + "/" + MODULES_ROOT + "/winery.storage/" + MODULE_MD_FILE_NAME;
        Assert.assertTrue(Files.exists(extractedPath.resolve(packageDocPath)));
        Assert.assertTrue(Files.exists(extractedPath.resolve(nonDefaultModuleDocPath)));

        // Verify the docs entry in package.json
        String packageJsonContent = Files.readString(extractedPath.resolve(PACKAGE_JSON));
        PackageJson packageJson = new Gson().fromJson(packageJsonContent, PackageJson.class);
        Assert.assertEquals(packageJson.getModules().size(), 1);
        Assert.assertEquals(packageDocPath, packageJson.getReadme());
        Assert.assertEquals(nonDefaultModuleDocPath, packageJson.getModules().stream().filter(
                module -> "winery.storage".equals(module.name())).findFirst().get().readme());
    }

    @Test (description = "Add readme entries for all places in the package with the old doc structure" +
            " to resolve the warning",
            dependsOnMethods = "testMultiModuleProjectWithOldDocStructure")
    public void testConvertMultiModuleOldDocStructureToNew() throws IOException {
        Path projectPath = this.testResources.resolve("readme-test-projects")
                .resolve("validMultiModuleProjectWithPackageAndModuleMds");
        System.setProperty(USER_DIR, projectPath.toString());
        Files.move(projectPath.resolve("With-readme-for-non-default-mod-Ballerina.toml"),
                        projectPath.resolve(BALLERINA_TOML), StandardCopyOption.REPLACE_EXISTING);

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();
        String buildLog = readOutput(true);
        String warning = """
                        The default file for package documentation is changed to README.md. If you prefer to \
                        use the Package.md, add the following line under the '[package]' section in your \
                        Ballerina.toml file:
                        \treadme = "Package.md"
                        """;
        Assert.assertFalse(buildLog.contains(warning));

        // Verify the docs
        Path balaDirPath = projectPath.resolve("target").resolve("bala");
        Path balaFilePath = balaDirPath.resolve("foo-winery-any-0.1.0.bala");
        Path extractedPath = balaDirPath.resolve("extracted");
        ProjectUtils.extractBala(balaFilePath, extractedPath);

        String packageDocPath = BALA_DOCS_DIR + "/" + PACKAGE_MD_FILE_NAME;
        String nonDefaultModuleDocPath = BALA_DOCS_DIR + "/" + MODULES_ROOT + "/winery.storage/" + MODULE_MD_FILE_NAME;
        Assert.assertTrue(Files.exists(extractedPath.resolve(packageDocPath)));
        Assert.assertTrue(Files.exists(extractedPath.resolve(nonDefaultModuleDocPath)));

        // Verify the docs entry in package.json
        String packageJsonContent = Files.readString(extractedPath.resolve(PACKAGE_JSON));
        PackageJson packageJson = new Gson().fromJson(packageJsonContent, PackageJson.class);
        Assert.assertEquals(packageJson.getModules().size(), 1);
        Assert.assertEquals(packageDocPath, packageJson.getReadme());
        Assert.assertEquals(nonDefaultModuleDocPath, packageJson.getModules().stream().filter(
                module -> "winery.storage".equals(module.name())).findFirst().get().readme());
    }

    @Test
    public void testMultiModuleProjectWithNewDefaultDocStructure() throws IOException {
        Path projectPath = this.testResources.resolve("readme-test-projects")
                .resolve("validMultiModuleProjectWithReadmeMds");
        System.setProperty(USER_DIR, projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();

        // Verify the docs
        Path balaDirPath = projectPath.resolve("target").resolve("bala");
        Path balaFilePath = balaDirPath.resolve("foo-winery-any-0.1.0.bala");
        Path extractedPath = balaDirPath.resolve("extracted");
        ProjectUtils.extractBala(balaFilePath, extractedPath);

        String packageDocPath = BALA_DOCS_DIR + "/" + README_MD_FILE_NAME;
        String nonDefaultModuleDocPath = BALA_DOCS_DIR + "/" + MODULES_ROOT + "/winery.storage/" + README_MD_FILE_NAME;
        Assert.assertTrue(Files.exists(extractedPath.resolve(packageDocPath)));
        Assert.assertTrue(Files.exists(extractedPath.resolve(nonDefaultModuleDocPath)));

        // Verify the docs entry in package.json
        String packageJsonContent = Files.readString(extractedPath.resolve(PACKAGE_JSON));
        PackageJson packageJson = new Gson().fromJson(packageJsonContent, PackageJson.class);
        Assert.assertEquals(packageJson.getModules().size(), 1);
        Assert.assertEquals(packageDocPath, packageJson.getReadme());
        Assert.assertEquals(nonDefaultModuleDocPath, packageJson.getModules().stream().filter(
                module -> "winery.storage".equals(module.name())).findFirst().get().readme());
    }

    @Test (description = "One non-default module does not contain a readme")
    public void testMultiModuleProjectWithNewDefaultDocStructure2() throws IOException {
        Path projectPath = this.testResources.resolve("readme-test-projects")
                .resolve("validMultiModuleProjectWithReadmeMds2");
        System.setProperty(USER_DIR, projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();

        // Verify the docs
        Path balaDirPath = projectPath.resolve("target").resolve("bala");
        Path balaFilePath = balaDirPath.resolve("foo-winery-any-0.1.0.bala");
        Path extractedPath = balaDirPath.resolve("extracted");
        ProjectUtils.extractBala(balaFilePath, extractedPath);

        String packageDocPath = BALA_DOCS_DIR + "/" + README_MD_FILE_NAME;
        String storageModuleDocPath = BALA_DOCS_DIR + "/" + MODULES_ROOT + "/winery.storage/" + README_MD_FILE_NAME;
        String commonModuleDocPath = BALA_DOCS_DIR + "/" + MODULES_ROOT + "/winery.common/" + README_MD_FILE_NAME;
        Assert.assertTrue(Files.exists(extractedPath.resolve(packageDocPath)));
        Assert.assertTrue(Files.exists(extractedPath.resolve(storageModuleDocPath)));
        Assert.assertFalse(Files.exists(extractedPath.resolve(commonModuleDocPath)));

        // Verify the docs entry in package.json
        String packageJsonContent = Files.readString(extractedPath.resolve(PACKAGE_JSON));
        PackageJson packageJson = new Gson().fromJson(packageJsonContent, PackageJson.class);
        Assert.assertEquals(packageJson.getModules().size(), 2);
        Assert.assertEquals(packageDocPath, packageJson.getReadme());
        Assert.assertEquals(storageModuleDocPath, packageJson.getModules().stream().filter(
                module -> "winery.storage".equals(module.name())).findFirst().get().readme());
    }

    @Test (description = "One non-default module uses a custom MD. " +
            "Other one and the package doc use the default README.md")
    public void testMultiModuleProjectCustomReadmes() throws IOException {
        Path projectPath = this.testResources.resolve("readme-test-projects")
                .resolve("validMultiModuleProjectCustomModuleReadme");
        System.setProperty(USER_DIR, projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();

        // Verify the docs
        Path balaDirPath = projectPath.resolve("target").resolve("bala");
        Path balaFilePath = balaDirPath.resolve("foo-winery-any-0.1.0.bala");
        Path extractedPath = balaDirPath.resolve("extracted");
        ProjectUtils.extractBala(balaFilePath, extractedPath);

        String packageDocPath = BALA_DOCS_DIR + "/" + README_MD_FILE_NAME;
        String storageModuleDocPath = BALA_DOCS_DIR + "/" + MODULES_ROOT + "/winery.storage/" + MODULE_MD_FILE_NAME;
        String commonModuleDocPath = BALA_DOCS_DIR + "/" + MODULES_ROOT + "/winery.common/" + README_MD_FILE_NAME;
        Assert.assertTrue(Files.exists(extractedPath.resolve(packageDocPath)));
        Assert.assertTrue(Files.exists(extractedPath.resolve(storageModuleDocPath)));
        Assert.assertTrue(Files.exists(extractedPath.resolve(commonModuleDocPath)));

        // Verify the docs entry in package.json
        String packageJsonContent = Files.readString(extractedPath.resolve(PACKAGE_JSON));
        PackageJson packageJson = new Gson().fromJson(packageJsonContent, PackageJson.class);
        Assert.assertEquals(packageJson.getModules().size(), 2);
        Assert.assertEquals(packageDocPath, packageJson.getReadme());
        Assert.assertEquals(storageModuleDocPath, packageJson.getModules().stream().filter(
                module -> "winery.storage".equals(module.name())).findFirst().get().readme());
        Assert.assertEquals(commonModuleDocPath, packageJson.getModules().stream().filter(
                module -> "winery.common".equals(module.name())).findFirst().get().readme());
    }

    @Test (description = "One non-default module does not have a doc")
    public void testMultiModuleProjectReadmeOptionality() throws IOException {
        Path projectPath = this.testResources.resolve("readme-test-projects")
                .resolve("validMultiModuleProjectOptionalModuleReadme");
        System.setProperty(USER_DIR, projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();

        // Verify the docs
        Path balaDirPath = projectPath.resolve("target").resolve("bala");
        Path balaFilePath = balaDirPath.resolve("foo-winery-any-0.1.0.bala");
        Path extractedPath = balaDirPath.resolve("extracted");
        ProjectUtils.extractBala(balaFilePath, extractedPath);

        String packageDocPath = BALA_DOCS_DIR + "/" + README_MD_FILE_NAME;
        String storageModuleDocPath = BALA_DOCS_DIR + "/" + MODULES_ROOT + "/winery.storage/" + MODULE_MD_FILE_NAME;
        String commonModuleDocPath = BALA_DOCS_DIR + "/" + MODULES_ROOT + "/winery.common/";
        Assert.assertTrue(Files.exists(extractedPath.resolve(packageDocPath)));
        Assert.assertTrue(Files.exists(extractedPath.resolve(storageModuleDocPath)));
        Assert.assertTrue(Files.notExists(extractedPath.resolve(commonModuleDocPath)));

        // Verify the docs entry in package.json
        String packageJsonContent = Files.readString(extractedPath.resolve(PACKAGE_JSON));
        PackageJson packageJson = new Gson().fromJson(packageJsonContent, PackageJson.class);
        Assert.assertEquals(packageJson.getModules().size(), 2);
        Assert.assertEquals(packageDocPath, packageJson.getReadme());
        Assert.assertEquals(storageModuleDocPath, packageJson.getModules().stream().filter(
                module -> "winery.storage".equals(module.name())).findFirst().get().readme());
    }

    @Test
    public void testReadMeWithHierarchicalModuleName() throws IOException {
        Path projectPath = this.testResources.resolve("readme-test-projects")
                .resolve("validHierarchicalModuleProjectWithReadmeMds");
        System.setProperty(USER_DIR, projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();

        // Verify the docs
        Path balaDirPath = projectPath.resolve("target").resolve("bala");
        Path balaFilePath = balaDirPath.resolve("foo-winery-any-0.1.0.bala");
        Path extractedPath = balaDirPath.resolve("extracted");
        ProjectUtils.extractBala(balaFilePath, extractedPath);

        String packageDocPath = BALA_DOCS_DIR + "/" + README_MD_FILE_NAME;
        String storageModuleDocPath = BALA_DOCS_DIR + "/" + MODULES_ROOT + "/winery.bar.storage/" + README_MD_FILE_NAME;
        String commonModuleDocPath = BALA_DOCS_DIR + "/" + MODULES_ROOT + "/winery.bar.common/";
        Assert.assertTrue(Files.exists(extractedPath.resolve(packageDocPath)));
        Assert.assertTrue(Files.exists(extractedPath.resolve(storageModuleDocPath)));
        Assert.assertTrue(Files.notExists(extractedPath.resolve(commonModuleDocPath)));

        // Verify the docs entry in package.json
        String packageJsonContent = Files.readString(extractedPath.resolve(PACKAGE_JSON));
        PackageJson packageJson = new Gson().fromJson(packageJsonContent, PackageJson.class);
        Assert.assertEquals(packageJson.getModules().size(), 2);
        Assert.assertEquals(packageDocPath, packageJson.getReadme());
        Assert.assertEquals(storageModuleDocPath, packageJson.getModules().stream().filter(
                module -> "winery.bar.storage".equals(module.name())).findFirst().get().readme());
    }

    @Test
    public void testReadMeWithHierarchicalPackageName() throws IOException {
        Path projectPath = this.testResources.resolve("readme-test-projects")
                .resolve("validHierarchicalPackageWithReadmeMds");
        System.setProperty(USER_DIR, projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();

        // Verify the docs
        Path balaDirPath = projectPath.resolve("target").resolve("bala");
        Path balaFilePath = balaDirPath.resolve("foo-bar.winery-any-0.1.0.bala");
        Path extractedPath = balaDirPath.resolve("extracted");
        ProjectUtils.extractBala(balaFilePath, extractedPath);

        String packageDocPath = BALA_DOCS_DIR + "/" + README_MD_FILE_NAME;
        String storageModuleDocPath = BALA_DOCS_DIR + "/" + MODULES_ROOT + "/bar.winery.storage/" + README_MD_FILE_NAME;
        String commonModuleDocPath = BALA_DOCS_DIR + "/" + MODULES_ROOT + "/bar.winery.common/";
        Assert.assertTrue(Files.exists(extractedPath.resolve(packageDocPath)));
        Assert.assertTrue(Files.exists(extractedPath.resolve(storageModuleDocPath)));
        Assert.assertTrue(Files.notExists(extractedPath.resolve(commonModuleDocPath)));

        // Verify the docs entry in package.json
        String packageJsonContent = Files.readString(extractedPath.resolve(PACKAGE_JSON));
        PackageJson packageJson = new Gson().fromJson(packageJsonContent, PackageJson.class);
        Assert.assertEquals(packageJson.getModules().size(), 2);
        Assert.assertEquals(packageDocPath, packageJson.getReadme());
        Assert.assertEquals(storageModuleDocPath, packageJson.getModules().stream().filter(
                module -> "bar.winery.storage".equals(module.name())).findFirst().get().readme());
    }

    @Test
    public void testLibPackageWithNoDocMds() throws IOException {
        Path projectPath = this.testResources.resolve("readme-test-projects")
                .resolve("libraryProjectWithNoMd");
        System.setProperty(USER_DIR, projectPath.toString());

        PackCommand packCommand = new PackCommand(projectPath, printStream, printStream, false, true);
        new CommandLine(packCommand).parseArgs();
        packCommand.execute();

        // Verify the docs
        Path balaDirPath = projectPath.resolve("target").resolve("bala");
        Path balaFilePath = balaDirPath.resolve("foo-winery-any-0.1.0.bala");
        Path extractedPath = balaDirPath.resolve("extracted");
        ProjectUtils.extractBala(balaFilePath, extractedPath);

        String packageDocPath = BALA_DOCS_DIR + "/" + README_MD_FILE_NAME;
        Assert.assertTrue(Files.notExists(extractedPath.resolve(packageDocPath)));

        // Verify the docs entry in package.json
        String packageJsonContent = Files.readString(extractedPath.resolve(PACKAGE_JSON));
        PackageJson packageJson = new Gson().fromJson(packageJsonContent, PackageJson.class);
        Assert.assertNull(packageJson.getModules());
    }

    @AfterClass
    public void cleanUp() throws IOException {
        Files.deleteIfExists(logFile);
        Files.deleteIfExists(logFile.getParent());
        Files.deleteIfExists(logFile.getParent().getParent());
    }
}
