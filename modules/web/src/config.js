import { PLUGIN_ID as LAYOUT_MANAGER_PLUGIN_ID } from './core/layout/constants';
import BallerinaPlugin from './plugins/ballerina/plugin';
import DebuggerPlugin from './plugins/debugger/plugin';
import HelpPlugin from './plugins/help/plugin';
import { PLUGIN_ID as HELP_PLUGIN_ID } from './plugins/help/constants';

export default {
    app: {
        plugins: [
            BallerinaPlugin,
            DebuggerPlugin,
            HelpPlugin,
        ],
    },
    // provide plugin specific configs - if any.
    // plugin-id will be the key
    pluginConfigs: {
        [LAYOUT_MANAGER_PLUGIN_ID]: {
            container: 'app-container',
            dialogContainer: 'dialog-container',
        },
        [HELP_PLUGIN_ID]: {
            issue_tracker_url: 'https://github.com/ballerinalang/composer/issues/',
            reference_url: 'http://ballerinalang.org/docs/user-guide/',
        },
    },
};