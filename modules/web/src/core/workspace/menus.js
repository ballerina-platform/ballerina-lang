import { MENUS } from './constants';
import { MENU_DEF_TYPES } from './../menu/constants';

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
            title: 'file',
            isActive: () => {
                return true;
            },
            icon: '',
            type: MENU_DEF_TYPES.ROOT,
        },
        {
            id: MENUS.SHOW_FILE_OPEN_WIZARD,
            parent: MENUS.FILE_MENU,
            title: 'Open File',
            isActive: () => {
                return true;
            },
            icon: '',
            type: MENU_DEF_TYPES.ITEM,
        },
        {
            id: MENUS.SHOW_FOLDER_OPEN_WIZARD,
            parent: MENUS.FILE_MENU,
            title: 'Open Folder',
            isActive: () => {
                return true;
            },
            icon: '',
            type: MENU_DEF_TYPES.ITEM,
        },
    ];
}
