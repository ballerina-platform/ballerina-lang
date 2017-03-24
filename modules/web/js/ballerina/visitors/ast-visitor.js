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
import _ from 'lodash';
import log from 'log';
import EventChannel from 'event_channel';
import AST from './../ast/module';

class ASTVisitor extends EventChannel {
    constructor() {
        super();
    }

    /**
     * @param node {ASTNode}
     */
    canVisit(node) {
        if(node instanceof AST.BallerinaASTRoot){
            return this.canVisitBallerinaASTRoot(node);
        } else if(node instanceof AST.ServiceDefinition){
            return this.canVisitServiceDefinition(node);
        } else if(node instanceof AST.ResourceDefinition){
            return this.canVisitResourceDefinition(node);
        } else if(node instanceof AST.FunctionDefinition){
            return this.canVisitFunctionDefinition(node);
        } else if(node instanceof AST.CommentStatement){
            return this.canVisitCommentStatement(node);
        } else if (node instanceof AST.Statement) {
            return this.canVisitStatement(node);
        } else if (node instanceof AST.PackageDefinition) {
            return this.canVisitPackageDefinition(node);
        } else if (node instanceof AST.ImportDeclaration) {
            return this.canVisitImportDeclaration(node);
        } else if (node instanceof AST.ConstantDefinition) {
            return this.canVisitConstantDefinition(node);
        } else if (node instanceof AST.WorkerDeclaration) {
            return this.canVisitWorkerDeclaration(node);
        } else if( node instanceof AST.ConnectorDeclaration){
            return this.canVisitConnectorDeclaration(node);
        } else if( node instanceof AST.Assignment){
            return this.canVisitAssignment(node);
        } else if( node instanceof AST.FunctionInvocationExpression){
            return this.canVisitFuncInvocationExpression(node);
        } else if(node instanceof AST.ActionInvocationExpression){
            return this.canVisitActionInvocationExpression(node);
        } else if( node instanceof AST.Expression){
            return this.canVisitExpression(node);
        } else if(node instanceof AST.VariableDeclaration){
            return this.canVisitVariableDeclaration(node);
        } else if(node instanceof AST.ConnectorDefinition){
            return this.canVisitConnectorDefinition(node);
        } else if(node instanceof AST.ConnectorAction){
            return this.canVisitConnectorAction(node);
        } else if(node instanceof AST.StructDefinition){
            return this.canVisitStructDefinition(node);
        } else if(node instanceof AST.TypeMapperDefinition){
            return this.canVisitTypeMapperDefinition(node);
        } else if(node instanceof AST.ResourceParameter){
            return this.canVisitResourceParameter(node);
        } else if(node instanceof AST.ReturnType){
            return this.canVisitReturnType(node);
        } else if(node instanceof AST.BlockStatement){
            return this.canVisitBlockStatement(node);
        } else if(node instanceof AST.VariableDefinition){
            return this.canVisitVariableDefinition(node);
        }
    }

    /**
     * @param node {ASTNode}
     */
    beginVisit(node) {
        if(node instanceof AST.BallerinaASTRoot){
            return this.beginVisitBallerinaASTRoot(node);
        } else if(node instanceof AST.ServiceDefinition){
            return this.beginVisitServiceDefinition(node);
        } else if(node instanceof AST.ResourceDefinition){
            return this.beginVisitResourceDefinition(node);
        } else if(node instanceof AST.FunctionDefinition){
            return this.beginVisitFunctionDefinition(node);
        } else if(node instanceof AST.CommentStatement){
            return this.beginVisitCommentStatement(node);
        } else if (node instanceof AST.Statement) {
            return this.beginVisitStatement(node);
        } else if (node instanceof AST.PackageDefinition) {
            return this.beginVisitPackageDefinition(node);
        } else if (node instanceof AST.ImportDeclaration) {
            return this.beginVisitImportDeclaration(node);
        } else if (node instanceof AST.ConstantDefinition) {
            return this.beginVisitConstantDefinition(node);
        } else if (node instanceof AST.WorkerDeclaration) {
            return this.beginVisitWorkerDeclaration(node);
        } else if( node instanceof AST.ConnectorDeclaration){
            return this.beginVisitConnectorDeclaration(node);
        } else if( node instanceof AST.Assignment){
            return this.beginVisitAssignment(node);
        } else if(node instanceof AST.FunctionInvocationExpression){
            return this.beginVisitFuncInvocationExpression(node);
        } else if(node instanceof AST.ActionInvocationExpression){
            return this.beginVisitActionInvocationExpression(node);
        } else if (node instanceof AST.Expression) {
            return this.beginVisitExpression(node);
        } else if(node instanceof AST.VariableDeclaration){
            return this.beginVisitVariableDeclaration(node);
        } else if(node instanceof AST.ConnectorDefinition){
            return this.beginVisitConnectorDefinition(node);
        } else if(node instanceof AST.ConnectorAction){
            return this.beginVisitConnectorAction(node);
        } else if(node instanceof AST.StructDefinition){
            return this.beginVisitStructDefinition(node);
        } else if(node instanceof AST.TypeMapperDefinition){
            return this.beginVisitTypeMapperDefinition(node);
        } else if(node instanceof AST.ResourceParameter){
            return this.beginVisitResourceParameter(node);
        } else if(node instanceof AST.ReturnType){
            return this.beginVisitReturnType(node);
        } else if(node instanceof AST.BlockStatement){
            return this.beginVisitBlockStatement(node);
        } else if(node instanceof AST.VariableDefinition){
            return this.beginVisitVariableDefinition(node);
        }
    }

