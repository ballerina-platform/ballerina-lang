/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import EventChannel from 'event_channel';
import ASTFactory from './../ast/ballerina-ast-factory';

class ASTVisitor extends EventChannel {
    constructor() {
        super();
    }

    /**
     * @param node {ASTNode}
     */
    canVisit(node) {
        if (ASTFactory.isBallerinaAstRoot(node)) {
            return this.canVisitBallerinaASTRoot(node);
        } else if (ASTFactory.isServiceDefinition(node)) {
            return this.canVisitServiceDefinition(node);
        } else if (ASTFactory.isAnnotationDefinition(node)) {
            return this.canVisitAnnotationDefinition(node);
        } else if (ASTFactory.isResourceDefinition(node)) {
            return this.canVisitResourceDefinition(node);
        } else if (ASTFactory.isFunctionDefinition(node)) {
            return this.canVisitFunctionDefinition(node);
        } else if (ASTFactory.isStatement(node)) {
            return this.canVisitStatement(node);
        } else if (ASTFactory.isPackageDefinition(node)) {
            return this.canVisitPackageDefinition(node);
        } else if (ASTFactory.isImportDeclaration(node)) {
            return this.canVisitImportDeclaration(node);
        } else if (ASTFactory.isConstantDefinition(node)) {
            return this.canVisitConstantDefinition(node);
        } else if (ASTFactory.isGlobalVariableDefinition(node)) {
            return this.canVisitGlobalVariableDefinition(node);
        } else if (ASTFactory.isWorkerDeclaration(node)) {
            return this.canVisitWorkerDeclaration(node);
        } else if (ASTFactory.isConnectorDeclaration(node)) {
            return this.canVisitConnectorDeclaration(node);
        } else if (ASTFactory.isFunctionInvocationExpression(node)) {
            return this.canVisitFuncInvocationExpression(node);
        } else if (ASTFactory.isActionInvocationExpression(node)) {
            return this.canVisitActionInvocationExpression(node);
        } else if (ASTFactory.isExpression(node)) {
            return this.canVisitExpression(node);
        } else if (ASTFactory.isVariableDeclaration(node)) {
            return this.canVisitVariableDeclaration(node);
        } else if (ASTFactory.isConnectorDefinition(node)) {
            return this.canVisitConnectorDefinition(node);
        } else if (ASTFactory.isConnectorAction(node)) {
            return this.canVisitConnectorAction(node);
        } else if (ASTFactory.isStructDefinition(node)) {
            return this.canVisitStructDefinition(node);
        } else if (ASTFactory.isResourceParameter(node)) {
            return this.canVisitResourceParameter(node);
        } else if (ASTFactory.isReturnType(node)) {
            return this.canVisitReturnType(node);
        } else if (ASTFactory.isBlockStatement(node)) {
            return this.canVisitBlockStatement(node);
        } else if (ASTFactory.isVariableDefinition(node)) {
            return this.canVisitVariableDefinition(node);
        } else if (ASTFactory.isAnnotation(node)) {
            return this.canVisitAnnotation(node);
        } else if (ASTFactory.isAnnotationEntry(node)) {
            return this.canVisitAnnotationEntry(node);
        } else if (ASTFactory.isAnnotationEntryArray(node)) {
            return this.canVisitAnnotationEntryArray(node);
        }
    }

