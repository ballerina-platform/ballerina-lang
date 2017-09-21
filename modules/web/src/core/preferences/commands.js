import PropTypes from 'prop-types';
import { COMMANDS } from './constants';

/**
 * Provides command definitions of preferences manager plugin.
 *
 * @returns {Object[]} command definitions.
 *
 */
export function getCommandDefinitions() {
    return [
        {
            id: COMMANDS.SHOW_SETTINGS_DIALOG,
            argTypes: {
                id: PropTypes.string.isRequired,
            },
            shortcut: {
                default: 'ctrl+alt+s',
            },
        },
    ];
}
