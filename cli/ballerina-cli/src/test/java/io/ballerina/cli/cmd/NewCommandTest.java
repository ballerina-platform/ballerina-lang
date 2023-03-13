/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.cli.cmd;

import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

import static io.ballerina.cli.cmd.CommandOutputUtils.getOutput;
import static io.ballerina.cli.cmd.CommandOutputUtils.readFileAsString;
import static io.ballerina.projects.util.ProjectConstants.USER_NAME;

/**
 * Test cases for bal new command.
 *
 * @since 2.0.0
 */
public class NewCommandTest extends BaseCommandTest {

    Path testResources;
    Path centralCache;

    @DataProvider(name = "invalidProjectNames")
    public Object[][] provideInvalidProjectNames() {
        return new Object[][] {
                { "hello-app", "hello_app" },
                { "my$project", "my_project" }
        };
    }

    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        testResources = Paths.get("src/test/resources/test-resources");
        centralCache = homeCache.resolve("repositories/central.ballerina.io").resolve("bala");
        Files.createDirectories(centralCache);

        Path testTemplatesDir = testResources.resolve("balacache-template");
        Files.walkFileTree(testTemplatesDir, new Copy(testTemplatesDir, centralCache));
    }

    @AfterClass
    public void afterClass() {
        ProjectUtils.deleteDirectory(centralCache);
    }

    @Test(description = "Create a new project")
    public void testNewCommand() throws IOException {
        System.setProperty(USER_NAME, "testuserorg");
        String[] args = {"project_name"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();
        // Check with spec
        // project_name/
        // - Ballerina.toml
        // - main.bal

        Path packageDir = tmpDir.resolve("project_name");
        Assert.assertTrue(Files.exists(packageDir));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.BALLERINA_TOML)));
        String tomlContent = Files.readString(
                packageDir.resolve(ProjectConstants.BALLERINA_TOML), StandardCharsets.UTF_8);
        String expectedContent = "[package]\n" +
                "org = \"testuserorg\"\n" +
                "name = \"" + args[0] + "\"\n" +
                "version = \"0.1.0\"\n" +
                "distribution = \"" + RepoUtils.getBallerinaShortVersion() + "\"\n\n" +
                "[build-options]\n" +
                "observabilityIncluded = true\n";
        Assert.assertEquals(tomlContent.trim(), expectedContent.trim());

        Assert.assertTrue(Files.exists(packageDir.resolve("main.bal")));
        Assert.assertFalse(Files.exists(packageDir.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME)));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.GITIGNORE_FILE_NAME)));
        String gitignoreContent = Files.readString(
                packageDir.resolve(ProjectConstants.GITIGNORE_FILE_NAME), StandardCharsets.UTF_8);
        String expectedGitignoreContent = "target\ngenerated\n" +
                "Config.toml\n";
        Assert.assertEquals(gitignoreContent.trim(), expectedGitignoreContent.trim());
        Assert.assertTrue(readOutput().contains("Created new package"));

        Assert.assertTrue(Files.exists(packageDir.resolve(".devcontainer.json")));
        String devcontainerContent = Files.readString(packageDir.resolve(".devcontainer.json"));
        Assert.assertTrue(devcontainerContent.contains(RepoUtils.getBallerinaVersion()));
    }

    @Test(description = "Test new command with main template")
    public void testNewCommandWithMain() throws IOException {
        System.setProperty(USER_NAME, "testuserorg");
        String[] args = {"main_sample", "-t", "main"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();
        // Check with spec
        // project_name/
        // - Ballerina.toml
        // - Package.md
        // - main.bal
        // - tests
        //      - main_test.bal

        Path packageDir = tmpDir.resolve("main_sample");
        Assert.assertTrue(Files.exists(packageDir));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.BALLERINA_TOML)));
        String tomlContent = Files.readString(
                packageDir.resolve(ProjectConstants.BALLERINA_TOML), StandardCharsets.UTF_8);
        String expectedContent = "[package]\n" +
                "org = \"testuserorg\"\n" +
                "name = \"" + args[0] + "\"\n" +
                "version = \"0.1.0\"\n" +
                "distribution = \"" + RepoUtils.getBallerinaShortVersion() + "\"\n\n" +
                "[build-options]\n" +
                "observabilityIncluded = true\n";
        Assert.assertEquals(tomlContent.trim(), expectedContent.trim());

        Assert.assertTrue(Files.exists(packageDir.resolve("main.bal")));
        Assert.assertTrue(Files.notExists(packageDir.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME)));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.TEST_DIR_NAME)));
        Path resourcePath = packageDir.resolve(ProjectConstants.RESOURCE_DIR_NAME);
        Assert.assertFalse(Files.exists(resourcePath));

        Assert.assertTrue(readOutput().contains("Created new package"));
    }

    @Test(description = "Test new command with service template")
    public void testNewCommandWithService() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"service_sample", "-t", "service"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false);
        new CommandLine(newCommand).parseArgs(args);
        newCommand.execute();
        // Check with spec
        // project_name/
        // - Ballerina.toml
        // - Package.md
        // - service.bal
        // - tests
        //      - service_test.bal
        // - .gitignore       <- git ignore file

        Path packageDir = tmpDir.resolve("service_sample");
        Assert.assertTrue(Files.exists(packageDir));
        Assert.assertTrue(Files.isDirectory(packageDir));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.BALLERINA_TOML)));
        String tomlContent = Files.readString(
                packageDir.resolve(ProjectConstants.BALLERINA_TOML), StandardCharsets.UTF_8);
        String expectedContent = "[build-options]\n" +
                "observabilityIncluded = true\n";
        Assert.assertTrue(tomlContent.contains(expectedContent));

        Assert.assertTrue(Files.exists(packageDir.resolve("service.bal")));
        Assert.assertTrue(Files.notExists(packageDir.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME)));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.TEST_DIR_NAME)));
        Path resourcePath = packageDir.resolve(ProjectConstants.RESOURCE_DIR_NAME);
        Assert.assertFalse(Files.exists(resourcePath));

        Assert.assertTrue(readOutput().contains("Created new package"));
    }

    @Test(description = "Test new command with lib template")
    public void testNewCommandWithLib() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"lib_sample", "-t", "lib"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false);
        new CommandLine(newCommand).parseArgs(args);
        newCommand.execute();
        // Check with spec
        // project_name/
        // - Ballerina.toml
        // - Package.md
        // - Module.md
        // - lib.bal
        // - resources
        // - tests
        //      - lib_test.bal
        // - .gitignore       <- git ignore file

        Path packageDir = tmpDir.resolve("lib_sample");
        Assert.assertTrue(Files.exists(packageDir));
        Assert.assertTrue(Files.isDirectory(packageDir));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.BALLERINA_TOML)));

        String tomlContent = Files.readString(
                packageDir.resolve(ProjectConstants.BALLERINA_TOML), StandardCharsets.UTF_8);

        String expectedTomlContent = "[package]\n" +
                "org = \"" + System.getProperty("user.name").replaceAll("[^a-zA-Z0-9_]", "_") + "\"\n" +
                "name = \"lib_sample\"\n" +
                "version = \"0.1.0\"\n" +
                "distribution = \"" + RepoUtils.getBallerinaShortVersion() + "\"" +
                "\n";
        Assert.assertTrue(tomlContent.contains(expectedTomlContent));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME)));
        Assert.assertTrue(Files.exists(packageDir.resolve("lib_sample.bal")));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.TEST_DIR_NAME)));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.RESOURCE_DIR_NAME)));

        Assert.assertTrue(readOutput().contains("Created new package"));
    }

    @Test(description = "Test new command with invalid project name", dataProvider = "invalidProjectNames")
    public void testNewCommandWithInvalidProjectName(String projectName, String derivedPkgName) throws IOException {
        // Test if no arguments was passed in
        String[] args = {projectName};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false);
        new CommandLine(newCommand).parseArgs(args);
        newCommand.execute();
        Path packageDir = tmpDir.resolve(projectName);
        Assert.assertTrue(Files.exists(packageDir));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.BALLERINA_TOML)));
        Assert.assertTrue(Files.exists(packageDir.resolve("main.bal")));
        String buildOutput = readOutput().replaceAll("\r", "");
        Assert.assertEquals(buildOutput, "package name is derived as '" + derivedPkgName + "'. " +
                "Edit the Ballerina.toml to change it.\n\n" +
                "Created new package '" + derivedPkgName + "' at " + projectName + ".\n");
    }

    @Test(description = "Test new command with invalid template")
    public void testNewCommandWithInvalidTemplate() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"myproject", "-t", "invalid"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false, homeCache);
        new CommandLine(newCommand).parseArgs(args);
        newCommand.execute();
        Assert.assertTrue(readOutput().contains("invalid package name provided"));
    }

    @Test(description = "Test new command with central template in the local cache")
    public void testNewCommandWithTemplateInLocalCache() throws IOException {
        // Test if no arguments was passed in
        String templateArg = "admin/Sample:0.1.5";
        String[] args = {"sample_pull_local", "-t", templateArg};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false, homeCache);
        new CommandLine(newCommand).parseArgs(args);
        newCommand.execute();

        Path packageDir = tmpDir.resolve("sample_pull_local");
        Assert.assertTrue(Files.exists(packageDir));

        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.BALLERINA_TOML)));
        String expectedTomlContent = "[package]\n" +
                "org = \"admin\"\n" +
                "name = \"sample_pull_local\"\n" +
                "version = \"0.1.5\"\n" +
                "export = [\"sample_pull_local\"]\n" +
                "distribution = \"slbeta4\"\n" +
                "\n[build-options]\n" +
                "observabilityIncluded = true\n";
        Assert.assertEquals(
                readFileAsString(packageDir.resolve(ProjectConstants.BALLERINA_TOML)), expectedTomlContent);

        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME)));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.GITIGNORE_FILE_NAME)));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.DEVCONTAINER)));
        Assert.assertTrue(readOutput().contains("Created new package"));
    }

    @Test(description = "Test new command by pulling a central template without specifying version")
    public void testNewCommandWithTemplateCentralPullWithoutVersion() throws IOException {
        // Test if no arguments was passed in
        String templateArg = "parkavik/Sample";
        String[] args = {"sample_pull_WO_Module_Version", "-t", templateArg};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false, homeCache);
        new CommandLine(newCommand).parseArgs(args);
        newCommand.execute();

        Path packageDir = tmpDir.resolve("sample_pull_WO_Module_Version");
        Assert.assertTrue(Files.exists(packageDir));

        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.BALLERINA_TOML)));
        String expectedTomlContent = "[package]\n" +
                "org = \"parkavik\"\n" +
                "name = \"sample_pull_WO_Module_Version\"\n" +
                "version = \"1.0.1\"\n" +
                "export = [\"sample_pull_WO_Module_Version\"]\n" +
                "distribution = \"slbeta4\"\n" +
                "\n[build-options]\n" +
                "observabilityIncluded = true\n";
        Assert.assertEquals(
                readFileAsString(packageDir.resolve(ProjectConstants.BALLERINA_TOML)), expectedTomlContent);
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME)));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.GITIGNORE_FILE_NAME)));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.DEVCONTAINER)));
        Assert.assertTrue(readOutput().contains("Created new package"));
    }

    @Test(description = "Test new command by pulling a central template with specifying version")
    public void testNewCommandWithTemplateCentralPullWithVersion() throws IOException {
        // Test if no arguments was passed in
        String templateArg = "parkavik/Sample:1.0.0";
        String[] args = {"sample_pull", "-t", templateArg};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false, homeCache);
        new CommandLine(newCommand).parseArgs(args);
        newCommand.execute();

        Path packageDir = tmpDir.resolve("sample_pull");
        Assert.assertTrue(Files.exists(packageDir));

        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.BALLERINA_TOML)));
        String expectedTomlContent = "[package]\n" +
                "org = \"parkavik\"\n" +
                "name = \"sample_pull\"\n" +
                "version = \"1.0.0\"\n" +
                "export = [\"sample_pull\"]\n" +
                "distribution = \"slbeta4\"\n" +
                "\n[build-options]\n" +
                "observabilityIncluded = true\n";
        Assert.assertEquals(
                readFileAsString(packageDir.resolve(ProjectConstants.BALLERINA_TOML)), expectedTomlContent);
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME)));
        Assert.assertTrue(Files.exists(packageDir.resolve("docs").resolve("icon.png")));
        Assert.assertTrue(readOutput().contains("Created new package"));
    }

    @Test
    public void testMultiModuleTemplate() throws IOException {
        // Test if no arguments was passed in
        String templateArg = "ballerina/protobuf:1.0.1";
        String[] args = {"sample-multi_module", "-t", templateArg};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false, homeCache);
        new CommandLine(newCommand).parseArgs(args);
        newCommand.execute();

        Path packageDir = tmpDir.resolve("sample-multi_module");
        Assert.assertTrue(Files.exists(packageDir));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME)));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.MODULE_MD_FILE_NAME)));
        Assert.assertTrue(Files.exists(packageDir.resolve("natives.bal")));
        Assert.assertTrue(Files.exists(packageDir.resolve("libs/protobuf-native-1.0.1.txt")));
        Assert.assertTrue(Files.exists(packageDir.resolve("modules/types.timestamp")));
        Assert.assertTrue(Files.exists(packageDir.resolve("modules/types.timestamp/timestamp.bal")));
        Assert.assertTrue(Files.exists(packageDir.resolve("modules/types.wrappers")));
        Assert.assertTrue(Files.exists(packageDir.resolve("modules/types.wrappers/int.bal")));
        Assert.assertTrue(Files.exists(packageDir.resolve("modules/types.wrappers/string.bal")));
        Assert.assertTrue(Files.exists(packageDir.resolve("modules/types.wrappers/" +
                ProjectConstants.MODULE_MD_FILE_NAME)));

        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.BALLERINA_TOML)));
        String expectedTomlContent = "[package]\n" +
                "org = \"ballerina\"\n" +
                "name = \"sample_multi_module\"\n" +
                "version = \"1.0.1\"\n" +
                "export = [\"sample_multi_module\",\"sample_multi_module.types.timestamp\"," +
                "\"sample_multi_module.types.wrappers\"]\n" +
                "distribution = \"slbeta4\"\n" +
                "license = [\"Apache-2.0\"]\n" +
                "authors = [\"Ballerina\"]\n" +
                "keywords = [\"wrappers\"]\n" +
                "repository = \"https://github.com/ballerina-platform/module-ballerina-protobuf\"\n" +
                "\n[build-options]\n" +
                "observabilityIncluded = true\n" +
                "\n[[platform.java11.dependency]]\n" +
                "path = \"libs" + File.separator + "protobuf-native-1.0.1.jar\"";
        Assert.assertEquals(
                readFileAsString(packageDir.resolve(ProjectConstants.BALLERINA_TOML)), expectedTomlContent);
        Assert.assertTrue(readOutput().contains("Created new package"));
    }

    @Test(description = "Test new command by pulling a central template without specifying version")
    public void testNewCommandWithTemplateUntagged() throws IOException {
        // Test if no arguments was passed in
        String templateArg = "ballerinax/twitter";
        String[] args = {"sample_pull_twitter", "-t", templateArg};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false);
        new CommandLine(newCommand).parseArgs(args);
        newCommand.execute();

        Assert.assertTrue(readOutput().contains("unable to create the package: specified package is not a template"));
    }

    @Test(description = "Test new command by pulling a central template with platform libs")
    public void testNewCommandCentralPullWithPlatformDependencies() throws IOException {
        // Test if no arguments was passed in
        String templateArg = "admin/lib_project:0.1.0";
        String[] args = {"sample_pull_libs", "-t", templateArg};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false, homeCache);
        new CommandLine(newCommand).parseArgs(args);
        newCommand.execute();

        Path packageDir = tmpDir.resolve("sample_pull_libs");
        Assert.assertTrue(Files.exists(packageDir));

        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.BALLERINA_TOML)));
        String tomlContent = Files.readString(
                packageDir.resolve(ProjectConstants.BALLERINA_TOML), StandardCharsets.UTF_8);

        String expectedTomlPkgContent = "[package]\n" +
                "org = \"admin\"\n" +
                "name = \"sample_pull_libs\"\n" +
                "version = \"0.1.0\"\n" +
                "export = [\"sample_pull_libs\"]\n" +
                "distribution = \"slbeta4\"\n";
        String expectedTomlLibContent =
                "artifactId = \"snakeyaml\"\n" +
                "groupId = \"org.yaml\"\n" +
                "version = \"1.32\"";

        Assert.assertTrue(tomlContent.contains(expectedTomlPkgContent));
        Assert.assertTrue(tomlContent.contains(expectedTomlLibContent));

        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME)));

        Assert.assertTrue(readOutput().contains("Created new package"));
    }

    @Test(description = "Test new command by pulling a central template having a central dependency")
    public void testNewCommandCentralPullWithCentralDependency() throws IOException {
        // Cache dependency to central --> pramodya/winery:0.1.0
        cacheBalaToCentralRepository(testResources.resolve("balacache-dependencies").resolve("pramodya")
                .resolve("winery").resolve("0.1.0").resolve("any"), "pramodya", "winery", "0.1.0", "any");

        // Publish template has dependency
        // pramodya/template_lib_project:1.0.0 --> pramodya/winery:0.1.0
        cacheBalaToCentralRepository(testResources.resolve("balacache-dependencies").resolve("pramodya")
                        .resolve("template_lib_project").resolve("1.0.0").resolve("any"), "pramodya",
                "template_lib_project", "1.0.0", "any");

        // Cache updated dependency to central --> pramodya/winery:2.1.0
        cacheBalaToCentralRepository(testResources.resolve("balacache-dependencies").resolve("pramodya")
                .resolve("winery").resolve("2.1.0").resolve("any"), "pramodya", "winery", "2.1.0", "any");

        // Create a new package using `template_lib_project` template
        String templateArg = "pramodya/template_lib_project:1.0.0";
        String packageName = "sample_lib_project";
        String[] args = {packageName, "-t", templateArg};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false, homeCache);
        new CommandLine(newCommand).parseArgs(args);
        newCommand.execute();

        Path packageDir = tmpDir.resolve(packageName);
        Assert.assertTrue(Files.exists(packageDir));
        Assert.assertTrue(readOutput().contains("Created new package"));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.BALLERINA_TOML)));
        String depsTomlContent = Files.readString(
                packageDir.resolve(ProjectConstants.DEPENDENCIES_TOML), StandardCharsets.UTF_8);
        Assert.assertTrue(depsTomlContent.contains("[[package]]\n" +
                "org = \"pramodya\"\n" +
                "name = \"winery\"\n" +
                "version = \"0.1.0\""));
    }
    @Test(description = "Test new command by pulling a central template that has simple include patterns")
    public void testNewCommandTemplateWithSimpleIncludePatterns() throws IOException {
        // Publish template to the central
        cacheBalaToCentralRepository(testResources.resolve("balacache-dependencies").resolve("foo")
                .resolve("winery").resolve("0.1.0").resolve("any"), "foo", "winery", "0.1.0", "any");

        // Create a new package with the foo/winery:0.1.0 template
        String packageName = "sample_project";
        String[] args = {packageName, "-t", "foo/winery:0.1.0"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false, homeCache);
        new CommandLine(newCommand).parseArgs(args);
        newCommand.execute();

        // Check if the package directory is created
        Path packageDir = tmpDir.resolve(packageName);
        Assert.assertTrue(Files.exists(packageDir));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.BALLERINA_TOML)));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.DEPENDENCIES_TOML)));

        // Check if the include files are copied
        Assert.assertTrue(Files.exists(packageDir.resolve("include-file.json")));
        Assert.assertTrue(Files.exists(packageDir.resolve("default-module-include/file")));
        Assert.assertTrue(Files.exists(packageDir.resolve("default-module-include-dir/include_text_file.txt")));
        Assert.assertTrue(Files.exists(packageDir.resolve("default-module-include-dir/include_image.png")));
        Assert.assertTrue(Files.exists(packageDir.resolve("modules/services/non-default-module-include/file")));
        Assert.assertTrue(Files.exists(
                packageDir.resolve("modules/services/non-default-module-include-dir/include_text_file.txt")));
        Assert.assertTrue(Files.exists(
                packageDir.resolve("modules/services/non-default-module-include-dir/include_image.png")));
    }

    @Test(description = "Test new command by pulling a central template that has complex include patterns")
    public void testNewCommandTemplateWithComplexIncludePatterns() throws IOException {
        // Publish template to the central
        cacheBalaToCentralRepository(testResources.resolve("balacache-dependencies")
                .resolve("foo-include_test-any-0.1.0"), "foo", "include_test", "0.1.0", "any");

        // Create a new package with the foo/winery:0.1.0 template
        String packageName = "include_pattern_project";
        String[] args = {packageName, "-t", "foo/include_test:0.1.0"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false, homeCache);
        new CommandLine(newCommand).parseArgs(args);
        newCommand.execute();

        // Check if the package directory is created
        Path packageDir = tmpDir.resolve(packageName);
        Assert.assertTrue(Files.exists(packageDir));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.BALLERINA_TOML)));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.DEPENDENCIES_TOML)));

        // Check if the include files are copied
        // foo
        Assert.assertTrue(Files.exists(packageDir.resolve("foo/temp.txt")));
        Assert.assertTrue(Files.exists(packageDir.resolve("include-resources/foo")));
        Assert.assertTrue(Files.exists(packageDir.resolve("modules/services/foo/temp.txt")));
        Assert.assertTrue(Files.exists(packageDir.resolve("modules/services/include-resources/foo")));

        // /bar
        Assert.assertTrue(Files.exists(packageDir.resolve("bar")));
        Assert.assertFalse(Files.exists(packageDir.resolve("include-resources/bar")));

        // baz/
        Assert.assertTrue(Files.exists(packageDir.resolve("include-resources/baz")));
        Assert.assertFalse(Files.exists(packageDir.resolve("include-resources2/baz")));

        // /qux/, /quux/
        Assert.assertTrue(Files.exists(packageDir.resolve("qux/temp.txt")));
        Assert.assertFalse(Files.exists(packageDir.resolve("include-resources/qux/temp.txt")));
        Assert.assertFalse(Files.exists(packageDir.resolve("quux")));
        Assert.assertFalse(Files.exists(packageDir.resolve("include-resources/quux")));

        // *.html
        Assert.assertTrue(Files.exists(packageDir.resolve("include-resources/temp.html")));
        Assert.assertFalse(Files.exists(packageDir.resolve("include-resources/html.txt")));
        Assert.assertFalse(Files.exists(packageDir.resolve("include-resources/html/temp.txt")));

        // foo*bar.*
        Assert.assertTrue(Files.exists(packageDir.resolve("include-resources/foobar.txt")));
        Assert.assertTrue(Files.exists(packageDir.resolve("include-resources/foobazbar.txt")));

        // plug?
        Assert.assertTrue(Files.exists(packageDir.resolve("include-resources2/plugs")));
        Assert.assertFalse(Files.exists(packageDir.resolve("include-resources2/plug")));
        Assert.assertFalse(Files.exists(packageDir.resolve("include-resources2/plugged")));

        // thud[ab]
        Assert.assertTrue(Files.exists(packageDir.resolve("include-resources2/range/thuda")));
        Assert.assertTrue(Files.exists(packageDir.resolve("include-resources2/range/thudb")));
        Assert.assertFalse(Files.exists(packageDir.resolve("include-resources2/range/thudc")));

        // fred[q-s]
        Assert.assertFalse(Files.exists(packageDir.resolve("include-resources2/range/fredp")));
        Assert.assertTrue(Files.exists(packageDir.resolve("include-resources2/range/fredq")));
        Assert.assertTrue(Files.exists(packageDir.resolve("include-resources2/range/fredr")));
        Assert.assertTrue(Files.exists(packageDir.resolve("include-resources2/range/freds")));
        Assert.assertFalse(Files.exists(packageDir.resolve("include-resources2/range/fredt")));

        // **/grault/garply
        Assert.assertTrue(Files.exists(packageDir.resolve("grault/garply/temp.txt")));
        Assert.assertTrue(Files.exists(packageDir.resolve("include-resources/grault/garply/temp.txt")));

        // waldo/xyzzy/**
        Assert.assertTrue(Files.exists(packageDir.resolve("waldo/xyzzy/temp.txt")));
        Assert.assertTrue(Files.exists(packageDir.resolve("include-resources/waldo/xyzzy/temp.txt")));

        // babble/**/bar
        Assert.assertTrue(Files.exists(packageDir.resolve("include-resources/babble/fuu/bar")));

        // *.rs - include all files with extension .rs
        // !corge.rs - exclude only corge.rs
        Assert.assertTrue(Files.exists(packageDir.resolve("include-resources/wombat.rs")));
        Assert.assertTrue(Files.exists(packageDir.resolve("include-resources/garply.rs")));
        Assert.assertFalse(Files.exists(packageDir.resolve("include-resources/corge.rs")));

        // exact file paths
        Assert.assertTrue(Files.exists(packageDir.resolve("include-resources/thud/temp.txt")));
        Assert.assertTrue(Files.exists(packageDir.resolve("include-resources/x.js")));
    }

    @Test(description = "Test new command without arguments")
    public void testNewCommandNoArgs() throws IOException {
        // Test if no arguments was passed in
        String[] args = {};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();

        Assert.assertTrue(readOutput().contains("project name is not provided"));
    }

    @Test(description = "Test new command with multiple arguments")
    public void testNewCommandMultipleArgs() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"sample2", "sample3"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();

        Assert.assertTrue(readOutput().contains("too many arguments"));
    }

    @Test(description = "Test new command with argument and a help")
    public void testNewCommandArgAndHelp() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"sample2", "--help"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();

        Assert.assertTrue(readOutput().contains("ballerina-new - Create a new Ballerina package"));
    }

    @Test(description = "Test new command with help flag")
    public void testNewCommandWithHelp() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"-h"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();

        Assert.assertTrue(readOutput().contains("ballerina-new - Create a new Ballerina package"));
    }

    @Test(description = "Test if directory already exists")
    public void testNewCommandDirectoryExist() throws IOException {
        // Test if no arguments was passed in
        Files.createDirectory(tmpDir.resolve("exist"));
        String[] args = {"exist"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();

        Assert.assertTrue(readOutput().contains("destination '"
                + tmpDir.resolve("exist").toString()
                + "' already exists"));
    }

    @Test(description = "Test if creating inside a ballerina project")
    public void testNewCommandInsideProject() throws IOException {
        // Test if no arguments was passed in
        String[] args = {"parent"};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();
        readOutput(true);

        Assert.assertTrue(Files.isDirectory(tmpDir.resolve("parent")));

        String[] args2 = {"subdir"};
        newCommand = new NewCommand(tmpDir.resolve("parent").resolve("sub_dir"), printStream, false);
        new CommandLine(newCommand).parse(args2);
        newCommand.execute();

        Assert.assertFalse(readOutput().contains("directory is already a ballerina project."));
        Assert.assertFalse(Files.isDirectory(tmpDir.resolve("parent").resolve("sub_dir").resolve("subdir")));
    }

    @Test(description = "Test if creating within a ballerina project", dependsOnMethods = "testNewCommandInsideProject")
    public void testNewCommandWithinProject() throws IOException {
        // Test if no arguments was passed in
        Path projectPath = tmpDir.resolve("parent").resolve("sub-dir");
        Files.createDirectory(projectPath);
        String[] args = {"sample"};
        NewCommand newCommand = new NewCommand(projectPath, printStream, false);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();

        Assert.assertFalse(readOutput().contains("directory is already within a ballerina project."));
        Assert.assertFalse(Files.isDirectory(tmpDir.resolve("parent").resolve("sub_dir").resolve("sample")));
    }

    @Test(description = "Test new command with invalid length package name")
    public void testNewCommandWithInvalidLengthPackageName() throws IOException {
        String longPkgName = "thisIsVeryLongPackageJustUsingItForTesting"
                + "thisIsVeryLongPackageJustUsingItForTesting"
                + "thisIsVeryLongPackageJustUsingItForTesting"
                + "thisIsVeryLongPackageJustUsingItForTesting"
                + "thisIsVeryLongPackageJustUsingItForTesting"
                + "thisIsVeryLongPackageJustUsingItForTesting"
                + "thisIsVeryLongPackageJustUsingItForTesting";
        String[] args = {longPkgName};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();

        Assert.assertTrue(readOutput().contains("invalid package name : '" + longPkgName + "' :\n"
                + "Maximum length of package name is 256 characters."));
    }

    @DataProvider(name = "invalidPackageNames")
    public Object[][] provideInvalidPackageNames() {
        return new Object[][] {
                { "_my_package", "new-pkg-with-initial-underscore.txt" },
                { "my_package_", "new-pkg-with-trailing-underscore.txt" },
                { "my__package", "new-pkg-with-consecutive-underscore.txt" }
        };
    }

    @Test(description = "Test new command with invalid package names", dataProvider = "invalidPackageNames")
    public void testNewCommandWithInvalidPackageNames1(String packageName, String outputLog) throws IOException {
        String[] args = {packageName};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false);
        new CommandLine(newCommand).parse(args);
        newCommand.execute();

        String buildLog = readOutput();
        Assert.assertEquals(buildLog.replaceAll("\r", ""), getOutput(outputLog));
    }

    @DataProvider(name = "PackageNameHasOnlyNonAlphanumeric")
    public Object[][] providePackageNameHasOnlyNonAlphanumeric() {
        return new Object[][] {
                { "#", "my_package" },
                { "_", "my_package" }
        };
    }
    @Test(description = "Test new command with package name has only non alpha-numeric characters",
            dataProvider = "PackageNameHasOnlyNonAlphanumeric")
    public void testNewCommandWithPackageNameHasOnlyNonAlphanumeric(String pkgName, String derivedPkgName)
            throws IOException {
        String[] args = {pkgName};
        NewCommand newCommand = new NewCommand(tmpDir, printStream, false);
        new CommandLine(newCommand).parseArgs(args);
        newCommand.execute();
        Path packageDir = tmpDir.resolve(pkgName);
        Assert.assertTrue(Files.exists(packageDir));
        Assert.assertTrue(Files.exists(packageDir.resolve(ProjectConstants.BALLERINA_TOML)));
        Assert.assertTrue(Files.exists(packageDir.resolve("main.bal")));
        String buildOutput = readOutput().replaceAll("\r", "");
        Assert.assertEquals(buildOutput, "package name is derived as '" + derivedPkgName + "'. " +
                "Edit the Ballerina.toml to change it.\n\n" +
                "Created new package '" + derivedPkgName + "' at " + pkgName + ".\n");
    }

    static class Copy extends SimpleFileVisitor<Path> {
        private Path fromPath;
        private Path toPath;
        private StandardCopyOption copyOption;


        public Copy(Path fromPath, Path toPath, StandardCopyOption copyOption) {
            this.fromPath = fromPath;
            this.toPath = toPath;
            this.copyOption = copyOption;
        }

        public Copy(Path fromPath, Path toPath) {
            this(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException {

            Path targetPath = toPath.resolve(fromPath.relativize(dir).toString());
            if (!Files.exists(targetPath)) {
                Files.createDirectory(targetPath);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {

            Files.copy(file, toPath.resolve(fromPath.relativize(file).toString()), copyOption);
            return FileVisitResult.CONTINUE;
        }
    }

    // Test if a path given to new command
}
