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
 * Add more resources to a service
 *
 * @class ServiceTransportLine
 * @extends {React.Component}
 */
class AddResourceDefinition extends React.Component {

    constructor(props) {
        super(props);
        this.onAddResourceClick = this.onAddResourceClick.bind(this);
    }

    /**
     * Handles the click event when adding a resource to a service
     */
    onAddResourceClick() {
        let serviceDef = this.props.model;
        let thisNodeIndex;
        if (TreeUtil.isResource(this.props.model)) {
            const resourceDef = this.props.model;
            serviceDef = resourceDef.parent;
            thisNodeIndex = serviceDef.getIndexOfResources(resourceDef) + 1;
        }
        // Check if service of http
        if (serviceDef.getProtocolPackageIdentifier().value === 'http') {
            const resource = DefaultNodeFactory.createHTTPResource();
            serviceDef.addResources(resource, thisNodeIndex, true);
            serviceDef.generateDefaultName(serviceDef, resource);
        } else if (serviceDef.getProtocolPackageIdentifier().value === 'ws') {
            let bBox;
            // Check if the node is a service
            if (TreeUtil.isService(this.props.model)) {
                bBox = this.props.model.viewState.components.transportLine;
            } else {
                const thisChildIndex = serviceDef.getIndexOfResources(this.props.model);
                if (TreeUtil.isResource(this.props.model) &&
                    (thisChildIndex !== serviceDef.getResources().length - 1)) {
                    bBox = this.props.model.viewState.bBox;
                }
            }
            const overlayComponents = {
                kind: 'WebsocketResourceSelect',
                props: {
                    key: this.props.model.getID(),
                    model: this.props.model,
                    bBox,
                },
            };
            this.props.model.viewState.showOverlayContainer = true;
            this.props.model.viewState.overlayContainer = overlayComponents;
        } else if (serviceDef.getProtocolPackageIdentifier().value === 'jms') {
            const resource = DefaultNodeFactory.createJMSResource();
            serviceDef.addResources(resource, thisNodeIndex, true);
            serviceDef.generateDefaultName(serviceDef, resource);
        } else if (serviceDef.getProtocolPackageIdentifier().value === 'fs') {
            const resource = DefaultNodeFactory.createFSResource();
            serviceDef.addResources(resource, thisNodeIndex, true);
            serviceDef.generateDefaultName(serviceDef, resource);
        } else if (serviceDef.getProtocolPackageIdentifier().value === 'ftp') {
            const resource = DefaultNodeFactory.createFTPResource();
            serviceDef.addResources(resource, thisNodeIndex, true);
            serviceDef.generateDefaultName(serviceDef, resource);
        }
        this.props.model.trigger('tree-modified', {
            origin: this.props.model,
            type: 'Resource Added',
            title: 'Resource added to Service',
            data: {
                node: this.props.model,
            },
        });
    }

    /**
     * React component render method.
     *
     * @returns {React.Component} ReactElement which represent add resource definition
     * @memberof AddResourceDefinition
     */
    render() {
        const bBox = this.props.model.viewState.bBox;
        const button = new SimpleBBox();
        const label = new SimpleBBox();
        if (TreeUtil.isResource(this.props.model)) {
            button.x = bBox.x - 12.5 - 25;
            button.y = bBox.y + bBox.h + 12.5;
            label.x = button.x + 30;
            label.y = button.y + 12.5;
        }

        if (TreeUtil.isService(this.props.model)) {
            button.x = bBox.x + 12.5;
            button.y = bBox.y + bBox.h - 12.5 - 25;
            label.x = button.x + 30;
            label.y = button.y + 12.5;
        }

        button.w = 25;
        button.h = 25;
        return (
            <g onClick={this.onAddResourceClick}>
                <rect
                    x={button.x}
                    y={button.y}
                    width={button.w}
                    height={button.h}
                    rx={button.w / 2}
                    ry={button.h / 2}
                    className={'add-resource-button'}
                    fill={!_.isEmpty(this.props.model.viewState.overlayContainer) ? '#63605f' : '#a09c9b'}
                />
                <text
                    x={button.x + 5.5}
                    y={button.y + 5.5}
                    width={14}
                    height={14}
                    className='add-resource-button-label'
                >{ImageUtil.getCodePoint('add')}
                </text>
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
                >Add Resource</text>
            </g>
        );
    }
}

AddResourceDefinition.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
};
export default AddResourceDefinition;
