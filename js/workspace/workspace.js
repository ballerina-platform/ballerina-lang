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
            app.tabController.newTab();
        };
        //The parent div provided should have following struct:
        //<parent> <div class="carousel-inner><item><div:preview-parent></div></item></div></parent>
        this.createCarouselView = function createCarouselView(parent) {
            parent.attr('id', 'theCarousel');
            parent.addClass('carousel slide multi-item-carousel');

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

            parent.append(nextControl);
            parent.append(prevControl);
            var firstItem = parent[0].children[0].children[0];
            $(firstItem).addClass('active');

            // Instantiate the Bootstrap carousel
            $('.multi-item-carousel').carousel({
                interval: false
            });
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

        };

        this.popupRegularWelcomeScreen = function () {
            // hide the page content and only the regular welcome screen will be shown
            $(app.config.container).hide();
        };

        app.commandManager.registerCommand("create-new-tab", {key: ""});
        app.commandManager.registerHandler('create-new-tab', this.createNewTab);
        app.commandManager.registerCommand("create-carousel-view");
        app.commandManager.registerHandler('create-carousel-view', this.createCarouselView);

    }


});

