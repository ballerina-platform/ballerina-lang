/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.cli.cmd;

import io.ballerina.cli.launcher.BLauncherException;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static io.ballerina.projects.util.ProjectConstants.CACHES_DIR_NAME;
import static io.ballerina.projects.util.ProjectConstants.DEPENDENCIES_TOML;

/**
 * Test cases for the bal init command, which initializes the dependencies of a package.
 *
 * @since 2.0.0
 */
public class InitCommandTest extends BaseCommandTest {

    private Path dependencyCache;

    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        Path dependency = writePackage(this.tmpDir.resolve("initdep"), "initdep",
                "public function hello() returns string => \"hello\";\n");
        BCompileUtil.compileAndCacheBala(dependency.toString(), testCentralRepoCache);
        dependencyCache = testCentralRepoCache.resolve(CACHES_DIR_NAME + "-" + RepoUtils.getBallerinaShortVersion())
                .resolve("testorg").resolve("initdep").resolve("1.0.0");
    }

    @Test(description = "Installing the dependencies compiles them into the cache and writes Dependencies.toml")
    public void testInitInstallsDependencies() throws IOException {
        ProjectUtils.deleteDirectory(dependencyCache);
        Path project = writePackage(this.tmpDir.resolve("initapp"), "initapp",
                "import testorg/initdep;\n\npublic function main() {\n    string _ = initdep:hello();\n}\n");

        init(project);

        String output = readOutput(true);
        Assert.assertTrue(output.contains("Installed 1 dependency (1 compiled, 0 already cached)."), output);
        Assert.assertTrue(Files.readString(project.resolve(DEPENDENCIES_TOML)).contains("name = \"initdep\""),
                "Dependencies.toml should record the dependency");
        List<Path> variants = variants();
        Assert.assertEquals(variants.size(), 1, variants.toString());
        Assert.assertTrue(Files.exists(variants.get(0).resolve("bir").resolve("initdep.bir")));
        Assert.assertTrue(Files.exists(variants.get(0).resolve(JvmTarget.JAVA_25.code())
                .resolve("testorg-initdep-1.0.0.jar")));

        // An initialized package is skipped, which is not a failure.
        String dependenciesToml = Files.readString(project.resolve(DEPENDENCIES_TOML));
        init(project);
        output = readOutput(true);
        Assert.assertTrue(output.contains("Package 'testorg/initapp' is already initialized"), output);
        Assert.assertEquals(Files.readString(project.resolve(DEPENDENCIES_TOML)), dependenciesToml);

        // Without Dependencies.toml, the package is initialized again with the dependency from the cache.
        Files.delete(project.resolve(DEPENDENCIES_TOML));
        init(project);
        output = readOutput(true);
        Assert.assertTrue(output.contains("Installed 1 dependency (0 compiled, 1 already cached)."), output);
    }

    @Test(description = "An import of a package that cannot be resolved fails the initialization")
    public void testInitWithUnresolvedImport() throws IOException {
        Path project = writePackage(this.tmpDir.resolve("initappmissing"), "initappmissing",
                "import testorg/initmissing;\n\npublic function main() {\n    initmissing:hello();\n}\n");

        String error = "";
        try {
            init(project);
        } catch (BLauncherException e) {
            error = String.join("\n", e.getDetailedMessages());
        }

        String output = readOutput(true);
        Assert.assertTrue(output.contains("cannot resolve module 'testorg/initmissing'"), output);
        Assert.assertTrue(error.contains("compilation of dependencies contains errors"), error);
        Assert.assertFalse(Files.exists(project.resolve(DEPENDENCIES_TOML)),
                "Dependencies.toml should not be written without the unresolved package");
    }

    @Test(description = "Errors in the package do not stop installing its dependencies")
    public void testInitWithErrorsInPackage() throws IOException {
        Path project = writePackage(this.tmpDir.resolve("initapperror"), "initapperror",
                "import testorg/initdep;\n\npublic function main() {\n    int _ = initdep:hello();\n}\n");

        init(project);

        String output = readOutput(true);
        Assert.assertTrue(output.contains("Installed"), output);
        Assert.assertFalse(output.contains("incompatible types"), "Errors in the package are reported by the build");
        Assert.assertTrue(Files.readString(project.resolve(DEPENDENCIES_TOML)).contains("name = \"initdep\""),
                "Dependencies.toml should record the dependency");
        Assert.assertFalse(variants().isEmpty(), "The dependency should be cached");
    }

    @Test(description = "A dependency is resolved to the latest version with the major version in Ballerina.toml")
    public void testInitHonorsMajorVersionInBallerinaToml() throws IOException {
        for (String version : List.of("1.0.0", "1.1.0", "2.0.0")) {
            Path dependency = writePackage(this.tmpDir.resolve("initver-" + version), "initver", version, "",
                    "public function hello() returns string => \"hello\";\n");
            BCompileUtil.compileAndCacheBala(dependency.toString(), testCentralRepoCache);
        }
        Path project = writePackage(this.tmpDir.resolve("initappver"), "initappver", "1.0.0",
                "\n[[dependency]]\norg = \"testorg\"\nname = \"initver\"\nversion = \"1.0.0\"\n",
                "import testorg/initver;\n\npublic function main() {\n    string _ = initver:hello();\n}\n");

        init(project);

        String output = readOutput(true);
        Assert.assertTrue(output.contains("Installed"), output);
        Assert.assertEquals(dependencyVersion(project, "initver"), "1.1.0");
    }

    @Test(description = "Initializing a workspace initializes each of its packages that is not initialized")
    public void testInitWorkspace() throws IOException {
        Path workspace = workspace("initws");

        init(workspace);

        String output = readOutput(true);
        for (String member : List.of("wslib", "wsapp", "wsother")) {
            Assert.assertTrue(output.contains("Initializing testorg/" + member), output);
            Assert.assertTrue(Files.exists(workspace.resolve(member).resolve(DEPENDENCIES_TOML)), member);
        }
        Assert.assertTrue(Files.readString(workspace.resolve("wsapp").resolve(DEPENDENCIES_TOML))
                .contains("name = \"initdep\""), "The dependencies of a workspace package should be recorded");

        // Only the packages that are not initialized are initialized again.
        Files.delete(workspace.resolve("wsapp").resolve(DEPENDENCIES_TOML));
        init(workspace);
        output = readOutput(true);
        Assert.assertTrue(output.contains("Initializing testorg/wsapp"), output);
        Assert.assertTrue(output.contains("Package 'testorg/wslib' is already initialized"), output);
        Assert.assertFalse(output.contains("Initializing testorg/wslib"), output);
    }

    @Test(description = "Initializing a workspace package initializes it and the workspace packages it depends on")
    public void testInitWorkspacePackage() throws IOException {
        Path workspace = workspace("initwsmember");

        init(workspace.resolve("wsapp"));

        String output = readOutput(true);
        Assert.assertTrue(output.contains("Initializing testorg/wslib"), output);
        Assert.assertTrue(output.contains("Initializing testorg/wsapp"), output);
        Assert.assertTrue(Files.exists(workspace.resolve("wslib").resolve(DEPENDENCIES_TOML)));
        Assert.assertTrue(Files.exists(workspace.resolve("wsapp").resolve(DEPENDENCIES_TOML)));
        Assert.assertFalse(Files.exists(workspace.resolve("wsother").resolve(DEPENDENCIES_TOML)),
                "A package the initialized package does not depend on should not be initialized");
    }

    @Test(description = "A package that fails to initialize does not stop the other packages of the workspace")
    public void testInitWorkspaceWithFailingPackage() throws IOException {
        Path workspace = Files.createDirectories(this.tmpDir.resolve("initwsfailing"));
        Files.writeString(workspace.resolve(ProjectConstants.BALLERINA_TOML),
                "[workspace]\npackages = [\"wsbroken\", \"wsok\"]\n");
        writePackage(workspace.resolve("wsbroken"), "wsbroken",
                "import testorg/initmissing;\n\npublic function main() {\n    initmissing:hello();\n}\n");
        writePackage(workspace.resolve("wsok"), "wsok",
                "import testorg/initdep;\n\npublic function main() {\n    string _ = initdep:hello();\n}\n");

        String error = "";
        try {
            init(workspace);
        } catch (BLauncherException e) {
            error = String.join("\n", e.getDetailedMessages());
        }

        String output = readOutput(true);
        Assert.assertTrue(error.contains("failed to initialize the dependencies of testorg/wsbroken"), error);
        Assert.assertFalse(error.contains("testorg/wsok"), error);
        Assert.assertTrue(output.contains("cannot resolve module 'testorg/initmissing'"), output);
        Assert.assertFalse(Files.exists(workspace.resolve("wsbroken").resolve(DEPENDENCIES_TOML)),
                "Dependencies.toml should not be written for the package that failed");
        Assert.assertTrue(Files.readString(workspace.resolve("wsok").resolve(DEPENDENCIES_TOML))
                .contains("name = \"initdep\""), "The other packages should be initialized");
    }

    @Test(description = "Initializing from a directory inside a package initializes the package")
    public void testInitFromPackageSubdirectory() throws IOException {
        Path project = writePackage(this.tmpDir.resolve("initappsub"), "initappsub",
                "import testorg/initdep;\n\npublic function main() {\n    string _ = initdep:hello();\n}\n");
        Path module = Files.createDirectories(project.resolve("modules").resolve("util"));
        Files.writeString(module.resolve("util.bal"), "public function util() {\n}\n");

        init(module);

        String output = readOutput(true);
        Assert.assertTrue(output.contains("Installed"), output);
        Assert.assertTrue(Files.readString(project.resolve(DEPENDENCIES_TOML)).contains("name = \"initdep\""),
                "Dependencies.toml should be written at the root of the package");
        Assert.assertFalse(Files.exists(module.resolve(DEPENDENCIES_TOML)));
    }

    @Test(description = "Installing the dependencies outside a package fails")
    public void testInitOutsidePackage() throws IOException {
        Path directory = Files.createDirectories(this.tmpDir.resolve("not-a-package"));

        new InitCommand(directory, printStream, false).execute();

        String output = readOutput(true);
        Assert.assertTrue(output.contains("not a Ballerina package"), output);
        Assert.assertTrue(output.contains("Use 'bal new .' to create one."), output);
    }

    @Test(description = "Initializing the dependencies of a standalone Ballerina file fails")
    public void testInitStandaloneFile() throws IOException {
        Path directory = Files.createDirectories(this.tmpDir.resolve("init-standalone-file"));
        Files.writeString(directory.resolve("main.bal"), "public function main() {\n}\n");
        InitCommand initCommand = new InitCommand(directory, printStream, false);
        new CommandLine(initCommand).parseArgs("main.bal");

        initCommand.execute();

        String output = readOutput(true);
        Assert.assertTrue(output.contains("initializing the dependencies of a standalone Ballerina file is not "
                + "supported"), output);
        Assert.assertFalse(Files.exists(directory.resolve(DEPENDENCIES_TOML)));
    }

    @Test(description = "The init command takes no arguments")
    public void testInitWithArguments() throws IOException {
        InitCommand initCommand = new InitCommand(this.tmpDir, printStream, false);
        new CommandLine(initCommand).parseArgs("foo", "bar");

        initCommand.execute();

        String output = readOutput(true);
        Assert.assertTrue(output.contains("too many arguments"), output);
        Assert.assertTrue(output.contains("bal init"), output);
    }

    @Test(description = "Test init command with help flag")
    public void testInitCommandWithHelp() throws IOException {
        InitCommand initCommand = new InitCommand(this.tmpDir, printStream, false);
        new CommandLine(initCommand).parseArgs("--help");

        initCommand.execute();

        Assert.assertTrue(readOutput(true).contains("ballerina-init - Initialize the dependencies"));
    }

    /** A workspace in which 'wsapp' depends on 'wslib', which depends on 'initdep', and 'wsother' is unrelated. */
    private Path workspace(String name) throws IOException {
        Path workspace = Files.createDirectories(this.tmpDir.resolve(name));
        Files.writeString(workspace.resolve(ProjectConstants.BALLERINA_TOML),
                "[workspace]\npackages = [\"wsapp\", \"wslib\", \"wsother\"]\n");
        writePackage(workspace.resolve("wslib"), "wslib",
                "import testorg/initdep;\n\npublic function greet() returns string => initdep:hello();\n");
        writePackage(workspace.resolve("wsapp"), "wsapp",
                "import testorg/wslib;\n\npublic function main() {\n    string _ = wslib:greet();\n}\n");
        writePackage(workspace.resolve("wsother"), "wsother", "public function main() {\n}\n");
        return workspace;
    }

    /** Runs the command with the dependencies resolved from the local repositories of the test home. */
    private void init(Path project) {
        new InitCommand(project, printStream, false, true).execute();
    }

    private List<Path> variants() throws IOException {
        if (!Files.isDirectory(dependencyCache)) {
            return List.of();
        }
        try (Stream<Path> paths = Files.list(dependencyCache)) {
            return paths.filter(path -> path.getFileName().toString().matches("[0-9a-f]{16}")).toList();
        }
    }

    private static Path writePackage(Path packagePath, String name, String source) throws IOException {
        return writePackage(packagePath, name, "1.0.0", "", source);
    }

    private static Path writePackage(Path packagePath, String name, String version, String extraToml, String source)
            throws IOException {
        Files.createDirectories(packagePath);
        Files.writeString(packagePath.resolve(ProjectConstants.BALLERINA_TOML),
                "[package]\norg = \"testorg\"\nname = \"" + name + "\"\nversion = \"" + version + "\"\n"
                        + extraToml);
        Files.writeString(packagePath.resolve("main.bal"), source);
        return packagePath;
    }

    private static String dependencyVersion(Path project, String name) throws IOException {
        String dependenciesToml = Files.readString(project.resolve(DEPENDENCIES_TOML));
        int nameIndex = dependenciesToml.indexOf("name = \"" + name + "\"");
        Assert.assertTrue(nameIndex >= 0, dependenciesToml);
        int versionIndex = dependenciesToml.indexOf("version = \"", nameIndex) + "version = \"".length();
        return dependenciesToml.substring(versionIndex, dependenciesToml.indexOf('"', versionIndex));
    }
}
