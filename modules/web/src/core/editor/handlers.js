import _ from 'lodash';
import { COMMANDS } from './constants';

/**
 * Provides command handler definitions of editor plugin.
 * @param {Plugin} editor plugin
 * @returns {Object[]} command handler definitions.
 *
 */
export function getHandlerDefinitions(editorPlugin) {
    return [
        {
            cmdID: COMMANDS.OPEN_CUSTOM_EDITOR_TAB,
            handler: (args) => {
                editorPlugin.onOpenCustomEditorTab(args);
            },
        },
        {
            cmdID: COMMANDS.OPEN_FILE_IN_EDITOR,
            handler: (args) => {
                editorPlugin.onOpenFileInEditor(args);
            },
        },
        {
            cmdID: COMMANDS.UNDO,
            handler: () => {
                const { editor } = editorPlugin.appContext;
                const activeEditor = editor.getActiveEditor();
                if (activeEditor && !_.isNil(activeEditor.undoManager) && activeEditor.undoManager.hasUndo()) {
                    activeEditor.undoManager.undo();
                }
                editorPlugin.dispatchToolBarUpdate();
            },
        },
        {
            cmdID: COMMANDS.REDO,
            handler: () => {
                const { editor } = editorPlugin.appContext;
                const activeEditor = editor.getActiveEditor();
                if (activeEditor && !_.isNil(activeEditor.undoManager) && activeEditor.undoManager.hasRedo()) {
                    activeEditor.undoManager.redo();
                }
                editorPlugin.dispatchToolBarUpdate();
            },
        },
    ];
}
