import { COMMANDS } from './constants';

/**
 * Provides command handler definitions of alert plugin.
 * @param {Plugin} plugin
 * @returns {Object[]} command handler definitions.
 *
 */
export function getHandlerDefinitions(plugin) {
    return [
        {
            cmdID: COMMANDS.SHOW_ALERT,
            handler: () => {
                plugin.reRender();
            },
        },
    ];
}
