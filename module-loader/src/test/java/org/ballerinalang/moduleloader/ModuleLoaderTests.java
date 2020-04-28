package org.ballerinalang.moduleloader;

import org.ballerinalang.toml.exceptions.TomlException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ModuleLoaderTests {

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


    @Test(description = "Get module version from Ballerina Toml")
    public void testGetModuleVersionFromBallerinaToml() throws TomlException {
        Project project = new Project();
        project.parseBallerinaToml("[project] \n" +
                "org-name = \"foo\" \n" +
                "version = \"1.0.0\"\n\n" +
                "[dependencies.\"bar/firstMod\"] \n"+
                "version = \"2.0.0\"");

        ModuleLoaderImpl moduleLoader = new ModuleLoaderImpl(project);

        ModuleId moduleId = new ModuleId();
        moduleId.orgName = "bar";
        moduleId.moduleName = "firstMod";

        ModuleId versionResolvedModuleId = moduleLoader.resolveVersion(moduleId, new ModuleId());
        Assert.assertEquals(versionResolvedModuleId.version, "2.0.0");
    }
}
