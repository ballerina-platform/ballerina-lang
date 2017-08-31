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
import alerts from 'alerts';
import LaunchManager from './../../launcher/launch-manager';
import DebugToolbarView from './debug-toolbar';
import DebugManager from './../../debugger/debug-manager';
import ToolSetView from './tool-set';
import ToolMenuItem from './tool-menu-item-view';
import ToolMenu from './tool-menu-view';
import ToolItemView from './tool-item-view';
/**
 * Tool Component which render a tool in toolbar.
 *
 * @class Tool
 * @extends {React.Component}
 */
class LaunchDebuggerView extends React.Component {
    constructor() {
        super();
        this.app = undefined;
        this.state = {
            debugChildVisible: 'none',
            startChildVisible: 'none',
            started: false,
            debugStarted: false,
            navigation: false,
        };
        this.onStartToolClick = this.onStartToolClick.bind(this);
        this.onDebugToolClick = this.onDebugToolClick.bind(this);
        this.completed = this.completed.bind(this);
        this.connectionStarted = this.connectionStarted.bind(this);
        this.connectionEnded = this.connectionEnded.bind(this);
        this.enableNavigation = this.enableNavigation.bind(this);
        this.disableNavigation = this.disableNavigation.bind(this);
        this.onToolClickHandler = this.onToolClickHandler.bind(this);
    }

    /**
     * hook for componentDidMount
     */
    componentDidMount() {
        this.startListner = LaunchManager.on('execution-started', () => {
            this.setState({ started: true });
        });
        this.endListner = LaunchManager.on('execution-ended', () => {
            this.setState({ started: false });
        });
        this.debugStartListner = DebugManager.on('session-started', () => {
            this.setState({ debugStarted: true });
            this.connectionStarted();
        });
        this.debugEndListner = DebugManager.on('session-ended', () => { this.completed(); });
        this.debugHitListner = DebugManager.on('debug-hit', () => { this.enableNavigation(); });
        this.debugResumeExecListner = DebugManager.on('resume-execution', () => { this.disableNavigation(); });
    }

    /**
     * hook for componentWillUnmount
     */
    componentWillUnmount() {
        LaunchManager.off('execution-started', this.startListner, this);
        LaunchManager.off('execution-ended', this.endListner, this);
        DebugManager.off('session-started', this.debugStartListner, this);
        DebugManager.off('session-ended', this.debugEndListner, this);
        DebugManager.off('debug-hit', this.debugHitListner, this);
        DebugManager.off('resume-execution', this.debugResumeExecListner, this);
    }

    /**
     * Click function of the tool
     * @param app
     * @param tool
     */
    onToolClickHandler(id) {
        switch (id) {
            case 'start-tool':
                this.onStartToolClick();
                break;
            case 'debug-tool':
                this.onDebugToolClick();
                break;
            case 'startApplication':
                this.startApplication();
                break;
            case 'startService':
                this.startService();
                break;
            case 'debugApplication':
                this.debugApplication();
                break;
            case 'debugService':
                this.debugService();
                break;
            case 'stop':
                LaunchManager.stopProgram();
                break;
        }
    }

    /**
     * Click function of start tool
     */
    onStartToolClick() {
        if (this.state.started === false) {
            this.resetMenu();
            let state;
            if (this.state.startChildVisible === 'none') {
                state = 'block';
            } else {
                state = 'none';
            }
            this.setState({ startChildVisible: state });
        }
    }

    /**
     * Click function of debug tool
     */
    onDebugToolClick() {
        this.resetMenu();
        let state;
        if (this.state.debugChildVisible === 'none') {
            state = 'block';
        } else {
            state = 'none';
        }
        this.setState({ debugChildVisible: state });
    }

    /**
     * Reset the drop down content
     */
    resetMenu() {
        const dropdowns = document.getElementsByClassName('dropdown-content');
        let i;
        for (i = 0; i < dropdowns.length; i++) {
            dropdowns[i].style.display = 'none';
        }
    }

    /**
     * Check whether a file is ready to run
     * @param tab
     * @returns {boolean}
     */
    isReadyToRun(tab) {
        if (typeof tab.getFile !== 'function') {
            return false;
        }
        const file = tab.getFile();
        if (file.isDirty()) {
            return false;
        }

        return true;
    }

    /**
     * Start application
     */
    startApplication() {
        const activeTab = this.app.tabController.getActiveTab();
        if (this.isReadyToRun(activeTab)) {
            const file = activeTab.getFile();
            LaunchManager.runApplication(file);
        } else {
            alerts.error('Save file before running application');
        }
    }

