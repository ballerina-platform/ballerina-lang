/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.composer.service.workspace.common;

import org.ballerinalang.composer.service.workspace.langconstruct.ModelPackage;
import org.ballerinalang.composer.service.workspace.util.BallerinaProgramContentProvider;
import java.util.Map;

/**
 *  Class with common utility functions used by workspace services.
 */
public class Utils {
    /**
     *
     * @param directoryCount - packagePath
     * @param filePath - file path to parent directory of the .bal file
     * @return parent dir
     */
    public static java.nio.file.Path getProgramDirectory(int directoryCount, java.nio.file.Path filePath) {
        // find program directory
        java.nio.file.Path parentDir = filePath.getParent();
        for (int i = 0; i < directoryCount; ++i) {
            parentDir = parentDir.getParent();
        }
        return parentDir;
    }

    /**
     * Get all the ballerina packages associated with the runtime.
     * @return - packages set
     */
    public static Map<String, ModelPackage> getAllPackages() {
        BallerinaProgramContentProvider programContentProvider = BallerinaProgramContentProvider.getInstance();
        Map<String, ModelPackage> packages = programContentProvider.getAllPackages();
        return packages;
    }
}