    /**
     * @param node {ASTNode}
     */
    beginVisit(node) {
        if (ASTFactory.isBallerinaAstRoot(node)) {
            return this.beginVisitBallerinaASTRoot(node);
        } else if (ASTFactory.isServiceDefinition(node)) {
            return this.beginVisitServiceDefinition(node);
        } else if (ASTFactory.isAnnotationDefinition(node)) {
            return this.beginVisitAnnotationDefinition(node);
        } else if (ASTFactory.isResourceDefinition(node)) {
            return this.beginVisitResourceDefinition(node);
        } else if (ASTFactory.isFunctionDefinition(node)) {
            return this.beginVisitFunctionDefinition(node);
        } else if (ASTFactory.isStatement(node)) {
            return this.beginVisitStatement(node);
        } else if (ASTFactory.isPackageDefinition(node)) {
            return this.beginVisitPackageDefinition(node);
        } else if (ASTFactory.isImportDeclaration(node)) {
            return this.beginVisitImportDeclaration(node);
        } else if (ASTFactory.isConstantDefinition(node)) {
            return this.beginVisitConstantDefinition(node);
        } else if (ASTFactory.isGlobalVariableDefinition(node)) {
            return this.beginVisitGlobalVariableDefinition(node);
        } else if (ASTFactory.isWorkerDeclaration(node)) {
            return this.beginVisitWorkerDeclaration(node);
        } else if (ASTFactory.isConnectorDeclaration(node)) {
            return this.beginVisitConnectorDeclaration(node);
        } else if (ASTFactory.isFunctionInvocationExpression(node)) {
            return this.beginVisitFuncInvocationExpression(node);
        } else if (ASTFactory.isActionInvocationExpression(node)) {
            return this.beginVisitActionInvocationExpression(node);
        } else if (ASTFactory.isExpression(node)) {
            return this.beginVisitExpression(node);
        } else if (ASTFactory.isVariableDeclaration(node)) {
            return this.beginVisitVariableDeclaration(node);
        } else if (ASTFactory.isConnectorDefinition(node)) {
            return this.beginVisitConnectorDefinition(node);
        } else if (ASTFactory.isConnectorAction(node)) {
            return this.beginVisitConnectorAction(node);
        } else if (ASTFactory.isStructDefinition(node)) {
            return this.beginVisitStructDefinition(node);
        } else if (ASTFactory.isResourceParameter(node)) {
            return this.beginVisitResourceParameter(node);
        } else if (ASTFactory.isReturnType(node)) {
            return this.beginVisitReturnType(node);
        } else if (ASTFactory.isBlockStatement(node)) {
            return this.beginVisitBlockStatement(node);
        } else if (ASTFactory.isVariableDefinition(node)) {
            return this.beginVisitVariableDefinition(node);
        } else if (ASTFactory.isAnnotation(node)) {
            return this.beginVisitAnnotation(node);
        } else if (ASTFactory.isAnnotationEntry(node)) {
            return this.beginVisitAnnotationEntry(node);
        } else if (ASTFactory.isAnnotationEntryArray(node)) {
            return this.beginVisitAnnotationEntryArray(node);
        }
    }

    /**
     * @param node {ASTNode}
     */
    visit(node) {
        if (ASTFactory.isBallerinaAstRoot(node)) {
            return this.visitBallerinaASTRoot(node);
        } else if (ASTFactory.isServiceDefinition(node)) {
            return this.visitServiceDefinition(node);
        } else if (ASTFactory.isAnnotationDefinition(node)) {
            return this.visitAnnotationDefinition(node);
        } else if (ASTFactory.isResourceDefinition(node)) {
            return this.visitResourceDefinition(node);
        } else if (ASTFactory.isFunctionDefinition(node)) {
            return this.visitFunctionDefinition(node);
        } else if (ASTFactory.isStatement(node)) {
            return this.visitStatement(node);
        } else if (ASTFactory.isPackageDefinition(node)) {
            return this.visitPackageDefinition(node);
        } else if (ASTFactory.isImportDeclaration(node)) {
            return this.visitImportDeclaration(node);
        } else if (ASTFactory.isConstantDefinition(node)) {
            return this.visitConstantDefinition(node);
        } else if (ASTFactory.isGlobalVariableDefinition(node)) {
            return this.visitGlobalVariableDefinition(node);
        } else if (ASTFactory.isWorkerDeclaration(node)) {
            return this.visitWorkerDeclaration(node);
        } else if (ASTFactory.isConnectorDeclaration(node)) {
            return this.visitConnectorDeclaration(node);
        } else if (ASTFactory.isFunctionInvocationExpression(node)) {
            return this.visitFuncInvocationExpression(node);
        } else if (ASTFactory.isActionInvocationExpression(node)) {
            return this.visitActionInvocationExpression(node);
        } else if (ASTFactory.isExpression(node)) {
            return this.visitExpression(node);
        } else if (ASTFactory.isVariableDeclaration(node)) {
            return this.visitVariableDeclaration(node);
        } else if (ASTFactory.isConnectorDefinition(node)) {
            return this.visitConnectorDefinition(node);
        } else if (ASTFactory.isConnectorAction(node)) {
            return this.visitConnectorAction(node);
        } else if (ASTFactory.isStructDefinition(node)) {
            return this.visitStructDefinition(node);
        } else if (ASTFactory.isResourceParameter(node)) {
            return this.visitResourceParameter(node);
        } else if (ASTFactory.isReturnType(node)) {
            return this.visitReturnType(node);
        } else if (ASTFactory.isBlockStatement(node)) {
            return this.visitBlockStatement(node);
        } else if (ASTFactory.isVariableDefinition(node)) {
            return this.visitVariableDefinition(node);
        } else if (ASTFactory.isAnnotation(node)) {
            return this.visitAnnotation(node);
        } else if (ASTFactory.isAnnotationEntry(node)) {
            return this.visitAnnotationEntry(node);
        } else if (ASTFactory.isAnnotationEntryArray(node)) {
            return this.visitAnnotationEntryArray(node);
        }
    }

