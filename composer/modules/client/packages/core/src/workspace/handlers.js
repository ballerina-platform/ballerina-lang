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
import log from '../log/log';
import { COMMANDS, DIALOGS, EVENTS } from './constants';
import { COMMANDS as LAYOUT_COMMANDS } from './../layout/constants';
import { createOrUpdate } from './fs-util';

/**
 * Provides command handler definitions of workspace plugin.
 * @param {WorkspaceManager} workspace Manager
 * @returns {Object[]} command handler definitions.
 *
 */
export function getHandlerDefinitions(workspaceManager) {
    return [
        {
            cmdID: COMMANDS.CREATE_NEW_FILE,
            handler: () => {
                workspaceManager.createNewFile();
            },
        },
        {
            cmdID: COMMANDS.OPEN_FILE,
            handler: ({ filePath, ext, activate = true }) => {
                workspaceManager.openFile(filePath, ext, activate);
            },
        },
        {
            cmdID: COMMANDS.OPEN_FOLDER,
            handler: ({ folderPath }) => {
                workspaceManager.openFolder(folderPath);
            },
        },
        {
            cmdID: COMMANDS.REMOVE_FOLDER,
            handler: ({ folderPath }) => {
                workspaceManager.removeFolder(folderPath);
            },
        },
        {
            cmdID: COMMANDS.SAVE_FILE,
            handler: ({ file, onSaveSuccess = () => {}, onSaveFail = () => {} }) => {
                const { command: { dispatch }, editor } = workspaceManager.appContext;
                const targetFile = file || editor.getActiveEditor().file;
                const onSuccess = () => {
                    onSaveSuccess();
                    dispatch(EVENTS.FILE_SAVED, { file: targetFile });
                };
                // File is not yet persisted - show save as dialog
                if (!targetFile.isPersisted) {
                    const id = DIALOGS.SAVE_FILE;
                    dispatch(LAYOUT_COMMANDS.POPUP_DIALOG, {
                        id,
                        additionalProps: {
                            file: targetFile,
                            mode: 'SAVE_FILE',
                            onSuccess,
                            onSaveFail,
                        },
                    });
                } else {
                    // File is already persisted
                    createOrUpdate(targetFile.path, targetFile.name + '.' + targetFile.extension, targetFile.content)
                        .then((success) => {
                            if (success) {
                                targetFile.isDirty = false;
                                targetFile.lastPersisted = _.now();
                                onSuccess();
                            } else {
                                throw new Error('Error while saving file ' + targetFile.fullPath);
                            }
                        })
                        .catch((error) => {
                            log.error(error);
                            onSaveFail(error);
                        });
                }
            },
        },
        {
            cmdID: COMMANDS.SAVE_FILE_AS,
            handler: () => {
                const { command: { dispatch }, editor } = workspaceManager.appContext;
                const id = DIALOGS.SAVE_FILE;
                const activeEditor = editor.getActiveEditor();
                if (activeEditor && activeEditor.file) {
                    dispatch(LAYOUT_COMMANDS.POPUP_DIALOG, {
                        id,
                        additionalProps: {
                            file: activeEditor.file,
                            mode: 'SAVE_FILE_AS',
                        },
                    });
                }
            },
        },
        {
            cmdID: COMMANDS.SHOW_FILE_OPEN_WIZARD,
            handler: () => {
                const { command: { dispatch } } = workspaceManager.appContext;
                const id = DIALOGS.OPEN_FILE;
                dispatch(LAYOUT_COMMANDS.POPUP_DIALOG, { id });
            },
        },
        {
            cmdID: COMMANDS.SHOW_FOLDER_OPEN_WIZARD,
            handler: () => {
                const { command: { dispatch } } = workspaceManager.appContext;
                const id = DIALOGS.OPEN_FOLDER;
                dispatch(LAYOUT_COMMANDS.POPUP_DIALOG, { id });
            },
        },
    ];
}
