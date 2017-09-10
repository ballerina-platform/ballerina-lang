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
            this.getMappingExpression(source.funcInv), compatibility.type, target.type);
        // remove existing right expression and set the new compatible right expression
        assignmentStmt.removeChild(source.funcInv, true);
        assignmentStmt.addChild(rightExpression, 1, true);

        const lexpr = assignmentStmt.getLeftExpression();
        lexpr.removeChild(lexpr.getChildren()[source.index], true);
        lexpr.addChild(targetExpression, source.index);
        // TODO: conditionally remove var if all outputs are mapped
        assignmentStmt.setIsDeclaredWithVar(false);

        if (!compatibility.safe) {
            // TODO: only add error reference if there is not one already
            // TODO: also remove error variable if no longer required
            const errorVarRef = DefaultASTFactory.createIgnoreErrorVariableReference();
            lexpr.addChild(errorVarRef);
        }
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
            return (this.getMappingExpression(stmt.getRightExpression()).getExpressionString()
                        === expression.getExpressionString());
        });
    }

    /**
     * The expression that can be used for mapping. E.g. : type cast expressions cannot be mapped
     * directly, but when the cast expression is removed they can be mapped. Similarly, when
     * temporary variables are used, their real mapping expression is the underlying expression
     * that is wrapped by the temporary variable
     * @param {Expression} expression
     * @return {Expression} mapping expression
     * @memberof TransformNodeMapper
     */
    getMappingExpression(expression) {
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
        const targetStmt = this.getTargetStatements(targetExpression);
        if (targetStmt) {
            if (this.isComplexStatement(targetStmt)) {
                this.removeLeftExpression(targetStmt, targetExpression);
            } else {
                this._transformStmt.removeChild(targetStmt, true);
            }
        }
    }

    /**
     * Remove mapping left expression from the assignment statement
     * @param {any} assignmentStmt assignment statement
     * @param {any} leftExpression left expression to be removed
     * @memberof TransformNodeMapper
     */
    removeLeftExpression(assignmentStmt, leftExpression) {
        const leftExp = assignmentStmt.getLeftExpression().getChildren().find((exp) => {
            return exp.getExpressionString() === leftExpression.getExpressionString();
        });
        assignmentStmt.getLeftExpression().removeChild(leftExp, true);
    }


    /**
     * Get statements that have given expression as target.
     * @param {Expression} targetExpression target expression
     * @return {AssignmentStatement} assignment statement with target as expression
     * @memberof TransformNodeMapper
     */
    getTargetStatements(targetExpression) {
        return this._transformStmt.filterChildren(ASTFactory.isAssignmentStatement).find((stmt) => {
            const leftExp = stmt.getLeftExpression().getChildren().find((exp) => {
                return targetExpression.getExpressionString() === exp.getExpressionString().trim();
            });
            if (leftExp) {
                return true;
            }
            return false;
        });
    }

    /**
     * Is the assignment statement a complex one or not. A complex statement will have
     * function invocation or an operator as the right expression.
     * @param {AssignmentStatement} assignmentStmt the assignment statement
     * @return {boolean} is direct or not
     * @memberof TransformNodeMapper
     */
    isComplexStatement(assignmentStmt) {
        const rightExp = assignmentStmt.getRightExpression();
        if (ASTFactory.isFunctionInvocationExpression(rightExp)) {
            return true;
        }
        if (ASTFactory.isBinaryExpression(rightExp)) {
            return true;
        }
        return false;
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
}

export default TransformNodeMapper;
