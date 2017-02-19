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

    ExpressionVisitor.prototype.canVisitReferenceTypeInitExpression = function (expression) {
        return false;
    };
    ExpressionVisitor.prototype.beginVisitReferenceTypeInitExpression = function (expression) {
    };
    ExpressionVisitor.prototype.visitReferenceTypeInitExpression= function (expression) {
    };
    ExpressionVisitor.prototype.endVisitReferenceTypeInitExpression = function (expression) {
    };

    ExpressionVisitor.prototype.canVisitTypeCastExpression = function (expression) {
        return false;
    };
    ExpressionVisitor.prototype.beginVisitTypeCastExpression = function (expression) {
    };
    ExpressionVisitor.prototype.visitTypeCastExpression= function (expression) {
    };
    ExpressionVisitor.prototype.endVisitTypeCastExpression = function (expression) {
    };
    
    /**
     * @param node {ASTNode}
     */
    ExpressionVisitor.prototype.visitExpression = function (node) {
        if (node instanceof AST.StructFieldAccessExpression) {
            return this.visitStructFieldAccessExpression(node);
        } else if (node instanceof AST.VariableReferenceExpression) {
            return this.visitVariableReferenceExpression(node);
        } else if (node instanceof AST.ReferenceTypeInitExpression) {
            return this.visitReferenceTypeInitExpression(node);
        } else if (node instanceof AST.TypeCastExpression) {
            return this.visitTypeCastExpression(node);
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
        } else if (node instanceof AST.ReferenceTypeInitExpression) {
            return this.canVisitReferenceTypeInitExpression(node);
        } else if (node instanceof AST.TypeCastExpression) {
            return this.canVisitTypeCastExpression(node);
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
        } else if (node instanceof AST.ReferenceTypeInitExpression) {
            this.beginVisitReferenceTypeInitExpression(node);
        } else if (node instanceof AST.TypeCastExpression) {
            this.beginVisitTypeCastExpression(node);
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
        } else if (node instanceof AST.ReferenceTypeInitExpression) {
            return this.endVisitReferenceTypeInitExpression(node);
        } else if (node instanceof AST.TypeCastExpression) {
            return this.endVisitTypeCastExpression(node);
        }
    };

    return ExpressionVisitor;
});
