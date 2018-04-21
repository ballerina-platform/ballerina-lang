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

import _ from 'lodash';
import React from 'react';
import PropTypes from 'prop-types';
import { Label, Segment } from 'semantic-ui-react';
import { parseFile } from 'api-client/api-client';
import TreeBuilder from 'plugins/ballerina/model/tree-builder';
import File from 'core/workspace/model/file';
import HttpClient from './http-client';

import './try-it-container.scss';


/**
 * Container for the try-it client.
 * @class TryItContainer
 * @extends {React.Component}
 */
class TryItContainer extends React.Component {

    /**
     * Creates an instance of TryItContainer.
     * @param {Object} props Component properties.
     * @memberof TryItContainer
     */
    constructor(props) {
        super(props);
        this.state = {
            selectedClientType: undefined,
            compilationUnit: undefined,
        };

        this.onClientTypeSelect = this.onClientTypeSelect.bind(this);
    }

    /**
     * @override
     */
    componentDidMount() {
        this.updateCompilationUnit();
        this.forceUpdate();
    }

    /**
     * Event handler when a client type is selected.
     * @param {string} eventKey The client type.
     * @memberof TryItContainer
     */
    onClientTypeSelect(eventKey) {
        this.setState({
            selectedClientType: eventKey,
        });
    }

    /**
     * Filters services base on protocol.
     * @param {string} type The protocol to filter by.
     * @returns {ServiceNode[]} Filtered services.
     * @memberof TryItContainer
     */
    filterServices(type) {
        if (this.state.compilationUnit) {
            const services = this.state.compilationUnit.filterTopLevelNodes({ kind: 'Service' });
            return services.filter((serviceNode) => {
                return serviceNode.getServiceTypeStruct().getPackageAlias().getValue() === type;
            });
        } else {
            return [];
        }
    }

    /**
     * Updates the compilation unit for the recieved file.
     * @memberof TryItContainer
     */
    updateCompilationUnit() {
        if (this.props.balFile) {
            parseFile(this.props.balFile)
                .then((jsonTree) => {
                    if (!(_.isNil(jsonTree.model) || _.isNil(jsonTree.model.kind))) {
                        const ast = TreeBuilder.build(jsonTree.model);
                        this.setState({
                            compilationUnit: ast,
                        });
                    }
                }).catch((e) => {
                    this.setState({
                        compilationUnit: undefined,
                    });
                });
        }
    }

    /**
     * Renders the client
     * @returns {ReactElement} The client.
     * @memberof TryItContainer
     */
    renderClientTypePills() {
        const httpServices = this.filterServices('http');
        const wsServices = this.filterServices('ws');
        const jmsServices = this.filterServices('jms');
        let activeKey;
        if (this.state.selectedClientType === undefined) {
            if (httpServices.length > 0) {
                activeKey = 'http';
            } else if (wsServices.length > 0) {
                activeKey = 'ws';
            } else if (jmsServices.length > 0) {
                activeKey = 'jms';
            } else {
                return (<Segment inverted><span>No Services Found!</span></Segment>);
            }
        } else {
            activeKey = this.state.selectedClientType;
        }
        return (<div activeKey={activeKey} onSelect={this.onClientTypeSelect}>
            {httpServices.length > 0 ? <Label eventKey={'http'}>Try-It for Http</Label> : (null)}
            {wsServices.length > 0 ? <Label eventKey={'ws'}>Try-It for Websockets</Label> : (null)}
            {jmsServices.length > 0 ? <Label eventKey={'jms'}>Try-It for JMS</Label> : (null)}
        </div>);
    }

    /**
     * Renders the client view.
     * @returns {ReactElement} The view.
     * @memberof TryItContainer
     */
    renderClientView() {
        const httpServices = this.filterServices('http');
        const wsServices = this.filterServices('ws');
        const jmsServices = this.filterServices('jms');
        let clientType = '';
        if (this.state.selectedClientType === undefined) {
            if (httpServices.length > 0) {
                clientType = 'http';
            } else if (wsServices.length > 0) {
                clientType = 'ws';
            } else if (jmsServices.length > 0) {
                clientType = 'jms';
            }
        } else {
            clientType = this.state.selectedClientType;
        }

        if (clientType === 'http') {
            return (<HttpClient serviceNodes={httpServices} />);
        } else if (clientType === 'ws') {
            return (<span>Websocket client is not yet supported.</span>);
        } else if (clientType === 'jms') {
            return (<span>JMS client is not yet supported.</span>);
        } else {
            return (null);
        }
    }

    /**
     * Renders the client.
     * @returns {ReactElement} The view.
     * @memberof TryItContainer
     */
    render() {
        const clientTypePills = this.renderClientTypePills();
        const clientView = this.renderClientView();
        return (<div className='try-it-container'>
            <div className='client-type-pills-wrapper'>
                {clientTypePills}
            </div>
            {clientView}
        </div>);
    }
}

TryItContainer.propTypes = {
    balFile: PropTypes.instanceOf(File),
};

TryItContainer.defaultProps = {
    balFile: undefined,
};

export default TryItContainer;
