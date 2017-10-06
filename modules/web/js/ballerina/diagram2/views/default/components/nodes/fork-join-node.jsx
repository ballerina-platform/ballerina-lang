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

class ForkJoinNode extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            innerDropZoneActivated: false,
            innerDropZoneDropNotAllowed: false,
            innerDropZoneExist: false,
            active: 'hidden',
        };

        this.joinConditionEditorOptions = {
            propertyType: 'text',
            key: 'Join condition',
            model: props.model.getJoinBody(),
            getterMethod: props.model.getJoinType,
            setterMethod: props.model.setJoinType,
        };

        this.timeoutConditionEditorOptions = {
            propertyType: 'text',
            key: 'Timeout condition',
            model: props.model.getTimeoutBody(),
            getterMethod: props.model.getTimeOutExpression,
            setterMethod: props.model.setTimeOutExpression,
        };

        this.joinParameterEditorOptions = {
            propertyType: 'text',
            key: 'Join parameter',
            value: props.model.getJoinResultVar().getSource(),
            model: props.model.getJoinBody(),
            getterMethod: props.model.getJoinResultVar,
            setterMethod: props.model.setJoinResultVar,
        };

        this.timeoutParameterEditorOptions = {
            propertyType: 'text',
            key: 'Timeout parameter',
            value: props.model.getTimeOutVariable().getSource(),
            model: props.model.getTimeoutBody(),
            getterMethod: props.model.getTimeOutVariable,
            setterMethod: props.model.setTimeOutVariable,
        };
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

        return (
            <g>
                <rect
                    x={dropZone.x}
                    y={dropZone.y}
                    width={dropZone.w}
                    height={dropZone.h}
                    className={dropZoneClassName}
                    {...fill}
                    onMouseOver={this.onDropZoneActivate}
                    onMouseOut={this.onDropZoneDeactivate}
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
                    expression={{text: model.getJoinType()}}
                    title={'Join'}
                    model={model.getJoinBody()}
                    body={model.getJoinBody()}
                    parameterBbox={model.getJoinBody().viewState.components.param}
                    parameterEditorOptions={this.joinParameterEditorOptions}
                    editorOptions={this.joinConditionEditorOptions}
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
                    expression={{text: model.getTimeOutExpression().getSource()}}
                    title={'Timeout'}
                    model={model.getTimeoutBody()}
                    body={model.getTimeoutBody()}
                    parameterEditorOptions={this.timeoutParameterEditorOptions}
                    editorOptions={this.timeoutConditionEditorOptions}
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
