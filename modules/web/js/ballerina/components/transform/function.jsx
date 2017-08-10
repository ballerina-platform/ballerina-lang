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
import BallerinaASTFactory from '../../ast/ballerina-ast-factory';

export default class FunctionInv extends React.Component {
    render() {
        const {
            func, enclosingAssignmentStatement, recordSourceElement, recordTargetElement, viewId,
            parentFunc, funcInv,
        } = this.props;
        const params = func.getParameters().map((paramObj) => {
            const param = paramObj.innerType || paramObj;
            const paramDetails = {
                name: paramObj.name,
                type: param.type,
                typeName: param.typeName,
                properties: param.properties,
                endpointKind: 'param',
                paramName: `${funcInv.getID()}:${func.getName()}`,
                enclosingAssignmentStatement,
                parentFunc,
                funcInv,
            };

            return paramDetails;
        });

        const returns = func.getReturnParams().map((returnsObj, index) => {
            return {
                name: returnsObj.name,
                id: returnsObj.name || index,
                type: returnsObj.typeName || returnsObj.type,
                paramName: `${funcInv.getID()}:${func.getName()}`,
                enclosingAssignmentStatement,
                parentFunc,
                funcInv,
            };
        });

        const onRemove = () => {
            // add children function invocations to the transform model so that only this function invocation
            // is removed from the view
            funcInv.getChildren().forEach(childFuncInv => {
                if(!BallerinaASTFactory.isFunctionInvocationExpression(childFuncInv)){
                    return;
                }

                const assignmentStmt = BallerinaASTFactory.createAssignmentStatement();
                const varRefList = BallerinaASTFactory.createVariableReferenceList();
                assignmentStmt.addChild(varRefList, 0);
                assignmentStmt.addChild(childFuncInv, 1);
                const transformModel = enclosingAssignmentStatement.getParent();
                transformModel.addChild(assignmentStmt,
                    transformModel.getIndexOfChild(enclosingAssignmentStatement), true);
            });

            if(!parentFunc) {
                enclosingAssignmentStatement.getParent().removeChild(enclosingAssignmentStatement);
            } else {
                parentFunc.removeChild(funcInv);
            }
        }

        return (
            <div className='transform-expanded-func func'>
                <div className='function-header'>
                    <i className='fw fw-function fw-inverse' />
                    <span className='func-name'>{func.getName()}</span>
                    <span onClick={onRemove} className='fw-stack fw-lg btn btn-remove-func'>
                        <i className='fw-delete fw-stack-1x fw-inverse' />
                    </span>
                </div>
                <div className='function-param-body'>
                    <div className='func-input'>
                        <Tree
                            type='param'
                            makeConnectPoint={recordTargetElement}
                            endpoints={params}
                            viewId={viewId}
                        />
                    </div>
                    <div className='func-output'>
                        <Tree
                            type='return'
                            makeConnectPoint={recordSourceElement}
                            endpoints={returns}
                            viewId={viewId}
                        />
                    </div>
                </div>
            </div>
        );
    }
}
