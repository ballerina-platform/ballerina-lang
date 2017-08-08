import React from 'react';
import './variable-endpoint.css';

export default class VariableEndpoint extends React.Component {
    render() {
        const {variable, makeConnectPoint, type, endpointKind, level, id, removeTypeCallbackFunc} = this.props;
        const connectPointClass = `${type}-connect-point`;

        let iconType = 'fw-variable';

        if(variable.type === 'struct') {
            iconType = 'fw-struct'
        }

        return (
            <div className='transform-endpoint variable'>
                <span style={{paddingLeft: level>0 ? ((level-1)*20) : 0}}>
                    {(level>0) && <span className='tree-view-icon'>└</span>}
                    <span className='variable-icon btn'>
                        <i className={`transform-endpoint-icon fw ${iconType}`}/>
                    </span>
                    <span className='variable-content'>
                        <span className='property-name'>
                            {variable.name}
                        </span>
                        <span className='property-name'>:</span>
                        <span className='property-type'>
                            {variable.typeName || variable.type}
                        </span>
                    </span>
                </span>
                <span id={variable.id + '-button'} className={`fw-stack fw-lg btn ${connectPointClass}`}>
                    <i id={id} ref={icon => makeConnectPoint(icon, id, variable)} className='fw fw-circle-outline fw-stack-1x'/>
                </span>
                {
                    endpointKind !== 'property' &&
                    <span id={variable.id + '-button'} onClick={event => removeTypeCallbackFunc(variable)} className='fw-stack fw-lg btn btn-remove'>
                        <i className='fw fw-delete fw-stack-1x'/>
                    </span>
                }
            </div>
        );
    }
}
