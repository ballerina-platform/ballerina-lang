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

package org.wso2.ballerina.core.model;

import org.wso2.ballerina.core.model.types.StructType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code BallerinaFile} represent a content of a Ballerina source file
 * <p>
 * A Ballerina file is structured as follows:
 * <p>
 * [package PackageName;]
 * [import PackageName[ as Identifier];]*
 * (ServiceDefinition | FunctionDefinition | ConnectorDefinition | TypeDefinition | TypeConvertorDefinition |
 * ConstantDefinition)+
 *
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class BallerinaFile {

    private String packageName;
    private List<Import> imports;
    private List<Service> services;
    private Map<String, Function> functions;
    private List<StructType> types;
    //TODO: add TypeConverters
    //TODO: add constants


    /**
     * Get the package name which file belongs to.
     *
     * @return package name
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Set the package name which file belongs to
     *
     * @param packageName name of the package
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Get {@code Import} statements the file
     *
     * @return list of imports
     */
    public List<Import> getImports() {
        return imports;
    }

    /**
     * Set {@code Import} statement list
     *
     * @param imports list of imports
     */
    public void setImports(List<Import> imports) {
        this.imports = imports;
    }

    /**
     * Add an {@code Import}
     *
     * @param importStmt an import to be added to the file
     */
    public void addImport(Import importStmt) {
        if (imports == null) {
            imports = new ArrayList<Import>();
        }
        imports.add(importStmt);
    }

    /**
     * Get {@code Service} list defined in the file
     *
     * @return list of Services
     */
    public List<Service> getServices() {
        return services;
    }

    /**
     * Set {@code Service} list
     *
     * @param services list of Services
     */
    public void setServices(List<Service> services) {
        this.services = services;
    }

    /**
     * Add a {@code Service} to the File
     *
     * @param service a Service
     */
    public void addService(Service service) {
        if (services == null) {
            services = new ArrayList<Service>();
        }
        services.add(service);
    }

    /**
     * Get {@code Function}s defined in the File
     *
     * @return map of functions defined in the File
     */
    public Map<String, Function> getFunctions() {
        return functions;
    }

    /**
     * Set the {@code Function}s
     *
     * @param functions map of Functions
     */
    public void setFunctions(Map<String, Function> functions) {
        this.functions = functions;
    }

    /**
     * Add a {@code Function} to the File
     *
     * @param function a Function to be added to the File
     */
    public void addFunction(Function function) {
        if (functions == null) {
            functions = new HashMap<String, Function>();
        }
        functions.put(function.getFunctionName().getName(), function);
    }

    /**
     * Get {@code Type} list defined in the File
     *
     * @return list of Types
     */
    public List<StructType> getTypes() {
        return types;
    }

    /**
     * Set the list of Types
     *
     * @param types list of Types
     */
    public void setTypes(List<StructType> types) {
        this.types = types;
    }


    /**
     * Add a {@code Type}
     *
     * @param type Type to be added to the File
     */
    public void addType(StructType type) {
        if (types == null) {
            types = new ArrayList<StructType>();
        }
        types.add(type);
    }

}
