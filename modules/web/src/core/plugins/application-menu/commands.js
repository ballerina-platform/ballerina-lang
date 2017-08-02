import { commands } from './constants';

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
            id: commands.ADD_MENU_ITEM,
        },
        {
            id: commands.REMOVE_MENU_ITEM,
        },
        {
            id: commands.UPDATE_MENU_ITEM,
        },
    ];
}