    /**
     * Start service
     */
    startService() {
        const activeTab = this.app.tabController.getActiveTab();
        if (this.isReadyToRun(activeTab)) {
            const file = activeTab.getFile();
            LaunchManager.runService(file);
        } else {
            alerts.error('Save file before running service');
        }
    }

    /**
     * On debug completed
     */
    completed() {
        this.setState({
            debugStarted: false,
            navigation: true,
        });
        this.render();
        this.connectionEnded();
    }

    /**
     * Enable navigation of the debugger toolbar
     */
    enableNavigation() {
        this.setState({
            navigation: true,
        });
        this.render();
    }

    /**
     * Disable navigation of the debugger toolbar
     */
    disableNavigation() {
        this.setState({
            navigation: false,
        });
        this.render();
    }

    /**
     * Debug application
     */
    debugApplication() {
        const activeTab = this.app.tabController.getActiveTab();
        if (this.isReadyToRun(activeTab)) {
            const file = activeTab.getFile();
            LaunchManager.debugApplication(file);
        } else {
            alerts.error('Save file before start debugging application');
        }
    }

    /**
     * Debug service
     */
    debugService() {
        const activeTab = this.app.tabController.getActiveTab();
        if (this.isReadyToRun(activeTab)) {
            const file = activeTab.getFile();
            LaunchManager.debugService(file);
        } else {
            alerts.error('Save file before start debugging application');
        }
    }

    /**
     * Get the frames when connection is started
     */
    connectionStarted() {
        const debuggerInstance = DebugManager.application.debugger;
        debuggerInstance._activateBtn.tab('show');
        const width = debuggerInstance.lastWidth || debuggerInstance._options.defaultWidth;
        debuggerInstance._$parent_el.parent().width(width);
        debuggerInstance._containerToAdjust.css('padding-left', width);
        debuggerInstance._verticalSeparator.css('left', width - debuggerInstance._options.separatorOffset);
        DebugManager.application.reRender();
    }

    /**
     * Remove the frames when connection is finished
     */
    connectionEnded() {
        const debuggerInstance = DebugManager.application.debugger;
        debuggerInstance._$parent_el.parent().width('0px');
        debuggerInstance._containerToAdjust.css('padding-left', debuggerInstance._options.leftOffset);
        debuggerInstance._verticalSeparator.css('left',
            debuggerInstance._options.leftOffset - debuggerInstance._options.separatorOffset);
        debuggerInstance._activateBtn.parent('li').removeClass('active');
        DebugManager.application.reRender();
    }
    /**
     * Render tool view.
     *
     * @returns {ReactElement} render tool view.
     *
     * @memberof Tool
     */
    render() {
        this.app = this.props.app;
        return (
            <ToolSetView
                id='launcher-debug'
                state='block'
            >
                {(!this.state.started && !this.state.debugStarted) &&
                <ToolMenu
                    id='start-tool'
                    tooltip='Run'
                    onToolClick={this.onToolClickHandler}
                    icon='fw-start'
                    menuState={this.state.startChildVisible}
                >
                    <ToolMenuItem
                        id='startApplication'
                        name='Application'
                        onToolClick={this.onToolClickHandler}
                    />
                    <ToolMenuItem
                        id='startService'
                        name='Service'
                        onToolClick={this.onToolClickHandler}
                    />
                </ToolMenu> }
                {(!this.state.debugStarted && !this.state.started) &&
                <ToolMenu
                    id='debug-tool'
                    tooltip='Debugger'
                    onToolClick={this.onToolClickHandler}
                    icon='fw-bug'
                    menuState={this.state.debugChildVisible}
                >
                    <ToolMenuItem
                        id='debugApplication'
                        name='Application'
                        onToolClick={this.onToolClickHandler}
                    />
                    <ToolMenuItem
                        id='debugService'
                        name='Service'
                        onToolClick={this.onToolClickHandler}
                    />
                </ToolMenu>}
                {this.state.debugStarted &&
                    <DebugToolbarView
                        navigation={this.state.navigation}
                    /> }
                {(this.state.started && !this.state.debugStarted) &&
                <ToolItemView
                    id='stop'
                    className='btn toolbar-icons'
                    title='Stop'
                    onToolClick={this.onToolClickHandler}
                    iconClass='fw-stop stop'
                />}
            </ToolSetView>
        );
    }
}

export default LaunchDebuggerView;
