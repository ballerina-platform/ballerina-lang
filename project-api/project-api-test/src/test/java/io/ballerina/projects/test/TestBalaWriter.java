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
import io.ballerina.projects.EmitResult;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.internal.bala.BalaJson;
import io.ballerina.projects.internal.bala.CompilerPluginJson;
import io.ballerina.projects.internal.bala.DependencyGraphJson;
import io.ballerina.projects.internal.bala.ModuleDependency;
import io.ballerina.projects.internal.bala.PackageJson;
import io.ballerina.projects.internal.model.Dependency;
import io.ballerina.projects.internal.model.Target;
import io.ballerina.projects.util.ProjectUtils;
import org.testng.Assert;
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

import static io.ballerina.projects.util.ProjectConstants.DEPENDENCY_GRAPH_JSON;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Contains cases to test the bala writer.
 *
 * @since 2.0.0
 */
public class TestBalaWriter {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src", "test", "resources");
    private Path tmpDir;
    private Path balaExportPath;

    @BeforeMethod
    public void setUp() throws IOException {
        this.tmpDir = Files.createTempDirectory("b7a-bala-writer-test-" + System.nanoTime());
        this.balaExportPath = this.tmpDir.resolve("tmpBalaDir");
        Files.createDirectory(Paths.get(String.valueOf(this.balaExportPath)));
    }

    @Test
    public void testBalaWriter() throws IOException {
        Gson gson = new Gson();
        Path projectPath = RESOURCE_DIRECTORY.resolve("balawriter").resolve("projectOne");
        Project project = TestUtils.loadBuildProject(projectPath);

        PackageCompilation packageCompilation = project.currentPackage().getCompilation();
        if (packageCompilation.diagnosticResult().hasErrors()) {
            Assert.fail("compilation failed:" + packageCompilation.diagnosticResult().errors());
        }

        Target target = new Target(project.sourceRoot());
        Path balaPath = target.getBalaPath();
        // invoke write bala method
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_11);
        EmitResult emitResult = jBallerinaBackend.emit(JBallerinaBackend.OutputType.BALA, balaPath);
        Assert.assertTrue(emitResult.successful());

        // unzip bala
        TestUtils.unzip(String.valueOf(balaPath.resolve("foo-winery-java11-0.1.0.bala")),
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

            Assert.assertEquals(packageJson.getVisibility(), "private");

            Assert.assertEquals(packageJson.getPlatform(), "java11");
            Assert.assertEquals(packageJson.getPlatformDependencies().size(), 1);

            Assert.assertEquals(packageJson.getBallerinaVersion(), RepoUtils.getBallerinaShortVersion());
            Assert.assertEquals(packageJson.getImplementationVendor(), "WSO2");
            Assert.assertEquals(packageJson.getLanguageSpecVersion(), RepoUtils.getBallerinaSpecVersion());

            Assert.assertEquals(Paths.get(packageJson.getIcon()), Paths.get("docs/sample.svg"));
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
        Path iconPath = balaExportPath.resolve("docs").resolve("sample.svg");
        Assert.assertTrue(iconPath.toFile().exists());

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
        Path platformDependancy = balaExportPath.resolve("platform").resolve("java11")
                .resolve("ballerina-io-1.0.0-java.txt");
        Assert.assertTrue(platformDependancy.toFile().exists());

        // Check if test scoped platform dependencies not exists
        Path testScopePlatformDependancy = balaExportPath.resolve("platform").resolve("java11")
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
    public void testBalaWriterWithMinimalBalProject() throws IOException {
        Gson gson = new Gson();
        Path projectPath = RESOURCE_DIRECTORY.resolve("balawriter").resolve("projectTwo");
        Project project = TestUtils.loadBuildProject(projectPath);

        PackageCompilation packageCompilation = project.currentPackage().getCompilation();
        Target target = new Target(project.sourceRoot());

        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_11);
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
    public void testBalaWriterWithTwoDirectDependencies() throws IOException {
        Gson gson = new Gson();
        // package_d --> package_b --> package_c
        // package_d --> package_e
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("projects_for_resolution_tests").resolve("package_d");
        BuildProject project = TestUtils.loadBuildProject(projectDirPath);

        PackageCompilation packageCompilation = project.currentPackage().getCompilation();
        Target target = new Target(project.sourceRoot());
        // invoke write bala method
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_11);
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
    public void testBalaWriterAccessDenied() {

        Path balaPath = mock(Path.class);
        File file = mock(File.class);
        when(file.canWrite()).thenReturn(false);
        when(file.isDirectory()).thenReturn(true);
        when(balaPath.toFile()).thenReturn(file);

        Path projectPath = RESOURCE_DIRECTORY.resolve("balawriter").resolve("projectTwo");
        Project project = TestUtils.loadBuildProject(projectPath);

        PackageCompilation packageCompilation = project.currentPackage().getCompilation();
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_11);
        jBallerinaBackend.emit(JBallerinaBackend.OutputType.BALA, balaPath);

//        // invoke write bala method
//        BalaWriter.write(project.currentPackage(), balaPath);
    }

    @AfterMethod(alwaysRun = true)
    public void cleanup() {
        ProjectUtils.deleteDirectory(this.tmpDir);
    }
}
