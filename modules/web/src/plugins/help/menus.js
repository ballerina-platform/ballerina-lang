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
            id: MENUS.WELCOME_PAGE_MENU,
            parent: MENUS.HELP_MENU,
            label: LABELS.WELCOME,
            isActive: (appContext) => {
                return true;
            },
            command: COMMANDS.SHOW_WELCOME,
            type: MENU_DEF_TYPES.ITEM,
        },
        {
            id: MENUS.REFERENCE_MENU,
            parent: MENUS.HELP_MENU,
            label: LABELS.REFERENCE,
            isActive: (appContext) => {
                return true;
            },
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
            command: COMMANDS.SHOW_ABOUT,
            type: MENU_DEF_TYPES.ITEM,
        },
    ];
}
