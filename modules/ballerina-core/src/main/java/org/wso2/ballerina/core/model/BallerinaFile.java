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

import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
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
public class BallerinaFile implements Node {

    private String packageName;
    private List<Import> imports = new ArrayList<>();
    private List<Service> services = new ArrayList<>();
    private List<Connector> connectorList = new ArrayList<>();
    private Map<String, Function> functions = new HashMap<>();
    private List<StructType> types = new ArrayList<>();
    private List<FunctionInvocationExpr> funcIExprList = new ArrayList<>();
    //TODO: add TypeConverters
    //TODO: add constants

    private SymScope packageScope;

    private BallerinaFile(
            String packageName,
            List<Import> importList,
            List<Service> serviceList,
            List<Connector> connectorList,
            Map<String, Function> functionMap,
            List<StructType> sTypeList,
            List<FunctionInvocationExpr> funcIExprList) {

        this.packageName = packageName;
        this.imports = importList;
        this.services = serviceList;
        this.connectorList = connectorList;
        this.functions = functionMap;
        this.types = sTypeList;
        this.funcIExprList = funcIExprList;

        packageScope = new SymScope();
    }

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
        functions.put(function.getName(), function);
    }

    public void addFuncInvocationExpr(FunctionInvocationExpr expr) {
        this.funcIExprList.add(expr);
    }

    public FunctionInvocationExpr[] getFuncIExprs() {
        return funcIExprList.toArray(new FunctionInvocationExpr[funcIExprList.size()]);
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
        types.add(type);
    }

    public SymScope getPackageScope() {
        return packageScope;
    }

    public void setPackageScope(SymScope packageScope) {
        this.packageScope = packageScope;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Builds a BFile which represents physical ballerina source file
     */
    public static class BFileBuilder {

        private String packageName;
        private List<Import> importList = new ArrayList<>();
        private List<Service> serviceList = new ArrayList<>();
        private List<Connector> connectorList = new ArrayList<>();
        private Map<String, Function> functionList = new HashMap<>();
        private List<StructType> sTypeList = new ArrayList<>();
        private List<FunctionInvocationExpr> funcIExprList = new ArrayList<>();

        public BFileBuilder() {

        }

        public void setPkgName(String packageName) {
            this.packageName = packageName;
        }

        public void addFunction(BallerinaFunction function) {
            this.functionList.put(function.getName(), function);
        }

        public void addService(Service service) {
            this.serviceList.add(service);
        }

        public void addConnector(Connector connector) {
            this.connectorList.add(connector);
        }

        public void addImportPackage(Import importPkg) {
            this.importList.add(importPkg);
        }

        public void addStructType(StructType structType) {
            this.sTypeList.add(structType);
        }

        public void addFuncIExpr(FunctionInvocationExpr expr) {
            this.funcIExprList.add(expr);
        }

        public BallerinaFile build() {
            return new BallerinaFile(
                    packageName,
                    importList,
                    serviceList,
                    connectorList,
                    functionList,
                    sTypeList,
                    funcIExprList);

        }
    }

}
