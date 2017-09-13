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
import { PLUGIN_ID, VIEWS as VIEW_IDS, DIALOGS as DIALOG_IDS, HISTORY, COMMANDS, EVENTS } from './constants';

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
        } else {
            log.error(`No editor is found to open file type ${file.extension}`);
        }
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
        this.activeEditorID = editor.id;
        const { pref: { history } } = this.appContext;
        history.put(HISTORY.ACTIVE_EDITOR, this.activeEditorID);
        this.reRender();
    }

    /**
     * @inheritdoc
     */
    activate(appContext) {
        super.activate(appContext);
        const { command, pref: { history } } = this.appContext;
        this.activeEditorID = history.get(HISTORY.ACTIVE_EDITOR);
        command.on(COMMANDS.OPEN_FILE_IN_EDITOR, this.onOpenFileInEditor);
        command.on(COMMANDS.OPEN_CUSTOM_EDITOR_TAB, this.onOpenCustomEditorTab);
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
            workspace.closeFile(targetEditor.file);
        }
    }

    /**
     * On command open-custom-editor-tab
     * @param {Object} command args
     */
    onOpenCustomEditorTab(args) {
        const { id, title, icon, component, propsProvider, customTitleClass } = args;
        this.openedEditors.push(new CustomEditor(id, title, icon, component, propsProvider, customTitleClass));
        this.reRender();
    }

    /**
     * On command open-file-in-editor
     * @param {Object} command args
     */
    onOpenFileInEditor({ activateEditor, file, editorDefinition }) {
        const editor = new Editor(file, editorDefinition);
        this.openedEditors.push(editor);
        if (activateEditor || _.isNil(this.activeEditorID)) {
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
        const { COMMANDS, HANDLERS, MENUS, VIEWS, DIALOGS } = CONTRIBUTIONS;
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
            [DIALOGS]: [],
        };
    }
}

export default EditorPlugin;
