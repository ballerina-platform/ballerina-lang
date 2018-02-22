/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import PropTypes, { element } from 'prop-types';
import Area from './area';
import IconButton from './icon-button';
import Menu from './menu';
import Item from './item';
import Search from './search';
import DefaultNodeFactory from '../model/default-node-factory';
import { WsResources } from '../model/default-node-factory';
import TreeUtil from '../model/tree-util';
import ConnectorAction from '../env/connector-action';

/**
 * Interaction lifeline button component
 */
class ServiceMenu extends React.Component {

    constructor(props, contex) {
        super();
        this.handleAddResource = this.handleAddResource.bind(this);
        this.handleList = this.handleList.bind(this);
        this.handleExpand = this.handleExpand.bind(this);
        this.handleAddWsResource = this.handleAddWsResource.bind(this);
        this.state = {
            wsList: false,
        };
    }

    /**
     * Handles the click event when adding a resource to a service
     */
    handleAddResource() {
        const serviceDef = this.props.model;
        const thisNodeIndex = serviceDef.resources.length;
        // Check if service of http
        if (serviceDef.getProtocolPackageIdentifier().value === 'http') {
            const resource = DefaultNodeFactory.createHTTPResource();
            serviceDef.addResources(resource, thisNodeIndex, true);
            serviceDef.generateDefaultName(serviceDef, resource);
        } else if (serviceDef.getProtocolPackageIdentifier().value === 'ws') {
            this.setState({ wsList: true });
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

    handleAddWsResource(el) {
        const serviceDef = this.props.model;
        const thisNodeIndex = serviceDef.resources.length;
        const resource = DefaultNodeFactory.createWSResource(el.fragment);
        serviceDef.addResources(resource, thisNodeIndex, true);
        this.props.model.trigger('tree-modified', {
            origin: this.props.model,
            type: 'Resource Added',
            title: 'Resource added to Service',
            data: {
                node: this.props.model,
            },
        });
    }

    handleExpand() {
        this.props.model.resources.forEach((resource) => {
            resource.viewState.collapsed = false;
        });
        this.context.editor.update();
    }

    handleList() {
        this.props.model.resources.forEach((resource) => {
            resource.viewState.collapsed = true;
        });
        this.context.editor.update();
    }

    componentWillUnmount() {
        this.setState({ wsList: false });
    }

    /**
     * render hover area and button
     * @return {object} button rendering object
     */
    render() {
        const resources = WsResources.filter((e) => {
            let notFound = true;
            this.props.model.resources.forEach((resource) => {
                if (resource.name.value === e.resourceName) {
                    notFound = false;
                }
            });
            return notFound;
        });
        return (
            <Area bBox={this.props.bBox}>
                <IconButton
                    buttonX={0}
                    buttonY={0}
                    showAlways
                    buttonIconColor='#333'
                    menuOverButton
                    icon='more-vert'
                    onClick={() => { this.setState({ wsList: false }); }}
                >
                    { !this.state.wsList &&
                        <Menu>
                            <Item
                                label='Add Resource'
                                icon='fw fw-resource'
                                closeMenu={false}
                                callback={this.handleAddResource}
                            />
                            <Item
                                label='List'
                                icon='fw fw-n'
                                closeMenu
                                callback={this.handleList}
                            />
                            <Item
                                label='Expand'
                                icon='fw fw-n'
                                closeMenu
                                callback={this.handleExpand}
                            />
                        </Menu>
                    }
                    { this.state.wsList &&
                        <Menu>
                            { resources.map((element) => {
                                return (<Item
                                    label={'Add ' + element.resourceName}
                                    icon='fw fw-resource'
                                    closeMenu
                                    callback={() => { this.handleAddWsResource(element); }}
                                />);
                            })}
                        </Menu>
                    }
                </IconButton>
            </Area>
        );
    }
}

ServiceMenu.propTypes = {

};

ServiceMenu.defaultProps = {
    button: { x: 0, y: 0 },
    showAlways: false,
};

ServiceMenu.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
};

export default ServiceMenu;
