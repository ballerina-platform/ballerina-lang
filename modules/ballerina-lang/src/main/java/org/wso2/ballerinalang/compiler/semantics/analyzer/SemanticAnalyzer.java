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

import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.List;

/**
 * @since 0.94
 */
public class SemanticAnalyzer extends BLangNodeVisitor {

    private static final CompilerContext.Key<SemanticAnalyzer> SYMBOL_ANALYZER_KEY =
            new CompilerContext.Key<>();

    private SymbolTable symTable;
    private Names names;
    private TypeChecker typeChecker;

    private SymbolEnv env;
    private BType expType;
    private String errMsgKey;
    private BType resType;

    public static SemanticAnalyzer getInstance(CompilerContext context) {
        SemanticAnalyzer semAnalyzer = context.get(SYMBOL_ANALYZER_KEY);
        if (semAnalyzer == null) {
            semAnalyzer = new SemanticAnalyzer(context);
        }

        return semAnalyzer;
    }

    public SemanticAnalyzer(CompilerContext context) {
        context.put(SYMBOL_ANALYZER_KEY, this);

        this.symTable = SymbolTable.getInstance(context);
        this.names = Names.getInstance(context);
        this.typeChecker = TypeChecker.getInstance(context);
    }

    public BLangPackage analyze(BLangPackage pkgNode) {
        pkgNode.accept(this);
        return pkgNode;
    }

    // Visitor methods

    public void visit(BLangPackage pkgNode) {
        // First visit all the imported packages

        // Then visit each top-level element sorted using the compilation unit
        pkgNode.topLevelNodes.forEach(topLevelNode -> ((BLangNode) topLevelNode).accept(this));
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


    BType analyzeStmtNode(BLangStatement stmtNode) {
        return null;
    }

    void visitStmtNodes(List<BLangStatement> stmtNodes) {
        stmtNodes.forEach(this::analyzeStmtNode);
    }

    BType analyzeNode(BLangNode node, SymbolEnv env) {
        return analyzeNode(node, env, symTable.noType, "");
    }

    BType analyzeNode(BLangNode node, SymbolEnv env, BType expType, String errMsgKey) {
        SymbolEnv prevEnv = this.env;
        BType preExpType = this.expType;
        String preErrMsgKey = this.errMsgKey;

        // TODO Check the possibility of using a try/finally here
        this.env = env;
        this.expType = expType;
        this.errMsgKey = errMsgKey;
        node.accept(this);
        this.env = prevEnv;
        this.expType = preExpType;
        this.errMsgKey = preErrMsgKey;

        return resType;
    }


    //

    public void visit(BLangVariable varNode) {
        // Define the variable symbol
        // Analyze the init expression
        // TODO

//        throw new AssertionError();
    }
}
