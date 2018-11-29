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
package org.ballerinalang.langserver.extensions.ballerina.document;

import org.ballerinalang.langserver.common.LSNodeVisitor;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.common.modal.SymbolMetaInfo;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.TopLevelNode;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.util.Flags;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Find the visible Symbols for Resources and Functions.
 * 
 * @since 0.985.0
 */
class SymbolFindVisitor extends LSNodeVisitor {

    private SymbolEnv symbolEnv;

    private SymbolResolver symbolResolver;

    private SymbolTable symTable;

    private Map<BLangNode, List<SymbolMetaInfo>> visibleSymbolsMap;
    
    private Map<PackageID, BLangImportPackage> packageMap;

    SymbolFindVisitor(CompilerContext compilerContext) {
        this.symTable = SymbolTable.getInstance(compilerContext);
        this.symbolResolver = SymbolResolver.getInstance(compilerContext);
        this.visibleSymbolsMap = new HashMap<>();
        this.packageMap = new HashMap<>();
    }

    Map<BLangNode, List<SymbolMetaInfo>> getVisibleSymbolsMap() {
        return visibleSymbolsMap;
    }

    @Override
    public void visit(BLangPackage pkgNode) {
        SymbolEnv pkgEnv = this.symTable.pkgEnvMap.get(pkgNode.symbol);
        this.symbolEnv = pkgEnv;

        List<TopLevelNode> topLevelNodes = pkgNode.topLevelNodes;
        pkgNode.getImports().forEach(importPackage -> this.packageMap.put(importPackage.symbol.pkgID, importPackage));

        topLevelNodes.forEach(topLevelNode -> acceptNode((BLangNode) topLevelNode, pkgEnv));
    }

    @Override
    public void visit(BLangFunction funcNode) {
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, this.symbolEnv);
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(funcNode.getBody(), funcEnv);
        List<SymbolMetaInfo> visibleEPSymbols = resolveVisibleEndpointSymbols(blockEnv, funcNode);
        this.visibleSymbolsMap.put(funcNode, visibleEPSymbols);
    }

    @Override
    public void visit(BLangService serviceNode) {
        SymbolEnv serviceEnv = SymbolEnv.createServiceEnv(serviceNode, serviceNode.symbol.scope, this.symbolEnv);
        ((BLangObjectTypeNode) serviceNode.serviceTypeDefinition.typeNode).getFunctions().stream()
                .filter(bLangFunction -> (bLangFunction.symbol.flags & Flags.RESOURCE) == Flags.RESOURCE)
                .forEach(bLangFunction -> this.acceptNode(bLangFunction, serviceEnv));
    }

    private void acceptNode(BLangNode node, SymbolEnv env) {
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
        return symbolResolver.getAllVisibleInScopeSymbols(symbolEnv).entrySet().stream()
                .map(entry -> entry.getValue().symbol)
                .collect(Collectors.toList()).stream()
                .filter(symbol -> symbol instanceof BVarSymbol && CommonUtil.isClientObject(symbol))
                .map(symbol -> {
                    BLangImportPackage importPackage = this.packageMap.get(symbol.pkgID);
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
                            .build();
                })
                .collect(Collectors.toList());
    }
}
