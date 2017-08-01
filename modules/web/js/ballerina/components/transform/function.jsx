import React from 'react';
import Tree from './tree.jsx';
import './function.css';

export default class FunctionInv extends React.Component {
    render() {
        const {func} = this.props;

        return (
            <div className='func'>
                <div className='function-header'>
                    <i className='fw fw-function fw-inverse'/>
                    <span>{func.getName()}</span>
                    <span className='fw-stack fw-lg btn btn-remove'>
                        <i className='fw-delete fw-stack-1x fw-inverse'/>
                    </span>
                </div>
                <div className='func-output'>
                </div>
                <div className='func-input'>
                </div>
            </div>
        );
    }
}
