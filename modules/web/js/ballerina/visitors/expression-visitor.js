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
define(['lodash', 'log', './ast-visitor', '../ast/module'], function (_, log, ASTVisitor, AST) {

    var ExpressionVisitor = function (args) {
        ASTVisitor.call(this, args);
    };

    ExpressionVisitor.prototype = Object.create(ASTVisitor.prototype);
    ExpressionVisitor.prototype.constructor = ExpressionVisitor;

    ExpressionVisitor.prototype.canVisitStructFieldAccessExpression = function (expression) {
        return false;
    };
    ExpressionVisitor.prototype.beginVisitStructFieldAccessExpression = function (expression) {
    };
    ExpressionVisitor.prototype.visitStructFieldAccessExpression = function (expression) {
    };
    ExpressionVisitor.prototype.endVisitStructFieldAccessExpression = function (expression) {
    };

    ExpressionVisitor.prototype.canVisitVariableReferenceExpression = function (expression) {
        return false;
    };
    ExpressionVisitor.prototype.beginVisitVariableReferenceExpression = function (expression) {
    };
    ExpressionVisitor.prototype.visitVariableReferenceExpression = function (expression) {
    };
    ExpressionVisitor.prototype.endVisitVariableReferenceExpression = function (expression) {
    };

    ExpressionVisitor.prototype.canVisitRefTypeInitExpression = function (expression) {
        return false;
    };
    ExpressionVisitor.prototype.beginVisitRefTypeInitExpression = function (expression) {
    };
    ExpressionVisitor.prototype.visitRefTypeInitExpression= function (expression) {
    };
    ExpressionVisitor.prototype.endVisitRefTypeInitExpression = function (expression) {
    };
    
    /**
     * @param node {ASTNode}
     */
    ExpressionVisitor.prototype.visitExpression = function (node) {
        if (node instanceof AST.StructFieldAccessExpression) {
            return this.visitStructFieldAccessExpression(node);
        } else if (node instanceof AST.VariableReferenceExpression) {
            return this.visitVariableReferenceExpression(node);
        } else if (node instanceof AST.RefTypeInitExpression) {
            return this.visitRefTypeInitExpression(node);
        }
    };

    /**
     * @param node {ASTNode}
     */
    ExpressionVisitor.prototype.canVisitExpression = function (node) {
        if (node instanceof AST.StructFieldAccessExpression) {
            return this.canVisitStructFieldAccessExpression(node);
        } else if (node instanceof AST.VariableReferenceExpression) {
            return this.canVisitVariableReferenceExpression(node);
        } else if (node instanceof AST.RefTypeInitExpression) {
            return this.canVisitRefTypeInitExpression(node);
        }
    };

    /**
     * @param node {ASTNode}
     */
    ExpressionVisitor.prototype.beginVisitExpression= function (node) {
        if (node instanceof AST.StructFieldAccessExpression) {
            this.beginVisitStructFieldAccessExpression(node);
        } else if (node instanceof AST.VariableReferenceExpression) {
            this.beginVisitVariableReferenceExpression(node);
        } else if (node instanceof AST.RefTypeInitExpression) {
            this.beginVisitRefTypeInitExpression(node);
        }
    };

    /**
     * @param node {ASTNode}
     */
    ExpressionVisitor.prototype.endVisitExpression = function (node) {
        if (node instanceof AST.StructFieldAccessExpression) {
            return this.endVisitStructFieldAccessExpression(node);
        } else if (node instanceof AST.VariableReferenceExpression) {
            return this.endVisitVariableReferenceExpression(node);
        } else if (node instanceof AST.RefTypeInitExpression) {
            return this.endVisitRefTypeInitExpression(node);
        }
    };

    return ExpressionVisitor;
});
