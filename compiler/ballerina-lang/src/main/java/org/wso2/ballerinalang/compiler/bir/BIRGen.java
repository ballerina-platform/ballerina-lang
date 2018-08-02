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
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
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

    public BLangPackage genBIR(BLangPackage astPkg) {
        astPkg.accept(this);
        astPkg.symbol.bir = this.env.enclPkg;
        return astPkg;
    }

    // Nodes

    public void visit(BLangPackage astPkg) {
        BIRPackage birPkg = new BIRPackage(astPkg.packageID.name, astPkg.packageID.version);

        this.env = new BIRGenEnv(birPkg);
        // Lower function nodes in AST to bir function nodes.
        // TODO handle init, start, stop functions
        astPkg.functions.forEach(astFunc -> astFunc.accept(this));
    }

    public void visit(BLangFunction astFunc) {
        BIRFunction birFunc = new BIRFunction(astFunc.symbol.name);
        birFunc.isDeclaration = Symbols.isNative(astFunc.symbol);
        birFunc.visibility = getVisibility(astFunc.symbol);
        birFunc.argsCount = astFunc.requiredParams.size() +
                astFunc.defaultableParams.size() + (astFunc.restParam != null ? 1 : 0);
        birFunc.type = (BInvokableType) astFunc.symbol.type;

        this.env.enclPkg.functions.add(birFunc);
        this.env.enclFunc = birFunc;

        // Special %0 location for storing return values
        BIRVariableDcl retVarDcl = new BIRVariableDcl(astFunc.symbol.retType,
                this.env.nextLocalVarId(names), VarKind.RETURN);
        birFunc.localVars.add(retVarDcl);

        // Create variable declaration for function params
        for (BLangVariable requiredParam : astFunc.requiredParams) {
            BIRVariableDcl birVarDcl = new BIRVariableDcl(requiredParam.symbol.type,
                    this.env.nextLocalVarId(names), VarKind.ARG);
            birFunc.localVars.add(birVarDcl);

            // We maintain a mapping from variable symbol to the bir_variable declaration.
            // This is required to pull the correct bir_variable declaration for variable references.
            this.env.symbolVarMap.put(requiredParam.symbol, birVarDcl);
        }

        // Create the entry basic block
        BIRBasicBlock entryBB = new BIRBasicBlock(this.env.nextBBId(names));
        birFunc.basicBlocks.add(entryBB);
        this.env.enclBB = entryBB;

        astFunc.body.accept(this);
        this.env.clear();
    }


    // Statements

    public void visit(BLangBlockStmt astBlockStmt) {
        for (BLangStatement astStmt : astBlockStmt.stmts) {
            astStmt.accept(this);
        }
    }

    public void visit(BLangVariableDef astVarDefStmt) {
        BIRVariableDcl birVarDcl = new BIRVariableDcl(astVarDefStmt.var.symbol.type,
                this.env.nextLocalVarId(names), VarKind.LOCAL);
        this.env.enclFunc.localVars.add(birVarDcl);

        // We maintain a mapping from variable symbol to the bir_variable declaration.
        // This is required to pull the correct bir_variable declaration for variable references.
        this.env.symbolVarMap.put(astVarDefStmt.var.symbol, birVarDcl);

        if (astVarDefStmt.var.expr == null) {
            return;
        }

        // Visit the rhs expression.
        astVarDefStmt.var.expr.accept(this);

        // Create a variable reference and
        BIRVarRef varRef = new BIRVarRef(birVarDcl);
        emit(new Move(this.env.targetOperand, varRef));
    }

    public void visit(BLangAssignment astAssignStmt) {
        // TODO The following works only for local variable references.
        BLangLocalVarRef astVarRef = (BLangLocalVarRef) astAssignStmt.varRef;

        astAssignStmt.expr.accept(this);
        BIRVarRef varRef = new BIRVarRef(this.env.symbolVarMap.get(astVarRef.symbol));
        emit(new Move(this.env.targetOperand, varRef));
    }

    public void visit(BLangReturn astReturnStmt) {
        astReturnStmt.expr.accept(this);
        BIRVarRef retVarRef = new BIRVarRef(this.env.enclFunc.localVars.get(0));
        emit(new Move(this.env.targetOperand, retVarRef));

        // Check whether this function already has returnBB.
        // A given function can have only one BB that has a return instruction.
        if (this.env.returnBB == null) {
            // If not create one
            BIRBasicBlock returnBB = new BIRBasicBlock(this.env.nextBBId(names));
            returnBB.terminator = new BIRTerminator.Return();
            this.env.enclFunc.basicBlocks.add(returnBB);
            this.env.returnBB = returnBB;
        }

        this.env.enclBB.terminator = new BIRTerminator.GOTO(this.env.returnBB);
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
        BIRVarRef fromVarRef = new BIRVarRef(this.env.symbolVarMap.get(astVarRefExpr.symbol));
        emit(new Move(fromVarRef, tempVarRef));
        this.env.targetOperand = tempVarRef;
    }

    public void visit(BLangBinaryExpr astBinaryExpr) {
        astBinaryExpr.lhsExpr.accept(this);
        BIROperand rhsOp1 = this.env.targetOperand;

        astBinaryExpr.rhsExpr.accept(this);
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
