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

package org.ballerinalang.test.packaging;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.programfile.ProgramFileConstants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.ballerinalang.test.packaging.PackerinaTestUtils.deleteFiles;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_JAR_EXT;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_PKG_BINARY_EXT;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.MANIFEST_FILE_NAME;

/**
 * Test cases related to packaging dependencies using scopes in Ballerina.toml.
 *
 *  @since 1.2.x
 */
public class DependencyScopeTestCase extends BaseTest {
    private Path tempHomeDirectory;
    private Path tempTestResources;
    private File baloFile;
    private File baloZipFile;
    private Map<String, String> envVariables;
    private BMainInstance balClient;
    private String moduleUtilsBaloFileName = "utils-" + ProgramFileConstants.IMPLEMENTATION_VERSION + "-java8-0.1.0"
            + BLANG_COMPILED_PKG_BINARY_EXT;

    private String moduleFooBaloFileName = "foo-" + ProgramFileConstants.IMPLEMENTATION_VERSION + "-any-0.1.0"
            + BLANG_COMPILED_PKG_BINARY_EXT;

    private String moduleUtilsBuildMsg = "target" + File.separator + "balo" + File.separator + moduleUtilsBaloFileName;
    private String moduleFooBuildMsg = "target" + File.separator + "balo" + File.separator + moduleFooBaloFileName;
    private String moduleFooJarFileName = "foo" + BLANG_COMPILED_JAR_EXT;
    private String executableFilePath = "target" + File.separator + "bin" + File.separator + moduleFooJarFileName;

    @BeforeClass()
    public void setUp() throws IOException, BallerinaTestException {
        this.tempHomeDirectory = Files.createTempDirectory("bal-test-integration-packaging-pathdep-home-");
        this.tempTestResources = Files.createTempDirectory("bal-test-integration-packaging-pathdep-project-");

        // copy resources to a temp
        Path testResource = Paths.get("src", "test", "resources", "packaging", "scope").toAbsolutePath();
        copyFolder(testResource, this.tempTestResources);

        PackerinaTestUtils.createSettingToml(tempHomeDirectory);
        envVariables = addEnvVariables(PackerinaTestUtils.getEnvVariables());
        balClient = new BMainInstance(balServer);
    }

    /**
     * Case1: Build TestProject1. TestProject1 has a module named "foo" module import the utils module which has
     * an interop jar as default scope platform dependency. Then run the jar of TestProject1.
     *
     * @throws BallerinaTestException Error when executing the commands.
     */
    @Test(description = "Case1: Test default scope for dependency jars")
    public void scopeDependencyCase1() throws BallerinaTestException {
        Path projectResources = tempTestResources.resolve("case1");
        Path baloPath = projectResources.resolve("TestProject1" + File.separator + "target" + File.separator +
                "balo");
        baloFile = new File(baloPath.toString() + File.separator + moduleUtilsBaloFileName);
        baloZipFile = new File(baloPath.toString() + File.separator +
                moduleUtilsBaloFileName.concat(".zip"));
        LogLeecher moduleUtilsBuildLeecher = new LogLeecher(moduleUtilsBuildMsg);
        LogLeecher moduleFooBuildLeecher = new LogLeecher(moduleFooBuildMsg);
        balClient.runMain("build", new String[]{"-a"}, envVariables, new String[]{},
                new LogLeecher[]{moduleUtilsBuildLeecher, moduleFooBuildLeecher},
                projectResources.resolve("TestProject1").toString());
        moduleUtilsBuildLeecher.waitForText(5000);
        moduleFooBuildLeecher.waitForText(5000);

        String msg = "This is a test string value !!!";

        LogLeecher fooRunLeecher = new LogLeecher(msg);
        balClient.runMain("run", new String[]{executableFilePath}, envVariables, new String[0],
                new LogLeecher[]{fooRunLeecher}, projectResources.resolve("TestProject1").toString());
        fooRunLeecher.waitForText(10000);

        // Check whether dependency jars getting packed to balo
        Assert.assertTrue(renameFile(baloFile, baloZipFile));
        Assert.assertTrue(isJarExists(baloZipFile, "platform-libs/utils.jar"));
    }

