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
define(['require','log', 'jquery', 'd3', 'backbone', './tool-view'], function (require, log, $, d3, Backbone, ToolView) {

    var toolGroupView = Backbone.View.extend({

        initialize: function (options) {
            log.debug("toolGroupview init");
            this._options = options;
            _.extend(this, _.pick(options, ["toolPalette"]));
        },

        render: function (parent) {
            var self = this;
            var groupDiv = $('<div></div>');
            parent.append(groupDiv);
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
            groupCollapseIcon.attr('class', "collapse-icon glyphicon glyphicon-chevron-down");

            var groupBodyDiv = $("<div></div>");
            groupDiv.append(groupBodyDiv);
            groupBodyDiv.attr('class', "tool-group-body");
            this._$toolGroupBody = groupBodyDiv;

            this.model.tools.forEach(function (tool) {
                var toolOptions = _.clone(_.get(self._options, 'tool'));
                _.set(toolOptions, 'toolPalette', self.toolPalette);
                _.set(toolOptions, 'model', tool);
                var toolView = new ToolView(toolOptions);
                toolView.render(groupBodyDiv);
            });

            this.el =  groupDiv[0].outerHTML;
            this.$el = groupDiv;

            groupHeaderDiv.click(function(){
                groupHeaderDiv.toggleClass("tool-group-header-collapse");
                groupBodyDiv.slideToggle(500, function () {
                        groupCollapseIcon.toggleClass("glyphicon-chevron-up")
                                            .toggleClass("glyphicon-chevron-down");
                    });
            });
            this.model.on('tool-added', this.onToolAdded, this);
            return this;
        },

        onToolAdded: function (tool) {
            var self = this;
            if (!_.isUndefined(self._$toolGroupBody)) {
                var toolOptions = _.clone(_.get(self._options, 'tool'));
                _.set(toolOptions, 'toolPalette', self.toolPalette);
                _.set(toolOptions, 'model', tool);
                var toolView = new ToolView(toolOptions);
                toolView.render(self._$toolGroupBody);
            }
        }
    });

    return toolGroupView;

});
