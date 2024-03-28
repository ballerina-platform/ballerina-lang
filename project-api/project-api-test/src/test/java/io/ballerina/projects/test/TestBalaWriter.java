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

package io.ballerina.projects.test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.EmitResult;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.internal.bala.BalToolJson;
import io.ballerina.projects.internal.bala.BalaJson;
import io.ballerina.projects.internal.bala.CompilerPluginJson;
import io.ballerina.projects.internal.bala.DependencyGraphJson;
import io.ballerina.projects.internal.bala.ModuleDependency;
import io.ballerina.projects.internal.bala.PackageJson;
import io.ballerina.projects.internal.model.Dependency;
import io.ballerina.projects.internal.model.Target;
import io.ballerina.projects.util.ProjectUtils;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.ballerina.projects.util.ProjectConstants.BALA_DIR_NAME;
import static io.ballerina.projects.util.ProjectConstants.BALA_DOCS_DIR;
import static io.ballerina.projects.util.ProjectConstants.DEPENDENCY_GRAPH_JSON;
import static io.ballerina.projects.util.ProjectConstants.TARGET_DIR_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Contains cases to test the bala writer.
 *
 * @since 2.0.0
 */
public class TestBalaWriter {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src", "test", "resources");
    private static final Path BALA_WRITER_RESOURCES = RESOURCE_DIRECTORY.resolve("balawriter");
    private static final String PACKAGE_PATH = "packagePath";
    private Path tmpDir;
    private Path balaExportPath;

    @BeforeMethod
    public void setUp() throws IOException {
        this.tmpDir = Files.createTempDirectory("b7a-bala-writer-test-" + System.nanoTime());
        this.balaExportPath = this.tmpDir.resolve("tmpBalaDir");
        Files.createDirectory(Paths.get(String.valueOf(this.balaExportPath)));
    }

    @Test
    public void testBalaWriter(ITestContext ctx) throws IOException {
        Gson gson = new Gson();
        Path projectPath = BALA_WRITER_RESOURCES.resolve("projectOne");
        ctx.getCurrentXmlTest().addParameter(PACKAGE_PATH, String.valueOf(projectPath));
        Project project = TestUtils.loadBuildProject(projectPath);

        PackageCompilation packageCompilation = project.currentPackage().getCompilation();
        if (packageCompilation.diagnosticResult().hasErrors()) {
            Assert.fail("compilation failed:" + packageCompilation.diagnosticResult().errors());
        }

        Target target = new Target(project.sourceRoot());
        Path balaPath = target.getBalaPath();
        // invoke write bala method
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_17);
        EmitResult emitResult = jBallerinaBackend.emit(JBallerinaBackend.OutputType.BALA, balaPath);
        Assert.assertTrue(emitResult.successful());

        // unzip bala
        TestUtils.unzip(String.valueOf(balaPath.resolve("foo-winery-java17-0.1.0.bala")),
                        String.valueOf(balaExportPath));

        // bala.json
        Path balaJsonPath = balaExportPath.resolve("bala.json");
        Assert.assertTrue(balaJsonPath.toFile().exists());

        try (FileReader reader = new FileReader(String.valueOf(balaJsonPath))) {
            BalaJson balaJson = gson.fromJson(reader, BalaJson.class);
            Assert.assertEquals(balaJson.getBala_version(), "2.0.0");
            Assert.assertEquals(balaJson.getBuilt_by(), "WSO2");
        }

        // package.json
        Path packageJsonPath = balaExportPath.resolve("package.json");
        Assert.assertTrue(packageJsonPath.toFile().exists());

        try (FileReader reader = new FileReader(String.valueOf(packageJsonPath))) {
            PackageJson packageJson = gson.fromJson(reader, PackageJson.class);
            Assert.assertEquals(packageJson.getOrganization(), "foo");
            Assert.assertEquals(packageJson.getName(), "winery");
            Assert.assertEquals(packageJson.getVersion(), "0.1.0");

            Assert.assertFalse(packageJson.getLicenses().isEmpty());
            Assert.assertEquals(packageJson.getLicenses().get(0), "MIT");
            Assert.assertEquals(packageJson.getLicenses().get(1), "Apache-2.0");

            Assert.assertFalse(packageJson.getAuthors().isEmpty());
            Assert.assertEquals(packageJson.getAuthors().get(0), "jo@wso2.com");
            Assert.assertEquals(packageJson.getAuthors().get(1), "pramodya@wso2.com");

            Assert.assertEquals(packageJson.getSourceRepository(), "https://github.com/ballerinalang/ballerina");

            Assert.assertFalse(packageJson.getKeywords().isEmpty());
            Assert.assertEquals(packageJson.getKeywords().get(0), "ballerina");
            Assert.assertEquals(packageJson.getKeywords().get(1), "security");
            Assert.assertEquals(packageJson.getKeywords().get(2), "crypto");

            Assert.assertFalse(packageJson.getExport().isEmpty());
            Assert.assertEquals(packageJson.getExport().get(0), "winery");
            Assert.assertEquals(packageJson.getExport().get(1), "winery.services");

            Assert.assertFalse(packageJson.getInclude().isEmpty());
            Assert.assertEquals(packageJson.getInclude().get(0), "**/include-file.*");
            Assert.assertEquals(packageJson.getInclude().get(1), "**/*module-include/file");
            Assert.assertEquals(packageJson.getInclude().get(2), "**/*-module-include-dir");

            Assert.assertEquals(packageJson.getVisibility(), "private");

            Assert.assertEquals(packageJson.getPlatform(), "java17");
            Assert.assertEquals(packageJson.getPlatformDependencies().size(), 1);

            Assert.assertEquals(packageJson.getBallerinaVersion(), RepoUtils.getBallerinaShortVersion());
            Assert.assertEquals(packageJson.getImplementationVendor(), "WSO2");
            Assert.assertEquals(packageJson.getLanguageSpecVersion(), RepoUtils.getBallerinaSpecVersion());

            Assert.assertEquals(Paths.get(packageJson.getIcon()), Paths.get("docs/samplePng01.png"));
            Assert.assertTrue(balaExportPath.resolve(packageJson.getIcon()).toFile().exists());
        }

