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
import org.wso2.ballerina.core.model.types.BTypes;

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
public class BallerinaFile implements Node {

    // Name of the main function
    public static final String MAIN_FUNCTION_NAME = "main";

    private String packageName = "main";
    private ImportPackage[] importPackages;

    private CompilationUnit[] compilationUnits;

    private List<Service> services = new ArrayList<>();
    private List<BallerinaConnector> connectorList = new ArrayList<>();
    private Function[] functions;
    private TypeConvertor[] typeConvertors;
    private Function mainFunction;
    private Const[] consts;
    private Struct[] structs;

    private int sizeOfStaticMem;

    private SymScope packageScope;

    private BallerinaFile(
            String packageName,
            ImportPackage[] importPackages,
            CompilationUnit[] compilationUnits,
            List<Service> serviceList,
            List<BallerinaConnector> connectorList,
            Function[] functions,
            Function mainFunction,
            Const[] consts,
            Struct[] structs,
            TypeConvertor[] typeConvertors) {

        this.packageName = packageName;
        this.importPackages = importPackages;
        this.compilationUnits = compilationUnits;
        this.services = serviceList;
        this.connectorList = connectorList;
        this.functions = functions;
        this.mainFunction = mainFunction;
        this.consts = consts;
        this.structs = structs;
        this.typeConvertors = typeConvertors;

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

    public CompilationUnit[] getCompilationUnits() {
        return compilationUnits;
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

    public Function[] getFunctions() {
        return functions;
    }

    public TypeConvertor[] getTypeConvertors() {
        return typeConvertors;
    }

    public Function getMainFunction() {
        return this.mainFunction;
    }

    public Struct[] getStructs() {
        return this.structs;
    }

    public SymScope getPackageScope() {
        return packageScope;
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

        private List<CompilationUnit> compilationUnitList = new ArrayList<>();
        private List<Service> serviceList = new ArrayList<>();
        private List<BallerinaConnector> connectorList = new ArrayList<>();
        private List<Function> functionList = new ArrayList<>();
        private List<TypeConvertor> typeConvertorList = new ArrayList<>();
        private Function mainFunction;

        private List<Const> constList = new ArrayList<>();

        private List<Struct> structList = new ArrayList<>();

        public BFileBuilder() {
        }

        public void setPkgName(String packageName) {
            this.packageName = packageName;
        }

        public void addFunction(BallerinaFunction function) {
            if (function.getName().equals(MAIN_FUNCTION_NAME)) {

                Parameter[] parameters = function.getParameters();
                if (parameters.length == 1 && parameters[0].getType() == BTypes.getArrayType(BTypes.
                        STRING_TYPE.toString())) {
                    mainFunction = function;
                }
            }

            this.compilationUnitList.add(function);
            this.functionList.add(function);
        }

        public void addService(Service service) {
            this.compilationUnitList.add(service);
            this.serviceList.add(service);
        }

        public void addConnector(BallerinaConnector connector) {
            this.compilationUnitList.add(connector);
            this.connectorList.add(connector);
        }

        public void addImportPackage(ImportPackage importPkg) {
            this.importPkgList.add(importPkg);
        }

        public void addConst(Const constant) {
            this.compilationUnitList.add((constant));
            this.constList.add(constant);
        }

        public void addTypeConverter(BTypeConvertor typeConvertor) {
            this.compilationUnitList.add(typeConvertor);
            this.typeConvertorList.add(typeConvertor);
        }

        /**
         * Add a ballerina user defined Struct to the ballerina file
         */
        public void addStruct(Struct struct) {
            this.compilationUnitList.add(struct);
            this.structList.add(struct);
        }

        public BallerinaFile build() {
            if (packageName != null) {
                importPkgList.add(new ImportPackage(packageName)); // Import self
            }

            return new BallerinaFile(
                    packageName,
                    importPkgList.toArray(new ImportPackage[importPkgList.size()]),
                    compilationUnitList.toArray(new CompilationUnit[compilationUnitList.size()]),
                    serviceList,
                    connectorList,
                    functionList.toArray(new Function[functionList.size()]),
                    mainFunction,
                    constList.toArray(new Const[constList.size()]),
                    structList.toArray(new Struct[structList.size()]),
                    typeConvertorList.toArray(new TypeConvertor[typeConvertorList.size()])
            );
        }
    }
}
