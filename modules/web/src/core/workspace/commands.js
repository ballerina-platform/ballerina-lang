import PropTypes from 'prop-types';
import { COMMANDS } from './constants';

/**
 * Provides command definitions of workspace plugin.
 *
 * @returns {Object[]} command definitions.
 *
 */
export function getCommandDefinitions() {
    return [
        {
            id: COMMANDS.CREATE_NEW_FILE,
            shortcut: {
                default: 'ctrl+alt+n',
            },
        },
        {
            id: COMMANDS.OPEN_FILE,
            argTypes: {
                filePath: PropTypes.string.isRequired,
                ext: PropTypes.string,
            },
        },
        {
            id: COMMANDS.OPEN_FOLDER,
            argTypes: {
                folderPath: PropTypes.string.isRequired,
            },
        },
        {
            id: COMMANDS.REMOVE_FOLDER,
            argTypes: {
                folderPath: PropTypes.string.isRequired,
            },
        },
        {
            id: COMMANDS.SAVE_FILE,
            shortcut: {
                default: 'ctrl+s',
            },
        },
        {
            id: COMMANDS.SAVE_FILE_AS,
            shortcut: {
                default: 'ctrl+shift+s',
            },
        },
        {
            id: COMMANDS.SHOW_FILE_OPEN_WIZARD,
            shortcut: {
                default: 'ctrl+o',
            },
        },
        {
            id: COMMANDS.SHOW_FOLDER_OPEN_WIZARD,
            shortcut: {
                default: 'ctrl+shift+o',
            },
        },
    ];
}
