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

import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.TreeUtils;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.ActionNode;
import org.ballerinalang.model.tree.AnnotatableNode;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.AnnotationAttributeNode;
import org.ballerinalang.model.tree.AnnotationNode;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.ballerinalang.model.tree.ConnectorNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.ImportPackageNode;
import org.ballerinalang.model.tree.InvokableNode;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.PackageDeclarationNode;
import org.ballerinalang.model.tree.StructNode;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.expressions.AnnotationAttributeValueNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.LiteralNode;
import org.ballerinalang.model.tree.expressions.RecordTypeLiteralNode;
import org.ballerinalang.model.tree.expressions.SimpleVariableReferenceNode;
import org.ballerinalang.model.tree.expressions.VariableReferenceNode;
import org.ballerinalang.model.tree.statements.AbortNode;
import org.ballerinalang.model.tree.statements.AssignmentNode;
import org.ballerinalang.model.tree.statements.BlockNode;
import org.ballerinalang.model.tree.statements.IfNode;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.ballerinalang.model.tree.statements.TransactionNode;
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNameReference;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttributeValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordTypeLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVariableReference;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.DiagnosticPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

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
        
    private Stack<ConnectorNode> connectorNodeStack = new Stack<>();
    
    private Stack<List<ActionNode>> actionNodeStack = new Stack<>();

    private Stack<AnnotationNode> annotationStack = new Stack<>();

    private Stack<AnnotationAttributeValueNode> annotAttribValStack = new Stack<>();

    private Stack<AnnotationAttachmentNode> annotAttachmentStack = new Stack<>();

    private Stack<IfNode> ifElseStatementStack = new Stack<>();

    private Stack<TransactionNode> transactionNodeStack = new Stack<>();

    public BLangPackageBuilder(CompilationUnitNode compUnit) {
        this.compUnit = compUnit;
    }

    public void addValueType(DiagnosticPos pos, String typeName) {
        BLangValueType typeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        typeNode.pos = pos;
        typeNode.typeKind = (TreeUtils.stringToTypeKind(typeName));

        addType(typeNode);
    }

    public void addArrayType(DiagnosticPos pos, int dimensions) {
        BLangType eType;
        if (!this.typeNodeListStack.empty()) {
            List<TypeNode> typeNodeList = this.typeNodeListStack.peek();
            eType = (BLangType) typeNodeList.get(typeNodeList.size() - 1);
            typeNodeList.remove(typeNodeList.size() - 1);
        } else {
            eType = (BLangType) this.typeNodeStack.pop();
        }
        BLangArrayType arrayTypeNode = (BLangArrayType) TreeBuilder.createArrayTypeNode();
        arrayTypeNode.pos = pos;
        arrayTypeNode.elemtype = eType;
        arrayTypeNode.dimensions = dimensions;

        addType(arrayTypeNode);
    }

    public void addUserDefineType(DiagnosticPos pos) {
        BLangNameReference nameReference = nameReferenceStack.pop();
        BLangUserDefinedType userDefinedType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
        userDefinedType.pos = pos;
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
        if (!this.typeNodeListStack.empty()) {
            this.typeNodeListStack.peek().add(typeNode);
        } else {
            this.typeNodeStack.push(typeNode);
        }
    }

    public void addNameReference(String pkgName, String name) {
        nameReferenceStack.push(new BLangNameReference(createIdentifier(pkgName), createIdentifier(name)));
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

    public void addVar(String identifier, boolean exprAvailable) {
        VariableNode var = this.generateBasicVarNode(identifier, exprAvailable);
        if (this.varListStack.empty()) {
            this.varStack.push(var);
        } else {
            this.varListStack.peek().add(var);
        }
    }

    public void endCallableUnitSignature(String identifier, boolean paramsAvail,
                                         boolean retParamsAvail, boolean retParamTypeOnly) {
        InvokableNode invNode = this.invokableNodeStack.peek();
        invNode.setName(this.createIdentifier(identifier));
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

    public void addVariableDefStatement(String identifier, boolean exprAvailable) {
        VariableDefinitionNode varDefNode = TreeBuilder.createVariableDefinitionNode();
        VariableNode var = TreeBuilder.createVariableNode();
        var.setName(this.createIdentifier(identifier));
        var.setTypeNode(this.typeNodeStack.pop());
        if (exprAvailable) {
            var.setInitialExpression(this.exprNodeStack.pop());
        }
        varDefNode.setVariable(var);
        addStmtToCurrentBlock(varDefNode);
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
        BLangSimpleVariableReference varRef =
                (BLangSimpleVariableReference) TreeBuilder.createSimpleVariableReferenceNode();
        varRef.variableName = createIdentifier(paramName);

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
        this.blockNodeStack.peek().addStatement(stmtNode);
    }

    public void addThrowStmt(DiagnosticPos poc) {
        ExpressionNode throwExpr = this.exprNodeStack.pop();
        BLangThrow throwNode = (BLangThrow) TreeBuilder.createThrowNode();
        throwNode.pos = poc;
        throwNode.expr = (BLangExpression) throwExpr;
        this.blockNodeStack.peek().addStatement(throwNode);
    }

    private void addExpressionNode(ExpressionNode expressionNode) {
        this.exprNodeStack.push(expressionNode);
    }

    public void addLiteralValue(Object value) {
        LiteralNode litExpr = TreeBuilder.createLiteralExpression();
        litExpr.setValue(value);
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
        BLangSimpleVariableReference varRef = (BLangSimpleVariableReference) TreeBuilder
                .createSimpleVariableReferenceNode();
        varRef.pos = pos;
        varRef.packageIdentifier = nameReference.pkgAlias;
        varRef.variableName = nameReference.name;
        this.exprNodeStack.push(varRef);
    }

    public void createFunctionInvocation(DiagnosticPos pos, boolean argsAvailable) {
        BLangInvocation invocationNode = (BLangInvocation) TreeBuilder.createInvocationNode();
        invocationNode.pos = pos;
        if (argsAvailable) {
            invocationNode.argsExpressions = exprNodeListStack.pop();
        }
        BLangNameReference nameReference = nameReferenceStack.pop();
        invocationNode.functionName = nameReference.name;
        invocationNode.packIdentifier = nameReference.pkgAlias;
        addExpressionNode(invocationNode);
    }

    public void createInvocationNode(DiagnosticPos pos, String invocation, boolean argsAvailable) {
        BLangInvocation invocationNode = (BLangInvocation) TreeBuilder.createInvocationNode();
        invocationNode.pos = pos;
        if (argsAvailable) {
            invocationNode.argsExpressions = exprNodeListStack.pop();
        }
        invocationNode.variableReferenceNode = (VariableReferenceNode) exprNodeStack.pop();
        invocationNode.functionName = createIdentifier(invocation);
        invocationNode.packIdentifier = createIdentifier(null);
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
        BLangBinaryExpression binaryExpressionNode = (BLangBinaryExpression) TreeBuilder.createBinaryExpressionNode();
        binaryExpressionNode.pos = pos;
        binaryExpressionNode.rightExpression = exprNodeStack.pop();
        binaryExpressionNode.leftExpression = exprNodeStack.pop();
        binaryExpressionNode.operator = OperatorKind.valueFrom(operator);
        addExpressionNode(binaryExpressionNode);
    }

    public void createUnaryExpr(DiagnosticPos pos, String operator) {
        BLangUnaryExpression unaryExpressionNode = (BLangUnaryExpression) TreeBuilder.createUnaryExpressionNode();
        unaryExpressionNode.pos = pos;
        unaryExpressionNode.expressionNode = exprNodeStack.pop();
        unaryExpressionNode.operator = OperatorKind.valueFrom(operator);
        addExpressionNode(unaryExpressionNode);
    }

    public void endFunctionDef() {
        this.compUnit.addTopLevelNode((FunctionNode) this.invokableNodeStack.pop());
    }

    public void endCallableUnitBody() {
        this.invokableNodeStack.peek().setBody(this.blockNodeStack.pop());
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
    
    public void addImportPackageDeclaration(String alias) {
        ImportPackageNode impDecl = TreeBuilder.createImportPackageNode();
        IdentifierNode aliasNode;
        if (alias != null) {
            aliasNode = this.createIdentifier(alias);
        } else {
            aliasNode = null;
        }
        impDecl.setPackageID(this.pkgIdStack.pop());
        impDecl.setAlias(aliasNode);
        this.compUnit.addTopLevelNode(impDecl);
    }

    private VariableNode generateBasicVarNode(String identifier, boolean exprAvailable) {
        IdentifierNode name = this.createIdentifier(identifier);
        VariableNode var = TreeBuilder.createVariableNode();
        var.setName(name);
        var.setTypeNode(this.typeNodeStack.pop());
        if (exprAvailable) {
            var.setInitialExpression(this.exprNodeStack.pop());
        }
        return var;
    }

    public void addGlobalVariable(String identifier, boolean exprAvailable) {
        VariableNode var = this.generateBasicVarNode(identifier, exprAvailable);
        this.compUnit.addTopLevelNode(var);
    }
    
    public void addConstVariable(String identifier) {
        VariableNode var = this.generateBasicVarNode(identifier, true);
        var.addFlag(Flag.CONST);
        this.compUnit.addTopLevelNode(var);
    }

    public void startStructDef() {
        StructNode structNode = TreeBuilder.createStructNode();
        attachAnnotations(structNode);
        this.structStack.add(structNode);
    }
    
    public void endStructDef(String identifier) {
        StructNode structNode = this.structStack.pop();
        structNode.setName(this.createIdentifier(identifier));
        this.varListStack.pop().forEach(structNode::addField);
        this.compUnit.addTopLevelNode(structNode);
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
    
    public void endConnectorDef(String identifier) {
        ConnectorNode connectorNode = this.connectorNodeStack.pop();
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
        attachAnnotations(actionNode);
        this.invokableNodeStack.push(actionNode);
    }

    public void endActionDef() {
        this.connectorNodeStack.peek().addAction((ActionNode) this.invokableNodeStack.pop());
    }

    public void startProcessingTypeNodeList() {
        this.typeNodeListStack.push(new ArrayList<>());
    }

    public void startAnnotationDef() {
        AnnotationNode annotNode = TreeBuilder.createAnnotationNode();
        attachAnnotations(annotNode);
        this.annotationStack.add(annotNode);
    }

    public void endAnnotationDef(String identifier) {
        AnnotationNode annotationNode = this.annotationStack.pop();
        annotationNode.setName(this.createIdentifier(identifier));
        this.varListStack.pop().forEach(var -> {
            AnnotationAttributeNode annAttrNode = TreeBuilder.createAnnotAttributeNode();
            var.getFlags().forEach(annAttrNode::addFlag);
            var.getAnnotationAttachments().forEach(annAttrNode::addAnnotationAttachment);
            annAttrNode.setTypeNode(var.getTypeNode());
            annAttrNode.setInitialExpression(var.getInitialExpression());
            annAttrNode.setName(var.getName());

            // add the attribute to the annotation definition
            annotationNode.addAttribute(annAttrNode);
        });

        this.compUnit.addTopLevelNode(annotationNode);
    }

    public void startAnnotationAttachment(DiagnosticPos currentPos) {
        BLangAnnotationAttachment annotAttachmentNode =
                (BLangAnnotationAttachment) TreeBuilder.createAnnotAttachmentNode();
        annotAttachmentNode.pos = currentPos;
        this.annotAttachmentStack.push(TreeBuilder.createAnnotAttachmentNode());
    }

    public void createLiteralTypeAttributeValue(DiagnosticPos currentPos) {
        createAnnotAttribValueFromExpr(currentPos);
    }

    public void createVarRefTypeAttributeValue(DiagnosticPos currentPos) {
        createAnnotAttribValueFromExpr(currentPos);
    }

    public void createAnnotationTypeAttributeValue(DiagnosticPos currentPos) {
        BLangAnnotAttributeValue annotAttrVal = (BLangAnnotAttributeValue) TreeBuilder.createAnnotAttributeValueNode();
        annotAttrVal.pos = currentPos;
        annotAttrVal.setValue(annotAttachmentStack.pop());
        annotAttribValStack.push(annotAttrVal);
    }

    public void createArrayTypeAttributeValue(DiagnosticPos currentPos) {
        BLangAnnotAttributeValue annotAttrVal = (BLangAnnotAttributeValue) TreeBuilder.createAnnotAttributeValueNode();
        annotAttrVal.pos = currentPos;
        while (!annotAttribValStack.isEmpty()) {
            annotAttrVal.addValue(annotAttribValStack.pop());
        }
        annotAttribValStack.push(annotAttrVal);
    }

    public void createAnnotationKeyValue(String attrName, DiagnosticPos currentPos) {
        annotAttachmentStack.peek().addAttribute(attrName, annotAttribValStack.pop());
    }

    private void createAnnotAttribValueFromExpr(DiagnosticPos currentPos) {
        BLangAnnotAttributeValue annotAttrVal = (BLangAnnotAttributeValue) TreeBuilder.createAnnotAttributeValueNode();
        annotAttrVal.pos = currentPos;
        annotAttrVal.setValue(exprNodeStack.pop());
        annotAttribValStack.push(annotAttrVal);
    }

    private void attachAnnotations(AnnotatableNode annotatableNode) {
        while (!annotAttachmentStack.empty()) {
            annotatableNode.addAnnotationAttachment(annotAttachmentStack.pop());
        }
    }

    public void addAssignmentStatement(boolean isVarDeclaration) {
        ExpressionNode rExprNode = exprNodeStack.pop();
        List<ExpressionNode> lExprList = exprNodeListStack.pop();
        if (rExprNode instanceof BLangExpression) {
            List<BLangVariableReference> lVariableReferenceList = lExprList.stream()
                    .filter(BLangVariableReference.class::isInstance).map(BLangVariableReference.class::cast)
                    .collect(Collectors.toList());
            AssignmentNode assignmentNode = TreeBuilder
                    .createAssignmentNode(lVariableReferenceList, (BLangExpression) rExprNode, isVarDeclaration);
            this.blockNodeStack.peek().addStatement(assignmentNode);
        }
    }

    public void startTransactionStmt() {
        transactionNodeStack.push((BLangTransaction) TreeBuilder.createTransactionNode());
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
        this.blockNodeStack.peek().addStatement(transaction);
    }

    public void addAbortStatement() {
        AbortNode abortNode = TreeBuilder.createAbortNode();
        this.blockNodeStack.peek().addStatement(abortNode);
    }

    public void startIfElseNode() {
        ifElseStatementStack.push(TreeBuilder.createIfElseStatementNode());
        startBlock();
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

    public void addExpressionStmt(DiagnosticPos pos) {
        BLangExpressionStmt exprStmt = (BLangExpressionStmt) TreeBuilder.createExpressionStatementNode();
        exprStmt.pos = pos;
        exprStmt.expr = (BLangExpression) exprNodeStack.pop();
        addStmtToCurrentBlock(exprStmt);
    }
}
