/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
/* eslint-env es6 */

import log from 'log';
import $ from 'jquery';
import _ from 'lodash';
import MenuBar from './menu-bar/menu-bar';
import BreadcrumbController from './breadcrumbs/breadcrumbs';
import TabController from './tab/file-tab-list';
import CommandManager from './command/command';
import BrowserStorage from './workspace/browser-storage';
import WorkspaceManager from './workspace/manager';
import WorkspaceExplorer from './workspace/explorer';
import Debugger from './debugger/module';
import DebugManager from './debugger/debug-manager';
import LaunchManager from './launcher/launch-manager';
import Launcher from './launcher/launcher';
import LangServerClientController from './langserver/lang-server-client-controller';
import BallerinaEnvironment from './ballerina/env/environment';
// importing for side effects only
import 'jquery-ui/ui/widgets/draggable';
import 'jquery-ui/themes/base/draggable.css';
import 'mcustom_scroller/jquery.mCustomScrollbar.css';
import 'bootstrap';
import 'theme_wso2';
import '../css/jstree.css';


class Application {

    /**
     * @constructs
     * @class Application wraps all the application logic and it is the main starting point.
     * @param {Object} config configuration options for the application
     */
    constructor(config) {
        this.validateConfig(config);
        this.config = config;
        this.initComponents();
    }

    initComponents() {
        // init command manager
        this.commandManager = new CommandManager(this);

        this.browserStorage = new BrowserStorage('ballerinaAppTempStorage');

        // init menu bar
        const menuBarOpts = _.get(this.config, 'menu_bar');
        _.set(menuBarOpts, 'application', this);
        this.menuBar = new MenuBar(menuBarOpts);

        // init workspace manager
        this.workspaceManager = new WorkspaceManager(this);

        const breadCrumbsOpts = _.get(this.config, 'breadcrumbs');
        _.set(breadCrumbsOpts, 'application', this);
        // init breadcrumbs controller
        this.breadcrumbController = new BreadcrumbController(breadCrumbsOpts);

        // init tab controller
        const tabControlOpts = _.get(this.config, 'tab_controller');
        _.set(tabControlOpts, 'application', this);

        // tab controller will take care of rendering tool palette
        this.tabController = new TabController(tabControlOpts);
        this.workspaceManager.listenToTabController();

        // init workspace explorer
        const workspaceExplorerOpts = _.get(this.config, 'workspace_explorer');
        _.set(workspaceExplorerOpts, 'application', this);
        this.workspaceExplorer = new WorkspaceExplorer(workspaceExplorerOpts);

        // init launcher
        const launcherOpts = _.get(this.config, 'launcher');
        _.set(launcherOpts, 'application', this);
        this.launcher = new Launcher(launcherOpts);

        LaunchManager.init(launcherOpts);

        // Setup the language server client controller
        const langserverOptions = {};
        _.set(langserverOptions, 'application', this);
        _.set(_.get('application'), 'config', this.config);
        this.langseverClientController = new LangServerClientController(langserverOptions);
        this.langseverClientController
                .init()
                .then(() => {
                    // lets initialize the ballerina environment.
                    BallerinaEnvironment.initialize({ app: this });
                })
                .catch(error => log.error('Error while initializing lang server. ' + error));

        // init debugger
        const debuggerOpts = _.get(this.config, 'debugger');
        _.set(debuggerOpts, 'application', this);
        _.set(debuggerOpts, 'launchManager', LaunchManager);
        this.debugger = new Debugger(debuggerOpts);

        DebugManager.init(debuggerOpts);

        // handle resize events
        // this is to resize the diagrams when the browser window is resized.
        jQuery(window).on('resize', _.debounce(_.bind(this.reRender, this), 150));
    }

    reRender() {
        this.tabController.forEach((tab) => {
            if (typeof tab.reRender === 'function') {
                tab.reRender();
            }
        });
    }

    validateConfig(config) {
        if (!_.has(config, 'services.workspace.endpoint')) {
            throw 'config services.workspace.endpoint could not be found for remote log initialization.';
        } else {
            // disable ajax appender
            // log.initAjaxAppender(_.get(config, 'services.workspace.endpoint'));
        }
        if (!_.has(config, 'breadcrumbs')) {
            log.error('breadcrumbs configuration is not provided.');
        }
        if (!_.has(config, 'workspace_explorer')) {
            log.error('Workspace explorer configuration is not provided.');
        }
        if (!_.has(config, 'tab_controller')) {
            log.error('tab_controller configuration is not provided.');
        }
    }

    render() {
        
        log.debug('start: rendering menu_bar control');
        this.menuBar.render();
        log.debug('end: rendering menu_bar control');

        log.debug('start: rendering breadcrumbs control');
        this.breadcrumbController.render();
        log.debug('end: rendering breadcrumbs control');

        log.debug('start: rendering workspace explorer control');
        this.workspaceExplorer.render();
        log.debug('end: rendering workspace explorer control');

        log.debug('start: rendering debugger control');
        this.debugger.render();
        log.debug('end: rendering debugger control');

        log.debug('start: rendering launcher control');
        this.launcher.render();
        log.debug('end: rendering launcher control');

        log.debug('start: rendering tab controller');
        this.tabController.render();
        log.debug('end: rendering tab controller');

        if (this.isElectronMode()) {
            this.menuBar.setVisible(false);
        }
    }

    displayInitialView() {
        this.workspaceManager.displayInitialTab();
    }

    hideWorkspaceArea() {
        $(this.config.container).hide();
    }

    showWorkspaceArea() {
        $(this.config.container).show();
    }

    getOperatingSystem() {
        let operatingSystem = 'Unknown OS';
        if (navigator.appVersion.indexOf('Win') != -1) {
            operatingSystem = 'Windows';
        } else if (navigator.appVersion.indexOf('Mac') != -1) {
            operatingSystem = 'MacOS';
        } else if (navigator.appVersion.indexOf('X11') != -1) {
            operatingSystem = 'UNIX';
        } else if (navigator.appVersion.indexOf('Linux') != -1) {
            operatingSystem = 'Linux';
        }
        return operatingSystem;
    }

    isRunningOnMacOS() {
        return _.isEqual(this.getOperatingSystem(), 'MacOS');
    }

    getPathSeperator() {
        return _.isEqual(this.getOperatingSystem(), 'Windows') ? '\\' : '/';
    }

    setElectronMode(isElectronMode, renderProcess) {
        this._isElectronMode = isElectronMode;
        this._renderProcess = renderProcess;
    }

    getNativeRenderProcess() {
        return this._renderProcess;
    }

    isElectronMode() {
        return this._isElectronMode;
    }

    getLangserverClientController() {
        return this.langseverClientController;
    }

}

export default Application;
