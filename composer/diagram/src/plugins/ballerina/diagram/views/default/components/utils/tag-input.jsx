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
import './properties-form.css';

/**
 * React component for a service definition.
 *
 * @class ServiceDefinition
 * @extends {React.Component}
 */
class TagInput extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            items: [],
            input: '',
        };
        this.handleInputChange = this.handleInputChange.bind(this);
        this.handleInputKeyDown = this.handleInputKeyDown.bind(this);
        this.handleRemoveItem = this.handleRemoveItem.bind(this);
    }

    render() {
        return (
            <label >
                <ul id='ulContainer'>
                    {this.props.taggedElements && this.props.taggedElements.map((item, i) =>
                        (<li
                            key={i}
                            className='tagItems'
                            onClick={() => { this.handleRemoveItem(i); }}
                        >
                            {item}
                            <span className='deleteSpan'><i className='fw fw-cancel iconDelete' /></span>
                        </li>))
                    }
                    <input
                        className='tagInput'
                        placeholder={this.props.placeholder}
                        value={this.state.input}
                        onChange={this.handleInputChange}
                        onKeyDown={this.handleInputKeyDown}
                    />
                </ul>
            </label>
        );
    }

    handleInputChange(evt) {
        this.setState({ input: evt.target.value });
    }

    handleInputKeyDown(evt) {
        this.props.onTagsAdded(evt);
        if (evt.keyCode === 13 || evt.keyCode === 188) {
            this.setState(state => ({
                input: '',
            }));
        }
    }

    handleRemoveItem(index) {
        this.props.removeTagsAdded(this.props.taggedElements, index);
    }
}
export default TagInput;
