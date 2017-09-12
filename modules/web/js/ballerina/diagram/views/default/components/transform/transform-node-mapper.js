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
import log from 'log';
import ASTFactory from '../../../../../ast/ast-factory';
import DefaultASTFactory from '../../../../../ast/default-ast-factory';

const ExpressionType = {
    ERROR: 'error',
    PLACEHOLDER: 'placeholder',
    TEMPVAR: 'tempvar',
    DIRECT: 'direct',
    CONST: 'const',
};

const VarPrefix = {
    TEMP: '__temp',
    OUTPUT: '__output',
};

/**
 * Performs utility methods for handling transform inner statement creation and removal
 * @class TransformNodeMapper
 */
class TransformNodeMapper {

    constructor(args) {
        this._transformStmt = _.get(args, 'transformStmt');
    }

    setTransformStmt(transformStmt) {
        this._transformStmt = transformStmt;
    }

    // **** CREATE MAPPING FUNCTIONS **** //

    /**
     * Create a direct source variable to direct target variable mapping
     * @param {Expression} sourceExpression source expression
     * @param {Expression} targetExpression target expression
     * @param {any} compatibility compatibility of the mapping
     * @memberof TransformNodeMapper
     */
    createInputToOutputMapping(sourceExpression, targetExpression, compatibility) {
        this.validateTargetMappings(targetExpression);

        const assignmentStmt = ASTFactory.createAssignmentStatement();
        const varRefList = ASTFactory.createVariableReferenceList();
        varRefList.addChild(targetExpression);
        assignmentStmt.addChild(varRefList, 0);

        assignmentStmt.addChild(sourceExpression, 1);

        if (!compatibility.safe) {
            const errorVarRef = DefaultASTFactory.createIgnoreErrorVariableReference();
            varRefList.addChild(errorVarRef);
        }
        this._transformStmt.addChild(assignmentStmt);
    }

    /**
     * Create direct input variable to a function node mapping.
     * @param {Expression} sourceExpression source expression
     * @param {any} target target function node definition
     * @param {any} compatibility compatibility of the mapping
     * @memberof TransformNodeMapper
     */
    createInputToFunctionMapping(sourceExpression, target, compatibility) {
        const funcNode = target.funcInv;
        const index = target.index;

        let refType = ASTFactory.createReferenceTypeInitExpression();
        if (target.isField &&
            ASTFactory.isReferenceTypeInitExpression(funcNode.children[index])) {
            refType = funcNode.children[index];
        }
        funcNode.removeChild(funcNode.children[index], true);
        // check function parameter is a struct and mapping is a complex mapping
        if (target.isField && _.find(this.state.vertices, (struct) => {
            return struct.typeName === this.getFunctionVertices(funcNode).parameters[index].type;
        })) {
            const keyValEx = ASTFactory.createKeyValueExpression();
            const nameVarRefExpression = ASTFactory.createSimpleVariableReferenceExpression();

            const propChain = (target.name).split('.').splice(1);

            nameVarRefExpression.setExpressionFromString(propChain[0]);
            keyValEx.addChild(nameVarRefExpression);
            keyValEx.addChild(sourceExpression);
            refType.addChild(keyValEx);
            funcNode.addChild(refType, index);
        } else {
            if (compatibility.safe) {
                funcNode.addChild(sourceExpression, index);
                return;
            }

            const assignmentStmt = this.findEnclosingAssignmentStatement(funcNode);
            const argumentExpression = this.getTempVertexExpression(sourceExpression,
                this._transformStmt.getIndexOfChild(assignmentStmt));

            funcNode.addChild(argumentExpression, index);
        }
    }