    /**
     * Case2: Build TestProject1. TestProject1 has a module named "foo" module import the utils module which has an
     * interop jar as package scope platform dependency. Then run the jar of TestProject1.
     *
     * @throws BallerinaTestException Error when executing the commands.
     */
    @Test(description = "Case2: Test package scope for dependency jars")
    public void scopeDependencyCase2() throws BallerinaTestException {
        Path projectResources = tempTestResources.resolve("case2");
        Path baloPath = projectResources.resolve("TestProject1" + File.separator + "target" + File.separator +
                "balo");
        baloFile = new File(baloPath.toString() + File.separator + moduleUtilsBaloFileName);
        baloZipFile = new File(baloPath.toString() + File.separator +
                moduleUtilsBaloFileName.concat(".zip"));
        LogLeecher moduleUtilsBuildLeecher = new LogLeecher(moduleUtilsBuildMsg);
        LogLeecher moduleFooBuildLeecher = new LogLeecher(moduleFooBuildMsg);
        balClient.runMain("build", new String[]{"-a"}, envVariables, new String[]{},
                new LogLeecher[]{moduleUtilsBuildLeecher, moduleFooBuildLeecher},
                projectResources.resolve("TestProject1").toString());
        moduleUtilsBuildLeecher.waitForText(5000);
        moduleFooBuildLeecher.waitForText(5000);

        String msg = "This is a test string value !!!";

        LogLeecher fooRunLeecher = new LogLeecher(msg);
        balClient.runMain("run", new String[]{executableFilePath}, envVariables, new String[0],
                new LogLeecher[]{fooRunLeecher}, projectResources.resolve("TestProject1").toString());
        fooRunLeecher.waitForText(10000);

        // Check whether dependency jars getting packed to balo
        Assert.assertTrue(renameFile(baloFile, baloZipFile));
        Assert.assertTrue(isJarExists(baloZipFile, "platform-libs/utils.jar"));
    }

    /**
     * Case3: Build TestProject1 which has a native module which has an interop jar as compile scope platform
     * dependency. Then build TestProject2 which refer to the native module balo. Run the jar of TestProject2
     *
     * @throws BallerinaTestException Error when executing the commands.
     */
    @Test(description = "Case3: Test compile scope for dependency jars")
    public void scopeDependencyCase3() throws BallerinaTestException {
        Path projectResources = tempTestResources.resolve("case3");
        Path baloPath = projectResources.resolve("TestProject1" + File.separator + "target" + File.separator +
                "balo");
        baloFile = new File(baloPath.toString() + File.separator + moduleUtilsBaloFileName);
        baloZipFile = new File(baloPath.toString() + File.separator +
                moduleUtilsBaloFileName.concat(".zip"));
        LogLeecher moduleUtilsBuildLeecher = new LogLeecher(moduleUtilsBuildMsg);
        balClient.runMain("build", new String[]{"-a", "-c"}, envVariables, new String[]{},
                new LogLeecher[]{moduleUtilsBuildLeecher},
                projectResources.resolve("TestProject1").toString());
        moduleUtilsBuildLeecher.waitForText(5000);

        // Without defining the dependency jar of the wso2/utils module in the toml file
        String errorMsg = "error: wso2/utils:0.1.0::/src/utils/main.bal:4:1: {ballerina/java}CLASS_NOT_FOUND " +
                "'org.wso2.test.StaticMethods'";
        LogLeecher errFooBuildLeecher = new LogLeecher(errorMsg, LogLeecher.LeecherType.ERROR);
        balClient.runMain("build", new String[]{"-a"}, envVariables, new String[]{},
                new LogLeecher[]{errFooBuildLeecher}, projectResources.resolve("TestProject2").toString());
        errFooBuildLeecher.waitForText(5000);

        copy(projectResources.resolve(MANIFEST_FILE_NAME), projectResources.resolve("TestProject2").
                resolve(MANIFEST_FILE_NAME));

        // Defined the dependency jar of the wso2/utils module in the toml file
        LogLeecher moduleFooBuildLeecher = new LogLeecher(moduleFooBuildMsg);
        balClient.runMain("build", new String[]{"-a"}, envVariables, new String[]{},
                new LogLeecher[]{moduleFooBuildLeecher}, projectResources.resolve("TestProject2").toString());
        moduleFooBuildLeecher.waitForText(5000);

        String msg = "This is a test string value !!!";
        LogLeecher fooRunLeecher = new LogLeecher(msg);
        balClient.runMain("run", new String[]{executableFilePath}, envVariables, new String[0],
                new LogLeecher[]{fooRunLeecher}, projectResources.resolve("TestProject2").toString());
        fooRunLeecher.waitForText(10000);

        // Check whether dependency jars getting packed to balo
        Assert.assertTrue(renameFile(baloFile, baloZipFile));
        Assert.assertFalse(isJarExists(baloZipFile, "platform-libs/utils.jar"));
    }

