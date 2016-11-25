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
define(['require', 'log', 'jquery', 'backbone', 'command', 'ballerina'],
    function (require, log, $, Backbone, CommandManager, Service) {

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

            passedFirstLaunch: function(){
                return this._options.application.browserStorage.get("pref:passedFirstLaunch") || false;
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
                samplesDiv.attr('id', 'theCarousel');
                samplesDiv.addClass('carousel slide multi-item-carousel');

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

                // Adding carousel view related elements
                var carouselDiv = $('<div></div>');
                carouselDiv.attr('id', "innerSamples");
                samplesDiv.append(carouselDiv);
                var nextControl = $('<a></a>');
                nextControl.addClass('right carousel-control');
                nextControl.attr('href', '#theCarousel').attr('data-slide', 'next');
                var nextIcon = $('<i></i>');
                nextIcon.addClass('fw fw-right right-carousel');
                nextControl.append(nextIcon);

                var prevControl = $('<a></a>');
                prevControl.addClass('left carousel-control');
                prevControl.attr('href', '#theCarousel').attr('data-slide', 'prev');
                var prevIcon = $('<i></i>');
                prevIcon.addClass('fw fw-left left-carousel');
                prevControl.append(prevIcon);

                samplesDiv.append(nextControl);
                samplesDiv.append(prevControl);

                //TODO: Need to iterate file list when implementation is complete
                for (var i = 0; i < 4; i++) {
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
                    var servicePreview = new Service.Views.ServicePreview(config);
                    servicePreview.render();

                }
                // class added after rendering to fix issue in firefox
                carouselDiv.addClass("carousel-inner");
                // initialise carousel
                $('.multi-item-carousel').carousel({
                    interval: false
                });
                // Carousel only shows the 'active' item in it. To show multiple items in the same slide, each next item is cloned.
                $('.carousel .item').each(function () {
                    var next = $(this).next();
                    if (!next.length) {
                        next = $(this).siblings(':first');
                    }
                    next.children(':first-child').clone().appendTo($(this));

                    for (var i = 1; i < 3; i++) {
                        next = next.next();
                        if (!next.length) {
                            next = $(this).siblings(':first');
                        }

                        next.children(':first-child').clone().appendTo($(this));
                    }
                });

                var command = this._options.application.commandManager;
                var browserStorage = this._options.application.browserStorage;

                $(newButton).on('click', function () {
                    command.dispatch("create-new-tab");
                    browserStorage.put("pref:passedFirstLaunch", true);
                });
            }

        });

        return InitialWelcomePage;

    });

