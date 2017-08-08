import { PLUGIN_ID as LAYOUT_MANAGER_PLUGIN_ID } from './core/layout/constants';

export default {
    app: {
        plugins: [],
    },
    // provide plugin specific configs - if any.
    // plugin-id will be the key
    pluginConfigs: {
        [LAYOUT_MANAGER_PLUGIN_ID]: {
            container: 'app-container',
        },
    },
};
