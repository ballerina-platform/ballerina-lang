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
// import ServiceDefinition from 'ballerina/ast/service-definition';
import PropTypes from 'prop-types';
import HttpClient from './http-client';

/**
 * Container for the try-it client.
 * @class TryItContainer
 * @extends {React.Component}
 */
class TryItContainer extends React.Component {

    /**
     * Renders the client
     * @returns {ReactElement} The client.
     * @memberof TryItContainer
     */
    renderClient() {
        // if (this.props.serviceDefinition.getProtocolPkgName() === 'http') {
        return (<HttpClient />);
        // } else {
            // return (null);
        // }
    }

    /**
     * Renders the client.
     * @returns {ReactElement} The view.
     * @memberof TryItContainer
     */
    render() {
        const client = this.renderClient();
        return (<div className='try-it-container'>
            {client}
        </div>);
    }
}

TryItContainer.propTypes = {
//     serviceDefinition: PropTypes.instanceOf(ServiceDefinition).isRequired,
};

export default TryItContainer;
