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
        './drag-drop-manager', './../search/search', './../search/import-search-adapter', 'mousetrap', 'mcustom_scroller'],
    function (require, log, $, Backbone, ToolGroupView, ToolGroup,
              DragDropManager, Search, ImportSearchAdapter, Mousetrap, mcustomScroller) {


    var ToolPalette = Backbone.View.extend({
        initialize: function (options) {
            var errMsg;
            if (!_.has(options, 'container')) {
                errMsg = 'unable to find configuration for container';
                log.error(errMsg);
                throw errMsg;
            }
            if (!_.has(options, 'itemProvider')) {
                errMsg = 'unable to find tool palette item provider';
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
            this.ballerinaFileEditor = options.ballerinaFileEditor;
            this._imports = [];
            this.dragDropManager = new DragDropManager();
            this._itemProvider = _.get(options, 'itemProvider');
            this._itemProvider.setToolPalette(this);

            this.search = Search(new ImportSearchAdapter());
            this.search.on('select', _.bindKey(this, 'addImport'));
            //bind event handlers
            this._$parent_el.on("click", "#addImportSearch", _.bindKey(this, 'showSearchImport'));
            Mousetrap.bind('ctrl+i', _.bindKey(this, 'showSearchImport'));

        },

        render: function () {
            var self = this;
            this._$parent_el.empty();
            var toolPaletteDiv = $('<div></div>');
            //Adding search bar to tool-palette
            var searchBarDiv = $('<div></div>');
            searchBarDiv.addClass(_.get(this._options, 'search_bar.cssClass.search_box'));
            var searchInput = $('<input>');
            // TODO: Need to enable this after the functionality implementation
            searchInput.attr('disabled','disabled');
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

            // Drawing initial set of tool groups
            _.forEach(this._itemProvider.getInitialToolGroups(), function (group) {
                var toolOrder = group.get('toolOrder');
                if(toolOrder === "horizontal"){
                    self.addHorizontallyFormattedToolGroup({group: group});
                }
            });

            var importForm = $('<div class="tool-import-wrapper">'+
                                '<div class="tool-group-import-header">'+
                                '  <a class="tool-group-header-title">Imports</a> '+
                                '  <span id="addImportSearch" class="tool-import-icon fw-stack fw-lg">'+
                                '      <i class="fw fw-add"></i>'+
                                '  </span>'+
                                '</div>'+
                                '</div>');
            this.$el.append(importForm);

            // Drawing tool groups that are added on the fly
            this._itemProvider.getDynamicToolGroups().forEach(function (group) {
                var view = self.addVerticallyFormattedToolGroup({group: group});
                self._itemProvider.saveImportToolGroupView(group.attributes.toolGroupName, view)
            });

            $(this._$parent_el).mCustomScrollbar({
                theme: "minimal-dark",
                scrollInertia: 0
            });
            return this;
        },


        showSearchImport : function(){
            var adapter = new ImportSearchAdapter();
            adapter.setExcludes(this._imports);
            this.search.setAdapter(adapter);
            this.search.show();
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
               toolGroup = _.find(this._itemProvider.getToolGroups(), function(group){
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

        /**
         * removes a tool from a particular tool group
         * @param {string} groupID - group ID of the tool group which the tool should be removed from
         * @param {string} toolId - tool ID of the tool to be removed
         */
        removeToolFromGroup: function (groupID, toolId) {
            var error,
                toolGroup = _.find(this._itemProvider.getToolGroups(), function(group){
                    return _.isEqual(group.get('toolGroupID'), groupID);
                });
            if(_.isNil(toolGroup)){
                error = 'cannot find a tool group with id ' + groupID;
                log.error(error);
                return;
            }
            if(!_.isNil(toolId)){
                toolGroup.removeToolByToolId(toolId);
            }
        },

        /**
         * Adding given package
         * @param {Object} package - package to add
         */
        //TODO: this method needs to be removed from tool palette class
        addImport: function (package) {
            this._itemProvider.addImportToolGroup(package);
        },

        /**
         * Adds a vertically formatted tool group view to this tool palette
         * @param {Object} args - data to draw the tool group
         * @param {Object} args.model - tool group model
         */
        addVerticallyFormattedToolGroup: function (args) {
            var toolGroupOptions = _.clone(_.get(this._options, 'toolGroup'));
            _.set(toolGroupOptions, 'toolPalette', this);
            _.set(toolGroupOptions, 'model', args.group);
            var groupView = new ToolGroupView(toolGroupOptions);

            var parent = this.$el.find('.tool-import-wrapper');
            var isVertical = _.isEqual('vertical', args.group.get('toolOrder'));

            var addToTop = false;
            var collapsed = true; // collapse by default
            if(!_.isNil(args.options)){
                addToTop = _.isNil(args.options.addToTop) ? false : args.options.addToTop;
                collapsed = _.isNil(args.options.collapsed) ? true : args.options.collapsed;
            }

            var group = groupView.render(parent, isVertical, addToTop, collapsed);
            this.$el.addClass('non-user-selectable');
            return groupView;
        },

        /**
         * Adds a horizontally formatted tool group view to this tool palette
         * @param {Object} args - data to draw the tool group
         * @param {Object} args.model - tool group model
         */
        addHorizontallyFormattedToolGroup: function (args) {
            var toolGroupOptions = _.clone(_.get(this._options, 'toolGroup'));
            _.set(toolGroupOptions, 'toolPalette', this);
            _.set(toolGroupOptions, 'model', args.group);
            var groupView = new ToolGroupView(toolGroupOptions);
            groupView.render(this.$el, false);
            this.$el.addClass('non-user-selectable');
        },

        addConnectorTool: function(toolDef){
            this.addNewToolToGroup('connectors-tool-group', toolDef);
        },

        hide: function () {
            this._$parent_el.hide();
        },

        show: function () {
            this._$parent_el.show();
        },

        /**
         * Returns the item provider associated with this tool palette
         * @returns {ToolPaletteItemProvider}
         */
        getItemProvider: function () {
            return this._itemProvider;
        },

        /**
         * updates tool item with given new values
         * @param {string} toolGroupID - Id of the tool group
         * @param {Object} toolItem - tool object
         * @param {Object} newValue - new value for the tool
         */
        updateToolPaletteItem: function (toolGroupID, toolItem, attribute, newValue, metaAttr) {
            var error,
                toolGroup = _.find(this._itemProvider.getToolGroups(), function (group) {
                    return _.isEqual(group.get('toolGroupID'), toolGroupID);
                });
            if (_.isNil(toolGroup)) {
                error = 'cannot find a tool group with id ' + toolGroupID;
                log.error(error);
                return;
            }
            if (!_.isNil(toolItem)) {
                toolGroup.updateTool(toolItem, attribute, newValue, metaAttr);
            }
        }
    });

    return ToolPalette;
});
