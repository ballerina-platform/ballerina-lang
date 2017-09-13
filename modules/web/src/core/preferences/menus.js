import { MENUS as MENU_IDS, COMMANDS, LABELS } from './constants';
import { MENU_DEF_TYPES } from './../menu/constants';
import { MENUS as WORKSPACE_MENUS } from './../workspace/constants';


/**
 * Provides menu definitions of preferences plugin.
 *
 * @returns {Object[]} menu definitions.
 *
 */
export function getMenuDefinitions() {
    return [
        {
            id: MENU_IDS.PREFERENCES_MENU,
            parent: WORKSPACE_MENUS.FILE_MENU,
            label: LABELS.PREFERENCES,
            isActive: (appContext) => {
                return true;
            },
            command: COMMANDS.SHOW_SETTINGS_DIALOG,
            icon: 'settings',
            type: MENU_DEF_TYPES.ITEM,
        },
    ];
}
