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
import AssignmentStatementAST from './../ast/statements/assignment-statement';
import MessageManager from './../visitors/message-manager';
import DragDropManager from '../tool-palette/drag-drop-manager';
import ActiveArbiter from './active-arbiter';
import ArrowDecorator from './arrow-decorator';
import BackwardArrowDecorator from './backward-arrow-decorator';
import BallerinaASTFactory from './../../ballerina/ast/ballerina-ast-factory';
import StatementDecorator from './statement-decorator';
import * as DesignerDefaults from './../configs/designer-defaults';
import ConnectorActivationContainer from './connector-activation-container';
import LifeLine from './lifeline.jsx';

/**
 * Assignment statement decorator.
 *
 * @class AssignmentStatement
 * @extends {React.Component}
 */
class AssignmentStatement extends React.Component {

    /**
     * Creates an instance of AssignmentStatement.
     * @param {Object} props React properties.
     * @memberof AssignmentStatement
     */
    constructor(props) {
        super(props);
        this.editorOptions = {
            propertyType: 'text',
            key: 'Assignment',
            model: props.model,
            getterMethod: props.model.getStatementString,
            setterMethod: props.model.setStatementFromString,
        };
    }

    /**
     * Event when mouse leaves the starting point.
     * @param {Object} e event.
     * @memberof AssignmentStatement
     */
    onArrowStartPointMouseOut(e) {
        e.target.style.fill = '#444';
        e.target.style.fillOpacity = 0;
    }

    /**
     * Event when mouse enters the starting point.
     * @param {Object} e event.
     * @memberof AssignmentStatement
     */
    onArrowStartPointMouseOver(e) {
        e.target.style.fill = '#444';
        e.target.style.fillOpacity = 0.5;
        e.target.style.cursor = 'url(images/BlackHandwriting.cur), pointer';
    }

    /**
     * On mouse down event.
     * @memberof AssignmentStatement
     */
    onMouseDown() {
        const messageManager = this.context.messageManager;
        const model = this.props.model;
        const bBox = model.getViewState().bBox;
        const statementH = this.statementBox.h;
        const messageStartX = bBox.x + bBox.w;
        const messageStartY = this.statementBox.y + (statementH / 2);
        const actionInvocation = model.getRightExpression();
        messageManager.setSource(actionInvocation);
        messageManager.setIsOnDrag(true);
        messageManager.setMessageStart(messageStartX, messageStartY);

        messageManager.setTargetValidationCallback(destination =>
                                                                actionInvocation.messageDrawTargetAllowed(destination));

        messageManager.startDrawMessage((source, destination) => {
            source.setConnector(destination);
            model.getStatementString();
            model.trigger('tree-modified', { type: 'custom', title: 'action set' });
        });
    }

    /**
     * On mouse up event.
     * @memberof AssignmentStatement
     */
    onMouseUp() {
        const messageManager = this.context.messageManager;
        messageManager.reset();
    }

    /**
     * Sets the visibility of the action.
     * @param {boolean} show true to show, else false.
     * @memberof AssignmentStatement
     */
    setActionVisibility(show) {
        if (!this.context.dragDropManager.isOnDrag()) {
            if (show) {
                this.context.activeArbiter.readyToActivate(this);
            } else {
                this.context.activeArbiter.readyToDeactivate(this);
            }
        }
    }

    /**
     * Render the connector declaration if the assignment statement has a connector init expression
     * @param {object} connectorViewState - connector view state
     * @return {XML} - Generated rendering react component
     */
    renderConnectorDeclaration(connectorViewState) {
        const statementContainerBBox = connectorViewState.components.statementContainer;
        const connectorBBox = {};
        const model = this.props.model;
        const connectorName = connectorViewState.variableTextTrimmed;
        connectorBBox.x = statementContainerBBox.x + ((statementContainerBBox.w - DesignerDefaults.lifeLine.width) / 2);
        connectorBBox.y = statementContainerBBox.y - DesignerDefaults.lifeLine.head.height;
        connectorBBox.w = DesignerDefaults.lifeLine.width;
        connectorBBox.h = statementContainerBBox.h + (DesignerDefaults.lifeLine.head.height * 2);
        const connectorInitializeStartY = model.getViewState().bBox.y +
            ((model.getViewState().bBox.h
            + model.getViewState().components['drop-zone'].h) / 2);

        const classes = {
            lineClass: 'connector-life-line',
            polygonClass: 'connector-life-line-polygon',
        };

        return (
            <g>
                <ConnectorActivationContainer bBox={statementContainerBBox} activationTarget={model} />
                <LifeLine
                    title={connectorName}
                    bBox={connectorBBox}
                    onDelete={this.onDelete}
                    classes={classes}
                    editorOptions={this.editorOptions}
                    startSolidLineFrom={connectorInitializeStartY}
                />
            </g>
        );
    }

