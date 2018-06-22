/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ToolsPortal from './tools-portal';

class ToolsOverlay extends React.Component {
    constructor() {
        super();
        this.handleClickOutside = this.handleClickOutside.bind(this);
    }
    /**
     * Bind mouse down event to handle out side click events
     */
    componentDidMount() {
        document.addEventListener('mousedown', this.handleClickOutside);
    }
    /**
     * remove mouse down event bound  when mounting component
     */
    componentWillUnmount() {
        document.removeEventListener('mousedown', this.handleClickOutside);
    }

    handleClickOutside(event) {
        if (this.wrapperRef && !this.wrapperRef.contains(event.target)) {
            this.props.hide();
        }
    }

    render() {
        return (
            <ToolsPortal>
                <div
                    style={{
                        position: 'absolute',
                        zIndex: 10,
                        left: this.props.bBox.x,
                        top: this.props.bBox.y,
                    }}
                    ref={(wrapperRef) => { this.wrapperRef = wrapperRef; }}
                >
                    {this.props.children}
                </div>
            </ToolsPortal>
        );
    }
}

export default ToolsOverlay;
