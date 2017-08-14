import { COMMANDS } from './constants';

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
    ];
}
