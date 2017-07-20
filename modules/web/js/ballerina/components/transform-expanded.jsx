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

import React from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import $ from 'jquery';
import alerts from 'alerts';
import log from 'log';
import TransformRender from '../../ballerina/components/transform-render';
import SuggestionsDropdown from './transform-endpoints-dropdown';
import BallerinaASTFactory from '../ast/ballerina-ast-factory';
import ASTNode from '../ast/node';
import DragDropManager from '../tool-palette/drag-drop-manager';

class TransformExpanded extends React.Component {
    constructor(props, context){
        super(props, context);
        this.state = {
            typedSource: '',
            typedTarget: '',
            selectedSource: '-1',
            selectedTarget: '-1',
        }
        this.predefinedStructs = [];
        this.getSourcesAndTargets();

        this.onSourceSelect = this.onSourceSelect.bind(this);
        this.onTargetSelect = this.onTargetSelect.bind(this);
        this.onSourceAdd = this.onSourceAdd.bind(this);
        this.onTargetAdd = this.onTargetAdd.bind(this);
        this.onClose = this.onClose.bind(this);
        this.onDropZoneActivate = this.onDropZoneActivate.bind(this);
        this.onDropZoneDeactivate = this.onDropZoneDeactivate.bind(this);
        this.onTransformDropZoneActivate = this.onTransformDropZoneActivate.bind(this);
        this.onTransformDropZoneDeactivate = this.onTransformDropZoneDeactivate.bind(this);
        this.onSourceInputChange = this.onSourceInputChange.bind(this);
        this.onTargetInputChange = this.onTargetInputChange.bind(this);
        this.onSourceInputEnter = this.onSourceInputEnter.bind(this);
        this.onTargetInputEnter = this.onTargetInputEnter.bind(this);
        this.addSource = this.addSource.bind(this);
        this.addTarget = this.addTarget.bind(this);
    }

    getFunctionDefinition(functionInvocationExpression) {
        const funPackage = this.context.environment.getPackageByName(functionInvocationExpression.getFullPackageName());
        let funcDef = funPackage.getFunctionDefinitionByName(functionInvocationExpression.getFunctionName());
         _.forEach(funcDef.getParameters(), (param) => {
                param.innerType = _.find(this.predefinedStructs, { typeName: param.type });
         });
        return funcDef;
    }

    onConnectionCallback(connection) {
        const self = this;
        const sourceStruct = _.find(self.predefinedStructs, { name:connection.sourceStruct});
        const targetStruct = _.find(self.predefinedStructs, { name:connection.targetStruct});
        let sourceExpression;
        let targetExpression;

        if (sourceStruct !== undefined) {
            sourceExpression = self.getStructAccessNode(
                connection.sourceStruct, connection.sourceProperty, (sourceStruct.type === 'struct'));
        }
        if (targetStruct !== undefined) {
            targetExpression = self.getStructAccessNode(
                connection.targetStruct, connection.targetProperty, (targetStruct.type === 'struct'));
        }

        if (!_.isUndefined(sourceStruct) && !_.isUndefined(targetStruct)) {
            //Connection is from source struct to target struct.
            const assignmentStmt = BallerinaASTFactory.createAssignmentStatement();
            let varRefList = BallerinaASTFactory.createVariableReferenceList();
            varRefList.addChild(targetExpression);
            assignmentStmt.addChild(varRefList, 0);
            assignmentStmt.addChild(sourceExpression, 1);
            self.props.model.addChild(assignmentStmt);
            return assignmentStmt.id;
        }

        if (!_.isUndefined(sourceStruct) && _.isUndefined(targetStruct)) {
            // Connection source is not a struct and target is a struct.
            // Source could be a function node.
            const assignmentStmtSource = self.findEnclosingAssignmentStatement(connection.targetReference.id);
            let funcNode;
            if (BallerinaASTFactory.isFunctionInvocationExpression(connection.targetReference)) {
                funcNode = connection.targetReference;
            } else {
                funcNode = connection.targetReference.getRightExpression();
            }
            let index = _.findIndex(self.getFunctionDefinition(funcNode).getParameters(),
                                                (param) => { return param.name == connection.targetProperty[0]});
            let refType = BallerinaASTFactory.createReferenceTypeInitExpression();
            if (connection.targetProperty.length > 1 && BallerinaASTFactory.isReferenceTypeInitExpression(funcNode.children[index])) {
                refType = funcNode.children[index];
            }
            funcNode.removeChild(funcNode.children[index], true);
            //check function parameter is a struct and mapping is a complex mapping
            if (connection.targetProperty.length > 1 && _.find(self.predefinedStructs, (struct) =>
                        {return struct.typeName == self.getFunctionDefinition(funcNode).getParameters()[index].type})) {
                let keyValEx = BallerinaASTFactory.createKeyValueExpression();
                let nameVarRefExpression = BallerinaASTFactory.createSimpleVariableReferenceExpression();
                nameVarRefExpression.setExpressionFromString(connection.targetProperty[1]);
            	keyValEx.addChild(nameVarRefExpression);
            	keyValEx.addChild(sourceExpression);
                refType.addChild(keyValEx);
            	funcNode.addChild(refType, index);
            } else {
                funcNode.addChild(sourceExpression, index);
            }
            return assignmentStmtSource.id;
        }

        if (_.isUndefined(sourceStruct) && !_.isUndefined(targetStruct)) {
            // Connection target is not a struct and source is a struct.
            // Target is a function node.
            const assignmentStmtTarget = self.findEnclosingAssignmentStatement(connection.sourceReference.id);
            let index = _.findIndex(self.getFunctionDefinition(
                                                    connection.sourceReference.getRightExpression()).getReturnParams(),
                                                            (param) => {
                                                                return param.name == connection.sourceProperty[0]});
            assignmentStmtTarget.getLeftExpression().addChild(targetExpression, index);
            return assignmentStmtTarget.id;
        }

        // Connection source and target are not structs
        // Source and target are function nodes.

        // target reference might be function invocation expression or assignment statement
        // based on how the nested invocation is drawn. i.e. : adding two function nodes and then drawing
        // will be different from removing a param from a function and then drawing the connection
        // to the parent function invocation.
        const assignmentStmtTarget = self.getParentAssignmentStmt(connection.targetReference);

        const assignmentStmtSource = connection.sourceReference;
        assignmentStmtTarget.getRightExpression().addChild(assignmentStmtSource.getRightExpression());
        //remove the source assignment statement since it is now included in the target assignment statement.
        const transformStmt = assignmentStmtSource.getParent();
        transformStmt.removeChild(assignmentStmtSource);

        return assignmentStmtTarget.id;
    };


