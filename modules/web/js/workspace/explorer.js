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

define(['log', 'jquery', 'backbone', 'lodash', './explorer-item', './service-client', 'context_menu', 'mcustom_scroller'],

    function (log, $, Backbone, _, ExplorerItem, ServiceClient, ContextMenu, mcustomScroller) {

    var WorkspaceExplorer = Backbone.View.extend({

        initialize: function (config) {
            var errMsg;
            if (!_.has(config, 'container')) {
                errMsg = 'unable to find configuration for container';
                log.error(errMsg);
                throw errMsg;
            }
            var container = $(_.get(config, 'container'));
            // check whether container element exists in dom
            if (!container.length > 0) {
                errMsg = 'unable to find container for file browser with selector: ' + _.get(config, 'container');
                log.error(errMsg);
                throw errMsg;
            }
            this._$parent_el = container;

            if(!_.has(config, 'application')){
                log.error('Cannot init file browser. config: application not found.')
            }
            this.application = _.get(config, 'application');
            this._options = config;
            this.workspaceServiceURL = _.get(this._options, 'application.config.services.workspace.endpoint');
            this._lastWidth = undefined;
            this._verticalSeparator = $(_.get(this._options, 'separator'));
            this._containerToAdjust = $(_.get(this._options, 'containerToAdjust'));
            this._openedFolders = this.application.browserStorage.get("file-explorer:openedFolders")||[];
            this._items = [];

            this._serviceClient = new ServiceClient({application: this.application});

            // register command
            this.application.commandManager.registerCommand(config.command.id, {shortcuts: config.command.shortcuts});
            this.application.commandManager.registerHandler(config.command.id, this.toggleExplorer, this);

            this.application.commandManager.registerCommand("open-folder", {});
            this.application.commandManager.registerHandler("open-folder", this.openFolder, this);

            this.application.commandManager.registerCommand("open-file", {});
            this.application.commandManager.registerHandler("open-file", this.openFile, this);

            this.application.commandManager.registerHandler("remove-explorer-item", this.removeExplorerItem, this);
        },

        openFolder: function(folderPath){
            this._openedFolders.push(folderPath);
            this.createExplorerItem(folderPath);
            this.persistState();
        },

        openFile: function(filePath){
            var file = this._serviceClient.readFile(filePath);
            var currentTabForFile = this.application.tabController.getTabForFile(file);
            if(!_.isNil(currentTabForFile)){
                this.application.tabController.setActiveTab(currentTabForFile);
                return;
            }
            this.application.commandManager.dispatch("create-new-tab", {tabOptions: {file: file}});
        },

        createExplorerItem: function(folderPath){
            var opts = {};
            _.set(opts, "application", this.application);
            _.set(opts, "path", folderPath);
            _.set(opts, "index", this._items.length - 1);
            _.set(opts, "container", this._explorerContainer);
            var explorerItem = new ExplorerItem(opts);
            explorerItem.render();
            this._items.push(explorerItem);
        },

        removeExplorerItem: function(item){
            item.remove();
            _.remove(this._items, function(itemEntry){
                return _.isEqual(itemEntry.path, item.path);
            });
            _.remove(this._openedFolders, function(path){
                return _.isEqual(path, item.path);
            });
            this.persistState();
        },

        persistState: function(){
            this.application.browserStorage.put("file-explorer:openedFolders", this._openedFolders);
        },

        isEmpty: function(){
              return _.isEmpty(this._openedFolders);
        },

        isActive: function(){
            return this._activateBtn.parent('li').hasClass('active');
        },

        toggleExplorer: function(){
            if(this.isActive()){
                this._$parent_el.parent().width('0px');
                this._containerToAdjust.css('padding-left', _.get(this._options, 'leftOffset'));
                this._verticalSeparator.css('left', _.get(this._options, 'leftOffset') - _.get(this._options, 'separatorOffset'));
                this._activateBtn.parent('li').removeClass('active');

            } else {
                this._activateBtn.tab('show');
                var width = this._lastWidth || _.get(this._options, 'defaultWidth');
                this._$parent_el.parent().width(width);
                this._containerToAdjust.css('padding-left', width);
                this._verticalSeparator.css('left',  width - _.get(this._options, 'separatorOffset'));
            }
        },

        render: function () {
            var self = this;
            var activateBtn = $(_.get(this._options, 'activateBtn'));
            this._activateBtn = activateBtn;

            var explorerContainer = $('<div role="tabpanel"></div>');
            explorerContainer.addClass(_.get(this._options, 'cssClass.container'));
            explorerContainer.attr('id', _.get(this._options, ('containerId')));
            this._$parent_el.append(explorerContainer);

            activateBtn.on('click', function(e){
                $(this).tooltip('hide');
                e.preventDefault();
                e.stopPropagation();
                self.application.commandManager.dispatch(_.get(self._options, 'command.id'));
            });

            activateBtn.attr("data-placement", "bottom").attr("data-container", "body");

            if (this.application.isRunningOnMacOS()) {
                activateBtn.attr("title", "Open File Explorer (" + _.get(self._options, 'command.shortcuts.mac.label') + ") ").tooltip();
            } else {
                activateBtn.attr("title", "Open File Explorer  (" + _.get(self._options, 'command.shortcuts.other.label') + ") ").tooltip();
            }

            this._verticalSeparator.on('drag', function(event){
                if( event.originalEvent.clientX >= _.get(self._options, 'resizeLimits.minX')
                    && event.originalEvent.clientX <= _.get(self._options, 'resizeLimits.maxX')){
                    self._verticalSeparator.css('left', event.originalEvent.clientX);
                    self._verticalSeparator.css('cursor', 'ew-resize');
                    var newWidth = event.originalEvent.clientX;
                    self._$parent_el.parent().width(newWidth);
                    self._containerToAdjust.css('padding-left', event.originalEvent.clientX);
                    self._lastWidth = newWidth;
                    self._isActive = true;
                }
                event.preventDefault();
                event.stopPropagation();
            });
            this._explorerContainer = explorerContainer;

            this._contextMenu = new ContextMenu({
                container: this._$parent_el,
                selector:  "div:first",
                items:{
                    "add_folder": {
                        name: "add folder",
                        icon: "",
                        callback: function () {
                            self.application.commandManager.dispatch("show-folder-open-dialog");
                        }
                    }
                }
            });
            explorerContainer.mCustomScrollbar({
                theme: "minimal",
                scrollInertia: 0,
                axis: "xy"
            });
            if(!_.isEmpty(this._openedFolders)){
                this._openedFolders.forEach(function(folder){
                    self.createExplorerItem(folder);
                });
            }
            return this;
        }
    });

    return WorkspaceExplorer;

});
