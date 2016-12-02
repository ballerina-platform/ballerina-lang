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
define(['log', 'lodash', 'jquery', 'd3', 'event_channel', 'd3utils'], function(log, _, $, d3, EventChannel, D3Utils){

    var Canvas = function(model, container, viewOptions) {

        viewOptions.diagram = viewOptions.diagram || {};
        viewOptions.diagram.height = viewOptions.diagram.height || "100%";
        viewOptions.diagram.width = viewOptions.diagram.width || "100%";
        viewOptions.diagram.padding =  viewOptions.diagram.padding || 50;
        viewOptions.diagram.viewBoxWidth =  viewOptions.diagram.viewBoxWidth || 1000;
        viewOptions.diagram.viewBoxHeight =  viewOptions.diagram.viewBoxHeight || 1000;

        viewOptions.diagram.class = viewOptions.diagram.class || "diagram";
        viewOptions.diagram.selector = viewOptions.diagram.selector || ".diagram";
        viewOptions.diagram.wrapper = viewOptions.diagram.wrapper ||{};
        // CHANGED
        viewOptions.diagram.wrapperId = viewOptions.wrapperId || "diagramWrapper";
        viewOptions.diagram.grid = viewOptions.diagram.grid || {};
        viewOptions.diagram.grid.height = viewOptions.diagram.grid.height || 25;
        viewOptions.diagram.grid.width = viewOptions.diagram.grid.width || 25;
        this.viewOptions = viewOptions;

        this.model = model;
        this.container = container;

        if (_.isUndefined(this.container)) {
            log.error("container is not defined");
        }

        var d3Container = d3.select(this.container);
        // wrap d3 with custom drawing apis
        d3Container = D3Utils.decorate(d3Container);
        var svg = d3Container.draw.svg(this.viewOptions.diagram);
        this._definitions = svg.append("defs");
        this._svg = svg;

        this._mainSVGGroup = this.d3svg.draw.group(this._svg).attr("id", this.viewOptions.diagram.wrapperId)
            .attr("width", "100%")
            .attr("height", "100%");
    };

    Canvas.prototype = Object.create(EventChannel.prototype);
    Canvas.prototype.constructor = Canvas;

    Canvas.prototype.getSVG = function () {
        return this._svg;
    };

    Canvas.prototype.getMainWrapper = function () {
        return this._mainSVGGroup;
    };

    return Canvas;

});