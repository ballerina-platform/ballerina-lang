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
import { CHANGE_EVT_TYPES } from 'plugins/ballerina/views/constants';
import TransformRender from './transformer-render';
import TransformNodeManager from './transformer-node-manager';
import SuggestionsDropdown from './transformer-endpoints-dropdown';
import TransformerNode from '../../../../../model/tree/abstract-tree/transformer-node';
import Tree from './tree';
import FunctionInv from './function';
import Operator from './operator';
import NestedTransformer from './nested-transformer';
import TreeUtil from '../../../../../model/tree-util';
import DropZone from '../../../../../drag-drop/DropZone';
import './transformer-expanded.css';
import IterableList from './iterable-list';
import ConversionList from './conversion-list';

/**
 * React component for transform expanded view
 * @class TransformerExpanded
 * @extends {React.Component}
 */
class TransformerExpanded extends React.Component {

    /**
     * Transform extended component constructor
     * @param {any} props props for the component
     * @param {any} context context for the component
     * @memberof TransformerExpanded
     */
    constructor(props, context) {
        super(props, context);
        this.state = {
            // vertices changes must re-render. Hence added as a state.
            name: props.model.getName().getValue(),
            connectionMenu: {},
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
        this.onAddNewVariable = this.onAddNewVariable.bind(this);
        this.onAddNewParameter = this.onAddNewParameter.bind(this);
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
        this.onNameChange = this.onNameChange.bind(this);
    }

    componentDidMount() {
        this.mapper = new TransformRender(this.onConnectionCallback.bind(this),
            this.onDisconnectionCallback.bind(this), $(this.transformOverlayContentDiv));

        this.props.model.on('tree-modified', (evt) => {
            const { designView } = this.context;
            // We keep info of what transformer is active by saving its signature
            // any change that changes the signature should update this reference
            designView.setActiveTransformerSignature(this.props.model.getSignature());
        });

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
                const { element } = this.targetElements[key];
                element.classList.remove('drop-not-valid');
                element.classList.remove('drop-valid');
            });
            this.transformOverlayDraggingContentDiv.classList.remove('drop-not-valid');
            this.transformOverlayDraggingContentDiv.classList.remove('drop-valid');

            clearInterval(this.scrollTimer);

