import PropTypes from 'prop-types';
import { COMMANDS } from './constants';

/**
 * Provides command definitions of toolbar plugin.
 * No shortcuts for these commands.
 *
 * @returns {Object[]} command definitions.
 *
 */
export function getCommandDefinitions() {
    return [
        {
            id: COMMANDS.SHOW_ALERT,
            argTypes: {
                level: PropTypes.string.isRequired,
                message: PropTypes.string.isRequired,
            },
        },
    ];
}
