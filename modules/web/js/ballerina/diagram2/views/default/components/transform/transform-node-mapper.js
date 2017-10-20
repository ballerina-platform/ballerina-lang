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
import TreeUtil from '../../../../../model/tree-util';
import NodeFactory from '../../../../../model/node-factory';
import TransformFactory from '../../../../../model/transform-factory';
import TransformUtils, { ExpressionType, VarPrefix } from '../../../../../utils/transform-utils';

/**
 * Performs utility methods for handling transform inner statement creation and removal
 * @class TransformNodeMapper
 */
class TransformNodeMapper {

    /**
     * Creates an instance of TransformNodeMapper.
     * @param {any} args arguments
     * @memberof TransformNodeMapper
     */
    constructor(args) {
        this._transformStmt = _.get(args, 'transformStmt');
    }

    /**
     * Set transform statement
     * @param {any} transformStmt transform statement
     * @memberof TransformNodeMapper
     */
    setTransformStmt(transformStmt) {
        this._transformStmt = transformStmt;
    }

    // **** CREATE MAPPING FUNCTIONS **** //

    /**
     * Create a direct source variable to direct target variable mapping
     * @param {Expression} sourceExpression source expression
     * @param {Expression} targetExpression target expression
     * @param {string} targetType target type
     * @param {any} compatibility compatibility of the mapping
     * @memberof TransformNodeMapper
     */
    createInputToOutputMapping(sourceExpression, targetExpression, targetType, compatibility) {
        this.validateTargetMappings(targetExpression);

        const assignmentStmt = NodeFactory.createAssignment();
        assignmentStmt.addVariables(targetExpression, true);
        assignmentStmt.setExpression(this.getCompatibleSourceExpression((sourceExpression),
                                            compatibility.type, targetType), true);

        if (!compatibility.safe) {
            const errorVarRef = TransformFactory.createVariableRefExpression('_');
            assignmentStmt.addVariables(errorVarRef, true);
        }
        this._transformStmt.body.addStatements(assignmentStmt, -1, true);
        this._transformStmt.trigger('tree-modified', {
            origin: this._transformStmt,
            type: 'transform-connection-created',
            title: `Create mapping ${sourceExpression.getSource()} to ${targetExpression.getSource()}`,
            data: {},
        });
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

        if (compatibility.safe) {
            funcNode.replaceArgumentExpressionsByIndex(index, sourceExpression, true);
        } else {
            sourceExpression = this.getCompatibleSourceExpression(sourceExpression, compatibility.type, target.type);
            let tempVarName = this.getAssignedTempVarName(sourceExpression);
            if (!tempVarName) {
                // if no assigned temp var, assign the expression to a new temp var
                const stmt = this.getParentStatement(funcNode);
                const stmtIndex = this._transformStmt.body.getIndexOfStatements(stmt);
                tempVarName = this.assignExpressionToTempVariable(sourceExpression, stmtIndex, false);
            }
            sourceExpression = TransformFactory.createVariableRefExpression(tempVarName);
            funcNode.replaceArgumentExpressionsByIndex(index, sourceExpression, true);
        }
        this._transformStmt.trigger('tree-modified', {
            origin: this._transformStmt,
            type: 'transform-connection-created',
            title: `Create mapping ${sourceExpression.getSource()} to ${funcNode.getFunctionName()}`,
            data: {},
        });
    }

    /**
     * Create direct input to operator mapping
     * @param {any} sourceExpression source expression
     * @param {any} target target
     * @param {any} compatibility compatibility
     * @memberof TransformNodeMapper
     */
    createInputToOperatorMapping(sourceExpression, target, compatibility) {
        if (TreeUtil.isTernaryExpr(target.operator)) {
            this.createInputToTernaryOperatorMapping(sourceExpression, target, compatibility);
        } else if (TreeUtil.isBinaryExpr(target.operator)) {
            this.createInputToBinaryOperatorMapping(sourceExpression, target, compatibility);
        } else if (TreeUtil.isUnaryExpr(target.operator)) {
            this.createInputToUnaryOperatorMapping(sourceExpression, target, compatibility);
        }
    }

    /**
     * Create direct input variable to a ternary operator node mapping.
     * @param {Expression} sourceExpression source expression
     * @param {any} target target operator node definition
     * @param {any} compatibility compatibility of the mapping
     * @memberof TransformNodeMapper
     */
    createInputToTernaryOperatorMapping(sourceExpression, target, compatibility) {
        const operatorNode = target.operator;
        const index = target.index;

        if (compatibility.safe) {
            if (index === 0) {
                operatorNode.setCondition(sourceExpression, true);
            } else if (index === 1) {
                operatorNode.setThenExpression(sourceExpression, true);
            } else {
                operatorNode.setElseExpression(sourceExpression, true);
            }
        } else {
            const stmt = this.getParentStatement(operatorNode);
            const argumentExpression = this.getTempVertexExpression(sourceExpression,
                                            this._transformStmt.body.getIndexOfStatements(stmt));
            operatorNode.addChild(argumentExpression, index, true);
        }
        this._transformStmt.trigger('tree-modified', {
            origin: this._transformStmt,
            type: 'transform-connection-created',
            title: `Create mapping ${sourceExpression.getSource()} to operator ${operatorNode.getOperatorKind()}`,
            data: {},
        });
    }

    /**
     * Create direct input variable to a binary operator node mapping.
     * @param {Expression} sourceExpression source expression
     * @param {any} target target operator node definition
     * @param {any} compatibility compatibility of the mapping
     * @memberof TransformNodeMapper
     */
    createInputToBinaryOperatorMapping(sourceExpression, target, compatibility) {
        const operatorNode = target.operator;
        const index = target.index;

        if (compatibility.safe) {
            if (index === 0) {
                operatorNode.setLeftExpression(sourceExpression, true);
            } else {
                operatorNode.setRightExpression(sourceExpression, true);
            }
        } else {
            const stmt = this.getParentStatement(operatorNode);
            const argumentExpression = this.getTempVertexExpression(sourceExpression,
                                            this._transformStmt.body.getIndexOfStatements(stmt));
            operatorNode.addChild(argumentExpression, index, true);
        }
        this._transformStmt.trigger('tree-modified', {
            origin: this._transformStmt,
            type: 'transform-connection-created',
            title: `Create mapping ${sourceExpression.getSource()} to operator ${operatorNode.getOperatorKind()}`,
            data: {},
        });
    }

