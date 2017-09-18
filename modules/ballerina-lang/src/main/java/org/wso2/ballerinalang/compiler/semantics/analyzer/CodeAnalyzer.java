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

import org.ballerinalang.model.tree.expressions.LiteralNode;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotAttribute;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangEnum;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackageDeclaration;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttributeValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeCastExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLanXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAbort;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangComment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReply;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BlangTransform;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticLog;

/**
 * This represents the code analyzing pass of semantic analysis. 
 * 
 * The following validations are done here:-
 * 
 * (*) Loop continuation statement validation.
 * (*) Function return path existence and unreachable code validation.
 * (*) Dead code detection.
 * (*) Worker send/receive validation.
 */
public class CodeAnalyzer extends BLangNodeVisitor {
    
    private static final CompilerContext.Key<CodeAnalyzer> CODE_ANALYZER_KEY =
            new CompilerContext.Key<>();
    
    private int loopCount;
    private boolean statementReturns;
    private boolean deadCode;
    private boolean unreachableCodeCheckDone;
    private DiagnosticLog dlog;
    private SymbolResolver symResolver;
    private SymbolTable symTable;

    public static CodeAnalyzer getInstance(CompilerContext context) {
        CodeAnalyzer codeGenerator = context.get(CODE_ANALYZER_KEY);
        if (codeGenerator == null) {
            codeGenerator = new CodeAnalyzer(context);
        }
        return codeGenerator;
    }

