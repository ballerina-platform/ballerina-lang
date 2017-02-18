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
define(['require', 'jquery', 'backbone', './tool'], function (require, $, Backbone, Tool) {

    var toolGroup = Backbone.Model.extend({
        initialize: function (attributes) {
            this.tools = [];
            var self = this;
            _.forEach( attributes.toolDefinitions,  function(toolDefinition){
                    self.addTool(toolDefinition);
                }
            );
        },

        addTool: function (definition) {
            var newTool = new Tool(definition);
            this.tools.push(newTool);
            this.trigger('tool-added', newTool);
        },

        /**
         * remove given tool item from the tool group
         * @param {Tool} tool - tool to be removed
         */
        removeTool: function (tool) {
            _.remove(this.tools, function (toolItem) {
                return _.isEqual(toolItem.id, tool.id);
            });
            this.trigger('tool-removed', tool.id);
        },

        /**
         * remove given tool item from the tool group
         * @param {string} toolId - tool ID of the tool to be removed.
         */
        removeToolByToolId: function (toolId) {
            _.remove(this.tools, function (toolItem) {
                return _.isEqual(toolItem.id, toolId);
            });
            this.trigger('tool-removed', toolId);
        },

        /**
         * updates the tool with provided new values
         * @param {Object} tool - tool to be updated
         * @param {Object} newValue - new value to update the tool with
         */
        updateTool: function (tool, attribute, newValue, metaAttr) {
            var selectedTool = _.find(this.tools, function (toolItem) {
                return _.isEqual(toolItem.get('id'), tool.getId());
            });
            if (!_.isNil(newValue)) {
                switch (attribute) {
                  case 'name':
                    selectedTool.setName(newValue);
                    selectedTool.setId(newValue);
                    break;

                  case 'params':
                    selectedTool.setParams(newValue);
                    break;

                  default:

                }

                if(!_.isUndefined(metaAttr)){
                    selectedTool.attributes.meta[metaAttr] = newValue;
                }
            }
        },

        modelName: "ToolGroup",

        defaults: {
            toolGroupID: "id-not-set",
            toolGroupName: "",
            toolGroup: ""
        }
    });

    return toolGroup;
});

