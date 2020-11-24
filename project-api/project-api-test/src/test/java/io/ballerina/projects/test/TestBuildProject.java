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
import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentConfig;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JdkVersion;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleCompilation;
import io.ballerina.projects.ModuleConfig;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.PackageResolution;
import io.ballerina.projects.PlatformLibrary;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.tools.text.LinePosition;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JdkVersion.JAVA_11);
        DiagnosticResult diagnosticResult = jBallerinaBackend.diagnosticResult();

        Assert.assertEquals(diagnosticResult.diagnosticCount(), 1);

        Collection<PlatformLibrary> platformLibraries = jBallerinaBackend
                                                            .platformLibraryDependencies(currentPackage.packageId());
        Assert.assertEquals(platformLibraries.size(), 1);
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

        projectPath = RESOURCE_DIRECTORY.resolve("single-file");
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
        BuildProject buildProject = (BuildProject) ProjectLoader.loadProject(filePath);
        Optional<DocumentId> oldDocumentId = ProjectLoader.getDocumentId(filePath, buildProject); // get the document ID
        Assert.assertTrue(oldDocumentId.isPresent());
        Module oldModule = buildProject.currentPackage().module(oldDocumentId.get().moduleId());
        Document oldDocument = oldModule.document(oldDocumentId.get());

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
        Assert.assertEquals(updatedPackage.module(oldDocument.module().moduleId()).document(oldDocumentId.get()),
                updatedDoc);
        Assert.assertEquals(updatedPackage, updatedDoc.module().packageInstance());
    }

    @Test
    public void testUpdateTestDocument() {
        // Inputs from langserver
        Path filePath = RESOURCE_DIRECTORY.resolve("myproject").resolve(ProjectConstants.TEST_DIR_NAME)
                .resolve("main_tests.bal").toAbsolutePath();

        // Load the project from document filepath
        BuildProject buildProject = (BuildProject) ProjectLoader.loadProject(filePath);
        Optional<DocumentId> oldDocumentId = ProjectLoader.getDocumentId(filePath, buildProject); // get the document ID
        Assert.assertTrue(oldDocumentId.isPresent());
        Module oldModule = buildProject.currentPackage().module(oldDocumentId.get().moduleId());
        Document oldDocument = oldModule.document(oldDocumentId.get());

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
        Assert.assertEquals(updatedPackage.module(oldDocument.module().moduleId()).document(oldDocumentId.get()),
                updatedDoc);
        Assert.assertEquals(updatedPackage, updatedDoc.module().packageInstance());
    }

    @Test
    public void testAddDocument() {
        Path filePath = RESOURCE_DIRECTORY.resolve("myproject").resolve("db.bal").toAbsolutePath();
        String newFileContent = "import ballerina/io;\n";

        BuildProject buildProject = (BuildProject) ProjectLoader.loadProject(filePath);
        ModuleId moduleId = null;
        for (ModuleId moduleId1 : buildProject.currentPackage().moduleIds()) {
            Optional<Path> optionalPath = buildProject.modulePath(moduleId1);
            Assert.assertTrue(optionalPath.isPresent());
            if (optionalPath.get().equals(filePath.getParent())) {
                moduleId = moduleId1;
            }
        }
        Assert.assertNotNull(moduleId);
        DocumentId newDocumentId = DocumentId.create(filePath.toString(), moduleId); // create a new document ID
        Module oldModule = buildProject.currentPackage().module(Objects.requireNonNull(newDocumentId).moduleId());

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
        Path filePath =
                RESOURCE_DIRECTORY.resolve("myproject").resolve(ProjectConstants.TEST_DIR_NAME).resolve("db_test.bal")
                        .toAbsolutePath();
        String newFileContent = "import ballerina/io;\n";

        BuildProject buildProject = (BuildProject) ProjectLoader.loadProject(filePath);

        ModuleId moduleId = null;
        for (ModuleId moduleId1 : buildProject.currentPackage().moduleIds()) {
            Optional<Path> optionalPath = buildProject.modulePath(moduleId1);
            Assert.assertTrue(optionalPath.isPresent());
            if (optionalPath.get().equals(filePath.getParent().getParent())) {
                moduleId = moduleId1;
            }
        }
        Assert.assertNotNull(moduleId);
        DocumentId newTestDocumentId = DocumentId.create(filePath.toString(), moduleId); // create a new document ID
        Module oldModule = buildProject.currentPackage().module(Objects.requireNonNull(newTestDocumentId).moduleId());

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

        BuildProject buildProject = (BuildProject) ProjectLoader.loadProject(filePath);
        Optional<DocumentId> removeDocumentId = ProjectLoader.getDocumentId(filePath, buildProject);
        Assert.assertTrue(removeDocumentId.isPresent());
        Module oldModule = buildProject.currentPackage().module(removeDocumentId.get().moduleId());

        Module newModule = oldModule.modify().removeDocument(removeDocumentId.get()).apply();

        Assert.assertEquals((oldModule.documentIds().size() - 1), newModule.documentIds().size());
        Assert.assertEquals(oldModule.testDocumentIds().size(), newModule.testDocumentIds().size());
        Assert.assertTrue(oldModule.documentIds().contains(removeDocumentId.get()));
        Assert.assertFalse(newModule.documentIds().contains(removeDocumentId.get()));
        for (DocumentId documentId : oldModule.documentIds()) {
            if (documentId == removeDocumentId.get()) {
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

        BuildProject buildProject = (BuildProject) ProjectLoader.loadProject(filePath);
        Optional<DocumentId> removeDocumentId = ProjectLoader.getDocumentId(filePath, buildProject);
        Assert.assertTrue(removeDocumentId.isPresent());
        Module oldModule = buildProject.currentPackage().module(removeDocumentId.get().moduleId());

        Module newModule = oldModule.modify().removeDocument(removeDocumentId.get()).apply();

        Assert.assertEquals((oldModule.testDocumentIds().size() - 1), newModule.testDocumentIds().size());
        Assert.assertEquals(oldModule.documentIds().size(), newModule.documentIds().size());
        Assert.assertTrue(oldModule.testDocumentIds().contains(removeDocumentId.get()));
        Assert.assertFalse(newModule.testDocumentIds().contains(removeDocumentId.get()));
        for (DocumentId documentId : oldModule.testDocumentIds()) {
            if (documentId == removeDocumentId.get()) {
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
                Collections.emptyList(), Collections.emptyList());
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
                Collections.singletonList(testDocumentConfig), Collections.emptyList());
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
    public void testRemoveModule() {
        Path filePath =
                RESOURCE_DIRECTORY.resolve("myproject").resolve(ProjectConstants.MODULES_ROOT).resolve("storage")
                        .toAbsolutePath();
        BuildProject buildProject = (BuildProject) ProjectLoader.loadProject(filePath);
        Package oldPackage = buildProject.currentPackage();
        ModuleId removeId = null;
        for (ModuleId moduleId : oldPackage.moduleIds()) {
            Optional<Path> optionalPath = buildProject.modulePath(moduleId);
            Assert.assertTrue(optionalPath.isPresent());
            if (optionalPath.get().equals(filePath)) {
                removeId = moduleId;
            }
        }
        Package newPackage = oldPackage.modify().removeModule(removeId).apply();

        Assert.assertEquals(newPackage.moduleIds().size(), (oldPackage.moduleIds().size() - 1));
        Assert.assertEquals(newPackage.moduleIds().size(), 2);
        Assert.assertTrue(oldPackage.moduleIds().contains(removeId));
        Assert.assertFalse(newPackage.moduleIds().contains(removeId));

        for (ModuleId moduleId : oldPackage.moduleIds()) {
            if (moduleId == removeId) {
                Assert.assertFalse(newPackage.moduleIds().contains(moduleId));
            } else {
                Assert.assertTrue(newPackage.moduleIds().contains(moduleId));
            }
        }
    }

    @Test
    public void testAccessNonExistingDocument() {
        Path filePath = RESOURCE_DIRECTORY.resolve("myproject").resolve("db.bal").toAbsolutePath();

        // Load the project from document filepath
        BuildProject buildProject = (BuildProject) ProjectLoader.loadProject(filePath);
        Optional<DocumentId> oldDocumentId = ProjectLoader.getDocumentId(filePath, buildProject); // get the document ID
        Assert.assertFalse(oldDocumentId.isPresent());
    }

    @Test
    public void testLoadFromNonExistentModule() {
        Path filePath =
                RESOURCE_DIRECTORY.resolve("myproject").resolve(ProjectConstants.MODULES_ROOT)
                        .resolve("db").resolve("main.bal").toAbsolutePath();

        try {
            BuildProject buildProject = (BuildProject) ProjectLoader.loadProject(filePath);
        } catch (ProjectException e) {
            Assert.assertTrue(e.getMessage().contains("module directory path does not exist"));
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

        // Test module level symbols
        List<Symbol> symbols = semanticModel.moduleLevelSymbols();
        Assert.assertEquals(symbols.size(), 5);

        // Test symbol
        Optional<Symbol> symbol = semanticModel.symbol("main.bal", LinePosition.from(5, 10));
        symbol.ifPresent(value -> assertEquals(value.name(), "runServices"));
    }
}
