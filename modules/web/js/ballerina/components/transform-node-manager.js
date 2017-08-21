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
import BallerinaASTFactory from '../ast/ast-factory';
import DefaultBallerinaASTFactory from '../ast/default-ast-factory';

/**
 * Transform node managing class
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
    }

    /**
     * Get compatibility of given source and target types
     * @param {any} source source type
     * @param {any} target target type
     */
    getCompatibility(source, target) {
        const compatibility = this._typeLattice.getCompatibility(source, target);
        if (!compatibility || (source === target)) {
            return { safe: true, type: 'implicit' };
        }
        return compatibility;
    }

    getVertexExpression(name, property, isStruct) {
        if (!isStruct || !property) {
            const simpleVarRefExpression = BallerinaASTFactory.createSimpleVariableReferenceExpression();
            simpleVarRefExpression.setExpressionFromString(name);
            return simpleVarRefExpression;
        } else {
            const fieldVarRefExpression = BallerinaASTFactory.createFieldBasedVarRefExpression();
            fieldVarRefExpression.setExpressionFromString(property);
            return fieldVarRefExpression;
        }
    }

    createStatementEdge(source, target, connection) {
        let sourceExpression;
        let targetExpression;

        if (source !== undefined) {
            sourceExpression = this.getVertexExpression(
                connection.sourceStruct, connection.sourceProperty,
                      ((source.type === 'struct') || (source.type.startsWith('json'))));
        }
        if (target !== undefined) {
            targetExpression = this.getVertexExpression(
                connection.targetStruct, connection.targetProperty,
                      ((target.type === 'struct') || (target.type.startsWith('json'))));
        }

        if (!_.isUndefined(source) && !connection.isSourceFunction &&
            !_.isUndefined(target) && !connection.isTargetFunction) {
            // Connection is from source struct to target struct.
            const assignmentStmt = BallerinaASTFactory.createAssignmentStatement();
            const varRefList = BallerinaASTFactory.createVariableReferenceList();
            varRefList.addChild(targetExpression);
            assignmentStmt.addChild(varRefList, 0);

            const compatibility = this.getCompatibility(connection.sourceType, connection.targetType);

            switch (compatibility.type) {
                case 'explicit' : {
                    const typeCastingExp = this.createTypeCastExpression(
                        connection.targetType, sourceExpression);
                    assignmentStmt.addChild(typeCastingExp, 1);
                    break;
                }
                case 'conversion' : {
                    const typeConversionExp = this.createTypeConversionExpression(
                        connection.targetType, sourceExpression);
                    assignmentStmt.addChild(typeConversionExp, 1);
                    break;
                }
                case 'implicit' :
                default :
                    assignmentStmt.addChild(sourceExpression, 1);
                    break;
            }
            if (!compatibility.safe) {
                const errorVarRef = DefaultBallerinaASTFactory.createIgnoreErrorVariableReference();
                varRefList.addChild(errorVarRef);
            }
            this._transformStmt.addChild(assignmentStmt);
            return;
        }

        if (!_.isUndefined(source) && connection.isTargetFunction) {
            // Connection source is a struct and target is not a struct.
            // Target could be a function node.
            const funcNode = connection.targetFuncInv;
            const index = _.findIndex(this.getFunctionDefinition(connection.targetFuncInv).getParameters(), param => {
                return param.name === connection.targetStruct;
            });

            let refType = BallerinaASTFactory.createReferenceTypeInitExpression();
            if (connection.targetProperty &&
                BallerinaASTFactory.isReferenceTypeInitExpression(funcNode.children[index])) {
                refType = funcNode.children[index];
            }
            funcNode.removeChild(funcNode.children[index], true);
            // check function parameter is a struct and mapping is a complex mapping
            if (connection.targetProperty && _.find(this.state.vertices, (struct) => {
                return struct.typeName === this.getFunctionDefinition(funcNode).getParameters()[index].type;
            })) {
                const keyValEx = BallerinaASTFactory.createKeyValueExpression();
                const nameVarRefExpression = BallerinaASTFactory.createSimpleVariableReferenceExpression();

                const propChain = connection.targetProperty.split('.').splice(1);

                nameVarRefExpression.setExpressionFromString(propChain[0]);
                keyValEx.addChild(nameVarRefExpression);
                keyValEx.addChild(sourceExpression);
                refType.addChild(keyValEx);
                funcNode.addChild(refType, index);
            } else {
                funcNode.addChild(sourceExpression, index);
            }
            return;
        }

        if (connection.isSourceFunction && !_.isUndefined(target)) {
            // Connection source is not a struct and target is a struct.
            // Source is a function node.
            const assignmentStmtSource = this.getParentAssignmentStmt(connection.sourceFuncInv);
            assignmentStmtSource.setIsDeclaredWithVar(false);

            const lexpr = assignmentStmtSource.getLeftExpression();
            lexpr.removeChild(lexpr.getChildren()[connection.sourceIndex], true);
            lexpr.addChild(targetExpression, connection.sourceIndex);
            return;
        }

        // Connection source and target are not structs
        // Source and target are function nodes.

        // target reference might be function invocation expression or assignment statement
        // based on how the nested invocation is drawn. i.e. : adding two function nodes and then drawing
        // will be different from removing a param from a function and then drawing the connection
        // to the parent function invocation.
        const assignmentStmtTarget = this.getParentAssignmentStmt(connection.targetFuncInv);
        const assignmentStmtSource = this.getParentAssignmentStmt(connection.sourceFuncInv);

        const funcNode = assignmentStmtTarget.getRightExpression();

        const index = _.findIndex(this.getFunctionDefinition(funcNode).getParameters(), (param) => {
            return param.name === (connection.targetProperty || connection.targetStruct);
        });

        // remove the source assignment statement since it is now included in the target assignment statement.
        this._transformStmt.removeChild(assignmentStmtSource, true);
        funcNode.addChild(assignmentStmtSource.getRightExpression(), index);
    }

    removeStatementEdge(source, target, connection) {
        if (!_.isUndefined(source) && !connection.isSourceFunction &&
            !_.isUndefined(target) && !connection.isTargetFunction) {
            const assignmentStmt = _.find(this._transformStmt.getChildren(), (child) => {
                return child.getLeftExpression().getChildren().find((leftExpression) => {
                    const leftExpressionStr = leftExpression.getExpressionString().trim();
                    const rightExpressionStr = this.getMappingRightExpression(child.getRightExpression()).getExpressionString().trim();
                    return leftExpressionStr === (connection.targetProperty || connection.targetStruct) &&
                        rightExpressionStr === (connection.sourceProperty || connection.sourceStruct);
                });
            });
            this._transformStmt.removeChild(assignmentStmt);
            return;
        }

        if (!_.isUndefined(source) && connection.isTargetFunction) {
            // Connection source is a struct and target is a function.
            // get the function invocation expression for nested and single cases.
            const funcInvocationExpression = connection.targetFuncInv;
            const expression = _.find(funcInvocationExpression.getChildren(), (child) => {
                return (child.getExpressionString().trim() === (connection.sourceProperty || connection.sourceStruct));
            });
            const index = funcInvocationExpression.getIndexOfChild(expression);
            funcInvocationExpression.removeChild(expression, true);
            funcInvocationExpression.addChild(BallerinaASTFactory.createNullLiteralExpression(), index, true);
            this._transformStmt.trigger('tree-modified', {
                origin: this,
                type: 'function-connection-removed',
                title: `Remove ${connection.sourceProperty}`,
                data: {},
            });
            return;
        }

        if (connection.isSourceFunction && !_.isUndefined(target)) {
            // Connection target is not a struct and source is a struct.
            // Target could be a function node.
            const assignmentStmtTarget = connection.sourceReference;
            const expression = _.find(assignmentStmtTarget.getLeftExpression().getChildren(), (child) => {
                return (child.getExpressionString().trim() === (connection.targetProperty || connection.targetStruct));
            });
            assignmentStmtTarget.getLeftExpression().removeChild(expression, true);
            assignmentStmtTarget.setIsDeclaredWithVar(true);
            const simpleVarRefExpression = BallerinaASTFactory.createSimpleVariableReferenceExpression();
            simpleVarRefExpression.setExpressionFromString('_temp' + (connection.sourceIndex + 1));
            assignmentStmtTarget.getLeftExpression().addChild(simpleVarRefExpression, connection.sourceIndex + 1);
            return;
        }

        // Connection source and target are not structs
        // Source and target could be function nodes.
        const targetFuncInvocationExpression = connection.targetFuncInv;
        const sourceFuncInvocationExpression = connection.sourceFuncInv;

        targetFuncInvocationExpression.removeChild(sourceFuncInvocationExpression, true);

        const assignmentStmt = BallerinaASTFactory.createAssignmentStatement();
        const varRefList = BallerinaASTFactory.createVariableReferenceList();
        assignmentStmt.addChild(varRefList, 0);
        assignmentStmt.addChild(sourceFuncInvocationExpression, 1);
        this._transformStmt.addChild(assignmentStmt, this._transformStmt.getIndexOfChild(connection.targetReference));
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
    *
    * Gets the enclosing assignment statement.
    *
    * @param {any} expression
    * @returns {AssignmentStatement} enclosing assignment statement
    * @memberof TransformStatementDecorator
    */
    getParentAssignmentStmt(node) {
        if (BallerinaASTFactory.isAssignmentStatement(node)) {
            return node;
        } else {
            return this.getParentAssignmentStmt(node.getParent());
        }
    }

    /**
     * @param {any} UUID of a function invocation statement
     * @returns {AssignmentStatement} enclosing assignment statement containing the matching function
     * invocation statement ID
     *
     * @memberof TransformStatementDecorator
     */
    findEnclosingAssignmentStatement(id) {
        const assignmentStmts = this.props.model.getChildren();
        const assignmentStmt = this.transformNodeManager.findExistingAssignmentStatement(id);
        if (assignmentStmt === undefined) {
            return _.find(assignmentStmts, (assignmentStmt) => {
                const expression = this.findFunctionInvocationById(assignmentStmt, id);
                if (expression !== undefined) {
                    return assignmentStmt;
                }
            });
        } else {
            return assignmentStmt;
        }
    }

    findFunctionInvocationById(expression, id) {
        let found = expression.getChildById(id);
        if (found !== undefined) {
            return found;
        } else {
            _.forEach(expression.getChildren(), (child) => {
                found = this.findFunctionInvocationById(child, id);
                if (found !== undefined) {
                    return found;
                }
            });
            return found;
        }
    }

    getFunctionDefinition(functionInvocationExpression) {
        const funPackage = this._environment.getPackageByName(functionInvocationExpression.getFullPackageName());
        const funcDef = funPackage.getFunctionDefinitionByName(functionInvocationExpression.getFunctionName());
        _.forEach(funcDef.getParameters(), (param) => {
            const structDef = this.getStructDefinition(param.packageName, param.type);
            if (structDef) {
                param.typeDef = this.getStructType(param.name, param.type, structDef);
            }
        });
        return funcDef;
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

    getStructType(name, typeName, structDefinition, isInner, fieldName) {
        const struct = {};

        struct.name = name;
        struct.properties = [];
        struct.type = 'struct';
        struct.typeName = typeName;
        struct.isInner = isInner;

        _.forEach(structDefinition.getFields(), (field) => {
            const property = {};
            property.name = field.getName();
            property.type = field.getType();
            property.packageName = field.getPackageName();
            property.structName = name;
            property.fieldName = (fieldName || name) + `.${property.name}`;

            const innerStruct = this.getStructDefinition(property.packageName, property.type);
            if (!_.isUndefined(innerStruct) && typeName !== property.type) {
                property.innerType = this.getStructType(property.name, property.type,
                    innerStruct, true, property.fieldName);
            }

            struct.properties.push(property);
        });
        return struct;
    }

    createTypeConversionExpression(type, vertexExpression) {
        const typeConversionExp = BallerinaASTFactory.createTypeConversionExpression();
        typeConversionExp.setExpressionFromString('<' + type + '>' + vertexExpression.getExpressionString());
        return typeConversionExp;
    }

    createTypeCastExpression(type, vertexExpression) {
        const typeConversionExp = BallerinaASTFactory.createTypeCastExpression();
        typeConversionExp.setExpressionFromString('(' + type + ')' + vertexExpression.getExpressionString());
        return typeConversionExp;
    }

    getMappingRightExpression(expression) {
        if (BallerinaASTFactory.isFieldBasedVarRefExpression(expression) ||
              BallerinaASTFactory.isSimpleVariableReferenceExpression(expression)) {
            return expression;
        } else if (BallerinaASTFactory.isTypeConversionExpression(expression) ||
                BallerinaASTFactory.isTypeCastExpression(expression)) {
            return expression.getRightExpression();
        }
        return expression;
    }

    addNewVariable(node) {
      let varName = "tempVar";
      const variableDefinitionStatement = BallerinaASTFactory.createVariableDefinitionStatement();
      variableDefinitionStatement.setStatementFromString('string ' + varName + ' = ""');
      node.addChild(variableDefinitionStatement);
      return variableDefinitionStatement;
    }

 }

export default TransformNodeManager;
