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
package org.wso2.ballerinalang.compiler.parser;

import org.apache.commons.lang3.StringEscapeUtils;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.TreeUtils;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.ActionNode;
import org.ballerinalang.model.tree.AnnotatableNode;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.AnnotationNode;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.ballerinalang.model.tree.ConnectorNode;
import org.ballerinalang.model.tree.EnumNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.ImportPackageNode;
import org.ballerinalang.model.tree.InvokableNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.PackageDeclarationNode;
import org.ballerinalang.model.tree.ResourceNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.StructNode;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.WorkerNode;
import org.ballerinalang.model.tree.expressions.AnnotationAttachmentAttributeValueNode;
import org.ballerinalang.model.tree.expressions.ConnectorInitNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.LiteralNode;
import org.ballerinalang.model.tree.expressions.RecordTypeLiteralNode;
import org.ballerinalang.model.tree.expressions.SimpleVariableReferenceNode;
import org.ballerinalang.model.tree.expressions.XMLAttributeNode;
import org.ballerinalang.model.tree.expressions.XMLLiteralNode;
import org.ballerinalang.model.tree.statements.BlockNode;
import org.ballerinalang.model.tree.statements.ForkJoinNode;
import org.ballerinalang.model.tree.statements.IfNode;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.ballerinalang.model.tree.statements.TransactionNode;
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotAttribute;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangEnum;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNameReference;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttributeValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConnectorInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordTypeLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangAbort;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.QuoteType;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * This class builds the package AST of a Ballerina source file.
 *
 * @since 0.94
 */
public class BLangPackageBuilder {

    private CompilationUnitNode compUnit;

    private Stack<BLangNameReference> nameReferenceStack = new Stack<>();

    private Stack<TypeNode> typeNodeStack = new Stack<>();

    private Stack<List<TypeNode>> typeNodeListStack = new Stack<>();

    private Stack<BlockNode> blockNodeStack = new Stack<>();

    private Stack<VariableNode> varStack = new Stack<>();

    private Stack<List<VariableNode>> varListStack = new Stack<>();

    private Stack<InvokableNode> invokableNodeStack = new Stack<>();

    private Stack<ExpressionNode> exprNodeStack = new Stack<>();

    private Stack<List<ExpressionNode>> exprNodeListStack = new Stack<>();

    private Stack<RecordTypeLiteralNode> recordTypeLiteralNodes = new Stack<>();

    private Stack<BLangTryCatchFinally> tryCatchFinallyNodesStack = new Stack<>();

    private Stack<PackageID> pkgIdStack = new Stack<>();

    private Stack<StructNode> structStack = new Stack<>();

    private Stack<EnumNode> enumStack = new Stack<>();

    private Stack<IdentifierNode> identifierStack = new Stack<>();

    private Stack<ConnectorNode> connectorNodeStack = new Stack<>();

    private Stack<List<ActionNode>> actionNodeStack = new Stack<>();

    private Stack<AnnotationNode> annotationStack = new Stack<>();

    private Stack<AnnotationAttachmentAttributeValueNode> annotAttribValStack = new Stack<>();

    private Stack<AnnotationAttachmentNode> annotAttachmentStack = new Stack<>();

    private Stack<IfNode> ifElseStatementStack = new Stack<>();

    private Stack<TransactionNode> transactionNodeStack = new Stack<>();

    private Stack<ForkJoinNode> forkJoinNodesStack = new Stack<>();

    private Stack<ServiceNode> serviceNodeStack = new Stack<>();

    private Stack<XMLAttributeNode> xmlAttributeNodeStack = new Stack<>();

    private Stack<ConnectorInitNode> connectorInitNodeStack = new Stack<>();

    protected int lambdaFunctionCount = 0;

    public BLangPackageBuilder(CompilationUnitNode compUnit) {
        this.compUnit = compUnit;
    }

    public void addValueType(DiagnosticPos pos, Set<WSToken> ws, String typeName) {
        BLangValueType typeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        typeNode.addWS(ws);
        typeNode.pos = pos;
        typeNode.typeKind = (TreeUtils.stringToTypeKind(typeName));

        addType(typeNode);
    }

    public void addArrayType(DiagnosticPos pos, int dimensions) {
        BLangType eType = (BLangType) this.typeNodeStack.pop();
        BLangArrayType arrayTypeNode = (BLangArrayType) TreeBuilder.createArrayTypeNode();
        arrayTypeNode.pos = pos;
        arrayTypeNode.elemtype = eType;
        arrayTypeNode.dimensions = dimensions;

        addType(arrayTypeNode);
    }

    public void addUserDefineType() {
        BLangNameReference nameReference = nameReferenceStack.pop();
        BLangUserDefinedType userDefinedType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
        userDefinedType.pos = nameReference.pos;
        userDefinedType.pkgAlias = (BLangIdentifier) nameReference.pkgAlias;
        userDefinedType.typeName = (BLangIdentifier) nameReference.name;

        addType(userDefinedType);
    }

    public void addBuiltInReferenceType(DiagnosticPos pos, String typeName) {
        BLangBuiltInRefTypeNode refType = (BLangBuiltInRefTypeNode) TreeBuilder.createBuiltInReferenceTypeNode();
        refType.typeKind = TreeUtils.stringToTypeKind(typeName);
        refType.pos = pos;
        addType(refType);
    }

    public void addConstraintType(DiagnosticPos pos, String typeName) {
        // TODO : Fix map<int> format.
        BLangNameReference nameReference = nameReferenceStack.pop();
        BLangUserDefinedType constraintType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
        constraintType.pos = pos;
        constraintType.pkgAlias = (BLangIdentifier) nameReference.pkgAlias;
        constraintType.typeName = (BLangIdentifier) nameReference.name;

        BLangBuiltInRefTypeNode refType = (BLangBuiltInRefTypeNode) TreeBuilder.createBuiltInReferenceTypeNode();
        refType.typeKind = TreeUtils.stringToTypeKind(typeName);
        refType.pos = pos;

        BLangConstrainedType constrainedType = (BLangConstrainedType) TreeBuilder.createConstrainedTypeNode();
        constrainedType.type = refType;
        constrainedType.constraint = constraintType;
        constrainedType.pos = pos;

        addType(constrainedType);
    }

