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
            id: COMMANDS.DEBUG,
            shortcut: {
                default: 'f5',
            },
        },
        {
            id: COMMANDS.STOP,
            shortcut: {
                default: 'shift+f5',
            },
        },
    ];
}
