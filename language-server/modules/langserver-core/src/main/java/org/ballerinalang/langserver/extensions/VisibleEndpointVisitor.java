/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.extensions;

import org.ballerinalang.langserver.common.LSNodeVisitor;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.common.modal.SymbolMetaInfo;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.TopLevelNode;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangObjectConstructorExpression;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Find the visible Symbols for Resources and Functions.
 *
 * @since 0.985.0
 */
public class VisibleEndpointVisitor extends LSNodeVisitor {

    private SymbolEnv symbolEnv;

    private SymbolResolver symbolResolver;

    private SymbolTable symTable;

    private Map<BLangNode, List<SymbolMetaInfo>> visibleEPsByNode;

    private Map<PackageID, BLangImportPackage> packageMap;

    public VisibleEndpointVisitor(CompilerContext compilerContext) {
        this.symTable = SymbolTable.getInstance(compilerContext);
        this.symbolResolver = SymbolResolver.getInstance(compilerContext);
        this.visibleEPsByNode = new HashMap<>();
        this.packageMap = new HashMap<>();
    }

    public Map<BLangNode, List<SymbolMetaInfo>> getVisibleEPsByNode() {
        return visibleEPsByNode;
    }

    @Override
    public void visit(BLangPackage pkgNode) {
        SymbolEnv pkgEnv = this.symTable.pkgEnvMap.get(pkgNode.symbol);
        this.symbolEnv = pkgEnv;

        List<TopLevelNode> topLevelNodes = pkgNode.topLevelNodes;
        pkgNode.getImports().forEach(importPackage -> {
            if (importPackage.symbol != null) {
                this.packageMap.put(importPackage.symbol.pkgID, importPackage);
            }
        });

        topLevelNodes.stream()
                .filter(CommonUtil.checkInvalidTypesDefs())
                .forEach(topLevelNode -> acceptNode((BLangNode) topLevelNode, pkgEnv));
    }