    public void addFunctionType(DiagnosticPos pos, boolean paramsAvail, boolean paramsTypeOnly,
                                boolean retParamsAvail, boolean retParamTypeOnly, boolean returnsKeywordExists) {
        // TODO : Fix function main ()(boolean , function(string x)(float, int)){} issue
        BLangFunctionTypeNode functionTypeNode = (BLangFunctionTypeNode) TreeBuilder.createFunctionTypeNode();
        functionTypeNode.pos = pos;
        functionTypeNode.returnsKeywordExists = returnsKeywordExists;

        if (retParamsAvail) {
            if (retParamTypeOnly) {
                functionTypeNode.returnParamTypeNodes.addAll(this.typeNodeListStack.pop());
            } else {
                this.varListStack.pop().forEach(v -> functionTypeNode.returnParamTypeNodes.add(v.getTypeNode()));
            }
        }
        if (paramsAvail) {
            if (paramsTypeOnly) {
                functionTypeNode.paramTypeNodes.addAll(this.typeNodeListStack.pop());
            } else {
                this.varListStack.pop().forEach(v -> functionTypeNode.paramTypeNodes.add(v.getTypeNode()));
            }
        }

        addType(functionTypeNode);
    }

    private void addType(TypeNode typeNode) {
        this.typeNodeStack.push(typeNode);
    }

    public void addNameReference(String pkgName, String name, DiagnosticPos currentPos) {
        nameReferenceStack.push(new BLangNameReference(createIdentifier(pkgName), createIdentifier(name), currentPos));
    }

    public void startVarList() {
        this.varListStack.push(new ArrayList<>());
    }

    public void startFunctionDef() {
        FunctionNode functionNode = TreeBuilder.createFunctionNode();
        attachAnnotations(functionNode);
        this.invokableNodeStack.push(functionNode);
    }

    public void startBlock() {
        this.blockNodeStack.push(TreeBuilder.createBlockNode());
    }

    private IdentifierNode createIdentifier(String identifier) {
        IdentifierNode node = TreeBuilder.createIdentifierNode();
        if (identifier != null) {
            node.setValue(identifier);
        }
        return node;
    }

    public void addVar(DiagnosticPos pos, Set<WSToken> ws, String identifier, boolean exprAvailable, int annotCount) {
        BLangVariable var = (BLangVariable) this.generateBasicVarNode(pos, ws, identifier, exprAvailable);
        attachAnnotations(var, annotCount);
        var.pos = pos;
        if (this.varListStack.empty()) {
            this.varStack.push(var);
        } else {
            this.varListStack.peek().add(var);
        }
    }

    public void endCallableUnitSignature(Set<WSToken> ws, String identifier, boolean paramsAvail,
                                         boolean retParamsAvail, boolean retParamTypeOnly) {
        InvokableNode invNode = this.invokableNodeStack.peek();
        invNode.setName(this.createIdentifier(identifier));
        invNode.addWS(ws);
        if (retParamsAvail) {
            if (retParamTypeOnly) {
                this.typeNodeListStack.pop().forEach(e -> {
                    VariableNode var = TreeBuilder.createVariableNode();
                    var.setTypeNode(e);

                    // Create an empty name node
                    IdentifierNode nameNode = TreeBuilder.createIdentifierNode();
                    nameNode.setValue("");
                    var.setName(nameNode);
                    invNode.addReturnParameter(var);
                });
            } else {
                this.varListStack.pop().forEach(invNode::addReturnParameter);
            }
        }
        if (paramsAvail) {
            this.varListStack.pop().forEach(invNode::addParameter);
        }
    }

    public void startLambdaFunctionDef() {
        startFunctionDef();
        BLangFunction lambdaFunction = (BLangFunction) this.invokableNodeStack.peek();
        lambdaFunction.setName(createIdentifier("$lambda$" + lambdaFunctionCount++));
        lambdaFunction.addFlag(Flag.LAMBDA);
    }

    public void addLambdaFunctionDef(DiagnosticPos pos, Set<WSToken> ws, boolean paramsAvail, boolean retParamsAvail,
                                     boolean retParamTypeOnly) {
        BLangFunction lambdaFunction = (BLangFunction) this.invokableNodeStack.peek();
        lambdaFunction.pos = pos;
        endCallableUnitSignature(ws, lambdaFunction.getName().value, paramsAvail, retParamsAvail, retParamTypeOnly);
        BLangLambdaFunction lambdaExpr = (BLangLambdaFunction) TreeBuilder.createLambdaFunctionNode();
        lambdaExpr.function = lambdaFunction;
        lambdaExpr.pos = pos;
        addExpressionNode(lambdaExpr);
        // TODO: is null correct here
        endFunctionDef(null);
    }

    public void addVariableDefStatement(DiagnosticPos pos, String identifier, boolean exprAvailable) {
        BLangVariable var = (BLangVariable) TreeBuilder.createVariableNode();
        var.pos = pos;
        var.setName(this.createIdentifier(identifier));
        var.setTypeNode(this.typeNodeStack.pop());
        if (exprAvailable) {
            var.setInitialExpression(this.exprNodeStack.pop());
        }

        BLangVariableDef varDefNode = (BLangVariableDef) TreeBuilder.createVariableDefinitionNode();
        varDefNode.pos = pos;
        varDefNode.setVariable(var);
        addStmtToCurrentBlock(varDefNode);
    }

    public void addConnectorVarDeclaration(DiagnosticPos pos, String identifier, boolean exprAvailable) {
        addVariableDefStatement(pos, identifier, exprAvailable);
    }

    public void addConnectorInitExpression(DiagnosticPos pos, boolean exprAvailable) {
        BLangConnectorInit connectorInitNode = (BLangConnectorInit) TreeBuilder.createConnectorInitNode();
        connectorInitNode.pos = pos;
        connectorInitNode.connectorType = (BLangUserDefinedType) typeNodeStack.pop();
        if (exprAvailable) {
            connectorInitNode.argsExpressions = this.exprNodeListStack.pop();
        }
        ConnectorInitNode previous = null;
        while (!connectorInitNodeStack.empty()) {
            connectorInitNode.filterConnectors.add(0, connectorInitNodeStack.pop());
        }
        this.addExpressionNode(connectorInitNode);
    }

    public void addFilterConnectorInitExpression(DiagnosticPos pos, boolean exprAvailable) {
        BLangConnectorInit connectorInitNode = (BLangConnectorInit) TreeBuilder.createConnectorInitNode();
        connectorInitNode.pos = pos;
        connectorInitNode.connectorType = (BLangUserDefinedType) typeNodeStack.pop();
        if (exprAvailable) {
            connectorInitNode.argsExpressions = this.exprNodeListStack.pop();
        }
        this.connectorInitNodeStack.push(connectorInitNode);
    }

    private void addStmtToCurrentBlock(StatementNode statement) {
        this.blockNodeStack.peek().addStatement(statement);
    }

