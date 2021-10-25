package org.ballerinalang.configschemagenerator;

import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.Project;
import io.ballerina.projects.TomlDocument;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.projects.plugins.CompilerPluginException;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.validator.TomlValidator;
import io.ballerina.toml.validator.schema.Schema;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.projects.util.ProjectConstants.CONFIGURATION_TOML;
import static org.ballerinalang.configschemagenerator.ConfigSchemaGenTask.CONFIG_SCHEMA;

/**
 * Tests to verify config schema generation task.
 */
public class ConfigSchemaGenTaskTest {

    private static final Path RESOURCES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    private static final String BALLERINA_HOME_KEY = "ballerina.home";

    @Test(dataProvider = "project-data-provider")
    public void testGeneratedSchema(String projectType, String projectName, boolean isSingleFileProject) {
        Path projectPath = RESOURCES_DIR.resolve(projectType).resolve(projectName);
        Project projectInstance = loadBuildProject(projectPath, isSingleFileProject);
        projectInstance.currentPackage().getCompilation();
        TomlValidator tomlValidator = new TomlValidator(Schema.from(
                readConfigJSONSchema(projectPath, isSingleFileProject)));
        TomlDocument configToml = TomlDocument.from(CONFIGURATION_TOML,
                readTomlContent(projectPath, isSingleFileProject));
        tomlValidator.validate(configToml.toml());
        TomlTableNode tomlAstNode = configToml.toml().rootNode();
        if (!tomlAstNode.diagnostics().isEmpty()) {
            String errorMsg = "";
            for (Diagnostic diagnostic : tomlAstNode.diagnostics()) {
                errorMsg = errorMsg.concat(diagnostic.message() + "\n");
            }
            Assert.fail(errorMsg);
        }
    }

    @DataProvider(name = "project-data-provider")
    public Object[][] dpMethod() {
        return new Object[][]{{"DefaultModuleProjects", "SimpleTypeConfigs", false},
                {"DefaultModuleProjects", "ComplexTypeConfigs", false},
                {"MultiModuleProjects", "SimpleTypeConfigs", false},
                {"SingleFileProject1", "testconfig.bal", true}};
    }

    static Project loadBuildProject(Path projectPath, boolean isSingleFileProject) {
        System.setProperty(BALLERINA_HOME_KEY, System.getenv(BALLERINA_HOME_KEY));
        BuildOptions buildOptions = BuildOptions.builder().setOffline(true).build();
        if (isSingleFileProject) {
            return SingleFileProject.load(projectPath, buildOptions);
        } else {
            return BuildProject.load(projectPath, buildOptions);
        }
    }

    /**
     * Read config JSON schema content.
     *
     * @param path Path to read schema from
     * @param isSingleFileProject flag to indicate if single file project
     * @return JSON schema content as String
     */
    private String readConfigJSONSchema(Path path, boolean isSingleFileProject) {
        Path targetPath;
        if (isSingleFileProject) {
            targetPath = path.getParent();
        } else {
            targetPath = path.resolve(ProjectConstants.TARGET_DIR_NAME);
        }
        return readFileContent(targetPath.resolve(ProjectConstants.BIN_DIR_NAME).
                resolve(CONFIG_SCHEMA));
    }

    /**
     * Read config toml content.
     *
     * @param projectPath Path to read toml from
     * @param isSingleFileProject flag to indicate if single file project
     * @return Config.toml content as String
     */
    private String readTomlContent(Path projectPath, boolean isSingleFileProject) {
        Path configTomlPath;
        if (isSingleFileProject) {
            configTomlPath = projectPath.getParent();
        } else {
            configTomlPath = projectPath;
        }
        return readFileContent(configTomlPath.resolve(CONFIGURATION_TOML));
    }

    /**
     * Read content of given file.
     *
     * @param file Path to read content from
     * @return content as String
     */
    private String readFileContent(Path file) {
        String content;
        try {
            content = Files.readString(file);
        } catch (IOException ioException) {
            throw new CompilerPluginException("Error occurred while reading the file " + file.toString());
        }
        return content;
    }
}
