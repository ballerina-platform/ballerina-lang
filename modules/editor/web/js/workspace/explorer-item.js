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

define(['lodash', 'log', 'file_browser', 'event_channel'],
    function (_, log, FileBrowser, EventChannel){

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

        header.append(arrowHeadIcon);
        header.append(folderIcon);
        header.append(folderName);
        item.append(header);
        item.append(body);
        this.container.append(item);

        header.click(function(){
            arrowHeadIcon.toggleClass("fw-rotate-90");
        });

        var fileBrowser = new FileBrowser({container: body, application: this.application, root: this.path,
            fetchFiles: true});
        fileBrowser.render();
        fileBrowser.on("double-click-node", function(node){
            if(_.isEqual('file', node.type)){
                this.application.commandManager.dispatch("open-file", node.id);
            }
        }, this);
        this._fileBrowser = fileBrowser;
    };

    return ExplorerItem;
        
});