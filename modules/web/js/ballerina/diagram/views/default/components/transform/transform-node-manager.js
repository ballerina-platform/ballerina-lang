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
import BallerinaASTFactory from '../../../../../ast/ast-factory';
import Mapper from './transform-node-mapper';

/**
 * Transform node manager
 * @class TransformNodeManager
 */
class TransformNodeManager {

    /**
     * Creates an instance of TransformNodeManager.
     * @param {any} args args to construct TransformNodeManager
     * @memberof TransformNodeManager
     */
    constructor(args) {
        this._transformStmt = _.get(args, 'transformStmt');
        this._typeLattice = _.get(args, 'typeLattice');
        this._environment = _.get(args, 'environment');
        this._mapper = new Mapper({ transformStmt: this._transformStmt });
    }

    /**
     * Set transform statement
     * @param {any} transformStmt transform statement
     * @memberof TransformNodeManager
     */
    setTransformStmt(transformStmt) {
        this._transformStmt = transformStmt;
        this._mapper.setTransformStmt(this._transformStmt);
    }

    /**
     * Get compatibility of given source and target types
     * @param {any} source source type
     * @param {any} target target type
     */
    getCompatibility(source, target) {
        const compatibility = this._typeLattice.getCompatibility(source, target);

        if (compatibility) {
            return compatibility;
        }

        if (source === target) {
            return { safe: true, type: 'implicit' };
        }

        // If the source and target are builtin types, we consider the mapping illegal
        // if it is not available in the type lattice.
        const builtInTypes = this._environment.getTypes();
        if (builtInTypes.includes(source) && builtInTypes.includes(target)) {
            return undefined;
        }
        return { safe: true, type: 'implicit' };
    }

    /**
     * Check validity of a source to target mapping based on the types
     * @param {any} source source type
     * @param {any} target target type
     * @returns {boolean} compatibility
     * @memberof TransformNodeManager
     */
    isConnectionValid(source, target) {
        if (this.getCompatibility(source, target)) {
            return true;
        }
        return false;
    }

    /**
     * Get the vertex expression by name of the vertex
     * @param {any} name name
     * @param {any} isField whether a struct field or a variable
     * @returns {Expression} vertex expression
     * @memberof TransformNodeManager
     */
    getVertexExpression(name, isField) {
        let expression;
        if (isField) {
            expression = BallerinaASTFactory.createFieldBasedVarRefExpression();
        } else {
            expression = BallerinaASTFactory.createSimpleVariableReferenceExpression();
        }
        expression.setExpressionFromString(name);
        return expression;
    }

    /**
     * Creates the statement edge based on the connection drawn
     * @param {any} connection connection created
     * @memberof TransformNodeManager
     */
    createStatementEdge(connection) {
        const { source, target } = connection;
        let sourceExpression;
        let targetExpression;
        const compatibility = this.getCompatibility(source.type, target.type);

        if (!compatibility) {
            return;
        }

        // create source and target expressions
        if (source.endpointKind === 'input') {
            sourceExpression = this.getVertexExpression(source.name, source.isField);
        }
        if (target.endpointKind === 'output') {
            targetExpression = this.getVertexExpression(target.name, target.isField);
        }

        // create or modify statements as per the connection.
        if (source.endpointKind === 'input' && target.endpointKind === 'output') {
            // Connection is from source variable to a target variable.
            this._mapper.createInputToOutputMapping(sourceExpression, targetExpression, compatibility);
            return;
        }

        if (source.endpointKind === 'input') {
            if (target.funcInv) {
                this._mapper.createInputToFunctionMapping(sourceExpression, target, compatibility);
                return;
            }
            if (target.operator) {
                this._mapper.createInputToOperatorMapping(sourceExpression, target, compatibility);
                return;
            }
            log.error('Unknown intermediate node');
        }

        if (target.endpointKind === 'output') {
            if (source.funcInv) {
                this._mapper.createFunctionToOutputMapping(targetExpression, source, target, compatibility);
                return;
            }
            if (source.operator) {
                this._mapper.createOperatorToOutputMapping(targetExpression, source, target, compatibility);
                return;
            }
            log.error('Unknown intermediate node');
        }

        this._mapper.createNodeToNodeMapping(source, target);
    }

    /**
     * Remove statement edge based on the removed connection
     * @param {any} connection connection removed
     * @memberof TransformNodeManager
     */
    removeStatementEdge(connection) {
        const { source, target } = connection;

        if (source.endpointKind === 'input' && target.endpointKind === 'output') {
            this._mapper.removeInputToOutputMapping(source.name, target.name);
            return;
        }

        if (source.endpointKind === 'input') {
            if (target.funcInv) {
                this._mapper.removeInputToFunctionMapping(source.name, target);
                return;
            }
            if (target.operator) {
                this._mapper.removeInputToOperatorMapping(source.name, target);
            }
        }

        if (target.endpointKind === 'output') {
            this._mapper.removeNodeToOutputMapping(source, target.name);
            return;
        }

        this._mapper.removeNodeToNodeMapping(source, target);
    }

