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
import log from 'log';
import $ from 'jquery';
import * as d3 from 'd3';
import Backbone from 'backbone';
import ToolView from './tool-view';

var toolGroupView = Backbone.View.extend({

    initialize: function (options) {
        log.debug("toolGroupview init");
        _.set(options, 'animationTime', 200);
        this._options = options;
        _.extend(this, _.pick(options, ["toolPalette"]));
    },

    render: function (parent, toolOrderVertical, addToTop, collapsed, gridConfig) {
        var self = this;
        var groupDiv = $('<div></div>');

        if(addToTop) {
            parent.prepend(groupDiv);
        } else {
            parent.append(groupDiv);
        }

        groupDiv.attr('id', "tool-group-" + this.model.attributes.toolGroupID);
        groupDiv.attr('class', "tool-group");

        var groupHeaderDiv = $("<div></div>");
        groupDiv.append(groupHeaderDiv);
        groupHeaderDiv.attr('class', "tool-group-header");

        var groupTitle = $("<a></a>");
        groupHeaderDiv.append(groupTitle);
        groupTitle.attr('class', "tool-group-header-title")
                .text(this.model.attributes.toolGroupName);

        var groupCollapseIcon = $("<span></span>");
        groupHeaderDiv.append(groupCollapseIcon);
        groupCollapseIcon.attr('class', "collapse-icon fw fw-up");

        var groupBodyDiv = $("<div></div>");
        groupDiv.append(groupBodyDiv);
        groupBodyDiv.attr('class', "tool-group-body tool-group-body-list");
        this._$toolGroupBody = groupBodyDiv;

        if(this.model.attributes.gridConfig){
            var toolsViewModeControlsDiv = $("<div class='tools-view-modes-controls clearfix'></div>");
            groupBodyDiv.append(toolsViewModeControlsDiv);

            var groupTilesIcon = $("<a class='tool-group-tiles-view'></a>");
            toolsViewModeControlsDiv.append(groupTilesIcon);
            groupTilesIcon.attr('class', "collapse-icon fw fw-tiles");
            groupTilesIcon.click(function(){
                $(this).parents('.tool-group').find('.tool-group-body')
                        .attr("class", "tool-group-body tool-group-body-tiles");
                return false;
            });

            var groupGridIcon = $("<a class='tool-group-action-grid'></a>");
            toolsViewModeControlsDiv.append(groupGridIcon);
            groupGridIcon.attr('class', "collapse-icon fw fw-grid");
            groupGridIcon.click(function(){
                $(this).parents('.tool-group').find('.tool-group-body')
                        .attr("class", "tool-group-body tool-group-body-grid");
                return false;
            });

            var groupListIcon = $("<a class='tool-group-list-view'></a>");
            toolsViewModeControlsDiv.append(groupListIcon);
            groupListIcon.attr('class', "collapse-icon fw fw-list");
            groupListIcon.click(function(){
                $(this).parents('.tool-group').find('.tool-group-body')
                        .attr("class", "tool-group-body tool-group-body-list");
                return false;
            });
        }

        if(collapsed) {
            groupBodyDiv.hide();
            groupHeaderDiv.addClass('tool-group-header-collapse');
            groupCollapseIcon.removeClass('fw-up');
            groupCollapseIcon.removeClass("glyphicon-chevron-up");
            groupCollapseIcon.addClass('fw-down');
            groupCollapseIcon.toggleClass("glyphicon-chevron-down");
        }

        // Iterate and stop adding duplicates of each function
        var tools = [];
        var toolDocumentMap = {};
        if (toolOrderVertical) {
            // Check whether parameter type string available
            var isStringParamExist = function (parameters) {
                return _.find(parameters, function (param) {
                    return _.isEqual(param.type,"string");
                }) ? true : false;
            };

                // Replace the existing tool with tool which has string params.
            var replaceGivenToolWithExistingTool = function (replacement) {
                var index = _.findIndex(tools, function (tool) {
                    return _.isEqual(tool.id, replacement.id);
                });
                tools[index] = replacement;
            };

            _.forEach(this.model.tools, function (tool) {
                var id = "/" + tool.id + "/";
                if (!toolDocumentMap[id]) {
                    toolDocumentMap[id] = id;
                    tools.push(tool);
                } else if (toolDocumentMap[id] && isStringParamExist(tool.attributes._parameters)) {
                    replaceGivenToolWithExistingTool(tool);
                }
            });
        } else {
            tools = this.model.tools;
        }

        tools.forEach(function (tool) {
            var toolOptions = _.clone(_.get(self._options, 'tool'));
            _.set(toolOptions, 'toolPalette', self.toolPalette);
            _.set(toolOptions, 'model', tool);
            var toolView = new ToolView(toolOptions);
            toolView.render(groupBodyDiv, toolOrderVertical);
        });

        this.el = groupDiv[0].outerHTML;
        this.$el = groupDiv;

        groupHeaderDiv.click(function () {
            groupHeaderDiv.toggleClass("tool-group-header-collapse");
            groupBodyDiv.slideToggle(_.get(self._options, 'animationTime'), function () {
                if (groupHeaderDiv.hasClass("tool-group-header-collapse")) {
                    groupCollapseIcon.removeClass('fw-up');
                    groupCollapseIcon.removeClass("glyphicon-chevron-up");
                    groupCollapseIcon.addClass('fw-down');
                    groupCollapseIcon.toggleClass("glyphicon-chevron-down");
                } else {
                    groupCollapseIcon.removeClass('fw-down');
                    groupCollapseIcon.removeClass("glyphicon-chevron-down");
                    groupCollapseIcon.addClass('fw-up');
                    groupCollapseIcon.toggleClass("glyphicon-chevron-up");
                }
            });
        });
        this.model.on('tool-added', this.onToolAdded, this);
        this.model.on('tool-removed', this.onToolRemoved, this);

        return this;
    },

    onToolAdded: function (tool) {
        var self = this;
        if (!_.isUndefined(self._$toolGroupBody)) {
            var toolOptions = _.clone(_.get(self._options, 'tool'));
            _.set(toolOptions, 'toolPalette', self.toolPalette);
            _.set(toolOptions, 'model', tool);
            var toolView = new ToolView(toolOptions);
            toolView.render(self._$toolGroupBody, true);
        }
    },

    /**
     * function for removing given tool item from the tool palette view
     * @param {string} toolId
     */
    onToolRemoved: function (toolId) {
        var self = this;
        self._$toolGroupBody.find('#'+toolId).remove();
    }
});

export default toolGroupView;
