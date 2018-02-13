/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.util;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the util class for ballerina.compression.
 */
public class CompressionUtils {
    private static final List<String> filesListInDir = new ArrayList<>();

    /**
     * Populate all the files in a directory to a List.
     *
     * @param dir directory with the files
     */
    public static List<String> populateFilesList(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    filesListInDir.add(file.getAbsolutePath());
                } else {
                    populateFilesList(file);
                }
            }
        }
        return filesListInDir;
    }
}
