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
import ASTVisitor from './ast-visitor';
import ASTFactory from './../ast/ballerina-ast-factory';

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

    canVisitTypeConversionExpression(expression) {
        return false;
    }

    beginVisitTypeConversionExpression(expression) {
    }

    visitTypeConversionExpression(expression) {
    }

    endVisitTypeConversionExpression(expression) {
    }

    /**
     * @param node {ASTNode}
     */
    visitExpression(node) {
        if (ASTFactory.isStructFieldAccessExpression(node)) {
            return this.visitStructFieldAccessExpression(node);
        } else if (ASTFactory.isVariableReferenceExpression(node)) {
            return this.visitVariableReferenceExpression(node);
        } else if (ASTFactory.isReferenceTypeInitExpression(node)) {
            return this.visitReferenceTypeInitExpression(node);
        } else if (ASTFactory.isTypeCastExpression(node)) {
            return this.visitTypeCastExpression(node);
        } else if (ASTFactory.isTypeConversionExpression(node)) {
            return this.visitTypeConversionExpression(node);
        }
    }

    /**
     * @param node {ASTNode}
     */
    canVisitExpression(node) {
        if (ASTFactory.isStructFieldAccessExpression(node)) {
            return this.canVisitStructFieldAccessExpression(node);
        } else if (ASTFactory.isVariableReferenceExpression(node)) {
            return this.canVisitVariableReferenceExpression(node);
        } else if (ASTFactory.isReferenceTypeInitExpression(node)) {
            return this.canVisitReferenceTypeInitExpression(node);
        } else if (ASTFactory.isTypeCastExpression(node)) {
            return this.canVisitTypeCastExpression(node);
        } else if (ASTFactory.isTypeConversionExpression(node)) {
            return this.canVisitTypeConversionExpression(node);
        }
    }

    /**
     * @param node {ASTNode}
     */
    beginVisitExpression(node) {
        if (ASTFactory.isStructFieldAccessExpression(node)) {
            this.beginVisitStructFieldAccessExpression(node);
        } else if (ASTFactory.isVariableReferenceExpression(node)) {
            this.beginVisitVariableReferenceExpression(node);
        } else if (ASTFactory.isReferenceTypeInitExpression(node)) {
            this.beginVisitReferenceTypeInitExpression(node);
        } else if (ASTFactory.isTypeCastExpression(node)) {
            this.beginVisitTypeCastExpression(node);
        } else if (ASTFactory.isTypeConversionExpression(node)) {
            this.beginVisitTypeConversionExpression(node);
        }
    }

    /**
     * @param node {ASTNode}
     */
    endVisitExpression(node) {
        if (ASTFactory.isStructFieldAccessExpression(node)) {
            return this.endVisitStructFieldAccessExpression(node);
        } else if (ASTFactory.isVariableReferenceExpression(node)) {
            return this.endVisitVariableReferenceExpression(node);
        } else if (ASTFactory.isReferenceTypeInitExpression(node)) {
            return this.endVisitReferenceTypeInitExpression(node);
        } else if (ASTFactory.isTypeCastExpression(node)) {
            return this.endVisitTypeCastExpression(node);
        } else if (ASTFactory.isTypeConversionExpression(node)) {
            return this.endVisitTypeConversionExpression(node);
        }
    }
}

export default ExpressionVisitor;
