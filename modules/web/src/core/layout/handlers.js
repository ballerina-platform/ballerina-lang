import { COMMANDS } from './constants';

/**
 * Provides command handler definitions of layout manager plugin.
 * @param {LayoutManager} LayoutManager
 * @returns {Object[]} command handler definitions.
 *
 */
export function getHandlerDefinitions(layoutManager) {
    return [
        {
            cmdID: COMMANDS.SHOW_VIEW,
            handler: (id, region, viewProps) => {
                
            },
        },
        {
            cmdID: COMMANDS.HIDE_VIEW,
            handler: (id) => {
                // TODO
            },
        },
    ];
}
