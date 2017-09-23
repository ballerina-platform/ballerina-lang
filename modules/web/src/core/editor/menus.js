import _ from 'lodash';
import { MENUS, COMMANDS, LABELS } from './constants';
import { MENU_DEF_TYPES } from './../menu/constants';

/**
 * Provides menu definitions of editor plugin.
 *
 * @returns {Object[]} menu definitions.
 *
 */
export function getMenuDefinitions(plugin) {
    return [
        {
            id: MENUS.EDIT,
            label: LABELS.EDIT,
            isActive: () => {
                return true;
            },
            icon: '',
            order: 2,
            type: MENU_DEF_TYPES.ROOT,
        },
        {
            id: MENUS.UNDO,
            parent: MENUS.EDIT,
            label: LABELS.UNDO,
            isActive: () => {
                const { editor } = plugin.appContext;
                const activeEditor = editor.getActiveEditor();
                if (activeEditor && !_.isNil(activeEditor.undoManager)) {
                    return activeEditor.undoManager.hasUndo();
                }
                return false;
            },
            command: COMMANDS.UNDO,
            icon: 'undo',
            type: MENU_DEF_TYPES.ITEM,
        },
        {
            id: MENUS.REDO,
            parent: MENUS.EDIT,
            label: LABELS.REDO,
            isActive: () => {
                const { editor } = plugin.appContext;
                const activeEditor = editor.getActiveEditor();
                if (activeEditor && !_.isNil(activeEditor.undoManager)) {
                    return activeEditor.undoManager.hasRedo();
                }
                return false;
            },
            command: COMMANDS.REDO,
            icon: 'redo',
            type: MENU_DEF_TYPES.ITEM,
        },
    ];
}
