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
import BallerinaASTFactory from '../../../../ast/ast-factory';
import DefaultBallerinaASTFactory from '../../../../ast/default-ast-factory';

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

    setTransformStmt(transformStmt) {
        this._transformStmt = transformStmt;
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

    getVertexExpression(name, isField) {
        let expression;
        if (isField) {
            expression = BallerinaASTFactory.createFieldBasedVarRefExpression(name);
        } else {
            expression = BallerinaASTFactory.createSimpleVariableReferenceExpression(name);
        }

        expression.setExpressionFromString(name);
        return expression;
    }

    createStatementEdge(connection) {
        const { source, target } = connection;
        let sourceExpression;
        let targetExpression;
        const compatibility = this.getCompatibility(source.type, target.type);

        if (source.endpointKind === 'input') {
            sourceExpression = this.getCompatibleSourceExpression(
                this.getVertexExpression(source.name, source.isField), compatibility.type, target.type)
        }
        if (target.endpointKind === 'output') {
            targetExpression = this.getVertexExpression(target.name, target.isField);
        }

        if (source.endpointKind === 'input' && target.endpointKind === 'output') {
            // Connection is from source struct to target struct.
            const assignmentStmt = BallerinaASTFactory.createAssignmentStatement();
            const varRefList = BallerinaASTFactory.createVariableReferenceList();
            varRefList.addChild(targetExpression);
            assignmentStmt.addChild(varRefList, 0);

            assignmentStmt.addChild(sourceExpression, 1);

            if (!compatibility.safe) {
                const errorVarRef = DefaultBallerinaASTFactory.createIgnoreErrorVariableReference();
                varRefList.addChild(errorVarRef);
            }
            this._transformStmt.addChild(assignmentStmt);
            return;
        }

        if (source.endpointKind === 'input') {
            // Connection source is a struct and target is not a struct.
            // Target could be a function node.
            const funcNode = target.funcInv;
            const index = target.index;

            let refType = BallerinaASTFactory.createReferenceTypeInitExpression();
            if (target.isField &&
                BallerinaASTFactory.isReferenceTypeInitExpression(funcNode.children[index])) {
                refType = funcNode.children[index];
            }
            funcNode.removeChild(funcNode.children[index], true);
            // check function parameter is a struct and mapping is a complex mapping
            if (target.isField && _.find(this.state.vertices, (struct) => {
                return struct.typeName === this.getFunctionVertices(funcNode).parameters[index].type;
            })) {
                const keyValEx = BallerinaASTFactory.createKeyValueExpression();
                const nameVarRefExpression = BallerinaASTFactory.createSimpleVariableReferenceExpression();

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
                // unsafe cast/conversion
                const tempVarName = this.getNextTempVarName();
                this.insertExplicitAssignmentStatement(tempVarName, sourceExpression, this._transformStmt.getIndexOfChild(assignmentStmt) - 1);

                const tempVarRefExpr = BallerinaASTFactory.createSimpleVariableReferenceExpression();
                tempVarRefExpr.setExpressionFromString(tempVarName);
                funcNode.addChild(tempVarRefExpr, index);
            }
            return;
        }

        if (target.endpointKind === 'output') {
            // Connection source is not a struct and target is a struct.
            // Source is a function node.
            const assignmentStmtSource = this.getParentAssignmentStmt(source.funcInv);
            const rightExpression = this.getCompatibleSourceExpression(source.funcInv, compatibility.type, target.type);
            assignmentStmtSource.removeChild(source.funcInv, true);
            assignmentStmtSource.addChild(rightExpression, 1, true);
            assignmentStmtSource.setIsDeclaredWithVar(false);

            const lexpr = assignmentStmtSource.getLeftExpression();
            lexpr.removeChild(lexpr.getChildren()[source.index], true);
            lexpr.addChild(targetExpression, source.index);

            if (!compatibility.safe) {
                const errorVarRef = DefaultBallerinaASTFactory.createIgnoreErrorVariableReference();
                lexpr.addChild(errorVarRef);
            }
            return;
        }

        // Connection source and target are not structs
        // Source and target are function nodes.

        // target reference might be function invocation expression or assignment statement
        // based on how the nested invocation is drawn. i.e. : adding two function nodes and then drawing
        // will be different from removing a param from a function and then drawing the connection
        // to the parent function invocation.
        const assignmentStmtSource = this.getParentAssignmentStmt(source.funcInv);

        // remove the source assignment statement since it is now included in the target assignment statement.
        this._transformStmt.removeChild(assignmentStmtSource, true);

        const currentChild = target.funcInv.getChildren()[target.index];
        if(currentChild) {
            target.funcInv.removeChild(currentChild, true);
        }

        target.funcInv.addChild(source.funcInv, target.index);
    }

    insertExplicitAssignmentStatement(tempVarName, sourceExpression, index) {
        const tempVarAssignmentStmt = BallerinaASTFactory.createAssignmentStatement();
        const varRefList = BallerinaASTFactory.createVariableReferenceList();
        varRefList.setExpressionFromString(tempVarName);
        tempVarAssignmentStmt.addChild(varRefList, 0);
        tempVarAssignmentStmt.addChild(sourceExpression, 1);
        tempVarAssignmentStmt.setIsDeclaredWithVar(true);
        const errorVarRef = DefaultBallerinaASTFactory.createIgnoreErrorVariableReference();
        varRefList.addChild(errorVarRef);
        this._transformStmt.addChild(tempVarAssignmentStmt, index);
    }

    removeStatementEdge(connection) {
        const { source, target } = connection;
        if (source.endpointKind === 'input' && target.endpointKind === 'output') {
            const assignmentStmt = _.find(this._transformStmt.getChildren(), (child) => {
              if(!BallerinaASTFactory.isVariableDefinitionStatement(child)) {
                    return child.getLeftExpression().getChildren().find((leftExpression) => {
                        const leftExpressionStr = leftExpression.getExpressionString().trim();
                        const rightExpressionStr = this.getMappingExpression(
                            child.getRightExpression()).getExpressionString().trim();
                        return (leftExpressionStr === target.name) && (rightExpressionStr === source.name);
                    });
              }
            });
            this._transformStmt.removeChild(assignmentStmt);
            return;
        }

        if (source.endpointKind === 'input') {
            // Connection source is a struct and target is a function.
            // get the function invocation expression for nested and single cases.
            const funcInvocationExpression = target.funcInv;
            const expression = _.find(funcInvocationExpression.getChildren(), (child) => {
                return (this.getMappingExpression(child).getExpressionString().trim() === source.name);
            });
            const index = funcInvocationExpression.getIndexOfChild(expression);
            funcInvocationExpression.removeChild(expression, true);
            funcInvocationExpression.addChild(BallerinaASTFactory.createNullLiteralExpression(), index, true);
            this._transformStmt.trigger('tree-modified', {
                origin: this,
                type: 'function-connection-removed',
                title: `Remove ${source.name}`,
                data: {},
            });
            return;
        }

        if (target.endpointKind === 'output') {
            // Connection target is not a struct and source is a struct.
            // Target could be a function node.
            const assignmentStmtSource = this.getParentAssignmentStmt(source.funcInv);
            const expression = _.find(assignmentStmtSource.getLeftExpression().getChildren(), (child) => {
                return (child.getExpressionString().trim() === target.name);
            });
            assignmentStmtSource.getLeftExpression().removeChild(expression, true);
            assignmentStmtSource.setIsDeclaredWithVar(true);
            const simpleVarRefExpression = BallerinaASTFactory.createSimpleVariableReferenceExpression();
            simpleVarRefExpression.setExpressionFromString('__temp' + (source.index + 1));
            assignmentStmtSource.getLeftExpression().addChild(simpleVarRefExpression, source.index + 1);
            return;
        }

        // Connection source and target are not structs
        // Source and target could be function nodes.
        const assignmentStmt = this.findEnclosingAssignmentStatement(target.funcInv);
        const newAssignIndex = this._transformStmt.getIndexOfChild(assignmentStmt);

        const index = target.funcInv.getIndexOfChild(source.funcInv);
        target.funcInv.removeChild(source.funcInv, true);
        target.funcInv.addChild(BallerinaASTFactory.createNullLiteralExpression(), index, true);

        const newAssignmentStmt = DefaultBallerinaASTFactory
            .createTransformAssignmentFunctionInvocationStatement({ funcInv: source.funcInv });
        this._transformStmt.addChild(newAssignmentStmt, newAssignIndex);
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

    createTypeConversionExpression(type, vertexExpression) {
        const typeConversionExp = BallerinaASTFactory.createTypeConversionExpression();
        typeConversionExp.addChild(vertexExpression);
        typeConversionExp.setTargetType(type);
        return typeConversionExp;
    }

    createTypeCastExpression(type, vertexExpression) {
        const typeCastExp = BallerinaASTFactory.createTypeCastExpression();
        typeCastExp.addChild(vertexExpression);
        typeCastExp.setTargetType(type);
        return typeCastExp;
    }

    getMappingExpression(expression) {
        if (BallerinaASTFactory.isFieldBasedVarRefExpression(expression)) {
            return expression;
        }
        if (BallerinaASTFactory.isSimpleVariableReferenceExpression(expression)) {
            if (expression.getVariableName().startsWith('__temp')) {
                const assignmentStmt = this.findAssignedVertexForTemp(expression);
                if (assignmentStmt) {
                    return assignmentStmt.getRightExpression();
                }
                return expression;
            }
            return expression;
        }
        if (BallerinaASTFactory.isTypeConversionExpression(expression) ||
                BallerinaASTFactory.isTypeCastExpression(expression)) {
            return expression.getRightExpression();
        }
        return expression;
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

    addNewVariable() {
        const varNameRegex = new RegExp('tempVar[\\d]*');
        const varDefStmts = this._transformStmt.filterChildren(BallerinaASTFactory.isVariableDefinitionStatement);
        const tempVarIdentifiers = varDefStmts.filter((varDef) => {
            return varNameRegex.test(varDef.getIdentifier());
        }).map((varDef) => {
            return varDef.getIdentifier();
        }).sort();

        let index = 0;
        if (tempVarIdentifiers.length > 0) {
            index = Number.parseInt(tempVarIdentifiers[tempVarIdentifiers.length - 1].substring(7), 10) + 1;
        }
        const varName = 'tempVar' + index;
        const variableDefinitionStatement = BallerinaASTFactory.createVariableDefinitionStatement();
        variableDefinitionStatement.setStatementFromString('string ' + varName + ' = ""');
        this._transformStmt.addChild(variableDefinitionStatement, 0);
        return variableDefinitionStatement;
    }

    getCompatibleSourceExpression(sourceExpression, compatibilityType, targetType) {
        switch (compatibilityType) {
            case 'explicit' : {
                return this.createTypeCastExpression(targetType, sourceExpression);
            }
            case 'conversion' : {
                return this.createTypeConversionExpression(targetType, sourceExpression);
            }
            case 'implicit' :
            default :
                return sourceExpression;
        }
    }

    updateVariable(node, varName, statementString) {
        const variableDefinitionStatement = BallerinaASTFactory.createVariableDefinitionStatement();
        variableDefinitionStatement.setStatementFromString(statementString);
        if(variableDefinitionStatement.children.length > 0) {
          const newVarName = variableDefinitionStatement.getVariableDef().getName();
              _.forEach(node.getChildren(), (child) => {
                  if(BallerinaASTFactory.isVariableDefinitionStatement(child)
                      && child.getLeftExpression().getVariableName() == varName) {
                     const index = node.getIndexOfChild(child);
                     node.removeChild(child, true);
                     node.addChild(variableDefinitionStatement, index, true);
                  } else if(BallerinaASTFactory.isAssignmentStatement(child)
                              && BallerinaASTFactory.isSimpleVariableReferenceExpression(child.getRightExpression())
                              && child.getRightExpression().getVariableName() == varName) {
                      child.removeChild(child.children[1], true);
                      const variableReferenceExpression = BallerinaASTFactory.createSimpleVariableReferenceExpression();
                      variableReferenceExpression.setExpressionFromString(newVarName);
                      child.addChild(variableReferenceExpression, 1, true);
                  }
              });
              const inputs  = node.getInput();
              _.forEach(node.getInput(), (input, i) => {
                  if(input.getVariableName() == varName) {
                    const variableReferenceExpression = BallerinaASTFactory.createSimpleVariableReferenceExpression();
                    variableReferenceExpression.setExpressionFromString(newVarName);
                    inputs[i] = variableReferenceExpression;
                }
            });
            node.setInput(inputs);
            node.trigger('tree-modified', {
                origin: this,
                type: 'variable-update',
                title: `Variable update ${varName}`,
                data: {},
            });
        }
    }

    getNextTempVarName() {
        const varNameRegex = new RegExp('__temp[\\d]*');
        const assignmentStmts = this._transformStmt.filterChildren(BallerinaASTFactory.isAssignmentStatement);
        const tempVarIdentifiers = [];
        assignmentStmts.forEach((assStmt) => {
            assStmt.getLeftExpression().getChildren().forEach((leftExpr) => {
                if (varNameRegex.test(leftExpr.getExpressionString())) {
                    tempVarIdentifiers.push(leftExpr.getExpressionString());
                }
            });
        });
        tempVarIdentifiers.sort();

        let index = 1;
        if (tempVarIdentifiers.length > 0) {
            index = Number.parseInt(tempVarIdentifiers[tempVarIdentifiers.length - 1].substring(6), 10) + 1;
        }
        return '__temp' + index;
    }
 }

export default TransformNodeManager;