    /**
     * Create direct input variable to a unary operator node mapping.
     * @param {Expression} sourceExpression source expression
     * @param {any} target target operator node definition
     * @param {any} compatibility compatibility of the mapping
     * @memberof TransformNodeMapper
     */
    createInputToUnaryOperatorMapping(sourceExpression, target, compatibility) {
        const operatorNode = target.operator;
        const index = target.index;

        if (compatibility.safe) {
            operatorNode.setExpression(sourceExpression, true);
        } else {
            const stmt = this.getParentStatement(operatorNode);
            const argumentExpression = this.getTempVertexExpression(sourceExpression,
                                            this._transformStmt.body.getIndexOfStatements(stmt));
            operatorNode.addChild(argumentExpression, index);
        }
        this._transformStmt.trigger('tree-modified', {
            origin: this._transformStmt,
            type: 'transform-connection-created',
            title: `Create mapping ${sourceExpression.getSource()} to operator ${operatorNode.getOperatorKind()}`,
            data: {},
        });
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

        const stmt = this.getParentStatement(source.funcInv);
        if (!stmt) {
            log.error('Cannot find statement containing the function invocation '
                        + source.funcInv.getFunctionName());
            return;
        }
        const rightExpression = this.getCompatibleSourceExpression(
            this.getMappableExpression(source.funcInv), compatibility.type, target.type);

        let compatibleExp = true;
        const stmtExpr = this.getExpression(stmt);

        if (rightExpression.getSource().trim() !== stmtExpr.getSource().trim()) {
            // If the current expression is not compatible to the new mapping.
            // e.g. : the current mapping is b = <boolean> func(5) but the new mapping is s = <string> func(5)
            // We cannot use the regular flow here. We need to set func(5) to a temporary variable and reuse.
            compatibleExp = false;
        }

        const outputExpressions = this.getOutputExpressions(stmt);

        if (outputExpressions[source.index].type === ExpressionType.TEMPVAR) {
            const newAssignmentStmt = NodeFactory.createAssignment();
            newAssignmentStmt.addVariables(targetExpression, true);

            // creating a new var ref expr from temp var since it can be an identifier node if the temp is assigned
            // with a var def statement
            newAssignmentStmt.setExpression(
                TransformFactory.createVariableRefExpression(
                    outputExpressions[source.index].expression.getSource().trim()), true);

            this._transformStmt.body.addStatements(newAssignmentStmt, -1, true);
        } else if (outputExpressions[source.index].type === ExpressionType.PLACEHOLDER) {
            if (TreeUtil.isAssignment(stmt)) {
                stmt.setExpression(rightExpression, true);
                stmt.replaceVariablesByIndex(source.index, targetExpression, true);
                // TODO: conditionally remove var if all outputs are mapped
                stmt.setDeclaredWithVar(false, true);

                if (!compatibility.safe) {
                    // TODO: only add error reference if there is not one already
                    // TODO: also remove error variable if no longer required
                    const errorVarRef = TransformFactory.createVariableRefExpression('_');
                    stmt.addVariables(errorVarRef, true);
                }
            } else if (TreeUtil.isVariableDef(stmt)) {
                const assignmentStmt = NodeFactory.createAssignment();
                assignmentStmt.addVariables(targetExpression, true);
                assignmentStmt.setExpression(TransformFactory.createVariableRefExpression(
                    outputExpressions[source.index].expression.getSource().trim()));

                if (!compatibility.safe) {
                    const errorVarRef = TransformFactory.createVariableRefExpression('_');
                    assignmentStmt.addVariables(errorVarRef, true);
                }
                this._transformStmt.body.addStatements(assignmentStmt, -1, true);
            }
        } else if (outputExpressions[source.index].type === ExpressionType.DIRECT) {
            if (!compatibleExp) {
                const mappingExp = this.getMappableExpression(stmtExpr);
                const tempVarName = this.assignExpressionToTempVariable(mappingExp,
                    this._transformStmt.body.getIndexOfStatements(stmt));
                const tempVarRefExpr = TransformFactory.createVariableRefExpression(tempVarName);
                stmtExpr.setExpression(tempVarRefExpr, true);

                const newAssignmentStmt = NodeFactory.createAssignment();
                rightExpression.setExpression(tempVarRefExpr);
                newAssignmentStmt.setExpression(rightExpression);
                newAssignmentStmt.replaceVariablesByIndex(source.index, targetExpression, true);
                this._transformStmt.body.addStatements(newAssignmentStmt, -1, true);
            } else {
                const tempVarName = this.assignStatementToTempVariable(stmt);
                const tempVarRefExpr = TransformFactory.createVariableRefExpression(tempVarName);
                stmt.setExpression(tempVarRefExpr, true);

                const newAssignmentStmt = NodeFactory.createAssignment();
                newAssignmentStmt.addVariables(targetExpression, source.index, true);
                newAssignmentStmt.setExpression(tempVarRefExpr, true);
                this._transformStmt.body.addStatements(newAssignmentStmt, -1, true);
            }
        }

        this._transformStmt.trigger('tree-modified', {
            origin: this._transformStmt,
            type: 'transform-connection-created',
            title: `Create mapping ${source.name} to ${target.name}`,
            data: {},
        });
    }

