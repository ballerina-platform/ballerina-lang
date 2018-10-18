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
import './service-definition.css';
import TreeUtil from './../../../../../model/tree-util';
import DefaultNodeFactory from './../../../../../model/default-node-factory';
import SimpleBBox from './../../../../../model/view/simple-bounding-box';
import ImageUtil from '../../../../image-util';
/**
 * Add more actions to a connector
 *
 * @class AddActionNode
 * @extends {React.Component}
 */
class AddActionNode extends React.Component {

    constructor(props) {
        super(props);
        this.onAddActionClick = this.onAddActionClick.bind(this);
    }

    /**
     * Handles the click event when adding an action to a connector
     */
    onAddActionClick() {
        let connectorNode = this.props.model;
        let thisNodeIndex;
        if (TreeUtil.isAction(this.props.model)) {
            const actionNode = this.props.model;
            connectorNode = actionNode.parent;
            thisNodeIndex = connectorNode.getIndexOfActions(actionNode) + 1;
        }

        const action = DefaultNodeFactory.createConnectorAction();
        connectorNode.addActions(action, thisNodeIndex, true);
        connectorNode.generateDefaultName(connectorNode, action);
        this.props.model.trigger('tree-modified', {
            origin: this.props.model,
            type: 'Action Added',
            title: 'Action added to Connector',
            data: {
                node: this.props.model,
            },
        });
    }

    /**
     * React component render method.
     *
     * @returns {React.Component} ReactElement which represent add action node
     * @memberof AddActionNode
     */
    render() {
        const bBox = this.props.model.viewState.bBox;
        const button = new SimpleBBox();
        const label = new SimpleBBox();
        if (TreeUtil.isAction(this.props.model)) {
            button.x = bBox.x - 12.5 - 25;
            button.y = bBox.y + bBox.h + 12.5;
            label.x = button.x + 30;
            label.y = button.y + 12.5;
        }

        if (TreeUtil.isConnector(this.props.model)) {
            button.x = bBox.x + 12.5;
            button.y = bBox.y + bBox.h - 12.5 - 25;
            label.x = button.x + 30;
            label.y = button.y + 12.5;
        }

        button.w = 25;
        button.h = 25;
        return (
            <g onClick={this.onAddActionClick}>
                <rect
                    x={button.x}
                    y={button.y}
                    width={button.w}
                    height={button.h}
                    rx={button.w / 2}
                    ry={button.h / 2}
                    className={'add-resource-button'}
                    fill='#3498db'
                />
                <image
                    x={button.x + 5.5}
                    y={button.y + 5.5}
                    width={14}
                    height={14}
                    fill='#fff'
                    xlinkHref={ImageUtil.getCodePoint('add')}
                    className='add-resource-button-label'
                />
                <polygon
                    points={`${label.x},${label.y} ${label.x + 12.5},${label.y - 12.5} ${label.x + 120},${label.y - 12.5} ${label.x + 120},${label.y + 12.5} ${label.x + 12.5},${label.y + 12.5}`}
                    fill='#dcdcdc'
                />
                <text
                    x={label.x + 60}
                    y={label.y}
                    alignmentBaseline='middle'
                    textAnchor='middle'
                    className='add-resource-label'
                >Add Action</text>
            </g>
        );
    }
}

AddActionNode.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
};
export default AddActionNode;
