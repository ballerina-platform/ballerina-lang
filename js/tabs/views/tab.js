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
define(['lodash', 'backbone'], function(_, Backbone){

    // View for each tab item (<li> item)
    var TabView = Backbone.View.extend({
        //create <li> element to hold each resource tab
        tagName: "li",
        className: "series ",
        template: _.template($('#resourceTabsTemplate').html()),
        initialize: function () {


        },
        events: {
            "click a": "clickedResourceTab",
            'click .remove-resource': 'removeResourceTab',
        },
        // on click of added tab
        clickedResourceTab: function (e) {
            e.preventDefault();
            currentTabId = this.model.get("hrefId");
            var currentTab = this.model;
            //Unique Id created for the svg element where elements can be drawn
            var svgUId = this.model.get("resourceId") + "4";

            if (diagram.selectedOptionsGroup) {
                diagram.selectedOptionsGroup.classed("option-menu-hide", true);
                diagram.selectedOptionsGroup.classed("option-menu-show", false);
            }
            diagram.selectedOptionsGroup = null;
            if (diagram.propertyWindow) {
                diagram.propertyWindow = false;
                defaultView.enableDragZoomOptions();
                $('#property-pane-svg').empty();
            }
            //first time click on the tab
            if (this.model.attributes.createdTab === false) {
                // get the diagram model for this tab
                // var dgModel = this.model.getDiagramOfTab(currentTab.attributes.diagramForTab.models[0].cid);
                // dgModel.CurrentDiagram(dgModel);
                // var options = {selector: currentTabId, wrapperId: svgUId};
                // // get the current diagram view for the tab
                // var currentView = dgModel.createDiagramView(dgModel, options);
                // // set current tab's diagram view as default view
                // currentView.currentDiagramView(currentView);
                // this.model.setDiagramViewForTab(currentView);
                // // mark tab as visited
                // this.model.setSelectedTab();
            }
            else {
                // not the first time click on the given tab
                var dgViewToRender = this.model.viewObj;
                dgViewToRender.currentDiagramView(dgViewToRender);
                //Setting diagram model for lifeline message drawing context
                lifeLineOptions.diagram = defaultView.model;
                currentTab.preview().render();
            }

        },
        //Remove tab and tab content on 'remove' button
        removeResourceTab: function (e) {
            e.preventDefault();
            if (diagram.selectedOptionsGroup) {
                diagram.selectedOptionsGroup.classed("option-menu-hide", true);
                diagram.selectedOptionsGroup.classed("option-menu-show", false);
            }
            diagram.selectedOptionsGroup = null;
            if (diagram.propertyWindow) {
                diagram.propertyWindow = false;
                defaultView.enableDragZoomOptions();
                $('#property-pane-svg').empty();
            }
            var anchor = $(e.currentTarget).siblings('a');
            $(anchor.attr('href')).remove();
            $(e.currentTarget).parent().remove();
        },

        render: function () {
            var html = this.template(this.model.attributes);
            $(this.el).append(html);
            var tabContent = new TabContentView({model: this.model});
            tabContent.render();

        }
    });
    return TabView;
});