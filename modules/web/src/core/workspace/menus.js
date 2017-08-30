import { MENUS, COMMANDS, LABELS, DIALOGS } from './constants';
import { MENU_DEF_TYPES } from './../menu/constants';
import { COMMANDS as LAYOUT_COMMANDS } from './../layout/constants'

/**
 * Provides menu definitions of workspace plugin.
 *
 * @returns {Object[]} menu definitions.
 *
 */
export function getMenuDefinitions(workspaceManager) {
    return [
        {
            id: MENUS.FILE_MENU,
            label: LABELS.FILE,
            isActive: (appContext) => {
                return true;
            },
            icon: '',
            type: MENU_DEF_TYPES.ROOT,
        },
        {
            id: MENUS.SHOW_FILE_OPEN_WIZARD,
            parent: MENUS.FILE_MENU,
            label: LABELS.SHOW_FILE_OPEN_WIZARD,
            isActive: (appContext) => {
                return true;
            },
            command: LAYOUT_COMMANDS.POPUP_DIALOG,
            commandArgs: {
                id: DIALOGS.OPEN_FILE,
            },
            icon: 'document',
            type: MENU_DEF_TYPES.ITEM,
        },
        {
            id: MENUS.SHOW_FOLDER_OPEN_WIZARD,
            parent: MENUS.FILE_MENU,
            label: LABELS.SHOW_FOLDER_OPEN_WIZARD,
            isActive: (appContext) => {
                return true;
            },
            command: COMMANDS.SHOW_FOLDER_OPEN_WIZARD,
            icon: 'folder-open',
            type: MENU_DEF_TYPES.ITEM,
        },
    ];
}
