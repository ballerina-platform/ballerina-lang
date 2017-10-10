/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
import log from 'log';
import _ from 'lodash';
import TreeUtil from '../model/tree-util';

export const ExpressionType = {
    ERROR: 'error',
    PLACEHOLDER: 'placeholder',
    TEMPVAR: 'tempvar',
    DIRECT: 'direct',
    CONST: 'const',
};

export const VarPrefix = {
    TEMP: '__temp',
    OUTPUT: '__output',
    NEWVAR: '__newVar',
};

/**
 * Transform utilities class
 */
export default class TransformUtils {
    /**
     * Get a new name for temporary variable
     * @param {string} varPrefix prefix for the variable
     * @param {TransformNode} transformNode transform node for which the new temp variable is created
     * @returns {Expression} temporary var expression
     * @memberof TransformNodeMapper
     */
    static getNewTempVarName(transformNode, varPrefix = VarPrefix.TEMP) {
        const varNameRegex = new RegExp(varPrefix + '[\\d]*');
        const tempVarSuffixes = [];

        transformNode.body.getStatements().forEach((stmt) => {
            let variables = [];
            if (TreeUtil.isAssignment(stmt)) {
                variables = stmt.getVariables();
            } else if (TreeUtil.isVariableDef(stmt)) {
                variables.push(stmt.getVariable());
            }

            variables.forEach((varExp) => {
                const expStr = varExp.getSource();
                if (varNameRegex.test(expStr)) {
                    const index = Number.parseInt(expStr.substring(varPrefix.length + 1), 10) || 0;
                    tempVarSuffixes.push(index);
                }
            });
        });

        // numeric sort by difference
        tempVarSuffixes.sort((a, b) => a - b);
        const index = (tempVarSuffixes[tempVarSuffixes.length - 1] || 0) + 1;
        return varPrefix + index;
    }


    /**
     * Get expressions that are mapped by the assignment statement.
     * The output mappings ignore error expressions and placeholder temporary variables.
     * @param {AssignmentStatement} assignmentStmt assignment statement
     * @returns {[Expression]} expressions that are output mappings
     * @memberof TransformNodeMapper
     */
    static getOutputExpressions(transformNode, assignmentStmt) {
        const outputMappings = [];
        assignmentStmt.getVariables().forEach((exp, index) => {
            outputMappings[index] = { expression: exp };
            if (this.isInTransformInputOutput(transformNode, exp)) {
                outputMappings[index].type = ExpressionType.DIRECT;
            } else if (this.isErrorExpression(transformNode, exp)) {
                outputMappings[index].type = ExpressionType.ERROR;
            } else if (this.isPlaceHolderTempVariable(transformNode, exp)) {
                outputMappings[index].type = ExpressionType.PLACEHOLDER;
            } else if (this.isConstant(transformNode, exp, assignmentStmt)) {
                outputMappings[index].type = ExpressionType.CONST;
            } else {
                outputMappings[index].type = ExpressionType.TEMPVAR;
            }
        });
        return outputMappings;
    }

    /**
     * Check whether the given expression is an input or output expression
     * of transform statement
     * @param {Expression} expression expression to check
     * @param {TransformNode} transformNode transform node to check for expression
     * @returns {boolean} whether or not the expression is an input or an output
     * @memberof TransformNodeMapper
     */
    static isInTransformInputOutput(transformNode, expression) {
        const inputOutput = [...transformNode.inputs, ...transformNode.outputs];
        const ioReference = inputOutput.find((io) => {
            return io === expression.getSource().trim();
        });
        return (ioReference !== undefined);
    }

    /**
    * Checks whether the given expression is an error expression
    * @param {Expression} expression expression
    * @returns {boolean} is or not an error expression
    * @memberof TransformNodeMapper
    */
    static isErrorExpression(transformNode, expression) {
        // TODO: enhance for user defined errors
        return (expression.getSource().trim() === '_');
    }

    /**
     * Check whether the given expression is a placeholder temporary variable in the transform
     * statement scope.
     * E.g. : var __output1 = func(null);
     * Here the __output1 is not declared earlier, but is a temporary variable, which
     * cannot be shown as a mapping.
     * @param {Expression} expression expression to check
     * @param {node} expression expression to check
     * @returns {boolean} whether a placeholder temporary variable or not
     * @memberof TransformNodeMapper
     */
    static isPlaceHolderTempVariable(transformNode, expression) {
        if (this.isInTransformInputOutput(transformNode, expression)) {
            return false;
        }
        const usedStatements = this.getInputStatements(transformNode, expression);
        return (usedStatements.length === 0);
    }

