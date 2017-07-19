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

import log from 'log';
import $ from 'jquery';
import Backbone from 'backbone';
import _ from 'lodash';
import alerts from 'alerts';
import LaunchManager from './launch-manager';

const Launcher = Backbone.View.extend({
    /**
     * Launcher view
     * @param {Object} config - Configuration object
     */
    initialize(config) {
        let errMsg;
        if (!_.has(config, 'container')) {
            errMsg = 'unable to find configuration for container';
            log.error(errMsg);
            throw errMsg;
        }
        const container = $(_.get(config, 'container'));
        // check whether container element exists in dom
        if (!container.length > 0) {
            errMsg = `unable to find container for file browser with selector: ${_.get(config, 'container')}`;
            log.error(errMsg);
            throw errMsg;
        }
        this._$parent_el = container;

        if (!_.has(config, 'application')) {
            log.error('Cannot init file browser. config: application not found.');
        }
        this.application = _.get(config, 'application');
        this._options = config;
        this._lastWidth = undefined;
        this._verticalSeparator = $(_.get(this._options, 'separator'));
        this._containerToAdjust = $(_.get(this._options, 'containerToAdjust'));
        this._items = [];

        // register command
        this.application.commandManager.registerCommand(config.command.id, { shortcuts: config.command.shortcuts });
        this.application.commandManager.registerHandler(config.command.id, this.toggleLauncher, this);

        this.compiled = _.template(`
            <div>
              <% if (!active) { %>
                <div class="debug-panel-header">
                     <span class="tool-group-header-title">Run</span>
                </div>
                <div class="btn-group col-xs-12">
                     <div type="button" id="run_application"
                        class="btn btn-default text-left btn-debug-activate col-xs-12" title="Start Application">
                     <span class="launch-label">Application</span>
                        <button type="button" class="btn btn-default pull-right btn-config" title="Config">
                          <i class="fw fw-configarations"></i>
                        </button>
                      </div>
                     <button type="button" id="run_service"
                        class="btn btn-default text-left btn-debug-activate col-xs-12" title="Start Service">
                        Service
                      </button>
                </div>
                <% } %>
                <% if (active) { %>
                <div class="debug-panel-header">
                     <span class="tool-group-header-title">Program Running</span></span>
                </div>
                <div class="btn-group col-xs-12">
                <button type="button" class="btn btn-default btn-debug-activate" title="Redeploy" id="reploy-program">
                    <i class="fw fw-refresh" />
                    Redeploy Application
                </button>
                <button type="button" class="btn btn-default btn-debug-activate"
                  title="Stop Application" id="stop_program">
                  <i class="fw fw-stop" />
                  Stop Application
                </button>
                <% if (tryItUrl) { %>
                    <button type="button" class="btn btn-default btn-debug-activate"
                    title="Try it" id="try_it_service">
                        <i class="fw fw-dgm-try-catch" />
                        Try it 
                    </button>
                <% } %>
              </div>
              <% } %>
            </div>`);

        this.appArgsDialog = $('#modalRunApplicationWithArgs');

        // event bindings
        this._$parent_el.on('click', '#run_application', () => { this.runApplication(); });
        this._$parent_el.on('click', '#run_service', () => { this.runService(); });
        this._$parent_el.on('click', '#stop_program', () => { this.stopProgram(); });
        this._$parent_el.on('click', '#reploy-program', () => { this.reDeployProgram(); });
        this._$parent_el.on('click', '#try_it_service', () => { this.showSwaggerView(); });

        LaunchManager.on('execution-started', () => { this.renderBody(); });
        LaunchManager.on('execution-ended', () => { this.renderBody(); });
        LaunchManager.on('try-it-url-received', () => { this.renderBody(); });

        this._$parent_el.on('click', '.btn-config', (e) => {
            e.preventDefault();
            e.stopPropagation();
            this.appArgsDialog.modal('show');
        });
    },
    /**
     * Run Service, Alerts an error to user if file is not saved
     */
    runService() {
        const activeTab = this.application.tabController.getActiveTab();
        if (this.isReadyToRun(activeTab)) {
            const file = activeTab.getFile();
            LaunchManager.runService(file);
        } else {
            alerts.error('Save file before running service');
        }
    },
    /**
     * Run application, Alerts an error to user if file is not saved
     */
    runApplication() {
        const activeTab = this.application.tabController.getActiveTab();

        // only file tabs can run application
        if (this.isReadyToRun(activeTab)) {
            const file = activeTab.getFile();
            LaunchManager.runApplication(file);
        } else {
            alerts.error('Save file before running application');
        }
    },
    /**
     * Checks whether file or service in the FileTab is ready to run.
     * @param {FileTab} tab - instance of FileTab
     * @returns Boolean
     */
    isReadyToRun(tab) {
        if (typeof tab.getFile !== 'function') {
            return false;
        }

        const file = tab.getFile();
            // file is not saved give an error and avoid running
        if (file.isDirty()) {
            return false;
        }

        return true;
    },
    /**
     * Stops currently running application or service
     */
    stopProgram() {
        LaunchManager.stopProgram();
    },
    /**
     * Redeploy currently running application or service
     */
    reDeployProgram() {
        const activeTab = this.application.tabController.getActiveTab();
        if (this.isReadyToRun(activeTab)) {
            this.stopProgram();
            // wait for termination message from channel
            LaunchManager.once('execution-ended', () => {
                this.runService();
            });
        } else {
            alerts.error('Save file before redeploying service');
        }
    },
    /**
     * Returns true if launcher toolbar is active
     * @returns Boolean
     */
    isActive() {
        return this._activateBtn.parent('li').hasClass('active');
    },
    /**
     * Toggle launcher view
     */
    toggleLauncher() {
        if (this.isActive()) {
            this._$parent_el.parent().width('0px');
            this._containerToAdjust.css('padding-left', _.get(this._options, 'leftOffset'));
            this._verticalSeparator.css('left',
                _.get(this._options, 'leftOffset') - _.get(this._options, 'separatorOffset'));
            this._activateBtn.parent('li').removeClass('active');
            this.application.reRender();// to update the diagrams
        } else {
            this._activateBtn.tab('show');
            const width = this._lastWidth || _.get(this._options, 'defaultWidth');
            this._$parent_el.parent().width(width);
            this._containerToAdjust.css('padding-left', width);
            this._verticalSeparator.css('left', width - _.get(this._options, 'separatorOffset'));
            this.application.reRender();// to update the diagrams
        }
    },
    showSwaggerView() {
        this.application.commandManager.dispatch('show-try-it-view');
    },
    /** @inheritdoc */
    render() {
        const activateBtn = $(_.get(this._options, 'activateBtn'));
        this._activateBtn = activateBtn;

        const launcherContainer = $('<div role="tabpanel"></div>');
        launcherContainer.addClass(_.get(this._options, 'cssClass.container'));
        launcherContainer.attr('id', _.get(this._options, ('containerId')));
        this._$parent_el.append(launcherContainer);

        activateBtn.on('click', (e) => {
            $(this).tooltip('hide');
            e.preventDefault();
            e.stopPropagation();
            this.application.commandManager.dispatch(_.get(this._options, 'command.id'));
        });

        activateBtn.attr('data-placement', 'bottom').attr('data-container', 'body');

        if (this.application.isRunningOnMacOS()) {
            activateBtn.attr('title', `Run ( ${_.get(this._options, 'command.shortcuts.mac.label')} ) `).tooltip();
        } else {
            activateBtn.attr('title', `Run ( ${_.get(this._options, 'command.shortcuts.other.label')} ) `).tooltip();
        }
        this._launcherContainer = launcherContainer;

        launcherContainer.mCustomScrollbar({
            theme: 'minimal',
            scrollInertia: 0,
            axis: 'xy',
        });
        if (!_.isEmpty(this._openedFolders)) {
            this._openedFolders.forEach((folder) => {
                this.createExplorerItem(folder);
            });
        }
        this.renderBody();
        $('.btn-debug-activate').tooltip();
        return this;
    },
    /**
     * Render body of the launcher view
     */
    renderBody() {
        this._launcherContainer.html(this.compiled(LaunchManager));
    },
    /**
     * Opens console output
     */
    showConsole() {
        $('#tab-content-wrapper').css('height:70%');
        $('#console-container').css('height:30%');
    },
});

export default Launcher;
