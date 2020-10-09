/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.packerina.cmd;

import com.moandjiezana.toml.Toml;
import org.ballerinalang.packerina.model.BaloToml;
import org.ballerinalang.toml.model.Module;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.programfile.ProgramFileConstants;
import picocli.CommandLine;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.stream.Stream;

import static org.ballerinalang.packerina.utils.FileUtils.deleteDirectory;


/**
 * Test cases for ballerina compile command.
 *
 * @since 1.0.0
 */
public class CompileFlagWithBuildCommandTest extends CommandTest {
    private Path moduleBalo;
    private Path testResources;

    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        try {
            this.testResources = super.tmpDir.resolve("compile-test-resources");
            URI testResourcesURI = getClass().getClassLoader().getResource("test-resources").toURI();
            Files.walkFileTree(Paths.get(testResourcesURI),
                    new CompileFlagWithBuildCommandTest.Copy(Paths.get(testResourcesURI), this.testResources));
        } catch (URISyntaxException e) {
            Assert.fail("error loading resources");
        }
    }

    @Test(description = "Compile non .bal file")
    public void testNonBalFileCompile() throws IOException {
        Path nonBalFilePath = this.testResources.resolve("non-bal-file");
        BuildCommand buildCommand = new BuildCommand(nonBalFilePath, printStream, printStream, false, true);
        new CommandLine(buildCommand).parse("-c", "hello_world.txt");
        buildCommand.execute();

        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog, "ballerina: invalid Ballerina source path. It should either be a name of a " +
                                      "module in a Ballerina project or a file with a '.bal' extension. " +
                                      "Use -a or --all to build or compile all modules.\n" +
                                      "\n" +
                                      "USAGE:\n" +
                                      "    ballerina build {<ballerina-file> | <module-name> | -a | --all}\n" +
                                      "\n" +
                                      "For more information try --help\n");
    }

    @Test(description = "Compile a valid ballerina file")
    public void testCompileBalFile() throws IOException {
        Path validBalFilePath = this.testResources.resolve("valid-bal-file");
        // set valid source root
        BuildCommand buildCommand = new BuildCommand(validBalFilePath, printStream, printStream, false, true);
        // name of the file as argument
        new CommandLine(buildCommand).parse("-c", "hello_world.bal");
        buildCommand.execute();

        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog, "ballerina: '-c' or '--compile' can only be used with modules.\n");
    }

    @Test(description = "Compile a valid ballerina file by passing invalid source root path and absolute bal file path")
    public void testCompileBalFileWithAbsolutePath() throws IOException {
        Path validBalFilePath = this.testResources.resolve("valid-bal-file");
        // set an invalid source root
        BuildCommand buildCommand = new BuildCommand(this.testResources, printStream, printStream, false, true);
        // give absolute path as arg
        new CommandLine(buildCommand).parse("-c",
                validBalFilePath.resolve("hello_world.bal").toAbsolutePath().toString());
        buildCommand.execute();

        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog, "ballerina: '-c' or '--compile' can only be used with modules.\n");
    }

    @Test(description = "Compile a valid ballerina file with toml")
    public void testCompileBalFileWithToml() throws IOException {
        Path sourceRoot = this.testResources.resolve("single-bal-file-with-toml");
        BuildCommand buildCommand = new BuildCommand(sourceRoot, printStream, printStream, false, true);
        new CommandLine(buildCommand).parse("-c", "hello_world.bal");
        buildCommand.execute();

        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog, "ballerina: '-c' or '--compile' can only be used with modules.\n");
    }

    @Test(description = "Compile a ballerina project with no modules.")
    public void testCompileBalProjWithNoModules() throws IOException {
        Path sourceRoot = this.testResources.resolve("project-with-no-modules");
        BuildCommand buildCommand = new BuildCommand(sourceRoot, printStream, printStream, false, true);
        new CommandLine(buildCommand).parse("-a");

        String exMsg = executeAndGetException(buildCommand);
        Assert.assertEquals(exMsg, "cannot find module(s) to build/compile as 'src' " +
                                   "directory is missing. modules should be placed inside " +
                                   "an 'src' directory of the project.");
    }

    @Test(description = "Compile a ballerina project with non existing module.")
    public void testCompileBalProjectWithInvalidModule() throws IOException {
        Path sourceRoot = this.testResources.resolve("project-with-no-modules");
        BuildCommand buildCommand = new BuildCommand(sourceRoot, printStream, printStream, false, true);
        new CommandLine(buildCommand).parse("-c", "xyz");
        buildCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog, "ballerina: invalid Ballerina source path. It should either be a name of a " +
                                      "module in a Ballerina project or a file with a '.bal' extension. " +
                                      "Use -a or --all to build or compile all modules.\n" +
                                      "\n" +
                                      "USAGE:\n" +
                                      "    ballerina build {<ballerina-file> | <module-name> | -a | --all}\n" +
                                      "\n" +
                                      "For more information try --help\n");
    }

    @Test(description = "Compile a ballerina project with non existing module.")
    public void testCompileBalProjectToml() throws IOException {
        Path sourceRoot = this.testResources.resolve("ballerina-toml");
        BuildCommand buildCommand = new BuildCommand(sourceRoot, printStream, printStream, false, true);
        new CommandLine(buildCommand).parse("-c", "foo", "--skip-tests");

        buildCommand.execute();
        String compileLog = readOutput(true);
        Assert.assertEquals(compileLog, "Compiling source\n" +
                                      "\tbar/foo:1.2.0\n" +
                                      "\nCreating balos\n" +
                                      "\ttarget/balo/foo-"
                        + ProgramFileConstants.IMPLEMENTATION_VERSION + "-any-1.2.0.balo\n"
                                      );

        deleteDirectory(sourceRoot.resolve("target"));

        String tomlContent = "";
        Files.write(sourceRoot.resolve("Ballerina.toml"), tomlContent.getBytes(),
        StandardOpenOption.TRUNCATE_EXISTING);
        buildCommand = new BuildCommand(sourceRoot, printStream, printStream, false, true);
        new CommandLine(buildCommand).parse("-c", "foo");
        String exMsg = executeAndGetException(buildCommand);
        Assert.assertEquals(exMsg, "invalid Ballerina.toml file: organization name and the version of the project " +
                                   "is missing. example: \n" +
                                   "[project]\n" +
                                   "org-name=\"my_org\"\n" +
                                   "version=\"1.0.0\"\n");

        tomlContent = "[project]";
        Files.write(sourceRoot.resolve("Ballerina.toml"), tomlContent.getBytes(),
                      StandardOpenOption.TRUNCATE_EXISTING);
        exMsg = executeAndGetException(buildCommand);
        Assert.assertEquals(exMsg, "invalid Ballerina.toml file: cannot find 'org-name' under [project]");

        tomlContent = "[project]\norg-name=\"bar\"";
        Files.write(sourceRoot.resolve("Ballerina.toml"), tomlContent.getBytes(),
                      StandardOpenOption.TRUNCATE_EXISTING);
        exMsg = executeAndGetException(buildCommand);
        Assert.assertEquals(exMsg, "invalid Ballerina.toml file: cannot find 'version' under [project]");

        tomlContent = "[project]\norg-name=\"bar\"\nversion=\"a.b.c\"";
        Files.write(sourceRoot.resolve("Ballerina.toml"), tomlContent.getBytes(),
                      StandardOpenOption.TRUNCATE_EXISTING);
        exMsg = executeAndGetException(buildCommand);
        Assert.assertEquals(exMsg, "invalid Ballerina.toml file: 'version' under [project] is not semver");

        readOutput(true);
    }

    @Test(description = "Test Compile Command in a Project")
    public void testBuildCommand() throws IOException {

        // Create jar files for the test since we cannot commit jar files to git.
        Path libs = this.testResources.resolve("valid-project").resolve("libs");
        Files.createDirectory(libs);
        Files.createFile(libs.resolve("toml4j.jar"));
        Files.createFile(libs.resolve("swagger.jar"));
        Files.createFile(libs.resolve("json.jar"));

        // Compile the project
        String[] compileArgs = {"--all", "-c", "--skip-tests"};
        BuildCommand buildCommand = new BuildCommand(this.testResources.resolve("valid-project"), printStream,
                printStream, false, true);
        new CommandLine(buildCommand).parse(compileArgs);
        buildCommand.execute();

        // Validate against the spec
        // - target/         <- directory for compile/build output
        // -- bin/           <- Executables will be created here
        // -- balo/          <- .balo files one per built module
        // --- module1.balo  <- balo object of module1
        // --- module2.balo  <- balo object of module2
        // -- apidocs/
        // --- module1/      <- directory containing the HTML files for API docs
        //                      of module1
        // --- module2/
        // -- kubernetes/    <- output of kubernetes compiler extension if used
        // -- potato/        <- output of potato compiler extension
        // -- cache          <- BIR cache directory

        Path target = this.testResources.resolve("valid-project").resolve(ProjectDirConstants.TARGET_DIR_NAME);
        Assert.assertTrue(Files.exists(target), "Check if target directory is created");

        Assert.assertTrue(Files.exists(target.resolve(ProjectDirConstants.TARGET_BALO_DIRECTORY)),
                "Check if balo directory exists");
        // {module}-{lang spec version}-{platform}-{version}.balo
        String baloName = "mymodule-" + ProgramFileConstants.IMPLEMENTATION_VERSION + "-java11-0.1.0.balo";
        this.moduleBalo = target.resolve(ProjectDirConstants.TARGET_BALO_DIRECTORY)
                .resolve(baloName);
        Assert.assertTrue(Files.exists(target.resolve(ProjectDirConstants.TARGET_BALO_DIRECTORY)),
                "Check if balo file exists");

        Path lockFile = this.testResources.resolve("valid-project").resolve(ProjectDirConstants.LOCK_FILE_NAME);
        Assert.assertTrue(Files.exists(lockFile), "Check if lock file is created");

        readOutput(true);
    }

    @Test(dependsOnMethods = {"testBuildCommand"})
    public void testBaloContents() throws IOException {
        URI baloZip = URI.create("jar:" + moduleBalo.toUri().toString());
        FileSystems.newFileSystem(baloZip, Collections.emptyMap())
                .getRootDirectories()
                .forEach(root -> {
                    try (Stream<Path> stream = Files.list(root)) {
                        // Validate against the spec
                        // /
                        // └─ metadata/
                        //    └─ BALO.toml
                        //    └─ MODULE.toml
                        // └─ src/
                        // └─ resources/
                        // └─ platform-libs/
                        // └─ docs/
                        //    └─ MODULE-DESC.md
                        //    └─ api-docs/
                        Path metadata = root.resolve(ProjectDirConstants.BALO_METADATA_DIR_NAME);
                        Assert.assertTrue(Files.exists(metadata));
                        Assert.assertTrue(Files.isDirectory(metadata));

                        Path baloToml = metadata.resolve(ProjectDirConstants.BALO_METADATA_FILE);
                        Assert.assertTrue(Files.exists(baloToml));

                        Path moduleToml = metadata.resolve(ProjectDirConstants.BALO_MODULE_METADATA_FILE);
                        Assert.assertTrue(Files.exists(moduleToml));

                        // validate the content of the metafiles
                        String moduleTomlContent = new String(Files.readAllBytes(moduleToml));
                        String baloTomlContent = new String(Files.readAllBytes(baloToml));

                        Module module = new Toml().read(moduleTomlContent).to(Module.class);
                        BaloToml balo = new Toml().read(baloTomlContent).to(BaloToml.class);

                        Assert.assertEquals(module.module_version, "0.1.0");
                        Assert.assertEquals(balo.balo_version, "1.0.0");

                        Path srcDir = root.resolve(ProjectDirConstants.SOURCE_DIR_NAME);
                        Assert.assertTrue(Files.exists(srcDir));

                        Path moduleDir = srcDir.resolve("mymodule");
                        Assert.assertTrue(Files.exists(moduleDir));

                        Path mainBal = moduleDir.resolve("main.bal");
                        Assert.assertTrue(Files.exists(mainBal));

                        Path moduleMD = moduleDir.resolve("Module.md");
                        Assert.assertFalse(Files.exists(moduleMD));

                        Path resourceDirInModule = moduleDir.resolve(ProjectDirConstants.RESOURCE_DIR_NAME);
                        Assert.assertFalse(Files.exists(resourceDirInModule));

                        // Check if resources is there
                        Path resourceDir = root.resolve(ProjectDirConstants.RESOURCE_DIR_NAME);
                        Assert.assertTrue(Files.exists(resourceDir));

                        Path resourceDirContent = resourceDir.resolve("resource.txt");
                        Assert.assertTrue(Files.exists(resourceDirContent));

                        // Check for module md
                        Path docsDir = root.resolve(ProjectDirConstants.BALO_DOC_DIR_NAME);
                        Assert.assertTrue(Files.exists(docsDir));

                        Path moduleMdInBalo = docsDir.resolve(ProjectDirConstants.MODULE_MD_FILE_NAME);
                        Assert.assertTrue(Files.exists(moduleMdInBalo));

                        // Check for platform libs
                        Path platformLibDir = root.resolve(ProjectDirConstants.BALO_PLATFORM_LIB_DIR_NAME);
                        Assert.assertTrue(Files.exists(platformLibDir));

                        Path jarFile = platformLibDir.resolve("toml4j.jar");
                        Assert.assertTrue(Files.exists(jarFile));

                        // check if MODULE.toml and BALO.toml can be serialise to


                    } catch (IOException ex) {
                        throw new AssertionError("Error while reading balo content");
                    }
                });

        // Check if imported bir is in the project target
        Path importBir = this.testResources.resolve("valid-project").resolve(ProjectDirConstants.TARGET_DIR_NAME)
                .resolve(ProjectDirConstants.CACHES_DIR_NAME).resolve(ProjectDirConstants.BIR_CACHE_DIR_NAME)
                .resolve("testOrg").resolve("myimport").resolve("0.1.0").resolve("myimport.bir");
        Assert.assertTrue(Files.exists(importBir));

    }

    @Test(dependsOnMethods = {"testBuildCommand"})
    public void testTargetCacheDirectory() throws IOException {
        // check for the cache directory in target
        Path cache = this.testResources.resolve("valid-project").resolve(ProjectDirConstants.TARGET_DIR_NAME)
                .resolve(ProjectDirConstants.CACHES_DIR_NAME);

        // Assert.assertTrue(Files.exists(cache) && Files.isDirectory(cache));
        // check if each module has a bit in cache directory
    }

    // Check compile command inside a module directory


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
}