    /**
     * whether the constant expression is a constant value
     * @param {any} expression expression
     * @param {any} assignmentStmt assignment statement
     * @returns whether the expression is a constant
     * @memberof TransformNodeMapper
     */
    static isConstant(transformNode, expression, assignmentStmt) {
        if (!assignmentStmt) {
            assignmentStmt = this.getOutputStatement(transformNode, expression);
        }
        return TreeUtil.isLiteral(assignmentStmt.getExpression());
    }

    /**
     * Check whether the given expression is a temporary variable in the transform
     * statement scope. A temporary variable is a variable that is declared in the
     * transform statement scope and holds a combination of source expressions.
     * E.g.:
     *  1) var __temp1, _ = <int> a;
     *  2) var __temp1 = func(a);
     *  3) var __temp1 = a + b
     *      where a and b are source variables of the transform statement.
     * Mappings of the temp variables can be done in any manner.
     * E.g.:
     *    var __temp1 = func(x);
     *    y = __temp1;
     *    x = __temp1;
     * Here although the mapping is done via a temporary variable, there is an actual
     * value associated, which can be shown as a mapping.
     * @param {Expression} expression expression to check
     * @return {boolean} whether temporary or not
     * @memberof TransformNodeMapper
     */
    static isTempVariable(transformNode, expression, assignmentStmt) {
        if (this.isInTransformInputOutput(transformNode, expression)
            || this.isErrorExpression(transformNode, expression)
            || this.isPlaceHolderTempVariable(transformNode, expression)
            || this.isConstant(transformNode, expression, assignmentStmt)) {
            return false;
        }
        return true;
    }

    /**
     * Get statement that have given expression as output.
     * @param {Expression} expression output expression
     * @param {TransformNode} transformNode transform node searched
     * @return {AssignmentStatement} assignment statement with expression as an output
     * @memberof TransformNodeMapper
     */
    static getOutputStatement(transformNode, expression) {
        return transformNode.body.filterStatements(TreeUtil.isAssignment).find((stmt) => {
            return stmt.getVariables().find((exp) => {
                return expression.getSource().trim() === exp.getSource().trim();
            });
        });
    }

    /**
    * Gets the statements where a given expression has been used.
    * Usage denotes that it is a right expression of a statement.
    * @param {Expression} expression expression to check usage
    * @param {TransformNode} transformNode transform node searched
    * @returns {[AssignmentStatement]} assignment statements with usage
    * @memberof TransformNodeMapper
    */
    static getInputStatements(transformNode, expression) {
        return transformNode.body.getStatements().filter((stmt) => {
            const matchingInput = this.getVerticesFromExpression(stmt.getExpression()).filter((exp) => {
                return (exp.getSource().trim() === expression.getSource().trim());
            });
            return (matchingInput.length > 0);
        });
    }

    /**
     * Get vertices that are used in expression
     * @param {Expression} expression expression to get vertices from
     * @returns {[Expression]} array of expressions that are inputs
     * @memberof TransformNodeMapper
     */
    static getVerticesFromExpression(expression) {
        if (TreeUtil.isSimpleVariableRef(expression)) {
            return [expression];
        }
        if (TreeUtil.isFieldBasedAccessExpr(expression)) {
            return [expression];
        }
        if (TreeUtil.isTypeCastExpr(expression) || TreeUtil.isTypeConversionExpr(expression)) {
            return [...this.getVerticesFromExpression(expression.getExpression())];
        }
        if (TreeUtil.isInvocation(expression)) {
            return _.flatten(expression.getArgumentExpressions().map((exp) => {
                return [...this.getVerticesFromExpression(exp)];
            }));
        }
        if (TreeUtil.isBinaryExpr(expression)) {
            return [...this.getVerticesFromExpression(expression.getLeftExpression()),
                ...this.getVerticesFromExpression(expression.getRightExpression())];
        }
        if (TreeUtil.isUnaryExpr(expression)) {
            return this.getVerticesFromExpression(expression.getExpression());
        }
        if (TreeUtil.isLiteral(expression)) {
            return [];
        }
        if (TreeUtil.isReferenceTypeInitExpression(expression)) {
            log.error('not implemented reference type init expression');
        }
        if (TreeUtil.isTernaryExpr(expression)) {
            log.error('not implemented reference type init expression');
        }
        return [];
    }
}
