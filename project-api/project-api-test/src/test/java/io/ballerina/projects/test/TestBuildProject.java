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

import com.google.gson.JsonObject;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.projects.BallerinaToml;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.CloudToml;
import io.ballerina.projects.CompilerPluginToml;
import io.ballerina.projects.DependenciesToml;
import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentConfig;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.EmitResult;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JarLibrary;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleConfig;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageResolution;
import io.ballerina.projects.PlatformLibrary;
import io.ballerina.projects.PlatformLibraryScope;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.ResourceConfig;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.projects.internal.model.BuildJson;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.toml.semantic.ast.TomlTableArrayNode;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.LinePosition;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static io.ballerina.projects.test.TestUtils.isWindows;
import static io.ballerina.projects.test.TestUtils.loadProject;
import static io.ballerina.projects.test.TestUtils.readFileAsString;
import static io.ballerina.projects.test.TestUtils.resetPermissions;
import static io.ballerina.projects.test.TestUtils.writeContent;
import static io.ballerina.projects.util.ProjectConstants.BALLERINA_TOML;
import static io.ballerina.projects.util.ProjectConstants.BUILD_FILE;
import static io.ballerina.projects.util.ProjectConstants.CACHES_DIR_NAME;
import static io.ballerina.projects.util.ProjectConstants.DEPENDENCIES_TOML;
import static io.ballerina.projects.util.ProjectConstants.MODULES_ROOT;
import static io.ballerina.projects.util.ProjectConstants.REPO_BIR_CACHE_NAME;
import static io.ballerina.projects.util.ProjectConstants.RESOURCE_DIR_NAME;
import static io.ballerina.projects.util.ProjectConstants.TARGET_DIR_NAME;
import static io.ballerina.projects.util.ProjectConstants.USER_NAME;
import static io.ballerina.projects.util.ProjectUtils.readBuildJson;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Contains cases to test the basic package structure.
 *
 * @since 2.0.0
 */
public class TestBuildProject extends BaseTest {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/");
    private static Path tempResourceDir;
    static final PrintStream OUT = System.out;
    private final String dummyContent = "function foo() {\n}";

    @BeforeClass
    public void setup() throws IOException {
        tempResourceDir = Files.createTempDirectory("project-api-test");
        FileUtils.copyDirectory(RESOURCE_DIRECTORY.resolve("project_no_permission").toFile(),
                tempResourceDir.resolve("project_no_permission").toFile());
    }
    @Test (description = "tests loading a valid build project")
    public void testBuildProjectAPI() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject");

        // 1) Initialize the project instance
        BuildProject project = loadBuildProject(projectPath);
        // 2) Load the package
        Package currentPackage = project.currentPackage();
        // 3) Load the default module
        Module defaultModule = currentPackage.getDefaultModule();
        Assert.assertEquals(defaultModule.documentIds().size(), 2);

        // 4) Get Ballerina.toml file
        Optional<BallerinaToml> ballerinaTomlOptional = currentPackage.ballerinaToml();
        Assert.assertTrue(ballerinaTomlOptional.isPresent());

        // TODO find an easy way to test the project structure. e.g. serialize the structure in a json file.
        int noOfSrcDocuments = 0;
        int noOfTestDocuments = 0;
        final Collection<ModuleId> moduleIds = currentPackage.moduleIds();
        Assert.assertEquals(moduleIds.size(), 3);
        for (ModuleId moduleId : moduleIds) {
            Module module = currentPackage.module(moduleId);
            for (DocumentId documentId : module.documentIds()) {
                noOfSrcDocuments++;
            }
            for (DocumentId testDocumentId : module.testDocumentIds()) {
                noOfTestDocuments++;
            }
            for (DocumentId docId : module.documentIds()) {
                Assert.assertNotNull(module.document(docId).syntaxTree());
            }
        }

