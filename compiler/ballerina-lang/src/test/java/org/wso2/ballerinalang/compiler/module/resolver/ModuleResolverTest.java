

package org.wso2.ballerinalang.compiler.module.resolver;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.toml.model.Dependency;
import org.ballerinalang.toml.model.DependencyMetadata;
import org.ballerinalang.toml.model.LockFile;
import org.ballerinalang.toml.model.LockFileImport;
import org.ballerinalang.toml.model.Manifest;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.BaloCache;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.ModuleResolverImpl;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.ProjectModules;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.RepoHierarchy;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.model.Module;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.model.Project;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@PrepareForTest(RepoUtils.class)
public class ModuleResolverTest extends PowerMockTestCase {

    private ModuleResolverImpl moduleLoader;
    private Project project;
    private RepoHierarchy repoHierarchy;

    @BeforeClass
    void setup() {
        System.out.println("##########");
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

        // create module loader
        moduleLoader = new ModuleResolverImpl(project, repoHierarchy, true);
    }

    @Test(description = "Get module version from ModuleId if module version exists in ModuleId")
    void testGetModuleVersionFromVersionId() throws IOException {
        String orgName = "foo_org";
        String moduleName = "fooModule";
        String moduleVersion = "1.5.0";
        PackageID moduleId = new PackageID(new Name(orgName), new Name(moduleName), new Name(moduleVersion));

        String enclOrgName = "encl-org";
        String enclModuleName = "enclModule";
        String enclModuleVersion = "1.1.0";
        PackageID enclModuleId = new PackageID(new Name(enclOrgName), new Name(enclModuleName),
                new Name(enclModuleVersion));

        PackageID versionResolvedModuleId = moduleLoader.resolveVersion(moduleId, enclModuleId);
        Assert.assertNotNull(versionResolvedModuleId);
        Assert.assertEquals(versionResolvedModuleId.version.getValue(), moduleVersion);
    }

    @Test(description = "Get module version from project modules")
    void testGetModuleVersionFromProjectModules() throws IOException {
        String orgName = "foo_org";
        String moduleName = "fooModule";
        String moduleVersion = "1.5.0";
        PackageID moduleId = new PackageID(new Name(orgName), new Name(moduleName), Names.DEFAULT_VERSION);

        String enclOrgName = "encl-org";
        String enclModuleName = "enclModule";
        String enclModuleVersion = "1.1.0";
        PackageID enclModuleId = new PackageID(new Name(enclOrgName), new Name(enclModuleName), new Name(enclModuleVersion));

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
    void testGetModuleVersionFromLockFile() throws IOException {
        String orgName = "foo_org";
        String moduleName = "fooModule";
        String moduleVersion = "1.5.0";
        PackageID moduleId = new PackageID(new Name(orgName), new Name(moduleName), Names.DEFAULT_VERSION);

        String enclOrgName = "encl-org";
        String enclModuleName = "enclModule";
        String enclModuleVersion = "1.1.0";
        PackageID enclModuleId = new PackageID(new Name(enclOrgName), new Name(enclModuleName), new Name(enclModuleVersion));

        // ModuleId does not exists in project modules
        when(project.isModuleExists(moduleId)).thenReturn(false);

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
    void testGetModuleVersionFromManifest() throws IOException {
        String orgName = "foo_org";
        String moduleName = "fooModule";
        String moduleVersion = "1.5.0";
        PackageID moduleId = new PackageID(new Name(orgName), new Name(moduleName), Names.DEFAULT_VERSION);

        String enclOrgName = "encl-org";
        String enclModuleName = "enclModule";
        String enclModuleVersion = "1.1.0";
        PackageID enclModuleId = new PackageID(new Name(enclOrgName), new Name(enclModuleName), new Name(enclModuleVersion));

        // enclModuleId exists in project modules, to make this module immediate import
        when(project.isModuleExists(enclModuleId)).thenReturn(true);

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

    @Test(description = "Get module version of a transitive dependency from dependent balos")
    void testGetModuleVersionFromDependentBalos() throws IOException {
        String orgName = "foo_org";
        String moduleName = "fooModule";
        String moduleVersion = "1.5.0";
        PackageID moduleId = new PackageID(new Name(orgName), new Name(moduleName), Names.DEFAULT_VERSION);

        String enclOrgName = "encl-org";
        String enclModuleName = "enclModule";
        String enclModuleVersion = "1.1.0";
        PackageID enclModuleId = new PackageID(new Name(enclOrgName), new Name(enclModuleName), new Name(enclModuleVersion));

        // moduleId & enclModuleId does not exists in project modules
        when(project.isModuleExists(moduleId)).thenReturn(false);
        when(project.isModuleExists(enclModuleId)).thenReturn(false);

        // Set repoList of moduleLoader
        // Here set `moduleId` in project modules
        BaloCache homeBaloCache = mock(BaloCache.class);
        when(homeBaloCache.resolveVersions(moduleId, moduleVersion))
                .thenReturn(new ArrayList<>(Collections.singletonList(moduleVersion)));
        when(repoHierarchy.getRepoList()).thenReturn(new ArrayList<>(Collections.singletonList(homeBaloCache)));

        when(repoHierarchy.getHomeBaloCache()).thenReturn(homeBaloCache);

        Path parentModulePath = Paths.get("src");
        Module parentModule = new Module(enclModuleId, parentModulePath);
        when(homeBaloCache.getModule(enclModuleId)).thenReturn(parentModule);

        Dependency dependency = mock(Dependency.class);
        when(dependency.getModuleName()).thenReturn(moduleName);
        when(dependency.getOrgName()).thenReturn(orgName);
        DependencyMetadata dependencyMetadata = mock(DependencyMetadata.class);
        when(dependencyMetadata.getVersion()).thenReturn(moduleVersion);
        when(dependency.getMetadata()).thenReturn(dependencyMetadata);

        List<Dependency> dependencies = Collections.singletonList(dependency);

        Manifest manifest = mock(Manifest.class);
        when(manifest.getDependencies()).thenReturn(dependencies);

        PowerMockito.mockStatic(RepoUtils.class);
        PowerMockito.when(RepoUtils.getManifestFromBalo(any())).thenReturn(manifest);

        moduleId = moduleLoader.resolveVersion(moduleId, enclModuleId);

        Assert.assertNotNull(moduleId);
        Assert.assertEquals(moduleVersion, moduleId.version.getValue());
    }
//
//    @Test(description = "Resolve module")
//    void testResolveModule() {
//        String moduleVersion = "11.0.1";
//
//        ModuleId moduleId = new ModuleId();
//        moduleId.setOrgName("hee-org");
//        moduleId.setModuleName("heeModule");
//        moduleId.setVersion(moduleVersion);
//
//        // Set `resolvedModules` map
//        ProjectModules projectModules = mock(ProjectModules.class);
//        Module module = new Module(moduleId, Paths.get("test/path/src" + moduleId.getModuleName()));
//        when(projectModules.getModule(moduleId)).thenReturn(module);
//        moduleLoader.resolvedModules.put(moduleId, projectModules);
//
//        // test the method
//        Module resolvedModule = moduleLoader.resolveModule(moduleId);
//        Assert.assertNotNull(resolvedModule);
//        Assert.assertEquals(resolvedModule.getModuleId(), moduleId);
//        Assert.assertEquals(resolvedModule.getSourcePath(), module.getSourcePath());
//    }
}