    /**
     * @param node {ASTNode}
     */
    endVisit(node) {
        if (ASTFactory.isBallerinaAstRoot(node)) {
            return this.endVisitBallerinaASTRoot(node);
        } else if (ASTFactory.isServiceDefinition(node)) {
            return this.endVisitServiceDefinition(node);
        } else if (ASTFactory.isAnnotationDefinition(node)) {
            return this.endVisitAnnotationDefinition(node);
        } else if (ASTFactory.isResourceDefinition(node)) {
            return this.endVisitResourceDefinition(node);
        } else if (ASTFactory.isFunctionDefinition(node)) {
            return this.endVisitFunctionDefinition(node);
        } else if (ASTFactory.isStatement(node)) {
            return this.endVisitStatement(node);
        } else if (ASTFactory.isPackageDefinition(node)) {
            return this.endVisitPackageDefinition(node);
        } else if (ASTFactory.isImportDeclaration(node)) {
            return this.endVisitImportDeclaration(node);
        } else if (ASTFactory.isConstantDefinition(node)) {
            return this.endVisitConstantDefinition(node);
        } else if (ASTFactory.isGlobalVariableDefinition(node)) {
            return this.endVisitGlobalVariableDefinition(node);
        } else if (ASTFactory.isWorkerDeclaration(node)) {
            return this.endVisitWorkerDeclaration(node);
        } else if (ASTFactory.isConnectorDeclaration(node)) {
            return this.endVisitConnectorDeclaration(node);
        } else if (ASTFactory.isFunctionInvocationExpression(node)) {
            return this.endVisitFuncInvocationExpression(node);
        } else if (ASTFactory.isActionInvocationExpression(node)) {
            return this.endVisitActionInvocationExpression(node);
        } else if (ASTFactory.isExpression(node)) {
            return this.endVisitExpression(node);
        } else if (ASTFactory.isVariableDeclaration(node)) {
            return this.endVisitVariableDeclaration(node);
        } else if (ASTFactory.isConnectorDefinition(node)) {
            return this.endVisitConnectorDefinition(node);
        } else if (ASTFactory.isConnectorAction(node)) {
            return this.endVisitConnectorAction(node);
        } else if (ASTFactory.isStructDefinition(node)) {
            return this.endVisitStructDefinition(node);
        } else if (ASTFactory.isResourceParameter(node)) {
            return this.endVisitResourceParameter(node);
        } else if (ASTFactory.isReturnType(node)) {
            return this.endVisitReturnType(node);
        } else if (ASTFactory.isBlockStatement(node)) {
            return this.endVisitBlockStatement(node);
        } else if (ASTFactory.isVariableDefinition(node)) {
            return this.endVisitVariableDefinition(node);
        } else if (ASTFactory.isAnnotation(node)) {
            return this.endVisitAnnotation(node);
        } else if (ASTFactory.isAnnotationEntry(node)) {
            return this.endVisitAnnotationEntry(node);
        } else if (ASTFactory.isAnnotationEntryArray(node)) {
            return this.endVisitAnnotationEntryArray(node);
        }
    }