    /**
     * Create function node to direct output variable mapping.
     * @param {Expression} targetExpression target expression
     * @param {any} source source function node definition
     * @param {any} target target variable definition
     * @param {any} compatibility compatibility of the mapping
     * @memberof TransformNodeMapper
     */
    createFunctionToOutputMapping(targetExpression, source, target, compatibility) {
        this.validateTargetMappings(targetExpression);

        const assignmentStmt = this.findEnclosingAssignmentStatement(source.funcInv);
        if (!assignmentStmt) {
            log.error('Cannot find assignment statement containing the function invocation '
                        + source.funcInv.getFunctionName());
            return;
        }
        const rightExpression = this.getCompatibleSourceExpression(
            this.getMappableExpression(source.funcInv), compatibility.type, target.type);

        const outputExpressions = this.getOutputExpressions(assignmentStmt);

        if (outputExpressions[source.index].type === ExpressionType.TEMPVAR) {
            const tempVarRefExpr = outputExpressions[source.index].expression;
            const newAssignmentStmt = ASTFactory.createAssignmentStatement();
            const varRefList = ASTFactory.createVariableReferenceList();
            varRefList.addChild(targetExpression, true);
            newAssignmentStmt.addChild(varRefList, 0, true);
            newAssignmentStmt.addChild(tempVarRefExpr, 1, true);
            this._transformStmt.addChild(newAssignmentStmt, true);
        } else if (outputExpressions[source.index].type === ExpressionType.PLACEHOLDER) {
            assignmentStmt.replaceChildByIndex(rightExpression, 1, true);

            assignmentStmt.getLeftExpression().replaceChildByIndex(targetExpression, source.index, true);
            // TODO: conditionally remove var if all outputs are mapped
            assignmentStmt.setIsDeclaredWithVar(false, true);

            if (!compatibility.safe) {
                // TODO: only add error reference if there is not one already
                // TODO: also remove error variable if no longer required
                const errorVarRef = DefaultASTFactory.createIgnoreErrorVariableReference();
                assignmentStmt.getLeftExpression().addChild(errorVarRef, true);
            }
        } else if (outputExpressions[source.index].type === ExpressionType.DIRECT) {
            const tempVarName = this.assignToTempVariable(assignmentStmt);
            const tempVarRefExpr = ASTFactory.createSimpleVariableReferenceExpression({ variableName: tempVarName });
            assignmentStmt.replaceChildByIndex(tempVarRefExpr, 1, true);

            const newAssignmentStmt = ASTFactory.createAssignmentStatement();
            const varRefList = ASTFactory.createVariableReferenceList();
            varRefList.addChild(targetExpression, source.index, true);
            newAssignmentStmt.addChild(varRefList, 0, true);
            newAssignmentStmt.addChild(tempVarRefExpr, 1, true);
            this._transformStmt.addChild(newAssignmentStmt, true);
        }

        this._transformStmt.trigger('tree-modified', {
            origin: this._transformStmt,
            type: 'function-connection-created',
            title: `Create mapping ${target.name}`,
            data: {},
        });
    }

    // **** REMOVE MAPPING FUNCTIONS **** //


    // **** UTILITY FUNCTIONS ***** //

    /**
     * Get assignment statement containing the given right expression
     * @param {any} expression right expression
     * @memberof TransformNodeMapper
     */
    findEnclosingAssignmentStatement(expression) {
        return this._transformStmt.getChildren().find((stmt) => {
            return (this.getMappableExpression(stmt.getRightExpression()).getExpressionString().trim()
                        === expression.getExpressionString().trim());
        });
    }

    /**
     * The expression that can be used for mapping. E.g. : type cast expressions cannot be mapped
     * directly, but when the cast expression is removed they can be mapped. Similarly, when
     * temporary variables are used, their real mapping expression is the underlying expression
     * that is wrapped by the temporary variable
     * @param {Expression} expression
     * @param {AssignmentStatement} assignmentStmt assignment statement
     * @return {Expression} mapping expression
     * @memberof TransformNodeMapper
     */
    getMappableExpression(expression) {
        if (ASTFactory.isTypeCastExpression(expression) || ASTFactory.isTypeConversionExpression(expression)) {
            return expression.getRightExpression();
        }
        return expression;
    }

    /**
     * Check other target mappings to create a valid statement
     * @param {Expression} targetExpression target expression
     * @memberof TransformNodeMapper
     */
    validateTargetMappings(targetExpression) {
        const targetStmt = this.getOutputStatement(targetExpression);
        if (targetStmt) {
            if (this.isComplexStatement(targetStmt)) {
                this.removeOutputMapping(targetStmt, targetExpression);
            } else {
                this._transformStmt.removeChild(targetStmt, true);
            }
        }
    }

    /**
     * Get expressions that are mapped by the assignment statement.
     * The output mappings ignore error expressions and placeholder temporary variables.
     * @param {AssignmentStatement} assignmentStmt assignment statement
     * @returns {[Expression]} expressions that are output mappings
     * @memberof TransformNodeMapper
     */
    getOutputExpressions(assignmentStmt) {
        const outputMappings = [];
        assignmentStmt.getLeftExpression().getChildren().forEach((exp, index) => {
            outputMappings[index] = { expression: exp };
            if (this.isInTransformInputOutput(exp)) {
                outputMappings[index].type = ExpressionType.DIRECT;
            } else if (this.isErrorExpression(exp)) {
                outputMappings[index].type = ExpressionType.ERROR;
            } else if (this.isPlaceHolderTempVariable(exp)) {
                outputMappings[index].type = ExpressionType.PLACEHOLDER;
            } else if (this.isConstant(exp, assignmentStmt)) {
                outputMappings[index].type = ExpressionType.CONST;
            } else {
                outputMappings[index].type = ExpressionType.TEMPVAR;
            }
        });
        return outputMappings;
    }

