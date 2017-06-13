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
import StatementDecorator from './statement-decorator';
import PropTypes from 'prop-types';
import _ from 'lodash';
import * as DesignerDefaults from './../configs/designer-defaults';
import MessageManager from './../visitors/message-manager';
import { util } from './../visitors/sizing-utils';
import BallerinaASTFactory from './../ast/ballerina-ast-factory';
import ArrowDecorator from './arrow-decorator';

class WorkerInvocationStatement extends React.Component {

    constructor(props) {
        super(props);
        this.editorOptions = {
            propertyType: 'text',
            key: 'WorkerInvocationStatement',
            model: props.model,
            getterMethod: props.model.getStatementString,
            setterMethod: props.model.setStatementFromString,
        };
    }

    render() {
        let model = this.props.model,
            expression = model.viewState.expression;
        const bBox = model.getViewState().bBox;
        const arrowEnd = {
            x: 0,
            y: 0,
        };
        const arrowStart = {
            x: 0,
            y: 0,
        };
        const statementY = bBox.y + model.getViewState().components['drop-zone'].h;
        const statementHeight = bBox.h - model.getViewState().components['drop-zone'].h;
        const statementWidth = bBox.w;
        const statementX = bBox.getLeft();

        arrowStart.y = statementY + statementHeight / 2;
        arrowEnd.y = arrowStart.y;

        const destinationWorkerName = model.getWorkerName();
        const topLevelParent = model.getTopLevelParent();
        const workersParent = BallerinaASTFactory.isWorkerDeclaration(topLevelParent) ? topLevelParent.getParent() : topLevelParent;
        let workerDeclaration;
        if (destinationWorkerName === 'default') {
            workerDeclaration = workersParent;
        } else {
            workerDeclaration = _.find(workersParent.getChildren(), (child) => {
                if (BallerinaASTFactory.isWorkerDeclaration(child)) {
                    return child.getWorkerName() === destinationWorkerName;
                }

                return false;
            });
        }
        const workerName = BallerinaASTFactory.isWorkerDeclaration(topLevelParent) ?
            topLevelParent.getWorkerName() : 'default';
        const workerReplyStatement = util.getWorkerReplyStatementTo(workerDeclaration, workerName);

        if (!_.isNil(workerReplyStatement)) {
            /**
             * If the worker invocation is located before the worker reply compared to the horizontal axis, then we need to
             * start message draw from invocation's right edge to reply's left edge
             * otherwise it should be from left edge to right edge
             */
            if (workerReplyStatement.getViewState().bBox.getRight() > bBox.getRight()) {
                arrowStart.x = bBox.getRight();
                arrowEnd.x = workerReplyStatement.getViewState().bBox.getLeft();
            } else {
                arrowStart.x = bBox.getLeft();
                arrowEnd.x = workerReplyStatement.getViewState().bBox.getRight();
            }
        } else {
            arrowStart.x = bBox.getRight();
        }

        return (<g>
          <StatementDecorator
            model={model} viewState={model.viewState}
            expression={expression} editorOptions={this.editorOptions}
          />
          <g>
            <circle
              cx={statementX}
              cy={arrowStart.y}
              r={10}
              fill="#444"
              fillOpacity={0}
              onMouseOver={e => this.onArrowStartPointMouseOver(e)}
              onMouseOut={e => this.onArrowStartPointMouseOut(e)}
              onMouseDown={e => this.onMouseDown(e)}
              onMouseUp={e => this.onMouseUp(e)}
            />
          </g>
          <g>
            <circle
              cx={statementX + statementWidth}
              cy={arrowStart.y}
              r={10}
              fill="#444"
              fillOpacity={0}
              onMouseOver={e => this.onArrowStartPointMouseOver(e)}
              onMouseOut={e => this.onArrowStartPointMouseOut(e)}
              onMouseDown={e => this.onMouseDown(e)}
              onMouseUp={e => this.onMouseUp(e)}
            />
          </g>
          {!_.isNil(workerReplyStatement) && <ArrowDecorator start={arrowStart} end={arrowEnd} enable />}
        </g>);
    }

    onArrowStartPointMouseOver(e) {
        e.target.style.fill = '#444';
        e.target.style.fillOpacity = 0.5;
        e.target.style.cursor = 'url(images/BlackHandwriting.cur), pointer';
    }

    onArrowStartPointMouseOut(e) {
        e.target.style.fill = '#444';
        e.target.style.fillOpacity = 0;
    }

    onMouseDown(e) {
        const messageManager = this.context.messageManager;
        const model = this.props.model;
        const bBox = model.getViewState().bBox;
        const messageStartX = bBox.getRight();
        const messageStartY = bBox.getTop() + (bBox.h + DesignerDefaults.statement.gutter.v) / 2;
        messageManager.setSource(model);
        messageManager.setIsOnDrag(true);
        messageManager.setMessageStart(messageStartX, messageStartY);

        messageManager.setTargetValidationCallback(destination => model.messageDrawTargetAllowed(destination));

        messageManager.startDrawMessage((source, destination) => {
            const expressionsList = ((source.getInvocationStatement().split('->')[0]).trim()).split(',');
            let expressionString = '';
            let workerName = '';

            /**
             * If the destination is not a worker declaration, it should be a top level element
             * (ie: resource definition, function definition, connector action definition)
             * For the top level elements, worker name is "default"
             */
            if (BallerinaASTFactory.isWorkerDeclaration(destination)) {
                workerName = destination.getWorkerName();
            } else {
                workerName = 'default';
            }
            source.setWorkerName(workerName);
            expressionString = _.join(expressionsList, ',');
            expressionString += `->${workerName}`;
            source.setInvocationStatement(expressionString);
            source.setDestination(destination);
        });
    }

    onMouseUp(e) {
        const messageManager = this.context.messageManager;
        messageManager.reset();
    }
}

WorkerInvocationStatement.contextTypes = {
    messageManager: PropTypes.instanceOf(MessageManager).isRequired,
};

WorkerInvocationStatement.propTypes = {
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
        h: PropTypes.number.isRequired,
    }),
};

export default WorkerInvocationStatement;
