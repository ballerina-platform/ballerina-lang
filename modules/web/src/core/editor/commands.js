import PropTypes from 'prop-types';
import { COMMANDS } from './constants';

/**
 * Provides command definitions of editor plugin.
 *
 * @returns {Object[]} command definitions.
 *
 */
export function getCommandDefinitions() {
    return [
        {
            id: COMMANDS.OPEN_FILE_IN_EDITOR,
            argTypes: {
                file: PropTypes.objectOf(Object).isRequired,
                editor: PropTypes.objectOf(Object).isRequired,
            },
        },
        {
            id: COMMANDS.OPEN_CUSTOM_EDITOR_TAB,
            argTypes: {
                id: PropTypes.string.isRequired,
                title: PropTypes.string.isRequired,
                icon: PropTypes.string.isRequired,
                component: PropTypes.node.isRequired,
                propsProvider: PropTypes.func.isRequired,
            },
        },
        {
            id: COMMANDS.UNDO,
            shortcut: {
                default: 'ctrl+z',
            },
        },
        {
            id: COMMANDS.REDO,
            shortcut: {
                default: 'ctrl+shift+z',
            },
        },
    ];
}
