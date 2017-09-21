import _ from 'lodash';
import log from 'log';
import { COMMANDS, DIALOGS } from './constants';
import { COMMANDS as LAYOUT_COMMANDS } from './../layout/constants';
import { update } from './fs-util';

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
            handler: ({ filePath, ext }) => {
                workspaceManager.openFile(filePath, ext);
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
            handler: () => {
                const { command: { dispatch }, editor } = workspaceManager.appContext;
                const { file } = editor.getActiveEditor();
                // File is not yet persisted - show save as dialog
                if (!file.isPersisted) {
                    const id = DIALOGS.SAVE_FILE;
                    dispatch(LAYOUT_COMMANDS.POPUP_DIALOG, { id });
                } else {
                    // File is already persisted
                    update(file.path, file.name, file.content)
                        .then((success) => {
                            if (success) {
                                file.isDirty = false;
                                file.lastPersisted = _.now();
                            } else {
                                throw new Error('Error while saving file ' + file.fullPath);
                            }
                        })
                        .catch((error) => {
                            log.error(error);
                        });
                }
            },
        },
        {
            cmdID: COMMANDS.SAVE_FILE_AS,
            handler: () => {
                const { command: { dispatch } } = workspaceManager.appContext;
                const id = DIALOGS.SAVE_FILE;
                dispatch(LAYOUT_COMMANDS.POPUP_DIALOG, { id });
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
