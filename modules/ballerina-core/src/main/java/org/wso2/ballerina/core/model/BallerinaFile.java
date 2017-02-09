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
import org.wso2.ballerina.core.model.types.TypeLattice;
import org.wso2.ballerina.core.model.types.TypeVertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static final String MAIN_FUNCTION_NAME = "main";

    private String pkgName = null;

    // We need to keep a map of import packages.
    // This is useful when analyzing import functions, actions and types.
    private Map<String, ImportPackage> importPkgMap = new HashMap<>();

    private ImportPackage[] importPkgs;

    private CompilationUnit[] compilationUnits;

    private Service[] services;
    private BallerinaConnectorDef[] connectors;
    private Function[] functions;
    private TypeLattice typeLattice;
    private Function mainFunction;
    private ConstDef[] consts;
    private StructDef[] structDefs;

    private int sizeOfStaticMem;

    private SymScope packageScope;

    private BallerinaFile(
            String pkgName,
            Map<String, ImportPackage> importPkgMap,
            ImportPackage[] importPkgs,
            CompilationUnit[] compilationUnits,
            Service[] services,
            BallerinaConnectorDef[] connectors,
            Function[] functions,
            Function mainFunction,
            ConstDef[] consts,
            StructDef[] structDefs,
            TypeLattice typeLattice) {

        this.pkgName = pkgName;
        this.importPkgMap = importPkgMap;
        this.importPkgs = importPkgs;
        this.compilationUnits = compilationUnits;
        this.services = services;
        this.connectors = connectors;
        this.functions = functions;
        this.mainFunction = mainFunction;
        this.consts = consts;
        this.structDefs = structDefs;
        this.typeLattice = typeLattice;

        packageScope = new SymScope(SymScope.Name.PACKAGE);
    }

    /**
     * Get the package name which file belongs to.
     *
     * @return package name
     */
    public String getPackagePath() {
        return pkgName;
    }

    /**
     * Get {@code Import} statements the file.
     *
     * @return list of imports
     */
    public Map<String, ImportPackage> getImportPackageMap() {
        return importPkgMap;
    }

    public ImportPackage[] getImportPackages() {
        return importPkgs;
    }

    public CompilationUnit[] getCompilationUnits() {
        return compilationUnits;
    }

    public ConstDef[] getConstants() {
        return consts;
    }

    /**
     * Get {@code BallerinaConnector} defined the file.
     *
     * @return list of imports
     */
    public BallerinaConnectorDef[] getConnectors() {
        return connectors;
    }

    /**
     * Get {@code Service} list defined in the file.
     *
     * @return list of Services
     */
    public Service[] getServices() {
        return services;
    }

    /**
     * Set {@code Service} list.
     *
     * @param services list of Services
     */
    public void setServices(Service[] services) {
        this.services = services;
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

    public StructDef[] getStructDefs() {
        return this.structDefs;
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

    @Override
    public NodeLocation getNodeLocation() {
        return null;
    }

    /**
     * Builds a BFile node which represents physical ballerina source file.
     *
     * @since 0.8.0
     */
    public static class BFileBuilder {
        private String pkgName;

        // We need to keep a map of import packages.
        // This is useful when analyzing import functions, actions and types.
        private Map<String, ImportPackage> importPkgMap = new HashMap<>();
        private List<ImportPackage> importPkgList = new ArrayList<>();

        private List<CompilationUnit> compilationUnitList = new ArrayList<>();
        private List<Service> serviceList = new ArrayList<>();
        private List<BallerinaConnectorDef> connectorList = new ArrayList<>();
        private List<Function> functionList = new ArrayList<>();
        private Function mainFunction;
        private TypeLattice typeLattice = new TypeLattice();

        private List<ConstDef> constList = new ArrayList<>();

        private List<StructDef> structDefList = new ArrayList<>();

        public BFileBuilder() {
        }

        public void setPackagePath(String pkgName) {
            this.pkgName = pkgName;
        }

        public void addFunction(BallerinaFunction function) {
            if (function.getName().equals(MAIN_FUNCTION_NAME)) {

                ParameterDef[] parameterDefs = function.getParameterDefs();
                if (parameterDefs.length == 1 && parameterDefs[0].getType() == BTypes.getArrayType(BTypes.
                        typeString.toString())) {
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

        public void addConnector(BallerinaConnectorDef connector) {
            this.compilationUnitList.add(connector);
            this.connectorList.add(connector);
        }

        public void addImportPackage(ImportPackage importPkg) {
            this.importPkgList.add(importPkg);
        }

        public void setImportPackageMap(Map<String, ImportPackage> importPkgMap) {
            this.importPkgMap = importPkgMap;
        }

        public void addConst(ConstDef constant) {
            this.compilationUnitList.add((constant));
            this.constList.add(constant);
        }

        public void addTypeConvertor(TypeVertex source, TypeVertex target,
                                     TypeConvertor typeConvertor, String packageName) {
            this.compilationUnitList.add((BTypeConvertor) typeConvertor);
            typeLattice.addVertex(source, true);
            typeLattice.addVertex(target, true);
            typeLattice.addEdge(source, target, typeConvertor, packageName);
        }

        /**
         * Add a ballerina user defined Struct to the ballerina file
         */
        public void addStruct(StructDef structDef) {
            this.compilationUnitList.add(structDef);
            this.structDefList.add(structDef);
        }

        public BallerinaFile build() {
            return new BallerinaFile(
                    pkgName,
                    importPkgMap,
                    importPkgList.toArray(new ImportPackage[importPkgList.size()]),
                    compilationUnitList.toArray(new CompilationUnit[compilationUnitList.size()]),
                    serviceList.toArray(new Service[serviceList.size()]),
                    connectorList.toArray(new BallerinaConnectorDef[connectorList.size()]),
                    functionList.toArray(new Function[functionList.size()]),
                    mainFunction,
                    constList.toArray(new ConstDef[constList.size()]),
                    structDefList.toArray(new StructDef[structDefList.size()]),
                    typeLattice
            );
        }
    }
}
