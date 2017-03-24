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
define(['require', 'lodash', 'log', 'jquery', 'backbone', 'command', 'ballerina', 'alerts'],
    function (require, _, log, $, Backbone, CommandManager, Ballerina, alerts) {

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
                var backgroundDiv = $('<div></div>');
                var mainWelcomeDiv = $('<div></div>');
                var headingDiv = $('<div></div>');
                var headingTitleSpan = $('<span></span>');
                var headingImage = $('<img>');
                var headingGroup1 = $('<div></div>');
                var wrapTitle = $('<div></div>');
                var wrapImage = $('<div></div>');

                var bodyDiv = $('<div></div>');
                var newButton = $('<button></button>');
                var openButton = $('<button></button>');
                var buttonGroup1 = $('<div></div>');                

                var bodyTitleSpan = $('<span></span>');
                var samplesDiv = $('<div></div>');

                backgroundDiv.addClass(_.get(this._options, 'cssClass.parent'));
                mainWelcomeDiv.addClass(_.get(this._options, 'cssClass.outer'));
                headingDiv.addClass(_.get(this._options, 'cssClass.heading'));
                headingTitleSpan.addClass(_.get(this._options, 'cssClass.headingTitle'));
                headingImage.addClass(_.get(this._options, 'cssClass.headingIcon'));
                headingImage.attr('src', 'images/Ballerina.svg');
                newButton.addClass(_.get(this._options, 'cssClass.buttonNew'));
                openButton.addClass(_.get(this._options, 'cssClass.buttonOpen'));
                headingGroup1.addClass(_.get(this._options, 'cssClass.headingTop'));
                buttonGroup1.addClass(_.get(this._options, 'cssClass.btnWrap1'));
                

                bodyDiv.addClass(_.get(this._options, 'cssClass.body'));
                bodyTitleSpan.addClass(_.get(this._options, 'cssClass.bodyTitle'));
                samplesDiv.addClass(_.get(this._options, 'cssClass.samples'));
                samplesDiv.attr('id', 'samplePanel');

                newButton.text("New");
                openButton.text("Open");

                headingTitleSpan.text("Welcome to");
                bodyTitleSpan.text("Try out our samples / Templates");

                wrapTitle.append(headingTitleSpan);
                headingGroup1.append(wrapTitle);
                wrapImage.append(headingImage);
                headingGroup1.append(wrapImage);

                buttonGroup1.append(newButton);
                buttonGroup1.append(openButton);

                headingDiv.append(headingGroup1);
                headingDiv.append(buttonGroup1);                

                bodyDiv.append(bodyTitleSpan);
                bodyDiv.append(samplesDiv);

                mainWelcomeDiv.append(headingDiv);
                mainWelcomeDiv.append(bodyDiv);
                backgroundDiv.append(mainWelcomeDiv);

                this._$parent_el.append(backgroundDiv);
                this.$el = backgroundDiv;

                var innerDiv = $('<div></div>');
                innerDiv.attr('id', "innerSamples");
                samplesDiv.append(innerDiv);

                var command = this._options.application.commandManager;
                var browserStorage = this._options.application.browserStorage;

                var samples = _.get(this._options, "samples", []);
                
                var config;
                var servicePreview;

                var self = this;

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
                                    "parentContainer": "#innerSamples",
                                    "firstItem": i === 0,
                                    "clickEventCallback": function () {
                                        var root = "";
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
                                                        var BallerinaASTDeserializer = Ballerina.ast.BallerinaASTDeserializer;
                                                        root = BallerinaASTDeserializer.getASTModel(data);
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
                                        command.dispatch("create-new-tab", {tabOptions: {astRoot: root}});
                                        browserStorage.put("pref:passedFirstLaunch", true);
                                    }
                                };
                            servicePreview = new Ballerina.views.ServicePreviewView(config);
                            servicePreview.render();
                        },
                        error: function() {
                            alerts.error("Unable to read a sample file.");
                            throw "Unable to read a sample file.";
                        }
                    });
                }

                var command = this._options.application.commandManager;
                var browserStorage = this._options.application.browserStorage;

                // When "new" is clicked, open up an empty workspace.
                $(newButton).on('click', function () {
                    command.dispatch("create-new-tab");
                    browserStorage.put("pref:passedFirstLaunch", true);
                });

                // Show the open file dialog when "open" is clicked.
                $(openButton).click(function(){
                    command.dispatch("open-file-open-dialog");
                    browserStorage.put("pref:passedFirstLaunch", true);
                });

                // upon welcome tab remove, set flag to indicate first launch pass
                this._tab.on('removed', function(){
                    browserStorage.put("pref:passedFirstLaunch", true);
                });
            }
        });

        return FirstLaunchWelcomePage;

    });

