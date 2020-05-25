package org.ballerinalang.moduleloader;

import org.ballerinalang.moduleloader.model.Module;
import org.ballerinalang.moduleloader.model.ModuleId;
import org.ballerinalang.moduleloader.model.Project;
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
public class ModuleLoaderImplTest extends PowerMockTestCase {

    private ModuleLoaderImpl moduleLoader;
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

//        // create repos
//        List<Repo> repos = new ArrayList<>();
//        repos.add(mock(ProjectModules.class));
//        repos.add(mock(BirCache.class));
//        repos.add(mock(BirCache.class));
//        repos.add(mock(BirCache.class));
//        repos.add(mock(BaloCache.class));
//        repos.add(mock(Central.class));

        // create repo hierarchy
        repoHierarchy = mock(RepoHierarchy.class);

        // create module loader
        moduleLoader = new ModuleLoaderImpl(project, repoHierarchy);
    }

    @Test(description = "Get module version from ModuleId if module version exists in ModuleId")
    void testGetModuleVersionFromVersionId() throws IOException {
        String moduleVersion = "1.5.0";
        ModuleId moduleId = new ModuleId();
        // Set version of the moduleId
        moduleId.setVersion(moduleVersion);

        ModuleId versionResolvedModuleId = moduleLoader.resolveVersion(moduleId, new ModuleId());
        Assert.assertNotNull(versionResolvedModuleId);
        Assert.assertEquals(versionResolvedModuleId.getVersion(), moduleVersion);
    }

    @Test(description = "Get module version from project modules")
    void testGetModuleVersionFromProjectModules() throws IOException {
        String moduleVersion = "1.7.1";
        ModuleId moduleId = new ModuleId();
        // ModuleId exists in project modules
        when(project.isModuleExists(moduleId)).thenReturn(true);

        Manifest manifest = mock(Manifest.class);
        org.ballerinalang.toml.model.Project tomlProject = mock(org.ballerinalang.toml.model.Project.class);

        // Set project version
        when(tomlProject.getVersion()).thenReturn(moduleVersion);
        when(manifest.getProject()).thenReturn(tomlProject);
        when(project.getManifest()).thenReturn(manifest);

        moduleId = moduleLoader.resolveVersion(moduleId, new ModuleId());
        Assert.assertNotNull(moduleId);
        Assert.assertEquals(moduleId.getVersion(), moduleVersion);
    }

    @Test(description = "Get module version from lock file")
    void testGetModuleVersionFromLockFile() throws IOException {
        final String moduleVersion = "1.2.0";
        final String barOrg = "bar_org";
        final String barModule = "barModule";

        ModuleId enclModuleId = new ModuleId();
        enclModuleId.setOrgName("foo_org");
        enclModuleId.setModuleName("fooModule");
        enclModuleId.setVersion("1.1.0");

        ModuleId moduleId = new ModuleId();
        moduleId.setOrgName(barOrg);
        moduleId.setModuleName(barModule);
        moduleId.setVersion(null);

        // ModuleId does not exists in project modules
        when(project.isModuleExists(moduleId)).thenReturn(false);

        // Set hasLockFile method to `true`
        when(project.hasLockFile()).thenReturn(true);

        // Make list of base imports and module import to the list
        List<LockFileImport> baseImports = new ArrayList<>();
        LockFileImport moduleImport = mock(LockFileImport.class);
        when(moduleImport.getOrgName()).thenReturn(barOrg);
        when(moduleImport.getName()).thenReturn(barModule);
        when(moduleImport.getVersion()).thenReturn(moduleVersion);
        baseImports.add(moduleImport);

        // Add base imports list to lock file imports map
        Map<String, List<LockFileImport>> imports = new HashMap<>();
        imports.put(enclModuleId.toString(), baseImports);

        LockFile lockFile = mock(LockFile.class);
        when(lockFile.getImports()).thenReturn(imports);

        when(project.getLockFile()).thenReturn(lockFile);

        // Set repoList of moduleLoader
        // Here set `barModule:1.2.0` in project modules
        ProjectModules projectModules = mock(ProjectModules.class);
        when(projectModules.resolveVersions(moduleId, moduleVersion))
                .thenReturn(new ArrayList<>(Collections.singletonList(moduleVersion)));
        when(repoHierarchy.getRepoList()).thenReturn(new ArrayList<>(Collections.singletonList(projectModules)));

        moduleId = moduleLoader.resolveVersion(moduleId, enclModuleId);
        Assert.assertNotNull(moduleId);
        Assert.assertEquals(moduleId.getVersion(), moduleVersion);
    }

    @Test(description = "Get module version from manifest (Ballerina.toml)")
    void testGetModuleVersionFromManifest() throws IOException {
        final String moduleVersion = "10.1.0";
        final String yooOrg = "yoo-org";
        final String yooModule = "yooModule";

        ModuleId enclModuleId = new ModuleId();

        ModuleId moduleId = new ModuleId();
        moduleId.setOrgName(yooOrg);
        moduleId.setModuleName(yooModule);
        moduleId.setVersion(null);

        // enclModuleId exists in project modules
        when(project.isModuleExists(enclModuleId)).thenReturn(true);

        // Set repoList of moduleLoader
        // Here set `yooModule:10.1.0` in project modules
        ProjectModules projectModules = mock(ProjectModules.class);
        when(projectModules.resolveVersions(moduleId, moduleVersion))
                .thenReturn(new ArrayList<>(Collections.singletonList(moduleVersion)));
        when(repoHierarchy.getRepoList()).thenReturn(new ArrayList<>(Collections.singletonList(projectModules)));

        // Create a dependency and add it to dependency list of the manifest
        Dependency dependency = mock(Dependency.class);
        when(dependency.getOrgName()).thenReturn(yooOrg);
        when(dependency.getModuleName()).thenReturn(yooModule);
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
        Assert.assertEquals(moduleVersion, moduleId.getVersion());
    }

    @Test(description = "Get module version of a transitive dependency from dependent balos")
    void testGetModuleVersionFromDependentBalos() throws IOException {
        final String moduleVersion = "2.5.0";

        ModuleId enclModuleId = new ModuleId();
        enclModuleId.setOrgName("hoo-org");
        enclModuleId.setModuleName("hooModule");
        enclModuleId.setVersion("2.0.0");

        ModuleId moduleId = new ModuleId();
        moduleId.setOrgName("hee-org");
        moduleId.setModuleName("heeModule");
        moduleId.setVersion(null);

        // moduleId & enclModuleId does not exists in project modules
        when(project.isModuleExists(moduleId)).thenReturn(false);
        when(project.isModuleExists(enclModuleId)).thenReturn(false);

        // Set repoList of moduleLoader
        // Here set `heeModule:2.5.0` in project modules
        BaloCache homeBaloCache = mock(BaloCache.class);
        when(homeBaloCache.resolveVersions(moduleId, moduleVersion))
                .thenReturn(new ArrayList<>(Collections.singletonList(moduleVersion)));
        when(repoHierarchy.getRepoList()).thenReturn(new ArrayList<>(Collections.singletonList(homeBaloCache)));

        when(repoHierarchy.getHomeBaloCache()).thenReturn(homeBaloCache);

        Path parentModulePath = Paths.get("src");
        Module parentModule = new Module(enclModuleId, parentModulePath);
        when(homeBaloCache.getModule(enclModuleId)).thenReturn(parentModule);

        Dependency dependency = mock(Dependency.class);
        when(dependency.getModuleName()).thenReturn("heeModule");
        when(dependency.getOrgName()).thenReturn("hee-org");
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
        Assert.assertEquals(moduleVersion, moduleId.getVersion());
    }

    @Test(description = "Resolve module")
    void testResolveModule() {
        String moduleVersion = "11.0.1";

        ModuleId moduleId = new ModuleId();
        moduleId.setOrgName("hee-org");
        moduleId.setModuleName("heeModule");
        moduleId.setVersion(moduleVersion);

        // Set `resolvedModules` map
        ProjectModules projectModules = mock(ProjectModules.class);
        Module module = new Module(moduleId, Paths.get("test/path/src" + moduleId.getModuleName()));
        when(projectModules.getModule(moduleId)).thenReturn(module);
        moduleLoader.resolvedModules.put(moduleId, projectModules);

        // test the method
        Module resolvedModule = moduleLoader.resolveModule(moduleId);
        Assert.assertNotNull(resolvedModule);
        Assert.assertEquals(resolvedModule.getModuleId(), moduleId);
        Assert.assertEquals(resolvedModule.getSourcePath(), module.getSourcePath());
    }
}
