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
import View from 'core/view/view';
import { VIEWS } from './../constants';
import './DebuggerPanel.css';
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
        this.state = {
            active: false,
            navigation: false,
            isDebugging: false,
            message: { frames: [] },
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
            });
        });
        this.props.DebugManager.on('debugging-started', () => {
            this.setState({
                isDebugging: true,
            });
        });
        this.props.DebugManager.on('debug-hit', (message) => {
            this.setState({
                navigation: true,
                message: processFrames(message),
            });
        });
        this.props.DebugManager.on('resume-execution', () => {
            this.setState({
                navigation: false,
                message: { frames: [] },
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
        this.props.commandProxy.dispatch('run');
    }
    startDebug() {
        this.props.commandProxy.dispatch('run', true);
    }

    stopApplication() {
        this.props.commandProxy.dispatch('stop');
    }

    /**
     * @inheritdoc
     */
    render() {
        if (this.state.isDebugging) {
            return (
                <div>
                    <div className="btn-group col-xs-12">
                        <Toolbar navigation={this.state.navigation} />
                    </div>
                    <div>
                        <Frames message={this.state.message} />
                    </div>

                </div>
            );
        }
        if (this.state.active) {
            return (
                <div>
                    <div className="btn-group col-xs-12">
                        <div
                            type="button"
                            id="stop_application"
                            className="btn text-left btn-debug-activate col-xs-12"
                            title="Stop Application"
                            onClick={this.stopApplication}
                        >
                            <span className="launch-label">Stop</span>
                        </div>
                    </div>
                </div>
            );
        } else {
            return (
                <div>
                    <div className="btn-group col-xs-12">
                        <div
                            type="button"
                            id="run_application"
                            className="btn text-left btn-debug-activate col-xs-12"
                            title="Start Application"
                            onClick={this.startApplication}
                        >
                            <span className="launch-label">Run</span>
                            <div type="button" className="btn pull-right btn-config" title="Config">
                                <i className="fw fw-configarations" />
                            </div>
                        </div>
                        <div
                            type="button"
                            id="start_debug"
                            className="btn text-left btn-debug-activate col-xs-12"
                            title="Start Application"
                            onClick={this.startDebug}
                        >
                            <span className="launch-label">Run with Debuging</span>
                            <div type="button" className="btn pull-right btn-config" title="Config">
                                <i className="fw fw-configarations" />
                            </div>
                        </div>
                    </div>
                </div>
            );
        }

    }
}

export default DebuggerPanel;
