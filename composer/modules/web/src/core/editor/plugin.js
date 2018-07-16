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
import log from 'log';
import _ from 'lodash';
import Plugin from './../plugin/plugin';
import { CONTRIBUTIONS } from './../plugin/constants';

import { REGIONS, COMMANDS as LAYOUT_COMMANDS } from './../layout/constants';

import { getCommandDefinitions } from './commands';
import { getHandlerDefinitions } from './handlers';
import { getMenuDefinitions } from './menus';
import { exists } from './../workspace/fs-util';
import { PLUGIN_ID, VIEWS as VIEW_IDS, HISTORY, COMMANDS as COMMMAND_IDS,
    EVENTS, TOOLS as TOOL_IDS, DIALOGS as DIALOG_IDS } from './constants';
import { EVENTS as WORKSPACE_EVENTS, COMMANDS as WORKSPACE_COMMANDS } from './../workspace/constants';

import EditorTabs from './views/EditorTabs';
import CustomEditor from './model/CustomEditor';
import Editor from './model/Editor';

import DirtyFileCloseConfirmDialog from './dialogs/DirtyFileCloseConfirmDialog';
import OpenedFileDeleteConfirmDialog from './dialogs/OpenedFileDeleteConfirmDialog';
import NonExistentFileOpenConfirmDialog from './dialogs/NonExistentFileOpenConfirmDialog';

/**
 * Editor Plugin is responsible for providing editors to opening files.
 *
 * @class EditorPlugin
 */
class EditorPlugin extends Plugin {

    /**
     * @inheritdoc
     */
    constructor(props) {
        super(props);
        this.editorDefinitions = [];
        this.activeEditor = undefined;
        this.activeEditorID = undefined;
        this.openedEditors = [];
        this.onOpenFileInEditor = this.onOpenFileInEditor.bind(this);
        this.onOpenCustomEditorTab = this.onOpenCustomEditorTab.bind(this);
        this.onTabClose = this.onTabClose.bind(this);
        this.dispatchActionTriggerUpdate = this.dispatchActionTriggerUpdate.bind(this);
        this.onEditorFileUpdated = this.onEditorFileUpdated.bind(this);
    }

    /**
     * @inheritdoc
     */
    getID() {
        return PLUGIN_ID;
    }

    /**
     * Register an editor contribution.
     *
     * @param {Object} editorDef Editor Definition
     */
    registerEditor(editorDef) {
        if (_.findIndex(this.editorDefinitions, ['id', editorDef.id]) === -1) {
            this.editorDefinitions.push(editorDef);
        } else {
            log.error(`Duplicate editor def found with ID ${editorDef.id}.`);
        }
    }

    /**
     * On Editor File Updated
     *
     * @param {File} file Target File
     */
    onEditorFileUpdated(file) {
        // File was the active file and now its persisted for the first time.
        // its temporary UUID was previously used as the activeEditorID.
        // Now we need to change it.
        // @see File#fullPath getter
        // For unpersisted files, we set fullpath to UUID upon creation.
        if (this.activeEditorID === file.id && file.isPersisted) {
            this.activeEditorID = file.fullPath;
        }
        // update file is the active file, update active editor ID
        if (this.activeEditor instanceof Editor
            && this.activeEditor.file.id === file.id) {
            this.activeEditorID = file.fullPath;
        }
    }

    /**
     * Open given file using relavant editor.
     *
     * @param {File} file File object
     * @param {boolean} activateEditor Indicate whether to activate this editor
     */
    open(file, activateEditor = true) {
        const editorDefinition = _.find(this.editorDefinitions, ['extension', file.extension]);
        if (!_.isNil(editorDefinition)) {
            this.onOpenFileInEditor({ file, editorDefinition, activateEditor });
            file.on(WORKSPACE_EVENTS.CONTENT_MODIFIED, this.dispatchActionTriggerUpdate);
            file.on(WORKSPACE_EVENTS.DIRTY_STATE_CHANGE, this.dispatchActionTriggerUpdate);
            file.on(WORKSPACE_EVENTS.FILE_UPDATED, this.onEditorFileUpdated);
        } else {
            log.error(`No editor is found to open file type ${file.extension}`);
        }
    }

    /**
     * Dispatch tool bar update
     */
    dispatchActionTriggerUpdate() {
        const { command: { dispatch } } = this.appContext;
        dispatch(LAYOUT_COMMANDS.UPDATE_ALL_ACTION_TRIGGERS, {});
    }

    /**
     * @inheritdoc
     */
    init(config) {
        super.init(config);
        return {
            open: this.open.bind(this),
            getActiveEditor: () => {
                return this.activeEditor;
            },
            getEditorByID: this.getEditorByID.bind(this),
            isFileOpenedInEditor: this.isFileOpenedInEditor.bind(this),
            setActiveEditor: this.setActiveEditor.bind(this),
            closeEditor: this.closeTab.bind(this),
            getSupportedExtensions: () => {
                return this.editorDefinitions.map(editorDef => editorDef.extension);
            },
            getDefaultContent: (filePath) => {
                const ext = filePath.split('.').pop();
                const editorDef = ext ? this.editorDefinitions.find(def => def.extension === ext) : undefined;
                return editorDef && editorDef.newFileContentProvider instanceof Function
                    ? editorDef.newFileContentProvider(filePath)
                    : '';
            },
        };
    }

