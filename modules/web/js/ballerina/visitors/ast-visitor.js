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
define(['lodash', 'log', 'event_channel', './../ast/module'], function(_, log, EventChannel, AST) {

    var ASTVisitor = function() {
        EventChannel.call(this);
    };

    ASTVisitor.prototype = Object.create(EventChannel.prototype);
    ASTVisitor.prototype.constructor = ASTVisitor;

    /**
     * @param node {ASTNode}
     */
    ASTVisitor.prototype.canVisit = function(node){
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
    };

    /**
     * @param node {ASTNode}
     */
    ASTVisitor.prototype.beginVisit = function(node){
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
    };

    /**
     * @param node {ASTNode}
     */
    ASTVisitor.prototype.visit = function(node){
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

    };

    /**
     * @param node {ASTNode}
     */
    ASTVisitor.prototype.endVisit = function(node){
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

    };

    ASTVisitor.prototype.canVisitBallerinaASTRoot = function(ballerinaASTRoot){
        return false;
    };
    ASTVisitor.prototype.beginVisitBallerinaASTRoot = function(ballerinaASTRoot){
    };
    ASTVisitor.prototype.visitBallerinaASTRoot= function(ballerinaASTRoot){
    };
    ASTVisitor.prototype.endVisitBallerinaASTRoot = function(ballerinaASTRoot){
    };

    ASTVisitor.prototype.canVisitServiceDefinition = function(serviceDefinition){
        return false;
    };
    ASTVisitor.prototype.beginVisitServiceDefinition = function(serviceDefinition){
    };
    ASTVisitor.prototype.visitServiceDefinition = function(serviceDefinition){
    };
    ASTVisitor.prototype.endVisitServiceDefinition = function(serviceDefinition){
    };

    ASTVisitor.prototype.canVisitConnectorDefinition = function(connectorDefinition){
        return false;
    };
    ASTVisitor.prototype.beginVisitConnectorDefinition = function(connectorDefinition){
    };
    ASTVisitor.prototype.visitConnectorDefinition = function(connectorDefinition){
    };
    ASTVisitor.prototype.endVisitConnectorDefinition = function(connectorDefinition){
    };

    ASTVisitor.prototype.canVisitFunctionDefinition = function(functionDefinition){
        return false;
    };
    ASTVisitor.prototype.beginVisitFunctionDefinition = function(functionDefinition){
    };
    ASTVisitor.prototype.visitFunctionDefinition = function(functionDefinition){
    };
    ASTVisitor.prototype.endVisitFunctionDefinition = function(functionDefinition){
    };

    ASTVisitor.prototype.canVisitResourceDefinition = function(resourceDefinition){
        return false;
    };
    ASTVisitor.prototype.beginVisitResourceDefinition = function(resourceDefinition){
    };
    ASTVisitor.prototype.visitResourceDefinition = function(resourceDefinition){
    };
    ASTVisitor.prototype.endVisitResourceDefinition = function(resourceDefinition){
    };

    ASTVisitor.prototype.canVisitFunctionDefinition = function(resourceDefinition){
        return false;
    };
    ASTVisitor.prototype.beginVisitFunctionDefinition = function(resourceDefinition){
    };
    ASTVisitor.prototype.visitFunctionDefinition = function(resourceDefinition){
    };
    ASTVisitor.prototype.endVisitFunctionDefinition = function(resourceDefinition){
    };

    ASTVisitor.prototype.canVisitStructDefinition = function(structDefinition){
        return false;
    };
    ASTVisitor.prototype.beginVisitStructDefinition = function(structDefinition){
    };
    ASTVisitor.prototype.visitStructDefinition = function(structDefinition){
    };
    ASTVisitor.prototype.endVisitStructDefinition = function(structDefinition){
    };

    ASTVisitor.prototype.canVisitTypeMapperDefinition = function(typeMapperDefinition){
        return false;
    };
    ASTVisitor.prototype.beginVisitTypeMapperDefinition = function(typeMapperDefinition){
    };
    ASTVisitor.prototype.visitTypeMapperDefinition = function(typeMapperDefinition){
    };
    ASTVisitor.prototype.endVisitTypeMapperDefinition = function(typeMapperDefinition){
    };

    ASTVisitor.prototype.canVisitResourceParameter = function(resourceParameter){
        return false;
    };
    ASTVisitor.prototype.beginVisitResourceParameter = function(resourceParameter){
    };
    ASTVisitor.prototype.visitResourceParameter = function(resourceParameter){
    };
    ASTVisitor.prototype.endVisitResourceParameter = function(resourceParameter){
    };

    ASTVisitor.prototype.canVisitBlockStatement = function(blockStatement){
        return false;
    };
    ASTVisitor.prototype.beginVisitBlockStatement = function(blockStatement){
    };
    ASTVisitor.prototype.visitBlockStatement = function(blockStatement){
    };
    ASTVisitor.prototype.endVisitBlockStatement = function(blockStatement){
    };

    ASTVisitor.prototype.canVisitReturnType = function(returnType){
        return false;
    };
    ASTVisitor.prototype.beginVisitReturnType = function(returnType){
    };
    ASTVisitor.prototype.visitReturnType = function(returnType){
    };
    ASTVisitor.prototype.endVisitReturnType = function(returnType){
    };

    ASTVisitor.prototype.canVisitPackageDefinition = function(packageDefinition){
        return false;
    };
    ASTVisitor.prototype.beginVisitPackageDefinition = function(packageDefinition){
    };
    ASTVisitor.prototype.visitPackageDefinition = function(packageDefinition){
    };
    ASTVisitor.prototype.endVisitPackageDefinition = function(packageDefinition){
    };

    ASTVisitor.prototype.canVisitImportDeclaration = function(importDeclaration){
        return false;
    };
    ASTVisitor.prototype.beginVisitImportDeclaration = function(importDeclaration){
    };
    ASTVisitor.prototype.visitImportDeclaration = function(importDeclaration){
    };
    ASTVisitor.prototype.endVisitImportDeclaration = function(importDeclaration){
    };

    ASTVisitor.prototype.canVisitConstantDefinition = function(constantDefinition){
        return false;
    };
    ASTVisitor.prototype.beginVisitConstantDefinition = function(constantDefinition){
    };
    ASTVisitor.prototype.visitConstantDefinition = function(constantDefinition){
    };
    ASTVisitor.prototype.endVisitConstantDefinition = function(constantDefinition){
    };

    ASTVisitor.prototype.canVisitWorkerDeclaration = function(importDeclaration){
        return false;
    };
    ASTVisitor.prototype.beginVisitWorkerDeclaration = function(importDeclaration){
    };
    ASTVisitor.prototype.visitWorkerDeclaration = function(importDeclaration){
    };
    ASTVisitor.prototype.endVisitWorkerDeclaration = function(importDeclaration){
    };

    ASTVisitor.prototype.canVisitStatement = function(statement){
        return false;
    };
    ASTVisitor.prototype.beginVisitStatement = function(statement){
    };
    ASTVisitor.prototype.visitStatement = function(statement){
    };
    ASTVisitor.prototype.endVisitStatement = function(statement){
    };

    ASTVisitor.prototype.canVisitExpression = function(statement){
        return false;
    };
    ASTVisitor.prototype.beginVisitExpression = function(statement){
    };
    ASTVisitor.prototype.visitExpression = function(statement){
    };
    ASTVisitor.prototype.endVisitExpression = function(statement){
    };

    ASTVisitor.prototype.canVisitConnectorAction = function(connectorAction){
        return false;
    };
    ASTVisitor.prototype.beginVisitConnectorAction = function(connectorAction){
    };
    ASTVisitor.prototype.visitConnectorAction = function(connectorAction){
    };
    ASTVisitor.prototype.endVisitConnectorAction = function(connectorAction){
    };

    ASTVisitor.prototype.canVisitActionInvocationExpression = function(){
        return true;
    };
    ASTVisitor.prototype.beginVisitActionInvocationExpression = function(expression){
    };
    ASTVisitor.prototype.visitActionInvocationExpression = function(expression){
    };
    ASTVisitor.prototype.endVisitActionInvocationExpression = function(expression){
    };

    /**
     *
     * @param statement - statement which should be checked
     * @returns {boolean}
     */
    ASTVisitor.prototype.canVisitFuncInvocationExpression = function(statement){
        return false;
    };
    /**
     *
     * @param statement - statement to begin visitFuncInvocationExpression with
     */
    ASTVisitor.prototype.beginVisitFuncInvocationExpression = function(statement){
    };
    /**
     *
     * @param statement - statement to visit FuncInvocationExpression with
     */
    ASTVisitor.prototype.visitFuncInvocationExpression = function(statement){
    };
    /**
     *
     * @param statement - statement to end visitFuncInvocationExpression with
     */
    ASTVisitor.prototype.endVisitFuncInvocationExpression = function(statement){
    };

    ASTVisitor.prototype.canVisitConnectorDeclaration = function(connectorDeclaration){
        return false;
    };
    ASTVisitor.prototype.beginVisitConnectorDeclaration = function(connectorDeclaration){
    };
    ASTVisitor.prototype.visitConnectorDeclaration = function(connectorDeclaration){
    };
    ASTVisitor.prototype.endVisitConnectorDeclaration = function(connectorDeclaration){
    };

    ASTVisitor.prototype.canVisitAssignment = function(assignment){
        return false;
    };
    ASTVisitor.prototype.beginVisitAssignment = function(assignment){
    };
    ASTVisitor.prototype.visitAssignment = function(assignment){
    };
    ASTVisitor.prototype.endVisitAssignment = function(assignment){
    };

    ASTVisitor.prototype.canVisitVariableDeclaration = function(variableDeclaration){
        return false;
    };
    ASTVisitor.prototype.beginVisitVariableDeclaration = function(variableDeclaration){
    };
    ASTVisitor.prototype.visitVariableDeclaration = function(variableDeclaration){
    };
    ASTVisitor.prototype.endVisitVariableDeclaration = function(variableDeclaration){
    };

    ASTVisitor.prototype.canVisitVariableDefinition = function(variableDefinition){
        return false;
    };
    ASTVisitor.prototype.beginVisitVariableDefinition = function(variableDefinition){
    };
    ASTVisitor.prototype.visitVariableDefinition = function(variableDefinition){
    };
    ASTVisitor.prototype.endVisitVariableDefinition = function(variableDefinition){
    };

    ASTVisitor.prototype.canVisitCommentStatement = function(variableDefinition){
        return false;
    };
    ASTVisitor.prototype.beginVisitCommentStatement = function(variableDefinition){
    };
    ASTVisitor.prototype.visitCommentStatement = function(variableDefinition){
    };
    ASTVisitor.prototype.endVisitCommentStatement = function(variableDefinition){
    };

    return ASTVisitor;
});