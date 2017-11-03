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
import { Scrollbars } from 'react-custom-scrollbars';
import log from 'log';
import TransformRender from './transform-render';
import TransformNodeManager from './transform-node-manager';
import SuggestionsDropdown from './transform-endpoints-dropdown';
import TransformerNode from '../../../../../model/tree/abstract-tree/transformer-node';
import Tree from './tree';
import FunctionInv from './function';
import Operator from './operator';
import { getLangServerClientInstance } from './../../../../../../langserver/lang-server-client-controller';
import { getResolvedTypeData } from './../../../../../../langserver/lang-server-utils';
import TreeUtil from '../../../../../model/tree-util';
import DropZone from '../../../../../drag-drop/DropZone';
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
            foldedOperators: {},
        };
        this.sourceElements = {};
        this.targetElements = {};
        this.transformNodeManager = new TransformNodeManager(
            {
                typeLattice: context.environment.getTypeLattice(),
                transformStmt: props.model,
                environment: context.environment,
            });
        this.loadVertices();

        this.onSourceSelect = this.onSourceSelect.bind(this);
        this.onTargetSelect = this.onTargetSelect.bind(this);
        this.onSourceAdd = this.onSourceAdd.bind(this);
        this.onTargetAdd = this.onTargetAdd.bind(this);
        this.onClose = this.onClose.bind(this);
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
        this.foldOperator = this.foldOperator.bind(this);
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

    foldOperator(key) {
        this.setState(
            {
                foldedOperators: _.extend(this.state.foldedOperators, {
                    [key]: !this.state.foldedOperators[key],
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

        if (TreeUtil.isComment(statement)) {
            return;
        }

        let variables = [];
        let stmtExp;

        if (TreeUtil.isAssignment(statement)) {
            variables = statement.getVariables();
            stmtExp = statement.getExpression();
        } else if (TreeUtil.isVariableDef(statement)) {
            variables.push(statement.getVariable().getName());
            stmtExp = statement.getVariable().getInitialExpression();
        } else {
            log.error('Invalid statement type in transformer');
            return;
        }

        const { exp: expression, isTemp } = this.transformNodeManager.getResolvedExpression(stmtExp, statement);
        if (!isTemp && (TreeUtil.isFieldBasedAccessExpr(expression) || TreeUtil.isSimpleVariableRef(expression))) {
            variables.forEach((variable) => {
                // TODO : remove replace whitespace once its handled from backend
                const sourceExprString = expression.getSource().replace(/ /g, '').trim();
                let sourceId = `${sourceExprString}:${viewId}`;
                let folded = false;
                if (!this.sourceElements[sourceId]) {
                    folded = true;
                    sourceId = this.getFoldedEndpointId(sourceExprString, viewId, 'source');
                }
                // TODO : remove replace whitespace once its handled from backend
                const targetExprString = variable.getSource().replace(/ /g, '').trim();
                let targetId = `${targetExprString}:${viewId}`;
                if (!this.targetElements[targetId]) {
                    folded = true;
                    targetId = this.getFoldedEndpointId(targetExprString, viewId, 'target');
                }

                this.drawConnection(sourceId, targetId, folded);
            });
        }
        if (TreeUtil.isInvocation(expression) || TreeUtil.isBinaryExpr(expression)
            || TreeUtil.isUnaryExpr(expression) || TreeUtil.isTernaryExpr(expression)) {
            this.drawIntermediateNode(variables, expression, statement, isTemp);
        }
    }

    /**
     * Draw connections in intermediate nodes. Intermediate nodes are functions and operators.
     * @param {[Expression]} argumentExpressions argument expressions
     * @param {Expression} nodeExpression intermediate node expression
     * @param {Statement} statement enclosing statement
     * @param {boolean} isTemp is a temporary expression
     * @memberof TransformExpanded
     */
    drawIntermediateNode(argumentExpressions, nodeExpression, statement, nodeExpIsTemp = false) {
        const viewId = this.props.model.getID();
        let nodeDef;
        let nodeName;
        let paramExpressions = [];
        let receiver;

        if (TreeUtil.isInvocation(nodeExpression)) {
            nodeDef = this.transformNodeManager.getFunctionVertices(nodeExpression);
            nodeName = nodeExpression.getFunctionName();
            paramExpressions = nodeExpression.argumentExpressions;
            receiver = nodeExpression.expression
        } else if (TreeUtil.isTernaryExpr(nodeExpression)) {
            nodeDef = this.transformNodeManager.getOperatorVertices(nodeExpression);
            nodeName = nodeExpression.getOperatorKind();
            paramExpressions.push(nodeExpression.getCondition());
            paramExpressions.push(nodeExpression.getThenExpression());
            paramExpressions.push(nodeExpression.getElseExpression());
        } else if (TreeUtil.isBinaryExpr(nodeExpression)) {
            nodeDef = this.transformNodeManager.getOperatorVertices(nodeExpression);
            nodeName = nodeExpression.getOperatorKind();
            paramExpressions.push(nodeExpression.getLeftExpression());
            paramExpressions.push(nodeExpression.getRightExpression());
        } else if (TreeUtil.isUnaryExpr(nodeExpression)) {
            nodeDef = this.transformNodeManager.getOperatorVertices(nodeExpression);
            nodeName = nodeExpression.getOperatorKind();
            paramExpressions.push(nodeExpression.getExpression());
        }

        if (_.isUndefined(nodeDef)) {
            this.context.alert.showError('Definition for "' + nodeName + '" cannot be found');
            return;
        }

        if (nodeDef.parameters.length !== paramExpressions.length) {
            this.context.alert.showWarning('Inputs and mapping count does not match in "' + nodeName + '"');
        }

        const returnParams = nodeDef.returnParams;
        const nodeExpID = nodeExpression.getID();

        if (!nodeExpIsTemp) {
            // if the node expression is a temp resolved expression, do not need to
            // draw the parameters
            if (receiver) {
                this.drawReceiverConnection(receiver, statement, nodeExpression, nodeDef);
            }

            paramExpressions.forEach((expression, i) => {
                const { exp, isTemp } = this.transformNodeManager.getResolvedExpression(expression, statement);
                expression = exp;
                if (TreeUtil.isInvocation(expression) || TreeUtil.isBinaryExpr(expression)
                    || TreeUtil.isUnaryExpr(expression)) {
                    this.drawInnerIntermediateNode(nodeExpression, expression, nodeDef, i, statement, isTemp);
                } else if ((!isTemp)
                    || (!TreeUtil.isInvocation(exp) || !TreeUtil.isBinaryExpr(exp) || !TreeUtil.isUnaryExpr(exp))) {
                    // a temp variable can still be drawable, if it is not a complex type. Unsafe conversions
                    // assigned to temp variables is one such case. The second check is to handle such scenarios.
                    // E.g. : var temp1, _ = <int> s1;
                    //        b = fuc(temp1);

                    if (TreeUtil.isLiteral(expression)) {
                        // TODO: implement default value logic
                    } else if (TreeUtil.isRecordLiteralExpr(expression)) {
                        this.drawKeyValueConnections(expression.getKeyValuePairs(), `${nodeExpID}:${i}`, nodeExpID);
                    } else {
                        // expression = this.transformNodeManager.getResolvedExpression(expression);
                        let sourceId = `${expression.getSource().trim()}:${viewId}`;
                        let folded = false;
                        if (!this.sourceElements[sourceId]) {
                            folded = true;
                            sourceId = this.getFoldedEndpointId(
                                expression.getSource().trim(), viewId, 'source');
                        }

                        let targetId = `${nodeExpID}:${i}:${viewId}`;
                        if (!this.targetElements[targetId]) {
                            // function is folded
                            folded = true;
                            targetId = `${nodeExpID}:${viewId}`;
                        }

                        this.drawConnection(sourceId, targetId, folded);
                    }
                } else {
                    log.error('Unhandled rendering scenario');
                }
            });
        }

        if (nodeDef.returnParams.length !== argumentExpressions.length) {
            this.context.alert.showWarning('Function inputs and mapping count does not match in "'
                                            + nodeDef.getName() + '"');
        }
        argumentExpressions.forEach((expression, i) => {
            const { exp, isTemp } = this.transformNodeManager.getResolvedExpression(expression, statement);
            if (isTemp) {
                return;
            }
            expression = exp;
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


            let targetId = `${expression.getSource().trim()}:${viewId}`;
            if (!this.targetElements[targetId]) {
                folded = true;
                targetId = this.getFoldedEndpointId(expression.getSource().trim(), viewId, 'target');
            }

            this.drawConnection(sourceId, targetId, folded);
        });
        this.mapper.reposition(this.props.model.getID());
    }

    drawKeyValueConnections(keyValues, parentName, nodeExpID) {
        const viewId = this.props.model.getID();
        keyValues.forEach((keyValue) => {
            const key = keyValue.getKey();
            const value = keyValue.getValue();

            const keyName = key.getSource().replace(/ /g, '').trim();

            if (TreeUtil.isSimpleVariableRef(value) || TreeUtil.isFieldBasedAccessExpr(value)) {
                let targetId = `${parentName}.${keyName}:${viewId}`;
                let folded = false;
                if (!this.targetElements[targetId]) {
                    folded = true;
                    targetId = this.getFoldedEndpointId(targetId, viewId, 'target');
                }

                if (!this.targetElements[targetId]) {
                    folded = true;
                    targetId = `${nodeExpID}:${viewId}`;
                }

                const valueName = value.getSource().replace(/ /g, '').trim();
                let sourceId = `${valueName}:${viewId}`;

                if (!this.sourceElements[sourceId]) {
                    folded = true;
                    sourceId = this.getFoldedEndpointId(sourceId, viewId, 'source');
                }

                this.drawConnection(sourceId, targetId, folded);
                return;
            }

            if (TreeUtil.isRecordLiteralExpr(value)) {
                this.drawKeyValueConnections(value.getKeyValuePairs(), `${parentName}.${keyName}`, nodeExpID);
            }
        })
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
                                      parentParameterIndex, statement, nodeExpIsTemp = false) {
        const viewId = this.props.model.getID();
        const nodeExpID = nodeExpression.getID();
        let nodeDef;
        let nodeName;
        let paramExpressions = [];
        let receiver;

        if (TreeUtil.isInvocation(nodeExpression)) {
            nodeDef = this.transformNodeManager.getFunctionVertices(nodeExpression);
            nodeName = nodeExpression.getFunctionName();
            paramExpressions = nodeExpression.argumentExpressions;
            receiver = nodeExpression.expression;
        } else if (TreeUtil.isBinaryExpr(nodeExpression)) {
            nodeDef = this.transformNodeManager.getOperatorVertices(nodeExpression);
            nodeName = nodeExpression.getOperatorKind();
            paramExpressions.push(nodeExpression.getLeftExpression());
            paramExpressions.push(nodeExpression.getRightExpression());
        } else if (TreeUtil.isUnaryExpr(nodeExpression)) {
            nodeDef = this.transformNodeManager.getOperatorVertices(nodeExpression);
            nodeName = nodeExpression.getOperatorKind();
            paramExpressions.push(nodeExpression.getExpression());
        } else {
            log.error('Invalid node type ' + nodeExpression.kind);
            return;
        }

        if (_.isUndefined(nodeDef)) {
            this.context.alert.showError('Function definition for "' + nodeName + '" cannot be found');
            return;
        }

        if (nodeDef.parameters.length !== paramExpressions.length) {
            this.context.alert.showWarning('Function inputs and mapping count does not match in "' + nodeName + '"');
        }

        if (receiver) {
            this.drawReceiverConnection(receiver, statement, nodeExpression, nodeDef);
        }

        paramExpressions.forEach((expression, i) => {
            const { exp, isTemp } = this.transformNodeManager.getResolvedExpression(expression, statement);
            expression = exp;
            if (TreeUtil.isInvocation(expression) || TreeUtil.isBinaryExpr(expression)
                    || TreeUtil.isUnaryExpr(expression)) {
                this.drawInnerIntermediateNode(nodeExpression, expression, nodeDef, i, statement, isTemp);
            } else {
                let sourceId = `${expression.getSource().trim()}:${viewId}`;
                let folded = false;
                if (!this.sourceElements[sourceId]) {
                    folded = true;
                    sourceId = this.getFoldedEndpointId(expression.getSource().trim(), viewId, 'source');
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

    /**
     * Draw connection to a receiver of a bound function
     */
    drawReceiverConnection (receiver, statement, nodeExpression, nodeDef) {
        const viewId = this.props.model.getID();
        const nodeExpID = nodeExpression.getID();
        const { exp:expression, isTemp } = this.transformNodeManager.getResolvedExpression(receiver, statement);
        if (TreeUtil.isInvocation(expression) || TreeUtil.isBinaryExpr(expression)
            || TreeUtil.isUnaryExpr(expression)) {
            this.drawInnerIntermediateNode(nodeExpression, expression, nodeDef, '0:receiver', statement, isTemp);
        } else if (TreeUtil.isSimpleVariableRef(expression) || TreeUtil.isFieldBasedAccessExpr(expression)) {
            let sourceId = `${expression.getSource().trim()}:${viewId}`;
            let folded = false;
            if (!this.sourceElements[sourceId]) {
                folded = true;
                sourceId = this.getFoldedEndpointId(
                    expression.getSource().trim(), viewId, 'source');
            }

            let targetId = `${nodeExpID}:0:receiver:${viewId}`;
            if (!this.targetElements[targetId]) {
                // function is folded
                folded = true;
                targetId = `${nodeExpID}:${viewId}`;
            }

            this.drawConnection(sourceId, targetId, folded);
        }
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
                this.context.alert.showError('Struct definition for variable "' + structName + '" cannot be found');
                return;
            }
            const structField = _.find(structDef.properties, { name: fieldName });
            if (_.isUndefined(structField)) {
                this.context.alert.showError('Struct field "' + fieldName + '" cannot be found in variable "'
                                              + structName + '"');
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

        this.props.model.getBody().getStatements().forEach((statement) => {
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

    componentDidMount() {
        this.mapper = new TransformRender(this.onConnectionCallback.bind(this),
            this.onDisconnectionCallback.bind(this), $(this.transformOverlayContentDiv));

        this.props.model.body.getStatements().forEach((statement) => {
            this.createConnection(statement);
        });

        this.mapper.connectionsAdded();
        this.markConnectedEndpoints();

        this.mapper.reposition(this.props.model.getID());

        const sourceKeys = Object.keys(this.sourceElements);
        sourceKeys.forEach((key) => {
            const { element, input } = this.sourceElements[key];
            this.mapper.addSource(element, input, true);
        });

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

    shouldComponentUpdate() {
        return !this.props.panelResizeInProgress;
    }

    componentWillReceiveProps() {
        if (this.props.panelResizeInProgress) {
            this.mapper.repaintEverything();
        }
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
        
        let offsetY = 0;
        if (e.pageY < (boundingRect.top + 10)) {
            offsetY = -5;
        } else if (e.pageY > (boundingRect.bottom - 10)) {
            offsetY = 5;
        }

        let offsetX = 0;
        if (e.pageX < (boundingRect.left + 10)) {
            offsetX = -5;
        } else if (e.pageX > (boundingRect.right - 10)) {
            offsetX = 5;
        }

        this.scrollTimer = setInterval(() => {
            this.vscroll.scrollTop(this.vscroll.getScrollTop() + offsetY);
            this.hscroll.scrollLeft(this.hscroll.getScrollLeft() + offsetX);
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

    canDrop(dragSource) {
        return TreeUtil.isAssignment(dragSource);
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
            const varDef = this.transformNodeManager.addNewVariable('source');
            const varVertex = ({
                name: varDef.getVariableName().getValue(),
                displayName: varDef.getVariableName().getValue(),
                type: varDef.getTypeNode().getTypeKind(),
                varDeclarationString: varDef.getSource(),
            });
            this.state.vertices.push(varVertex);
            this.addSource(varDef.getVariableName().getValue());
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
                name: variableDefinitionStatement.getVariableName().getValue(),
                displayName: variableDefinitionStatement.getVariableName().getValue(),
                type: variableDefinitionStatement.getTypeNode().getTypeKind(),
                varDeclarationString: variableDefinitionStatement.getSource(),
            });
            this.state.vertices.push(varVertex);
            this.addTarget(variableDefinitionStatement.getVariableName().getValue());
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

    /**
     * Add source to transform statement
     * @param {any} source source
     * @memberof TransformExpanded
     */
    addSource(source) {
        const vertex = this.state.vertices.filter((val) => { return val === source; })[0];
        if (vertex) {
            this.props.model.setSourceParam(this.transformNodeManager.createVariable('source', vertex));
            this.setState({ typedSource: '' });
        }
    }

    /**
     * Add target to transform statement
     * @param {any} target target
     * @memberof TransformExpanded
     */
    addTarget(target) {
        const vertex = this.state.vertices.filter((val) => { return val === target; })[0];
        if (vertex) {
            this.props.model.setReturnParameters([this.transformNodeManager.createVariable('target', vertex)]);
            this.setState({ typedTarget: '' });
        }
    }

    onClose() {
        const { designView } = this.context;
        designView.setTransformActive(false);
    }

    /**
     * TODO: Remove this after revisiting
     * Load vertices of the transform graph.
     * @memberof TransformExpanded
     */
    loadVertices(callback) {
        this.context.environment.getTypes().forEach((type) => {
            this.state.vertices.push(type);
        });
        if (callback) {
            callback();
        }
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
                if (property.typeName) {
                    this.convertFieldType(property.properties, type);
                    property.typeName = type;
                } else {
                    property.type = type;
                }
            });
        }
    }

    isVertexExist(currentSelection) {
        const sourceSelection = _.find(this.state.vertices, { name: currentSelection });
        if (_.isUndefined(sourceSelection)) {
            this.context.alert.showError('Mapping source "' + currentSelection + '" cannot be found');
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
        // TODO: check and remove this
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

    /**
     * Get intermediate node definitions
     * @param {any} nodeExpression node expression
     * @param {any} statement containing statements
     * @param {any} [intermediateNodes=[]] if it is nested intermediate nodes
     * @param {any} parentNode parent node
     * @returns intermediate nodes
     * @memberof TransformExpanded
     */
    getIntermediateNodes(nodeExpression, statement, intermediateNodes = [], parentNode) {
        if (TreeUtil.isInvocation(nodeExpression)) {
            const func = this.transformNodeManager.getFunctionVertices(nodeExpression);
            if (_.isUndefined(func)) {
                this.context.alert.showError('Function definition for "' +
                     nodeExpression.getFunctionName() + '" cannot be found');
                return [];
            }
            nodeExpression.argumentExpressions.forEach((arg) => {
                this.getIntermediateNodes(arg, statement, intermediateNodes, nodeExpression);
            });
            
            if (nodeExpression.expression) {
                this.getIntermediateNodes(nodeExpression.expression, statement, intermediateNodes, nodeExpression);
            }

            intermediateNodes.push({
                type: 'function',
                func,
                parentNode,
                statement,
                funcInv: nodeExpression,
            });
            return intermediateNodes;
        } else if (TreeUtil.isTernaryExpr(nodeExpression)) {
            const operator = this.transformNodeManager.getOperatorVertices(nodeExpression);
            this.getIntermediateNodes(nodeExpression.getCondition(), statement,
                                            intermediateNodes, nodeExpression);
            this.getIntermediateNodes(nodeExpression.getThenExpression(), statement,
                                            intermediateNodes, nodeExpression);
            this.getIntermediateNodes(nodeExpression.getElseExpression(), statement,
                                            intermediateNodes, nodeExpression);
            intermediateNodes.push({
                type: 'operator',
                operator,
                parentNode,
                statement,
                opExp: nodeExpression,
            });
            return intermediateNodes;
        } else if (TreeUtil.isBinaryExpr(nodeExpression)) {
            const operator = this.transformNodeManager.getOperatorVertices(nodeExpression);
            this.getIntermediateNodes(nodeExpression.getLeftExpression(), statement,
                                                intermediateNodes, nodeExpression);
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
        } else if (TreeUtil.isUnaryExpr(nodeExpression)) {
            const operator = this.transformNodeManager.getOperatorVertices(nodeExpression);
            this.getIntermediateNodes(nodeExpression.getExpression(), statement,
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
        if (this.transformNodeManager.updateVariable(this.props.model, varName, statementString, type,
                                                     this.state.vertices)) {
            this.isUpdatingVertices = true;
            this.loadVertices(() => { this.isUpdatingVertices = false; });
            return true;
        } else {
            this.context.alert.showError('Invalid value for variable');
            return false;
        }
    }

    markConnectedEndpoints() {
        $('.variable-endpoint').removeClass('fw-circle-in-circle').addClass('fw-circle-outline');
        $('.variable-endpoint.jtk-connected').removeClass('fw-circle-outline').addClass('fw-circle-in-circle');
    }

    getVerticeData(varNode) {
        if (Array.isArray(varNode)) {
            return varNode.map((varN) => {
                return this.getVerticeData(varN);
            });
        } else {
            const pkgAlias = (varNode.typeNode.packageAlias) ? varNode.typeNode.packageAlias.value : '';
            let type = (varNode.typeNode.typeName) ? varNode.typeNode.typeName.value : varNode.typeNode.typeKind;
            const name = varNode.name.value;
            const varDefinitions = this.props.model.getBody().filterStatements(TreeUtil.isVariableDef);
            const structDef = this.transformNodeManager.getStructDefinition(pkgAlias, type);

            if (structDef) {
                type = ((pkgAlias) ? (pkgAlias + ':') : '') + type;
                return this.transformNodeManager.getStructType(name, type, structDef);
            }

            const variableType = {};
            variableType.name = name;
            variableType.displayName = name;
            variableType.varDeclarationString = '';
            _.forEach(varDefinitions, (varDef) => {
                if (variableType.name === varDef.getVariableName().getValue()) {
                    variableType.varDeclarationString = varDef.getSource();
                }
            });

            if (varNode.typeNode.constraint) {
                variableType.type = varNode.getSource();
                variableType.constraintType = varNode.typeNode.constraint;
                const conPkgAlias = (varNode.typeNode.constraint.packageAlias) ?
                                    varNode.typeNode.constraint.packageAlias.value : '';
                const conType = (varNode.typeNode.constraint.typeName) ?
                                    varNode.typeNode.constraint.typeName.value : varNode.typeNode.constraint.typeKind;
                const baseType = (varNode.typeNode.type) ? varNode.typeNode.type.typeKind : '';

                const constraintDef = this.transformNodeManager.getStructDefinition(conPkgAlias, conType);
                if (constraintDef) {
                    const constraintVar = this.transformNodeManager
                                            .getStructType(name, variableType.type, constraintDef);
                    // For constraint types, the field types must be the same type as the variable and
                    // not the struct field types. E.g. : struct.name type maybe string but if it is a json,
                    // type has to be json and not string. Hence converting all field types to variable
                    // type.
                    // TODO : revisit this conversion if ballerina language supports constrained field
                    // access to be treated as the field type (i.e. as string from the struct field
                    // and not json)
                    this.convertFieldType(constraintVar.properties, baseType);

                    // constraint properties (fields) become variable fields
                    // variableType.properties = constraint.properties;
                    // variableType.constraint = constraint;
                    return constraintVar;
                }
            } else {
                variableType.type = type;
                return variableType;
            }
        }
    }


    render() {
        const { leftOffset } = this.props;
        const vertices = this.state.vertices;
        const sourceNode = this.props.model.getSourceParam();
        const returnNodes = this.props.model.getReturnParameters();
        const paramNodes = this.props.model.getParameters();

        const intermediateNodes = [];

        const source = this.getVerticeData(sourceNode);
        const params = this.getVerticeData(paramNodes);
        const returns = this.getVerticeData(returnNodes);

        source.endpointKind = 'input';
        params.forEach(p => {p.endpointKind = 'input'});
        returns.forEach(r => {r.endpointKind = 'output'});
        
        this.props.model.getBody().getStatements().forEach((stmt) => {
            let stmtExp;
            if (TreeUtil.isAssignment(stmt)) {
                stmtExp = stmt.getExpression();
            } else if (TreeUtil.isVariableDef(stmt)) {
                stmtExp = stmt.getVariable().getInitialExpression();
            } else {
                return;
            }

            const { exp: expression, isTemp } = this.transformNodeManager.getResolvedExpression(stmtExp, stmt);

            if (TreeUtil.isInvocation(expression) || TreeUtil.isBinaryExpr(expression)
                || TreeUtil.isUnaryExpr(expression) || TreeUtil.isTernaryExpr(expression)) {
                if (!isTemp) {
                    // only add if the function invocation is not pre available.
                    // this check is required for instances where the function invocations
                    // are used via temporary variables
                    intermediateNodes.push(...this.getIntermediateNodes(expression, stmt));
                }
            }
        });

        return (
            <div
                className='transformOverlay'
                style={{ paddingLeft: leftOffset }}
            >
                <div id='transformHeader' className='transform-header'>
                    <i onClick={this.onClose} className='fw fw-left icon close-transform' />
                    <p className='transform-header-text '>
                        <i className='transform-header-icon fw fw-type-converter' />
                        <b>{this.props.model.getSignature()}</b>
                    </p>
                </div>
                <div
                    id={`transformOverlay-content-dragging-${this.props.model.getID()}`}
                    ref={(div) => { this.transformOverlayDraggingContentDiv = div; }}
                    className='transform-dragging-connections'
                    onMouseLeave={this.onDragLeave}
                    onMouseEnter={this.onDragEnter}
                    onMouseMove={this.onMouseMove}
                >
                    <Scrollbars
                        ref={(scroll) => { this.hscroll = scroll; }}
                        onScroll={this.onConnectionsScroll}
                        renderThumbHorizontal={props => <div {...props} className='transform-horizontal-scroll'/>}
                    >
                        <div className='transform-content'>
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
                            <div className="middle-content-frame" />
                            <Scrollbars
                                style={{height: 'calc(100% - 50px)'}}
                                ref={(scroll) => { this.vscroll = scroll; }}
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
                                                endpoints={[source, ...params]}
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
                                    <DropZone
                                        baseComponent='div'
                                        className='middle-content'
                                        dropTarget={this.props.model.getBody()}
                                        canDrop={this.canDrop}
                                    >
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
                                                    isCollapsed={this.state.foldedFunctions[node.opExp.getID()]}
                                                    onFolderClick={this.foldFunction}
                                                />);
                                            })
                                        }
                                    </DropZone>
                                    <div className='right-content'>
                                        <div className='rightType'>
                                            <Tree
                                                viewId={this.props.model.getID()}
                                                endpoints={returns}
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
                    </Scrollbars>
                </div>
            </div>
        );
    }
}

TransformExpanded.propTypes = {
    model: PropTypes.instanceOf(TransformerNode).isRequired,
    panelResizeInProgress: PropTypes.bool.isRequired,
    leftOffset: PropTypes.number.isRequired,
    width: PropTypes.number.isRequired,
    height: PropTypes.number.isRequired,
};

TransformExpanded.contextTypes = {
    designView: PropTypes.instanceOf(Object).isRequired,
    environment: PropTypes.instanceOf(Object).isRequired,
    alert: PropTypes.shape({
        showInfo: PropTypes.func,
        showSuccess: PropTypes.func,
        showWarning: PropTypes.func,
        showError: PropTypes.func,
        closeEditor: PropTypes.func,
    }).isRequired,
};

export default TransformExpanded;
