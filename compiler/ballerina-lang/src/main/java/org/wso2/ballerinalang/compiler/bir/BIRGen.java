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
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRGlobalVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRTypeDefinition;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.BinaryOp;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.FieldAccess;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.Move;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.bir.model.Visibility;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangArrayAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangMapAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangStructFieldAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangMapLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKey;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangStructLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangLocalVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangPackageVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.util.BArrayState;
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
    private final SymbolTable symTable;

    // Required variables to generate code for assignment statements
    private boolean varAssignment = false;

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
        this.symTable = SymbolTable.getInstance(context);
    }

    public BLangPackage genBIR(BLangPackage astPkg) {
        astPkg.accept(this);
        return astPkg;
    }

    // Nodes

    public void visit(BLangPackage astPkg) {
        BIRPackage birPkg = new BIRPackage(astPkg.pos, astPkg.packageID.orgName,
                astPkg.packageID.name, astPkg.packageID.version);
        astPkg.symbol.bir = birPkg;

        this.env = new BIRGenEnv(birPkg);
        // Lower function nodes in AST to bir function nodes.
        // TODO handle init, start, stop functions
        astPkg.imports.forEach(impPkg -> impPkg.accept(this));
        astPkg.typeDefinitions.forEach(astTypeDef -> astTypeDef.accept(this));
        astPkg.globalVars.forEach(astGlobalVar -> astGlobalVar.accept(this));
//        astPkg.initFunction.accept(this);
        astPkg.functions.forEach(astFunc -> astFunc.accept(this));
    }

    public void visit(BLangTypeDefinition astTypeDefinition) {
        Visibility visibility = getVisibility(astTypeDefinition.symbol);
        BIRTypeDefinition typeDef = new BIRTypeDefinition(astTypeDefinition.pos,
                astTypeDefinition.symbol.name, visibility, astTypeDefinition.typeNode.type);
        this.env.enclPkg.typeDefs.add(typeDef);
    }

    public void visit(BLangImportPackage impPkg) {
        this.env.enclPkg.importModules.add(new BIRNode.BIRImportModule(impPkg.pos, impPkg.symbol.pkgID.orgName,
                impPkg.symbol.pkgID.name, impPkg.symbol.pkgID.version));
    }

    public void visit(BLangFunction astFunc) {
        Visibility visibility = getVisibility(astFunc.symbol);
        BInvokableType type = astFunc.symbol.getType();
        BIRFunction birFunc = new BIRFunction(astFunc.pos, astFunc.symbol.name, visibility, type);
        birFunc.isDeclaration = Symbols.isNative(astFunc.symbol);
        birFunc.argsCount = astFunc.requiredParams.size() +
                            astFunc.defaultableParams.size() + (astFunc.restParam != null ? 1 : 0);

        this.env.enclPkg.functions.add(birFunc);
        this.env.enclFunc = birFunc;

        if (astFunc.symbol.retType.tag != TypeTags.NIL) {
            // Special %0 location for storing return values
            BIRVariableDcl retVarDcl = new BIRVariableDcl(astFunc.pos, astFunc.symbol.retType,
                                                          this.env.nextLocalVarId(names), VarKind.RETURN);
            birFunc.localVars.add(retVarDcl);
        }

        // Create variable declaration for function params
        for (BLangVariable requiredParam : astFunc.requiredParams) {
            BIRVariableDcl birVarDcl = new BIRVariableDcl(requiredParam.pos, requiredParam.symbol.type,
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
            enclBB.terminator = new BIRTerminator.GOTO(null, this.env.returnBB);
        }
    }

    public void visit(BLangSimpleVariableDef astVarDefStmt) {
        BIRVariableDcl birVarDcl = new BIRVariableDcl(astVarDefStmt.pos, astVarDefStmt.var.symbol.type,
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
        BIROperand varRef = new BIROperand(birVarDcl);
        emit(new Move(astVarDefStmt.pos, this.env.targetOperand, varRef));
    }

    public void visit(BLangSimpleVariable varNode) {
        Visibility visibility = getVisibility(varNode.symbol);

        BIRGlobalVariableDcl birVarDcl = new BIRGlobalVariableDcl(varNode.pos, visibility, varNode.symbol.type,
                names.fromString(varNode.symbol.name.value), VarKind.GLOBAL);
        this.env.enclPkg.globalVars.add(birVarDcl);

        this.env.globalVarMap.put(varNode.symbol, birVarDcl);
    }

    public void visit(BLangAssignment astAssignStmt) {
        astAssignStmt.expr.accept(this);

        this.varAssignment = true;
        astAssignStmt.varRef.accept(this);
        this.varAssignment = false;
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

        BIROperand lhsOp = null;
        if (invocationExpr.type.tag != TypeTags.NIL) {
            // Create a temporary variable to store the return operation result.
            BIRVariableDcl tempVarDcl = new BIRVariableDcl(invocationExpr.type,
                                                           this.env.nextLocalVarId(names), VarKind.TEMP);
            this.env.enclFunc.localVars.add(tempVarDcl);
            lhsOp = new BIROperand(tempVarDcl);
            this.env.targetOperand = lhsOp;
        }

        this.env.enclBB.terminator = new BIRTerminator.Call(invocationExpr.pos, invocationExpr.symbol.pkgID,
                names.fromString(invocationExpr.name.value), args, lhsOp, thenBB);

        this.env.enclBB = thenBB;
    }


    public void visit(BLangReturn astReturnStmt) {
        if (astReturnStmt.expr.type.tag != TypeTags.NIL) {
            astReturnStmt.expr.accept(this);
            BIROperand retVarRef = new BIROperand(this.env.enclFunc.localVars.get(0));
            emit(new Move(astReturnStmt.pos, this.env.targetOperand, retVarRef));
        }

        // Check whether this function already has a returnBB.
        // A given function can have only one BB that has a return instruction.
        if (this.env.returnBB == null) {
            // If not create one
            BIRBasicBlock returnBB = new BIRBasicBlock(this.env.nextBBId(names));
            returnBB.terminator = new BIRTerminator.Return(astReturnStmt.pos);
            this.env.returnBB = returnBB;
        }

        this.env.enclBB.terminator = new BIRTerminator.GOTO(astReturnStmt.pos, this.env.returnBB);
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
        BIRTerminator.Branch branchIns = new BIRTerminator.Branch(astIfStmt.pos, ifExprResult, thenBB, null);
        this.env.enclBB.terminator = branchIns;

        // Visit the then-block
        this.env.enclBB = thenBB;
        astIfStmt.body.accept(this);

        // If a terminator statement has not been set for the then-block then just add it.
        if (this.env.enclBB.terminator == null) {
            this.env.enclBB.terminator = new BIRTerminator.GOTO(null, nextBB);
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
                this.env.enclBB.terminator = new BIRTerminator.GOTO(null, nextBB);
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
        this.env.enclBB.terminator = new BIRTerminator.GOTO(astWhileStmt.pos, whileExprBB);

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
        whileExprBB.terminator = new BIRTerminator.Branch(astWhileStmt.pos, whileExprResult, whileBodyBB, whileEndBB);

        // Visit while body
        this.env.enclBB = whileBodyBB;
        astWhileStmt.body.accept(this);
        if (this.env.enclBB.terminator == null) {
            this.env.enclBB.terminator = new BIRTerminator.GOTO(null, whileExprBB);
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
        BIROperand toVarRef = new BIROperand(tempVarDcl);
        emit(new BIRNonTerminator.ConstantLoad(astLiteralExpr.pos,
                astLiteralExpr.value, astLiteralExpr.type, toVarRef));
        this.env.targetOperand = toVarRef;
    }

    public void visit(BLangMapLiteral astMapLiteralExpr) {
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(astMapLiteralExpr.type,
                this.env.nextLocalVarId(names), VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);
        emit(new BIRNonTerminator.NewStructure(astMapLiteralExpr.pos, astMapLiteralExpr.type, toVarRef));
        this.env.targetOperand = toVarRef;

        // Handle Map init stuff
        for (BLangRecordKeyValue keyValue : astMapLiteralExpr.keyValuePairs) {
            BLangExpression keyExpr = keyValue.key.expr;
            keyExpr.accept(this);
            BIROperand keyRegIndex = this.env.targetOperand;

            BLangExpression valueExpr = keyValue.valueExpr;
            valueExpr.accept(this);
            BIROperand rhsOp = this.env.targetOperand;

            emit(new BIRNonTerminator.FieldAccess(astMapLiteralExpr.pos,
                    InstructionKind.MAP_STORE, toVarRef, keyRegIndex, rhsOp));
        }

        this.env.targetOperand = toVarRef;
    }

    public void visit(BLangTypeConversionExpr astTypeConversionExpr) {

        BIRVariableDcl tempVarDcl = new BIRVariableDcl(astTypeConversionExpr.targetType,
                this.env.nextLocalVarId(names), VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);

        astTypeConversionExpr.expr.accept(this);
        BIROperand rhsOp = this.env.targetOperand;

        emit(new BIRNonTerminator.TypeCast(astTypeConversionExpr.pos, toVarRef, rhsOp));

        this.env.targetOperand = toVarRef;
    }

    public void visit(BLangStructLiteral astStructLiteralExpr) {
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(astStructLiteralExpr.type,
                this.env.nextLocalVarId(names), VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);
        emit(new BIRNonTerminator.NewStructure(astStructLiteralExpr.pos, astStructLiteralExpr.type, toVarRef));
        this.env.targetOperand = toVarRef;


        BRecordTypeSymbol structSymbol = (BRecordTypeSymbol) astStructLiteralExpr.type.tsymbol;

        // Invoke the struct initializer here.
        if (astStructLiteralExpr.initializer != null) {
            //TODO
        }

        // Generate code the struct literal.
        for (BLangRecordKeyValue keyValue : astStructLiteralExpr.keyValuePairs) {
            BLangRecordKey key = keyValue.key;
            key.expr.accept(this);
            BIROperand keyRegIndex = this.env.targetOperand;

            keyValue.valueExpr.accept(this);
            BIROperand valueRegIndex = this.env.targetOperand;

            emit(new FieldAccess(astStructLiteralExpr.pos,
                    InstructionKind.MAP_STORE, toVarRef, keyRegIndex, valueRegIndex));
        }
        this.env.targetOperand = toVarRef;
    }

    public void visit(BLangArrayLiteral astArrayLiteralExpr) {
        // Emit create array instruction
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(astArrayLiteralExpr.type,
                this.env.nextLocalVarId(names), VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);

        long size = astArrayLiteralExpr.type.tag == TypeTags.ARRAY &&
                ((BArrayType) astArrayLiteralExpr.type).state != BArrayState.UNSEALED ?
                (long) ((BArrayType) astArrayLiteralExpr.type).size : -1L;

        BLangLiteral literal = new BLangLiteral();
        literal.pos = astArrayLiteralExpr.pos;
        literal.value = size;
        literal.type = symTable.intType;
        literal.accept(this);
        BIROperand sizeOp = this.env.targetOperand;

        emit(new BIRNonTerminator.NewArray(astArrayLiteralExpr.pos, astArrayLiteralExpr.type, toVarRef, sizeOp));

        // Emit instructions populate initial array values;
        for (int i = 0; i < astArrayLiteralExpr.exprs.size(); i++) {
            BLangExpression argExpr = astArrayLiteralExpr.exprs.get(i);
            argExpr.accept(this);
            BIROperand exprIndex = this.env.targetOperand;

            BLangLiteral indexLiteral = new BLangLiteral();
            indexLiteral.pos = astArrayLiteralExpr.pos;
            indexLiteral.value = (long) i;
            indexLiteral.type = symTable.intType;
            indexLiteral.accept(this);
            BIROperand arrayIndex = this.env.targetOperand;

            emit(new BIRNonTerminator.FieldAccess(astArrayLiteralExpr.pos,
                    InstructionKind.ARRAY_STORE, toVarRef, arrayIndex, exprIndex));
        }
        this.env.targetOperand = toVarRef;
    }

    @Override
    public void visit(BLangMapAccessExpr astMapAccessExpr) {
        visitIndexBased(astMapAccessExpr);
    }

    @Override
    public void visit(BLangStructFieldAccessExpr astStructFieldAccessExpr) {
        visitIndexBased(astStructFieldAccessExpr);
    }

    private void visitIndexBased(BLangIndexBasedAccess astIndexBasedAccessExpr) {
        boolean variableStore = this.varAssignment;
        this.varAssignment = false;

        if (variableStore) {
            BIROperand rhsOp = this.env.targetOperand;

            astIndexBasedAccessExpr.expr.accept(this);
            BIROperand varRefRegIndex = this.env.targetOperand;

            astIndexBasedAccessExpr.indexExpr.accept(this);
            BIROperand keyRegIndex = this.env.targetOperand;

            emit(new BIRNonTerminator.FieldAccess(astIndexBasedAccessExpr.pos,
                    InstructionKind.MAP_STORE, varRefRegIndex, keyRegIndex, rhsOp));
        } else {
            BIRVariableDcl tempVarDcl = new BIRVariableDcl(astIndexBasedAccessExpr.type,
                    this.env.nextLocalVarId(names), VarKind.TEMP);
            this.env.enclFunc.localVars.add(tempVarDcl);
            BIROperand tempVarRef = new BIROperand(tempVarDcl);

            astIndexBasedAccessExpr.expr.accept(this);
            BIROperand varRefRegIndex = this.env.targetOperand;

            astIndexBasedAccessExpr.indexExpr.accept(this);
            BIROperand keyRegIndex = this.env.targetOperand;

            emit(new BIRNonTerminator.FieldAccess(astIndexBasedAccessExpr.pos,
                    InstructionKind.MAP_LOAD, tempVarRef, keyRegIndex, varRefRegIndex));
            this.env.targetOperand = tempVarRef;
        }
        this.varAssignment = variableStore;
    }

    public void visit(BLangArrayAccessExpr astArrayAccessExpr) {
        boolean variableStore = this.varAssignment;
        this.varAssignment = false;

        if (variableStore) {
            BIROperand rhsOp = this.env.targetOperand;

            astArrayAccessExpr.expr.accept(this);
            BIROperand varRefRegIndex = this.env.targetOperand;

            astArrayAccessExpr.indexExpr.accept(this);
            BIROperand keyRegIndex = this.env.targetOperand;

            emit(new BIRNonTerminator.FieldAccess(astArrayAccessExpr.pos,
                    InstructionKind.ARRAY_STORE, varRefRegIndex, keyRegIndex, rhsOp));
        } else {
            BIRVariableDcl tempVarDcl = new BIRVariableDcl(astArrayAccessExpr.type,
                    this.env.nextLocalVarId(names), VarKind.TEMP);
            this.env.enclFunc.localVars.add(tempVarDcl);
            BIROperand tempVarRef = new BIROperand(tempVarDcl);

            astArrayAccessExpr.expr.accept(this);
            BIROperand varRefRegIndex = this.env.targetOperand;

            astArrayAccessExpr.indexExpr.accept(this);
            BIROperand keyRegIndex = this.env.targetOperand;

            emit(new BIRNonTerminator.FieldAccess(astArrayAccessExpr.pos,
                    InstructionKind.ARRAY_LOAD, tempVarRef, keyRegIndex, varRefRegIndex));
            this.env.targetOperand = tempVarRef;
        }

        this.varAssignment = variableStore;
    }

    @Override
    public void visit(BLangBracedOrTupleExpr bracedOrTupleExpr) {
        // Emit create array instruction
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(bracedOrTupleExpr.type,
                this.env.nextLocalVarId(names), VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);

        long size = bracedOrTupleExpr.expressions.size();

        BLangLiteral literal = new BLangLiteral();
        literal.pos = bracedOrTupleExpr.pos;
        literal.value = size;
        literal.type = symTable.intType;
        literal.accept(this);
        BIROperand sizeOp = this.env.targetOperand;

        emit(new BIRNonTerminator.NewArray(bracedOrTupleExpr.pos, bracedOrTupleExpr.type, toVarRef, sizeOp));

        // Emit instructions populate initial array values;
        for (int i = 0; i < bracedOrTupleExpr.expressions.size(); i++) {
            BLangExpression argExpr = bracedOrTupleExpr.expressions.get(i);
            argExpr.accept(this);
            BIROperand exprIndex = this.env.targetOperand;

            BLangLiteral indexLiteral = new BLangLiteral();
            indexLiteral.pos = bracedOrTupleExpr.pos;
            indexLiteral.value = (long) i;
            indexLiteral.type = symTable.intType;
            indexLiteral.accept(this);
            BIROperand arrayIndex = this.env.targetOperand;

            emit(new BIRNonTerminator.FieldAccess(bracedOrTupleExpr.pos,
                    InstructionKind.ARRAY_STORE, toVarRef, arrayIndex, exprIndex));
        }
        this.env.targetOperand = toVarRef;
    }

    @Override
    public void visit(BLangLocalVarRef astVarRefExpr) {
        boolean variableStore = this.varAssignment;
        this.varAssignment = false;

        if (variableStore) {
            BIROperand varRef = new BIROperand(this.env.symbolVarMap.get(astVarRefExpr.symbol));
            emit(new Move(astVarRefExpr.pos, this.env.targetOperand, varRef));
        } else {
            BIRVariableDcl tempVarDcl = new BIRVariableDcl(astVarRefExpr.type,
                    this.env.nextLocalVarId(names), VarKind.TEMP);
            this.env.enclFunc.localVars.add(tempVarDcl);
            BIROperand tempVarRef = new BIROperand(tempVarDcl);
            BIROperand fromVarRef = new BIROperand(this.env.symbolVarMap.get(astVarRefExpr.symbol));
            emit(new Move(astVarRefExpr.pos, fromVarRef, tempVarRef));
            this.env.targetOperand = tempVarRef;
        }
        this.varAssignment = variableStore;
    }

    public void visit(BLangPackageVarRef astPackageVarRefExpr) {
        boolean variableStore = this.varAssignment;
        this.varAssignment = false;

        if (variableStore) {
            BIROperand varRef = new BIROperand(this.env.globalVarMap.get(astPackageVarRefExpr.symbol));
            emit(new Move(astPackageVarRefExpr.pos, this.env.targetOperand, varRef));
        } else {
            BIRVariableDcl tempVarDcl = new BIRVariableDcl(astPackageVarRefExpr.type,
                    this.env.nextLocalVarId(names), VarKind.GLOBAL);
            this.env.enclFunc.localVars.add(tempVarDcl);
            BIROperand tempVarRef = new BIROperand(tempVarDcl);
            BIROperand fromVarRef = new BIROperand(this.env.globalVarMap.get(astPackageVarRefExpr.symbol));
            emit(new Move(astPackageVarRefExpr.pos, fromVarRef, tempVarRef));
            this.env.targetOperand = tempVarRef;
        }
        this.varAssignment = variableStore;
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
        BIROperand lhsOp = new BIROperand(tempVarDcl);
        this.env.targetOperand = lhsOp;

        // Create binary instruction
        BinaryOp binaryIns = new BinaryOp(astBinaryExpr.pos, getBinaryInstructionKind(astBinaryExpr.opKind),
                astBinaryExpr.type, lhsOp, rhsOp1, rhsOp2);
        emit(binaryIns);
    }

    public void visit(BLangErrorConstructorExpr errorExpr) {
        // Create a temporary variable to store the error.
        BIRVariableDcl tempVarError = new BIRVariableDcl(errorExpr.type,
                                                         this.env.nextLocalVarId(names), VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarError);
        BIROperand lhsOp = new BIROperand(tempVarError);
        // visit reason and detail expressions
        this.env.targetOperand = lhsOp;
        errorExpr.reasonExpr.accept(this);
        BIROperand reasonOp = this.env.targetOperand;
        errorExpr.detailsExpr.accept(this);
        BIROperand detailsOp = this.env.targetOperand;
        BIRNonTerminator.NewError newError = new BIRNonTerminator.NewError(errorExpr.pos, InstructionKind.NEW_ERROR,
                                                                           lhsOp, reasonOp, detailsOp);
        emit(newError);
        this.env.targetOperand = lhsOp;
    }

    public void visit(BLangPanic panicNode) {
        panicNode.expr.accept(this);
        emit(new BIRNonTerminator.Panic(panicNode.pos, InstructionKind.PANIC, this.env.targetOperand));
    }

    // private methods

    private Visibility getVisibility(BSymbol symbol) {
        if (Symbols.isPublic(symbol)) {
            return Visibility.PUBLIC;
        } else if (Symbols.isPrivate(symbol)) {
            return Visibility.PRIVATE;
        } else {
            return Visibility.PACKAGE_PRIVATE;
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
