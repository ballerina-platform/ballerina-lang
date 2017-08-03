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
            cmdID: COMMANDS.ADD_VIEW_TO_LAYOUT,
            handler: (view) => {
            },
        },
        {
            cmdID: COMMANDS.REMOVE_VIEW_FROM_LAYOUT,
            handler: (view) => {
                // TODO
            },
        },
    ];
}
