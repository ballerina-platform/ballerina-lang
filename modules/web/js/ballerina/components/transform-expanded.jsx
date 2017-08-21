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
import TransformNodeManager from './transform-node-manager';
import SuggestionsDropdown from './transform-endpoints-dropdown';
import ASTNode from '../ast/node';
import DragDropManager from '../tool-palette/drag-drop-manager';
import Tree from './transform/tree';
import FunctionInv from './transform/function';
import { getLangServerClientInstance } from './../../langserver/lang-server-client-controller';
import { getResolvedTypeData } from './../../langserver/lang-server-utils';
import ASTFactory from '../ast/ast-factory';
import './transform-expanded.css';

/**
 * React component for transform expanded view
 * @class TransformExpanded
 * @extends {React.Component}
 */
class TransformExpanded extends React.Component {

    /**
     * Transform extended component constructor
     * @param {any} props props for the component
     * @param {any} context context for the component
     * @memberof TransformExpanded
     */
    constructor(props, context) {
        super(props, context);
        this.state = {
            // vertices changes must re-render. Hence added as a state.
            vertices: [],
            typedSource: '',
            typedTarget: '',
            selectedSource: '-1',
            selectedTarget: '-1',
        };
        this.sourceElements = {};
        this.targetElements = {};
        this.loadVertices();

        this.onSourceSelect = this.onSourceSelect.bind(this);
        this.onTargetSelect = this.onTargetSelect.bind(this);
        this.onSourceAdd = this.onSourceAdd.bind(this);
        this.onTargetAdd = this.onTargetAdd.bind(this);
        this.onClose = this.onClose.bind(this);
        this.onTransformDropZoneActivate = this.onTransformDropZoneActivate.bind(this);
        this.onTransformDropZoneDeactivate = this.onTransformDropZoneDeactivate.bind(this);
        this.onSourceInputChange = this.onSourceInputChange.bind(this);
        this.onTargetInputChange = this.onTargetInputChange.bind(this);
        this.onSourceInputEnter = this.onSourceInputEnter.bind(this);
        this.onTargetInputEnter = this.onTargetInputEnter.bind(this);
        this.addSource = this.addSource.bind(this);
        this.addTarget = this.addTarget.bind(this);
        this.recordSourceElement = this.recordSourceElement.bind(this);
        this.recordTargetElement = this.recordTargetElement.bind(this);
        this.removeSourceType = this.removeSourceType.bind(this);
        this.removeTargetType = this.removeTargetType.bind(this);
    }

    onConnectionCallback(connection) {
        const source = _.find(this.state.vertices, { name: connection.sourceStruct });
        const target = _.find(this.state.vertices, { name: connection.targetStruct });
        this.transformNodeManager.createStatementEdge(source, target, connection);
    }

    onDisconnectionCallback(connection) {
        // on removing a connection
        const source = _.find(this.state.vertices, { name: connection.sourceStruct });
        const target = _.find(this.state.vertices, { name: connection.targetStruct });
        this.transformNodeManager.removeStatementEdge(source, target, connection);
    }

    recordSourceElement(element, id, input) {
        this.sourceElements[id] = { element, input };
    }

    recordTargetElement(element, id, output) {
        this.targetElements[id] = { element, output };
    }

    createConnection(statement) {
        const viewId = this.props.model.getID();

        if (ASTFactory.isCommentStatement(statement)) {
            return;
        }

        if (!ASTFactory.isAssignmentStatement(statement)) {
            log.error('Invalid statement type in transform statement');
            return;
        }

        // There can be multiple left expressions.
        // E.g. : e.name, e.username = p.first_name;
        const leftExpression = statement.getLeftExpression();
        const rightExpression = this.transformNodeManager
                                    .getMappingRightExpression(statement.getRightExpression());

        if (ASTFactory.isFieldBasedVarRefExpression(rightExpression) ||
              ASTFactory.isSimpleVariableReferenceExpression(rightExpression)) {
            _.forEach(leftExpression.getChildren(), (leftExpr) => {
                const sourceId = `${rightExpression.getExpressionString().trim()}:${viewId}`;
                const targetId = `${leftExpr.getExpressionString().trim()}:${viewId}`;
                this.mapper.addConnection(sourceId, targetId);
            });
        }

        if (ASTFactory.isFunctionInvocationExpression(rightExpression)) {
            this.drawFunctionInvocationExpression(leftExpression, rightExpression, statement);
        }
        this.mapper.reposition(this.props.model.getID());
    }

