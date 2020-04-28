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
import java.util.Map;
import java.util.zip.ZipFile;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.ballerinalang.test.packaging.PackerinaTestUtils.deleteFiles;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_PKG_BINARY_EXT;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.MANIFEST_FILE_NAME;

/**
 * Test cases related to populating dependencies using it's scopes.
 *
 *  @since 1.3.0
 */
public class DependencyScopeTestCase extends BaseTest {
    private Path tempHomeDirectory;
    private Path tempTestResources;
    private Path projectResources;
    private Map<String, String> envVariables;
    private BMainInstance balClient;

    private String jarEntry = "platform-libs/utils.jar";

    @BeforeClass()
    public void setUp() throws IOException, BallerinaTestException {
        this.tempHomeDirectory = Files.createTempDirectory("bal-test-integration-packaging-pathdep-home-");
        this.tempTestResources = Files.createTempDirectory("bal-test-integration-packaging-pathdep-project-");

        // copy resources to a temp
        Path testResource = Paths.get("src", "test", "resources", "packaging", "scope").toAbsolutePath();
        projectResources = tempTestResources.resolve("dependency-scope");
        copyFolder(testResource, this.tempTestResources);

        PackerinaTestUtils.createSettingToml(tempHomeDirectory);
        envVariables = addEnvVariables(PackerinaTestUtils.getEnvVariables());
        balClient = new BMainInstance(balServer);
    }

    /**
     * Build TestProject1 which has a native module which needs an interop jar to compile. Then build TestProject2
     * in which testable package needs an interop jar. Dependency scope is 'compile' for both projects.
     *
     * @throws BallerinaTestException Error when executing the commands.
     */
    @Test(description = "Test compile scope for platform dependency jars")
    public void compileScopeDependencyCase() throws BallerinaTestException, IOException {
        String moduleUtilsBaloFileName = "utils-" + ProgramFileConstants.IMPLEMENTATION_VERSION + "-java8-0.1.0"
                + BLANG_COMPILED_PKG_BINARY_EXT;
        String moduleUtilsBuildMsg = "target" + File.separator + "balo" + File.separator + moduleUtilsBaloFileName;

        Path baloPath = projectResources.resolve("TestProject1" + File.separator + "target" + File.separator +
                "balo");
        File baloFile = new File(baloPath.toString() + File.separator + moduleUtilsBaloFileName);
        File baloZipFile = new File(baloPath.toString() + File.separator +
                moduleUtilsBaloFileName.concat(".zip"));

        LogLeecher moduleUtilsBuildLeecher = new LogLeecher(moduleUtilsBuildMsg);
        balClient.runMain("build", new String[]{"-a", "-c"}, envVariables, new String[]{},
                new LogLeecher[]{moduleUtilsBuildLeecher},
                projectResources.resolve("TestProject1").toString());
        moduleUtilsBuildLeecher.waitForText(5000);

        // Check whether dependency jars getting packed to balo
        Assert.assertTrue(renameFile(baloFile, baloZipFile));
        Assert.assertFalse(isJarExists(baloZipFile, jarEntry));

        // Define the scope as compile for a dependency jar which is needed for testable package
        copy(tempTestResources.resolve("compile").resolve(MANIFEST_FILE_NAME),
                projectResources.resolve("TestProject2").resolve(MANIFEST_FILE_NAME));
        String moduleFooTestMsg = "Tested utils getString method using interop and received: " +
                "This is a test string value !!!";
        LogLeecher moduleFooTestLeecher = new LogLeecher(moduleFooTestMsg);
        balClient.runMain("build", new String[]{"-a"}, envVariables, new String[]{},
                new LogLeecher[]{moduleFooTestLeecher}, projectResources.resolve("TestProject2").toString());
        moduleFooTestLeecher.waitForText(5000);
    }

    /**
     * Build TestProject2 in which testable package needs an interop jar. Then build TestProject1 which has a native
     * module which needs an interop jar to compile. Dependency scope is 'test' for both projects.
     *
     * @throws BallerinaTestException Error when executing the commands.
     */
    @Test(description = "Test test scope for platform dependency jars")
    public void testScopeDependencyCase() throws BallerinaTestException, IOException {
        Path baloPath = projectResources.resolve("TestProject2" + File.separator + "target" + File.separator +
                "balo");
        String moduleFooBaloFileName = "foo-" + ProgramFileConstants.IMPLEMENTATION_VERSION + "-java8-0.1.0"
                + BLANG_COMPILED_PKG_BINARY_EXT;
        File baloFile = new File(baloPath.toString() + File.separator + moduleFooBaloFileName);
        File baloZipFile = new File(baloPath.toString() + File.separator +
                moduleFooBaloFileName.concat(".zip"));

        String moduleFooTestMsg = "Tested utils getString method using interop and received: " +
                "This is a test string value !!!";
        LogLeecher moduleFooTestLeecher = new LogLeecher(moduleFooTestMsg);
        balClient.runMain("build", new String[]{"-a"}, envVariables, new String[]{},
                new LogLeecher[]{moduleFooTestLeecher}, projectResources.resolve("TestProject2").toString());
        moduleFooTestLeecher.waitForText(5000);

        // Check whether dependency jars getting packed to balo
        Assert.assertTrue(renameFile(baloFile, baloZipFile));
        Assert.assertFalse(isJarExists(baloZipFile, jarEntry));

        // Define the scope as test for a dependency jar which is needed for compiling module
        copy(tempTestResources.resolve("test").resolve(MANIFEST_FILE_NAME),
                projectResources.resolve("TestProject1").resolve(MANIFEST_FILE_NAME));
        String errorMsg = "error: wso2/utils:0.1.0::main.bal:4:1: {ballerina/java}CLASS_NOT_FOUND" +
                " 'org.wso2.test.StaticMethods'";
        LogLeecher utilsCompileLeecher = new LogLeecher(errorMsg, LogLeecher.LeecherType.ERROR);
        balClient.runMain("build", new String[]{"-a", "-c"}, envVariables, new String[]{},
                new LogLeecher[]{utilsCompileLeecher}, projectResources.resolve("TestProject1").toString());
        utilsCompileLeecher.waitForText(5000);

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

    private boolean renameFile(File oldName, File newName) {
        return oldName.renameTo(newName);
    }

    private boolean isJarExists (File file, String jarEntry) throws IOException {
        try (ZipFile zipFile = new ZipFile(file)) {
            return zipFile.getEntry(jarEntry) != null;
        }
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