    /**
     * Create operator node to direct output variable mapping.
     * @param {Expression} targetExpression target expression
     * @param {any} source source function node definition
     * @param {any} target target variable definition
     * @param {any} compatibility compatibility of the mapping
     * @memberof TransformNodeMapper
     */
    createOperatorToOutputMapping(targetExpression, source, target, compatibility) {
        this.validateTargetMappings(targetExpression);

        const stmt = this.getParentStatement(source.operator);
        if (!stmt) {
            log.error('Cannot find statement containing the operator '
                        + source.operator.getOperatorKind());
            return;
        }
        const expression = this.getCompatibleSourceExpression(
            this.getMappableExpression(source.operator), compatibility.type, target.type);

        const outputExpressions = this.getOutputExpressions(stmt);

        if (outputExpressions[0].type === ExpressionType.TEMPVAR) {
            const tempVarRefExpr = outputExpressions[source.index].expression;
            const newAssignmentStmt = NodeFactory.createAssignment();
            newAssignmentStmt.addVariables(targetExpression, true);

            // creating a new var ref expr from temp var since it can be an identifier node if the temp is assigned
            // with a var def statement
            newAssignmentStmt.setExpression(
                TransformFactory.createVariableRefExpression(tempVarRefExpr.getSource().trim()), true);

            this._transformStmt.body.addStatements(newAssignmentStmt, -1, true);
        } else if (outputExpressions[0].type === ExpressionType.PLACEHOLDER) {
            if (TreeUtil.isAssignment(stmt)) {
                stmt.setExpression(expression, true);

                stmt.replaceVariablesByIndex(0, targetExpression, true);
                // TODO: conditionally remove var if all outputs are mapped
                stmt.setDeclaredWithVar(false, true);

                if (!compatibility.safe) {
                    // TODO: only add error reference if there is not one already
                    // TODO: also remove error variable if no longer required
                    const errorVarRef = TransformFactory.createVariableRefExpression('_');
                    stmt.addVariables(errorVarRef, true);
                }
            } else if (TreeUtil.isVariableDef(stmt)) {
                const assignmentStmt = NodeFactory.createAssignment();
                assignmentStmt.addVariables(targetExpression, true);
                assignmentStmt.setExpression(TransformFactory.createVariableRefExpression(
                    outputExpressions[source.index].expression.getSource().trim()));

                if (!compatibility.safe) {
                    const errorVarRef = TransformFactory.createVariableRefExpression('_');
                    assignmentStmt.addVariables(errorVarRef, true);
                }
                this._transformStmt.body.addStatements(assignmentStmt, -1, true);
            }
        } else if (outputExpressions[0].type === ExpressionType.DIRECT) {
            const tempVarName = this.assignStatementToTempVariable(stmt);
            const tempVarRefExpr = TransformFactory.createVariableRefExpression(tempVarName);
            stmt.setExpression(tempVarRefExpr, true);

            const newAssignmentStmt = NodeFactory.createAssignment({});
            newAssignmentStmt.addVariables(targetExpression, 0, true);
            newAssignmentStmt.setExpression(tempVarRefExpr, true);
            this._transformStmt.body.addStatements(newAssignmentStmt, -1, true);
        }

        this._transformStmt.trigger('tree-modified', {
            origin: this._transformStmt,
            type: 'transform-connection-created',
            title: `Create mapping operator ${source.operator.getOperatorKind()} to ${target.name}`,
            data: {},
        });
    }

    /**
     * Create complex node to node mappings. Complex nodes are functions and operators.
     * @param {any} source source
     * @param {any} target target
     * @memberof TransformNodeMapper
     */
    createNodeToNodeMapping(source, target) {
        if (source.funcInv && target.funcInv) {
            this.createFunctionToFunctionMapping(source, target);
        } else if (source.funcInv && target.operator) {
            this.createFunctionToOperatorMapping(source, target);
        } else if (source.operator && target.funcInv) {
            this.createOperatorToFunctionMapping(source, target);
        } else if (source.operator && target.operator) {
            this.createOperatorToOperatorMapping(source, target);
        }
    }

    /**
     * Create function to function mapping
     * @param {any} source source
     * @param {any} target target
     * @memberof TransformNodeMapper
     */
    createFunctionToFunctionMapping(source, target) {
        const stmt = this.getParentStatement(source.funcInv);
        // remove the source assignment statement since it is now included in the target assignment statement.
        this._transformStmt.body.removeStatements(stmt, true);

        target.funcInv.replaceArgumentExpressionsByIndex(target.index, source.funcInv, true);

        this._transformStmt.trigger('tree-modified', {
            origin: this._transformStmt,
            type: 'transform-connection-created',
            title: 'Create mapping operator ' + source.funcInv.getFunctionName() + ' to function ' +
                    target.funcInv.getFunctionName(),
            data: {},
        });
    }

    /**
    * Create operator to operator mapping
    * @param {any} source source
    * @param {any} target target
    * @memberof TransformNodeMapper
    */
    createOperatorToOperatorMapping(source, target) {
        const stmt = this.getParentStatement(source.operator);
        // remove the source assignment statement since it is now included in the target assignment statement.
        this._transformStmt.body.removeStatements(stmt, true);

        const setter = this.getOperandSetterFunction(target.operator, target.index);
        if (!setter) {
            log.error('Unknown operator type for mapping');
            return;
        }
        setter(source.operator, true);

        this._transformStmt.trigger('tree-modified', {
            origin: this._transformStmt,
            type: 'transform-connection-created',
            title: 'Create mapping operator ' + source.operator.getOperatorKind() + ' to function ' +
                    target.operator.getOperatorKind(),
            data: {},
        });
    }

    /**
     * Create operator to function mapping
     * @param {any} source source
     * @param {any} target target
     * @memberof TransformNodeMapper
     */
    createOperatorToFunctionMapping(source, target) {
        const assignmentStmtSource = this.getParentStatement(source.operator);
        // remove the source assignment statement since it is now included in the target assignment statement.
        this._transformStmt.body.removeStatements(assignmentStmtSource, true);

        target.funcInv.replaceArgumentExpressionsByIndex(target.index, source.operator, true);

        this._transformStmt.trigger('tree-modified', {
            origin: this._transformStmt,
            type: 'transform-connection-created',
            title: 'Create mapping operator ' + source.operator.getOperatorKind() + ' to function ' +
                    target.funcInv.getFunctionName(),
            data: {},
        });
    }

    /**
    * Create function to operator mapping
    * @param {any} source source
    * @param {any} target target
    * @memberof TransformNodeMapper
    */
    createFunctionToOperatorMapping(source, target) {
        const assignmentStmtSource = this.getParentStatement(source.funcInv);
        // remove the source assignment statement since it is now included in the target assignment statement.
        this._transformStmt.body.removeStatements(assignmentStmtSource, true);

        const setter = this.getOperandSetterFunction(target.operator, target.index);
        if (!setter) {
            log.error('Unknown operator type for mapping');
            return;
        }
        setter(source.funcInv, true);

        this._transformStmt.trigger('tree-modified', {
            origin: this._transformStmt,
            type: 'transform-connection-created',
            title: 'Create mapping function ' + source.funcInv.getFunctionName() + ' to operator ' +
                    target.operator.getOperatorKind(),
            data: {},
        });
    }

    // **** REMOVE MAPPING FUNCTIONS **** //

