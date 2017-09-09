import { COMMANDS, DIALOG } from './constants';
import { COMMANDS as LAYOUT_COMMANDS } from './../../core/layout/constants';
/**
 * Provides command handler definitions of debugger plugin.
 * @param {debugger} debugger plugin instance
 * @returns {Object[]} command handler definitions.
 *
 */
export function getHandlerDefinitions(plugin) {
    return [
        {
            cmdID: COMMANDS.SHOW_WELCOME,
            handler: () => {
                // TODO
            },
        },
        {
            cmdID: COMMANDS.OPEN_REFRENCE,
            handler: () => {
                window.open(plugin.config.reference_url);
            },
        },
        {
            cmdID: COMMANDS.REPORT_ISSUE,
            handler: () => {
                window.open(plugin.config.issue_tracker_url);
            },
        },
        {
            cmdID: COMMANDS.SHOW_ABOUT,
            handler: () => {
                console.log(plugin);
                const id = DIALOG.ABOUT;
                plugin.appContext.command.dispatch(LAYOUT_COMMANDS.POPUP_DIALOG, { id });
            },
        },
    ];
}
