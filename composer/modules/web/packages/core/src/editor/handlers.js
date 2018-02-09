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
 *
 */

import _ from 'lodash';
import { COMMANDS } from './constants';

/**
 * Provides command handler definitions of editor plugin.
 * @param {Plugin} editor plugin
 * @returns {Object[]} command handler definitions.
 *
 */
export function getHandlerDefinitions(editorPlugin) {
    return [
        {
            cmdID: COMMANDS.OPEN_CUSTOM_EDITOR_TAB,
            handler: (args) => {
                editorPlugin.onOpenCustomEditorTab(args);
            },
        },
        {
            cmdID: COMMANDS.OPEN_FILE_IN_EDITOR,
            handler: (args) => {
                editorPlugin.onOpenFileInEditor(args);
            },
        },
        {
            cmdID: COMMANDS.UNDO,
            handler: () => {
                const { editor } = editorPlugin.appContext;
                const activeEditor = editor.getActiveEditor();
                if (activeEditor && !_.isNil(activeEditor.undoManager) && activeEditor.undoManager.hasUndo()) {
                    activeEditor.undoManager.undo();
                }
                editorPlugin.dispatchActionTriggerUpdate();
            },
        },
        {
            cmdID: COMMANDS.REDO,
            handler: () => {
                const { editor } = editorPlugin.appContext;
                const activeEditor = editor.getActiveEditor();
                if (activeEditor && !_.isNil(activeEditor.undoManager) && activeEditor.undoManager.hasRedo()) {
                    activeEditor.undoManager.redo();
                }
                editorPlugin.dispatchActionTriggerUpdate();
            },
        },
        {
            cmdID: COMMANDS.ACTIVATE_EDITOR_FOR_FILE,
            handler: (args) => {
                const { filePath } = args;
                const editor = editorPlugin.getEditorByID(filePath);
                if (editor) {
                    editorPlugin.setActiveEditor(editor);
                }
            },
        },
    ];
}
