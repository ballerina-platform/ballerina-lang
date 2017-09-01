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
    ];
}
