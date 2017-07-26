import React from 'react';
import './variable-endpoint.css';

export default class VariableEndpoint extends React.Component {
    render() {
        const {variable, makeConnectPoint, type, level, id} = this.props;
        const connectPointClass = `${type}-connect-point`;

        return (
            <div id={id} style={{paddingLeft: level*5}} className='transform-endpoint variable'>
                <i className='transform-endpoint-icon fw fw-variable'/>
                <a className='variable-content'>
                    <span className='property-name'>
                        {variable.name}
                    </span>
                    <span className='property-name'>:</span>
                    <span className='property-type'>
                        {variable.type}
                    </span>
                </a>
                <span id={variable.id + '-button'} className={`fw-stack fw-lg btn ${connectPointClass}`}>
                    <i ref={icon => makeConnectPoint(icon, id, variable)} className='fw fw-circle-outline fw-stack-1x'/>
                </span>
                <span id={variable.id + '-button'} className='fw-stack fw-lg btn btn-remove'>
                    <i className='fw fw-delete fw-stack-1x'/>
                </span>
            </div>
        );
    }
}
