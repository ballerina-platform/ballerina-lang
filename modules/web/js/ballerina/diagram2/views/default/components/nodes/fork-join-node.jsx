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
import CompoundStatementDecorator from './compound-statement-decorator';
import TreeBuilder from './../../../../../model/tree-builder';
import DropZone from './../../../../../drag-drop/DropZone';
import FragmentUtils from './../../../../../utils/fragment-utils';
import DefaultNodeFactory from './../../../../../model/default-node-factory';

/**
 * Class for fork join statement.
 * @abstract React.Component
 * @class ForkJoinNode
 * */
class ForkJoinNode extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            innerDropZoneActivated: false,
            innerDropZoneDropNotAllowed: false,
            innerDropZoneExist: false,
            active: 'hidden',
        };

        // Bind this context to following functions.
        this.handleSetTimeoutCondition = this.handleSetTimeoutCondition.bind(this);
        this.handleSetTimeoutParameter = this.handleSetTimeoutParameter.bind(this);
        this.handleSetJoinParameter = this.handleSetJoinParameter.bind(this);
        this.handleGetJoinCondition = this.handleGetJoinCondition.bind(this);
        this.handleSetJoinCondition = this.handleSetJoinCondition.bind(this);
        this.addTimeoutBody = this.addTimeoutBody.bind(this);
    }

    /**
     * Handle the set timeout condition.
     * @param {string} value - value for the timeout condition.
     * @return {null} null if unsuccessful.
     * */
    handleSetTimeoutCondition(value) {
        if (_.isNil(value)) {
            return null;
        }
        const valueSansColon = value.replace(/;\s*$/, '');
        const forkJoinNode = this.props.model;
        const parsedJson = FragmentUtils.parseFragment(FragmentUtils.createExpressionFragment(valueSansColon));
        const newTimeoutCondition = TreeBuilder.build(parsedJson, forkJoinNode, forkJoinNode.kind);
        newTimeoutCondition.clearWS();
        if (newTimeoutCondition.variable.initialExpression) {
            forkJoinNode.setTimeOutExpression(newTimeoutCondition.variable.initialExpression);
        }
        return null;
    }

    /**
     * Handle the set timeout parameters.
     * @param {string} value - value for the timeout parameter.
     * @return {null} null if unsuccessful.
     * */
    handleSetTimeoutParameter(value) {
        if (_.isNil(value)) {
            return null;
        }

        const forkJoinNode = this.props.model;
        const valueSansColon = value.replace(/;\s*$/, '');
        const parsedJson = FragmentUtils.parseFragment(FragmentUtils.createArgumentParameterFragment(valueSansColon));
        const newTimeoutParameter = TreeBuilder.build(parsedJson, forkJoinNode, forkJoinNode.kind);
        newTimeoutParameter.clearWS();
        forkJoinNode.setTimeOutVariable(newTimeoutParameter);
        return null;
    }

    /**
     * Handle the set join parameter.
     * @param {string} value - value for the join timeout parameter.
     * @return {null} null if unsuccessful.
     * */
    handleSetJoinParameter(value) {
        if (_.isNil(value)) {
            return null;
        }

        const forkJoinNode = this.props.model;
        const valueSansColon = value.replace(/;\s*$/, '');
        const parsedJson = FragmentUtils.parseFragment(FragmentUtils.createArgumentParameterFragment(valueSansColon));
        const newJoinParameter = TreeBuilder.build(parsedJson, forkJoinNode, forkJoinNode.kind);
        newJoinParameter.clearWS();
        forkJoinNode.setJoinResultVar(newJoinParameter);
        return null;
    }

    /**
     * Handle the get join condition.
     * @return {string} get the condition string.
     * */
    handleGetJoinCondition() {
        return this.props.model.getJoinConditionString();
    }

    /**
     * Handle the set join condition.
     * @param {string} value - value for the join timeout parameter.
     * @return {null} null if unsuccessful.
     * */
    handleSetJoinCondition(value) {
        if (_.isNil(value)) {
            return null;
        }

        const forkJoinNode = this.props.model;
        const valueSansColon = value.replace(/;\s*$/, '');
        const parsedJson = FragmentUtils.parseFragment(FragmentUtils.createJoinCondition(valueSansColon));
        const newJoinNode = TreeBuilder.build(parsedJson);
        newJoinNode.clearWS();
        forkJoinNode.setJoinedWorkerIdentifiers(newJoinNode.getJoinedWorkerIdentifiers(), true);
        forkJoinNode.setJoinType(newJoinNode.getJoinType(), true);
        forkJoinNode.setJoinCount(newJoinNode.getJoinCount());
        return null;
    }

    /**
     * Add new timeout body.
     * */
    addTimeoutBody() {
        const parent = this.props.model;
        const newForkJoinNode = DefaultNodeFactory.createForkJoin();
        parent.setTimeOutVariable(newForkJoinNode.getTimeOutVariable(), true, 'Set timeout variable');
        parent.setTimeOutExpression(newForkJoinNode.getTimeOutExpression(), true, 'Set timeout expression');
        parent.setTimeoutBody(newForkJoinNode.getTimeoutBody(), false, 'Set timeout body');
    }

    /**
     * Render the fork join node.
     * @return {XML} react component for fork join.
     * */
    render() {
        const model = this.props.model;
        const bBox = model.viewState.bBox;
        const dropZone = model.viewState.components['drop-zone'];
        const forkLineHiderBottom = model.getJoinBody() ? model.getJoinBody().viewState.bBox.y :
            (model.getTimeoutBody() ? model.getTimeoutBody().viewState.bBox.y : (bBox.y +
                (model.viewState.components['statement-box'].h - model.viewState.components['block-header'].h)));
        const joinLineHiderBottom = model.getTimeoutBody() ? model.getTimeoutBody().viewState.bBox.y :
            (model.getJoinBody() ? model.getJoinBody().viewState.bBox.getBottom() : 0);
        const timeoutLineHiderbottom = model.getTimeoutBody() ? model.getTimeoutBody().viewState.bBox.getBottom()
            : 0;

        let joinConditionEditorOptions = false;
        let joinParameterEditorOptions = false;
        if (model.joinBody) {
            joinConditionEditorOptions = {
                propertyType: 'text',
                key: 'Join condition',
                model: this.props.model,
                getterMethod: this.handleGetJoinCondition,
                setterMethod: this.handleSetJoinCondition,
            };

            joinParameterEditorOptions = {
                propertyType: 'text',
                key: 'Join parameter',
                value: model.getJoinResultVar().getSource(),
                model: model.getJoinResultVar(),
                setterMethod: this.handleSetJoinParameter,
            };
        }

        let timeoutConditionEditorOptions = false;
        let timeoutParameterEditorOptions = false;
        if (model.timeoutBody) {
            timeoutConditionEditorOptions = {
                propertyType: 'text',
                key: 'Timeout condition',
                model: model.getTimeOutExpression(),
                setterMethod: this.handleSetTimeoutCondition,
            };

            timeoutParameterEditorOptions = {
                propertyType: 'text',
                key: 'Timeout parameter',
                value: model.getTimeOutVariable().getSource(),
                model: model.getTimeOutVariable(),
                setterMethod: this.handleSetTimeoutParameter,
            };
        }

        // Get join block life line y1 and y2.
        let joinLifeLineY1 = 0;
        let joinLifeLineY2 = 0;
        if (model.getJoinBody() && model.getJoinBody().getStatements().length > 0) {
            const joinChildren = model.getJoinBody().getStatements();
            const firstJoinChild = joinChildren[0].viewState;
            joinLifeLineY1 = firstJoinChild.bBox.y + firstJoinChild.bBox.h;
            const lastJoinChild = joinChildren[joinChildren.length - 1].viewState;
            joinLifeLineY2 = lastJoinChild.bBox.y
                + (lastJoinChild.components['drop-zone'] ? lastJoinChild.components['drop-zone'].h : 0);
        }

        // Get timeout block life line y1 and y2.
        let timeoutLifeLineY1 = 0;
        let timeoutLifeLineY2 = 0;
        if (model.getTimeoutBody() && model.getTimeoutBody().getStatements().length > 0) {
            const children = model.getTimeoutBody().getStatements();
            const firstChild = children[0].viewState;
            timeoutLifeLineY1 = firstChild.bBox.y + firstChild.bBox.h;
            const lastChild = children[children.length - 1].viewState;
            timeoutLifeLineY2 = lastChild.bBox.y
                + (lastChild.components['drop-zone'] ? lastChild.components['drop-zone'].h : 0);
        }

        return (
            <g>
                <DropZone
                    x={dropZone.x}
                    y={dropZone.y}
                    width={dropZone.w}
                    height={dropZone.h}
                    baseComponent="rect"
                    dropTarget={model.parent}
                    dropBefore={model}
                    renderUponDragStart
                    enableDragBg
                    enableCenterOverlayLine
                />
                <line
                    x1={bBox.getCenterX()}
                    y1={bBox.y}
                    x2={bBox.getCenterX()}
                    y2={forkLineHiderBottom}
                    className="life-line-hider"
                />

                <CompoundStatementDecorator
                    dropTarget={model}
                    bBox={bBox}
                    title={'fork'}
                    model={model}
                    body={model}
                    disableDropzoneMiddleLineOverlay
                >
                    {
                        this.props.model.workers.map((item) => {
                            return (<DropZone
                                x={item.getBody().viewState.bBox.x}
                                y={item.getBody().viewState.bBox.y}
                                width={item.getBody().viewState.bBox.w}
                                height={item.getBody().viewState.bBox.h}
                                baseComponent="rect"
                                dropTarget={item.getBody()}
                                enableDragBg
                            />);
                        })
                    }
                </CompoundStatementDecorator>

                {model.joinBody &&
                <line
                    x1={model.getJoinBody().viewState.bBox.getCenterX()}
                    y1={model.getJoinBody().viewState.bBox.y}
                    x2={model.getJoinBody().viewState.bBox.getCenterX()}
                    y2={joinLineHiderBottom}
                    className='life-line-hider'
                />
                }

                {model.joinBody &&
                <line
                    x1={model.getJoinBody().viewState.bBox.getCenterX()}
                    y1={joinLifeLineY1}
                    x2={model.getJoinBody().viewState.bBox.getCenterX()}
                    y2={joinLifeLineY2}
                    className="join-lifeline"
                    key="join-life-line"
                />
                }

                {model.joinBody &&
                <CompoundStatementDecorator
                    dropTarget={model.getJoinBody()}
                    bBox={model.getJoinBody().viewState.bBox}
                    expression={{
                        text: model.getJoinBody().viewState.components.expression.text,
                    }}
                    title={'join'}
                    model={model.getJoinBody()}
                    body={model.getJoinBody()}
                    parameterBbox={model.getJoinBody().viewState.components.param}
                    parameterEditorOptions={joinParameterEditorOptions}
                    editorOptions={joinConditionEditorOptions}
                    disableButtons={{
                        delete: true,
                    }}
                    disableDropzoneMiddleLineOverlay
                />
                }

                {model.joinBody && !model.timeoutBody &&
                <g onClick={this.addTimeoutBody}>
                    <title>Add Timeout</title>
                    <rect
                        x={model.joinBody.viewState.components['statement-box'].x
                        + model.joinBody.viewState.components['statement-box'].w
                        + model.joinBody.viewState.bBox.expansionW - 10}
                        y={model.joinBody.viewState.components['statement-box'].y
                        + model.joinBody.viewState.components['statement-box'].h - 25}
                        width={20}
                        height={20}
                        rx={10}
                        ry={10}
                        className="add-catch-button"
                    />
                    <text
                        x={model.joinBody.viewState.components['statement-box'].x
                        + model.joinBody.viewState.components['statement-box'].w
                        + model.joinBody.viewState.bBox.expansionW - 4}
                        y={model.joinBody.viewState.components['statement-box'].y
                        + model.joinBody.viewState.components['statement-box'].h - 15}
                        width={20}
                        height={20}
                        className="add-catch-button-label"
                    >
                        +
                    </text>
                </g>
                }

                {model.timeoutBody &&
                <line
                    x1={model.getTimeoutBody().viewState.bBox.getCenterX()}
                    y1={model.getTimeoutBody().viewState.bBox.y}
                    x2={model.getTimeoutBody().viewState.bBox.getCenterX()}
                    y2={timeoutLineHiderbottom}
                    className='life-line-hider'
                />
                }

                {model.timeoutBody &&
                <line
                    x1={model.getTimeoutBody().viewState.bBox.getCenterX()}
                    y1={timeoutLifeLineY1}
                    x2={model.getTimeoutBody().viewState.bBox.getCenterX()}
                    y2={timeoutLifeLineY2}
                    className="join-lifeline"
                    key="timeout-life-line"
                />
                }

                {model.timeoutBody &&
                <CompoundStatementDecorator
                    dropTarget={model.getTimeoutBody()}
                    bBox={model.getTimeoutBody().viewState.bBox}
                    parameterBbox={model.getTimeoutBody().viewState.components.param}
                    expression={{
                        text: model.getTimeOutExpression().getSource(),
                    }}
                    title={'timeout'}
                    model={model.getTimeoutBody()}
                    body={model.getTimeoutBody()}
                    parameterEditorOptions={timeoutParameterEditorOptions}
                    editorOptions={timeoutConditionEditorOptions}
                    disableDropzoneMiddleLineOverlay
                />
                }
            </g>
        );
    }
}

ForkJoinNode.propTypes = {
    model: PropTypes.shape({
        getJoinConditionString: PropTypes.func.isRequired,
        workers: PropTypes.array.isRequired,
    }).isRequired,
};

ForkJoinNode.contextTypes = {
    mode: PropTypes.string,
};

export default ForkJoinNode;
