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
import { Scrollbars } from 'react-custom-scrollbars';
import log from 'log';
import TransformRender from './transform-render';
import TransformNodeManager from './transform-node-manager';
import SuggestionsDropdown from './transform-endpoints-dropdown';
import ASTNode from '../../../../../ast/node';
import DragDropManager from '../../../../../tool-palette/drag-drop-manager';
import Tree from './tree';
import FunctionInv from './function';
import Operator from './operator';
import { getLangServerClientInstance } from './../../../../../../langserver/lang-server-client-controller';
import { getResolvedTypeData } from './../../../../../../langserver/lang-server-utils';
import ASTFactory from '../../../../../ast/ast-factory';
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
            foldedEndpoints: {},
            foldedFunctions: {},
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
        this.foldEndpoint = this.foldEndpoint.bind(this);
        this.foldFunction = this.foldFunction.bind(this);
        this.removeEndpoint = this.removeEndpoint.bind(this);
        this.removeIntermediateNode = this.removeIntermediateNode.bind(this);
        this.updateVariable = this.updateVariable.bind(this);
        this.onDragLeave = this.onDragLeave.bind(this);
        this.onDragEnter = this.onDragEnter.bind(this);
        this.onMouseMove = this.onMouseMove.bind(this);
        this.onConnectionsScroll = this.onConnectionsScroll.bind(this);
        this.onConnectPointMouseEnter = this.onConnectPointMouseEnter.bind(this);
    }

    foldEndpoint(key) {
        this.setState(
            {
                foldedEndpoints: _.extend(this.state.foldedEndpoints, {
                    [key]: !this.state.foldedEndpoints[key],
                }),
            });
    }

    foldFunction(key) {
        this.setState(
            {
                foldedFunctions: _.extend(this.state.foldedFunctions, {
                    [key]: !this.state.foldedFunctions[key],
                }),
            });
    }

    removeEndpoint(key) {
        this.mapper.remove(key);
    }

    removeIntermediateNode(expression, parentNode, statement) {
        this.transformNodeManager.removeIntermediateNode(expression, parentNode, statement);
    }

    onConnectionCallback(connection) {
        this.transformNodeManager.createStatementEdge(connection);
    }

    onDisconnectionCallback(connection) {
        this.transformNodeManager.removeStatementEdge(connection);
    }

    onConnectionsScroll() {
        this.mapper.repaintDraggingArrows();
    }

    onConnectPointMouseEnter(endpoint) {
        if (!this.mapper.isConnectionDragging()) {
            return;
        }
        this.mapper.setDroppingTarget(endpoint);
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

        if (ASTFactory.isVariableDefinitionStatement(statement)) {
            // TODO: handle this
            return;
        }

        if (!ASTFactory.isAssignmentStatement(statement)) {
            log.error('Invalid statement type in transform statement');
            return;
        }

        // There can be multiple left expressions.
        // E.g. : e.name, e.username = p.first_name;
        const leftExpression = statement.getLeftExpression();
        const { exp: rightExpression, isTemp } = this.transformNodeManager.getResolvedExpression(statement.getRightExpression(), statement);
        if (ASTFactory.isFieldBasedVarRefExpression(rightExpression) ||
              ASTFactory.isSimpleVariableReferenceExpression(rightExpression)) {
            _.forEach(leftExpression.getChildren(), (leftExpr) => {
                const sourceExprString = rightExpression.getExpressionString().trim();
                let sourceId = `${sourceExprString}:${viewId}`;
                let folded = false;
                if (!this.sourceElements[sourceId]) {
                    folded = true;
                    sourceId = this.getFoldedEndpointId(sourceExprString, viewId, 'source');
                }

                const targetExprString = leftExpr.getExpressionString().trim();
                let targetId = `${targetExprString}:${viewId}`;
                if (!this.targetElements[targetId]) {
                    folded = true;
                    targetId = this.getFoldedEndpointId(targetExprString, viewId, 'target');
                }

                this.drawConnection(sourceId, targetId, folded);
            });
        }

        if (ASTFactory.isFunctionInvocationExpression(rightExpression)
                    || ASTFactory.isBinaryExpression(rightExpression)
                    || ASTFactory.isUnaryExpression(rightExpression)) {
            this.drawIntermediateNode(leftExpression, rightExpression, statement);
        }
    }

    /**
     * Draw connections in intermediate nodes. Intermediate nodes are functions and operators.
     * @param {any} argumentExpressions argument expressions
     * @param {any} nodeExpression intermediate node expression
     * @param {any} statement enclosing statement
     * @memberof TransformExpanded
     */
    drawIntermediateNode(argumentExpressions, nodeExpression, statement) {
        if (!ASTFactory.isFunctionInvocationExpression(nodeExpression)
            && !ASTFactory.isBinaryExpression(nodeExpression)
            && !ASTFactory.isUnaryExpression(nodeExpression)) {
            return;
        }

        const viewId = this.props.model.getID();
        let nodeDef;
        let nodeName;
        let paramExpressions = [];
        
        if (ASTFactory.isFunctionInvocationExpression(nodeExpression)) {
            nodeDef = this.transformNodeManager.getFunctionVertices(nodeExpression);
            nodeName = nodeExpression.getFunctionName();
            paramExpressions = nodeExpression.getChildren();
        } else {
            nodeDef = this.transformNodeManager.getOperatorVertices(nodeExpression);
            nodeName = nodeExpression.getOperator();
            if (ASTFactory.isBinaryExpression(nodeExpression)) {
                paramExpressions.push(nodeExpression.getLeftExpression());
            }
            paramExpressions.push(nodeExpression.getRightExpression());
        }
        
        if (_.isUndefined(nodeDef)) {
            // alerts.error('Definition for "' + nodeName + '" cannot be found');
            return;
        }
        if (nodeDef.parameters.length !== paramExpressions.length) {
            // alerts.warn('Inputs and mapping count does not match in "' + nodeName + '"');
        }

        const returnParams = nodeDef.returnParams;
        const nodeExpID = nodeExpression.getID();

        paramExpressions.forEach((expression, i) => {
            if (ASTFactory.isFunctionInvocationExpression(expression)
                    || ASTFactory.isBinaryExpression(expression)
                    || ASTFactory.isUnaryExpression(expression)) {
                this.drawInnerIntermediateNode(nodeExpression, expression, nodeDef, i, statement);
            } else {
                let target;
                let source;
                const { exp, isTemp} = this.transformNodeManager.getResolvedExpression(expression, statement);
                expression = exp;
                if (ASTFactory.isKeyValueExpression(expression.children[0])) {
                    // if parameter is a key value expression, iterate each expression and draw connections
                    _.forEach(expression.children, (propParam) => {
                        source = this.getConnectionProperties('source', propParam.children[1]);
                        target = this.getConnectionProperties('target', nodeDef.getParameters()[i]);
                        target.targetProperty.push(propParam.children[0].getVariableName());
                        const typeDef = _.find(this.state.vertices, { typeName: nodeDef.getParameters()[i].type });
                        const propType = _.find(typeDef.properties, { name: propParam.children[0].getVariableName() });
                        target.targetType.push(propType.type);
                        this.drawConnection(statement.getID() + nodeExpression.getID(), source, target);
                    });
                } else {
                    // expression = this.transformNodeManager.getResolvedExpression(expression);
                    let sourceId = `${expression.getExpressionString().trim()}:${viewId}`;
                    let folded = false;
                    if (!this.sourceElements[sourceId]) {
                        folded = true;
                        sourceId = this.getFoldedEndpointId(expression.getExpressionString().trim(), viewId, 'source');
                    }

                    let targetId = `${nodeExpID}:${i}:${viewId}`;

                    if (!this.targetElements[targetId]) {
                        // function is folded
                        folded = true;
                        targetId = `${nodeExpID}:${viewId}`;
                    }

                    this.drawConnection(sourceId, targetId, folded);
                }
            }
        });

        if (nodeDef.returnParams.length !== argumentExpressions.getChildren().length) {
            // alerts.warn('Function inputs and mapping count does not match in "' + func.getName() + '"');
        }
        _.forEach(argumentExpressions.getChildren(), (expression, i) => {
            if (!returnParams[i]) {
                return;
            }
            let folded = false;

            let sourceId = `${nodeExpID}:${i}:return:${viewId}`;
            if (!this.sourceElements[sourceId]) {
                // function is folded
                folded = true;
                sourceId = `${nodeExpID}:${viewId}`;
            }


            let targetId = `${expression.getExpressionString().trim()}:${viewId}`;
            if (!this.targetElements[targetId]) {
                folded = true;
                targetId = this.getFoldedEndpointId(expression.getExpressionString().trim(), viewId, 'target');
            }

            this.drawConnection(sourceId, targetId, folded);
        });
        this.mapper.reposition(this.props.model.getID());
    }

    /**
     * Draw nested intermediate nodes
     * @param {any} parentNodeExpression parent node expression
     * @param {any} nodeExpression node expression
     * @param {any} parentNodeDefinition parent node definition
     * @param {any} parentParameterIndex parameter index of the parent node
     * @param {any} statement enclosed statement
     * @memberof TransformExpanded
     */
    drawInnerIntermediateNode(parentNodeExpression, nodeExpression, parentNodeDefinition,
                                      parentParameterIndex, statement) {
        if (!ASTFactory.isFunctionInvocationExpression(nodeExpression)
                && !ASTFactory.isBinaryExpression(nodeExpression)
                && !ASTFactory.isUnaryExpression(nodeExpression)) {
            return;
        }
        const viewId = this.props.model.getID();
        const nodeExpID = nodeExpression.getID();
        let nodeDef;
        let nodeName;
        let paramExpressions = [];

        if (ASTFactory.isFunctionInvocationExpression(nodeExpression)) {
            nodeDef = this.transformNodeManager.getFunctionVertices(nodeExpression);
            nodeName = nodeExpression.getFunctionName();
            paramExpressions = nodeExpression.getChildren();
        } else {
            nodeDef = this.transformNodeManager.getOperatorVertices(nodeExpression);
            nodeName = nodeExpression.getOperator();
            if (ASTFactory.isBinaryExpression(nodeExpression)) {
                paramExpressions.push(nodeExpression.getLeftExpression());
            }
            paramExpressions.push(nodeExpression.getRightExpression());
        }

        if (_.isUndefined(nodeDef)) {
            // alerts.error(
            // 'Function definition for "' + nodeName + '" cannot be found');
            return;
        }

        if (nodeDef.parameters.length !== paramExpressions.length) {
            // alerts.warn('Function inputs and mapping count does not match in "' + nodeName + '"');
        }

        paramExpressions.forEach((expression, i) => {
            if (ASTFactory.isFunctionInvocationExpression(expression)
                || ASTFactory.isBinaryExpression(expression)
                || ASTFactory.isUnaryExpression(expression)) {
                this.drawInnerIntermediateNode(nodeExpression, expression, nodeDef, i, statement);
            } else {
                let sourceId = `${expression.getExpressionString().trim()}:${viewId}`;
                let folded = false;
                if (!this.sourceElements[sourceId]) {
                    folded = true;
                    sourceId = this.getFoldedEndpointId(expression.getExpressionString().trim(), viewId, 'source');
                }

                let targetId = `${nodeExpID}:${i}:${viewId}`;
                if (!this.targetElements[targetId]) {
                    // function is folded
                    folded = true;
                    targetId = `${nodeExpID}:${viewId}`;
                }

                this.drawConnection(sourceId, targetId, folded);
            }
        });

        if (!parentNodeDefinition) {
            return;
        }

        let folded = false;

        let sourceId = `${nodeExpID}:0:return:${viewId}`;
        if (!this.sourceElements[sourceId]) {
            // function is folded
            folded = true;
            sourceId = `${nodeExpID}:${viewId}`;
        }

        const parentNodeID = parentNodeExpression.getID();
        let targetId = `${parentNodeID}:${parentParameterIndex}:${viewId}`;
        if (!this.targetElements[targetId]) {
            // function is folded
            folded = true;
            targetId = `${parentNodeID}:${viewId}`;
        }

        this.drawConnection(sourceId, targetId, folded);
        this.mapper.reposition(this.props.model.getID());
    }

    getFoldedEndpointId(exprString, viewId, type = 'source') {
        let currentExprString = exprString;
        let endpointId = `${currentExprString}:${viewId}`;

        while (exprString.includes('.') && !this[`${type}Elements`][endpointId]) {
            const newExprString = currentExprString.slice(0, currentExprString.lastIndexOf('.'));
            if (newExprString === currentExprString) {
                break;
            }
            currentExprString = newExprString;
            endpointId = `${currentExprString}:${viewId}`;
        }

        return endpointId;
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

    drawConnection(sourceId, targetId, folded) {
        this.mapper.addConnection(sourceId, targetId, folded);
    }

    createComplexProp(structName, expression) {
        const prop = {};
        prop.names = [];
        prop.types = [];

        if (ASTFactory.isFieldBasedVarRefExpression(expression)) {
            const fieldName = expression.getFieldName();
            const structDef = _.find(this.state.vertices, { name: structName });
            if (_.isUndefined(structDef)) {
                // alerts.error('Struct definition for variable "' + structName + '" cannot be found');
                return;
            }
            const structField = _.find(structDef.properties, { name: fieldName });
            if (_.isUndefined(structField)) {
                // alerts.error('Struct field "' + fieldName + '" cannot be found in variable "' + structName + '"');
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
        this.transformNodeManager.setTransformStmt(this.props.model);

        this.mapper.disconnectAll();

        let sourceKeys = Object.keys(this.sourceElements);
        sourceKeys.forEach((key) => {
            const { element, input } = this.sourceElements[key];
            if (!element) {
                delete this.sourceElements[key];
            }
        });

        let targetKeys = Object.keys(this.targetElements);
        targetKeys.forEach((key) => {
            const { element, output } = this.targetElements[key];
            if (!element) {
                delete this.targetElements[key];
            }
        });

        sourceKeys = Object.keys(this.sourceElements);
        targetKeys = Object.keys(this.targetElements);

        sourceKeys.forEach((key) => {
            const { element, input } = this.sourceElements[key];
            this.mapper.addSource(element, input, false);
        });

        targetKeys.forEach((key) => {
            const { element, output } = this.targetElements[key];
            this.mapper.addTarget(element, output, false,
                    this.transformNodeManager.isConnectionValid.bind(this.transformNodeManager));
        });

        _.forEach(this.props.model.getChildren(), (statement) => {
            this.createConnection(statement);
        });

        this.mapper.connectionsAdded();

        sourceKeys.forEach((key) => {
            const { element, input } = this.sourceElements[key];
            this.mapper.addSource(element, input, true);
        });

        this.markConnectedEndpoints();

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

        this.mapper.reposition(this.props.model.getID());

        this.mapper.onConnectionAborted((con, ev) => {
            const targetKeys = Object.keys(this.targetElements);
            targetKeys.forEach((key) => {
                const { element, output } = this.targetElements[key];
                element.classList.remove('drop-not-valid');
                element.classList.remove('drop-valid');
            });
            this.transformOverlayDraggingContentDiv.classList.remove('drop-not-valid');
            this.transformOverlayDraggingContentDiv.classList.remove('drop-valid');

            clearInterval(this.scrollTimer);

            const {clientX:x, clientY:y} = ev;
            const { element, output } = this.findTargetAt({x, y});

            if (!output) {
                // connection is not dropped on a target. No need of more processing
                return;
            }

            const input = con.getParameters().input;
            const isValid = this.transformNodeManager.isConnectionValid(input.type, output.type);

            if (!isValid) {
                return;
            }

            const connection = this.mapper.getConnectionObject(input, output);
            this.transformNodeManager.createStatementEdge(connection);
        });
    }

    componentWillUnmount() {
        if (this.scrollTimer) {
            clearInterval(this.scrollTimer);
        }
    }

    onDragLeave(e) {
        if (!this.mapper.isConnectionDragging()) {
            return;
        }
        const boundingRect = this.transformOverlayDraggingContentDiv.getBoundingClientRect();
        const middle = boundingRect.top + ((boundingRect.bottom - boundingRect.top) / 2);
        let offset = -5;
        if (e.pageY > middle) {
            offset = 5;
        }

        this.scrollTimer = setInterval(() => {
            this.scroll.scrollTop(this.scroll.getScrollTop() + offset);
        }, 10);
    }

    onDragEnter(e) {
        if (!this.mapper.isConnectionDragging()) {
            return;
        }

        clearInterval(this.scrollTimer);
    }

    onMouseMove(e) {
        const {clientX:x, clientY:y} = e;
        if (!this.mapper.isConnectionDragging()) {
            return;
        }

        const { element, output } = this.findTargetAt({x, y});
        if (!output) {
            // has moved outside of target elements
            if (this._hoveredTarget) {
                this.transformOverlayDraggingContentDiv.classList.remove('drop-not-valid');
                this.transformOverlayDraggingContentDiv.classList.remove('drop-valid');
                this._hoveredTarget.classList.remove('drop-not-valid');
                this._hoveredTarget.classList.remove('drop-valid');
                this._hoveredTarget = null;
            }
            return;
        }

        if (this._hoveredTarget === element) {
            // still inside the same element. no need to do anything
            return;
        }

        if (this._hoveredTarget) {
            this._hoveredTarget.classList.remove('drop-valid');
            this._hoveredTarget.classList.add('drop-not-valid');
        }

        this._hoveredTarget = element;

        const conn = this.mapper.getDraggingConnection();
        const isValid = this.transformNodeManager.isConnectionValid(conn.getParameters().input.type, output.type);

        if (!isValid) {
            element.classList.remove('drop-valid');
            element.classList.add('drop-not-valid');
            this.transformOverlayDraggingContentDiv.classList.remove('drop-valid');
            this.transformOverlayDraggingContentDiv.classList.add('drop-not-valid');
        } else {
            element.classList.remove('drop-not-valid');
            element.classList.add('drop-valid');
            this.transformOverlayDraggingContentDiv.classList.remove('drop-not-valid');
            this.transformOverlayDraggingContentDiv.classList.add('drop-valid');
        }
    }

    onTransformDropZoneActivate(e) {
        const dragDropManager = this.context.dragDropManager;
        const dropTarget = this.props.model;

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
        if (suggestionValue === '') {
            const variableDefinitionStatement = this.transformNodeManager.addNewVariable('source');
            const varVertex = ({
                name: variableDefinitionStatement.getVariableDef().getName(),
                displayName: variableDefinitionStatement.getVariableDef().getName(),
                type: variableDefinitionStatement.getVariableDef().getTypeName(),
                varDeclarationString: variableDefinitionStatement.getStatementString(),
            });
            this.state.vertices.push(varVertex);
            this.addSource(variableDefinitionStatement.getVariableDef().getName());
        } else {
            this.setState({
                selectedSource: suggestionValue,
            });
            this.addSource(suggestionValue);
        }
    }

    onTargetSelect(e, { suggestionValue }) {
        if (suggestionValue === '') {
            const variableDefinitionStatement = this.transformNodeManager.addNewVariable('target');
            const varVertex = ({
                name: variableDefinitionStatement.getVariableDef().getName(),
                displayName: variableDefinitionStatement.getVariableDef().getName(),
                type: variableDefinitionStatement.getVariableDef().getTypeName(),
                varDeclarationString: variableDefinitionStatement.getStatementString(),
            });
            this.state.vertices.push(varVertex);
            this.addTarget(variableDefinitionStatement.getVariableDef().getName());
        } else {
            this.setState({
                selectedTarget: suggestionValue,
            });
            this.addTarget(suggestionValue);
        }
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

    removeSourceType(type) {
        _.remove(this.state.vertices, (vertex) => {
            return vertex.name === type.name && vertex.varDeclarationString;
        });
        this.transformNodeManager.removeSourceType(type);
    }

    removeTargetType(type) {
        _.remove(this.state.vertices, (vertex) => {
            return vertex.name === type.name && vertex.varDeclarationString;
        });
        this.transformNodeManager.removeTargetType(type);
    }

    addSource(selectedSource) {
        const inputDef = ASTFactory
                                .createSimpleVariableReferenceExpression({ variableName: selectedSource });
        if (this.isVertexExist(selectedSource)) {
            const inputs = this.props.model.getInput();
            inputs.push(inputDef);
            this.props.model.setInput(inputs);
            this.setState({ typedSource: '' });
        }
    }

    addTarget(selectedTarget) {
        const outDef = ASTFactory
                                .createSimpleVariableReferenceExpression({ variableName: selectedTarget });
        if (this.isVertexExist(selectedTarget)) {
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

            const fileData = this.context.designView.context.editor.props.file;
            const position = this.props.model.getPosition();

            const options = {
                textDocument: fileData.content,
                position: {
                    line: position.stopLine,
                    character: position.stopOffset,
                },
                fileName: fileData.name,
                filePath: fileData.path,
                packageName: fileData.packageName,
            };

            langServerClient.getCompletions(options, (response) => {
                const completions = response.result.filter((completionItem) => {
                    // all variables have type as 9 as per the declaration in lang server
                    return ((completionItem.kind === 9)
                        && !completionItem.label.startsWith('__temp')
                        && !completionItem.label.startsWith('__output')
                        && completionItem.label !== '_');
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
                const varDefinations = this.props.model.filterChildren(ASTFactory.isVariableDefinitionStatement);
                _.forEach(transformVars, (arg) => {
                    const structDef = this.transformNodeManager.getStructDefinition(arg.pkgName, arg.type);

                    if (structDef) {
                        arg.type = ((arg.pkgName) ? (arg.pkgName + ':') : '') + arg.type;
                        const structVar = this.transformNodeManager.getStructType(
                            arg.name, arg.type, structDef);
                        vertices.push(structVar);
                    }

                    if (!structDef) {
                        const variableType = {};
                        variableType.name = arg.name;
                        variableType.displayName = arg.name;
                        variableType.varDeclarationString = '';
                        _.forEach(varDefinations, (varDef) => {
                            if (variableType.name === varDef.getLeftExpression().getVariableName()) {
                                variableType.varDeclarationString = varDef.getStatementString();
                            }
                        });

                        if (arg.constraint !== undefined) {
                            variableType.type = arg.type + '<'
                                        + ((arg.constraint.packageName) ? arg.constraint.packageName + ':' : '')
                                        + arg.constraint.type + '>';
                            variableType.constraintType = arg.constraint;
                            const constraintDef = this.transformNodeManager.getStructDefinition(
                                arg.constraint.packageName, arg.constraint.type);
                            if (constraintDef !== undefined) {
                                const constraintVar = this.transformNodeManager.getStructType(
                                    arg.name, variableType.type, constraintDef);
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
                            vertices.push(variableType);
                        }
                    }
                });
                // set state with new vertices
                if (!_.isEqual(vertices, this.state.vertices)) {
                    this.setState({ vertices });
                }
            });
        }).catch((error) => {
            // alerts.error('Could not initialize transform statement view ' + error);
        });
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

    isVertexExist(currentSelection) {
        const sourceSelection = _.find(this.state.vertices, { name: currentSelection });
        if (_.isUndefined(sourceSelection)) {
            // alerts.error('Mapping source "' + currentSelection + '" cannot be found');
            return false;
        }
        return true;
    }

    findTargetAt({x, y}) {
        const targetKeys = Object.keys(this.targetElements);
        let foundOutput = {};
        targetKeys.forEach((key) => {
            const { element, output } = this.targetElements[key];
            const connectPointRect = element.getBoundingClientRect();

            const { left, right, top, bottom } = connectPointRect;
            if(left < x && x < right && top < y && y < bottom){
                foundOutput = { element, output };
            }
        });

        return foundOutput;
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

    getIntermediateNodes(nodeExpression, statement, intermediateNodes = [], parentNode) {
        if (ASTFactory.isFunctionInvocationExpression(nodeExpression)) {
            const func = this.transformNodeManager.getFunctionVertices(nodeExpression);
            if (_.isUndefined(func)) {
                // alerts.error('Function definition for "' +
                //     functionInvocationExpression.getFunctionName() + '" cannot be found');
                return [];
            }
            nodeExpression.getChildren().forEach((child) => {
                this.getIntermediateNodes(child, statement, intermediateNodes, nodeExpression);
            });
            intermediateNodes.push({
                type: 'function',
                func,
                parentNode,
                statement,
                funcInv: nodeExpression,
            });
            return intermediateNodes;
        } else if (ASTFactory.isBinaryExpression(nodeExpression)
                      || ASTFactory.isUnaryExpression(nodeExpression)) {
            const operator = this.transformNodeManager.getOperatorVertices(nodeExpression);
            if (ASTFactory.isBinaryExpression(nodeExpression)) {
                this.getIntermediateNodes(nodeExpression.getLeftExpression(), statement,
                                                            intermediateNodes, nodeExpression);
            }
            this.getIntermediateNodes(nodeExpression.getRightExpression(), statement,
                                                intermediateNodes, nodeExpression);
            intermediateNodes.push({
                type: 'operator',
                operator,
                parentNode,
                statement,
                opExp: nodeExpression,
            });
            return intermediateNodes;
        } else {
            return [];
        }
    }

    updateVariable(varName, statementString, type) {
        const isUpdated = this.transformNodeManager.updateVariable(this.props.model, varName, statementString, type);
        this.loadVertices();
        if (!isUpdated) {
            alerts.error('Invalid value for variable ' + varName);
            return false;
        }
        return true;
    }

    markConnectedEndpoints() {
        $('.variable-endpoint').removeClass('fw-circle').addClass('fw-circle-outline');
        $('.variable-endpoint.jtk-connected').removeClass('fw-circle-outline').addClass('fw-circle');
    }

    render() {
        const vertices = this.state.vertices.filter(vertex => (!vertex.isInner));
        const inputNodes = this.props.model.getInput();
        const outputNodes = this.props.model.getOutput();
        const inputs = [];
        const outputs = [];
        const intermediateNodes = [];

        if (this.state.vertices.length > 0) {
            inputNodes.forEach((inputNode) => {
                const name = inputNode.getVariableName();
                const sourceSelection = _.find(vertices, { name });
                if (_.isUndefined(sourceSelection)) {
                    // alerts.error('Mapping source "' + name + '" cannot be found');
                    return;
                }
                sourceSelection.endpointKind = 'input';
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
                targetSelection.endpointKind = 'output';
                _.remove(vertices, (vertex) => { return vertex.name === targetSelection.name; });
                outputs.push(targetSelection);
            });

            this.props.model.getChildren().forEach((child) => {
                if (!ASTFactory.isAssignmentStatement(child)) {
                    return; // TODO: handle var def stmts as well
                }
                const { exp: rightExpression, isTemp } = this.transformNodeManager
                                                    .getResolvedExpression(child.getRightExpression(), child);

                if (ASTFactory.isFunctionInvocationExpression(rightExpression)
                    || ASTFactory.isBinaryExpression(rightExpression)
                    || ASTFactory.isUnaryExpression(rightExpression)) {
                    if (!isTemp) {
                        // only add if the function invocation is not pre available.
                        // this check is required for instances where the function invocations
                        // are used via temporary variables
                        intermediateNodes.push(...this.getIntermediateNodes(rightExpression, child));
                    }
                }
            });
        }

        return (
            <div
                className='transformOverlay'
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
                <div className="select-source">
                    <SuggestionsDropdown
                        value={this.state.typedSource}
                        onChange={this.onSourceInputChange}
                        onEnter={this.onSourceInputEnter}
                        suggestionsPool={vertices}
                        placeholder='Select Source'
                        onSuggestionSelected={this.onSourceSelect}
                        type='source'
                    />
                </div>
                <div className="select-target">
                    <SuggestionsDropdown
                        value={this.state.typedTarget}
                        onChange={this.onTargetInputChange}
                        onEnter={this.onTargetInputEnter}
                        suggestionsPool={vertices}
                        placeholder='Select Target'
                        onSuggestionSelected={this.onTargetSelect}
                        type='target'
                    />
                </div>
                <div
                    id={`transformOverlay-content-dragging-${this.props.model.getID()}`}
                    ref={(div) => { this.transformOverlayDraggingContentDiv = div; }}
                    className='transform-dragging-connections'
                    onMouseLeave={this.onDragLeave}
                    onMouseEnter={this.onDragEnter}
                    onMouseMove={this.onMouseMove}
                >
                    <div className="middle-content-frame" />
                    <Scrollbars
                        ref={ scroll => {this.scroll = scroll;} }
                        onScroll={this.onConnectionsScroll}
                    >
                        <div
                            id={`transformOverlay-content-${this.props.model.getID()}`}
                            ref={(div) => { this.transformOverlayContentDiv = div; }}
                            className='transform-connections'
                        >
                            <div className='left-content'>
                                <div className="leftType">
                                    <Tree
                                        viewId={this.props.model.getID()}
                                        endpoints={inputs}
                                        type='source'
                                        makeConnectPoint={this.recordSourceElement}
                                        removeTypeCallbackFunc={this.removeSourceType}
                                        updateVariable={this.updateVariable}
                                        onEndpointRemove={this.removeEndpoint}
                                        onConnectPointMouseEnter={this.onConnectPointMouseEnter}
                                        foldEndpoint={this.foldEndpoint}
                                        foldedEndpoints={this.state.foldedEndpoints}
                                    />
                                </div>
                            </div>
                            <div className="middle-content">
                                {
                                    intermediateNodes.map((node) => {
                                        if (node.type === 'function') {
                                            return (<FunctionInv
                                                key={node.funcInv.getID()}
                                                func={node.func}
                                                statement={node.statement}
                                                parentNode={node.parentNode}
                                                funcInv={node.funcInv}
                                                recordSourceElement={this.recordSourceElement}
                                                recordTargetElement={this.recordTargetElement}
                                                viewId={this.props.model.getID()}
                                                onEndpointRemove={this.removeEndpoint}
                                                onFunctionRemove={this.removeIntermediateNode}
                                                onConnectPointMouseEnter={this.onConnectPointMouseEnter}
                                                foldEndpoint={this.foldEndpoint}
                                                foldedEndpoints={this.state.foldedEndpoints}
                                                isCollapsed={this.state.foldedFunctions[node.funcInv.getID()]}
                                                onHeaderClick={this.foldFunction}
                                            />);
                                        }
                                        return (<Operator
                                            key={node.opExp.getID()}
                                            operator={node.operator}
                                            statement={node.statement}
                                            parentNode={node.parentNode}
                                            opExp={node.opExp}
                                            recordSourceElement={this.recordSourceElement}
                                            recordTargetElement={this.recordTargetElement}
                                            viewId={this.props.model.getID()}
                                            onEndpointRemove={this.removeEndpoint}
                                            onOperatorRemove={this.removeIntermediateNode}
                                            onConnectPointMouseEnter={this.onConnectPointMouseEnter}
                                        />);
                                    })
                                }
                            </div>
                            <div className='right-content'>
                                <div className='rightType'>
                                    <Tree
                                        viewId={this.props.model.getID()}
                                        endpoints={outputs}
                                        type='target'
                                        makeConnectPoint={this.recordTargetElement}
                                        removeTypeCallbackFunc={this.removeTargetType}
                                        updateVariable={this.updateVariable}
                                        onConnectPointMouseEnter={this.onConnectPointMouseEnter}
                                        foldEndpoint={this.foldEndpoint}
                                        foldedEndpoints={this.state.foldedEndpoints}
                                        onEndpointRemove={this.removeEndpoint}
                                    />
                                </div>
                            </div>
                            <div id='transformContextMenu' className='transformContextMenu' />
                        </div>
                    </Scrollbars>
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