    onDisconnectionCallback(connection) {
        const self = this;
        // on removing a connection
        const sourceStruct = _.find(self.predefinedStructs, { name: connection.sourceStruct });
        const targetStruct = _.find(self.predefinedStructs, { name: connection.targetStruct });

        let sourceExpression;
        let targetExpression;

        if (targetStruct !== undefined){
            sourceExpression = self.getStructAccessNode(
                connection.targetStruct, connection.targetProperty, (targetStruct.type === 'struct'));
        } else {
            sourceExpression = self.getStructAccessNode(
                connection.targetStruct, connection.targetProperty, false);
        }

        if (sourceStruct !== undefined) {
            targetExpression = self.getStructAccessNode(
                connection.sourceStruct, connection.sourceProperty, (sourceStruct.type === 'struct'));
        } else {
            targetExpression = self.getStructAccessNode(
                connection.sourceStruct, connection.sourceProperty, false);
        }

        if (!_.isUndefined(sourceStruct) && !_.isUndefined(targetStruct)) {
            const assignmentStmt = _.find(self.props.model.children, { id: connection.id });
            self.props.model.removeChild(assignmentStmt);
        } else if (!_.isUndefined(sourceStruct) && _.isUndefined(targetStruct)) {
            // Connection source is not a struct and target is a struct.
            // Source is a function node.
            const assignmentStmtSource = self.findEnclosingAssignmentStatement(connection.targetReference.id);

            // get the function invocation expression for nested and single cases.
            const funcInvocationExpression = self.findFunctionInvocationById(
                assignmentStmtSource, connection.targetReference.id);
            const expression = _.find(funcInvocationExpression.getChildren(), (child) => {
                return (child.getExpressionString().trim() === targetExpression.getExpressionString().trim());
            });
            funcInvocationExpression.removeChild(expression);
        } else if (_.isUndefined(sourceStruct) && !_.isUndefined(targetStruct)) {
            // Connection target is not a struct and source is a struct.
            // Target could be a function node.
            const assignmentStmtTarget = self.findEnclosingAssignmentStatement(connection.sourceReference.id);
            const expression = _.find(assignmentStmtTarget.getLeftExpression().getChildren(), (child) => {
                return (child.getExpressionString().trim() === sourceExpression.getExpressionString().trim());
            });
            assignmentStmtTarget.getLeftExpression().removeChild(expression);
        } else {
            // Connection source and target are not structs
            // Source and target could be function nodes.
            const targetFuncInvocationExpression = connection.targetReference;
            const sourceFuncInvocationExpression = connection.sourceReference;

            targetFuncInvocationExpression.removeChild(sourceFuncInvocationExpression);
        }
    };

