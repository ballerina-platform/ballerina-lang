/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.projects.BuildOptionsBuilder;
import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.DependencyResolutionType;
import io.ballerina.projects.Document;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.PackageDependencyScope;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.projects.environment.PackageLockingMode;
import io.ballerina.projects.environment.PackageMetadataResponse;
import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.environment.ResolutionResponse;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contains cases to test dependency graph changes with package edits.
 *
 * @since 2.0.0
 */
public class DependencyGraphTests extends BaseTest {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources").toAbsolutePath();
    ProjectEnvironmentBuilder projectEnvironmentBuilder;
    @BeforeClass
    public void setup() throws IOException {
        // dist => cache (0.1.0), io (1.4.2)
        // central => cache (0.1.0), io (1.5.0)
        Path customUserHome = Paths.get("build", "userHome");
        Path centralCache = customUserHome.resolve("repositories/central.ballerina.io");
        Files.createDirectories(centralCache);
        BCompileUtil.compileAndCacheBala(
                "projects_for_resolution_tests/ultimate_package_resolution/package_runtime");
        BCompileUtil.compileAndCacheBala(
                "projects_for_resolution_tests/ultimate_package_resolution/package_jsonutils");
        BCompileUtil.compileAndCacheBala(
                "projects_for_resolution_tests/ultimate_package_resolution/package_io_1_4_2");
        BCompileUtil.compileAndCacheBala(
                "projects_for_resolution_tests/ultimate_package_resolution/package_cache");
        BCompileUtil.compileAndCacheBala(
                "projects_for_resolution_tests/ultimate_package_resolution/package_cache", centralCache);
        BCompileUtil.compileAndCacheBala(
                "projects_for_resolution_tests/ultimate_package_resolution/package_io_1_5_0",
                centralCache);

        Environment environment = EnvironmentBuilder.getBuilder().setUserHome(customUserHome).build();
        projectEnvironmentBuilder = ProjectEnvironmentBuilder.getBuilder(environment);
    }

    @Test
    public void testVersionChange() {
        // 1) load the project
        Path projectPath = RESOURCE_DIRECTORY.resolve("projects_for_edit_api_tests/package_test_dependencies_toml");

        // Create build options with sticky
        BuildProject project = BuildProject.load(projectPath, new BuildOptionsBuilder().sticky(true).build());
        DependencyGraph<ResolvedPackageDependency> dependencyGraphOld =
                project.currentPackage().getResolution().dependencyGraph();
        // dependency graph should contain self and package_dep
        Assert.assertEquals(dependencyGraphOld.getNodes().size(), 3);

        // verify that the compiler package cache contains package_dep
        PackageCache packageCache = PackageCache.getInstance(
                project.projectEnvironmentContext().getService(CompilerContext.class));
        project.currentPackage().getCompilation();
        ResolvedPackageDependency packageC = dependencyGraphOld.getNodes().stream().filter(resolvedPackageDependency ->
                resolvedPackageDependency.packageInstance().packageName().toString()
                        .equals("package_dep")).collect(Collectors.toList()).get(0);
        PackageID packageID = new PackageID(new Name(packageC.packageInstance().packageOrg().value()),
                new Name(packageC.packageInstance().getDefaultModule().moduleName().toString()),
                new Name(packageC.packageInstance().packageVersion().toString()));
        Assert.assertNotNull(packageCache.getSymbol(packageID));

        // 2) update version of the package_c dependency in Dependencies.toml
        project.currentPackage().dependenciesToml().get().modify().withContent(
                        "[ballerina]\n" +
                        "dependencies-toml-version = \"2\"\n" +
                        "\n" +
                        "[[dependency]]\n" +
                        "org = \"foo\"\n" +
                        "name = \"package_dep\"\n" +
                        "version = \"0.1.1\"\n").apply();

        // 3) compare dependency graphs before and after edit
        DependencyGraph<ResolvedPackageDependency> dependencyGraphNew =
                project.currentPackage().getResolution().dependencyGraph();
        // dependency graph should contain self and package_c
        Assert.assertEquals(dependencyGraphNew.getNodes().size(), 3);

        // verify that the package cache is flushed
        Assert.assertNull(packageCache.getSymbol(packageID));
    }

