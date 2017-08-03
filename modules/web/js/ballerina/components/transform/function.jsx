import React from 'react';
import Tree from './tree.jsx';
import './function.css';

export default class FunctionInv extends React.Component {
    render() {
        const {func, enclosingAssignmentStatement, recordSourceElement, recordTargetElement} = this.props;
        const params = func.getParameters().map(paramObj => {
            const param = paramObj.innerType || paramObj;
            const paramDetails = {
                name: paramObj.name,
                type: param.type,
                typeName: param.typeName,
                properties: param.properties,
                endpointKind: 'param',
                paramName: `${func.getFullPackageName()}:${func.getName()}:${param.name}`,
                enclosingAssignmentStatement,
            };

            return paramDetails;
        });

        const returns = func.getReturnParams().map(returnsObj => {
            return {
                name: returnsObj.name,
                type: returnsObj.typeName || returnsObj.type,
                enclosingAssignmentStatement,
            }
        });

        return (
            <div className='transform-expanded-func func'>
                <div className='function-header'>
                    <i className='fw fw-function fw-inverse'/>
                    <span>{func.getName()}</span>
                    <span className='fw-stack fw-lg btn btn-remove'>
                        <i className='fw-delete fw-stack-1x fw-inverse'/>
                    </span>
                </div>
                <div className='function-param-body'>
                    <div className='func-input'>
                        <Tree
                            type='param'
                            makeConnectPoint={recordTargetElement}
                            endpoints={params}
                            viewId=''
                        />
                    </div>
                    <div className='func-output'>
                        <Tree
                            type='return'
                            makeConnectPoint={recordSourceElement}
                            endpoints={returns}
                            viewId=''
                        />
                    </div>
                </div>
            </div>
        );
    }
}
