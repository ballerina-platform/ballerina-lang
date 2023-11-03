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

import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.DependencyResolutionType;
import io.ballerina.projects.Document;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleDescriptor;
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
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.environment.ResolutionResponse;
import io.ballerina.projects.util.ProjectUtils;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.ballerina.projects.test.TestUtils.replaceDistributionVersionOfDependenciesToml;

/**
 * Contains cases to test dependency graph changes with package edits.
 *
 * @since 2.0.0
 */
public class DependencyGraphTests extends BaseTest {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources").toAbsolutePath();
    private static final ResolutionOptions resolutionOptions = ResolutionOptions.builder().setOffline(true).build();
    ProjectEnvironmentBuilder projectEnvironmentBuilder;
    private static Path tempResourceDir;

    @BeforeClass
    public void setup() throws IOException {
        // copy the resource directory to a temp directory
        tempResourceDir = Files.createTempDirectory("project-api-test");
        FileUtils.copyDirectory(RESOURCE_DIRECTORY.toFile(), tempResourceDir.toFile());

        // dist => cache (0.1.0), io (1.4.2)
        // central => cache (0.1.0), io (1.5.0)
        BCompileUtil.compileAndCacheBala(
                "projects_for_resolution_tests/ultimate_package_resolution/package_runtime");
        BCompileUtil.compileAndCacheBala(
                "projects_for_resolution_tests/ultimate_package_resolution/package_jsonutils");
        BCompileUtil.compileAndCacheBala(
                "projects_for_resolution_tests/ultimate_package_resolution/package_io_1_4_2");
        BCompileUtil.compileAndCacheBala(
                "projects_for_resolution_tests/ultimate_package_resolution/package_cache");
        BCompileUtil.compileAndCacheBala(
                "projects_for_resolution_tests/ultimate_package_resolution/package_cache", CENTRAL_CACHE);
        BCompileUtil.compileAndCacheBala(
                "projects_for_resolution_tests/ultimate_package_resolution/package_io_1_5_0",
                CENTRAL_CACHE);

        Environment environment = EnvironmentBuilder.getBuilder().setUserHome(CUSTOM_USER_HOME).build();
        projectEnvironmentBuilder = ProjectEnvironmentBuilder.getBuilder(environment);
    }

    @Test
    public void testVersionChange() throws IOException {
        /* test_dependencies_package --> package_dep (0.1.0), package_c
         * Specify minimum version for package_dep as 0.1.1
         */

        // 1) load the project
        Path projectPath = tempResourceDir.resolve("projects_for_edit_api_tests/package_test_dependencies_toml");
        replaceDistributionVersionOfDependenciesToml(projectPath, RepoUtils.getBallerinaShortVersion());

        // Create build options with sticky
        BuildProject project = TestUtils.loadBuildProject(projectPath, BuildOptions.builder().setSticky(true).build());
        DependencyGraph<ResolvedPackageDependency> dependencyGraphOld =
                project.currentPackage().getResolution().dependencyGraph();
        // dependency graph should contain self and package_dep
        Assert.assertEquals(dependencyGraphOld.getNodes().size(), 3);

        // verify that the compiler package cache contains package_dep
        PackageCache packageCache = PackageCache.getInstance(
                project.projectEnvironmentContext().getService(CompilerContext.class));
        project.currentPackage().getCompilation();

        ResolvedPackageDependency packageDep = dependencyGraphOld.getNodes().stream().filter(
                resolvedPackageDependency -> resolvedPackageDependency.packageInstance().packageName().toString()
                        .equals("package_dep")).collect(Collectors.toList()).get(0);
        PackageID packageDepPkgID = new PackageID(new Name(packageDep.packageInstance().packageOrg().value()),
                new Name(packageDep.packageInstance().getDefaultModule().moduleName().toString()),
                new Name(packageDep.packageInstance().packageVersion().toString()));
        Assert.assertNotNull(packageCache.getSymbol(packageDepPkgID));

        PackageID packageCPkgID = new PackageID(new Name("samjs"), new Name("package_c"), new Name("0.1.0"));
        Assert.assertNotNull(packageCache.getSymbol(packageCPkgID));

        // 2) update version of the package_dep dependency in Dependencies.toml
        project.currentPackage().ballerinaToml().get().modify().withContent(
                        "[package]\n" +
                        "org = \"foo\"\n" +
                        "name = \"test_dependencies_package\"\n" +
                        "version = \"2.1.0\"\n" +
                        "\n" +
                        "[build-options]\n" +
                        "observabilityIncluded = false\n\n" +
                        "[[dependency]]\n" +
                        "org = \"foo\"\n" +
                        "name = \"package_dep\"\n" +
                        "version = \"0.1.1\"").apply();

        // 3) compare dependency graphs before and after edit
        DependencyGraph<ResolvedPackageDependency> dependencyGraphNew =
                project.currentPackage().getResolution().dependencyGraph();
        // dependency graph should contain self and package_c
        Assert.assertEquals(dependencyGraphNew.getNodes().size(), 3);

        // verify that the package cache is cleaned
        Assert.assertNull(packageCache.getSymbol(packageDepPkgID));
        packageDepPkgID = new PackageID(new Name("foo"), new Name("package_dep"), new Name("0.1.1"));
        Assert.assertNull(packageCache.getSymbol(packageDepPkgID));
        Assert.assertNotNull(packageCache.getSymbol(packageCPkgID));
    }

