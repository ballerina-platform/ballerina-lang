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
import './operator.css';
import ASTFactory from '../../../../../ast/ast-factory';
import opClassMap from './op-class-map';

export default class Operator extends React.Component {
    render() {
        let targetPoint01 = [];
        let targetPoint02 = [];

        const {
            operator, opExp, recordSourceElement, recordTargetElement, viewId, isCollapsed,
            parentNode, statement, onEndpointRemove, onConnectPointMouseEnter, onOperatorRemove, onFolderClick,
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

        let opBody;

        const opCellComp = (
            <div className='operator-cell operator-name'>
                <i className={`fw fw-${opClassMap[opExp.operatorKind]}`}/>
            </div>
        );

        if(isCollapsed) {
            opBody = (
                <div className='folded-op-body' id={`${opExp.getID()}:${viewId}`}>
                    <div className='operator-col'>
                        { opCellComp }
                    </div>
                    <div className='operator-col'>
                        <div className='operator-cell'>
                            <span
                                onClick={() => this.props.onFolderClick(opExp.getID())}
                                className='fw-stack fw-lg btn folder'
                            >
                                <i className='fw-stack-1x fw-down' />
                            </span>
                        </div>
                    </div>
                </div>
            );
        } else {
            opBody = (
                <div>
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
                        { opCellComp }
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
                        <div className='operator-cell' >
                            <span
                                onClick={() => this.props.onFolderClick(opExp.getID())}
                                className='fw-stack fw-lg btn btn-remove-func'
                            >
                                <i className='fw-delete fw-stack-1x fw-up' />
                            </span>
                        </div>
                    </div>
                </div>
            );
        }

        return (
            <div className='operator-expanded-func func'>
                <div className='function-param-body'>
                    {opBody}
                </div>
            </div>
        );
    }
}
