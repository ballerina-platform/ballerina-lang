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
define(['require', 'backbone', 'lodash','ballerina'], function ( require, Backbone, _, Ballerina) {

    var RegularWelcomePage = Backbone.View.extend(
        /** @lends RegularWelcomePage.prototype */
        {
            /**
             * @augments
             * @constructs
             * @class RegularWelcomePage Represents the view for Regular welcome screen.
             * @param {Object} options Rendering options for the view
             */
            initialize: function (options) {
                var errMsg;
                if (!_.has(options, 'tab')) {
                    errMsg = 'unable to find a reference for editor tab';
                    log.error(errMsg);
                    throw errMsg;
                }
                this._tab = _.get(options, 'tab');
                this.commandManager = options.commandManager;
                this._$container = $(this._tab.getContentContainer());
                // make sure default tab content are cleared
                this._$container.empty();
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
                var outerContainer = $("<div class='regular-welcome-screen-container'></div>");
                var leftPanel = $("<div class='col-md-2 reg-welcome-left'></div>");
                var rightPanel = $("<div class='col-md-10 reg-welcome-right'></div>");
                var leftLogoContainer = $(
                    "<div style='padding-top: 75px'>" +
                    "<img src='images/wso2-logo.jpg' width='33%' style='margin: auto; display: block'>" +
                    "<div style='width: 55%; font-size: 40px; margin: 0px 27px';>Ballerina</div>" +
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
                    var previewParent = $("<div class='col-xs-3 preview-parent'></div>");
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
                var templatePreviewSection = $("<div class='carousel slide regular-item-carousel template-preview-section'></div>");
                templatePreviewSection.attr('id', 'regularWelcomeCarousel');
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

                // Adding carousel view related elements
                var carouselDiv = $('<div></div>');
                carouselDiv.attr('id', "innerSamples");
                templatePreviewSection.append(carouselDiv);
                var nextControl = $('<a></a>');
                nextControl.addClass('right carousel-control regular-welcome-right-control');
                nextControl.attr('href', '#regularWelcomeCarousel').attr('data-slide', 'next');
                var nextIcon = $('<i></i>');
                nextIcon.addClass('fw fw-right right-carousel');
                nextControl.append(nextIcon);

                var prevControl = $('<a></a>');
                prevControl.addClass('left carousel-control regular-welcome-left-control');
                prevControl.attr('href', '#regularWelcomeCarousel').attr('data-slide', 'prev');
                var prevIcon = $('<i></i>');
                prevIcon.addClass('fw fw-left left-carousel');
                prevControl.append(prevIcon);
                templatePreviewSection.append(prevControl);
                templatePreviewSection.append(nextControl);

///TODO: Need to iterate file list when implementation is complete
                for (var i = 0; i < 2; i++) {
                    if (i == 0) {
                        var config =
                        {
                            "sampleName": "SampleConfiguration.bal",
                            "parentContainer": "#innerSamples",
                            "firstItem": true
                        }
                    }
                    else {
                        var config =
                        {
                            "sampleName": "SampleConfiguration.bal",
                            "parentContainer": "#innerSamples"
                        }
                    }
                    //FIXME: fix previews
                    var servicePreview = Ballerina.viewsModule.ServicePreviewView(config);

                    //var servicePreview = new Service.Views.ServicePreview(config);
                    //servicePreview.render();

                }
                // class added after rendering to fix issue in firefox
                carouselDiv.addClass("carousel-inner");
                $('.regular-item-carousel').carousel({
                    interval: false
                });



                var commandManager = this.commandManager;
                (newButton.find('.btn-new')).click(function () {
                    $(containerId).show();
                    $(".regular-welcome-screen-container").hide();
                    commandManager.dispatch('create-new-tab');
                });

                this.$el = outerContainer;
            }
        });

    return RegularWelcomePage;
});