    @Test
    public void testRemoveDependency() throws IOException {
        /*
         * package_b.mod_b2 --> package_c.mod_c1
         * Remove package_c dependency
         */

        // 1) load the project
        Path projectDirPath = tempResourceDir.resolve("projects_for_resolution_tests/package_b");
        replaceDistributionVersionOfDependenciesToml(projectDirPath, RepoUtils.getBallerinaShortVersion());

        BuildProject project = TestUtils.loadBuildProject(projectDirPath);
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
        PackageID packageCPkgID = new PackageID(new Name(packageC.packageInstance().packageOrg().value()),
                new Name(packageC.packageInstance().getDefaultModule().moduleName().toString()),
                new Name(packageC.packageInstance().packageVersion().toString()));
        Assert.assertNotNull(packageCache.getSymbol(packageCPkgID));

        // 2) update the mod_b2/mod2.bal file to remove package_c dependency
        Module modB2 = project.currentPackage().module(ModuleName.from(PackageName.from("package_b"), "mod_b2"));
        Document document = modB2.document(modB2.documentIds().stream().findFirst().get());
        document.modify().withContent("public function func2() {\n" + "}").apply();

        // 3) compare dependency graphs before and after edit
        DependencyGraph<ResolvedPackageDependency> dependencyGraphNew =
                project.currentPackage().getResolution().dependencyGraph();
        // dependency graph should contain only self
        Assert.assertEquals(dependencyGraphNew.getNodes().size(), 1);

        // verify that the package cache is cleaned
        Assert.assertNull(packageCache.getSymbol(packageCPkgID));
    }

    @Test
    public void testAddDependency() throws IOException {
        /*
         * package_b.mod_b2 --> package_c.mod_c1
         * Import package_e
         */

        // 1) load the project
        Path projectDirPath = tempResourceDir.resolve("projects_for_resolution_tests/package_b");
        replaceDistributionVersionOfDependenciesToml(projectDirPath, RepoUtils.getBallerinaShortVersion());
        BuildProject project = TestUtils.loadBuildProject(projectDirPath);
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
        PackageID packageCPkgID = new PackageID(new Name(packageC.packageInstance().packageOrg().value()),
                new Name(packageC.packageInstance().getDefaultModule().moduleName().toString()),
                new Name(packageC.packageInstance().packageVersion().toString()));
        Assert.assertNotNull(packageCache.getSymbol(packageCPkgID));

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

        // verify that package_c in package cache is unaffected
        Assert.assertNotNull(packageCache.getSymbol(packageCPkgID));
    }

    @Test
    public void testRemoveAndAddDependencies() throws IOException {
        /*
         * package_b.mod_b2 --> package_c.mod_c1
         * Import package_e. remove pacakge_c.mod_c1
         */

        // 1) load the project
        Path projectDirPath = tempResourceDir.resolve("projects_for_resolution_tests/package_b");
        replaceDistributionVersionOfDependenciesToml(projectDirPath, RepoUtils.getBallerinaShortVersion());
        BuildProject project = TestUtils.loadBuildProject(projectDirPath);
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
        // verify that the package cache is cleaned
        Assert.assertNull(packageCache.getSymbol(packageID));
    }

