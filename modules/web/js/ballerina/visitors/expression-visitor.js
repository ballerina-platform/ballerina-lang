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
import ASTVisitor from './ast-visitor';
import AST from '../ast/module';

class ExpressionVisitor extends ASTVisitor {
    constructor(args) {
        super(args);
    }

    canVisitStructFieldAccessExpression(expression) {
        return false;
    }

    beginVisitStructFieldAccessExpression(expression) {
    }

    visitStructFieldAccessExpression(expression) {
    }

    endVisitStructFieldAccessExpression(expression) {
    }

    canVisitVariableReferenceExpression(expression) {
        return false;
    }

    beginVisitVariableReferenceExpression(expression) {
    }

    visitVariableReferenceExpression(expression) {
    }

    endVisitVariableReferenceExpression(expression) {
    }

    canVisitReferenceTypeInitExpression(expression) {
        return false;
    }

    beginVisitReferenceTypeInitExpression(expression) {
    }

    visitReferenceTypeInitExpression(expression) {
    }

    endVisitReferenceTypeInitExpression(expression) {
    }

    canVisitTypeCastExpression(expression) {
        return false;
    }

    beginVisitTypeCastExpression(expression) {
    }

    visitTypeCastExpression(expression) {
    }

    endVisitTypeCastExpression(expression) {
    }

    /**
     * @param node {ASTNode}
     */
    visitExpression(node) {
        if (node instanceof AST.StructFieldAccessExpression) {
            return this.visitStructFieldAccessExpression(node);
        } else if (node instanceof AST.VariableReferenceExpression) {
            return this.visitVariableReferenceExpression(node);
        } else if (node instanceof AST.ReferenceTypeInitExpression) {
            return this.visitReferenceTypeInitExpression(node);
        } else if (node instanceof AST.TypeCastExpression) {
            return this.visitTypeCastExpression(node);
        }
    }

    /**
     * @param node {ASTNode}
     */
    canVisitExpression(node) {
        if (node instanceof AST.StructFieldAccessExpression) {
            return this.canVisitStructFieldAccessExpression(node);
        } else if (node instanceof AST.VariableReferenceExpression) {
            return this.canVisitVariableReferenceExpression(node);
        } else if (node instanceof AST.ReferenceTypeInitExpression) {
            return this.canVisitReferenceTypeInitExpression(node);
        } else if (node instanceof AST.TypeCastExpression) {
            return this.canVisitTypeCastExpression(node);
        }
    }

    /**
     * @param node {ASTNode}
     */
    beginVisitExpression(node) {
        if (node instanceof AST.StructFieldAccessExpression) {
            this.beginVisitStructFieldAccessExpression(node);
        } else if (node instanceof AST.VariableReferenceExpression) {
            this.beginVisitVariableReferenceExpression(node);
        } else if (node instanceof AST.ReferenceTypeInitExpression) {
            this.beginVisitReferenceTypeInitExpression(node);
        } else if (node instanceof AST.TypeCastExpression) {
            this.beginVisitTypeCastExpression(node);
        }
    }

    /**
     * @param node {ASTNode}
     */
    endVisitExpression(node) {
        if (node instanceof AST.StructFieldAccessExpression) {
            return this.endVisitStructFieldAccessExpression(node);
        } else if (node instanceof AST.VariableReferenceExpression) {
            return this.endVisitVariableReferenceExpression(node);
        } else if (node instanceof AST.ReferenceTypeInitExpression) {
            return this.endVisitReferenceTypeInitExpression(node);
        } else if (node instanceof AST.TypeCastExpression) {
            return this.endVisitTypeCastExpression(node);
        }
    }
}

export default ExpressionVisitor;

