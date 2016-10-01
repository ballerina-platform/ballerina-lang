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

var Tools = (function (tools) {
    var views = tools.Views || {};

    var toolGroupWrapperView = Backbone.View.extend({

        initialize: function () {
            console.log("toolGroupWrapperView init");
        },

        render: function () {

            var groupDiv = $('<div></div>');
            groupDiv.attr('id', "tool-group-" + this.model.attributes.toolGroupID);
            groupDiv.attr('class', "tool-group");

            var groupHeaderDiv = $("<div></div>");
            groupHeaderDiv.attr('class', "tool-group-header");

            var groupTitle = $("<a></a>");
            groupTitle.attr('class', "tool-group-header-title")
                      .text(this.model.attributes.toolGroupName);

            var groupCollapseIcon = $("<span></span>");
            groupCollapseIcon.attr('class', "collapse-icon glyphicon glyphicon-chevron-down");

            groupHeaderDiv.append(groupTitle);
            groupHeaderDiv.append(groupCollapseIcon);
            groupDiv.append(groupHeaderDiv);

            var groupBodyDiv = $("<div></div>");
            groupBodyDiv.attr('class', "tool-group-body");
            var toolGroupView = new Tools.Views.ToolGroupView({collection: this.model.attributes.toolGroup});
            var groupViewHtml = toolGroupView.render().el;
            groupBodyDiv.html(groupViewHtml);

            groupDiv.append(groupBodyDiv);
            this.el =  groupDiv[0].outerHTML;
            this.$el = groupDiv;
            var groupID =  this.model.attributes.toolGroupID;

            $(document).ready(function(){
                $("#tool-group-" + groupID + " .tool-group-header").click(function(){
                    $("#tool-group-" + groupID + " .tool-group-body")
                        .slideToggle(500, function () {
                        $("#tool-group-" + groupID + " .tool-group-header .collapse-icon")
                            .toggleClass("glyphicon-chevron-up")
                            .toggleClass("glyphicon-chevron-down");
                    });
                });
            });

            return this;
        }
    });

    views.ToolGroupWrapperView = toolGroupWrapperView;
    tools.Views = views;
    return tools;

}(Tools || {}));
