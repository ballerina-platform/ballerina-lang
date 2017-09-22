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
import { PLUGIN_ID, VIEWS as VIEW_IDS, DIALOGS as DIALOG_IDS,
    HISTORY, EVENTS, COMMANDS as COMMAND_IDS, TOOLS as TOOL_IDS } from './constants';

import WorkspaceExplorer from './views/WorkspaceExplorer';
import FileOpenDialog from './dialogs/FileOpenDialog';
import FolderOpenDialog from './dialogs/FolderOpenDialog';
import FileSaveDialog from './dialogs/FileSaveDialog';
import { read } from './fs-util';
import File from './model/file';

const skipEventSerialization = (key, value) => {
    return key === '_events' ? undefined : value;
};

/**
 * Workspace Plugin is responsible for managing workspace.
 *
 * @class WorkspacePlugin
 */
class WorkspacePlugin extends Plugin {

    /**
     * @inheritdoc
     */
    constructor(props) {
        super(props);
        this.openedFolders = [];
        this.openedFiles = [];
        this._selectedNodeInExplorer = undefined;
        this.onWorkspaceFileUpdated = this.onWorkspaceFileUpdated.bind(this);
        this.onNodeSelectedInExplorer = this.onNodeSelectedInExplorer.bind(this);
    }

    get selectedNodeInExplorer() {
        return this._selectedNodeInExplorer;
    }

    set selectedNodeInExplorer(newT) {
        this._selectedNodeInExplorer = newT;
    }

    /**
     * @inheritdoc
     */
    getID() {
        return PLUGIN_ID;
    }

    /**
     * On an opened file in workspace update
     */
    onWorkspaceFileUpdated() {
        const { pref: { history } } = this.appContext;
        history.put(HISTORY.OPENED_FILES, this.openedFiles, skipEventSerialization);
    }

    /**
     * Upon tree node selection in explorer
     */
    onNodeSelectedInExplorer(node) {
        if (!_.isNil(this.selectedNodeInExplorer) && (node.id !== this.selectedNodeInExplorer.id)) {
            this.selectedNodeInExplorer.active = false;
        }
        this.selectedNodeInExplorer = node;
        this.reRender();
    }

    /**
     * Opens a file using related editor
     *
     * @param {String} filePath Path of the file.
     * @param {String} type type of the file.
     * @return {Promise} Resolves or reject with error.
     */
    openFile(filePath, type = 'bal') {
        return new Promise((resolve, reject) => {
            // if not already opened
            if (_.findIndex(this.openedFiles, file => file === filePath) === -1) {
                read(filePath)
                    .then((file) => {
                        file.extension = type;
                        this.openedFiles.push(file);
                        const { pref: { history }, editor } = this.appContext;
                        history.put(HISTORY.OPENED_FILES, this.openedFiles, skipEventSerialization);
                        file.on(EVENTS.FILE_UPDATED, this.onWorkspaceFileUpdated);
                        editor.open(file);
                        resolve(file);
                    })
                    .catch((err) => {
                        reject(JSON.stringify(err));
                    });
            } else {
                reject(`File ${filePath} is already opened.`);
            }
        });
    }

    /**
     * Create a new file and opens it in a new tab
     */
    createNewFile() {
        const newFile = new File({});
        this.openedFiles.push(newFile);
        const { pref: { history }, editor } = this.appContext;
        history.put(HISTORY.OPENED_FILES, this.openedFiles, skipEventSerialization);
        newFile.on(EVENTS.FILE_UPDATED, this.onWorkspaceFileUpdated);
        editor.open(newFile);
    }

    /**
     * Close an opened file
     *
     * @param {String} filePath Path of the file.
     * @return {Promise} Resolves or reject with error.
     */
    closeFile(file) {
        return new Promise((resolve, reject) => {
            if (this.openedFiles.includes(file)) {
                _.remove(this.openedFiles, file);
                const { pref: { history } } = this.appContext;
                history.put(HISTORY.OPENED_FILES, this.openedFiles, skipEventSerialization);
                file.off(EVENTS.FILE_UPDATED, this.onWorkspaceFileUpdated);
            } else {
                reject(`File ${file.fullPath} cannot be found in opened file set.`);
            }
        });
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
                this.reRender();
            }
            resolve();
        });
    }

    /**
     * Remove an opened folder from explorer
     *
     * @param {String} folderPath Path of the folder.
     * @return {Promise} Resolves or reject with error.
     */
    removeFolder(folderPath) {
        return new Promise((resolve, reject) => {
            if (_.findIndex(this.openedFolders, folder => folder === folderPath) !== -1) {
                _.remove(this.openedFolders, folder => folder === folderPath);
                const { pref: { history } } = this.appContext;
                history.put(HISTORY.OPENED_FOLDERS, this.openedFolders);
                this.reRender();
            }
            resolve();
        });
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
            removeFolder: this.removeFolder.bind(this),
        };
    }

    /**
     * @inheritdoc
     */
    activate(appContext) {
        super.activate(appContext);
        const { pref: { history } } = appContext;
        this.openedFolders = history.get(HISTORY.OPENED_FOLDERS) || [];
        // make File objects for each serialized file
        const serializedFiles = history.get(HISTORY.OPENED_FILES) || [];
        this.openedFiles = serializedFiles.map((serializedFile) => {
            return Object.assign(new File({}), serializedFile);
        });
    }

    /**
     * @inheritdoc
     */
    onAfterInitialRender() {
        const { editor } = this.appContext;
        this.openedFiles.forEach((file) => {
            file.on(EVENTS.FILE_UPDATED, this.onWorkspaceFileUpdated);
            // no need to activate this editor
            // as this is loading from history.
            // Editor plugin will decide which editor
            // to activate depending on editor tabs history
            editor.open(file, false);
        });
    }

    /**
     * @inheritdoc
     */
    getContributions() {
        const { COMMANDS, HANDLERS, MENUS, VIEWS, DIALOGS, TOOLS } = CONTRIBUTIONS;
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
                                icon: 'add',
                                isActive: () => {
                                    return true;
                                },
                                handleAction: () => {
                                    const { command: { dispatch } } = this.appContext;
                                    dispatch(COMMAND_IDS.SHOW_FOLDER_OPEN_WIZARD, {});
                                },
                            },
                            {
                                icon: 'refresh',
                                isActive: () => {
                                    return true;
                                },
                                handleAction: () => {
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
                {
                    id: DIALOG_IDS.SAVE_FILE,
                    component: FileSaveDialog,
                    propsProvider: () => {
                        return {
                            workspaceManager: this,
                        };
                    },
                },
            ],
            [TOOLS]: [
                {
                    id: TOOL_IDS.NEW_FILE,
                    group: TOOL_IDS.GROUP,
                    icon: 'blank-document',
                    commandID: COMMAND_IDS.CREATE_NEW_FILE,
                },
                {
                    id: TOOL_IDS.OPEN_FILE,
                    group: TOOL_IDS.GROUP,
                    icon: 'folder-open',
                    commandID: COMMAND_IDS.SHOW_FILE_OPEN_WIZARD,
                },
                {
                    id: TOOL_IDS.SAVE_FILE,
                    group: TOOL_IDS.GROUP,
                    icon: 'save',
                    commandID: COMMAND_IDS.SAVE_FILE,
                    isActive: () => {
                        const { editor } = this.appContext;
                        const activeEditor = editor.getActiveEditor();
                        if (activeEditor && !_.isNil(activeEditor.file)) {
                            return activeEditor.file.isDirty;
                        }
                        return false;
                    },
                },
            ],
        };
    }
}

export default WorkspacePlugin;
