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
package org.ballerinalang.composer.service.workspace.langserver;

import org.ballerinalang.composer.service.workspace.langserver.dto.Position;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilterDataModel;
import org.ballerinalang.model.tree.Node;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @since 0.94
 */
public class TreeVisitor extends BLangNodeVisitor {

    private SymbolEnv symbolEnv;
    private SymbolTable symTable;
    private SymbolResolver symbolResolver;
    private boolean terminateVisitor = false;
    private List<SymbolInfo> symbols;
    private Position position;
    private SuggestionsFilterDataModel filterDataModel;

    public TreeVisitor(CompilerContext compilerContext, List<SymbolInfo> symbolInfoList, Position pos, SuggestionsFilterDataModel filterDataModel) {
        this.symTable = SymbolTable.getInstance(compilerContext);
        this.symbolResolver = SymbolResolver.getInstance(compilerContext);
        this.symbols = symbolInfoList;
        this.position = pos;
        this.filterDataModel = filterDataModel;
    }

    // Visitor methods

    public void visit(BLangPackage pkgNode) {
        // First visit all the imported packages

        SymbolEnv pkgEnv = SymbolEnv.createPkgEnv(pkgNode, pkgNode.symbol.scope, symTable.rootPkgNode);

        // Then visit each top-level element sorted using the compilation unit
        pkgNode.topLevelNodes.forEach(topLevelNode -> this.acceptNode((BLangNode) topLevelNode, pkgEnv));
    }

    public void visit(BLangImportPackage importPkgNode) {
        throw new AssertionError();
    }

    public void visit(BLangXMLNS xmlnsNode) {
        throw new AssertionError();
    }

    public void visit(BLangFunction funcNode) {
        BSymbol funcSymbol = funcNode.symbol;
        SymbolEnv funcEnv = SymbolEnv.createPkgLevelSymbolEnv(funcNode, symbolEnv, funcSymbol.scope);
        this.acceptNode(funcNode.body, funcEnv);
    }

    public void visit(BLangStruct structNode) {
    }

    public void visit(BLangVariable varNode) {
//        if ((ownerSymTag & SymTag.INVOKABLE) == SymTag.INVOKABLE) {
//            // This is a variable declared in a function, an action or a resource
//            // If the variable is parameter then the variable symbol is already defined
//            if (varNode.symbol == null) {
//                symbolEnter.defineNode(varNode, env);
//            }
//        }
//
//        // Analyze the init expression
//        if (varNode.expr != null) {
//            // Here we create a new symbol environment to catch self references by keep the current
//            // variable symbol in the symbol environment
//            // e.g. int a = x + a;
//            SymbolEnv varInitEnv = SymbolEnv.createVarInitEnv(varNode, env, varNode.symbol);
//            typeChecker.checkExpr(varNode.expr, varInitEnv, Lists.of(varNode.symbol.type));
//        }
//        varNode.type = varNode.symbol.type;
    }


    // Statements

    public void visit(BLangBlockStmt blockNode) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockNode, symbolEnv);
        blockNode.statements.forEach(stmt -> this.acceptNode(stmt, blockEnv));
    }

    public void visit(BLangVariableDef varDefNode) {
        int line = position.getLine();
        int col = position.getCharacter();
        int nodeELine = varDefNode.getPosition().eLine;
        int nodeECol = varDefNode.getPosition().eCol;
        if (line < nodeELine || (nodeELine == line && col < nodeECol)) {
            Map<Name, Scope.ScopeEntry> visibleSymbolEntries = this.resolveAllVisibleSymbols(symbolEnv);
            System.out.println(Collections.singletonList(visibleSymbolEntries));
            this.populateSymbols(visibleSymbolEntries);
            this.terminateVisitor = true;
        } else {
            this.acceptNode(varDefNode.var, symbolEnv);
        }
    }

    public void visit(BLangIf ifNode) {
//        typeChecker.checkExpr(ifNode.expr, env, Lists.of(symTable.booleanType));
        this.acceptNode(ifNode.body, symbolEnv);

        if (ifNode.elseStmt != null) {
            this.acceptNode(ifNode.elseStmt, symbolEnv);
        }
    }

    public void visit(BLangWhile whileNode) {
//        typeChecker.checkExpr(whileNode.expr, env, Lists.of(symTable.booleanType));
        this.acceptNode(whileNode.body, symbolEnv);
    }

    void visitStmtNodes(List<BLangStatement> stmtNodes) {
//        stmtNodes.forEach(this::analyzeStmtNode);
    }

    void acceptNode(BLangNode node, SymbolEnv env) {

        if (this.terminateVisitor) {
            return;
        }

        SymbolEnv prevEnv = this.symbolEnv;
        this.symbolEnv = env;
        node.accept(this);
        this.symbolEnv = prevEnv;
    }

    // Need symbol to node mapping from language core
    public Map<Name, Scope.ScopeEntry> filterEntriesBefore(int line, int col, Map<Name, Scope.ScopeEntry> visibleSymbolEntries) {
        Map<Name, Scope.ScopeEntry> filteredEntries = visibleSymbolEntries.entrySet().stream().filter(entry -> {
            int entryELine = ((Node)entry.getKey()).getPosition().getEndLine();
            int entryECol = ((Node)entry.getKey()).getPosition().endColumn();
            return line < entryELine || (entryELine == line && col < entryECol);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return filteredEntries;
    }

    private Map<Name, Scope.ScopeEntry> resolveAllVisibleSymbols(SymbolEnv symbolEnv) {
        Map<Name, Scope.ScopeEntry> visibleSymbolEntries = symbolResolver.lookupAllVisibleSymbols(symbolEnv);
        System.out.println(visibleSymbolEntries);
        return  visibleSymbolEntries;
    }

    private void populateSymbols(Map<Name, Scope.ScopeEntry> symbolEntries) {
        this.filterDataModel.setSymbolEnvNode(this.symbolEnv.node);
        symbolEntries.forEach((k, v) -> {
            SymbolInfo symbolInfo = new SymbolInfo(k.getValue(), v);
            symbols.add(symbolInfo);
        });
    }
}