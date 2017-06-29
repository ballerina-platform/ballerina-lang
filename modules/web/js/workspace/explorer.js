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

import log from 'log';
import $ from 'jquery';
import Backbone from 'backbone';
import _ from 'lodash';
import ExplorerItem from './explorer-item';
import ServiceClient from './service-client';
import ContextMenu from 'context_menu';
import 'mcustom_scroller';

const WorkspaceExplorer = Backbone.View.extend({

    initialize(config) {
        let errMsg;
        if (!_.has(config, 'container')) {
            errMsg = 'unable to find configuration for container';
            log.error(errMsg);
            throw errMsg;
        }
        const container = $(_.get(config, 'container'));
        // check whether container element exists in dom
        if (!container.length > 0) {
            errMsg = `unable to find container for file browser with selector: ${_.get(config, 'container')}`;
            log.error(errMsg);
            throw errMsg;
        }
        this._$parent_el = container;

        if (!_.has(config, 'application')) {
            log.error('Cannot init file browser. config: application not found.');
        }
        this.application = _.get(config, 'application');
        this._options = config;
        this.workspaceServiceURL = _.get(this._options, 'application.config.services.workspace.endpoint');
        this._lastWidth = undefined;
        this._verticalSeparator = $(_.get(this._options, 'separator'));
        this._containerToAdjust = $(_.get(this._options, 'containerToAdjust'));
        this._openedFolders = this.application.browserStorage.get('file-explorer:openedFolders') || [];
        this._items = [];

        this._serviceClient = new ServiceClient({ application: this.application });

        // register command
        this.application.commandManager.registerCommand(config.command.id, { shortcuts: config.command.shortcuts });
        this.application.commandManager.registerHandler(config.command.id, this.toggleExplorer, this);

        this.application.commandManager.registerCommand('open-folder', {});
        this.application.commandManager.registerHandler('open-folder', this.openFolder, this);

        this.application.commandManager.registerCommand('open-file', {});
        this.application.commandManager.registerHandler('open-file', this.openFile, this);

        this.application.commandManager.registerHandler('remove-explorer-item', this.removeExplorerItem, this);
    },

    openFolder(folderPath) {
        const exist = _.includes(this._openedFolders, folderPath);
        if (!exist) {
            this._openedFolders.push(folderPath);
            this.createExplorerItem(folderPath);
            this.persistState();
        }
    },

    openFile(filePath) {
        const file = this._serviceClient.readFile(filePath);
        const currentTabForFile = this.application.tabController.getTabForFile(file);
        if (!_.isNil(currentTabForFile)) {
            this.application.tabController.setActiveTab(currentTabForFile);
            return;
        }
        this.application.commandManager.dispatch('create-new-tab', { tabOptions: { file } });
    },

    createExplorerItem(folderPath) {
        this._openFolderBtn.hide();
        const opts = {};
        _.set(opts, 'application', this.application);
        _.set(opts, 'path', folderPath);
        _.set(opts, 'index', this._items.length - 1);
        _.set(opts, 'container', this._explorerContainer);
        const explorerItem = new ExplorerItem(opts);
        explorerItem.render();
        this._items.push(explorerItem);
    },

    removeExplorerItem(item) {
        item.remove();
        _.remove(this._items, itemEntry => _.isEqual(itemEntry.path, item.path));
        _.remove(this._openedFolders, path => _.isEqual(path, item.path));
        if (_.isEmpty(this._openedFolders)) {
            this._openFolderBtn.show();
        }
        this.persistState();
    },

    persistState() {
        this.application.browserStorage.put('file-explorer:openedFolders', this._openedFolders);
    },

    isEmpty() {
        return _.isEmpty(this._openedFolders);
    },

    isActive() {
        return this._activateBtn.parent('li').hasClass('active');
    },

    toggleExplorer() {
        if (this.isActive()) {
            this._$parent_el.parent().width('0px');
            this._containerToAdjust.css('padding-left', _.get(this._options, 'leftOffset'));
            this._verticalSeparator.css('left', _.get(this._options, 'leftOffset') - _.get(this._options, 'separatorOffset'));
            this._activateBtn.parent('li').removeClass('active');
            this.application.reRender(); // to update the diagrams
        } else {
            this._activateBtn.tab('show');
            const width = this._lastWidth || _.get(this._options, 'defaultWidth');
            this._$parent_el.parent().width(width);
            this._containerToAdjust.css('padding-left', width);
            this._verticalSeparator.css('left', width - _.get(this._options, 'separatorOffset'));
            this.application.reRender(); // to update the diagrams
        }
    },

    render() {
        const self = this;
        const activateBtn = $(_.get(this._options, 'activateBtn'));
        this._activateBtn = activateBtn;

        const explorerContainer = $('<div role="tabpanel"></div>');
        explorerContainer.addClass(_.get(this._options, 'cssClass.container'));
        explorerContainer.attr('id', _.get(this._options, ('containerId')));
        this._$parent_el.append(explorerContainer);

        activateBtn.on('click', function (e) {
            $(this).tooltip('hide');
            e.preventDefault();
            e.stopPropagation();
            self.application.commandManager.dispatch(_.get(self._options, 'command.id'));
        });

        activateBtn.attr('data-placement', 'bottom').attr('data-container', 'body');

        if (this.application.isRunningOnMacOS()) {
            activateBtn.attr('title', `Open File Explorer (${
             _.get(self._options, 'command.shortcuts.mac.label')}) `).tooltip();
        } else {
            activateBtn.attr('title', `Open File Explorer  (${
            _.get(self._options, 'command.shortcuts.other.label')}) `).tooltip();
        }

        this._verticalSeparator.draggable();
        this._verticalSeparator.on('drag', (event) => {
            if (event.clientX >= _.get(self._options, 'resizeLimits.minX')
                && event.clientX <= _.get(self._options, 'resizeLimits.maxX')) {
                self._verticalSeparator.css('left', event.clientX);
                self._verticalSeparator.css('cursor', 'ew-resize');
                const newWidth = event.clientX;
                self._$parent_el.parent().width(newWidth);
                self._containerToAdjust.css('padding-left', event.clientX);
                self._lastWidth = newWidth;
                self._isActive = true;
            } else {
                // cannot allow dragging vertical seperator since it reached resizeLimits
                event.preventDefault();
                event.stopPropagation();
            }
        });

        this._explorerContainer = explorerContainer;
        const openFolderBtn = $(
            `<div class="open-folder-btn-wrapper">
               <span class="open-folder-button">
                 <i class="fw fw-folder-open"></i>Open Program Directory
               </span>
            </div>`
        );
        openFolderBtn.click(() => {
            this.application.commandManager.dispatch('show-folder-open-dialog');
        });
        explorerContainer.append(openFolderBtn);
        this._openFolderBtn = openFolderBtn;

        this._contextMenu = new ContextMenu({
            container: this._$parent_el,
            selector: 'div:first',
            items: {
                add_folder: {
                    name: 'add program directory',
                    icon: '',
                    callback() {
                        self.application.commandManager.dispatch('show-folder-open-dialog');
                    },
                },
            },
        });
        explorerContainer.mCustomScrollbar({
            theme: 'minimal',
            scrollInertia: 0,
            axis: 'xy',
        });
        if (!_.isEmpty(this._openedFolders)) {
            this._openedFolders.forEach((folder) => {
                self.createExplorerItem(folder);
            });
            openFolderBtn.hide();
        }
        return this;
    },
});

export default WorkspaceExplorer;
