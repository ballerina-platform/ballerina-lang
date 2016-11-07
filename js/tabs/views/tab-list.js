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
define(['jquery', 'lodash', 'backbone', 'app/tabs/models/tab'], function($, _, Backbone, Tab){

    var TabListView = Backbone.View.extend({
        el: '#tabList',
        initialize: function () {
        },

        events: {
            "click .add-resource": "addResourceTab"
        },
        render: function (model) {
            var tabView = new TabView({model: model});
            tabView.render();
            var t = $(this.el)[0].childNodes[1].childNodes[0];
            $(t).closest("li").before(tabView.el);
            // $(this.el).append(tabView.el);

        },
        //function to fire when a new resource tab button is clicked
        addResourceTab: function (e) {
            // required to clean previous views
            this.undelegateEvents();
            e.preventDefault();
            //create Unique id for each tab
            var id =  Math.random().toString(36).substr(2, 9);
            var hrefId = '#seq_' + id;
            var resourceId = 'seq_' + id;
            //create new Tab based resource model:todo: change resource title
            var resourceModel = new Tab({
                resourceId: resourceId,
                hrefId: hrefId,
                resourceTitle: "New Resource" ,
                createdTab: false
            });

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

            var nextTabListView = new TabListView({model: resourceModel});

            nextTabListView.render(resourceModel);
            //create new diagram object for the tab
            var diagramObj = new Diagrams.Models.Diagram({});
            resourceModel.addDiagramForTab(diagramObj);
            //Activating tab on creation itself
            $('.tabList a[href="#' + resourceId + '"]').tab('show');
            var dgModel = resourceModel.getDiagramOfTab(resourceModel.attributes.diagramForTab.models[0].cid);
            dgModel.CurrentDiagram(dgModel);
            var svgUId = resourceId + "4";
            var options = {selector: hrefId, wrapperId: svgUId};
            // get the current diagram view for the tab
            var currentView = dgModel.createDiagramView(dgModel, options);
            // add diagramModel
            var preview = new DiagramOutlineView({mainView: currentView});
            preview.render();
            resourceModel.preview(preview);
            // set current tab's diagram view as default view
            currentView.currentDiagramView(currentView);
            resourceModel.setDiagramViewForTab(currentView);
            // mark tab as visited
            resourceModel.setSelectedTab();
            currentView.renderMainElement("Source", 1, MainElements.lifelines.SourceLifeline);
            currentView.model.sourceLifeLineCounter(1);
            currentView.renderMainElement("Resource", 1, MainElements.lifelines.ResourceLifeline);
            currentView.model.resourceLifeLineCounter(1);
            // first arrow creation between source and resource
            var currentSource = currentView.model.diagramSourceElements().models[0];
            var currentResource = currentView.model.diagramResourceElements().models[0];
            this.drawInitArrow(currentSource,currentResource,currentView);
        },

        //Draw initial arrow between the source and resource element
        drawInitArrow:function(source,destination,diagramView){
            centerS = createPoint(200, 50);
            centerR = createPoint(380, 50);
            var sourcePoint = new SequenceD.Models.MessagePoint({
                model: {type: "messagePoint"},
                x: centerS.x(),
                y: centerS.y(),
                direction: "outbound"
            });
            var destinationPoint = new SequenceD.Models.MessagePoint({
                model: {type: "messagePoint"},
                x: centerR.x(),
                y: centerR.y(),
                direction: "inbound"
            });
            var messageLink = new SequenceD.Models.MessageLink({
                source: sourcePoint,
                destination: destinationPoint
            });
            var messageOptionsInbound = {'class': 'messagePoint', 'direction': 'inbound'};
            var messageOptionsOutbound = {'class': 'messagePoint', 'direction': 'outbound'};
            source.addChild(sourcePoint, messageOptionsOutbound);
            destination.addChild(destinationPoint, messageOptionsInbound);
            diagramView.render();
        }

    });
    return TabListView;
});