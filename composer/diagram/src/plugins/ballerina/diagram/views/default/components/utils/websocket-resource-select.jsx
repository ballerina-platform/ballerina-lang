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
import './abstract-drop-down.css';
import TreeUtil from './../../../../../model/tree-util';
import DefaultNodeFactory from './../../../../../model/default-node-factory';
/**
 * React component for a drop down menu
 *
 * @class WebsocketResourceSelect
 * @extends {React.Component}
 */
class WebsocketResourceSelect extends React.Component {

    constructor(props) {
        super(props);
        this.addedResources = this.getAddedResources();
        // ToDo make it dynamic
        this.webSocketResources = ['onOpen', 'onTextMessage', 'onHandshake', 'onBinaryMessage',
            'onClose', 'onIdleTimeOut', 'onPing', 'onPong'];
        this.resourcesNotAdded = this.getResourcesNotAdded();
        this.addResource = this.addResource.bind(this);
        this.getAddedResources = this.getAddedResources.bind(this);
        this.getChildResources = this.getChildResources.bind(this);
        this.getResourcesNotAdded = this.getResourcesNotAdded.bind(this);
        this.handleOutsideClick = this.handleOutsideClick.bind(this);
    }

    componentDidMount() {
        if (this.props.model.props.model.viewState.showOverlayContainer) {
            document.addEventListener('mouseup', this.handleOutsideClick, false);
        } else {
            document.removeEventListener('mouseup', this.handleOutsideClick, false);
        }
    }

    componentDidUpdate() {
        if (this.props.model.props.model.viewState.showOverlayContainer) {
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
        if (node.getResources()) {
            node.getResources().forEach((child) => {
                if (TreeUtil.isResource(child)) {
                    addedMethods.push(child.getName().value);
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
        const node = this.props.model.props.model;
        if (TreeUtil.isService(node)) {
            addedResources = this.getChildResources(node);
        } else if (TreeUtil.isResource(node)) {
            addedResources = this.getChildResources(node.parent);
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
                fragment: `
                resource onHandshake(ws:HandshakeConnection conn) {

                }
            `,
            },
            {
                resourceName: 'onOpen',
                fragment: `
                resource onOpen(ws:Connection conn) {

                }
            `,
            },
            {
                resourceName: 'onTextMessage',
                fragment: `
                resource onTextMessage(ws:Connection conn, ws:TextFrame frame) {

                }
            `,
            },
            {
                resourceName: 'onBinaryMessage',
                fragment: `
                resource onBinaryMessage(ws:Connection conn, ws:BinaryFrame frame) {

                }
            `,
            },
            {
                resourceName: 'onClose',
                fragment: `
                resource onClose(ws:Connection conn, ws:CloseFrame frame) {

                }
            `,
            },
            {
                resourceName: 'onIdleTimeOut',
                fragment: `
                resource onIdleTimeOut(ws:Connection conn) {

                }
            `,
            },
            {
                resourceName: 'onPing',
                fragment: `
                resource onPing(ws:Connection conn, ws:PingFrame frame) {

                }
            `,
            },
            {
                resourceName: 'onPong',
                fragment: `
                resource onPong(ws:Connection conn, ws:PongFrame frame) {

                }
            `,
            },
        ];
    }

    /**
     * Add a new resource to a ws service
     * @param value
     */
    addResource(value) {
        const props = this.props.model.props;
        this.getWebSocketResourceInfo().forEach((resource) => {
            if (resource.resourceName === value) {
                let serviceDef = props.model;
                let thisNodeIndex;
                if (TreeUtil.isResource(props.model)) {
                    const resourceDef = props.model;
                    serviceDef = resourceDef.parent;
                    thisNodeIndex = serviceDef.getIndexOfResources(resourceDef) + 1;
                }
                serviceDef.addResources(DefaultNodeFactory.createWSResource(resource.fragment), thisNodeIndex);
            }
        });
        props.model.viewState.showOverlayContainer = false;
        props.model.viewState.overlayContainer = {};
        this.context.editor.update();
    }

    /**
     * Handles the outside click event of the drop down
     * @param e
     */
    handleOutsideClick(e) {
        if (this.node) {
            if (!this.node.contains(e.target)) {
                this.props.model.props.model.viewState.showOverlayContainer = false;
                this.props.model.props.model.viewState.overlayContainer = {};
                this.context.editor.update();
            }
        }
    }

    /**
     * Renders the view for a drop down menu
     *
     * @returns {ReactElement} The view.
     * @memberof WebsocketResourceSelect
     */
    render() {
        const props = this.props.model.props;
        this.addedResources = this.getAddedResources();
        this.resourcesNotAdded = this.getResourcesNotAdded();
        const yPosition = props.model.viewState.bBox.y + props.model.viewState.bBox.h - 40;
        let yPositionOfDropDown = 30;
        let xPositionOfDropDown = 20;
        if (TreeUtil.isResource(props.model)) {
            yPositionOfDropDown = 78;
            xPositionOfDropDown = -35;
        }

        const style = {
            display: props.model.viewState.showOverlayContainer ? 'block' : 'none',
            top: yPosition + yPositionOfDropDown,
            left: props.bBox.x + xPositionOfDropDown,
        };
        return (
            <div
                id='myDropdown'
                className='dropdown-content'
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
            </div>);
    }
}

WebsocketResourceSelect.propTypes = {
    model: PropTypes.instanceOf(Object).isRequired,
};

WebsocketResourceSelect.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
};

export default WebsocketResourceSelect;
