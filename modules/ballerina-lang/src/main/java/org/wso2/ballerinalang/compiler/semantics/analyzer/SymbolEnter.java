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
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackageDeclaration;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
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

    public Map<BTypeSymbol, SymbolEnv> symbolEnvs;

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

        this.symbolEnvs = new HashMap<>();
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

        // TODO Verify this ENV design
        SymbolEnv prevEnv = env;
        env = new SymbolEnv(pkgNode, new Scope(pSymbol));
        symbolEnvs.put(pSymbol, env);

        // visit the package node recursively and define package level symbols.
        pkgNode.accept(this);
        env = prevEnv;
        return pSymbol;
    }

    public void visit(BLangPackage pkgNode) {
        // Create PackageSymbol.
        // And maintain a list of created package symbols.
        // Load each import package
        defineStructs(pkgNode.structs, env);
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
            BStructSymbol symbol = new BStructSymbol(
                    Names.fromIdNode(struct.name), null, owner);
            pkgEnv.scope.define(symbol.name, symbol);
        });
    }
}
