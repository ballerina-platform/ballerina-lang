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
import ActionInvocationStatementAST from './../ast/statements/action-invocation-statement';
import ArrowDecorator from './arrow-decorator';
import BackwardArrowDecorator from './backward-arrow-decorator';
import DragDropManager from '../tool-palette/drag-drop-manager';
import MessageManager from './../visitors/message-manager';
import StatementDecorator from './statement-decorator';

/**
 * React component for action invocation statement.
 *
 * @class ActionInvocationStatement
 * @extends {React.Component}
 */
class ActionInvocationStatement extends React.Component {

    /**
     * Creates an instance of ActionInvocationStatement.
     * @param {Object} props React properties.
     *
     * @memberof ActionInvocationStatement
     */
    constructor(props) {
        super(props);
        this.editorOptions = {
            propertyType: 'text',
            key: 'ActionInvocation',
            model: props.model,
            getterMethod: props.model.getStatementString,
            setterMethod: props.model.setStatementFromString,
        };
    }

    /**
     * On arrow starting point out event.
     *
     * @param {Object} e event.
     *
     * @memberof ActionInvocationStatement
     */
    onArrowStartPointMouseOut(e) {
        e.target.style.fill = '#444';
        e.target.style.fillOpacity = 0;
    }

    /**
     * On arrow starting point event.
     *
     * @param {Object} e event.
     *
     * @memberof ActionInvocationStatement
     */
    onArrowStartPointMouseOver(e) {
        e.target.style.fill = '#444';
        e.target.style.fillOpacity = 0.5;
        e.target.style.cursor = 'url(images/BlackHandwriting.cur), pointer';
    }

    /**
     * The mouse down event.
     *
     * @memberof ActionInvocationStatement
     */
    onMouseDown() {
        const messageManager = this.context.messageManager;
        const model = this.props.model;
        const messageStartX = this.statementBox.x + this.statementBox.w;
        const messageStartY = this.statementBox.y + (this.statementBox.h / 2);
        const actionInvocation = model.getChildren()[0];
        messageManager.setSource(actionInvocation);
        messageManager.setIsOnDrag(true);
        messageManager.setMessageStart(messageStartX, messageStartY);

        messageManager.setTargetValidationCallback(
                                                destination => actionInvocation.messageDrawTargetAllowed(destination));

        messageManager.startDrawMessage((source, destination) => {
            source.setConnector(destination, { doSilently: false });
        });
    }

    /**
     * The mouse up event
     *
     * @memberof ActionInvocationStatement
     */
    onMouseUp() {
        const messageManager = this.context.messageManager;
        messageManager.reset();
    }

    /**
     * Renders the view for an action invocation statement.
     *
     * @returns {ReactElement} The view.
     *
     * @memberof ActionInvocationStatement
     */
    render() {
        const model = this.props.model;
        const expression = model.viewState.expression;
        const actionInvocation = model.getChildren()[0];
        const bBox = model.getViewState().bBox;
        let connector = actionInvocation.getConnector();

        const arrowStart = { x: 0, y: 0 };
        const arrowEnd = { x: 0, y: 0 };
        const backArrowStart = { x: 0, y: 0 };
        const backArrowEnd = { x: 0, y: 0 };
        const innerZoneHeight = model.viewState.components['drop-zone'].h;

        // calculate the bBox for the statement
        this.statementBox = {};
        this.statementBox.h = bBox.h - innerZoneHeight;
        this.statementBox.y = bBox.y + innerZoneHeight;
        this.statementBox.w = bBox.w;
        this.statementBox.x = bBox.x;

        const arrowStartPointX = this.statementBox.x + this.statementBox.w;
        const arrowStartPointY = this.statementBox.y + (this.statementBox.h / 2);

        arrowStart.x = this.statementBox.x + this.statementBox.w;
        arrowStart.y = this.statementBox.y + (this.statementBox.h / 3);
        if (!_.isNil(connector)) {
            arrowEnd.x = connector.getViewState().bBox.x + (connector.getViewState().bBox.w / 2);
            arrowEnd.y = arrowStart.y;

            // TODO: need a proper way to do this
            const isConnectorAvailable = !_.isEmpty(connector.getParent().filterChildren(
                                                                                child => child.id === connector.id));

            if (!isConnectorAvailable) {
                connector = undefined;
                actionInvocation.setConnector(undefined);
            }
        }
        backArrowStart.x = arrowEnd.x;
        backArrowStart.y = this.statementBox.y + (2 * this.statementBox.h / 3);
        backArrowEnd.x = arrowStart.x;
        backArrowEnd.y = backArrowStart.y;

        return (<StatementDecorator
            viewState={model.viewState}
            expression={expression}
            editorOptions={this.editorOptions}
            model={model}
        >
            <g>
                <circle
                    cx={arrowStartPointX}
                    cy={arrowStartPointY}
                    r={10}
                    fill="#444"
                    fillOpacity={0}
                    onMouseOver={e => this.onArrowStartPointMouseOver(e)}
                    onMouseOut={e => this.onArrowStartPointMouseOut(e)}
                    onMouseDown={e => this.onMouseDown(e)}
                    onMouseUp={e => this.onMouseUp(e)}
                />
                {!_.isNil(connector) && <ArrowDecorator start={arrowStart} end={arrowEnd} enable />}
                {!_.isNil(connector) && <BackwardArrowDecorator start={backArrowStart} end={backArrowEnd} enable />}
            </g>
        </StatementDecorator>);
    }
}

ActionInvocationStatement.propTypes = {
    model: PropTypes.instanceOf(ActionInvocationStatementAST).isRequired,
};

ActionInvocationStatement.contextTypes = {
    dragDropManager: PropTypes.instanceOf(DragDropManager).isRequired,
    messageManager: PropTypes.instanceOf(MessageManager).isRequired,
};

export default ActionInvocationStatement;