    getStructAccessNode(name, property, isStruct) {
        if (!isStruct) {
            let simpleVarRefExpression = BallerinaASTFactory.createSimpleVariableReferenceExpression();
            simpleVarRefExpression.setExpressionFromString(name);
            return simpleVarRefExpression;
        } else {
            let fieldVarRefExpression = BallerinaASTFactory.createFieldBasedVarRefExpression();
            fieldVarRefExpression.setExpressionFromString(`${name}.${_.join(property, '.')}`);
            return fieldVarRefExpression;
        }
    }


    createConnection(statement) {
        if (BallerinaASTFactory.isAssignmentStatement(statement)) {
            // There can be multiple left expressions.
            // E.g. : e.name, e.username = p.first_name;
            const leftExpressions = statement.getLeftExpression();
            const rightExpression = statement.getRightExpression();

            if (BallerinaASTFactory.isFieldBasedVarRefExpression(rightExpression) ||
                  BallerinaASTFactory.isSimpleVariableReferenceExpression(rightExpression)) {
                _.forEach(leftExpressions.getChildren(), (expression) => {
                    const target = this.getConnectionProperties('target', expression);
                    const source = this.getConnectionProperties('source', rightExpression);
                    this.drawConnection(statement.getID(), source, target);
                });
            } else if (BallerinaASTFactory.isFunctionInvocationExpression(rightExpression)) {
                // draw the function nodes first to fix issues related to rendering arrows with function nodes
                // not yet drawn in nested cases. TODO : introduce a pooling mechanism to avoid this.
                this.drawFunctionDefinitionNodes(rightExpression, statement);
                this.drawFunctionInvocationExpression(leftExpressions, rightExpression, statement);
            } else {
                log.error('Invalid expression type in transform statement body');
            }
        } else if (BallerinaASTFactory.isCommentStatement(statement)) {
            //ignore comment statements
        } else {
            log.error('Invalid statement type in transform statement');
        }
    }

    drawFunctionDefinitionNodes(functionInvocationExpression, statement) {
        const func = this.getFunctionDefinition(functionInvocationExpression);
        if (_.isUndefined(func)) {
            alerts.error(
                'Function definition for "' + functionInvocationExpression.getFunctionName() + '" cannot be found');
            return;
        }

        if (func.getParameters().length !== functionInvocationExpression.getChildren().length) {
            alerts.warn('Function inputs and mapping count does not match in "' + func.getName() + '"');
        } else {
            const funcTarget = this.getConnectionProperties('target', functionInvocationExpression);
            _.forEach(functionInvocationExpression.getChildren(), (expression) => {
                if (BallerinaASTFactory.isFunctionInvocationExpression(expression)) {
                    this.drawInnerFunctionDefinitionNodes(functionInvocationExpression, expression, statement);
                }
            });
        }

        // Removing this node means removing the assignment statement from the transform statement,
        // since this is the top most invocation.
        // Hence passing the assignment statement as remove reference.
        this.mapper.addFunction(func, functionInvocationExpression,
            statement.getParent().removeChild.bind(statement.getParent()), statement);
    }

    drawInnerFunctionDefinitionNodes(parentFunctionInvocationExpression, functionInvocationExpression, statement) {
        const func = this.getFunctionDefinition(functionInvocationExpression);
            if (_.isUndefined(func)) {
                alerts.error(
                    'Function definition for "' + functionInvocationExpression.getFunctionName() + '" cannot be found');
                return;
        }

        if (func.getParameters().length !== functionInvocationExpression.getChildren().length) {
            alerts.warn('Function inputs and mapping count does not match in "' + func.getName() + '"');
        } else {
            const funcTarget = this.getConnectionProperties('target', functionInvocationExpression);
            _.forEach(functionInvocationExpression.getChildren(), (expression) => {
                if (BallerinaASTFactory.isFunctionInvocationExpression(expression)) {
                    this.drawInnerFunctionDefinitionNodes(functionInvocationExpression, expression, statement);
                }
            });
        }

        //Removing this node means removing the function invocation from the parent function invocation.
        //Hence passing the current function invocation as remove reference.
        this.mapper.addFunction(
            func, functionInvocationExpression,
            parentFunctionInvocationExpression.removeChild.bind(parentFunctionInvocationExpression),
            functionInvocationExpression);
    }

