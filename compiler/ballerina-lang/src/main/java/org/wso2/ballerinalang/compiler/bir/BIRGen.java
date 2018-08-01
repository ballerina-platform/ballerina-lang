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

import org.wso2.ballerinalang.compiler.bir.model.BIRInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.BinaryOp;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.Move;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand.BIRConstant;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand.BIRVarRef;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.bir.model.Visibility;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangLocalVarRef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
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

    public BIRPackage genBIR(BLangPackage astPkg) {
        astPkg.accept(this);
        return this.env.enclPkg;
    }

    // Nodes

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
        BLangBlockStmt astFuncBlock = astFunc.body;
        BIRGenEnv funcEnv = BIRGenEnv.funcEnv(this.env, birFunc);

        // Return bir variable declaration
        BIRVariableDcl retVarDcl = new BIRVariableDcl(astFunc.symbol.retType,
                funcEnv.nextLocalVarId(names), VarKind.RETURN);
        funcEnv.enclFunc.localVars.add(retVarDcl);

        for (BLangVariable requiredParam : astFunc.requiredParams) {
            BIRVariableDcl birVarDcl = new BIRVariableDcl(requiredParam.symbol.type,
                    funcEnv.nextLocalVarId(names), VarKind.ARG);
            funcEnv.enclFunc.localVars.add(birVarDcl);

            // We maintain a mapping from variable symbol to the bir_variable declaration.
            // This is required to pull the correct bir_variable declaration for variable references.
            funcEnv.addVarDcl(requiredParam.symbol, birVarDcl);
        }

        // Create variable declarations

        // Create the entry basic block
        BIRBasicBlock birBB = new BIRBasicBlock(funcEnv.nextBBId(names));
        funcEnv.enclFunc.basicBlocks.add(birBB);
        BIRGenEnv bbEnv = BIRGenEnv.bbEnv(funcEnv, birBB);

        genNode(astFuncBlock, bbEnv);
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
        BIRVariableDcl birVarDcl = new BIRVariableDcl(astVarDefStmt.var.symbol.type, this.env.nextLocalVarId(names), VarKind.LOCAL);
        this.env.enclFunc.localVars.add(birVarDcl);

        // We maintain a mapping from variable symbol to the bir_variable declaration.
        // This is required to pull the correct bir_variable declaration for variable references.
        this.env.addVarDcl(astVarDefStmt.var.symbol, birVarDcl);

        if (astVarDefStmt.var.expr == null) {
            return;
        }

        // Visit the rhs expression.
        genNode(astVarDefStmt.var.expr, this.env);

        // Create a variable reference and
        BIRVarRef varRef = new BIRVarRef(birVarDcl);
        emit(new Move(this.env.targetOperand, varRef));
    }

    public void visit(BLangAssignment astAssignStmt) {
        // TODO The following works only for local variable references.
        BLangLocalVarRef astVarRef = (BLangLocalVarRef) astAssignStmt.varRef;

        genNode(astAssignStmt.expr, this.env);
        BIRVarRef varRef = new BIRVarRef(this.env.getVarDcl(astVarRef.symbol));
        emit(new Move(this.env.targetOperand, varRef));
    }

    public void visit(BLangReturn astReturnStmt) {
        genNode(astReturnStmt.expr, this.env);
        BIRVarRef retVarRef = new BIRVarRef(this.env.enclFunc.localVars.get(0));
        emit(new Move(this.env.targetOperand, retVarRef));
        this.env.enclBB.terminator = new BIRTerminator.Return();
    }


    // Expressions

    public void visit(BLangLiteral astLiteralExpr) {
        this.env.targetOperand = new BIRConstant(astLiteralExpr.type, astLiteralExpr.value);
    }

    public void visit(BLangLocalVarRef astVarRefExpr) {
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(astVarRefExpr.type,
                this.env.nextLocalVarId(names), VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIRVarRef tempVarRef = new BIRVarRef(tempVarDcl);
        BIRVarRef fromVarRef = new BIRVarRef(this.env.getVarDcl(astVarRefExpr.symbol));
        emit(new Move(fromVarRef, tempVarRef));
        this.env.targetOperand = tempVarRef;
    }

    public void visit(BLangBinaryExpr astBinaryExpr) {
        genNode(astBinaryExpr.lhsExpr, this.env);
        BIROperand rhsOp1 = this.env.targetOperand;

        genNode(astBinaryExpr.rhsExpr, this.env);
        BIROperand rhsOp2 = this.env.targetOperand;

        // Create a temporary variable to store the binary operation result.
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(astBinaryExpr.type,
                this.env.nextLocalVarId(names), VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIRVarRef lhsOp = new BIRVarRef(tempVarDcl);
        this.env.targetOperand = lhsOp;

        // Create binary instruction
        BinaryOp binaryIns = new BinaryOp(astBinaryExpr.opKind, astBinaryExpr.type, lhsOp, rhsOp1, rhsOp2);
        emit(binaryIns);
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

    private void emit(BIRInstruction instruction) {
        this.env.enclBB.instructions.add(instruction);
    }
}
