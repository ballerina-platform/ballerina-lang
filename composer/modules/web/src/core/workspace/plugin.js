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
import log from 'log';
import Plugin from './../plugin/plugin';
import { CONTRIBUTIONS } from './../plugin/constants';
import { COMMANDS as EDITOR_COMMANDS } from './../editor/constants';

import { REGIONS, COMMANDS as LAYOUT_COMMANDS } from './../layout/constants';

import { getCommandDefinitions } from './commands';
import { getHandlerDefinitions } from './handlers';
import { getMenuDefinitions } from './menus';
import { PLUGIN_ID, VIEWS as VIEW_IDS, DIALOGS as DIALOG_IDS,
    HISTORY, EVENTS, COMMANDS as COMMAND_IDS, TOOLS as TOOL_IDS } from './constants';

import WorkspaceExplorer from './views/WorkspaceExplorer';
import FileOpenDialog from './dialogs/FileOpenDialog';
import FolderOpenDialog from './dialogs/FolderOpenDialog';
import FileSaveDialog from './dialogs/FileSaveDialog';
import FileReplaceConfirmDialog from './dialogs/FileReplaceConfirmDialog';
import FileDeleteConfirmDialog from './dialogs/FileDeleteConfirmDialog';

import { read } from './fs-util';
import File from './model/file';
import Folder from './model/folder';
import CreateProjectDialog from './dialogs/CreateProjectDialog';

import { isOnElectron } from './../utils/client-info';

