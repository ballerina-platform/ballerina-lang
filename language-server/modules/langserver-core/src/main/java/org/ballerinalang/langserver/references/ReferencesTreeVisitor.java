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
package org.ballerinalang.langserver.references;

import org.ballerinalang.langserver.common.LSNodeVisitor;
import org.ballerinalang.langserver.common.constants.NodeContextKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.compiler.common.LSDocument;
import org.ballerinalang.langserver.hover.util.HoverUtil;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangScope;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.types.BLangEndpointTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Tree visitor for finding the references of a statement.
 */
public class ReferencesTreeVisitor extends LSNodeVisitor {
    private boolean terminateVisitor = false;
    private LSServiceOperationContext context;
    private List<Location> locations;


    public ReferencesTreeVisitor(LSServiceOperationContext context) {
        this.context = context;
        this.locations = context.get(NodeContextKeys.REFERENCE_RESULTS_KEY);
    }

    @Override
    public void visit(BLangPackage pkgNode) {
        List<TopLevelNode> topLevelNodes = new ArrayList<>(pkgNode.topLevelNodes);
        if (pkgNode.containsTestablePkg()) {
            topLevelNodes.addAll(pkgNode.getTestablePkg().topLevelNodes);
        }
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

        if (isReferenced(funcNode.name.getValue(), funcNode.symbol.owner, funcNode.symbol.pkgID,
                         funcNode.symbol.owner.pkgID)) {
            addLocation(funcNode, funcNode.symbol.pkgID.name.getValue(), funcNode.symbol.pkgID.name.getValue());
        }

        if (funcNode.receiver != null &&
                !funcNode.receiver.getName().getValue().equals("self")) {
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

    @Override
    public void visit(BLangService serviceNode) {
        BSymbol servSymbol = serviceNode.symbol;
        if (isReferenced(serviceNode.name.getValue(), servSymbol.owner, servSymbol.pkgID, servSymbol.owner.pkgID)) {
            addLocation(serviceNode, servSymbol.pkgID.name.getValue(), servSymbol.pkgID.name.getValue());
        }

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

    @Override
    public void visit(BLangResource resourceNode) {
        if (isReferenced(resourceNode.name.getValue(), resourceNode.symbol.owner, resourceNode.symbol.pkgID,
                         resourceNode.symbol.owner.pkgID)) {
            CommonUtil.replacePosition(resourceNode.getPosition(), HoverUtil.getIdentifierPosition(resourceNode));
            addLocation(resourceNode, resourceNode.symbol.pkgID.name.getValue(),
                        resourceNode.symbol.pkgID.name.getValue());
        }

        if (resourceNode.requiredParams != null) {
            resourceNode.requiredParams.forEach(this::acceptNode);
        }

        if (resourceNode.body != null) {
            this.acceptNode(resourceNode.body);
        }
    }

    @Override
    public void visit(BLangAction actionNode) {
        if (isReferenced(actionNode.name.getValue(), actionNode.symbol.owner, actionNode.symbol.pkgID,
                         actionNode.symbol.owner.pkgID)) {
            addLocation(actionNode, actionNode.symbol.pkgID.name.getValue(),
                        actionNode.symbol.pkgID.name.getValue());
        }

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

    @Override
    public void visit(BLangVariable varNode) {
        BVarSymbol varSymbol = varNode.symbol;
        if (!(varNode.type instanceof BUnionType) && isReferenced(varNode.name.getValue(), varSymbol.owner,
                                                                  varSymbol.pkgID, varSymbol.owner.pkgID)) {
            addLocation(varNode, varSymbol.owner.pkgID.name.getValue(), varNode.pos.getSource().pkgID.name.getValue());
        }

        if (varNode.typeNode != null) {
            this.acceptNode(varNode.typeNode);
        }

        if (varNode.expr != null) {
            this.acceptNode(varNode.expr);
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
        if (assignNode.varRef != null) {
            this.acceptNode(assignNode.varRef);
        }
        if (assignNode.expr != null) {
            this.acceptNode(assignNode.expr);
        }
    }

    @Override
    public void visit(BLangIf ifNode) {
        if (ifNode.expr != null) {
            this.acceptNode(ifNode.expr);
        }

        if (ifNode.body != null) {
            this.acceptNode(ifNode.body);
        }

        if (ifNode.elseStmt != null) {
            this.acceptNode(ifNode.elseStmt);
        }
    }

    @Override
    public void visit(BLangForeach foreach) {
        this.acceptNode(foreach.collection);

        if (foreach.varRefs != null) {
            foreach.varRefs.forEach(this::acceptNode);
        }

        if (foreach.body != null) {
            this.acceptNode(foreach.body);
        }
    }

    @Override
    public void visit(BLangWhile whileNode) {
        if (whileNode.expr != null) {
            this.acceptNode(whileNode.expr);
        }

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
        CommonUtil.calculateEndColumnOfGivenName(varRefExpr.getPosition(), varRefExpr.variableName.value,
                                                 varRefExpr.pkgAlias.value);
        if (varRefExpr.symbol != null && isReferenced(varRefExpr.variableName.getValue(),
                                                      varRefExpr.symbol.owner,
                                                      varRefExpr.symbol.pkgID,
                                                      varRefExpr.symbol.owner.pkgID)) {
            addLocation(varRefExpr, varRefExpr.symbol.owner.pkgID.name.getValue(),
                        varRefExpr.pos.getSource().pkgID.name.getValue());

        } else if (varRefExpr.type.tsymbol != null && isReferenced(varRefExpr.variableName.getValue(),
                                                                   varRefExpr.type.tsymbol.owner,
                                                                   varRefExpr.type.tsymbol.pkgID,
                                                                   varRefExpr.type.tsymbol.owner.pkgID)) {
            addLocation(varRefExpr, varRefExpr.type.tsymbol.owner.pkgID.name.getValue(),
                        varRefExpr.pos.getSource().pkgID.name.getValue());
        }
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        if (bLangLambdaFunction.function != null) {
            this.acceptNode(bLangLambdaFunction.function);
        }
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        BSymbol inExSymbol = invocationExpr.symbol;
        if (isReferencedRegardlessPkgs(invocationExpr.name.getValue(), inExSymbol.owner)) {
            addLocation(invocationExpr, inExSymbol.owner.pkgID.name.getValue(),
                        invocationExpr.pos.getSource().pkgID.name.getValue());
        }

        if (invocationExpr.expr != null) {
            this.acceptNode(invocationExpr.expr);
        }

        if (invocationExpr.argExprs != null) {
            invocationExpr.argExprs.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        if (exprStmtNode.expr != null) {
            this.acceptNode(exprStmtNode.expr);
        }
    }

    @Override
    public void visit(BLangReturn returnNode) {
        if (returnNode.expr != null) {
            this.acceptNode(returnNode.expr);
        }
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldBasedAccess) {
        BSymbol fbaSymbol = fieldBasedAccess.symbol;
        if (isReferenced(fieldBasedAccess.field.getValue(), fbaSymbol.owner, fbaSymbol.pkgID, fbaSymbol.owner.pkgID)) {
            addLocation(fieldBasedAccess, fbaSymbol.owner.pkgID.name.getValue(),
                        fieldBasedAccess.pos.getSource().pkgID.name.getValue());
        }
        if (fieldBasedAccess.expr != null) {
            this.acceptNode(fieldBasedAccess.expr);
        }
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
        if (userDefinedType.getPosition() != null) {
            userDefinedType.getPosition().sCol += (this.context.get(NodeContextKeys.PREVIOUSLY_VISITED_NODE_KEY)
                    instanceof BLangEndpointTypeNode ? "endpoint<".length() : 0);
            CommonUtil.calculateEndColumnOfGivenName(userDefinedType.getPosition(), userDefinedType.typeName.value,
                                                     userDefinedType.pkgAlias.value);
            BType udType = userDefinedType.type;
            if (udType.tsymbol != null && isReferencedRegardlessPkgs(userDefinedType.typeName.getValue(),
                                                                                   udType.tsymbol.owner)) {
                addLocation(userDefinedType, udType.tsymbol.owner.pkgID.name.getValue(),
                            userDefinedType.pos.getSource().pkgID.name.getValue());
            } else if (udType instanceof BUnionType) {
                try {
                    BUnionType bUnionType = (BUnionType) udType;
                    for (BType type : bUnionType.memberTypes) {
                        if (type.tsymbol != null && isReferencedRegardlessPkgs(type.tsymbol.name.getValue(),
                                                                               type.tsymbol.owner)) {
                            addLocation(userDefinedType, type.tsymbol.owner.pkgID.name.getValue(),
                                        userDefinedType.pos.getSource().pkgID.name.getValue());
                            break;
                        }
                    }
                } catch (ClassCastException e) {
                    // Ignores
                }
            }
        }
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        if (binaryExpr.lhsExpr != null) {
            acceptNode(binaryExpr.lhsExpr);
        }

        if (binaryExpr.rhsExpr != null) {
            acceptNode(binaryExpr.rhsExpr);
        }
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        if (conversionExpr.expr != null) {
            acceptNode(conversionExpr.expr);
        }

        if (conversionExpr.typeNode != null) {
            acceptNode(conversionExpr.typeNode);
        }
    }

    @Override
    public void visit(BLangEndpoint endpointNode) {
        if (isReferenced(endpointNode.name.getValue(), endpointNode.symbol.owner, endpointNode.symbol.pkgID,
                         endpointNode.symbol.owner.pkgID)) {
            addLocation(endpointNode, endpointNode.symbol.owner.pkgID.name.getValue(),
                        endpointNode.pos.getSource().pkgID.name.getValue());
        }

        if (endpointNode.endpointTypeNode != null) {
            this.acceptNode(endpointNode.endpointTypeNode);
        }

        if (endpointNode.configurationExpr != null) {
            this.acceptNode(endpointNode.configurationExpr);
        }
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        if (ternaryExpr.expr != null) {
            this.acceptNode(ternaryExpr.expr);
        }

        if (ternaryExpr.thenExpr != null) {
            this.acceptNode(ternaryExpr.thenExpr);
        }

        if (ternaryExpr.elseExpr != null) {
            this.acceptNode(ternaryExpr.elseExpr);
        }
    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode) {
        if (unionTypeNode.memberTypeNodes != null) {
            unionTypeNode.memberTypeNodes.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {
        if (tupleTypeNode.memberTypeNodes != null) {
            tupleTypeNode.memberTypeNodes.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangBracedOrTupleExpr bracedOrTupleExpr) {
        if (bracedOrTupleExpr.expressions != null) {
            bracedOrTupleExpr.expressions.forEach(this::acceptNode);
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
    public void visit(BLangObjectTypeNode objectTypeNode) {
        BSymbol objectSymbol = objectTypeNode.symbol;
        if (isReferenced(objectSymbol.name.getValue(), objectSymbol.owner, objectSymbol.pkgID,
                         objectSymbol.owner.pkgID)) {
            addLocation(objectTypeNode, objectSymbol.owner.pkgID.name.getValue(),
                        objectTypeNode.pos.getSource().pkgID.name.getValue());
        }

        if (objectTypeNode.fields != null) {
            objectTypeNode.fields.forEach(this::acceptNode);
        }

        if (objectTypeNode.functions != null) {
            objectTypeNode.functions.forEach(this::acceptNode);
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
        if (patternClauseNode.variable != null) {
            this.acceptNode(patternClauseNode.variable);
        }

        if (patternClauseNode.body != null) {
            this.acceptNode(patternClauseNode.body);
        }
    }

    @Override
    public void visit(BLangTypeInit typeInitExpr) {
        if (typeInitExpr.userDefinedType != null) {
            acceptNode(typeInitExpr.userDefinedType);
        }

        if (typeInitExpr.argsExpr != null) {
            typeInitExpr.argsExpr.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        if (recordTypeNode.fields != null) {
            recordTypeNode.fields.forEach(this::acceptNode);
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
    public void visit(BLangMatchExpression bLangMatchExpression) {
        if (bLangMatchExpression.impConversionExpr != null) {
            this.acceptNode(bLangMatchExpression.impConversionExpr);
        }

        if (bLangMatchExpression.expr != null) {
            this.acceptNode(bLangMatchExpression.expr);
        }

        if (bLangMatchExpression.patternClauses != null) {
            bLangMatchExpression.patternClauses.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangMatchExpression.BLangMatchExprPatternClause bLangMatchExprPatternClause) {
        if (bLangMatchExprPatternClause.variable != null) {
            this.acceptNode(bLangMatchExprPatternClause.variable);
        }

        if (bLangMatchExprPatternClause.expr != null) {
            this.acceptNode(bLangMatchExprPatternClause.expr);
        }
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        BTypeSymbol typeSymbol = typeDefinition.symbol;
        if (isReferenced(typeDefinition.name.value, typeSymbol.owner, typeSymbol.pkgID, typeSymbol.owner.pkgID)) {
            addLocation(typeDefinition, typeSymbol.owner.pkgID.name.getValue(),
                        typeDefinition.pos.getSource().pkgID.name.getValue());
        }

        if (typeDefinition.typeNode != null) {
            this.acceptNode(typeDefinition.typeNode);
        }

//        if (typeDefinition.valueSpace != null) {
//            typeDefinition.valueSpace.forEach(this::acceptNode);
//        }

    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        if (checkedExpr.expr != null) {
            this.acceptNode(checkedExpr.expr);
        }
    }

    @Override
    public void visit(BLangIndexBasedAccess indexBasedAccess) {
        if (indexBasedAccess.expr != null) {
            this.acceptNode(indexBasedAccess.expr);
        }
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        if (unaryExpr.expr != null) {
            this.acceptNode(unaryExpr.expr);
        }
    }

    @Override
    public void visit(BLangScope scopeNode) {
        if (scopeNode.scopeBody != null) {
            this.acceptNode(scopeNode.scopeBody);
        }

        visit(scopeNode.compensationFunction);
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignNode) {
        this.acceptNode(compoundAssignNode.varRef);
        if (compoundAssignNode.expr != null) {
            this.acceptNode(compoundAssignNode.expr);
        }
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

    /**
     * Get the physical source location of the given package.
     *
     * @param bLangNode          ballerina language node references are requested for
     * @param ownerPackageName   list of name compositions of the node's package name
     * @param currentPackageName list of name compositions of the current package
     * @return location of the package of the given node
     */
    private Location getLocation(BLangNode bLangNode, String ownerPackageName, String currentPackageName) {
        Location l = new Location();
        Range r = new Range();
        TextDocumentPositionParams position = this.context.get(DocumentServiceKeys.POSITION_KEY);
        Path parentPath = new LSDocument(position.getTextDocument().getUri()).getPath().getParent();
        if (parentPath != null) {
            String fileName = bLangNode.getPosition().getSource().getCompilationUnitName();
            Path filePath = Paths.get(CommonUtil
                                              .getPackageURI(currentPackageName, parentPath.toString(),
                                                             ownerPackageName), fileName);
            l.setUri(filePath.toUri().toString());

            // Subtract 1 to convert the token lines and char positions to zero based indexing
            r.setStart(new Position(bLangNode.getPosition().getStartLine() - 1,
                                    bLangNode.getPosition().getStartColumn() - 1));
            r.setEnd(new Position(bLangNode.getPosition().getEndLine() - 1,
                                  bLangNode.getPosition().getEndColumn() - 1));
            l.setRange(r);
        }

        return l;
    }

    /**
     * Add location to locations list.
     *
     * @param node       node to calculate the location of
     * @param ownerPkg   package of the owner
     * @param currentPkg package of the current node as a list of package paths
     */
    private void addLocation(BLangNode node, String ownerPkg, String currentPkg) {
        this.locations.add(getLocation(node, ownerPkg, currentPkg));
    }

    /**
     * Returns true if the node is referenced.
     *
     * @param name name of the node
     * @param owner owner of the node
     * @return true if the node is referenced
     */
    private boolean isReferencedRegardlessPkgs(String name, BSymbol owner) {
        return isReferenced(name, owner, null, null);
    }

    /**
     * Returns true if the node is referenced.
     *
     * @param name name of the node
     * @param owner owner of the node
     * @param pkgID package id of the node
     * @param ownerPkgId package id of the owner of the node
     * @return true if the node is referenced
     */
    private boolean isReferenced(String name, BSymbol owner, PackageID pkgID, PackageID ownerPkgId) {
        if (this.context.get(NodeContextKeys.NAME_OF_NODE_KEY) == null ||
                this.context.get(NodeContextKeys.PACKAGE_OF_NODE_KEY) == null ||
                this.context.get(NodeContextKeys.NODE_OWNER_PACKAGE_KEY) == null) {
            return false;
        }
        boolean isSameVar = this.context.get(NodeContextKeys.VAR_NAME_OF_NODE_KEY) != null &&
                this.context.get(NodeContextKeys.VAR_NAME_OF_NODE_KEY).equals(name);
        boolean isSameNode = this.context.get(NodeContextKeys.NAME_OF_NODE_KEY).equals(name);
        boolean isSameOwner = (owner == null || this.context.get(NodeContextKeys.NODE_OWNER_KEY).equals(
                owner.name.getValue()));
        boolean isSameOwnerPkg = ownerPkgId == null ||
                this.context.get(NodeContextKeys.NODE_OWNER_PACKAGE_KEY).name.getValue().
                        equals(ownerPkgId.name.getValue());
        boolean isSamePkg = pkgID == null ||
                this.context.get(NodeContextKeys.PACKAGE_OF_NODE_KEY).name.getValue().equals(pkgID.name.getValue());
        List<PackageID> packageIds = this.context.get(NodeContextKeys.REFERENCE_PKG_IDS_KEY);
        if (!isSamePkg && (isSameNode || isSameVar) && isSameOwner && isSameOwnerPkg && packageIds != null) {
            return packageIds.stream().anyMatch(pkgID::equals);
        }
        return isSamePkg && (isSameNode || isSameVar) && isSameOwner && isSameOwnerPkg;
    }
}
