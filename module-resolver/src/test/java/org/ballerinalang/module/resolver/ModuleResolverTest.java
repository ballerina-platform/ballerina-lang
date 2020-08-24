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

package org.ballerinalang.module.resolver;

import io.ballerina.projects.Project;
import io.ballerina.projects.model.BallerinaToml;
import io.ballerina.projects.model.Package;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.module.resolver.model.ProjectModuleEntity;
import org.ballerinalang.repository.PackageEntity;
import org.ballerinalang.toml.model.Dependency;
import org.ballerinalang.toml.model.DependencyMetadata;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for module resolver.
 */
@PrepareForTest(RepoUtils.class)
public class ModuleResolverTest extends PowerMockTestCase {

    private ModuleResolverImpl moduleLoader;
    private Project project;
    private RepoHierarchyImpl repoHierarchy;

    @BeforeClass
    void setup() {
        String tomlProjectOrgName = "test-org";
        String tomlProjectVersion = "1.0.0";

        // create project
        project = mock(Project.class);
        io.ballerina.projects.Package pkg = mock(io.ballerina.projects.Package.class);
        when(project.currentPackage()).thenReturn(pkg);

        BallerinaToml ballerinaToml = mock(BallerinaToml.class);
        Package ballerinaTomlPackage = mock(Package.class);
        when(ballerinaTomlPackage.getOrg()).thenReturn(tomlProjectOrgName);
        when(ballerinaTomlPackage.getVersion()).thenReturn(tomlProjectVersion);
        when(ballerinaToml.getPackage()).thenReturn(ballerinaTomlPackage);
        when(project.currentPackage().ballerinaToml()).thenReturn(ballerinaToml);

        // create repo hierarchy
        repoHierarchy = mock(RepoHierarchyImpl.class);

        SourceDirectory sourceDirectory = mock(SourceDirectory.class);

        // create module loader
        moduleLoader = new ModuleResolverImpl(project, repoHierarchy, true, true, sourceDirectory);
    }

    @Test(description = "Get module version from ModuleId if module version exists in ModuleId")
    void testGetModuleVersionFromVersionId() {
        String orgName = "foo_org";
        String moduleName = "fooModule";
        String moduleVersion = "1.5.0";
        PackageID moduleId = new PackageID(new Name(orgName), new Name(moduleName), new Name(moduleVersion));

        String enclOrgName = "encl-org";
        String enclModuleName = "enclModule";
        String enclModuleVersion = "1.1.0";
        PackageID enclModuleId = new PackageID(new Name(enclOrgName), new Name(enclModuleName),
                new Name(enclModuleVersion));

        // Create repoList
        List<Repo> repoList = new ArrayList<>();
        ProjectModules projectModules = mock(ProjectModules.class);
        repoList.add(projectModules);
        when(repoHierarchy.getRepoList()).thenReturn(repoList);

        // Return module version when projectModules resolveVersions method call
        when(projectModules.resolveVersions(moduleId, moduleId.version.getValue()))
                .thenReturn(Collections.singletonList(moduleVersion));

        PackageID versionResolvedModuleId = moduleLoader.resolveVersion(moduleId, enclModuleId);
        Assert.assertNotNull(versionResolvedModuleId);
        Assert.assertEquals(versionResolvedModuleId.version.getValue(), moduleVersion);
    }

    @Test(description = "Get module version from project modules")
    void testGetModuleVersionFromProjectModules() {
        String orgName = "foo_org";
        String moduleName = "fooModule";
        String moduleVersion = "1.5.0";
        PackageID moduleId = new PackageID(new Name(orgName), new Name(moduleName), Names.DEFAULT_VERSION);

        String enclOrgName = "encl-org";
        String enclModuleName = "enclModule";
        String enclModuleVersion = "1.1.0";
        PackageID enclModuleId = new PackageID(new Name(enclOrgName), new Name(enclModuleName),
                new Name(enclModuleVersion));

        // ModuleId exists in project modules
        when(project.currentPackage().containsModule(moduleId)).thenReturn(true);

        BallerinaToml ballerinaToml = mock(BallerinaToml.class);
        Package tomlPackage = mock(Package.class);

        // Set project version
        when(tomlPackage.getVersion()).thenReturn(moduleVersion);
        when(ballerinaToml.getPackage()).thenReturn(tomlPackage);
        when(project.currentPackage().ballerinaToml()).thenReturn(ballerinaToml);

        moduleId = moduleLoader.resolveVersion(moduleId, enclModuleId);
        Assert.assertNotNull(moduleId);
        Assert.assertEquals(moduleId.version.getValue(), moduleVersion);
    }

