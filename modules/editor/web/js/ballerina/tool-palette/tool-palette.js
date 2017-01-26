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
        './drag-drop-manager', './../ast/ballerina-ast-factory','./initial-definitions',
        './../search/search', './../search/import-search-adapter', 'mousetrap' ],
    function (require, log, $, Backbone, ToolGroupView, ToolGroup,
              DragDropManager, BallerinaASTFactory, initialTools, Search, ImportSearchAdapter, Mousetrap) {

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
            this.ballerinaFileEditor = options.ballerinaFileEditor;
            this._toolGroups = _.cloneDeep(initialTools);
            this._imports = [];
            this.dragDropManager = new DragDropManager();

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

            var toolGroupOptions = _.clone(_.get(self._options, 'toolGroup'));
            _.set(toolGroupOptions, 'toolPalette', self);
            _.set(toolGroupOptions, 'model', this._toolGroups.elements);
            var groupView = new ToolGroupView(toolGroupOptions);
            groupView.render(self.$el, false);
            this.$el.addClass('non-user-selectable');

            var toolGroupOptions = _.clone(_.get(self._options, 'toolGroup'));
            _.set(toolGroupOptions, 'toolPalette', self);
            _.set(toolGroupOptions, 'model', this._toolGroups.statements);
            var groupView = new ToolGroupView(toolGroupOptions);
            groupView.render(self.$el, false);
            this.$el.addClass('non-user-selectable');   

            if(this._toolGroups.package.tools.length != 0){
                var toolGroupOptions = _.clone(_.get(self._options, 'toolGroup'));
                _.set(toolGroupOptions, 'toolPalette', self);
                _.set(toolGroupOptions, 'model', this._toolGroups.package);
                var groupView = new ToolGroupView(toolGroupOptions);
                groupView.render(self.$el, true);
                this.$el.addClass('non-user-selectable');
            }  

            var importForm = $('<div class="tool-import-wrapper">'+
                               '<div class="tool-group-import-header">'+
                               '  <a class="tool-group-header-title">Imports</a> ( Ctrl + I )'+
                               '  <span id="addImportSearch" class="tool-import-icon fw-stack fw-lg">'+
                               '      <i class="fw fw-square fw-stack-2x"></i>'+
                               '      <i class="fw fw-add fw-stack-1x fw-inverse"></i>'+
                               '  </span>'+
                               '</div>'+
                               '</div>');
            this.$el.append(importForm);
            
            this._toolGroups.imports.forEach(function (package){
                self.addImport(package);
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

        /**
         * Adds a package to a tool palette.
         * @param package {Object} Package Object.
         */
        addImport: function(package){
            var import_pkg = package;
            if(_.find(this._imports, function(_import){ return (_import.getName() == import_pkg.getName()) }) != undefined){
                return false;
            }            

            var definitions = [];
            _.forEach(package.getConnectorDefinitions() , function(connector) {
                connector.nodeFactoryMethod = BallerinaASTFactory.createConnectorDeclaration();
                connector.meta = {
                    connectorName: connector.getName(),
                    connectorPackageName: import_pkg.name
                };               
                definitions.push(connector);
                if(connector['actions'] != undefined){
                    _.forEach(connector.actions , function(action ,index, collection){
                        /* We need to add a special class to actions to indent them in tool palette. */
                        action.classNames = "tool-connector-action";
                        if( (index + 1 ) == collection.length){
                            action.classNames = "tool-connector-action tool-connector-last-action";
                        }
                        action.nodeFactoryMethod = BallerinaASTFactory.createAggregatedActionInvocationExpression
                        definitions.push(action);
                    });
                }
            });

            this._imports.push(package);
            var group = new ToolGroup({
                toolGroupName: package.getName(),
                toolGroupID: package.getName() + "-tool-group",
                toolOrder: "vertical",
                toolDefinitions: definitions
            });
            
            var toolGroupOptions = _.clone(_.get(this._options, 'toolGroup'));
            _.set(toolGroupOptions, 'toolPalette', this);
            _.set(toolGroupOptions, 'model', group);
            var groupView = new ToolGroupView(toolGroupOptions);
            var group = groupView.render(this.$el.find('.tool-import-wrapper'), _.isEqual('vertical', group.get('toolOrder')));
            this.$el.addClass('non-user-selectable');

            this.ballerinaFileEditor.importPackage(package.name);
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
