package org.ballerinalang.moduleloader;

import org.ballerinalang.toml.exceptions.TomlException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ModuleLoaderTests {

    @Test(description = "Get module version from module ID")
    public void testGetModuleFromModuleId() {
        Project project = new Project();
        ModuleLoaderImpl moduleLoader = new ModuleLoaderImpl(project);

        ModuleId moduleId = new ModuleId();
        moduleId.version = "1.0.0";

        ModuleId versionResolvedModuleId = moduleLoader.resolveVersion(moduleId, new ModuleId());
        Assert.assertEquals("1.0.0", versionResolvedModuleId.version);
    }

    @Test(description = "Get module version from Ballerina Toml")
    public void testGetModuleVersionFromBallerinaToml() throws TomlException {
        Project project = new Project();
        project.parseBallerinaToml("[project] \n" +
                "org-name = \"foo\" \n" +
                "version = \"1.0.0\"\n\n" +
                "[dependencies.\"bar/firstMod\"] \n"
                + "version = \"2.0.0\"");

        ModuleLoaderImpl moduleLoader = new ModuleLoaderImpl(project);

        ModuleId moduleId = new ModuleId();
        moduleId.orgName = "bar";
        moduleId.moduleName = "firstMod";

        ModuleId versionResolvedModuleId = moduleLoader.resolveVersion(moduleId, new ModuleId());
        Assert.assertEquals("2.0.0", versionResolvedModuleId.version);
    }
}
