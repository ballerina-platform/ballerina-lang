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

import org.wso2.ballerinalang.compiler.semantics.model.Scope.ScopeEntry;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.List;

/**
 * @since 0.94
 */
public class SemanticAnalyzer extends BLangNodeVisitor {

    private static final CompilerContext.Key<SemanticAnalyzer> SYMBOL_ANALYZER_KEY =
            new CompilerContext.Key<>();

    private SymbolEnter symEnter;
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

        this.symEnter = SymbolEnter.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
        this.names = Names.getInstance(context);
        this.typeChecker = TypeChecker.getInstance(context);
    }

    BType analyzeTypeNode(BLangType typeNode, SymbolEnv env) {
        return analyzeTypeNode(typeNode, env, symTable.noType);
    }

    BType analyzeTypeNode(BLangType typeNode, SymbolEnv env, BType expType) {
        return analyzeNode(typeNode, env, expType, "");
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
        symEnter.defineNode(varNode, env);

        // Analyze the init expression
        // TODO

//        throw new AssertionError();
    }

    // Type nodes
    public void visit(BLangValueType valueType) {
        ScopeEntry entry = symTable.rootScope.lookup(names.fromTypeKind(valueType.typeKind));
//        throw new AssertionError();
    }

    public void visit(BLangArrayType arrayType) {
        throw new AssertionError();
    }

    public void visit(BLangConstrainedType constrainedType) {
        throw new AssertionError();
    }

    public void visit(BLangUserDefinedType userDefinedType) {
        throw new AssertionError();
    }
}
