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

import { REGIONS } from './../layout/constants';

import { getCommandDefinitions } from './commands';
import { getHandlerDefinitions } from './handlers';
import { getMenuDefinitions } from './menus';
import { PLUGIN_ID, VIEWS as VIEW_IDS, HISTORY, COMMANDS as COMMMAND_IDS,
    EVENTS, TOOLS as TOOL_IDS } from './constants';
import { COMMANDS as TOOL_BAR_COMMANDS } from './../toolbar/constants';
import { EVENTS as WORKSPACE_EVENTS } from './../workspace/constants';

import EditorTabs from './views/EditorTabs';
import CustomEditor from './model/CustomEditor';
import Editor from './model/Editor';

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
        this.dispatchToolBarUpdate = this.dispatchToolBarUpdate.bind(this);
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
     * Open given file using relavant editor.
     *
     * @param {File} file File object
     * @param {boolean} activateEditor Indicate whether to activate this editor
     */
    open(file, activateEditor = true) {
        const editorDefinition = _.find(this.editorDefinitions, ['extension', file.extension]);
        if (!_.isNil(editorDefinition)) {
            this.onOpenFileInEditor({ file, editorDefinition, activateEditor });
            file.on(WORKSPACE_EVENTS.CONTENT_MODIFIED, this.dispatchToolBarUpdate);
            file.on(WORKSPACE_EVENTS.DIRTY_STATE_CHANGE, this.dispatchToolBarUpdate);
        } else {
            log.error(`No editor is found to open file type ${file.extension}`);
        }
    }

    /**
     * Dispatch tool bar update
     */
    dispatchToolBarUpdate() {
        const { command: { dispatch } } = this.appContext;
        dispatch(TOOL_BAR_COMMANDS.UPDATE_TOOL_BAR, {});
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
        };
    }

    /**
     * Set active editor
     *
     * @param {EditorTab} editor
     */
    setActiveEditor(editor) {
        this.activeEditor = editor;
        this.activeEditorID = editor ? editor.id : undefined;
        const { pref: { history } } = this.appContext;
        history.put(HISTORY.ACTIVE_EDITOR, this.activeEditorID);
        this.reRender();
        this.dispatchToolBarUpdate();
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
     * On Tab Close
     * @param {Editor} targetEditor Editor instance
     */
    onTabClose(targetEditor) {
        const { openedEditors, appContext: { workspace } } = this;
        const searchByID = editor => editor.id === targetEditor.id;
        const editorIndex = _.findIndex(openedEditors, searchByID);
        const newActiveEditorIndex = editorIndex > 0 ? editorIndex - 1 : 1;
        const newActiveEditor = !_.isNil(openedEditors[newActiveEditorIndex])
                                ? openedEditors[newActiveEditorIndex]
                                : undefined;
        _.remove(openedEditors, searchByID);
        this.setActiveEditor(newActiveEditor);
        if (targetEditor instanceof Editor) {
            targetEditor.file.off(WORKSPACE_EVENTS.CONTENT_MODIFIED, this.dispatchToolBarUpdate);
            targetEditor.file.off(WORKSPACE_EVENTS.DIRTY_STATE_CHANGE, this.dispatchToolBarUpdate);
            workspace.closeFile(targetEditor.file);
        }
    }

    /**
     * On command open-custom-editor-tab
     * @param {Object} command args
     */
    onOpenCustomEditorTab(args) {
        const { id, title, icon, component, propsProvider, customTitleClass } = args;
        const editor = new CustomEditor(id, title, icon, component, propsProvider, customTitleClass);
        this.openedEditors.push(editor);
        if (_.isNil(this.activeEditorID)) {
            this.setActiveEditor(editor);
        }
        this.reRender();
    }

    /**
     * On command open-file-in-editor
     * @param {Object} command args
     */
    onOpenFileInEditor({ activateEditor, file, editorDefinition }) {
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
                    group: TOOL_IDS.GROUP,
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
                },
                {
                    id: TOOL_IDS.REDO,
                    group: TOOL_IDS.GROUP,
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
                },
            ],
            [DIALOGS]: [],
        };
    }
}

export default EditorPlugin;
