package org.ballerinalang.moduleloader;

import org.ballerinalang.moduleloader.model.ModuleId;
import org.ballerinalang.moduleloader.model.Project;
import org.ballerinalang.toml.exceptions.TomlException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ModuleLoaderTests {

    private Project tomlProject = new Project();
    private ModuleLoaderImpl tomlModuleLoader;

    @BeforeClass()
    public void setUp() throws TomlException {
        tomlProject.parseBallerinaToml("[project] \n" +
                "org-name = \"doe\" \n" +
                "version = \"1.0.0\"\n\n" +
                "[dependencies]\n" +
                "\"foo/firstMod\" = \"2.0.0\"\n" +
                "\"foo/secondMod\" = \"\"");

        tomlModuleLoader = new ModuleLoaderImpl(tomlProject);
    }

    @Test(description = "Get module version from module ID")
    public void testGetModuleVersionFromModuleId() {
        Project project = new Project();
        ModuleLoaderImpl moduleLoader = new ModuleLoaderImpl(project);

        ModuleId moduleId = new ModuleId();
        moduleId.version = "1.0.0";

        ModuleId versionResolvedModuleId = moduleLoader.resolveVersion(moduleId, new ModuleId());
        Assert.assertEquals(versionResolvedModuleId.version, "1.0.0");
    }

    @Test(description = "Get module version from lock file")
    public void testGetModuleVersionFromLockFile() {
        Project project = new Project();
        // Can set the model w/o giving the toml
        String lockFile = "org_name = \"bar\"\n" +
                "version = \"1.0.0\"\n" +
                "lockfile_version = \"1.0.0\"\n" +
                "ballerina_version = \"1.2.2\"\n" +
                "\n" + "[[imports.\"bar/parentModule:1.0.0\"]]\n" +
                "org_name = \"foo\"\n" +
                "name = \"secondMod\"\n" +
                "version = \"1.1.0\"";
        InputStream stream = new ByteArrayInputStream(lockFile.getBytes(StandardCharsets.UTF_8));
        project.parseLockFile(stream);

        ModuleLoaderImpl moduleLoader = new ModuleLoaderImpl(project);

        ModuleId moduleId = new ModuleId();
        moduleId.orgName = "foo";
        moduleId.moduleName = "secondMod";

        ModuleId enclModuleId = new ModuleId();
        enclModuleId.orgName = "bar";
        enclModuleId.moduleName = "parentModule";
        enclModuleId.version = "1.0.0";

        ModuleId versionResolvedModuleId = moduleLoader.resolveVersion(moduleId, enclModuleId);
        Assert.assertEquals(versionResolvedModuleId.version, "1.1.0");
    }

    @Test(description = "Get module version from Ballerina Toml when specific version is given")
    public void testGetModuleVersionFromSpecificVersionInBallerinaToml() {
        ModuleId moduleId = new ModuleId();
        moduleId.orgName = "foo";
        moduleId.moduleName = "firstMod";

        ModuleId versionResolvedModuleId = tomlModuleLoader.resolveVersion(moduleId, new ModuleId());
        Assert.assertEquals(versionResolvedModuleId.version, "2.0.0");
    }

    @Test(description = "Get module version from Ballerina Toml when range of version is given")
    public void testGetModuleVersionFromRangeOfVersionsInBallerinaToml() { // => ask from Hemika
        ModuleId moduleId = new ModuleId();
        moduleId.orgName = "foo";
        moduleId.moduleName = "secondMod";

        ModuleId versionResolvedModuleId = tomlModuleLoader.resolveVersion(moduleId, new ModuleId());
        Assert.assertEquals(versionResolvedModuleId.version, "2.0.0");
    }
}
