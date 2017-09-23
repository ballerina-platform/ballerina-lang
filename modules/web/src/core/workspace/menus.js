import { MENUS, COMMANDS, LABELS } from './constants';
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
            label: LABELS.FILE,
            isActive: (appContext) => {
                return true;
            },
            icon: '',
            order: 0,
            type: MENU_DEF_TYPES.ROOT,
        },
        {
            id: MENUS.NEW_FILE,
            parent: MENUS.FILE_MENU,
            label: LABELS.NEW_FILE,
            isActive: () => {
                return true;
            },
            command: COMMANDS.CREATE_NEW_FILE,
            icon: 'add',
            type: MENU_DEF_TYPES.ITEM,
            divider: {
                after: true,
            },
        },
        {
            id: MENUS.SHOW_FILE_OPEN_WIZARD,
            parent: MENUS.FILE_MENU,
            label: LABELS.SHOW_FILE_OPEN_WIZARD,
            isActive: (appContext) => {
                return true;
            },
            command: COMMANDS.SHOW_FILE_OPEN_WIZARD,
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
            divider: {
                after: true,
            },
        },
        {
            id: MENUS.SAVE_FILE,
            parent: MENUS.FILE_MENU,
            label: LABELS.SAVE,
            isActive: () => {
                const { editor } = workspaceManager.appContext;
                const activeTab = editor.getActiveEditor();
                return activeTab && activeTab.isDirty ? activeTab.isDirty : false;
            },
            command: COMMANDS.SAVE_FILE,
            icon: 'save',
            type: MENU_DEF_TYPES.ITEM,
        },
        {
            id: MENUS.SAVE_FILE_AS,
            parent: MENUS.FILE_MENU,
            label: LABELS.SAVE_AS,
            isActive: () => {
                return true;
            },
            command: COMMANDS.SAVE_FILE_AS,
            icon: 'folder-open',
            type: MENU_DEF_TYPES.ITEM,
            divider: {
                after: true,
            },
        },
    ];
}
