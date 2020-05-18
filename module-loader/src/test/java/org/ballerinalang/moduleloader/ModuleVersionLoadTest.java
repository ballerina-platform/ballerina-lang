package org.ballerinalang.moduleloader;

import org.ballerinalang.moduleloader.model.ModuleId;
import org.ballerinalang.moduleloader.model.Project;
import org.ballerinalang.toml.model.Dependency;
import org.ballerinalang.toml.model.DependencyMetadata;
import org.ballerinalang.toml.model.LockFile;
import org.ballerinalang.toml.model.LockFileImport;
import org.ballerinalang.toml.model.Manifest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ModuleVersionLoadTest {

    private ModuleLoaderImpl moduleLoader;
    private Project project;

    @BeforeClass
    public void setup() {
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

        // create repos
        List<Repo> repos = new ArrayList<>();
        repos.add(mock(ProjectModules.class));
        repos.add(mock(BirCache.class));
        repos.add(mock(BirCache.class));
        repos.add(mock(BirCache.class));
        repos.add(mock(BaloCache.class));
        repos.add(mock(Central.class));

        moduleLoader = new ModuleLoaderImpl(project, repos);
    }

    @Test
    public void testGetModuleVersionFromVersionId() throws IOException {
        ModuleId moduleId = new ModuleId();
        // Set version of the moduleId to `1.0.0`
        moduleId.setVersion("1.0.0");

        ModuleId versionResolvedModuleId = moduleLoader.resolveVersion(moduleId, new ModuleId());
        Assert.assertNotNull(versionResolvedModuleId);
        Assert.assertEquals(versionResolvedModuleId.getVersion(), "1.0.0");
    }

    @Test
    public void testGetModuleVersionFromCurrentProject() throws IOException {
        ModuleId moduleId = new ModuleId();
        // ModuleId exists in project modules
        when(project.isModuleExists(moduleId)).thenReturn(true);

        Manifest manifest = mock(Manifest.class);
        org.ballerinalang.toml.model.Project tomlProject = mock(org.ballerinalang.toml.model.Project.class);

        // Set project version to `1.0.0`
        when(tomlProject.getVersion()).thenReturn("1.0.0");
        when(manifest.getProject()).thenReturn(tomlProject);
        when(project.getManifest()).thenReturn(manifest);

        moduleId = moduleLoader.resolveVersion(moduleId, new ModuleId());
        Assert.assertNotNull(moduleId);
        Assert.assertEquals(moduleId.getVersion(), "1.0.0");
    }

    @Test(description = "Get module version from lock file")
    public void testGetModuleVersionFromLockFile() throws IOException {
        ModuleId enclModuleId = new ModuleId();
        enclModuleId.setOrgName("foo_org");
        enclModuleId.setModuleName("fooModule");
        enclModuleId.setVersion("1.1.0");

        ModuleId moduleId = new ModuleId();
        moduleId.setOrgName("bar_org");
        moduleId.setModuleName("barModule");
        moduleId.setVersion(null);

        // ModuleId does not exists in project modules
        when(project.isModuleExists(moduleId)).thenReturn(false);

        // Set hasLockFile method to `true`
        when(project.hasLockFile()).thenReturn(true);

        // Make list of base imports and module import to the list
        List<LockFileImport> baseImports = new ArrayList<>();
        LockFileImport moduleImport = mock(LockFileImport.class);
        when(moduleImport.getName()).thenReturn("barModule");
        when(moduleImport.getOrgName()).thenReturn("bar_org");
        when(moduleImport.getVersion()).thenReturn("1.2.0");
        baseImports.add(moduleImport);

        // Add base imports list to lock file imports map
        Map<String, List<LockFileImport>> imports = new HashMap<>();
        imports.put(enclModuleId.toString(), baseImports);

        LockFile lockFile = mock(LockFile.class);
        when(lockFile.getImports()).thenReturn(imports);

        when(project.getLockFile()).thenReturn(lockFile);

        // Set repos of moduleLoader
        // Here set `barModule:1.2.0` in project modules
        List<Repo> repos = moduleLoader.getRepos();
        when(repos.get(0).resolveVersions(moduleId, "1.2.0"))
                .thenReturn(new ArrayList<>(Collections.singletonList("1.2.0")));
        moduleLoader.setRepos(repos);


        moduleId = moduleLoader.resolveVersion(moduleId, enclModuleId);
        Assert.assertNotNull(moduleId);
        Assert.assertEquals(moduleId.getVersion(), "1.2.0");
    }

    @Test(description = "Get module version from manifest (Ballerina.toml)")
    public void testGetModuleVersionFromManifest() throws IOException {
        ModuleId enclModuleId = new ModuleId();

        ModuleId moduleId = new ModuleId();
        moduleId.setOrgName("fee-org");
        moduleId.setModuleName("feeModule");
        moduleId.setVersion(null);

        // ModuleId does not exists in project modules
        when(project.isModuleExists(moduleId)).thenReturn(false);
        // EnclModuleId exists in the project modules
        when(project.isModuleExists(enclModuleId)).thenReturn(true);

        // Create a dependency and add it to dependency list of the manifest
        Dependency dependency = mock(Dependency.class);
        when(dependency.getModuleName()).thenReturn("feeModule");
        when(dependency.getOrgName()).thenReturn("fee-org");
        DependencyMetadata dependencyMetadata = mock(DependencyMetadata.class);
        when(dependencyMetadata.getVersion()).thenReturn("1.3.0");
        when(dependency.getMetadata()).thenReturn(dependencyMetadata);

        Manifest manifest = mock(Manifest.class);
        when(manifest.getDependencies()).thenReturn(new ArrayList<>(Collections.singletonList(dependency)));

        // Set manifest of the project
        when(project.getManifest()).thenReturn(manifest);

        // Set repos of moduleLoader
        // Here set `feeModule:1.3.0` in project modules
        List<Repo> repos = moduleLoader.getRepos();
        when(repos.get(0).resolveVersions(moduleId, "1.3.0"))
                .thenReturn(new ArrayList<>(Collections.singletonList("1.3.0")));
        moduleLoader.setRepos(repos);

        moduleId = moduleLoader.resolveVersion(moduleId, enclModuleId);
        Assert.assertNotNull(moduleId);
        Assert.assertEquals(moduleId.getVersion(), "1.3.0");
    }
}
