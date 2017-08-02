import { commands } from './constants';

/**
 * Provides command handler definitions of menu plugin.
 * @param {MenuManager} menuManager
 * @returns {Object[]} command handler definitions.
 *
 */
export function getHandlerDefinitions(menuManager) {
    return [
        {
            cmdID: commands.ADD_MENU_ITEM,
            handler: (menuItem) => {
                menuManager.menuItems.push(menuItem);
            },
        },
        {
            cmdID: commands.REMOVE_MENU_ITEM,
            handler: (menuItem) => {
                // TODO
            },
        },
        {
            cmdID: commands.UPDATE_MENU_ITEM,
            handler: (menuItem) => {
                // TODO
            },
        },
    ];
}
