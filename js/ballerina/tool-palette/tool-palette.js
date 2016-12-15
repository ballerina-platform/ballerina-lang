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
define(['require', 'log', 'jquery', 'backbone', './tool-group-view', './tool-group',
        'main_elements', 'processors', './drag-drop-manager', './../ast/ballerina-ast-factory','./initial-definitions'],
    function (require, log, $, Backbone, ToolGroupView, ToolGroup,
              MainElements, Processors, DragDropManager, BallerinaASTFactory, initialTools) {

    var ToolPalette = Backbone.View.extend({
        initialize: function (options) {
            var errMsg;
            if (!_.has(options, 'container')) {
                errMsg = 'unable to find configuration for container';
                log.error(errMsg);
                throw errMsg;
            }
            var container = $(_.get(options, 'container'));
            // check whether container element exists in dom
            if (!container.length > 0) {
                errMsg = 'unable to find container for tool palette with selector: ' + _.get(options, 'container');
                log.error(errMsg);
                throw errMsg;
            }
            this._$parent_el = container;
            this._options = options;
            this._toolGroups = _.cloneDeep(initialTools);
            this.dragDropManager = new DragDropManager();
        },

        render: function () {
            var self = this;
            var toolPaletteDiv = $('<div></div>');
            //Adding search bar to tool-palette
            var searchBarDiv = $('<div></div>');
            searchBarDiv.addClass(_.get(this._options, 'search_bar.cssClass.search_box'));
            var searchInput = $('<input>');
            searchInput.addClass(_.get(this._options, 'search_bar.cssClass.search_input'));
            searchInput.attr('id','search-field').attr('placeholder','Search').attr('type','text');
            var searchIcon = $('<i></i>');
            searchIcon.addClass(_.get(this._options, 'search_bar.cssClass.search_icon'));
            searchBarDiv.append(searchIcon);
            searchBarDiv.append(searchInput);
            toolPaletteDiv.append(searchBarDiv);
            // End of adding search bar
            this._$parent_el.append(toolPaletteDiv);
            this.$el = toolPaletteDiv;

            this._toolGroups.forEach(function (group){
                var toolGroupOptions = _.clone(_.get(self._options, 'toolGroup'));
                _.set(toolGroupOptions, 'toolPalette', self);
                _.set(toolGroupOptions, 'model', group);
                var groupView = new ToolGroupView(toolGroupOptions);
                groupView.render(self.$el);
                self.$el.addClass('non-user-selectable');
            });
            return this;
        },

        /**
         * Dynamically loads avaiable tools from a package
         *
         * @param packageModel {Package}
         */
        loadToolsFromPackage: function(packageModel){

        },

        /**
         * Adds a new tool to a particular tool group
         * @param groupID {String} ID of the tool group to which the tool should be added.
         * @param toolDef {Object} Tool definition
         * @param toolDef.id {String} Tool ID
         * @param toolDef.name {String} Tool Name
         * @param toolDef.icon {String} Path to SVG Icon
         * @param toolDef.title {String} Tooltip text
         * @param toolDef.nodeFactoryMethod {Function} Factory method to create new node upon drag.
         *
         */
        addNewToolToGroup: function(groupID, toolDef){
           var error,
               toolGroup = _.find(this._toolGroups, function(group){
               return _.isEqual(group.get('toolGroupID'), groupID);
           });
           if(_.isNil(toolGroup)){
               error = 'cannot find a tool group with id ' + groupID;
               log.error(error);
               return;
           }
           if(!_.isNil(toolDef)){
               toolGroup.addTool(toolDef);
           }
        },

        addConnectorTool: function(toolDef){
            this.addNewToolToGroup('connectors-tool-group', toolDef);
        },

        hide: function () {
            this._$parent_el.hide();
        },

        show: function () {
            this._$parent_el.show();
        }
    });

    return ToolPalette;
});
