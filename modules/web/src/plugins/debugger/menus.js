import { MENU_DEF_TYPES } from 'core/menu/constants';
import { MENUS, COMMANDS, LABELS } from './constants';

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
            order: 21,
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
