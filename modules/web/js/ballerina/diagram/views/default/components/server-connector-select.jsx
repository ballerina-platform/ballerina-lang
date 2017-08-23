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

/**
 * Link to the server connector from resource.
 *
 * @class ResourceTransportLink
 * @extends {React.Component}
 */
class ServerConnectorSelect extends React.Component {

    /**
     * React component render method.
     *
     * @returns {React.Component} ReactElement which represent resource link to server conector.
     * @memberof ServerConnectorSelect
     */
    render() {
        const bBox = this.props.bBox;
        return (
            <g>
                <line x1={bBox.x} y1={bBox.y + 50} x2={bBox.x} y2={bBox.y + bBox.h} stroke="black" strokeWidth="2" />
            </g>
        );
    }
}

export default ServerConnectorSelect;