    drawInnerFunctionInvocationExpression(parentFunctionInvocationExpression, functionInvocationExpression,
                                                      parentFunctionDefinition, parentParameterIndex, statement) {
        const func = this.getFunctionDefinition(functionInvocationExpression);
        if (_.isUndefined(func)) {
            alerts.error(
                'Function definition for "' + functionInvocationExpression.getFunctionName() + '" cannot be found');
            return;
        }

        if (func.getParameters().length !== functionInvocationExpression.getChildren().length) {
            alerts.warn('Function inputs and mapping count does not match in "' + func.getName() + '"');
        } else {
            const funcTarget = this.getConnectionProperties('target', functionInvocationExpression);
            _.forEach(functionInvocationExpression.getChildren(), (expression, i) => {
                if (BallerinaASTFactory.isFunctionInvocationExpression(expression)) {
                    this.drawInnerFunctionInvocationExpression(
                        functionInvocationExpression, expression, func, i, statement);
                } else {
                    const target = this.getConnectionProperties('target', func.getParameters()[i]);
                    _.merge(target, funcTarget); // merge parameter props with function props
                    const source = this.getConnectionProperties('source', expression);
                    this.drawConnection(statement.getID() + functionInvocationExpression.getID(), source, target);
                }
            });
        }

        if (parent !== undefined) {
            const funcSource = this.getConnectionProperties('source', functionInvocationExpression);
            const funcSourceParam = this.getConnectionProperties('source', func.getReturnParams()[0]);
            _.merge(funcSource, funcSourceParam); // merge parameter props with function props

            const funcTarget = this.getConnectionProperties('target', parentFunctionInvocationExpression);
            const funcTargetParam = this.getConnectionProperties(
                'target', parentFunctionDefinition.getParameters()[parentParameterIndex]);
            _.merge(funcTarget, funcTargetParam); // merge parameter props with function props

            this.drawConnection(statement.getID() + functionInvocationExpression.getID(), funcSource, funcTarget);
        }

        //TODO : draw function node here when connection pooling for nested functions is implemented.
    }

    drawFunctionInvocationExpression(argumentExpressions, functionInvocationExpression, statement) {
        const func = this.getFunctionDefinition(functionInvocationExpression);
        if (_.isUndefined(func)) {
            alerts.error(
                'Function definition for "' + functionInvocationExpression.getFunctionName() + '" cannot be found');
            return;
        }
        if (func.getParameters().length !== functionInvocationExpression.getChildren().length) {
            alerts.warn('Function inputs and mapping count does not match in "' + func.getName() + '"');
        } else {
            const funcTarget = this.getConnectionProperties('target', functionInvocationExpression);
            _.forEach(functionInvocationExpression.getChildren(), (expression, i) => {
                if (BallerinaASTFactory.isFunctionInvocationExpression(expression)) {
                    this.drawInnerFunctionInvocationExpression(
                        functionInvocationExpression, expression, func, i, statement);
                } else {
                    let target;
                    let source;
                    if (BallerinaASTFactory.isKeyValueExpression(expression.children[0])) {
                    //if parameter is a key value expression, iterate each expression and draw connections
                    _.forEach(expression.children, (propParam) => {
                        source = this.getConnectionProperties('source', propParam.children[1]);
                        target = this.getConnectionProperties('target', func.getParameters()[i]);
                        _.merge(target, funcTarget); // merge parameter props with function props
                        target.targetProperty.push(propParam.children[0].getVariableName());
                        let typeDef = _.find(this.predefinedStructs, { typeName: func.getParameters()[i].type});
                        let propType = _.find(typeDef.properties, { name: propParam.children[0].getVariableName()});
                        target.targetType.push(propType.type);
                        this.drawConnection(statement.getID() + functionInvocationExpression.getID(), source, target);
                    });
                    } else {
                       target = this.getConnectionProperties('target', func.getParameters()[i]);
                       _.merge(target, funcTarget); // merge parameter props with function props
                       source = this.getConnectionProperties('source', expression);
                        this.drawConnection(statement.getID() + functionInvocationExpression.getID(), source, target);
                    }
                }
            });
        }

        if (func.getReturnParams().length !== argumentExpressions.getChildren().length) {
            alerts.warn('Function inputs and mapping count does not match in "' + func.getName() + '"');
        } else {
            const funcSource = this.getConnectionProperties('source', functionInvocationExpression);
            _.forEach(argumentExpressions.getChildren(), (expression, i) => {
                const source = this.getConnectionProperties('source', func.getReturnParams()[i]);
                _.merge(source, funcSource); // merge parameter props with function props
                const target = this.getConnectionProperties('target', expression);
                this.drawConnection(statement.getID() + functionInvocationExpression.getID(), source, target);
            });
        }

        //TODO : draw function node here when connection pooling for nested functions is implemented.
    }