    //    @Test(description = "Get module version from lock file")
    //    void testGetModuleVersionFromLockFile() {
    //        String orgName = "foo_org";
    //        String moduleName = "fooModule";
    //        String moduleVersion = "1.5.0";
    //        PackageID moduleId = new PackageID(new Name(orgName), new Name(moduleName), Names.DEFAULT_VERSION);
    //
    //        String enclOrgName = "encl-org";
    //        String enclModuleName = "enclModule";
    //        String enclModuleVersion = "1.1.0";
    //        PackageID enclModuleId = new PackageID(new Name(enclOrgName), new Name(enclModuleName),
    //                new Name(enclModuleVersion));
    //
    //        // ModuleId does not exists in project modules
    //        when(project.currentPackage().containsModule(moduleId)).thenReturn(false);
    //
    //        // ModuleId does not exists in project build repo
    //        setModuleIdNotExistsInProjectBuildRepo(moduleId);
    //
    //        // Set hasLockFile method to `true`
    //        when(project.hasLockFile()).thenReturn(true);
    //
    //        // Make list of base imports and module import to the list
    //        List<LockFileImport> baseImports = new ArrayList<>();
    //        LockFileImport moduleImport = mock(LockFileImport.class);
    //        when(moduleImport.getOrgName()).thenReturn(orgName);
    //        when(moduleImport.getName()).thenReturn(moduleName);
    //        when(moduleImport.getVersion()).thenReturn(moduleVersion);
    //        baseImports.add(moduleImport);
    //
    //        // Add base imports list to imports map
    //        Map<String, List<LockFileImport>> imports = new HashMap<>();
    //        imports.put(enclModuleId.toString(), baseImports);
    //
    //        // Assign imports map to getImports of lockfile
    //        LockFile lockFile = mock(LockFile.class);
    //        when(lockFile.getImports()).thenReturn(imports);
    //
    //        // This lockfile will be returned when `project.getLockFile` method is called
    //        when(project.getLockFile()).thenReturn(lockFile);
    //
    //        // Set repoList of moduleLoader
    //        // Here set `barModule:1.2.0` in project modules
    //        ProjectModules projectModules = mock(ProjectModules.class);
    //        when(projectModules.resolveVersions(moduleId, moduleVersion))
    //                .thenReturn(new ArrayList<>(Collections.singletonList(moduleVersion)));
    //        when(repoHierarchy.getRepoList()).thenReturn(new ArrayList<>(Collections.singletonList(projectModules)));
    //
    //        moduleId = moduleLoader.resolveVersion(moduleId, enclModuleId);
    //        Assert.assertNotNull(moduleId);
    //        Assert.assertEquals(moduleId.version.getValue(), moduleVersion);
    //    }

    @Test(description = "Get module version from manifest (Ballerina.toml)")
    void testGetModuleVersionFromManifest() {
        String orgName = "foo_org";
        String moduleName = "fooModule";
        String moduleVersion = "1.5.0";
        PackageID moduleId = new PackageID(new Name(orgName), new Name(moduleName), Names.DEFAULT_VERSION);

        String enclOrgName = "encl-org";
        String enclModuleName = "enclModule";
        String enclModuleVersion = "1.1.0";
        PackageID enclModuleId = new PackageID(new Name(enclOrgName), new Name(enclModuleName),
                new Name(enclModuleVersion));

        // enclModuleId exists in project modules, to make this module immediate import
        when(project.currentPackage().containsModule(enclModuleId)).thenReturn(true);

        // ModuleId does not exists in project build repo
        setModuleIdNotExistsInProjectBuildRepo(moduleId);

        // Set repoList of moduleLoader
        // Here set `moduleId` in project modules
        ProjectModules projectModules = mock(ProjectModules.class);
        when(projectModules.resolveVersions(moduleId, moduleVersion))
                .thenReturn(new ArrayList<>(Collections.singletonList(moduleVersion)));
        when(repoHierarchy.getRepoList()).thenReturn(new ArrayList<>(Collections.singletonList(projectModules)));

        // Create a dependency and add it to dependency list of the manifest
        Dependency dependency = mock(Dependency.class);
        when(dependency.getOrgName()).thenReturn(orgName);
        when(dependency.getModuleName()).thenReturn(moduleName);
        DependencyMetadata dependencyMetadata = mock(DependencyMetadata.class);
        when(dependencyMetadata.getVersion()).thenReturn(moduleVersion);
        when(dependency.getMetadata()).thenReturn(dependencyMetadata);
        BallerinaToml ballerinaToml = mock(BallerinaToml.class);
        when(ballerinaToml.getDependencies()).thenReturn(new ArrayList<>(Collections.singletonList(dependency)));

        // Set manifest of the project
        when(project.currentPackage().ballerinaToml()).thenReturn(ballerinaToml);

        // test method
        moduleId = moduleLoader.resolveVersion(moduleId, enclModuleId);
        Assert.assertNotNull(moduleId);
        Assert.assertEquals(moduleVersion, moduleId.version.getValue());
    }

    @Test(description = "Resolve module")
    void testResolveModule() {
        String orgName = "hee-org";
        String moduleName = "heeModule";
        String moduleVersion = "11.0.1";
        PackageID moduleId = new PackageID(new Name(orgName), new Name(moduleName), new Name(moduleVersion));

        // Set `resolvedModules` map
        ProjectModules projectModules = mock(ProjectModules.class);
        PackageEntity pkgEntity = new ProjectModuleEntity(moduleId, Paths.get("test/path/src" + moduleId.getName()),
                true);
        when(projectModules.getModule(moduleId)).thenReturn(pkgEntity);
        moduleLoader.resolvedModules.put(moduleId, projectModules);

        // test the method
        PackageEntity resolvedModule = moduleLoader.resolveModule(moduleId);
        Assert.assertNotNull(resolvedModule);
        Assert.assertEquals(resolvedModule.getPackageId(), moduleId);
    }

    @Test(description = "Get module version from ModuleId if module version exists in ModuleId", enabled = false)
    void testGetCentralVersions() throws IOException {
        List<String> versions = Central.getCentralVersions("wso2", "sfdc46", "*");

        Assert.assertEquals(versions.size(), 3);
        Assert.assertEquals(versions.get(0), "0.10.0");
        Assert.assertEquals(versions.get(1), "0.10.1");
        Assert.assertEquals(versions.get(2), "0.11.0");
    }

    private void setModuleIdNotExistsInProjectBuildRepo(PackageID moduleId) {
        ProjectBuildRepo projectBuildRepo = mock(ProjectBuildRepo.class);
        when(projectBuildRepo.isModuleExists(moduleId)).thenReturn(false);
        when(this.repoHierarchy.getProjectBuildRepo()).thenReturn(projectBuildRepo);
    }
}
