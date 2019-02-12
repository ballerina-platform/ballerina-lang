/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.testerina.util;

import org.ballerinalang.launcher.LauncherUtils;
import org.ballerinalang.testerina.core.BTestRunner;
import org.ballerinalang.testerina.core.TesterinaConstants;
import org.ballerinalang.testerina.core.entity.TesterinaReport;
import org.wso2.ballerinalang.compiler.FileSystemProjectDirectory;
import org.wso2.ballerinalang.compiler.SourceDirectory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Utility methods to run Ballerina tests.
 *
 * @since 0.990.4
 */
public class BTestUtil {

    /**
     * Method to run all tests in a Ballerina project.
     *
     * @param sourceRoot        the source root
     * @param enableExpFeatures whether experimental features should be allowed
     * @return                  the Testerina Report for the tests run
     */
    public static TesterinaReport runTestsInPackage(String sourceRoot, boolean enableExpFeatures) {
        BTestRunner testRunner = new BTestRunner();
        Path sourceRootPath = LauncherUtils.getSourceRootPath(Paths.get(sourceRoot).toString());
        SourceDirectory sourceDirectory = new FileSystemProjectDirectory(sourceRootPath);
        List<String> sourceFileList = sourceDirectory.getSourcePackageNames();
        Path[] paths = sourceFileList.stream()
                .map(Paths::get)
                .sorted()
                .toArray(Path[]::new);
        TesterinaUtils.setManifestConfigs(sourceRootPath);
        testRunner.runTest(sourceRootPath.toString(), paths, null, enableExpFeatures);
        TesterinaUtils.cleanUpDir(sourceRootPath.resolve(TesterinaConstants.TESTERINA_TEMP_DIR));
        return testRunner.getTesterinaReport();
    }
}
