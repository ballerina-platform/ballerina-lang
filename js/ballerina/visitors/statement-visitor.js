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

    var StatementVisitor = function (args) {
        ASTVisitor.call(this, args);
    };

    StatementVisitor.prototype = Object.create(ASTVisitor.prototype);
    StatementVisitor.prototype.constructor = StatementVisitor;

    /**
     * @param node {ASTNode}
     */
    StatementVisitor.prototype.canVisit = function (node) {
        if (node instanceof AST.IfElseStatement) {
            return this.canVisitStatement(node);
        } else if (node instanceof AST.IfStatement) {
            return this.canVisitIfStatement(node);
        } else if (node instanceof AST.ElseStatement) {
            return this.canVisitElseStatement(node);
        } else if (node instanceof AST.TryCatchStatement) {
            return this.canVisitStatement(node);
        } else if (node instanceof AST.TryStatement) {
            return this.canVisitTryStatement(node);
        } else if (node instanceof AST.CatchStatement) {
            return this.canVisitCatchStatement(node);
        } else if (node instanceof AST.WhileStatement) {
            return this.canVisitWhileStatement(node);
        } else if (node instanceof AST.Assignment) {
            return this.canVisitStatement(node);
        }
        else if (node instanceof AST.GetActionStatement) {
            return this.canVisitGetActionStatement(node);
        }
    };

    /**
     * @param node {ASTNode}
     */
    StatementVisitor.prototype.beginVisit = function (node) {
        if (node instanceof AST.IfElseStatement) {
            return this.beginVisitStatement(node);
        } else if (node instanceof AST.IfStatement) {
            return this.beginVisitIfStatement(node);
        } else if (node instanceof AST.ElseStatement) {
            return this.beginVisitElseStatement(node);
        } else if (node instanceof AST.TryCatchStatement) {
            return this.beginVisitStatement(node);
        } else if (node instanceof AST.TryStatement) {
            return this.beginVisitTryStatement(node);
        } else if (node instanceof AST.CatchStatement) {
            return this.beginVisitCatchStatement(node);
        } else if (node instanceof AST.WhileStatement) {
            return this.beginVisitWhileStatement(node);
        } else if (node instanceof AST.Assignment) {
            return this.beginVisitStatement(node);
        }
        else if (node instanceof AST.GetActionStatement) {
            return this.beginVisitGetActionStatement(node);
        }
    };

    /**
     * @param node {ASTNode}
     */
    StatementVisitor.prototype.visit = function (node) {
        if (node instanceof AST.IfElseStatement) {
            return this.visitStatement(node);
        } else if (node instanceof AST.IfStatement) {
            return this.visitIfStatement(node);
        } else if (node instanceof AST.ElseStatement) {
            return this.visitElseStatement(node);
        } else if (node instanceof AST.TryCatchStatement) {
            return this.visitStatement(node);
        } else if (node instanceof AST.TryStatement) {
            return this.visitTryStatement(node);
        } else if (node instanceof AST.CatchStatement) {
            return this.visitCatchStatement(node);
        } else if (node instanceof AST.WhileStatement) {
            return this.visitWhileStatement(node);
        } else if (node instanceof AST.Assignment) {
            return this.visitStatement(node);
        }
        else if (node instanceof AST.GetActionStatement) {
            return this.visitGetActionStatement(node);
        }
    };

    /**
     * @param node {ASTNode}
     */
    StatementVisitor.prototype.endVisit = function (node) {
        if (node instanceof AST.IfElseStatement) {
            return this.endVisitStatement(node);
        } else if (node instanceof AST.IfStatement) {
            return this.endVisitIfStatement(node);
        } else if (node instanceof AST.ElseStatement) {
            return this.endVisitElseStatement(node);
        } else if (node instanceof AST.TryCatchStatement) {
            return this.endVisitStatement(node);
        } else if (node instanceof AST.TryStatement) {
            return this.endVisitTryStatement(node);
        } else if (node instanceof AST.CatchStatement) {
            return this.endVisitCatchStatement(node);
        } else if (node instanceof AST.WhileStatement) {
            return this.endVisitWhileStatement(node);
        } else if (node instanceof AST.Assignment) {
            return this.endVisitStatement(node);
        }
        else if (node instanceof AST.GetActionStatement) {
            return this.endVisitGetActionStatement(node);
        }
    };

    StatementVisitor.prototype.canVisitIfStatement = function (statement) {
        return false;
    };
    StatementVisitor.prototype.beginVisitIfStatement = function (statement) {
    };
    StatementVisitor.prototype.visitIfStatement = function (statement) {
    };
    StatementVisitor.prototype.endVisitIfStatement = function (statement) {
    };

    StatementVisitor.prototype.canVisitElseStatement = function (statement) {
        return false;
    };
    StatementVisitor.prototype.beginVisitElseStatement = function (statement) {
    };
    StatementVisitor.prototype.visitElseStatement = function (statement) {
    };
    StatementVisitor.prototype.endVisitElseStatement = function (statement) {
    };

    StatementVisitor.prototype.canVisitTryStatement = function (statement) {
        return false;
    };
    StatementVisitor.prototype.beginVisitTryStatement = function (statement) {
    };
    StatementVisitor.prototype.visitTryStatement = function (statement) {
    };
    StatementVisitor.prototype.endVisitTryStatement = function (statement) {
    };

    StatementVisitor.prototype.canVisitCatchStatement = function (statement) {
        return false;
    };
    StatementVisitor.prototype.beginVisitCatchStatement = function (statement) {
    };
    StatementVisitor.prototype.visitCatchStatement = function (statement) {
    };
    StatementVisitor.prototype.endVisitCatchStatement = function (statement) {
    };

    StatementVisitor.prototype.canVisitWhileStatement = function (statement) {
        return false;
    };
    StatementVisitor.prototype.beginVisitWhileStatement = function (statement) {
    };
    StatementVisitor.prototype.visitWhileStatement = function (statement) {
    };
    StatementVisitor.prototype.endVisitWhileStatement = function (statement) {
    };

    StatementVisitor.prototype.canVisitStatement = function (statement) {
        return false;
    };
    StatementVisitor.prototype.beginVisitStatement = function (statement) {
    };
    StatementVisitor.prototype.visitStatement = function (statement) {
    };
    StatementVisitor.prototype.endVisitStatement = function (statement) {
    };

    StatementVisitor.prototype.canVisitGetActionStatement = function (statement) {
        return false;
    };
    StatementVisitor.prototype.beginVisitGetActionStatement = function (statement) {
    };
    StatementVisitor.prototype.visitGetActionStatement = function (statement) {
    };
    StatementVisitor.prototype.endVisitGetActionStatement = function (statement) {
    };

    return StatementVisitor;
});