        // compiler-plugin.json
        Path compilerPluginJsonPath = balaExportPath.resolve("compiler-plugin").resolve("compiler-plugin.json");
        try (FileReader reader = new FileReader(String.valueOf(compilerPluginJsonPath))) {
            CompilerPluginJson compilerPluginJson = gson.fromJson(reader, CompilerPluginJson.class);
            Assert.assertEquals(compilerPluginJson.pluginId(), "openapi-validator");
            Assert.assertEquals(compilerPluginJson.pluginClass(), "io.ballerina.openapi.Validator");
            Assert.assertEquals(compilerPluginJson.dependencyPaths().size(), 1);
        }

        // bal-tool.json
        Path balToolJsonPath = balaExportPath.resolve("tool").resolve("bal-tool.json");
        try (FileReader reader = new FileReader(String.valueOf(balToolJsonPath))) {
            BalToolJson balToolJson = gson.fromJson(reader, BalToolJson.class);
            Assert.assertEquals(balToolJson.toolId(), "openapi");
            Assert.assertEquals(balToolJson.dependencyPaths().size(), 1);
        }

        // Check if compiler plugin dependencies exists
        Path compilerPluginDependency = balaExportPath.resolve("compiler-plugin").resolve("libs")
                .resolve("platform-io-1.3.0-java.txt");
        Assert.assertTrue(compilerPluginDependency.toFile().exists());

        // docs
        Path packageMdPath = balaExportPath.resolve("docs").resolve("Package.md");
        Assert.assertTrue(packageMdPath.toFile().exists());
        Path defaultModuleMdPath = balaExportPath
                .resolve("docs").resolve("modules").resolve("winery").resolve("Module.md");
        Assert.assertTrue(defaultModuleMdPath.toFile().exists());
        Path servicesModuleMdPath = balaExportPath.resolve("docs").resolve("modules").resolve("winery.services")
                .resolve("Module.md");
        Assert.assertTrue(servicesModuleMdPath.toFile().exists());
        Path storageModuleMdPath = balaExportPath.resolve("docs").resolve("modules").resolve("winery.storage")
                .resolve("Module.md");
        Assert.assertTrue(storageModuleMdPath.toFile().exists());
        // check icon
        Path iconPath = balaExportPath.resolve("docs").resolve("samplePng01.png");
        Assert.assertTrue(iconPath.toFile().exists());

        // check for includes
        Path defaultModuleIncludeJson = balaExportPath.resolve("include-file.json");
        Assert.assertTrue(defaultModuleIncludeJson.toFile().exists());
        Path defaultModuleIncludeFile = balaExportPath.resolve("default-module-include/file");
        Assert.assertTrue(defaultModuleIncludeFile.toFile().exists());
        Path defaultModuleIncludeTextFile = balaExportPath.resolve("default-module-include-dir/include_text_file.txt");
        Assert.assertTrue(defaultModuleIncludeTextFile.toFile().exists());
        Path defaultModuleIncludeImageFile = balaExportPath.resolve("default-module-include-dir/include_image.png");
        Assert.assertTrue(defaultModuleIncludeImageFile.toFile().exists());

        Path nonDefaultModuleIncludeFile = balaExportPath
                .resolve("modules/winery.services/non-default-module-include/file");
        Assert.assertTrue(nonDefaultModuleIncludeFile.toFile().exists());
        Path nonDefaultModuleIncludeTextFile = balaExportPath
                .resolve("modules/winery.services/non-default-module-include-dir/include_text_file.txt");
        Assert.assertTrue(nonDefaultModuleIncludeTextFile.toFile().exists());
        Path nonDefaultModuleIncludeImageFile = balaExportPath
                .resolve("modules/winery.services/non-default-module-include-dir/include_image.png");
        Assert.assertTrue(nonDefaultModuleIncludeImageFile.toFile().exists());

