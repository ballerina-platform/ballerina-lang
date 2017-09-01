import { COMMANDS, DIALOGS } from './constants';
import { COMMANDS as WORKSPACE_COMMANDS } from './../workspace/constants';
/**
 * Provides command handler definitions of edotpr plugin.
 * @param {Plugin} editor plugin
 * @returns {Object[]} command handler definitions.
 *
 */
export function getHandlerDefinitions(plugin) {
    return [
        {
            cmdID: WORKSPACE_COMMANDS.OPEN_FILE,
            handler: ({ filePath, type }) => {
                // TODO
            },
        },
    ];
}
