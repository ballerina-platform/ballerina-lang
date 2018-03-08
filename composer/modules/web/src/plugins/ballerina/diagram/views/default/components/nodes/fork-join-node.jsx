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
import TreeBuilder from './../../../../../model/tree-builder';
import FragmentUtils from './../../../../../utils/fragment-utils';
import DefaultNodeFactory from './../../../../../model/default-node-factory';
import { getComponentForNodeArray } from './../../../../diagram-util';
import ArrowDecorator from '../decorators/arrow-decorator';
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
        const cmp = model.viewState.components;

        let joinBBox = {};
        if (this.props.model.joinBody) {
            joinBBox = this.props.model.joinBody.viewState.bBox;
        }

        const forkContainer = model.viewState.components.forkContainer;
        const workers = getComponentForNodeArray(this.props.model.workers);
        const joinBody = getComponentForNodeArray(this.props.model.joinBody);
        const timeoutBody = getComponentForNodeArray(this.props.model.timeoutBody);

        return (
            <g>
                <g className='fork-container'>
                    <rect
                        x={forkContainer.x}
                        y={forkContainer.y}
                        width={forkContainer.w}
                        height={forkContainer.h - 15}
                        className='background-empty-rect'
                    />
                    <rect
                        x={forkContainer.x - 10}
                        y={forkContainer.y}
                        width={forkContainer.w}
                        height={5}
                        className='fork-bar'
                    />
                    <rect
                        x={forkContainer.x - 10}
                        y={forkContainer.y + forkContainer.h - 15}
                        width={forkContainer.w}
                        height={5}
                        className='fork-bar'
                    />
                    {workers}
                </g>
                {model.joinBody &&
                    <g>
                        <rect
                            x={cmp.joinHeader.x}
                            y={cmp.joinHeader.y}
                            width={cmp.joinHeader.w}
                            height={cmp.joinHeader.h + joinBBox.h}
                            className='compound-statement-rect'
                            rx={5}
                            ry={5}
                        />
                        <text
                            x={cmp.joinHeader.x + 5}
                            y={cmp.joinHeader.y + 10}
                            className='statement-title-text-left'
                        >
                            join
                        </text>
                        {joinBody}
                    </g>
                }
                {model.timeoutBody &&
                    <g>
                        <line
                            x1={model.timeoutBody.viewState.bBox.x}
                            y1={cmp.timeoutHeader.y - 10}
                            x2={model.timeoutBody.viewState.bBox.x}
                            y2={model.timeoutBody.viewState.bBox.y + model.timeoutBody.viewState.bBox.h + 10}
                            className='worker-life-line'
                        />
                        <rect
                            x={cmp.timeoutHeader.x}
                            y={cmp.timeoutHeader.y}
                            width={cmp.timeoutHeader.w}
                            height={cmp.timeoutHeader.h}
                            className='statement-title-rect'
                            rx={5}
                            ry={5}
                        />
                        <text
                            x={cmp.timeoutHeader.x + 5}
                            y={cmp.timeoutHeader.y + 10}
                            className='statement-title-text-left'
                        >
                            timeout
                        </text>
                        <ArrowDecorator
                            start={{
                                x: model.timeoutBody.viewState.bBox.x,
                                y: model.timeoutBody.viewState.bBox.y + model.timeoutBody.viewState.bBox.h + 10,
                            }}
                            end={{
                                x: model.viewState.bBox.x + 60,
                                y: model.timeoutBody.viewState.bBox.y + model.timeoutBody.viewState.bBox.h + 10,
                            }}
                            classNameArrow='flowchart-action-arrow'
                            classNameArrowHead='flowchart-action-arrow-head'
                        />
                        {timeoutBody}
                    </g>
                }
            </g>
        );
    }
}

ForkJoinNode.propTypes = {
    model: PropTypes.shape({
        getJoinConditionString: PropTypes.func.isRequired,
        workers: PropTypes.array.isRequired,
        joinBody: PropTypes.instanceOf(Object),
        timeoutBody: PropTypes.instanceOf(Object),
    }).isRequired,
};

ForkJoinNode.contextTypes = {
    mode: PropTypes.string,
};

export default ForkJoinNode;
