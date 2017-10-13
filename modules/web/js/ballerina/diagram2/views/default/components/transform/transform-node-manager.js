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
import Mapper from './transform-node-mapper';
import TransformFactory from '../../../../../model/transform-factory';
import TransformUtils, { VarPrefix, ExpressionType } from '../../../../../utils/transform-utils';

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
        return TransformFactory.createVariableRefExpression(name);
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
            this._mapper.createInputToOutputMapping(sourceExpression, targetExpression, target.type, compatibility);
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
                this._mapper.removeInputToOperatorMapping(source, target);
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
        if (parentNode) {
            let parentDef;
            if (TreeUtil.isInvocation(parentNode)) {
                parentDef = this.getFunctionVertices(parentNode);
            }
            this._mapper.removeNode(expression, statement, parentNode, parentDef);
        } else {
            this._mapper.removeNode(expression, statement);
        }
    }

    removeSourceType(type) {
        this._mapper.removeSourceType(type);
    }

    removeTargetType(type) {
        this._mapper.removeTargetType(type);
    }

    /**
     * Get function vertices from the function invocation expression
     * @param {any} functionInvocationExpression function invocation expression
     * @returns {Object} function vertices
     * @memberof TransformNodeManager
     */
    getFunctionVertices(functionInvocationExpression) {
        const fullPackageName = TreeUtil.getFullPackageName(functionInvocationExpression);
        const funPackage = this._environment.getPackageByName(fullPackageName);

        if (!funPackage) {
            log.error('Cannot find package definition for ' + fullPackageName);
            return;
        }

        const funcDef = funPackage.getFunctionDefinitionByName(functionInvocationExpression.getFunctionName());

        if (!funcDef) {
            log.error('Cannot find function definition for ' + functionInvocationExpression.getFunctionName());
            return;
        }

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

    /**
     * Get operator input and output vertices
     * @param {any} operatorExpression operator expression
     * @returns vertices
     * @memberof TransformNodeManager
     */
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

        if (TreeUtil.isBinaryExpr(operatorExpression)) {
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

    /**
     * Get the struct definition
     * @param {string} packageIdentifier package alias or identifier
     * @param {string} structName struct name
     * @returns {Object} struct definition
     * @memberof TransformNodeManager
     */
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
        if (TreeUtil.isSimpleVariableRef(expression)
            && this._mapper.isTempVariable(mapExp, statement)) {
            return {
                exp: this._mapper.getMappableExpression(this._mapper.getTempResolvedExpression(mapExp)),
                isTemp: true,
            };
        }
        return { exp: mapExp, isTemp: false };
    }

    /**
     * Add a new variable in transform scope or outer scope
     * @param {target|source} type type of the variable
     * @returns new variable identifier
     * @memberof TransformNodeManager
     */
    addNewVariable(type) {
        let variableName = 'tempVar';
        let node = this._transformStmt.getBody();
        let newVarIndex = 0;
        if (type === 'target') {
            node = this._transformStmt.parent;
            variableName = 'var';
        }
        const varNameRegex = new RegExp(variableName + '[\\d]*');
        const varDefStmts = node.filterStatements(TreeUtil.isVariableDef);
        const tempVarIdentifiers = varDefStmts.filter((varDef) => {
            return varNameRegex.test(varDef.getVariableName().getValue());
        }).map((varDef) => {
            return varDef.getVariableName().getValue();
        }).sort();

        let index = 0;
        if (tempVarIdentifiers.length > 0) {
            index = Number.parseInt(tempVarIdentifiers[tempVarIdentifiers.length - 1]
                    .substring(variableName.length), 10) + 1;
        }
        const varName = variableName + index;

        const variableDef = TransformFactory.createVariableDef(varName, 'string', '');
        node.addStatements(variableDef, newVarIndex, false);
        return variableDef;
    }

    updateVariable(node, varName, statementString, type) {
        const variableDefinitionStatement = TransformFactory.createVariableDefFromStatement(statementString);
        let varDefNode = node.body;
        let entities = node.inputs;
        if (type === 'target') {
            varDefNode = node.parent;
            entities = node.outputs;
        }
        const newVarName = variableDefinitionStatement.getVariableName().value;
        _.forEach(varDefNode.statements, (child) => {
            if (TreeUtil.isVariableDef(child)
                  && child.getVariableName().value === varName) {
                varDefNode.replaceStatements(child, variableDefinitionStatement, false);
            } else if (TreeUtil.isAssignment(child)
                          && TreeUtil.isSimpleVariableRef(child.expression)
                          && child.expression.getVariableName().value === varName) {
                const variableReferenceExpression = TransformFactory.createVariableRefExpression(newVarName);
                child.setExpression(variableReferenceExpression);
            } else if (TreeUtil.isTransform(child) && type === 'target') {
                _.forEach(child.body.statements, (transChild) => {
                    if (TreeUtil.isAssignment(transChild) && TreeUtil.isSimpleVariableRef(transChild.variables[0])
                                  && transChild.variables[0].getVariableName().getValue() === varName) {
                        const variableReferenceExpression = TransformFactory.createVariableRefExpression(newVarName);
                        transChild.replaceVariables(transChild.variables[0], variableReferenceExpression);
                    }
                });
            }
        });
        _.forEach(entities, (input, i) => {
            if (input === varName) {
                entities[i] = newVarName;
            }
        });
        if (type === 'target') {
            node.setOutputs(entities);
        } else {
            node.setInputs(entities);
        }
        node.trigger('tree-modified', {
            origin: node,
            type: 'variable-update',
            title: `Variable update ${varName}`,
            data: {},
        });
        return true;
    }
 }

export default TransformNodeManager;
