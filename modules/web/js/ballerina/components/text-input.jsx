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
import ReactDOM from 'react-dom';
import {getCanvasOverlay} from '../configs/app-context';
import './text-input.css'

class TextBox extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            value: this.props.initialValue,
            display: true
        };
        this.onChange = this.onChange.bind(this);
        this.onBlur = this.onBlur.bind(this);
    }

    componentWillReceiveProps(nextProps) {
        this.setState({
            value: nextProps.initialValue,
            display: nextProps.display
        });
    }

    onChange(e) {
        this.setState({
            value: this.textInput.value
        });
        this.props.onChange(e.target.value);
    }

    onBlur(e) {
        this.setState({
            display: false
        });
    }

    render() {
        const inputStyle = {
            position: 'absolute',
            top: this.props.bBox.y,
            left: this.props.bBox.x,
            width: this.props.bBox.w,
            height: this.props.bBox.h
        };

        if (this.state.display === false) {
            inputStyle.display = 'none';
        }

        return (
            <input
                className='text-input'
                ref={i => {
                    this.textInput = i
                }}
                style={inputStyle}
                onChange={this.onChange}
                onBlur={this.onBlur}
                value={this.state.value}
            />
        )
    }
}

export function renderTextBox(bBox, onChange, initialValue) {
    ReactDOM.render(
        <TextBox bBox={bBox} onChange={onChange} initialValue={initialValue} display={true}/>,
        getCanvasOverlay());
}
