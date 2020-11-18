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
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.ballerinalang.packerina.utils.FileUtils.deleteDirectory;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_JAR_EXT;

/**
 * Build command tests.
 *
 * @since 1.0
 */
public class BuildCommandTest extends CommandTest {
    private Path moduleBalo;
    private Path tplModuleBalo;
    private Path testResources;

    @BeforeClass
    public void setup() throws IOException {
        super.setup();
        try {
            this.testResources = super.tmpDir.resolve("build-test-resources");
            URI testResourcesURI = getClass().getClassLoader().getResource("test-resources").toURI();
            Files.walkFileTree(Paths.get(testResourcesURI), new BuildCommandTest.Copy(Paths.get(testResourcesURI),
                    this.testResources));
        } catch (URISyntaxException e) {
            Assert.fail("error loading resources");
        }
    }

    @Test(description = "Build non .bal file")
    public void testNonBalFileBuild() throws IOException {
        Path nonBalFilePath = this.testResources.resolve("non-bal-file");
        BuildCommand buildCommand = new BuildCommand(nonBalFilePath, printStream, printStream, false, true);
        new CommandLine(buildCommand).parse("hello_world.txt");
        buildCommand.execute();
    
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                "ballerina: invalid Ballerina source path. It should either be a name of a module " +
                                      "in a Ballerina project or a file with a '.bal' extension. Use -a or " +
                                      "--all to build or compile all modules.\n" +
                                      "\n" +
                                      "USAGE:\n" +
                                      "    ballerina build {<ballerina-file> | <module-name> | -a | --all}\n" +
                                      "\n" +
                                      "For more information try --help\n");
    }
    
    @Test(description = "Build a valid ballerina file")
    public void testBuildBalFile() throws IOException {
        Path validBalFilePath = this.testResources.resolve("valid-bal-file");
        // set valid source root
        BuildCommand buildCommand = new BuildCommand(validBalFilePath, printStream, printStream, false, true,
                validBalFilePath);
        // name of the file as argument
        new CommandLine(buildCommand).parse("hello_world.bal");
        buildCommand.execute();
        
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), "Compiling source\n" +
                                      "\thello_world.bal\n" +
                                      "\n" +
                                      "Generating executables\n" +
                                      "\thello_world.jar\n");
        
        Assert.assertTrue(Files.exists(this.testResources
                .resolve("valid-bal-file")
                .resolve("hello_world.jar")));
    
        Files.delete(this.testResources
                .resolve("valid-bal-file")
                .resolve("hello_world.jar"));
    
        readOutput(true);
    }
    
    @Test(description = "Build a valid ballerina file with output flag")
    public void testBuildBalFileWithOutputFlag() throws IOException {
        Path validBalFilePath = this.testResources.resolve("valid-bal-file");
        // set valid source root
        BuildCommand buildCommand = new BuildCommand(validBalFilePath, printStream, printStream, false, true);
        // name of the file as argument
        new CommandLine(buildCommand).parse("-o", "foo.jar", "hello_world.bal");
        buildCommand.execute();

        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), "Compiling source\n" +
                                      "\thello_world.bal\n" +
                                      "\n" +
                                      "Generating executables\n" +
                                      "\tfoo.jar\n");

        Assert.assertTrue(Files.exists(this.testResources.resolve("valid-bal-file").resolve("foo.jar")));
        long executableSize = Files.size(this.testResources.resolve("valid-bal-file").resolve("foo.jar"));
        Files.delete(this.testResources.resolve("valid-bal-file").resolve("foo.jar"));

        // only give the name of the file without extension
        buildCommand = new BuildCommand(validBalFilePath, printStream, printStream, false, true);
        new CommandLine(buildCommand).parse("-o", "bar", "hello_world.bal");
        buildCommand.execute();

        buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), "Compiling source\n" +
                                      "\thello_world.bal\n" +
                                      "\n" +
                                      "Generating executables\n" +
                                      "\tbar.jar\n");

        Assert.assertTrue(Files.exists(this.testResources.resolve("valid-bal-file").resolve("bar.jar")));
        Assert.assertEquals(Files.size(this.testResources.resolve("valid-bal-file").resolve("bar.jar")),
                executableSize);
        Files.delete(this.testResources.resolve("valid-bal-file").resolve("bar.jar"));
    
    
        // create executable in a different path
        buildCommand = new BuildCommand(validBalFilePath, printStream, printStream, false, true);
        Path helloExecutableTmpDir = Files.createTempDirectory("hello_executable-");
        new CommandLine(buildCommand).parse("-o", helloExecutableTmpDir.toAbsolutePath().toString(), "hello_world.bal");
        buildCommand.execute();
    
        buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), "Compiling source\n" +
                                      "\thello_world.bal\n" +
                                      "\n" +
                                      "Generating executables\n" +
                                      "\t" +
                                      helloExecutableTmpDir.toAbsolutePath().resolve("hello_world.jar") +
                                      "\n");
    
        Assert.assertTrue(Files.exists(helloExecutableTmpDir.toAbsolutePath().resolve("hello_world.jar")));
    
        // create executable in a different path with .jar extension
        buildCommand = new BuildCommand(validBalFilePath, printStream, printStream, false, true);
        new CommandLine(buildCommand).parse("-o",
                helloExecutableTmpDir.toAbsolutePath().resolve("hippo.jar").toString(),
                "hello_world.bal");
        buildCommand.execute();
    
        buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), "Compiling source\n" +
                                      "\thello_world.bal\n" +
                                      "\n" +
                                      "Generating executables\n" +
                                      "\t" +
                                      helloExecutableTmpDir.toAbsolutePath().resolve("hippo.jar") +
                                      "\n");
    
        Assert.assertTrue(Files.exists(helloExecutableTmpDir.toAbsolutePath().resolve("hippo.jar")));
        
        deleteDirectory(helloExecutableTmpDir);

        readOutput(true);
    }
    
    @Test(description = "Build a valid ballerina file by passing invalid source root path and absolute bal file path")
    public void testBuildBalFileWithAbsolutePath() throws IOException {
        Path validBalFilePath = this.testResources.resolve("valid-bal-file");
        // set an invalid source root
        BuildCommand buildCommand = new BuildCommand(this.testResources, printStream, printStream, false, true,
                validBalFilePath);
        // give absolute path as arg
        new CommandLine(buildCommand).parse(validBalFilePath.resolve("hello_world.bal").toAbsolutePath().toString());
        buildCommand.execute();
        
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), "Compiling source\n" +
                                      "\thello_world.bal\n" + "" +
                                      "\n" +
                                      "Generating executables\n" + "" +
                                      "\thello_world.jar\n");
        
        Assert.assertTrue(Files.exists(this.testResources
                .resolve("valid-bal-file")
                .resolve("hello_world.jar")));
        
        Files.delete(this.testResources
                .resolve("valid-bal-file")
                .resolve("hello_world.jar"));
    
        readOutput(true);
    }
    
    @Test(description = "Build a valid ballerina file with invalid source root and bal file name")
    public void testBuildBalFileWithInvalidSourceRoot() throws IOException {
        // give an invalid source path
        BuildCommand buildCommand = new BuildCommand(this.testResources.resolve("oo"), printStream, printStream, 
                                                     false, true);
        // the name of the bal file
        new CommandLine(buildCommand).parse("hello_world.bal");
        buildCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), "ballerina: '" +
                                      this.testResources.resolve("oo").resolve("hello_world.bal").toString() +
                                      "' Ballerina file does not exist.\n");
    }
    
    @Test(description = "Build non existing bal file with a valid source root path")
    public void testNonExistingBalFile() throws IOException {
        // valid source root path
        Path validBalFilePath = this.testResources.resolve("valid-bal-file");
        BuildCommand buildCommand = new BuildCommand(validBalFilePath, printStream, printStream, false, true);
        // non existing bal file
        new CommandLine(buildCommand).parse("xyz.bal");
        buildCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), "ballerina: '" +
                                      validBalFilePath.resolve("xyz.bal").toString() +
                                      "' Ballerina file does not exist.\n");
    }
    
    @Test(description = "Build a bal file without passing bal file name as arg")
    public void testBuildBalFileWithNoArg() throws IOException {
        // valid source root path
        Path validBalFilePath = this.testResources.resolve("valid-bal-file");
        BuildCommand buildCommand = new BuildCommand(validBalFilePath, printStream, printStream, false, true);
        // non existing bal file
        new CommandLine(buildCommand).parse();
        buildCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                                      "ballerina: 'build' command requires a module name or a Ballerina file to " +
                                      "build/compile. Use '-a' or '--all' to build/compile all the modules of " +
                                      "the project.\n" +
                                      "\n" +
                                      "USAGE:\n" +
                                      "    ballerina build {<ballerina-file> | <module-name> | -a | --all}\n");
    }

    @Test(description = "Build all modules with passing arguments")
    public void testBuildAllWithArg() throws IOException {
        // valid source root path
        Path validBalFilePath = this.testResources.resolve("valid-project");
        BuildCommand buildCommand = new BuildCommand(validBalFilePath, printStream, printStream, false, true);
        new CommandLine(buildCommand).parse("-a", "hello2");
        buildCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertTrue(buildLog.contains("too many arguments.\n"));
    }

    @Test(description = "Build bal file with no entry")
    public void testBuildBalFileWithNoEntry() throws IOException {
        // valid source root path
        Path sourceRoot = this.testResources.resolve("valid-bal-file-with-no-entry");
        BuildCommand buildCommand = new BuildCommand(sourceRoot, printStream, printStream, false, true);
        // non existing bal file
        new CommandLine(buildCommand).parse("hello_world.bal");
        
        String exMsg = executeAndGetException(buildCommand);
        Assert.assertEquals(exMsg, "error: no entry points found in '" +
                                   sourceRoot.resolve("hello_world.bal").toString() + "'.");
    }
    
    @Test(description = "Build a valid ballerina file with toml")
    public void testBuildBalFileWithToml() throws IOException {
        Path sourceRoot = this.testResources.resolve("single-bal-file-with-toml");
        BuildCommand buildCommand = new BuildCommand(sourceRoot, printStream, printStream, false, true, sourceRoot);
        new CommandLine(buildCommand).parse("hello_world.bal");
        buildCommand.execute();
        
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), "Compiling source\n" +
                                      "\thello_world.bal\n" + "" +
                                      "\n" +
                                      "Generating executables\n" + "" +
                                      "\thello_world.jar\n");
        
        Assert.assertTrue(Files.exists(sourceRoot.resolve("hello_world.jar")));
    
        Files.delete(sourceRoot.resolve("hello_world.jar"));
    
        readOutput(true);
    }
    
    @Test(description = "Build a ballerina project with no modules.")
    public void testBuildBalProjWithNoModules() throws IOException {
        Path sourceRoot = this.testResources.resolve("project-with-no-modules");
        BuildCommand buildCommand = new BuildCommand(sourceRoot, printStream, printStream, false, true);
        new CommandLine(buildCommand).parse("-a");
        
        String exMsg = executeAndGetException(buildCommand);
        Assert.assertEquals(exMsg, "cannot find module(s) to build/compile as 'src' " +
                                   "directory is missing. modules should be placed inside " +
                                   "an 'src' directory of the project.");
    }
    
    @Test(description = "Build a ballerina project with non existing module.")
    public void testBuildBalProjectWithInvalidModule() throws IOException {
        Path sourceRoot = this.testResources.resolve("project-with-no-modules");
        BuildCommand buildCommand = new BuildCommand(sourceRoot, printStream, printStream, false, true);
        new CommandLine(buildCommand).parse("xyz");
        buildCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""),
                "ballerina: invalid Ballerina source path. It should either be a name of a module " +
                                      "in a Ballerina project or a file with a '.bal' extension. Use -a or --all " +
                                      "to build or compile all modules.\n" +
                                      "\n" +
                                      "USAGE:\n" +
                                      "    ballerina build {<ballerina-file> | <module-name> | -a | --all}\n" +
                                      "\n" +
                                      "For more information try --help\n");
    }
    
    @Test(description = "Build a ballerina project with non existing module.")
    public void testBuildBalProjectToml() throws IOException {
        Path sourceRoot = this.testResources.resolve("ballerina-toml");
        BuildCommand buildCommand = new BuildCommand(sourceRoot, printStream, printStream, false, true);
        new CommandLine(buildCommand).parse("foo", "--skip-tests");
    
        buildCommand.execute();
        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), "Compiling source\n" +
                                      "\tbar/foo:1.2.0\n" +
                                      "\nCreating balos\n" +
                                      "\ttarget" + File.separator + "balo" + File.separator +
                                      "foo-" + ProgramFileConstants.IMPLEMENTATION_VERSION + "-any-1.2.0.balo\n" +
                                      "\n" +
                                      "Generating executables\n" +
                                      "\ttarget" + File.separator + "bin" + File.separator + "foo.jar\n");

        // Commented out below since it does a system exit and prevent rest of the test running.
        // CleanCommand cleanCommand = new CleanCommand(sourceRoot);
        // new CommandLine(cleanCommand).parse();
        // cleanCommand.execute();
        // readOutput(true);
    
        String tomlContent = "";
        Files.write(sourceRoot.resolve("Ballerina.toml"), tomlContent.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        buildCommand = new BuildCommand(sourceRoot, printStream, printStream, false, true);
        new CommandLine(buildCommand).parse("foo");
        String exMsg = executeAndGetException(buildCommand);
        Assert.assertEquals(exMsg, "invalid Ballerina.toml file: organization name and the version of the project " +
                                   "is missing. example: \n" +
                                     "[project]\n" +
                                     "org-name=\"my_org\"\n" +
                                     "version=\"1.0.0\"\n");
    
        tomlContent = "[project]";
        Files.write(sourceRoot.resolve("Ballerina.toml"), tomlContent.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        exMsg = executeAndGetException(buildCommand);
        Assert.assertEquals(exMsg, "invalid Ballerina.toml file: cannot find 'org-name' under [project]");
    
        tomlContent = "[project]\norg-name=\"bar\"";
        Files.write(sourceRoot.resolve("Ballerina.toml"), tomlContent.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        exMsg = executeAndGetException(buildCommand);
        Assert.assertEquals(exMsg, "invalid Ballerina.toml file: cannot find 'version' under [project]");
    
        tomlContent = "[project]\norg-name=\"bar\"\nversion=\"a.b.c\"";
        Files.write(sourceRoot.resolve("Ballerina.toml"), tomlContent.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        exMsg = executeAndGetException(buildCommand);
        Assert.assertEquals(exMsg, "invalid Ballerina.toml file: 'version' under [project] is not semver");
    
        readOutput(true);
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
        String[] compileArgs = {"--all", "--skip-tests"};
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
        Assert.assertTrue(Files.exists(this.moduleBalo),
                "Check if balo file exists");

        String tplBaloName = "mytemplate-" + ProgramFileConstants.IMPLEMENTATION_VERSION + "-any-0.1.0.balo";
        this.tplModuleBalo = target.resolve(ProjectDirConstants.TARGET_BALO_DIRECTORY)
                .resolve(tplBaloName);
        Assert.assertTrue(Files.exists(this.tplModuleBalo),
                "Check if template balo file exists");

        Path lockFile = this.testResources.resolve("valid-project").resolve(ProjectDirConstants.LOCK_FILE_NAME);
        Assert.assertTrue(Files.exists(lockFile), "Check if lock file is created");

        readOutput(true);
    }

    @Test(description = "Build a valid ballerina file with relative path")
    public void testBuildWithRelativePath() throws IOException {
        String buildPath = "relative" + File.separator + "testDir" + File.separator + ".." + File.separator + "testBal"
                + File.separator + "hello_world.bal";
        Path sourceRoot = this.testResources.resolve("valid-bal-file");
        BuildCommand buildCommand = new BuildCommand(sourceRoot, printStream, printStream, false, true, sourceRoot);
        new CommandLine(buildCommand).parse(buildPath);
        buildCommand.execute();
        Assert.assertTrue(Files.exists(sourceRoot.resolve("hello_world.jar")));
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

    @Test(description = "Test Build Command in a Project which use dependency jar which include stored jar.")
    public void testBuildCommandWithStoredJarDependency() throws IOException {
        // Build the project
        String[] compileArgs = {"--all", "--skip-tests"};
        BuildCommand buildCommand = new BuildCommand(this.testResources.resolve("stored-jar-dependency-project"),
                                                     printStream, printStream, false, true);
        new CommandLine(buildCommand).parse(compileArgs);
        buildCommand.execute();
        Path target = this.testResources.resolve("stored-jar-dependency-project")
                .resolve(ProjectDirConstants.TARGET_DIR_NAME);
        Assert.assertTrue(Files.exists(target), "Check if target directory is created");
        Path executablePath = target.resolve(ProjectDirConstants.BIN_DIR_NAME).resolve("mymodule.jar");
        JarFile jarFile = new JarFile(executablePath.toFile());
        Enumeration e = jarFile.entries();
        while (e.hasMoreElements()) {
            JarEntry je = (JarEntry) e.nextElement();
            String name = je.getName();
            int method = je.getMethod();
            if (name.endsWith(BLANG_COMPILED_JAR_EXT)) {
                Assert.assertEquals(method, ZipEntry.STORED);
            }
        }
        readOutput(true);
    }

    @Test(dependsOnMethods = {"testBuildCommand"})
    public void testBuildOutput() throws IOException {
        Path bin = this.testResources.resolve("valid-project").resolve(ProjectDirConstants.TARGET_DIR_NAME)
                .resolve(ProjectDirConstants.BIN_DIR_NAME);
        Assert.assertTrue(Files.exists(bin));
        Path myModuleJar = bin.resolve("mymodule" + BLANG_COMPILED_JAR_EXT);
        Assert.assertTrue(Files.exists(myModuleJar));
        JarFile jar = new JarFile(myModuleJar.toFile());
        // check resources
        Assert.assertNotNull(jar.getJarEntry("resources/testOrg/mymodule/resource.txt"));
        Assert.assertNotNull(jar.getJarEntry("resources/testOrg/mymodule/myresource/insideDirectory.txt"));
    }
    
    @Test(dependsOnMethods = {"testBuildOutput"})
    public void testCleanCommand() {
        CleanCommand cleanCommand = new CleanCommand(Paths.get(System.getProperty("user.dir")), false);
        new CommandLine(cleanCommand).parse("--sourceroot", this.testResources.resolve("valid-project").toString());
        cleanCommand.execute();
        Path bin = this.testResources.resolve("valid-project").resolve(ProjectDirConstants.TARGET_DIR_NAME
                + File.separator + ProjectDirConstants.BIN_DIR_NAME);
        Path balo = this.testResources.resolve("valid-project").resolve(ProjectDirConstants.TARGET_DIR_NAME
                + File.separator + ProjectDirConstants.TARGET_BALO_DIRECTORY);
        Path caches = this.testResources.resolve("valid-project").resolve(ProjectDirConstants.TARGET_DIR_NAME
                + File.separator + ProjectDirConstants.CACHES_DIR_NAME);

        Assert.assertFalse(Files.exists(bin), "Check if bin directory is deleted");
        Assert.assertFalse(Files.exists(balo), "Check if balo directory is deleted");
        Assert.assertFalse(Files.exists(caches), "Check if caches directory is deleted");
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

                        Path testDir = moduleDir.resolve("tests");
                        Assert.assertFalse(Files.exists(testDir));

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


    @Test(dependsOnMethods = {"testBuildCommand"})
    public void testTemplateBaloContents() throws IOException {
        URI baloZip = URI.create("jar:" + tplModuleBalo.toUri().toString());
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

                        Path moduleDir = srcDir.resolve("mytemplate");
                        Assert.assertTrue(Files.exists(moduleDir));

                        // Check if tests are included in the balo
                        Path testDir = moduleDir.resolve("tests");
                        Assert.assertTrue(Files.exists(testDir));

                        Path testFile = testDir.resolve("main_test.bal");
                        Assert.assertTrue(Files.exists(testFile));

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
                        Assert.assertFalse(Files.exists(platformLibDir));

                        Path jarFile = platformLibDir.resolve("toml4j.jar");
                        Assert.assertFalse(Files.exists(jarFile));

                        // Check if resources are copied.
                        // check if template manifest exists
                    } catch (IOException ex) {
                        throw new AssertionError("Error while reading balo content");
                    }
                });
    }

    @Test(dependsOnMethods = {"testBuildCommand"})
    public void testTargetCacheDirectory() throws IOException {
        // check for the cache directory in target
        Path cache = this.testResources.resolve("valid-project").resolve(ProjectDirConstants.TARGET_DIR_NAME)
                .resolve(ProjectDirConstants.CACHES_DIR_NAME);

        // Assert.assertTrue(Files.exists(cache) && Files.isDirectory(cache));
        // check if each module has a bit in cache directory
    }

    @Test(description = "Test the cleaning of target resources in the build command.",
            dependsOnMethods = {"testBuildCommand"})
    public void testTargetClean() throws IOException {
        // If a single module is built only the relevant module's resources should be cleaned,
        // else the entire target will be deleted.
        String[] compileArgs = {"mymodule", "--skip-tests"};
        Path target = this.testResources.resolve("valid-project").resolve(ProjectDirConstants.TARGET_DIR_NAME);

        BuildCommand buildCommand = new BuildCommand(this.testResources.resolve("valid-project"),
                printStream, printStream, false, true);
        new CommandLine(buildCommand).parse(compileArgs);
        buildCommand.execute();

        // Executable of a module that is not built.
        Path executablePath = target.resolve(ProjectDirConstants.BIN_DIR_NAME).resolve("mytemplate.jar");
        Assert.assertTrue(Files.exists(executablePath),
                "Check if executables of other modules are not deleted during a single module build");
    }

    @Test(description = "Test Build Command for a single file.")
    public void testBuildCommandSingleFile() throws IOException {
        // Build the project
        String[] compileArgs = {"hello_world.bal"};
        BuildCommand buildCommand = new BuildCommand(this.testResources.resolve("valid-bal-file"), printStream,
                printStream, false, true);
        new CommandLine(buildCommand).parse(compileArgs);
        buildCommand.execute();
        readOutput();
        Assert.assertFalse(Files.exists(tmpDir.resolve(ProjectDirConstants.TARGET_DIR_NAME)),
                "Check if target directory is not created");
        Path lockFile = tmpDir.resolve(ProjectDirConstants.LOCK_FILE_NAME);
        Assert.assertFalse(Files.exists(lockFile), "Check if lock file is created");
        //Check if executable jar gets created
        Path execJar = Paths.get("hello_world.jar");
        Assert.assertTrue(Files.exists(execJar), "Check if jar gets created");
        Files.delete(execJar);
    }

    @Test(description = "Test Build Command for a single file with output flag.")
    public void testBuildCommandSingleFileWithOutput() throws IOException {
        // Build the project
        String[] compileArgs = {"-osample.jar", "hello_world.bal"};
        BuildCommand buildCommand = new BuildCommand(this.testResources.resolve("valid-bal-file"), printStream,
                printStream, false, true);
        new CommandLine(buildCommand).parse(compileArgs);
        buildCommand.execute();
        readOutput();

        Assert.assertFalse(Files.exists(tmpDir.resolve(ProjectDirConstants.TARGET_DIR_NAME)),
                "Check if target directory is not created");
        Path lockFile = tmpDir.resolve(ProjectDirConstants.LOCK_FILE_NAME);
        Assert.assertFalse(Files.exists(lockFile), "Check if lock file is created");
        //Check if executable jar gets created
        Path execJar = this.testResources.resolve("valid-bal-file").resolve("sample.jar");
        Assert.assertTrue(Files.exists(execJar), "Check if jar gets created");
    }

    @Test(description = "Test the --skip-tests flag in the build command to ensure it avoids compiling tests")
    public void testBuildWithSkipTests() throws IOException {
        // valid source root path where the project contains test bal files with compilation errors
        Path projectWithTestErrors = this.testResources.resolve("project-with-test-errors");
        BuildCommand buildCommand = new BuildCommand(projectWithTestErrors, printStream, printStream,
                false, true);
        new CommandLine(buildCommand).parse("--skip-tests", "-a");
        buildCommand.execute();

        String buildLog = readOutput(true);
        Assert.assertEquals(buildLog.replaceAll("\r", ""), "Compiling source\n" +
                "\ttestOrg/module1:0.1.0\n" +
                "\nCreating balos\n" +
                "\ttarget/balo/module1-" + ProgramFileConstants.IMPLEMENTATION_VERSION + "-any-0.1.0.balo\n" +
                "\nGenerating executables\n" +
                "\ttarget/bin/module1.jar\n");
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
