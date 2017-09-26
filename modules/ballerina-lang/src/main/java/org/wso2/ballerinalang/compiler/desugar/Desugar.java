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
package org.wso2.ballerinalang.compiler.desugar;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.tree.NodeKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConnectorInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess.BLangStructFieldAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangArrayAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangMapAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BFunctionPointerInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangJSONLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangMapLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangStructLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangFieldVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangFunctionVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangLocalVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangPackageVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeCastExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransform;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 0.94
 */
public class Desugar extends BLangNodeVisitor {

    private static final CompilerContext.Key<Desugar> DESUGAR_KEY =
            new CompilerContext.Key<>();

    private SymbolTable symTable;
    private SymbolEnter symbolEnter;

    private BLangNode result;

    public static Desugar getInstance(CompilerContext context) {
        Desugar desugar = context.get(DESUGAR_KEY);
        if (desugar == null) {
            desugar = new Desugar(context);
        }

        return desugar;
    }

    private Desugar(CompilerContext context) {
        context.put(DESUGAR_KEY, this);

        this.symTable = SymbolTable.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
    }

    public BLangPackage perform(BLangPackage pkgNode) {
        return rewrite(pkgNode);
    }

    // visitors

    @Override
    public void visit(BLangPackage pkgNode) {
        if (pkgNode.phase != CompilerPhase.DESUGAR) {
            result = pkgNode;
            return;
        }
        pkgNode.imports = rewrite(pkgNode.imports);
        pkgNode.globalVars = rewrite(pkgNode.globalVars);
        pkgNode.functions = rewrite(pkgNode.functions);
        pkgNode.connectors = rewrite(pkgNode.connectors);
        pkgNode.services = rewrite(pkgNode.services);
        result = pkgNode;
        pkgNode.phase = CompilerPhase.CODE_GEN;
    }

