import React from 'react';
import Tree from './tree.jsx';
import './function.css';

export default class FunctionInv extends React.Component {
    render() {
        const {
            func, enclosingAssignmentStatement, recordSourceElement, recordTargetElement, viewId,
            parentFunc, funcInv,
        } = this.props;
        const params = func.getParameters().map(paramObj => {
            const param = paramObj.innerType || paramObj;
            const paramDetails = {
                name: paramObj.name,
                type: param.type,
                typeName: param.typeName,
                properties: param.properties,
                endpointKind: 'param',
                paramName: `${func.getFullPackageName()}:${func.getName()}`,
                enclosingAssignmentStatement,
                parentFunc,
                funcInv,
            };

            return paramDetails;
        });

        const returns = func.getReturnParams().map((returnsObj, index) => {
            return {
                name: returnsObj.name||index,
                type: returnsObj.typeName || returnsObj.type,
                paramName: `${func.getFullPackageName()}:${func.getName()}`,
                enclosingAssignmentStatement,
                parentFunc,
            }
        });

        return (
            <div className='transform-expanded-func func'>
                <div className='function-header'>
                    <i className='fw fw-function fw-inverse'/>
                    <span className='func-name'>{func.getName()}</span>
                    <span className='fw-stack fw-lg btn btn-remove-func'>
                        <i className='fw-delete fw-stack-1x fw-inverse'/>
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
