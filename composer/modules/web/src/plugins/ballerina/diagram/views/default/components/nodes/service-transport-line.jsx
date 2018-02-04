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
// import AddResourceDefinition from './add-resource-definition';
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
        const yPosition = this.props.model.viewState.bBox.y + this.props.model.viewState.bBox.h - 25;
        const bBox = this.props.bBox;
        // For Web sockets
        let showAddResourceBtnForWS = true;
        if (this.props.model.getProtocolPackageIdentifier().value === 'ws' && this.props.resources.length >= 6) {
            showAddResourceBtnForWS = false;
        }
        // For JMS, FTP and FS allow only one resource
        let showAddResourceForOneResource = true;
        if ((this.props.model.getProtocolPackageIdentifier().value === 'jms' ||
            this.props.model.getProtocolPackageIdentifier().value === 'ftp' ||
            this.props.model.getProtocolPackageIdentifier().value === 'fs')
            && this.props.resources.length >= 1) {
            showAddResourceForOneResource = false;
        }
        const bodyH = this.props.model.viewState.variablesExpanded ? (this.props.model.viewState.components.body.h +
        (this.props.model.getVariables().length * 30)) :
            this.props.model.viewState.components.body.h - 30;
        return (
            <g>
                {(this.props.addResource && showAddResourceBtnForWS && showAddResourceForOneResource
                && this.props.resources.length && !this.props.model.viewState.collapsed) &&
                <g id='resourceProtocolLine'>
                    {/* <line
                        x1={bBox.x + 30}
                        y1={bBox.y2 - 70}
                        x2={bBox.x + 30}
                        y2={yPosition}
                        strokeWidth='3'
                        strokeDasharray='1,1'
                        className='protocol-line new-node'
                    /> */}
                    <AddResourceDefinition
                        model={this.props.model}
                    />
                </g> }
                {/* this.props.resources.length && <line
                    x1={bBox.x + 30}
                    y1={bBox.y + 50}
                    x2={bBox.x + 30}
                    y2={bBox.y2 - 80}
                    stroke='black'
                    strokeWidth='3'
                    className='protocol-line'
                /> */}
                {!this.props.resources.length &&
                <g>
                    <AddResourceDefinition
                        model={this.props.model}
                    />
                    {/* <line
                        x1={bBox.x + 30}
                        y1={bBox.y + 50}
                        x2={bBox.x + 30}
                        y2={bBox.y + bodyH}
                        stroke='black'
                        strokeWidth='3'
                        className='protocol-line'
                    /> */}
                </g> }
                {/* <line
                    x1={bBox.x + 30}
                    y1={bBox.y + 52}
                    x2={bBox.x + 60}
                    y2={bBox.y + 15}
                    stroke='black'
                    strokeWidth='3'
                    className='protocol-line'
                />
                <line
                    x1={bBox.x + 60}
                    y1={bBox.y - 5}
                    x2={bBox.x + 60}
                    y2={bBox.y + 16}
                    stroke='black'
                    strokeWidth='3'
                    className='protocol-line'
                /> */}
            </g>
        );
    }
}

export default ServiceTransportLine;
