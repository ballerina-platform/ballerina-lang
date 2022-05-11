package org.ballerinalang.semver.checker;

import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.directory.BuildProject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.StringJoiner;

public class ProjectUtil {

    public static final String BAL_TOML_FILE_NAME = "Ballerina.toml";
    public static final String BAL_FILE_EXT = ".bal";
    private Path tempProjectDir;
    private static final String TEMP_DIR_PREFIX = "semver-enforcing-dir-";
    private static final String MAIN_FILE_PREFIX = "main-";
    private static final String PACKAGE_ORG = "Semver-validator";
    private static final String PACKAGE_NAME = "patch-validator";
    private static final String PACKAGE_VERSION = "1.0.0";


    public BuildProject createProject(String mainBalContent) throws Exception {

        try {
            // Creates a new directory in the default temporary file directory.
            this.tempProjectDir = Files.createTempDirectory(TEMP_DIR_PREFIX + System.currentTimeMillis());

            this.tempProjectDir.toFile().deleteOnExit();

            // Creates a main file and writes the generated code snippet.
            createMainBalFile(mainBalContent);
            // Creates the Ballerina.toml file and writes the package meta information.
            createBallerinaToml();

            BuildOptions.BuildOptionsBuilder buildOptionsBuilder = BuildOptions.builder();

            buildOptionsBuilder
                    .setCodeCoverage(false)
                    .setExperimental(false)
                    .setOffline(true)
                    .setSkipTests(true)
                    .setTestReport(false)
                    .setObservabilityIncluded(false)
                    .setSticky(false)
                    .setDumpGraph(false)
                    .setDumpRawGraphs(false)
                    .setConfigSchemaGen(false);

           /* if (targetDir != null) {
                buildOptionsBuilder.targetDir(targetDir.toString());
            }*/


           BuildOptions buildOptions =  buildOptionsBuilder.build();
           return BuildProject.load(this.tempProjectDir, buildOptions);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void createMainBalFile(String content) throws Exception {

        File mainBalFile = File.createTempFile(MAIN_FILE_PREFIX, BAL_FILE_EXT, tempProjectDir.toFile());
        mainBalFile.deleteOnExit();
        FileUtils.writeToFile(mainBalFile, content);
    }

    public void createBallerinaToml() throws Exception {

        Path ballerinaTomlPath = tempProjectDir.resolve(BAL_TOML_FILE_NAME);
        File balTomlFile = Files.createFile(ballerinaTomlPath).toFile();
        balTomlFile.deleteOnExit();
        StringJoiner balTomlContent = new StringJoiner(System.lineSeparator());
        balTomlContent.add("[package]");
        balTomlContent.add(String.format("org = \"%s\"", PACKAGE_ORG));
        balTomlContent.add(String.format("name = \"%s\"", PACKAGE_NAME));
        balTomlContent.add(String.format("version = \"%s\"", PACKAGE_VERSION));
        FileUtils.writeToFile(balTomlFile, balTomlContent.toString());

    }
}
