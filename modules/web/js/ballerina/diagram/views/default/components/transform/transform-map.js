/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ASTFactory from '../../../../../ast/ast-factory';

const StatementType = {
    MAP: 1, // mapping statement
    TEMP_CAST: 2, // declare a temp variable for cast or conversion
    TEMP_FUNC: 3, // declare a temp variable for a function invocation
    CONST: 4, // a constant variable declaration
};

/**
 * Maintains transform statement map containing details of input and output
 * @class TransformStmtMap
 */
class TransformMap {

    constructor(args) {
        this._transformMap = [];
        this._transformStmt = _.get(args, 'transformStmt');
        // this._transformStmt.on('tree-modified', () => {
        //     this.init();
        // });
        this.init();
    }

    setTransformStmt(transformStmt) {
        this._transformStmt = transformStmt;
        this.init();
    }

    init() {
        this._transformMap = [];
        const transformInput = this._transformStmt.getInput().map((exp) => {
            return exp.getExpressionString();
        });
        const transformOutput = this._transformStmt.getOutput().map((exp) => {
            return exp.getExpressionString();
        });

        this._transformMap = this._transformStmt.getChildren().map((statement, index) => {
            const statementMap = { index, statement };
            statementMap.type = StatementType.MAP;
            if (ASTFactory.isAssignmentStatement(statement)) {
                const isVar = statement.getIsDeclaredWithVar();
                statementMap.output = statement.getLeftExpression().getChildren().map((expression, expIndex) => {
                    const outputNode = { index: expIndex };
                    if (transformOutput.includes(expression.getExpressionString())) {
                        outputNode.direct = true;
                        outputNode.key = expression.getExpressionString().trim();
                        return outputNode;
                    }
                    if (expression.getExpressionString() === '_') {
                        outputNode.error = true;
                        outputNode.key = '_';
                        return outputNode;
                    }
                    if (isVar) {
                        outputNode.temp = true;
                        outputNode.key = expression.getExpressionString();
                        return outputNode;
                    }
                    outputNode.direct = true;
                    outputNode.name = expression.getExpressionString();
                    return outputNode;
                });

                const rightExp = statement.getRightExpression();
                const inputNode = {};
                if (ASTFactory.isFunctionInvocationExpression(rightExp)) {
                    inputNode.funInv = true;
                } else if (transformInput.includes(rightExp.getExpressionString())) {
                    inputNode.direct = true;
                    inputNode.key = rightExp.getExpressionString();
                } else {
                    inputNode.temp = true;
                    inputNode.key = rightExp.getExpressionString();
                }
                statementMap.input = inputNode;
            }
            return statementMap;
        });
        console.log(this._transformMap);
    }

    getOutputMapping(key) {
        const mapping = this._transformMap.find((stmt) => {
            return stmt.output.find((out) => {
                return (out.key === key);
            });
        });
        if (mapping) {
            return mapping.statement;
        }
        return undefined;
    }

    // /**
    //  * If the target is a cast expression, we need to find the underlying expression
    //  * that needs to be mapped in the view. Similarly if the source is a temp variable,
    //  * corresponding expression that needs to be mapped is also found here.
    //  * @param {Expression} expression lhs or rhs expression
    //  * @param {boolean} isTempResolved should the mapping for temp vars be resolved
    //  * @returns {Expression} mapping expression
    //  * @memberof TransformNodeManager
    //  */
    // getMappingExpression(expression, isTempResolved = true) {
    //     if (BallerinaASTFactory.isFieldBasedVarRefExpression(expression)) {
    //         return expression;
    //     }
    //     if (BallerinaASTFactory.isSimpleVariableReferenceExpression(expression)) {
    //         if (isTempResolved && (expression.getVariableName().startsWith('__temp'))) {
    //             const assignmentStmt = this.findAssignedVertexForTemp(expression);
    //             if (assignmentStmt) {
    //                 return this.getMappingExpression(assignmentStmt.getRightExpression());
    //             }
    //             return expression;
    //         }
    //         return expression;
    //     }
    //     if (BallerinaASTFactory.isTypeConversionExpression(expression) ||
    //             BallerinaASTFactory.isTypeCastExpression(expression)) {
    //         return expression.getRightExpression();
    //     }
    //     return expression;
    // }
}

export default TransformMap;
