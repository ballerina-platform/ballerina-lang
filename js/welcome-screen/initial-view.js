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
define(['require', 'log', 'jquery', 'backbone', 'command'],
    function (require, log, $, Backbone, CommandManager) {

        var InitialWelcomePage = Backbone.View.extend({
            initialize: function (options) {
                var errMsg;
                if (!_.has(options, 'container')) {
                    errMsg = 'unable to find configuration for container';
                    log.error(errMsg);
                    throw errMsg;
                }
                var container = $(_.get(options, 'container'));
                // check whether container element exists in dom
                if (!container.length > 0) {
                    errMsg = 'unable to find container for welcome screen with selector: ' + _.get(options, 'container');
                    log.error(errMsg);
                    throw errMsg;
                }
                this._$parent_el = container;
                this._options = options;

            },

            render: function () {
                var backgroundDiv = $('<div></div>');
                var mainWelcomeDiv = $('<div></div>');
                var headingDiv = $('<div></div>');
                var headingTitleSpan = $('<span></span>');
                var headingImage = $('<img>');
                var headingGroup1 = $('<div></div>');
                var wrapTitle =  $('<div></div>');
                var wrapImage = $('<div></div>');

                var bodyDiv = $('<div></div>');
                var newButton = $('<button></button>');
                var openButton = $('<button></button>');
                var buttonGroup1 = $('<div></div>');
                var buttonGroup2 = $('<div></div>');

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
                buttonGroup2.addClass(_.get(this._options, 'cssClass.btnWrap2'));

                bodyDiv.addClass(_.get(this._options, 'cssClass.body'));
                bodyTitleSpan.addClass(_.get(this._options, 'cssClass.bodyTitle'));
                samplesDiv.addClass(_.get(this._options, 'cssClass.samples'));

                newButton.text("Get Started");
                openButton.text("Open");

                headingTitleSpan.text("Welcome to");
                bodyTitleSpan.text("Try out our samples / Templates");

                wrapTitle.append(headingTitleSpan);
                headingGroup1.append(wrapTitle);
                wrapImage.append(headingImage);
                headingGroup1.append(wrapImage);

                buttonGroup1.append(newButton);
                buttonGroup2.append(openButton);

                headingDiv.append(headingGroup1);
                headingDiv.append(buttonGroup1);
                headingDiv.append(buttonGroup2);

                bodyDiv.append(bodyTitleSpan);
                bodyDiv.append(samplesDiv);

                mainWelcomeDiv.append(headingDiv);
                mainWelcomeDiv.append(bodyDiv);
                backgroundDiv.append(mainWelcomeDiv);

                this._$parent_el.append(backgroundDiv);
                this.$el = backgroundDiv;

                var command = this._options.application.commandManager;

                $(newButton).on('click', function () {
                    command.dispatch("create-new-tab");

                });
            }

        });

        return InitialWelcomePage;

    });

