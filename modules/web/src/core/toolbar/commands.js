import { COMMANDS } from './constants';

/**
 * Provides command definitions of toolbar plugin.
 * No shortcuts for these commands.
 *
 * @returns {Object[]} command definitions.
 *
 */
export function getCommandDefinitions() {
    return [
        {
            id: COMMANDS.UPDATE_TOOL_BAR,
        },
    ];
}