    /**
     * Case4: Build TestProject1 which has a native module which has an interop jar as test scope platform dependency.
     * Then build TestProject2 which has test package which has an interop jar as test scope platform dependency.
     *
     * @throws BallerinaTestException Error when executing the commands.
     */
    @Test(description = "Case4: Test test scope dependency jars when packaging balo")
    public void scopeDependencyCase4() throws BallerinaTestException {
        Path projectResources = tempTestResources.resolve("case4");
        Path baloPath = projectResources.resolve("TestProject1" + File.separator + "target" + File.separator +
                "balo");
        baloFile = new File(baloPath.toString() + File.separator + moduleUtilsBaloFileName);
        baloZipFile = new File(baloPath.toString() + File.separator +
                moduleUtilsBaloFileName.concat(".zip"));
        LogLeecher moduleUtilsBuildLeecher = new LogLeecher(moduleUtilsBuildMsg);

        // Define the scope as test for a dependency jar which is needed for compiling
        String errorMsg = "error: wso2/utils:0.1.0::main.bal:4:1: {ballerina/java}CLASS_NOT_FOUND" +
                " 'org.wso2.test.StaticMethods'";
        LogLeecher utilsCompileLeecher = new LogLeecher(errorMsg, LogLeecher.LeecherType.ERROR);
        balClient.runMain("build", new String[]{"-a", "-c"}, envVariables, new String[]{},
                new LogLeecher[]{moduleUtilsBuildLeecher, utilsCompileLeecher},
                projectResources.resolve("TestProject1").toString());
        moduleUtilsBuildLeecher.waitForText(5000);
        utilsCompileLeecher.waitForText(5000);

        // Check whether dependency jars getting packed to balo
        Assert.assertTrue(renameFile(baloFile, baloZipFile));
        Assert.assertFalse(isJarExists(baloZipFile, "platform-libs/utils.jar"));

        String moduleFooTestMsg = "Tested utils getString method using interop and received: " +
                "This is a test string value !!!";
        LogLeecher moduleFooTestLeecher = new LogLeecher(moduleFooTestMsg);
        balClient.runMain("build", new String[]{"-a"}, envVariables, new String[]{},
                new LogLeecher[]{moduleFooTestLeecher}, projectResources.resolve("TestProject2").toString());
        moduleFooTestLeecher.waitForText(5000);
    }

    /**
     * Get environment variables and add ballerina_home as a env variable the tmp directory.
     *
     * @return env directory variable array
     */
    private Map<String, String> addEnvVariables(Map<String, String> envVariables) {
        envVariables.put(ProjectDirConstants.HOME_REPO_ENV_KEY, tempHomeDirectory.toString());
        envVariables.put("BALLERINA_DEV_STAGE_CENTRAL", "true");
        return envVariables;
    }

    public void copyFolder(Path src, Path dest) throws IOException {
        Files.walk(src).forEach(source -> copy(source, dest.resolve(src.relativize(source))));
    }

    public boolean renameFile(File oldName, File newName) {
        return oldName.renameTo(newName);
    }

    public boolean isJarExists (File file, String jarEntry) {
        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                if (entries.nextElement().getName().equals(jarEntry)) {
                    return true;
                }
            }
        } catch (IOException e) {
            //
        }
        return false;
    }

    private void copy(Path source, Path dest) {
        try {
            Files.copy(source, dest, REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @AfterClass
    private void cleanup() throws Exception {
        deleteFiles(this.tempHomeDirectory);
        deleteFiles(this.tempTestResources);
    }
}
