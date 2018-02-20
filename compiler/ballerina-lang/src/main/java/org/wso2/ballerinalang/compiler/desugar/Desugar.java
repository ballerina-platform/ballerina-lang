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
import org.ballerinalang.model.tree.OperatorKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConversionOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLNSSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS.BLangLocalXMLNS;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS.BLangPackageXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral.BLangJSONArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConnectorInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess.BLangEnumeratorAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess.BLangStructFieldAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangArrayAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangJSONAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangMapAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangXMLAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BFunctionPointerInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BLangActionInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BLangAttachedFunctionInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BLangTransformerInvocation;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeofExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttributeAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAbort;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBind;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangNext;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn.BLangWorkerReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement.BLangStatementLink;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * @since 0.94
 */
public class Desugar extends BLangNodeVisitor {

    private static final CompilerContext.Key<Desugar> DESUGAR_KEY =
            new CompilerContext.Key<>();

    private SymbolTable symTable;
    private SymbolResolver symResolver;
    private SymbolEnter symbolEnter;
    private IterableCodeDesugar iterableCodeDesugar;

    private BLangNode result;

    private BLangStatementLink currentLink;
    private Stack<BLangWorker> workerStack = new Stack<>();

    public Stack<BLangLock> enclLocks = new Stack<>();

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
        this.symResolver = SymbolResolver.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
        this.iterableCodeDesugar = IterableCodeDesugar.getInstance(context);
    }

    public BLangPackage perform(BLangPackage pkgNode) {
        return rewrite(pkgNode);
    }

    // visitors

    @Override
    public void visit(BLangPackage pkgNode) {
        if (pkgNode.completedPhases.contains(CompilerPhase.DESUGAR)) {
            result = pkgNode;
            return;
        }
        pkgNode.imports = rewrite(pkgNode.imports);
        pkgNode.xmlnsList = rewrite(pkgNode.xmlnsList);
        pkgNode.globalVars = rewrite(pkgNode.globalVars);
        pkgNode.functions = rewrite(pkgNode.functions);
        pkgNode.connectors = rewrite(pkgNode.connectors);
        pkgNode.services = rewrite(pkgNode.services);
        pkgNode.initFunction = rewrite(pkgNode.initFunction);
        pkgNode.transformers = rewrite(pkgNode.transformers);
        pkgNode.completedPhases.add(CompilerPhase.DESUGAR);
        result = pkgNode;
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
        addReturnIfNotPresent(funcNode);
        funcNode.body = rewrite(funcNode.body);
        funcNode.workers = rewrite(funcNode.workers);

        // If the function has a receiver, we rewrite it's parameter list to have
        // the struct variable as the first parameter
        if (funcNode.receiver != null) {
            BInvokableSymbol funcSymbol = funcNode.symbol;
            List<BVarSymbol> params = funcSymbol.params;
            params.add(0, funcNode.receiver.symbol);
            BInvokableType funcType = (BInvokableType) funcSymbol.type;
            funcType.paramTypes.add(0, funcNode.receiver.type);
        }

        result = funcNode;
    }

    @Override
    public void visit(BLangService serviceNode) {
        serviceNode.resources = rewrite(serviceNode.resources);
        serviceNode.vars = rewrite(serviceNode.vars);
        serviceNode.initFunction = rewrite(serviceNode.initFunction);
        result = serviceNode;
    }

    @Override
    public void visit(BLangResource resourceNode) {
        addReturnIfNotPresent(resourceNode);
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
        connectorNode.initAction = rewrite(connectorNode.initAction);
        result = connectorNode;
    }

    @Override
    public void visit(BLangAction actionNode) {
        addReturnIfNotPresent(actionNode);
        actionNode.body = rewrite(actionNode.body);
        actionNode.workers = rewrite(actionNode.workers);

        // we rewrite it's parameter list to have the receiver variable as the first parameter
        BInvokableSymbol actionSymbol = actionNode.symbol;
        List<BVarSymbol> params = actionSymbol.params;
        BVarSymbol receiverSymbol = actionNode.symbol.receiverSymbol;
        params.add(0, receiverSymbol);
        BInvokableType actionType = (BInvokableType) actionSymbol.type;
        if (receiverSymbol != null) {
            actionType.paramTypes.add(0, receiverSymbol.type);
        }
        result = actionNode;
    }

    @Override
    public void visit(BLangWorker workerNode) {
        this.workerStack.push(workerNode);
        workerNode.body = rewrite(workerNode.body);
        this.workerStack.pop();
        result = workerNode;
    }

    @Override
    public void visit(BLangVariable varNode) {
        if ((varNode.symbol.owner.tag & SymTag.INVOKABLE) == SymTag.INVOKABLE) {
            varNode.expr = rewriteExpr(varNode.expr);
        } else {
            varNode.expr = null;
        }
        result = varNode;
    }

    public void visit(BLangTransformer transformerNode) {
        addTransformerReturn(transformerNode);
        transformerNode.body = rewrite(transformerNode.body);

        addArgInitExpr(transformerNode, transformerNode.retParams.get(0));

        BInvokableSymbol transformerSymbol = transformerNode.symbol;
        List<BVarSymbol> params = transformerSymbol.params;
        params.add(0, transformerNode.source.symbol);
        BInvokableType transformerType = (BInvokableType) transformerSymbol.type;
        transformerType.paramTypes.add(0, transformerNode.source.type);

        result = transformerNode;
    }

    // Statements

    @Override
    public void visit(BLangBlockStmt block) {
        block.stmts = rewriteStmt(block.stmts);
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
    public void visit(BLangBind bindNode) {
        bindNode.varRef = rewriteExpr(bindNode.varRef);
        bindNode.expr = rewriteExpr(bindNode.expr);
        result = new BLangAssignment(bindNode.pos, Arrays.asList(bindNode.varRef), bindNode.expr, false);
    }

    @Override
    public void visit(BLangAbort abortNode) {
        result = abortNode;
    }

    @Override
    public void visit(BLangNext nextNode) {
        result = nextNode;
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
        if (!workerStack.empty()) {
            result = new BLangWorkerReturn(returnNode.exprs);
            result.pos = returnNode.pos;
            return;
        }
        result = returnNode;
    }

    @Override
    public void visit(BLangThrow throwNode) {
        throwNode.expr = rewriteExpr(throwNode.expr);
        result = throwNode;
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        xmlnsStmtNode.xmlnsDecl = rewrite(xmlnsStmtNode.xmlnsDecl);
        result = xmlnsStmtNode;
    }


    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        BLangXMLNS generatedXMLNSNode;
        xmlnsNode.namespaceURI = rewriteExpr(xmlnsNode.namespaceURI);
        BSymbol ownerSymbol = xmlnsNode.symbol.owner;

        // Local namespace declaration in a function/resource/action/worker
        if ((ownerSymbol.tag & SymTag.INVOKABLE) == SymTag.INVOKABLE) {
            generatedXMLNSNode = new BLangLocalXMLNS();
        } else {
            generatedXMLNSNode = new BLangPackageXMLNS();
        }

        generatedXMLNSNode.namespaceURI = xmlnsNode.namespaceURI;
        generatedXMLNSNode.prefix = xmlnsNode.prefix;
        generatedXMLNSNode.symbol = xmlnsNode.symbol;
        result = generatedXMLNSNode;
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        exprStmtNode.expr = rewriteExpr(exprStmtNode.expr);
        result = exprStmtNode;
    }

    @Override
    public void visit(BLangIf ifNode) {
        ifNode.expr = rewriteExpr(ifNode.expr);
        ifNode.body = rewrite(ifNode.body);
        ifNode.elseStmt = rewrite(ifNode.elseStmt);
        result = ifNode;
    }

    @Override
    public void visit(BLangForeach foreach) {
        foreach.varRefs = rewrite(foreach.varRefs);
        foreach.collection = rewriteExpr(foreach.collection);
        foreach.body = rewrite(foreach.body);
        result = foreach;
    }

    @Override
    public void visit(BLangWhile whileNode) {
        whileNode.expr = rewriteExpr(whileNode.expr);
        whileNode.body = rewrite(whileNode.body);
        result = whileNode;
    }

    @Override
    public void visit(BLangLock lockNode) {
        enclLocks.push(lockNode);
        lockNode.body = rewrite(lockNode.body);
        enclLocks.pop();
        lockNode.lockVariables = lockNode.lockVariables.stream().sorted((v1, v2) -> {
            String o1FullName = String.join(":", v1.pkgID.getName().getValue(), v1.name.getValue());
            String o2FullName = String.join(":", v2.pkgID.getName().getValue(), v2.name.getValue());
            return o1FullName.compareTo(o2FullName);
        }).collect(Collectors.toSet());
        result = lockNode;
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        transactionNode.transactionBody = rewrite(transactionNode.transactionBody);
        transactionNode.failedBody = rewrite(transactionNode.failedBody);
        transactionNode.retryCount = rewriteExpr(transactionNode.retryCount);
        result = transactionNode;
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
        forkJoin.joinResultVar = rewrite(forkJoin.joinResultVar);
        forkJoin.joinedBody = rewrite(forkJoin.joinedBody);
        forkJoin.timeoutBody = rewrite(forkJoin.timeoutBody);
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

        if (arrayLiteral.type.tag == TypeTags.JSON || getElementType(arrayLiteral.type).tag == TypeTags.JSON) {
            result = new BLangJSONArrayLiteral(arrayLiteral.exprs, arrayLiteral.type);
            return;
        }
        result = arrayLiteral;
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        recordLiteral.keyValuePairs.forEach(keyValue -> {
            BLangExpression keyExpr = keyValue.key.expr;
            if (keyExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                BLangSimpleVarRef varRef = (BLangSimpleVarRef) keyExpr;
                keyValue.key.expr = createStringLiteral(varRef.pos, varRef.variableName.value);
            } else {
                keyValue.key.expr = rewriteExpr(keyValue.key.expr);
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

        // XML qualified name reference. e.g: ns0:foo
        if (varRefExpr.pkgSymbol != null && varRefExpr.pkgSymbol.tag == SymTag.XMLNS) {
            BLangXMLQName qnameExpr = new BLangXMLQName(varRefExpr.variableName);
            qnameExpr.nsSymbol = (BXMLNSSymbol) varRefExpr.pkgSymbol;
            qnameExpr.localname = varRefExpr.variableName;
            qnameExpr.prefix = varRefExpr.pkgAlias;
            qnameExpr.namespaceURI = qnameExpr.nsSymbol.namespaceURI;
            qnameExpr.isUsedInXML = false;
            qnameExpr.pos = varRefExpr.pos;
            qnameExpr.type = symTable.stringType;
            result = qnameExpr;
            return;
        }

        BSymbol ownerSymbol = varRefExpr.symbol.owner;
        if ((varRefExpr.symbol.tag & SymTag.FUNCTION) == SymTag.FUNCTION &&
                varRefExpr.symbol.type.tag == TypeTags.INVOKABLE) {
            genVarRefExpr = new BLangFunctionVarRef(varRefExpr.symbol);
        } else if ((ownerSymbol.tag & SymTag.INVOKABLE) == SymTag.INVOKABLE) {
            // Local variable in a function/resource/action/worker
            genVarRefExpr = new BLangLocalVarRef(varRefExpr.symbol);
        } else if ((ownerSymbol.tag & SymTag.CONNECTOR) == SymTag.CONNECTOR) {
            // Field variable in a receiver
            genVarRefExpr = new BLangFieldVarRef(varRefExpr.symbol);
        } else if ((ownerSymbol.tag & SymTag.PACKAGE) == SymTag.PACKAGE ||
                (ownerSymbol.tag & SymTag.SERVICE) == SymTag.SERVICE) {
            // Package variable | service variable
            // We consider both of them as package level variables
            genVarRefExpr = new BLangPackageVarRef(varRefExpr.symbol);

            //Only locking service level and package level variables
            if (!enclLocks.isEmpty()) {
                enclLocks.peek().addLockVariable(varRefExpr.symbol);
            }
        }

        genVarRefExpr.type = varRefExpr.type;
        result = genVarRefExpr;
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        BLangVariableReference targetVarRef = fieldAccessExpr;
        if (fieldAccessExpr.expr.type.tag == TypeTags.ENUM) {
            targetVarRef = new BLangEnumeratorAccessExpr(fieldAccessExpr.pos,
                    fieldAccessExpr.field, fieldAccessExpr.symbol);
        } else {
            fieldAccessExpr.expr = rewrite(fieldAccessExpr.expr);
            BType varRefType = fieldAccessExpr.expr.type;
            if (varRefType.tag == TypeTags.STRUCT) {
                targetVarRef = new BLangStructFieldAccessExpr(fieldAccessExpr.pos,
                        fieldAccessExpr.expr, fieldAccessExpr.symbol);
            } else if (varRefType.tag == TypeTags.MAP) {
                BLangLiteral stringLit = createStringLiteral(fieldAccessExpr.pos, fieldAccessExpr.field.value);
                targetVarRef = new BLangMapAccessExpr(fieldAccessExpr.pos, fieldAccessExpr.expr, stringLit);
            } else if (varRefType.tag == TypeTags.JSON) {
                BLangLiteral stringLit = createStringLiteral(fieldAccessExpr.pos, fieldAccessExpr.field.value);
                targetVarRef = new BLangJSONAccessExpr(fieldAccessExpr.pos, fieldAccessExpr.expr, stringLit);
            }
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
        } else if (varRefType.tag == TypeTags.JSON || getElementType(varRefType).tag == TypeTags.JSON) {
            targetVarRef = new BLangJSONAccessExpr(indexAccessExpr.pos, indexAccessExpr.expr,
                    indexAccessExpr.indexExpr);
        } else if (varRefType.tag == TypeTags.ARRAY) {
            targetVarRef = new BLangArrayAccessExpr(indexAccessExpr.pos,
                    indexAccessExpr.expr, indexAccessExpr.indexExpr);
        } else if (varRefType.tag == TypeTags.XML) {
            targetVarRef = new BLangXMLAccessExpr(indexAccessExpr.pos,
                    indexAccessExpr.expr, indexAccessExpr.indexExpr);
        }

        targetVarRef.lhsVar = indexAccessExpr.lhsVar;
        targetVarRef.type = indexAccessExpr.type;
        result = targetVarRef;
    }

    @Override
    public void visit(BLangInvocation iExpr) {
        BLangInvocation genIExpr = iExpr;
        iExpr.argExprs = rewriteExprs(iExpr.argExprs);
        if (iExpr.functionPointerInvocation) {
            visitFunctionPointerInvocation(iExpr);
            return;
        } else if (iExpr.iterableOperationInvocation) {
            visitIterableOperationInvocation(iExpr);
            return;
        }
        iExpr.expr = rewriteExpr(iExpr.expr);
        result = genIExpr;
        if (iExpr.expr == null) {
            return;
        }

        switch (iExpr.expr.type.tag) {
            case TypeTags.BOOLEAN:
            case TypeTags.STRING:
            case TypeTags.INT:
            case TypeTags.FLOAT:
            case TypeTags.BLOB:
            case TypeTags.JSON:
            case TypeTags.XML:
            case TypeTags.MAP:
            case TypeTags.TABLE:
            case TypeTags.STRUCT:
                List<BLangExpression> argExprs = new ArrayList<>(iExpr.argExprs);
                argExprs.add(0, iExpr.expr);
                result = new BLangAttachedFunctionInvocation(iExpr.pos, argExprs, iExpr.symbol,
                        iExpr.types, iExpr.expr);
                break;
            case TypeTags.ENDPOINT:
                List<BLangExpression> actionArgExprs = new ArrayList<>(iExpr.argExprs);
                actionArgExprs.add(0, iExpr.expr);
                result = new BLangActionInvocation(iExpr.pos, actionArgExprs, iExpr.symbol, iExpr.types);
                break;
        }
    }

    public void visit(BLangConnectorInit connectorInitExpr) {
        connectorInitExpr.argsExpr = rewriteExprs(connectorInitExpr.argsExpr);
        result = connectorInitExpr;
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

        // Check lhs and rhs type compatibility
        if (binaryExpr.lhsExpr.type.tag == binaryExpr.rhsExpr.type.tag) {
            return;
        }

        if (binaryExpr.lhsExpr.type.tag == TypeTags.STRING && binaryExpr.opKind == OperatorKind.ADD) {
            binaryExpr.rhsExpr = createTypeConversionExpr(binaryExpr.rhsExpr,
                    binaryExpr.rhsExpr.type, binaryExpr.lhsExpr.type);
            return;
        }

        if (binaryExpr.rhsExpr.type.tag == TypeTags.STRING && binaryExpr.opKind == OperatorKind.ADD) {
            binaryExpr.lhsExpr = createTypeConversionExpr(binaryExpr.lhsExpr,
                    binaryExpr.lhsExpr.type, binaryExpr.rhsExpr.type);
            return;
        }

        if (binaryExpr.lhsExpr.type.tag == TypeTags.FLOAT) {
            binaryExpr.rhsExpr = createTypeConversionExpr(binaryExpr.rhsExpr,
                    binaryExpr.rhsExpr.type, binaryExpr.lhsExpr.type);
            return;
        }

        if (binaryExpr.rhsExpr.type.tag == TypeTags.FLOAT) {
            binaryExpr.lhsExpr = createTypeConversionExpr(binaryExpr.lhsExpr,
                    binaryExpr.lhsExpr.type, binaryExpr.rhsExpr.type);
        }
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        unaryExpr.expr = rewriteExpr(unaryExpr.expr);
        if (unaryExpr.expr.getKind() == NodeKind.TYPEOF_EXPRESSION) {
            result = unaryExpr.expr;
        } else {
            result = unaryExpr;
        }
    }

    @Override
    public void visit(BLangTypeCastExpr castExpr) {
        castExpr.expr = rewriteExpr(castExpr.expr);
        result = castExpr;
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        conversionExpr.expr = rewriteExpr(conversionExpr.expr);

        // Built-in conversion
        if (conversionExpr.conversionSymbol.tag != SymTag.TRANSFORMER) {
            result = conversionExpr;
            return;
        }

        // Named transformer invocation
        if (conversionExpr.transformerInvocation != null) {
            conversionExpr.transformerInvocation = rewrite(conversionExpr.transformerInvocation);
            // Add the rExpr as the first argument
            conversionExpr.transformerInvocation.argExprs.add(0, conversionExpr.expr);
            result = new BLangTransformerInvocation(conversionExpr.pos, conversionExpr.transformerInvocation.argExprs,
                    conversionExpr.transformerInvocation.symbol, conversionExpr.types);
            return;
        }

        // Unnamed transformer invocation
        BConversionOperatorSymbol transformerSym = conversionExpr.conversionSymbol;
        BLangTransformerInvocation transformerInvoc = new BLangTransformerInvocation(conversionExpr.pos,
                Lists.of(conversionExpr.expr), transformerSym, conversionExpr.types);
        transformerInvoc.types = transformerSym.type.getReturnTypes();
        result = transformerInvoc;
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
        xmlAttribute.name = rewriteExpr(xmlAttribute.name);
        xmlAttribute.value = rewriteExpr(xmlAttribute.value);
        result = xmlAttribute;
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        xmlElementLiteral.startTagName = rewriteExpr(xmlElementLiteral.startTagName);
        xmlElementLiteral.endTagName = rewriteExpr(xmlElementLiteral.endTagName);
        xmlElementLiteral.modifiedChildren = rewriteExprs(xmlElementLiteral.modifiedChildren);
        xmlElementLiteral.attributes = rewriteExprs(xmlElementLiteral.attributes);

        // Separate the in-line namepsace declarations and attributes.
        Iterator<BLangXMLAttribute> attributesItr = xmlElementLiteral.attributes.iterator();
        while (attributesItr.hasNext()) {
            BLangXMLAttribute attribute = attributesItr.next();
            if (!attribute.isNamespaceDeclr) {
                continue;
            }

            // Create local namepace declaration for all in-line namespace declarations
            BLangLocalXMLNS xmlns = new BLangLocalXMLNS();
            xmlns.namespaceURI = attribute.value.concatExpr;
            xmlns.prefix = ((BLangXMLQName) attribute.name).localname;
            xmlns.symbol = (BXMLNSSymbol) attribute.symbol;

            xmlElementLiteral.inlineNamespaces.add(xmlns);
            attributesItr.remove();
        }

        result = xmlElementLiteral;
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        xmlTextLiteral.concatExpr = rewriteExpr(xmlTextLiteral.concatExpr);
        result = xmlTextLiteral;
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        xmlCommentLiteral.concatExpr = rewriteExpr(xmlCommentLiteral.concatExpr);
        result = xmlCommentLiteral;
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        xmlProcInsLiteral.target = rewriteExpr(xmlProcInsLiteral.target);
        xmlProcInsLiteral.dataConcatExpr = rewriteExpr(xmlProcInsLiteral.dataConcatExpr);
        result = xmlProcInsLiteral;
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {
        xmlQuotedString.concatExpr = rewriteExpr(xmlQuotedString.concatExpr);
        result = xmlQuotedString;
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        stringTemplateLiteral.concatExpr = rewriteExpr(stringTemplateLiteral.concatExpr);
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

    @Override
    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        xmlAttributeAccessExpr.indexExpr = rewrite(xmlAttributeAccessExpr.indexExpr);
        xmlAttributeAccessExpr.expr = rewrite(xmlAttributeAccessExpr.expr);

        if (xmlAttributeAccessExpr.indexExpr != null
                && xmlAttributeAccessExpr.indexExpr.getKind() == NodeKind.XML_QNAME) {
            ((BLangXMLQName) xmlAttributeAccessExpr.indexExpr).isUsedInXML = true;
        }

        result = xmlAttributeAccessExpr;
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

    @Override
    public void visit(BLangTypeofExpr accessExpr) {
        result = accessExpr;
    }

    @Override
    public void visit(BLangIntRangeExpression intRangeExpression) {
        intRangeExpression.startExpr = rewriteExpr(intRangeExpression.startExpr);
        intRangeExpression.endExpr = rewriteExpr(intRangeExpression.endExpr);
        result = intRangeExpression;
    }

    // private functions

    private void visitFunctionPointerInvocation(BLangInvocation iExpr) {
        BLangVariableReference expr;
        if (iExpr.expr == null) {
            expr = new BLangSimpleVarRef();
        } else {
            BLangFieldBasedAccess fieldBasedAccess = new BLangFieldBasedAccess();
            fieldBasedAccess.expr = iExpr.expr;
            fieldBasedAccess.field = iExpr.name;
            expr = fieldBasedAccess;
        }
        expr.symbol = (BVarSymbol) iExpr.symbol;
        expr.type = iExpr.symbol.type;
        expr = rewriteExpr(expr);
        result = new BFunctionPointerInvocation(iExpr, expr);
    }

    private void visitIterableOperationInvocation(BLangInvocation iExpr) {
        if (iExpr.iContext.operations.getLast().iExpr != iExpr) {
            result = null;
            return;
        }
        iterableCodeDesugar.desugar(iExpr.iContext);
        result = rewrite(iExpr.iContext.iteratorCaller);
    }

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
            node.impCastExpr = null;
        }

        expr.accept(this);
        BLangNode resultNode = this.result;
        this.result = null;
        return (E) resultNode;
    }

    @SuppressWarnings("unchecked")
    private <E extends BLangStatement> E rewrite(E statement) {
        if (statement == null) {
            return null;
        }
        BLangStatementLink link = new BLangStatementLink();
        link.parent = currentLink;
        currentLink = link;
        BLangStatement stmt = (BLangStatement) rewrite((BLangNode) statement);
        // Link Statements.
        link.statement = stmt;
        stmt.statementLink = link;
        currentLink = link.parent;
        return (E) stmt;
    }

    private <E extends BLangStatement> List<E> rewriteStmt(List<E> nodeList) {
        for (int i = 0; i < nodeList.size(); i++) {
            nodeList.set(i, rewrite(nodeList.get(i)));
        }
        return nodeList;
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

    private BLangExpression createTypeConversionExpr(BLangExpression expr, BType sourceType, BType targetType) {
        BConversionOperatorSymbol symbol = (BConversionOperatorSymbol)
                symResolver.resolveConversionOperator(sourceType, targetType);
        BLangTypeConversionExpr conversionExpr = (BLangTypeConversionExpr) TreeBuilder.createTypeConversionNode();
        conversionExpr.pos = expr.pos;
        conversionExpr.expr = expr;
        conversionExpr.type = targetType;
        conversionExpr.types = Lists.of(targetType);
        conversionExpr.conversionSymbol = symbol;
        return conversionExpr;
    }

    private BType getElementType(BType type) {
        if (type.tag != TypeTags.ARRAY) {
            return type;
        }

        return getElementType(((BArrayType) type).getElementType());
    }

    private void addReturnIfNotPresent(BLangInvokableNode invokableNode) {
        if (Symbols.isNative(invokableNode.symbol)) {
            return;
        }
        //This will only check whether last statement is a return and just add a return statement.
        //This won't analyse if else blocks etc to see whether return statements are present
        BLangBlockStmt blockStmt = invokableNode.body;
        if (invokableNode.workers.size() == 0 && invokableNode.retParams.isEmpty() && (blockStmt.stmts.size() < 1
                || blockStmt.stmts.get(blockStmt.stmts.size() - 1).getKind() != NodeKind.RETURN)) {
            BLangReturn returnStmt = (BLangReturn) TreeBuilder.createReturnNode();
            DiagnosticPos invPos = invokableNode.pos;
            DiagnosticPos pos = new DiagnosticPos(invPos.src, invPos.eLine, invPos.eLine, invPos.sCol, invPos.sCol);
            returnStmt.pos = pos;
            blockStmt.addStatement(returnStmt);
        }
    }

    private void addTransformerReturn(BLangTransformer transformerNode) {
        //This will only check whether last statement is a return and just add a return statement.
        //This won't analyze if else blocks etc to see whether return statements are present
        BLangBlockStmt blockStmt = transformerNode.body;
        if (transformerNode.workers.size() == 0 && (blockStmt.stmts.size() < 1
                || blockStmt.stmts.get(blockStmt.stmts.size() - 1).getKind() != NodeKind.RETURN)) {
            BLangReturn returnStmt = (BLangReturn) TreeBuilder.createReturnNode();
            DiagnosticPos invPos = transformerNode.pos;
            DiagnosticPos pos = new DiagnosticPos(invPos.src, invPos.eLine, invPos.eLine, invPos.sCol, invPos.sCol);
            returnStmt.pos = pos;
            
            if (!transformerNode.retParams.isEmpty()) {
                returnStmt.namedReturnVariables = transformerNode.retParams;
            }
            blockStmt.addStatement(returnStmt);
        }
    }

    private void addArgInitExpr(BLangTransformer transformerNode, BLangVariable var) {
        BLangSimpleVarRef varRef = new BLangLocalVarRef(var.symbol);
        varRef.lhsVar = true;
        varRef.pos = var.pos;
        varRef.type = var.type;

        BLangExpression initExpr = null;
        switch (var.type.tag) {
            case TypeTags.MAP:
                initExpr = new BLangMapLiteral(new ArrayList<>(), var.type);
                break;
            case TypeTags.JSON:
                initExpr = new BLangJSONLiteral(new ArrayList<>(), var.type);
                break;
            case TypeTags.STRUCT:
                initExpr = new BLangStructLiteral(new ArrayList<>(), var.type);
                break;
            case TypeTags.INT:
            case TypeTags.FLOAT:
            case TypeTags.STRING:
            case TypeTags.BOOLEAN:
            case TypeTags.BLOB:
            case TypeTags.XML:
                return;
            case TypeTags.TABLE:
                // TODO: add this once the able initializing is supported.
                return;
            default:
                return;
        }
        initExpr.pos = var.pos;

        BLangAssignment assignStmt = (BLangAssignment) TreeBuilder.createAssignmentNode();
        assignStmt.pos = var.pos;
        assignStmt.addVariable(varRef);
        assignStmt.expr = initExpr;

        transformerNode.body.stmts.add(0, assignStmt);
    }
}
