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
import React from "react";
import StatementDecorator from "./statement-decorator";
import PropTypes from "prop-types";
import _ from 'lodash';
import * as DesignerDefaults from './../configs/designer-defaults';
import ASTFactory from './../ast/ballerina-ast-factory';
import ArrowDecorator from './arrow-decorator';
import BackwardArrowDecorator from './backward-arrow-decorator';
import DragDropManager from '../tool-palette/drag-drop-manager';
import MessageManager from './../visitors/message-manager';

/**
 * Action Invocation statement decorator.
 * */
class ActionInvocationStatement extends React.Component {

    constructor(props){
        super(props);
        this.editorOptions = {
            propertyType: 'text',
            key: 'ActionInvocation',
            model: props.model,
            getterMethod: props.model.getStatementString,
            setterMethod: props.model.setStatementString
        };
    }

    /**
     * Render Function for the Action Invocation statement.
     * */
    render() {
        let model = this.props.model,
            expression = model.viewState.expression;
        let actionInvocation = model.getChildren()[0];
        const bBox = model.getViewState().bBox;
        const connector = actionInvocation._connector;
        let arrowStart = { x: 0, y: 0 };
        let arrowEnd = { x: 0, y: 0 };
        let backArrowStart = { x: 0, y: 0 };
        let backArrowEnd = { x: 0, y: 0 };
        let innerZoneHeight = model.viewState.components['drop-zone'].h;

        // calculate the bBox for the statement
        this.statementBox = {};
        this.statementBox.h = bBox.h - innerZoneHeight;
        this.statementBox.y = bBox.y + innerZoneHeight;
        this.statementBox.w = bBox.w;
        this.statementBox.x = bBox.x;

        const arrowStartPointX = this.statementBox.x + this.statementBox.w;
        const arrowStartPointY = this.statementBox.y + this.statementBox.h/2;

        arrowStart.x = this.statementBox.x + this.statementBox.w;
        arrowStart.y = this.statementBox.y + this.statementBox.h/3;
        if (!_.isNil(connector)) {
            arrowEnd.x = connector.getViewState().bBox.x + connector.getViewState().bBox.w/2;
            arrowEnd.y = arrowStart.y;
        }
        backArrowStart.x = arrowEnd.x;
        backArrowStart.y = this.statementBox.y + (2 * this.statementBox.h/3);
        backArrowEnd.x = arrowStart.x;
        backArrowEnd.y = backArrowStart.y;

        return (<StatementDecorator viewState={model.viewState} expression={expression}
                                    editorOptions={this.editorOptions} model={model} >
            <g>
                <circle cx={arrowStartPointX}
                        cy={arrowStartPointY}
                        r={10}
                        fill="#444"
                        fillOpacity={0}
                        onMouseOver={(e) => this.onArrowStartPointMouseOver(e)}
                        onMouseOut={(e) => this.onArrowStartPointMouseOut(e)}
                        onMouseDown={(e) => this.onMouseDown(e)}
                        onMouseUp={(e) => this.onMouseUp(e)}/>
                {!_.isNil(connector) && <ArrowDecorator start={arrowStart} end={arrowEnd} enable={true}/>}
                {!_.isNil(connector) && <BackwardArrowDecorator start={backArrowStart} end={backArrowEnd} enable={true}/>}
            </g>
        </StatementDecorator>);
    }

    onArrowStartPointMouseOver (e) {
        e.target.style.fill = '#444';
        e.target.style.fillOpacity = 0.5;
        e.target.style.cursor = 'url(images/BlackHandwriting.cur), pointer';
    }

    onArrowStartPointMouseOut (e) {
        e.target.style.fill = '#444';
        e.target.style.fillOpacity = 0;
    }

    onMouseDown (e) {
        const messageManager = this.context.messageManager;
        const model = this.props.model;
        const messageStartX = this.statementBox.x + this.statementBox.w;
        const messageStartY = this.statementBox.y + this.statementBox.h/2;
        const actionInvocation = model.getChildren()[0];
        messageManager.setSource(actionInvocation);
        messageManager.setIsOnDrag(true);
        messageManager.setMessageStart(messageStartX, messageStartY);

        messageManager.setTargetValidationCallback(function (destination) {
            return actionInvocation.messageDrawTargetAllowed(destination);
        });

        messageManager.startDrawMessage(function (source, destination) {
            source.setConnector(destination, {doSilently: false});
        });
    }

    onMouseUp (e) {
        const messageManager = this.context.messageManager;
        messageManager.reset();
    }
}

ActionInvocationStatement.propTypes = {
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
        h: PropTypes.number.isRequired
    }),
    expression: PropTypes.shape({
        expression: PropTypes.string
    })
};

ActionInvocationStatement.contextTypes = {
    dragDropManager: PropTypes.instanceOf(DragDropManager).isRequired,
    messageManager: PropTypes.instanceOf(MessageManager).isRequired,
};

export default ActionInvocationStatement;
