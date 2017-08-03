import { COMMANDS } from './constants';

/**
 * Provides command handler definitions of menu plugin.
 * @param {MenuManager} menuManager
 * @returns {Object[]} command handler definitions.
 *
 */
export function getHandlerDefinitions(menuManager) {
    return [
        {
            cmdID: COMMANDS.ADD_MENU_ITEM,
            handler: (menuItem) => {
                menuManager.menuItems.push(menuItem);
            },
        },
        {
            cmdID: COMMANDS.REMOVE_MENU_ITEM,
            handler: (menuItem) => {
                // TODO
            },
        },
        {
            cmdID: COMMANDS.UPDATE_MENU_ITEM,
            handler: (menuItem) => {
                // TODO
            },
        },
    ];
}