    /**
     * Returns the editor for give file - if file is opened already.
     *
     * @param {String} filePath Path of the file.
     *
     * @returns {Editor|undefined} Editor instance or null
     *
     */
    getEditorByID(filePath) {
        return _.find(this.openedEditors, ['id', filePath]);
    }

    /**
     * Indicates whether the given file is opened in editor area.
     *
     * @param {String} filePath Path of the file.
     *
     * @returns {boolean} true if opened in Editor area.
     *
     */
    isFileOpenedInEditor(filePath) {
        return _.findIndex(this.openedEditors, ['id', filePath]) !== -1;
    }

    /**
     * Set active editor
     *
     * @param {EditorTab} editor
     */
    setActiveEditor(editor) {
        if (editor instanceof Editor && !editor.file.fullPath.endsWith("untitled.bal")) {
            exists(editor.file.fullPath).then((response) => {
                if (response.exists === false) {
                    this.showNonExistentFileOpenDialog(editor);
                }
            }).catch(() => {
                this.showNonExistentFileOpenDialog(editor);
            });
        }
        this.activeEditor = editor;
        this.activeEditorID = editor ? editor.id : undefined;
        const { pref: { history }, workspace, command: { dispatch } } = this.appContext;
        history.put(HISTORY.ACTIVE_EDITOR, this.activeEditorID);
        this.reRender();
        this.dispatchActionTriggerUpdate();
        if (editor instanceof Editor) {
            setTimeout(() => {
                workspace.goToFileInExplorer(editor.file.fullPath);
            }, 100);
        }
        dispatch(EVENTS.ACTIVE_TAB_CHANGE, { editor });
    }

    /**
     * Shows non-existent file open dialog box.
     * @param {Editor} editor Editor instance
     */
    showNonExistentFileOpenDialog(editor) {
        const {command: {dispatch}} = this.appContext;
        dispatch(LAYOUT_COMMANDS.POPUP_DIALOG, {
            id: DIALOG_IDS.NON_EXISTENT_FILE_OPEN_CONFIRM,
            additionalProps: {
                file: editor.file,
                onConfirm: () => {
                    this.closeTab(editor);
                },
                onSave: () => {
                    dispatch(WORKSPACE_COMMANDS.SAVE_FILE, {
                        file: editor.file,
                        onSaveSuccess: () => {
                            this.closeTab(editor);
                        },
                    });
                },
            },
        });
    }

    /**
     * @inheritdoc
     */
    activate(appContext) {
        super.activate(appContext);
        const { pref: { history } } = this.appContext;
        this.activeEditorID = history.get(HISTORY.ACTIVE_EDITOR);
    }

    /**
     * Closes a tab
     * @param {Editor} targetEditor Editor instance
     * @param {Editor} nextActiveEditor Editor to be activated if the targetEditor is the active one. If not given the
     *      editor to the left of targetEditor is activated.
     */
    closeTab(targetEditor, nextActiveEditor) {
        const { openedEditors, appContext: { workspace } } = this;
        const searchByID = editor => editor.id === targetEditor.id;
        // only change active editor when the closing one is the currently active one
        if (this.activeEditorID === targetEditor.id) {
            let newActiveEditor = nextActiveEditor;
            if (!newActiveEditor) {
                const editorIndex = _.findIndex(openedEditors, searchByID);
                const newActiveEditorIndex = editorIndex > 0 ? editorIndex - 1 : 1;
                newActiveEditor = !_.isNil(openedEditors[newActiveEditorIndex])
                                        ? openedEditors[newActiveEditorIndex]
                                        : undefined;
            }
            _.remove(openedEditors, searchByID);
            this.setActiveEditor(newActiveEditor);
        } else {
            _.remove(openedEditors, searchByID);
            this.reRender();
        }

        if (targetEditor instanceof Editor) {
            targetEditor.file.off(WORKSPACE_EVENTS.CONTENT_MODIFIED, this.dispatchActionTriggerUpdate);
            targetEditor.file.off(WORKSPACE_EVENTS.DIRTY_STATE_CHANGE, this.dispatchActionTriggerUpdate);
            targetEditor.file.off(WORKSPACE_EVENTS.FILE_UPDATED, this.onEditorFileUpdated);
            workspace.closeFile(targetEditor.file);
        }
    }

