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
import _ from 'lodash';
import Plugin from './../plugin/plugin';
import { CONTRIBUTIONS } from './../plugin/constants';

import { REGIONS } from './../layout/constants';

import { getCommandDefinitions } from './commands';
import { getHandlerDefinitions } from './handlers';
import { getMenuDefinitions } from './menus';
import { PLUGIN_ID, VIEWS as VIEW_IDS, DIALOGS as DIALOG_IDS, HISTORY, EVENTS } from './constants';

import WorkspaceExplorer from './views/WorkspaceExplorer';
import FileOpenDialog from './dialogs/FileOpenDialog';
import FolderOpenDialog from './dialogs/FolderOpenDialog';
import { create, update, read, remove } from './fs-util';

/**
 * Workspace Manager is responsible for managing workspace.
 *
 * @class WorkspaceManagerPlugin
 */
class WorkspaceManagerPlugin extends Plugin {

    /**
     * @inheritdoc
     */
    constructor(props) {
        super(props);
        this.openedFolders = [];
        this.openedFiles = [];
    }

    /**
     * @inheritdoc
     */
    getID() {
        return PLUGIN_ID;
    }

    /**
     * Opens a file using related editor
     *
     * @param {String} filePath Path of the file.
     * @param {String} type type of the file.
     * @return {Promise} Resolves or reject with error.
     */
    openFile(filePath, type = 'bal') {
        return new Promise();
    }

    /**
     * Close an opened file
     *
     * @param {String} filePath Path of the file.
     * @return {Promise} Resolves or reject with error.
     */
    closeFile(filePath) {
        return new Promise();
    }

    /**
     * Opens a folder in explorer
     *
     * @param {String} folderPath Path of the folder.
     * @return {Promise} Resolves or reject with error.
     */
    openFolder(folderPath) {
        return new Promise((resolve, reject) => {
            // add path to opened folders list - if not added alreadt
            if (_.findIndex(this.openedFolders, folder => folder === folderPath) === -1) {
                this.openedFolders.push(folderPath);
                const { pref: { history } } = this.appContext;
                history.put(HISTORY.OPENED_FOLDERS, this.openedFolders);
                this.trigger(EVENTS.OPEN_FOLDER, folderPath);
            }
            resolve();
        });
    }

    /**
     * Close an opened folder from explorer
     *
     * @param {String} folderPath Path of the folder.
     * @return {Promise} Resolves or reject with error.
     */
    closeFolder(folderPath) {
        return new Promise();
    }

    /**
     * @inheritdoc
     */
    init(config) {
        super.init(config);
        return {
            openFile: this.openFile.bind(this),
            openFolder: this.openFolder.bind(this),
            closeFile: this.closeFile.bind(this),
            closeFolder: this.closeFolder.bind(this),
        };
    }

    /**
     * @inheritdoc
     */
    activate(appContext) {
        super.activate(appContext);
        const { pref: { history } } = appContext;
        this.openedFolders = history.get(HISTORY.OPENED_FOLDERS) || [];
    }

    /**
     * @inheritdoc
     */
    getContributions() {
        const { COMMANDS, HANDLERS, MENUS, VIEWS, DIALOGS } = CONTRIBUTIONS;
        return {
            [COMMANDS]: getCommandDefinitions(this),
            [HANDLERS]: getHandlerDefinitions(this),
            [MENUS]: getMenuDefinitions(this),
            [VIEWS]: [
                {
                    id: VIEW_IDS.EXPLORER,
                    component: WorkspaceExplorer,
                    propsProvider: () => {
                        return {
                            workspaceManager: this,
                        };
                    },
                    region: REGIONS.LEFT_PANEL,
                    // region specific options for left-panel views
                    regionOptions: {
                        activityBarIcon: 'file-browse',
                        panelTitle: 'Explorer',
                        panelActions: [
                            {
                                icon: '',
                                state: () => {

                                },
                            },
                        ],
                    },
                    displayOnLoad: true,
                },
            ],
            [DIALOGS]: [
                {
                    id: DIALOG_IDS.OPEN_FILE,
                    component: FileOpenDialog,
                    propsProvider: () => {
                        return {
                            workspaceManager: this,
                        };
                    },
                },
                {
                    id: DIALOG_IDS.OPEN_FOLDER,
                    component: FolderOpenDialog,
                    propsProvider: () => {
                        return {
                            workspaceManager: this,
                        };
                    },
                },
            ],
        };
    }
}

export default WorkspaceManagerPlugin;
