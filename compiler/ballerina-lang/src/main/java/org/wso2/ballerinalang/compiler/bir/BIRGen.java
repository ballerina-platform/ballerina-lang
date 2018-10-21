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

import org.ballerinalang.model.tree.OperatorKind;
import org.wso2.ballerinalang.compiler.bir.model.BIRInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.BinaryOp;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.Move;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand.BIRVarRef;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.bir.model.Visibility;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangLocalVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.List;

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
        BIRPackage birPkg = new BIRPackage(astPkg.packageID.orgName,
                astPkg.packageID.name, astPkg.packageID.version);

        this.env = new BIRGenEnv(birPkg);
        // Lower function nodes in AST to bir function nodes.
        // TODO handle init, start, stop functions
        astPkg.functions.forEach(astFunc -> astFunc.accept(this));
    }

    public void visit(BLangFunction astFunc) {
        Visibility visibility = getVisibility(astFunc.symbol);
        BInvokableType type = astFunc.symbol.getType();
        BIRFunction birFunc = new BIRFunction(astFunc.symbol.name, visibility, type);
        birFunc.isDeclaration = Symbols.isNative(astFunc.symbol);
        birFunc.argsCount = astFunc.requiredParams.size() +
                            astFunc.defaultableParams.size() + (astFunc.restParam != null ? 1 : 0);

        this.env.enclPkg.functions.add(birFunc);
        this.env.enclFunc = birFunc;

        if (astFunc.symbol.retType.tag != TypeTags.NIL) {
            // Special %0 location for storing return values
            BIRVariableDcl retVarDcl = new BIRVariableDcl(astFunc.symbol.retType,
                                                          this.env.nextLocalVarId(names), VarKind.RETURN);
            birFunc.localVars.add(retVarDcl);
        }

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
        birFunc.basicBlocks.add(this.env.returnBB);
        this.env.clear();

        // Rearrange basic block ids.
        birFunc.basicBlocks.forEach(bb -> bb.id = this.env.nextBBId(names));
        this.env.clear();
    }


    // Statements

    public void visit(BLangBlockStmt astBlockStmt) {
        for (BLangStatement astStmt : astBlockStmt.stmts) {
            astStmt.accept(this);
        }

        // Due to the current algorithm, some basic blocks will not contain any instructions or a terminator.
        // These basic blocks will be remove by the optimizer, but for now just add a return terminator
        BIRBasicBlock enclBB = this.env.enclBB;
        if (enclBB.instructions.size() == 0 && enclBB.terminator == null) {
            enclBB.terminator = new BIRTerminator.GOTO(this.env.returnBB);
        }
    }

    public void visit(BLangSimpleVariableDef astVarDefStmt) {
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

    public void visit(BLangExpressionStmt exprStmtNode) {
        //        this.acceptNode(exprStmtNode.expr);
        exprStmtNode.expr.accept(this);
    }

    public void visit(BLangInvocation invocationExpr) {
        // Lets create a block the jump after successful function return
        BIRBasicBlock thenBB = new BIRBasicBlock(this.env.nextBBId(names));
        this.env.enclFunc.basicBlocks.add(thenBB);

        List<BLangExpression> requiredArgs = invocationExpr.requiredArgs;
        List<BLangExpression> restArgs = invocationExpr.restArgs;
        List<BIROperand> args = new ArrayList<>();

        for (BLangExpression requiredArg : requiredArgs) {
            requiredArg.accept(this);
            args.add(this.env.targetOperand);
        }

        //TODO: We unroll this array for now because LLVM side can't handle arrays yet, remove this later
        // seems like restArgs.size() is always 1 or 0, but lets iterate just in case
        for (BLangExpression arg : restArgs) {
            if (arg instanceof BLangArrayLiteral) {
                BLangArrayLiteral arrArg = (BLangArrayLiteral) arg;
                List<BLangExpression> exprs = arrArg.exprs;
                for (BLangExpression expr : exprs) {
                    if (expr instanceof BLangTypeConversionExpr) {
                        BLangExpression innerExpr = ((BLangTypeConversionExpr) expr).expr;
                        innerExpr.accept(this);
                        args.add(this.env.targetOperand);
                    } else {
                        expr.accept(this);
                        args.add(this.env.targetOperand);
                    }
                }
            }
        }

        BIRVarRef lhsOp = null;
        if (invocationExpr.type.tag != TypeTags.NIL) {
            // Create a temporary variable to store the return operation result.
            BIRVariableDcl tempVarDcl = new BIRVariableDcl(invocationExpr.type,
                                                           this.env.nextLocalVarId(names), VarKind.TEMP);
            this.env.enclFunc.localVars.add(tempVarDcl);
            lhsOp = new BIRVarRef(tempVarDcl);
            this.env.targetOperand = lhsOp;
        }

        this.env.enclBB.terminator = new BIRTerminator.Call(invocationExpr.symbol.pkgID,
                                                            names.fromString(invocationExpr.name.value),
                                                            args,
                                                            lhsOp,
                                                            thenBB);

        this.env.enclBB = thenBB;
    }


    public void visit(BLangReturn astReturnStmt) {
        if (astReturnStmt.expr.type.tag != TypeTags.NIL) {
            astReturnStmt.expr.accept(this);
            BIRVarRef retVarRef = new BIRVarRef(this.env.enclFunc.localVars.get(0));
            emit(new Move(this.env.targetOperand, retVarRef));
        }

        // Check whether this function already has returnBB.
        // A given function can have only one BB that has a return instruction.
        if (this.env.returnBB == null) {
            // If not create one
            BIRBasicBlock returnBB = new BIRBasicBlock(this.env.nextBBId(names));
            returnBB.terminator = new BIRTerminator.Return();
            this.env.returnBB = returnBB;
        }

        this.env.enclBB.terminator = new BIRTerminator.GOTO(this.env.returnBB);
    }

    public void visit(BLangIf astIfStmt) {
        astIfStmt.expr.accept(this);
        BIROperand ifExprResult = this.env.targetOperand;

        // Create the basic block for the if-then block.
        BIRBasicBlock thenBB = new BIRBasicBlock(this.env.nextBBId(names));
        this.env.enclFunc.basicBlocks.add(thenBB);

        // This basic block will contain statement that comes right after this 'if' statement.
        BIRBasicBlock nextBB = new BIRBasicBlock(this.env.nextBBId(names));

        // Add the branch instruction to the current basic block.
        // This is the end of the current basic block.
        BIRTerminator.Branch branchIns = new BIRTerminator.Branch(ifExprResult, thenBB, null);
        this.env.enclBB.terminator = branchIns;

        // Visit the then-block
        this.env.enclBB = thenBB;
        astIfStmt.body.accept(this);

        // If a terminator statement has not been set for the then-block then just add it.
        if (this.env.enclBB.terminator == null) {
            this.env.enclBB.terminator = new BIRTerminator.GOTO(nextBB);
        }

        // Check whether there exists an else-if or an else block.
        if (astIfStmt.elseStmt != null) {
            // Create a basic block for the else block.
            BIRBasicBlock elseBB = new BIRBasicBlock(this.env.nextBBId(names));
            this.env.enclFunc.basicBlocks.add(elseBB);
            branchIns.falseBB = elseBB;

            // Visit the else block. This could be an else-if block or an else block.
            this.env.enclBB = elseBB;
            astIfStmt.elseStmt.accept(this);

            // If a terminator statement has not been set for the else-block then just add it.
            if (this.env.enclBB.terminator == null) {
                this.env.enclBB.terminator = new BIRTerminator.GOTO(nextBB);
            }

        } else {
            branchIns.falseBB = nextBB;
        }

        // Set the elseBB as the basic block for the rest of statements followed by this if.
        this.env.enclFunc.basicBlocks.add(nextBB);
        this.env.enclBB = nextBB;
    }

    public void visit(BLangWhile astWhileStmt) {
        // Create a basic block for the while expression.
        BIRBasicBlock whileExprBB = new BIRBasicBlock(this.env.nextBBId(names));
        this.env.enclFunc.basicBlocks.add(whileExprBB);

        // Insert a GOTO instruction as the terminal instruction into current basic block.
        this.env.enclBB.terminator = new BIRTerminator.GOTO(whileExprBB);

        // Visit condition expression
        this.env.enclBB = whileExprBB;
        astWhileStmt.expr.accept(this);
        BIROperand whileExprResult = this.env.targetOperand;

        // Create the basic block for the while-body block.
        BIRBasicBlock whileBodyBB = new BIRBasicBlock(this.env.nextBBId(names));
        this.env.enclFunc.basicBlocks.add(whileBodyBB);

        // Create the basic block for the statements that comes after the while statement.
        BIRBasicBlock whileEndBB = new BIRBasicBlock(this.env.nextBBId(names));

        // Add the branch instruction to the while expression basic block.
        whileExprBB.terminator = new BIRTerminator.Branch(whileExprResult, whileBodyBB, whileEndBB);

        // Visit while body
        this.env.enclBB = whileBodyBB;
        astWhileStmt.body.accept(this);
        if (this.env.enclBB.terminator == null) {
            this.env.enclBB.terminator = new BIRTerminator.GOTO(whileExprBB);
        } else {
            throw new RuntimeException("there cannot be a terminator in while body basic block");
        }

        this.env.enclFunc.basicBlocks.add(whileEndBB);
        this.env.enclBB = whileEndBB;
    }


    // Expressions

    public void visit(BLangLiteral astLiteralExpr) {
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(astLiteralExpr.type,
                this.env.nextLocalVarId(names), VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIRVarRef toVarRef = new BIRVarRef(tempVarDcl);
        emit(new BIRNonTerminator.ConstantLoad(astLiteralExpr.value, astLiteralExpr.type, toVarRef));
        this.env.targetOperand = toVarRef;
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
        BinaryOp binaryIns = new BinaryOp(getBinaryInstructionKind(astBinaryExpr.opKind),
                astBinaryExpr.type, lhsOp, rhsOp1, rhsOp2);
        emit(binaryIns);
    }


    // private methods

    private Visibility getVisibility(BSymbol symbol) {
        if (Symbols.isPublic(symbol)) {
            return Visibility.PUBLIC;
        } else {
            return Visibility.PRIVATE;
        }
    }

    private void emit(BIRInstruction instruction) {
        this.env.enclBB.instructions.add(instruction);
    }

    private InstructionKind getBinaryInstructionKind(OperatorKind opKind) {
        switch (opKind) {
            case ADD:
                return InstructionKind.ADD;
            case SUB:
                return InstructionKind.SUB;
            case MUL:
                return InstructionKind.MUL;
            case DIV:
                return InstructionKind.DIV;
            case MOD:
                return InstructionKind.MOD;
            case EQUAL:
                return InstructionKind.EQUAL;
            case NOT_EQUAL:
                return InstructionKind.NOT_EQUAL;
            case GREATER_THAN:
                return InstructionKind.GREATER_THAN;
            case GREATER_EQUAL:
                return InstructionKind.GREATER_EQUAL;
            case LESS_THAN:
                return InstructionKind.LESS_THAN;
            case LESS_EQUAL:
                return InstructionKind.LESS_EQUAL;
            default:
                throw new IllegalStateException("unsupported binary operation: " + opKind.value());
        }
    }
}
