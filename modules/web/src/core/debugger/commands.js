import { COMMANDS } from './constants';

/**
 * Provides command definitions of debugger plugin.
 *
 * @returns {Object[]} command definitions.
 *
 */
export function getCommandDefinitions(debuggerInstance) {
    return [
        {
            id: COMMANDS.START_DEBUG,
            shortcut: {
                default: 'f5',
            },
        },
        {
            id: COMMANDS.STOP_DEBUG,
            shortcut: {
                default: 'shift+f5',
            },
        },
    ];
}
