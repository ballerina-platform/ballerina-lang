/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.langserver.definition;

import org.ballerinalang.langserver.common.LSNodeVisitor;
import org.ballerinalang.langserver.common.constants.NodeContextKeys;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.model.tree.TopLevelNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangObject;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecord;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSingleton;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Tree visitor to find the definition of variables.
 */
public class DefinitionTreeVisitor extends LSNodeVisitor {

    private boolean terminateVisitor = false;
    private LSServiceOperationContext context;
    private String fileName;

    public DefinitionTreeVisitor(LSServiceOperationContext context) {
        this.context = context;
        this.fileName = context.get(DocumentServiceKeys.FILE_NAME_KEY);
        this.context.put(NodeContextKeys.NODE_KEY, null);
    }

    @Override
    public void visit(BLangPackage pkgNode) {
        // Then visit each top-level element sorted using the compilation unit
        List<TopLevelNode> topLevelNodes = pkgNode.topLevelNodes.stream().filter(node ->
                node.getPosition().getSource().getCompilationUnitName().equals(this.fileName)
        ).collect(Collectors.toList());

        if (topLevelNodes.isEmpty()) {
            terminateVisitor = true;
            acceptNode(null);
        } else {
            topLevelNodes.forEach(topLevelNode -> acceptNode((BLangNode) topLevelNode));
        }
    }

    @Override
    public void visit(BLangFunction funcNode) {
        // Check for native functions
        BSymbol funcSymbol = funcNode.symbol;
        if (Symbols.isNative(funcSymbol)) {
            return;
        }

        if (funcNode.name.getValue().equals(this.context.get(NodeContextKeys.NODE_OWNER_KEY))) {

            if (funcNode.receiver != null) {
                this.acceptNode(funcNode.receiver);
            }

            if (funcNode.requiredParams != null) {
                funcNode.requiredParams.forEach(this::acceptNode);
            }

            if (funcNode.returnTypeNode != null && !(funcNode.returnTypeNode.type instanceof BNilType)) {
                this.acceptNode(funcNode.returnTypeNode);
            }

            if (funcNode.endpoints != null) {
                funcNode.endpoints.forEach(this::acceptNode);
            }

            if (funcNode.body != null) {
                this.acceptNode(funcNode.body);
            }

            if (funcNode.workers != null) {
                funcNode.workers.forEach(this::acceptNode);
            }

            if (funcNode.defaultableParams != null) {
                funcNode.defaultableParams.forEach(this::acceptNode);
            }
        }
    }

    @Override
    public void visit(BLangService serviceNode) {
        if (serviceNode.name.getValue()
                .equals(this.context.get(NodeContextKeys.NODE_OWNER_KEY))) {

            if (serviceNode.serviceTypeStruct != null) {
                this.acceptNode(serviceNode.serviceTypeStruct);
            }

            if (serviceNode.vars != null) {
                serviceNode.vars.forEach(this::acceptNode);
            }

            if (serviceNode.resources != null) {
                serviceNode.resources.forEach(this::acceptNode);
            }

            if (serviceNode.endpoints != null) {
                serviceNode.endpoints.forEach(this::acceptNode);
            }

            if (serviceNode.boundEndpoints != null) {
                serviceNode.boundEndpoints.forEach(this::acceptNode);
            }

            if (serviceNode.initFunction != null) {
                this.acceptNode(serviceNode.initFunction);
            }
        }
    }

    @Override
    public void visit(BLangResource resourceNode) {
        if (resourceNode.name.getValue()
                .equals(this.context.get(NodeContextKeys.NODE_OWNER_KEY))) {
            if (resourceNode.requiredParams != null) {
                resourceNode.requiredParams.forEach(this::acceptNode);
            }

            if (resourceNode.body != null) {
                this.acceptNode(resourceNode.body);
            }
        }
    }

    @Override
    public void visit(BLangConnector connectorNode) {
        if (connectorNode.name.getValue()
                .equals(this.context.get(NodeContextKeys.NODE_OWNER_KEY))) {
            if (connectorNode.params != null) {
                connectorNode.params.forEach(this::acceptNode);
            }

            if (connectorNode.varDefs != null) {
                connectorNode.varDefs.forEach(this::acceptNode);
            }

            if (connectorNode.actions != null) {
                connectorNode.actions.forEach(this::acceptNode);
            }
        }
    }

