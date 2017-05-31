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
import BallerinaASTDeserializer from 'ballerina/ast/ballerina-ast-deserializer';
import alerts from 'alerts';
import React from 'react';
import ReactDOM from 'react-dom';
import WelcomeView from './welcome.jsx';
import ServicePreviewView from './service-preview.jsx';

var FirstLaunchWelcomePage = Backbone.View.extend({
    initialize: function (options) {
        var errMsg;
        if (!_.has(options, 'tab')) {
            errMsg = 'unable to find a reference for editor tab';
            log.error(errMsg);
            throw errMsg;
        }
        this._tab = _.get(options, 'tab');
        var container = $(this._tab.getContentContainer());
        // make sure default tab content are cleared
        container.empty();
        // check whether container element exists in dom
        if (!container.length > 0) {
            errMsg = 'unable to find container for welcome screen with selector: ' + _.get(options, 'container');
            log.error(errMsg);
            throw errMsg;
        }
        this._$parent_el = container;
        this._options = options;
    },

    hide: function(){
        //Hiding menu bar
        this._options.application.menuBar.show();
        this.$el.hide();
    },

    show: function(){
        //Hiding menu bar
        this._options.application.menuBar.hide();
        this.$el.show();
    },

    render: function () {
        let view = React.createElement(WelcomeView, {}, null);
        ReactDOM.render(
            view,
            this._$parent_el[0]
        );

        var command = this._options.application.commandManager;
        var browserStorage = this._options.application.browserStorage;

        var samples = _.get(this._options, "samples", []);

        var config;
        var servicePreview;

        var self = this;
        var sampleConfigs = [];

        for (var i = 0; i < samples.length; i++) {
            $.ajax({
                url: samples[i],
                type: "GET",
                async: false,
                success: function(fileContentAsString) {
                    var content = {"content": fileContentAsString};
                    var config =
                        {
                            "sampleName": samples[i].replace(/^.*[\\\/]/, '').match(/[^.]*/i)[0],
                            "parentContainer": "#inner-samples",
                            "firstItem": i === 0,
                            "clickEventCallback": function () {
                                var response = "";
                                $.ajax({
                                    url: _.get(self._options.application, "config.services.parser.endpoint"),
                                    type: "POST",
                                    data: JSON.stringify(content),
                                    contentType: "application/json; charset=utf-8",
                                    async: false,
                                    dataType: "json",
                                    success: function (data, textStatus, xhr) {
                                        if (xhr.status == 200) {
                                            if (!_.isUndefined(data.errorMessage)) {
                                                alerts.error("Unable to parse the source: " + data.errorMessage);
                                            } else {
                                                response = data;
                                            }
                                        } else {
                                            log.error("Error while parsing the source: " + JSON.stringify(xhr));
                                            alerts.error("Error while parsing the source.");
                                        }
                                    },
                                    error: function (res, errorCode, error) {
                                        log.error("Error while parsing the source. " + JSON.stringify(res));
                                        alerts.error("Error while parsing the source.");
                                    }
                                });
                                command.dispatch("create-new-tab", {tabOptions: {parseResponse: response}});
                                browserStorage.put("pref:passedFirstLaunch", true);
                            }
                        };

                    sampleConfigs.push(config);
                },
                error: function() {
                    alerts.error("Unable to read a sample file.");
                    throw "Unable to read a sample file.";
                }
            });
        }

        let previews = React.createElement(ServicePreviewView, {sampleConfigs}, null);
        ReactDOM.render(
            previews,
            $("#inner-samples")[0]
        );

        var command = this._options.application.commandManager;
        var browserStorage = this._options.application.browserStorage;

        // When "new" is clicked, open up an empty workspace.
        $('#btn-welcome-new').on('click', function () {
            command.dispatch("create-new-tab");
            browserStorage.put("pref:passedFirstLaunch", true);
        });

        // Show the open file dialog when "open" is clicked.
        $('#btn-welcome-open').on('click', function(){
            command.dispatch("open-file-open-dialog");
            browserStorage.put("pref:passedFirstLaunch", true);
        });

        // upon welcome tab remove, set flag to indicate first launch pass
        this._tab.on('removed', function(){
            browserStorage.put("pref:passedFirstLaunch", true);
        });
    }
});

export default FirstLaunchWelcomePage;
