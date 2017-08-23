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
import StatementDecorator from './statement-decorator';
import MessageManager from './../../../../visitors/message-manager';
import DragDropManager from '../../../../tool-palette/drag-drop-manager';
import ActiveArbiter from './active-arbiter';
import ArrowDecorator from './arrow-decorator';
import BackwardArrowDecorator from './backward-arrow-decorator';
import ASTFactory from '../../../../ast/ast-factory';
import FunctionDefinition from './function-definition';
import ImageUtil from './image-util';

/**
 * Variable Definition Statement Decorator.
 * */
class VariableDefinitionStatement extends React.Component {


    constructor(props) {
        super(props);
        this.editorOptions = {
            propertyType: 'text',
            key: 'VariableDefinition',
            model: this.props.model,
            getterMethod: this.props.model.getStatementString,
            setterMethod: this.props.model.setStatementFromString,
        };
        this.state = { actionInvocationDelete: 'hidden' };
        this.onDelete = this.onDelete.bind(this);
        this.onDeleteConnectorActionMouseOut = this.onDeleteConnectorActionMouseOut.bind(this);
        this.onDeleteConnectorActionMouseEnter = this.onDeleteConnectorActionMouseEnter.bind(this);
        this.designer = _.get(props, 'designer');
        this.mode = _.get(props, 'mode');
    }

    onArrowStartPointMouseOut(e) {
        e.target.style.fill = '#444';
        e.target.style.fillOpacity = 0;
    }

    onArrowStartPointMouseOver(e) {
        e.target.style.fill = '#444';
        e.target.style.fillOpacity = 0.5;
        e.target.style.cursor = 'url(images/BlackHandwriting.cur), pointer';
    }

    onDeleteConnectorActionMouseOut() {
        this.setState({
            actionInvocationDelete: 'hidden',
        });
    }

    onDeleteConnectorActionMouseEnter() {
        this.setState({
            actionInvocationDelete: 'visible',
        });
    }

    onDelete() {
        const model = this.props.model;
        if ((!_.isNil(model.getRightExpression())
            && ASTFactory.isActionInvocationExpression(model.getRightExpression()))) {
            model.getRightExpression().setActionConnectorName('clientConnector', { doSilently: true });
            model.getRightExpression().setConnector(undefined);
        }
        this.onDeleteConnectorActionMouseOut();
    }