    /**
     * Remove intermediate node which is a function or an operator
     * @param {any} expression expression
     * @param {any} parentNode parent node expression
     * @param {any} statement enclosing statement
     * @memberof TransformNodeManager
     */
    removeIntermediateNode(expression, parentNode, statement) {
        this._mapper.removeNode(expression, parentNode, statement);
    }

    removeSourceType(type) {
        this._mapper.removeSourceType(type);
    }

    removeTargetType(type) {
        this._mapper.removeTargetType(type);
    }

    /**
     * @param {any} UUID of an assignment statement
     * @returns {AssignmentStatement} assignment statement for maching ID
     *
     * @memberof TransformStatementDecorator
     */
    findExistingAssignmentStatement(id) {
        return _.find(this._transformStmt.getChildren(), (child) => {
            return child.getID() === id;
        });
    }

    /**
     * @param {any} UUID of a function invocation statement
     * @returns {AssignmentStatement} enclosing assignment statement containing the matching function
     * invocation statement ID
     *
     * @memberof TransformStatementDecorator
     */
    findEnclosingAssignmentStatement(funcInv) {
        return _.find(this._transformStmt.getChildren(), (assignmentStmt) => {
            return this.findFunctionInvocationById(assignmentStmt.getRightExpression(), funcInv.id);
        });
    }

    findFunctionInvocationById(expression, id) {
        if (expression.id === id) {
            return expression;
        }
        if (expression.getChildById(id)) {
            return expression.getChildById(id);
        }
        return expression.getChildren().find((child) => {
            return (this.findFunctionInvocationById(child, id));
        });
    }

    getFunctionVertices(functionInvocationExpression) {
        const funPackage = this._environment.getPackageByName(functionInvocationExpression.getFullPackageName());
        const funcDef = funPackage.getFunctionDefinitionByName(functionInvocationExpression.getFunctionName());
        const parameters = [];
        const returnParams = [];

        _.forEach(funcDef.getParameters(), (param, index) => {
            const structDef = this.getStructDefinition(param.packageName, param.type);
            let paramObj;
            const paramName = `${functionInvocationExpression.getID()}:${index}`;
            if (structDef) {
                paramObj = this.getStructType(paramName, param.type, structDef);
            } else {
                paramObj = {
                    name: paramName,
                    type: param.type,
                };
            }

            paramObj.displayName = param.name;
            paramObj.index = index;
            paramObj.funcInv = functionInvocationExpression;

            parameters.push(paramObj);
        });

        _.forEach(funcDef.getReturnParams(), (returnParam, index) => {
            const paramName = `${functionInvocationExpression.getID()}:${index}:return`;
            const paramObj = {
                name: paramName,
                type: returnParam.type,
                index,
                funcInv: functionInvocationExpression,
            };
            returnParams.push(paramObj);
        });

        return {
            parameters,
            returnParams,
        };
    }

    getOperatorVertices(operatorExpression) {
        const parameters = [];
        const returnParams = [];

        parameters[0] = {
            name: operatorExpression.getID() + ':0',
            displayName: '',
            type: 'var',
            operator: operatorExpression,
            index: 0,
        };

        if (BallerinaASTFactory.isBinaryExpression(operatorExpression)) {
            parameters[1] = {
                name: operatorExpression.getID() + ':1',
                displayName: '',
                type: 'var',
                operator: operatorExpression,
                index: 1,
            };
        }

        returnParams.push({
            name: operatorExpression.getID() + ':0:return',
            displayName: '',
            type: 'var',
            operator: operatorExpression,
            index: 0,
        });

        return {
            parameters,
            returnParams,
        };
    }

    getStructDefinition(packageIdentifier, structName) {
        let pkg;
        if (packageIdentifier == null) {
            // check both undefined and null
            pkg = this._environment.getCurrentPackage();
        } else {
            pkg = this._environment.getPackageByIdentifier(packageIdentifier);
        }

        if (pkg === undefined) {
            return undefined;
        }

        return _.find(pkg.getStructDefinitions(), (structDef) => {
            return structName === structDef.getName();
        });
    }

    getStructType(name, typeName, structDefinition, currentRoot) {
        const struct = {};
        struct.name = name;
        struct.displayName = name;
        struct.properties = [];
        struct.type = 'struct';
        struct.typeName = typeName;

        const root = currentRoot || struct;

        _.forEach(structDefinition.getFields(), (field) => {
            let property = {};
            const fieldName = `${name}.${field.getName()}`;

            const innerStruct = this.getStructDefinition(field.getPackageName(), field.getType());
            if (!_.isUndefined(innerStruct) && typeName !== property.type) {
                property = this.getStructType(fieldName, field.getType(), innerStruct, root);
            } else {
                property.name = fieldName;
                property.type = field.getType();
                property.packageName = field.getPackageName();
                property.structName = name;
            }

            property.isField = true;
            property.root = root;
            property.displayName = field.getName();

            struct.properties.push(property);
        });
        return struct;
    }

