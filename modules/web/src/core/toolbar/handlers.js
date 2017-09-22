import { COMMANDS } from './constants';

/**
 * Provides command handler definitions of toolbar plugin.
 * @param {Plugin} plugin
 * @returns {Object[]} command handler definitions.
 *
 */
export function getHandlerDefinitions(plugin) {
    return [
        {
            cmdID: COMMANDS.UPDATE_TOOL_BAR,
            handler: () => {
                plugin.reRender();
            },
        },
    ];
}
