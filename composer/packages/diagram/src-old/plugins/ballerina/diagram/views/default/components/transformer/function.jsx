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
import PropTypes from 'prop-types';
import Tree from './tree';
import './function.css';

export default class FunctionInv extends React.Component {
    render() {
        const {
            func, statement, recordSourceElement, recordTargetElement, viewId,
            parentNode, funcInv, onEndpointRemove, onConnectPointMouseEnter, isCollapsed,
            foldedEndpoints, foldEndpoint, onFunctionRemove,
        } = this.props;

        func.parameters.forEach((param) => {
            param.endpointKind = 'param';
        });

        func.returnParams.forEach((returnsObj) => {
            returnsObj.endpointKind = 'return';
        });

        const paramEndpoints = [...func.parameters];

        if (func.receiver) {
            paramEndpoints.push(func.receiver);
            func.receiver.endpointKind = 'receiver';
        }

        const functionBody = (
            <div className='function-param-body'>
                <div className='func-input'>
                    <Tree
                        type='param'
                        makeConnectPoint={recordTargetElement}
                        endpoints={paramEndpoints}
                        viewId={viewId}
                        onEndpointRemove={onEndpointRemove}
                        onConnectPointMouseEnter={onConnectPointMouseEnter}
                        foldedEndpoints={foldedEndpoints}
                        foldEndpoint={foldEndpoint}
                    />
                </div>
                <div className='func-output'>
                    <Tree
                        type='return'
                        makeConnectPoint={recordSourceElement}
                        endpoints={func.returnParams}
                        viewId={viewId}
                        onEndpointRemove={onEndpointRemove}
                        onConnectPointMouseEnter={onConnectPointMouseEnter}
                        foldedEndpoints={foldedEndpoints}
                        foldEndpoint={foldEndpoint}
                    />
                </div>
            </div>
        );

        const funcInvID = funcInv.getID();
        const foldIndicator = isCollapsed ? 'fw-down' : 'fw-up';
        return (
            <div className='transform-expanded-func'>
                <div
                    id={`${funcInvID}:${viewId}`}
                    className='function-header'
                    title={isCollapsed ? 'Expand' : 'Collapse'}
                >
                    <i
                        className={`fw ${foldIndicator} fold-indicator`}
                        onClick={() => this.props.onHeaderClick(funcInvID)}
                    />
                    <i className={`fw fw-${func.type === 'iterable'
                        ? 'iterable-operations' : 'function'} fw-inverse`}
                    />
                    <span className='func-name'>{funcInv.getFunctionName()}</span>
                    <span
                        onClick={(e) => {
                            e.stopPropagation();
                            onFunctionRemove(funcInv, parentNode, statement);
                        }}
                        className='fw-stack fw-lg btn btn-remove-func'
                        title='Delete function'
                    >
                        <i className='fw-delete fw-stack-1x fw-inverse' />
                    </span>
                    { funcInv.iterableOperation
                        && funcInv.getArgumentExpressions().length > 0 &&
                        <span
                            onClick={(e) => {
                                const { goToSource } = this.context;
                                goToSource(funcInv.getArgumentExpressions()[0].functionNode);
                            }}
                            className='fw-stack fw-lg btn btn-remove-func'
                            title='Jump to Source'
                        >
                            <i className='fw-edit fw-stack-1x fw-inverse' />
                        </span>
                    }
                </div>
                { !isCollapsed && functionBody }
            </div>
        );
    }
}

FunctionInv.contextTypes = {
    goToSource: PropTypes.func.isRequired,
};
