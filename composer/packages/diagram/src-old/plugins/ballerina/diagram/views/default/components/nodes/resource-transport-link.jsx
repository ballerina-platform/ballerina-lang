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
class ResourceTransportLink extends React.Component {

    /**
     * React component render method.
     *
     * @returns {React.Component} ReactElement which represent resource link to server conector.
     * @memberof ResourceTransportLink
     */
    render() {
        const bBox = this.props.bBox;
        return (
            <g>
                <line x1={bBox.x - 25} y1={bBox.y + 15} x2={bBox.x} y2={bBox.y + 15} stroke='black' strokeWidth='3' className='protocol-line' />
                <circle cx={bBox.x - 25} cy={bBox.y + 15} r='6' stroke='black' strokeWidth='3' fill='white' className='protocol-line' />
            </g>
        );
    }
}

export default ResourceTransportLink;
