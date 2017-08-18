import { COMMANDS } from './constants';

/**
 * Provides command definitions of workspace plugin.
 *
 * @returns {Object[]} command definitions.
 *
 */
export function getCommandDefinitions() {
    return [
        {
            id: COMMANDS.OPEN_FILE,
        },
        {
            id: COMMANDS.OPEN_FOLDER,
        },
        {
            id: COMMANDS.SHOW_FILE_OPEN_WIZARD,
            shortcut: {
                default: 'ctrl+o',
            },
        },
        {
            id: COMMANDS.SHOW_FOLDER_OPEN_WIZARD,
            shortcut: {
                default: 'ctrl+shift+o',
            },
        },
    ];
}