    /**
     * Renders the view for an assignment statement.
     * @returns {ReactElement} The view.
     * @memberof AssignmentStatement
     */
    render() {
        const model = this.props.model;
        const expression = model.viewState.expression;
        const bBox = model.getViewState().bBox;

        const innerZoneHeight = model.getViewState().components['drop-zone'].h;

        // calculate the bBox for the statement
        this.statementBox = {};
        this.statementBox.h = bBox.h - innerZoneHeight;
        this.statementBox.y = bBox.y + innerZoneHeight;
        this.statementBox.w = bBox.w;
        this.statementBox.x = bBox.x;

        const arrowStartPointX = bBox.getRight();
        const arrowStartPointY = this.statementBox.y + (this.statementBox.h / 2);
        const radius = 10;
        const actionInvocation = BallerinaASTFactory.isActionInvocationExpression(
                        model.getRightExpression()) ? model.getRightExpression() : undefined;
        let connector;
        const arrowStart = { x: 0, y: 0 };
        const arrowEnd = { x: 0, y: 0 };
        const backArrowStart = { x: 0, y: 0 };
        const backArrowEnd = { x: 0, y: 0 };
        let connectorDeclaration;
        if (BallerinaASTFactory.isConnectorInitExpression(model.getChildren()[1])) {
            connectorDeclaration = this.renderConnectorDeclaration(model.getViewState().connectorDeclViewState);
        }

        if (!_.isNil(actionInvocation) && !_.isNil(actionInvocation.getConnector())) {
            connector = actionInvocation.getConnector();

            // TODO: need a proper way to do this
            const isConnectorAvailable = !_.isEmpty(connector.getParent().filterChildren(
                                                                                child => child.id === connector.id));

            arrowStart.x = this.statementBox.x + this.statementBox.w;
            arrowStart.y = this.statementBox.y + (this.statementBox.h / 3);

            if (!isConnectorAvailable) {
                connector = undefined;
                actionInvocation.setConnector(undefined);
            } else {
                arrowEnd.x = connector.getViewState().bBox.x + (connector.getViewState().bBox.w / 2);
            }

            arrowEnd.y = arrowStart.y;
            backArrowStart.x = arrowEnd.x;
            backArrowStart.y = this.statementBox.y + (2 * this.statementBox.h / 3);
            backArrowEnd.x = arrowStart.x;
            backArrowEnd.y = backArrowStart.y;
        }


        return (
            <g>
                <StatementDecorator
                    viewState={model.viewState}
                    expression={expression}
                    editorOptions={this.editorOptions}
                    model={model}
                >
                    {!_.isNil(actionInvocation) &&
                    <g>
                        <circle
                            cx={arrowStartPointX}
                            cy={arrowStartPointY}
                            r={radius}
                            fill="#444"
                            fillOpacity={0}
                            onMouseOver={e => this.onArrowStartPointMouseOver(e)}
                            onMouseOut={e => this.onArrowStartPointMouseOut(e)}
                            onMouseDown={e => this.onMouseDown(e)}
                            onMouseUp={e => this.onMouseUp(e)}
                        />
                        {connector && <ArrowDecorator start={arrowStart} end={arrowEnd} enable />}
                        {connector && <BackwardArrowDecorator start={backArrowStart} end={backArrowEnd} enable />}
                    </g>
                    }
                </StatementDecorator>
                {connectorDeclaration}
            </g>);
    }
}

AssignmentStatement.propTypes = {
    model: PropTypes.instanceOf(AssignmentStatementAST).isRequired,
};

AssignmentStatement.contextTypes = {
    dragDropManager: PropTypes.instanceOf(DragDropManager).isRequired,
    messageManager: PropTypes.instanceOf(MessageManager).isRequired,
    activeArbiter: PropTypes.instanceOf(ActiveArbiter).isRequired,
};

export default AssignmentStatement;
