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

class ForkJoinNode extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            innerDropZoneActivated: false,
            innerDropZoneDropNotAllowed: false,
            innerDropZoneExist: false,
            active: 'hidden',
        };

        this.handleSetTimeoutCondition = this.handleSetTimeoutCondition.bind(this);
        this.handleSetTimeoutParameter = this.handleSetTimeoutParameter.bind(this);
        this.handleSetJoinParameter = this.handleSetJoinParameter.bind(this);
        this.handleGetJoinCondition = this.handleGetJoinCondition.bind(this);
        this.handleSetJoinCondition = this.handleSetJoinCondition.bind(this);
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

        const forkJoinNode = this.props.model;
        const parsedJson = FragmentUtils.parseFragment(FragmentUtils.createExpressionFragment(value));
        const newTimeoutCondition = TreeBuilder.build(parsedJson, forkJoinNode, forkJoinNode.kind);
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
        const parsedJson = FragmentUtils.parseFragment(FragmentUtils.createArgumentParameterFragment(value));
        const newTimeoutParameter = TreeBuilder.build(parsedJson, forkJoinNode, forkJoinNode.kind);
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
        const parsedJson = FragmentUtils.parseFragment(FragmentUtils.createArgumentParameterFragment(value));
        const newJoinParameter = TreeBuilder.build(parsedJson, forkJoinNode, forkJoinNode.kind);
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
        const parsedJson = FragmentUtils.parseFragment(FragmentUtils.createJoinCondition(value));
        const newJoinNode = TreeBuilder.build(parsedJson);

        forkJoinNode.setJoinedWorkerIdentifiers(newJoinNode.getJoinedWorkerIdentifiers());
        forkJoinNode.setJoinType(newJoinNode.getJoinType());
        forkJoinNode.setJoinCount(newJoinNode.getJoinCount());
        return null;
    }

    render() {
        const model = this.props.model;
        const bBox = model.viewState.bBox;
        const dropZone = model.viewState.components['drop-zone'];
        const innerDropZoneActivated = this.state.innerDropZoneActivated;
        const innerDropZoneDropNotAllowed = this.state.innerDropZoneDropNotAllowed;
        const dropZoneClassName = ((!innerDropZoneActivated) ? 'inner-drop-zone' : 'inner-drop-zone active')
            + ((innerDropZoneDropNotAllowed) ? ' block' : '');
        const fill = this.state.innerDropZoneExist ? {} : { fill: 'none' };

        const forkLineHiderBottom = model.getJoinBody() ? model.getJoinBody().viewState.bBox.y :
            (model.getTimeoutBody() ? model.getTimeoutBody().viewState.bBox.y : bBox.getBottom());
        const joinLineHiderBottom = model.getTimeoutBody() ? model.getTimeoutBody().viewState.bBox.y :
            (model.getJoinBody() ? model.getJoinBody().viewState.bBox.getBottom() : 0);
        const timeoutLineHiderbottom = model.getTimeoutBody() ? model.getTimeoutBody().viewState.bBox.getBottom()
            : 0;

        const joinConditionEditorOptions = {
            propertyType: 'text',
            key: 'Join condition',
            model: this.props.model,
            getterMethod: this.handleGetJoinCondition,
            setterMethod: this.handleSetJoinCondition,
        };

        const timeoutConditionEditorOptions = {
            propertyType: 'text',
            key: 'Timeout condition',
            model: model.getTimeOutExpression(),
            setterMethod: this.handleSetTimeoutCondition,
        };

        const joinParameterEditorOptions = {
            propertyType: 'text',
            key: 'Join parameter',
            value: model.getJoinResultVar().getSource(),
            model: model.getJoinResultVar(),
            setterMethod: this.handleSetJoinParameter,
        };

        const timeoutParameterEditorOptions = {
            propertyType: 'text',
            key: 'Timeout parameter',
            value: model.getTimeOutVariable().getSource(),
            model: model.getTimeOutVariable(),
            setterMethod: this.handleSetTimeoutParameter,
        };

        return (
            <g>
                <DropZone
                    x={dropZone.x}
                    y={dropZone.y + model.viewState.components['block-header'].h}
                    width={dropZone.w}
                    height={dropZone.h - model.viewState.components['block-header'].h}
                    baseComponent="rect"
                    dropTarget={model.parent}
                    dropBefore={model}
                    renderUponDragStart
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
                    title={'Fork'}
                    model={model}
                    body={model.workers}
                />
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
                <CompoundStatementDecorator
                    dropTarget={model.getJoinBody()}
                    bBox={model.getJoinBody().viewState.bBox}
                    expression={{ text: model.getJoinBody().viewState.components.expression.text }}
                    title={'Join'}
                    model={model.getJoinBody()}
                    body={model.getJoinBody()}
                    parameterBbox={model.getJoinBody().viewState.components.param}
                    parameterEditorOptions={joinParameterEditorOptions}
                    editorOptions={joinConditionEditorOptions}
                />
                }
                {model.getTimeoutBody() &&
                <line
                    x1={model.getTimeoutBody().viewState.bBox.getCenterX()}
                    y1={model.getTimeoutBody().viewState.bBox.y}
                    x2={model.getTimeoutBody().viewState.bBox.getCenterX()}
                    y2={timeoutLineHiderbottom}
                    className='life-line-hider'
                />
                }
                {model.timeoutBody &&
                <CompoundStatementDecorator
                    dropTarget={model.getTimeoutBody()}
                    bBox={model.getTimeoutBody().viewState.bBox}
                    parameterBbox={model.getTimeoutBody().viewState.components.param}
                    expression={{ text: model.getTimeOutExpression().getSource() }}
                    title={'Timeout'}
                    model={model.getTimeoutBody()}
                    body={model.getTimeoutBody()}
                    parameterEditorOptions={timeoutParameterEditorOptions}
                    editorOptions={timeoutConditionEditorOptions}
                />
                }
            </g>
        );
    }
}

ForkJoinNode.propTypes = {
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
        h: PropTypes.number.isRequired,
    }),
};

ForkJoinNode.contextTypes = {
    mode: PropTypes.string,
};

export default ForkJoinNode;
