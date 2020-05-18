//package org.ballerinalang.moduleloader;
//
//import org.ballerinalang.moduleloader.model.ModuleId;
//import org.ballerinalang.moduleloader.model.Project;
//import org.ballerinalang.toml.exceptions.TomlException;
//import org.testng.Assert;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.Test;
//
//import java.io.IOException;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.mockito.Mockito.mock;
//
//public class RepoTest {
//
//    private Path projectPath = Paths.get("src", "test", "resources", "test-resources", "repo", "test-repo-project");
//    private ProjectModules projectModules;
//    private ModuleId moduleId;
//    private Project project;
//
//    @BeforeClass()
//    public void setUp() throws TomlException {
//        projectModules = new ProjectModules(projectPath, "test", "0.1.0");
//
//        moduleId = new ModuleId();
//        moduleId.moduleName = "bye";
//        moduleId.orgName = "test";
//
//        project = new Project();
//        project.parseBallerinaToml("[project] \n" +
//                "org-name = \"test\"\n" +
//                "version = \"0.1.0\"\n");
//    }
//
//    @Test(description = "Test resolve versions from project modules")
//    public void testResolveProjectModuleVersions() {
//        List<String> modules = projectModules.resolveVersions(moduleId, "");
//        Assert.assertEquals(1, modules.size());
//        Assert.assertEquals("0.1.0", modules.get(0));
//    }
//
//    @Test(description = "Test is module exists in project modules")
//    public void testIsModuleExistsInProjectModules() {
//        moduleId.version = "0.1.0";
//        Assert.assertTrue(projectModules.isModuleExists(moduleId));
//    }
//
//    @Test
//    public void testAddProjectModules() throws IOException {
//        ModuleLoaderImpl moduleLoader = new ModuleLoaderImpl(project, new ArrayList<>());
//        moduleLoader.addProjectModules(moduleLoader.repos, projectPath);
//
//        Assert.assertFalse(moduleLoader.repos.isEmpty());
//        Assert.assertTrue(moduleLoader.repos.get(0) instanceof ProjectModules);
//
//        // resolve version `bye` from project modules
//        List<String> versions = moduleLoader.repos.get(0).resolveVersions(moduleId, "");
//        Assert.assertEquals(1, versions.size());
//        Assert.assertEquals("0.1.0", versions.get(0));
//
//        // check `bye` is exists in project modules
//        moduleId.version = "0.1.0";
//        Assert.assertTrue(moduleLoader.repos.get(0).isModuleExists(moduleId));
//    }
//}
