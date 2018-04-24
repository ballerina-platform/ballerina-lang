/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.net.grpc.proto;

import org.ballerinalang.net.grpc.proto.definition.File;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Holder to store service definitions generated at compile time.
 * This is used at generate code phase to create service proto files.
 *
 * @since 0.970.0
 */
public class FileDefinitionHolder {

    private static final FileDefinitionHolder dataHolder = new FileDefinitionHolder();
    private Map<String, File> serviceDefinitionMap = new HashMap<>();

    private FileDefinitionHolder() {

    }

    /**
     * Returns File definition holder instance.
     * @return dataHolder instance.
     */
    public static FileDefinitionHolder getInstance() {
        return dataHolder;
    }

    /**
     * Add new File Definition to the definition map.
     * @param serviceName Service name.
     * @param file Proto file definition.
     */
    public void addDefinition(String serviceName, File file) {
        serviceDefinitionMap.put(serviceName, file);
    }

    /**
     * Returns proto file definition map.
     * @return proto file definition map.
     */
    public Map<String, File> getDefinitionMap() {
        return Collections.unmodifiableMap(serviceDefinitionMap);
    }

    /**
     * Removes all of the mappings from definition map.
     */
    public void clearDefinitionMap() {
        serviceDefinitionMap.clear();
    }

    /**
     * Removes the mapping for a service name from definition map if it is present.
     * @param serviceName Service name.
     */
    public void removeDefinition(String serviceName) {
        serviceDefinitionMap.remove(serviceName);
    }
}
