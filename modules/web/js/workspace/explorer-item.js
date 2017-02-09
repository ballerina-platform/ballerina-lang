/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

define(['lodash', 'log', 'file_browser', 'event_channel', 'context_menu', 'bootstrap'],
    function (_, log, FileBrowser, EventChannel, ContextMenu){

    var ExplorerItem = function(args){
        _.assign(this, args);
    };

    ExplorerItem.prototype = Object.create(EventChannel.prototype);
    ExplorerItem.prototype.constructor = ExplorerItem;

    ExplorerItem.prototype.getFolderName = function (folderPath) {
            var splitArr = _.split(folderPath, this.application.getPathSeperator());
            return _.gt(_.last(splitArr).length, 0) ? _.last(splitArr) :
                    _.nth(splitArr, splitArr.length - 2);
    };

    ExplorerItem.prototype.render = function(){
        var item = $('<div class="folder-tree"><div>'),
            folderName = $("<span>" + this.getFolderName(this.path) +  "</span>"),
            id = "folder-tree_" + this.index,
            header = $('<div class="folder-tree-header" role="button" href="#' + id +
                '"+ data-toggle="collapse" aria-expanded="true" aria-controls="' +
                id + '"></div>'),
            body = $('<div class="collapse folder-tree-body" id="' + id +
                '"></div>'),
            folderIcon = $("<i class='fw fw-folder item-icon'></i>"),
            arrowHeadIcon = $("<i class='fw fw-right expand-icon'></i>");

        header.attr("id", this.path);
        header.append(arrowHeadIcon);
        header.append(folderIcon);
        header.append(folderName);
        item.append(header);
        item.append(body);
        this.container.find('.mCSB_container').append(item); //add to mscroller container 
        this._itemElement = item;

        header.attr('title', this.path);
        header.tooltip({
            'delay': { show: 1000, hide: 0 },
            'placement': 'bottom',
            'container': 'body'
        });

        body.on('show.bs.collapse', function(){
            arrowHeadIcon.addClass("fw-rotate-90");
        });

        body.on('hide.bs.collapse', function(){
            arrowHeadIcon.removeClass("fw-rotate-90");
        });

        var fileBrowser = new FileBrowser({
            container: body,
            application: this.application, root: this.path,
            fetchFiles: true
        });
        fileBrowser.render();
        fileBrowser.on("double-click-node", function(node){
            if(_.isEqual('file', node.type)){
                this.application.commandManager.dispatch("open-file", node.id);
            }
        }, this);

        this._contextMenu = new ContextMenu({
            container: item,
            selector: ".folder-tree-header, li",
            provider: this.createContextMenuProvider()
        });
        this._fileBrowser = fileBrowser;
    };

    ExplorerItem.prototype.remove = function(){
        this._itemElement.remove();
    };

    ExplorerItem.prototype.createContextMenuProvider = function(){
        var self = this;
        return function($trigger, e) {
            var items = {},
                menu = {items: items},
                isRoot = $trigger.hasClass("folder-tree-header"),
                path = $trigger.attr('id'),
                node = isRoot ? self._fileBrowser.getNode('#') : self._fileBrowser.getNode(path);

            if(isRoot || _.isEqual('folder', node.type)){
                items.createNewFile = {
                    name: "new file",
                    icon: "",
                    callback: function () {
                        self.application.commandManager.dispatch("create-new-item-at-path",
                            {
                                path: path,
                                type: 'ballerina-file',
                                onSuccess: function(){
                                    self._fileBrowser.refresh(node);
                                }
                            });
                    }
                };
                items.createNewFolder = {
                    name: "new folder",
                    icon: "",
                    callback: function () {
                        self.application.commandManager.dispatch("create-new-item-at-path",
                            {
                                path: path,
                                type: 'folder',
                                onSuccess: function(){
                                    self._fileBrowser.refresh(node);
                                }
                            });
                    }
                };
                items.refreshBtn = {
                    name: "refresh",
                    icon: "",
                    callback: function () {
                        self._fileBrowser.refresh(node);
                    }
                };
                items.deleteFolder = {
                    name: "delete",
                    icon: "",
                    callback: function () {
                        self.application.commandManager.dispatch("remove-from-disk",
                            {
                                type: "folder",
                                path: path,
                                onSuccess: function(){
                                    if(isRoot){
                                        self.application.commandManager.dispatch("remove-explorer-item", self);
                                    } else {
                                        self._fileBrowser.refresh(node.parent);
                                    }
                                }
                            });
                    }
                }
            }
            else if(_.isEqual('file', node.type)){
                items.deleteFile = {
                    name: "delete",
                    icon: "",
                    callback: function () {
                        self.application.commandManager.dispatch("remove-from-disk",
                            {
                                type: "file",
                                path: node.id,
                                onSuccess: function(){
                                    self._fileBrowser.refresh(node.parent);
                                }
                            });
                    }
                }
            }

            if(isRoot){
                items.removeFolderFromExplorer = {
                    name: "remove folder",
                    icon: "",
                    callback: function () {
                        self.application.commandManager.dispatch("remove-explorer-item", self);
                    }
                };
            }
            return menu;
        };
    };

    return ExplorerItem;
        
});