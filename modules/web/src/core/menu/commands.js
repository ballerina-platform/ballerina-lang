import { COMMANDS } from './constants';

/**
 * Provides command definitions of menu plugin.
 * No shortcuts for these commands.
 *
 * @returns {Object[]} command definitions.
 *
 */
export function getCommandDefinitions() {
    return [
        {
            id: COMMANDS.ADD_MENU_ITEM,
        },
        {
            id: COMMANDS.REMOVE_MENU_ITEM,
        },
        {
            id: COMMANDS.UPDATE_MENU_ITEM,
        },
    ];
}
