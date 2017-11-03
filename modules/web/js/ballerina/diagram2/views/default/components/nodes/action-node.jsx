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
import _ from 'lodash';
import PropTypes from 'prop-types';
import LifeLineDecorator from './../decorators/lifeline.jsx';
import PanelDecorator from './../decorators/panel-decorator';
import { getComponentForNodeArray } from './../../../../diagram-util';
import { lifeLine } from './../../designer-defaults';
import ImageUtil from './../../../../image-util';
import './service-definition.css';
import TreeUtil from '../../../../../model/tree-util';
import EndpointDecorator from '../decorators/endpoint-decorator';
import StatementDropZone from '../../../../../drag-drop/DropZone';
import AddActionNode from './add-action-node';

class ActionNode extends React.Component {

    /**
     * Creates an instance of ActionNode.
     * @param {Object} props React properties.
     * @memberof ActionNode
     */
    constructor(props) {
        super(props);
        this.state = {
            style: 'hideResourceGroup',
        };
        this.onMouseOut = this.onMouseOut.bind(this);
        this.onMouseOver = this.onMouseOver.bind(this);
    }

    canDropToPanelBody(dragSource) {
        return TreeUtil.isEndpointTypeVariableDef(dragSource)
            || TreeUtil.isWorker(dragSource);
    }

    /**
     * Handles the mouse over event of the add action button
     */
    onMouseOver() {
        this.setState({ style: 'showResourceGroup' });
    }

    /**
     * Handles the mouse out event of the add action button
     */
    onMouseOut() {
        if (_.isEmpty(this.props.model.viewState.overlayContainer)) {
            this.setState({ style: 'hideResourceGroup' });
        }
    }

    render() {
        const bBox = this.props.model.viewState.bBox;
        const name = this.props.model.getName().value;
        const statementContainerBBox = this.props.model.body.viewState.bBox;
        const bodyBBox = this.props.model.body.viewState.bBox;
        const body = this.props.model.getBody();
        const action_worker_bBox = {};
        action_worker_bBox.x = statementContainerBBox.x + (statementContainerBBox.w - lifeLine.width) / 2;
        action_worker_bBox.y = statementContainerBBox.y - lifeLine.head.height;
        action_worker_bBox.w = lifeLine.width;
        action_worker_bBox.h = statementContainerBBox.h + lifeLine.head.height * 2;

        const classes = {
            lineClass: 'default-worker-life-line',
            polygonClass: 'default-worker-life-line-polygon',
        };
        const argumentParameters = this.props.model.getParameters();
        const returnParameters = this.props.model.getReturnParameters();

        const blockNode = getComponentForNodeArray(this.props.model.getBody(), this.context.mode);
        const connectors = this.props.model.body.statements
            .filter((element) => { return TreeUtil.isEndpointTypeVariableDef(element); }).map((statement) => {
                return (
                    <EndpointDecorator
                        model={statement}
                        title={statement.variable.name.value}
                        bBox={statement.viewState.bBox}
                    />);
            });
        const workers = getComponentForNodeArray(this.props.model.workers, this.context.mode);
        let annotationBodyHeight = 0;
        if (!_.isNil(this.props.model.viewState.components.annotation)) {
            annotationBodyHeight = this.props.model.viewState.components.annotation.h;
        }

        const tLinkBox = Object.assign({}, bBox);
        tLinkBox.y += annotationBodyHeight;
        const parentNode = this.props.model.parent;
        const thisNodeIndex = parentNode.getIndexOfActions(this.props.model);
        return (
            <g>
                <PanelDecorator
                    icon="tool-icons/action"
                    title={name}
                    bBox={bBox}
                    model={this.props.model}
                    dropTarget={this.props.model}
                    canDrop={this.canDropToPanelBody}
                    argumentParams={argumentParameters}
                    returnParams={returnParameters}
                >
                    <g>
                        { this.props.model.getWorkers().length === 0 &&
                            <g>
                                <StatementDropZone
                                    x={bodyBBox.x}
                                    y={bodyBBox.y}
                                    width={bodyBBox.w}
                                    height={bodyBBox.h}
                                    baseComponent="rect"
                                    dropTarget={body}
                                    enableDragBg
                                />
                                <LifeLineDecorator
                                    title="default"
                                    bBox={action_worker_bBox}
                                    classes={classes}
                                    icon={ImageUtil.getSVGIconString('tool-icons/worker-white')}
                                    iconColor='#025482'
                                />
                                {blockNode}
                            </g>
                        }{
                            this.props.model.workers.map((item) => {
                                return (<StatementDropZone
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
                        {workers}
                        {connectors}
                    </g>
                </PanelDecorator>
                {(thisNodeIndex !== parentNode.getActions().length - 1 &&
                !this.props.model.viewState.collapsed) &&
                <g
                    className={this.state.style}
                    onMouseOver={this.onMouseOver}
                    onMouseOut={this.onMouseOut}
                >
                    <rect
                        x={bBox.x - 50}
                        y={bBox.y + bBox.h}
                        width={bBox.w + 20}
                        height='50'
                        fillOpacity="0"
                        strokeOpacity="0"
                    />
                    <AddActionNode model={this.props.model} />
                </g>
                }
            </g>);
    }
}

ActionNode.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
    mode: PropTypes.string,
};

export default ActionNode;
