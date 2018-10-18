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
import PropTypes from 'prop-types';
import Tree from './tree';
import './function.css';

export default class NestedTransformer extends React.Component {
    render() {
        const {
            transformerInvocation, statement, recordSourceElement, recordTargetElement, viewId,
            parentNode, conExp, onEndpointRemove, onConnectPointMouseEnter, isCollapsed,
            foldedEndpoints, foldEndpoint, onFunctionRemove,
        } = this.props;

        transformerInvocation.parameters.forEach((param) => {
            param.endpointKind = 'param';
        });

        transformerInvocation.returnParams.forEach((returnsObj) => {
            returnsObj.endpointKind = 'return';
        });

        const paramEndpoints = [...transformerInvocation.parameters];

        if (transformerInvocation.receiver) {
            paramEndpoints.push(transformerInvocation.receiver);
            transformerInvocation.receiver.endpointKind = 'receiver';
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
                        endpoints={transformerInvocation.returnParams}
                        viewId={viewId}
                        onEndpointRemove={onEndpointRemove}
                        onConnectPointMouseEnter={onConnectPointMouseEnter}
                        foldedEndpoints={foldedEndpoints}
                        foldEndpoint={foldEndpoint}
                    />
                </div>
            </div>
        );

        const funcInvID = conExp.getID();
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
                    <i className={'fw fw-type-converter fw-inverse nested-transformer-header'} />
                    <span className='func-name'>{conExp.getTransformerInvocation().getName().getValue()}</span>
                    <span
                        onClick={(e) => {
                            e.stopPropagation();
                            onFunctionRemove(conExp, parentNode, statement);
                        }}
                        className='fw-stack fw-lg btn btn-remove-func'
                        title='Delete function'
                    >
                        <i className='fw-delete fw-stack-1x fw-inverse' />
                    </span>
                </div>
                { !isCollapsed && functionBody }
            </div>
        );
    }
}

NestedTransformer.propTypes = {
    transformerInvocation: PropTypes.isRequired,
    statement: PropTypes.isRequired,
    recordSourceElement: PropTypes.isRequired,
    recordTargetElement: PropTypes.isRequired,
    viewId: PropTypes.isRequired,
    parentNode: PropTypes.isRequired,
    conExp: PropTypes.isRequired,
    onEndpointRemove: PropTypes.isRequired,
    onConnectPointMouseEnter: PropTypes.isRequired,
    isCollapsed: PropTypes.isRequired,
    foldedEndpoints: PropTypes.isRequired,
    foldEndpoint: PropTypes.isRequired,
    onFunctionRemove: PropTypes.isRequired,
    onHeaderClick: PropTypes.isRequired,
};

NestedTransformer.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
};
