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

package org.wso2.ballerinalang.compiler.module.resolver;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageEntity;
import org.ballerinalang.toml.model.Dependency;
import org.ballerinalang.toml.model.DependencyMetadata;
import org.ballerinalang.toml.model.LockFile;
import org.ballerinalang.toml.model.LockFileImport;
import org.ballerinalang.toml.model.Manifest;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.Central;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.ModuleResolverImpl;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.ProjectBuildRepo;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.ProjectModules;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.Repo;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.RepoHierarchy;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.model.Project;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.model.ProjectModuleEntity;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for module resolver.
 */
@PrepareForTest(RepoUtils.class)
public class ModuleResolverTest extends PowerMockTestCase {

    private ModuleResolverImpl moduleLoader;
    private Project project;
    private RepoHierarchy repoHierarchy;

    @BeforeClass
    void setup() {
        String tomlProjectOrgName = "test-org";
        String tomlProjectVersion = "1.0.0";

        // create project
        project = mock(Project.class);
        Manifest manifest = mock(Manifest.class);
        org.ballerinalang.toml.model.Project tomlProject = mock(org.ballerinalang.toml.model.Project.class);
        when(tomlProject.getOrgName()).thenReturn(tomlProjectOrgName);
        when(tomlProject.getVersion()).thenReturn(tomlProjectVersion);
        when(manifest.getProject()).thenReturn(tomlProject);
        when(project.getManifest()).thenReturn(manifest);

        // create repo hierarchy
        repoHierarchy = mock(RepoHierarchy.class);

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
        when(project.isModuleExists(moduleId)).thenReturn(true);

        Manifest manifest = mock(Manifest.class);
        org.ballerinalang.toml.model.Project tomlProject = mock(org.ballerinalang.toml.model.Project.class);

        // Set project version
        when(tomlProject.getVersion()).thenReturn(moduleVersion);
        when(manifest.getProject()).thenReturn(tomlProject);
        when(project.getManifest()).thenReturn(manifest);

        moduleId = moduleLoader.resolveVersion(moduleId, enclModuleId);
        Assert.assertNotNull(moduleId);
        Assert.assertEquals(moduleId.version.getValue(), moduleVersion);
    }

    @Test(description = "Get module version from lock file")
    void testGetModuleVersionFromLockFile() {
        String orgName = "foo_org";
        String moduleName = "fooModule";
        String moduleVersion = "1.5.0";
        PackageID moduleId = new PackageID(new Name(orgName), new Name(moduleName), Names.DEFAULT_VERSION);

        String enclOrgName = "encl-org";
        String enclModuleName = "enclModule";
        String enclModuleVersion = "1.1.0";
        PackageID enclModuleId = new PackageID(new Name(enclOrgName), new Name(enclModuleName),
                new Name(enclModuleVersion));

        // ModuleId does not exists in project modules
        when(project.isModuleExists(moduleId)).thenReturn(false);

        // ModuleId does not exists in project build repo
        setModuleIdNotExistsInProjectBuildRepo(moduleId);

        // Set hasLockFile method to `true`
        when(project.hasLockFile()).thenReturn(true);

        // Make list of base imports and module import to the list
        List<LockFileImport> baseImports = new ArrayList<>();
        LockFileImport moduleImport = mock(LockFileImport.class);
        when(moduleImport.getOrgName()).thenReturn(orgName);
        when(moduleImport.getName()).thenReturn(moduleName);
        when(moduleImport.getVersion()).thenReturn(moduleVersion);
        baseImports.add(moduleImport);

        // Add base imports list to imports map
        Map<String, List<LockFileImport>> imports = new HashMap<>();
        imports.put(enclModuleId.toString(), baseImports);

        // Assign imports map to getImports of lockfile
        LockFile lockFile = mock(LockFile.class);
        when(lockFile.getImports()).thenReturn(imports);

        // This lockfile will be returned when `project.getLockFile` method is called
        when(project.getLockFile()).thenReturn(lockFile);

        // Set repoList of moduleLoader
        // Here set `barModule:1.2.0` in project modules
        ProjectModules projectModules = mock(ProjectModules.class);
        when(projectModules.resolveVersions(moduleId, moduleVersion))
                .thenReturn(new ArrayList<>(Collections.singletonList(moduleVersion)));
        when(repoHierarchy.getRepoList()).thenReturn(new ArrayList<>(Collections.singletonList(projectModules)));

        moduleId = moduleLoader.resolveVersion(moduleId, enclModuleId);
        Assert.assertNotNull(moduleId);
        Assert.assertEquals(moduleId.version.getValue(), moduleVersion);
    }

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
        when(project.isModuleExists(enclModuleId)).thenReturn(true);

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
        Manifest manifest = mock(Manifest.class);
        when(manifest.getDependencies()).thenReturn(new ArrayList<>(Collections.singletonList(dependency)));

        // Set manifest of the project
        when(project.getManifest()).thenReturn(manifest);

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