    /**
     * @param node {ASTNode}
     */
    visit(node) {
        if(node instanceof AST.BallerinaASTRoot){
            return this.visitBallerinaASTRoot(node);
        } else if(node instanceof AST.ServiceDefinition){
            return this.visitServiceDefinition(node);
        } else if(node instanceof AST.ResourceDefinition){
            return this.visitResourceDefinition(node);
        } else if(node instanceof AST.FunctionDefinition){
            return this.visitFunctionDefinition(node);
        } else if(node instanceof AST.CommentStatement){
            return this.visitCommentStatement(node);
        } else if(node instanceof AST.Statement){
            return this.visitStatement(node);
        } else if(node instanceof AST.PackageDefinition){
            return this.visitPackageDefinition(node);
        } else if(node instanceof AST.ImportDeclaration){
            return this.visitImportDeclaration(node);
        } else if(node instanceof AST.ConstantDefinition){
            return this.visitConstantDefinition(node);
        } else if(node instanceof AST.WorkerDeclaration){
            return this.visitWorkerDeclaration(node);
        } else if( node instanceof AST.ConnectorDeclaration){
            return this.visitConnectorDeclaration(node);
        } else if( node instanceof AST.Assignment){
            return this.visitAssignment(node);
        } else if(node instanceof  AST.FunctionInvocationExpression){
            return this.visitFuncInvocationExpression(node);
        } else if(node instanceof AST.ActionInvocationExpression){
            return this.visitActionInvocationExpression(node);
        } else if(node instanceof AST.Expression){
            return this.visitExpression(node);
        } else if(node instanceof AST.VariableDeclaration){
            return this.visitVariableDeclaration(node);
        } else if(node instanceof AST.ConnectorDefinition){
            return this.visitConnectorDefinition(node);
        } else if(node instanceof AST.ConnectorAction){
            return this.visitConnectorAction(node);
        } else if(node instanceof AST.StructDefinition){
            return this.visitStructDefinition(node);
        } else if(node instanceof AST.TypeMapperDefinition){
            return this.visitTypeMapperDefinition(node);
        } else if(node instanceof AST.ResourceParameter){
            return this.visitResourceParameter(node);
        } else if(node instanceof AST.ReturnType){
            return this.visitReturnType(node);
        } else if(node instanceof AST.BlockStatement){
            return this.visitBlockStatement(node);
        } else if(node instanceof AST.VariableDefinition){
            return this.visitVariableDefinition(node);
        }

    }

    /**
     * @param node {ASTNode}
     */
    endVisit(node) {
        if(node instanceof AST.BallerinaASTRoot){
            return this.endVisitBallerinaASTRoot(node);
        } else if(node instanceof AST.ServiceDefinition){
            return this.endVisitServiceDefinition(node);
        } else if(node instanceof AST.ResourceDefinition){
            return this.endVisitResourceDefinition(node);
        } else if(node instanceof AST.FunctionDefinition){
            return this.endVisitFunctionDefinition(node);
        } else if(node instanceof AST.CommentStatement){
            return this.endVisitCommentStatement(node);
        } else if(node instanceof AST.Statement){
            return this.endVisitStatement(node);
        }  else if(node instanceof AST.PackageDefinition){
            return this.endVisitPackageDefinition(node);
        } else if(node instanceof AST.ImportDeclaration){
            return this.endVisitImportDeclaration(node);
        } else if(node instanceof AST.ConstantDefinition){
            return this.endVisitConstantDefinition(node);
        } else if(node instanceof AST.WorkerDeclaration){
            return this.endVisitWorkerDeclaration(node);
        } else if( node instanceof AST.ConnectorDeclaration){
            return this.endVisitConnectorDeclaration(node);
        } else if( node instanceof AST.Assignment){
            return this.endVisitAssignment(node);
        } else if(node instanceof AST.FunctionInvocationExpression) {
            return this.endVisitFuncInvocationExpression(node);
        } else if(node instanceof AST.ActionInvocationExpression){
                return this.endVisitActionInvocationExpression(node);
        } else if(node instanceof AST.Expression){
            return this.endVisitExpression(node);
        } else if(node instanceof AST.VariableDeclaration){
            return this.endVisitVariableDeclaration(node);
        } else if(node instanceof AST.ConnectorDefinition){
            return this.endVisitConnectorDefinition(node);
        } else if(node instanceof AST.ConnectorAction){
            return this.endVisitConnectorAction(node);
        } else if(node instanceof AST.StructDefinition){
            return this.endVisitStructDefinition(node);
        } else if(node instanceof AST.TypeMapperDefinition){
            return this.endVisitTypeMapperDefinition(node);
        } else if(node instanceof AST.ResourceParameter){
            return this.endVisitResourceParameter(node);
        } else if(node instanceof AST.ReturnType){
            return this.endVisitReturnType(node);
        } else if(node instanceof AST.BlockStatement){
            return this.endVisitBlockStatement(node);
        } else if(node instanceof AST.VariableDefinition){
            return this.endVisitVariableDefinition(node);
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

    beginVisitServiceDefinition(serviceDefinition) {
    }

    visitServiceDefinition(serviceDefinition) {
    }

    endVisitServiceDefinition(serviceDefinition) {
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

    canVisitTypeMapperDefinition(typeMapperDefinition) {
        return false;
    }

    beginVisitTypeMapperDefinition(typeMapperDefinition) {
    }

    visitTypeMapperDefinition(typeMapperDefinition) {
    }

    endVisitTypeMapperDefinition(typeMapperDefinition) {
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

    canVisitCommentStatement(variableDefinition) {
        return false;
    }

    beginVisitCommentStatement(variableDefinition) {
    }

    visitCommentStatement(variableDefinition) {
    }

    endVisitCommentStatement(variableDefinition) {
    }
}

export default ASTVisitor;