    public void startTryCatchFinallyStmt() {
        this.tryCatchFinallyNodesStack.push((BLangTryCatchFinally) TreeBuilder.createTryCatchFinallyNode());
        startBlock();
    }

    public void addTryClause(DiagnosticPos pos) {
        BLangBlockStmt tryBlock = (BLangBlockStmt) this.blockNodeStack.pop();
        tryBlock.pos = pos;
        tryCatchFinallyNodesStack.peek().tryBody = tryBlock;
    }

    public void startCatchClause() {
        startBlock();
    }

    public void addCatchClause(DiagnosticPos poc, String paramName) {
        BLangSimpleVarRef varRef =
                (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
        varRef.variableName = (BLangIdentifier) createIdentifier(paramName);

        BLangVariable variableNode = (BLangVariable) TreeBuilder.createVariableNode();
        variableNode.typeNode = (BLangType) this.typeNodeStack.pop();
        variableNode.name = (BLangIdentifier) createIdentifier(paramName);
        variableNode.expr = varRef;

        BLangCatch catchNode = (BLangCatch) TreeBuilder.createCatchNode();
        catchNode.pos = poc;
        catchNode.body = (BLangBlockStmt) this.blockNodeStack.pop();
        catchNode.param = variableNode;
        tryCatchFinallyNodesStack.peek().catchBlocks.add(catchNode);
    }

    public void startFinallyBlock() {
        startBlock();
    }

    public void addFinallyBlock(DiagnosticPos poc) {
        tryCatchFinallyNodesStack.peek().finallyBody = (BLangBlockStmt) this.blockNodeStack.pop();
        tryCatchFinallyNodesStack.peek().finallyBody.pos = poc;
    }

    public void addTryCatchFinallyStmt(DiagnosticPos poc) {
        BLangTryCatchFinally stmtNode = tryCatchFinallyNodesStack.pop();
        stmtNode.pos = poc;
        addStmtToCurrentBlock(stmtNode);
    }

    public void addThrowStmt(DiagnosticPos poc) {
        ExpressionNode throwExpr = this.exprNodeStack.pop();
        BLangThrow throwNode = (BLangThrow) TreeBuilder.createThrowNode();
        throwNode.pos = poc;
        throwNode.expr = (BLangExpression) throwExpr;
        addStmtToCurrentBlock(throwNode);
    }

    private void addExpressionNode(ExpressionNode expressionNode) {
        this.exprNodeStack.push(expressionNode);
    }

    public void addLiteralValue(DiagnosticPos pos, int typeTag, Object value) {
        BLangLiteral litExpr = (BLangLiteral) TreeBuilder.createLiteralExpression();
        litExpr.pos = pos;
        litExpr.typeTag = typeTag;
        litExpr.value = value;
        addExpressionNode(litExpr);
    }

    public void addArrayInitExpr(DiagnosticPos pos, boolean argsAvailable) {
        List<ExpressionNode> argExprList;
        if (argsAvailable) {
            argExprList = exprNodeListStack.pop();
        } else {
            argExprList = new ArrayList<>(0);
        }
        BLangArrayLiteral arrayLiteral = (BLangArrayLiteral) TreeBuilder.createArrayLiteralNode();
        arrayLiteral.expressionNodes = argExprList;
        arrayLiteral.pos = pos;
        addExpressionNode(arrayLiteral);
    }

    public void addKeyValueRecord() {
        ExpressionNode valueExpr = exprNodeStack.pop();
        ExpressionNode keyExpr = exprNodeStack.pop();
        IdentifierNode identifierNode = null;
        if (keyExpr instanceof BLangLiteral) {
            identifierNode = createIdentifier(((BLangLiteral) keyExpr).getValue().toString());
            identifierNode.setLiteral(true);
        } else if (keyExpr instanceof SimpleVariableReferenceNode) {
            identifierNode = ((SimpleVariableReferenceNode) keyExpr).getVariableName();
        }
        recordTypeLiteralNodes.peek().getKeyValuePairs().put(identifierNode, valueExpr);
    }

    public void addMapStructLiteral(DiagnosticPos pos) {
        BLangRecordTypeLiteral recordTypeLiteralNode = (BLangRecordTypeLiteral) recordTypeLiteralNodes.pop();
        recordTypeLiteralNode.pos = pos;
        addExpressionNode(recordTypeLiteralNode);
    }

    public void startMapStructLiteral() {
        BLangRecordTypeLiteral literalNode = (BLangRecordTypeLiteral) TreeBuilder.createRecordTypeLiteralNode();
        recordTypeLiteralNodes.push(literalNode);
    }

    public void startExprNodeList() {
        this.exprNodeListStack.push(new ArrayList<>());
    }

    public void endExprNodeList(int exprCount) {
        List<ExpressionNode> exprList = exprNodeListStack.peek();
        addExprToExprNodeList(exprList, exprCount);
    }

    private void addExprToExprNodeList(List<ExpressionNode> exprList, int n) {
        if (exprNodeStack.isEmpty()) {
            throw new IllegalStateException("Expression stack cannot be empty in processing an ExpressionList");
        }
        ExpressionNode expr = exprNodeStack.pop();
        if (n > 1) {
            addExprToExprNodeList(exprList, n - 1);
        }
        exprList.add(expr);
    }


    public void createSimpleVariableReference(DiagnosticPos pos) {
        BLangNameReference nameReference = nameReferenceStack.pop();
        BLangSimpleVarRef varRef = (BLangSimpleVarRef) TreeBuilder
                .createSimpleVariableReferenceNode();
        varRef.pos = pos;
        varRef.packageIdentifier = (BLangIdentifier) nameReference.pkgAlias;
        varRef.variableName = (BLangIdentifier) nameReference.name;
        this.exprNodeStack.push(varRef);
    }

    public void createFunctionInvocation(DiagnosticPos pos, boolean argsAvailable) {
        BLangInvocation invocationNode = (BLangInvocation) TreeBuilder.createInvocationNode();
        invocationNode.pos = pos;
        if (argsAvailable) {
            List<ExpressionNode> exprNodes = exprNodeListStack.pop();
            exprNodes.forEach(exprNode -> invocationNode.argsExprs.add((BLangExpression) exprNode));
        }

        BLangNameReference nameReference = nameReferenceStack.pop();
        invocationNode.functionName = (BLangIdentifier) nameReference.name;
        invocationNode.pkgAlias = (BLangIdentifier) nameReference.pkgAlias;
        addExpressionNode(invocationNode);
    }

    public void createInvocationNode(DiagnosticPos pos, String invocation, boolean argsAvailable) {
        BLangInvocation invocationNode = (BLangInvocation) TreeBuilder.createInvocationNode();
        invocationNode.pos = pos;
        if (argsAvailable) {
            List<ExpressionNode> exprNodes = exprNodeListStack.pop();
            exprNodes.forEach(exprNode -> invocationNode.argsExprs.add((BLangExpression) exprNode));
        }

        invocationNode.variableReferenceNode = (BLangVariableReference) exprNodeStack.pop();
        invocationNode.functionName = (BLangIdentifier) createIdentifier(invocation);
        invocationNode.pkgAlias = (BLangIdentifier) createIdentifier(null);
        addExpressionNode(invocationNode);
    }

    public void createFieldBasedAccessNode(DiagnosticPos pos, String fieldName) {
        BLangFieldBasedAccess fieldBasedAccess = (BLangFieldBasedAccess) TreeBuilder.createFieldBasedAccessNode();
        fieldBasedAccess.pos = pos;
        fieldBasedAccess.fieldName = createIdentifier(fieldName);
        fieldBasedAccess.expressionNode = exprNodeStack.pop();
        addExpressionNode(fieldBasedAccess);
    }

    public void createIndexBasedAccessNode(DiagnosticPos pos) {
        BLangIndexBasedAccess indexBasedAccess = (BLangIndexBasedAccess) TreeBuilder.createIndexBasedAccessNode();
        indexBasedAccess.pos = pos;
        indexBasedAccess.index = exprNodeStack.pop();
        indexBasedAccess.expression = exprNodeStack.pop();
        addExpressionNode(indexBasedAccess);
    }

    public void createBinaryExpr(DiagnosticPos pos, String operator) {
        BLangBinaryExpr binaryExpressionNode = (BLangBinaryExpr) TreeBuilder.createBinaryExpressionNode();
        binaryExpressionNode.pos = pos;
        binaryExpressionNode.rhsExpr = (BLangExpression) exprNodeStack.pop();
        binaryExpressionNode.lhsExpr = (BLangExpression) exprNodeStack.pop();
        binaryExpressionNode.opKind = OperatorKind.valueFrom(operator);
        addExpressionNode(binaryExpressionNode);
    }

    public void createTypeCastExpr(DiagnosticPos pos) {
        BLangTypeCastExpr typeCastNode = (BLangTypeCastExpr) TreeBuilder.createTypeCastNode();
        typeCastNode.pos = pos;
        typeCastNode.expr = (BLangExpression) exprNodeStack.pop();
        typeCastNode.typeNode = (BLangType) typeNodeStack.pop();
        addExpressionNode(typeCastNode);
    }

    public void createTypeConversionExpr(DiagnosticPos pos) {
        BLangTypeConversionExpr typeConversionNode = (BLangTypeConversionExpr) TreeBuilder.createTypeConversionNode();
        typeConversionNode.pos = pos;
        typeConversionNode.expr = exprNodeStack.pop();
        typeConversionNode.typeName = typeNodeStack.pop();
        addExpressionNode(typeConversionNode);
    }

    public void createUnaryExpr(DiagnosticPos pos, String operator) {
        BLangUnaryExpr unaryExpressionNode = (BLangUnaryExpr) TreeBuilder.createUnaryExpressionNode();
        unaryExpressionNode.pos = pos;
        unaryExpressionNode.expressionNode = exprNodeStack.pop();
        unaryExpressionNode.operator = OperatorKind.valueFrom(operator);
        addExpressionNode(unaryExpressionNode);
    }

    public void createTernaryExpr(DiagnosticPos pos) {
        BLangTernaryExpr ternaryExpr = (BLangTernaryExpr) TreeBuilder.createTernaryExpressionNode();
        ternaryExpr.pos = pos;
        ternaryExpr.elseExpr = (BLangExpression) exprNodeStack.pop();
        ternaryExpr.thenExpr = (BLangExpression) exprNodeStack.pop();
        ternaryExpr.expr = (BLangExpression) exprNodeStack.pop();
    }

    public void endFunctionDef(Set<WSToken> ws) {
        FunctionNode function = (FunctionNode) this.invokableNodeStack.pop();
        function.addWS(ws);
        this.compUnit.addTopLevelNode(function);
    }

    public void startWorker() {
        WorkerNode workerNode = TreeBuilder.createWorkerNode();
        this.invokableNodeStack.push(workerNode);
        startBlock();
    }

    public void addWorker(DiagnosticPos pos, String workerName) {
        BLangWorker worker = (BLangWorker) this.invokableNodeStack.pop();
        worker.setName(createIdentifier(workerName));
        worker.pos = pos;
        worker.setBody(this.blockNodeStack.pop());
        if (this.forkJoinNodesStack.empty()) {
            InvokableNode invokableNode = this.invokableNodeStack.peek();
            invokableNode.getParameters().forEach(worker::addParameter);
            invokableNode.getReturnParameters().forEach(worker::addReturnParameter);
            invokableNode.addWorker(worker);
            invokableNode.addFlag(Flag.PARALLEL);
        } else {
            ((BLangForkJoin) this.forkJoinNodesStack.peek()).workers.add(worker);
        }
    }

    public void startForkJoinStmt() {
        this.forkJoinNodesStack.push(TreeBuilder.createForkJoinNode());
    }

    public void addForkJoinStmt(DiagnosticPos pos) {
        BLangForkJoin forkJoin = (BLangForkJoin) this.forkJoinNodesStack.pop();
        forkJoin.pos = pos;
        this.addStmtToCurrentBlock(forkJoin);
    }

    public void startJoinCause() {
        startBlock();
    }

    public void addJoinCause() {
        BLangForkJoin forkJoin = (BLangForkJoin) this.forkJoinNodesStack.peek();
        forkJoin.joinedBody = (BLangBlockStmt) this.blockNodeStack.pop();
    }

    public void addJoinCondition(String joinType, List<String> workerNames, int joinCount) {
        BLangForkJoin forkJoin = (BLangForkJoin) this.forkJoinNodesStack.peek();
        forkJoin.joinedWorkerCount = joinCount;
        forkJoin.joinType = ForkJoinNode.JoinType.valueOf(joinType);
        workerNames.forEach(s -> forkJoin.joinedWorkers.add((BLangIdentifier) createIdentifier(s)));
    }

    public void startTimeoutCause() {
        startBlock();
    }

    public void addTimeoutCause(String paramName) {
        BLangSimpleVarRef varRef =
                (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
        varRef.variableName = (BLangIdentifier) createIdentifier(paramName);

        BLangVariable variableNode = (BLangVariable) TreeBuilder.createVariableNode();
        variableNode.typeNode = (BLangType) this.typeNodeStack.pop();
        variableNode.name = (BLangIdentifier) createIdentifier(paramName);
        variableNode.expr = varRef;

        BLangForkJoin forkJoin = (BLangForkJoin) this.forkJoinNodesStack.peek();
        forkJoin.timeoutBody = (BLangBlockStmt) this.blockNodeStack.pop();
        forkJoin.timeoutExpression = this.exprNodeStack.pop();
        forkJoin.timeoutVariable = variableNode;
    }

    public void endCallableUnitBody(Set<WSToken> ws) {
        BlockNode block = this.blockNodeStack.pop();
        block.addWS(ws);
        this.invokableNodeStack.peek().setBody(block);
    }

    public void addPackageId(List<String> nameComps, String version) {
        List<IdentifierNode> nameCompNodes = new ArrayList<>();
        IdentifierNode versionNode;
        if (version != null) {
            versionNode = TreeBuilder.createIdentifierNode();
            versionNode.setValue(version);
        } else {
            versionNode = null;
        }
        nameComps.forEach(e -> nameCompNodes.add(this.createIdentifier(e)));
        this.pkgIdStack.add(new PackageID(nameCompNodes, versionNode));
    }

    public void populatePackageDeclaration() {
        PackageDeclarationNode pkgDecl = TreeBuilder.createPackageDeclarationNode();
        pkgDecl.setPackageID(this.pkgIdStack.pop());
        this.compUnit.addTopLevelNode(pkgDecl);
    }

    public void addImportPackageDeclaration(Set<WSToken> ws, String alias) {
        ImportPackageNode impDecl = TreeBuilder.createImportPackageNode();
        IdentifierNode aliasNode;
        if (alias != null) {
            aliasNode = this.createIdentifier(alias);
        } else {
            aliasNode = null;
        }
        impDecl.setPackageID(this.pkgIdStack.pop());
        impDecl.setAlias(aliasNode);
        impDecl.addWS(ws);
        this.compUnit.addTopLevelNode(impDecl);
    }

    private VariableNode generateBasicVarNode(DiagnosticPos pos, Set<WSToken> ws, String identifier, boolean exprAvailable) {
        BLangVariable var = (BLangVariable) TreeBuilder.createVariableNode();
        var.pos = pos;
        IdentifierNode name = this.createIdentifier(identifier);
        name.addWS(ws);
        var.setName(name);
        var.setTypeNode(this.typeNodeStack.pop());
        if (exprAvailable) {
            var.setInitialExpression(this.exprNodeStack.pop());
        }
        return var;
    }

    public void addGlobalVariable(DiagnosticPos pos, Set<WSToken> ws, String identifier, boolean exprAvailable) {
        VariableNode var = this.generateBasicVarNode(pos, ws, identifier, exprAvailable);
        this.compUnit.addTopLevelNode(var);
    }

    public void addConstVariable(DiagnosticPos pos, Set<WSToken> ws, String identifier) {
        VariableNode var = this.generateBasicVarNode(pos, ws, identifier, true);
        var.addFlag(Flag.CONST);
        this.compUnit.addTopLevelNode(var);
    }

    public void startStructDef() {
        StructNode structNode = TreeBuilder.createStructNode();
        attachAnnotations(structNode);
        this.structStack.add(structNode);
    }

    public void endStructDef(DiagnosticPos pos, String identifier) {
        BLangStruct structNode = (BLangStruct) this.structStack.pop();
        structNode.pos = pos;
        structNode.setName(this.createIdentifier(identifier));
        this.varListStack.pop().forEach(structNode::addField);
        this.compUnit.addTopLevelNode(structNode);
    }

    public void startEnumDef(DiagnosticPos currentPos) {
        BLangEnum bLangEnum = (BLangEnum) TreeBuilder.createEnumNode();
        bLangEnum.pos = currentPos;
        this.enumStack.add(bLangEnum);
    }

    public void endEnumDef(String identifier) {
        EnumNode enumNode = this.enumStack.pop();
        enumNode.setName(this.createIdentifier(identifier));
        while (!this.identifierStack.empty()) {
            enumNode.addEnumField(this.identifierStack.pop());
        }
        this.compUnit.addTopLevelNode(enumNode);
    }

    public void addEnumFieldList(List<String> enumFieldList) {
        enumFieldList.forEach(identifier -> this.identifierStack.push(this.createIdentifier(identifier)));
    }

    public void startConnectorDef() {
        ConnectorNode connectorNode = TreeBuilder.createConnectorNode();
        attachAnnotations(connectorNode);
        this.connectorNodeStack.push(connectorNode);
    }

    public void startConnectorBody() {
        /* end of connector definition header, so let's populate 
         * the connector information before processing the body */
        ConnectorNode connectorNode = this.connectorNodeStack.peek();
        if (!this.varStack.empty()) {
            connectorNode.setFilteredParamter(this.varStack.pop());
        }
        if (!this.varListStack.empty()) {
            this.varListStack.pop().forEach(connectorNode::addParameter);
        }
        /* add a temporary block node to contain connector variable definitions */
        this.blockNodeStack.add(TreeBuilder.createBlockNode());
        /* action node list to contain the actions of the connector */
        this.actionNodeStack.add(new ArrayList<>());
    }

    public void endConnectorDef(DiagnosticPos pos, String identifier) {
        BLangConnector connectorNode = (BLangConnector) this.connectorNodeStack.pop();
        connectorNode.pos = pos;
        connectorNode.setName(this.createIdentifier(identifier));
        this.compUnit.addTopLevelNode(connectorNode);
    }

    public void endConnectorBody() {
        ConnectorNode connectorNode = this.connectorNodeStack.peek();
        this.blockNodeStack.pop().getStatements().forEach(
                e -> connectorNode.addVariableDef((VariableDefinitionNode) e));
        this.actionNodeStack.pop().forEach(connectorNode::addAction);
    }

    public void startActionDef() {
        ActionNode actionNode = TreeBuilder.createActionNode();
        this.invokableNodeStack.push(actionNode);
    }

    public void endActionDef(int annotCount) {
        ActionNode actionNode = (ActionNode) this.invokableNodeStack.pop();
        attachAnnotations(actionNode, annotCount);
        this.connectorNodeStack.peek().addAction(actionNode);
    }

    public void startProcessingTypeNodeList() {
        this.typeNodeListStack.push(new ArrayList<>());
    }

    public void endProcessingTypeNodeList(int size) {
        for (int i = 0; i < size; i++) {
            this.typeNodeListStack.peek().add(0, typeNodeStack.pop());
        }
    }

    public void startAnnotationDef(DiagnosticPos pos) {
        BLangAnnotation annotNode = (BLangAnnotation) TreeBuilder.createAnnotationNode();
        annotNode.pos = pos;
        attachAnnotations(annotNode);
        this.annotationStack.add(annotNode);
    }

    public void endAnnotationDef(String identifier) {
        AnnotationNode annotationNode = this.annotationStack.pop();
        annotationNode.setName(this.createIdentifier(identifier));
        this.varListStack.pop().forEach(var -> {
            BLangVariable variable = (BLangVariable) var;
            BLangAnnotAttribute annAttrNode = (BLangAnnotAttribute) TreeBuilder.createAnnotAttributeNode();
            var.getFlags().forEach(annAttrNode::addFlag);
            var.getAnnotationAttachments().forEach(annAttrNode::addAnnotationAttachment);
            annAttrNode.typeNode = variable.typeNode;
            annAttrNode.expr = variable.expr;
            annAttrNode.name = variable.name;
            annAttrNode.pos = variable.pos;

            // add the attribute to the annotation definition
            annotationNode.addAttribute(annAttrNode);
        });

        this.compUnit.addTopLevelNode(annotationNode);
    }

    public void startAnnotationAttachment(DiagnosticPos currentPos) {
        BLangAnnotationAttachment annotAttachmentNode =
                (BLangAnnotationAttachment) TreeBuilder.createAnnotAttachmentNode();
        annotAttachmentNode.pos = currentPos;
        annotAttachmentStack.push(annotAttachmentNode);
    }

    public void setAnnotationAttachmentName(String annotationName) {
        annotAttachmentStack.peek().setAnnotationName(createIdentifier(annotationName));
    }

    public void createLiteralTypeAttributeValue(DiagnosticPos currentPos) {
        createAnnotAttribValueFromExpr(currentPos);
    }

    public void createVarRefTypeAttributeValue(DiagnosticPos currentPos) {
        createAnnotAttribValueFromExpr(currentPos);
    }

    public void createAnnotationTypeAttributeValue(DiagnosticPos currentPos) {
        BLangAnnotAttachmentAttributeValue annotAttrVal =
                (BLangAnnotAttachmentAttributeValue) TreeBuilder.createAnnotAttributeValueNode();
        annotAttrVal.pos = currentPos;
        annotAttrVal.setValue(annotAttachmentStack.pop());
        annotAttribValStack.push(annotAttrVal);
    }

    public void createArrayTypeAttributeValue(DiagnosticPos currentPos) {
        BLangAnnotAttachmentAttributeValue annotAttrVal =
                (BLangAnnotAttachmentAttributeValue) TreeBuilder.createAnnotAttributeValueNode();
        annotAttrVal.pos = currentPos;
        while (!annotAttribValStack.isEmpty()) {
            annotAttrVal.addValue(annotAttribValStack.pop());
        }
        annotAttribValStack.push(annotAttrVal);
    }

    public void createAnnotAttachmentAttribute(DiagnosticPos pos, String attrName) {
        annotAttachmentStack.peek().addAttribute(attrName, annotAttribValStack.pop());
    }

    private void createAnnotAttribValueFromExpr(DiagnosticPos currentPos) {
        BLangAnnotAttachmentAttributeValue annotAttrVal =
                (BLangAnnotAttachmentAttributeValue) TreeBuilder.createAnnotAttributeValueNode();
        annotAttrVal.pos = currentPos;
        annotAttrVal.setValue(exprNodeStack.pop());
        annotAttribValStack.push(annotAttrVal);
    }

    private void attachAnnotations(AnnotatableNode annotatableNode) {
        annotAttachmentStack.forEach(annot -> annotatableNode.addAnnotationAttachment(annot));
        annotAttachmentStack.clear();
    }

    private void attachAnnotations(AnnotatableNode annotatableNode, int count) {
        if (count == 0 || annotAttachmentStack.empty()) {
            return;
        }

        List<AnnotationAttachmentNode> tempAnnotAttachments = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            if (annotAttachmentStack.empty()) {
                break;
            }
            tempAnnotAttachments.add(annotAttachmentStack.pop());
        }
        // reversing the collected annotations to preserve the original order
        Collections.reverse(tempAnnotAttachments);
        tempAnnotAttachments.forEach(annot -> annotatableNode.addAnnotationAttachment(annot));
    }

    public void addAssignmentStatement(DiagnosticPos pos, boolean isVarDeclaration) {
        ExpressionNode rExprNode = exprNodeStack.pop();
        List<ExpressionNode> lExprList = exprNodeListStack.pop();
        BLangAssignment assignmentNode = (BLangAssignment) TreeBuilder.createAssignmentNode();
        assignmentNode.setExpression(rExprNode);
        assignmentNode.setDeclaredWithVar(isVarDeclaration);
        assignmentNode.pos = pos;
        lExprList.forEach(expressionNode -> assignmentNode.addVariable((BLangVariableReference) expressionNode));
        addStmtToCurrentBlock(assignmentNode);
    }

    public void startWhileStmt() {
        startBlock();
    }

    public void addWhileStmt(DiagnosticPos pos) {
        BLangWhile whileNode = (BLangWhile) TreeBuilder.createWhileNode();
        whileNode.setCondition(exprNodeStack.pop());
        whileNode.pos = pos;
        BLangBlockStmt whileBlock = (BLangBlockStmt) this.blockNodeStack.pop();
        whileBlock.pos = pos;
        whileNode.setBody(whileBlock);
        addStmtToCurrentBlock(whileNode);
    }

    public void addContinueStatement(DiagnosticPos pos) {
        BLangContinue continueNode = (BLangContinue) TreeBuilder.createContinueNode();
        continueNode.pos = pos;
        addStmtToCurrentBlock(continueNode);
    }

    public void addBreakStatement(DiagnosticPos pos) {
        BLangBreak breakNode = (BLangBreak) TreeBuilder.createBreakNode();
        breakNode.pos = pos;
        addStmtToCurrentBlock(breakNode);
    }

    public void startTransactionStmt() {
        transactionNodeStack.push(TreeBuilder.createTransactionNode());
        startBlock();
    }

    public void addTransactionBlock(DiagnosticPos pos) {
        TransactionNode transactionNode = transactionNodeStack.peek();
        BLangBlockStmt transactionBlock = (BLangBlockStmt) this.blockNodeStack.pop();
        transactionBlock.pos = pos;
        transactionNode.setTransactionBody(transactionBlock);
    }

    public void startFailedBlock() {
        startBlock();
    }

    public void addFailedBlock(DiagnosticPos pos) {
        TransactionNode transactionNode = transactionNodeStack.peek();
        BLangBlockStmt failedBlock = (BLangBlockStmt) this.blockNodeStack.pop();
        failedBlock.pos = pos;
        transactionNode.setFailedBody(failedBlock);
    }

    public void startCommittedBlock() {
        startBlock();
    }

    public void addCommittedBlock(DiagnosticPos pos) {
        TransactionNode transactionNode = transactionNodeStack.peek();
        BLangBlockStmt committedBlock = (BLangBlockStmt) this.blockNodeStack.pop();
        committedBlock.pos = pos;
        transactionNode.setCommittedBody(committedBlock);
    }

    public void startAbortedBlock() {
        startBlock();
    }

    public void addAbortedBlock(DiagnosticPos pos) {
        TransactionNode transactionNode = transactionNodeStack.peek();
        BLangBlockStmt abortedBlock = (BLangBlockStmt) this.blockNodeStack.pop();
        abortedBlock.pos = pos;
        transactionNode.setAbortedBody(abortedBlock);
    }

    public void endTransactionStmt(DiagnosticPos pos) {
        BLangTransaction transaction = (BLangTransaction) transactionNodeStack.pop();
        transaction.pos = pos;
        addStmtToCurrentBlock(transaction);
    }

    public void addAbortStatement(DiagnosticPos pos) {
        BLangAbort abortNode = (BLangAbort) TreeBuilder.createAbortNode();
        abortNode.pos = pos;
        addStmtToCurrentBlock(abortNode);
    }

    public void startIfElseNode(DiagnosticPos pos) {
        BLangIf ifNode = (BLangIf) TreeBuilder.createIfElseStatementNode();
        ifNode.pos = pos;
        ifElseStatementStack.push(ifNode);
        startBlock();
    }

    public void addRetrytmt() {
        TransactionNode transactionNode = transactionNodeStack.peek();
        transactionNode.setRetryCount(exprNodeStack.pop());
    }

    public void addIfBlock() {
        IfNode ifNode = ifElseStatementStack.peek();
        ifNode.setCondition(exprNodeStack.pop());
        ifNode.setBody(blockNodeStack.pop());
    }

    public void addElseIfBlock() {
        IfNode elseIfNode = ifElseStatementStack.pop();
        elseIfNode.setCondition(exprNodeStack.pop());
        elseIfNode.setBody(blockNodeStack.pop());

        IfNode parentIfNode = ifElseStatementStack.peek();
        while (parentIfNode.getElseStatement() != null) {
            parentIfNode = (IfNode) parentIfNode.getElseStatement();
        }
        parentIfNode.setElseStatement(elseIfNode);
    }

    public void addElseBlock() {
        IfNode ifNode = ifElseStatementStack.peek();
        while (ifNode.getElseStatement() != null) {
            ifNode = (IfNode) ifNode.getElseStatement();
        }
        ifNode.setElseStatement(blockNodeStack.pop());
    }

    public void endIfElseNode() {
        addStmtToCurrentBlock(ifElseStatementStack.pop());
    }

    public void addWorkerSendStmt(DiagnosticPos pos, String workerName, boolean isForkJoinSend) {
        BLangWorkerSend workerSendNode = (BLangWorkerSend) TreeBuilder.createWorkerSendNode();
        workerSendNode.workerIdentifier = createIdentifier(workerName);
        workerSendNode.expressions = exprNodeListStack.pop();
        workerSendNode.isForkJoinSend = isForkJoinSend;
        addStmtToCurrentBlock(workerSendNode);
    }

    public void addWorkerReceiveStmt(DiagnosticPos pos, String workerName) {
        BLangWorkerReceive workerReceiveNode = (BLangWorkerReceive) TreeBuilder.createWorkerReceiveNode();
        workerReceiveNode.workerIdentifier = createIdentifier(workerName);
        workerReceiveNode.expressions = exprNodeListStack.pop();
        addStmtToCurrentBlock(workerReceiveNode);
    }

    public void addExpressionStmt(DiagnosticPos pos) {
        BLangExpressionStmt exprStmt = (BLangExpressionStmt) TreeBuilder.createExpressionStatementNode();
        exprStmt.pos = pos;
        exprStmt.expr = (BLangExpression) exprNodeStack.pop();
        addStmtToCurrentBlock(exprStmt);
    }

    public void startServiceDef(DiagnosticPos pos) {
        BLangService serviceNode = (BLangService) TreeBuilder.createServiceNode();
        serviceNode.pos = pos;
        attachAnnotations(serviceNode);
        serviceNodeStack.push(serviceNode);
    }

    public void addServiceBody() {
        ServiceNode serviceNode = serviceNodeStack.peek();
        blockNodeStack.pop().getStatements()
                .forEach(varDef -> serviceNode.addVariable((VariableDefinitionNode) varDef));
    }

    public void endServiceDef(String protocolPkg, String serviceName) {
        ServiceNode serviceNode = serviceNodeStack.pop();
        serviceNode.setName(createIdentifier(serviceName));
        serviceNode.setProtocolPackageIdentifier(createIdentifier(protocolPkg));
        this.compUnit.addTopLevelNode(serviceNode);
    }

    public void startResourceDef() {
        ResourceNode resourceNode = TreeBuilder.createResourceNode();
        invokableNodeStack.push(resourceNode);
    }

    public void endResourceDef(String resourceName, int annotCount) {
        ResourceNode resourceNode = (ResourceNode) invokableNodeStack.pop();
        resourceNode.setName(createIdentifier(resourceName));
        attachAnnotations(resourceNode, annotCount);
        varListStack.pop().forEach(resourceNode::addParameter);
        serviceNodeStack.peek().addResource(resourceNode);
    }

    public void createXMLQName(DiagnosticPos pos, String localname, String prefix) {
        BLangXMLQName qname = (BLangXMLQName) TreeBuilder.createXMLQNameNode();
        qname.localname = localname;
        qname.prefix = prefix;
        qname.pos = pos;
        addExpressionNode(qname);
    }

    public void createXMLAttribute(DiagnosticPos pos) {
        ExpressionNode attrValue = exprNodeStack.pop();
        ExpressionNode attrName = exprNodeStack.pop();
        BLangXMLAttribute xmlAttribute = (BLangXMLAttribute) TreeBuilder.createXMLAttributeNode();
        xmlAttribute.value = attrValue;
        xmlAttribute.name = attrName;
        xmlAttribute.pos = pos;
        xmlAttributeNodeStack.push(xmlAttribute);
    }

    public void startXMLElement(DiagnosticPos pos) {
        ExpressionNode tagName = exprNodeStack.pop();
        BLangXMLElementLiteral xmlElement = (BLangXMLElementLiteral) TreeBuilder.createXMLElementLiteralNode();
        xmlElement.startTagName = tagName;
        xmlElement.pos = pos;
        xmlAttributeNodeStack.forEach(attribute -> xmlElement.addAttribute(attribute));
        xmlAttributeNodeStack.clear();
        addExpressionNode(xmlElement);
    }

    public void endXMLElement() {
        ExpressionNode endTag = exprNodeStack.pop();
        BLangXMLElementLiteral xmlElement = (BLangXMLElementLiteral) exprNodeStack.peek();
        xmlElement.endTagName = endTag;
    }

    public void createXMLQuotedLiteral(DiagnosticPos pos, Stack<String> precedingTextFragments, String endingText,
                                       QuoteType quoteType) {
        List<ExpressionNode> templateExprs =
                getExpressionsInTemplate(pos, precedingTextFragments, endingText, NodeKind.LITERAL);
        BLangXMLQuotedString quotedString = (BLangXMLQuotedString) TreeBuilder.createXMLQuotedStringNode();
        quotedString.pos = pos;
        quotedString.quoteType = quoteType;
        quotedString.textFragments = templateExprs;
        addExpressionNode(quotedString);
    }

    public void addChildToXMLElement() {
        XMLLiteralNode child = (XMLLiteralNode) exprNodeStack.pop();
        BLangXMLElementLiteral parentXMLExpr = (BLangXMLElementLiteral) exprNodeStack.peek();
        parentXMLExpr.addChild(child);
    }

    public void createXMLTextLiteral(DiagnosticPos pos, String text) {
        BLangXMLTextLiteral xmlTextLiteral = (BLangXMLTextLiteral) TreeBuilder.createXMLTextLiteralNode();
        addLiteralValue(pos, TypeTags.STRING, text);
        ExpressionNode textLiteral = exprNodeStack.pop();
        xmlTextLiteral.addTextFragment(textLiteral);
        xmlTextLiteral.pos = pos;
        addExpressionNode(xmlTextLiteral);
    }

    public void createXMLTextLiteral(DiagnosticPos pos, Stack<String> precedingTextFragments, String endingText) {
        BLangXMLTextLiteral xmlTextLiteral = (BLangXMLTextLiteral) TreeBuilder.createXMLTextLiteralNode();
        xmlTextLiteral.textFragments =
                getExpressionsInTemplate(pos, precedingTextFragments, endingText, NodeKind.XML_TEXT_LITERAL);
        xmlTextLiteral.pos = pos;
        addExpressionNode(xmlTextLiteral);
    }

    public void addXMLTextToElement(DiagnosticPos pos, Stack<String> precedingTextFragments, String endingText) {
        List<ExpressionNode> templateExprs =
                getExpressionsInTemplate(pos, precedingTextFragments, endingText, NodeKind.XML_TEXT_LITERAL);
        BLangXMLElementLiteral parentElement = (BLangXMLElementLiteral) exprNodeStack.peek();
        templateExprs.forEach(expr -> parentElement.addChild(expr));
    }

    public void createXMLCommentLiteral(DiagnosticPos pos, Stack<String> precedingTextFragments, String endingText) {
        BLangXMLCommentLiteral xmlCommentLiteral = (BLangXMLCommentLiteral) TreeBuilder.createXMLCommentLiteralNode();
        xmlCommentLiteral.textFragments =
                getExpressionsInTemplate(pos, precedingTextFragments, endingText, NodeKind.LITERAL);
        xmlCommentLiteral.pos = pos;
        addExpressionNode(xmlCommentLiteral);
    }

    public void createXMLPILiteral(DiagnosticPos pos, String targetQName, Stack<String> precedingTextFragments,
                                   String endingText) {
        List<ExpressionNode> dataExprs =
                getExpressionsInTemplate(pos, precedingTextFragments, endingText, NodeKind.LITERAL);
        addLiteralValue(pos, TypeTags.STRING, targetQName);
        LiteralNode target = (LiteralNode) exprNodeStack.pop();

        BLangXMLProcInsLiteral xmlProcInsLiteral =
                (BLangXMLProcInsLiteral) TreeBuilder.createXMLProcessingIntsructionLiteralNode();
        xmlProcInsLiteral.pos = pos;
        xmlProcInsLiteral.dataFragments = dataExprs;
        xmlProcInsLiteral.target = target;

        addExpressionNode(xmlProcInsLiteral);
    }

    public void createStringTemplateLiteral(DiagnosticPos pos, Stack<String> precedingTextFragments,
                                            String endingText) {
        BLangStringTemplateLiteral stringTemplateLiteral =
                (BLangStringTemplateLiteral) TreeBuilder.createStringTemplateLiteralNode();
        stringTemplateLiteral.expressions =
                getExpressionsInTemplate(pos, precedingTextFragments, endingText, NodeKind.LITERAL);
        stringTemplateLiteral.pos = pos;
        addExpressionNode(stringTemplateLiteral);
    }

    private List<ExpressionNode> getExpressionsInTemplate(DiagnosticPos pos, Stack<String> precedingTextFragments,
                                                          String endingText, NodeKind targetStrExprKind) {
        List<ExpressionNode> expressions = new ArrayList<ExpressionNode>();

        if (endingText != null && !endingText.isEmpty()) {
            endingText = StringEscapeUtils.unescapeJava(endingText);
            createXMLStringExpr(pos, targetStrExprKind, endingText);
            expressions.add(exprNodeStack.pop());
        }

        while (!precedingTextFragments.isEmpty()) {
            expressions.add(exprNodeStack.pop());
            String textFragment = precedingTextFragments.pop();

            if (textFragment != null && !textFragment.isEmpty()) {
                textFragment = StringEscapeUtils.unescapeJava(textFragment);
                createXMLStringExpr(pos, targetStrExprKind, textFragment);
                expressions.add(exprNodeStack.pop());
            }
        }

        Collections.reverse(expressions);
        return expressions;
    }

    private void createXMLStringExpr(DiagnosticPos pos, NodeKind targetExprKind, String strContent) {
        if (targetExprKind == NodeKind.XML_TEXT_LITERAL) {
            createXMLTextLiteral(pos, strContent);
        } else {
            addLiteralValue(pos, TypeTags.STRING, strContent);
        }
    }
}
