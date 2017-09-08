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
import Tree from './tree.jsx';
import './function.css';
import ASTFactory from '../../../../../ast/ast-factory';

export default class Operator extends React.Component {
    render() {
        const {
            key, operator, opStmt, recordSourceElement, recordTargetElement, viewId,
            parentFunc, funcInv, onEndpointRemove
        } = this.props;

        operator.parameters.forEach((param, index) => {
            param.endpointKind = 'param';
        });

        operator.returnParams.forEach((returnsObj, index) => {
            returnsObj.endpointKind = 'return';
        });

        const onRemove = () => {

        };

        return (
            <div className='transform-expanded-func func'>
                <div className='function-header'>
                    <i className='fw fw-function fw-inverse' />
                    <span className='func-name'>{operator.name}</span>
                    <span onClick={onRemove} className='fw-stack fw-lg btn btn-remove-func'>
                        <i className='fw-delete fw-stack-1x fw-inverse' />
                    </span>
                </div>
                <div className='function-param-body'>
                    <div className='func-input'>
                        <Tree
                            type='param'
                            makeConnectPoint={recordTargetElement}
                            endpoints={operator.parameters}
                            viewId={viewId}
                            onEndpointRemove={onEndpointRemove}
                        />
                    </div>
                    <div className='func-output'>
                        <Tree
                            type='return'
                            makeConnectPoint={recordSourceElement}
                            endpoints={operator.returnParams}
                            viewId={viewId}
                            onEndpointRemove={onEndpointRemove}
                        />
                    </div>
                </div>
            </div>
        );
    }
}
