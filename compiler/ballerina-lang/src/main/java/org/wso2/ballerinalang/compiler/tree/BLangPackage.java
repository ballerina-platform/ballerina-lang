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
package org.wso2.ballerinalang.compiler.tree;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.AnnotationNode;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.ballerinalang.model.tree.EndpointNode;
import org.ballerinalang.model.tree.EnumNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.ImportPackageNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.ObjectNode;
import org.ballerinalang.model.tree.PackageDeclarationNode;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.StructNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.TransformerNode;
import org.ballerinalang.model.tree.TypeDefinition;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.XMLNSDeclarationNode;
import org.ballerinalang.repository.PackageRepository;
import org.wso2.ballerinalang.compiler.packaging.RepoHierarchy;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * @since 0.94
 */
public class BLangPackage extends BLangNode implements PackageNode {
    public List<BLangCompilationUnit> compUnits;
    public BLangPackageDeclaration pkgDecl;
    public List<BLangImportPackage> imports;
    public List<BLangXMLNS> xmlnsList;
    public List<BLangEndpoint> globalEndpoints;
    public List<BLangVariable> globalVars;
    public List<BLangService> services;
    public List<BLangFunction> functions;
    public List<BLangStruct> structs;
    public List<BLangObject> objects;
    public List<BLangTypeDefinition> typeDefinitions;
    public List<BLangEnum> enums;
    public List<BLangAnnotation> annotations;
    public List<BLangRecord> records;
    public BLangFunction initFunction, startFunction, stopFunction;
    public Set<CompilerPhase> completedPhases;
    public List<BLangTransformer> transformers;
    public List<BSymbol> objAttachedFunctions;
    public List<TopLevelNode> topLevelNodes;

    public PackageID packageID;
    public BPackageSymbol symbol;
    public PackageRepository packageRepository;

    // TODO Revisit these instance variables
    public Path loadedFilePath;
    public boolean loadedFromProjectDir;
    public RepoHierarchy repos;

    public BLangPackage() {
        this.compUnits = new ArrayList<>();
        this.imports = new ArrayList<>();
        this.xmlnsList = new ArrayList<>();
        this.globalEndpoints = new ArrayList<>();
        this.globalVars = new ArrayList<>();
        this.services = new ArrayList<>();
        this.functions = new ArrayList<>();
        this.structs = new ArrayList<>();
        this.objects = new ArrayList<>();
        this.records = new ArrayList<>();
        this.typeDefinitions = new ArrayList<>();
        this.enums = new ArrayList<>();
        this.annotations = new ArrayList<>();
        this.transformers = new ArrayList<>();

        this.objAttachedFunctions = new ArrayList<>();
        this.topLevelNodes = new ArrayList<>();
        this.completedPhases = EnumSet.noneOf(CompilerPhase.class);
    }

    @Override
    public List<BLangCompilationUnit> getCompilationUnits() {
        return compUnits;
    }

    @Override
    public void addCompilationUnit(CompilationUnitNode compUnit) {
        this.compUnits.add((BLangCompilationUnit) compUnit);
    }

    @Override
    public List<BLangImportPackage> getImports() {
        return imports;
    }

    @Override
    public List<BLangXMLNS> getNamespaceDeclarations() {
        return xmlnsList;
    }

    @Override
    public List<? extends EndpointNode> getGlobalEndpoints() {
        return globalEndpoints;
    }

    @Override
    public List<BLangVariable> getGlobalVariables() {
        return globalVars;
    }

    @Override
    public List<BLangService> getServices() {
        return services;
    }

    @Override
    public List<BLangFunction> getFunctions() {
        return functions;
    }

    @Override
    public List<BLangStruct> getStructs() {
        return structs;
    }

    @Override
    public List<? extends ObjectNode> getObjects() {
        return objects;
    }

    @Override
    public List<? extends TypeDefinition> getTypeDefinitions() {
        return typeDefinitions;
    }

    @Override
    public List<? extends EnumNode> getEnums() {
        return enums;
    }

    @Override
    public List<BLangAnnotation> getAnnotations() {
        return annotations;
    }

    @Override
    public List<? extends TransformerNode> getTransformers() {
        return transformers;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void addImport(ImportPackageNode importPkg) {
        this.imports.add((BLangImportPackage) importPkg);
    }

    @Override
    public void addNamespaceDeclaration(XMLNSDeclarationNode xmlnsDecl) {
        this.xmlnsList.add((BLangXMLNS) xmlnsDecl);
        this.topLevelNodes.add(xmlnsDecl);
    }

    @Override
    public void addGlobalVariable(VariableNode globalVar) {
        this.globalVars.add((BLangVariable) globalVar);
        this.topLevelNodes.add(globalVar);
    }

    @Override
    public void addService(ServiceNode service) {
        this.services.add((BLangService) service);
        this.topLevelNodes.add(service);
    }

    @Override
    public void addFunction(FunctionNode function) {
        this.functions.add((BLangFunction) function);
        this.topLevelNodes.add(function);
    }

    @Override
    public void addStruct(StructNode struct) {
        this.structs.add((BLangStruct) struct);
        this.topLevelNodes.add(struct);
    }

    @Override
    public void addObject(ObjectNode object) {
        this.objects.add((BLangObject) object);
        this.topLevelNodes.add(object);
    }

    @Override
    public void addEnum(EnumNode enumNode) {
        this.enums.add((BLangEnum) enumNode);
        this.topLevelNodes.add(enumNode);
    }

    @Override
    public void addAnnotation(AnnotationNode annotation) {
        this.annotations.add((BLangAnnotation) annotation);
        this.topLevelNodes.add(annotation);
    }

    @Override
    public void addTransformer(TransformerNode transformer) {
        this.transformers.add((BLangTransformer) transformer);
        this.topLevelNodes.add(transformer);
    }

    @Override
    public void addTypeDefinition(TypeDefinition typeDefinition) {
        this.typeDefinitions.add((BLangTypeDefinition) typeDefinition);
        this.topLevelNodes.add(typeDefinition);
    }

    @Override
    public void setPackageDeclaration(PackageDeclarationNode pkgDecl) {
        this.pkgDecl = (BLangPackageDeclaration) pkgDecl;
    }

    @Override
    public PackageDeclarationNode getPackageDeclaration() {
        return pkgDecl;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.PACKAGE;
    }
}
