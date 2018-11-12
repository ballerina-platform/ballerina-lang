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
import Tree from './tree';
import './function.css';
import './operator.css';
import opClassMap from './op-class-map';

export default class Operator extends React.Component {
    render() {
        const targetPoints = [];

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

        let opBody;

        const opCellComp = (
            <div className='operator-cell operator-name'>
                <i className={`fw fw-${opClassMap[opExp.getOperatorKind()]}`} />
            </div>
        );

        if (operator.parameters.length === 1) {
            targetPoints[0] = [];
            targetPoints[1] = [operator.parameters[0]];
        } else if (operator.parameters.length === 2) {
            targetPoints[0] = [operator.parameters[0]];
            targetPoints[1] = [operator.parameters[1]];
        } else if (operator.parameters.length === 3) {
            targetPoints[0] = [operator.parameters[0]];
            targetPoints[1] = [operator.parameters[1]];
            targetPoints[2] = [operator.parameters[2]];
        }

        if (isCollapsed) {
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
                                title='Expand'
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
                                endpoints={targetPoints[0]}
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
                                endpoints={targetPoints[1]}
                                viewId={viewId}
                                onEndpointRemove={onEndpointRemove}
                                onConnectPointMouseEnter={onConnectPointMouseEnter}
                            />
                        </div>
                        {(() => {
                            if (targetPoints[2]) {
                                return (<div className='operator-cell'>
                                    <Tree
                                        type='param'
                                        makeConnectPoint={recordTargetElement}
                                        endpoints={targetPoints[2]}
                                        viewId={viewId}
                                        onEndpointRemove={onEndpointRemove}
                                        onConnectPointMouseEnter={onConnectPointMouseEnter}
                                    />
                                </div>);
                            }
                            return '';
                        })()}
                    </div>
                    <div className='operator-col'>
                        <div className='operator-cell'>
                            <span
                                onClick={(e) => {
                                    e.stopPropagation();
                                    onOperatorRemove(opExp, parentNode, statement);
                                }}
                                className='fw-stack fw-lg btn operator-remove'
                                title='Delete operator'
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
                        {(() => {
                            if (targetPoints[2]) {
                                return (<div className='operator-cell' />);
                            }
                            return '';
                        })()}
                        <div className='operator-cell' >
                            <span
                                onClick={() => this.props.onFolderClick(opExp.getID())}
                                className='fw-stack fw-lg btn btn-remove-func'
                                title='Collapse'
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