    public CodeAnalyzer(CompilerContext context) {
        context.put(CODE_ANALYZER_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.dlog = DiagnosticLog.getInstance(context);
    }
    
    private void resetPackage() {
        this.deadCode = false;
    }
    
    private void resetFunction() {
        this.unreachableCodeCheckDone = false;
        this.resetStatementReturns();
    }
    
    private void resetStatementReturns() {
        this.statementReturns = false;
    }
    
    public BLangPackage analyze(BLangPackage pkgNode) {
        this.resetPackage();
        pkgNode.accept(this);
        return pkgNode;
    }
    
    @Override
    public void visit(BLangPackage pkgNode) {
        pkgNode.compUnits.forEach(e -> e.accept(this));
    }
    
    @Override
    public void visit(BLangCompilationUnit compUnitNode) {
        compUnitNode.topLevelNodes.forEach(e -> ((BLangNode) e).accept(this));
    }
    
    @Override
    public void visit(BLangFunction funcNode) {
        this.resetFunction();
        boolean functionReturns = funcNode.retParams.size() > 0;
        funcNode.body.accept(this);
        /* the function returns, but none of the statements surely returns */
        if (functionReturns && !this.statementReturns) {
            this.dlog.error(funcNode.pos, DiagnosticCode.FUNCTION_MUST_RETURN, 
                    String.valueOf(funcNode.getReturnParameters()));
        }
        funcNode.workers.forEach(e -> e.accept(this));
    }
    
    @Override
    public void visit(BLangForkJoin forkJoin) {
        forkJoin.workers.forEach(e -> e.accept(this));
    }
    
    @Override
    public void visit(BLangWorker worker) {
        worker.body.accept(this);
    }
    
    private void checkUnreachableCode(BLangStatement stmt) {
        if (this.statementReturns && !this.unreachableCodeCheckDone) {
            this.dlog.error(stmt.pos, DiagnosticCode.UNREACHABLE_CODE);
            /* to make sure we don't give the same error again to following statements */
            this.unreachableCodeCheckDone = true;
        }
    }
    
    private void checkDeadCode(BLangStatement stmt) {
        if (this.deadCode) {
            this.dlog.warning(stmt.pos, DiagnosticCode.DEAD_CODE);
        }
    }
    
    private void checkStatementExecutionValidity(BLangStatement stmt) {
        this.checkUnreachableCode(stmt);
        this.checkDeadCode(stmt);
    }
    
    @Override
    public void visit(BLangBlockStmt blockNode) {
        blockNode.statements.forEach(e -> {
            e.accept(this);
        });
    }
    
    @Override
    public void visit(BLangReturn returnStmt) {
        this.statementReturns = true;
    }
    
    @Override
    public void visit(BLangIf ifStmt) {
        boolean originalDeadCode = this.deadCode;
        this.checkStatementExecutionValidity(ifStmt);
        boolean ifTrue = this.isBooleanTrue(ifStmt.expr);
        boolean ifFalse = this.isBooleanFalse(ifStmt.expr);
        boolean extendedDeadCode = false;
        if (ifFalse) {
            this.deadCode = true;
        }
        ifStmt.body.accept(this);
        if (ifTrue && this.statementReturns) {
            extendedDeadCode = true;
        }
        if (ifStmt.elseStmt != null) {
            if (ifTrue) {
                this.deadCode = true;
            }
            boolean ifStatementReturns = this.statementReturns;
            this.resetStatementReturns();
            ifStmt.elseStmt.accept(this);
            if (ifFalse && this.statementReturns) {
                extendedDeadCode = true;
            }
            this.statementReturns = ifStatementReturns && this.statementReturns;
            /* if the whole if/else returns for sure, the following is not dead code,
             * but rather unreachable code */
            if (this.statementReturns) {
                extendedDeadCode = false;
            }
        }
        /* this is the case where if (true) return or if (false) else return,
         * where after the if/else, the following statements are also dead code */
        if (extendedDeadCode) {
            this.deadCode = true;
        } else {
            this.deadCode = originalDeadCode;
        }
    }
    
    private boolean isBooleanTrue(BLangExpression expr) {
        return (expr instanceof LiteralNode) && ((LiteralNode) expr).getValue().equals(Boolean.TRUE);
    }
    
    private boolean isBooleanFalse(BLangExpression expr) {
        return (expr instanceof LiteralNode) && ((LiteralNode) expr).getValue().equals(Boolean.FALSE);
    }
        
    @Override
    public void visit(BLangWhile whileNode) {
        this.checkStatementExecutionValidity(whileNode);
        this.loopCount++;
        whileNode.body.statements.forEach(e -> e.accept(this));
        this.loopCount--;
    }
    
    @Override
    public void visit(BLangContinue continueNode) {
        if (this.loopCount == 0) {
            this.dlog.error(continueNode.pos, DiagnosticCode.NEXT_CANNOT_BE_OUTSIDE_LOOP);
        }
    }
    
    private boolean workerExists(SymbolEnv env, String workerName) {
        BSymbol symbol = this.symResolver.lookupSymbol(env, new Name(workerName), SymTag.WORKER);
        return (symbol != this.symTable.notFoundSymbol);
    }
    
    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        String workerName = workerSendNode.workerIdentifier.getValue();
        if (!this.workerExists(workerSendNode.env, workerName)) {
            this.dlog.error(workerSendNode.pos, DiagnosticCode.UNDEFINED_WORKER, workerName);
        }
    }
    
    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        String workerName = workerReceiveNode.workerIdentifier.getValue();
        if (!this.workerExists(workerReceiveNode.env, workerName)) {
            this.dlog.error(workerReceiveNode.pos, DiagnosticCode.UNDEFINED_WORKER, workerName);
        }
    }
    
    public void visit(BLangPackageDeclaration pkgDclNode) {
        /* ignore */
    }

    public void visit(BLangImportPackage importPkgNode) {
        /* ignore */
    }

    public void visit(BLangXMLNS xmlnsNode) {
        /* ignore */
    }

    public void visit(BLangService serviceNode) {
        /* ignore */
    }

    public void visit(BLangResource resourceNode) {
        /* ignore */
    }

    public void visit(BLangConnector connectorNode) {
        /* ignore */
    }

    public void visit(BLangAction actionNode) {
        /* ignore */
    }

    public void visit(BLangStruct structNode) {
        /* ignore */
    }

    public void visit(BLangEnum enumNode) {
        /* ignore */
    }

    public void visit(BLangVariable varNode) {
        /* ignore */
    }

    public void visit(BLangIdentifier identifierNode) {
        /* ignore */
    }

    public void visit(BLangAnnotation annotationNode) {
        /* ignore */
    }

    public void visit(BLangAnnotAttribute annotationAttribute) {
        /* ignore */
    }

    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        /* ignore */
    }

    public void visit(BLangAnnotAttachmentAttributeValue annotAttributeValue) {
        /* ignore */
    }

    public void visit(BLangAnnotAttachmentAttribute annotAttachmentAttribute) {
        /* ignore */
    }

    public void visit(BLangVariableDef varDefNode) {
        this.checkStatementExecutionValidity(varDefNode);
    }

    public void visit(BLangAssignment assignNode) {
        this.checkStatementExecutionValidity(assignNode);
    }

    public void visit(BLangAbort abortNode) {
        /* ignore */
    }

    public void visit(BLangBreak breakNode) {
        /* ignore */
    }

    public void visit(BLangReply replyNode) {
        /* ignore */
    }

    public void visit(BLangThrow throwNode) {
        /* ignore */
    }

    public void visit(BLanXMLNSStatement xmlnsStmtNode) {
        this.checkStatementExecutionValidity(xmlnsStmtNode);
    }

    public void visit(BLangExpressionStmt exprStmtNode) {
        this.checkStatementExecutionValidity(exprStmtNode);
    }

    public void visit(BLangComment commentNode) {
        /* ignore */
    }

    public void visit(BLangTransaction transactionNode) {
        this.checkStatementExecutionValidity(transactionNode);
    }

    public void visit(BlangTransform transformNode) {
        this.checkStatementExecutionValidity(transformNode);
    }

    public void visit(BLangTryCatchFinally tryNode) {
        /* ignore */
    }

    public void visit(BLangCatch catchNode) {
        /* ignore */
    }

    public void visit(BLangLiteral literalExpr) {
        /* ignore */
    }

    public void visit(BLangArrayLiteral arrayLiteral) {
        /* ignore */
    }

    public void visit(BLangRecordLiteral recordLiteral) {
        /* ignore */
    }

    public void visit(BLangSimpleVarRef varRefExpr) {
        /* ignore */
    }

    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        /* ignore */
    }

    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        /* ignore */
    }

    public void visit(BLangInvocation invocationExpr) {
        /* ignore */
    }

    public void visit(BLangTernaryExpr ternaryExpr) {
        /* ignore */
    }

    public void visit(BLangBinaryExpr binaryExpr) {
        /* ignore */
    }

    public void visit(BLangUnaryExpr unaryExpr) {
        /* ignore */
    }

    public void visit(BLangTypeCastExpr castExpr) {
        /* ignore */
    }

    public void visit(BLangTypeConversionExpr conversionExpr) {
        /* ignore */
    }

    public void visit(BLangXMLQName xmlQName) {
        /* ignore */
    }

    public void visit(BLangXMLAttribute xmlAttribute) {
        /* ignore */
    }

    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        /* ignore */
    }

    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        /* ignore */
    }

    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        /* ignore */
    }

    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        /* ignore */
    }

    public void visit(BLangXMLQuotedString xmlQuotedString) {
        /* ignore */
    }

    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        /* ignore */
    }

    // Type nodes

    public void visit(BLangValueType valueType) {
        /* ignore */
    }

    public void visit(BLangArrayType arrayType) {
        /* ignore */
    }

    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
        /* ignore */
    }

    public void visit(BLangConstrainedType constrainedType) {
        /* ignore */
    }

    public void visit(BLangUserDefinedType userDefinedType) {
        /* ignore */
    }
    
}
