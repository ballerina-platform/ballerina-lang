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
define(['jquery', 'lodash', 'backbone', 'log', 'dialogs', 'welcome-page', 'tab/tab', 'workspace'],
    function ($, _, Backbone, log, Dialogs, WelcomePages, GenericTab, Workspace) {

    // workspace manager constructor
    /**
     * Arg: application instance
     */
    return function (app) {

        if (_.isUndefined(app.commandManager)) {
            var error = "CommandManager is not initialized.";
            log.error(error);
            throw error;
        }

        this.createNewTab = function createNewTab(ballerinaRoot) {
            // Showing menu bar
            app.tabController.newTab({ballerinaRoot: ballerinaRoot});
        };

        this.displayInitialTab = function () {
            //TODO : remove this if else condition
            // display first launch welcome page tab
            if (!this.passedFirstLaunch()) {
                // create a generic tab - without ballerina editor components
                var tab = app.tabController.newTab({
                    tabModel: GenericTab,
                    tabOptions:{title: 'welcome-page'}
                });
                var opts = _.get(app.config, 'welcome');
                _.set(opts, 'application', app);
                _.set(opts, 'tab', tab);
                this.welcomePage = new WelcomePages.FirstLaunchWelcomePage(opts);
                this.welcomePage.render();
            } else {
                // user has no active tabs from last session
                if (!app.tabController.hasFilesInWorkingSet()) {
                    // create a generic tab - without ballerina editor components
                    var tab = app.tabController.newTab({
                        tabModel: GenericTab,
                        tabOptions:{title: 'welcome-page'}
                    });
                    // Showing FirstLaunchWelcomePage instead of regularWelcomePage
                    var opts = _.get(app.config, 'welcome');
                    _.set(opts, 'application', app);
                    _.set(opts, 'tab', tab);
                    this.welcomePage = new WelcomePages.FirstLaunchWelcomePage(opts);
                    this.welcomePage.render();
                }
            }
        };

        this.passedFirstLaunch = function(){
            return app.browserStorage.get("pref:passedFirstLaunch") || false;
        };

        /**
         * Showing the welcome page. If "welcome page" exists, then show existing "welcome page", else create new
         * "welcome page".
         * @param workspaceManager - The workspace manager.
         */
        this.showWelcomePage = function(workspaceManager) {
            var existingWelcomeTab = _.find(app.tabController.getTabList(), function (tab) {
                return tab._title == "welcome-page";
            });

            if (_.isUndefined(existingWelcomeTab)) {
                // Creating a new welcome tab.
                var tab = app.tabController.newTab({
                    tabModel: GenericTab,
                    tabOptions:{title: 'welcome-page'}
                });
                // Showing FirstLaunchWelcomePage instead of regularWelcomePage
                var opts = _.get(app.config, 'welcome');
                _.set(opts, 'application', app);
                _.set(opts, 'tab', tab);
                workspaceManager.welcomePage = new WelcomePages.FirstLaunchWelcomePage(opts);
                workspaceManager.welcomePage.render();
            } else {
                // Showing existing welcome tab.
                app.tabController.setActiveTab(existingWelcomeTab);
            }
        };

        this.openFileSaveDialog = function openFileSaveDialog() {
            var dialog = new Dialogs.save_to_file_dialog(app);
            dialog.render();
        };

        this.openFileOpenDialog = function openFileOpenDialog() {
            var dialog = new Dialogs.open_file_dialog(app);
            dialog.render();
        };

        this.goToWelcomePage = function goToWelcomePage() {
            this.workspaceManager.showWelcomePage(this.workspaceManager);
        };

        this.handleUndo = function() {
            app.tabController.getActiveTab().getBallerinaFileEditor().undoManager.undo();
            app.menuBar.getMenuItemByID('edit.undo').addLabelSuffix(
                app.tabController.getActiveTab().getBallerinaFileEditor().undoManager.undoStackTop().getTitle());
        };

        this.handleRedo = function() {
            app.tabController.getActiveTab().getBallerinaFileEditor().undoManager.redo();
        };

        app.commandManager.registerCommand("create-new-tab", {key: ""});
        app.commandManager.registerHandler('create-new-tab', this.createNewTab);

        app.commandManager.registerCommand("undo", {key: ""});
        app.commandManager.registerHandler('undo', this.handleUndo);

        app.commandManager.registerCommand("redo", {key: ""});
        app.commandManager.registerHandler('redo', this.handleRedo);

        // Open file save dialog
        app.commandManager.registerCommand("open-file-save-dialog", {key: ""});
        app.commandManager.registerHandler('open-file-save-dialog', this.openFileSaveDialog);

        // Open file open dialog
        app.commandManager.registerCommand("open-file-open-dialog", {key: ""});
        app.commandManager.registerHandler('open-file-open-dialog', this.openFileOpenDialog);

        // Go to Welcome Page.
        app.commandManager.registerCommand("go-to-welcome-page", {key: ""});
        app.commandManager.registerHandler('go-to-welcome-page', this.goToWelcomePage);

    }

});

