import { MENUS, COMMANDS, LABELS } from './constants';
import { MENU_DEF_TYPES } from './../menu/constants';

/**
 * Provides menu definitions of debugger plugin.
 *
 * @returns {Object[]} menu definitions.
 *
 */
export function getMenuDefinitions(debuggerInstance) {
    return [
        {
            id: MENUS.DEBUG_MENU,
            label: LABELS.DEBUG,
            isActive: (appContext) => {
                return true;
            },
            icon: '',
            type: MENU_DEF_TYPES.ROOT,
        },
        {
            id: MENUS.DEBUG_START_MENU,
            parent: MENUS.DEBUG_MENU,
            label: LABELS.DEBUG_START,
            isActive: (appContext) => {
                return true;
            },
            command: COMMANDS.START_DEBUG,
            icon: 'bug',
            type: MENU_DEF_TYPES.ITEM,
        },
        {
            id: MENUS.DEBUG_STOP_MENU,
            parent: MENUS.DEBUG_MENU,
            label: LABELS.DEBUG_STOP,
            isActive: (appContext) => {
                return true;
            },
            command: COMMANDS.STOP_DEBUG,
            icon: 'stop',
            type: MENU_DEF_TYPES.ITEM,
        },
    ];
}
