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
import './service-definition.css';
import AddResourceDefinition from './add-resource-definition';
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
        const yPosition = this.props.model.viewState.bBox.y + this.props.model.viewState.bBox.h - 40;
        const bBox = this.props.bBox;
        return (
            <g>
                {this.props.resources.length &&
                <g id="resourceProtocolLine" className={this.props.style}>
                    <AddResourceDefinition
                        model={this.props.model}
                        bBox={this.props.bBox}
                    />
                    <line
                        x1={bBox.x}
                        y1={bBox.y2}
                        x2={bBox.x}
                        y2={yPosition}
                        strokeDasharray="5, 5"
                        strokeWidth="3"
                        className="add-resources-protocol-line"
                    />
                </g> }
                {this.props.resources.length && <line
                    x1={bBox.x}
                    y1={bBox.y + 50}
                    x2={bBox.x}
                    y2={bBox.y2}
                    stroke="black"
                    strokeWidth="3"
                    className="protocol-line"
                />}
                {!this.props.resources.length &&
                <g>
                    <AddResourceDefinition
                        model={this.props.model}
                        bBox={this.props.bBox}
                    />
                    <line
                        x1={bBox.x}
                        y1={bBox.y + 50}
                        x2={bBox.x}
                        y2={bBox.y + this.props.model.viewState.components.body.h - 30}
                        stroke="black"
                        strokeWidth="3"
                        className="protocol-line"
                    />
                </g> }
                <line
                    x1={bBox.x - 1}
                    y1={bBox.y + 50 + 2}
                    x2={bBox.x + 60}
                    y2={bBox.y + 15}
                    stroke="black"
                    strokeWidth="3"
                    className="protocol-line"
                />
                <line
                    x1={bBox.x + 60}
                    y1={bBox.y}
                    x2={bBox.x + 60}
                    y2={bBox.y + 16}
                    stroke="black"
                    strokeWidth="3"
                    className="protocol-line"
                />
            </g>
        );
    }
}

export default ServiceTransportLine;
