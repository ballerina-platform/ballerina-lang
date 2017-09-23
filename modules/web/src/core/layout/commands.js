import PropTypes from 'prop-types';
import { COMMANDS } from './constants';

/**
 * Provides command definitions of layout manager plugin.
 *
 * @returns {Object[]} command definitions.
 *
 */
export function getCommandDefinitions() {
    return [
        {
            id: COMMANDS.SHOW_VIEW,
            argTypes: {
                id: PropTypes.string.isRequired,
            },
        },
        {
            id: COMMANDS.HIDE_VIEW,
            argTypes: {
                id: PropTypes.string.isRequired,
            },
        },
        {
            id: COMMANDS.POPUP_DIALOG,
            argTypes: {
                id: PropTypes.string.isRequired,
            },
        },
        {
            id: COMMANDS.TOGGLE_BOTTOM_PANEL,
            shortcut: {
                default: 'ctrl+`',
            },
        },
        {
            id: COMMANDS.RE_RENDER_PLUGIN,
            argTypes: {
                id: PropTypes.string.isRequired,
            },
        },
    ];
}
