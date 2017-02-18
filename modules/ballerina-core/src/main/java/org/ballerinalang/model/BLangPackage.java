/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.model;

import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.model.types.TypeLattice;
import org.ballerinalang.natives.NativePackageProxy;
import org.ballerinalang.util.repository.PackageRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code BLangPackage} represents a Ballerina package.
 *
 * @since 0.8.0
 */
public class BLangPackage implements SymbolScope, BLangSymbol, Node {
    protected String pkgPath;
    protected SymbolName symbolName;

    protected CompilationUnit[] compilationUnits;
    protected BallerinaFile[] ballerinaFiles;
    protected ImportPackage[] importPackages;
    protected Service[] services;
    protected BallerinaConnectorDef[] connectors;
    protected Function[] functions;
    protected TypeLattice typeLattice;
    protected ConstDef[] consts;
    protected StructDef[] structDefs;
    protected Function mainFunction;
    protected TypeMapper[] typeMappers;

    protected List<BLangPackage> dependentPkgs = new ArrayList<>();

    // Scope related variables
    private SymbolScope enclosingScope;
    private Map<SymbolName, BLangSymbol> symbolMap = new HashMap<>();

    private boolean symbolsDefined = false;
    private PackageRepository pkgRepo;
    private boolean isNative = false;

    public BLangPackage(BLangProgram programScope) {
        this.enclosingScope = programScope;
        symbolMap = new HashMap<>();
    }

    public BLangPackage(GlobalScope golbalScope) {
        this.enclosingScope = golbalScope;
        symbolMap = new HashMap<>();
    }

    public BLangPackage(String packagePath, PackageRepository packageRepo, BLangProgram programScope) {
        this.pkgPath = packagePath;
        this.pkgRepo = packageRepo;
        this.enclosingScope = programScope;
        this.symbolName = new SymbolName(packagePath);
        symbolMap = new HashMap<>();
    }

    public void setPackagePath(String pkgPath) {
        this.pkgPath = pkgPath;
        this.symbolName = new SymbolName(pkgPath);
    }

    public BallerinaFile[] getBallerinaFiles() {
        return ballerinaFiles;
    }

    public void setBallerinaFiles(BallerinaFile[] ballerinaFiles) {
        this.ballerinaFiles = ballerinaFiles;
    }

    public ImportPackage[] getImportPackages() {
        return importPackages;
    }

    public void setImportPackages(ImportPackage[] importPackages) {
        this.importPackages = importPackages;
    }

    public void addDependentPackage(BLangPackage bLangPackage) {
        this.dependentPkgs.add(bLangPackage);
    }

    public BLangPackage[] getDependentPackages() {
        return dependentPkgs.toArray(new BLangPackage[dependentPkgs.size()]);
    }

    public CompilationUnit[] getCompilationUntis() {
        return compilationUnits;
    }

    public CompilationUnit[] getCompilationUnits() {
        return compilationUnits;
    }

    public Service[] getServices() {
        return services;
    }

    public BallerinaConnectorDef[] getConnectors() {
        return connectors;
    }

    public Function[] getFunctions() {
        return functions;
    }

    public TypeLattice getTypeLattice() {
        return typeLattice;
    }

    public ConstDef[] getConsts() {
        return consts;
    }

    public StructDef[] getStructDefs() {
        return structDefs;
    }

    public boolean isSymbolsDefined() {
        return symbolsDefined;
    }

    public void setSymbolsDefined(boolean symbolsDefined) {
        this.symbolsDefined = symbolsDefined;
    }

    public PackageRepository getPackageRepository() {
        return pkgRepo;
    }

    public void setPackageRepository(PackageRepository pkgRepo) {
        this.pkgRepo = pkgRepo;
    }

    public void setNative(boolean isNative) {
        this.isNative = isNative;
    }

    public TypeMapper[] getTypeMappers() {
        return typeMappers;
    }

    // Methods in the SymbolScope interface

    @Override
    public ScopeName getScopeName() {
        return ScopeName.PACKAGE;
    }

    @Override
    public SymbolScope getEnclosingScope() {
        return enclosingScope;
    }

    @Override
    public void define(SymbolName name, BLangSymbol symbol) {
        symbolMap.put(name, symbol);
    }