    @Test
    public void testRemoveDependency() {
        // 1) load the project
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("projects_for_resolution_tests/package_b");
        BuildProject project = BuildProject.load(projectDirPath);
        DependencyGraph<ResolvedPackageDependency> dependencyGraphOld =
                project.currentPackage().getResolution().dependencyGraph();
        // dependency graph should contain self and package_c
        Assert.assertEquals(dependencyGraphOld.getNodes().size(), 2);

        // verify that the compiler package cache contains package_c
        PackageCache packageCache = PackageCache.getInstance(
                project.projectEnvironmentContext().getService(CompilerContext.class));
        project.currentPackage().getCompilation();
        ResolvedPackageDependency packageC = dependencyGraphOld.getNodes().stream().filter(resolvedPackageDependency ->
                resolvedPackageDependency.packageInstance().packageName().toString().equals("package_c"))
                .collect(Collectors.toList()).get(0);
        PackageID packageID = new PackageID(new Name(packageC.packageInstance().packageOrg().value()),
                new Name(packageC.packageInstance().getDefaultModule().moduleName().toString()),
                new Name(packageC.packageInstance().packageVersion().toString()));
        Assert.assertNotNull(packageCache.getSymbol(packageID));

        // 2) update the mod_b2/mod2.bal file to remove package_c dependency
        Module modB2 = project.currentPackage().module(ModuleName.from(PackageName.from("package_b"), "mod_b2"));
        Document document = modB2.document(modB2.documentIds().stream().findFirst().get());
        document.modify().withContent("public function func2() {\n" + "}").apply();

        // 3) compare dependency graphs before and after edit
        DependencyGraph<ResolvedPackageDependency> dependencyGraphNew =
                project.currentPackage().getResolution().dependencyGraph();
        // dependency graph should contain only self
        Assert.assertEquals(dependencyGraphNew.getNodes().size(), 1);

        // verify that the package cache is flushed
        Assert.assertNull(packageCache.getSymbol(packageID));
    }

    @Test
    public void testAddDependency() {
        // 1) load the project
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("projects_for_resolution_tests/package_b");
        BuildProject project = BuildProject.load(projectDirPath);
        DependencyGraph<ResolvedPackageDependency> dependencyGraphOld =
                project.currentPackage().getResolution().dependencyGraph();
        // dependency graph should contain self and package_c
        Assert.assertEquals(dependencyGraphOld.getNodes().size(), 2);

        // verify that the compiler package cache contains package_c
        PackageCache packageCache = PackageCache.getInstance(
                project.projectEnvironmentContext().getService(CompilerContext.class));
        project.currentPackage().getCompilation();

        ResolvedPackageDependency packageC = dependencyGraphOld.getNodes().stream().filter(resolvedPackageDependency ->
                resolvedPackageDependency.packageInstance().packageName().toString().equals("package_c"))
                .collect(Collectors.toList()).get(0);
        PackageID packageID = new PackageID(new Name(packageC.packageInstance().packageOrg().value()),
                new Name(packageC.packageInstance().getDefaultModule().moduleName().toString()),
                new Name(packageC.packageInstance().packageVersion().toString()));
        Assert.assertNotNull(packageCache.getSymbol(packageID));

        // 2) update the mod_b2/mod2.bal file to add a new dependency
        Module modB2 = project.currentPackage().module(ModuleName.from(PackageName.from("package_b"), "mod_b2"));
        Document document = modB2.document(modB2.documentIds().stream().findFirst().get());
        document.modify().withContent(
                "import samjs/package_c.mod_c1;\n" +
                "import samjs/package_e as _;\n" +
                "\n" +
                "public function func2() {\n" +
                "    mod_c1:func1();\n" +
                "}"
        ).apply();

        // 3) compare dependency graphs before and after edit
        DependencyGraph<ResolvedPackageDependency> dependencyGraphNew =
                project.currentPackage().getResolution().dependencyGraph();
        // dependency graph should contain self, package_c and package_e
        Assert.assertEquals(dependencyGraphNew.getNodes().size(), 3);

        // verify that the package cache is not flushed
        Assert.assertNotNull(packageCache.getSymbol(packageID));
    }