    @Override
    public void visit(BLangAction actionNode) {
        if (actionNode.name.getValue()
                .equals(this.context.get(NodeContextKeys.NODE_OWNER_KEY))) {
            if (actionNode.requiredParams != null) {
                actionNode.requiredParams.forEach(this::acceptNode);
            }

            if (actionNode.body != null) {
                acceptNode(actionNode.body);
            }

            if (actionNode.workers != null) {
                actionNode.workers.forEach(this::acceptNode);
            }
        }
    }

    @Override
    public void visit(BLangVariable varNode) {
        if (varNode.name.getValue()
                .equals(this.context.get(NodeContextKeys.VAR_NAME_OF_NODE_KEY))) {
            this.context.put(NodeContextKeys.NODE_KEY, varNode);
            terminateVisitor = true;
        }
    }

    @Override
    public void visit(BLangWorker workerNode) {
        if (workerNode.body != null) {
            this.acceptNode(workerNode.body);
        }

        if (workerNode.workers != null) {
            workerNode.workers.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        if (blockNode.stmts != null) {
            blockNode.stmts.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangVariableDef varDefNode) {
        if (varDefNode.getVariable() != null) {
            this.acceptNode(varDefNode.getVariable());
        }
    }

    @Override
    public void visit(BLangAssignment assignNode) {
    }

    @Override
    public void visit(BLangIf ifNode) {
        if (ifNode.body != null) {
            this.acceptNode(ifNode.body);
        }

        if (ifNode.elseStmt != null) {
            this.acceptNode(ifNode.elseStmt);
        }
    }

    @Override
    public void visit(BLangForeach foreach) {
        if (foreach.varRefs != null) {
            foreach.varRefs.forEach(this::acceptNode);
        }

        if (foreach.body != null) {
            this.acceptNode(foreach.body);
        }
    }

    @Override
    public void visit(BLangWhile whileNode) {
        if (whileNode.body != null) {
            this.acceptNode(whileNode.body);
        }
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        if (transactionNode.transactionBody != null) {
            this.acceptNode(transactionNode.transactionBody);
        }

        if (transactionNode.onRetryBody != null) {
            this.acceptNode(transactionNode.onRetryBody);
        }
    }

    @Override
    public void visit(BLangTryCatchFinally tryNode) {
        if (tryNode.tryBody != null) {
            this.acceptNode(tryNode.tryBody);
        }

        if (tryNode.catchBlocks != null) {
            tryNode.catchBlocks.forEach(this::acceptNode);
        }

        if (tryNode.finallyBody != null) {
            this.acceptNode(tryNode.finallyBody);
        }
    }

    @Override
    public void visit(BLangCatch catchNode) {
        if (catchNode.body != null) {
            this.acceptNode(catchNode.body);
        }
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        if (forkJoin.getWorkers() != null) {
            forkJoin.getWorkers().forEach(this::acceptNode);
        }

        if (forkJoin.joinedBody != null) {
            this.acceptNode(forkJoin.joinedBody);
        }

        if (forkJoin.timeoutBody != null) {
            this.acceptNode(forkJoin.timeoutBody);
        }
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        if (varRefExpr.variableName.getValue()
                .equals(this.context.get(NodeContextKeys.VAR_NAME_OF_NODE_KEY))) {
            this.context.put(NodeContextKeys.NODE_KEY, varRefExpr);
            terminateVisitor = true;
        }
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        if (bLangLambdaFunction.function != null) {
            this.acceptNode(bLangLambdaFunction.function);
        }
    }

    @Override
    public void visit(BLangTransformer transformerNode) {
        if (transformerNode.source != null) {
            acceptNode(transformerNode.source);
        }

        if (transformerNode.requiredParams != null) {
            transformerNode.requiredParams.forEach(this::acceptNode);
        }

        if (transformerNode.retParams != null) {
            transformerNode.retParams.forEach(this::acceptNode);
        }

        if (transformerNode.body != null) {
            acceptNode(transformerNode.body);
        }

        if (transformerNode.workers != null) {
            transformerNode.workers.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangEndpoint endpointNode) {
        if (endpointNode.name.getValue()
                .equals(this.context.get(NodeContextKeys.VAR_NAME_OF_NODE_KEY))) {
            this.context.put(NodeContextKeys.NODE_KEY, endpointNode);
            terminateVisitor = true;
        }

        if (endpointNode.endpointTypeNode != null) {
            this.acceptNode(endpointNode.endpointTypeNode);
        }

        if (endpointNode.configurationExpr != null) {
            this.acceptNode(endpointNode.configurationExpr);
        }
    }

    @Override
    public void visit(BLangTupleDestructure stmt) {
        if (stmt.varRefs != null) {
            stmt.varRefs.forEach(this::acceptNode);
        }

        if (stmt.expr != null) {
            this.acceptNode(stmt.expr);
        }
    }

    @Override
    public void visit(BLangObject objectNode) {
        if (objectNode.name.getValue()
                .equals(this.context.get(NodeContextKeys.VAR_NAME_OF_NODE_KEY))) {
            this.context.put(NodeContextKeys.NODE_KEY, objectNode);
            terminateVisitor = true;
        }

        if (objectNode.fields != null) {
            objectNode.fields.forEach(this::acceptNode);
        }

        if (objectNode.functions != null) {
            objectNode.functions.forEach(this::acceptNode);
        }

        if (objectNode.initFunction != null) {
            this.acceptNode(objectNode.initFunction);
        }

        if (objectNode.receiver != null) {
            this.acceptNode(objectNode.receiver);
        }
    }

    @Override
    public void visit(BLangTypeInit connectorInitExpr) {
        if (connectorInitExpr.userDefinedType != null) {
            this.acceptNode(connectorInitExpr.userDefinedType);
        }
        if (connectorInitExpr.argsExpr != null) {
            connectorInitExpr.argsExpr.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangMatch matchNode) {
        if (matchNode.expr != null) {
            this.acceptNode(matchNode.expr);
        }

        if (matchNode.patternClauses != null) {
            matchNode.patternClauses.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangMatch.BLangMatchStmtPatternClause patternClauseNode) {
        if (patternClauseNode.getVariableNode() != null &&
                patternClauseNode.getVariableNode().getName() != null &&
                patternClauseNode.getVariableNode().getName().getValue()
                        .equals(this.context.get(NodeContextKeys.VAR_NAME_OF_NODE_KEY))) {
            this.context.put(NodeContextKeys.NODE_KEY, patternClauseNode.getVariableNode());
            terminateVisitor = true;
        }

        if (patternClauseNode.variable != null) {
            this.acceptNode(patternClauseNode.variable);
        }

        if (patternClauseNode.body != null) {
            this.acceptNode(patternClauseNode.body);
        }
    }

    @Override
    public void visit(BLangRecord record) {
        if (record.name.getValue()
                .equals(this.context.get(NodeContextKeys.VAR_NAME_OF_NODE_KEY))) {
            this.context.put(NodeContextKeys.NODE_KEY, record);
            terminateVisitor = true;
        }

        if (record.fields != null) {
            record.fields.forEach(this::acceptNode);
        }

        if (record.initFunction != null &&
                !(record.initFunction.returnTypeNode.type instanceof BNilType)) {
            this.acceptNode(record.initFunction);
        }
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        if (recordLiteral.keyValuePairs != null) {
            recordLiteral.keyValuePairs.forEach((bLangRecordKeyValue -> {
                if (bLangRecordKeyValue.valueExpr != null) {
                    this.acceptNode(bLangRecordKeyValue.valueExpr);
                }
            }));
        }

        if (recordLiteral.impConversionExpr != null) {
            this.acceptNode(recordLiteral.impConversionExpr);
        }
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        if (typeDefinition.name.getValue()
                .equals(this.context.get(NodeContextKeys.VAR_NAME_OF_NODE_KEY))) {
            this.context.put(NodeContextKeys.NODE_KEY, typeDefinition);
            terminateVisitor = true;
        }

        if (typeDefinition.typeNode != null) {
            this.acceptNode(typeDefinition.typeNode);
        }

        if (typeDefinition.valueSpace != null) {
            typeDefinition.valueSpace.forEach(this::acceptNode);
        }
    }

    public void visit(BLangSingleton singleton) {
        /* ignore */
    }

    /**
     * Accept node to visit.
     *
     * @param node node to be accepted to visit.
     */
    private void acceptNode(BLangNode node) {
        if (this.terminateVisitor) {
            return;
        }
        node.accept(this);
    }
}
