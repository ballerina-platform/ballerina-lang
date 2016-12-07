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
define(['lodash', 'log', 'd3', 'jquery', 'd3utils', 'app/diagram-core/models/point', './life-line'],
    function (_, log, d3, $, D3utils, Point, LifeLine) {

        /**
         * The view for the resource definition model.
         * @param model Resource definition model.
         * @param container The SVG container.
         * @param viewOptions Options to configure the view.
         * @constructor
         */
        var ResourceDefinitionView = function (args) {
            this._model =  _.get(args, 'model', null);
            this._viewOptions =  _.get(args, 'viewOptions', {});
            this._container = _.get(args, 'container', null);
            if (!_.isNil(this._model) && !_.isNil(this._container)) {

                // Center point of the resource
                this._viewOptions.centerPoint = _.get(args, "viewOptions.centerPoint", {});
                this._viewOptions.centerPoint.x = _.get(args, "viewOptions.centerPoint.x", 50);
                this._viewOptions.centerPoint.y = _.get(args, "viewOptions.centerPoint.y", 100);

                // View options for height and width of the heading box.
                this._viewOptions.heading = _.get(args, "viewOptions.heading", {});
                this._viewOptions.heading.hight = _.get(args, "viewOptions.heading.height", 25);
                this._viewOptions.heading.width = _.get(args, "viewOptions.heading.width", 1000);

                // View options for height and width of the resource icon in the heading box.
                this._viewOptions.heading.icon = _.get(args, "viewOptions.heading.icon", {});
                this._viewOptions.heading.icon.height = _.get(args, "viewOptions.heading.icon.height", 25);
                this._viewOptions.heading.icon.width = _.get(args, "viewOptions.heading.icon.width", 25);

                this._viewOptions.contentCollapsed = _.get(args, "viewOptions.contentCollapsed", false);
                this._viewOptions.contentWidth = _.get(args, "viewOptions.contentWidth", 1000);
                this._viewOptions.contentHeight = _.get(args, "viewOptions.contentHeight", 200);


            } else {
                log.error("Invalid args received for creating a resource definition view. Model: " + this._model +
                    ". Container: " + this._container);
            }
        };

        ResourceDefinitionView.prototype.constructor = ResourceDefinitionView;

        ResourceDefinitionView.prototype.setModel = function (model) {
            if (!_.isNil(model)) {
                this._model = model;
            } else {
                log.error("Unknown definition received for resource definition.");
            }
        };

        ResourceDefinitionView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("SVG container for the resource is null or empty.");
            }
        };

        ResourceDefinitionView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        ResourceDefinitionView.prototype.getModel = function () {
            return this._model;
        };

        ResourceDefinitionView.prototype.getContainer = function () {
            return this._container;
        };

        ResourceDefinitionView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        ResourceDefinitionView.prototype.render = function () {
            // Render resource view
            var svgContainer = $(this._container).children()[0];
            var headingStart = new Point(this._viewOptions.centerPoint.x, this._viewOptions.centerPoint.y);
            var contentStart = new Point(this._viewOptions.centerPoint.x, this._viewOptions.centerPoint.y + this._viewOptions.heading.hight);

            var headerGroup = D3utils.group(d3.select(svgContainer));

            var headingRect = D3utils.rect(headingStart.x(), headingStart.y(), this._viewOptions.heading.width, this._viewOptions.heading.hight, 0, 0, headerGroup);
            // TODO: Move these styling to css
            headingRect.attr('fill', "#FFFFFF");
            headingRect.attr('stroke-width', "1");
            headingRect.attr('stroke', "#cbcbcb");

            // Drawing resource icon
            // TODO : FIX
            var headingRectIcon = D3utils.rect(headingStart.x(), headingStart.y(), this._viewOptions.heading.icon.width,
                this._viewOptions.heading.icon.height, 0, 0, headerGroup).classed("fw fw-dgm-service fw-inverse");

            // Create rect for the http method text
            var httpMethodRect = D3utils.rect(headingStart.x() + this._viewOptions.heading.icon.width, headingStart.y() + 0.5, this._viewOptions.heading.icon.width + 25,
                this._viewOptions.heading.icon.height - 1, 0, 0, headerGroup);
            httpMethodRect.attr('fill', "#f3f3f3");

            // Set HTTP Method
            var httpMethodText = D3utils.textElement(headingStart.x() + this._viewOptions.heading.icon.width + 5, headingStart.y() + 4, this._model.getResourceMethod(), headerGroup);
            httpMethodText.attr('dominant-baseline', "text-before-edge");
            httpMethodText.attr("fill", "#777777");

            // Setting resource path prefix
            var resourcePathPrefix = D3utils.textElement(headingStart.x() + this._viewOptions.heading.icon.width + 55, headingStart.y() + 4, "Path: ", headerGroup);
            resourcePathPrefix.attr('dominant-baseline', "text-before-edge");
            resourcePathPrefix.attr('stroke-width', 0.5);
            resourcePathPrefix.attr('stroke', "#000000");

            var resourcePath = D3utils.textElement(headingStart.x() + this._viewOptions.heading.icon.width + 90, headingStart.y() + 4, this._model.getResourcePath(), headerGroup);
            resourcePath.attr('dominant-baseline', "text-before-edge");

            var contentRect = D3utils.rect(contentStart.x(), contentStart.y(), this._viewOptions.contentWidth, this._viewOptions.contentHeight, 0, 0, d3.select(svgContainer));
            // TODO: Move these styling to css
            contentRect.attr('fill', "#FFFFFF");
            contentRect.attr('stroke-width', "1");
            contentRect.attr('stroke', "#cbcbcb");

            // Move up the resource content rect before client lifeline so that the client lifeline will go through the
            // rect.
            var contentRectNode = $(contentRect.node());
            $(svgContainer).prepend(contentRectNode);

            // Drawing default worker
            var defaultWorkerOptions = {
                "editable": true,
                "centerPoint": {
                    "x": 180,
                    "y": 150
                },
                "class": "lifeline",
                "polygon": {
                    "shape" : "rect",
                    "width": 120,
                    "height": 30,
                    "roundX": 0,
                    "roundY": 0,
                    "class": "lifeline-polygon"
                },
                "droppableRect": {
                    "width": 100,
                    "height": 300,
                    "roundX": 0,
                    "roundY": 0,
                    "class": "lifeline-droppableRect"
                },
                "line": {
                    "height": 100,
                    "class": "lifeline-line"
                },
                "text": {
                    "value": "Resource Worker",
                    "class": "lifeline-text"
                }
            };
            var defaultWorker = new LifeLine(svgContainer, defaultWorkerOptions);
            defaultWorker.render();



            log.debug("Rendering Resource View");
        };

        return ResourceDefinitionView;

    });