    /**
     * On Tab Close
     * @param {Editor} targetEditor Editor instance
     */
    onTabClose(targetEditor) {
        const { appContext: { command: { dispatch } } } = this;
        if(targetEditor.isDirty){
            if(targetEditor.file.content === '' && targetEditor.file.name === 'untitled'){
                this.closeTab(targetEditor);
            } else {
                dispatch(LAYOUT_COMMANDS.POPUP_DIALOG, {
                    id: DIALOG_IDS.DIRTY_CLOSE_CONFIRM,
                    additionalProps: {
                        file: targetEditor.file,
                        onConfirm: () => {
                            this.closeTab(targetEditor);
                        },
                        onSave: () => {
                            dispatch(WORKSPACE_COMMANDS.SAVE_FILE, {
                                file: targetEditor.file,
                                onSaveSuccess: () => {
                                    this.closeTab(targetEditor);
                                },
                            });
                        },
                    },
                });
            }
        } else {
            this.closeTab(targetEditor);
        }
    }

    /**
     * On command open-custom-editor-tab
     * @param {Object} command args
     */
    onOpenCustomEditorTab(args) {
        const { id, title, icon, component, propsProvider, options,
            additionalProps, customTitleClass, activate } = args;
        const derivedId = options && options.clone ? options.key : id;
        const existingEditor = this.getEditorByID(derivedId);
        if (!existingEditor) {
            const editor = new CustomEditor(derivedId, title, icon, component, propsProvider,
                additionalProps, customTitleClass);
            this.openedEditors.push(editor);
            if (activate || _.isNil(this.activeEditorID)) {
                this.setActiveEditor(editor);
            }
        } else if (activate) {
            existingEditor.additionalProps = additionalProps;
            this.setActiveEditor(existingEditor);
        }
        this.reRender();
    }

    /**
     * On command open-file-in-editor
     * @param {Object} command args
     */
    onOpenFileInEditor({ activateEditor, file, editorDefinition }) {
        if (!this.getEditorByID(file.fullPath)) {
            const editor = new Editor(file, editorDefinition);
            this.openedEditors.push(editor);
            if (activateEditor
                || _.isNil(this.activeEditorID)
                || this.activeEditorID === editor.id) {
                this.setActiveEditor(editor);
            }
            editor.on(EVENTS.UPDATE_TAB_TITLE, () => {
                this.reRender();
            });
        } else if (activateEditor) {
            this.setActiveEditor(this.getEditorByID(file.fullPath));
        }
        this.reRender();
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
                    id: VIEW_IDS.EDITOR_TABS,
                    component: EditorTabs,
                    propsProvider: () => {
                        return {
                            editorPlugin: this,
                        };
                    },
                    region: REGIONS.EDITOR_AREA,
                    displayOnLoad: true,
                },
            ],
            [TOOLS]: [
                {
                    id: TOOL_IDS.UNDO,
                    group: TOOL_IDS.UNDO_REDO_GROUP,
                    icon: 'undo',
                    commandID: COMMMAND_IDS.UNDO,
                    commandArgs: {},
                    isActive: () => {
                        const { editor } = this.appContext;
                        const activeEditor = editor.getActiveEditor();
                        if (activeEditor && !_.isNil(activeEditor.undoManager)) {
                            return activeEditor.undoManager.hasUndo();
                        }
                        return false;
                    },
                    description: 'Undo',
                },
                {
                    id: TOOL_IDS.REDO,
                    group: TOOL_IDS.UNDO_REDO_GROUP,
                    icon: 'redo',
                    commandID: COMMMAND_IDS.REDO,
                    commandArgs: {},
                    isActive: () => {
                        const { editor } = this.appContext;
                        const activeEditor = editor.getActiveEditor();
                        if (activeEditor && !_.isNil(activeEditor.undoManager)) {
                            return activeEditor.undoManager.hasRedo();
                        }
                        return false;
                    },
                    description: 'Redo',
                },
                {
                    id: TOOL_IDS.FORMAT,
                    group: TOOL_IDS.CODE_GROUP,
                    icon: 'format',
                    commandID: COMMMAND_IDS.FORMAT,
                    commandArgs: {},
                    isActive: () => {
                        const { editor } = this.appContext;
                        const activeEditor = editor.getActiveEditor();
                        if (activeEditor && activeEditor.constructor.name === 'Editor') {
                            return true;
                        }
                        return false;
                    },
                    description: 'Reformat Code',
                },
            ],
            [DIALOGS]: [
                {
                    id: DIALOG_IDS.DIRTY_CLOSE_CONFIRM,
                    component: DirtyFileCloseConfirmDialog,
                    propsProvider: () => {
                        return {
                            editorPlugin: this,
                        };
                    },
                },
                {
                    id: DIALOG_IDS.OPENED_FILE_DELETE_CONFIRM,
                    component: OpenedFileDeleteConfirmDialog,
                    propsProvider: () => {
                        return {
                            editorPlugin: this,
                        };
                    },
                },
                {
                    id: DIALOG_IDS.NON_EXISTENT_FILE_OPEN_CONFIRM,
                    component: NonExistentFileOpenConfirmDialog,
                    propsProvider: () => {
                        return {
                            editorPlugin: this,
                        };
                    },
                },
            ],
        };
    }
}

export default EditorPlugin;
