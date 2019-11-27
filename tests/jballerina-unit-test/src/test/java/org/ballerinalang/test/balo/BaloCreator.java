/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.balo;

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BFileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.FileSystemProjectDirectory;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinalang.util.BLangConstants.USER_REPO_DEFAULT_DIRNAME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_LIB;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.DOT_BALLERINA_REPO_DIR_NAME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.HOME_REPO_DEFAULT_DIRNAME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.USER_DIR;

/**
 * Class containing utility methods for creating BALO.
 * 
 * @since 0.975.0
 */
public class BaloCreator {

    private static final Path TEST_RESOURCES_SOURCE_PATH = Paths.get("src", "test", "resources");

    /**
     * Generates BALO from the provided package and copy it to the ballerina.home directory.
     * 
     * @param projectPath Path to the project
     * @param packageId Package ID
     * @param orgName Organization name
     * @throws IOException If any error occurred while reading the source files
     */
    private static void create(Path projectPath, String packageId, String orgName) throws IOException {
        String buildFolder = Paths.get(System.getProperty(USER_DIR))
                .relativize(Paths.get(System.getProperty(BALLERINA_HOME))).toString();
        Path baloPath = Paths.get(USER_REPO_DEFAULT_DIRNAME);
        projectPath = TEST_RESOURCES_SOURCE_PATH.resolve(projectPath);

        // Clear any old balos
        // clearing from .ballerina will remove the .ballerina file as well. Therefore start clearing from
        // another level down
        BFileUtil.delete(projectPath.resolve(baloPath).resolve(DOT_BALLERINA_REPO_DIR_NAME));

        // compile and create the balo
        compileWithTestsAndWrite(projectPath, packageId, buildFolder + "/" + BALLERINA_HOME_LIB + "/");

        // copy the balo to the temp-ballerina-home/libs/
        BFileUtil.delete(Paths.get(buildFolder, BALLERINA_HOME_LIB, DOT_BALLERINA_REPO_DIR_NAME, orgName, packageId));
        BFileUtil.copy(projectPath.resolve(baloPath), Paths.get(buildFolder, BALLERINA_HOME_LIB));
    }

    /**
     * Helper method to create balo from the source and setup the repository.
     *
     * @param projectRoot   root folder location.
     * @param orgName       org name.
     * @param pkgName       package name.
     */
    public static void createAndSetupBalo(String projectRoot, String orgName, String pkgName) {
        try {
            create(Paths.get(projectRoot), pkgName, orgName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method to clean up pkg from the ballerina repository after tests are run.
     *
     * @param orgName organization name.
     * @param pkgName package name.
     */
    public static void clearPackageFromRepository(String projectRoot, String orgName, String pkgName) {
        String buildFolder = Paths.get(System.getProperty(USER_DIR))
                .relativize(Paths.get(System.getProperty(BALLERINA_HOME))).toString();
        BFileUtil.delete(Paths.get(buildFolder, BALLERINA_HOME_LIB, DOT_BALLERINA_REPO_DIR_NAME, orgName, pkgName));

        // Remove contents of the .ballerina folder in resources, if folder exists.
        BFileUtil.delete(Paths.get(buildFolder, "resources", "test", projectRoot,
                HOME_REPO_DEFAULT_DIRNAME, DOT_BALLERINA_REPO_DIR_NAME, orgName, pkgName));

        // Remove contents of the .ballerina folder from the src.
        Path projectPath = Paths.get(projectRoot);
        projectPath = TEST_RESOURCES_SOURCE_PATH.resolve(projectPath);
        BFileUtil.delete(Paths.get(projectPath.toString(), HOME_REPO_DEFAULT_DIRNAME, DOT_BALLERINA_REPO_DIR_NAME));
    }

    public static void compileWithTestsAndWrite(Path sourceRootPath, String packageName, String targetPath) {
        CompilerContext context = new CompilerContext();
        context.put(SourceDirectory.class, new FileSystemProjectDirectory(sourceRootPath));

        CompileResult compileResult = BCompileUtil
                .compileOnJBallerina(context, sourceRootPath.toString(), packageName, false, true);

        if (compileResult.getErrorCount() > 0) {
            throw new RuntimeException(compileResult.toString());
        }

        BLangPackage bLangPackage = (BLangPackage) compileResult.getAST();
        Compiler compiler = Compiler.getInstance(context);
        compiler.write(bLangPackage, targetPath);
    }
        
}
