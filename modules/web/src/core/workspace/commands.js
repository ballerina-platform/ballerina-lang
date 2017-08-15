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
            shortcuts: {
                mac: {
                    key: 'command+o',
                    label: '\u2318O',
                },
                other: {
                    key: 'ctrl+o',
                    label: 'Ctrl+O',
                },
            },
        },
        {
            id: COMMANDS.SHOW_FOLDER_OPEN_WIZARD,
            shortcuts: {
                mac: {
                    key: 'command+shift+o',
                    label: '\u2318\u21E7O',
                },
                other: {
                    key: 'ctrl+shift+o',
                    label: 'Ctrl+Shift+O',
                },
            },
        },
    ];
}