    @Override
    public void visit(BLangFunction funcNode) {
        if (funcNode.body == null) {
            return;
        }
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, this.symbolEnv);
        SymbolEnv funcBodyEnv = SymbolEnv.createFuncBodyEnv(funcNode.body, funcEnv);
        // resolve visible in-scope endpoints coming from current module or other modules
        List<SymbolMetaInfo> visibleEPSymbols = resolveVisibleEndpointSymbols(funcBodyEnv, funcNode);
        this.visibleEPsByNode.put(funcNode.body, visibleEPSymbols);
        this.acceptNode(funcNode.body, funcBodyEnv);
    }

    @Override
    public void visit(BLangService serviceNode) {
        SymbolEnv serviceEnv = SymbolEnv.createServiceEnv(serviceNode, serviceNode.symbol.scope, this.symbolEnv);
        serviceNode.serviceClass.getFunctions().stream()
                .filter(bLangFunction -> (bLangFunction.symbol.flags & Flags.RESOURCE) == Flags.RESOURCE)
                .forEach(bLangFunction -> this.acceptNode(bLangFunction, serviceEnv));
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        // resolve locally declared endpoints
        this.resolveEndpointsFromStatements(blockNode.stmts, blockNode);
    }

    @Override
    public void visit(BLangBlockFunctionBody blockFuncBody) {
        // resolve locally declared endpoints
        this.resolveEndpointsFromStatements(blockFuncBody.stmts, blockFuncBody);
    }

    @Override
    public void visit(BLangIf ifNode) {
        this.acceptNode(ifNode.body, this.symbolEnv);
        this.acceptNode(ifNode.elseStmt, this.symbolEnv);
    }

    @Override
    public void visit(BLangWhile whileNode) {
        this.acceptNode(whileNode.body, this.symbolEnv);
    }

    @Override
    public void visit(BLangForeach foreach) {
        this.acceptNode(foreach.body, this.symbolEnv);
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        this.acceptNode(transactionNode.transactionBody, this.symbolEnv);
    }

    @Override
    public void visit(BLangObjectConstructorExpression bLangObjectConstructorExpression) {
    }

    @Override
    public void visit(BLangClassDefinition classDefinition) {
    }

    private void acceptNode(BLangNode node, SymbolEnv env) {
        if (node == null) {
            return;
        }
        SymbolEnv prevEnv = this.symbolEnv;
        this.symbolEnv = env;
        node.accept(this);
        this.symbolEnv = prevEnv;
    }

    /**
     * Resolve all visible symbols.
     *
     * @param symbolEnv symbol environment
     * @return all visible symbols for current scope
     */
    private List<SymbolMetaInfo> resolveVisibleEndpointSymbols(SymbolEnv symbolEnv, BLangFunction ownerFunction) {
        List<BSymbol> parameters = ownerFunction.getParameters().stream()
                .map(bLangSimpleVariable -> bLangSimpleVariable.symbol)
                .collect(Collectors.toList());
        List<BSymbol> visibleSymbols = new ArrayList<>();
        symbolResolver.getAllVisibleInScopeSymbols(symbolEnv).forEach((key, value) ->
                visibleSymbols.addAll(
                        value.stream()
                                .map(scopeEntry -> scopeEntry.symbol)
                                .collect(Collectors.toList())));

        return visibleSymbols.stream()
                .filter(symbol -> symbol instanceof BVarSymbol && this.isClient(symbol)
                        && (parameters.contains(symbol) || symbol.owner instanceof BPackageSymbol))
                .map(symbol -> {
                    BLangImportPackage importPackage = this.packageMap.get(symbol.type.tsymbol.pkgID);
                    String typeName = symbol.type.tsymbol.getName().getValue();
                    String pkgName = symbol.pkgID.getName().getValue();
                    String orgName = symbol.pkgID.getOrgName().getValue();
                    String alias = importPackage == null ? "" : importPackage.getAlias().getValue();
                    boolean isCaller = parameters.contains(symbol);
                    return new SymbolMetaInfo.SymbolMetaInfoBuilder()
                            .setName(symbol.getName().getValue())
                            .setPkgName(pkgName)
                            .setPkgOrgName(orgName)
                            .setPkgAlias(alias)
                            .setKind("VisibleEndpoint")
                            .setCaller(isCaller)
                            .setTypeName(typeName)
                            .setLocal(false)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private void resolveEndpointsFromStatements(List<BLangStatement> statements, BLangNode owner) {
        statements.forEach(stmt -> {
            if (stmt instanceof BLangSimpleVariableDef) {
                BVarSymbol symbol = ((BLangSimpleVariableDef) stmt).var.symbol;
                if (this.isClient(symbol)) {
                    BLangImportPackage importPackage = this.packageMap.get(symbol.type.tsymbol.pkgID);
                    String typeName = symbol.type.tsymbol.getName().getValue();
                    String pkgName = symbol.pkgID.getName().getValue();
                    String orgName = symbol.pkgID.getOrgName().getValue();
                    String alias = importPackage == null ? "" : importPackage.getAlias().getValue();
                    SymbolMetaInfo visibleEndpoint = new SymbolMetaInfo.SymbolMetaInfoBuilder()
                            .setName(symbol.getName().getValue())
                            .setPkgName(pkgName)
                            .setPkgOrgName(orgName)
                            .setPkgAlias(alias)
                            .setKind("VisibleEndpoint")
                            .setCaller(false)
                            .setTypeName(typeName)
                            .setLocal(true)
                            .setPos(stmt.pos)
                            .build();
                    if (this.visibleEPsByNode.containsKey(owner)) {
                        this.visibleEPsByNode.get(owner).add(visibleEndpoint);
                    } else {
                        this.visibleEPsByNode.put(owner, Collections.singletonList(visibleEndpoint));
                    }
                }
            } else {
                stmt.accept(this);
            }
        });
    }

    private boolean isClient(BSymbol symbol) {
        return symbol.type != null && symbol.type.tsymbol != null
                && (symbol.type.tsymbol.flags & Flags.CLIENT) == Flags.CLIENT;
    }
}
