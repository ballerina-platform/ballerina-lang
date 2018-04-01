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
import PanelDecorator from '../decorators/panel-decorator';
import ImageUtil from '../../../../image-util';
import LifeLine from '../decorators/lifeline';
import Client from '../decorators/client';
import FunctionNodeModel from '../../../../../model/tree/function-node';
import { getComponentForNodeArray } from './../../../../diagram-util';
import TreeUtil from '../../../../../model/tree-util';
import EndpointDecorator from '../decorators/endpoint-decorator';
import ReceiverNode from './receiver-node';

class FunctionNode extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            showStructBinding: false,
        };
        this.canDropToPanelBody = this.canDropToPanelBody.bind(this);
        this.onMouseEnter = this.onMouseEnter.bind(this);
        this.onMouseLeave = this.onMouseLeave.bind(this);
    }

    onMouseEnter() {
        if (!TreeUtil.isMainFunction(this.props.model)) {
            this.setState({ showStructBinding: true });
        }
    }

    onMouseLeave() {
        if (_.isEmpty(this.props.model.viewState.overlayContainer)) {
            if (!TreeUtil.isMainFunction(this.props.model)) {
                this.setState({ showStructBinding: false });
            }
        }
    }

    canDropToPanelBody(dragSource) {
        return TreeUtil.isEndpointTypeVariableDef(dragSource)
            || TreeUtil.isWorker(dragSource);
    }

    render() {
        const model = this.props.model;
        const bBox = model.viewState.bBox;
        const name = model.getName().value;
        // change icon for main function
        let icons = 'function';
        if (TreeUtil.isMainFunction(model)) {
            icons = 'main-function';
        }
        const body = this.props.model.getBody();
        const blockNode = getComponentForNodeArray(body, this.context.mode);
        const workers = getComponentForNodeArray(this.props.model.workers, this.context.mode);

        const classes = {
            lineClass: 'default-worker-life-line',
            polygonClass: 'default-worker-life-line-polygon',
            textClass: 'default-worker-icon',
        };

        const connectors = this.props.model.endpointNodes.map((endpointNode) => {
            return (
                <EndpointDecorator
                    model={endpointNode}
                    title={endpointNode.name.value}
                    bBox={endpointNode.viewState.bBox}
                />);
        });
        const nodeDetails = ({ x, y }) => (
            <ReceiverNode
                x={x}
                y={y}
                model={model}
                showStructBinding={this.state.showStructBinding}
            />
        );
        let receiverType;
        if (model.getReceiver()) {
            receiverType = model.getReceiver().getTypeNode().getTypeName().value + ' ' +
                model.getReceiver().getName().value;
        }
        return (
            <g
                onMouseLeave={this.onMouseLeave}
                onMouseEnter={this.onMouseEnter}
            >
                <PanelDecorator
                    bBox={bBox}
                    model={this.props.model}
                    headerComponent={nodeDetails}
                    icon={icons}
                    dropTarget={this.props.model}
                    canDrop={this.canDropToPanelBody}
                    title={name}
                    receiver={receiverType}
                >
                    <Client
                        title='caller'
                        bBox={this.props.model.viewState.components.client}
                    />
                    { this.props.model.getWorkers().length === 0 &&
                        <g>
                            <LifeLine
                                title='default'
                                bBox={this.props.model.viewState.components.defaultWorkerLine}
                                classes={classes}
                                icon={ImageUtil.getCodePoint('worker')}
                                model={this.props.model}
                            />
                            {blockNode}
                        </g>
                    }
                    {workers}
                    {connectors}
                </PanelDecorator> </g>);
    }
}

FunctionNode.propTypes = {
    model: PropTypes.instanceOf(FunctionNodeModel).isRequired,
};

FunctionNode.contextTypes = {
    mode: PropTypes.string,
};

export default FunctionNode;
