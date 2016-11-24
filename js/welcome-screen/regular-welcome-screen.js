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
define(['require', 'backbone', 'lodash'], function ( require, Backbone, _) {

    var RegularWelcomeScreenView = Backbone.View.extend(
        /** @lends RegularWelcomeScreenView.prototype */
        {
            /**
             * @augments
             * @constructs
             * @class RegularWelcomeScreenView Represents the view for Regular welcome screen.
             * @param {Object} options Rendering options for the view
             */
            initialize: function (options) {
                if (!_.has(options, 'container')) {
                    throw "Container not provided"
                }
                this.commandManager = options.commandManager;
                this._$container = $(_.get(options, 'container'));
                this._options = options;
            },

            render: function () {
                var outerContainer = $("<div class='regular-welcome-screen-container'></div>");
                var leftPanel = $("<div class='col-md-2 reg-welcome-left'></div>");
                var rightPanel = $("<div class='col-md-10 reg-welcome-right'></div>");
                var leftLogoContainer = $(
                    "<div style='padding-top: 75px'>" +
                        "<img src='images/wso2-logo.jpg' width='33%' style='margin: auto; display: block'>" +
                        "<div style='width: 55%; font-size: 40px; margin: auto'>Ballerina</div>" +
                    "</div>"
                );
                var leftActionsContainer = $("<div></div>");
                var buttonContainer = $("<div class='btn-container' style='padding-top: 30px; width: 55%; margin: auto'></div>");
                var newButton = $("<div><button type='button' class='btn btn-new btn-block'>New</button></div>");
                var openButton = $("<div style='padding-top: 10px'><button type='button' class='btn btn-open btn-block'>Open</button></div>");
                buttonContainer.append(newButton);
                buttonContainer.append(openButton);
                leftActionsContainer.append(buttonContainer);
                leftPanel.append(leftLogoContainer);
                leftPanel.append(leftActionsContainer);

                // Create the DOM for the right panel
                var openRecentSection = $("<div class='open-recent-section'></div>");
                var openRecentHeading = $("<div class='open-recent-heading'>Open Recent</div>");
                var recentPreviewSection = $("<div class='recent-preview-section'></div>");

                // Dynamically add the preview divs for recent open diagrams
                // TODO: make it dynamically configurable
                for (var i = 0; i < 4; i ++) {
                    var previewParent = $("<div class='col-md-3 preview-parent'></div>");
                    var previewDiv = $("<div class='preview-div'></div>");
                    var fileName = $("<div class='file-name'>SampleConfiguration.bal</div>");
                    var previewName = $("<div class='preview-name-div'></div>");
                    previewName.append(fileName);

                    previewParent.append(previewDiv);
                    previewParent.append(previewName);

                    recentPreviewSection.append(previewParent);
                }

                openRecentSection.append(openRecentHeading);
                openRecentSection.append(recentPreviewSection);

                var templatesSection = $("<div class='open-template-section'></div>");
                var templatesHeading = $("<div class='open-template-heading'>Try out our samples / Templates</div>");
                var templatePreviewSection = $("<div class='template-preview-section'></div>");

                // Dynamically add the preview divs for recent open diagrams
                // TODO: make it dynamically configurable
                for (var i = 0; i < 4; i ++) {
                    var previewParent = $("<div class='col-md-3 preview-parent'></div>");
                    var previewDiv = $("<div class='preview-div'></div>");
                    var fileName = $("<div class='file-name'>SampleConfiguration.bal</div>");
                    var previewName = $("<div class='preview-name-div'></div>");
                    previewName.append(fileName);

                    previewParent.append(previewDiv);
                    previewParent.append(previewName);

                    templatePreviewSection.append(previewParent);
                }

                templatesSection.append(templatesHeading);
                templatesSection.append(templatePreviewSection);

                var separator = $("<div class='separator'><hr></div>");

                rightPanel.append(openRecentSection);
                rightPanel.append(separator);
                rightPanel.append(templatesSection);


                outerContainer.append(leftPanel);
                outerContainer.append(rightPanel);
                this._$container.append(outerContainer);
                var containerId = this._$container;

                var commandManager = this.commandManager;
                (newButton.find('.btn-new')).click(function () {
                    $(containerId).show();
                    $(".regular-welcome-screen-container").hide();
                    commandManager.dispatch('create-new-tab');
                });

                //Hiding menu bar
                this._options.application.menuBar.hide();
            }
        });

    return RegularWelcomeScreenView;
});

