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
        'main_elements', 'processors', './drag-drop-manager'],
    function (require, log, $, Backbone, ToolGroupView, ToolGroup,
              MainElements, Processors, DragDropManager) {

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
                errMsg = 'unable to find container for tab list with selector: ' + _.get(options, 'container');
                log.error(errMsg);
                throw errMsg;
            }
            this._$parent_el = container;
            this._options = options;
            this._initTools();
            this.dragDropManager = new DragDropManager();
        },

        _initTools: function(){

            var _toolGroups = [];
            // Create main tool group
            var mainToolGroup = new ToolGroup({
                toolGroupName: "Elements",
                toolGroupID: "main-tool-group",
                toolDefinitions: MainElements.lifelines
            });
            _toolGroups.push(mainToolGroup);

            // Create mediators tool group
            var mediatorsToolGroup = new ToolGroup({
                toolGroupName: "Mediators",
                toolGroupID: "mediators-tool-group",
                toolDefinitions: _.assign({},Processors.manipulators, Processors.flowControllers)
            });
            _toolGroups.push(mediatorsToolGroup);

            this._toolGroups = _toolGroups;
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
            this._toolGroups.forEach(function (group) {
                var groupView = new ToolGroupView({model: group, toolPalette: self});
                groupView.render(self.$el);
                self.$el.addClass('non-user-selectable');
            });

            return this;
        }
    });

    return ToolPalette;
});
