import { MENUS, COMMANDS, LABELS } from './constants';
import { MENU_DEF_TYPES } from './../../core/menu/constants';

/**
 * Provides menu definitions of help plugin.
 *
 * @returns {Object[]} menu definitions.
 *
 */
export function getMenuDefinitions(plugin) {
    return [
        {
            id: MENUS.HELP_MENU,
            label: LABELS.HELP,
            isActive: (appContext) => {
                return true;
            },
            icon: '',
            type: MENU_DEF_TYPES.ROOT,
        },
        {
            id: MENUS.REFERENCE_MENU,
            parent: MENUS.HELP_MENU,
            label: LABELS.REFERENCE,
            isActive: (appContext) => {
                return true;
            },
            order: 0,
            command: COMMANDS.OPEN_REFRENCE,
            type: MENU_DEF_TYPES.ITEM,
        },
        {
            id: MENUS.REPORT_MENU,
            parent: MENUS.HELP_MENU,
            label: LABELS.REPORT,
            isActive: (appContext) => {
                return true;
            },
            icon: 'warning',
            order: 10,
            command: COMMANDS.REPORT_ISSUE,
            type: MENU_DEF_TYPES.ITEM,
        },
        {
            id: MENUS.ABOUT_MENU,
            parent: MENUS.HELP_MENU,
            label: LABELS.ABOUT,
            isActive: (appContext) => {
                return true;
            },
            order: 20,
            command: COMMANDS.SHOW_ABOUT,
            type: MENU_DEF_TYPES.ITEM,
        },
    ];
}