        // module sources
        // default module
        Path defaultModuleSrcPath = balaExportPath.resolve("modules").resolve("winery");
        Assert.assertTrue(defaultModuleSrcPath.toFile().exists());
        Assert.assertTrue(defaultModuleSrcPath.resolve(Paths.get("main.bal")).toFile().exists());
        Assert.assertTrue(defaultModuleSrcPath.resolve(Paths.get("utils.bal")).toFile().exists());
        Assert.assertTrue(defaultModuleSrcPath.resolve(Paths.get("resources")).toFile().exists());
        Assert.assertTrue(defaultModuleSrcPath.resolve(Paths.get("resources", "main.json")).toFile().exists());
        Assert.assertFalse(defaultModuleSrcPath.resolve("modules").toFile().exists());
        Assert.assertFalse(defaultModuleSrcPath.resolve("tests").toFile().exists());
        Assert.assertFalse(defaultModuleSrcPath.resolve("targets").toFile().exists());
        Assert.assertFalse(defaultModuleSrcPath.resolve("Package.md").toFile().exists());
        Assert.assertFalse(defaultModuleSrcPath.resolve("Ballerina.toml").toFile().exists());
        // other modules
        // storage module
        Path storageModuleSrcPath = balaExportPath.resolve("modules").resolve("winery.storage");
        Assert.assertTrue(storageModuleSrcPath.resolve("db.bal").toFile().exists());
        Assert.assertTrue(storageModuleSrcPath.resolve("resources").toFile().exists());
        Assert.assertTrue(storageModuleSrcPath.resolve("resources").resolve("db.json").toFile().exists());
        Assert.assertFalse(storageModuleSrcPath.resolve("tests").toFile().exists());
        Assert.assertFalse(storageModuleSrcPath.resolve("Module.md").toFile().exists());

        // Check if platform dependencies exists
        Path platformDependancy = balaExportPath.resolve("platform").resolve("java17")
                .resolve("ballerina-io-1.0.0-java.txt");
        Assert.assertTrue(platformDependancy.toFile().exists());

        // Check if test scoped platform dependencies not exists
        Path testScopePlatformDependancy = balaExportPath.resolve("platform").resolve("java17")
                .resolve("ballerina-io-1.2.0-java.txt");
        Assert.assertFalse(testScopePlatformDependancy.toFile().exists());

        // dependencies.json
        Path dependenciesJsonPath = balaExportPath.resolve(DEPENDENCY_GRAPH_JSON);
        Assert.assertTrue(dependenciesJsonPath.toFile().exists());

