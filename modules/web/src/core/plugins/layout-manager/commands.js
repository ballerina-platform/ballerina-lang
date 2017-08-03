import { COMMANDS } from './constants';

/**
 * Provides command definitions of layout manager plugin.
 *
 * @returns {Object[]} command definitions.
 *
 */
export function getCommandDefinitions() {
    return [
        {
            id: COMMANDS.ADD_VIEW_TO_LAYOUT,
        },
        {
            id: COMMANDS.REMOVE_VIEW_FROM_LAYOUT,
        },
    ];
}