    @Override
    public BLangSymbol resolve(SymbolName name) {
        if (name.getPkgPath() == null) {
            return resolve(symbolMap, name);
        }
        
        // resolve the package symbol first
        SymbolName pkgSymbolName = new SymbolName(name.getPkgPath());
        BLangSymbol pkgSymbol = getEnclosingScope().resolve(pkgSymbolName);
        if (pkgSymbol == null) {
            return null;
        }
        
        if (pkgSymbol instanceof NativePackageProxy) {
            pkgSymbol = ((NativePackageProxy) pkgSymbol).load();
        }

        return ((BLangPackage) pkgSymbol).resolveMembers(new SymbolName(name.getName()));
    }

    @Override
    public Map<SymbolName, BLangSymbol> getSymbolMap() {
        return Collections.unmodifiableMap(this.symbolMap);
    }

    public BLangSymbol resolveMembers(SymbolName name) {
        return symbolMap.get(name);
    }


    // Methods in the BLangSymbol interface

    @Override
    public String getName() {
        return pkgPath;
    }

    @Override
    public String getPackagePath() {
        return pkgPath;
    }

    @Override
    public boolean isPublic() {
        return false;
    }

    @Override
    public boolean isNative() {
        return isNative;
    }

    @Override
    public SymbolName getSymbolName() {
        return symbolName;
    }

    @Override
    public SymbolScope getSymbolScope() {
        return null;
    }


    // Methods in the Node interface

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeLocation getNodeLocation() {
        return null;
    }

    /**
     * @since 0.8.0
     */
    public static class PackageBuilder {
        private BLangPackage bLangPackage;

        private Map<String, ImportPackage> importPkgMap = new HashMap<>();
        private List<CompilationUnit> compilationUnitList = new ArrayList<>();
        private List<Service> serviceList = new ArrayList<>();
        private List<BallerinaConnectorDef> connectorList = new ArrayList<>();
        private List<Function> functionList = new ArrayList<>();
        private List<ConstDef> constList = new ArrayList<>();
        private List<StructDef> structDefList = new ArrayList<>();
        private TypeLattice typeLattice = new TypeLattice();
        private List<TypeMapper> typeMapperList = new ArrayList<>();

        private List<BallerinaFile> ballerinaFileList = new ArrayList<>();

        public PackageBuilder(BLangPackage bLangPackage) {
            this.bLangPackage = bLangPackage;
        }

        public PackageBuilder(String packagePath, PackageRepository packageRepo, BLangProgram programScope) {
            this.bLangPackage = new BLangPackage(packagePath, packageRepo, programScope);
        }

        public SymbolScope getCurrentScope() {
            return bLangPackage;
        }

        public void addFunction(BallerinaFunction function) {
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
            this.importPkgMap.put(importPkg.getPath(), importPkg);
        }

        public void addConst(ConstDef constant) {
            this.compilationUnitList.add((constant));
            this.constList.add(constant);
        }

        public void addTypeMapper(TypeMapper typeMapper) {
            this.compilationUnitList.add((BTypeMapper) typeMapper);
            typeMapperList.add(typeMapper);
        }

        public void addStruct(StructDef structDef) {
            this.compilationUnitList.add(structDef);
            this.structDefList.add(structDef);
        }

        public void addBallerinaFile(BallerinaFile bFile) {
            this.ballerinaFileList.add(bFile);
        }

        public void setBallerinaFileList(List<BallerinaFile> ballerinaFileList) {
            this.ballerinaFileList = ballerinaFileList;
        }

        public BLangPackage build() {
            bLangPackage.compilationUnits = this.compilationUnitList.toArray(new CompilationUnit[0]);
            bLangPackage.functions = this.functionList.toArray(new Function[0]);
            bLangPackage.services = this.serviceList.toArray(new Service[0]);
            bLangPackage.connectors = this.connectorList.toArray(new BallerinaConnectorDef[0]);
            bLangPackage.structDefs = this.structDefList.toArray(new StructDef[0]);
            bLangPackage.consts = this.constList.toArray(new ConstDef[0]);
            bLangPackage.importPackages = this.importPkgMap.values().toArray(new ImportPackage[0]);
            bLangPackage.typeLattice = this.typeLattice;
            bLangPackage.ballerinaFiles = ballerinaFileList.toArray(new BallerinaFile[0]);
            bLangPackage.typeMappers = this.typeMapperList.toArray(new TypeMapper[0]);
            return bLangPackage;
        }
    }
}