        try (FileReader reader = new FileReader(String.valueOf(dependenciesJsonPath))) {
            DependencyGraphJson dependencyGraphJson = gson.fromJson(reader, DependencyGraphJson.class);

            Assert.assertEquals(dependencyGraphJson.getPackageDependencyGraph().size(), 2);
            Dependency firstDependency = dependencyGraphJson.getPackageDependencyGraph().get(0);
            Dependency javaDependency;
            Dependency fooDependency;

            if (firstDependency.getOrg().equals("ballerina")) {
                javaDependency = firstDependency;
                fooDependency = dependencyGraphJson.getPackageDependencyGraph().get(1);
            } else {
                fooDependency = firstDependency;
                javaDependency = dependencyGraphJson.getPackageDependencyGraph().get(1);
            }

            Assert.assertEquals(javaDependency.getOrg(), "ballerina");
            Assert.assertEquals(javaDependency.getName(), "jballerina.java");

            Assert.assertEquals(fooDependency.getOrg(), "foo");
            Assert.assertEquals(fooDependency.getName(), "winery");
            Assert.assertEquals(fooDependency.getVersion(), "0.1.0");

            // foo has a dependency on java, assert foo's dependencies
            List<Dependency> fooDependencies = fooDependency.getDependencies();
            Assert.assertEquals(fooDependencies.size(), 1);
            Assert.assertEquals(fooDependencies.get(0).getOrg(), "ballerina");
            Assert.assertEquals(fooDependencies.get(0).getName(), "jballerina.java");


            List<ModuleDependency> moduleDependencyGraph = dependencyGraphJson.getModuleDependencies();
            Assert.assertEquals(moduleDependencyGraph.size(), 3);

            List<String> moduleNames = new ArrayList<>(Arrays.asList("winery", "winery.services", "winery.storage"));
            for (ModuleDependency moduleDependency : moduleDependencyGraph) {
                if (!moduleNames.contains(moduleDependency.getModuleName())) {
                    Assert.fail("invalid module:" + moduleDependency.getModuleName());
                }
            }
        }
    }

    @Test
    public void testBalaWriterWithMinimalBalProject(ITestContext ctx) throws IOException {
        Gson gson = new Gson();
        Path projectPath = BALA_WRITER_RESOURCES.resolve("projectTwo");
        ctx.getCurrentXmlTest().addParameter(PACKAGE_PATH, String.valueOf(projectPath));
        Project project = TestUtils.loadBuildProject(projectPath);

        PackageCompilation packageCompilation = project.currentPackage().getCompilation();
        Target target = new Target(project.sourceRoot());

        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_17);
        jBallerinaBackend.emit(JBallerinaBackend.OutputType.BALA, target.getBalaPath());

        // invoke write bala method
        jBallerinaBackend.emit(JBallerinaBackend.OutputType.BALA, target.getBalaPath());

        // unzip bala
        TestUtils.unzip(String.valueOf(target.getBalaPath().resolve("bar-winery-any-0.1.0.bala")),
                        String.valueOf(balaExportPath));

        // bala.json
        Path balaJsonPath = balaExportPath.resolve("bala.json");
        Assert.assertTrue(balaJsonPath.toFile().exists());

        try (FileReader reader = new FileReader(String.valueOf(balaJsonPath))) {
            BalaJson balaJson = gson.fromJson(reader, BalaJson.class);
            Assert.assertEquals(balaJson.getBala_version(), "2.0.0");
            Assert.assertEquals(balaJson.getBuilt_by(), "WSO2");
        }

        // package.json
        Path packageJsonPath = balaExportPath.resolve("package.json");
        Assert.assertTrue(packageJsonPath.toFile().exists());

        try (FileReader reader = new FileReader(String.valueOf(packageJsonPath))) {
            PackageJson packageJson = gson.fromJson(reader, PackageJson.class);
            Assert.assertEquals(packageJson.getOrganization(), "bar");
            Assert.assertEquals(packageJson.getName(), "winery");
            Assert.assertEquals(packageJson.getVersion(), "0.1.0");
            //        Assert.assertEquals(packageJson.getBallerinaVersion(), "unknown");
        }

        // docs should not exists
        Assert.assertFalse(balaExportPath.resolve("docs").toFile().exists());

        // module sources
        Path defaultModuleSrcPath = balaExportPath.resolve("modules").resolve("winery");
        Assert.assertTrue(defaultModuleSrcPath.toFile().exists());
        Assert.assertTrue(defaultModuleSrcPath.resolve(Paths.get("main.bal")).toFile().exists());
    }

    @Test
    public void testBalaWriterWithProvidedPlatformLibs(ITestContext ctx) throws IOException {
        Gson gson = new Gson();
        Path projectPath = BALA_WRITER_RESOURCES.resolve("projectProvidedScope");
        ctx.getCurrentXmlTest().addParameter(PACKAGE_PATH, String.valueOf(projectPath));
        Project project = TestUtils.loadBuildProject(projectPath);

        PackageCompilation packageCompilation = project.currentPackage().getCompilation();
        if (packageCompilation.diagnosticResult().hasErrors()) {
            Assert.fail("compilation failed:" + packageCompilation.diagnosticResult().errors());
        }

        Target target = new Target(project.sourceRoot());
        Path balaPath = target.getBalaPath();
        // invoke write bala method
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_17);
        EmitResult emitResult = jBallerinaBackend.emit(JBallerinaBackend.OutputType.BALA, balaPath);
        Assert.assertTrue(emitResult.successful());

        // unzip bala
        TestUtils.unzip(String.valueOf(balaPath.resolve("foo-pkg_a-java17-1.0.0.bala")),
                String.valueOf(balaExportPath));

        // package.json
        Path packageJsonPath = balaExportPath.resolve("package.json");
        Assert.assertTrue(packageJsonPath.toFile().exists());
        try (FileReader reader = new FileReader(String.valueOf(packageJsonPath))) {
            PackageJson packageJson = gson.fromJson(reader, PackageJson.class);
            Assert.assertEquals(packageJson.getPlatform(), "java17");
            JsonObject foundInBala = packageJson.getPlatformDependencies().get(0).getAsJsonObject();
            JsonObject expected = new JsonObject();
            expected.addProperty("artifactId", "project1");
            expected.addProperty("groupId", "com.example");
            expected.addProperty("version", "1.0");
            expected.addProperty("scope", "provided");
            Assert.assertEquals(packageJson.getPlatformDependencies().size(), 1);
            Assert.assertEquals(foundInBala, expected);
        }
        // Check if test scoped platform dependencies not exists
        Path providedScopePlatformDependancy = balaExportPath.resolve("platform").resolve("java17")
                .resolve("project1-1.0.0.jar");
        Assert.assertFalse(providedScopePlatformDependancy.toFile().exists());
    }

    @Test
    public void testBalaWriterWithToolProject(ITestContext ctx) throws IOException {
        Gson gson = new Gson();
        Path projectPath = BALA_WRITER_RESOURCES.resolve("projectTool");
        ctx.getCurrentXmlTest().addParameter(PACKAGE_PATH, String.valueOf(projectPath));
        Project project = TestUtils.loadBuildProject(projectPath);

        PackageCompilation packageCompilation = project.currentPackage().getCompilation();
        Target target = new Target(project.sourceRoot());

        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_17);
        jBallerinaBackend.emit(JBallerinaBackend.OutputType.BALA, target.getBalaPath());

        // invoke write bala method
        jBallerinaBackend.emit(JBallerinaBackend.OutputType.BALA, target.getBalaPath());

        // unzip bala
        TestUtils.unzip(String.valueOf(target.getBalaPath().resolve("foo-tool_test-java17-1.0.1.bala")),
                String.valueOf(balaExportPath));

        // bala.json
        Path balaJsonPath = balaExportPath.resolve("bala.json");
        Assert.assertTrue(balaJsonPath.toFile().exists());

        try (FileReader reader = new FileReader(String.valueOf(balaJsonPath))) {
            BalaJson balaJson = gson.fromJson(reader, BalaJson.class);
            Assert.assertEquals(balaJson.getBala_version(), "2.0.0");
            Assert.assertEquals(balaJson.getBuilt_by(), "WSO2");
        }

        // package.json
        Path packageJsonPath = balaExportPath.resolve("package.json");
        Assert.assertTrue(packageJsonPath.toFile().exists());

        try (FileReader reader = new FileReader(String.valueOf(packageJsonPath))) {
            PackageJson packageJson = gson.fromJson(reader, PackageJson.class);
            Assert.assertEquals(packageJson.getOrganization(), "foo");
            Assert.assertEquals(packageJson.getName(), "tool_test");
            Assert.assertEquals(packageJson.getVersion(), "1.0.1");
        }

        // bal-tool.json
        Path balToolJsonPath = balaExportPath.resolve("tool").resolve("bal-tool.json");
        try (FileReader reader = new FileReader(String.valueOf(balToolJsonPath))) {
            BalToolJson balToolJson = gson.fromJson(reader, BalToolJson.class);
            Assert.assertEquals(balToolJson.toolId(), "tool_test");
            Assert.assertEquals(balToolJson.dependencyPaths().size(), 1);
        }

        // module sources
        Path defaultModuleSrcPath = balaExportPath.resolve("modules").resolve("tool_test");
        Assert.assertTrue(defaultModuleSrcPath.toFile().exists());
        Path mainFilePath = defaultModuleSrcPath.resolve(Paths.get("main.bal"));
        Assert.assertTrue(mainFilePath.toFile().exists());
        String expectedMainContent = """
                // AUTO-GENERATED FILE.

                // This file is auto-generated by Ballerina for packages with empty default modules.\s
                """;
        Assert.assertEquals(Files.readString(mainFilePath), expectedMainContent);
    }

    @Test
    public void testBalaWriterWithTwoDirectDependencies(ITestContext ctx) throws IOException {
        Gson gson = new Gson();
        // package_d --> package_b --> package_c
        // package_d --> package_e
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("projects_for_resolution_tests").resolve("package_d");
        ctx.getCurrentXmlTest().addParameter(PACKAGE_PATH, String.valueOf(projectDirPath));
        BuildProject project = TestUtils.loadBuildProject(projectDirPath);

        PackageCompilation packageCompilation = project.currentPackage().getCompilation();
        Target target = new Target(project.sourceRoot());
        // invoke write bala method
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_17);
        jBallerinaBackend.emit(JBallerinaBackend.OutputType.BALA, target.getBalaPath());

        // unzip bala
        TestUtils.unzip(String.valueOf(target.getBalaPath().resolve("samjs-package_d-any-0.1.0.bala")),
                        String.valueOf(balaExportPath));

        // bala.json
        Path balaJsonPath = balaExportPath.resolve("bala.json");
        Assert.assertTrue(balaJsonPath.toFile().exists());

        try (FileReader reader = new FileReader(String.valueOf(balaJsonPath))) {
            BalaJson balaJson = gson.fromJson(reader, BalaJson.class);
            Assert.assertEquals(balaJson.getBala_version(), "2.0.0");
            Assert.assertEquals(balaJson.getBuilt_by(), "WSO2");
        }

        // package.json
        Path packageJsonPath = balaExportPath.resolve("package.json");
        Assert.assertTrue(packageJsonPath.toFile().exists());

        try (FileReader reader = new FileReader(String.valueOf(packageJsonPath))) {
            PackageJson packageJson = gson.fromJson(reader, PackageJson.class);
            Assert.assertEquals(packageJson.getOrganization(), "samjs");
            Assert.assertEquals(packageJson.getName(), "package_d");
            Assert.assertEquals(packageJson.getVersion(), "0.1.0");
            Assert.assertEquals(packageJson.getPlatform(), "any");
        }

        // module sources
        // default module
        Path defaultModuleSrcPath = balaExportPath.resolve("modules").resolve("package_d");
        Assert.assertTrue(defaultModuleSrcPath.toFile().exists());
        Assert.assertTrue(defaultModuleSrcPath.resolve(Paths.get("main.bal")).toFile().exists());

        // dependencies.json
        Path dependencyGraphJsonPath = balaExportPath.resolve(DEPENDENCY_GRAPH_JSON);
        Assert.assertTrue(dependencyGraphJsonPath.toFile().exists());

        try (FileReader reader = new FileReader(String.valueOf(dependencyGraphJsonPath))) {
            DependencyGraphJson dependencyGraphJson = gson.fromJson(reader, DependencyGraphJson.class);

            List<Dependency> packageDependencyGraph = dependencyGraphJson.getPackageDependencyGraph();
            Assert.assertEquals(packageDependencyGraph.size(), 4);
            for (Dependency dependency : packageDependencyGraph) {
                if (dependency.getName().equals("package_d")) {
                    Assert.assertEquals(dependency.getDependencies().size(), 2);
                    for (Dependency dep : dependency.getDependencies()) {
                        if (!(dep.getName().equals("package_b") || dep.getName().equals("package_e"))) {
                            Assert.fail("invalid dependency:" + dep.getName());
                        }
                    }
                }
            }

            List<ModuleDependency> moduleDependencyGraph = dependencyGraphJson.getModuleDependencies();
            Assert.assertEquals(moduleDependencyGraph.get(0).getModuleName(), "package_d");
        }
    }

    @Test(enabled = false, expectedExceptions = AccessDeniedException.class,
            expectedExceptionsMessageRegExp = "No write access to create bala:.*")
    public void testBalaWriterAccessDenied(ITestContext ctx) {

        Path balaPath = mock(Path.class);
        File file = mock(File.class);
        when(file.canWrite()).thenReturn(false);
        when(file.isDirectory()).thenReturn(true);
        when(balaPath.toFile()).thenReturn(file);

        Path projectPath = BALA_WRITER_RESOURCES.resolve("projectTwo");
        ctx.getCurrentXmlTest().addParameter(PACKAGE_PATH, String.valueOf(projectPath));
        Project project = TestUtils.loadBuildProject(projectPath);

        PackageCompilation packageCompilation = project.currentPackage().getCompilation();
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_17);
        jBallerinaBackend.emit(JBallerinaBackend.OutputType.BALA, balaPath);

