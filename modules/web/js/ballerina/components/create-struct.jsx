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
import PropTypes from 'prop-types';
/**
 *
 *
 * @class CreateStruct
 * @extends {React.Component}
 */
class CreateStruct extends React.Component {
    /**
     * Creates an instance of CreateStruct.
     * @param {Object} props
     *
     * @memberof CreateStruct
     */
    constructor(props) {
        super(props);
        this.state = {
            display: true,
        };
    }
    /**
     * Handle create new struct button click
     */
    handleSubmit() {
        this.props.onSubmit(this.typeInput.value, this.identifierInput.value, this.valueInput.value);
        this.identifierInput.value = '';
        this.valueInput.value = '';
    }
    /**
     * @inheritdoc
     */
    render() {
        const style = {
            position: 'absolute',
            top: this.props.bBox.y,
            left: this.props.bBox.x,
            width: this.props.bBox.w,
            height: this.props.bBox.h,
        };

        if (this.state.display === false) {
            style.display = 'none';
        }
        const { types = [] } = this.props;
        const renderingContext = this.props.renderingContext;
        return (
            <div style={style} className="struct-content-operations-wrapper">
                <div className="type-drop-wrapper struct-view">
                    <select
                        tabIndex="-1"
                        style={{ width: 188, height: 25 }}
                        ref={(input) => { this.typeInput = input; }}>
                        {
                            types.map(type => <option key={type} value={type}>{type}</option>)
                        }
                    </select>
                </div>
                <input
                    type="text"
                    className="struct-identifier-text-input"
                    placeholder="Identifier"
                    ref={(input) => { this.identifierInput = input; }}
                />
                <input
                    type="text"
                    className="struct-default-value-text-input"
                    placeholder="Default Value"
                    ref={(input) => { this.valueInput = input; }} />
                <div className="add-struct-variable-button pull-left" onClick={event => this.handleSubmit(event)}>
                    <span className="fw-stack fw-lg">
                        <i className="fw fw-square fw-stack-2x" />
                        <i className="fw fw-check fw-stack-1x fw-inverse add-struct-variable-button-square" />
                    </span>
                </div>
            </div>
        );
    }
}

export default CreateStruct;