    getTempResolvedExpression(expression) {
        const assignmentStmt = this.getOutputStatement(expression);
        return assignmentStmt.getRightExpression();
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
    isTempVariable(expression, assignmentStmt) {
        if (this.isInTransformInputOutput(expression)
            || this.isErrorExpression(expression)
            || this.isPlaceHolderTempVariable(expression)
            || this.isConstant(expression, assignmentStmt)) {
            return false;
        }
        return true;
    }

    /**
     * Check whether the given expression is a placeholder temporary variable in the transform
     * statement scope.
     * E.g. : var __output1 = func(null);
     * Here the __output1 is not declared earlier, but is a temporary variable, which
     * cannot be shown as a mapping.
     * @param {Expression} expression expression to check
     * @returns {boolean} whether a placeholder temporary variable or not
     * @memberof TransformNodeMapper
     */
    isPlaceHolderTempVariable(expression) {
        if (this.isInTransformInputOutput(expression)) {
            return false;
        }
        const usedStatements = this.getInputStatements(expression);
        return (usedStatements.length === 0);
    }

    /**
     * Check whether the given expression is an input or output expression
     * of transform statement
     * @param {Expression} expression expression to check
     * @returns {boolean} whether or not the expression is an input or an output
     * @memberof TransformNodeMapper
     */
    isInTransformInputOutput(expression) {
        const inputOutput = [...this._transformStmt.getInput(), ...this._transformStmt.getOutput()];
        const ioReference = inputOutput.find((io) => {
            return io.getExpressionString().trim() === expression.getExpressionString().trim();
        });
        return (ioReference !== undefined);
    }

    /**
    * Checks whether the given expression is an error expression
    * @param {Expression} expression expression
    * @returns {boolean} is or not an error expression
    * @memberof TransformNodeMapper
    */
    isErrorExpression(expression) {
        // TODO: enhance for user defined errors
        if (expression.getExpressionString().trim() === '_') {
            return true;
        }
        return false;
    }

    /**
     * whether the constant expression is a constant value
     * @param {any} expression expression
     * @param {any} assignmentStmt assignment statement
     * @returns whether the expression is a constant
     * @memberof TransformNodeMapper
     */
    isConstant(expression, assignmentStmt) {
        if (!assignmentStmt) {
            assignmentStmt = this.getOutputStatement(expression);
        }
        return ASTFactory.isBasicLiteralExpression(assignmentStmt.getRightExpression());
    }

    /**
     * Get vertices that are used in expression
     * @param {Expression} expression expression to get vertices from
     * @returns {[Expression]} array of expressions that are inputs
     * @memberof TransformNodeMapper
     */
    getVerticesFromExpression(expression) {
        if (ASTFactory.isSimpleVariableReferenceExpression(expression)) {
            return [expression];
        }
        if (ASTFactory.isFieldBasedVarRefExpression(expression)) {
            return [expression];
        }
        if (ASTFactory.isReferenceTypeInitExpression(expression)) {
            log.error('not implemented reference type init expression');
        }
        if (ASTFactory.isTypeCastExpression(expression) || ASTFactory.isTypeConversionExpression(expression)) {
            return [...this.getVerticesFromExpression(expression.getRightExpression())];
        }
        if (ASTFactory.isFunctionInvocationExpression(expression)) {
            return _.flatten(expression.getChildren().map((exp) => {
                return [...this.getVerticesFromExpression(exp)];
            }));
        }
        log.error('unknown expression type to get vertices');
        return [];
    }

    /**
     * Remove output mapping from the assignment statement
     * @param {AssignmentStatement} assignmentStmt assignment statement
     * @param {Expression} leftExpression left expression to be removed
     * @memberof TransformNodeMapper
     */
    removeOutputMapping(assignmentStmt, leftExpression) {
        const leftExp = assignmentStmt.getLeftExpression().getChildren().find((exp) => {
            return exp.getExpressionString().trim() === leftExpression.getExpressionString();
        });
        const outputVar = ASTFactory.createSimpleVariableReferenceExpression();
        outputVar.setExpressionFromString(this.getNewTempVarName(VarPrefix.OUTPUT));
        assignmentStmt.getLeftExpression().replaceChild(leftExp, outputVar, true);
    }


    /**
     * Get statement that have given expression as output.
     * @param {Expression} expression output expression
     * @return {AssignmentStatement} assignment statement with expression as an output
     * @memberof TransformNodeMapper
     */
    getOutputStatement(expression) {
        return this._transformStmt.getChildren().find((stmt) => {
            return stmt.getLeftExpression().getChildren().find((exp) => {
                return expression.getExpressionString().trim() === exp.getExpressionString().trim();
            });
        });
    }

    /**
    * Gets the statements where a given expression has been used.
    * Usage denotes that it is a right expression of a statement.
    * @param {Expression} expression expression to check usage
    * @returns {[AssignmentStatement]} assignment statements with usage
    * @memberof TransformNodeMapper
    */
    getInputStatements(expression) {
        return this._transformStmt.getChildren().filter((stmt) => {
            const matchingInput = this.getVerticesFromExpression(stmt.getRightExpression()).filter((exp) => {
                return (exp.getExpressionString().trim() === expression.getExpressionString().trim());
            });
            return (matchingInput.length > 0);
        });
    }

    /**
     * Assign the given assignment statement value to a temporary variable
     * @param {any} assignmentStmt the assignment statement to be extracted to a temp variable
     * @returns the temp variable name
     * @memberof TransformNodeMapper
     */
    assignToTempVariable(assignmentStmt) {
        const tempVarName = this.getNewTempVarName();
        const tempVarRefExpr = ASTFactory.createSimpleVariableReferenceExpression();
        tempVarRefExpr.setExpressionFromString(tempVarName);

        const varRefList = ASTFactory.createVariableReferenceList();
        varRefList.addChild(tempVarRefExpr, true);
        let index = this._transformStmt.getIndexOfChild(assignmentStmt);
        index = (index === 0) ? 0 : index - 1;
        this.insertAssignmentStatement(varRefList, assignmentStmt.getRightExpression(), index, true);
        return tempVarName;
    }

    /**
     * Insert an assignment statement with given expressions at the index
     * @param {any} leftExpressions left expression
     * @param {any} rightExpression right expression
     * @param {any} index index to insert
     * @param {boolean} [isVar=true] whether assignment statement must be declared with var
     * @memberof TransformNodeMapper
     */
    insertAssignmentStatement(leftExpressions, rightExpression, index, isVar = true) {
        const assignmentStmt = ASTFactory.createAssignmentStatement();

        assignmentStmt.addChild(leftExpressions, 0);
        assignmentStmt.addChild(rightExpression, 1);
        assignmentStmt.setIsDeclaredWithVar(isVar);
        // TODO: handle type cast and coversion
    //     const errorVarRef = DefaultBallerinaASTFactory.createIgnoreErrorVariableReference();
    //     varRefList.addChild(errorVarRef);
        this._transformStmt.addChild(assignmentStmt, index, true);
    }

    /**
     * Get a new name for temporary variable
     * @param {string} varPrefix prefix for the variable
     * @returns {Expression} temporary var expression
     * @memberof TransformNodeMapper
     */
    getNewTempVarName(varPrefix = VarPrefix.TEMP) {
        const varNameRegex = new RegExp(varPrefix + '[\\d]*');
        const tempVarNames = [];
        this._transformStmt.getChildren().forEach((stmt) => {
            this.getOutputExpressions(stmt).forEach((mapping) => {
                const expStr = mapping.expression.getExpressionString();
                if ((mapping.type === ExpressionType.TEMPVAR) && varNameRegex.test(expStr)) {
                    tempVarNames.push(expStr);
                }
            });
        });

        tempVarNames.sort();
        let index = 1;
        if (tempVarNames.length > 0) {
            index = Number.parseInt(tempVarNames[tempVarNames.length - 1].substring(varPrefix.length), 10) + 1;
        }
        return varPrefix + index;
    }

    /**
     * Create the compatible source expression based on the compatibility
     * @param {Expression} sourceExpression source expression
     * @param {('implicit'|'explicit'|'conversion')} compatibilityType compatibility type
     * @param {string} targetType target type
     * @returns {Expression} compatible source expression
     * @memberof TransformNodeMapper
     */
    getCompatibleSourceExpression(sourceExpression, compatibilityType, targetType) {
        switch (compatibilityType) {
            case 'explicit' : {
                const typeCastExp = ASTFactory.createTypeCastExpression();
                typeCastExp.addChild(sourceExpression);
                typeCastExp.setTargetType(targetType);
                return typeCastExp;
            }
            case 'conversion' : {
                const typeConversionExp = ASTFactory.createTypeConversionExpression();
                typeConversionExp.addChild(sourceExpression);
                typeConversionExp.setTargetType(targetType);
                return typeConversionExp;
            }
            case 'implicit' :
            default :
                return sourceExpression;
        }
    }

    /**
    * Is the assignment statement a complex one or not. A complex statement will have
    * function invocation or an operator as the right expression.
    * @param {AssignmentStatement} assignmentStmt the assignment statement
    * @return {boolean} is direct or not
    * @memberof TransformNodeMapper
    */
    isComplexStatement(assignmentStmt) {
        const rightExp = this.getMappableExpression(assignmentStmt.getRightExpression());
        if (ASTFactory.isFunctionInvocationExpression(rightExp)) {
            return true;
        }
        if (ASTFactory.isBinaryExpression(rightExp)) {
            return true;
        }
        return false;
    }
}

export default TransformNodeMapper;
