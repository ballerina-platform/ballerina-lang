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
        } else if (node instanceof AST.Statement) {
            return this.canVisitStatement(node);
        } else if (node instanceof AST.PackageDefinition) {
            return this.canVisitPackageDefinition(node);
        } else if (node instanceof AST.ImportDeclaration) {
            return this.canVisitImportDeclaration(node);
        } else if (node instanceof AST.WorkerDeclaration) {
            return this.canVisitWorkerDeclaration(node);
        } else if( node instanceof AST.ConnectorDeclaration){
            return this.canVisitConnectorDeclaration(node);
        } else if( node instanceof AST.Assignment){
            return this.canVisitAssignment(node);
        } else if( node instanceof AST.Expression){
            return this.canVisitExpression(node);
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
        }else if(node instanceof AST.FunctionDefinition){
            return this.beginVisitFunctionDefinition(node);
        } else if (node instanceof AST.Statement) {
            return this.beginVisitStatement(node);
        } else if (node instanceof AST.PackageDefinition) {
            return this.beginVisitPackageDefinition(node);
        } else if (node instanceof AST.ImportDeclaration) {
            return this.beginVisitImportDeclaration(node);
        } else if (node instanceof AST.WorkerDeclaration) {
            return this.beginVisitWorkerDeclaration(node);
        } else if( node instanceof AST.ConnectorDeclaration){
            return this.beginVisitConnectorDeclaration(node);
        } else if( node instanceof AST.Assignment){
            return this.beginVisitAssignment(node);
        } else if (node instanceof AST.Expression) {
            return this.beginVisitExpression(node);
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
        } else if(node instanceof AST.Statement){
            return this.visitStatement(node);
        } else if(node instanceof AST.PackageDefinition){
            return this.visitPackageDefinition(node);
        } else if(node instanceof AST.ImportDeclaration){
            return this.visitImportDeclaration(node);
        } else if(node instanceof AST.WorkerDeclaration){
            return this.visitWorkerDeclaration(node);
        } else if( node instanceof AST.ConnectorDeclaration){
            return this.visitConnectorDeclaration(node);
        } else if( node instanceof AST.Assignment){
            return this.visitAssignment(node);
        } else if(node instanceof AST.Expression){
            return this.visitExpression(node);
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
        } else if(node instanceof AST.Statement){
            return this.endVisitStatement(node);
        } else if(node instanceof AST.PackageDefinition){
            return this.endVisitPackageDefinition(node);
        } else if(node instanceof AST.ImportDeclaration){
            return this.endVisitImportDeclaration(node);
        } else if(node instanceof AST.WorkerDeclaration){
            return this.endVisitWorkerDeclaration(node);
        } else if( node instanceof AST.ConnectorDeclaration){
            return this.endVisitConnectorDeclaration(node);
        } else if( node instanceof AST.Assignment){
            return this.endVisitAssignment(node);
        } else if(node instanceof AST.Expression){
            return this.endVisitExpression(node);
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

    return ASTVisitor;
});