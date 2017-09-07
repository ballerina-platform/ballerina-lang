import { COMMANDS } from './constants';

/**
 * Provides command definitions of help plugin.
 *
 * @returns {Object[]} command definitions.
 *
 */
export function getCommandDefinitions(plugin) {
    return [
        {
            id: COMMANDS.SHOW_WELCOME,
        },
        {
            id: COMMANDS.OPEN_REFRENCE,
            shortcut: {
                default: 'f1',
            },
        },
        {
            id: COMMANDS.REPORT_ISSUE,
        },
        {
            id: COMMANDS.SHOW_ABOUT,
        },
    ];
}
