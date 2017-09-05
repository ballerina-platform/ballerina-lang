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
package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.wso2.ballerinalang.compiler.PackageLoader;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackageDeclaration;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since 0.94
 */
public class SymbolEnter extends BLangNodeVisitor {

    private static final CompilerContext.Key<SymbolEnter> SYMBOL_ENTER_KEY =
            new CompilerContext.Key<>();

    // Private Log

    private CompilerContext context;
    private PackageLoader pkgLoader;
    private SymbolTable symTable;
    private Names names;
    private SemanticAnalyzer semAnalyzer;
    private TypeChecker typeChecker;

    public Map<BTypeSymbol, SymbolEnv> symbolEnvs;

    private BLangPackage rootPkgNode;

    private SymbolEnv env;

    public static SymbolEnter getInstance(CompilerContext context) {
        SymbolEnter symbolEnter = context.get(SYMBOL_ENTER_KEY);
        if (symbolEnter == null) {
            symbolEnter = new SymbolEnter(context);
        }

        return symbolEnter;
    }

    public SymbolEnter(CompilerContext context) {
        this.context = context;
        this.context.put(SYMBOL_ENTER_KEY, this);

        this.pkgLoader = PackageLoader.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
        this.names = Names.getInstance(context);
        this.semAnalyzer = SemanticAnalyzer.getInstance(context);
        this.typeChecker = TypeChecker.getInstance(context);

        this.symbolEnvs = new HashMap<>();
        this.rootPkgNode = (BLangPackage) TreeBuilder.createPackageNode();
        this.rootPkgNode.symbol = symTable.rootPkg;
    }

    public BPackageSymbol definePackage(BLangPackage pkgNode) {
        populatePackageNode(pkgNode);

        BPackageSymbol pSymbol;
        if (pkgNode.pkgDecl == null) {
            pSymbol = new BPackageSymbol(PackageID.EMPTY, symTable.rootPkg);
        } else {
            pSymbol = new BPackageSymbol(pkgNode.pkgDecl.pkgId, symTable.rootPkg);
        }
        pkgNode.symbol = pSymbol;
        pSymbol.scope = new Scope(pSymbol);

        // TODO Verify this ENV design
        SymbolEnv prevEnv = env;
        env = createPkgEnv(pkgNode, pSymbol.scope);
//        symbolEnvs.put(pSymbol, env);

        // visit the package node recursively and define package level symbols.
        pkgNode.accept(this);
        env = prevEnv;
        return pSymbol;
    }

    public void defineNode(BLangNode node, SymbolEnv env) {
        SymbolEnv prevEnv = this.env;
        this.env = env;
        node.accept(this);
        this.env = prevEnv;
    }

    public void visit(BLangPackage pkgNode) {
        // Create PackageSymbol.
        // And maintain a list of created package symbols.
        // Load each import package
        defineStructs(pkgNode.structs, env);
        //defineConnector(pkgNode.connectors, env);
        defineStructFields(pkgNode.structs, env);
    }

    public void visit(BLangImportPackage importPkgNode) {
        throw new AssertionError();
    }

    public void visit(BLangXMLNS xmlnsNode) {
        throw new AssertionError();
    }

    public void visit(BLangFunction funcNode) {
        throw new AssertionError();
    }

    public void visit(BLangVariable varNode) {
        // assign the type to var type node
        BType varType = semAnalyzer.analyzeTypeNode(varNode.typeNode, env);

        // Create variable symbol
        Scope enclScope = env.scope;
        BVarSymbol varSymbol = new BVarSymbol(names.fromIdNode(varNode.name),
                varType, enclScope.owner);
        varNode.symbol = varSymbol;

        // Add it to the enclosing scope
        // Find duplicates
        if (typeChecker.checkForUniqueSymbol(varSymbol, enclScope)) {
            enclScope.define(varSymbol.name, varSymbol);
        }
    }

    private SymbolEnv createPkgEnv(BLangPackage node, Scope scope) {
        SymbolEnv env = new SymbolEnv(node, scope);
        env.enclPkg = rootPkgNode;
        return env;
    }

    private SymbolEnv createStructEnv(BLangStruct struct, SymbolEnv pkgEnv) {
        SymbolEnv structEnv = new SymbolEnv(struct, struct.symbol.scope);
        structEnv.enclPkg = (BLangPackage) pkgEnv.node;
        structEnv.enclEnv = pkgEnv;
        return structEnv;
    }

    /**
     * Visit each compilation unit (.bal file) and add each top-level node
     * in the compilation unit to the package node.
     *
     * @param pkgNode current package node
     */
    private void populatePackageNode(BLangPackage pkgNode) {
        List<BLangCompilationUnit> compUnits = pkgNode.getCompilationUnits();
        compUnits.forEach(compUnit -> populateCompilationUnit(pkgNode, compUnit));
    }

    /**
     * Visit each top-level node and add it to the package node.
     *
     * @param pkgNode  current package node
     * @param compUnit current compilation unit
     */
    private void populateCompilationUnit(BLangPackage pkgNode, BLangCompilationUnit compUnit) {
        // TODO Check whether package in 'compUnit' is equal to the package in 'pkgNode'

        // TODO If the pkgID is null, then assign an unnamed package/default package.
        compUnit.getTopLevelNodes().forEach(node -> addTopLevelNode(pkgNode, node));
    }

    private void addTopLevelNode(BLangPackage pkgNode, TopLevelNode node) {
        NodeKind kind = node.getKind();
        switch (kind) {
            case PACKAGE_DECLARATION:
                // TODO verify the rules..
                pkgNode.pkgDecl = (BLangPackageDeclaration) node;
                break;
            case IMPORT:
                // TODO Verify the rules..
                // TODO Check whether the same package alias (if any) has been used for the same import
                // TODO The version of an import package can be specified only once for a package
                if (!pkgNode.imports.contains(node)) {
                    pkgNode.imports.add((BLangImportPackage) node);
                }
                break;
            case FUNCTION:
                pkgNode.functions.add((BLangFunction) node);
                break;
            case STRUCT:
                pkgNode.structs.add((BLangStruct) node);
                break;
            case CONNECTOR:
                pkgNode.connectors.add((BLangConnector) node);
                break;
            case SERVICE:
                pkgNode.services.add((BLangService) node);
                break;
            case VARIABLE:
                // TODO There are two kinds of package level variables, constant and regular variables.
                break;
            case ANNOTATION:
                // TODO
                break;
            case XMLNS:
                // TODO
                break;
        }
    }

    private void defineStructs(List<BLangStruct> structNodes, SymbolEnv pkgEnv) {
        structNodes.forEach(struct -> {
            BSymbol owner = pkgEnv.scope.owner;
            BStructSymbol structSymbol = new BStructSymbol(
                    names.fromIdNode(struct.name), null, owner);
            structSymbol.scope = new Scope(structSymbol);
            struct.symbol = structSymbol;

            if (typeChecker.checkForUniqueSymbol(structSymbol, pkgEnv.scope)) {
                pkgEnv.scope.define(structSymbol.name, structSymbol);
            }
        });
    }

    private void defineStructFields(List<BLangStruct> structNodes, SymbolEnv pkgEnv) {
        structNodes.forEach(struct -> {
            SymbolEnv structEnv = createStructEnv(struct, pkgEnv);
            defineStructFields(struct, structEnv);
        });
    }

    private void defineStructFields(BLangStruct struct, SymbolEnv env) {
        // TODO Validate initial expression
        struct.fields.forEach(field -> {
            semAnalyzer.analyzeNode(field, env);
        });
    }
}