    @Test
    public void testUnaffectedEdit() throws IOException {
        /*
         * package_b.mod_b2 --> package_c.mod_c1
         * Import package_e
         */

        // 1) load the project
        Path projectDirPath = tempResourceDir.resolve("projects_for_resolution_tests/package_b");
        replaceDistributionVersionOfDependenciesToml(projectDirPath, RepoUtils.getBallerinaShortVersion());
        BuildProject project = TestUtils.loadBuildProject(projectDirPath);
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
        PackageID packageCPkgID = new PackageID(new Name(packageC.packageInstance().packageOrg().value()),
                new Name(packageC.packageInstance().getDefaultModule().moduleName().toString()),
                new Name(packageC.packageInstance().packageVersion().toString()));
        Assert.assertNotNull(packageCache.getSymbol(packageCPkgID));

        // 2) update the content in the default module
        Module defaultModule = project.currentPackage().getDefaultModule();
        Document document = defaultModule.document(defaultModule.documentIds().stream().findFirst().get());
        document.modify().withContent("public function func2() {\n" + "}").apply();

        // 3) compare dependency graphs before and after edit
        DependencyGraph<ResolvedPackageDependency> dependencyGraphNew =
                project.currentPackage().getResolution().dependencyGraph();
        // dependency graph should contain self and package_c
        Assert.assertEquals(dependencyGraphNew.getNodes().size(), 2);

        // verify that the package_c in package cache is not affected
        Assert.assertNotNull(packageCache.getSymbol(packageCPkgID));
    }

    @Test
    public void testMissingTransitiveDependency() throws IOException {
        /*
         * package_a --> package_b(0.1.0) --> package_c(0.1.0)
         * Specify package_c(0.3.0) in Ballerina.toml
         * Revert Ballerina.toml changes
         */

        Path projectDirPath = tempResourceDir.resolve("projects_for_resolution_tests/package_a");
        replaceDistributionVersionOfDependenciesToml(projectDirPath, RepoUtils.getBallerinaShortVersion());
        BuildProject project = TestUtils.loadBuildProject(projectDirPath);
        DependencyGraph<ResolvedPackageDependency> dependencyGraphOld =
                project.currentPackage().getResolution().dependencyGraph();
        // dependency graph should contain self, package_b and package_c
        Assert.assertEquals(dependencyGraphOld.getNodes().size(), 3);
        project.currentPackage().getCompilation();

        // verify that the compiler package cache contains package_b and package_c
        PackageCache packageCache = PackageCache.getInstance(
                project.projectEnvironmentContext().getService(CompilerContext.class));

        PackageID packageBPkgID = new PackageID(new Name("samjs"), new Name("package_b"), new Name("0.1.0"));
        Assert.assertNotNull(packageCache.getSymbol(packageBPkgID));
        PackageID packageCPkgID = new PackageID(new Name("samjs"), new Name("package_c"), new Name("0.1.0"));
        Assert.assertNotNull(packageCache.getSymbol(packageCPkgID));

        // 1) update version of the package_c dependency in Ballerina.toml. 0.3.0 is unavailable
        project.currentPackage().ballerinaToml().get().modify().withContent(
                "[package]\n" +
                "org = \"samjs\"\n" +
                "name = \"package_a\"\n" +
                "version = \"0.1.0\"\n\n" +
                "[[dependency]]\n" +
                "org = \"samjs\"\n" +
                "name = \"package_c\"\n" +
                "version = \"0.3.0\"").apply();

        dependencyGraphOld = project.currentPackage().getResolution().dependencyGraph();
        // dependency graph should contain self and package_b
        Assert.assertEquals(dependencyGraphOld.getNodes().size(), 2);

        // The bir of the direct dependency should be removed since the compiler throws
        // an exception when compiling with the BIR
        ProjectUtils.deleteDirectory(Paths.get("build/repo/cache/samjs/package_b/0.1.0"));

        project.currentPackage().getCompilation();
        // verify that the compiler package cache contains package_b but not package_c
        Assert.assertNull(packageCache.getSymbol(packageBPkgID));
        PackageID packageCPkgID2 = new PackageID(new Name("samjs"), new Name("package_c"), new Name("0.3.0"));
        Assert.assertNull(packageCache.getSymbol(packageCPkgID2));

        // 2) Revert Ballerina.toml changes and update the content in the default module to import package_c
        project.currentPackage().ballerinaToml().get().modify().withContent(
                "[package]\n" +
                "org = \"samjs\"\n" +
                "name = \"package_a\"\n" +
                "version = \"0.1.0\"\n").apply();

        // 3) check the new dependency graph. dependency graph should contain self package_b. package_c
        DependencyGraph<ResolvedPackageDependency> dependencyGraphNew =
                project.currentPackage().getResolution().dependencyGraph();
        Assert.assertEquals(dependencyGraphNew.getNodes().size(), 3);

        // verify that the package_c and package_b are not there in the package cache
        // reason: when a transitive dependency changes, its dependant modules also should be recompiled
        Assert.assertNull(packageCache.getSymbol(packageBPkgID));
        Assert.assertNull(packageCache.getSymbol(packageCPkgID));

        packageCPkgID = new PackageID(new Name("samjs"), new Name("package_c"), new Name("0.1.0"));
        Assert.assertNull(packageCache.getSymbol(packageCPkgID));
    }

