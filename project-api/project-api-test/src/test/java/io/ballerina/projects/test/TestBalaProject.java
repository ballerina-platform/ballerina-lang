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

import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.EmitResult;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.ModuleMd;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.PackageMd;
import io.ballerina.projects.PackageResolution;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.bala.BalaProject;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.internal.model.CompilerPluginDescriptor;
import io.ballerina.projects.internal.model.Target;
import io.ballerina.projects.repos.TempDirCompilationCache;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * Contains cases to test the load bala project.
 *
 * @since 2.0.0
 */
public class TestBalaProject {

    private static final Path RESOURCE_DIRECTORY = Paths.get("src", "test", "resources");

    @Test(description = "tests loading a valid bala project")
    public void testBalaProjectAPI() {
        Path balaPath = RESOURCE_DIRECTORY.resolve("balaloader").resolve("foo-winery-any-0.1.0.bala");
        // 1) Initialize the project instance
        BalaProject balaProject = null;
        try {
            ProjectEnvironmentBuilder defaultBuilder = ProjectEnvironmentBuilder.getDefaultBuilder();
            defaultBuilder.addCompilationCacheFactory(TempDirCompilationCache::from);
            balaProject = BalaProject.loadProject(defaultBuilder, balaPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
        // 2) Load the package
        Package currentPackage = balaProject.currentPackage();

        // Package descriptor
        Assert.assertEquals(currentPackage.descriptor().org().value(), "foo");
        Assert.assertEquals(currentPackage.descriptor().name().value(), "winery");
        Assert.assertEquals(currentPackage.descriptor().version().value().toString(), "0.1.0");

        // Package Manifest
        PackageManifest manifest = currentPackage.manifest();
        Assert.assertEquals(manifest.authors().size(), 1);
        Assert.assertEquals(manifest.authors().get(0), "wso2");
        Assert.assertEquals(manifest.exportedModules().size(), 2);
        Assert.assertEquals(manifest.exportedModules().get(0), "winery");
        Assert.assertEquals(manifest.exportedModules().get(1), "winery.service");

        // 3) Load the default module
        Module defaultModule = currentPackage.getDefaultModule();
        Assert.assertEquals(defaultModule.moduleName().toString(), "winery");
        Assert.assertEquals(defaultModule.documentIds().size(), 2);

        // TODO find an easy way to test the project structure. e.g. serialize the structure in a json file.
        int noOfSrcDocuments = 0;
        int noOfTestDocuments = 0;
        final ArrayList<String> moduleNames = new ArrayList<>(
                Arrays.asList("winery.services", "winery.storage", "winery"));
        final Collection<ModuleId> moduleIds = currentPackage.moduleIds();
        Assert.assertEquals(moduleIds.size(), 3);

        for (ModuleId moduleId : moduleIds) {
            Module module = currentPackage.module(moduleId);
            // test module names
            if (!moduleNames.contains(module.moduleName().toString())) {
                Assert.fail("module name '" + module.moduleName().toString() + "' is not valid");
            }
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
        Assert.assertEquals(noOfTestDocuments, 0);

        PackageResolution resolution = currentPackage.getResolution();
        DependencyGraph<ResolvedPackageDependency> packageDescriptorDependencyGraph = resolution.dependencyGraph();
        Assert.assertEquals(packageDescriptorDependencyGraph.getNodes().size(), 1);
        DependencyGraph<ModuleDescriptor> moduleDescriptorDependencyGraph = currentPackage.moduleDependencyGraph();
        Assert.assertEquals(moduleDescriptorDependencyGraph.getNodes().size(), 3);

        // compiler plugin
        Optional<CompilerPluginDescriptor> pluginDescriptor = currentPackage.compilerPluginDescriptor();
        Assert.assertTrue(pluginDescriptor.isPresent());
        Assert.assertEquals(pluginDescriptor.get().plugin().getId(), "openapi-validator");
        Assert.assertEquals(pluginDescriptor.get().plugin().getClassName(), "io.ballerina.openapi.Validator");
        Assert.assertEquals(pluginDescriptor.get().getCompilerPluginDependencies().size(), 2);
    }

    @Test (description = "test bala project load with newly created bala", enabled = false)
    public void testBalaProjectAPIWithNewBalaBuild() throws IOException {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject");

        // 1) Initialize the project instance
        BuildProject project = null;
        try {
            BuildOptions buildOptions = BuildOptions.builder().setSticky(true).build();
            project = TestUtils.loadBuildProject(projectPath, buildOptions);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }

        PackageCompilation packageCompilation = project.currentPackage().getCompilation();
        if (packageCompilation.diagnosticResult().hasErrors()) {
            Assert.fail("compilation failed:" + packageCompilation.diagnosticResult().errors());
        }

        Target target = new Target(project.sourceRoot());
        Path baloPath = target.getBalaPath();
        // invoke write balo method
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_11);
        EmitResult emitResult = jBallerinaBackend.emit(JBallerinaBackend.OutputType.BALA, baloPath);

        // Load the balo as a project
        BalaProject baloProject = null;
        try {
            ProjectEnvironmentBuilder defaultBuilder = ProjectEnvironmentBuilder.getDefaultBuilder();
            defaultBuilder.addCompilationCacheFactory(TempDirCompilationCache::from);
            baloProject = BalaProject.loadProject(defaultBuilder, emitResult.generatedArtifactPath());
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }

        Package currentPackage = baloProject.currentPackage();
        // Get the Package.md
        Optional<PackageMd> packageMd = currentPackage.packageMd();
        Assert.assertTrue(packageMd.isPresent());
        Assert.assertEquals(packageMd.get().content(), "# Package Md");
        // Get the Module.md
        Optional<ModuleMd> mdDocument = baloProject.currentPackage().getDefaultModule().moduleMd();
        Assert.assertTrue(mdDocument.isPresent());
        Assert.assertEquals(mdDocument.get().content(), "# Default Module Md");
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testGetDocumentIdFromPath() {
        Path balaPath = RESOURCE_DIRECTORY.resolve("balaloader").resolve("foo-winery-any-0.1.0.bala");
        Project balaProject = TestUtils.loadProject(balaPath);
        balaProject.documentId(balaPath.resolve("modules").resolve("winery").resolve("main.bal"));
    }

    @Test
    public void testGetDocumentIdFromPathInExtractedBala() {
        Path balaPath = RESOURCE_DIRECTORY.resolve("balaloader").resolve("extracted-bala");
        Project balaProject = TestUtils.loadProject(balaPath);
        DocumentId expectedDefaultDocId = balaProject.currentPackage().getDefaultModule()
                .documentIds().stream().findFirst().get();
        DocumentId actualDefaultDocId = balaProject
                .documentId(balaPath.resolve("modules").resolve("a").resolve("main.bal"));
        Assert.assertEquals(actualDefaultDocId, expectedDefaultDocId);

        DocumentId expectedNonDefaultDocId = balaProject.currentPackage().module(
                ModuleName.from(balaProject.currentPackage().packageName(), "c"))
                .documentIds().stream().findFirst().get();
        DocumentId actualNonDefaultDocId = balaProject
                .documentId(balaPath.resolve("modules").resolve("a.c").resolve("modc.bal"));
        Assert.assertEquals(actualNonDefaultDocId, expectedNonDefaultDocId);

        // try to get id of a file that does not belong to the project
        try {
            balaProject.documentId(balaPath.resolve("main.bal"));
            Assert.fail("expected a ProjectException");
        } catch (ProjectException e) {
            // ignore
        }

        // try to get id of the bala root
        try {
            balaProject.documentId(balaPath);
            Assert.fail("expected a ProjectException");
        } catch (ProjectException e) {
            // ignore
        }

        // try to get id of a non-existing file
        try {
            balaProject.documentId(Paths.get("foo.bal"));
            Assert.fail("expected a ProjectException");
        } catch (ProjectException e) {
            // ignore
        }

        // try to get id of a non-bal file from the project
        try {
            balaProject.documentId(balaPath.resolve("bala.json"));
            Assert.fail("expected a ProjectException");
        } catch (ProjectException e) {
            // ignore
        }
    }

    @Test
    public void testProjectRefresh() {
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("projects_for_refresh_tests").resolve("package_refresh_bala");
        Project project = TestUtils.loadProject(projectDirPath);
        Assert.assertEquals(project.kind(), ProjectKind.BALA_PROJECT);
        PackageCompilation compilation = project.currentPackage().getCompilation();
        int errorCount = compilation.diagnosticResult().errorCount();
        Assert.assertEquals(errorCount, 3);

        BCompileUtil.compileAndCacheBala("projects_for_refresh_tests/package_refresh_two_v3");
        int errorCount2 = project.currentPackage().getCompilation().diagnosticResult().errorCount();
        Assert.assertEquals(errorCount2, 3);

        project.clearCaches();
        int errorCount3 = project.currentPackage().getCompilation().diagnosticResult().errorCount();
        Assert.assertEquals(errorCount3, 0);
    }

    @Test
    public void testProjectDuplicate() {
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("projects_for_refresh_tests").resolve("package_refresh_bala");
        Project project = TestUtils.loadProject(projectDirPath);
        Assert.assertEquals(project.kind(), ProjectKind.BALA_PROJECT);
        Project duplicate = project.duplicate();
        Assert.assertEquals(project.kind(), ProjectKind.BALA_PROJECT);

        Assert.assertNotSame(project, duplicate);
        Assert.assertNotSame(project.currentPackage().project(), duplicate.currentPackage().project());

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
        }
        Assert.assertNotSame(project.projectEnvironmentContext().getService(CompilerContext.class),
                duplicate.projectEnvironmentContext().getService(CompilerContext.class));
        Assert.assertNotSame(
                PackageCache.getInstance(project.projectEnvironmentContext().getService(CompilerContext.class)),
                PackageCache.getInstance(duplicate.projectEnvironmentContext().getService(CompilerContext.class)));

        project.currentPackage().getCompilation();
        duplicate.currentPackage().getCompilation();
    }

    @Test
    public void testLoadResourcesFromBala() {
        Path balaPath = RESOURCE_DIRECTORY.resolve("balaloader").resolve("foo-winery-any-0.1.0.bala");
        Project balaProject = TestUtils.loadProject(balaPath);
        for (ModuleId moduleId : balaProject.currentPackage().moduleIds()) {
            Module module = balaProject.currentPackage().module(moduleId);
            if (module.moduleName().toString().equals("winery")) {
                Assert.assertEquals(module.resourceIds().size(), 1);
                Assert.assertEquals(module.resource(module.resourceIds().stream().findFirst().orElseThrow()).name(),
                        "main.json");
            } else if (module.moduleName().toString().equals("winery.storage")) {
                Assert.assertEquals(module.resourceIds().size(), 1);
                Assert.assertEquals(module.resource(module.resourceIds().stream().findFirst().orElseThrow()).name(),
                        "db.json");
            } else {
                Assert.assertEquals(module.resourceIds().size(), 4);
            }
        }
    }

    @Test
    public void testLoadResourcesFromExtractedBala() {
        Path balaPath = RESOURCE_DIRECTORY.resolve("balaloader").resolve("extracted-bala");
        Project balaProject = TestUtils.loadProject(balaPath);
        for (ModuleId moduleId : balaProject.currentPackage().moduleIds()) {
            Module module = balaProject.currentPackage().module(moduleId);
            if (module.moduleName().toString().equals("a")) {
                Assert.assertEquals(module.resourceIds().size(), 1);
                Assert.assertEquals(module.resource(module.resourceIds().stream().findFirst().orElseThrow()).name(),
                        "config/default.conf");
            } else {
                Assert.assertEquals(module.resourceIds().size(), 0);
            }
        }
    }

    @Test(description = "tests calling targetDir for balaProjects")
    public void testBalaProjectTargetDir() {
        Path balaPath = RESOURCE_DIRECTORY.resolve("balaloader").resolve("foo-winery-any-0.1.0.bala");
        // 1) Initialize the project instance
        BalaProject balaProject = null;
        try {
            ProjectEnvironmentBuilder defaultBuilder = ProjectEnvironmentBuilder.getDefaultBuilder();
            defaultBuilder.addCompilationCacheFactory(TempDirCompilationCache::from);
            balaProject = BalaProject.loadProject(defaultBuilder, balaPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }

        try {
            balaProject.targetDir();
        } catch (UnsupportedOperationException e) {
            Assert.assertEquals(e.getMessage(), "target directory is not supported for BalaProject");
        }
    }
}
