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
define(['jquery', 'lodash', 'backbone', 'log', 'dialogs', 'welcome-page', 'tab/tab', 'bootstrap'],
    function ($, _, Backbone, log, Dialogs, WelcomePages, GenericTab) {

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
            //Showing menu bar
            app.menuBar.show();
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
                this.firstLaunchWelcomePage = new WelcomePages.FirstLaunchWelcomePage(opts);
                this.firstLaunchWelcomePage.render();
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
                    this.regularWelcomePage = new WelcomePages.FirstLaunchWelcomePage(opts);
                    this.regularWelcomePage.render();
                }
            }
        };

        this.passedFirstLaunch = function(){
            return app.browserStorage.get("pref:passedFirstLaunch") || false;
        };

        this.openFileSaveDialog = function openFileSaveDialog() {
            var dialog = new Dialogs.save_to_file_dialog(app);
            dialog.render();
        };

        this.openFileOpenDialog = function openFileOpenDialog() {
            var dialog = new Dialogs.open_file_dialog(app);
            dialog.render();
        }

        app.commandManager.registerCommand("create-new-tab", {key: ""});
        app.commandManager.registerHandler('create-new-tab', this.createNewTab);

        // Open file save dialog
        app.commandManager.registerCommand("open-file-save-dialog", {key: ""});
        app.commandManager.registerHandler('open-file-save-dialog', this.openFileSaveDialog);

        // Open file open dialog
        app.commandManager.registerCommand("open-file-open-dialog", {key: ""});
        app.commandManager.registerHandler('open-file-open-dialog', this.openFileOpenDialog);

    }

});