    getConnectionProperties(type, expression) {
        const con = {};
        if (BallerinaASTFactory.isFieldBasedVarRefExpression(expression)) {
            const structVarRef = expression.getStructVariableReference();
            con[type + 'Struct'] = structVarRef.getVariableName();
            const complexProp = this.createComplexProp(con[type + 'Struct'], structVarRef.getParent());
            con[type + 'Type'] = complexProp.types;
            con[type + 'Property'] = complexProp.names;
        } else if (BallerinaASTFactory.isFunctionInvocationExpression(expression)) {
            con[type + 'Function'] = true;
            if (_.isNull(expression.getPackageName())) {
                // for current package, where package name is null
                const packageName = expression.getFullPackageName().replace(' ', '');
                con[type + 'Struct'] = packageName + '-' + expression.getFunctionName();
            } else {
                const packageName = expression.getPackageName().replace(' ', '');
                con[type + 'Struct'] = packageName + '-' + expression.getFunctionName();
            }
            con[type + 'Id'] = expression.getID();
        } else if (BallerinaASTFactory.isSimpleVariableReferenceExpression(expression)) {
            con[type + 'Struct'] = expression.getVariableName();
            const varRef = _.find(this.predefinedStructs, { name: expression.getVariableName() });
            if (!_.isUndefined(varRef)) {
                con[type + 'Type'] = [varRef.type];
            }
            con[type + 'Property'] = [expression.getVariableName()];
        } else if (['name', 'type'].every(prop => prop in expression)) {
            con[type + 'Property'] = [expression.name];
            con[type + 'Type'] = [expression.type];
        } else if (_.has(expression, 'type')) {
            con[type + 'Property'] = [undefined];
            con[type + 'Type'] = [expression.type];
        } else {
            log.error('Unknown type to define connection properties');
        }
        return con;
    }

    drawConnection(id, source, target) {
        const con = { id: id };
        _.merge(con, source, target);
        this.mapper.addConnection(con);
    }