            const { clientX: x, clientY: y } = ev;
            const { output } = this.findTargetAt({ x, y });

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
        this.forceUpdate();
    }

    componentWillReceiveProps() {
        if (this.props.panelResizeInProgress) {
            this.mapper.repaintEverything();
        }
    }

    shouldComponentUpdate() {
        return !this.props.panelResizeInProgress;
    }

    componentDidUpdate(prevProps, prevState) {
        this.transformNodeManager.setTransformStmt(this.props.model);

        if (prevProps.model !== this.props.model) {
            this.props.model.on(CHANGE_EVT_TYPES.TREE_MODIFIED, (evt) => {
                const { designView } = this.context;
                designView.setActiveTransformerSignature(this.props.model.getSignature());
            });
        }

        this.mapper.disconnectAll();

        let sourceKeys = Object.keys(this.sourceElements);
        sourceKeys.forEach((key) => {
            const { element } = this.sourceElements[key];
            if (!element) {
                delete this.sourceElements[key];
            }
        });

        let targetKeys = Object.keys(this.targetElements);
        targetKeys.forEach((key) => {
            const { element } = this.targetElements[key];
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
            targetKeys = Object.keys(this.targetElements);
            targetKeys.forEach((key) => {
                const { element } = this.targetElements[key];
                element.classList.remove('drop-not-valid');
                element.classList.remove('drop-valid');
            });
            this.transformOverlayDraggingContentDiv.classList.remove('drop-not-valid');
            this.transformOverlayDraggingContentDiv.classList.remove('drop-valid');

            clearInterval(this.scrollTimer);

            const { clientX: x, clientY: y } = ev;
            const { output } = this.findTargetAt({ x, y });

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
        this.onClose();
    }

    onConnectionCallback(connection) {
        this.transformNodeManager.createStatementEdge(connection);
    }
    /**
     * Add a new variable delcaration to transformer body
     */

    onAddNewVariable() {
        this.transformNodeManager.addNewVariable('source');
    }

    /**
     * Add a parameter to transformer
     */
    onAddNewParameter() {
        if (this.props.model.getName().getValue() === '') {
            this.props.model.getName().setValue('newTransformer');
        }
        this.props.model.addParameters(this.transformNodeManager.createVariable(
                                            'param' + (this.props.model.getParameters().length + 1), 'string'));
    }

    onClose() {
        const { designView } = this.context;
        designView.setTransformActive(false);
    }

    onSourceAdd() {
        this.addSource(this.state.selectedSource);
    }

    onTargetAdd() {
        this.addTarget(this.state.selectedTarget);
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
        const { clientX: x, clientY: y } = e;
        if (!this.mapper.isConnectionDragging()) {
            return;
        }

        const { element, output } = this.findTargetAt({ x, y });
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

    onNameChange(event) {
        this.setState({ name: event.target.value });
        this.props.model.name.setValue(event.target.value);
    }


    getVerticeData(varNode) {
        if (Array.isArray(varNode)) {
            return varNode.map((varN) => {
                return this.getVerticeData(varN);
            });
        } else {
            const variableType = {};
            if (varNode.getKind() === 'VariableDef') {
                varNode = varNode.getVariable();
                variableType.varDeclarationString = varNode.getSource();
            } else {
                this.props.model.getParameters().forEach((param) => {
                    if (varNode.getName() === param.getName()) {
                        variableType.varDeclarationString = varNode.getSource();
                    }
                });
            }
            const pkgAlias = (varNode.typeNode.packageAlias) ? varNode.typeNode.packageAlias.value : '';
            let type = (varNode.typeNode.typeName) ? varNode.typeNode.typeName.value : varNode.typeNode.typeKind;
            const name = varNode.name.value;
            const structDef = this.transformNodeManager.getStructDefinition(pkgAlias, type);

            if (structDef) {
                type = ((pkgAlias) ? (pkgAlias + ':') : '') + type;
                return this.transformNodeManager.getStructType(name, type, structDef);
            }
            variableType.name = name;
            variableType.displayName = name;
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
            return '';
        }
    }

    /**
     * Get intermediate node definitions
     * @param {any} nodeExpression node expression
     * @param {any} statement containing statements
     * @param {any} [intermediateNodes=[]] if it is nested intermediate nodes
     * @param {any} parentNode parent node
     * @returns intermediate nodes
     * @memberof TransformerExpanded
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
        } else if (this.transformNodeManager.isTransformerConversion(nodeExpression)) {
            const conversion = this.transformNodeManager.getConversionVertices(nodeExpression);
            intermediateNodes.push({
                type: 'conversion',
                conversion,
                parentNode,
                statement,
                conExp: nodeExpression,
            });
            return intermediateNodes;
        } else {
            return [];
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

    /**
     * Get conversion type for provided sourceId and arguements
     * @param  {object[]} arguements function Invocation arguements
     * @param  {string} sourceId   Source Id to be searched
     * @return {string}              Type Name
     */
    getFunctionArgConversionType(arguements, sourceId) {
        let type = '';
        arguements.forEach((arg) => {
            if (this.transformNodeManager.isTransformerConversion(arg) &&
                arg.getExpression().getSource() === sourceId) {
                type = arg.getTypeNode().getTypeKind();
            } else if (TreeUtil.isInvocation(arg)) {
                type = this.getFunctionArgConversionType(arg.getArgumentExpressions(), sourceId);
            }
        });
        return type;
    }

    removeSourceType(type) {
        this.transformNodeManager.removeSourceType(type);
    }

    removeTargetType(type) {
        this.transformNodeManager.removeTargetType(type);
    }

    /**
     * Add source to transform statement
     * @param {any} source source
     * @memberof TransformerExpanded
     */
    addSource(source) {
        const vertex = this.state.vertices.filter((val) => { return val === source; })[0];
        if (vertex) {
            this.props.model.setSource(this.transformNodeManager.createVariable('source', vertex));
            this.setState({ typedSource: '' });
        }
    }

    /**
     * Add target to transform statement
     * @param {any} target target
     * @memberof TransformerExpanded
     */
    addTarget(target) {
        const vertex = this.state.vertices.filter((val) => { return val === target; })[0];
        if (vertex) {
            this.props.model.setReturnParameters([this.transformNodeManager.createVariable('target', vertex)]);
            this.setState({ typedTarget: '' });
        }
    }

    canDrop(dragSource) {
        return TreeUtil.isAssignment(dragSource);
    }

    removeIntermediateNode(expression, parentNode, statement) {
        this.transformNodeManager.removeIntermediateNode(expression, parentNode, statement);
    }

    removeEndpoint(key) {
        this.mapper.remove(key);
    }

    recordSourceElement(element, id, input) {
        this.sourceElements[id] = { element, input };
    }

    recordTargetElement(element, id, output) {
        this.targetElements[id] = { element, output };
    }

    createConnection(statement) {
        const viewId = this.props.model.getID();
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
        if (!isTemp && !this.transformNodeManager.isTransformerConversion(stmtExp) &&
        (TreeUtil.isFieldBasedAccessExpr(expression) || TreeUtil.isSimpleVariableRef(expression))) {
            variables.forEach((variable) => {
                const sourceExprString = this.generateEndpointId(expression.getSource());
                let sourceId = `${sourceExprString}:${viewId}`;
                let folded = false;
                if (!this.sourceElements[sourceId]) {
                    folded = true;
                    sourceId = this.getFoldedEndpointId(sourceExprString, viewId, 'source');
                }
                const targetExprString = this.generateEndpointId(variable.getSource());
                let targetId = `${targetExprString}:${viewId}`;
                if (!this.targetElements[targetId]) {
                    folded = true;
                    targetId = this.getFoldedEndpointId(targetExprString, viewId, 'target');
                }

                this.drawConnection(sourceId, targetId, folded, statement);
            });
        }
        if (TreeUtil.isInvocation(expression) || TreeUtil.isBinaryExpr(expression)
            || TreeUtil.isUnaryExpr(expression) || TreeUtil.isTernaryExpr(expression)) {
            this.drawIntermediateNode(variables, expression, statement, isTemp);
        } else if (this.transformNodeManager.isTransformerConversion(stmtExp)) {
            this.drawIntermediateNode(variables, stmtExp, statement, isTemp);
        }
    }

    /**
     * Generate endpoint ID
     * @param {string} expression expression string
     * @returns {string} id generated
     */
    generateEndpointId(expression) {
        if (expression.lastIndexOf('\n') !== -1) {
            expression = expression.substring(expression.lastIndexOf('\n'));
        }
        return expression.replace(/ /g, '').trim();
    }

    /**
     * Draw connections in intermediate nodes. Intermediate nodes are functions and operators.
     * @param {[Expression]} argumentExpressions argument expressions
     * @param {Expression} nodeExpression intermediate node expression
     * @param {Statement} statement enclosing statement
     * @param {boolean} isTemp is a temporary expression
     * @memberof TransformerExpanded
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
            receiver = nodeExpression.expression;
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
        } else if (this.transformNodeManager.isTransformerConversion(nodeExpression)) {
            nodeDef = this.transformNodeManager.getConversionVertices(nodeExpression);
            nodeName = nodeExpression.getTransformerInvocation().getName().getValue();
            paramExpressions.push(nodeExpression.getExpression());
            paramExpressions = paramExpressions.concat(nodeExpression
                                .getTransformerInvocation().getArgumentExpressions());
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
                    || TreeUtil.isUnaryExpr(expression)
                    || this.transformNodeManager.isTransformerConversion(nodeExpression)) {
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

                        this.drawConnection(sourceId, targetId, folded, statement);
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

            this.drawConnection(sourceId, targetId, folded, statement);
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
        });
    }

    /**
     * Draw nested intermediate nodes
     * @param {any} parentNodeExpression parent node expression
     * @param {any} nodeExpression node expression
     * @param {any} parentNodeDefinition parent node definition
     * @param {any} parentParameterIndex parameter index of the parent node
     * @param {any} statement enclosed statement
     * @memberof TransformerExpanded
     */
    drawInnerIntermediateNode(parentNodeExpression, nodeExpression, parentNodeDefinition,
                                      parentParameterIndex, statement, nodeExpIsTemp = false) {
        const viewId = this.props.model.getID();
        let nodeExpID = nodeExpression.getID();
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
        } else if (this.transformNodeManager.isTransformerConversion(parentNodeExpression)) {
            nodeDef = this.transformNodeManager.getConversionVertices(parentNodeExpression);
            nodeName = parentNodeExpression.getTransformerInvocation().getName().getValue();
            paramExpressions.push(parentNodeExpression.getExpression());
            paramExpressions = paramExpressions.concat(parentNodeExpression
                                .getTransformerInvocation().getArgumentExpressions());
            nodeExpID = parentNodeExpression.getID();
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

                this.drawConnection(sourceId, targetId, folded, statement);
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

        if (!this.transformNodeManager.isTransformerConversion(parentNodeExpression)) {
            this.drawConnection(sourceId, targetId, folded);
        }
        this.mapper.reposition(this.props.model.getID());
    }

    /**
     * Draw connection to a receiver of a bound function
     */
    drawReceiverConnection(receiver, statement, nodeExpression, nodeDef) {
        const viewId = this.props.model.getID();
        const nodeExpID = nodeExpression.getID();
        const { exp: expression, isTemp } = this.transformNodeManager.getResolvedExpression(receiver, statement);
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

            this.drawConnection(sourceId, targetId, folded, statement);
        }
    }

    drawConnection(sourceId, targetId, folded, statement) {
        let type = '';
        if (sourceId && targetId && statement) {
            let expression;
            if (TreeUtil.isVariableDef(statement)) {
                expression = statement.getVariable().getInitialExpression();
            } else {
                expression = statement.getExpression();
            }
            // Only consider non transformer conversions
            if (TreeUtil.isTypeConversionExpr(expression) &&
                !this.transformNodeManager.isTransformerConversion(expression)) {
                if (TreeUtil.isFieldBasedAccessExpr(expression.getExpression())
                    || TreeUtil.isSimpleVariableRef(expression.getExpression())) {
                    if (TreeUtil.isUserDefinedType(expression.getTypeNode())) {
                        type = expression.getTypeNode().getTypeName().getValue();
                    } else {
                        type = expression.getTypeNode().getTypeKind();
                    }
                } else if (TreeUtil.isInvocation(expression.getExpression())) {
                    // Handle Function outgoing parameter conversion
                    if (expression.getExpression().getID() === sourceId.split(':')[0]) {
                        type = expression.getTypeNode().getTypeKind();
                    } else {
                        type = this.getFunctionArgConversionType(
                            expression.getExpression().getArgumentExpressions(), sourceId.split(':')[0]);
                    }
                }
            } else if (TreeUtil.isInvocation(expression)) {
                if (expression.iterableOperation && !targetId.includes('receiver')) {
                    type = 'iterable';
                } else {
                    type = this.getFunctionArgConversionType(expression.getArgumentExpressions(),
                    sourceId.split(':')[0]);
                }
            } else if (TreeUtil.isFieldBasedAccessExpr(expression)
                        && (expression.symbolType && expression.symbolType[0].includes('[]'))) {
                type = 'iterable';
            }
        }
        const callback = (pageX, pageY, connection) => {
            this.setState({ connectionMenu: {
                showIterables: type === 'iterable',
                showConversions: type !== 'iterable',
                iterableX: pageX,
                iterableY: pageY,
                currrentConnection: connection,
                currentStatement: statement,
            } });
        };
        this.mapper.addConnection(sourceId, targetId, folded, type, callback);
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

    /**
     * TODO: Remove this after revisiting
     * Load vertices of the transform graph.
     * @memberof TransformerExpanded
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
     * @memberof TransformerExpanded
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

    findTargetAt({ x, y }) {
        const targetKeys = Object.keys(this.targetElements);
        let foundOutput = {};
        targetKeys.forEach((key) => {
            const { element, output } = this.targetElements[key];
            const connectPointRect = element.getBoundingClientRect();

            const { left, right, top, bottom } = connectPointRect;
            if (left < x && x < right && top < y && y < bottom) {
                foundOutput = { element, output };
            }
        });

        return foundOutput;
    }

    updateVariable(varName, statementString, type) {
        if (this.transformNodeManager.updateVariable(this.props.model, varName, statementString, type)) {
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

    render() {
        const vertices = this.state.vertices;
        const sourceNode = this.props.model.getSource();
        const returnNodes = this.props.model.getReturnParameters();
        const paramNodes = this.props.model.getParameters();
        const varDeclarations = this.props.model.getBody().getStatements()
          .filter((node) => { return node.getKind() === 'VariableDef'; });
        const intermediateNodes = [];
        const source = this.getVerticeData(sourceNode);
        const params = this.getVerticeData(paramNodes);
        const returns = this.getVerticeData(returnNodes);
        const declarations = this.getVerticeData(varDeclarations);

        source.endpointKind = 'input';
        source.isSource = true;
        params.forEach((p) => { p.endpointKind = 'input'; });
        declarations.forEach((p) => { p.endpointKind = 'input'; });
        returns.forEach((r) => {
            r.endpointKind = 'output';
            r.isTarget = true;
        });

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
            } else if (this.transformNodeManager.isTransformerConversion(stmtExp)) {
                intermediateNodes.push(...this.getIntermediateNodes(stmtExp, stmt));
            }
        });

        return (
            <div
                className='transformOverlay'
            >
                <div id='transformHeader' className='transform-header'>
                    <i onClick={this.onClose} className='fw fw-left close-transform' />
                    <p className='transform-header-text '>
                        <i className='transform-header-icon fw fw-type-converter' />
                        <b>Transformer</b>
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
                        renderThumbHorizontal={props => <div {...props} className='transform-horizontal-scroll' />}
                    >
                        <div className='transform-content'>
                            <div className='select-source'>
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
                            <div className='select-target'>
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
                            <div className='middle-content-frame' />
                            <div className='transform-name-container'>
                                <input
                                    type='text'
                                    className='transform-name-text'
                                    value={this.state.name}
                                    placeholder='name'
                                    onChange={this.onNameChange}
                                />
                            </div>
                            <Scrollbars
                                style={{ height: 'calc(100% - 50px)' }}
                                ref={(scroll) => { this.vscroll = scroll; }}
                                onScroll={this.onConnectionsScroll}
                            >
                                <div
                                    id={`transformOverlay-content-${this.props.model.getID()}`}
                                    ref={(div) => { this.transformOverlayContentDiv = div; }}
                                    className='transform-connections'
                                >
                                    <div className='left-content'>
                                        <div className='leftType'>
                                            <Tree
                                                viewId={this.props.model.getID()}
                                                endpoints={[source, ...params, ...declarations]}
                                                type='source'
                                                makeConnectPoint={this.recordSourceElement}
                                                removeTypeCallbackFunc={this.removeSourceType}
                                                updateVariable={this.updateVariable}
                                                onEndpointRemove={this.removeEndpoint}
                                                onConnectPointMouseEnter={this.onConnectPointMouseEnter}
                                                foldEndpoint={this.foldEndpoint}
                                                foldedEndpoints={this.state.foldedEndpoints}
                                            />
                                            <a className='btn-add-var' onClick={this.onAddNewParameter}>
                                                <span>
                                                    <i className='fw fw-add' />
                                                </span>
                                                <span className='btn-add-text'>Add New Parameter</span>
                                            </a>
                                            <a className='btn-add-var' onClick={this.onAddNewVariable}>
                                                <span>
                                                    <i className='fw fw-add' />
                                                </span>
                                                <span className='btn-add-text'>Add New Variable</span>
                                            </a>
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
                                                } else if (node.type === 'operator') {
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
                                                } else {
                                                    return (<NestedTransformer
                                                        key={node.conExp.getID()}
                                                        transformerInvocation={node.conversion}
                                                        statement={node.statement}
                                                        parentNode={node.parentNode}
                                                        conExp={node.conExp}
                                                        recordSourceElement={this.recordSourceElement}
                                                        recordTargetElement={this.recordTargetElement}
                                                        viewId={this.props.model.getID()}
                                                        onEndpointRemove={this.removeEndpoint}
                                                        onFunctionRemove={this.removeIntermediateNode}
                                                        onConnectPointMouseEnter={this.onConnectPointMouseEnter}
                                                        foldEndpoint={this.foldEndpoint}
                                                        foldedEndpoints={this.state.foldedEndpoints}
                                                        isCollapsed={this.state.foldedFunctions[node.conExp.getID()]}
                                                        onHeaderClick={this.foldFunction}
                                                    />);
                                                }
                                            })
                                        }
                                    </DropZone>

                                    {/*
                                    // need to move this to graphical editor
                                    <Button
                                        className='transformer-button'
                                        bBox={{ x: 0, y: 0, h: 0, w: 300 }}
                                        showAlways
                                        model={this.props.model.getBody()}
                                        transformNodeManager={this.transformNodeManager}
                                    /> */}
                                    <IterableList
                                        showIterables={this.state.connectionMenu.showIterables}
                                        bBox={{
                                            x: this.state.connectionMenu.iterableX,
                                            y: this.state.connectionMenu.iterableY,
                                            h: 0,
                                            w: 0,
                                        }}
                                        currrentConnection={this.state.connectionMenu.currrentConnection}
                                        transformNodeManager={this.transformNodeManager}
                                        callback={() => {
                                            this.setState({ connectionMenu: { showIterables: false } });
                                        }}
                                    />
                                    <ConversionList
                                        showConvesrsion={this.state.connectionMenu.showConversions}
                                        bBox={{
                                            x: this.state.connectionMenu.iterableX,
                                            y: this.state.connectionMenu.iterableY,
                                            h: 0,
                                            w: 0,
                                        }}
                                        currrentConnection={this.state.connectionMenu.currrentConnection}
                                        currentStatement={this.state.connectionMenu.currentStatement}
                                        transformNodeManager={this.transformNodeManager}
                                        callback={() => {
                                            this.setState({ connectionMenu: { showConvesrsion: false } });
                                        }}
                                    />
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

TransformerExpanded.propTypes = {
    model: PropTypes.instanceOf(TransformerNode).isRequired,
    panelResizeInProgress: PropTypes.bool.isRequired,
    width: PropTypes.number.isRequired,
    height: PropTypes.number.isRequired,
};

TransformerExpanded.contextTypes = {
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

export default TransformerExpanded;
