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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.projects.BallerinaToml;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.BuildOptionsBuilder;
import io.ballerina.projects.CloudToml;
import io.ballerina.projects.DependenciesToml;
import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentConfig;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleCompilation;
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
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.toml.semantic.ast.TomlTableArrayNode;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.tools.text.LinePosition;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ballerina.projects.test.TestUtils.isWindows;
import static io.ballerina.projects.test.TestUtils.resetPermissions;
import static org.testng.Assert.assertEquals;

/**
 * Contains cases to test the basic package structure.
 *
 * @since 2.0.0
 */
public class TestBuildProject {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/");
    private final String dummyContent = "function foo() {\n}";

    @Test (description = "tests loading a valid build project")
    public void testBuildProjectAPI() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject");

        // 1) Initialize the project instance
        BuildProject project = null;
        try {
            project = BuildProject.load(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
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
            for (Document doc : module.documents()) {
                Assert.assertNotNull(doc.syntaxTree());
            }
        }

        Assert.assertEquals(noOfSrcDocuments, 4);
        Assert.assertEquals(noOfTestDocuments, 3);

    }

    @Test (description = "tests loading a build project with no read permission")
    public void testBuildProjectWithNoReadPermission() {
        // Skip test in windows due to file permission setting issue
        if (isWindows()) {
            throw new SkipException("Skipping tests on Windows");
        }

        Path projectPath = RESOURCE_DIRECTORY.resolve("project_no_permission");

        // 1) Remove read permission
        boolean readable = projectPath.toFile().setReadable(false, true);
        if (!readable) {
            Assert.fail("could not remove read permission");
        }

        // 2) Initialize the project instance
        BuildProject project = null;
        try {
            project = BuildProject.load(projectPath);
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

        Path projectPath = RESOURCE_DIRECTORY.resolve("project_no_permission");

        // 1) Remove write permission
        boolean writable = projectPath.toFile().setWritable(false, true);
        if (!writable) {
            Assert.fail("could not remove write permission");
        }

        // 2) Initialize the project instance
        BuildProject project = null;
        try {
            project = BuildProject.load(projectPath);
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

    @Test(description = "tests package compilation", enabled = false)
    public void testPackageCompilation() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("test_proj_pkg_compilation");

        // 1) Initialize the project instance
        BuildProject project = null;
        try {
            project = BuildProject.load(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        // 2) Load the package
        Package currentPackage = project.currentPackage();

        // 3) Compile the current package
        PackageCompilation compilation = currentPackage.getCompilation();

        // The current package has 4 modules and each module has one semantic or syntactic error.
        // This shows that all 4 modules has been compiled, even though the `utils`
        //   module is not imported by any of the other modules.
        Assert.assertEquals(compilation.diagnosticResult().diagnosticCount(), 4);
    }

    @Test(description = "tests package diagnostics")
    public void testDiagnostics() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("test_proj_pkg_compilation");

        // 1) Initialize the project instance
        BuildProject project = null;
        try {
            project = BuildProject.load(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
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

        // Verify paths in jBallerina backend diagnostics
        diagnosticFilePaths = jBallerinaBackend.diagnosticResult().diagnostics().stream().map(diagnostic ->
                diagnostic.location().lineRange().filePath()).distinct().collect(Collectors.toList());

        for (String path : expectedPaths) {
            Assert.assertTrue(diagnosticFilePaths.contains(path), diagnosticFilePaths.toString());
        }
    }

    @Test(description = "tests codegen with native libraries", enabled = false)
    public void testJBallerinaBackend() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("test_proj_pkg_compilation_simple");

        // 1) Initialize the project instance
        BuildProject project = null;
        try {
            project = BuildProject.load(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        // 2) Load the package
        Package currentPackage = project.currentPackage();

        // 3) Compile the current package
        PackageCompilation compilation = currentPackage.getCompilation();
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_11);
        DiagnosticResult diagnosticResult = jBallerinaBackend.diagnosticResult();

        Assert.assertEquals(diagnosticResult.diagnosticCount(), 1);

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
        BuildProject project = null;
        try {
            project = BuildProject.load(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        // 2) Load the package
        Package currentPackage = project.currentPackage();

        // 3) Compile the current package
        PackageCompilation compilation = currentPackage.getCompilation();

        // The current package has e test modules and each module has one semantic or syntactic error.
        // This shows that all 3 modules has been compiled
        Assert.assertEquals(compilation.diagnosticResult().diagnosticCount(), 3);
    }

    @Test(description = "tests loading a valid build project using project compilation", enabled = false)
    public void testBuildProjectAPIWithPackageCompilation() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject");

        // 1) Initialize the project instance
        BuildProject project = null;
        try {
            project = BuildProject.load(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
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
            BuildProject.load(projectPath);
        } catch (ProjectException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid Ballerina package directory: " + projectPath));
        }

        projectPath = RESOURCE_DIRECTORY.resolve("myproject").resolve("modules").resolve("services");
        try {
            BuildProject.load(projectPath);
        } catch (ProjectException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid Ballerina package directory: " + projectPath));
        }

        projectPath = RESOURCE_DIRECTORY.resolve("single_file");
        try {
            BuildProject.load(projectPath);
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
            BuildProject.load(projectPath);
            Assert.fail("expected an invalid project exception");
        } catch (ProjectException e) {
            Assert.assertTrue(e.getMessage().contains("Provided path is already within a Ballerina package: " +
                    projectPath));
        }
    }

    @Test(enabled = false, description = "tests loading a valid build project with build options from toml")
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
        Assert.assertTrue(project.buildOptions().experimental());
        Assert.assertFalse(project.buildOptions().testReport());
    }

    @Test(enabled = false, description = "tests loading a valid build project with build options from toml")
    public void testOverrideBuildOptions() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("projectWithBuildOptions");
        // 1) Initialize the project instance
        BuildProject project = null;
        BuildOptions buildOptions = new BuildOptionsBuilder().skipTests(false).build();
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
        Assert.assertTrue(project.buildOptions().experimental());
        Assert.assertFalse(project.buildOptions().testReport());
    }

    @Test
    public void testUpdateDocument() {
        // Inputs from langserver
        Path filePath = RESOURCE_DIRECTORY.resolve("myproject").resolve("main.bal").toAbsolutePath();

        // Load the project from document filepath
        Project buildProject = ProjectLoader.loadProject(filePath);
        DocumentId oldDocumentId = buildProject.documentId(filePath); // get the document ID
        Module oldModule = buildProject.currentPackage().module(oldDocumentId.moduleId());
        Document oldDocument = oldModule.document(oldDocumentId);

        // Update the document
        Document updatedDoc = oldDocument.modify().withContent(dummyContent).apply();

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
        Project buildProject = ProjectLoader.loadProject(filePath);
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

        BuildProject buildProject = (BuildProject) ProjectLoader.loadProject(projectPath);

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

        BuildProject buildProject = (BuildProject) ProjectLoader.loadProject(projectPath);

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

        Project buildProject = ProjectLoader.loadProject(filePath);
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

        Project buildProject = ProjectLoader.loadProject(filePath);
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
        BuildProject buildProject = (BuildProject) ProjectLoader.loadProject(projectRoot);
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
        BuildProject buildProject = (BuildProject) ProjectLoader.loadProject(projectRoot);
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
        Project buildProject = ProjectLoader.loadProject(projectPath);
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
            BuildProject buildProject = (BuildProject) ProjectLoader.loadProject(filePath);
        } catch (ProjectException e) {
            Assert.assertTrue(e.getMessage().contains("provided file path does not exist"));
        }
    }

    @Test(description = "tests get semantic model in module compilation", enabled = false)
    public void testGetSemanticModel() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject");

        // 1) Initialize the project instance
        BuildProject project = null;
        try {
            project = BuildProject.load(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        // 2) Load the package
        Package currentPackage = project.currentPackage();
        // 3) Load the default module
        Module defaultModule = currentPackage.getDefaultModule();
        Assert.assertEquals(defaultModule.documentIds().size(), 2);

        // 5) Compile the module
        ModuleCompilation compilation = defaultModule.getCompilation();

        // 6) Get semantic model
        SemanticModel semanticModel = compilation.getSemanticModel();

        // 7) Get the document
        Document srcFile = null;
        for (Document doc : defaultModule.documents()) {
            if (doc.name().equals("main.bal")) {
                srcFile = doc;
                break;
            }
        }

        if (srcFile == null) {
            Assert.fail("Source file 'main.bal' not found");
        }

        // Test module level symbols
        List<Symbol> symbols = semanticModel.moduleSymbols();
        Assert.assertEquals(symbols.size(), 5);

        // Test symbol
        Optional<Symbol> symbol = semanticModel.symbol(srcFile, LinePosition.from(5, 10));
        symbol.ifPresent(value -> assertEquals(value.getName().get(), "runServices"));
    }

    @Test(description = "tests if other documents exists ie. Ballerina.toml, Package.md", enabled = true)
    public void testOtherDocuments() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject");

        // 1) Initialize the project instance
        BuildProject project = null;
        try {
            project = BuildProject.load(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        // 2) Check if files exists
        Package currentPackage = project.currentPackage();
        Assert.assertTrue(currentPackage.ballerinaToml().isPresent());
        Assert.assertTrue(currentPackage.dependenciesToml().isPresent());
        Assert.assertTrue(currentPackage.cloudToml().isPresent());
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
        Assert.assertEquals(dependenciesToml.entries().size(), 1);

        TomlTableNode cloudToml = currentPackage.cloudToml().get().tomlAstNode();
        Assert.assertEquals(cloudToml.entries().size(), 1);
    }

    @Test(description = "tests if other documents can be edited ie. Ballerina.toml, Package.md")
    public void testOtherDocumentModify() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject");

        // 1) Initialize the project instance
        BuildProject project = null;
        try {
            project = BuildProject.load(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        // 2) Check editing files
        BallerinaToml newBallerinaToml = project.currentPackage().ballerinaToml().get().modify().withContent("" +
                "[package]\n" +
                "org = \"sameera\"\n" +
                "name = \"yourproject\"\n" +
                "version = \"0.1.0\"\n" +
                "[sample]\n" +
                "test = \"attribute\"").apply();
        TomlTableNode ballerinaToml = newBallerinaToml.tomlAstNode();
        Assert.assertEquals(ballerinaToml.entries().size(), 2);
        Package newPackage = newBallerinaToml.packageInstance();
        Assert.assertEquals(newPackage.packageName().toString(), "yourproject");
        PackageCompilation compilation = newPackage.getCompilation();

        DependenciesToml newDependenciesToml = project.currentPackage().dependenciesToml()
                .get().modify().withContent("" +
                "[[dependency]]\n" +
                "org = \"samjs\"\n" +
                "name = \"package_k\"\n" +
                "version = \"1.1.0-alpha\"\n" +
                "[[dependency]]\n" +
                "org = \"samjs\"\n" +
                "name = \"package_p\"\n" +
                "version = \"1.1.0-alpha\"").apply();
        TomlTableNode dependenciesToml = newDependenciesToml.tomlAstNode();
        Assert.assertEquals(((TomlTableArrayNode) dependenciesToml.entries().get("dependency")).children().size(), 2);

        CloudToml newCloudToml = project.currentPackage().cloudToml().get().modify().withContent("" +
                "[test]\n" +
                "attribute = \"value\"\n" +
                "[test2]\n" +
                "attribute = \"value2\"").apply();
        TomlTableNode cloudToml = newCloudToml.tomlAstNode();
        Assert.assertEquals(cloudToml.entries().size(), 2);

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
        project.currentPackage().getDefaultModule().modify().removeModuleMd().apply();

        Assert.assertTrue(project.currentPackage().packageMd().isEmpty());
        Assert.assertTrue(project.currentPackage().cloudToml().isEmpty());
        Assert.assertTrue(project.currentPackage().dependenciesToml().isEmpty());
        Assert.assertTrue(project.currentPackage().getDefaultModule().moduleMd().isEmpty());
    }

    @Test(description = "tests adding Dependencies.toml, Package.md", enabled = true)
    public void testOtherDocumentAdd() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("project_without_k8s");

        // 1) Initialize the project instance
        BuildProject project = null;
        try {
            project = BuildProject.load(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        // 2) Check editing files
        Package currentPackage = project.currentPackage();

        Assert.assertTrue(currentPackage.dependenciesToml().isEmpty());
        Assert.assertTrue(currentPackage.cloudToml().isEmpty());
        // Assert.assertTrue(currentPackage.packageMd().isEmpty());

        DocumentConfig dependenciesToml = DocumentConfig.from(
                DocumentId.create(ProjectConstants.DEPENDENCIES_TOML, null),
                        "[[dependency]]\n" +
                        "org = \"samjs\"\n" +
                        "name = \"package_k\"\n" +
                        "version = \"1.1.0-alpha\"\n" +
                        "[[dependency]]\n" +
                        "org = \"samjs\"\n" +
                        "name = \"package_p\"\n" +
                        "version = \"1.1.0-alpha\"",
                    ProjectConstants.DEPENDENCIES_TOML
                );

        currentPackage = currentPackage.modify().addDependenciesToml(dependenciesToml).apply();
        TomlTableNode dependenciesTomlTable = currentPackage.dependenciesToml().get().tomlAstNode();
        Assert.assertEquals(((TomlTableArrayNode) dependenciesTomlTable.entries()
                .get("dependency")).children().size(), 2);

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
                .get("dependency")).children().size(), 2);

    }

    @Test(description = "tests if other documents can be edited ie. Ballerina.toml, Package.md", enabled = true)
    public void testOtherMinimalistProjectEdit() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject_minimalist");

        // 1) Initialize the project instance
        BuildProject project = null;
        try {
            project = BuildProject.load(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
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
        Path projectPath = RESOURCE_DIRECTORY.resolve("project_for_module_edit_test");
        String updatedFunctionStr = "public function concatStrings(string a, string b, string c) returns string {\n" +
                "\treturn a + b;\n" +
                "}\n";

        // 1) Initialize the project instance
        BuildProject project = null;
        try {
            project = BuildProject.load(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
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
        Path projectPath = RESOURCE_DIRECTORY.resolve("project_for_module_edit_test");

        // 1) Initialize the project instance
        BuildProject project = null;
        try {
            project = BuildProject.load(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
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
        Path projectPath = RESOURCE_DIRECTORY.resolve("project_for_module_edit_test2");
        String updatedFunctionStr = "public function concatStrings(string a, string b) returns string {\n" +
                "\treturn a + b;\n" +
                "}\n";

        // 1) Initialize the project instance
        BuildProject project = null;
        try {
            project = BuildProject.load(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
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

    @AfterClass (alwaysRun = true)
    public void reset() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("project_no_permission");
        TestUtils.resetPermissions(projectPath);
    }
}