    /**
     * Remove node which is a function or an operator. If there are nested nodes,
     * they are moved to a new assignment statement.
     * @param {any} nodeExpression node expression
     * @param {any} parentNode parent node
     * @param {any} parentDef parent node definition
     * @param {any} statement enclosing statement
     * @memberof TransformNodeMapper
     */
    removeNode(nodeExpression, statement, parentNode, parentDef) {
        const nestedNodes = [];
        let nodeName;
        if (TreeUtil.isInvocation(nodeExpression)) {
            nodeName = nodeExpression.getFunctionName();
            nodeExpression.getArgumentExpressions().forEach((paramExp) => {
                if (this.isComplexExpression(paramExp)) {
                    nestedNodes.push(paramExp);
                }
            });
        } else {
            nodeName = nodeExpression.getOperatorKind();
            if (TreeUtil.isUnaryExpr(nodeExpression)
                    && this.isComplexExpression(nodeExpression.getExpression())) {
                nestedNodes.push(nodeExpression.getExpression());
            } else if (TreeUtil.isBinaryExpr(nodeExpression)
                        && this.isComplexExpression(nodeExpression.getRightExpression())) {
                nestedNodes.push(nodeExpression.getRightExpression());
            } else if (TreeUtil.isBinaryExpr(nodeExpression)
                        && this.isComplexExpression(nodeExpression.getLeftExpression())) {
                nestedNodes.push(nodeExpression.getLeftExpression());
            }
        }

        const statementIndex = this._transformStmt.body.getIndexOfStatements(statement);
        nestedNodes.forEach((node) => {
            const assignmentStmt = this.createNewAssignment(node);
            this._transformStmt.body.addStatements(assignmentStmt, statementIndex, true);
        });

        if (!parentNode) {
            this._transformStmt.body.removeStatements(statement, true);
        } else {
            let defaultExp = TransformFactory.createDefaultExpression();
            if (TreeUtil.isInvocation(parentNode)) {
                if (parentDef) {
                    const index = parentNode.getIndexOfArgumentExpressions(nodeExpression);
                    defaultExp = TransformFactory.createDefaultExpression(parentDef.parameters[index].type);
                }
                parentNode.replaceArgumentExpressions(nodeExpression, defaultExp, true);
            } else if (TreeUtil.isUnaryExpr(parentNode)
                        && (parentNode.getExpression() === nodeExpression)) {
                defaultExp = TransformFactory.createDefaultExpression('string');
                parentNode.setExpression(defaultExp, true);
            } else if (TreeUtil.isBinaryExpr(parentNode)
                        && (parentNode.getRightExpression() === nodeExpression)) {
                defaultExp = TransformFactory.createDefaultExpression('string');
                parentNode.setRightExpression(defaultExp, true);
            } else if (TreeUtil.isBinaryExpr(parentNode)
                        && (parentNode.getLeftExpression() === nodeExpression)) {
                defaultExp = TransformFactory.createDefaultExpression('string');
                parentNode.setLeftExpression(defaultExp, true);
            }
        }

        this._transformStmt.trigger('tree-modified', {
            origin: this._transformStmt,
            type: 'transform-connection-removed',
            title: `Remove mapping function ${nodeName}`,
            data: {},
        });
    }

    /**
     * Remove direct input to output mapping.
     * @param {any} sourceName source name
     * @param {any} targetName target name
     * @memberof TransformNodeMapper
     */
    removeInputToOutputMapping(sourceName, targetName) {
        const assignmentStmt = _.find(this.getMappingStatements(), (stmt) => {
            if (TreeUtil.isAssignment(stmt)) {
                return this.getVariables(stmt).find((varExp) => {
                    const varExpStr = varExp.getSource().trim();
                    const expStr = this.getMappableExpression(stmt.getExpression()).getSource().trim();
                    return (varExpStr === targetName) && (expStr === sourceName);
                });
            }
            return false;
        });
        this._transformStmt.body.removeStatements(assignmentStmt, true);
        this._transformStmt.trigger('tree-modified', {
            origin: this._transformStmt,
            type: 'transform-connection-removed',
            title: `Remove mapping ${sourceName} to ${targetName}`,
            data: {},
        });
    }

    /**
     * Remove function/operator node to output variable mapping.
     * @param {any} source source
     * @param {any} targetName target name
     * @memberof TransformNodeMapper
     */
    removeNodeToOutputMapping(source, targetName) {
        const nodeExpression = (source.funcInv) ? source.funcInv : source.operator;
        const nodeName = (source.funcInv) ? nodeExpression.getFunctionName() : nodeExpression.getOperatorKind();

        const stmtSource = this.getParentStatement(nodeExpression);
        const expression = this.getVariables(stmtSource)[source.index];

        if (expression.getSource().trim() === targetName) {
            this.removeOutputExpressions(stmtSource, targetName);
             // const errExpression = _.find(assignmentStmtSource.getLeftExpression().getChildren(), (child) => {
            //     return (child.getExpressionString().trim() === '_');
            // });
            // assignmentStmtSource.getLeftExpression().removeChild(errExpression, true);
        } else {
            // TODO: handle multiple return scenarios
            const tempVarExp = this.getVariables(stmtSource)[0];
            if (this.isTempVariable(tempVarExp, stmtSource)) {
                // get statements using the temp var as an input
                const statement = this.getInputStatements(tempVarExp).find((stmt) => {
                    return (this.getVariables(stmt)[0].getSource().trim() === targetName);
                });

                if (!statement) {
                    return;
                }

                this.removeOutputExpressions(statement, targetName);
            }
        }

        this._transformStmt.trigger('tree-modified', {
            origin: this._transformStmt,
            type: 'transform-connection-removed',
            title: `Remove mapping ${nodeName} to ${targetName}`,
            data: {},
        });
    }

    /**
     * Remove input variable to function mapping.
     * @param {any} source source
     * @param {any} target target
     * @memberof TransformNodeMapper
     */
    removeInputToFunctionMapping(source, target) {
        const functionInv = target.funcInv;

        const argExp = functionInv.getArgumentExpressions()[target.index];
        const stmt = this.getParentStatement(functionInv);

        this.removeTempVariable(argExp, stmt);

        functionInv.replaceArgumentExpressionsByIndex(target.index,
            TransformFactory.createDefaultExpression(target.type), true);

        this._transformStmt.trigger('tree-modified', {
            origin: this._transformStmt,
            type: 'transform-connection-removed',
            title: `Remove mapping ${source.name} to ${functionInv.getFunctionName()}`,
            data: {},
        });
    }

