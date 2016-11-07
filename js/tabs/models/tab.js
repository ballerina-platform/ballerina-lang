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

    // Model for Tab based resources
    var Tab = Backbone.Model.extend({
        modelName: "Tab",

        initialize: function (attrs, options) {
            //store diagram object of a given tab
            var diagramSet = new DiagramsList([]);
            this.diagramForTab(diagramSet);
            this.viewObj = {};

        },
        // mark already visited tabs
        setSelectedTab: function () {
            this.attributes.createdTab = true;
        },
        getSelectedTab: function () {
            return this.createdTab;
        },
        setDiagramViewForTab: function (view) {
            this.viewObj = view;
        },

        // diagram collection getters/setters
        diagramForTab: function (element) {
            if (_.isUndefined(element)) {
                return this.get('diagramForTab');
            } else {
                this.set('diagramForTab', element);
            }
        },
        // on new tab creation add the diagram object to list
        addDiagramForTab: function (element) {

            this.diagramForTab().add(element);
        },
        //get the diagram object from the collection
        getDiagramOfTab: function (id) {
            return this.diagramForTab().get(id);
        },

        preview: function(preview){
            if(_.isUndefined(preview)){
                return this.get("preview");
            }
            this.set("preview", preview);
        },

        defaults: {
            resourceId: "id-not-set",
            resourceTitle: "",
            hrefId: "id-not-set",
            createdTab: false,
        }
    });

    return Tab;
});