        Assert.assertEquals(noOfSrcDocuments, 4);
        Assert.assertEquals(noOfTestDocuments, 3);

    }

    @Test (description = "tests loading a build project containing invalid platformdependency paths")
    public void testBuildProjectWithInvalidDependencyPaths() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject_invalidDependencyPath");

        // 1) Initialize the project instance
        BuildProject project = loadBuildProject(projectPath);
        // 2) Load the package
        Package currentPackage = project.currentPackage();
        // 3) Load the default module
        Module defaultModule = currentPackage.getDefaultModule();
        Assert.assertEquals(defaultModule.documentIds().size(), 1);

        PackageCompilation packageCompilation = project.currentPackage().getCompilation();
        Assert.assertEquals(packageCompilation.diagnosticResult().errorCount(), 1);
        Assert.assertEquals(packageCompilation.diagnosticResult().errors().stream().findFirst().get().toString(),
                "ERROR [Ballerina.toml:(4:1,4:44)] " +
                        "could not locate dependency path './libs/ballerina-io-1.0.0-java.txt'");
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_11);
        Assert.assertEquals(jBallerinaBackend.diagnosticResult().errorCount(), 1);

        EmitResult emitResult = jBallerinaBackend.emit(JBallerinaBackend.OutputType.EXEC, Paths.get("test.jar"));
        Assert.assertFalse(emitResult.successful());

        emitResult = jBallerinaBackend.emit(JBallerinaBackend.OutputType.BALA, projectPath);
        Assert.assertFalse(emitResult.successful());
    }

    @Test (description = "tests loading a build project with no read permission")
    public void testBuildProjectWithNoReadPermission() {
        // Skip test in windows due to file permission setting issue
        if (isWindows()) {
            throw new SkipException("Skipping tests on Windows");
        }

        Path projectPath = tempResourceDir.resolve("project_no_permission");

        // 1) Remove read permission
        boolean readable = projectPath.toFile().setReadable(false, true);
        if (!readable) {
            Assert.fail("could not remove read permission");
        }

        // 2) Initialize the project instance
        BuildProject project = null;
        try {
            project = TestUtils.loadBuildProject(projectPath);
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("does not have read permissions"));
        }

        resetPermissions(projectPath);
    }

    @Test (description = "tests compiling a build project with no write permission",
            expectedExceptions = RuntimeException.class)
    public void testBuildProjectWithNoWritePermission() {

        // Skip test in windows due to file permission setting issue
        if (isWindows()) {
            throw new SkipException("Skipping tests on Windows");
        }

        Path projectPath = tempResourceDir.resolve("project_no_permission");

        // 1) Remove write permission
        boolean writable = projectPath.toFile().setWritable(false, true);
        if (!writable) {
            Assert.fail("could not remove write permission");
        }

        // 2) Initialize the project instance
        BuildProject project = null;
        try {
            project = TestUtils.loadBuildProject(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        // 3) Load the package
        Package currentPackage = project.currentPackage();
        Collection<ResolvedPackageDependency> resolvedPackageDependencies
                = currentPackage.getResolution().allDependencies();

        // 4) Compile the current package
        PackageCompilation compilation = currentPackage.getCompilation();
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_11);

        resetPermissions(projectPath);
    }

    @Test(description = "tests package compilation")
    public void testPackageCompilation() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("test_proj_pkg_compilation");

        // 1) Initialize the project instance
        BuildProject project = loadBuildProject(projectPath);
        // 2) Load the package
        Package currentPackage = project.currentPackage();

        // 3) Compile the current package
        PackageCompilation compilation = currentPackage.getCompilation();

        // The current package has 4 modules and each module has at least one semantic or syntactic error.
        // This shows that all 4 modules has been compiled, even though the `utils`
        //   module is not imported by any of the other modules.
        Assert.assertEquals(compilation.diagnosticResult().diagnosticCount(), 12);
    }

    @Test(description = "tests package diagnostics")
    public void testDiagnostics() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("test_proj_pkg_compilation");

        // 1) Initialize the project instance
        BuildProject project = loadBuildProject(projectPath);
        // 2) Load the package
        Package currentPackage = project.currentPackage();

        // 3) Compile the current package
        PackageCompilation compilation = currentPackage.getCompilation();

        // The current package has 4 modules and each module has one semantic or syntactic error.
        // This shows that all 4 modules has been compiled, even though the `utils`
        //   module is not imported by any of the other modules.
        Assert.assertEquals(compilation.diagnosticResult().diagnosticCount(), 12);
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_11);
        Assert.assertEquals(jBallerinaBackend.diagnosticResult().diagnosticCount(), 12);

        List<String> expectedPaths = Arrays.asList(
                Paths.get("modules").resolve("utils").resolve("utils.bal").toString(),
                Paths.get("modules").resolve("storage").resolve("db.bal").toString(),
                Paths.get("modules").resolve("services").resolve("svc.bal").toString(),
                Paths.get("modules").resolve("services").resolve("tests").resolve("svc_tests.bal").toString(),
                Paths.get("tests").resolve("main_tests.bal").toString(),
                "main.bal", "utils.bal");

        // Verify paths in packageCompilation diagnostics
        List<String> diagnosticFilePaths = compilation.diagnosticResult().diagnostics().stream().map(diagnostic ->
                diagnostic.location().lineRange().filePath()).distinct().collect(Collectors.toList());

        for (String path : expectedPaths) {
            Assert.assertTrue(diagnosticFilePaths.contains(path), diagnosticFilePaths.toString());
        }
        String diagnoticsStr = compilation.diagnosticResult().diagnostics().stream().map(Diagnostic::toString)
                .distinct().collect(Collectors.joining());

        // Verify the Diagnostic.toString method
        for (String path : expectedPaths) {
            Assert.assertTrue(diagnoticsStr.contains(path), diagnoticsStr);
        }

        // Verify paths in jBallerina backend diagnostics
        diagnosticFilePaths = jBallerinaBackend.diagnosticResult().diagnostics().stream().map(diagnostic ->
                diagnostic.location().lineRange().filePath()).distinct().collect(Collectors.toList());

        for (String path : expectedPaths) {
            Assert.assertTrue(diagnosticFilePaths.contains(path), diagnosticFilePaths.toString());
        }
    }

    @Test(description = "tests codegen with native libraries")
    public void testJBallerinaBackend() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("test_proj_pkg_compilation_simple");

        // 1) Initialize the project instance
        BuildProject project = loadBuildProject(projectPath);
        // 2) Load the package
        Package currentPackage = project.currentPackage();

        // 3) Compile the current package
        PackageCompilation compilation = currentPackage.getCompilation();
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_11);
        DiagnosticResult diagnosticResult = jBallerinaBackend.diagnosticResult();

        Assert.assertEquals(diagnosticResult.diagnosticCount(), 4);

        Collection<PlatformLibrary> platformLibraries = jBallerinaBackend.platformLibraryDependencies(
                currentPackage.packageId(), PlatformLibraryScope.DEFAULT);
        Assert.assertEquals(platformLibraries.size(), 1);

        platformLibraries = jBallerinaBackend.platformLibraryDependencies(
                currentPackage.packageId(), PlatformLibraryScope.TEST_ONLY);
        Assert.assertEquals(platformLibraries.size(), 3);

        platformLibraries = jBallerinaBackend.platformLibraryDependencies(currentPackage.packageId());
        Assert.assertEquals(platformLibraries.size(), 4);
    }

    @Test(description = "tests package compilation with errors in test source files")
    public void testPackageCompilationWithTests() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("project_with_tests");

        // 1) Initialize the project instance
        BuildProject project = loadBuildProject(projectPath);
        // 2) Load the package
        Package currentPackage = project.currentPackage();

        // 3) Compile the current package
        PackageCompilation compilation = currentPackage.getCompilation();

        // The current package has e test modules and each module has one semantic or syntactic error.
        // This shows that all 3 modules has been compiled
        Assert.assertEquals(compilation.diagnosticResult().diagnosticCount(), 3);
    }

    @Test(description = "tests loading a valid build project using project compilation")
    public void testBuildProjectAPIWithPackageCompilation() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject");

        // 1) Initialize the project instance
        BuildProject project = loadBuildProject(projectPath);
        // 2) Load the package
        Package currentPackage = project.currentPackage();

        // 3) Resolve the package dependencies
        PackageResolution resolution = currentPackage.getResolution();

        ResolvedPackageDependency currentNode = null;
        DependencyGraph<ResolvedPackageDependency> dependencyGraph = resolution.dependencyGraph();
        for (ResolvedPackageDependency graphNode : dependencyGraph.getNodes()) {
            if (graphNode.packageId() == currentPackage.packageId()) {
                currentNode = graphNode;
            }
        }

        if (currentNode == null) {
            throw new IllegalStateException("Current package is found in the dependency graph");
        }

        Assert.assertEquals(dependencyGraph.getDirectDependencies(currentNode).size(), 1);
    }

    @Test (description = "tests loading an invalid Ballerina project")
    public void testLoadBallerinaProjectNegative() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject").resolve("modules").resolve("services")
                .resolve("svc.bal");
        try {
            TestUtils.loadBuildProject(projectPath);
        } catch (ProjectException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid Ballerina package directory: " + projectPath));
        }

        projectPath = RESOURCE_DIRECTORY.resolve("myproject").resolve("modules").resolve("services");
        try {
            TestUtils.loadBuildProject(projectPath);
        } catch (ProjectException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid Ballerina package directory: " + projectPath));
        }

        projectPath = RESOURCE_DIRECTORY.resolve("single_file");
        try {
            TestUtils.loadBuildProject(projectPath);
            Assert.fail("expected an invalid project exception");
        } catch (ProjectException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid Ballerina package directory: " + projectPath));
        }
    }

    @Test (description = "tests loading another invalid Ballerina project")
    public void testLoadBallerinaProjectInProject() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject").resolve("modules").resolve("services")
                .resolve("resources").resolve("invalidProject");
        try {
            TestUtils.loadBuildProject(projectPath);
            Assert.fail("expected an invalid project exception");
        } catch (ProjectException e) {
            Assert.assertTrue(e.getMessage().contains("Provided path is already within a Ballerina package: " +
                    projectPath));
        }
    }

    @Test(description = "tests loading a valid build project with build options from toml")
    public void testLoadingBuildOptionsFromToml() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("projectWithBuildOptions");
        // 1) Initialize the project instance
        BuildProject project = null;
        try {
            project = BuildProject.load(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        // Verify expected default buildOptions
        Assert.assertTrue(project.buildOptions().skipTests());
        Assert.assertTrue(project.buildOptions().observabilityIncluded());
        Assert.assertFalse(project.buildOptions().codeCoverage());
        Assert.assertFalse(project.buildOptions().offlineBuild());
        Assert.assertFalse(project.buildOptions().testReport());
    }

    @Test(description = "tests loading a valid build project with build options from toml")
    public void testOverrideBuildOptions() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("projectWithBuildOptions");
        // 1) Initialize the project instance
        BuildProject project = null;
        BuildOptions buildOptions = BuildOptions.builder().setSkipTests(false).build();
        try {
            project = BuildProject.load(projectPath, buildOptions);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        // Verify expected default buildOptions
        Assert.assertFalse(project.buildOptions().skipTests());
        Assert.assertTrue(project.buildOptions().observabilityIncluded());
        Assert.assertFalse(project.buildOptions().codeCoverage());
        Assert.assertFalse(project.buildOptions().offlineBuild());
        Assert.assertFalse(project.buildOptions().testReport());
    }

    @Test(description = "tests overriding build options when editing Toml")
    public void testOverrideBuildOptionsOnTomlEdit() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("projectWithBuildOptions");
        // Initialize the project instance
        BuildOptions buildOptions = BuildOptions.builder().setOffline(true).build();
        BuildProject project = loadBuildProject(projectPath, buildOptions);

        // Test when build option provided only during project load
        BallerinaToml newBallerinaToml = project.currentPackage().ballerinaToml().get().modify().apply();
        Package newPackage = newBallerinaToml.packageInstance();
        Assert.assertTrue(newPackage.project().buildOptions().offlineBuild());

        newBallerinaToml = project.currentPackage().ballerinaToml().get().modify().withContent("[package]\n" +
                "org = \"sameera\"\n" +
                "name = \"winery\"\n" +
                "version = \"0.1.0\"\n" +
                "\n" +
                "[build-options]\n" +
                "observabilityIncluded = true\n" +
                "skipTests=true\n" +
                "offline=false\n" +
                "codeCoverage=true").apply();
        newPackage = newBallerinaToml.packageInstance();
        // Test when build option provided in both project load and Ballerina TOML
        Assert.assertTrue(newPackage.project().buildOptions().offlineBuild());
        // Test when build option provided only in Ballerina TOML
        Assert.assertTrue(newPackage.project().buildOptions().codeCoverage());
        Assert.assertTrue(newPackage.project().buildOptions().observabilityIncluded());
        Assert.assertTrue(newPackage.project().buildOptions().skipTests());
    }

    @Test
    public void testUpdateDocument() {
        // Inputs from langserver
        Path filePath = RESOURCE_DIRECTORY.resolve("myproject").resolve("main.bal").toAbsolutePath();

        // Load the project from document filepath
        Project buildProject = TestUtils.loadProject(filePath);
        DocumentId oldDocumentId = buildProject.documentId(filePath); // get the document ID
        Module oldModule = buildProject.currentPackage().module(oldDocumentId.moduleId());
        Document oldDocument = oldModule.document(oldDocumentId);

        PackageCompilation compilation = buildProject.currentPackage().getCompilation();
        JBallerinaBackend.from(compilation, JvmTarget.JAVA_11);

        // Update the document
        Document updatedDoc = oldDocument.modify().withContent(dummyContent).apply();

        compilation = buildProject.currentPackage().getCompilation();
        JBallerinaBackend.from(compilation, JvmTarget.JAVA_11);


        Assert.assertEquals(oldDocument.module().documentIds().size(), updatedDoc.module().documentIds().size());
        Assert.assertEquals(oldDocument.module().testDocumentIds().size(),
                updatedDoc.module().testDocumentIds().size());
        for (DocumentId documentId : oldDocument.module().documentIds()) {
            Assert.assertTrue(updatedDoc.module().documentIds().contains(documentId));
            Assert.assertFalse(updatedDoc.module().testDocumentIds().contains(documentId));
        }

        Assert.assertNotEquals(oldDocument, updatedDoc);
        Assert.assertNotEquals(oldDocument.syntaxTree().textDocument().toString(), dummyContent);
        Assert.assertEquals(updatedDoc.syntaxTree().textDocument().toString(), dummyContent);

        Package updatedPackage = buildProject.currentPackage();
        Assert.assertNotEquals(oldModule.packageInstance(), buildProject.currentPackage());
        Assert.assertEquals(updatedPackage.module(oldDocument.module().moduleId()).document(oldDocumentId),
                updatedDoc);
        Assert.assertEquals(updatedPackage, updatedDoc.module().packageInstance());
    }

    @Test
    public void testUpdateTestDocument() {
        // Inputs from langserver
        Path filePath = RESOURCE_DIRECTORY.resolve("myproject").resolve(ProjectConstants.TEST_DIR_NAME)
                .resolve("main_tests.bal").toAbsolutePath();

        // Load the project from document filepath
        Project buildProject = TestUtils.loadProject(filePath);
        DocumentId oldDocumentId = buildProject.documentId(filePath); // get the document ID
        Module oldModule = buildProject.currentPackage().module(oldDocumentId.moduleId());
        Document oldDocument = oldModule.document(oldDocumentId);

        // Update the document
        Document updatedDoc = oldDocument.modify().withContent(dummyContent).apply();

        Assert.assertEquals(oldDocument.module().documentIds().size(), updatedDoc.module().documentIds().size());
        Assert.assertEquals(oldDocument.module().testDocumentIds().size(),
                updatedDoc.module().testDocumentIds().size());
        for (DocumentId documentId : oldDocument.module().testDocumentIds()) {
            Assert.assertTrue(updatedDoc.module().testDocumentIds().contains(documentId));
            Assert.assertFalse(updatedDoc.module().documentIds().contains(documentId));
        }

        Assert.assertNotEquals(oldDocument, updatedDoc);
        Assert.assertNotEquals(oldDocument.syntaxTree().textDocument().toString(), dummyContent);
        Assert.assertEquals(updatedDoc.syntaxTree().textDocument().toString(), dummyContent);

        Package updatedPackage = buildProject.currentPackage();
        Assert.assertNotEquals(oldModule.packageInstance(), buildProject.currentPackage());
        Assert.assertEquals(updatedPackage.module(oldDocument.module().moduleId()).document(oldDocumentId),
                updatedDoc);
        Assert.assertEquals(updatedPackage, updatedDoc.module().packageInstance());
    }

    @Test
    public void testAddDocument() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject");
        Path filePath = RESOURCE_DIRECTORY.resolve("myproject").resolve("db.bal").toAbsolutePath();
        String newFileContent = "import ballerina/io;\n";

        BuildProject buildProject = (BuildProject) TestUtils.loadProject(projectPath);

        // get module to which the document should be added
        Module oldModule = buildProject.currentPackage().module(
                ModuleName.from(buildProject.currentPackage().packageName()));
        // create a new document ID
        DocumentId newDocumentId = DocumentId.create(filePath.toString(), oldModule.moduleId());

        DocumentConfig documentConfig = DocumentConfig.from(
                newDocumentId, newFileContent, filePath.getFileName().toString());
        Module newModule = oldModule.modify().addDocument(documentConfig).apply();

        Assert.assertEquals((oldModule.documentIds().size() + 1), newModule.documentIds().size());
        Assert.assertEquals(oldModule.testDocumentIds().size(), newModule.testDocumentIds().size());
        Assert.assertFalse(oldModule.documentIds().contains(newDocumentId));
        Assert.assertTrue(newModule.documentIds().contains(newDocumentId));
        for (DocumentId documentId : oldModule.documentIds()) {
            Assert.assertTrue(newModule.documentIds().contains(documentId));
            Assert.assertFalse(newModule.testDocumentIds().contains(documentId));
        }

        Document newDocument = newModule.document(newDocumentId);
        Assert.assertEquals(newDocument.syntaxTree().textDocument().toString(), newFileContent);

        Package updatedPackage = buildProject.currentPackage();
        Assert.assertNotEquals(oldModule.packageInstance(), buildProject.currentPackage());
        Assert.assertEquals(updatedPackage.module(newDocument.module().moduleId()).document(newDocumentId),
                newDocument);
        Assert.assertEquals(updatedPackage, newDocument.module().packageInstance());
    }

    @Test
    public void testAddTestDocument() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject");
        Path filePath =
                RESOURCE_DIRECTORY.resolve("myproject").resolve(ProjectConstants.TEST_DIR_NAME).resolve("db_test.bal")
                        .toAbsolutePath();
        String newFileContent = "import ballerina/io;\n";

        BuildProject buildProject = (BuildProject) TestUtils.loadProject(projectPath);

        // get module to which the document should be added
        Module oldModule = buildProject.currentPackage().module(ModuleName.from(
                buildProject.currentPackage().packageName()));
        // create a new document ID
        DocumentId newTestDocumentId = DocumentId.create(filePath.toString(), oldModule.moduleId());

        DocumentConfig documentConfig = DocumentConfig.from(
                newTestDocumentId, newFileContent, filePath.getFileName().toString());
        Module newModule = oldModule.modify().addTestDocument(documentConfig).apply();

        Assert.assertEquals((oldModule.testDocumentIds().size() + 1), newModule.testDocumentIds().size());
        Assert.assertEquals(oldModule.documentIds().size(), newModule.documentIds().size());
        Assert.assertFalse(oldModule.testDocumentIds().contains(newTestDocumentId));
        Assert.assertTrue(newModule.testDocumentIds().contains(newTestDocumentId));
        for (DocumentId documentId : oldModule.testDocumentIds()) {
            Assert.assertTrue(newModule.testDocumentIds().contains(documentId));
            Assert.assertFalse(newModule.documentIds().contains(documentId));
        }

        Document newDocument = newModule.document(newTestDocumentId);
        Assert.assertEquals(newDocument.syntaxTree().textDocument().toString(), newFileContent);

        Package updatedPackage = buildProject.currentPackage();
        Assert.assertNotEquals(oldModule.packageInstance(), buildProject.currentPackage());
        Assert.assertEquals(updatedPackage.module(newDocument.module().moduleId()).document(newTestDocumentId),
                newDocument);
        Assert.assertEquals(updatedPackage, newDocument.module().packageInstance());
    }

    @Test
    public void testRemoveDocument() {
        Path filePath = RESOURCE_DIRECTORY.resolve("myproject").resolve("utils.bal").toAbsolutePath();

        Project buildProject = TestUtils.loadProject(filePath);
        DocumentId removeDocumentId = buildProject.documentId(filePath);
        Module oldModule = buildProject.currentPackage().module(removeDocumentId.moduleId());

        Module newModule = oldModule.modify().removeDocument(removeDocumentId).apply();

        Assert.assertEquals((oldModule.documentIds().size() - 1), newModule.documentIds().size());
        Assert.assertEquals(oldModule.testDocumentIds().size(), newModule.testDocumentIds().size());
        Assert.assertTrue(oldModule.documentIds().contains(removeDocumentId));
        Assert.assertFalse(newModule.documentIds().contains(removeDocumentId));
        for (DocumentId documentId : oldModule.documentIds()) {
            if (documentId == removeDocumentId) {
                Assert.assertFalse(newModule.documentIds().contains(documentId));
            } else {
                Assert.assertTrue(newModule.documentIds().contains(documentId));
            }
            Assert.assertFalse(newModule.testDocumentIds().contains(documentId));
        }

        Assert.assertNotEquals(oldModule.packageInstance(), buildProject.currentPackage());
    }

    @Test
    public void testRemoveTestDocument() {
        Path filePath =
                RESOURCE_DIRECTORY.resolve("myproject").resolve(ProjectConstants.MODULES_ROOT).resolve("services")
                        .resolve(ProjectConstants.TEST_DIR_NAME).resolve("svc_tests.bal").toAbsolutePath();

        Project buildProject = TestUtils.loadProject(filePath);
        DocumentId removeDocumentId = buildProject.documentId(filePath);
        Module oldModule = buildProject.currentPackage().module(removeDocumentId.moduleId());

        Module newModule = oldModule.modify().removeDocument(removeDocumentId).apply();

        Assert.assertEquals((oldModule.testDocumentIds().size() - 1), newModule.testDocumentIds().size());
        Assert.assertEquals(oldModule.documentIds().size(), newModule.documentIds().size());
        Assert.assertTrue(oldModule.testDocumentIds().contains(removeDocumentId));
        Assert.assertFalse(newModule.testDocumentIds().contains(removeDocumentId));
        for (DocumentId documentId : oldModule.testDocumentIds()) {
            if (documentId == removeDocumentId) {
                Assert.assertFalse(newModule.testDocumentIds().contains(documentId));
            } else {
                Assert.assertTrue(newModule.testDocumentIds().contains(documentId));
            }
            Assert.assertFalse(newModule.testDocumentIds().contains(documentId));
        }

        Assert.assertNotEquals(oldModule.packageInstance(), buildProject.currentPackage());
    }

    @Test
    public void testAddEmptyModule() {
        Path filePath =
                RESOURCE_DIRECTORY.resolve("myproject").resolve(ProjectConstants.MODULES_ROOT).resolve("newModule")
                        .toAbsolutePath();
        Path projectRoot = ProjectUtils.findProjectRoot(filePath);
        BuildProject buildProject = (BuildProject) TestUtils.loadProject(projectRoot);
        Package oldPackage = buildProject.currentPackage();
        PackageManifest pkgManifest = oldPackage.manifest();

        ModuleId newModuleId = ModuleId.create(filePath.toString(), oldPackage.packageId());
        ModuleName moduleName = ModuleName.from(oldPackage.packageName(), filePath.getFileName().toString());
        ModuleDescriptor moduleDesc = ModuleDescriptor.from(moduleName, pkgManifest.descriptor());

        ModuleConfig newModuleConfig = ModuleConfig.from(newModuleId, moduleDesc, Collections.emptyList(),
                Collections.emptyList(), null,  Collections.emptyList());
        Package newPackage = oldPackage.modify().addModule(newModuleConfig).apply();

        Assert.assertEquals(newPackage.module(newModuleId).documentIds().size(), 0);
        Assert.assertEquals(newPackage.module(newModuleId).testDocumentIds().size(), 0);

        Assert.assertEquals((oldPackage.moduleIds().size() + 1), newPackage.moduleIds().size());
        Assert.assertFalse(oldPackage.moduleIds().contains(newModuleConfig.moduleId()));
        Assert.assertTrue(newPackage.moduleIds().contains(newModuleConfig.moduleId()));

        for (ModuleId moduleId : oldPackage.moduleIds()) {
            Assert.assertTrue(newPackage.moduleIds().contains(moduleId));
        }

        // Test adding a document to empty module
        DocumentId documentId = DocumentId.create(filePath.resolve("main.bal").toString(), newModuleId);
        String mainContent = "import ballerina/io;\n";
        DocumentConfig documentConfig = DocumentConfig.from(documentId, mainContent, filePath.getFileName().toString());
        newPackage.module(newModuleId).modify().addDocument(documentConfig).apply();

        Assert.assertEquals(buildProject.currentPackage().module(newModuleId).documentIds().size(), 1);
        Assert.assertEquals(buildProject.currentPackage().module(newModuleId).testDocumentIds().size(), 0);

        Assert.assertEquals(buildProject.currentPackage().module(newModuleId).document(
                documentId).syntaxTree().textDocument().toString(), mainContent);
    }

    @Test
    public void testAddModuleWithFiles() {
        Path filePath =
                RESOURCE_DIRECTORY.resolve("myproject").resolve(ProjectConstants.MODULES_ROOT).resolve("newModule")
                        .toAbsolutePath();
        Path projectRoot = ProjectUtils.findProjectRoot(filePath);
        BuildProject buildProject = (BuildProject) TestUtils.loadProject(projectRoot);
        Package oldPackage = buildProject.currentPackage();
        PackageManifest pkgManifest = oldPackage.manifest();

        ModuleId newModuleId = ModuleId.create(filePath.toString(), oldPackage.packageId());
        ModuleName moduleName = ModuleName.from(oldPackage.packageName(), filePath.getFileName().toString());
        ModuleDescriptor moduleDesc = ModuleDescriptor.from(moduleName, pkgManifest.descriptor());
        DocumentId documentId = DocumentId.create(filePath.resolve("main.bal").toString(), newModuleId);
        String mainContent = "import ballerina/io;\n";
        DocumentConfig documentConfig = DocumentConfig.from(documentId, mainContent, filePath.getFileName().toString());

        DocumentId testDocumentId = DocumentId.create(filePath.resolve("tests").resolve("main.bal").toString(),
                newModuleId);
        String testContent = "import ballerina/test;\n";
        DocumentConfig testDocumentConfig = DocumentConfig.from(
                testDocumentId, testContent, filePath.getFileName().toString());

        ModuleConfig newModuleConfig = ModuleConfig.from(newModuleId, moduleDesc,
                Collections.singletonList(documentConfig),
                Collections.singletonList(testDocumentConfig), null, Collections.emptyList());
        Package newPackage = oldPackage.modify().addModule(newModuleConfig).apply();

        Assert.assertEquals(newPackage.module(newModuleId).documentIds().size(), 1);
        Assert.assertEquals(newPackage.module(newModuleId).testDocumentIds().size(), 1);

        Assert.assertEquals((oldPackage.moduleIds().size() + 1), newPackage.moduleIds().size());
        Assert.assertFalse(oldPackage.moduleIds().contains(newModuleConfig.moduleId()));
        Assert.assertTrue(newPackage.moduleIds().contains(newModuleConfig.moduleId()));

        for (ModuleId moduleId : oldPackage.moduleIds()) {
            Assert.assertTrue(newPackage.moduleIds().contains(moduleId));
        }
    }

    @Test
    public void testAccessNonExistingDocument() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject");
        Path filePath = RESOURCE_DIRECTORY.resolve("myproject").resolve("db.bal").toAbsolutePath();

        // Load the project from document filepath
        Project buildProject = TestUtils.loadProject(projectPath);
        try {
            DocumentId oldDocumentId = buildProject.documentId(filePath); // get the document ID
            Assert.fail();
        } catch (ProjectException e) {
            Assert.assertTrue(e.getMessage().contains("provided path does not belong to the project"));
        }
    }

    @Test
    public void testLoadFromNonExistentModule() {
        Path filePath =
                RESOURCE_DIRECTORY.resolve("myproject").resolve(ProjectConstants.MODULES_ROOT)
                        .resolve("db");

        try {
            BuildProject buildProject = (BuildProject) TestUtils.loadProject(filePath);
        } catch (ProjectException e) {
            Assert.assertTrue(e.getMessage().contains("provided file path does not exist"));
        }
    }

    @Test(description = "tests get semantic model in module compilation")
    public void testGetSemanticModel() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject");

        // 1) Initialize the project instance
        BuildProject project = loadBuildProject(projectPath);
        // 2) Load the package
        Package currentPackage = project.currentPackage();
        // 3) Load the default module
        Module defaultModule = currentPackage.getDefaultModule();
        Assert.assertEquals(defaultModule.documentIds().size(), 2);

        // 5) Compile the package
        PackageCompilation compilation = project.currentPackage().getCompilation();

        // 6) Get semantic model
        SemanticModel semanticModel = compilation.getSemanticModel(defaultModule.moduleId());

        // 7) Get the document
        Document srcFile = null;
        for (DocumentId docId : defaultModule.documentIds()) {
            if (defaultModule.document(docId).name().equals("main.bal")) {
                srcFile = defaultModule.document(docId);
                break;
            }
        }

        if (srcFile == null) {
            Assert.fail("Source file 'main.bal' not found");
        }

        // Test module level symbols
        List<Symbol> symbols = semanticModel.moduleSymbols();
        Assert.assertEquals(symbols.size(), 4);

        // Test symbol
        Optional<Symbol> symbol = semanticModel.symbol(srcFile, LinePosition.from(4, 10));
        symbol.ifPresent(value -> assertEquals(value.getName().get(), "runServices"));
    }

    @Test(description = "tests if other documents exists ie. Ballerina.toml, Package.md")
    public void testOtherDocuments() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject");

        // 1) Initialize the project instance
        BuildProject project = loadBuildProject(projectPath);
        // 2) Check if files exists
        Package currentPackage = project.currentPackage();
        Assert.assertTrue(currentPackage.ballerinaToml().isPresent());
        Assert.assertTrue(currentPackage.dependenciesToml().isPresent());
        Assert.assertTrue(currentPackage.cloudToml().isPresent());
        Assert.assertTrue(currentPackage.compilerPluginToml().isPresent());
        Assert.assertTrue(currentPackage.packageMd().isPresent());
        // Check module.md files
        Module defaultModule = currentPackage.getDefaultModule();
        Assert.assertTrue(defaultModule.moduleMd().isPresent());
        Module services = currentPackage
                .module(ModuleName.from(currentPackage.packageName(), "services"));
        Assert.assertTrue(services.moduleMd().isPresent());
        Module storage = currentPackage
                .module(ModuleName.from(currentPackage.packageName(), "storage"));
        Assert.assertTrue(storage.moduleMd().isEmpty());

        // Test the content
        TomlTableNode ballerinaToml = currentPackage.ballerinaToml().get().tomlAstNode();
        Assert.assertEquals(ballerinaToml.entries().size(), 1);

        TomlTableNode dependenciesToml = currentPackage.dependenciesToml().get().tomlAstNode();
        Assert.assertEquals(dependenciesToml.entries().size(), 2);

        TomlTableNode cloudToml = currentPackage.cloudToml().get().tomlAstNode();
        Assert.assertEquals(cloudToml.entries().size(), 1);

        TomlTableNode compilerPluginToml = currentPackage.compilerPluginToml().get().tomlAstNode();
        Assert.assertEquals(compilerPluginToml.entries().size(), 2);
    }

    @Test(description = "test editing Ballerina.toml")
    public void testModifyBallerinaToml() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("project_with_tests");

        // 1) Initialize the project instance
        BuildProject project = loadBuildProject(projectPath);
        DocumentId myProjectDocumentId = project.documentId(projectPath.resolve("main.bal"));
        Assert.assertEquals(project.currentPackage().packageName().toString(), "myproject");
        for (ModuleId moduleId : project.currentPackage().moduleIds()) {
            Assert.assertTrue(project.currentPackage().module(moduleId).moduleName().toString().contains("myproject"));
        }

        PackageCompilation compilation = project.currentPackage().getCompilation();
        // there should be 3 diagnostics coming from test files
        Assert.assertEquals(compilation.diagnosticResult().diagnosticCount(), 3);

        // 2) Check editing file - add build option
        BallerinaToml newBallerinaToml = project.currentPackage().ballerinaToml().get().modify().withContent("" +
                "[package]\n" +
                "org = \"sameera\"\n" +
                "name = \"myproject\"\n" +
                "version = \"0.1.0\"\n" +
                "[build-options]\n" +
                "skipTests = true").apply();
        TomlTableNode ballerinaToml = newBallerinaToml.tomlAstNode();
        Assert.assertEquals(ballerinaToml.entries().size(), 2);
        Package newPackage = newBallerinaToml.packageInstance();

        PackageCompilation newPackageCompilation = newPackage.getCompilation();
        // the 3 test diagnostics should be included since test sources are still compiled
        Assert.assertEquals(newPackageCompilation.diagnosticResult().diagnosticCount(), 3);

        // 2) Check editing file - change package metadata
        newBallerinaToml = project.currentPackage().ballerinaToml().get().modify().withContent("" +
                "[package]\n" +
                "org = \"sameera\"\n" +
                "name = \"yourproject\"\n" +
                "version = \"0.1.0\"\n" +
                "[sample]\n" +
                "test = \"attribute\"").apply();
        ballerinaToml = newBallerinaToml.tomlAstNode();
        Assert.assertEquals(ballerinaToml.entries().size(), 2);
        newPackage = newBallerinaToml.packageInstance();
        Assert.assertEquals(newPackage.packageName().toString(), "yourproject");
        for (ModuleId moduleId : project.currentPackage().moduleIds()) {
            Assert.assertTrue(
                    project.currentPackage().module(moduleId).moduleName().toString().contains("yourproject"));
        }
        DocumentId yourProjectPackageId = newPackage.project().documentId(projectPath.resolve("main.bal"));

        Assert.assertEquals(myProjectDocumentId, yourProjectPackageId);

        newPackageCompilation = newPackage.getCompilation();
        // imports within the package should not be resolved since the package name has changed
        // the original 3 test diagnostics should also be present
        Assert.assertEquals(newPackageCompilation.diagnosticResult().diagnosticCount(), 12);
    }

    @Test(description = "test editing Ballerina.toml")
    public void testModifyDependenciesToml() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("projects_for_edit_api_tests/package_test_dependencies_toml");
        BuildProject project = loadBuildProject(projectPath, BuildOptions.builder().setSticky(true).build());

        PackageCompilation compilation = project.currentPackage().getCompilation();
        ResolvedPackageDependency packageDep =
                project.currentPackage().getResolution().dependencyGraph().toTopologicallySortedList()
                        .stream().filter(resolvedPackageDependency -> resolvedPackageDependency
                        .packageInstance().packageName().toString().equals("package_dep")).findFirst().get();
        Assert.assertEquals(packageDep.packageInstance().packageVersion().toString(), "0.1.0");
        Assert.assertEquals(compilation.diagnosticResult().diagnosticCount(), 0);

        DependenciesToml newDependenciesToml = project.currentPackage().dependenciesToml()
                .get().modify().withContent("" +
                "[ballerina]\n" +
                "dependencies-toml-version = \"2\"\n" +
                "\n" +
                "[[package]]\n" +
                "org = \"foo\"\n" +
                "name = \"test_dependencies_package\"\n" +
                "version = \"2.1.0\"\n" +
                "dependencies = [\n" +
                "    {org = \"foo\", name = \"package_dep\"}\n" +
                "]\n" +
                "modules = [\n" +
                "    {org = \"foo\", " +
                        "packageName = \"test_dependencies_package\", " +
                        "moduleName = \"test_dependencies_package\"}\n" +
                "]\n" +
                "\n" +
                "[[package]]\n" + "org = \"foo\"\n" +
                "name = \"package_dep\"\n" +
                "version = \"0.1.1\"").apply();
        TomlTableNode dependenciesToml = newDependenciesToml.tomlAstNode();
        Assert.assertEquals(((TomlTableArrayNode) dependenciesToml.entries().get("package")).children().size(), 2);

        PackageCompilation newCompilation = project.currentPackage().getCompilation();
        ResolvedPackageDependency packageDepNew =
                project.currentPackage().getResolution().dependencyGraph().toTopologicallySortedList()
                        .stream().filter(resolvedPackageDependency -> resolvedPackageDependency
                        .packageInstance().packageName().toString().equals("package_dep")).findFirst().get();
        Assert.assertEquals(packageDepNew.packageInstance().packageVersion().toString(), "0.1.1");
        newCompilation.diagnosticResult().diagnostics().forEach(OUT::println);
        Assert.assertEquals(newCompilation.diagnosticResult().diagnosticCount(), 1);

        // Set the old version again
        project.currentPackage().dependenciesToml()
                .get().modify().withContent("" +
                "[ballerina]\n" +
                "dependencies-toml-version = \"2\"\n" +
                "\n" +
                "[[package]]\n" +
                "org = \"foo\"\n" +
                "name = \"test_dependencies_package\"\n" +
                "version = \"2.1.0\"\n" +
                "dependencies = [\n" +
                "    {org = \"foo\", name = \"package_dep\"}\n" +
                "]\n" +
                "modules = [\n" +
                "    {org = \"foo\", " +
                "packageName = \"test_dependencies_package\", " +
                "moduleName = \"test_dependencies_package\"}\n" +
                "]\n" +
                "\n" +
                "[[package]]\n" + "org = \"foo\"\n" +
                "name = \"package_dep\"\n" +
                "version = \"0.1.0\"").apply();
        PackageCompilation newCompilation2 = project.currentPackage().getCompilation();
        Assert.assertEquals(newCompilation2.diagnosticResult().diagnosticCount(), 0);
    }

    @Test(description = "tests if other documents can be edited ie. Dependencies.toml, Package.md")
    public void testOtherDocumentModify() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject");

        // 1) Initialize the project instance
        BuildProject project = loadBuildProject(projectPath);
        // 2) Check editing files
        DependenciesToml newDependenciesToml = project.currentPackage().dependenciesToml()
                .get().modify().withContent("" +
                "[[package]]\n" +
                "org = \"samjs\"\n" +
                "name = \"package_k\"\n" +
                "version = \"1.1.0-alpha\"\n" +
                "[[package]]\n" +
                "org = \"samjs\"\n" +
                "name = \"package_p\"\n" +
                "version = \"1.1.0-alpha\"").apply();
        TomlTableNode dependenciesToml = newDependenciesToml.tomlAstNode();
        Assert.assertEquals(((TomlTableArrayNode) dependenciesToml.entries().get("package")).children().size(), 2);

        CloudToml newCloudToml = project.currentPackage().cloudToml().get().modify().withContent("" +
                "[test]\n" +
                "attribute = \"value\"\n" +
                "[test2]\n" +
                "attribute = \"value2\"").apply();
        TomlTableNode cloudToml = newCloudToml.tomlAstNode();
        Assert.assertEquals(cloudToml.entries().size(), 2);

        CompilerPluginToml newCompilerPluginToml = project.currentPackage().compilerPluginToml().get().modify()
                .withContent("" +
                            "[plugin]\n" +
                            "id = \"openapi-validator\"\n" +
                            "class = \"io.ballerina.openapi.Validator\"\n" +
                            "\n" +
                            "[[dependency]]\n" +
                            "path = \"./libs/platform-io-1.3.0-java.txt\"\n").apply();
        TomlTableNode compilerPluginToml = newCompilerPluginToml.tomlAstNode();
        Assert.assertEquals(compilerPluginToml.entries().size(), 2);

        // Check if PackageMd is editable
        project.currentPackage().packageMd().get().modify().withContent("#Modified").apply();
        String packageMdContent = project.currentPackage().packageMd().get().content();
        Assert.assertEquals(packageMdContent, "#Modified");

        // Check if ModuleMd is editable
        project.currentPackage().getDefaultModule().moduleMd().get().modify().withContent("#Modified").apply();
        String moduleMdContent = project.currentPackage().getDefaultModule().moduleMd().get().content();
        Assert.assertEquals(packageMdContent, "#Modified");

        // Check if ModuleMd is editable other than default module
        // todo enable following after resolving package name edit bug
        // ModuleName services = ModuleName.from(project.currentPackage().packageName(), "services");
        // project.currentPackage().module(services).moduleMd().get().modify().withContent("#Modified").apply();
        // moduleMdContent = project.currentPackage().module(services).moduleMd().get().content();
        // Assert.assertEquals(packageMdContent, "#Modified");

        // Test remove capability
        project.currentPackage().modify().removePackageMd().apply();
        project.currentPackage().modify().removeDependenciesToml().apply();
        project.currentPackage().modify().removeCloudToml().apply();
        project.currentPackage().modify().removeCompilerPluginToml().apply();
        project.currentPackage().getDefaultModule().modify().removeModuleMd().apply();

        Assert.assertTrue(project.currentPackage().packageMd().isEmpty());
        Assert.assertTrue(project.currentPackage().cloudToml().isEmpty());
        Assert.assertTrue(project.currentPackage().compilerPluginToml().isEmpty());
        Assert.assertTrue(project.currentPackage().dependenciesToml().isEmpty());
        Assert.assertTrue(project.currentPackage().getDefaultModule().moduleMd().isEmpty());
    }

    @Test(description = "tests adding Dependencies.toml, Package.md")
    public void testOtherDocumentAdd() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("project_without_k8s");

        // 1) Initialize the project instance
        BuildProject project = loadBuildProject(projectPath);
        // 2) Check editing files
        Package currentPackage = project.currentPackage();

        Assert.assertTrue(currentPackage.dependenciesToml().isEmpty());
        Assert.assertTrue(currentPackage.cloudToml().isEmpty());
        Assert.assertTrue(currentPackage.compilerPluginToml().isEmpty());
        // Assert.assertTrue(currentPackage.packageMd().isEmpty());

        DocumentConfig dependenciesToml = DocumentConfig.from(
                DocumentId.create(ProjectConstants.DEPENDENCIES_TOML, null),
                        "[[package]]\n" +
                        "org = \"samjs\"\n" +
                        "name = \"package_k\"\n" +
                        "version = \"1.1.0-alpha\"\n" +
                        "[[package]]\n" +
                        "org = \"samjs\"\n" +
                        "name = \"package_p\"\n" +
                        "version = \"1.1.0-alpha\"",
                    ProjectConstants.DEPENDENCIES_TOML
                );

        currentPackage = currentPackage.modify().addDependenciesToml(dependenciesToml).apply();
        TomlTableNode dependenciesTomlTable = currentPackage.dependenciesToml().get().tomlAstNode();
        Assert.assertEquals(((TomlTableArrayNode) dependenciesTomlTable.entries()
                .get("package")).children().size(), 2);

        DocumentConfig cloudToml = DocumentConfig.from(
                DocumentId.create(ProjectConstants.CLOUD_TOML, null),
                "[test]\n" +
                  "attribute = \"value\"\n" +
                  "[test2]\n" +
                  "attribute = \"value2\"",
                ProjectConstants.CLOUD_TOML
        );

        currentPackage = currentPackage.modify().addCloudToml(cloudToml).apply();
        TomlTableNode cloudTomlTable = currentPackage.cloudToml().get().tomlAstNode();
        Assert.assertEquals(((TomlTableArrayNode) dependenciesTomlTable.entries()
                .get("package")).children().size(), 2);

        DocumentConfig compilerPluginToml = DocumentConfig.from(
                DocumentId.create(ProjectConstants.COMPILER_PLUGIN_TOML, null),
                "[plugin]\n" +
                        "id = \"openapi-validator\"\n" +
                        "class = \"io.ballerina.openapi.Validator\"\n" +
                        "\n" +
                        "[[dependency]]\n" +
                        "path = \"./libs/platform-io-1.3.0-java.txt\"\n",
                ProjectConstants.COMPILER_PLUGIN_TOML);

        currentPackage = currentPackage.modify().addCompilerPluginToml(compilerPluginToml).apply();
        TomlTableNode compilerPluginTomlTable = currentPackage.compilerPluginToml().get().tomlAstNode();
        Assert.assertEquals(compilerPluginTomlTable.entries().size(), 2);
    }

    @Test(description = "tests if other documents can be edited ie. Ballerina.toml, Package.md")
    public void testOtherMinimalistProjectEdit() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject_minimalist");

        // 1) Initialize the project instance
        BuildProject project = loadBuildProject(projectPath);
        // 2) Check editing files
        Package currentPackage = project.currentPackage();

        List<String> data = new ArrayList<>();
        data.add("[package]");
        data.add("[package]" +
                "name");
        data.add("[package]" +
                "name=");
        data.add("[package]" +
                "name=\"te");
        data.add("[package]" +
                "name=\"test");
        data.add("[package]" +
                 "name=\"test" +
                 "or");
        data.add("[package]" +
                 "name=\"test" +
                 "org");
        data.add("[package]" +
                 "name=\"test" +
                 "org=");
        data.add("[package]" +
                 "name=\"test" +
                 "org=win");
        data.add("[package]" +
                 "name=\"test" +
                 "org=winery");
        data.add("[package]" +
                 "name=\"test" +
                 "org=winery" +
                 "ver");
        data.add("[package]" +
                 "name=\"test" +
                 "org=winery" +
                 "version");
        data.add("[package]" +
                 "name=\"test" +
                 "org=winery" +
                 "version=");
        data.add("[package]" +
                 "name=\"test" +
                 "org=winery" +
                 "version=1.");
        data.add("[package]" +
                 "name=\"test" +
                 "org=winery" +
                 "version=1.0.0");

        for (String dataItem: data) {
            BallerinaToml newBallerinaToml = currentPackage.ballerinaToml().get().modify().withContent("" +
                    dataItem).apply();
            TomlTableNode ballerinaToml = newBallerinaToml.tomlAstNode();
            Package newPackage = newBallerinaToml.packageInstance();
        }

    }

    @Test
    public void testEditDependantModuleDocument() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("projects_for_edit_api_tests/package_with_dependencies");
        String updatedFunctionStr = "public function concatStrings(string a, string b, string c) returns string {\n" +
                "\treturn a + b;\n" +
                "}\n";

        // 1) Initialize the project instance
        BuildProject project = loadBuildProject(projectPath);
        // 2) Load current package
        Package currentPackage = project.currentPackage();

        // 3) Compile the package
        PackageCompilation compilation = currentPackage.getCompilation();
        Assert.assertEquals(compilation.diagnosticResult().diagnosticCount(), 0);

        // 4) Edit a module that is used by another module
        Module module = currentPackage.module(ModuleName.from(PackageName.from("myproject"), "util"));
        DocumentId documentId = module.documentIds().stream().findFirst().get();
        module.document(documentId).modify().withContent(updatedFunctionStr).apply();

        PackageCompilation compilation1 = project.currentPackage().getCompilation();
        DiagnosticResult diagnosticResult = compilation1.diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 1);
        Assert.assertEquals(diagnosticResult.diagnostics().stream().findAny().get().location().lineRange().filePath(),
                "main.bal");
        Assert.assertTrue(diagnosticResult.diagnostics().stream().findAny().get().message()
                .contains("missing required parameter 'c'"));
    }

    @Test
    public void testRemoveDependantModuleDocument() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("projects_for_edit_api_tests/package_with_dependencies");

        // 1) Initialize the project instance
        BuildProject project = loadBuildProject(projectPath);
        // 2) Load current package
        Package currentPackage = project.currentPackage();

        // 3) Compile the package
        PackageCompilation compilation = currentPackage.getCompilation();
        Assert.assertEquals(compilation.diagnosticResult().diagnosticCount(), 0);

        // 4) Edit a module that is used by another module
        Module module = currentPackage.module(ModuleName.from(PackageName.from("myproject"), "util"));
        DocumentId documentId = module.documentIds().stream().findFirst().get();
        module.modify().removeDocument(documentId).apply();

        PackageCompilation compilation1 = project.currentPackage().getCompilation();
        DiagnosticResult diagnosticResult = compilation1.diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 1);
        Assert.assertEquals(diagnosticResult.diagnostics().stream().findAny().get().location().lineRange().filePath(),
                "main.bal");
        Assert.assertTrue(diagnosticResult.diagnostics().stream().findAny().get().message()
                .contains("undefined function 'concatStrings'"));
    }

    @Test
    public void testEditTransitivelyDependantModuleDocument() {
        Path projectPath = RESOURCE_DIRECTORY
                .resolve("projects_for_edit_api_tests/package_with_transitive_dependencies");
        String updatedFunctionStr = "public function concatStrings(string a, string b) returns string {\n" +
                "\treturn a + b;\n" +
                "}\n";

        // 1) Initialize the project instance
        BuildProject project = loadBuildProject(projectPath);
        // 2) Load current package
        Package currentPackage = project.currentPackage();

        // 3) Compile the package
        PackageCompilation compilation = currentPackage.getCompilation();
        Assert.assertEquals(compilation.diagnosticResult().diagnosticCount(), 0);

        // 4) Edit a module that is used by another module
        Module module = currentPackage.module(ModuleName.from(PackageName.from("myproject"), "util"));
        DocumentId documentId = module.documentIds().stream().findFirst().get();
        module.document(documentId).modify().withContent(updatedFunctionStr).apply();

        PackageCompilation compilation1 = project.currentPackage().getCompilation();
        DiagnosticResult diagnosticResult = compilation1.diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 1);

        Assert.assertEquals(diagnosticResult.diagnostics().stream().findAny().get().location().lineRange().filePath(),
                Paths.get("modules").resolve("schema").resolve("schema.bal").toString());
        Assert.assertTrue(diagnosticResult.diagnostics().stream().findAny().get().message()
                .contains("unknown type 'PersonalDetails'"));
    }

    @Test
    public void testEditPackageWithCyclicDependency() {
        Path projectPath = RESOURCE_DIRECTORY
                .resolve("projects_for_edit_api_tests/package_with_cyclic_dependencies");
        String updatedFunctionStr = "public function concatStrings(string a, string b) returns string {\n" +
                "\treturn a + b;\n" +
                "}\n";

        // 1) Initialize the project instance
        BuildProject project = loadBuildProject(projectPath);
        // 2) Load current package
        Package currentPackage = project.currentPackage();

        // 3) Compile the package
        PackageCompilation compilation = currentPackage.getCompilation();
        Assert.assertEquals(compilation.diagnosticResult().diagnosticCount(), 1);

        // 4) Edit a module that is used by another module
        Module module = currentPackage.module(ModuleName.from(PackageName.from("myproject"), "util"));
        DocumentId documentId = module.documentIds().stream().findFirst().get();
        module.document(documentId).modify().withContent(updatedFunctionStr).apply();

        PackageCompilation compilation1 = project.currentPackage().getCompilation();
        DiagnosticResult diagnosticResult = compilation1.diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 1);

        Iterator<Diagnostic> diagnosticIterator = diagnosticResult.diagnostics().iterator();
        Diagnostic firstDiagnostic = diagnosticIterator.next();
        Assert.assertEquals(firstDiagnostic.message(), "cyclic module imports detected " +
                "'foo/myproject.schema:0.1.0 -> foo/myproject.storage:0.1.0 -> foo/myproject.schema:0.1.0'");
    }

    /**
     * Test DocumentId of a document which it's module name contains package name.
     * Package name: winery
     * Module name: winery1
     */
    @Test
    public void testDocumentIdWhichModuleContainsPackageName() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("projectForDocumentIdTest");

        // 1) Initialize the project instance
        BuildProject project = loadBuildProject(projectPath);
        // 2) Load current package
        Package currentPackage = project.currentPackage();

        // 3) Compile the package
        PackageCompilation compilation = currentPackage.getCompilation();
        Assert.assertFalse(compilation.diagnosticResult().hasErrors());

        // Inputs from langserver
        Path filePath = RESOURCE_DIRECTORY.resolve("projectForDocumentIdTest").resolve("modules").resolve("winery1")
                .resolve("winery1.bal").toAbsolutePath();

        // Load the project from document filepath
        Project buildProject = TestUtils.loadProject(filePath);
        buildProject.documentId(filePath); // get the document ID
    }

    @Test(description = "test auto updating dependencies using build file")
    public void testAutoUpdateWithBuildFile() throws IOException {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject");
        // Delete build file if already exists
        if (projectPath.resolve(TARGET_DIR_NAME).resolve(BUILD_FILE).toFile().exists()) {
            Files.delete(projectPath.resolve(TARGET_DIR_NAME).resolve(BUILD_FILE));
        }
        // Set sticky false, to imitate the default build command behavior
        BuildOptions.BuildOptionsBuilder buildOptionsBuilder = BuildOptions.builder();
        buildOptionsBuilder.setSticky(false);
        BuildOptions buildOptions = buildOptionsBuilder.build();

        // 1) Initialize the project instance
        BuildProject project = loadBuildProject(projectPath, buildOptions);
        project.save();

        Assert.assertEquals(project.currentPackage().packageName().toString(), "myproject");
        Path buildFile = project.targetDir().resolve(BUILD_FILE);
        Assert.assertTrue(buildFile.toFile().exists());
        BuildJson initialBuildJson = readBuildJson(buildFile);
        Assert.assertTrue(initialBuildJson.lastBuildTime() > 0);
        Assert.assertTrue(initialBuildJson.lastUpdateTime() > 0);
        Assert.assertFalse(initialBuildJson.isExpiredLastUpdateTime());

        // 2) Build project again with build file
        BuildProject projectSecondBuild = loadBuildProject(projectPath, buildOptions);
        projectSecondBuild.save();

        Assert.assertTrue(buildFile.toFile().exists());
        BuildJson secondBuildJson = readBuildJson(buildFile);
        Assert.assertTrue(secondBuildJson.lastBuildTime() > initialBuildJson.lastBuildTime());
        assertEquals(initialBuildJson.lastUpdateTime(), secondBuildJson.lastUpdateTime());
        Assert.assertFalse(secondBuildJson.isExpiredLastUpdateTime());
        Assert.assertFalse(projectSecondBuild.currentPackage().getResolution().autoUpdate());

        // 3) Change `last_update-time` in `build` file to a timestamp older than one day and build the project again
        secondBuildJson.setLastUpdateTime(secondBuildJson.lastUpdateTime() - (24 * 60 * 60 * 1000 + 1));
        ProjectUtils.writeBuildFile(buildFile, secondBuildJson);
        BuildProject projectThirdBuild = loadBuildProject(projectPath, buildOptions);
        Assert.assertTrue(projectThirdBuild.currentPackage().getResolution().autoUpdate());
        projectThirdBuild.save();

        Assert.assertTrue(buildFile.toFile().exists());
        BuildJson thirdBuildJson = readBuildJson(buildFile);
        Assert.assertTrue(thirdBuildJson.lastBuildTime() > initialBuildJson.lastBuildTime());
        Assert.assertTrue(thirdBuildJson.lastUpdateTime() > initialBuildJson.lastUpdateTime());
        Assert.assertFalse(thirdBuildJson.isExpiredLastUpdateTime());
    }

    @Test(description = "test auto updating dependencies with build file after removing Dependencies.toml")
    public void testAutoUpdateWithBuildFileWithoutDepsToml() throws IOException {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject");
        // Delete build file and Dependencies.toml file if already exists
        Files.deleteIfExists(projectPath.resolve(TARGET_DIR_NAME).resolve(BUILD_FILE));
        Files.deleteIfExists(projectPath.resolve(DEPENDENCIES_TOML));

        // Set sticky false, to imitate the default build command behavior
        BuildOptions.BuildOptionsBuilder buildOptionsBuilder = BuildOptions.builder();
        buildOptionsBuilder.setSticky(false);
        BuildOptions buildOptions = buildOptionsBuilder.build();

        // 1) Initialize the project instance
        BuildProject project = loadBuildProject(projectPath, buildOptions);
        project.save();

        Assert.assertEquals(project.currentPackage().packageName().toString(), "myproject");
        Path buildFile = project.targetDir().resolve(BUILD_FILE);
        Assert.assertTrue(buildFile.toFile().exists());
        BuildJson initialBuildJson = readBuildJson(buildFile);
        Assert.assertTrue(initialBuildJson.lastBuildTime() > 0, "invalid last_build_time in the build file");
        Assert.assertTrue(initialBuildJson.lastUpdateTime() > 0, "invalid last_update_time in the build file");
        Assert.assertFalse(initialBuildJson.isExpiredLastUpdateTime(), "last_update_time is expired");
        Assert.assertEquals(
                readFileAsString(projectPath.resolve(RESOURCE_DIR_NAME).resolve("expectedDependencies.toml")),
                readFileAsString(projectPath.resolve(DEPENDENCIES_TOML)));


        // 2) Build project again with build file after removing Dependencies.toml
        Files.deleteIfExists(projectPath.resolve(DEPENDENCIES_TOML));
        Assert.assertTrue(project.targetDir().resolve(BUILD_FILE).toFile().exists());
        BuildProject projectSecondBuild = loadBuildProject(projectPath, buildOptions);
        projectSecondBuild.save();

        Assert.assertTrue(buildFile.toFile().exists());
        BuildJson secondBuildJson = readBuildJson(buildFile);
        Assert.assertTrue(secondBuildJson.lastBuildTime() > initialBuildJson.lastBuildTime(),
                          "last_build_time has not updated for the second build");
        assertEquals(initialBuildJson.lastUpdateTime(), secondBuildJson.lastUpdateTime(),
                     "last_update_time has updated for the second build");
        Assert.assertFalse(secondBuildJson.isExpiredLastUpdateTime(), "last_update_time is expired");
        Assert.assertFalse(projectSecondBuild.currentPackage().getResolution().autoUpdate());
        Assert.assertEquals(
                readFileAsString(projectPath.resolve(RESOURCE_DIR_NAME).resolve("expectedDependencies.toml")),
                readFileAsString(projectPath.resolve(DEPENDENCIES_TOML)));

        // Remove generated files
        Files.deleteIfExists(projectSecondBuild.targetDir().resolve(BUILD_FILE));
    }

    @Test(description = "test auto updating dependencies with old distribution version")
    public void testAutoUpdateWithOldDistVersion() throws IOException {
        Path projectPath = RESOURCE_DIRECTORY.resolve("old_dist_version_project");
        // Delete build file and Dependencies.toml file if already exists
        Files.deleteIfExists(projectPath.resolve(TARGET_DIR_NAME).resolve(BUILD_FILE));
        Files.deleteIfExists(projectPath.resolve(DEPENDENCIES_TOML));

        // Set sticky false, to imitate the default build command behavior
        BuildOptions.BuildOptionsBuilder buildOptionsBuilder = BuildOptions.builder();
        buildOptionsBuilder.setSticky(false);
        BuildOptions buildOptions = buildOptionsBuilder.build();

        // 1) Initialize the project instance
        BuildProject project = loadBuildProject(projectPath, buildOptions);
        project.save();

        Assert.assertEquals(project.currentPackage().packageName().toString(), "myproject");
        Path buildFile = project.targetDir().resolve(BUILD_FILE);
        Assert.assertTrue(buildFile.toFile().exists());
        BuildJson initialBuildJson = readBuildJson(buildFile);
        Assert.assertTrue(initialBuildJson.lastBuildTime() > 0, "invalid last_build_time in the build file");
        Assert.assertTrue(initialBuildJson.lastUpdateTime() > 0, "invalid last_update_time in the build file");
        Assert.assertFalse(initialBuildJson.isExpiredLastUpdateTime(), "last_update_time is expired");

        // 2) Build project again after setting un-matching dist version
        // When distribution is not matching always should update Dependencies.toml, even build file has not expired
        initialBuildJson.setDistributionVersion("slbeta0");
        ProjectUtils.writeBuildFile(buildFile, initialBuildJson);
        Assert.assertTrue(project.targetDir().resolve(BUILD_FILE).toFile().exists());
        BuildProject projectSecondBuild = loadBuildProject(projectPath, buildOptions);
        projectSecondBuild.save();

        Assert.assertTrue(buildFile.toFile().exists());
        BuildJson secondBuildJson = readBuildJson(buildFile);
        Assert.assertTrue(secondBuildJson.lastBuildTime() > initialBuildJson.lastBuildTime(),
                "last_build_time has not updated for the second build");
        assertTrue(secondBuildJson.lastUpdateTime() > initialBuildJson.lastUpdateTime(),
                "last_update_time has not updated for the second build");
        Assert.assertFalse(secondBuildJson.isExpiredLastUpdateTime(), "last_update_time is expired");
        Assert.assertTrue(projectSecondBuild.currentPackage().getResolution().autoUpdate());

        // 3) Build project again after setting dist version as null
        initialBuildJson.setDistributionVersion(null);
        ProjectUtils.writeBuildFile(buildFile, initialBuildJson);
        Assert.assertTrue(projectSecondBuild.targetDir().resolve(BUILD_FILE).toFile().exists());
        BuildProject projectThirdBuild = loadBuildProject(projectPath, buildOptions);
        projectThirdBuild.save();

        Assert.assertTrue(buildFile.toFile().exists());
        BuildJson thirdBuildJson = readBuildJson(buildFile);
        Assert.assertTrue(thirdBuildJson.lastBuildTime() > secondBuildJson.lastBuildTime(),
                "last_build_time has not updated for the second build");
        assertTrue(thirdBuildJson.lastUpdateTime() > secondBuildJson.lastUpdateTime(),
                "last_update_time has not updated for the second build");
        Assert.assertFalse(thirdBuildJson.isExpiredLastUpdateTime(), "last_update_time is expired");
        Assert.assertTrue(projectThirdBuild.currentPackage().getResolution().autoUpdate());

        // Remove generated files
        Files.deleteIfExists(projectSecondBuild.targetDir().resolve(BUILD_FILE));
        Files.deleteIfExists(projectPath.resolve(DEPENDENCIES_TOML));
    }

    @Test(description = "test build package without dependencies")
    public void testPackageWithoutDependencies() throws IOException {
        Path projectPath = RESOURCE_DIRECTORY.resolve("project_wo_deps");
        // Delete Dependencies.toml and build file if already exists
        Files.deleteIfExists(projectPath.resolve(DEPENDENCIES_TOML));
        Files.deleteIfExists(projectPath.resolve(TARGET_DIR_NAME).resolve(BUILD_FILE));

        // 1) Initialize the project instance
        BuildProject project = loadBuildProject(projectPath);
        project.save();

        Assert.assertEquals(project.currentPackage().packageName().toString(), "project_wo_deps");
        // Dependencies.toml should not be created when there is no package dependencies
        // build file should be deleted, since if not we are not trying to update Dependencies.toml
        // Since we are adding root package to the Dependencies.toml, it will be created anyway
        Path dependenciesTomlPath = project.sourceRoot().resolve(DEPENDENCIES_TOML);
        Path buildFilePath = project.sourceRoot().resolve(TARGET_DIR_NAME).resolve(BUILD_FILE);
        Assert.assertTrue(dependenciesTomlPath.toFile().exists());

        // 2) load and save project again
        Files.deleteIfExists(buildFilePath);
        Assert.assertFalse(buildFilePath.toFile().exists());
        project = loadBuildProject(projectPath);
        project.save();

        // Existing Dependencies.toml should not be deleted when there is no package dependencies
        Assert.assertTrue(dependenciesTomlPath.toFile().exists());
        // It should consist of the dependency toml version
        String expected = "[ballerina]\n"
                + "dependencies-toml-version = \"2\"";
        String actual = Files.readString(projectPath.resolve(DEPENDENCIES_TOML));
        Assert.assertTrue(actual.contains(expected));

        // Clean Dependencies.toml and build file if already exists
        Files.deleteIfExists(projectPath.resolve(DEPENDENCIES_TOML));
        Files.deleteIfExists(projectPath.resolve(TARGET_DIR_NAME).resolve(BUILD_FILE));
    }

    @Test(description = "tests Dependencies.toml creation and its content")
    public void testDependenciesTomlCreationAndItsContent() throws IOException {
        // package_d --> package_b --> package_c
        // package_d --> package_e
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("projects_for_resolution_tests").resolve("package_d");

        // Delete build file and Dependencies.toml if exists
        Files.deleteIfExists(projectDirPath.resolve(TARGET_DIR_NAME).resolve(BUILD_FILE));
        Files.deleteIfExists(projectDirPath.resolve(DEPENDENCIES_TOML));

        BuildProject buildProject = BuildProject.load(projectDirPath);
        buildProject.save();
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        diagnosticResult.errors().forEach(OUT::println);
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 0, "Unexpected compilation diagnostics");

        // Check Dependencies.toml
        Assert.assertEquals(
                readFileAsString(projectDirPath.resolve(RESOURCE_DIR_NAME).resolve("expectedDependencies.toml")),
                readFileAsString(projectDirPath.resolve(DEPENDENCIES_TOML)));

        // Clean Dependencies.toml and build file if already exists
        Files.deleteIfExists(projectDirPath.resolve(DEPENDENCIES_TOML));
        Files.deleteIfExists(projectDirPath.resolve(TARGET_DIR_NAME).resolve(BUILD_FILE));
    }

    @Test
    public void testGetResources() {
        // 1. load the project
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject");
        BuildProject buildProject = loadBuildProject(projectPath);
        for (ModuleId moduleId : buildProject.currentPackage().moduleIds()) {
            Module module = buildProject.currentPackage().module(moduleId);
            if (module.isDefaultModule()) {
                Assert.assertEquals(module.resourceIds().size(), 2);
                for (DocumentId documentId : module.resourceIds()) {
                    Assert.assertTrue(module.resource(documentId).name().equals("expectedDependencies.toml") ||
                            module.resource(documentId).name().equals("main.json"));
                }
                Assert.assertEquals(module.testResourceIds().size(), 0);
                continue;
            }
            if (module.moduleName().toString().equals("myproject.services")) {
                Assert.assertEquals(module.resourceIds().size(), 4);
                for (DocumentId documentId : module.resourceIds()) {
                    Assert.assertTrue(module.resource(documentId).name().equals("config.json") ||
                            module.resource(documentId).name().equals("invalidProject/tests/main_tests.bal") ||
                            module.resource(documentId).name().equals("invalidProject/main.bal") ||
                            module.resource(documentId).name().equals("invalidProject/Ballerina.toml")
                            );
                }

                Assert.assertEquals(module.testResourceIds().size(), 0);
                continue;
            }
            Assert.assertEquals(module.resourceIds().size(), 1);
            Assert.assertEquals(module.resource(module.resourceIds().stream().findFirst().orElseThrow()).name(),
                    "db.json");
            Assert.assertEquals(module.testResourceIds().size(), 0);
        }
    }

    @Test
    public void testGetResourcesOfDependencies() throws IOException {
        Path projectPath = RESOURCE_DIRECTORY.resolve("projects_for_resources_tests/package_e");
        BuildProject buildProject = loadBuildProject(projectPath);
        Module defaultModule = buildProject.currentPackage().getDefaultModule();
        DocumentId documentId = defaultModule.resourceIds().stream().findFirst().orElseThrow();
        Assert.assertEquals(defaultModule.resource(documentId).name(), "project-info.properties");

        List<ResolvedPackageDependency> dependencies = buildProject.currentPackage().getResolution().dependencyGraph()
                .getNodes().stream().filter(resolvedPackageDependency ->
                        !resolvedPackageDependency.packageInstance().equals(buildProject.currentPackage()))
                .collect(Collectors.toList());
        Module depDefaultModule = dependencies.get(0).packageInstance().getDefaultModule();
        DocumentId dependencyDocId = depDefaultModule.resourceIds()
                .stream().findFirst().orElseThrow();
        Assert.assertEquals(depDefaultModule.resource(dependencyDocId).name(), "project-info.properties");

        PackageCompilation compilation = buildProject.currentPackage().getCompilation();
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_11);
        Path execPath = buildProject.sourceRoot().resolve(TARGET_DIR_NAME).resolve("temp.jar");
        jBallerinaBackend.emit(JBallerinaBackend.OutputType.EXEC, execPath);

        JarFile execJar = new JarFile(execPath.toString());
        String resourceName = RESOURCE_DIR_NAME + "/asmaj/package_e/0/project-info.properties";
        String depResourceName = RESOURCE_DIR_NAME + "/samjs/package_e/0/project-info.properties";

        Assert.assertNotNull(execJar.getJarEntry(resourceName));
        try (InputStream inputStream = execJar.getInputStream(execJar.getJarEntry(resourceName))) {
            Assert.assertTrue(new String(inputStream.readAllBytes()).contains("asmaj"));
        }
        Assert.assertNotNull(execJar.getJarEntry(depResourceName));
        try (InputStream inputStream = execJar.getInputStream(execJar.getJarEntry(depResourceName))) {
            Assert.assertTrue(new String(inputStream.readAllBytes()).contains("samjs"));
        }
    }

    @Test
    public void testAddResources() throws IOException {
        // 1. load the project
        Path projectPath = RESOURCE_DIRECTORY.resolve("projects_for_resources_tests/myproject");
        BuildProject buildProject = loadBuildProject(projectPath);
        Module defaultModule = buildProject.currentPackage().getDefaultModule();

        // 2. Create and add a resource
        DocumentId documentId = DocumentId.create("config/project-info.json", defaultModule.moduleId());
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("org", buildProject.currentPackage().descriptor().org().toString());
        jsonObject.addProperty("name", buildProject.currentPackage().descriptor().name().toString());
        jsonObject.addProperty("version", buildProject.currentPackage().descriptor().version().toString());
        byte[] serialize = jsonObject.toString().getBytes();
        ResourceConfig resourceConfig = ResourceConfig.from(documentId, "config/project-info.json", serialize);
        // Should we throw an unsupported exception for SingleFileProject??
        defaultModule.modify().addResource(resourceConfig).apply();

        // 3. Compile and generate caches and executable
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_11);
        Path execPath = buildProject.sourceRoot().resolve(TARGET_DIR_NAME).resolve("temp.jar");
        jBallerinaBackend.emit(JBallerinaBackend.OutputType.EXEC, execPath);

        // 4. Verify the existence of resources in thin jar, testable jar and executable jar
        JarFile execJar = new JarFile(execPath.toString());

        for (ModuleId moduleId : buildProject.currentPackage().moduleIds()) {
            Module module = buildProject.currentPackage().module(moduleId);
            Path moduleJarPath = buildProject.sourceRoot().resolve(TARGET_DIR_NAME).resolve(CACHES_DIR_NAME)
                    .resolve(module.descriptor().org().toString())
                    .resolve(module.descriptor().packageName().toString())
                    .resolve(module.descriptor().version().toString()).resolve("java11")
                    .resolve(module.descriptor().org().toString() + "-" + module.descriptor().name().toString() + "-"
                            + module.descriptor().version().toString() + ".jar");
            JarFile jar = new JarFile(moduleJarPath.toString());
            for (String name : getResources(buildProject.currentPackage().module(moduleId))) {
                Assert.assertNotNull(jar.getJarEntry(name));
                Assert.assertNotNull(execJar.getJarEntry(name));
            }

            Path testableJarPath = buildProject.sourceRoot().resolve(TARGET_DIR_NAME).resolve(CACHES_DIR_NAME)
                    .resolve(module.descriptor().org().toString())
                    .resolve(module.descriptor().packageName().toString())
                    .resolve(module.descriptor().version().toString()).resolve("java11")
                    .resolve(module.descriptor().org().toString() + "-" + module.descriptor().name().toString() + "-"
                            + module.descriptor().version().toString() + "-testable.jar");
            if (Files.exists(testableJarPath)) {
                JarFile testableJar = new JarFile(testableJarPath.toString());
                for (String name : getResources(buildProject.currentPackage().module(moduleId))) {
                    Assert.assertNotNull(testableJar.getJarEntry(name));
                }
                for (String name : getTestResources(buildProject.currentPackage().module(moduleId))) {
                    Assert.assertNotNull(testableJar.getJarEntry(name));
                }

            }
        }

        // Assert resources of dependencies
        for (ResolvedPackageDependency resolvedPackageDependency :
                buildProject.currentPackage().getResolution().dependencyGraph().toTopologicallySortedList()) {
            Package depPackage = resolvedPackageDependency.packageInstance();
            for (ModuleId moduleId : depPackage.moduleIds()) {
                for (String name : getResources(depPackage.module(moduleId))) {
                    Assert.assertNotNull(execJar.getJarEntry(name));
                }
            }

        }

        Path balaPath = buildProject.sourceRoot().resolve(TARGET_DIR_NAME);
        jBallerinaBackend.emit(JBallerinaBackend.OutputType.BALA, balaPath);
        JarFile bala = new JarFile(balaPath.resolve("sameera-myproject-any-0.1.0.bala").toString());

        for (ModuleId moduleId : buildProject.currentPackage().moduleIds()) {
            for (String name : getResourcesInBala(buildProject.currentPackage().module(moduleId))) {
                Assert.assertNotNull(bala.getJarEntry(name));
            }
        }
    }

    private List<String> getResources(Module module) {
        List<String> resources = new ArrayList<>();
        for (DocumentId documentId : module.resourceIds()) {
            String name = RESOURCE_DIR_NAME + "/" +
                    module.descriptor().org().toString() + "/" +
                    module.moduleName() + "/" +
                    module.descriptor().version().value().major() + "/" +
                    module.resource(documentId).name();
            resources.add(name);
        }
        return resources;
    }

    private List<String> getResourcesInBala(Module module) {
        List<String> resources = new ArrayList<>();
        for (DocumentId documentId : module.resourceIds()) {
            String name = MODULES_ROOT + "/" +
                    module.moduleName() + "/" + RESOURCE_DIR_NAME + "/" +
                    module.resource(documentId).name();
            resources.add(name);
        }
        return resources;
    }

    private List<String> getTestResources(Module module) {
        List<String> resources = new ArrayList<>();
        for (DocumentId documentId : module.testResourceIds()) {
            String name = RESOURCE_DIR_NAME + "/" +
                    module.descriptor().org().toString() + "/" +
                    module.moduleName() + "/" +
                    module.descriptor().version().value().major() + "/" +
                    module.resource(documentId).name();
            resources.add(name);
        }
        return resources;
    }

    @Test
    public void testProjectClearCaches() {
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("projects_for_refresh_tests").resolve("package_refresh_one");
        BuildProject buildProject = TestUtils.loadBuildProject(projectDirPath);
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();
        int errorCount = compilation.diagnosticResult().errorCount();
        Assert.assertEquals(errorCount, 3);

        BCompileUtil.compileAndCacheBala("projects_for_refresh_tests/package_refresh_two");
        int errorCount2 = buildProject.currentPackage().getCompilation().diagnosticResult().errorCount();
        Assert.assertEquals(errorCount2, 3);

        buildProject.clearCaches();
        int errorCount3 = buildProject.currentPackage().getCompilation().diagnosticResult().errorCount();
        Assert.assertEquals(errorCount3, 0);
    }

    @Test
    public void testProjectDuplicate() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject");
        BuildOptions.BuildOptionsBuilder optionsBuilder = BuildOptions.builder().setCodeCoverage(true);
        Project project = loadProject(projectPath, optionsBuilder.build());

        Project duplicate = project.duplicate();
        Assert.assertNotSame(project, duplicate);
        Assert.assertNotSame(project.currentPackage().project(), duplicate.currentPackage().project());
        Assert.assertNotSame(
                project.currentPackage().project().buildOptions(), duplicate.currentPackage().project().buildOptions());
        Assert.assertNotSame(project.projectEnvironmentContext(),
                duplicate.projectEnvironmentContext());
        Assert.assertNotSame(project.projectEnvironmentContext().getService(CompilerContext.class),
                duplicate.projectEnvironmentContext().getService(CompilerContext.class));
        Assert.assertNotSame(
                PackageCache.getInstance(project.projectEnvironmentContext().getService(CompilerContext.class)),
                PackageCache.getInstance(duplicate.projectEnvironmentContext().getService(CompilerContext.class)));

        Assert.assertEquals(project.sourceRoot().toString(), duplicate.sourceRoot().toString());
        Assert.assertTrue(duplicate.buildOptions().codeCoverage());
        Assert.assertFalse(duplicate.buildOptions().testReport());

        Assert.assertNotSame(project.currentPackage(), duplicate.currentPackage());
        Assert.assertEquals(project.currentPackage().packageId(), duplicate.currentPackage().packageId());
        Assert.assertTrue(project.currentPackage().moduleIds().containsAll(duplicate.currentPackage().moduleIds())
                && duplicate.currentPackage().moduleIds().containsAll(project.currentPackage().moduleIds()));
        Assert.assertEquals(project.currentPackage().packageMd().isPresent(),
                duplicate.currentPackage().packageMd().isPresent());
        if (project.currentPackage().packageMd().isPresent()) {
            Assert.assertEquals(project.currentPackage().packageMd().get().content(),
                    duplicate.currentPackage().packageMd().get().content());
        }

        for (ModuleId moduleId : project.currentPackage().moduleIds()) {
            Assert.assertNotSame(project.currentPackage().module(moduleId),
                    duplicate.currentPackage().module(moduleId));
            Assert.assertNotSame(project.currentPackage().module(moduleId).project(),
                    duplicate.currentPackage().module(moduleId).project());
            Assert.assertNotSame(project.currentPackage().module(moduleId).packageInstance(),
                    duplicate.currentPackage().module(moduleId).packageInstance());

            Assert.assertEquals(project.currentPackage().module(moduleId).descriptor(),
                    duplicate.currentPackage().module(moduleId).descriptor());
            Assert.assertEquals(project.currentPackage().module(moduleId).moduleMd().isPresent(),
                    duplicate.currentPackage().module(moduleId).moduleMd().isPresent());
            if (project.currentPackage().module(moduleId).moduleMd().isPresent()) {
                Assert.assertEquals(project.currentPackage().module(moduleId).moduleMd().get().content(),
                        duplicate.currentPackage().module(moduleId).moduleMd().get().content());
            }

            Assert.assertTrue(project.currentPackage().module(moduleId).documentIds().containsAll(
                    duplicate.currentPackage().module(moduleId).documentIds())
                    && duplicate.currentPackage().module(moduleId).documentIds().containsAll(
                    project.currentPackage().module(moduleId).documentIds()));
            for (DocumentId documentId : project.currentPackage().module(moduleId).documentIds()) {
                Assert.assertNotSame(project.currentPackage().module(moduleId).document(documentId),
                        duplicate.currentPackage().module(moduleId).document(documentId));
                Assert.assertNotSame(project.currentPackage().module(moduleId).document(documentId).module(),
                        duplicate.currentPackage().module(moduleId).document(documentId).module());
                Assert.assertNotSame(project.currentPackage().module(moduleId).document(documentId).syntaxTree(),
                        duplicate.currentPackage().module(moduleId).document(documentId).syntaxTree());

                Assert.assertEquals(project.currentPackage().module(moduleId).document(documentId).name(),
                        duplicate.currentPackage().module(moduleId).document(documentId).name());
                Assert.assertEquals(
                        project.currentPackage().module(moduleId).document(documentId).syntaxTree().toSourceCode(),
                        duplicate.currentPackage().module(moduleId).document(documentId).syntaxTree().toSourceCode());
            }

            for (DocumentId documentId : project.currentPackage().module(moduleId).testDocumentIds()) {
                Assert.assertNotSame(project.currentPackage().module(moduleId).document(documentId),
                        duplicate.currentPackage().module(moduleId).document(documentId));
                Assert.assertNotSame(project.currentPackage().module(moduleId).document(documentId).module(),
                        duplicate.currentPackage().module(moduleId).document(documentId).module());
                Assert.assertNotSame(project.currentPackage().module(moduleId).document(documentId).syntaxTree(),
                        duplicate.currentPackage().module(moduleId).document(documentId).syntaxTree());

                Assert.assertEquals(project.currentPackage().module(moduleId).document(documentId).name(),
                        duplicate.currentPackage().module(moduleId).document(documentId).name());
                Assert.assertEquals(
                        project.currentPackage().module(moduleId).document(documentId).syntaxTree().toSourceCode(),
                        duplicate.currentPackage().module(moduleId).document(documentId).syntaxTree().toSourceCode());
            }
        }

        project.currentPackage().getCompilation();
        duplicate.currentPackage().getCompilation();
    }

    @Test (description = "tests calling targetDir for Build Project")
    public void testBuildProjectTargetDir() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject");
        BuildOptions.BuildOptionsBuilder buildOptionsBuilder = BuildOptions.builder();
        buildOptionsBuilder.setSticky(false);
        BuildProject project = loadBuildProject(projectPath, buildOptionsBuilder.build());
        Path targetDirPath = project.targetDir();
        Path expectedPath = projectPath.resolve("target");
        Assert.assertEquals(targetDirPath, expectedPath);
    }

    @DataProvider(name = "provideBallerinaTomlContentForUpdates")
    public Object[][] provideBallerinaTomlContentForUpdates() {
        String myPkgDir = "my-package";
        String numericPkgDir = "1994";
        System.setProperty(USER_NAME, "testuserorg");

        String content1 =
                "";
        List<String> warnings1 =
                List.of("WARNING [Ballerina.toml:(2:1,2:1)] missing table '[package]' in 'Ballerina.toml'. " +
                        "Defaulting to:\n" +
                        "[package]\n" +
                        "org = \"testuserorg\"\n" +
                        "name = \"my_package\"\n" +
                        "version = \"0.1.0\"");

        String content2 =
                "# this is a comment\n" +
                        "\n" +
                        "[package]";
        List<String> warnings2 =
                List.of("WARNING [Ballerina.toml:(3:1,3:10)] missing key 'name' in table '[package]' in " +
                                "'Ballerina.toml'. Defaulting to 'name = \"my_package\"'",
                        "WARNING [Ballerina.toml:(3:1,3:10)] missing key 'org' in table '[package]' in " +
                                "'Ballerina.toml'. Defaulting to 'org = \"testuserorg\"'",
                        "WARNING [Ballerina.toml:(3:1,3:10)] missing key 'version' in table '[package]' in " +
                                "'Ballerina.toml'. Defaulting to 'version = \"0.1.0\"'");

        String content3 =
                "# this is a comment\n" +
                        "\n" +
                        "[package]\n" +
                        "org = \"winery\"\n" +
                        "version = \"2.0.0\"";
        List<String> warnings3 =
                List.of("WARNING [Ballerina.toml:(3:1,5:18)] missing key 'name' in table '[package]' " +
                        "in 'Ballerina.toml'. Defaulting to 'name = \"my_package\"'");

        String content4 =
                "";
        List<String> warnings4 =
                List.of("WARNING [Ballerina.toml:(2:1,2:1)] missing table '[package]' in 'Ballerina.toml'. " +
                        "Defaulting to:\n" +
                        "[package]\n" +
                        "org = \"testuserorg\"\n" +
                        "name = \"app1994\"\n" +
                        "version = \"0.1.0\"");

        String content5 =
                "[package]\n" +
                        "org = \"foo\"\n" +
                        "license = [\"MIT\", \"Apache-2.0\"]\n" +
                        "authors = [\"jo@wso2.com\", \"pramodya@wso2.com\"]\n" +
                        "repository = \"https://github.com/ballerinalang/ballerina\"\n" +
                        "keywords = [\"ballerina\", \"security\", \"crypto\"]\n" +
                        "visibility = \"private\"";
        List<String> warnings5 =
                List.of("WARNING [Ballerina.toml:(1:1,7:23)] missing key 'name' in table '[package]' in " +
                                "'Ballerina.toml'. Defaulting to 'name = \"app1994\"'",
                        "WARNING [Ballerina.toml:(1:1,7:23)] missing key 'version' in table '[package]' in " +
                                "'Ballerina.toml'. Defaulting to 'version = \"0.1.0\"'");

        String content6 =
                "[package]\n" +
                        "org = \"foo\"\n" +
                        "name = \"winery\"\n" +
                        "license = [\"MIT\", \"Apache-2.0\"]\n" +
                        "authors = [\"jo@wso2.com\", \"pramodya@wso2.com\"]\n" +
                        "repository = \"https://github.com/ballerinalang/ballerina\"\n" +
                        "keywords = [\"ballerina\", \"security\", \"crypto\"]\n" +
                        "visibility = \"private\"";
        List<String> warnings6 =
                List.of("WARNING [Ballerina.toml:(1:1,8:23)] missing key 'version' in table '[package]' in " +
                        "'Ballerina.toml'. Defaulting to 'version = \"0.1.0\"'");

        String content7 =
                "# this is a comment\n" +
                        "\n" +
                        "[package]\n" +
                        "name = \"winery\"";
        List<String> warnings7 =
                List.of("WARNING [Ballerina.toml:(3:1,4:16)] missing key 'org' in table '[package]' in " +
                                "'Ballerina.toml'. Defaulting to 'org = \"testuserorg\"'",
                        "WARNING [Ballerina.toml:(3:1,4:16)] missing key 'version' in table '[package]' in " +
                                "'Ballerina.toml'. Defaulting to 'version = \"0.1.0\"'");

        String content8 =
                "[package]\n" +
                        "version = \"1.1.0\"\n" +
                        "distribution = \"2201.0.3-SNAPSHOT\"\n" +
                        "\n" +
                        "[build-options]\n" +
                        "#observabilityIncluded = true\n" +
                        "\n" +
                        "[[app]]\n" +
                        "org = \"yo\"\n" +
                        "name = \"ro\"\n" +
                        "version = \"1.2.3\"";
        List<String> warnings8 =
                List.of("WARNING [Ballerina.toml:(1:1,3:35)] missing key 'name' in table '[package]' in " +
                                "'Ballerina.toml'. Defaulting to 'name = \"app1994\"'",
                        "WARNING [Ballerina.toml:(1:1,3:35)] missing key 'org' in table '[package]' in " +
                                "'Ballerina.toml'. Defaulting to 'org = \"testuserorg\"'");

        String content9 =
                "[package]\n" +
                        "\n" +
                        "[build-options]\n" +
                        "#observabilityIncluded = true\n" +
                        "\n" +
                        "[[app]]\n" +
                        "org = \"yo\"\n" +
                        "name = \"ro\"\n" +
                        "version = \"1.2.3\"";
        List<String> warnings9 =
                List.of("WARNING [Ballerina.toml:(1:1,1:10)] missing key 'name' in table '[package]' in " +
                                "'Ballerina.toml'. Defaulting to 'name = \"app1994\"'",
                        "WARNING [Ballerina.toml:(1:1,1:10)] missing key 'org' in table '[package]' in " +
                                "'Ballerina.toml'. Defaulting to 'org = \"testuserorg\"'",
                        "WARNING [Ballerina.toml:(1:1,1:10)] missing key 'version' in table '[package]' in " +
                                "'Ballerina.toml'. Defaulting to 'version = \"0.1.0\"'");

        String content10 =
                "[build-options]\n" +
                        "#observabilityIncluded = true\n" +
                        "\n" +
                        "[[app]]\n" +
                        "org = \"yo\"\n" +
                        "name = \"ro\"\n" +
                        "version = \"1.2.3\"";
        List<String> warnings10 =
                List.of("WARNING [Ballerina.toml:(1:1,7:18)] missing table '[package]' in 'Ballerina.toml'. " +
                        "Defaulting to:\n" +
                        "[package]\n" +
                        "org = \"testuserorg\"\n" +
                        "name = \"app1994\"\n" +
                        "version = \"0.1.0\"");

        return new Object[][]{
                {myPkgDir, content1, warnings1},
                {myPkgDir, content2, warnings2},
                {myPkgDir, content3, warnings3},
                {numericPkgDir, content4, warnings4},
                {numericPkgDir, content5, warnings5},
                {numericPkgDir, content6, warnings6},
                {myPkgDir, content7, warnings7},
                {numericPkgDir, content8, warnings8},
                {numericPkgDir, content9, warnings9},
                {numericPkgDir, content10, warnings10},
        };
    }

    @Test(description = "tests build project with uncompleted package information in Ballerina.toml",
            dataProvider = "provideBallerinaTomlContentForUpdates")
    public void testBuildProjectWithUncompletedPackageInformation(String projectDir, String balTomlContent,
                                                                  List<String> warnings)
            throws IOException {
        Path projectPath = RESOURCE_DIRECTORY.resolve("projects_for_config_file_updates").resolve(projectDir);

        // Clean project directory
        writeContent(projectPath.resolve(BALLERINA_TOML), "");
        Files.deleteIfExists(projectPath.resolve(DEPENDENCIES_TOML));
        Files.deleteIfExists(projectPath.resolve(BUILD_FILE));

        // write content to the Ballerina.toml
        writeContent(projectPath.resolve(BALLERINA_TOML), balTomlContent);

        // 1) Initialize the project instance
        BuildOptions.BuildOptionsBuilder buildOptionsBuilder = BuildOptions.builder();
        buildOptionsBuilder.setSticky(false);

        BuildProject project = loadBuildProject(projectPath, buildOptionsBuilder.build());
        project.save();

        // 2) Check compilation diagnostics
        PackageCompilation compilation = project.currentPackage().getCompilation();
        Assert.assertFalse(compilation.diagnosticResult().hasErrors());
        Assert.assertTrue(compilation.diagnosticResult().hasWarnings());
        Assert.assertEquals(compilation.diagnosticResult().diagnosticCount(), warnings.size());

        Iterator<Diagnostic> compilationWarnings = compilation.diagnosticResult().diagnostics().iterator();
        Iterator<String> expectedWarnings = warnings.iterator();
        while (compilationWarnings.hasNext() && expectedWarnings.hasNext()) {
            Assert.assertEquals(compilationWarnings.next().toString(), expectedWarnings.next());
        }

        // Clean project directory
        writeContent(projectPath.resolve(BALLERINA_TOML), "");
        Files.deleteIfExists(projectPath.resolve(DEPENDENCIES_TOML));
    }

    @Test(description = "test accessing semantic model after first build",
            dependsOnMethods = "testGetSemanticModel")
    public void testAccessSemanticModelAfterFirstBuild() throws IOException {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject");
        // Delete build file if already exists
        if (projectPath.resolve(TARGET_DIR_NAME).resolve(BUILD_FILE).toFile().exists()) {
            Files.delete(projectPath.resolve(TARGET_DIR_NAME).resolve(BUILD_FILE));
        }
        // Set sticky false, to imitate the default build command behavior
        BuildOptions.BuildOptionsBuilder buildOptionsBuilder = BuildOptions.builder();
        buildOptionsBuilder.setSticky(false);

        // 1) Initialize the project instance
        BuildProject project = loadBuildProject(projectPath, buildOptionsBuilder.build());
        // Get compilation
        PackageCompilation compilation = project.currentPackage().getCompilation();
        compilation.diagnosticResult().diagnostics().forEach(OUT::println);
        Assert.assertFalse(compilation.diagnosticResult().hasErrors());
        // Call `JBallerinaBackend`
        JBallerinaBackend.from(compilation, JvmTarget.JAVA_11);
        // BIR is not expected to be generated since the enable-cache option is not set
        Assert.assertFalse(project.targetDir().resolve(CACHES_DIR_NAME).resolve("sameera").resolve("myproject")
                .resolve("0.1.0").resolve(REPO_BIR_CACHE_NAME).resolve("myproject.bir").toFile().exists());

        // 2) Build project again with build file
        // Set temp dir as the target
        buildOptionsBuilder.targetDir(ProjectUtils.getTemporaryTargetPath());
        BuildProject projectSecondBuild = loadBuildProject(projectPath, buildOptionsBuilder.build());
        projectSecondBuild.save();
        // Get compilation
        PackageCompilation compilationSecondBuild = projectSecondBuild.currentPackage().getCompilation();
        compilationSecondBuild.diagnosticResult().diagnostics().forEach(OUT::println);
        Assert.assertFalse(compilationSecondBuild.diagnosticResult().hasErrors());
        // Get semantic model of the default module
        Module defaultModule = projectSecondBuild.currentPackage().getDefaultModule();
        compilationSecondBuild.getSemanticModel(defaultModule.moduleId());
    }

    @Test(description = "tests the order of module documents")
    public void testDocumentsOrder() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("project_documents");
        String sourceFileName = "types.bal";
        String testFileName = "tests/types.bal";
        String newFileContent = "type Integer 1|2";

        // Initialize the project and load the package
        BuildProject project = loadBuildProject(projectPath);
        Package currentPackage = project.currentPackage();

        // Obtain the service module and check the documents order at project creation
        Module serviceModule = currentPackage.module(ModuleName.from(currentPackage.packageName(), "services"));
        List<String> expectedServiceFileNames = Arrays.asList("auth.bal", "subscribe.bal", "update.bal");
        List<String> actualServiceFileNames = getDocumentFileNames(serviceModule, serviceModule.documentIds());

        assertEquals(actualServiceFileNames.size(), 3);
        assertEquals(actualServiceFileNames, expectedServiceFileNames);

        // Obtain the modifier of the default module
        Module oldDefaultModule = currentPackage.getDefaultModule();
        Module.Modifier oldDefaultModuleModifier = oldDefaultModule.modify();

        // Add types.bal and test_types.bal to the module
        oldDefaultModuleModifier.addDocument(DocumentConfig.from(DocumentId.create(
                sourceFileName, oldDefaultModule.moduleId()), newFileContent, sourceFileName));

        oldDefaultModuleModifier.addTestDocument(DocumentConfig.from(DocumentId.create(
                testFileName, oldDefaultModule.moduleId()), newFileContent, testFileName));

        Module newDefaultModule = oldDefaultModuleModifier.apply();

        // Check the documents order after adding documents to a module
        // Check the order of source documents
        List<String> expectedSourceFileNames = Arrays.asList("main.bal", sourceFileName, "utils.bal");
        List<String> actualSourceFileNames = getDocumentFileNames(newDefaultModule, newDefaultModule.documentIds());

        assertEquals(actualSourceFileNames.size(), 3);
        assertEquals(actualSourceFileNames, expectedSourceFileNames);

        // Check the order of test documents
        List<String> expectedTestFileNames = Arrays.asList(
                "tests/main_test.bal",
                testFileName,
                "tests/utils_test.bal");
        List<String> actualTestFileNames = getDocumentFileNames(newDefaultModule, newDefaultModule.testDocumentIds());

        assertEquals(actualTestFileNames.size(), 3);
        assertEquals(actualTestFileNames, expectedTestFileNames);

        // Check the order of diagnostics
        PackageCompilation compilation = currentPackage.getCompilation();

        List<String> actualDiagnosticPaths = compilation.diagnosticResult().diagnostics().stream().map(diagnostic ->
                diagnostic.location().lineRange().filePath()).distinct().collect(Collectors.toList());

        List<String> expectedDiagnosticPaths = Arrays.asList(
                "main.bal", Paths.get("tests").resolve("main_test.bal").toString(),
                Paths.get("tests").resolve("utils_test.bal").toString(), "utils.bal",
                Paths.get("modules").resolve("services").resolve("auth.bal").toString(),
                Paths.get("modules").resolve("services").resolve("subscribe.bal").toString(),
                Paths.get("modules").resolve("services").resolve("update.bal").toString(),
                Paths.get("modules").resolve("storage").resolve("db.bal").toString(),
                Paths.get("modules").resolve("storage").resolve("tests").resolve("db_test.bal").toString(),
                Paths.get("modules").resolve("utils").resolve("utils.bal").toString());

        assertEquals(actualDiagnosticPaths.size(), 10);
        assertEquals(actualDiagnosticPaths, expectedDiagnosticPaths);
    }

    private List<String> getDocumentFileNames(Module module, Collection<DocumentId> documentIds) {
        List<String> actualFileNames = new ArrayList<>();
        for (DocumentId documentId : documentIds) {
            actualFileNames.add(module.document(documentId).name());
        }
        return actualFileNames;
    }

    @Test (description = "tests jar resolution for Build Project")
    public void testConflictingJars() {
        Path dep1Path = RESOURCE_DIRECTORY.resolve("conflicting_jars_test/platformLibPkg1").toAbsolutePath();
        Path dep2Path = RESOURCE_DIRECTORY.resolve("conflicting_jars_test/platformLibPkg2").toAbsolutePath();
        Path customUserHome = Paths.get("build", "userHome");
        Environment environment = EnvironmentBuilder.getBuilder().setUserHome(customUserHome).build();
        ProjectEnvironmentBuilder envBuilder = ProjectEnvironmentBuilder.getBuilder(environment);

        CompileResult compileResult = BCompileUtil.compileAndCacheBala(dep1Path.toString(), CENTRAL_CACHE, envBuilder);
        if (compileResult.getDiagnosticResult().hasErrors()) {
            Assert.fail("unexpected diagnostics found when caching platformLibPkg1:\n"
                    + getErrorsAsString(compileResult.getDiagnosticResult()));
        }
        compileResult = BCompileUtil.compileAndCacheBala(dep2Path.toString(), CENTRAL_CACHE, envBuilder);
        if (compileResult.getDiagnosticResult().hasErrors()) {
            Assert.fail("unexpected diagnostics found when caching platformLibPkg2:\n"
                    + getErrorsAsString(compileResult.getDiagnosticResult()));
        }

        Path projectPath = RESOURCE_DIRECTORY.resolve("conflicting_jars_test/platformLibPkg3");
        BuildProject project = TestUtils.loadBuildProject(envBuilder, projectPath);
        PackageCompilation compilation = project.currentPackage().getCompilation();
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_11);
        if (jBallerinaBackend.diagnosticResult().hasErrors()) {
            Assert.fail("unexpected compilation failure:\n" + getErrorsAsString(compilation.diagnosticResult()));
        }
        Collection<JarLibrary> jarLibraries =
                jBallerinaBackend.jarResolver().getJarFilePathsRequiredForExecution();
        Assert.assertEquals(jarLibraries.size(), 9);

        Assert.assertTrue(jarLibraries.contains(new JarLibrary(
                CENTRAL_CACHE.resolve("bala/ballerina/platformLibPkg2/0.1.0/java11/platform/java11/native1.txt"),
                PlatformLibraryScope.DEFAULT, "native1", "org.ballerina", "1.0.1", "ballerina/platformLibPkg2")));
        Assert.assertTrue(jarLibraries.contains(new JarLibrary(
                CENTRAL_CACHE.resolve("bala/ballerina/platformLibPkg1/0.1.0/java11/platform/java11/lib1.txt"),
                PlatformLibraryScope.DEFAULT, "lib1", "org.apache", "2.0.0", "ballerina/platformLibPkg1")));
        Assert.assertTrue(jarLibraries.contains(new JarLibrary(
                CENTRAL_CACHE.resolve("bala/ballerina/platformLibPkg1/0.1.0/java11/platform/java11/lib2.txt"),
                PlatformLibraryScope.DEFAULT))
                || jarLibraries.contains(new JarLibrary(
                CENTRAL_CACHE.resolve("bala/ballerina/platformLibPkg2/0.1.0/java11/platform/java11/lib2.txt"),
                PlatformLibraryScope.DEFAULT)));
        Assert.assertTrue(jarLibraries.contains(new JarLibrary(
                CENTRAL_CACHE.resolve("bala/ballerina/platformLibPkg2/0.1.0/java11/platform/java11/lib3.txt"),
                PlatformLibraryScope.DEFAULT,
                "lib3", "org.apache", "2.0.1", "ballerina/platformLibPkg2")));
        Assert.assertTrue(jarLibraries.contains(new JarLibrary(
                CENTRAL_CACHE.resolve("bala/ballerina/platformLibPkg2/0.1.0/java11/platform/java11/lib4.txt"),
                PlatformLibraryScope.DEFAULT)));
        Assert.assertTrue(jarLibraries.contains(new JarLibrary(
                Paths.get("src/test/resources/conflicting_jars_test/platformLibPkg3/" +
                        "target/cache/user/platformLibPkg3/0.1.0/java11/user-platformLibPkg3-0.1.0.jar"),
                PlatformLibraryScope.DEFAULT)));
        Assert.assertTrue(jarLibraries.contains(new JarLibrary(
                CENTRAL_CACHE.resolve(System.getProperty("project.version") +
                        "/ballerina/platformLibPkg1/0.1.0/java11/ballerina-platformLibPkg1-0.1.0.jar"),
                PlatformLibraryScope.DEFAULT)));
        Assert.assertTrue(jarLibraries.contains(new JarLibrary(
                CENTRAL_CACHE.resolve(System.getProperty("project.version") +
                        "/ballerina/platformLibPkg2/0.1.0/java11/ballerina-platformLibPkg2-0.1.0.jar"),
                PlatformLibraryScope.DEFAULT)));
        Assert.assertTrue(jarLibraries.contains(new JarLibrary(
                CENTRAL_CACHE.resolve(System.getProperty("ballerina.home") +
                        "bre/lib/ballerina-rt-" + System.getProperty("project.version") + ".jar"),
                PlatformLibraryScope.DEFAULT)));
    }

    @Test (description = "tests jar resolution with non ballerina packages for Build Project")
    public void testConflictingJarsInNonBalPackages() {
        Path dep1Path = RESOURCE_DIRECTORY.resolve("conflicting_jars_test/platformLibNonBalPkg1").toAbsolutePath();
        Path dep2Path = RESOURCE_DIRECTORY.resolve("conflicting_jars_test/platformLibNonBalPkg2").toAbsolutePath();
        Path customUserHome = Paths.get("build", "userHome");
        Environment environment = EnvironmentBuilder.getBuilder().setUserHome(customUserHome).build();
        ProjectEnvironmentBuilder envBuilder = ProjectEnvironmentBuilder.getBuilder(environment);

        CompileResult compileResult = BCompileUtil.compileAndCacheBala(dep1Path.toString(), CENTRAL_CACHE, envBuilder);
        if (compileResult.getDiagnosticResult().hasErrors()) {
            Assert.fail("unexpected diagnostics found when caching platformLibNonBalPkg1:\n"
                    + getErrorsAsString(compileResult.getDiagnosticResult()));
        }
        compileResult = BCompileUtil.compileAndCacheBala(dep2Path.toString(), CENTRAL_CACHE, envBuilder);
        if (compileResult.getDiagnosticResult().hasErrors()) {
            Assert.fail("unexpected diagnostics found when caching platformLibNonBalPkg2:\n"
                    + getErrorsAsString(compileResult.getDiagnosticResult()));
        }

        Path projectPath = RESOURCE_DIRECTORY.resolve("conflicting_jars_test/platformLibNonBalPkg3");
        BuildProject project = TestUtils.loadBuildProject(envBuilder, projectPath);
        PackageCompilation compilation = project.currentPackage().getCompilation();
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_11);
        if (jBallerinaBackend.diagnosticResult().hasErrors()) {
            Assert.fail("unexpected compilation failure:\n" + getErrorsAsString(compilation.diagnosticResult()));
        }

        EmitResult emitResult = jBallerinaBackend.emit(JBallerinaBackend.OutputType.EXEC, Paths.get("test.jar"));

        Assert.assertFalse(emitResult.diagnostics().hasErrors());
        Assert.assertTrue(emitResult.diagnostics().hasWarnings());
        Assert.assertEquals(emitResult.diagnostics().warningCount(), 2);

        ArrayList<Diagnostic> diagnostics =
                new ArrayList<>(emitResult.diagnostics().diagnostics());
        Assert.assertEquals(diagnostics.get(0).toString(), "WARNING [platformLibNonBalPkg3] detected conflicting jar" +
                " files. 'native1-1.0.1.jar' dependency of 'platformlib/pkg2' conflicts with 'native1-1.0.0.jar'" +
                " dependency of 'platformlib/pkg1'. Picking 'native1-1.0.1.jar' over 'native1-1.0.0.jar'.");
        Assert.assertEquals(diagnostics.get(1).toString(), "WARNING [platformLibNonBalPkg3] detected conflicting jar" +
                " files. 'lib3-2.0.1.jar' dependency of 'platformlib/pkg2' conflicts with 'lib3-2.0.0.jar'" +
                " dependency of 'platformlib/pkg1'. Picking 'lib3-2.0.1.jar' over 'lib3-2.0.0.jar'.");
    }

    private static BuildProject loadBuildProject(Path projectPath) {
        return loadBuildProject(projectPath, null);
    }

    private static BuildProject loadBuildProject(Path projectPath, BuildOptions buildOptions) {
        BuildProject buildProject = null;
        try {
            if (buildOptions == null) {
                buildProject = TestUtils.loadBuildProject(projectPath);
            } else {
                buildProject = TestUtils.loadBuildProject(projectPath, buildOptions);
            }
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        return buildProject;
    }
}