    /**
     * Remove input variable to operator mapping.
     * @param {any} source source
     * @param {any} target target
     * @memberof TransformNodeMapper
     */
    removeInputToOperatorMapping(source, target) {
        const operatorExp = target.operator;

        const defaultExp = TransformFactory.createDefaultOperatorExpression(operatorExp, target.index);

        const setter = this.getOperandSetterFunction(operatorExp, target.index);
        if (!setter) {
            log.error('Unknown operator type for mapping');
            return;
        }
        setter(defaultExp, true);

        this._transformStmt.trigger('tree-modified', {
            origin: this._transformStmt,
            type: 'transform-connection-removed',
            title: `Remove mapping ${source.name} to ${operatorExp.getOperatorKind()}`,
            data: {},
        });
    }

    /**
     * Remove complex node to node mappings. Complex nodes are functions and operators.
     * @param {any} source source
     * @param {any} target target
     * @memberof TransformNodeMapper
     */
    removeNodeToNodeMapping(source, target) {
        if (source.funcInv && target.funcInv) {
            this.removeFunctionToFunctionMapping(source, target);
        } else if (source.funcInv && target.operator) {
            this.removeFunctionToOperatorMapping(source, target);
        } else if (source.operator && target.funcInv) {
            this.removeOperatorToFunctionMapping(source, target);
        } else if (source.operator && target.operator) {
            this.removeOperatorToOperatorMapping(source, target);
        }
    }

    /**
     * Remove function to function mapping.
     * @param {any} source source
     * @param {any} target target
     * @memberof TransformNodeMapper
     */
    removeFunctionToFunctionMapping(source, target) {
        const assignmentStmt = this.getParentStatement(target.funcInv);
        const newAssignIndex = this._transformStmt.body.getIndexOfStatements(assignmentStmt);

        target.funcInv.replaceArgumentExpressions(source.funcInv,
            TransformFactory.createDefaultExpression(target.type), true);

        const newAssignmentStmt = this.createNewAssignment(source.funcInv);
        this._transformStmt.body.addStatements(newAssignmentStmt, newAssignIndex, true);

        this._transformStmt.trigger('tree-modified', {
            origin: this._transformStmt,
            type: 'transform-connection-removed',
            title: `Remove mapping ${source.funcInv.getFunctionName()} to ${target.funcInv.getFunctionName()}`,
            data: {},
        });
    }

    /**
     * Remove operator to operator mapping.
     * @param {any} source source
     * @param {any} target target
     * @memberof TransformNodeMapper
     */
    removeOperatorToOperatorMapping(source, target) {
        const assignmentStmt = this.getParentStatement(target.operator);
        const newAssignIndex = this._transformStmt.body.getIndexOfStatements(assignmentStmt);

        const setter = this.getOperandSetterFunction(target.operator, target.index);
        if (!setter) {
            log.error('Unknown operator type for mapping');
            return;
        }
        setter(TransformFactory.createDefaultExpression(target.type), true);

        const newAssignmentStmt = this.createNewAssignment(source.operator);
        this._transformStmt.body.addStatements(newAssignmentStmt, newAssignIndex, true);

        this._transformStmt.trigger('tree-modified', {
            origin: this._transformStmt,
            type: 'transform-connection-removed',
            title: `Remove mapping ${source.operator.getOperatorKind()} to ${target.operator.getOperatorKind()}`,
            data: {},
        });
    }

    /**
     * remove function to operator mapping.
     * @param {any} source source
     * @param {any} target target
     * @memberof TransformNodeMapper
     */
    removeFunctionToOperatorMapping(source, target) {
        // Connection source and target are not structs
        // Source and target could be function nodes.
        const assignmentStmt = this.getParentStatement(target.operator);
        const newAssignIndex = this._transformStmt.body.getIndexOfStatements(assignmentStmt);

        const setter = this.getOperandSetterFunction(target.operator, target.index);
        if (!setter) {
            log.error('Unknown operator type for mapping');
            return;
        }
        setter(TransformFactory.createDefaultExpression(), true);

        const newAssignmentStmt = this.createNewAssignment(source.funcInv);
        this._transformStmt.body.addStatements(newAssignmentStmt, newAssignIndex, true);

        this._transformStmt.trigger('tree-modified', {
            origin: this._transformStmt,
            type: 'transform-connection-removed',
            title: `Remove mapping ${source.funcInv.getFunctionName()} to ${target.operator.getOperatorKind()}`,
            data: {},
        });
    }

    /**
     * Remove operator to function mapping.
     * @param {any} source source
     * @param {any} target target
     * @memberof TransformNodeMapper
     */
    removeOperatorToFunctionMapping(source, target) {
        const assignmentStmt = this.getParentStatement(target.funcInv);
        const newAssignIndex = this._transformStmt.body.getIndexOfStatements(assignmentStmt);

        target.funcInv.replaceArgumentExpressions(source.operator,
            TransformFactory.createDefaultExpression(target.type), true);

        const newAssignmentStmt = this.createNewAssignment(source.operator);
        this._transformStmt.body.addStatements(newAssignmentStmt, newAssignIndex, true);

        this._transformStmt.trigger('tree-modified', {
            origin: this._transformStmt,
            type: 'transform-connection-removed',
            title: `Remove mapping ${source.operator.getOperatorKind()} to ${target.funcInv.getFunctionName()}`,
            data: {},
        });
    }

    /**
     * Remove source types
     * @param {any} type type
     * @memberof TransformNodeMapper
     */
    removeSourceType(type) {
        _.remove(this._transformStmt.inputs, (input) => {
            return type.name === input;
        });
        this._transformStmt.body.filterStatements(
            (node) => { return TreeUtil.isVariableDef(node) || TreeUtil.isAssignment(node); })
            .forEach((stmt) => {
                this.removeInputExpressions(stmt, type.name);
            });
        this._transformStmt.trigger('tree-modified', {
            origin: this._transformStmt,
            type: 'transform-source-removed',
            title: `Remove source ${type.name}`,
            data: {},
        });
    }

    /**
     * Remove input expressions from a statement
     * @param {any} stmt statement
     * @param {any} expStr input expression string
     * @memberof TransformNodeMapper
     */
    removeInputExpressions(stmt, expStr) {
        if (TreeUtil.isVariableDef(stmt)) {
            if (stmt.getVariableName().getValue() === expStr) {
                this._transformStmt.body.removeStatements(stmt, true);
            }
        } else {
            const rightExpression = stmt.getExpression();
            if ((TreeUtil.isSimpleVariableRef(rightExpression)
                  && rightExpression.variableName.getValue() === expStr) ||
                (TreeUtil.isFieldBasedAccessExpr(rightExpression)
                  && rightExpression.expression.variableName === expStr)) {
                this._transformStmt.body.removeStatements(stmt, true);
            } else {
                this.removeInputNestedExpressions(rightExpression, expStr);
            }
        }
    }