    onMouseDown(e) {
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
            source.setActionConnectorName(destination.getConnectorVariable());
            source.setConnector(destination);
            model.getStatementString();
            model.trigger('tree-modified', { type: 'custom', title: 'action set' });
        });
    }

    onMouseUp(e) {
        const messageManager = this.context.messageManager;
        messageManager.reset();
    }

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
     * Render Function for the variable statement.
     * */
    render() {
        const model = this.props.model;
        const expression = model.viewState.expression;
        const bBox = model.getViewState().bBox;

        const innerZoneHeight = model.getViewState().components['drop-zone'].h;

        // calculate the bBox for the statement
        this.statementBox = {};
        this.statementBox.h = this.designer.statement.height;
        this.statementBox.y = bBox.y + innerZoneHeight;
        this.statementBox.w = bBox.w;
        this.statementBox.x = bBox.x;

        const arrowStartPointX = bBox.getRight();
        const arrowStartPointY = this.statementBox.y + (this.statementBox.h / 2);
        const radius = 10;
        const actionInvocation = (!_.isNil(model.getRightExpression())
        && ASTFactory.isActionInvocationExpression(model.getRightExpression())) ?
            model.getRightExpression() : undefined;
        let connector;
        const arrowStart = { x: 0, y: 0 };
        const arrowEnd = { x: 0, y: 0 };
        const backArrowStart = { x: 0, y: 0 };
        const backArrowEnd = { x: 0, y: 0 };

        if (!_.isNil(actionInvocation) && !_.isNil(actionInvocation.getConnector())) {
            connector = actionInvocation.getConnector();

            // TODO: need a proper way to do this
            const isConnectorAvailable = !_.isEmpty(connector.getParent()
                .filterChildren(child => child.id === connector.id));

            arrowStart.x = this.statementBox.x + this.statementBox.w;
            arrowStart.y = this.statementBox.y + (this.statementBox.h / 3);

            if (!isConnectorAvailable) {
                connector = undefined;
                actionInvocation._connector = undefined;
            } else {
                const connectorViewState = ASTFactory.isAssignmentStatement(connector) ?
                    connector.getViewState().connectorDeclViewState : connector.getViewState();
                arrowEnd.x = connectorViewState.bBox.x + (connectorViewState.bBox.w / 2);
            }

            arrowEnd.y = arrowStart.y;
            backArrowStart.x = arrowEnd.x;
            backArrowStart.y = this.statementBox.y + (2 * this.statementBox.h / 3);
            backArrowEnd.x = arrowStart.x;
            backArrowEnd.y = backArrowStart.y;
        }

        const lambdaFunc = model.getLambdaChildren().map(f =>
            <FunctionDefinition designer={this.designer} model={f} key={f.getFunctionName()} />);

        return (
            <StatementDecorator
                model={model}
                viewState={model.viewState}
                expression={expression}
                editorOptions={this.editorOptions}
                designer={this.props.designer}
                mode={this.props.mode}
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

                    <g className="arrow-grouping">
                        {connector && <ArrowDecorator
                            start={arrowStart}
                            end={arrowEnd}
                            enable
                        />}
                        {connector && <BackwardArrowDecorator
                            start={backArrowStart}
                            end={backArrowEnd}
                            enable
                        />}
                        {connector &&
                        <g
                            className='connector-message-delete'
                            visibility={this.state.actionInvocationDelete}
                        >
                            <rect
                                x={((arrowStart.x + arrowEnd.x) / 2) -
                                this.designer.actionInvocationDelete.iconSize.padding}
                                y={arrowStart.y + (this.designer.actionInvocationDelete.outerRect.height / 2)}
                                width={(this.designer.actionInvocationDelete.outerRect.height / 2)}
                                height={(this.designer.actionInvocationDelete.outerRect.height / 2)}
                                rx="0"
                                ry="0"
                                className="property-pane-action-button-wrapper"
                            />
                            <image
                                width={this.designer.actionInvocationDelete.iconSize.width}
                                height={this.designer.actionInvocationDelete.iconSize.width}
                                className="property-pane-action-button-delete"
                                xlinkHref={ImageUtil.getSVGIconString('delete-dark')}
                                x={((arrowStart.x + arrowEnd.x) / 2) -
                                this.designer.actionInvocationDelete.iconSize.padding}
                                y={arrowStart.y + (this.designer.actionInvocationDelete.outerRect.height / 2)}
                                onClick={this.onDelete}
                            >
                                <title>Delete</title> </image>
                        </g>
                        }
                        <rect
                            x={((arrowStart.x + arrowEnd.x) / 2) -
                            (this.designer.actionInvocationDelete.outerRect.width / 2)}
                            y={arrowStart.y}
                            width={this.designer.actionInvocationDelete.outerRect.width}
                            height={this.designer.actionInvocationDelete.outerRect.height}
                            rx="0"
                            ry="0"
                            fillOpacity="0.0"
                            onMouseEnter={this.onDeleteConnectorActionMouseEnter}
                            onMouseOut={this.onDeleteConnectorActionMouseOut}
                            onClick={this.onDelete}
                        />
                    </g>
                </g>
                }
                {lambdaFunc}
            </StatementDecorator>);
    }
}

VariableDefinitionStatement.propTypes = {
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
        h: PropTypes.number.isRequired,
    }),
};

VariableDefinitionStatement.contextTypes = {
    dragDropManager: PropTypes.instanceOf(DragDropManager).isRequired,
    messageManager: PropTypes.instanceOf(MessageManager).isRequired,
    activeArbiter: PropTypes.instanceOf(ActiveArbiter).isRequired,
};

export default VariableDefinitionStatement;