    /**
     * @param {any} UUID of an assignment statement
     * @returns {AssignmentStatement} assignment statement for maching ID
     *
     * @memberof TransformStatementDecorator
     */
    findExistingAssignmentStatement(id) {
        return _.find(this.props.model.getChildren(), (child) => {
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
        if (BallerinaASTFactory.isAssignmentStatement(node)){
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
        let assignmentStmts = this.props.model.getChildren();
        let assignmentStmt = this.findExistingAssignmentStatement(id);
        if (assignmentStmt === undefined) {
            return _.find(assignmentStmts, (assignmentStmt) => {
                let expression = this.findFunctionInvocationById(assignmentStmt, id);
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

    createComplexProp(structName, expression)    {
        let prop = {};
        prop.names = [];
        prop.types = [];

        if (BallerinaASTFactory.isFieldBasedVarRefExpression(expression)) {
            let fieldName = expression.getFieldName();
            const structDef = _.find(this.predefinedStructs, { name: structName });
            if (_.isUndefined(structDef)) {
                alerts.error('Struct definition for variable "' + structName + '" cannot be found');
                return;
            }
            const structField = _.find(structDef.properties, { name: fieldName });
            if (_.isUndefined(structField)) {
                alerts.error('Struct field "' + fieldName + '" cannot be found in variable "' + structName + '"');
                return;
            }
            const structFieldType = structField.type;
            prop.types.push(structFieldType);
            prop.names.push(fieldName);

            let parentProp = this.createComplexProp(fieldName, expression.getParent());
            prop.names = [...prop.names, ...parentProp.names];
            prop.types = [...prop.types, ...parentProp.types];
        }
        return prop;
    }

    getStructDefinition(packageIdentifier, structName) {
        let _package;
        if (packageIdentifier !== undefined) {
            _package = this.context.environment.getPackageByIdentifier(packageIdentifier);
        } else {
            _package = this.context.environment.getCurrentPackage();
        }

        if (_package === undefined) {
            alerts.error('Referred package ' + packageIdentifier + ' cannot be resolved');
            return;
        }

        return _.find(_package.getStructDefinitions(), (structDef) => {
            return structName === structDef.getName();
        });
    }

    createType(name, typeName, predefinedStruct, isInner) {
        let struct = {};
        struct.name = name;
        struct.properties = [];
        struct.type = 'struct';
        struct.typeName = typeName;
        struct.isInner = isInner

        _.forEach(predefinedStruct.getFields(), (field) => {
            let property = {};
            property.name = field.getName();
            property.type = field.getType();
            property.packageName = field.getPackageName();

            let innerStruct = this.getStructDefinition(property.packageName, property.type);
            if (innerStruct != null) {
                property.innerType = this.createType(property.name, typeName, innerStruct, true);
            }

            struct.properties.push(property);
        });
        this.predefinedStructs.push(struct);
        return struct;
    }

    componentDidUpdate(prevProps) {
        if(this.props.model === prevProps.model) {
            return;
        }

        const nextProps = this.props;
        this.mapper.disconnectAll();

        this.predefinedStructs = [];
        this.getSourcesAndTargets();

        this.mapper = new TransformRender(
            this.onConnectionCallback.bind(this), this.onDisconnectionCallback.bind(this));

        _.forEach(nextProps.model.getInput(), (input) => {
            //trim expression to remove any possible white spaces
            this.setSource(input.getExpressionString().trim(), this.predefinedStructs);
        });

        _.forEach(nextProps.model.getOutput(), (output) => {
            //trim expression to remove any possible white spaces
            this.setTarget(output.getExpressionString().trim(), this.predefinedStructs);
        });

        _.forEach(nextProps.model.getChildren(), (statement) => {
            this.createConnection(statement);
        });

        this.props.model.on('child-added', (node) => {
            if (BallerinaASTFactory.isAssignmentStatement(node) &&
                    BallerinaASTFactory.isFunctionInvocationExpression(node.getRightExpression())) {
                const functionInvocationExpression = node.getRightExpression();
                const func = this.getFunctionDefinition(functionInvocationExpression);
                if (_.isUndefined(func)) {
                    alerts.error('Function definition for "' +
                        functionInvocationExpression.getFunctionName() + '" cannot be found');
                    return;
                }
                this.mapper.addFunction(func, node, node.getParent().removeChild.bind(node.getParent()));
            }
        });
    }

    componentDidMount() {
        this.mapper = new TransformRender(
            this.onConnectionCallback.bind(this), this.onDisconnectionCallback.bind(this));

        _.forEach(this.props.model.getInput(), (input) => {
            //trim expression to remove any possible white spaces
            this.setSource(input.getExpressionString().trim(), this.predefinedStructs);
        });

        _.forEach(this.props.model.getOutput(), (output) => {
            //trim expression to remove any possible white spaces
            this.setTarget(output.getExpressionString().trim(), this.predefinedStructs);
        });

        _.forEach(this.props.model.getChildren(), (statement) => {
            this.createConnection(statement);
        });

        $(window).on('resize', () => {
            this.mapper.reposition(this.mapper);
        });

        $('.leftType, .rightType, .middle-content').on('scroll', () => {
            this.mapper.reposition(this.mapper);
        });

        this.props.model.on('child-added', (node) => {
            if (BallerinaASTFactory.isAssignmentStatement(node) &&
                    BallerinaASTFactory.isFunctionInvocationExpression(node.getRightExpression())) {
                const functionInvocationExpression = node.getRightExpression();
                const func = this.getFunctionDefinition(functionInvocationExpression);
                if (_.isUndefined(func)) {
                    alerts.error('Function definition for "' +
                        functionInvocationExpression.getFunctionName() + '" cannot be found');
                    return;
                }
                this.mapper.addFunction(func, node, node.getParent().removeChild.bind(node.getParent()));
            }
        });
    }

    onTransformDropZoneActivate(e) {
        this.transformOverlayContentDiv = document.getElementById('transformOverlay-content');
        const dragDropManager = this.context.dragDropManager;
        const dropTarget = this.props.model;
        const model = this.props.model;

        if (dragDropManager.isOnDrag()) {
            if (_.isEqual(dragDropManager.getActivatedDropTarget(), dropTarget)) {
                return;
            }
            dragDropManager.setActivatedDropTarget(dropTarget,
                (nodeBeingDragged) => {
                    // This drop zone is for assignment statements only.
                    // Functions with atleast one return parameter is allowed to be dropped. If the dropped node
                    // is an Assignment Statement, that implies there is a return parameter . If there is no
                    // return parameter, then it is a Function Invocation Statement, which is validated with below check.
                    return model.getFactory().isAssignmentStatement(nodeBeingDragged);
                },
                () => {
                    return dropTarget.getChildren().length;
                });
        }
        e.stopPropagation();
    }

    onTransformDropZoneDeactivate(e) {
        this.transformOverlayContentDiv = document.getElementById('transformOverlay-content');
        const dragDropManager = this.context.dragDropManager;
        const dropTarget = this.props.model.getParent();
        if (dragDropManager.isOnDrag()) {
            if (_.isEqual(dragDropManager.getActivatedDropTarget(), dropTarget)) {
                dragDropManager.clearActivatedDropTarget();
                this.setState({ innerDropZoneActivated: false, innerDropZoneDropNotAllowed: false });
            }
        }
        e.stopPropagation();
    }

    onDropZoneActivate(e) {
        const dragDropManager = this.context.dragDropManager;
        const dropTarget = this.props.model.getParent();
        const model = this.props.model;
        if (dragDropManager.isOnDrag()) {
            if (_.isEqual(dragDropManager.getActivatedDropTarget(), dropTarget)) {
                return;
            }
            dragDropManager.setActivatedDropTarget(dropTarget,
                (nodeBeingDragged) => {
                    // IMPORTANT: override node's default validation logic
                    // This drop zone is for statements only.
                    // Statements should only be allowed here.
                    return model.getFactory().isStatement(nodeBeingDragged);
                },
                () => {
                    return dropTarget.getIndexOfChild(model);
                });
            this.setState({ innerDropZoneActivated: true,
                innerDropZoneDropNotAllowed: !dragDropManager.isAtValidDropTarget(),
            });
            dragDropManager.once('drop-target-changed', function () {
                this.setState({ innerDropZoneActivated: false, innerDropZoneDropNotAllowed: false });
            }, this);
        }
        e.stopPropagation();
    }

    onDropZoneDeactivate(e) {
        const dragDropManager = this.context.dragDropManager;
        const dropTarget = this.props.model.getParent();
        if (dragDropManager.isOnDrag()) {
            if (_.isEqual(dragDropManager.getActivatedDropTarget(), dropTarget)) {
                dragDropManager.clearActivatedDropTarget();
                this.setState({ innerDropZoneActivated: false, innerDropZoneDropNotAllowed: false });
            }
        }
        e.stopPropagation();
    }

    onSourceInputChange(e, {newValue}) {
        this.setState({
            typedSource: newValue,
        });
    }

    onTargetInputChange(e, {newValue}) {
        this.setState({
            typedTarget: newValue,
        });
    }

    onSourceSelect(e, {suggestionValue}) {
        this.setState({
            selectedSource: suggestionValue,
        });
    }

    onTargetSelect(e, {suggestionValue}) {
        this.setState({
            selectedTarget: suggestionValue,
        });
    }

    onSourceInputEnter() {
        this.addSource(this.state.typedSource);
    }

    onTargetInputEnter() {
        this.addTarget(this.state.typedTarget);
    }

    onSourceAdd() {
        this.addSource(this.state.selectedSource);
    }

    onTargetAdd() {
        this.addTarget(this.state.selectedTarget);
    }

    addSource(selectedSource) {
        let inputDef = BallerinaASTFactory
                                .createSimpleVariableReferenceExpression({ variableName: selectedSource });
        if (this.setSource(selectedSource, this.predefinedStructs, this.props.model, inputDef.id)) {
            let inputs = this.props.model.getInput();
            inputs.push(inputDef);
            this.props.model.setInput(inputs);
            this.setState({typedSource: ''});
        }
    }

    addTarget(selectedTarget) {
        let outDef = BallerinaASTFactory
                                .createSimpleVariableReferenceExpression({ variableName: selectedTarget });
        if (this.setTarget(selectedTarget, this.predefinedStructs, this.props.model, outDef.id)) {
            let outputs = this.props.model.getOutput();
            outputs.push(outDef);
            this.props.model.setOutput(outputs);
            this.setState({typedTarget: ''});
        }
    }

    onClose() {
        const { designView } = this.context;
        designView.setTransformActive(false);
    }

    getTransformVarJson(args) {
        let argArray = [];
        _.forEach(args, argument => {
            if (BallerinaASTFactory.isVariableDefinitionStatement(argument)) {
                let arg = {
                    id : argument.getID(),
                    type : argument.children[0].getVariableType(),
                    name : argument.children[0].getVariableName(),
                    pkgName : argument.children[0].children[0].getPkgName()
                };
                argArray.push(arg);
            } else if (BallerinaASTFactory.isParameterDefinition(argument)) {
                let arg = {
                    id : argument.getID(),
                    type : argument.getTypeName(),
                    name : argument.getName()
                };
                argArray.push(arg);
            }
        });
        return argArray;
    }

    getSourcesAndTargets() {
        const variables = this.props.model.filterChildrenInScope(
                                     this.props.model.getFactory().isVariableDefinitionStatement)
        const argHolders = this.props.model.filterChildrenInScope(
                                     this.props.model.getFactory().isArgumentParameterDefinitionHolder)
        const paramArgs = [];
        _.forEach(argHolders, (argHolder) => {
            _.forEach(argHolder.getChildren(), (arg) => {
                paramArgs.push(arg);
            });
        });

        const transformVars = this.getTransformVarJson(variables.concat(paramArgs));
        const items = [];

        _.forEach(transformVars, (arg) => {
            let isStruct = false;
            const structDef = this.getStructDefinition(arg.pkgName, arg.type);

            if (structDef !== undefined) {
                const struct = this.createType(arg.name, arg.type, structDef);
                items.push({ name: struct.name, type: struct.typeName });
                isStruct = true;
            }

            if (!isStruct) {
                const variableType = {};
                variableType.id = arg.id;
                variableType.name = arg.name;
                variableType.type = arg.type;
                this.predefinedStructs.push(variableType);
                items.push({ name: variableType.name, type: variableType.type });
            }
        });

        return items;
    }

    setSource(currentSelection, predefinedStructs) {
        var sourceSelection =  _.find(predefinedStructs, { name:currentSelection });
        if (_.isUndefined(sourceSelection)){
            alerts.error('Mapping source "' + currentSelection + '" cannot be found');
            return false;
        }

        const removeFunc = id => {
            this.mapper.removeType(id);
            _.remove(this.props.model.getInput(),(currentObject) => {
                return currentObject.getVariableName() === id;
            });
            this.removeAssignmentStatements(id, "source");
            this.props.model.setInput(this.props.model.getInput());
            var currentSelectionObj =  _.find(this.predefinedStructs, { name:id});
            currentSelectionObj.added = false;
        }

        if (!sourceSelection.added) {
            if (sourceSelection.type === 'struct') {
                this.mapper.addSourceType(sourceSelection, removeFunc);
            } else {
                this.mapper.addVariable(sourceSelection, 'source', removeFunc);
            }
            sourceSelection.added = true;
            return true;
        }
            return false;

    }

    setTarget(currentSelection, predefinedStructs) {
        var targetSelection = _.find(predefinedStructs, { name: currentSelection});
        if (_.isUndefined(targetSelection)){
            alerts.error('Mapping target "' + currentSelection + '" cannot be found');
            return false;
        }

        const removeFunc = id => {
            this.mapper.removeType(id);
            _.remove(this.props.model.getOutput(),(currentObject) => {
                return currentObject.getVariableName() === id;
            });
            this.removeAssignmentStatements(id, "target");
            this.props.model.setOutput(this.props.model.getOutput());
            var currentSelectionObj =  _.find(this.predefinedStructs, { name:id});
            currentSelectionObj.added = false;
        }

        if (!targetSelection.added) {
            if (targetSelection.type === 'struct') {
                this.mapper.addTargetType(targetSelection, removeFunc);
            } else {
                this.mapper.addVariable(targetSelection, 'target', removeFunc);
            }
            targetSelection.added = true;
            return true;
        }

        return false;
    }

    removeAssignmentStatements(id, type) {
        var index = 0;
        if(type === "source") {
            index = 1;
        }
        _.remove(this.props.model.getChildren(),(currentObject) => {
            var condition = false;
            if (currentObject.children[index].children[0].getFactory()
                           .isFieldBasedVarRefExpression(currentObject.children[index].children[0])) {
                condition = currentObject.children[index].children[0].children[0].getExpressionString() === id;
            } else {
               condition = currentObject.children[index].children[0].getVariableName() === id;
            }
            return condition;
        });
    }

    render() {
        const sourceId = 'sourceStructs' + this.props.model.id;
        const targetId = 'targetStructs' + this.props.model.id;
        const sourcesAndTargets = this.predefinedStructs.filter(endpoint => (!endpoint.isInner));

        return (
            <div id='transformOverlay' className='transformOverlay'>
                <div id = 'transformOverlay-content' className='transformOverlay-content'
                    ref={div => this.transformOverlayContentDiv=div }
                    onMouseOver={this.onTransformDropZoneActivate}
                    onMouseOut={this.onTransformDropZoneDeactivate}>
                    <div id ="transformHeader" className="transform-header">
                        <i onClick={this.onClose} className="fw fw-left-arrow icon close-transform"></i>
                        <p className="transform-header-text ">
                            <i className="transform-header-icon fw fw-type-converter"></i>
                            Transform
                        </p>
                    </div>
                    <div id ="transformHeaderPadding" className="transform-header-padding"></div>
                    <div className="source-view">
                        <SuggestionsDropdown
                            value={this.state.typedSource}
                            onChange={this.onSourceInputChange}
                            onEnter={this.onSourceInputEnter}
                            suggestionsPool={sourcesAndTargets}
                            placeholder='Select Source'
                            onSuggestionSelected={this.onSourceSelect}
                        />
                        <span
                            id="btn-add-source"
                            className="btn-add-type fw-stack fw-lg btn btn-add"
                            onClick={this.onSourceAdd}
                        >
                            <i className="fw fw-add fw-stack-1x"></i>
                        </span>
                    </div>
                    <div className="leftType"></div>
                    <div className="middle-content"></div>
                    <div className="target-view">
                        <SuggestionsDropdown
                            value={this.state.typedTarget}
                            onChange={this.onTargetInputChange}
                            onEnter={this.onTargetInputEnter}
                            suggestionsPool={sourcesAndTargets}
                            placeholder='Select Target'
                            onSuggestionSelected={this.onTargetSelect}
                        />
                        <span
                            id="btn-add-target"
                            className="btn-add-type fw-stack fw-lg btn btn-add"
                            onClick={this.onTargetAdd}
                        >
                            <i className="fw fw-add fw-stack-1x"></i>
                        </span>
                    </div>
                    <div className="rightType"></div>
                    <div id ="transformContextMenu" className="transformContextMenu"></div>
                    <div id ="transformFooter" className="transform-footer"></div>
                </div>
            </div>
        );
    }
}

TransformExpanded.propTypes = {
    model: PropTypes.instanceOf(ASTNode).isRequired,
};

TransformExpanded.contextTypes = {
    dragDropManager: PropTypes.instanceOf(DragDropManager).isRequired,
    designView: PropTypes.instanceOf(Object).isRequired,
    environment: PropTypes.instanceOf(Object).isRequired,
};

export default TransformExpanded;