    /**
     * If the target is a cast expression, we need to find the underlying expression
     * that needs to be mapped in the view. Similarly if the source is a temp variable,
     * corresponding expression that needs to be mapped is also found here.
     * @param {Expression} expression lhs or rhs expression
     * @returns {Expression} mapping expression
     * @memberof TransformNodeManager
     */
    getResolvedExpression(expression, statement) {
        const mapExp = this._mapper.getMappableExpression(expression);
        if (BallerinaASTFactory.isSimpleVariableReferenceExpression(expression)
            && this._mapper.isTempVariable(mapExp, statement)) {
            return {
                exp: this._mapper.getMappableExpression(this._mapper.getTempResolvedExpression(mapExp)),
                isTemp: true,
            };
        }
        return { exp: mapExp, isTemp: false };
    }

    findTempVarUsages(tempVarName) {
        const assignmentStmts = this._transformStmt.filterChildren(BallerinaASTFactory.isAssignmentStatement);
        const tempUsedAssignmentStmts = assignmentStmts.filter((assStmt) => {
            const rightExp = this.getMappingExpression(assStmt.getRightExpression());
            if (BallerinaASTFactory.isFunctionInvocationExpression(rightExp)) {
                const matchingExpressions = rightExp.getChildren().find((exp) => {
                    return (this.getMappingExpression(exp, false).getExpressionString() === tempVarName);
                });
                if (matchingExpressions) {
                    return true;
                }
                return false;
            } else {
                return (rightExp.getExpressionString() === tempVarName);
            }
        });
        return tempUsedAssignmentStmts;
    }

    findAssignedVertexForTemp(expression) {
        const tempVarName = expression.getVariableName();
        const assignmentStmts = this._transformStmt.filterChildren(BallerinaASTFactory.isAssignmentStatement);
        return assignmentStmts.find((assStmt) => {
            return assStmt.getLeftExpression().getChildren().find((leftExpr) => {
                return (tempVarName === leftExpr.getExpressionString());
            });
        });
    }

    addNewVariable(type) {
        let variableName = 'tempVar';
        let node = this._transformStmt;
        let newVarIndex = 0;
        if (type === 'target') {
            node = this._transformStmt.getParent();
            variableName = 'var';
            newVarIndex = this._transformStmt.getParent().getIndexOfChild(this._transformStmt);
        }
        const varNameRegex = new RegExp(variableName + '[\\d]*');
        const varDefStmts = node.filterChildren(BallerinaASTFactory.isVariableDefinitionStatement);
        const tempVarIdentifiers = varDefStmts.filter((varDef) => {
            return varNameRegex.test(varDef.getIdentifier());
        }).map((varDef) => {
            return varDef.getIdentifier();
        }).sort();

        let index = 0;
        if (tempVarIdentifiers.length > 0) {
            index = Number.parseInt(tempVarIdentifiers[tempVarIdentifiers.length - 1]
                    .substring(variableName.length), 10) + 1;
        }
        const varName = variableName + index;
        const variableDefinitionStatement = BallerinaASTFactory.createVariableDefinitionStatement();
        variableDefinitionStatement.setStatementFromString('string ' + varName + ' = ""');
        node.addChild(variableDefinitionStatement, newVarIndex);
        return variableDefinitionStatement;
    }

    updateVariable(node, varName, statementString, type) {
        const variableDefinitionStatement = BallerinaASTFactory.createVariableDefinitionStatement();
        let varDefNode = node;
        let entities = node.getInput();
        if (type === 'target') {
            varDefNode = node.getParent();
            entities = node.getOutput();
        }

        variableDefinitionStatement.setStatementFromString(statementString);
        if (variableDefinitionStatement.children.length > 0) {
            const newVarName = variableDefinitionStatement.getVariableDef().getName();
            _.forEach(varDefNode.getChildren(), (child) => {
                if (BallerinaASTFactory.isVariableDefinitionStatement(child)
                      && child.getLeftExpression().getVariableName() === varName) {
                    const index = varDefNode.getIndexOfChild(child);
                    varDefNode.removeChild(child, true);
                    varDefNode.addChild(variableDefinitionStatement, index, true);
                } else if (BallerinaASTFactory.isAssignmentStatement(child)
                              && BallerinaASTFactory.isSimpleVariableReferenceExpression(child.getRightExpression())
                              && child.getRightExpression().getVariableName() === varName) {
                    child.removeChild(child.children[1], true);
                    const variableReferenceExpression = BallerinaASTFactory.createSimpleVariableReferenceExpression();
                    variableReferenceExpression.setExpressionFromString(newVarName);
                    child.addChild(variableReferenceExpression, 1, true);
                }
            });
            _.forEach(entities, (input, i) => {
                if (input.getVariableName() === varName) {
                    const variableReferenceExpression = BallerinaASTFactory.createSimpleVariableReferenceExpression();
                    variableReferenceExpression.setExpressionFromString(newVarName);
                    entities[i] = variableReferenceExpression;
                }
            });
            if (type === 'target') {
                node.setOutput(entities);
            } else {
                node.setInput(entities);
            }
            node.trigger('tree-modified', {
                origin: this,
                type: 'variable-update',
                title: `Variable update ${varName}`,
                data: {},
            });
            return true;
        } else {
            return false;
        }
    }
 }

export default TransformNodeManager;