    /**
     * Remove input nested expressions in statements of the given expression
     * @param {any} expression expression
     * @param {any} expStr input expression string
     * @memberof TransformNodeMapper
     */
    removeInputNestedExpressions(expression, expStr) {
        if (TreeUtil.isInvocation(expression)) {
            expression.getArgumentExpressions().forEach((child, index) => {
                if (child.getSource().trim() === expStr) {
                    // TODO: get type here
                    expression.replaceArgumentExpressionsByIndex(index, TransformFactory.createDefaultExpression(), true);
                } else {
                    this.removeInputNestedExpressions(child, expStr);
                }
            });
        } else if (TreeUtil.isUnaryExpr(expression)) {
            if (expression.getExpression().getSource().trim() === expStr) {
                expression.setExpression(TransformFactory.createDefaultExpression());
            } else {
                this.removeInputNestedExpressions(expression.getExpression(), expStr);
            }
        } else if (TreeUtil.isBinaryExpr(expression)) {
            // TODO: do a index check here
            if (expression.getRightExpression().getSource().trim() === expStr) {
                expression.setRightExpression(TransformFactory.createDefaultExpression());
            } else {
                this.removeInputNestedExpressions(expression.getRightExpression(), expStr);
            }
            if (expression.getLeftExpression().getSource().trim() === expStr) {
                expression.setLeftExpression(TransformFactory.createDefaultExpression());
            } else {
                this.removeInputNestedExpressions(expression.getLeftExpression(), expStr);
            }
        } else if (TreeUtil.isTernaryExpr(expression)) {
            // TODO: do a index check here
            if (expression.getCondition().getSource().trim() === expStr) {
                expression.setCondition(TransformFactory.createDefaultExpression());
            } else {
                this.removeInputNestedExpressions(expression.getCondition(), expStr);
            }
            if (expression.getThenExpression().getSource().trim() === expStr) {
                expression.setThenExpression(TransformFactory.createDefaultExpression());
            } else {
                this.removeInputNestedExpressions(expression.getThenExpression(), expStr);
            }
            if (expression.getElseExpression().getSource().trim() === expStr) {
                expression.setElseExpression(TransformFactory.createDefaultExpression());
            } else {
                this.removeInputNestedExpressions(expression.getThenExpression(), expStr);
            }
        } else if (TreeUtil.isFieldBasedAccessExpr(expression)) {
            if (TreeUtil.isSimpleVariableRef(expression.getExpression())) {
                this.removeInputExpressions(expression, expStr);
            } else {
                this.removeInputNestedExpressions(expression.getExpression(), expStr);
            }
        }
    }

    /**
     * Remove target types
     * @param {any} type type
     * @memberof TransformNodeMapper
     */
    removeTargetType(type) {
        _.remove(this._transformStmt.outputs, (output) => {
            return type.name === output;
        });
        this._transformStmt.body.filterStatements(TreeUtil.isAssignment).forEach((stmt) => {
            this.removeOutputExpressions(stmt, type.name);
        });
        this._transformStmt.trigger('tree-modified', {
            origin: this._transformStmt,
            type: 'transform-target-removed',
            title: `Remove target ${type.name}`,
            data: {},
        });
    }

    /**
     * Remove output expressions from a statement
     * @param {any} stmt statement
     * @param {any} expStr output expression string
     * @memberof TransformNodeMapper
     */
    removeOutputExpressions(stmt, expStr) {
        this.getVariables(stmt).forEach((exp, index) => {
            if (exp.getSource().trim() === expStr) {
                const stmtExpr = this.getExpression(stmt);
                if (TreeUtil.isSimpleVariableRef(stmtExpr)) {
                    // if it is a direct mapping, remove the statement
                    this._transformStmt.body.removeStatements(stmt, true);

                    // check for the statement is set to a temporary variable
                    const tempExp = this.getExpression(stmt);
                    if (this.isTempVariable(tempExp)) {
                        const tempUsedStmts = this.getInputStatements(tempExp);
                        if (tempUsedStmts.length === 1) {
                            // get temp declared statement
                            const tempStmt = this.getOutputStatement(tempExp);

                            // remove the temp reference if there are no statement using the temp
                            const varExp = TransformFactory.createVariableRefExpression(
                                this.getVariables(tempUsedStmts[0])[0].getSource().trim());

                            if (TreeUtil.isAssignment(tempStmt)) {
                                // TODO: improve for multiple assignments
                                tempStmt.replaceVariablesByIndex(0, varExp, true);
                                tempStmt.setDeclaredWithVar(false, true);
                            } else if (TreeUtil.isVariableDef(tempStmt)) {
                                const assStmt = NodeFactory.createAssignment();
                                assStmt.setExpression(tempStmt.getVariable().getInitialExpression());
                                assStmt.addVariables(this.getVariables(tempUsedStmts[0])[0]);
                                this._transformStmt.body.replaceStatements(tempStmt, assStmt, true);
                            }
                            this._transformStmt.body.removeStatements(tempUsedStmts[0], true);
                        }
                    }
                } else if (TreeUtil.isAssignment(stmt)) {
                    stmt.setDeclaredWithVar(true, true);
                    const outputVarName = TransformUtils.getNewTempVarName(this._transformStmt, VarPrefix.VAR);
                    const simpleVarRefExpression = TransformFactory.createVariableRefExpression(outputVarName);
                    stmt.replaceVariablesByIndex(index, simpleVarRefExpression, true);
                } else {
                    log.warn('Cannot remove output expressions in other statement types');
                }
            } else if (TreeUtil.isFieldBasedAccessExpr(stmt.getVariables()[0])) {
                this.removeNestedOutputExpressions(stmt, stmt.getVariables()[0], expStr);
            }
        });
    }

    /**
     * Remove output nested expressions in statements of the given expression
     * @param {object} stmt mapping statement to be removed
     * @param {object} expression expression
     * @param {any} expStr output expression string
     * @memberof TransformNodeMapper
     */
    removeNestedOutputExpressions(stmt, expression, expStr) {
        if (TreeUtil.isFieldBasedAccessExpr(expression)) {
            this.removeNestedOutputExpressions(stmt, expression.getExpression(), expStr);
        } else if (expression.getVariableName().getValue() === expStr) {
            this._transformStmt.body.removeStatements(stmt, true);
        }
    }

    // **** UTILITY FUNCTIONS ***** //

