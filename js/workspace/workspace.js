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
define(['jquery', 'lodash', 'backbone', 'log', 'bootstrap'], function ($, _, Backbone, log) {

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

        this.createNewTab = function createNewTab() {
            var welcomeContainerId = app.config.welcome.container;
            $(welcomeContainerId).css("display", "none");
            var editorId = app.config.container;
            $(editorId).css("display", "block");
            //Showing menu bar
            app.menuBar.show();
            app.tabController.newTab();
        };

        this.displayInitialView = function () {

            app.hideWorkspaceArea();
            app.initialWelcomePage.hide();
            app.reqularWelcomeScreen.hide();

            if(app.initialWelcomePage.passedFirstLaunch()){
                if(app.tabController.hasFilesInWorkingSet()){
                    // there were active tabs when closing app last time - open them
                    app.showWorkspaceArea();
                } else {
                    // show regular welcome screen with open recent, etc. and hide others
                    app.reqularWelcomeScreen.show();
                }
            } else {
                // show initial product launch page and hide others
               app.initialWelcomePage.show();
            }

        };

        app.commandManager.registerCommand("create-new-tab", {key: ""});
        app.commandManager.registerHandler('create-new-tab', this.createNewTab);

    }


});

