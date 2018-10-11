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
import Mapper from './transformer-node-mapper';
import TransformFactory from '../../../../../model/transformer-factory';

/**
 * Transform node manager
 * @class TransformNodeManager
 */
class TransformerNodeManager {

    /**
     * Creates an instance of TransformerNodeManager.
     * @param {any} args args to construct TransformerNodeManager
     * @memberof TransformerNodeManager
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
     * @memberof TransformerNodeManager
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
     * @memberof TransformerNodeManager
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
     * @returns {Expression} vertex expression
     * @memberof TransformerNodeManager
     */
    getVertexExpression(name, type) {
        return TransformFactory.createVariableRefExpression(name, type);
    }

    /**
     * create a variable for provided name and type
     * @param  {string} name variable name
     * @param  {string} type variable type
     * @return {object}     created variable node
     */
    createVariable(name, type) {
        return TransformFactory.createVariable(name, type);
    }

    /**
     * Creates the statement edge based on the connection drawn
     * @param {any} connection connection created
     * @memberof TransformerNodeManager
     */
    createStatementEdge(connection) {
        const { source, target } = connection;
        let sourceType = source.type;
        let targetType = target.type;
        let sourceExpression;
        let targetExpression;
        let compatibility;
        if (source.typeName && target.typeName) {
            sourceType = source.typeName;
            targetType = target.typeName;
            compatibility = {
                safe: false,
                type: 'conversion',
            };
        } else {
            compatibility = this.getCompatibility(sourceType, targetType);
        }

        if (!compatibility) {
            return;
        }

        // create source and target expressions
        if (source.endpointKind === 'input') {
            sourceExpression = this.getVertexExpression(source.name, sourceType);
        }
        if (target.endpointKind === 'output') {
            targetExpression = this.getVertexExpression(target.name, targetType);
        }

        // create or modify statements as per the connection.
        if (source.endpointKind === 'input' && target.endpointKind === 'output') {
            // Connection is from source variable to a target variable.
            this._mapper.createInputToOutputMapping(sourceExpression, targetExpression, targetType, compatibility);
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
     * @memberof TransformerNodeManager
     */
    removeStatementEdge(connection) {
        const { source, target } = connection;

        if (source.endpointKind === 'input' && (target.endpointKind === 'output' || target.endpointKind === 'param')) {
            this._mapper.removeInputToOutputMapping(source.name, target.name);
            return;
        }

        if (source.endpointKind === 'input') {
            if (target.funcInv) {
                this._mapper.removeInputToFunctionMapping(source, target);
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
     * @memberof TransformerNodeManager
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
     * @memberof TransformerNodeManager
     */
    getFunctionVertices(functionInvocationExpression) {
        let fullPackageName = functionInvocationExpression.getFullPackageName();
        let type = 'function';
        let status = true;
        if (!fullPackageName) {
            // TODO: Fix obtaining the full package name for bound functions
            // package name should be found through the receiver in bound functions
            // but there is no straightforward way to get the variable def of the receiver
            fullPackageName = TreeUtil.getFullPackageName(functionInvocationExpression);
        }

        const funPackage = this._environment.getPackageByName(fullPackageName);

        if (!funPackage) {
            log.error('Cannot find package definition for ' + fullPackageName);
            status = false;
        }

        const funcDef = funPackage.getFunctionDefinitionByName(functionInvocationExpression.getFunctionName());
        let parameters = [];
        let returnParams = [];
        let receiver;
        if (functionInvocationExpression.iterableOperation) {
            const inputParamName = `${functionInvocationExpression.getID()}:0:receiver`;
            const returnParamName = `${functionInvocationExpression.getID()}:0:return`;
            let dataType = '[]';
            let dataReturnType = '[]';
            type = 'iterable';
            if (functionInvocationExpression.getExpression().symbolType
                && functionInvocationExpression.getExpression().symbolType.length > 0) {
                dataType = functionInvocationExpression.getExpression().symbolType[0];
            }
            if (functionInvocationExpression.symbolType && functionInvocationExpression.symbolType.length > 0) {
                dataReturnType = functionInvocationExpression.symbolType[0];
            }
            const argType = dataType.replace('intermediate_collection', '[]');
            if (functionInvocationExpression.argumentExpressions.length === 0) {
                receiver = {
                    name: inputParamName,
                    type: argType,
                    funcInv: functionInvocationExpression,
                };
            } else {
                parameters = [{ name: inputParamName,
                    type: argType,
                    index: 0 }];
            }
            returnParams = [{
                index: 0,
                name: returnParamName,
                type: dataReturnType.replace('intermediate_collection', '[]'),
                funcInv: functionInvocationExpression }];
        } else if (!funcDef) {
            log.error('Cannot find function definition for ' + functionInvocationExpression.getFunctionName());
            status = false;
        } else {
            if (funcDef.getReceiverType()) {
                receiver = {
                    name: `${functionInvocationExpression.getID()}:0:receiver`,
                    type: funcDef.getReceiverType(),
                    funcInv: functionInvocationExpression,
                };
            }

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
        }

        if (status) {
            return {
                type,
                parameters,
                returnParams,
                receiver,
            };
        } else {
            return null;
        }
    }

    /**
     * Get operator input and output vertices
     * @param {any} operatorExpression operator expression
     * @returns vertices
     * @memberof TransformerNodeManager
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

        if (TreeUtil.isTernaryExpr(operatorExpression)) {
            parameters[1] = {
                name: operatorExpression.getID() + ':1',
                displayName: '',
                type: 'var',
                operator: operatorExpression,
                index: 1,
            };
            parameters[2] = {
                name: operatorExpression.getID() + ':2',
                displayName: '',
                type: 'var',
                operator: operatorExpression,
                index: 2,
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
     * Get conversion input and output vertices
     * @param {any} conversionExpression conversion expression
     * @returns vertices
     * @memberof TransformerNodeManager
     */
    getConversionVertices(conversionExpression) {
        const parameters = [];
        const returnParams = [];

        const transformerDef = this._transformStmt.parent.getTopLevelNodes().filter((node) => {
            return TreeUtil.isTransformer(node)
            && conversionExpression.transformerInvocation.getName().getValue() === node.getName().getValue();
        });

        if (transformerDef.length > 0) {
            parameters.push({
                name: conversionExpression.getID() + ':0',
                displayName: transformerDef[0].source.getName().getValue(),
                type: transformerDef[0].source.getTypeNode().getTypeName().getValue(),
                index: -1,
            });
            transformerDef[0].parameters.forEach((param, index) => {
                parameters.push({
                    name: conversionExpression.getID() + ':' + (index + 1),
                    displayName: param.getName().getValue(),
                    type: param.getTypeNode().typeKind,
                    index,
                    funcInv: conversionExpression.transformerInvocation,
                });
            });
            transformerDef[0].returnParameters.forEach((param) => {
                returnParams.push({
                    name: conversionExpression.getID() + ':0:return',
                    displayName: param.getName().getValue(),
                    type: param.getTypeNode().getTypeName().getValue(),
                    index: 0,
                });
            });
        }

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
     * @memberof TransformerNodeManager
     */
    getStructDefinition(packageIdentifier, structName) {
        let pkg;
        if (!packageIdentifier) {
            // check both undefined and null
            pkg = this._environment.getBuiltInCurrentPackage();
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

    getStructType(name, typeName, structDefinition, currentRoot, pathFromRoot = []) {
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
            const currentPathFromRoot = [...pathFromRoot];
            currentPathFromRoot.push(field.getName());

            // TODO: handle recursive structs in a better manner.
            // Currently we will consider it as a primitive field.
            if (field.getType() === typeName) {
                property.name = fieldName;
                property.type = field.getType();
                property.packageName = field.getPackageName();
                property.structName = name;
            } else {
                const innerStruct = this.getStructDefinition(field.getPackageName(), field.getType());
                if (!_.isUndefined(innerStruct) && typeName !== property.type) {
                    property = this.getStructType(fieldName, field.getType(), innerStruct, root, currentPathFromRoot);
                } else {
                    property.name = fieldName;
                    property.type = field.getType();
                    property.packageName = field.getPackageName();
                    property.structName = name;
                }
            }

            property.pathFromRoot = currentPathFromRoot;
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
     * @memberof TransformerNodeManager
     */
    getResolvedExpression(expression, statement) {
        const mapExp = this._mapper.getMappableExpression(expression);
        if (TreeUtil.isSimpleVariableRef(mapExp)
            && this._mapper.isTempVariable(mapExp, statement)) {
            return {
                exp: this._mapper.getMappableExpression(this._mapper.getTempResolvedExpression(mapExp)),
                isTemp: true,
            };
        }
        return { exp: mapExp, isTemp: false };
    }


    /**
     * Add given operator default statement to transformer
     * @param {[type]} child [description]
     */
    addDefaultOperator(funcArg) {
        this._transformStmt.getBody().addStatements(funcArg.callback(funcArg.args));
    }

    /**
     * Add a new variable in transform scope or outer scope
     * @param {target|source} type type of the variable
     * @returns new variable identifier
     * @memberof TransformerNodeManager
     */
    addNewVariable(type) {
        let variableName = 'tempVar';
        let node = this._transformStmt.getBody();
        const newVarIndex = 0;
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

        const variableDef = TransformFactory.createVariableDef(varName, 'string', '""');
        node.addStatements(variableDef, newVarIndex, false);
        return variableDef;
    }

    updateVariable(node, varName, statementString, type) {
        const variableDefinitionStatement = TransformFactory.createVariableDefFromStatement(statementString);
        const varDefNode = node.body;
        if (!variableDefinitionStatement.error) {
            const newVarName = variableDefinitionStatement.getVariableName().value;
            if (type === 'variable') {
                _.forEach(varDefNode.statements, (child) => {
                    if (TreeUtil.isVariableDef(child)
                          && child.getVariableName().value === varName) {
                        varDefNode.replaceStatements(child, variableDefinitionStatement, false);
                    } else if (TreeUtil.isAssignment(child)
                                  && TreeUtil.isSimpleVariableRef(child.expression)
                                  && child.expression.getVariableName().value === varName) {
                        const variableReferenceExpression = TransformFactory.createVariableRefExpression(newVarName);
                        child.setExpression(variableReferenceExpression);
                    }
                });
                node.trigger('tree-modified', {
                    origin: node,
                    type: 'variable-update',
                    title: `Variable update ${varName}`,
                    data: {},
                });
                return true;
            } if (type === 'param') {
                node.getParameters().forEach((param) => {
                    if (param.getName().getValue() === varName) {
                        node.replaceParameters(param, variableDefinitionStatement.getVariable(), false);
                    }
                });
                _.forEach(varDefNode.statements, (child) => {
                    if (TreeUtil.isAssignment(child)
                                  && TreeUtil.isSimpleVariableRef(child.expression)
                                  && child.expression.getVariableName().value === varName) {
                        const variableReferenceExpression = TransformFactory.createVariableRefExpression(newVarName);
                        child.setExpression(variableReferenceExpression);
                    }
                });
                node.trigger('tree-modified', {
                    origin: node,
                    type: 'variable-update',
                    title: `Variable update ${varName}`,
                    data: {},
                });
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Generate iterable operation to the given connection
     * @param {*} connection where iterable operation should be added
     * @param {*} type iterable operation type
     * @param {*} isLamda is given iterable operation has a lambda function
     */
    addIterableOperator(connection, type, isLamda) {
        this._mapper.addIterableOperator(connection, type, isLamda);
    }

    /**
     * Checks given expression is a transformer conversion
     * @param {Expression} nodeExpression expression needs to be checked
     * @returns {boolean} is a transformer conversion
     */
    isTransformerConversion(nodeExpression) {
        return TreeUtil.isTypeConversionExpr(nodeExpression) && nodeExpression.transformerInvocation;
    }

    /**
     * Provide compatible transformers list for given source target types
     * @param {string} sourceType conversion source type
     * @param {string} targetType conversion target type
     * @returns {[Object]} compatible transformers list
     */
    getCompatibleTransformers(sourceType, targetType) {
        return this._transformStmt.parent.getTopLevelNodes()
            .filter((node) => {
                return TreeUtil.isTransformer(node)
                        && node.getSource().getTypeNode().getTypeName().getValue() === sourceType
                        && node.getReturnParameters()[0].getTypeNode().getTypeName().getValue() === targetType;
            });
    }
 }

export default TransformerNodeManager;
