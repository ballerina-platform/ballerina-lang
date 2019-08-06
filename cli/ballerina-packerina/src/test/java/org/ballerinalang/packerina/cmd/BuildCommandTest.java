/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Build command tests.
 *
 * @since 1.0
 */
public class BuildCommandTest extends CommandTest {
    private Path moduleBalo;
    private Path testResources;

    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        try {
            this.testResources = super.tmpDir.resolve("build-test-resources");
            URI testResourcesURI = getClass().getClassLoader().getResource("test-resources").toURI();
            Files.walkFileTree(Paths.get(testResourcesURI), new CompileCommandTest.Copy(Paths.get(testResourcesURI),
                    this.testResources));
        } catch (URISyntaxException e) {
            Assert.fail("error loading resources");
        }
    }

    @Test(description = "Build non .bal file")
    public void nonBalFileBuildTest() {
        Path nonBalFilePath = this.testResources.resolve("non-bal-file");
        BuildCommand buildCommand = new BuildCommand(nonBalFilePath, printStream, printStream, false);
        new CommandLine(buildCommand).parse("hello_world.txt");
        String exMsg = executeAndGetException(buildCommand);
        Assert.assertEquals(exMsg, "error: invalid ballerina source path, it should either be a module name in a " +
                                          "ballerina project or a file with a \'.bal\' extension.");
    }
    
    @Test(description = "Build a valid ballerina file")
    public void buildBalFileTest() throws IOException {
        Path validBalFilePath = this.testResources.resolve("valid-bal-file");
        // set valid source root
        BuildCommand buildCommand = new BuildCommand(validBalFilePath, printStream, printStream, false);
        // name of the file as argument
        new CommandLine(buildCommand).parse("hello_world.bal");
        buildCommand.execute();
        
        String buildLog = readOutput(true);
        Assert.assertTrue(buildLog.contains("Compiling source"));
        Assert.assertTrue(buildLog.contains("\thello_world.bal"));
        Assert.assertTrue(buildLog.contains("Generating executables"));
        Assert.assertTrue(buildLog.contains("\thello_world-executable.jar"));
        Assert.assertTrue(Files.exists(this.testResources
                .resolve("valid-bal-file")
                .resolve("hello_world-executable.jar")));
    
        Files.delete(this.testResources
                .resolve("valid-bal-file")
                .resolve("hello_world-executable.jar"));
    }
    
    @Test(description = "Build a valid ballerina file by passing invalid source root path and absolute bal file path")
    public void buildBalFileWithAbsolutePathTest() throws IOException {
        Path validBalFilePath = this.testResources.resolve("valid-bal-file");
        // set an invalid source root
        BuildCommand buildCommand = new BuildCommand(this.testResources, printStream, printStream, false);
        // give absolute path as arg
        new CommandLine(buildCommand).parse(validBalFilePath.resolve("hello_world.bal").toAbsolutePath().toString());
        buildCommand.execute();
        
        String buildLog = readOutput(true);
        Assert.assertTrue(buildLog.contains("Compiling source"));
        Assert.assertTrue(buildLog.contains("\thello_world.bal"));
        Assert.assertTrue(buildLog.contains("Generating executables"));
        Assert.assertTrue(buildLog.contains("\thello_world-executable.jar"));
        Assert.assertTrue(Files.exists(this.testResources
                .resolve("valid-bal-file")
                .resolve("hello_world-executable.jar")));
        
        Files.delete(this.testResources
                .resolve("valid-bal-file")
                .resolve("hello_world-executable.jar"));
    }
    
    @Test(description = "Build a valid ballerina file with invalid source root and bal file name")
    public void buildBalFileWithInvalidSourceRootTest() {
        // give an invalid source path
        BuildCommand buildCommand = new BuildCommand(this.testResources.resolve("oo"), printStream, printStream, false);
        // the name of the bal file
        new CommandLine(buildCommand).parse("hello_world.bal");
        
        String exMsg = executeAndGetException(buildCommand);
        Assert.assertEquals(exMsg, "error: '" + this.testResources.resolve("oo").resolve("hello_world.bal").toString() +
                                   "' ballerina file does not exist.");
    }
    
    @Test(description = "Build non existing bal file with a valid source root path")
    public void nonExistingBalFileTest() {
        // valid source root path
        Path validBalFilePath = this.testResources.resolve("valid-bal-file");
        BuildCommand buildCommand = new BuildCommand(validBalFilePath, printStream, printStream, false);
        // non existing bal file
        new CommandLine(buildCommand).parse("xyz.bal");
        
        String exMsg = executeAndGetException(buildCommand);
        Assert.assertEquals(exMsg, "error: '" + validBalFilePath.resolve("xyz.bal").toString() + "' ballerina file " +
                                   "does not exist.");
    }
    
    @Test(description = "Build a bal file without passing bal file name as arg")
    public void buildBalFileWithNoArg() {
        // valid source root path
        Path validBalFilePath = this.testResources.resolve("valid-bal-file");
        BuildCommand buildCommand = new BuildCommand(validBalFilePath, printStream, printStream, false);
        // non existing bal file
        new CommandLine(buildCommand).parse();
        
        String exMsg = executeAndGetException(buildCommand);
        Assert.assertEquals(exMsg, "error: you are trying to build a ballerina project but there is no " +
                                   "Ballerina.toml file. Run 'ballerina new' from '" +
                                   this.testResources.resolve("valid-bal-file") + "' to initialize it as a project.");
    }
    
    @Test(description = "Build bal file with no entry")
    public void buildBalFileWithNoEntry() {
        // valid source root path
        Path validBalFilePath = this.testResources.resolve("valid-bal-file-with-no-entry");
        BuildCommand buildCommand = new BuildCommand(validBalFilePath, printStream, printStream, false);
        // non existing bal file
        new CommandLine(buildCommand).parse("hello_world.bal");
        
        String exMsg = executeAndGetException(buildCommand);
        Assert.assertEquals(exMsg, "error: no entry points found in '" +
                                   validBalFilePath.resolve("hello_world.bal").toString() + "'.");
    }
    
    @Test(description = "Test Build Command in a Project")
    public void testBuildCommand() throws IOException {

        // Create jar files for the test since we cannot commit jar files to git.
        Path libs = this.testResources.resolve("valid-project").resolve("libs");
        Files.createDirectory(libs);

        zipFile(libs.resolve("toml4j.jar").toFile(), "toml.class");
        zipFile(libs.resolve("swagger.jar").toFile(), "swagger.class");
        zipFile(libs.resolve("json.jar").toFile(), "json.class");

        // Build the project
        String[] compileArgs = {};
        BuildCommand buildCommand = new BuildCommand(this.testResources.resolve("valid-project"), printStream,
                printStream, false);
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
        String baloName = "mymodule-" + ProgramFileConstants.IMPLEMENTATION_VERSION + "-java-0.1.0.balo";
        this.moduleBalo = target.resolve(ProjectDirConstants.TARGET_BALO_DIRECTORY)
                .resolve(baloName);
        Assert.assertTrue(Files.exists(target.resolve(ProjectDirConstants.TARGET_BALO_DIRECTORY)),
                "Check if balo file exists");

        Path lockFile = this.testResources.resolve("valid-project").resolve(ProjectDirConstants.LOCK_FILE_NAME);
        Assert.assertTrue(Files.exists(lockFile), "Check if lock file is created");

        readOutput(true);
    }

    private static void zipFile(File file, String contentFile) {
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
            ZipEntry e = new ZipEntry(contentFile);
            out.putNextEntry(e);
            StringBuilder sb = new StringBuilder();
            sb.append("Test String");
            byte[] data = sb.toString().getBytes();
            out.write(data, 0, data.length);
            out.closeEntry();
            out.close();
        } catch (FileNotFoundException ex) {
            System.err.format("The file %s does not exist", file.getName());
        } catch (IOException ex) {
            System.err.format("I/O error: " + ex);
        }
    }

    @Test(dependsOnMethods = {"testBuildCommand"})
    public void testBuildOutput() {
        Path bin = this.testResources.resolve("valid-project").resolve(ProjectDirConstants.TARGET_DIR_NAME)
                .resolve(ProjectDirConstants.BIN_DIR_NAME);
        Assert.assertTrue(Files.exists(bin));
        Assert.assertTrue(Files.exists(bin.resolve("mymodule" + ProjectDirConstants.EXEC_SUFFIX +
                                                   ProjectDirConstants.BLANG_COMPILED_JAR_EXT)));
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
    }

    @Test(dependsOnMethods = {"testBuildCommand"}, enabled = false)
    public void testTargetCacheDirectory() throws IOException {
        // check for the cache directory in target
        Path cache = this.testResources.resolve("valid-project").resolve(ProjectDirConstants.TARGET_DIR_NAME)
                .resolve(ProjectDirConstants.CACHES_DIR_NAME);

        // Assert.assertTrue(Files.exists(cache) && Files.isDirectory(cache));
        // check if each module has a bit in cache directory
    }


    @Test(description = "Test Build Command for a single file.",
            dependsOnMethods = "testBuildCommandWithoutArgs",
            enabled = false)
    public void testBuildCommandSingleFile() throws IOException {
        // Build the project
        String[] compileArgs = {"main.bal"};
        BuildCommand buildCommand = new BuildCommand(this.testResources.resolve("valid-project"), printStream,
                printStream, false);
        new CommandLine(buildCommand).parse(compileArgs);
        buildCommand.execute();

        Assert.assertFalse(Files.exists(tmpDir.resolve(ProjectDirConstants.TARGET_DIR_NAME)),
                "Check if target directory is not created");
        Path lockFile = tmpDir.resolve(ProjectDirConstants.LOCK_FILE_NAME);
        Assert.assertFalse(Files.exists(lockFile), "Check if lock file is created");
        //Check if executable jar gets created
        Path execJar = tmpDir.resolve("main.jar");
        Assert.assertTrue(Files.exists(execJar), "Check if jar gets created");
    }

    @Test(description = "Test Build Command for a single file with output flag.",
            dependsOnMethods = "testBuildCommandWithoutArgs",
            enabled = false)
    public void testBuildCommandSingleFileWithOutput() throws IOException {
        // Build the project
        String[] compileArgs = {"main.bal"};
        BuildCommand buildCommand = new BuildCommand(tmpDir, printStream, printStream, false);
        new CommandLine(buildCommand).parse(compileArgs);
        buildCommand.execute();

        Assert.assertFalse(Files.exists(tmpDir.resolve(ProjectDirConstants.TARGET_DIR_NAME)),
                "Check if target directory is not created");
        Path lockFile = tmpDir.resolve(ProjectDirConstants.LOCK_FILE_NAME);
        Assert.assertFalse(Files.exists(lockFile), "Check if lock file is created");
        //Check if executable jar gets created
        Path execJar = tmpDir.resolve("sample.jar");
        Assert.assertTrue(Files.exists(execJar), "Check if jar gets created");
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