    canVisitBallerinaASTRoot(ballerinaASTRoot) {
        return false;
    }

    beginVisitBallerinaASTRoot(ballerinaASTRoot) {
    }

    visitBallerinaASTRoot(ballerinaASTRoot) {
    }

    endVisitBallerinaASTRoot(ballerinaASTRoot) {
    }

    canVisitServiceDefinition(serviceDefinition) {
        return false;
    }

    canVisitAnnotationDefinition(annotationDefinition) {
        return false;
    }

    beginVisitServiceDefinition(serviceDefinition) {
    }

    visitServiceDefinition(serviceDefinition) {
    }

    endVisitServiceDefinition(serviceDefinition) {
    }

    beginVisitAnnotationDefinition(annotationDefinition) {
    }

    visitAnnotationDefinition(annotationDefinition) {
    }

    endVisitAnnotationDefinition(annotationDefinition) {
    }

    canVisitConnectorDefinition(connectorDefinition) {
        return false;
    }

    beginVisitConnectorDefinition(connectorDefinition) {
    }

    visitConnectorDefinition(connectorDefinition) {
    }

    endVisitConnectorDefinition(connectorDefinition) {
    }

    canVisitFunctionDefinition(functionDefinition) {
        return false;
    }

    beginVisitFunctionDefinition(functionDefinition) {
    }

    visitFunctionDefinition(functionDefinition) {
    }

    endVisitFunctionDefinition(functionDefinition) {
    }

    canVisitResourceDefinition(resourceDefinition) {
        return false;
    }

    beginVisitResourceDefinition(resourceDefinition) {
    }

    visitResourceDefinition(resourceDefinition) {
    }

    endVisitResourceDefinition(resourceDefinition) {
    }

    canVisitFunctionDefinition(resourceDefinition) {
        return false;
    }

    beginVisitFunctionDefinition(resourceDefinition) {
    }

    visitFunctionDefinition(resourceDefinition) {
    }

    endVisitFunctionDefinition(resourceDefinition) {
    }

    canVisitStructDefinition(structDefinition) {
        return false;
    }

    beginVisitStructDefinition(structDefinition) {
    }

    visitStructDefinition(structDefinition) {
    }

    endVisitStructDefinition(structDefinition) {
    }

    canVisitResourceParameter(resourceParameter) {
        return false;
    }

    beginVisitResourceParameter(resourceParameter) {
    }

    visitResourceParameter(resourceParameter) {
    }

    endVisitResourceParameter(resourceParameter) {
    }

    canVisitBlockStatement(blockStatement) {
        return false;
    }

    beginVisitBlockStatement(blockStatement) {
    }

    visitBlockStatement(blockStatement) {
    }

    endVisitBlockStatement(blockStatement) {
    }

    canVisitReturnType(returnType) {
        return false;
    }

    beginVisitReturnType(returnType) {
    }

    visitReturnType(returnType) {
    }

    endVisitReturnType(returnType) {
    }

    canVisitPackageDefinition(packageDefinition) {
        return false;
    }

    beginVisitPackageDefinition(packageDefinition) {
    }

    visitPackageDefinition(packageDefinition) {
    }

    endVisitPackageDefinition(packageDefinition) {
    }

    canVisitImportDeclaration(importDeclaration) {
        return false;
    }

    beginVisitImportDeclaration(importDeclaration) {
    }

    visitImportDeclaration(importDeclaration) {
    }

    endVisitImportDeclaration(importDeclaration) {
    }

    canVisitConstantDefinition(constantDefinition) {
        return false;
    }

    beginVisitConstantDefinition(constantDefinition) {
    }

    visitConstantDefinition(constantDefinition) {
    }

