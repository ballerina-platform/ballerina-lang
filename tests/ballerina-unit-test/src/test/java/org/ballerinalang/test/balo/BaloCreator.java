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

import org.ballerinalang.launcher.util.BFileUtil;
import org.ballerinalang.packerina.BuilderUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinalang.util.BLangConstants.USER_REPO_DEFAULT_DIRNAME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_LIB;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.DOT_BALLERINA_REPO_DIR_NAME;

/**
 * Class containing utility methods for creating BALO.
 * 
 * @since 0.975.0
 */
public class BaloCreator {

    private static final String TARGET = "target";

    /**
     * Generates BALO from the provided package and copy it to the ballerina.home directory.
     * 
     * @param projectPath Path to the project
     * @param packageId Package ID
     * @param orgName Organization name
     * @throws IOException If any error occurred while reading the source files
     */
    private static void create(Path projectPath, String packageId, String orgName) throws IOException {
        Path baloPath = Paths.get(USER_REPO_DEFAULT_DIRNAME);
        projectPath = Paths.get("src", "test", "resources").resolve(projectPath);

        // Clear any old balos
        // clearing from .ballerina will remove the .ballerina file as well. Therefore start clearing from
        // another level down
        BFileUtil.delete(projectPath.resolve(baloPath).resolve(DOT_BALLERINA_REPO_DIR_NAME));

        // compile and create the balo
        BuilderUtils.compileWithTestsAndWrite(projectPath, packageId, TARGET + "/" + BALLERINA_HOME_LIB + "/", false,
                                              true, false, true);

        // copy the balo to the temp-ballerina-home/libs/
        BFileUtil.delete(Paths.get(TARGET, BALLERINA_HOME_LIB, DOT_BALLERINA_REPO_DIR_NAME, orgName, packageId));
        BFileUtil.copy(projectPath.resolve(baloPath), Paths.get(TARGET, BALLERINA_HOME_LIB));
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
     * @param orgName   organization name.
     * @param pkgName   package name.
     */
    public static void clearPackageFromRepository(String orgName, String pkgName) {
        BFileUtil.delete(Paths.get(TARGET, BALLERINA_HOME_LIB, DOT_BALLERINA_REPO_DIR_NAME, orgName, pkgName));
    }
}