    @Test
    public void testRemoveAndAddDependencies() {
        // 1) load the project
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("projects_for_resolution_tests/package_b");
        BuildProject project = BuildProject.load(projectDirPath);
        DependencyGraph<ResolvedPackageDependency> dependencyGraphOld =
                project.currentPackage().getResolution().dependencyGraph();
        // dependency graph should contain self and package_c
        Assert.assertEquals(dependencyGraphOld.getNodes().size(), 2);

        // verify that the compiler package cache contains package_c
        PackageCache packageCache = PackageCache.getInstance(
                project.projectEnvironmentContext().getService(CompilerContext.class));
        project.currentPackage().getCompilation();
        ResolvedPackageDependency packageC = dependencyGraphOld.getNodes().stream().filter(resolvedPackageDependency ->
                resolvedPackageDependency.packageInstance().packageName().toString().equals("package_c"))
                .collect(Collectors.toList()).get(0);
        PackageID packageID = new PackageID(new Name(packageC.packageInstance().packageOrg().value()),
                new Name(packageC.packageInstance().getDefaultModule().moduleName().toString()),
                new Name(packageC.packageInstance().packageVersion().toString()));
        Assert.assertNotNull(packageCache.getSymbol(packageID));

        // 2) update the mod_b2/mod2.bal file to remove package_c dependency
        Module modB2 = project.currentPackage().module(ModuleName.from(PackageName.from("package_b"), "mod_b2"));
        Document document = modB2.document(modB2.documentIds().stream().findFirst().get());
        document.modify().withContent("import samjs/package_e as _;\n" +
                "public function func2() {\n" + "}").apply();

        // 3) compare dependency graphs before and after edit
        DependencyGraph<ResolvedPackageDependency> dependencyGraphNew =
                project.currentPackage().getResolution().dependencyGraph();
        // dependency graph should contain self and package_e
        Assert.assertEquals(dependencyGraphNew.getNodes().size(), 2);
        // verify that the package cache is flushed
        Assert.assertNull(packageCache.getSymbol(packageID));
    }

    @Test
    public void testUnaffectedEdit() {
        // 1) load the project
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("projects_for_resolution_tests/package_b");
        BuildProject project = BuildProject.load(projectDirPath);
        DependencyGraph<ResolvedPackageDependency> dependencyGraphOld =
                project.currentPackage().getResolution().dependencyGraph();
        // dependency graph should contain self and package_c
        Assert.assertEquals(dependencyGraphOld.getNodes().size(), 2);

        // verify that the compiler package cache contains the dependency
        PackageCache packageCache = PackageCache.getInstance(
                project.projectEnvironmentContext().getService(CompilerContext.class));
        project.currentPackage().getCompilation();
        ResolvedPackageDependency packageC = dependencyGraphOld.getNodes().stream().filter(resolvedPackageDependency ->
                resolvedPackageDependency.packageInstance().packageName().toString().equals("package_c"))
                .collect(Collectors.toList()).get(0);
        PackageID packageID = new PackageID(new Name(packageC.packageInstance().packageOrg().value()),
                new Name(packageC.packageInstance().getDefaultModule().moduleName().toString()),
                new Name(packageC.packageInstance().packageVersion().toString()));
        Assert.assertNotNull(packageCache.getSymbol(packageID));

        // 2) update the mod_b2/mod2.bal file to remove package_c dependency
        Module defaultModule = project.currentPackage().getDefaultModule();
        Document document = defaultModule.document(defaultModule.documentIds().stream().findFirst().get());
        document.modify().withContent("public function func2() {\n" + "}").apply();

        // 3) compare dependency graphs before and after edit
        DependencyGraph<ResolvedPackageDependency> dependencyGraphNew =
                project.currentPackage().getResolution().dependencyGraph();
        // dependency graph should contain self and package_e
        Assert.assertEquals(dependencyGraphNew.getNodes().size(), 2);

        // verify that the package cache is not flushed
        Assert.assertNotNull(packageCache.getSymbol(packageID));
    }

