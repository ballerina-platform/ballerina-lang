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
        let targetPoint01 = [];
        let targetPoint02 = [];

        const {
            operator, opExp, recordSourceElement, recordTargetElement, viewId,
            parentNode, statement, onEndpointRemove, onConnectPointMouseEnter, onOperatorRemove
        } = this.props;

        operator.parameters.forEach((param) => {
            param.endpointKind = 'param';
        });

        operator.returnParams.forEach((returnsObj) => {
            returnsObj.endpointKind = 'return';
        });

        if (operator.parameters.length === 1) {
            targetPoint02 = operator.parameters.slice(0, 1);
        } else {
            targetPoint01 = operator.parameters.slice(0, 1);
            targetPoint02 = operator.parameters.slice(1, 2);
        }

        return (
            <div className='operator-expanded-func func'>
                <div className='function-param-body'>
                    <div className='operator-col'>
                        <div className='operator-cell'>
                            <Tree
                                type='param'
                                makeConnectPoint={recordTargetElement}
                                endpoints={targetPoint01}
                                viewId={viewId}
                                onEndpointRemove={onEndpointRemove}
                                onConnectPointMouseEnter={onConnectPointMouseEnter}
                            />
                        </div>
                        <div className='operator-cell'><span className='operator-name'>{opExp.getOperator()}</span></div>
                        <div className='operator-cell'>
                            <Tree
                                type='param'
                                makeConnectPoint={recordTargetElement}
                                endpoints={targetPoint02}
                                viewId={viewId}
                                onEndpointRemove={onEndpointRemove}
                                onConnectPointMouseEnter={onConnectPointMouseEnter}
                            />
                        </div>
                    </div>
                    <div className='operator-col'>
                        <div className='operator-cell'>
                            <span
                                onClick={(e) => {
                                    e.stopPropagation();
                                    onOperatorRemove(opExp, parentNode, statement);
                                }}
                                className='fw-stack fw-lg btn operator-remove'
                            >
                                <i className='fw-delete fw-stack-1x' />
                            </span>
                        </div>
                        <div className='operator-cell'>
                            <Tree
                                type='return'
                                makeConnectPoint={recordSourceElement}
                                endpoints={operator.returnParams}
                                viewId={viewId}
                                onEndpointRemove={onEndpointRemove}
                                onConnectPointMouseEnter={onConnectPointMouseEnter}
                            />
                        </div>
                        <div className='operator-cell' />
                    </div>
                </div>
            </div>
        );
    }
}