    /**
     * Remove a temporary variable if it is no longer being used and is not a complex expression.
     * @param {any} tempVarExp temp variable expression
     * @param {any} statement statement
     * @memberof TransformNodeMapper
     */
    removeTempVariable(tempVarExp, statement) {
        if (this.isTempVariable(tempVarExp, statement)) {
            // remove temp variable assignment if it is no longer being used
            const tempUsedStmts = this.getInputStatements(tempVarExp);
            if (tempUsedStmts.length === 1) {
                const tempAssignStmt = this.getOutputStatement(tempVarExp);
                if (tempAssignStmt && !this.isComplexExpression(this.getExpression(tempAssignStmt))) {
                    // only remove if the temp variable is not a complex expression, since a complex expression
                    // is open for further mappings
                    this._transformStmt.body.removeStatements(tempAssignStmt, true);
                }
            }
        }
    }

    /**
     * Get the temporary variable name assigned for a given source expression.
     *  - If the source expression is already assigned to a temporary variable or a placeholder,
     *      return that variable name
     *  - If the source expression is assigned to a direct variable, assign that to a temporary variable and
     *  return the new temporary variable name
     * @param {any} expression source expression
     * @returns {string} temporary variable name
     * @memberof TransformNodeMapper
     */
    getAssignedTempVarName(expression) {
        let tempVarName;
        this.getMappingStatements().forEach((stmt) => {
            if (this.getExpression(stmt).getSource().trim() === expression.getSource().trim()) {
                const outputExpressions = this.getOutputExpressions(stmt);
                // TODO: handle multiple temp returns here
                if (outputExpressions.length > 0) {
                    if ((outputExpressions[0].type === ExpressionType.TEMPVAR)
                        || (outputExpressions[0].type === ExpressionType.PLACEHOLDER)) {
                        tempVarName = outputExpressions[0].expression.getSource();
                    } else if (outputExpressions[0].type === ExpressionType.DIRECT) {
                        tempVarName = this.assignStatementToTempVariable(stmt);
                    }
                }
            }
        });
        return tempVarName;
    }

    /**
     * Create a new assignment statement with the given expression
     * @param {Expression} expression expression for the assignment
     * @returns {Assignment} assignment statement
     * @memberof TransformNodeMapper
     */
    createNewAssignment(expression) {
        const assignment = NodeFactory.createAssignment();
        assignment.setExpression(expression);
        const variableExp = TransformFactory.createVariableRefExpression(
                                TransformUtils.getNewTempVarName(this._transformStmt, VarPrefix.VAR));
        assignment.addVariables(variableExp);
        assignment.setDeclaredWithVar(true);
        return assignment;
    }

    /**
    * Gets the enclosing statement.
    * @param {Statement|Expression} node
    * @returns {Statement} enclosing statement
    * @memberof TransformNodeMapper
    */
    getParentStatement(node) {
        if (TreeUtil.isAssignment(node) || TreeUtil.isVariableDef(node)) {
            return node;
        } else {
            return this.getParentStatement(node.parent);
        }
    }

