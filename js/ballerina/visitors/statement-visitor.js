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
define(['lodash', 'log', './ast-visitor', '../ast/module'],
    function(_, log, ASTVisitor, AST) {

    var StatementVisitor = function() {
        ASTVisitor.call(this);
    };

    StatementVisitor.prototype = Object.create(ASTVisitor.prototype);
    StatementVisitor.prototype.constructor = StatementVisitor;

    /**
     * @param node {ASTNode}
     */
    StatementVisitor.prototype.canVisit = function(node){
        if(node instanceof AST.IfStatement){
            return this.canVisitIfStatement(node);
        } else if(node instanceof AST.TryCatchStatement){
            return this.canVisitTryCatchStatement(node);
        }
    };

    /**
     * @param node {ASTNode}
     */
    StatementVisitor.prototype.beginVisit = function(node){
        if(node instanceof AST.IfStatement){
            return this.beginVisitIfStatement(node);
        } else if(node instanceof AST.TryCatchStatement){
            return this.beginVisitTryCatchStatement(node);
        }
    };

    /**
     * @param node {ASTNode}
     */
    StatementVisitor.prototype.visit = function(node){
        if(node instanceof AST.IfStatement){
            return this.visitIfStatement(node);
        } else if(node instanceof AST.TryCatchStatement){
            return this.visitTryCatchStatement(node);
        }
    };

    /**
     * @param node {ASTNode}
     */
    StatementVisitor.prototype.endVisit = function(node){
        if(node instanceof AST.IfStatement){
            return this.endVisitIfStatement(node);
        } else if(node instanceof AST.TryCatchStatement){
            return this.endVisitTryCatchStatement(node);
        }
    };

    ASTVisitor.prototype.canVisitIfStatement = function(statement){
        return false;
    };
    ASTVisitor.prototype.beginVisitIfStatement = function(statement){
    };
    ASTVisitor.prototype.visitIfStatement= function(statement){
    };
    ASTVisitor.prototype.endVisitIfStatement = function(statement){
    };

    ASTVisitor.prototype.canVisitTryCatchStatement = function(statement){
        return false;
    };
    ASTVisitor.prototype.beginVisitTryCatchStatement = function(statement){
    };
    ASTVisitor.prototype.visitTryCatchStatement= function(statement){
    };
    ASTVisitor.prototype.endVisitTryCatchStatement = function(statement){
    };

    return StatementVisitor;
});