    @Test
    public void testVersionResolutionSOFT() {
        // http -> io, cache -> io (1.4.2)
        Path projectDirPath = tempResourceDir
                .resolve("projects_for_resolution_tests/ultimate_package_resolution/package_http");
        BuildProject project = TestUtils.loadBuildProject(projectEnvironmentBuilder, projectDirPath);
        PackageResolver packageResolver = project.projectEnvironmentContext().getService(PackageResolver.class);

        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("samjs"), PackageName.from("io"), null),
                PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE, PackageLockingMode.SOFT);
        ResolutionRequest resolutionRequest2 = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("samjs"), PackageName.from("cache"), null),
                PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE, PackageLockingMode.SOFT);
        // Adding an unrelated package to test the unresolved packages
        ResolutionRequest resolutionRequest3 = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("samjs"), PackageName.from("dummy"), null),
                PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE, PackageLockingMode.SOFT);

        List<ResolutionRequest> resolutionRequestList = new ArrayList<>();
        resolutionRequestList.add(resolutionRequest);
        resolutionRequestList.add(resolutionRequest2);
        resolutionRequestList.add(resolutionRequest3);

        Collection<PackageMetadataResponse> responseDescriptors =
                packageResolver.resolvePackageMetadata(resolutionRequestList, resolutionOptions);
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
                PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE, PackageLockingMode.SOFT);
        responseDescriptors = packageResolver.resolvePackageMetadata(
                Collections.singletonList(resolutionRequest), resolutionOptions);

        PackageMetadataResponse packageMetadataResponse = responseDescriptors.iterator().next();
        Assert.assertEquals(packageMetadataResponse.resolvedDescriptor().version().toString(), "1.5.0");
        Assert.assertEquals(packageMetadataResponse.resolutionStatus(),
                ResolutionResponse.ResolutionStatus.RESOLVED);
    }

    @Test
    public void testVersionResolutionMEDIUM() {
        // http -> io, cache -> io (1.4.2)
        Path projectDirPath = tempResourceDir
                .resolve("projects_for_resolution_tests/ultimate_package_resolution/package_http");
        BuildProject project = TestUtils.loadBuildProject(projectEnvironmentBuilder, projectDirPath);
        PackageResolver packageResolver = project.projectEnvironmentContext().getService(PackageResolver.class);

        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("samjs"), PackageName.from("io"), PackageVersion.from("1.4.2")),
                PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE, PackageLockingMode.MEDIUM);
        ResolutionRequest resolutionRequest2 = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("samjs"), PackageName.from("cache"), null),
                PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE, PackageLockingMode.MEDIUM);
        // Adding an unrelated package to test the unresolved packages
        ResolutionRequest resolutionRequest3 = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("samjs"), PackageName.from("dummy"), null),
                PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE, PackageLockingMode.MEDIUM);

        List<ResolutionRequest> resolutionRequestList = new ArrayList<>();
        resolutionRequestList.add(resolutionRequest);
        resolutionRequestList.add(resolutionRequest2);
        resolutionRequestList.add(resolutionRequest3);

        Collection<PackageMetadataResponse> responseDescriptors = packageResolver.resolvePackageMetadata(
                resolutionRequestList, resolutionOptions);
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
                PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE, PackageLockingMode.MEDIUM);
        responseDescriptors = packageResolver.resolvePackageMetadata(
                Collections.singletonList(resolutionRequest), resolutionOptions);
        PackageMetadataResponse packageMetadataResponse = responseDescriptors.iterator().next();
        Assert.assertEquals(packageMetadataResponse.resolvedDescriptor().version().toString(), "1.4.2");
        Assert.assertEquals(packageMetadataResponse.resolutionStatus(),
                ResolutionResponse.ResolutionStatus.RESOLVED);

        // Test passing an unavailable version that is not compatible with any existing versions (1.3.2)
        resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("samjs"), PackageName.from("io"), PackageVersion.from("1.3.2")),
                PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE, PackageLockingMode.MEDIUM);
        responseDescriptors = packageResolver.resolvePackageMetadata(
                Collections.singletonList(resolutionRequest), resolutionOptions);
        packageMetadataResponse = responseDescriptors.iterator().next();
        Assert.assertNull(packageMetadataResponse.resolvedDescriptor());
        Assert.assertEquals(packageMetadataResponse.resolutionStatus(),
                ResolutionResponse.ResolutionStatus.UNRESOLVED);

        // Test passing the request without the version
        resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("samjs"), PackageName.from("io"), null),
                PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE, PackageLockingMode.MEDIUM);
        responseDescriptors = packageResolver.resolvePackageMetadata(
                Collections.singletonList(resolutionRequest), resolutionOptions);
        packageMetadataResponse = responseDescriptors.iterator().next();
        Assert.assertEquals(packageMetadataResponse.resolvedDescriptor().version().toString(), "1.5.0");
        Assert.assertEquals(packageMetadataResponse.resolutionStatus(),
                ResolutionResponse.ResolutionStatus.RESOLVED);
    }

    @Test
    public void testVersionResolutionHARD() {
        // http -> io, cache -> io (1.4.2)
        Path projectDirPath = tempResourceDir
                .resolve("projects_for_resolution_tests/ultimate_package_resolution/package_http");
        BuildProject project = TestUtils.loadBuildProject(projectEnvironmentBuilder, projectDirPath);
        PackageResolver packageResolver = project.projectEnvironmentContext().getService(PackageResolver.class);

        ResolutionRequest resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("samjs"), PackageName.from("io"), PackageVersion.from("1.4.2")),
                PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE, PackageLockingMode.HARD);
        ResolutionRequest resolutionRequest2 = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("samjs"), PackageName.from("cache"), null),
                PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE, PackageLockingMode.HARD);
        // Adding an unrelated package to test the unresolved packages
        ResolutionRequest resolutionRequest3 = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("samjs"), PackageName.from("dummy"), null),
                PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE, PackageLockingMode.HARD);

        List<ResolutionRequest> resolutionRequestList = new ArrayList<>();
        resolutionRequestList.add(resolutionRequest);
        resolutionRequestList.add(resolutionRequest2);
        resolutionRequestList.add(resolutionRequest3);

        Collection<PackageMetadataResponse> responseDescriptors = packageResolver.resolvePackageMetadata(
                resolutionRequestList, resolutionOptions);
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
                PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE, PackageLockingMode.HARD);
        responseDescriptors = packageResolver.resolvePackageMetadata(
                Collections.singletonList(resolutionRequest), resolutionOptions);
        PackageMetadataResponse packageMetadataResponse = responseDescriptors.iterator().next();
        Assert.assertNull(packageMetadataResponse.resolvedDescriptor());
        Assert.assertEquals(packageMetadataResponse.resolutionStatus(),
                ResolutionResponse.ResolutionStatus.UNRESOLVED);

        // Test passing with no version
        resolutionRequest = ResolutionRequest.from(
                PackageDescriptor.from(PackageOrg.from("samjs"), PackageName.from("io"), null),
                PackageDependencyScope.DEFAULT, DependencyResolutionType.SOURCE, PackageLockingMode.HARD);
        responseDescriptors = packageResolver.resolvePackageMetadata(
                Collections.singletonList(resolutionRequest), resolutionOptions);
        packageMetadataResponse = responseDescriptors.iterator().next();
        Assert.assertEquals(packageMetadataResponse.resolvedDescriptor().version().toString(), "1.5.0");
        Assert.assertEquals(packageMetadataResponse.resolutionStatus(),
                ResolutionResponse.ResolutionStatus.RESOLVED);
    }

    @Test
    public void testGetAllDependents() {
        // Create a sample DependencyGraph with String nodes
        DependencyGraph<String> dependencyGraph = DependencyGraph.from(new LinkedHashMap<>() {{
            put("A", new LinkedHashSet<>() {{
                add("B");
                add("C");
            }});
            put("B", new LinkedHashSet<>() {{
                add("D");
            }});
            put("C", new LinkedHashSet<>());
            put("D", new LinkedHashSet<>());
            put("E", new LinkedHashSet<>() {{
                add("F");
            }});
            put("F", new LinkedHashSet<>());
        }});

        Collection<String> allDependents = dependencyGraph.getAllDependents("D");
        Set<String> expectedDependents = new HashSet<>(Arrays.asList("A", "B"));

        Assert.assertEquals(expectedDependents, allDependents);
    }

    @Test
    public void testGetAllDependencies() {
        // Create a sample DependencyGraph with String nodes
        DependencyGraph<String> dependencyGraph = DependencyGraph.from(new LinkedHashMap<>() {{
            put("A", new LinkedHashSet<>() {{
                add("B");
                add("C");
            }});
            put("B", new LinkedHashSet<>() {{
                add("D");
            }});
            put("C", new LinkedHashSet<>());
            put("D", new LinkedHashSet<>());
            put("E", new LinkedHashSet<>() {{
                add("F");
            }});
            put("F", new LinkedHashSet<>());
        }});

        Collection<String> allDependencies = dependencyGraph.getAllDependencies("A");
        Set<String> expectedDependencies = new HashSet<>(Arrays.asList("B", "C", "D"));

        Assert.assertEquals(expectedDependencies, allDependencies);
    }

    @Test
    public void testTopologicalSortOfModuleDescriptor() {
        PackageName packageName = PackageName.from("package");
        PackageDescriptor packageDescriptor = PackageDescriptor.from(
                PackageOrg.BALLERINA_ORG, packageName, PackageVersion.from("0.0.1"));

        ModuleDescriptor moduleDescriptor = ModuleDescriptor.from(ModuleName.from(packageName), packageDescriptor);
        ModuleDescriptor moduleADescriptor = ModuleDescriptor.from(
                ModuleName.from(packageName, "module_a"), packageDescriptor);
        ModuleDescriptor moduleBDescriptor = ModuleDescriptor.from(
                ModuleName.from(packageName, "module_b"), packageDescriptor);
        ModuleDescriptor moduleCDescriptor = ModuleDescriptor.from(
                ModuleName.from(packageName, "module_c"), packageDescriptor);

        DependencyGraph<ModuleDescriptor> dependencyGraph = DependencyGraph.from(new LinkedHashMap<>() {{
            put(moduleADescriptor, new LinkedHashSet<>() {{
                add(moduleBDescriptor);
            }});
            put(moduleDescriptor, new LinkedHashSet<>() {{
                add(moduleBDescriptor);
                add(moduleADescriptor);
                add(moduleCDescriptor);
            }});
            put(moduleBDescriptor, new LinkedHashSet<>());
            put(moduleCDescriptor, new LinkedHashSet<>());
        }});

        Assert.assertEquals(dependencyGraph.toTopologicallySortedList(), new LinkedList<>() {{
            add(moduleBDescriptor);
            add(moduleADescriptor);
            add(moduleCDescriptor);
            add(moduleDescriptor);
        }});
    }

    @Test
    public void testTopologicalSortOfPackageDescriptor() {
        PackageVersion packageVersion = PackageVersion.from("0.0.1");
        PackageName packageName = PackageName.from("package_c");

        PackageDescriptor firstPackageDescriptor = PackageDescriptor.from(
                PackageOrg.BALLERINA_ORG, PackageName.from("package_a"), packageVersion);
        PackageDescriptor secondPackageDescriptor = PackageDescriptor.from(
                PackageOrg.BALLERINA_ORG, PackageName.from("package_b"), packageVersion);
        PackageDescriptor thirdPackageDescriptor = PackageDescriptor.from(
                PackageOrg.BALLERINA_ORG, packageName, packageVersion);
        PackageDescriptor forthPackageDescriptor = PackageDescriptor.from(
                PackageOrg.BALLERINA_X_ORG, packageName, packageVersion);
        PackageDescriptor fifthPackageDescriptor = PackageDescriptor.from(
                PackageOrg.BALLERINA_ORG, packageName, PackageVersion.from("0.0.2"));

        DependencyGraph<PackageDescriptor> dependencyGraph = DependencyGraph.from(new LinkedHashMap<>() {{
            put(secondPackageDescriptor, new LinkedHashSet<>() {{
                add(fifthPackageDescriptor);
            }});
            put(firstPackageDescriptor, new LinkedHashSet<>() {{
                add(forthPackageDescriptor);
                add(secondPackageDescriptor);
                add(thirdPackageDescriptor);
            }});
            put(fifthPackageDescriptor, new LinkedHashSet<>());
            put(thirdPackageDescriptor, new LinkedHashSet<>());
            put(forthPackageDescriptor, new LinkedHashSet<>());
        }});

        Assert.assertEquals(dependencyGraph.toTopologicallySortedList(), new LinkedList<>() {{
            add(fifthPackageDescriptor);
            add(secondPackageDescriptor);
            add(thirdPackageDescriptor);
            add(forthPackageDescriptor);
            add(firstPackageDescriptor);
        }});
    }

    @Test(dataProvider = "provideDependenciesInDifferentOrder")
    public void testTopologicalSortConsistency(Map<String, Set<String>> dependencies) {
        DependencyGraph<String> dependencyGraph = DependencyGraph.from(dependencies);
        Assert.assertEquals(dependencyGraph.toTopologicallySortedList(), new LinkedList<>() {{
            add("package7");
            add("package8");
            add("package6");
            add("package3");
            add("package4");
            add("package5");
            add("package1");
            add("package2");
        }});
    }

    @DataProvider(name = "provideDependenciesInDifferentOrder")
    public Object[][] provideDependenciesInDifferentOrder() {
        return new Object[][]{
                {new LinkedHashMap<>() {{
                    put("package1", new LinkedHashSet<>() {{
                        add("package3");
                        add("package4");
                        add("package5");
                    }});
                    put("package2", new LinkedHashSet<>() {{
                        add("package5");
                    }});
                    put("package3", new LinkedHashSet<>() {{
                        add("package6");
                        add("package7");
                    }});
                    put("package4", new LinkedHashSet<>() {{
                        add("package6");
                    }});
                    put("package5", new LinkedHashSet<>() {{
                        add("package8");
                    }});
                    put("package6", new LinkedHashSet<>() {{
                        add("package7");
                        add("package8");
                    }});
                    put("package7", new LinkedHashSet<>());
                    put("package8", new LinkedHashSet<>());
                }}},
                {new LinkedHashMap<>() {{
                    put("package8", new LinkedHashSet<>());
                    put("package7", new LinkedHashSet<>());
                    put("package6", new LinkedHashSet<>() {{
                        add("package8");
                        add("package7");
                    }});
                    put("package4", new LinkedHashSet<>() {{
                        add("package6");
                    }});
                    put("package5", new LinkedHashSet<>() {{
                        add("package8");
                    }});
                    put("package3", new LinkedHashSet<>() {{
                        add("package7");
                        add("package6");
                    }});
                    put("package2", new LinkedHashSet<>() {{
                        add("package5");
                    }});
                    put("package1", new LinkedHashSet<>() {{
                        add("package5");
                        add("package4");
                        add("package3");
                    }});
                }}},
                {new LinkedHashMap<>() {{
                    put("package4", new LinkedHashSet<>() {{
                        add("package6");
                    }});
                    put("package3", new LinkedHashSet<>() {{
                        add("package6");
                        add("package7");
                    }});
                    put("package2", new LinkedHashSet<>() {{
                        add("package5");
                    }});
                    put("package1", new LinkedHashSet<>() {{
                        add("package5");
                        add("package3");
                        add("package4");
                    }});
                    put("package8", new LinkedHashSet<>());
                    put("package7", new LinkedHashSet<>());
                    put("package6", new LinkedHashSet<>() {{
                        add("package8");
                        add("package7");
                    }});
                    put("package5", new LinkedHashSet<>() {{
                        add("package8");
                    }});
                }}}
        };
    }
}
