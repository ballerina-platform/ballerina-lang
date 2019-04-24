/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.client.generator.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holding generated file definitions in the compile time.
 * Later these will be written in to target folder in generate code phase.
 *
 * @since 0.990.3
 */
public class FileDefinitionHolder {
    private static final FileDefinitionHolder definitionHolder = new FileDefinitionHolder();
    private static Map<String, String> fileDefinitionMap = new ConcurrentHashMap<>();

    private FileDefinitionHolder() {
    }

    /**
     * Get the current instance of the file definition holder.
     *
     * @return {@link FileDefinitionHolder}
     */
    public static FileDefinitionHolder getInstance() {
        return definitionHolder;
    }

    /**
     * Add file definition under file name in to the definition map.
     *
     * @param fileName    file name for the service
     * @param fileContent file content for the service
     */
    public void addDefinition(String fileName, String fileContent) {
        fileDefinitionMap.put(fileName, fileContent);
    }

    /**
     * Get the file definition map.
     *
     * @return {@link Map} of strings
     */
    public Map<String, String> getFileDefinitionMap() {
        return fileDefinitionMap;
    }

    /**
     * Remove definition related to the given file name from the definition map.
     *
     * @param fileName file name to be removed
     */
    public void removeFileDefinition(String fileName) {
        fileDefinitionMap.remove(fileName);
    }
}