    drawFunctionDefinitionNodes(functionInvocationExpression, statement) {
        const func = this.transformNodeManager.getFunctionDefinition(functionInvocationExpression);
        if (_.isUndefined(func)) {
            alerts.error(
                'Function definition for "' + functionInvocationExpression.getFunctionName() + '" cannot be found');
            return;
        }

        if (func.getParameters().length !== functionInvocationExpression.getChildren().length) {
            // alerts.warn('Function inputs and mapping count does not match in "' + func.getName() + '"');
        } else {
            _.forEach(functionInvocationExpression.getChildren(), (expression) => {
                if (ASTFactory.isFunctionInvocationExpression(expression)) {
                    this.drawInnerFunctionDefinitionNodes(functionInvocationExpression, expression, statement);
                }
            });
        }

        // Removing this node means removing the assignment statement from the transform statement,
        // since this is the top most invocation.
        // Hence passing the assignment statement as remove reference.
        // this.mapper.addFunction(func, functionInvocationExpression,
        //     statement.getParent().removeChild.bind(statement.getParent()), statement);
    }

    drawInnerFunctionDefinitionNodes(parentFunctionInvocationExpression, functionInvocationExpression, statement) {
        const func = this.transformNodeManager.getFunctionDefinition(functionInvocationExpression);
        if (_.isUndefined(func)) {
            alerts.error(
                    'Function definition for "' + functionInvocationExpression.getFunctionName() + '" cannot be found');
            return;
        }

        if (func.getParameters().length !== functionInvocationExpression.getChildren().length) {
            // alerts.warn('Function inputs and mapping count does not match in "' + func.getName() + '"');
        } else {
            _.forEach(functionInvocationExpression.getChildren(), (expression) => {
                if (ASTFactory.isFunctionInvocationExpression(expression)) {
                    this.drawInnerFunctionDefinitionNodes(functionInvocationExpression, expression, statement);
                }
            });
        }

        // Removing this node means removing the function invocation from the parent function invocation.
        // Hence passing the current function invocation as remove reference.
        this.mapper.addFunction(
            func, functionInvocationExpression,
            parentFunctionInvocationExpression.removeChild.bind(parentFunctionInvocationExpression),
            functionInvocationExpression);
    }

    drawInnerFunctionInvocationExpression(parentFunctionInvocationExpression, functionInvocationExpression,
                                          parentFunctionDefinition, parentParameterIndex, statement) {
        const viewId = this.props.model.getID();
        const func = this.transformNodeManager.getFunctionDefinition(functionInvocationExpression);
        if (_.isUndefined(func)) {
            alerts.error(
                'Function definition for "' + functionInvocationExpression.getFunctionName() + '" cannot be found');
            return;
        }

        if (func.getParameters().length !== functionInvocationExpression.getChildren().length) {
            // alerts.warn('Function inputs and mapping count does not match in "' + func.getName() + '"');
        }

        const params = func.getParameters();
        const returnParams = func.getReturnParams();
        const functionInvID = functionInvocationExpression.getID();
        const funcName = functionInvocationExpression.getFunctionName();

        _.forEach(functionInvocationExpression.getChildren(), (expression, i) => {
            if (ASTFactory.isFunctionInvocationExpression(expression)) {
                this.drawInnerFunctionInvocationExpression(
                    functionInvocationExpression, expression, func, i, statement);
            } else {
                const sourceId = `${expression.getExpressionString().trim()}:${viewId}`;
                const targetId = `${functionInvID}:${funcName}:${params[i].name}:${viewId}`;
                this.mapper.addConnection(sourceId, targetId);
            }
        });

        if (!parentFunctionDefinition) {
            return;
        }

        const sourceId = `${functionInvID}:${funcName}:${returnParams[0].name || 0}:${viewId}`;

        const parentParams = parentFunctionDefinition.getParameters();
        const parentFuncInvID = parentFunctionInvocationExpression.getID();
        const parentFuncName = parentFunctionInvocationExpression.getFunctionName();

        const targetId = `${parentFuncInvID}:${parentFuncName}:${parentParams[parentParameterIndex].name}:${viewId}`;

        this.mapper.addConnection(sourceId, targetId);
        this.mapper.reposition(this.props.model.getID());
    }

