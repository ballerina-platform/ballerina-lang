import { COMMANDS, DIALOGS } from './constants';
import { COMMANDS as LAYOUT_COMMANDS } from './../layout/constants';
/**
 * Provides command handler definitions of workspace plugin.
 * @param {WorkspaceManager} workspace Manager
 * @returns {Object[]} command handler definitions.
 *
 */
export function getHandlerDefinitions(workspaceManager) {
    return [
        {
            cmdID: COMMANDS.OPEN_FILE,
            handler: (filePath) => {
                // TODO
            },
        },
        {
            cmdID: COMMANDS.OPEN_FOLDER,
            handler: (folderPath) => {
                // TODO
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
