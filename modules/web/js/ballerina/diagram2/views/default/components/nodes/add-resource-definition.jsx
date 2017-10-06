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
            serviceDef.addResources(DefaultNodeFactory.createHTTPResource(), thisNodeIndex);
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
                kind: 'DropdownMenu',
                props: {
                    key: this.props.model.getID(),
                    model: this.props.model,
                    bBox,
                    editor: this.context.editor,
                },
            };
            this.props.model.viewState.overlayContainer = overlayComponents;
            this.context.editor.update();
        }
    }

    /**
     * React component render method.
     *
     * @returns {React.Component} ReactElement which represent add resource definition
     * @memberof AddResourceDefinition
     */
    render() {
        const bBox = this.props.bBox;
        let yPosition = this.props.model.viewState.bBox.y + this.props.model.viewState.bBox.h - 40;
        let yPositionOfResourceBtn = 5;
        let yPositionOfResourceBtnLbl = 17;
        let xPositionOfResourceBtn = 15;
        let xPositionOfResourceBtnLbl = 22;
        let xPositionOfAddResourceRect = 35;
        let xPositionOfAddResourceLbl = 85;
        if (TreeUtil.isResource(this.props.model)) {
            yPosition = bBox.y + bBox.h;
            yPositionOfResourceBtn = 12;
            yPositionOfResourceBtnLbl = 24;
            xPositionOfResourceBtn = 35;
            xPositionOfResourceBtnLbl = 43;
            xPositionOfAddResourceRect = 55;
            xPositionOfAddResourceLbl = 105;
        }
        return (
            <g onClick={this.onAddResourceClick}>
                <rect
                    x={bBox.x + xPositionOfAddResourceRect}
                    y={yPosition + yPositionOfResourceBtn}
                    width="100"
                    height="25"
                    fill="#a09c9b"
                />
                <text
                    x={bBox.x + xPositionOfAddResourceLbl}
                    y={yPosition + yPositionOfResourceBtnLbl}
                    alignmentBaseline="middle"
                    textAnchor="middle"
                    className="add-resource-label"
                >Add Resource</text>
                <rect
                    x={bBox.x + xPositionOfResourceBtn}
                    y={yPosition + yPositionOfResourceBtn}
                    width={25}
                    height={25}
                    rx={12.5}
                    ry={12.5}
                    className={'add-resource-button'}
                    fill={!_.isEmpty(this.props.model.viewState.overlayContainer) ? '#63605f' : '#a09c9b'}
                />
                <text
                    x={bBox.x + xPositionOfResourceBtnLbl}
                    y={yPosition + yPositionOfResourceBtnLbl}
                    width={25}
                    height={25}
                    className="add-resource-button-label"
                >+</text>
            </g>
        );
    }
}

AddResourceDefinition.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
};
export default AddResourceDefinition;
