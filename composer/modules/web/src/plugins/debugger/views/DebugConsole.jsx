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
import _ from 'lodash';
import ConsoleDecorators from 'plugins/debugger/views/console-decorators';
import View from 'core/view/view';
import { VIEWS } from './../constants';
import './DebugConsole.css';

let messageCache = [];
/**
 * Debugger Console View
 */
class DebuggerConsole extends View {
    /**
     * Creates an instance of DebuggerConsole.
     * @memberof DebuggerConsole
     */
    constructor(props) {
        super(props);
        this.state = {
            messages: [],
        };
        this.debouncedSetState = _.debounce((nextState) => {
            this.setState({ messages: nextState });
        },
        400);

        props.LaunchManager.on('execution-started', () => {
            messageCache = [];
            this.setState({
                messages: [],
            });
        });
        props.LaunchManager.on('print-message', (message) => {
            messageCache.push(message);
            this.debouncedSetState(messageCache);
        });
    }

    componentDidMount() {
        const messages = this.props.LaunchManager.messages.filter( (message) => {
            return message.type !== 'TRACE';
        });
        this.setState({
            messages,
        });
        messageCache = messages;
        console.log(messages);
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
        const { height } = this.props;
        return (
            <div id="console" style={{ height }}>
                {this.state.messages.map((message) => {
                    return (<ConsoleDecorators
                        message={message}
                        command={this.props.debuggerPlugin.appContext.command}
                        key={message.id}
                    />);
                })}
            </div>
        );
    }
}

export default DebuggerConsole;
