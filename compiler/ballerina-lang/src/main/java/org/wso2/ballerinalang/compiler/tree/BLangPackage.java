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

import io.ballerina.projects.internal.ModuleContextDataHolder;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.AnnotationNode;
import org.ballerinalang.model.tree.ClassDefinition;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.ImportPackageNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.TypeDefinition;
import org.ballerinalang.model.tree.XMLNSDeclarationNode;
import org.wso2.ballerinalang.compiler.diagnostic.DiagnosticComparator;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

/**
 * @since 0.94
 */
public class BLangPackage extends BLangNode implements PackageNode {

    // BLangNodes
    public List<BLangCompilationUnit> compUnits;
    public List<BLangImportPackage> imports;
    public List<BLangXMLNS> xmlnsList;
    public List<BLangConstant> constants;
    public List<BLangVariable> globalVars;
    public List<BLangService> services;
    public List<BLangFunction> functions;
    public List<BLangTypeDefinition> typeDefinitions;
    public List<BLangAnnotation> annotations;
    public BLangFunction initFunction, startFunction, stopFunction;
    public List<TopLevelNode> topLevelNodes;
    public List<BLangTestablePackage> testablePkgs;
    public List<BLangClassDefinition> classDefinitions;

    // Parser Flags and Data
    public List<BSymbol> objAttachedFunctions;
    public Set<Flag> flagSet;

    // Semantic Data
    public Set<CompilerPhase> completedPhases;
    // Queue to maintain lambda functions so that we can visit all lambdas at the end of the semantic phase
    public Queue<BLangLambdaFunction> lambdaFunctions = new ArrayDeque<>();
    // Hold global variable dependencies identified in DataflowAnalyzer.
    public Map<BSymbol, Set<BVarSymbol>> globalVariableDependencies;

    public PackageID packageID;
    public BPackageSymbol symbol;
    public byte[] jarBinaryContent;

    private int errorCount;
    private int warnCount;
    private TreeSet<Diagnostic> diagnostics;

    public ModuleContextDataHolder moduleContextDataHolder;

    public BLangPackage() {
        this.compUnits = new ArrayList<>();
        this.imports = new ArrayList<>();
        this.xmlnsList = new ArrayList<>();
        this.constants = new ArrayList<>();
        this.globalVars = new ArrayList<>();
        this.services = new ArrayList<>();
        this.functions = new ArrayList<>();
        this.typeDefinitions = new ArrayList<>();
        this.annotations = new ArrayList<>();
        this.classDefinitions = new ArrayList<>();

        this.objAttachedFunctions = new ArrayList<>();
        this.topLevelNodes = new ArrayList<>();
        this.completedPhases = EnumSet.noneOf(CompilerPhase.class);
        this.testablePkgs = new ArrayList<>();
        this.flagSet = EnumSet.noneOf(Flag.class);
        this.diagnostics = new TreeSet<>(new DiagnosticComparator());
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
    public List<BLangConstant> getConstants() {
        return constants;
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
    public List<BLangTypeDefinition> getTypeDefinitions() {
        return typeDefinitions;
    }

    @Override
    public List<BLangAnnotation> getAnnotations() {
        return annotations;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> void accept(BLangNodeAnalyzer<T> analyzer, T props) {
        analyzer.visit(this, props);
    }

    @Override
    public <T, R> R apply(BLangNodeTransformer<T, R> modifier, T props) {
        return modifier.transform(this, props);
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
    public void addGlobalVariable(SimpleVariableNode globalVar) {
        this.globalVars.add((BLangSimpleVariable) globalVar);
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
    public void addAnnotation(AnnotationNode annotation) {
        this.annotations.add((BLangAnnotation) annotation);
        this.topLevelNodes.add(annotation);
    }

    @Override
    public List<? extends ClassDefinition> getClassDefinitions() {
        return this.classDefinitions;
    }

    @Override
    public void addTypeDefinition(TypeDefinition typeDefinition) {
        this.typeDefinitions.add((BLangTypeDefinition) typeDefinition);
        this.topLevelNodes.add(typeDefinition);
    }

    /**
     * Add testable package to package list.
     *
     * @param testablePkg testable package node
     */
    public void addTestablePkg(BLangTestablePackage testablePkg) {
        this.testablePkgs.add(testablePkg);
    }

    /**
     * Get the testable package list.
     *
     * @return testable package list
     */
    public List<BLangTestablePackage> getTestablePkgs() {
        return testablePkgs;
    }

    /**
     * Get testable package from the list.
     *
     * @return testable package
     */
    public BLangTestablePackage getTestablePkg() {
        return testablePkgs.stream().findAny().orElse(null);
    }

    /**
     * Checks if the package contains a testable package.
     *
     * @return true it testable package exists else false
     */
    public boolean containsTestablePkg() {
        return testablePkgs.stream().findAny().isPresent();
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.PACKAGE;
    }

    /**
     * Get flags.
     *
     * @return flags of the package
     */
    public Set<Flag> getFlags() {
        return flagSet;
    }

    public boolean hasTestablePackage() {
        return this.testablePkgs.size() > 0;
    }

    public void addClassDefinition(BLangClassDefinition classDefNode) {
        this.topLevelNodes.add(classDefNode);
        this.classDefinitions.add(classDefNode);
    }

    /**
     * Add a diagnostic to this package.
     *
     * @param diagnostic Diagnostic to be added
     */
    public void addDiagnostic(Diagnostic diagnostic) {
        boolean isAdded = this.diagnostics.add(diagnostic);
        if (!isAdded) {
            return;
        }
        if (diagnostic.diagnosticInfo().severity() == DiagnosticSeverity.ERROR) {
            this.errorCount++;
        } else if (diagnostic.diagnosticInfo().severity() == DiagnosticSeverity.WARNING) {
            this.warnCount++;
        }
    }

    /**
     * Get all the diagnostics of this package.
     *
     * @return List of diagnostics in this package
     */
    public List<Diagnostic> getDiagnostics() {
        return new ArrayList<>(this.diagnostics);
    }

    public int getErrorCount() {
        return errorCount;
    }

    public int getWarnCount() {
        return warnCount;
    }

    /**
     * Check whether this package has any errors.
     *
     * @return <code>true</code> if this package has any errors. <code>false</code> otherwise
     */
    public boolean hasErrors() {
        return this.errorCount > 0;
    }
}