    endVisitConstantDefinition(constantDefinition) {
    }

    canVisitGlobalVariableDefinition(globalVariableDefinition) {
        return false;
    }

    beginVisitGlobalVariableDefinition(globalVariableDefinition) {
    }

    visitGlobalVariableDefinition(globalVariableDefinition) {
    }

    endVisitGlobalVariableDefinition(globalVariableDefinition) {
    }

    canVisitWorkerDeclaration(importDeclaration) {
        return false;
    }

    beginVisitWorkerDeclaration(importDeclaration) {
    }

    visitWorkerDeclaration(importDeclaration) {
    }

    endVisitWorkerDeclaration(importDeclaration) {
    }

    canVisitStatement(statement) {
        return false;
    }

    beginVisitStatement(statement) {
    }

    visitStatement(statement) {
    }

    endVisitStatement(statement) {
    }

    canVisitExpression(statement) {
        return false;
    }

    beginVisitExpression(statement) {
    }

    visitExpression(statement) {
    }

    endVisitExpression(statement) {
    }

    canVisitConnectorAction(connectorAction) {
        return false;
    }

    beginVisitConnectorAction(connectorAction) {
    }

    visitConnectorAction(connectorAction) {
    }

    endVisitConnectorAction(connectorAction) {
    }

    canVisitActionInvocationExpression() {
        return true;
    }

    beginVisitActionInvocationExpression(expression) {
    }

    visitActionInvocationExpression(expression) {
    }

    endVisitActionInvocationExpression(expression) {
    }

    /**
     *
     * @param statement - statement which should be checked
     * @returns {boolean}
     */
    canVisitFuncInvocationExpression(statement) {
        return false;
    }

    /**
     *
     * @param statement - statement to begin visitFuncInvocationExpression with
     */
    beginVisitFuncInvocationExpression(statement) {
    }

    /**
     *
     * @param statement - statement to visit FuncInvocationExpression with
     */
    visitFuncInvocationExpression(statement) {
    }

    /**
     *
     * @param statement - statement to end visitFuncInvocationExpression with
     */
    endVisitFuncInvocationExpression(statement) {
    }

    canVisitConnectorDeclaration(connectorDeclaration) {
        return false;
    }

    beginVisitConnectorDeclaration(connectorDeclaration) {
    }

    visitConnectorDeclaration(connectorDeclaration) {
    }

    endVisitConnectorDeclaration(connectorDeclaration) {
    }

    canVisitAssignment(assignment) {
        return false;
    }

    beginVisitAssignment(assignment) {
    }

    visitAssignment(assignment) {
    }

    endVisitAssignment(assignment) {
    }

    canVisitVariableDeclaration(variableDeclaration) {
        return false;
    }

    beginVisitVariableDeclaration(variableDeclaration) {
    }

    visitVariableDeclaration(variableDeclaration) {
    }

    endVisitVariableDeclaration(variableDeclaration) {
    }

    canVisitVariableDefinition(variableDefinition) {
        return false;
    }

    beginVisitVariableDefinition(variableDefinition) {
    }

    visitVariableDefinition(variableDefinition) {
    }

    endVisitVariableDefinition(variableDefinition) {
    }

    canVisitAnnotation(annotation) {
        return false;
    }

    beginVisitAnnotation(annotation) {
    }

    visitAnnotation(annotation) {
    }

    endVisitAnnotation(annotation) {
    }

    canVisitAnnotationEntry(annotationEntry) {
        return false;
    }

    beginVisitAnnotationEntry(annotationEntry) {
    }

    visitAnnotationEntry(annotationEntry) {
    }

    endVisitAnnotationEntry(annotationEntry) {
    }

    canVisitAnnotationEntryArray(annotationEntryArray) {
        return false;
    }

    beginVisitAnnotationEntryArray(annotationEntryArray) {
    }

    visitAnnotationEntryArray(annotationEntryArray) {
    }

    visitJoinStatement(joinStatement) {
    }

    endVisitAnnotationEntryArray(annotationEntryArray) {
    }
}

export default ASTVisitor;