//        // invoke write bala method
//        BalaWriter.write(project.currentPackage(), balaPath);
    }

    @Test(description = "tests build project with a valid icon in Ballerina.toml")
    public void testBuildProjectWithValidIcon(ITestContext ctx) throws IOException {
        Path packagePath = BALA_WRITER_RESOURCES.resolve("projectWithValidIcon");
        ctx.getCurrentXmlTest().addParameter(PACKAGE_PATH, String.valueOf(packagePath));

        BuildProject buildProject = BuildProject.load(packagePath);
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();

        Target target = new Target(buildProject.sourceRoot());
        // invoke write bala method
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_17);
        jBallerinaBackend.emit(JBallerinaBackend.OutputType.BALA, target.getBalaPath());

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 0, "Unexpected compilation diagnostics");

        // unzip bala
        TestUtils.unzip(String.valueOf(target.getBalaPath().resolve("sameera-myproject-any-0.1.0.bala")),
                String.valueOf(balaExportPath));
        // Check icon is added to `bala/docs` directory
        Assert.assertTrue(balaExportPath.resolve(BALA_DOCS_DIR).resolve("samplePng01.png").toFile().exists());
    }

    @Test(description = "tests build project with an invalid svg icon renamed as png")
    public void testBuildProjectWithInvalidIcon(ITestContext ctx) {
        Path packagePath = BALA_WRITER_RESOURCES.resolve("projectWithInvalidIcon");
        ctx.getCurrentXmlTest().addParameter(PACKAGE_PATH, String.valueOf(packagePath));

        BuildProject buildProject = BuildProject.load(packagePath);
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 1);
        Assert.assertEquals(diagnosticResult.diagnostics().iterator().next().message(),
                "invalid 'icon' under [package]: 'icon' can only have 'png' images");
    }

    @Test(description = "tests build project with different include patterns")
    public void testBuildProjectWithIncludes(ITestContext ctx) throws IOException {
        Gson gson = new Gson();
        Path packagePath = BALA_WRITER_RESOURCES.resolve("projectWithInclude");
        ctx.getCurrentXmlTest().addParameter(PACKAGE_PATH, String.valueOf(packagePath));

        BuildProject buildProject = BuildProject.load(packagePath);
        PackageCompilation packageCompilation = buildProject.currentPackage().getCompilation();

        Target target = new Target(buildProject.sourceRoot());
        Path balaPath = target.getBalaPath();

        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_17);
        EmitResult emitResult = jBallerinaBackend.emit(JBallerinaBackend.OutputType.BALA, balaPath);
        Assert.assertTrue(emitResult.successful());

        TestUtils.unzip(String.valueOf(balaPath.resolve("foo-include_test-any-0.1.0.bala")),
                String.valueOf(balaExportPath));

        Path packageJsonPath = balaExportPath.resolve("package.json");
        Assert.assertTrue(packageJsonPath.toFile().exists());

        try (FileReader reader = new FileReader(String.valueOf(packageJsonPath))) {
            PackageJson packageJson = gson.fromJson(reader, PackageJson.class);
            Assert.assertFalse(packageJson.getInclude().isEmpty());
            Assert.assertEquals(packageJson.getInclude().get(0), "foo");
            Assert.assertEquals(packageJson.getInclude().get(1), "/bar");
            Assert.assertEquals(packageJson.getInclude().get(2), "baz/");
            Assert.assertEquals(packageJson.getInclude().get(3), "/qux/");
            Assert.assertEquals(packageJson.getInclude().get(4), "/quux/");
            Assert.assertEquals(packageJson.getInclude().get(5), "*.html");
            Assert.assertEquals(packageJson.getInclude().get(6), "foo*bar.*");
            Assert.assertEquals(packageJson.getInclude().get(7), "plug?");
            Assert.assertEquals(packageJson.getInclude().get(8), "thud[ab]");
            Assert.assertEquals(packageJson.getInclude().get(9), "fred[q-s]");
            Assert.assertEquals(packageJson.getInclude().get(10), "**/grault/garply");
            Assert.assertEquals(packageJson.getInclude().get(11), "waldo/xyzzy/**");
            Assert.assertEquals(packageJson.getInclude().get(12), "babble/**/bar");
            Assert.assertEquals(packageJson.getInclude().get(13), "*.rs");
            Assert.assertEquals(packageJson.getInclude().get(14), "!corge.rs");
            Assert.assertEquals(packageJson.getInclude().get(15), "include-resources/thud");
            Assert.assertEquals(packageJson.getInclude().get(16), "include-resources/x.js");
        }

        // foo
        Path simplePatternDirInDefaultModule = balaExportPath.resolve("foo/temp.txt");
        Assert.assertTrue(simplePatternDirInDefaultModule.toFile().exists());
        Path simplePatternFileInDefaultModule = balaExportPath.resolve("include-resources/foo");
        Assert.assertTrue(simplePatternFileInDefaultModule.toFile().exists());
        Path simplePatternDirInNonDefaultModule = balaExportPath
                .resolve("modules/include_test.services/foo/temp.txt");
        Assert.assertTrue(simplePatternDirInNonDefaultModule.toFile().exists());
        Path simplePatternFileInNonDefaultModule = balaExportPath
                .resolve("modules/include_test.services/include-resources/foo");
        Assert.assertTrue(simplePatternFileInNonDefaultModule.toFile().exists());

        // /bar
        Path rootOnlyPatternFileInRoot = balaExportPath.resolve("bar");
        Assert.assertTrue(rootOnlyPatternFileInRoot.toFile().exists());
        Path rootOnlyPatternFileNotInRoot = balaExportPath.resolve("include-resources/bar");
        Assert.assertFalse(rootOnlyPatternFileNotInRoot.toFile().exists());
        Path rootOnlyPatternFileInNonDefaultModule = balaExportPath
                .resolve("modules/include_test.services/include-resources/bar/temp.txt");
        Assert.assertFalse(rootOnlyPatternFileInNonDefaultModule.toFile().exists());

        // baz/
        Path dirOnlyPatternDir = balaExportPath.resolve("include-resources/baz");
        Assert.assertTrue(dirOnlyPatternDir.toFile().exists());
        Path dirOnlyPatternFile = balaExportPath.resolve("include-resources2/baz");
        Assert.assertFalse(dirOnlyPatternFile.toFile().exists());

        // /qux/, /quux/
        Path rootOnlyDirOnlyPatternDirInRoot = balaExportPath.resolve("qux/temp.txt");
        Assert.assertTrue(rootOnlyDirOnlyPatternDirInRoot.toFile().exists());
        Path rootOnlyDirOnlyPatternDirNotInRoot = balaExportPath.resolve("include-resources/qux/temp.txt");
        Assert.assertFalse(rootOnlyDirOnlyPatternDirNotInRoot.toFile().exists());
        Path rootOnlyDirOnlyPatternFileInRoot = balaExportPath.resolve("quux");
        Assert.assertFalse(rootOnlyDirOnlyPatternFileInRoot.toFile().exists());
        Path rootOnlyDirOnlyPatternFileNotInRoot = balaExportPath.resolve("include-resources/quux");
        Assert.assertFalse(rootOnlyDirOnlyPatternFileNotInRoot.toFile().exists());

        // *.html
        Path starPatternFileMatchingExt = balaExportPath.resolve("include-resources/temp.html");
        Assert.assertTrue(starPatternFileMatchingExt.toFile().exists());
        Path starPatternFileNotMatchingExt = balaExportPath.resolve("include-resources/html.txt");
        Assert.assertFalse(starPatternFileNotMatchingExt.toFile().exists());
        Path starPatternDirMatchingExt = balaExportPath.resolve("include-resources/html/temp.txt");
        Assert.assertFalse(starPatternDirMatchingExt.toFile().exists());

        // foo*bar.*
        Path starPatternFile1 = balaExportPath.resolve("include-resources/foobar.txt");
        Assert.assertTrue(starPatternFile1.toFile().exists());
        Path starPatternFile2 = balaExportPath.resolve("include-resources/foobazbar.txt");
        Assert.assertTrue(starPatternFile2.toFile().exists());

        // plug?
        Path anySingleCharPatternMatchingFile = balaExportPath.resolve("include-resources2/plugs");
        Assert.assertTrue(anySingleCharPatternMatchingFile.toFile().exists());
        Path anySingleCharPatternNotMatchingFile1 = balaExportPath.resolve("include-resources2/plug");
        Assert.assertFalse(anySingleCharPatternNotMatchingFile1.toFile().exists());
        Path anySingleCharPatternNotMatchingFile2 = balaExportPath.resolve("include-resources2/plugged");
        Assert.assertFalse(anySingleCharPatternNotMatchingFile2.toFile().exists());

        // thud[ab]
        Path rangePatternMatchingFile1 = balaExportPath.resolve("include-resources2/range/thuda");
        Assert.assertTrue(rangePatternMatchingFile1.toFile().exists());
        Path rangePatternMatchingFile2 = balaExportPath.resolve("include-resources2/range/thudb");
        Assert.assertTrue(rangePatternMatchingFile2.toFile().exists());
        Path rangePatternNotMatchingFile3 = balaExportPath.resolve("include-resources2/range/thudc");
        Assert.assertFalse(rangePatternNotMatchingFile3.toFile().exists());

        // fred[q-s]
        Path rangePatternNotMatchingFile4 = balaExportPath.resolve("include-resources2/range/fredp");
        Assert.assertFalse(rangePatternNotMatchingFile4.toFile().exists());
        Path rangePatternMatchingFile5 = balaExportPath.resolve("include-resources2/range/fredq");
        Assert.assertTrue(rangePatternMatchingFile5.toFile().exists());
        Path rangePatternMatchingFile6 = balaExportPath.resolve("include-resources2/range/fredr");
        Assert.assertTrue(rangePatternMatchingFile6.toFile().exists());
        Path rangePatternMatchingFile7 = balaExportPath.resolve("include-resources2/range/freds");
        Assert.assertTrue(rangePatternMatchingFile7.toFile().exists());
        Path rangePatternNotMatchingFile8 = balaExportPath.resolve("include-resources2/range/fredt");
        Assert.assertFalse(rangePatternNotMatchingFile8.toFile().exists());

        // **/grault/garply
        Path doubleStarAtStartPatternDirInRoot = balaExportPath.resolve("grault/garply/temp.txt");
        Assert.assertTrue(doubleStarAtStartPatternDirInRoot.toFile().exists());
        Path doubleStarAtStartPatternDirNotInRoot = balaExportPath.resolve("include-resources/grault/garply/temp.txt");
        Assert.assertTrue(doubleStarAtStartPatternDirNotInRoot.toFile().exists());

        // waldo/xyzzy/**
        Path doubleStarAtEndPatternDirInRoot = balaExportPath.resolve("waldo/xyzzy/temp.txt");
        Assert.assertTrue(doubleStarAtEndPatternDirInRoot.toFile().exists());
        Path doubleStarAtEndPatternDirNotInRoot = balaExportPath.resolve("include-resources/waldo/xyzzy/temp.txt");
        Assert.assertTrue(doubleStarAtEndPatternDirNotInRoot.toFile().exists());

        // babble/**/bar
        Path doubleStarInMiddlePatternFile = balaExportPath.resolve("include-resources/babble/fuu/bar");
        Assert.assertTrue(doubleStarInMiddlePatternFile.toFile().exists());

        // *.rs - include all files with extension .rs
        // !corge.rs - exclude only corge.rs
        Path includeRsExtPatternIncludedFile1 = balaExportPath.resolve("include-resources/wombat.rs");
        Assert.assertTrue(includeRsExtPatternIncludedFile1.toFile().exists());
        Path includeRsExtPatternIncludedFile2 = balaExportPath.resolve("include-resources/garply.rs");
        Assert.assertTrue(includeRsExtPatternIncludedFile2.toFile().exists());
        Path includeRsExtPatternExcludedFile = balaExportPath.resolve("include-resources/corge.rs");
        Assert.assertFalse(includeRsExtPatternExcludedFile.toFile().exists());

        // exact file paths
        // include-resources/thud
        // include-resources/x.js
        Path exactPathPatternDir = balaExportPath.resolve("include-resources/thud/temp.txt");
        Assert.assertTrue(exactPathPatternDir.toFile().exists());
        Path exactPathPatternFile = balaExportPath.resolve("include-resources/x.js");
        Assert.assertTrue(exactPathPatternFile.toFile().exists());

        // patterns that include the same file twice
        // test the handling of ZipException thrown from putZipEntry when the same file is included twice
        // hoge/, hoge/y
        Path patternOverlapFile = balaExportPath.resolve("include-resources/hoge/y");
        Assert.assertTrue(patternOverlapFile.toFile().exists());
    }

    @AfterMethod(alwaysRun = true)
    public void cleanup(ITestContext ctx) {
        ProjectUtils.deleteDirectory(this.tmpDir);
        String pkgPathParam = ctx.getCurrentXmlTest().getParameter(PACKAGE_PATH);
        if (pkgPathParam == null) {
            return;
        }
        Path packagePath = Path.of(pkgPathParam);
        ProjectUtils.deleteDirectory(packagePath.resolve(TARGET_DIR_NAME));
        ProjectUtils.deleteDirectory(packagePath.resolve(BALA_DIR_NAME));
    }
}
