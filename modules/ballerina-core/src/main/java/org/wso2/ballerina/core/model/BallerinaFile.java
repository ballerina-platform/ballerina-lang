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
import org.wso2.ballerina.core.model.expressions.TypeCastExpression;
import org.wso2.ballerina.core.model.types.BTypes;
import org.wso2.ballerina.core.model.types.TypeLattice;
import org.wso2.ballerina.core.model.types.TypeVertex;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code BallerinaFile} represent a content of a Ballerina source file.
 * <p>
 * A Ballerina file is structured as follows:
 * <p>
 * [package PackageName;]
 * [import PackageName[ as Identifier];]*
 * (ServiceDefinition | FunctionDefinition | ConnectorDefinition | TypeDefinition | TypeConvertorDefinition |
 * ConstantDefinition)+
 *
 * @since 0.8.0
 */
@SuppressWarnings("unused")
public class BallerinaFile implements Node {

    // Name of the main function
    public static final String MAIN_FUNCTION_NAME = "main";

    private String packageName = "main";
    private ImportPackage[] importPackages;

    private List<Service> services = new ArrayList<>();
    private List<BallerinaConnector> connectorList = new ArrayList<>();
    private Function[] functions;
    private TypeLattice typeLattice;
    private Function mainFunction;
    private List<FunctionInvocationExpr> funcIExprList = new ArrayList<>();
    private List<ActionInvocationExpr> actionIExprList = new ArrayList<>();
    private Const[] consts;
    private BallerinaStruct[] structs;

    private int sizeOfStaticMem;

    private SymScope packageScope;

    private BallerinaFile(
            String packageName,
            ImportPackage[] importPackages,
            List<Service> serviceList,
            List<BallerinaConnector> connectorList,
            Function[] functions,
            Function mainFunction,
            List<FunctionInvocationExpr> funcIExprList,
            List<ActionInvocationExpr> actionInvocationExpr,
            Const[] consts,
            BallerinaStruct[] structs,
            TypeLattice typeLattice) {

        this.packageName = packageName;
        this.importPackages = importPackages;
        this.services = serviceList;
        this.connectorList = connectorList;
        this.functions = functions;
        this.mainFunction = mainFunction;
        this.funcIExprList = funcIExprList;
        this.actionIExprList = actionInvocationExpr;
        this.consts = consts;
        this.structs = structs;
        this.typeLattice = typeLattice;

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
     * Set the package name which file belongs to.
     *
     * @param packageName name of the package
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Get {@code Import} statements the file.
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
     * Get list of Connectors.
     *
     * @return connectors list
     */
    public List<BallerinaConnector> getConnectorList() {
        return connectorList;
    }

    /**
     * Get {@code BallerinaConnector} defined the file.
     *
     * @return list of imports
     */
    public List<BallerinaConnector> getConnectors() {
        return connectorList;
    }

    /**
     * Get {@code Service} list defined in the file.
     *
     * @return list of Services
     */
    public List<Service> getServices() {
        return services;
    }

    /**
     * Set {@code Service} list.
     *
     * @param services list of Services
     */
    public void setServices(List<Service> services) {
        this.services = services;
    }

    /**
     * Add a {@code Service} to the File.
     *
     * @param service a Service
     */
    public void addService(Service service) {
        services.add(service);
    }

    public Function[] getFunctions() {
        return functions;
    }

    public TypeLattice getTypeLattice() {
        return typeLattice;
    }

    public Function getMainFunction() {
        return this.mainFunction;
    }
    
    public BallerinaStruct[] getStructs() {
        return this.structs;
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
     * Builds a BFile which represents physical ballerina source file.
     */
    public static class BFileBuilder {

        private String packageName;
        private List<ImportPackage> importPkgList = new ArrayList<>();
        private List<Service> serviceList = new ArrayList<>();
        private List<BallerinaConnector> connectorList = new ArrayList<>();
        private List<Function> functionList = new ArrayList<>();
        private Function mainFunction;
        private TypeLattice typeLattice = new TypeLattice();

        private List<FunctionInvocationExpr> funcIExprList = new ArrayList<>();
        private List<ActionInvocationExpr> actionIExprList = new ArrayList<>();
        private List<TypeCastExpression> typeCastExprList = new ArrayList<>();

        private List<Const> constList = new ArrayList<>();
        
        private List<BallerinaStruct> structList = new ArrayList<>();

        public BFileBuilder() {
        }

        public void setPkgName(String packageName) {
            this.packageName = packageName;
        }

        public void addFunction(BallerinaFunction function) {
            if (function.getName().equals(MAIN_FUNCTION_NAME)) {

                Parameter[] parameters = function.getParameters();
                if (parameters.length == 1 && parameters[0].getType().equals(BTypes.getArrayType(BTypes.
                        STRING_TYPE.toString()))) {
                    mainFunction = function;
                }
            }

            this.functionList.add(function);
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

        public void addTypeCastExpr(TypeCastExpression expr) {
            this.typeCastExprList.add(expr);
        }

        public void addConst(Const constant) {
            this.constList.add(constant);
        }

        public void addTypeConvertor(TypeVertex source, TypeVertex target,
                                     TypeConvertor typeConvertor, String packageName) {
            typeLattice.addVertex(source, true);
            typeLattice.addVertex(target, true);
            typeLattice.addEdge(source, target, typeConvertor, packageName);
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
                    functionList.toArray(new Function[funcIExprList.size()]),
                    mainFunction,
                    funcIExprList,
                    actionIExprList,
                    constList.toArray(new Const[constList.size()]),
                    structList.toArray(new BallerinaStruct[structList.size()]),
                    typeLattice
                    );
        }

        /**
         * Add a ballerina user defined Struct to the ballerina file
         */
        public void addStruct(BallerinaStruct struct) {
            this.structList.add(struct);
        }
    }
}
