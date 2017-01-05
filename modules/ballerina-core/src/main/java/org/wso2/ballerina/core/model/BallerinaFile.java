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
import org.wso2.ballerina.core.model.expressions.ActionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;

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

    private String packageName = "main";
    private ImportPackage[] importPackages;

    private List<Service> services = new ArrayList<>();
    private List<BallerinaConnector> connectorList = new ArrayList<>();
    private Map<String, Function> functions = new HashMap<>();
    private List<FunctionInvocationExpr> funcIExprList = new ArrayList<>();
    private List<ActionInvocationExpr> actionIExprList = new ArrayList<>();
    private Const[] consts;
    //TODO: add TypeConverters
    //TODO: add constants

    private int sizeOfStaticMem;

    private SymScope packageScope;

    private BallerinaFile(
            String packageName,
            ImportPackage[] importPackages,
            List<Service> serviceList,
            List<BallerinaConnector> connectorList,
            Map<String, Function> functionMap,
            List<FunctionInvocationExpr> funcIExprList,
            List<ActionInvocationExpr> actionInvocationExpr,
            Const[] consts) {

        this.packageName = packageName;
        this.importPackages = importPackages;
        this.services = serviceList;
        this.connectorList = connectorList;
        this.functions = functionMap;
        this.funcIExprList = funcIExprList;
        this.actionIExprList = actionInvocationExpr;
        this.consts = consts;

        packageScope = new SymScope(SymScope.Name.PACKAGE);
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
    public ImportPackage[] getImportPackages() {
        return importPackages;
    }

    public Const[] getConstants() {
        return consts;
    }

    /**
     * Get list of Connectors
     *
     * @return connectors list
     */
    public List<BallerinaConnector> getConnectorList() {
        return connectorList;
    }

    /**
     * Get {@code BallerinaConnector} defined the file
     *
     * @return list of imports
     */
    public List<BallerinaConnector> getConnectors() {
        return connectorList;
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

    public void addActionIExpr(ActionInvocationExpr expr) {
        this.actionIExprList.add(expr);
    }

    public ActionInvocationExpr[] getActionIExprs() {
        return actionIExprList.toArray(new ActionInvocationExpr[actionIExprList.size()]);
    }

    public SymScope getPackageScope() {
        return packageScope;
    }

    public void setPackageScope(SymScope packageScope) {
        this.packageScope = packageScope;
    }

    public int getSizeOfStaticMem() {
        return sizeOfStaticMem;
    }

    public void setSizeOfStaticMem(int sizeOfStaticMem) {
        this.sizeOfStaticMem = sizeOfStaticMem;
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
        private List<ImportPackage> importPkgList = new ArrayList<>();
        private List<Service> serviceList = new ArrayList<>();
        private List<BallerinaConnector> connectorList = new ArrayList<>();
        private Map<String, Function> functionList = new HashMap<>();

        private List<FunctionInvocationExpr> funcIExprList = new ArrayList<>();
        private List<ActionInvocationExpr> actionIExprList = new ArrayList<>();

        private List<Const> constList = new ArrayList<>();

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

        public void addConnector(BallerinaConnector connector) {
            this.connectorList.add(connector);
        }

        public void addImportPackage(ImportPackage importPkg) {
            this.importPkgList.add(importPkg);
        }

        public void addFuncIExpr(FunctionInvocationExpr expr) {
            this.funcIExprList.add(expr);
        }

        public void addConst(Const constant) {
            this.constList.add(constant);
        }

        public BallerinaFile build() {
            
            if (packageName != null) {
                importPkgList.add(new ImportPackage(packageName)); // Import self
            }
            
            return new BallerinaFile(
                    packageName,
                    importPkgList.toArray(new ImportPackage[importPkgList.size()]),
                    serviceList,
                    connectorList,
                    functionList,
                    funcIExprList,
                    actionIExprList,
                    constList.toArray(new Const[constList.size()]));

        }
    }
}
