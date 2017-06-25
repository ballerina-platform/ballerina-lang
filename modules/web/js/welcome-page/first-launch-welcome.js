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
import _ from 'lodash';
import log from 'log';
import $ from 'jquery';
import Backbone from 'backbone';
import React from 'react';
import ReactDOM from 'react-dom';
import WelcomeView from './welcome.jsx';
import ServicePreviewView from './service-preview.jsx';

const FirstLaunchWelcomePage = Backbone.View.extend({
    initialize(options) {
        let errMsg;
        if (!_.has(options, 'tab')) {
            errMsg = 'unable to find a reference for editor tab';
            log.error(errMsg);
            throw errMsg;
        }
        this._tab = _.get(options, 'tab');
        const container = $(this._tab.getContentContainer());
        // make sure default tab content are cleared
        container.empty();
        // check whether container element exists in dom
        if (!container.length > 0) {
            errMsg = `unable to find container for welcome screen with selector: ${_.get(options, 'container')}`;
            log.error(errMsg);
            throw errMsg;
        }
        this._$parent_el = container;
        this._options = options;
    },

    hide() {
        // Hiding menu bar
        this._options.application.menuBar.show();
        this.$el.hide();
    },

    show() {
        // Hiding menu bar
        this._options.application.menuBar.hide();
        this.$el.show();
    },

    render() {
        const view = React.createElement(WelcomeView, {}, null);
        ReactDOM.render(view, this._$parent_el[0]);

        const commandManager = this._options.application.commandManager;
        const browserStorage = this._options.application.browserStorage;
        const workspaceExplorer = this._options.application.workspaceExplorer;
        const pathSeperator = this._options.application.getPathSeperator();

        const ballerinaHome = _.get(this._options, 'balHome');
        const samples = _.get(this._options, 'samples', []);
        let sampleConfigs = [];
        sampleConfigs = samples.map(sample => ({
            sampleName: sample.name,
            isFile: sample.isFile,
            clickEventCallback: () => {
                // convert paths to platform specific paths 
                const sampleFile = sample.path.split('/').join(pathSeperator),
                      sampleFolder = sample.folder.split('/').join(pathSeperator);
                if (sample.isFile) {
                    commandManager.dispatch('open-file', ballerinaHome + sampleFile);
                } else {
                    commandManager.dispatch('open-folder', ballerinaHome + sampleFolder);
                    if (!workspaceExplorer.isActive()) {
                        commandManager.dispatch('toggle-file-explorer');
                    }
                    commandManager.dispatch('open-file', ballerinaHome + sampleFile);
                }
            },
            image: sample.image,
        }));

        const previews = React.createElement(ServicePreviewView, { sampleConfigs }, null);
        ReactDOM.render(previews, $('#inner-samples')[0]);

        // When "new" is clicked, open up an empty workspace.
        $('#btn-welcome-new').on('click', () => {
            commandManager.dispatch('create-new-tab');
            browserStorage.put('pref:passedFirstLaunch', true);
        });

        // Show the open file dialog when "open" is clicked.
        $('#btn-welcome-open').on('click', () => {
            commandManager.dispatch('open-file-open-dialog');
            browserStorage.put('pref:passedFirstLaunch', true);
        });

        // upon welcome tab remove, set flag to indicate first launch pass
        this._tab.on('removed', () => {
            browserStorage.put('pref:passedFirstLaunch', true);
        });
    },
});

export default FirstLaunchWelcomePage;