    @Test
    public void testVersionResolutionSOFT() {
        // http -> io, cache -> io (1.4.2)
        Path projectDirPath = RESOURCE_DIRECTORY
                .resolve("projects_for_resolution_tests/ultimate_package_resolution/package_http");
        BuildProject project = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        PackageResolver packageResolver = project.projectEnvironmentContext().getService(PackageResolver.class);

        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("samjs"), PackageName.from("io"), null),
                PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE, true, PackageLockingMode.SOFT);
        ResolutionRequest resolutionRequest2 = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("samjs"), PackageName.from("cache"), null),
                PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE, true, PackageLockingMode.SOFT);
        // Adding an unrelated package to test the unresolved packages
        ResolutionRequest resolutionRequest3 = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("samjs"), PackageName.from("dummy"), null),
                PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE, true, PackageLockingMode.SOFT);

        List<ResolutionRequest> resolutionRequestList = new ArrayList<>();
        resolutionRequestList.add(resolutionRequest);
        resolutionRequestList.add(resolutionRequest2);
        resolutionRequestList.add(resolutionRequest3);

        List<PackageMetadataResponse> responseDescriptors = packageResolver.resolvePackageMetadata(
                resolutionRequestList);
        Assert.assertEquals(responseDescriptors.size(), 3);
        for (PackageMetadataResponse responseDescriptor : responseDescriptors) {
            if (responseDescriptor.resolvedDescriptor() == null) {
                Assert.assertEquals(responseDescriptor.packageLoadRequest().packageName().toString(),
                        "dummy");
                Assert.assertEquals(responseDescriptor.resolutionStatus(),
                        ResolutionResponse.ResolutionStatus.UNRESOLVED);
            } else if (responseDescriptor.resolvedDescriptor().name().toString().equals("io")) {
                Assert.assertEquals(responseDescriptor.resolvedDescriptor().version().toString(),
                        "1.5.0");
                Assert.assertEquals(responseDescriptor.resolutionStatus(),
                        ResolutionResponse.ResolutionStatus.RESOLVED);
            } else {
                Assert.assertEquals(responseDescriptor.resolvedDescriptor().version().toString(),
                        "0.1.0");
                Assert.assertEquals(responseDescriptor.resolutionStatus(),
                        ResolutionResponse.ResolutionStatus.RESOLVED);
            }
        }

        // Test passing the request with a version
        resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("samjs"), PackageName.from("io"), PackageVersion.from("1.4.2")),
                PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE, true, PackageLockingMode.SOFT);
        responseDescriptors = packageResolver.resolvePackageMetadata(Collections.singletonList(resolutionRequest));
        Assert.assertEquals(responseDescriptors.get(0).resolvedDescriptor().version().toString(), "1.5.0");
        Assert.assertEquals(responseDescriptors.get(0).resolutionStatus(),
                ResolutionResponse.ResolutionStatus.RESOLVED);
    }

    @Test
    public void testVersionResolutionMEDIUM() {
        // http -> io, cache -> io (1.4.2)
        Path projectDirPath = RESOURCE_DIRECTORY
                .resolve("projects_for_resolution_tests/ultimate_package_resolution/package_http");
        BuildProject project = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        PackageResolver packageResolver = project.projectEnvironmentContext().getService(PackageResolver.class);

        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("samjs"), PackageName.from("io"), PackageVersion.from("1.4.2")),
                PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE, true, PackageLockingMode.MEDIUM);
        ResolutionRequest resolutionRequest2 = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("samjs"), PackageName.from("cache"), null),
                PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE, true, PackageLockingMode.MEDIUM);
        // Adding an unrelated package to test the unresolved packages
        ResolutionRequest resolutionRequest3 = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("samjs"), PackageName.from("dummy"), null),
                PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE, true, PackageLockingMode.MEDIUM);

        List<ResolutionRequest> resolutionRequestList = new ArrayList<>();
        resolutionRequestList.add(resolutionRequest);
        resolutionRequestList.add(resolutionRequest2);
        resolutionRequestList.add(resolutionRequest3);

        List<PackageMetadataResponse> responseDescriptors = packageResolver.resolvePackageMetadata(
                resolutionRequestList);
        Assert.assertEquals(responseDescriptors.size(), 3);
        for (PackageMetadataResponse responseDescriptor : responseDescriptors) {
            if (responseDescriptor.resolvedDescriptor() == null) {
                Assert.assertEquals(responseDescriptor.packageLoadRequest().packageName().toString(),
                        "dummy");
                Assert.assertEquals(responseDescriptor.resolutionStatus(),
                        ResolutionResponse.ResolutionStatus.UNRESOLVED);
            } else if (responseDescriptor.resolvedDescriptor().name().toString().equals("io")) {
                Assert.assertEquals(responseDescriptor.resolvedDescriptor().version().toString(),
                        "1.4.2");
                Assert.assertEquals(responseDescriptor.resolutionStatus(),
                        ResolutionResponse.ResolutionStatus.RESOLVED);
            } else {
                Assert.assertEquals(responseDescriptor.resolvedDescriptor().version().toString(),
                        "0.1.0");
                Assert.assertEquals(responseDescriptor.resolutionStatus(),
                        ResolutionResponse.ResolutionStatus.RESOLVED);
            }
        }

        // Test passing an unavailable version but compatible with an existing version (1.4.2)
        resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("samjs"), PackageName.from("io"), PackageVersion.from("1.4.0")),
                PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE, true, PackageLockingMode.MEDIUM);
        responseDescriptors = packageResolver.resolvePackageMetadata(Collections.singletonList(resolutionRequest));
        Assert.assertEquals(responseDescriptors.get(0).resolvedDescriptor().version().toString(), "1.4.2");
        Assert.assertEquals(responseDescriptors.get(0).resolutionStatus(),
                ResolutionResponse.ResolutionStatus.RESOLVED);

        // Test passing an unavailable version that is not compatible with any existing versions (1.3.2)
        resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("samjs"), PackageName.from("io"), PackageVersion.from("1.3.2")),
                PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE, true, PackageLockingMode.MEDIUM);
        responseDescriptors = packageResolver.resolvePackageMetadata(Collections.singletonList(resolutionRequest));
        Assert.assertNull(responseDescriptors.get(0).resolvedDescriptor());
        Assert.assertEquals(responseDescriptors.get(0).resolutionStatus(),
                ResolutionResponse.ResolutionStatus.UNRESOLVED);

        // Test passing the request without the version
        resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("samjs"), PackageName.from("io"), null),
                PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE, true, PackageLockingMode.MEDIUM);
        responseDescriptors = packageResolver.resolvePackageMetadata(Collections.singletonList(resolutionRequest));
        Assert.assertEquals(responseDescriptors.get(0).resolvedDescriptor().version().toString(), "1.5.0");
        Assert.assertEquals(responseDescriptors.get(0).resolutionStatus(),
                ResolutionResponse.ResolutionStatus.RESOLVED);
    }

    @Test
    public void testVersionResolutionHARD() {
        // http -> io, cache -> io (1.4.2)
        Path projectDirPath = RESOURCE_DIRECTORY
                .resolve("projects_for_resolution_tests/ultimate_package_resolution/package_http");
        BuildProject project = BuildProject.load(projectEnvironmentBuilder, projectDirPath);
        PackageResolver packageResolver = project.projectEnvironmentContext().getService(PackageResolver.class);

        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("samjs"), PackageName.from("io"), PackageVersion.from("1.4.2")),
                PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE, true, PackageLockingMode.HARD);
        ResolutionRequest resolutionRequest2 = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("samjs"), PackageName.from("cache"), null),
                PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE, true, PackageLockingMode.HARD);
        // Adding an unrelated package to test the unresolved packages
        ResolutionRequest resolutionRequest3 = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("samjs"), PackageName.from("dummy"), null),
                PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE, true, PackageLockingMode.HARD);

        List<ResolutionRequest> resolutionRequestList = new ArrayList<>();
        resolutionRequestList.add(resolutionRequest);
        resolutionRequestList.add(resolutionRequest2);
        resolutionRequestList.add(resolutionRequest3);

        List<PackageMetadataResponse> responseDescriptors = packageResolver.resolvePackageMetadata(
                resolutionRequestList);
        Assert.assertEquals(responseDescriptors.size(), 3);
        for (PackageMetadataResponse responseDescriptor : responseDescriptors) {
            if (responseDescriptor.resolvedDescriptor() == null) {
                Assert.assertEquals(responseDescriptor.packageLoadRequest().packageName().toString(),
                        "dummy");
                Assert.assertEquals(responseDescriptor.resolutionStatus(),
                        ResolutionResponse.ResolutionStatus.UNRESOLVED);
            } else if (responseDescriptor.resolvedDescriptor().name().toString().equals("io")) {
                Assert.assertEquals(responseDescriptor.resolvedDescriptor().version().toString(),
                        "1.4.2");
                Assert.assertEquals(responseDescriptor.resolutionStatus(),
                        ResolutionResponse.ResolutionStatus.RESOLVED);
            } else {
                Assert.assertEquals(responseDescriptor.resolvedDescriptor().version().toString(),
                        "0.1.0");
                Assert.assertEquals(responseDescriptor.resolutionStatus(),
                        ResolutionResponse.ResolutionStatus.RESOLVED);
            }
        }

        // Test passing an unavailable version
        resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("samjs"), PackageName.from("io"), PackageVersion.from("1.4.0")),
                PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE, true, PackageLockingMode.HARD);
        responseDescriptors = packageResolver.resolvePackageMetadata(
                Collections.singletonList(resolutionRequest));
        Assert.assertNull(responseDescriptors.get(0).resolvedDescriptor());
        Assert.assertEquals(responseDescriptors.get(0).resolutionStatus(),
                ResolutionResponse.ResolutionStatus.UNRESOLVED);

        // Test passing with no version
        resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("samjs"), PackageName.from("io"), null),
                PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE, true, PackageLockingMode.HARD);
        responseDescriptors = packageResolver.resolvePackageMetadata(
                Collections.singletonList(resolutionRequest));
        Assert.assertEquals(responseDescriptors.get(0).resolvedDescriptor().version().toString(), "1.5.0");
        Assert.assertEquals(responseDescriptors.get(0).resolutionStatus(),
                ResolutionResponse.ResolutionStatus.RESOLVED);
    }
}
