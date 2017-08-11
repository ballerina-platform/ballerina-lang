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
import VariableEndpoint from './variable-endpoint.jsx';
import './tree.css';

export default class Tree extends React.Component {
    renderStruct(endpoint, level) {
        return (<div key={endpoint.name}>
            {
                this.renderEndpoint(endpoint, (endpoint.isInner ? 'property' : 'struct-head'), level)
            }
            {
                endpoint.properties.map((property) => {
                    if (property.innerType) {
                        property.innerType.root = endpoint.root || endpoint;
                        return this.renderStruct(property.innerType, level + 1);
                    }
                    property.root = endpoint.root || endpoint;
                    return this.renderEndpoint(property, 'property', level + 1);
                })
            }
        </div>);
    }

    renderEndpoint(endpoint, kind, level) {
        const { endpoints, type, makeConnectPoint, viewId } = this.props;
        const paramNamePrefix = endpoint.paramName ? `${endpoint.paramName}:` : '';
        const key = `${paramNamePrefix}${endpoint.fieldName || endpoint.name || endpoint.index}:${viewId}`;

        let endpointKind = kind;
        if (type === 'param' || type === 'return') {
            endpointKind = `function-${kind}`;
        }

        return (
            <VariableEndpoint
                key={key}
                id={key}
                type={type}
                variable={endpoint}
                endpointKind={endpointKind}
                makeConnectPoint={makeConnectPoint}
                level={level}
            />
        );
    }

    render() {
        const { endpoints, type, removeTypeCallbackFunc } = this.props;
        return (
            <div className='transform-endpoint-tree'>
                {
                endpoints.map((endpoint) => {
                    const className = `transform-endpoint-container transform-${type}`;
                    return (
                        <div key={endpoint.name} className={className}>
                            <div className='transform-endpoint-details'>
                                {
                                    endpoint.type === 'struct' ?
                                    this.renderStruct(endpoint, 0) : this.renderEndpoint(endpoint, 'variable', 0)
                                }
                                {
                                    (type === 'source' || type === 'target') &&
                                    <span
                                        onClick={() => removeTypeCallbackFunc(endpoint)}
                                        className='fw-stack fw-lg btn btn-remove'
                                    >
                                        <i className='fw fw-delete fw-stack-1x' />
                                    </span>
                                }
                            </div>
                        </div>
                    );
                })
            }
            </div>
        );
    }
}