    @Override
    public void visit(BLangImportPackage importPkgNode) {
        BPackageSymbol pkgSymbol = importPkgNode.symbol;
        SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgSymbol);
        rewrite(pkgEnv.node);
        result = importPkgNode;
    }

    @Override
    public void visit(BLangFunction funcNode) {
        funcNode.body = rewrite(funcNode.body);
        funcNode.workers = rewrite(funcNode.workers);
        result = funcNode;
    }

    @Override
    public void visit(BLangService serviceNode) {
        serviceNode.resources = rewrite(serviceNode.resources);
        serviceNode.vars = rewrite(serviceNode.vars);
        result = serviceNode;
    }

    @Override
    public void visit(BLangResource resourceNode) {
        resourceNode.body = rewrite(resourceNode.body);
        resourceNode.workers = rewrite(resourceNode.workers);
        result = resourceNode;
    }

    @Override
    public void visit(BLangConnector connectorNode) {
        connectorNode.params = rewrite(connectorNode.params);
        connectorNode.actions = rewrite(connectorNode.actions);
        connectorNode.varDefs = rewrite(connectorNode.varDefs);
        connectorNode.initFunction = rewrite(connectorNode.initFunction);
        result = connectorNode;
    }

    @Override
    public void visit(BLangAction actionNode) {
        actionNode.params = rewrite(actionNode.params);
        actionNode.body = rewrite(actionNode.body);
        actionNode.workers = rewrite(actionNode.workers);
        result = actionNode;
    }

    @Override
    public void visit(BLangWorker workerNode) {
        workerNode.body = rewrite(workerNode.body);
        result = workerNode;
    }

    @Override
    public void visit(BLangVariable varNode) {
        varNode.expr = rewriteExpr(varNode.expr);
        result = varNode;
    }


    // Statements

    @Override
    public void visit(BLangBlockStmt block) {
        block.stmts = rewrite(block.stmts);
        result = block;
    }

    @Override
    public void visit(BLangVariableDef varDefNode) {
        varDefNode.var = rewrite(varDefNode.var);
        result = varDefNode;
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        assignNode.varRefs = rewriteExprs(assignNode.varRefs);
        assignNode.expr = rewriteExpr(assignNode.expr);
        result = assignNode;
    }

    @Override
    public void visit(BLangAbort abortNode) {
        result = abortNode;
    }

    @Override
    public void visit(BLangContinue continueNode) {
        result = continueNode;
    }

    @Override
    public void visit(BLangBreak breakNode) {
        result = breakNode;
    }

    @Override
    public void visit(BLangReturn returnNode) {
        if (returnNode.namedReturnVariables != null) {
            // Handled named returns.
            for (BLangVariable variable : returnNode.namedReturnVariables) {
                BLangSimpleVarRef varRef = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
                varRef.variableName = variable.name;
                varRef.symbol = variable.symbol;
                varRef.type = variable.type;
                varRef.pos = returnNode.pos;
                returnNode.exprs.add(varRef);
            }
        }
        returnNode.exprs = rewriteExprs(returnNode.exprs);
        result = returnNode;
    }

    @Override
    public void visit(BLangReply replyNode) {
        result = replyNode;
    }

    @Override
    public void visit(BLangThrow throwNode) {
        throwNode.expr = rewriteExpr(throwNode.expr);
        result = throwNode;
    }

    @Override
    public void visit(BLanXMLNSStatement xmlnsStmtNode) {
        result = xmlnsStmtNode;
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        exprStmtNode.expr = rewriteExpr(exprStmtNode.expr);
        result = exprStmtNode;
    }

    @Override
    public void visit(BLangComment commentNode) {
        result = commentNode;
    }

    @Override
    public void visit(BLangIf ifNode) {
        ifNode.expr = rewriteExpr(ifNode.expr);
        ifNode.body = rewrite(ifNode.body);
        ifNode.elseStmt = rewrite(ifNode.elseStmt);
        result = ifNode;
    }

    @Override
    public void visit(BLangWhile whileNode) {
        whileNode.expr = rewriteExpr(whileNode.expr);
        whileNode.body = rewrite(whileNode.body);
        result = whileNode;
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        transactionNode.transactionBody = rewrite(transactionNode.transactionBody);
        transactionNode.failedBody = rewrite(transactionNode.failedBody);
        transactionNode.abortedBody = rewrite(transactionNode.abortedBody);
        transactionNode.committedBody = rewrite(transactionNode.committedBody);
        transactionNode.retryCount = rewriteExpr(transactionNode.retryCount);
        result = transactionNode;
    }

    @Override
    public void visit(BLangTransform transformNode) {
        transformNode.body = rewrite(transformNode.body);
        result = transformNode;
    }

    @Override
    public void visit(BLangTryCatchFinally tryNode) {
        tryNode.tryBody = rewrite(tryNode.tryBody);
        tryNode.catchBlocks = rewrite(tryNode.catchBlocks);
        tryNode.finallyBody = rewrite(tryNode.finallyBody);
        result = tryNode;
    }

    @Override
    public void visit(BLangCatch catchNode) {
        catchNode.body = rewrite(catchNode.body);
        result = catchNode;
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        forkJoin.workers = rewrite(forkJoin.workers);
        result = forkJoin;
    }


    // Expressions

    @Override
    public void visit(BLangLiteral literalExpr) {
        result = literalExpr;
    }

    @Override
    public void visit(BLangArrayLiteral arrayLiteral) {
        arrayLiteral.exprs = rewriteExprs(arrayLiteral.exprs);
        result = arrayLiteral;
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        recordLiteral.keyValuePairs.forEach(keyValue -> {
            BLangExpression keyExpr = keyValue.key.expr;
            if (keyExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                BLangSimpleVarRef varRef = (BLangSimpleVarRef) keyExpr;
                keyValue.key.expr = createStringLiteral(varRef.pos, varRef.variableName.value);
            }

            keyValue.valueExpr = rewriteExpr(keyValue.valueExpr);
        });

        if (recordLiteral.type.tag == TypeTags.STRUCT) {
            result = new BLangStructLiteral(recordLiteral.keyValuePairs, recordLiteral.type);
        } else if (recordLiteral.type.tag == TypeTags.MAP) {
            result = new BLangMapLiteral(recordLiteral.keyValuePairs, recordLiteral.type);
        } else {
            result = new BLangJSONLiteral(recordLiteral.keyValuePairs, recordLiteral.type);
        }
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        BLangSimpleVarRef genVarRefExpr = varRefExpr;
        BSymbol ownerSymbol = varRefExpr.symbol.owner;
        if ((varRefExpr.symbol.tag & SymTag.FUNCTION) == SymTag.FUNCTION &&
                varRefExpr.symbol.type.tag == TypeTags.INVOKABLE) {
            genVarRefExpr = new BLangFunctionVarRef(varRefExpr.symbol);
        } else if ((ownerSymbol.tag & SymTag.INVOKABLE) == SymTag.INVOKABLE) {
            // Local variable in a function/resource/action/worker
            genVarRefExpr = new BLangLocalVarRef(varRefExpr.symbol);
        } else if ((ownerSymbol.tag & SymTag.STRUCT) == SymTag.STRUCT ||
                (ownerSymbol.tag & SymTag.CONNECTOR) == SymTag.CONNECTOR) {
            // Field variable in a struct or a connector
            genVarRefExpr = new BLangFieldVarRef(varRefExpr.symbol);
        } else if ((ownerSymbol.tag & SymTag.PACKAGE) == SymTag.PACKAGE ||
                (ownerSymbol.tag & SymTag.SERVICE) == SymTag.SERVICE) {
            // Package variable | service variable
            // We consider both of them as package level variables
            genVarRefExpr = new BLangPackageVarRef(varRefExpr.symbol);
        }

        genVarRefExpr.type = varRefExpr.type;
        result = genVarRefExpr;
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        BLangVariableReference targetVarRef = fieldAccessExpr;
        fieldAccessExpr.expr = rewrite(fieldAccessExpr.expr);
        BType varRefType = fieldAccessExpr.expr.type;
        if (varRefType.tag == TypeTags.STRUCT) {
            targetVarRef = new BLangStructFieldAccessExpr(fieldAccessExpr.pos,
                    fieldAccessExpr.expr, fieldAccessExpr.symbol);
        } else if (varRefType.tag == TypeTags.MAP) {
            BLangLiteral stringLit = createStringLiteral(fieldAccessExpr.pos, fieldAccessExpr.field.value);
            targetVarRef = new BLangMapAccessExpr(fieldAccessExpr.pos, fieldAccessExpr.expr, stringLit);
        } else if (varRefType.tag == TypeTags.JSON) {
            //TODO
            targetVarRef = fieldAccessExpr;
        }

        targetVarRef.lhsVar = fieldAccessExpr.lhsVar;
        targetVarRef.type = fieldAccessExpr.type;
        result = targetVarRef;
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        BLangVariableReference targetVarRef = indexAccessExpr;
        indexAccessExpr.indexExpr = rewrite(indexAccessExpr.indexExpr);
        indexAccessExpr.expr = rewrite(indexAccessExpr.expr);
        BType varRefType = indexAccessExpr.expr.type;
        if (varRefType.tag == TypeTags.STRUCT) {
            targetVarRef = new BLangStructFieldAccessExpr(indexAccessExpr.pos,
                    indexAccessExpr.expr, indexAccessExpr.symbol);
        } else if (varRefType.tag == TypeTags.MAP) {
            targetVarRef = new BLangMapAccessExpr(indexAccessExpr.pos,
                    indexAccessExpr.expr, indexAccessExpr.indexExpr);
        } else if (varRefType.tag == TypeTags.JSON) {
            // TODO
            targetVarRef = indexAccessExpr;
        } else if (varRefType.tag == TypeTags.ARRAY) {
            targetVarRef = new BLangArrayAccessExpr(indexAccessExpr.pos,
                    indexAccessExpr.expr, indexAccessExpr.indexExpr);
        }

        targetVarRef.lhsVar = indexAccessExpr.lhsVar;
        targetVarRef.type = indexAccessExpr.type;
        result = targetVarRef;
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        BLangInvocation genIExpr = invocationExpr;
        if (invocationExpr.expr == null) {
            invocationExpr.argExprs = rewriteExprs(invocationExpr.argExprs);
            if (invocationExpr.functionPointerInvocation) {
                BLangSimpleVarRef varRef = new BLangSimpleVarRef();
                varRef.symbol = (BVarSymbol) invocationExpr.symbol;
                varRef.type = invocationExpr.symbol.type;
                genIExpr = new BFunctionPointerInvocation(invocationExpr, varRef);
            }
        } else if (invocationExpr.expr instanceof BLangSimpleVarRef) {
            invocationExpr.argExprs = rewriteExprs(invocationExpr.argExprs);
            invocationExpr.expr = rewriteExpr(invocationExpr.expr);
            if (invocationExpr.functionPointerInvocation) {
                BLangSimpleVarRef varRef = new BLangSimpleVarRef();
                varRef.symbol = (BVarSymbol) invocationExpr.symbol;
                varRef.type = invocationExpr.symbol.type;
                genIExpr = new BFunctionPointerInvocation(invocationExpr, varRef);
            }
        }


        genIExpr.impCastExpr = invocationExpr.impCastExpr;
        result = genIExpr;
    }

    public void visit(BLangConnectorInit connectorInitExpr) {
        BLangConnectorInit genCIExpr = connectorInitExpr;
        connectorInitExpr.argsExpr = rewriteExprs(connectorInitExpr.argsExpr);
        genCIExpr.impCastExpr = connectorInitExpr.impCastExpr;
        result = genCIExpr;
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        ternaryExpr.expr = rewriteExpr(ternaryExpr.expr);
        ternaryExpr.thenExpr = rewriteExpr(ternaryExpr.thenExpr);
        ternaryExpr.elseExpr = rewriteExpr(ternaryExpr.elseExpr);
        result = ternaryExpr;
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        binaryExpr.lhsExpr = rewriteExpr(binaryExpr.lhsExpr);
        binaryExpr.rhsExpr = rewriteExpr(binaryExpr.rhsExpr);
        result = binaryExpr;
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        unaryExpr.expr = rewriteExpr(unaryExpr.expr);
        result = unaryExpr;
    }

    @Override
    public void visit(BLangTypeCastExpr castExpr) {
        castExpr.expr = rewriteExpr(castExpr.expr);
        result = castExpr;
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        conversionExpr.expr = rewriteExpr(conversionExpr.expr);
        result = conversionExpr;
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        result = bLangLambdaFunction;
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {
        result = xmlQName;
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {
        result = xmlAttribute;
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        result = xmlElementLiteral;
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        result = xmlTextLiteral;
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        result = xmlCommentLiteral;
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        result = xmlProcInsLiteral;
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {
        result = xmlQuotedString;
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        stringTemplateLiteral.exprs = rewriteExprs(stringTemplateLiteral.exprs);
        result = stringTemplateLiteral;
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        workerSendNode.exprs = rewriteExprs(workerSendNode.exprs);
        result = workerSendNode;
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        workerReceiveNode.exprs = rewriteExprs(workerReceiveNode.exprs);
        result = workerReceiveNode;
    }


    // Generated expressions. Following expressions are not part of the original syntax
    // tree which is coming out of the parser

    @Override
    public void visit(BLangLocalVarRef localVarRef) {
        result = localVarRef;
    }

    @Override
    public void visit(BLangFieldVarRef fieldVarRef) {
        result = fieldVarRef;
    }

    @Override
    public void visit(BLangPackageVarRef packageVarRef) {
        result = packageVarRef;
    }

    @Override
    public void visit(BLangFunctionVarRef functionVarRef) {
        result = functionVarRef;
    }

    @Override
    public void visit(BLangStructFieldAccessExpr fieldAccessExpr) {
        result = fieldAccessExpr;
    }

    @Override
    public void visit(BLangMapAccessExpr mapKeyAccessExpr) {
        result = mapKeyAccessExpr;
    }

    @Override
    public void visit(BLangArrayAccessExpr arrayIndexAccessExpr) {
        result = arrayIndexAccessExpr;
    }

    @Override
    public void visit(BLangJSONLiteral jsonLiteral) {
        result = jsonLiteral;
    }

    @Override
    public void visit(BLangMapLiteral mapLiteral) {
        result = mapLiteral;
    }

    @Override
    public void visit(BLangStructLiteral structLiteral) {
        result = structLiteral;
    }

    @Override
    public void visit(BFunctionPointerInvocation fpInvocation) {
        result = fpInvocation;
    }


    // private functions

    @SuppressWarnings("unchecked")
    private <E extends BLangNode> E rewrite(E node) {
        if (node == null) {
            return null;
        }

        node.accept(this);
        BLangNode resultNode = this.result;
        this.result = null;
        return (E) resultNode;
    }

    @SuppressWarnings("unchecked")
    private <E extends BLangExpression> E rewriteExpr(E node) {
        if (node == null) {
            return null;
        }

        BLangExpression expr = node;
        if (node.impCastExpr != null) {
            expr = node.impCastExpr;
            node.impCastExpr.expr = node;
            node.impCastExpr = null;
        }

        expr.accept(this);
        BLangNode resultNode = this.result;
        this.result = null;
        return (E) resultNode;
    }

    private <E extends BLangNode> List<E> rewrite(List<E> nodeList) {
        for (int i = 0; i < nodeList.size(); i++) {
            nodeList.set(i, rewrite(nodeList.get(i)));
        }
        return nodeList;
    }

    private <E extends BLangExpression> List<E> rewriteExprs(List<E> nodeList) {
        for (int i = 0; i < nodeList.size(); i++) {
            nodeList.set(i, rewriteExpr(nodeList.get(i)));
        }
        return nodeList;
    }

    private BLangLiteral createStringLiteral(DiagnosticPos pos, String value) {
        BLangLiteral stringLit = new BLangLiteral();
        stringLit.pos = pos;
        stringLit.value = value;
        stringLit.type = symTable.stringType;
        return stringLit;
    }
}
