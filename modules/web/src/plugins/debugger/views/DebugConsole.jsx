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
import _ from 'lodash';
import View from 'core/view/view';
import { VIEWS } from './../constants';
import './DebugConsole.css';

/**
 * Debugger Console View
 */
class DebuggerConsole extends View {
    /**
     * Creates an instance of DebuggerConsole.
     * @memberof DebuggerConsole
     */
    constructor() {
        super();
        this.state = {
            messages: [],
        };
    }
    /**
     * React.Component lifecycle hook
     *
     * @memberof DebuggerConsole
     */
    componentDidMount() {
        this.props.LaunchManager.on('execution-started', () => {
            this.setState({
                messages: [],
            });
        });
        this.props.LaunchManager.on('print-message', (message) => {
            this.setState({
                messages: this.state.messages.concat([message]),
            });
        });
    }
    /**
     * React.Component lifecycle hook
     *
     * @memberof DebuggerConsole
     */
    componentDidUnMount() {
        this.props.LaunchManager.off('execution-started');
        this.props.LaunchManager.off('print-message');
    }

    /**
     * @inheritdoc
     */
    getID() {
        return VIEWS.DEBUGGER_CONSOLE;
    }

    /**
     * @inheritdoc
     */
    render() {
        return (
            <div id="console">
                {this.state.messages.map((message, i) => {
                    const specialCharsEscapedStr = _.escape(message.message);
                    return (
                        <div className={message.type} key={i}><pre>{specialCharsEscapedStr}</pre></div>
                    );
                })}
            </div>
        );
    }
}

export default DebuggerConsole;
