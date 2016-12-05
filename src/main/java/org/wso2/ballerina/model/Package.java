/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerina.model;

import org.wso2.ballerina.model.types.StructType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * {@code Package} represents a Package in a Ballerina Program.
 *
 * @since 1.0.0
 */
public class Package {

    private String fullQualifiedName;
    private String name;

    private List<BallerinaFile> files = new ArrayList<>();
    private List<Service> services = new ArrayList<>();
    private Map<String, Function> publicFunctions = new HashMap<>();
    private Map<String, Function> privateFunctions = new HashMap<>();
    private List<StructType> types = new ArrayList<StructType>();
    //TODO: add TypeConverters and Constants


    /**
     * @param fullQualifiedName Full qualified name of the package
     */
    public Package(String fullQualifiedName) {
        this.fullQualifiedName = fullQualifiedName;
        name = fullQualifiedName.substring(fullQualifiedName.lastIndexOf(".") + 1);
    }

    /**
     * Get the Full qualified name of the package
     *
     * @return FQN of the package
     */
    public String getFullQualifiedName() {
        return fullQualifiedName;
    }

    /**
     * Get the short name of the package
     *
     * @return name of the package
     */
    public String getName() {
        return name;
    }

    /**
     * Get {@code BallerinaFile} list belongs to a package
     *
     * @return list of files in the package
     */
    public List<BallerinaFile> getFiles() {
        return files;
    }

    /**
     * Add a {@code BallerinaFile} to the package
     *
     * @param file Ballerina File
     */
    public void addFiles(BallerinaFile file) {
        files.add(file);

        // Add references of top level entities in the Ballerina file to the package
        services.addAll(file.getServices());

        file.getFunctions().forEach((funcName, function) -> {
            if (function.isPublic()) {
                publicFunctions.put(funcName, function);
            } else {
                privateFunctions.put(funcName, function);
            }
        });

        privateFunctions.putAll(file.getFunctions());
        types.addAll(file.getTypes());
    }

    /**
     * Get all {@code Service} definitions in the package
     *
     * @return list of all Services
     */
    public List<Service> getServices() {
        return services;
    }


    /**
     * Get public {@code Function} definitions in the package
     *
     * @return map of public Functions
     */
    public Map getPublicFunctions() {
        return publicFunctions;
    }

    /**
     * Get a public {@code Function} in the package
     *
     * @param functionName fqn of the function
     * @return public function
     */
    public Function getPublicFunction(String functionName) {
        return publicFunctions.get(functionName);
    }

    /**
     * Get private {@code Function} definitions in the package
     *
     * @return map of private Functions
     */
    public Map getPrivateFunctions() {
        return privateFunctions;
    }

    /**
     * Get a private {@code Function} in the package
     *
     * @param functionName fqn of the function
     * @return private function
     */
    public Function getPrivateFunction(String functionName) {
        return privateFunctions.get(functionName);
    }

    /**
     * Get all {@code StructType} definitions in the package
     *
     * @return list of StructTypes
     */
    public List<StructType> getTypes() {
        return types;
    }

}
