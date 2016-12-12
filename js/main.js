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
define(['require', 'log', 'jquery', 'lodash', 'backbone', 'app/menu-bar/menu-bar', 'breadcrumbs', 'file_browser', 'tab/file-tab-list',

    'welcome','command','workspace',/* void modules */ 'jquery_ui', 'bootstrap'],

    function (require, log, $, _, Backbone, MenuBar, BreadcrumbController, FileBrowser, TabController, WelcomeScreen, CommandManager, Workspace) {

    var Application = Backbone.View.extend(
    /** @lends Application.prototype */
    {
        /**
         * @augments Backbone.View
         * @constructs
         * @class Application wraps all the application logic and it is the main starting point.
         * @param {Object} config configuration options for the application
         */
        initialize: function (config) {
            this.validateConfig(config);
            this.config = config;
            this.initComponents();
        },

        initComponents: function(){

            // init command manager
            this.commandManager = new CommandManager();

            this.browserStorage = new Workspace.BrowserStorage('ballerinaAppTempStorage');

            //init workspace manager
            this.workspaceManager = new Workspace.Manager(this);

            //init menu bar
            var menuBarOpts = _.get(this.config, "menu_bar");
            _.set(menuBarOpts, 'application', this);
            this.menuBar = new MenuBar(menuBarOpts);

            // init breadcrumbs controller
            this.breadcrumbController = new BreadcrumbController(_.get(this.config, "breadcrumbs"));

            //init workspace explorer
            var workspaceExplorerOpts = _.get(this.config, "workspace_explorer");
            _.set(workspaceExplorerOpts, 'application', this);
            this.workspaceExplorer = new Workspace.Explorer(workspaceExplorerOpts);

            //init tab controller
            var tabControlOpts = _.get(this.config, "tab_controller");
            _.set(tabControlOpts, 'application', this);

            // tab controller will take care of rendering tool palette
            this.tabController = new TabController(tabControlOpts);

            //TODO : get from module
            var welcomeOpts = _.get(this.config, "welcome");
            _.set(welcomeOpts, 'application', this);
            this.initialWelcomePage = new WelcomeScreen.Views.PrimaryView(welcomeOpts);

            //init the regular welcome screen
            var regWelcomeScreenOpts = {
                container: '#page-content-wrapper',
                commandManager: this.commandManager
            };
            _.set(regWelcomeScreenOpts, 'application', this);
            this.regularWelcomeScreen = new WelcomeScreen.Views.RegularView(regWelcomeScreenOpts);
        },

        validateConfig: function(config){
            if(!_.has(config, 'services.workspace.endpoint')){
                throw 'config services.workspace.endpoint could not be found for remote log initialization.'
            } else {
                log.initAjaxAppender(_.get(config, 'services.workspace.endpoint'));
            }
            if(!_.has(config, 'breadcrumbs')){
                log.error('breadcrumbs configuration is not provided.');
            }
            if(!_.has(config, 'workspace_explorer')){
                log.error('Workspace explorer configuration is not provided.');
            }
            if(!_.has(config, 'tab_controller')){
                log.error('tab_controller configuration is not provided.');
            }
        },

        render: function () {
            log.debug("start: rendering regular welcome page");
            this.regularWelcomeScreen.render();
            log.debug("end: rendering regular welcome page");

            log.debug("start: rendering initial welcome page");
            this.initialWelcomePage.render();
            log.debug("end: rendering initial welcome page");

            log.debug("start: rendering menu_bar control");
            this.menuBar.render();
            log.debug("end: rendering menu_bar control");

            log.debug("start: rendering breadcrumbs control");
            this.breadcrumbController.render();
            log.debug("end: rendering breadcrumbs control");

            log.debug("start: rendering workspace explorer control");
            this.workspaceExplorer.render();
            log.debug("end: rendering workspace explorer control");

            log.debug("start: rendering tab controller");
            this.tabController.render();
            log.debug("end: rendering tab controller");
        },

        displayInitialView: function () {
            this.workspaceManager.displayInitialView();
        },

        hideWorkspaceArea: function(){
            $(this.config.container).hide();
        },

        showWorkspaceArea: function(){
            $(this.config.container).show();
        }

    });

    return Application;
});