// FIXME: Find a proper way of removing circular deps from serialization
const skipEventAndCustomPropsSerialization = (key, value) => {
    return key === '_events' || key === '_props' ? undefined : value;
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
        this.onWorkspaceFileContentChanged = this.onWorkspaceFileContentChanged.bind(this);
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
    onWorkspaceFileUpdated(file) {
        const { pref: { history } } = this.appContext;
        // prevent ast from saving to local storage
        const openedFiles = this.openedFiles.map((file) => {
            const newFile = _.clone(file);
            delete newFile._props.ast;
            return newFile;
        });
        history.put(HISTORY.OPENED_FILES, this.openedFiles, skipEventAndCustomPropsSerialization);
    }

    /**
     * On content of a file updated
     */
    onWorkspaceFileContentChanged({ file }) {
        const { command: { dispatch } } = this.appContext;
        dispatch(EVENTS.FILE_UPDATED, { file });
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
        const { editor } = this.appContext;
        if (editor.isFileOpenedInEditor(node.id)) {
            const targetEditor = editor.getEditorByID(node.id);
            editor.setActiveEditor(targetEditor);
        }
    }

    /**
     * Opens a file using related editor
     *
     * @param {String} filePath Path of the file.
     * @param {Boolean} activate activate upon open.
     *
     * @return {Promise} Resolves or reject with error.
     */
    openFile(filePath, activate = true) {
        return new Promise((resolve, reject) => {
            const indexInOpenedFiles = _.findIndex(this.openedFiles, file => file.fullPath === filePath);
            const { command: { dispatch } } = this.appContext;
            // if not already opened
            if (indexInOpenedFiles === -1) {
                read(filePath)
                    .then((file) => {
                        this.openedFiles.push(file);
                        const { pref: { history }, editor } = this.appContext;
                        history.put(HISTORY.OPENED_FILES, this.openedFiles, skipEventAndCustomPropsSerialization);
                        file.on(EVENTS.FILE_UPDATED, this.onWorkspaceFileUpdated);
                        file.on(EVENTS.CONTENT_MODIFIED, this.onWorkspaceFileContentChanged);
                        editor.open(file, activate);
                        dispatch(EVENTS.FILE_OPENED, { file });
                        resolve(file);
                    })
                    .catch((err) => {
                        reject(JSON.stringify(err));
                    });
            } else {
                dispatch(EDITOR_COMMANDS.ACTIVATE_EDITOR_FOR_FILE, {
                    filePath,
                });
                log.debug(`File ${filePath} is already opened.`);
                resolve(this.openedFiles[indexInOpenedFiles]);
            }
        });
    }

    /**
     * Refresh Path in Explorer, if it's already opened
     * @param {String} filePath Target File Path
     */
    refreshPathInExplorer(filePath) {
        const { command: { dispatch } } = this.appContext;
        dispatch(COMMAND_IDS.REFRESH_PATH_IN_EXPLORER, {
            filePath,
        });
    }

    /**
     * Go To Give File in Explorer, if it's already opened
     * @param {String} filePath Target File Path
     */
    goToFileInExplorer(filePath) {
        const { command: { dispatch } } = this.appContext;
        dispatch(COMMAND_IDS.GO_TO_FILE_IN_EXPLORER, {
            filePath,
        });
    }

    /**
     * Gets the parent folder opened in explorer for the given file
     * @param {String} filePath target file path
     */
    getExplorerFolderForPath(filePath) {
        return this.openedFolders.find((folder) => {
            return filePath.startsWith(folder.fullPath);
        });
    }

    /**
     * Checks whether the given path is opened in explorer
     * @param {String} filePath target path
     */
    isFilePathOpenedInExplorer(filePath) {
        return this.openedFolders.find((folder) => {
            return filePath.startsWith(folder.fullPath);
        }) !== undefined;
    }

    createNewProject() {
        console.log('aaaa');
    }

    /**
     * Create a new file and opens it in a new tab
     */
    createNewFile() {
        const { pref: { history }, editor, command: { dispatch } } = this.appContext;
        const supportedExts = editor.getSupportedExtensions();
        let extension;
        if (supportedExts.length === 1) {
            extension = 'bal';
        } else {
            // Right now, when the createNewFile is cmd is invoked through shortcut
            // or top menu, we create a bal file
            // creating custom file types are only supported through right click menu of explorer
            // TODO: provide user a choice on which type of file to create
            extension = 'bal';
        }
        const content = editor.getDefaultContent('temp.' + extension);
        const newFile = new File({ extension, content });
        this.openedFiles.push(newFile);
        history.put(HISTORY.OPENED_FILES, this.openedFiles, skipEventAndCustomPropsSerialization);
        newFile.on(EVENTS.FILE_UPDATED, this.onWorkspaceFileUpdated);
        newFile.on(EVENTS.CONTENT_MODIFIED, this.onWorkspaceFileContentChanged);
        editor.open(newFile);
        dispatch(EVENTS.FILE_OPENED, { file: newFile });
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
                const { pref: { history }, command: { dispatch } } = this.appContext;
                history.put(HISTORY.OPENED_FILES, this.openedFiles, skipEventAndCustomPropsSerialization);
                file.off(EVENTS.FILE_UPDATED, this.onWorkspaceFileUpdated);
                file.off(EVENTS.CONTENT_MODIFIED, this.onWorkspaceFileContentChanged);
                dispatch(EVENTS.FILE_CLOSED, { file });
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
            // add path to opened folders list - if not added already
            if (_.findIndex(this.openedFolders, folder => folder.fullPath === folderPath) === -1) {
                this.openedFolders.push(new Folder({ fullPath: folderPath }));
                const { pref: { history } } = this.appContext;
                history.put(HISTORY.OPENED_FOLDERS, this.openedFolders);
                this.reRender();
            }
            const { command: { dispatch } } = this.appContext;
            dispatch(LAYOUT_COMMANDS.SHOW_VIEW, { id: VIEW_IDS.EXPLORER });
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
            if (_.findIndex(this.openedFolders, folder => folder.fullPath === folderPath) !== -1) {
                _.remove(this.openedFolders, folder => folder.fullPath === folderPath);
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
        this.config = config;
        return {
            openFile: this.openFile.bind(this),
            openFolder: this.openFolder.bind(this),
            closeFile: this.closeFile.bind(this),
            removeFolder: this.removeFolder.bind(this),
            goToFileInExplorer: this.goToFileInExplorer.bind(this),
            isFilePathOpenedInExplorer: this.isFilePathOpenedInExplorer.bind(this),
            getExplorerFolderForPath: this.getExplorerFolderForPath.bind(this),
            refreshPathInExplorer: this.refreshPathInExplorer.bind(this),
        };
    }

    /**
     * @inheritdoc
     */
    activate(appContext) {
        super.activate(appContext);
        const { pref: { history } } = appContext;
        const serializedFolders = history.get(HISTORY.OPENED_FOLDERS) || [];
        this.openedFolders = serializedFolders.map((serializedFolder) => {
            return Object.assign(new Folder({}), serializedFolder);
        });
        // make File objects for each serialized file
        const serializedFiles = history.get(HISTORY.OPENED_FILES) || [];
        this.openedFiles = serializedFiles.map((serializedFile) => {
            return Object.assign(new File({}), serializedFile);
        });
        if (this.config && this.config.startupFile) {
            this.openFile(this.config.startupFile);
        }
        if (isOnElectron()) {
            const { ipcRenderer } = require('electron');
            ipcRenderer.on('open-file', (e, filePath) => {
                this.openFile(filePath);
            });
        }
    }

    /**
     * @inheritdoc
     */
    onAfterInitialRender() {
        const { editor, command: { dispatch } } = this.appContext;
        this.openedFiles.forEach((file) => {
            file.on(EVENTS.FILE_UPDATED, this.onWorkspaceFileUpdated);
            file.on(EVENTS.CONTENT_MODIFIED, this.onWorkspaceFileContentChanged);
            // no need to activate this editor
            // as this is loading from history.
            // Editor plugin will decide which editor
            // to activate depending on editor tabs history
            editor.open(file, false);
            dispatch(EVENTS.FILE_OPENED, { file });
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
                                icon: 'refresh2',
                                isActive: () => {
                                    return true;
                                },
                                handleAction: () => {
                                    const { command: { dispatch } } = this.appContext;
                                    dispatch(COMMAND_IDS.REFRESH_EXPLORER, {});
                                },
                                description: 'Refresh',
                            },
                            {
                                icon: 'folder-open',
                                isActive: () => {
                                    return true;
                                },
                                handleAction: () => {
                                    const { command: { dispatch } } = this.appContext;
                                    dispatch(COMMAND_IDS.SHOW_FOLDER_OPEN_WIZARD, {});
                                },
                                description: 'Open Directory',
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
                {
                    id: DIALOG_IDS.REPLACE_FILE_CONFIRM,
                    component: FileReplaceConfirmDialog,
                    propsProvider: () => {
                        return {
                            workspaceManager: this,
                        };
                    },
                },
                {
                    id: DIALOG_IDS.DELETE_FILE_CONFIRM,
                    component: FileDeleteConfirmDialog,
                    propsProvider: () => {
                        return {
                            workspaceManager: this,
                        };
                    },
                },
                {
                    id: DIALOG_IDS.CREATE_PROJECT,
                    component: CreateProjectDialog,
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
                    description: 'Create New',
                },
                {
                    id: TOOL_IDS.OPEN_FILE,
                    group: TOOL_IDS.GROUP,
                    icon: 'folder-open',
                    commandID: COMMAND_IDS.SHOW_FILE_OPEN_WIZARD,
                    description: 'Open File',
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
                    description: 'Save File',
                },
            ],
        };
    }
}

export default WorkspacePlugin;
