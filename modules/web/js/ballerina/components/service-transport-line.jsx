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
 * @class ServiceTransportLine
 * @extends {React.Component}
 */
class ServiceTransportLine extends React.Component {

    /**
     * React component render method.
     *
     * @returns {React.Component} ReactElement which represent resource link to server conector.
     * @memberof ServiceTransportLine
     */
    render() {
        const bBox = this.props.bBox;
        let x = bBox.x;
        let y = bBox.y + 50;
        return (
            <g>
                <line x1={bBox.x} y1={bBox.y + 50} x2={bBox.x} y2={bBox.y + bBox.h} stroke="black" strokeWidth="3" className="protocol-line" />
                <line x1={bBox.x - 1} y1={bBox.y + 50 + 2} x2={bBox.x + 60} y2={bBox.y + 15} stroke="black" strokeWidth="3" className="protocol-line" />
                <line x1={bBox.x + 60} y1={bBox.y} x2={bBox.x + 60} y2={bBox.y + 16} stroke="black" strokeWidth="3" className="protocol-line" />
            </g>
        );
    }
}

export default ServiceTransportLine;