    drawFunctionInvocationExpression(argumentExpressions, functionInvocationExpression, statement) {
        const func = this.transformNodeManager.getFunctionDefinition(functionInvocationExpression);
        const viewId = this.props.model.getID();
        if (_.isUndefined(func)) {
            alerts.error(
                'Function definition for "' + functionInvocationExpression.getFunctionName() + '" cannot be found');
            return;
        }
        if (func.getParameters().length !== functionInvocationExpression.getChildren().length) {
            // alerts.warn('Function inputs and mapping count does not match in "' + func.getName() + '"');
        }

        const params = func.getParameters();
        const returnParams = func.getReturnParams();
        const funcInvID = functionInvocationExpression.getID();
        const funcName = functionInvocationExpression.getFunctionName();

        _.forEach(functionInvocationExpression.getChildren(), (expression, i) => {
            if (ASTFactory.isFunctionInvocationExpression(expression)) {
                this.drawInnerFunctionInvocationExpression(
                    functionInvocationExpression, expression, func, i, statement);
            } else {
                let target;
                let source;
                if (ASTFactory.isKeyValueExpression(expression.children[0])) {
                // if parameter is a key value expression, iterate each expression and draw connections
                    _.forEach(expression.children, (propParam) => {
                        source = this.getConnectionProperties('source', propParam.children[1]);
                        target = this.getConnectionProperties('target', func.getParameters()[i]);
                        _.merge(target, funcTarget); // merge parameter props with function props
                        target.targetProperty.push(propParam.children[0].getVariableName());
                        const typeDef = _.find(this.state.vertices, { typeName: func.getParameters()[i].type });
                        const propType = _.find(typeDef.properties, { name: propParam.children[0].getVariableName() });
                        target.targetType.push(propType.type);
                        this.drawConnection(statement.getID() + functionInvocationExpression.getID(), source, target);
                    });
                } else {
                    const sourceId = `${expression.getExpressionString().trim()}:${viewId}`;
                    const targetId = `${funcInvID}:${funcName}:${params[i].name}:${viewId}`;
                    this.mapper.addConnection(sourceId, targetId);
                }
            }
        });

        if (func.getReturnParams().length !== argumentExpressions.getChildren().length) {
            // alerts.warn('Function inputs and mapping count does not match in "' + func.getName() + '"');
        }

        _.forEach(argumentExpressions.getChildren(), (expression, i) => {
            const sourceId = `${funcInvID}:${funcName}:${returnParams[i].name || i}:${viewId}`;
            const targetId = `${expression.getExpressionString().trim()}:${viewId}`;
            this.mapper.addConnection(sourceId, targetId);
        });
        this.mapper.reposition(this.props.model.getID());
    }

