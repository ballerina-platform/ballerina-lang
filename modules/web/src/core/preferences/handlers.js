import { COMMANDS, DIALOGS } from './constants';
import { COMMANDS as LAYOUT_COMMANDS } from './../layout/constants';

/**
 * Provides command handler definitions of preferences manager plugin.
 *
 * @returns {Object[]} command handler definitions.
 *
 */
export function getHandlerDefinitions(plugin) {
    return [
        {
            cmdID: COMMANDS.SHOW_SETTINGS_DIALOG,
            handler: () => {
                plugin.appContext.command
                    .dispatch(LAYOUT_COMMANDS.SHOW_DIALOG, DIALOGS.PREFERENCES_DIALOG);
            },
        },
    ];
}
