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
import { Button } from 'semantic-ui-react';
import View from 'core/view/view';
import { VIEWS, COMMANDS } from './../constants';
import './DebuggerPanel.scss';
import Toolbar from '../views/Toolbar';
import Frames from '../views/Frames';
import { processFrames } from '../views/Frames/utils';


/**
 * Debugger
 */
class DebuggerPanel extends View {
    /**
     * Creates an instance of DebuggerPanel.
     * @memberof DebuggerPanel
     */
    constructor() {
        super();
        this.startApplication = this.startApplication.bind(this);
        this.startDebug = this.startDebug.bind(this);
        this.stopApplication = this.stopApplication.bind(this);
        this.showRemoteDebugDialog = this.showRemoteDebugDialog.bind(this);
        this.reConnect = this.reConnect.bind(this);
        this.cancelReconnect = this.cancelReconnect.bind(this);
        this.state = {
            active: false,
            navigation: false,
            isDebugging: false,
            message: { frames: [] },
            connecting: false,
            showRetry: false,
        };
    }

    componentDidMount() {
        this.props.LaunchManager.on('execution-started', () => {
            this.setState({
                active: true,
            });
        });
        this.props.LaunchManager.on('execution-ended', () => {
            this.setState({
                active: false,
                isDebugging: false,
                connecting: false,
            });
        });
        this.props.DebugManager.on('debugging-started', () => {
            this.setState({
                isDebugging: true,
                connecting: false,
                showRetry: false,
            });
        });
        this.props.DebugManager.on('debug-hit', (message) => {
            this.setState({
                navigation: true,
                isDebugging: true,
                message: processFrames(message),
            });
        });

        this.props.DebugManager.on('execution-ended', () => {
            this.setState({
                active: false,
                isDebugging: false,
            });
        });

        this.props.DebugManager.on('resume-execution', () => {
            this.setState({
                navigation: false,
                isDebugging: false,
                message: { frames: [] },
            });
        });

        this.props.DebugManager.on('connecting', () => {
            this.setState({
                connecting: true,
                showRetry: false,
            });
        });
        this.props.DebugManager.on('session-error', () => {
            this.setState({
                showRetry: true,
            });
        });
    }

    /**
     * @inheritdoc
     */
    getID() {
        return VIEWS.DEBUGGER_PANEL;
    }

    startApplication() {
        this.props.commandProxy.dispatch(COMMANDS.RUN);
    }
    startDebug() {
        this.props.commandProxy.dispatch(COMMANDS.RUN_WITH_DEBUG);
    }

    stopApplication() {
        this.props.commandProxy.dispatch(COMMANDS.STOP);
    }
    showRemoteDebugDialog() {
        this.props.commandProxy.dispatch(COMMANDS.SHOW_REMOTE_DEBUG_DIALOG);
    }
    reConnect() {
        this.props.DebugManager.reConnect();
    }
    cancelReconnect() {
        this.setState({
            showRetry: false,
            connecting: false,
        });
    }

    /**
     * @inheritdoc
     */
    render() {
        if (this.state.showRetry) {
            return (
                <div>
                    <div className='debug-panel-header'>
                        <span className='tool-group-header-title'>Could not connect to debugger</span>
                    </div>
                    <div className='debug-buttons'>
                        <Button
                            onClick={this.reConnect}
                            title='Reconnect'
                        >
                            <i className='fw fw-refresh' /> Retry
                        </Button>
                        <Button
                            onClick={this.cancelReconnect}
                            title='Reconnect'
                        >
                            <i className='fw fw-cancel' /> Cancel
                        </Button>
                    </div>
                </div>
            );
        }
        if (this.state.connecting) {
            return (
                <div className='debug-panel-header'>
                    <span className='tool-group-header-title'>Waiting for debugger to connect ...</span>
                </div>
            );
        }
        if (this.state.isDebugging) {
            return (
                <div>
                    <div className='btn-group col-xs-12'>
                        <Toolbar navigation={this.state.navigation} dispatch={this.props.commandProxy.dispatch} />
                    </div>
                    <div>
                        <Frames message={this.state.message} />
                    </div>

                </div>
            );
        }
        if (this.state.active) {
            return (
                <div className='debug-buttons'>
                    <Button
                        title='Stop Application'
                        onClick={this.stopApplication}
                        fluid
                        className='debug-button'
                    >
                        Stop
                    </Button>
                </div>
            );
        } else {
            return (
                <div className='debug-buttons'>
                    <Button
                        title='Run (Ctrl+Shift+R)'
                        onClick={this.startApplication}
                        fluid
                        className='debug-button'
                    >
                        Run
                    </Button>
                    <Button
                        title='Run With Debug (Ctrl+Shift+D)'
                        onClick={this.startDebug}
                        fluid
                        className='debug-button'
                    >
                        Debug
                    </Button>
                    <Button
                        title='Remote Debug'
                        onClick={this.showRemoteDebugDialog}
                        fluid
                        className='debug-button'
                    >
                        Remote Debug
                    </Button>
                </div>
            );
        }
    }
}

export default DebuggerPanel;
