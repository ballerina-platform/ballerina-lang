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
define(['lodash', 'log', 'd3', 'jquery', 'd3utils', 'app/diagram-core/models/point'],
    function (_, log, d3, $, d3utils, Point) {

        /**
         * The view for the resource definition model.
         * @param model Resource definition model.
         * @param container The SVG container.
         * @param viewOptions Options to configure the view.
         * @constructor
         */
        var ResourceDefinitionView = function (model, container, viewOptions) {
            if (!_.isNil(model) && !_.isNil(container)) {
                this._model = model;
                this._container = container;
                this._viewOptions = viewOptions;
            } else {
                log.error("Invalid args received for creating a resource definition. Model: " + model + ". Container: " +
                    container);
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
            var headingStart = new Point(200, 50);
            var contentStart = new Point(200, 75);

            var headingRect = d3utils.rect(headingStart.x(), headingStart.y(), 1000, 25, 0, 0, d3.select(svgContainer));
            // TODO: Move these styling to css
            headingRect.attr('fill', "#FFFFFF");
            headingRect.attr('stroke-width', "1");
            headingRect.attr('stroke', "#000000");
            var contentRect = d3utils.rect(contentStart.x(), contentStart.y(), 1000, 200, 0, 0, d3.select(svgContainer));
            // TODO: Move these styling to css
            contentRect.attr('fill', "#FFFFFF");
            contentRect.attr('stroke-width', "1");
            contentRect.attr('stroke', "#000000");
            log.debug("Rendering Resource View");
        };

        return ResourceDefinitionView

    });
