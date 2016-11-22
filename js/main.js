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
define(['require', 'log', 'jquery', 'lodash', 'backbone', 'breadcrumbs', 'file_browser', 'tab/service-tab-list', 'app/tool-palette/tool-palette',

    'welcome','command','workspace',/* void modules */ 'jquery_ui', 'bootstrap'],

    function (require, log, $, _, Backbone, BreadcrumbController, FileBrowser, TabController, ToolPalette, WelcomeScreen,CommandManager,Workspace) {

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

            //init workspace manager
            this.workspaceManager = new Workspace(this);

            // init breadcrumbs controller
            this.breadcrumbController = new BreadcrumbController(_.get(this.config, "breadcrumbs"));

            //init file browser
            var fileBrowserOpts = _.get(this.config, "file_browser");
            _.set(fileBrowserOpts, 'application', this);
            this.fileBrowser = new FileBrowser(fileBrowserOpts);

            //init tool palette
            var toolPaletteOpts = _.get(this.config, "tab_controller.tool_palette");
            _.set(toolPaletteOpts, 'application', this);
            this.toolPalette = new ToolPalette(toolPaletteOpts);

            //init tab controller
            var tabControlOpts = _.get(this.config, "tab_controller");
            _.set(tabControlOpts, 'application', this);

            // tab controller will take care of rendering tool palette
            _.set(tabControlOpts, 'toolPalette', this.toolPalette);
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
            this.reqularWelcomeScreen = new WelcomeScreen.Views.RegularView(regWelcomeScreenOpts);
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
            if(!_.has(config, 'file_browser')){
                log.error('file_browser configuration is not provided.');
            }
            if(!_.has(config, 'tab_controller.tool_palette')){
                log.error('tool_palette configuration is not provided.');
            }
            if(!_.has(config, 'tab_controller')){
                log.error('tab_controller configuration is not provided.');
            }
        },

        render: function () {
            this.reqularWelcomeScreen.render(this);
            log.debug("start: rendering breadcrumbs control");
            this.breadcrumbController.render();
            log.debug("end: rendering breadcrumbs control");

            log.debug("start: rendering file_browser control");
            this.fileBrowser.render();
            log.debug("end: rendering file_browser control");

            log.debug("start: rendering tab controller");
            this.tabController.render();
            log.debug("end: rendering tab controller");
        },

        showWelcomeScreen: function () {
            this.workspaceManager.popupRegularWelcomeScreen();
        }

    });

    return Application;
});