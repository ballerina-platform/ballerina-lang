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
import './drop-down-menu.css';
import ASTFactory from '../../../../ast/ast-factory';
/**
 * React component for a drop down menu
 *
 * @class DropdownMenu
 * @extends {React.Component}
 */
class DropdownMenu extends React.Component {

    constructor(props) {
        super(props);
        this.addedResources = this.getAddedResources();
        // ToDo make it dynamic
        this.webSocketResources = ['onOpen', 'onTextMessage', 'onHandshake', 'onBinaryMessage',
            'onClose', 'onIdleTimeOut'];
        this.resourcesNotAdded = this.getResourcesNotAdded();
        this.addResource = this.addResource.bind(this);
        this.getAddedResources = this.getAddedResources.bind(this);
        this.getChildResources = this.getChildResources.bind(this);
        this.getResourcesNotAdded = this.getResourcesNotAdded.bind(this);
        this.handleOutsideClick = this.handleOutsideClick.bind(this);
    }

    componentDidMount() {
        if (this.props.model.getViewState().showWebSocketMethods) {
            document.addEventListener('mouseup', this.handleOutsideClick, false);
        } else {
            document.removeEventListener('mouseup', this.handleOutsideClick, false);
        }
    }

    componentDidUpdate() {
        if (this.props.model.getViewState().showWebSocketMethods) {
            document.addEventListener('mouseup', this.handleOutsideClick, false);
        } else {
            document.removeEventListener('mouseup', this.handleOutsideClick, false);
        }
    }

    componentWillUnmount() {
        document.removeEventListener('mouseup', this.handleOutsideClick, false);
    }

    /**
     * Get resource definitions of a service
     * @param node
     * @returns {Array}
     */
    getChildResources(node) {
        const addedMethods = [];
        if (node.getChildren()) {
            node.getChildren().map((child) => {
                if (ASTFactory.isResourceDefinition(child)) {
                    addedMethods.push(child.getResourceName());
                }
            });
        }
        return addedMethods;
    }

    /**
     * Get the existing/added resource defs for a sevice
     * @returns {Array}
     */
    getAddedResources() {
        let addedResources = [];
        const node = this.props.model;
        if (ASTFactory.isServiceDefinition(node)) {
            addedResources = this.getChildResources(node);
        } else if (ASTFactory.isResourceDefinition(node)) {
            addedResources = this.getChildResources(node.getParent());
        }
        return addedResources;
    }

    /**
     * Get the resources that are not added for a ws service
     */
    getResourcesNotAdded() {
        return _.differenceWith(this.webSocketResources, this.addedResources, _.isEqual);
    }

    /**
     * Get info about the predefined resources of ws
     * @returns {[*,*,*,*,*,*]}
     */
    getWebSocketResourceInfo() {
        return [
            {
                resourceName: 'onHandshake',
                parameters: [
                    {
                        type: 'ws:HandshakeConnection',
                        value: 'conn',
                    },
                ],
            },
            {
                resourceName: 'onOpen',
                parameters: [
                    {
                        type: 'ws:Connection',
                        value: 'conn',
                    },
                ],
            },
            {
                resourceName: 'onTextMessage',
                parameters: [
                    {
                        type: 'ws:Connection',
                        value: 'conn',
                    },
                    {
                        type: 'ws:TextFrame',
                        value: 'frame',
                    },
                ],
            },
            {
                resourceName: 'onBinaryMessage',
                parameters: [
                    {
                        type: 'ws:Connection',
                        value: 'conn',
                    },
                    {
                        type: 'ws:BinaryFrame',
                        value: 'frame',
                    },
                ],
            },
            {
                resourceName: 'onClose',
                parameters: [
                    {
                        type: 'ws:Connection',
                        value: 'conn',
                    },
                    {
                        type: 'ws:CloseFrame',
                        value: 'frame',
                    },
                ],
            },
            {
                resourceName: 'onIdleTimeOut',
                parameters: [
                    {
                        type: 'Connection',
                        value: 'conn',
                    },
                ],
            },
        ];
    }

    /**
     * Add a new resource to a ws service
     * @param value
     */
    addResource(value) {
        this.getWebSocketResourceInfo().map((resource) => {
            if (resource.resourceName === value) {
                let serviceDef = this.props.model;
                let thisNodeIndex;
                if (ASTFactory.isResourceDefinition(this.props.model)) {
                    const resourceDef = this.props.model;
                    serviceDef = resourceDef.getParent();
                    thisNodeIndex = serviceDef.getIndexOfChild(resourceDef) + 1;
                }
                const newResourceDef = ASTFactory.createResourceDefinition(resource);
                if (resource.parameters.length > 0) {
                    // Define the argument param definition holder
                    const argumentParameterDefinitionHolder = ASTFactory.createArgumentParameterDefinitionHolder();
                    resource.parameters.map((parameter) => {
                        const parameterDef = ASTFactory.createParameterDefinition(resource);
                        parameterDef.setTypeName(parameter.type);
                        parameterDef.setName(parameter.value);
                        argumentParameterDefinitionHolder.addChild(parameterDef);
                    });
                    newResourceDef.addChild(argumentParameterDefinitionHolder);
                }
                serviceDef.addChild(newResourceDef, thisNodeIndex, undefined, undefined, true);
            }
        });
        this.props.model.getViewState().showWebSocketMethods = false;
        this.props.editor.update();
    }

    /**
     * Handles the outside click event of the drop down
     * @param e
     */
    handleOutsideClick(e) {
        if (this.node) {
            if (!this.node.contains(e.target)) {
                this.props.model.getViewState().showWebSocketMethods = false;
                this.props.editor.update();
            }
        }
    }

    /**
     * Renders the view for a drop down menu
     *
     * @returns {ReactElement} The view.
     * @memberof DropdownMenu
     */
    render() {
        this.addedResources = this.getAddedResources();
        this.resourcesNotAdded = this.getResourcesNotAdded();
        const yPosition = this.props.model.viewState.bBox.y + this.props.model.viewState.bBox.h - 40;
        let yPositionOfDropDown = 30;
        let xPositionOfDropDown = -10;
        if (ASTFactory.isResourceDefinition(this.props.model)) {
            yPositionOfDropDown = 78;
            xPositionOfDropDown = 35;
        }

        const style = {
            display: this.props.model.getViewState().showWebSocketMethods ? 'block' : 'none',
            top: yPosition + yPositionOfDropDown,
            left: this.props.bBox.x + xPositionOfDropDown,
        };
        return (
            <div
                id="myDropdown"
                className="dropdown-content"
                style={style}
                ref={(node) => { this.node = node; }}
            >
                {this.resourcesNotAdded.map((resource) => {
                    return (<a
                        key={resource}
                        name={resource}
                        onClick={e => this.addResource(resource)}
                    >{resource}</a>);
                })}
                {(this.resourcesNotAdded.length === 0) &&
                <a>All Resources Added</a>}
            </div>);
    }
}

export default DropdownMenu;
