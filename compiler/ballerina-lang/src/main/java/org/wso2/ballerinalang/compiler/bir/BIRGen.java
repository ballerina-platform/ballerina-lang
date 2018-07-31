/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.bir;

import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.bir.model.Visibility;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;

/**
 * Lower the AST to BIR.
 *
 * @since 0.980.0
 */
public class BIRGen extends BLangNodeVisitor {

    private static final CompilerContext.Key<BIRGen> BIR_GEN =
            new CompilerContext.Key<>();

    private BIRGenEnv env;
    private Names names;

    public static BIRGen getInstance(CompilerContext context) {
        BIRGen birGen = context.get(BIR_GEN);
        if (birGen == null) {
            birGen = new BIRGen(context);
        }

        return birGen;
    }

    private BIRGen(CompilerContext context) {
        context.put(BIR_GEN, this);

        this.names = Names.getInstance(context);
    }

    public void visit(BLangPackage astPkg) {
        BIRPackage birPkg = new BIRPackage(astPkg.packageID.name, astPkg.packageID.version);

        this.env = BIRGenEnv.packageEnv(birPkg);
        // Lower function nodes in AST to bir function nodes.
        // TODO handle init, start, stop functions
        astPkg.functions.forEach(astFunc -> genNode(astFunc, this.env));
    }

    public void visit(BLangFunction astFunc) {
        BIRFunction birFunc = new BIRFunction(astFunc.symbol.name);
        birFunc.isDeclaration = Symbols.isNative(astFunc.symbol);
        birFunc.visibility = getVisibility(astFunc.symbol);
        birFunc.argsCount = astFunc.requiredParams.size() +
                astFunc.defaultableParams.size() + (astFunc.restParam != null ? 1 : 0);
        birFunc.type = (BInvokableType) astFunc.symbol.type;

        this.env.enclPkg.functions.add(birFunc);

        // TODO Support for multiple workers
        BLangWorker astDefaultWorker = astFunc.workers.get(0);
        BIRGenEnv funcEnv = BIRGenEnv.funcEnv(this.env, birFunc);
        genNode(astDefaultWorker, funcEnv);
    }

    public void visit(BLangWorker astWorker) {
        BIRBasicBlock birBB = new BIRBasicBlock(this.env.nextBBId(names));
        this.env.enclFunc.basicBlocks.add(birBB);
        BIRGenEnv bbEnv = BIRGenEnv.bbEnv(this.env, birBB);
        genNode(astWorker.body, bbEnv);
    }


    // Statements

    public void visit(BLangBlockStmt astBlockStmt) {
        for (BLangStatement astStmt : astBlockStmt.stmts) {
            genNode(astStmt, this.env);
        }
    }


    public void visit(BLangVariableDef astVarDefStmt) {
        BVarSymbol varSymbol = astVarDefStmt.var.symbol;
        BIRVariableDcl birVarDcl = new BIRVariableDcl(varSymbol.type, this.env.nextLocalVarId(names), VarKind.LOCAL);


    }

    public void visit(BLangAssignment astAssignStmt) {

    }


    // Expressions

    public void visit(BLangVariableReference astVarRef) {
        //astVarRef.symbol
    }

    public void visit(BLangBinaryExpr astBinaryExpr) {

    }


    // private methods

    // TODO Replace string with the proper env
    private <T extends BLangNode, U extends BIRGenEnv> T genNode(T t, U u) {
        BIRGenEnv prevEnv = this.env;
        this.env = u;
        t.accept(this);
        this.env = prevEnv;
        return t;
    }

    private Visibility getVisibility(BSymbol symbol) {
        if (Symbols.isPublic(symbol)) {
            return Visibility.PUBLIC;
        } else {
            return Visibility.PRIVATE;
        }

        //TODO handle package-private case.
    }
}