    /**
     * The expression that can be used for mapping. E.g. : type cast expressions cannot be mapped
     * directly, but when the cast expression is removed they can be mapped. Similarly, when
     * temporary variables are used, their real mapping expression is the underlying expression
     * that is wrapped by the temporary variable
     * @param {Expression} expression expression
     * @return {Expression} mapping expression
     * @memberof TransformNodeMapper
     */
    getMappableExpression(expression) {
        if (TreeUtil.isTypeCastExpr(expression) || TreeUtil.isTypeConversionExpr(expression)) {
            return this.getMappableExpression(expression.getExpression());
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
                this._transformStmt.body.removeStatements(targetStmt, true);
            }
        }
    }

    /**
     * Get the expression assigned by the temporary variable
     * @param {Expression} expression temporary variable
     * @returns {Expression} assigned expression
     * @memberof TransformNodeMapper
     */
    getTempResolvedExpression(expression) {
        const statement = this.getOutputStatement(expression);
        return this.getExpression(statement);
    }

    /**
     * Remove output mapping from the assignment statement
     * @param {AssignmentStatement} assignmentStmt assignment statement
     * @param {Expression} leftExpression left expression to be removed
     * @memberof TransformNodeMapper
     */
    removeOutputMapping(assignmentStmt, varExpression) {
        const varExp = assignmentStmt.getVariables().find((exp) => {
            return exp.getSource().trim() === varExpression.getSource();
        });
        const tempVarName = TransformUtils.getNewTempVarName(this._transformStmt, VarPrefix.VAR);
        const outputVar = TransformFactory.createVariableRefExpression(tempVarName);
        assignmentStmt.replaceVariables(varExp, outputVar, true);
    }

    /**
     * Assign the given assignment statement value to a temporary variable
     * @param {Statement} stmt the statement to be extracted to a temp variable
     * @returns {string} the temp variable name
     * @memberof TransformNodeMapper
     */
    assignStatementToTempVariable(stmt) {
        const index = this._transformStmt.body.getIndexOfStatements(stmt);
        // TODO: handle unsafe expressions here
        return this.assignExpressionToTempVariable(this.getExpression(stmt), index);
    }

    /**
     * Assign the given expression to a temporary variable
     * @param {Expression} expression the expression to assign to temp variable
     * @param {integer} index index to insert the assignment statement
     * @param {boolean} safe whether the expression is safe
     * @returns {string} the temp variable name
     * @memberof TransformNodeMapper
     */
    assignExpressionToTempVariable(expression, index = 0, safe = true) {
        const tempVarName = TransformUtils.getNewTempVarName(this._transformStmt, VarPrefix.VAR);
        const variables = [];
        variables.push(TransformFactory.createVariableRefExpression(tempVarName));
        if (!safe) {
            const errorVarRef = TransformFactory.createVariableRefExpression('_');
            variables.push(errorVarRef);
        }
        this.insertAssignmentStatement(variables, expression, index, true);
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
    insertAssignmentStatement(variableExpressions, rightExpression, index, isVar = true) {
        const assignmentStmt = NodeFactory.createAssignment({});

        assignmentStmt.setVariables(variableExpressions, true);
        assignmentStmt.setExpression(rightExpression, true);
        assignmentStmt.setDeclaredWithVar(isVar, true);
        // TODO: handle type cast and coversion
    //     const errorVarRef = DefaultBallerinaASTFactory.createIgnoreErrorVariableReference();
    //     varRefList.addChild(errorVarRef);
        this._transformStmt.body.addStatements(assignmentStmt, index, true);
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
            case 'explicit' :
                return TransformFactory.createTypeCastExpr(sourceExpression, targetType);
            case 'conversion' :
                return TransformFactory.createTypeConversionExpr(sourceExpression, targetType);
            case 'implicit' :
            default :
                return sourceExpression;
        }
    }

    /**
    * Is the statement a complex one or not. A complex statement will have
    * function invocation or an operator as the right expression.
    * @param {Statement} statement the statement
    * @return {boolean} is complex or not
    * @memberof TransformNodeMapper
    */
    isComplexStatement(statement) {
        return (this.isComplexExpression(this.getMappableExpression(this.getExpression(statement))));
    }

    /**
     * An expression is complex if it is function invocation or an operator expression.
     * @param {Expression} expression expression
     * @returns {boolean} is complex or not
     * @memberof TransformNodeMapper
     */
    isComplexExpression(expression) {
        if (TreeUtil.isInvocation(expression)) {
            return true;
        }
        if (TreeUtil.isTernaryExpr(expression)) {
            return true;
        }
        if (TreeUtil.isBinaryExpr(expression)) {
            return true;
        }
        if (TreeUtil.isUnaryExpr(expression)) {
            return true;
        }
        return false;
    }

    /**
     * Get expressions that are mapped by the assignment or variable definition statement.
     * The output mappings ignore error expressions and placeholder temporary variables.
     * @param {Statement} statement statement
     * @returns {[Expression]} expressions that are output mappings
     * @memberof TransformNodeMapper
     */
    getOutputExpressions(statement) {
        const outputMappings = [];

        this.getVariables(statement).forEach((exp, index) => {
            outputMappings[index] = { expression: exp };
            if (this.isInTransformInputOutput(exp)) {
                outputMappings[index].type = ExpressionType.DIRECT;
            } else if (this.isErrorExpression(exp)) {
                outputMappings[index].type = ExpressionType.ERROR;
            } else if (this.isPlaceHolderTempVariable(exp)) {
                outputMappings[index].type = ExpressionType.PLACEHOLDER;
            } else if (this.isConstant(exp, statement)) {
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
    isInTransformInputOutput(expression) {
        const inputOutput = [...this._transformStmt.inputs, ...this._transformStmt.outputs];
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
    isErrorExpression(expression) {
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
     * whether the constant expression is a constant value
     * @param {Expression} expression expression
     * @param {Assignment} statement assignment statement
     * @returns {boolean} whether the expression is a constant
     * @memberof TransformNodeMapper
     */
    isConstant(expression, statement) {
        if (!statement) {
            statement = this.getOutputStatement(expression);
        }
        return TreeUtil.isLiteral(this.getExpression(statement));
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
     * @param {Statement} statement statement
     * @return {boolean} whether temporary or not
     * @memberof TransformNodeMapper
     */
    isTempVariable(expression, statement) {
        if (this.isInTransformInputOutput(expression)
            || this.isErrorExpression(expression)
            || this.isPlaceHolderTempVariable(expression)
            || this.isConstant(expression, statement)) {
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
    getOutputStatement(expression) {
        return this.getMappingStatements().find((stmt) => {
            return this.getVariables(stmt).find((varExp) => {
                return expression.getSource().trim() === varExp.getSource().trim();
            });
        });
    }

    /**
    * Gets the statements where a given expression has been used as input in any form as a vertice.
    * Usage denotes that it is a right expression of a statement.
    * @param {Expression} expression expression to check usage
    * @param {TransformNode} transformNode transform node searched
    * @returns {[AssignmentStatement]} assignment statements with usage
    * @memberof TransformNodeMapper
    */
    getInputStatements(expression) {
        return this.getMappingStatements().filter((stmt) => {
            const matchingInput = this.getVerticesFromExpression(this.getExpression(stmt)).filter((exp) => {
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
    getVerticesFromExpression(expression) {
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
        if (TreeUtil.isTernaryExpr(expression)) {
            return [...this.getVerticesFromExpression(expression.getCondition()),
                ...this.getVerticesFromExpression(expression.getThenExpression()),
                ...this.getVerticesFromExpression(expression.getElseExpression())];
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
        if (TreeUtil.isIdentifier(expression)) {
            return [expression];
        }
        log.error('No vertices detected for expression ' + expression.kind);
        return [];
    }


    /**
     * Get expression from statement
     * @param {Statement} stmt statement
     * @return {Expression} expression of the statement
     * @memberof TransformNodeMapper
     */
    getExpression(stmt) {
        if (TreeUtil.isAssignment(stmt)) {
            return stmt.getExpression();
        }
        if (TreeUtil.isVariableDef(stmt)) {
            return stmt.getVariable().getInitialExpression();
        }
        return undefined;
    }

    /**
     * Get variables from statement
     * @param {Statement} stmt statement
     * @return {[Expression]} variables of the statement
     * @memberof TransformNodeMapper
     */
    getVariables(stmt) {
        if (TreeUtil.isAssignment(stmt)) {
            return stmt.getVariables();
        }
        if (TreeUtil.isVariableDef(stmt)) {
            return [stmt.getVariable().getName()];
        }
        return [];
    }

    /**
     * Get mapping related statements in transform statement. The only statements that can be mapped are
     * assignment and variable def statements
     * @returns {[Statement]} mapping statements
     * @memberof TransformNodeMapper
     */
    getMappingStatements() {
        return this._transformStmt.body.filterStatements(_.negate(TreeUtil.isComment));
    }

    /**
     * Get associated setter function of the operator for the given index
     * @param {any} operator operator
     * @param {any} index index of the operand
     * @returns {Function} setter function
     * @memberof TransformNodeMapper
     */
    getOperandSetterFunction(operator, index) {
        if (TreeUtil.isUnaryExpr(operator)) {
            return operator.setExpression.bind(operator);
        } else if (TreeUtil.isBinaryExpr(operator)) {
            if (index === 0) {
                return operator.setLeftExpression.bind(operator);
            } else if (index === 1) {
                return operator.setRightExpression.bind(operator);
            }
        } else if (TreeUtil.isTernaryExpr(operator)) {
            if (index === 0) {
                return operator.setCondition.bind(operator);
            } else if (index === 1) {
                return operator.setThenExpression.bind(operator);
            } else if (index === 2) {
                return operator.setElseExpression.bind(operator);
            }
        }
        return undefined;
    }
}

export default TransformNodeMapper;