    getConnectionProperties(type, expression) {
        const con = {};
        if (ASTFactory.isFieldBasedVarRefExpression(expression)) {
            const structVarRef = expression.getStructVariableReference();
            con[type + 'Struct'] = structVarRef.getVariableName();
            const complexProp = this.createComplexProp(con[type + 'Struct'], structVarRef.getParent());
            con[type + 'Type'] = complexProp.types;
            con[type + 'Property'] = complexProp.names;
        } else if (ASTFactory.isFunctionInvocationExpression(expression)) {
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
        } else if (ASTFactory.isSimpleVariableReferenceExpression(expression)) {
            con[type + 'Struct'] = expression.getVariableName();
            const varRef = _.find(this.state.vertices, { name: expression.getVariableName() });
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
        let sourceProp;
        let targetProp;
        let sourceParent = source.sourceStruct;
        let targetParent = target.targetStruct;

        if (source.sourceProperty.length > 1) {
            sourceParent = source.sourceProperty[source.sourceProperty.length - 2];
        }
        if (target.targetProperty.length > 1) {
            targetParent = target.targetProperty[target.targetProperty.length - 2];
        }
        _.forEach(this.state.vertices, (struct) => {
            if (struct.name === sourceParent) {
                sourceProp = _.find(struct.properties, (field) => {
                    return field.name === source.sourceProperty[source.sourceProperty.length - 1];
                });
                if (_.isUndefined(sourceProp)) {
                    sourceProp = struct;
                }
            } else if (struct.name === targetParent) {
                targetProp = _.find(struct.properties, (field) => {
                    return field.name === target.targetProperty[target.targetProperty.length - 1];
                });
                if (_.isUndefined(targetProp)) {
                    targetProp = struct;
                }
            }
        });
        const con = { id, input: sourceProp, output: targetProp };
        _.merge(con, source, target);
        this.mapper.addConnection(con);
    }

    createComplexProp(structName, expression) {
        const prop = {};
        prop.names = [];
        prop.types = [];

        if (ASTFactory.isFieldBasedVarRefExpression(expression)) {
            const fieldName = expression.getFieldName();
            const structDef = _.find(this.state.vertices, { name: structName });
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

            const parentProp = this.createComplexProp(fieldName, expression.getParent());
            prop.names = [...prop.names, ...parentProp.names];
            prop.types = [...prop.types, ...parentProp.types];
        }
        return prop;
    }

    componentDidUpdate(prevProps, prevState) {
        const sourceKeys = Object.keys(this.sourceElements);
        sourceKeys.forEach((key) => {
            const { element, input } = this.sourceElements[key];
            if (element) {
                input.id = key;
                this.mapper.addSource(element, input);
            } else {
                // this source connect point is unmounted
                this.mapper.remove(key);
            }
        });

        const targetKeys = Object.keys(this.targetElements);
        targetKeys.forEach((key) => {
            const { element, output } = this.targetElements[key];
            if (element) {
                output.id = key;
                this.mapper.addTarget(element, output);
            } else {
                // this target connect point is unmounted
                this.mapper.remove(key);
            }
        });

        this.mapper.disconnectAll();

        _.forEach(this.props.model.getChildren(), (statement) => {
            this.createConnection(statement);
        });
        this.mapper.reposition(this.props.model.getID());
        if ((this.props.model === prevProps.model) && prevState.vertices.length !== 0) {
            return;
        }
        this.loadVertices();
    }

    componentDidMount() {
        this.mapper = new TransformRender(this.onConnectionCallback.bind(this),
            this.onDisconnectionCallback.bind(this), $(this.transformOverlayContentDiv));
        this.transformNodeManager = new TransformNodeManager(
            {
                typeLattice: this.context.environment.getTypeLattice(),
                transformStmt: this.props.model,
                environment: this.context.environment,
            });

        const sourceKeys = Object.keys(this.sourceElements);
        sourceKeys.forEach((key) => {
            const { element, input } = this.sourceElements[key];
            this.mapper.addSource(element, input);
        });

        const targetKeys = Object.keys(this.targetElements);
        targetKeys.forEach((key) => {
            const { element, output } = this.targetElements[key];
            this.mapper.addTarget(element, output);
        });

        if (this.state.vertices.length > 0) {
            // if there are no vertices, cannot draw assignment connections
            _.forEach(this.props.model.getChildren(), (statement) => {
                this.createConnection(statement);
            });
        }

        $('.middle-content, .leftType, .rightType').scroll(() => {
            this.mapper.reposition(this.props.model.getID());
        });

        this.mapper.reposition(this.props.model.getID());
    }

    onTransformDropZoneActivate(e) {
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
                    // return parameter, then it is a Function Invocation Statement,
                    // which is validated with below check.
                    return ASTFactory.isAssignmentStatement(nodeBeingDragged);
                },
                () => {
                    return dropTarget.getChildren().length;
                });
        }
        e.stopPropagation();
    }

    onTransformDropZoneDeactivate(e) {
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

    onSourceInputChange(e, { newValue }) {
        this.setState({
            typedSource: newValue,
        });
    }

    onTargetInputChange(e, { newValue }) {
        this.setState({
            typedTarget: newValue,
        });
    }

    onSourceSelect(e, { suggestionValue }) {
        this.setState({
            selectedSource: suggestionValue,
        });
        this.addSource(suggestionValue);
    }

    onTargetSelect(e, { suggestionValue }) {
        this.setState({
            selectedTarget: suggestionValue,
        });
        this.addTarget(suggestionValue);
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

    removeSourceType(removedType) {
        this.mapper.removeType(removedType.name);
        this.props.model.removeInput(removedType);
    }

    removeTargetType(removedType) {
        this.mapper.removeType(removedType.name);
        this.props.model.removeOutput(removedType);
    }

    addSource(selectedSource) {
        const inputDef = ASTFactory
                                .createSimpleVariableReferenceExpression({ variableName: selectedSource });
        if (this.setSource(selectedSource, this.state.vertices, this.props.model, inputDef.id)) {
            const inputs = this.props.model.getInput();
            inputs.push(inputDef);
            this.props.model.setInput(inputs);
            this.setState({ typedSource: '' });
        }
    }

    addTarget(selectedTarget) {
        const outDef = ASTFactory
                                .createSimpleVariableReferenceExpression({ variableName: selectedTarget });
        if (this.setTarget(selectedTarget, this.state.vertices, this.props.model, outDef.id)) {
            const outputs = this.props.model.getOutput();
            outputs.push(outDef);
            this.props.model.setOutput(outputs);
            this.setState({ typedTarget: '' });
        }
    }

    onClose() {
        const { designView } = this.context;
        designView.setTransformActive(false);
    }

    /**
     * Load vertices of the transform graph.
     * @memberof TransformExpanded
     */
    loadVertices() {
        getLangServerClientInstance().then((langServerClient) => {
            const vertices = [];

            const fileData = this.context.designView.context.editor.props.file.attributes;
            const position = this.props.model.getPosition();

            const options = {
                textDocument: fileData.content,
                position: {
                    line: position.startLine,
                    character: position.startOffset,
                },
                fileName: fileData.name,
                filePath: fileData.path,
                packageName: fileData.packageName,
            };

            langServerClient.getCompletions(options, (response) => {
                const completions = response.result.filter((completionItem) => {
                    // all variables have type as 9 as per the declaration in lang server
                    return (completionItem.kind === 9);
                });
                const transformVars = completions.map((completionItem) => {
                    const typeData = getResolvedTypeData(completionItem);
                    return ({
                        type: typeData.typeName,
                        name: completionItem.label,
                        pkgName: typeData.packageName,
                        constraint: typeData.constraint,
                    });
                });
                _.forEach(transformVars, (arg) => {
                    let isStruct = false;
                    const structDef = this.transformNodeManager.getStructDefinition(arg.pkgName, arg.type);
                    if (structDef !== undefined) {
                        arg.type = ((arg.pkgName) ? (arg.pkgName + ':') : '') + arg.type;
                        const structVar = this.transformNodeManager.getStructType(arg.name, arg.type, structDef);
                        vertices.push(structVar);
                        isStruct = true;
                    }
                    if (!isStruct) {
                        const variableType = {};
                        variableType.id = arg.id;
                        variableType.name = arg.name;
                        if (arg.constraint !== undefined) {
                            variableType.type = arg.type + '<'
                                        + ((arg.constraint.packageName) ? arg.constraint.packageName + ':' : '')
                                        + arg.constraint.type + '>';
                            variableType.constraintType = arg.constraint;
                            const constraintDef = this.transformNodeManager.getStructDefinition(
                                arg.constraint.packageName, arg.constraint.type);
                            if (constraintDef !== undefined) {
                                const constraintVar = this.transformNodeManager.getStructType(arg.name, variableType.type, constraintDef);
                                // For constraint types, the field types must be the same type as the variable and
                                // not the struct field types. E.g. : struct.name type maybe string but if it is a json,
                                // type has to be json and not string. Hence converting all field types to variable
                                // type.
                                // TODO : revisit this conversion if ballerina language supports constrained field
                                // access to be treated as the field type (i.e. as string from the struct field
                                // and not json)
                                this.convertFieldType(constraintVar.properties, arg.type);

                                // constraint properties (fields) become variable fields
                                // variableType.properties = constraint.properties;
                                // variableType.constraint = constraint;
                                vertices.push(constraintVar);
                            }
                        } else {
                            variableType.type = arg.type;
                            vertices.push({ name: variableType.name, type: variableType.type });
                        }
                    }
                });
                // set state with new vertices
                if (!_.isEqual(vertices, this.state.vertices)) {
                    this.setState({ vertices });
                }
            });
        }).catch(error => alerts.error('Could not initialize transform statement view ' + error));
    }

    /**
     * Converts the property types to a given type
     * @param {[Property]} properties properties
     * @param {string} type type to convert to
     * @memberof TransformExpanded
     */
    convertFieldType(properties, type) {
        if (properties) {
            properties.forEach((property) => {
                if (property.innerType) {
                    this.convertFieldType(property.innerType.properties, type);
                }
                property.type = type;
            });
        }
    }

    setSource(currentSelection) {
        const sourceSelection = _.find(this.state.vertices, { name: currentSelection });
        if (_.isUndefined(sourceSelection)) {
            // alerts.error('Mapping source "' + currentSelection + '" cannot be found');
            return false;
        }
        return true;
    }

    setTarget(currentSelection) {
        const targetSelection = _.find(this.state.vertices, { name: currentSelection });
        if (_.isUndefined(targetSelection)) {
            // alerts.error('Mapping target "' + currentSelection + '" cannot be found');
            return false;
        }
        return true;
    }

    removeAssignmentStatements(id, type) {
        const statementsToRemove = [];

        this.props.model.getChildren().forEach((currentObject) => {
            let nodeToRemove;

            if (type === 'source') {
                nodeToRemove = currentObject.getRightExpression();
            } else {
                nodeToRemove = currentObject.getLeftExpression();
            }

            if (ASTFactory.isFieldBasedVarRefExpression(nodeToRemove)) {
                if (nodeToRemove.getExpressionString().startsWith(`${id}.`)) {
                    statementsToRemove.push(currentObject);
                    return;
                }
            }

            if ((ASTFactory.isVariableReferenceList(nodeToRemove))) {
                nodeToRemove.getChildren().forEach((childVarRef) => {
                    if (ASTFactory.isFieldBasedVarRefExpression(childVarRef)) {
                        if (childVarRef.getExpressionString().startsWith(`${id}.`)) {
                            statementsToRemove.push(currentObject);
                            return;
                        }
                    }

                    if (childVarRef.getVariableName() === id) {
                        statementsToRemove.push(currentObject);
                    }
                });
                return;
            }

            if (nodeToRemove.getVariableName() === id) {
                statementsToRemove.push(currentObject);
            }
        });

        statementsToRemove.forEach((statement) => {
            this.props.model.removeChild(statement);
        });
    }

    findFunctionInvocations(functionInvocationExpression, functions = [], parentFunc) {
        const func = this.transformNodeManager.getFunctionDefinition(functionInvocationExpression);
        if (_.isUndefined(func)) {
            alerts.error('Function definition for "' +
                functionInvocationExpression.getFunctionName() + '" cannot be found');
            return;
        }
        functionInvocationExpression.getChildren().forEach((child) => {
            if (ASTFactory.isFunctionInvocationExpression(child)) {
                this.findFunctionInvocations(child, functions, functionInvocationExpression);
            }
        });
        functions.push({ func, parentFunc, funcInv: functionInvocationExpression });
        return functions;
    }

    render() {
        const vertices = this.state.vertices.filter(vertex => (!vertex.isInner));
        const inputNodes = this.props.model.getInput();
        const outputNodes = this.props.model.getOutput();
        const inputs = [];
        const outputs = [];
        const functions = [];

        if (this.state.vertices.length > 0) {
            inputNodes.forEach((inputNode) => {
                const name = inputNode.getVariableName();
                const sourceSelection = _.find(vertices, { name });
                if (_.isUndefined(sourceSelection)) {
                    // alerts.error('Mapping source "' + name + '" cannot be found');
                    return;
                }
                _.remove(vertices, (vertex) => { return vertex.name === sourceSelection.name; });
                inputs.push(sourceSelection);
            });

            outputNodes.forEach((outputNode) => {
                const name = outputNode.getVariableName();
                const targetSelection = _.find(vertices, { name });
                if (_.isUndefined(targetSelection)) {
                    // alerts.error('Mapping target "' + name + '" cannot be found');
                    return;
                }
                _.remove(vertices, (vertex) => { return vertex.name === targetSelection.name; });
                outputs.push(targetSelection);
            });

            this.props.model.getChildren().forEach((child) => {
                if (!ASTFactory.isAssignmentStatement(child)) {
                    return;
                }
                const rightExpression = child.getRightExpression();

                if (ASTFactory.isFunctionInvocationExpression(rightExpression)) {
                    const funcInvs = this.findFunctionInvocations(rightExpression);
                    funcInvs.forEach((funcDetails) => {
                        funcDetails.assignmentStmt = child;
                    });
                    functions.push(...funcInvs);
                }
            });
        }
        return (
            <div
                id={`transformOverlay-content-${this.props.model.getID()}`}
                className='transformOverlay'
                ref={div => this.transformOverlayContentDiv = div}
                onMouseOver={this.onTransformDropZoneActivate}
                onMouseOut={this.onTransformDropZoneDeactivate}
            >
                <div id='transformHeader' className='transform-header'>
                    <i onClick={this.onClose} className='fw fw-left icon close-transform' />
                    <p className='transform-header-text '>
                        <i className='transform-header-icon fw fw-type-converter' />
                        Transform
                    </p>
                </div>
                <div className='left-content'>
                    <div className="select-source">
                        <SuggestionsDropdown
                            value={this.state.typedSource}
                            onChange={this.onSourceInputChange}
                            onEnter={this.onSourceInputEnter}
                            suggestionsPool={vertices}
                            placeholder='Select Source'
                            onSuggestionSelected={this.onSourceSelect}
                        />
                    </div>
                    <div className="leftType">
                        <Tree
                            viewId={this.props.model.getID()}
                            endpoints={inputs}
                            type='source'
                            makeConnectPoint={this.recordSourceElement}
                            removeTypeCallbackFunc={this.removeSourceType}
                        />
                    </div>
                </div>
                <div className="middle-content">
                    {
                        functions.map(({ func, assignmentStmt, parentFunc, funcInv }) => (
                            <FunctionInv
                                key={funcInv.getID()}
                                func={func}
                                enclosingAssignmentStatement={assignmentStmt}
                                parentFunc={parentFunc}
                                funcInv={funcInv}
                                recordSourceElement={this.recordSourceElement}
                                recordTargetElement={this.recordTargetElement}
                                viewId={this.props.model.getID()}
                            />
                        ))
                    }
                </div>
                <div className='right-content'>
                    <div className="select-target">
                        <SuggestionsDropdown
                            value={this.state.typedTarget}
                            onChange={this.onTargetInputChange}
                            onEnter={this.onTargetInputEnter}
                            suggestionsPool={vertices}
                            placeholder='Select Target'
                            onSuggestionSelected={this.onTargetSelect}
                        />
                    </div>
                    <div className='rightType'>
                        <Tree
                            viewId={this.props.model.getID()}
                            endpoints={outputs}
                            type='target'
                            makeConnectPoint={this.recordTargetElement}
                            removeTypeCallbackFunc={this.removeTargetType}
                        />
                    </div>
                </div>
                <div id='transformContextMenu' className='transformContextMenu' />
                <div id='transformFooter' className='transform-footer' />
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
