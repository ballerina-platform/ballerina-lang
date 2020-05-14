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
import java.util.Arrays;
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
        project = mock(Project.class);
        moduleLoader = new ModuleLoaderImpl(project, new ArrayList<>());
    }

    @Test
    public void testGetModuleVersionFromVersionId() throws IOException {
        ModuleId moduleId = new ModuleId();
        // Set version of the moduleId to `1.0.0`
        moduleId.version = "1.0.0";

        ModuleId versionResolvedModuleId = moduleLoader.resolveVersion(moduleId, new ModuleId());
        Assert.assertNotNull(versionResolvedModuleId);
        Assert.assertEquals(versionResolvedModuleId.version, "1.0.0");
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
        Assert.assertEquals(moduleId.version, "1.0.0");
    }

    @Test(description = "Get module version from lock file")
    public void testGetModuleVersionFromLockFile() throws IOException {
        ModuleId enclModuleId = new ModuleId();
        enclModuleId.orgName = "foo-org";
        enclModuleId.moduleName = "fooModule";
        enclModuleId.version = "1.1.0";

        ModuleId moduleId = new ModuleId();
        moduleId.orgName = "bar-org";
        moduleId.moduleName = "barModule";
        moduleId.version = null;

        // ModuleId does not exists in project modules
        when(project.isModuleExists(moduleId)).thenReturn(false);

        // Set hasLockFile method to `true`
        when(project.hasLockFile()).thenReturn(true);

        // Make list of base imports and module import to the list
        List<LockFileImport> baseImports = new ArrayList<>();
        LockFileImport moduleImport = mock(LockFileImport.class);
        when(moduleImport.getName()).thenReturn("barModule");
        when(moduleImport.getOrgName()).thenReturn("bar-org");
        when(moduleImport.getVersion()).thenReturn("1.2.0");
        baseImports.add(moduleImport);

        // Add base imports list to lock file imports map.
        Map<String, List<LockFileImport>> imports = new HashMap<>();
        imports.put(enclModuleId.toString(), baseImports);

        LockFile lockFile = mock(LockFile.class);
        when(lockFile.getImports()).thenReturn(imports);

        when(project.getLockFile()).thenReturn(lockFile);

        moduleId = moduleLoader.resolveVersion(moduleId, enclModuleId);
        Assert.assertNotNull(moduleId);
        Assert.assertEquals(moduleId.version, "1.2.0");
    }

    @Test(description = "Get module version from manifest (Ballerina.toml)")
    public void testGetModuleVersionFromManifest() throws IOException {
        ModuleId enclModuleId = new ModuleId();

        ModuleId moduleId = new ModuleId();
        moduleId.orgName = "fee-org";
        moduleId.moduleName = "feeModule";
        moduleId.version = null;

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

        moduleId = moduleLoader.resolveVersion(moduleId, enclModuleId);
        Assert.assertNotNull(moduleId);
        Assert.assertEquals(moduleId.version, "1.3.0");
    }
}
