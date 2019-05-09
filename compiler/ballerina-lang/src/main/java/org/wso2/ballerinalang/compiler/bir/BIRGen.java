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

import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.NodeKind;
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
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.UnaryOP;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.bir.model.VarScope;
import org.wso2.ballerinalang.compiler.bir.model.Visibility;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLNSSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS.BLangLocalXMLNS;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS.BLangPackageXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral.BLangJSONArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangArrayAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangJSONAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangMapAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangStructFieldAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangXMLAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsLikeExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangJSONLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangMapLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKey;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangStructLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangLocalVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangPackageVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTrapExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttributeAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.util.BArrayState;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.FieldKind;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;

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
    private Map<BTypeSymbol, BIRTypeDefinition> typeDefs = new LinkedHashMap<>();

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

    @Override
    public void visit(BLangPackage astPkg) {
        BIRPackage birPkg = new BIRPackage(astPkg.pos, astPkg.packageID.orgName,
                astPkg.packageID.name, astPkg.packageID.version, astPkg.packageID.sourceFileName);
        astPkg.symbol.bir = birPkg;

        this.env = new BIRGenEnv(birPkg);
        // Lower function nodes in AST to bir function nodes.
        // TODO handle init, start, stop functions
        astPkg.imports.forEach(impPkg -> impPkg.accept(this));
        astPkg.typeDefinitions.forEach(astTypeDef -> astTypeDef.accept(this));
        astPkg.globalVars.forEach(astGlobalVar -> astGlobalVar.accept(this));
        astPkg.initFunction.accept(this);
        astPkg.functions.forEach(astFunc -> astFunc.accept(this));
    }

    @Override
    public void visit(BLangTypeDefinition astTypeDefinition) {
        Visibility visibility = getVisibility(astTypeDefinition.symbol);

        BIRTypeDefinition typeDef = new BIRTypeDefinition(astTypeDefinition.pos,
                                                          astTypeDefinition.symbol.name,
                                                          visibility,
                                                          astTypeDefinition.typeNode.type,
                                                          new ArrayList<>());
        typeDefs.put(astTypeDefinition.symbol, typeDef);
        this.env.enclPkg.typeDefs.add(typeDef);
        typeDef.index = this.env.enclPkg.typeDefs.size() - 1;
    }

    @Override
    public void visit(BLangImportPackage impPkg) {
        this.env.enclPkg.importModules.add(new BIRNode.BIRImportModule(impPkg.pos, impPkg.symbol.pkgID.orgName,
                impPkg.symbol.pkgID.name, impPkg.symbol.pkgID.version));
    }

    @Override
    public void visit(BLangFunction astFunc) {
        Visibility visibility = getVisibility(astFunc.symbol);
        BInvokableType type = astFunc.symbol.getType();

        boolean isTypeAttachedFunction = astFunc.flagSet.contains(Flag.ATTACHED) &&
                !typeDefs.containsKey(astFunc.receiver.type.tsymbol);

        Name funcName;
        if (isTypeAttachedFunction) {
            funcName = names.fromString(astFunc.symbol.name.value);
        } else {
            funcName = getFuncName(astFunc.symbol);
        }

        BIRFunction birFunc = new BIRFunction(astFunc.pos, funcName, visibility, type);
        birFunc.isDeclaration = Symbols.isNative(astFunc.symbol);
        birFunc.isInterface = astFunc.interfaceFunction;
        birFunc.argsCount = astFunc.requiredParams.size() + astFunc.defaultableParams.size()
                + (astFunc.restParam != null ? 1 : 0) + astFunc.paramClosureMap.size();
        if (astFunc.flagSet.contains(Flag.ATTACHED) && typeDefs.containsKey(astFunc.receiver.type.tsymbol)) {
            typeDefs.get(astFunc.receiver.type.tsymbol).attachedFuncs.add(birFunc);
        } else {
            this.env.enclPkg.functions.add(birFunc);
        }

        this.env.enclFunc = birFunc;

        if (astFunc.symbol.retType != null && astFunc.symbol.retType.tag != TypeTags.NIL) {
            // Special %0 location for storing return values
            BIRVariableDcl retVarDcl = new BIRVariableDcl(astFunc.pos, astFunc.symbol.retType,
                    this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.RETURN);
            birFunc.localVars.add(retVarDcl);
        }

        //add closure vars
        astFunc.paramClosureMap.forEach((k, v) -> addParam(birFunc, v, astFunc.pos));

        // Create variable declaration for function params
        astFunc.requiredParams.forEach(requiredParam -> addParam(birFunc, requiredParam));
        astFunc.defaultableParams.forEach(defaultableParam -> addParam(birFunc, defaultableParam.var));
        if (astFunc.restParam != null) {
            addParam(birFunc, astFunc.restParam);
        }

        if (birFunc.isInterface || birFunc.isDeclaration) {
            this.env.clear();
            return;
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
        // Rearrange error entries.
        birFunc.errorTable.sort(Comparator.comparing(o -> o.trapBB.id.value));
        this.env.clear();
    }

    @Override
    public void visit(BLangLambdaFunction lambdaExpr) {
        //fpload instruction
        BIRVariableDcl tempVarLambda = new BIRVariableDcl(lambdaExpr.type,
                this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarLambda);
        BIROperand lhsOp = new BIROperand(tempVarLambda);
        Name funcName = getFuncName(lambdaExpr.function.symbol);

        List<BIRVariableDcl> params = new ArrayList<>();

        lambdaExpr.function.requiredParams.forEach(param -> {
            BIRVariableDcl birVarDcl = new BIRVariableDcl(param.pos, param.symbol.type,
                    this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.ARG);
            params.add(birVarDcl);
        });

        lambdaExpr.function.defaultableParams.forEach(param -> {
            BIRVariableDcl birVarDcl = new BIRVariableDcl(param.pos, param.var.symbol.type,
                    this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.ARG);
            params.add(birVarDcl);
        });
        BLangSimpleVariable restParam = lambdaExpr.function.restParam;
        if (restParam != null) {
            BIRVariableDcl birVarDcl = new BIRVariableDcl(restParam.pos, restParam.symbol.type,
                    this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.ARG);
            params.add(birVarDcl);
        }

        emit(new BIRNonTerminator.FPLoad(lambdaExpr.pos, lambdaExpr.function.symbol.pkgID, funcName, lhsOp, params,
                getClosureMapOperands(lambdaExpr)));
        this.env.targetOperand = lhsOp;
    }

    private List<BIROperand> getClosureMapOperands(BLangLambdaFunction lambdaExpr) {
        List<BIROperand> closureMaps = new ArrayList<>();

        lambdaExpr.function.paramClosureMap.forEach((k, v) -> {
            BVarSymbol symbol = lambdaExpr.enclMapSymbols.get(k);
            if (symbol == null) {
                symbol = lambdaExpr.paramMapSymbolsOfEnclInvokable.get(k);
            }
            BIROperand varRef = new BIROperand(this.env.symbolVarMap.get(symbol));
            closureMaps.add(varRef);
        });

        return closureMaps;
    }

    private Name getFuncName(BInvokableSymbol symbol) {
        if (symbol.receiverSymbol == null) {
            return symbol.name;
        }

        int offset = symbol.receiverSymbol.type.tsymbol.name.value.length() + 1;
        String attachedFuncName = symbol.name.value;
        return names.fromString(attachedFuncName.substring(offset, attachedFuncName.length()));
    }

    private void addParam(BIRFunction birFunc, BLangVariable requiredParam) {
        BIRVariableDcl birVarDcl = new BIRVariableDcl(requiredParam.pos, requiredParam.symbol.type,
                this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.ARG);
        birFunc.localVars.add(birVarDcl);

        // We maintain a mapping from variable symbol to the bir_variable declaration.
        // This is required to pull the correct bir_variable declaration for variable references.
        this.env.symbolVarMap.put(requiredParam.symbol, birVarDcl);
    }

    private void addParam(BIRFunction birFunc, BVarSymbol paramSymbol, DiagnosticPos pos) {
        BIRVariableDcl birVarDcl = new BIRVariableDcl(pos, paramSymbol.type,
                this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.ARG);
        birFunc.localVars.add(birVarDcl);

        // We maintain a mapping from variable symbol to the bir_variable declaration.
        // This is required to pull the correct bir_variable declaration for variable references.
        this.env.symbolVarMap.put(paramSymbol, birVarDcl);
    }


    // Statements

    @Override
    public void visit(BLangBlockStmt astBlockStmt) {
        for (BLangStatement astStmt : astBlockStmt.stmts) {
            astStmt.accept(this);
        }

        // Due to the current algorithm, some basic blocks will not contain any instructions or a terminator.
        // These basic blocks will be remove by the optimizer, but for now just add a return terminator
        BIRBasicBlock enclBB = this.env.enclBB;
        if (enclBB.instructions.size() == 0 && enclBB.terminator == null && this.env.returnBB != null) {
            enclBB.terminator = new BIRTerminator.GOTO(null, this.env.returnBB);
        }
    }

    @Override
    public void visit(BLangSimpleVariableDef astVarDefStmt) {
        BIRVariableDcl birVarDcl = new BIRVariableDcl(astVarDefStmt.pos, astVarDefStmt.var.symbol.type,
                this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.LOCAL);
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

    @Override
    public void visit(BLangSimpleVariable varNode) {
        Visibility visibility = getVisibility(varNode.symbol);

        BIRGlobalVariableDcl birVarDcl = new BIRGlobalVariableDcl(varNode.pos, visibility, varNode.symbol.type,
                this.env.nextGlobalVarId(names), VarScope.GLOBAL, VarKind.GLOBAL);
        this.env.enclPkg.globalVars.add(birVarDcl);

        this.env.globalVarMap.put(varNode.symbol, birVarDcl);
    }

    @Override
    public void visit(BLangAssignment astAssignStmt) {
        astAssignStmt.expr.accept(this);

        this.varAssignment = true;
        astAssignStmt.varRef.accept(this);
        this.varAssignment = false;
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        //        this.acceptNode(exprStmtNode.expr);
        exprStmtNode.expr.accept(this);
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        createCall(invocationExpr, false);
    }

    @Override
    public void visit(BLangStatementExpression statementExpression) {
        statementExpression.stmt.accept(this);
        statementExpression.expr.accept(this);
    }

    @Override
    public void visit(BLangInvocation.BLangAttachedFunctionInvocation invocationExpr) {
        createCall(invocationExpr, true);
    }

    @Override
    public void visit(BLangInvocation.BFunctionPointerInvocation invocation) {
        invocation.functionPointerInvocation = true;
        createCall(invocation, false);
    }

    private void createWait(BLangWaitExpr waitExpr) {

        BIRBasicBlock thenBB = new BIRBasicBlock(this.env.nextBBId(names));
        // This only supports wait for single future and alternate wait
        List<BIROperand> exprList = new ArrayList<>();

        waitExpr.exprList.forEach(expr -> {
            expr.accept(this);
            exprList.add(this.env.targetOperand);
        });

        BIRVariableDcl tempVarDcl = new BIRVariableDcl(waitExpr.type, this.env.nextLocalVarId(names),
                VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand lhsOp = new BIROperand(tempVarDcl);
        this.env.targetOperand = lhsOp;

        this.env.enclBB.terminator = new BIRTerminator.Wait(waitExpr.pos, exprList, lhsOp);

        this.env.enclFunc.basicBlocks.add(thenBB);
        this.env.enclBB = thenBB;
    }

    private void createCall(BLangInvocation invocationExpr, boolean isVirtual) {
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

        for (BLangExpression namedArg : invocationExpr.namedArgs) {
            namedArg.accept(this);
            args.add(this.env.targetOperand);
        }

        // seems like restArgs.size() is always 1 or 0, but lets iterate just in case
        for (BLangExpression arg : restArgs) {
            arg.accept(this);
            args.add(this.env.targetOperand);
        }

        BIROperand fp = null;
        if (invocationExpr.functionPointerInvocation) {
            invocationExpr.expr.accept(this);
            fp = this.env.targetOperand;
        }

        BIROperand lhsOp = null;
        if (invocationExpr.type.tag != TypeTags.NIL) {
            // Create a temporary variable to store the return operation result.
            BIRVariableDcl tempVarDcl = new BIRVariableDcl(invocationExpr.type, this.env.nextLocalVarId(names),
                    VarScope.FUNCTION, VarKind.TEMP);
            this.env.enclFunc.localVars.add(tempVarDcl);
            lhsOp = new BIROperand(tempVarDcl);
            this.env.targetOperand = lhsOp;
        }

        // TODO: make vCall a new instruction to avoid package id in vCall
        Name funcName = getFuncName((BInvokableSymbol) invocationExpr.symbol);
        if (invocationExpr.functionPointerInvocation) {
            this.env.enclBB.terminator = new BIRTerminator.FPCall(invocationExpr.pos, InstructionKind.FP_CALL,
                    fp, args, lhsOp, invocationExpr.async, thenBB);
        } else if (invocationExpr.async) {
            this.env.enclBB.terminator = new BIRTerminator.AsyncCall(invocationExpr.pos, InstructionKind.ASYNC_CALL,
                    isVirtual, invocationExpr.symbol.pkgID, funcName, args, lhsOp, thenBB);
        } else {
            this.env.enclBB.terminator = new BIRTerminator.Call(invocationExpr.pos, InstructionKind.CALL, isVirtual,
                    invocationExpr.symbol.pkgID, funcName, args, lhsOp, thenBB);
        }

        this.env.enclBB = thenBB;
    }

    @Override
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
        if (this.env.enclBB.terminator == null) {
            this.env.enclBB.terminator = new BIRTerminator.GOTO(astReturnStmt.pos, this.env.returnBB);
        }
    }

    @Override
    public void visit(BLangPanic panicNode) {
        panicNode.expr.accept(this);
        // Some functions will only have panic but we need to add return for them to make current algorithm work.
        if (this.env.returnBB == null) {
            BIRBasicBlock returnBB = new BIRBasicBlock(this.env.nextBBId(names));
            returnBB.terminator = new BIRTerminator.Return(panicNode.pos);
            this.env.returnBB = returnBB;
        }
        this.env.enclBB.terminator = new BIRTerminator.Panic(panicNode.pos, this.env.targetOperand);
    }

    @Override
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

    @Override
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
        this.env.enclLoopBB = whileExprBB;
        this.env.enclLoopEndBB = whileEndBB;
        astWhileStmt.body.accept(this);
        if (this.env.enclBB.terminator == null) {
            this.env.enclBB.terminator = new BIRTerminator.GOTO(null, whileExprBB);
        }

        this.env.enclFunc.basicBlocks.add(whileEndBB);
        this.env.enclBB = whileEndBB;
    }


    // Expressions

    @Override
    public void visit(BLangLiteral astLiteralExpr) {
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(astLiteralExpr.type,
                this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);
        emit(new BIRNonTerminator.ConstantLoad(astLiteralExpr.pos,
                astLiteralExpr.value, astLiteralExpr.type, toVarRef));
        this.env.targetOperand = toVarRef;
    }

    @Override
    public void visit(BLangMapLiteral astMapLiteralExpr) {
        generateMappingLiteral(astMapLiteralExpr);
    }

    @Override
    public void visit(BLangJSONLiteral jsonLiteral) {
        generateMappingLiteral(jsonLiteral);
    }

    @Override
    public void visit(BLangTypeConversionExpr astTypeConversionExpr) {
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(astTypeConversionExpr.type,
                this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);

        astTypeConversionExpr.expr.accept(this);
        BIROperand rhsOp = this.env.targetOperand;

        emit(new BIRNonTerminator.TypeCast(astTypeConversionExpr.pos, toVarRef, rhsOp));
        this.env.targetOperand = toVarRef;
    }

    @Override
    public void visit(BLangStructLiteral astStructLiteralExpr) {
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(astStructLiteralExpr.type,
                this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);
        emit(new BIRNonTerminator.NewStructure(astStructLiteralExpr.pos, astStructLiteralExpr.type, toVarRef));
        this.env.targetOperand = toVarRef;

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

    @Override
    public void visit(BLangTypeInit connectorInitExpr) {
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(connectorInitExpr.type, this.env.nextLocalVarId(names),
                                                       VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);

        BTypeSymbol objectTypeSymbol = getObjectTypeSymbol(connectorInitExpr.type);
        emit(new BIRNonTerminator.NewInstance(connectorInitExpr.pos, typeDefs.get(objectTypeSymbol), toVarRef));
        this.env.targetOperand = toVarRef;
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFieldVarRef fieldVarRef) {
    }

    @Override
    public void visit(BLangTableLiteral tableLiteral) {
        generateTableLiteral(tableLiteral);
    }

    @Override
    public void visit(BLangArrayLiteral astArrayLiteralExpr) {
        generateArrayLiteral(astArrayLiteralExpr);
    }

    @Override
    public void visit(BLangJSONArrayLiteral jsonArrayLiteralExpr) {
        generateArrayLiteral(jsonArrayLiteralExpr);
    }

    @Override
    public void visit(BLangMapAccessExpr astMapAccessExpr) {
        generateMappingAccess(astMapAccessExpr);
    }

    @Override
    public void visit(BLangStructFieldAccessExpr astStructFieldAccessExpr) {
        generateMappingAccess(astStructFieldAccessExpr);
    }

    @Override
    public void visit(BLangJSONAccessExpr astJSONFieldAccessExpr) {
        if (astJSONFieldAccessExpr.indexExpr.type.tag == TypeTags.INT) {
            generateArrayAccess(astJSONFieldAccessExpr);
            return;
        }

        generateMappingAccess(astJSONFieldAccessExpr);
    }

    @Override
    public void visit(BLangArrayAccessExpr astArrayAccessExpr) {
        generateArrayAccess(astArrayAccessExpr);
    }

    @Override
    public void visit(BLangBracedOrTupleExpr bracedOrTupleExpr) {
        // Emit create array instruction
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(bracedOrTupleExpr.type,
                this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.TEMP);
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
    public void visit(BLangIsLikeExpr isLikeExpr) {
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(symTable.booleanType,
                this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);

        isLikeExpr.expr.accept(this);
        BIROperand exprIndex = this.env.targetOperand;

        emit(new BIRNonTerminator.IsLike(isLikeExpr.pos, isLikeExpr.typeNode.type, toVarRef, exprIndex));

        this.env.targetOperand = toVarRef;
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(symTable.booleanType,
                this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);

        typeTestExpr.expr.accept(this);
        BIROperand exprIndex = this.env.targetOperand;

        emit(new BIRNonTerminator.TypeTest(typeTestExpr.pos, typeTestExpr.typeNode.type, toVarRef, exprIndex));

        this.env.targetOperand = toVarRef;
    }

    @Override
    public void visit(BLangLocalVarRef astVarRefExpr) {
        boolean variableStore = this.varAssignment;
        this.varAssignment = false;
        BSymbol varSymbol = astVarRefExpr.symbol;

        if (variableStore) {
            BIROperand varRef = new BIROperand(this.env.symbolVarMap.get(varSymbol));
            emit(new Move(astVarRefExpr.pos, this.env.targetOperand, varRef));
        } else {
            BIRVariableDcl tempVarDcl = new BIRVariableDcl(varSymbol.type,
                    this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.TEMP);
            this.env.enclFunc.localVars.add(tempVarDcl);
            BIROperand tempVarRef = new BIROperand(tempVarDcl);

            BIRVariableDcl varDecl;
            if (isSelfVar(varSymbol)) {
                varDecl = new BIRVariableDcl(varSymbol.type, varSymbol.name, VarScope.FUNCTION, VarKind.SELF);
            } else {
                varDecl = this.env.symbolVarMap.get(varSymbol);
            }
            BIROperand fromVarRef = new BIROperand(varDecl);

            emit(new Move(astVarRefExpr.pos, fromVarRef, tempVarRef));
            this.env.targetOperand = tempVarRef;
        }
        this.varAssignment = variableStore;
    }

    private boolean isSelfVar(BSymbol symbol) {
        return Names.SELF.equals(symbol.name);
    }

    @Override
    public void visit(BLangPackageVarRef astPackageVarRefExpr) {
        boolean variableStore = this.varAssignment;
        this.varAssignment = false;

        if (variableStore) {
            BIROperand varRef = new BIROperand(this.env.globalVarMap.get(astPackageVarRefExpr.symbol));
            emit(new Move(astPackageVarRefExpr.pos, this.env.targetOperand, varRef));
        } else {
            BIRVariableDcl tempVarDcl = new BIRVariableDcl(astPackageVarRefExpr.type,
                    this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.TEMP);
            this.env.enclFunc.localVars.add(tempVarDcl);
            BIROperand tempVarRef = new BIROperand(tempVarDcl);
            BIROperand fromVarRef = new BIROperand(this.env.globalVarMap.get(astPackageVarRefExpr.symbol));
            emit(new Move(astPackageVarRefExpr.pos, fromVarRef, tempVarRef));
            this.env.targetOperand = tempVarRef;
        }
        this.varAssignment = variableStore;
    }

    @Override
    public void visit(BLangBinaryExpr astBinaryExpr) {
        astBinaryExpr.lhsExpr.accept(this);
        BIROperand rhsOp1 = this.env.targetOperand;

        astBinaryExpr.rhsExpr.accept(this);
        BIROperand rhsOp2 = this.env.targetOperand;

        // Create a temporary variable to store the binary operation result.
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(astBinaryExpr.type,
                this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand lhsOp = new BIROperand(tempVarDcl);
        this.env.targetOperand = lhsOp;

        // Create binary instruction
        BinaryOp binaryIns = new BinaryOp(astBinaryExpr.pos, getBinaryInstructionKind(astBinaryExpr.opKind),
                astBinaryExpr.type, lhsOp, rhsOp1, rhsOp2);
        emit(binaryIns);
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        unaryExpr.expr.accept(this);
        BIROperand rhsOp = this.env.targetOperand;

        // Create a temporary variable to store the unary operation result.
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(unaryExpr.type,
                this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand lhsOp = new BIROperand(tempVarDcl);
        this.env.targetOperand = lhsOp;

        UnaryOP unaryIns = new UnaryOP(unaryExpr.pos, getUnaryInstructionKind(unaryExpr.operator), lhsOp, rhsOp);
        emit(unaryIns);
        this.env.targetOperand = lhsOp;
    }

    @Override
    public void visit(BLangErrorConstructorExpr errorExpr) {
        // Create a temporary variable to store the error.
        BIRVariableDcl tempVarError = new BIRVariableDcl(errorExpr.type,
                this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.TEMP);
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

    @Override
    public void visit(BLangTrapExpr trapExpr) {
        // This will move instructions inside trap expression for a new basic block unless current block does not have 
        // any instructions or already in the current trap block.
        if (!this.env.enclBB.instructions.isEmpty() && this.env.trapBB != this.env.enclBB) {
            this.env.trapBB = new BIRBasicBlock(this.env.nextBBId(names));
            env.enclFunc.basicBlocks.add(this.env.trapBB);
            this.env.enclBB.terminator = new BIRTerminator.GOTO(trapExpr.pos, this.env.trapBB);
            this.env.enclBB = this.env.trapBB;
        } else {
            this.env.trapBB = this.env.enclBB;
        }
        trapExpr.expr.accept(this);
        if (this.env.trapBB.terminator != null) {
            // Once trap expression is visited,  we need to back track all basic blocks which is covered by the trap 
            // and add error entry for each and every basic block.
            genIntermediateErrorEntries(this.env.trapBB);
        } else {
            // Create new block for instructions after trap.
            this.env.enclFunc.errorTable.add(new BIRNode.BIRErrorEntry(this.env.trapBB, this.env.targetOperand));
            this.env.trapBB = new BIRBasicBlock(this.env.nextBBId(names));
            env.enclFunc.basicBlocks.add(this.env.trapBB);
            this.env.enclBB.terminator = new BIRTerminator.GOTO(trapExpr.pos, this.env.trapBB);
            this.env.enclBB = this.env.trapBB;
        }
    }

    @Override
    public void visit(BLangWaitExpr waitExpr) {
        createWait(waitExpr);
    }

    @Override
    public void visit(BLangIsAssignableExpr assignableExpr) {
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(symTable.booleanType, this.env.nextLocalVarId(names),
                VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);

        assignableExpr.lhsExpr.accept(this);
        BIROperand exprIndex = this.env.targetOperand;

        emit(new BIRNonTerminator.TypeTest(assignableExpr.pos, assignableExpr.targetType, toVarRef, exprIndex));
        this.env.targetOperand = toVarRef;
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {
        BIRVariableDcl tempVarDcl =
                new BIRVariableDcl(xmlQName.type, this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);

        // If the QName is use outside of XML, treat it as string.
        if (!xmlQName.isUsedInXML) {
            String qName = xmlQName.namespaceURI == null ? xmlQName.localname.value
                    : ("{" + xmlQName.namespaceURI + "}" + xmlQName.localname);
            generateStringLiteral(qName);
            return;
        }

        // Else, treat it as QName
        BIROperand nsURIIndex = generateStringLiteral(xmlQName.namespaceURI);
        BIROperand localnameIndex = generateStringLiteral(xmlQName.localname.value);
        BIROperand prefixIndex = generateStringLiteral(xmlQName.prefix.value);
        BIRNonTerminator.NewXMLQName newXMLQName =
                new BIRNonTerminator.NewXMLQName(xmlQName.pos, toVarRef, localnameIndex, nsURIIndex, prefixIndex);
        emit(newXMLQName);
        this.env.targetOperand = toVarRef;
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(xmlElementLiteral.type, this.env.nextLocalVarId(names),
                VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);

        // Visit in-line namespace declarations. These needs to be visited first before visiting the
        // attributes, start and end tag names of the element.
        xmlElementLiteral.inlineNamespaces.forEach(xmlns -> {
            xmlns.accept(this);
        });

        // Create start tag name
        BLangExpression startTagName = (BLangExpression) xmlElementLiteral.getStartTagName();
        startTagName.accept(this);
        BIROperand startTagNameIndex = this.env.targetOperand;

        // Create end tag name. If there is no end-tag name (self closing tag),
        // then consider start tag name as the end tag name too.
        BIROperand endTagNameIndex;
        BLangExpression endTagName = (BLangExpression) xmlElementLiteral.getEndTagName();
        if (endTagName == null) {
            endTagNameIndex = startTagNameIndex;
        } else {
            endTagName.accept(this);
            endTagNameIndex = this.env.targetOperand;
        }

        // Create default namespace uri
        BIROperand defaultNsURIVarRef = generateNamespaceRef(xmlElementLiteral.defaultNsSymbol, xmlElementLiteral.pos);

        // Create xml element
        BIRNonTerminator.NewXMLElement newXMLElement = new BIRNonTerminator.NewXMLElement(xmlElementLiteral.pos,
                toVarRef, startTagNameIndex, endTagNameIndex, defaultNsURIVarRef);
        emit(newXMLElement);

        // Populate the XML by adding namespace declarations, attributes and children
        populateXML(xmlElementLiteral, toVarRef);
        this.env.targetOperand = toVarRef;
    }

    @Override
    public void visit(BLangXMLAttribute attribute) {
        BIROperand xmlVarRef = this.env.targetOperand;

        attribute.name.accept(this);
        BIROperand attrNameOp = this.env.targetOperand;

        attribute.value.accept(this);
        BIROperand attrValueOp = this.env.targetOperand;
        emit(new BIRNonTerminator.FieldAccess(attribute.pos, InstructionKind.XML_ATTRIBUTE_STORE, xmlVarRef, attrNameOp,
                attrValueOp));
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(xmlTextLiteral.type, this.env.nextLocalVarId(names),
                VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);

        xmlTextLiteral.concatExpr.accept(this);
        BIROperand xmlTextIndex = this.env.targetOperand;

        BIRNonTerminator.NewXMLText newXMLElement =
                new BIRNonTerminator.NewXMLText(xmlTextLiteral.pos, toVarRef, xmlTextIndex);
        emit(newXMLElement);
        this.env.targetOperand = toVarRef;
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(xmlCommentLiteral.type, this.env.nextLocalVarId(names),
                VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);

        xmlCommentLiteral.concatExpr.accept(this);
        BIROperand xmlCommentIndex = this.env.targetOperand;

        BIRNonTerminator.NewXMLComment newXMLComment =
                new BIRNonTerminator.NewXMLComment(xmlCommentLiteral.pos, toVarRef, xmlCommentIndex);
        emit(newXMLComment);
        this.env.targetOperand = toVarRef;
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(xmlProcInsLiteral.type, this.env.nextLocalVarId(names),
                VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);

        xmlProcInsLiteral.dataConcatExpr.accept(this);
        BIROperand dataIndex = this.env.targetOperand;

        xmlProcInsLiteral.target.accept(this);
        BIROperand targetIndex = this.env.targetOperand;

        BIRNonTerminator.NewXMLProcIns newXMLProcIns =
                new BIRNonTerminator.NewXMLProcIns(xmlProcInsLiteral.pos, toVarRef, dataIndex, targetIndex);
        emit(newXMLProcIns);
        this.env.targetOperand = toVarRef;
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {
        xmlQuotedString.concatExpr.accept(this);
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        xmlnsStmtNode.xmlnsDecl.accept(this);
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        // do nothing
    }

    @Override
    public void visit(BLangLocalXMLNS xmlnsNode) {
        generateXMLNamespace(xmlnsNode);
    }

    @Override
    public void visit(BLangPackageXMLNS xmlnsNode) {
        generateXMLNamespace(xmlnsNode);
    }

    @Override
    public void visit(BLangXMLAccessExpr xmlAccessExpr) {
        generateMappingAccess(xmlAccessExpr);
    }

    @Override
    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        if (xmlAttributeAccessExpr.indexExpr != null) {
            generateMappingAccess(xmlAttributeAccessExpr);
            return;
        }

        // This is getting xml attributes as a map. i.e.: x@
        // Model as a conversion where source type is xml, and target type is map<string>.
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(symTable.mapStringType, this.env.nextLocalVarId(names),
                VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);

        xmlAttributeAccessExpr.expr.accept(this);
        BIROperand xmlVarOp = this.env.targetOperand;
        emit(new BIRNonTerminator.TypeCast(xmlAttributeAccessExpr.pos, toVarRef, xmlVarOp));
        this.env.targetOperand = toVarRef;
    }

    @Override
    public void visit(BLangTypedescExpr accessExpr) {
        BIRVariableDcl tempVarDcl =
                new BIRVariableDcl(accessExpr.type, this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);
        emit(new BIRNonTerminator.NewTypeDesc(accessExpr.pos, toVarRef, accessExpr.resolvedType));
        this.env.targetOperand = toVarRef;
    }

    @Override
    public void visit(BLangBreak breakStmt) {
        this.env.enclBB.terminator = new BIRTerminator.GOTO(breakStmt.pos, this.env.enclLoopEndBB);
    }

    @Override
    public void visit(BLangContinue continueStmt) {
        this.env.enclBB.terminator = new BIRTerminator.GOTO(continueStmt.pos, this.env.enclLoopBB);
    }

    // private methods

    private void genIntermediateErrorEntries(BIRBasicBlock thenBB) {
        if (thenBB != this.env.enclBB) {
            this.env.enclFunc.errorTable.add(new BIRNode.BIRErrorEntry(thenBB, this.env.targetOperand));
            this.env.trapBB = ((BIRTerminator.Call) thenBB.terminator).thenBB;
            genIntermediateErrorEntries(this.env.trapBB);
        }
    }

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
            case AND:
                return InstructionKind.AND;
            case OR:
                return InstructionKind.OR;
            default:
                throw new IllegalStateException("unsupported binary operation: " + opKind.value());
        }
    }

    private InstructionKind getUnaryInstructionKind(OperatorKind opKind) {
        switch (opKind) {
            case TYPEOF:
                return InstructionKind.TYPEOF;
            case NOT:
                return InstructionKind.NOT;
            default:
                throw new IllegalStateException("unsupported unary operator: " + opKind.value());
        }
    }

    private void generateMappingLiteral(BLangRecordLiteral mappingLiteralExpr) {
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(mappingLiteralExpr.type, this.env.nextLocalVarId(names),
                VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);
        emit(new BIRNonTerminator.NewStructure(mappingLiteralExpr.pos, mappingLiteralExpr.type, toVarRef));
        this.env.targetOperand = toVarRef;

        // Handle Map init stuff
        for (BLangRecordKeyValue keyValue : mappingLiteralExpr.keyValuePairs) {
            BLangExpression keyExpr = keyValue.key.expr;
            keyExpr.accept(this);
            BIROperand keyRegIndex = this.env.targetOperand;

            BLangExpression valueExpr = keyValue.valueExpr;
            valueExpr.accept(this);
            BIROperand rhsOp = this.env.targetOperand;

            emit(new BIRNonTerminator.FieldAccess(mappingLiteralExpr.pos, InstructionKind.MAP_STORE, toVarRef,
                    keyRegIndex, rhsOp));
        }

        this.env.targetOperand = toVarRef;
    }

    private void generateArrayLiteral(BLangArrayLiteral astArrayLiteralExpr) {
        // Emit create array instruction
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(astArrayLiteralExpr.type, this.env.nextLocalVarId(names),
                VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);

        long size = astArrayLiteralExpr.type.tag == TypeTags.ARRAY &&
                ((BArrayType) astArrayLiteralExpr.type).state != BArrayState.UNSEALED
                        ? (long) ((BArrayType) astArrayLiteralExpr.type).size
                        : -1L;

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

            emit(new BIRNonTerminator.FieldAccess(astArrayLiteralExpr.pos, InstructionKind.ARRAY_STORE, toVarRef,
                    arrayIndex, exprIndex));
        }
        this.env.targetOperand = toVarRef;
    }

    private void generateTableLiteral(BLangTableLiteral tableLiteral) {
        BIRVariableDcl tempVarDcl = new BIRVariableDcl(tableLiteral.type, this.env.nextLocalVarId(names),
                                                       VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempVarDcl);
        BIROperand toVarRef = new BIROperand(tempVarDcl);

        BLangArrayLiteral columnLiteral = new BLangArrayLiteral();
        columnLiteral.pos = tableLiteral.pos;
        columnLiteral.type = symTable.stringArrayType;
        columnLiteral.exprs = new ArrayList<>();
        tableLiteral.columns.forEach(col -> {
            BLangLiteral colLiteral = new BLangLiteral();
            colLiteral.pos = tableLiteral.pos;
            colLiteral.type = symTable.stringType;
            colLiteral.value = col.columnName;
            columnLiteral.exprs.add(colLiteral);
        });
        columnLiteral.accept(this);
        BIROperand columnsOp = this.env.targetOperand;

        BLangArrayLiteral dataLiteral = new BLangArrayLiteral();
        dataLiteral.pos = tableLiteral.pos;
        dataLiteral.type = symTable.anydataArrayType;
        dataLiteral.exprs = new ArrayList<>(tableLiteral.tableDataRows);
        dataLiteral.accept(this);
        BIROperand dataOp = this.env.targetOperand;

        tableLiteral.indexColumnsArrayLiteral.accept(this);
        BIROperand indexColOp = this.env.targetOperand;

        tableLiteral.keyColumnsArrayLiteral.accept(this);
        BIROperand keyColOp = this.env.targetOperand;

        emit(new BIRNonTerminator.NewTable(tableLiteral.pos, tableLiteral.type, toVarRef, columnsOp, dataOp,
                indexColOp, keyColOp));

        this.env.targetOperand = toVarRef;
    }

    private void generateArrayAccess(BLangIndexBasedAccess astArrayAccessExpr) {
        boolean variableStore = this.varAssignment;
        this.varAssignment = false;

        if (variableStore) {
            BIROperand rhsOp = this.env.targetOperand;

            astArrayAccessExpr.expr.accept(this);
            BIROperand varRefRegIndex = this.env.targetOperand;

            astArrayAccessExpr.indexExpr.accept(this);
            BIROperand keyRegIndex = this.env.targetOperand;

            emit(new BIRNonTerminator.FieldAccess(astArrayAccessExpr.pos, InstructionKind.ARRAY_STORE, varRefRegIndex,
                    keyRegIndex, rhsOp));
        } else {
            BIRVariableDcl tempVarDcl = new BIRVariableDcl(astArrayAccessExpr.type, this.env.nextLocalVarId(names),
                    VarScope.FUNCTION, VarKind.TEMP);
            this.env.enclFunc.localVars.add(tempVarDcl);
            BIROperand tempVarRef = new BIROperand(tempVarDcl);

            astArrayAccessExpr.expr.accept(this);
            BIROperand varRefRegIndex = this.env.targetOperand;

            astArrayAccessExpr.indexExpr.accept(this);
            BIROperand keyRegIndex = this.env.targetOperand;

            emit(new BIRNonTerminator.FieldAccess(astArrayAccessExpr.pos, InstructionKind.ARRAY_LOAD, tempVarRef,
                    keyRegIndex, varRefRegIndex));
            this.env.targetOperand = tempVarRef;
        }

        this.varAssignment = variableStore;
    }

    private void generateMappingAccess(BLangIndexBasedAccess astIndexBasedAccessExpr) {
        boolean variableStore = this.varAssignment;
        this.varAssignment = false;
        InstructionKind insKind;
        if (variableStore) {
            BIROperand rhsOp = this.env.targetOperand;

            astIndexBasedAccessExpr.expr.accept(this);
            BIROperand varRefRegIndex = this.env.targetOperand;

            astIndexBasedAccessExpr.indexExpr.accept(this);
            BIROperand keyRegIndex = this.env.targetOperand;

            if (astIndexBasedAccessExpr.getKind() == NodeKind.XML_ATTRIBUTE_ACCESS_EXPR) {
                insKind = InstructionKind.XML_ATTRIBUTE_STORE;
                keyRegIndex = getQNameOP(astIndexBasedAccessExpr.indexExpr, keyRegIndex);
            } else if (astIndexBasedAccessExpr.expr.type.tag == TypeTags.OBJECT) {
                insKind = InstructionKind.OBJECT_STORE;
            } else {
                insKind = InstructionKind.MAP_STORE;
            }
            emit(new BIRNonTerminator.FieldAccess(astIndexBasedAccessExpr.pos, insKind, varRefRegIndex, keyRegIndex,
                    rhsOp));
        } else {
            BIRVariableDcl tempVarDcl = new BIRVariableDcl(astIndexBasedAccessExpr.type, this.env.nextLocalVarId(names),
                    VarScope.FUNCTION, VarKind.TEMP);
            this.env.enclFunc.localVars.add(tempVarDcl);
            BIROperand tempVarRef = new BIROperand(tempVarDcl);

            astIndexBasedAccessExpr.expr.accept(this);
            BIROperand varRefRegIndex = this.env.targetOperand;

            astIndexBasedAccessExpr.indexExpr.accept(this);
            BIROperand keyRegIndex = this.env.targetOperand;

            if (astIndexBasedAccessExpr.getKind() == NodeKind.XML_ATTRIBUTE_ACCESS_EXPR) {
                insKind = InstructionKind.XML_ATTRIBUTE_LOAD;
                keyRegIndex = getQNameOP(astIndexBasedAccessExpr.indexExpr, keyRegIndex);
            } else if (astIndexBasedAccessExpr.expr.type.tag == TypeTags.XML) {
                generateXMLAccess((BLangXMLAccessExpr) astIndexBasedAccessExpr, tempVarRef, varRefRegIndex,
                        keyRegIndex);
                this.varAssignment = variableStore;
                return;
            } else if (astIndexBasedAccessExpr.expr.type.tag == TypeTags.OBJECT) {
                insKind = InstructionKind.OBJECT_LOAD;
            } else {
                insKind = InstructionKind.MAP_LOAD;
            }
            emit(new BIRNonTerminator.FieldAccess(astIndexBasedAccessExpr.pos, insKind, tempVarRef, keyRegIndex,
                    varRefRegIndex));
            this.env.targetOperand = tempVarRef;
        }
        this.varAssignment = variableStore;
    }

    private BTypeSymbol getObjectTypeSymbol(BType type) {
        if (type.tag == TypeTags.UNION) {
            return ((BUnionType) type).getMemberTypes().stream()
                    .filter(t -> t.tag == TypeTags.OBJECT)
                    .findFirst()
                    .orElse(symTable.noType).tsymbol;
        }
        return type.tsymbol;
    }

    private BIROperand generateStringLiteral(String value) {
        BLangLiteral prefixLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        prefixLiteral.value = value;

        if (value == null) {
            prefixLiteral.type = symTable.nilType;
        } else {
            prefixLiteral.type = symTable.stringType;
        }

        prefixLiteral.accept(this);
        return this.env.targetOperand;
    }

    private void generateXMLNamespace(BLangXMLNS xmlnsNode) {
        BIRVariableDcl birVarDcl = new BIRVariableDcl(xmlnsNode.pos, symTable.stringType,
                this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.LOCAL);
        this.env.enclFunc.localVars.add(birVarDcl);
        this.env.symbolVarMap.put(xmlnsNode.symbol, birVarDcl);

        // Visit the namespace uri expression.
        xmlnsNode.namespaceURI.accept(this);

        // Create a variable reference and
        BIROperand varRef = new BIROperand(birVarDcl);
        emit(new Move(xmlnsNode.pos, this.env.targetOperand, varRef));
    }

    private BIROperand generateNamespaceRef(BXMLNSSymbol nsSymbol, DiagnosticPos pos) {
        if (nsSymbol == null) {
            return generateStringLiteral(null);
        }

        // global-level, object-level, record-level namespace declarations will not have 
        // any interpolated content. hence the namespace URI is statically known.
        int ownerTag = nsSymbol.owner.tag;
        if ((ownerTag & SymTag.PACKAGE) == SymTag.PACKAGE ||
                (ownerTag & SymTag.OBJECT) == SymTag.OBJECT ||
                (ownerTag & SymTag.RECORD) == SymTag.RECORD) {
            return generateStringLiteral(nsSymbol.namespaceURI);
        }

        BIRVariableDcl nsURIVarDcl = new BIRVariableDcl(symTable.stringType, this.env.nextLocalVarId(names),
                VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(nsURIVarDcl);
        BIROperand nsURIVarRef = new BIROperand(nsURIVarDcl);

        BIRVariableDcl varDecl = this.env.symbolVarMap.get(nsSymbol);
        BIROperand fromVarRef = new BIROperand(varDecl);
        emit(new Move(pos, fromVarRef, nsURIVarRef));
        return nsURIVarRef;
    }

    private void populateXML(BLangXMLElementLiteral xmlElementLiteral, BIROperand toVarRef) {
        // Add namespaces decelerations visible to this element.
        xmlElementLiteral.namespacesInScope.forEach((name, symbol) -> {
            BLangXMLQName nsQName = new BLangXMLQName(name.getValue(), XMLConstants.XMLNS_ATTRIBUTE);
            nsQName.type = symTable.stringType;
            nsQName.accept(this);
            BIROperand nsQNameIndex = this.env.targetOperand;
            BIROperand nsURIIndex = generateNamespaceRef(symbol, xmlElementLiteral.pos);
            emit(new BIRNonTerminator.FieldAccess(xmlElementLiteral.pos, InstructionKind.XML_ATTRIBUTE_STORE, toVarRef,
                    nsQNameIndex, nsURIIndex));
        });

        // Add attributes
        xmlElementLiteral.attributes.forEach(attribute -> {
            this.env.targetOperand = toVarRef;
            attribute.accept(this);
        });

        // Add children
        xmlElementLiteral.modifiedChildren.forEach(child -> {
            child.accept(this);
            BIROperand childOp = this.env.targetOperand;
            emit(new BIRNonTerminator.XMLAccess(child.pos, InstructionKind.XML_SEQ_STORE, toVarRef, childOp));
        });
    }

    private BIROperand getQNameOP(BLangExpression qnameExpr, BIROperand keyRegIndex) {
        if (qnameExpr.getKind() == NodeKind.XML_QNAME) {
            return keyRegIndex;
        }

        BIRVariableDcl tempQNameVarDcl = new BIRVariableDcl(qnameExpr.type,
                this.env.nextLocalVarId(names), VarScope.FUNCTION, VarKind.TEMP);
        this.env.enclFunc.localVars.add(tempQNameVarDcl);
        BIROperand qnameVarRef = new BIROperand(tempQNameVarDcl);
        emit(new BIRNonTerminator.NewStringXMLQName(qnameExpr.pos, qnameVarRef, keyRegIndex));
        return qnameVarRef;
    }

    private void generateXMLAccess(BLangXMLAccessExpr xmlAccessExpr, BIROperand tempVarRef,
                                   BIROperand varRefRegIndex, BIROperand keyRegIndex) {
        this.env.targetOperand = tempVarRef;
        InstructionKind insKind;
        if (xmlAccessExpr.fieldType == FieldKind.ALL) {
            emit(new BIRNonTerminator.XMLAccess(xmlAccessExpr.pos, InstructionKind.XML_LOAD_ALL, tempVarRef,
                    varRefRegIndex));
            return;
        } else if (xmlAccessExpr.indexExpr.type.tag == TypeTags.STRING) {
            insKind = InstructionKind.XML_LOAD;
        } else {
            insKind = InstructionKind.XML_SEQ_LOAD;
        }

        emit(new BIRNonTerminator.FieldAccess(xmlAccessExpr.pos, insKind, tempVarRef, keyRegIndex, varRefRegIndex));
    }
}
