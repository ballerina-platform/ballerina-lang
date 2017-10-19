import { COMMANDS } from './constants';
import { COMMANDS as LAYOUT_COMMANDS } from './../layout/constants';

/**
 * Provides command handler definitions of toolbar plugin.
 * @param {Plugin} plugin
 * @returns {Object[]} command handler definitions.
 *
 */
export function getHandlerDefinitions(plugin) {
    return [
        {
            cmdID: LAYOUT_COMMANDS.UPDATE_ALL_ACTION_TRIGGERS,
            handler: () => {
                plugin.reRender();
            },
        },
        {
            cmdID: COMMANDS.UPDATE_TOOL_BAR,
            handler: () => {
                plugin.reRender();
            },
        },
    ];
}
