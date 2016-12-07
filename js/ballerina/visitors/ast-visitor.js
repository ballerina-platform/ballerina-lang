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

    var ASTVisitor = function() {};

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
        } else if(node instanceof AST.Statement){
            return this.visitStatementDefinition(node);
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

    return ASTVisitor;

});